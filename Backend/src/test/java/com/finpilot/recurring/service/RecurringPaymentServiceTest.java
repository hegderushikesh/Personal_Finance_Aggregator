package com.finpilot.recurring.service;

import com.finpilot.account.entity.Account;
import com.finpilot.category.entity.Category;
import com.finpilot.category.repository.CategoryRepository;
import com.finpilot.recurring.dto.DetectRecurringRequest;
import com.finpilot.recurring.dto.DetectRecurringResponse;
import com.finpilot.recurring.dto.RecurringPaymentResponse;
import com.finpilot.recurring.entity.RecurringFrequency;
import com.finpilot.recurring.entity.RecurringPayment;
import com.finpilot.recurring.entity.RecurringPaymentStatus;
import com.finpilot.recurring.repository.RecurringPaymentRepository;
import com.finpilot.transaction.entity.Transaction;
import com.finpilot.transaction.entity.TransactionType;
import com.finpilot.transaction.repository.TransactionRepository;
import com.finpilot.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecurringPaymentServiceTest {

    @Mock
    private RecurringPaymentRepository recurringPaymentRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Spy
    private MerchantNormalizer merchantNormalizer = new MerchantNormalizer();

    @Spy
    private RecurringPaymentDetector recurringPaymentDetector = new RecurringPaymentDetector(new MerchantNormalizer());

    @InjectMocks
    private RecurringPaymentService recurringPaymentService;

    private User user1;
    private User user2;
    private Account account1;
    private Category category1;

    @BeforeEach
    void setUp() {
        user1 = User.builder().id(1L).email("user1@example.com").name("User One").build();
        user2 = User.builder().id(2L).email("user2@example.com").name("User Two").build();
        account1 = Account.builder().id(10L).name("Account 1").user(user1).build();
        category1 = Category.builder().id(20L).name("Entertainment").user(user1).build();
    }

    private Transaction buildTx(String merchant, BigDecimal amount, LocalDate date) {
        return Transaction.builder()
                .user(user1)
                .merchantName(merchant)
                .amount(amount)
                .transactionDate(date)
                .type(TransactionType.EXPENSE)
                .account(account1)
                .category(category1)
                .build();
    }

    @Test
    @DisplayName("18. Duplicate recurring records prevented")
    void testDuplicateRecurringRecordsPrevented() {
        List<Transaction> txs = List.of(
                buildTx("Netflix", new BigDecimal("649.00"), LocalDate.of(2026, 7, 10)),
                buildTx("Netflix", new BigDecimal("649.00"), LocalDate.of(2026, 8, 10)),
                buildTx("Netflix", new BigDecimal("649.00"), LocalDate.of(2026, 9, 10))
        );

        when(transactionRepository.findAll(any(Specification.class))).thenReturn(txs);
        when(recurringPaymentRepository.findByUserAndNormalizedMerchantNameAndFrequency(user1, "netflix", RecurringFrequency.MONTHLY))
                .thenReturn(Optional.empty());
        when(recurringPaymentRepository.save(any(RecurringPayment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DetectRecurringResponse response = recurringPaymentService.detectRecurringPayments(user1, new DetectRecurringRequest());
        assertEquals(1, response.getDetected());
        verify(recurringPaymentRepository, times(1)).save(any(RecurringPayment.class));
    }

    @Test
    @DisplayName("19. Re-running detection updates existing record")
    void testReRunningDetectionUpdatesExistingRecord() {
        List<Transaction> txs = List.of(
                buildTx("Netflix", new BigDecimal("649.00"), LocalDate.of(2026, 7, 10)),
                buildTx("Netflix", new BigDecimal("649.00"), LocalDate.of(2026, 8, 10)),
                buildTx("Netflix", new BigDecimal("649.00"), LocalDate.of(2026, 9, 10))
        );

        when(transactionRepository.findAll(any(Specification.class))).thenReturn(txs);

        RecurringPayment existing = RecurringPayment.builder()
                .id(99L)
                .user(user1)
                .merchantName("Netflix")
                .normalizedMerchantName("netflix")
                .frequency(RecurringFrequency.MONTHLY)
                .averageAmount(new BigDecimal("600.00"))
                .lastAmount(new BigDecimal("600.00"))
                .intervalDays(30)
                .nextExpectedDate(LocalDate.of(2026, 9, 10))
                .lastTransactionDate(LocalDate.of(2026, 8, 10))
                .occurrenceCount(2)
                .status(RecurringPaymentStatus.ACTIVE)
                .confidence(60)
                .build();

        when(recurringPaymentRepository.findByUserAndNormalizedMerchantNameAndFrequency(user1, "netflix", RecurringFrequency.MONTHLY))
                .thenReturn(Optional.of(existing));
        when(recurringPaymentRepository.save(any(RecurringPayment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DetectRecurringResponse response = recurringPaymentService.detectRecurringPayments(user1, new DetectRecurringRequest());

        assertEquals(1, response.getDetected());
        RecurringPaymentResponse item = response.getRecurringPayments().get(0);
        assertEquals(99L, item.getId(), "Should update existing record with ID 99 rather than creating new one");
        assertEquals(new BigDecimal("649.00"), item.getAverageAmount());
        assertEquals(3, item.getOccurrenceCount());

        verify(recurringPaymentRepository, times(1)).save(existing);
    }

    @Test
    @DisplayName("20. User ownership enforced")
    void testUserOwnershipEnforced() {
        when(recurringPaymentRepository.findByIdAndUser(100L, user2))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
                recurringPaymentService.getRecurringPaymentById(user2, 100L)
        );

        assertThrows(ResponseStatusException.class, () ->
                recurringPaymentService.deleteRecurringPayment(user2, 100L)
        );
    }
}
