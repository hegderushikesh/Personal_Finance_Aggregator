package com.finpilot.recurring.service;

import com.finpilot.account.entity.Account;
import com.finpilot.category.entity.Category;
import com.finpilot.recurring.entity.RecurringFrequency;
import com.finpilot.recurring.entity.RecurringPayment;
import com.finpilot.recurring.entity.RecurringPaymentStatus;
import com.finpilot.transaction.entity.Transaction;
import com.finpilot.transaction.entity.TransactionType;
import com.finpilot.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecurringPaymentDetectorTest {

    private RecurringPaymentDetector detector;
    private MerchantNormalizer normalizer;
    private User testUser;
    private Account account1;
    private Account account2;
    private Category entertainmentCat;
    private Category billsCat;

    @BeforeEach
    void setUp() {
        normalizer = new MerchantNormalizer();
        detector = new RecurringPaymentDetector(normalizer);

        testUser = User.builder().id(1L).email("user@example.com").name("Test User").build();
        account1 = Account.builder().id(101L).name("HDFC Checking").user(testUser).build();
        account2 = Account.builder().id(102L).name("SBI Savings").user(testUser).build();

        entertainmentCat = Category.builder().id(201L).name("Entertainment").build();
        billsCat = Category.builder().id(202L).name("Bills").build();
    }

    private Transaction buildTx(String merchant, BigDecimal amount, LocalDate date, TransactionType type, Account acc, Category cat) {
        return Transaction.builder()
                .user(testUser)
                .merchantName(merchant)
                .amount(amount)
                .transactionDate(date)
                .type(type)
                .account(acc != null ? acc : account1)
                .category(cat != null ? cat : entertainmentCat)
                .build();
    }

    @Test
    @DisplayName("1. Three monthly transactions -> detected")
    void testThreeMonthlyTransactionsDetected() {
        List<Transaction> txs = List.of(
                buildTx("Netflix", new BigDecimal("649.00"), LocalDate.of(2026, 7, 10), TransactionType.EXPENSE, account1, entertainmentCat),
                buildTx("Netflix", new BigDecimal("649.00"), LocalDate.of(2026, 8, 10), TransactionType.EXPENSE, account1, entertainmentCat),
                buildTx("Netflix", new BigDecimal("649.00"), LocalDate.of(2026, 9, 10), TransactionType.EXPENSE, account1, entertainmentCat)
        );

        List<RecurringPayment> result = detector.detect(testUser, txs);
        assertEquals(1, result.size());
        RecurringPayment rp = result.get(0);
        assertEquals("Netflix", rp.getMerchantName());
        assertEquals("netflix", rp.getNormalizedMerchantName());
        assertEquals(RecurringFrequency.MONTHLY, rp.getFrequency());
        assertEquals(new BigDecimal("649.00"), rp.getAverageAmount());
        assertEquals(3, rp.getOccurrenceCount());
        assertEquals(LocalDate.of(2026, 9, 10), rp.getLastTransactionDate());
        assertEquals(31, rp.getIntervalDays());
        assertEquals(LocalDate.of(2026, 10, 11), rp.getNextExpectedDate());
        assertEquals(90, rp.getConfidence());
    }

    @Test
    @DisplayName("2. Three weekly transactions -> detected")
    void testThreeWeeklyTransactionsDetected() {
        List<Transaction> txs = List.of(
                buildTx("Gym Class", new BigDecimal("500.00"), LocalDate.of(2026, 8, 1), TransactionType.EXPENSE, account1, entertainmentCat),
                buildTx("Gym Class", new BigDecimal("500.00"), LocalDate.of(2026, 8, 8), TransactionType.EXPENSE, account1, entertainmentCat),
                buildTx("Gym Class", new BigDecimal("500.00"), LocalDate.of(2026, 8, 15), TransactionType.EXPENSE, account1, entertainmentCat)
        );

        List<RecurringPayment> result = detector.detect(testUser, txs);
        assertEquals(1, result.size());
        assertEquals(RecurringFrequency.WEEKLY, result.get(0).getFrequency());
        assertEquals(7, result.get(0).getIntervalDays());
    }

    @Test
    @DisplayName("3. Three quarterly transactions -> detected")
    void testThreeQuarterlyTransactionsDetected() {
        List<Transaction> txs = List.of(
                buildTx("Water Board", new BigDecimal("1200.00"), LocalDate.of(2026, 1, 1), TransactionType.EXPENSE, account1, billsCat),
                buildTx("Water Board", new BigDecimal("1200.00"), LocalDate.of(2026, 4, 1), TransactionType.EXPENSE, account1, billsCat),
                buildTx("Water Board", new BigDecimal("1200.00"), LocalDate.of(2026, 7, 1), TransactionType.EXPENSE, account1, billsCat)
        );

        List<RecurringPayment> result = detector.detect(testUser, txs);
        assertEquals(1, result.size());
        assertEquals(RecurringFrequency.QUARTERLY, result.get(0).getFrequency());
    }

    @Test
    @DisplayName("4. Three yearly transactions -> detected")
    void testThreeYearlyTransactionsDetected() {
        List<Transaction> txs = List.of(
                buildTx("Amazon Prime", new BigDecimal("1499.00"), LocalDate.of(2024, 1, 15), TransactionType.EXPENSE, account1, entertainmentCat),
                buildTx("Amazon Prime", new BigDecimal("1499.00"), LocalDate.of(2025, 1, 15), TransactionType.EXPENSE, account1, entertainmentCat),
                buildTx("Amazon Prime", new BigDecimal("1499.00"), LocalDate.of(2026, 1, 15), TransactionType.EXPENSE, account1, entertainmentCat)
        );

        List<RecurringPayment> result = detector.detect(testUser, txs);
        assertEquals(1, result.size());
        assertEquals(RecurringFrequency.YEARLY, result.get(0).getFrequency());
    }

    @Test
    @DisplayName("5. Same merchant + different amount within tolerance -> detected")
    void testSameMerchantDifferentAmountWithinTolerance() {
        // Spotify: ₹119, ₹119, ₹129 -> average ~122.33, diff is small, tolerance max(50, 12.23) = 50.
        List<Transaction> txs = List.of(
                buildTx("Spotify", new BigDecimal("119.00"), LocalDate.of(2026, 7, 5), TransactionType.EXPENSE, account1, entertainmentCat),
                buildTx("Spotify", new BigDecimal("119.00"), LocalDate.of(2026, 8, 5), TransactionType.EXPENSE, account1, entertainmentCat),
                buildTx("Spotify", new BigDecimal("129.00"), LocalDate.of(2026, 9, 5), TransactionType.EXPENSE, account1, entertainmentCat)
        );

        List<RecurringPayment> result = detector.detect(testUser, txs);
        assertEquals(1, result.size());
        assertEquals("Spotify", result.get(0).getMerchantName());
        assertEquals(new BigDecimal("122.33"), result.get(0).getAverageAmount());
        assertEquals(new BigDecimal("129.00"), result.get(0).getLastAmount());
    }

    @Test
    @DisplayName("6. Same merchant + amount too different -> not detected")
    void testSameMerchantAmountTooDifferentNotDetected() {
        // Amazon: 500, 3500, 800 -> variance too high
        List<Transaction> txs = List.of(
                buildTx("Amazon", new BigDecimal("500.00"), LocalDate.of(2026, 1, 5), TransactionType.EXPENSE, account1, entertainmentCat),
                buildTx("Amazon", new BigDecimal("3500.00"), LocalDate.of(2026, 2, 5), TransactionType.EXPENSE, account1, entertainmentCat),
                buildTx("Amazon", new BigDecimal("800.00"), LocalDate.of(2026, 3, 5), TransactionType.EXPENSE, account1, entertainmentCat)
        );

        List<RecurringPayment> result = detector.detect(testUser, txs);
        assertTrue(result.isEmpty(), "Amazon with excessive variance should not be detected");
    }

    @Test
    @DisplayName("7. Irregular intervals -> not detected")
    void testIrregularIntervalsNotDetected() {
        // 7 days then 90 days
        List<Transaction> txs = List.of(
                buildTx("Vendor", new BigDecimal("500.00"), LocalDate.of(2026, 1, 1), TransactionType.EXPENSE, account1, billsCat),
                buildTx("Vendor", new BigDecimal("500.00"), LocalDate.of(2026, 1, 8), TransactionType.EXPENSE, account1, billsCat),
                buildTx("Vendor", new BigDecimal("500.00"), LocalDate.of(2026, 4, 8), TransactionType.EXPENSE, account1, billsCat)
        );

        List<RecurringPayment> result = detector.detect(testUser, txs);
        assertTrue(result.isEmpty(), "Transactions with irregular intervals should not be detected");
    }

    @Test
    @DisplayName("8. Only two transactions -> not detected")
    void testOnlyTwoTransactionsNotDetected() {
        List<Transaction> txs = List.of(
                buildTx("Netflix", new BigDecimal("649.00"), LocalDate.of(2026, 7, 10), TransactionType.EXPENSE, account1, entertainmentCat),
                buildTx("Netflix", new BigDecimal("649.00"), LocalDate.of(2026, 8, 10), TransactionType.EXPENSE, account1, entertainmentCat)
        );

        List<RecurringPayment> result = detector.detect(testUser, txs);
        assertTrue(result.isEmpty(), "Fewer than 3 transactions should not be detected");
    }

    @Test
    @DisplayName("9. Income transactions ignored")
    void testIncomeTransactionsIgnored() {
        List<Transaction> txs = List.of(
                buildTx("Salary", new BigDecimal("50000.00"), LocalDate.of(2026, 7, 1), TransactionType.INCOME, account1, null),
                buildTx("Salary", new BigDecimal("50000.00"), LocalDate.of(2026, 8, 1), TransactionType.INCOME, account1, null),
                buildTx("Salary", new BigDecimal("50000.00"), LocalDate.of(2026, 9, 1), TransactionType.INCOME, account1, null)
        );

        List<RecurringPayment> result = detector.detect(testUser, txs);
        assertTrue(result.isEmpty(), "INCOME transactions should be ignored");
    }

    @Test
    @DisplayName("10. Transfer transactions ignored")
    void testTransferTransactionsIgnored() {
        List<Transaction> txs = List.of(
                buildTx("Self Transfer", new BigDecimal("5000.00"), LocalDate.of(2026, 7, 1), TransactionType.TRANSFER, account1, null),
                buildTx("Self Transfer", new BigDecimal("5000.00"), LocalDate.of(2026, 8, 1), TransactionType.TRANSFER, account1, null),
                buildTx("Self Transfer", new BigDecimal("5000.00"), LocalDate.of(2026, 9, 1), TransactionType.TRANSFER, account1, null)
        );

        List<RecurringPayment> result = detector.detect(testUser, txs);
        assertTrue(result.isEmpty(), "TRANSFER transactions should be ignored");
    }

    @Test
    @DisplayName("11. Null/empty merchant ignored")
    void testNullMerchantIgnored() {
        List<Transaction> txs = List.of(
                buildTx(null, new BigDecimal("100.00"), LocalDate.of(2026, 7, 1), TransactionType.EXPENSE, account1, null),
                buildTx("", new BigDecimal("100.00"), LocalDate.of(2026, 8, 1), TransactionType.EXPENSE, account1, null),
                buildTx("   ", new BigDecimal("100.00"), LocalDate.of(2026, 9, 1), TransactionType.EXPENSE, account1, null)
        );

        List<RecurringPayment> result = detector.detect(testUser, txs);
        assertTrue(result.isEmpty(), "Null or empty merchant transactions should be ignored");
    }

    @Test
    @DisplayName("12. Merchant case differences normalized")
    void testMerchantCaseDifferencesNormalized() {
        List<Transaction> txs = List.of(
                buildTx("Netflix", new BigDecimal("649.00"), LocalDate.of(2026, 7, 10), TransactionType.EXPENSE, account1, entertainmentCat),
                buildTx("NETFLIX", new BigDecimal("649.00"), LocalDate.of(2026, 8, 10), TransactionType.EXPENSE, account1, entertainmentCat),
                buildTx("netflix", new BigDecimal("649.00"), LocalDate.of(2026, 9, 10), TransactionType.EXPENSE, account1, entertainmentCat)
        );

        List<RecurringPayment> result = detector.detect(testUser, txs);
        assertEquals(1, result.size());
        assertEquals("netflix", result.get(0).getNormalizedMerchantName());
    }

    @Test
    @DisplayName("13. Website suffix normalization (.com, etc.)")
    void testWebsiteSuffixNormalization() {
        List<Transaction> txs = List.of(
                buildTx("Netflix", new BigDecimal("649.00"), LocalDate.of(2026, 7, 10), TransactionType.EXPENSE, account1, entertainmentCat),
                buildTx("Netflix.com", new BigDecimal("649.00"), LocalDate.of(2026, 8, 10), TransactionType.EXPENSE, account1, entertainmentCat),
                buildTx("NETFLIX.COM", new BigDecimal("649.00"), LocalDate.of(2026, 9, 10), TransactionType.EXPENSE, account1, entertainmentCat)
        );

        List<RecurringPayment> result = detector.detect(testUser, txs);
        assertEquals(1, result.size());
        assertEquals("netflix", result.get(0).getNormalizedMerchantName());
    }

    @Test
    @DisplayName("14. Four+ transactions increase confidence to 100")
    void testFourPlusTransactionsIncreaseConfidence() {
        List<Transaction> txs = List.of(
                buildTx("Netflix", new BigDecimal("649.00"), LocalDate.of(2026, 6, 10), TransactionType.EXPENSE, account1, entertainmentCat),
                buildTx("Netflix", new BigDecimal("649.00"), LocalDate.of(2026, 7, 10), TransactionType.EXPENSE, account1, entertainmentCat),
                buildTx("Netflix", new BigDecimal("649.00"), LocalDate.of(2026, 8, 10), TransactionType.EXPENSE, account1, entertainmentCat),
                buildTx("Netflix", new BigDecimal("649.00"), LocalDate.of(2026, 9, 10), TransactionType.EXPENSE, account1, entertainmentCat)
        );

        List<RecurringPayment> result = detector.detect(testUser, txs);
        assertEquals(1, result.size());
        assertEquals(4, result.get(0).getOccurrenceCount());
        assertEquals(100, result.get(0).getConfidence());
    }

    @Test
    @DisplayName("15. Multiple accounts for same merchant handled")
    void testMultipleAccountsForSameMerchantHandled() {
        List<Transaction> txs = List.of(
                buildTx("Netflix", new BigDecimal("649.00"), LocalDate.of(2026, 7, 10), TransactionType.EXPENSE, account1, entertainmentCat),
                buildTx("Netflix", new BigDecimal("649.00"), LocalDate.of(2026, 8, 10), TransactionType.EXPENSE, account1, entertainmentCat),
                buildTx("Netflix", new BigDecimal("649.00"), LocalDate.of(2026, 9, 10), TransactionType.EXPENSE, account2, entertainmentCat)
        );

        List<RecurringPayment> result = detector.detect(testUser, txs);
        assertEquals(1, result.size());
        assertNull(result.get(0).getAccount(), "Account should be null when transactions span multiple accounts");
    }

    @Test
    @DisplayName("16. Different categories handled (majority category selected)")
    void testDifferentCategoriesHandled() {
        List<Transaction> txs = List.of(
                buildTx("Netflix", new BigDecimal("649.00"), LocalDate.of(2026, 7, 10), TransactionType.EXPENSE, account1, entertainmentCat),
                buildTx("Netflix", new BigDecimal("649.00"), LocalDate.of(2026, 8, 10), TransactionType.EXPENSE, account1, billsCat),
                buildTx("Netflix", new BigDecimal("649.00"), LocalDate.of(2026, 9, 10), TransactionType.EXPENSE, account1, entertainmentCat)
        );

        List<RecurringPayment> result = detector.detect(testUser, txs);
        assertEquals(1, result.size());
        assertEquals(entertainmentCat.getId(), result.get(0).getCategory().getId(), "Most frequent category should be selected");
    }

    @Test
    @DisplayName("17. Next expected date calculated correctly")
    void testNextExpectedDateCalculatedCorrectly() {
        // Last tx is Sep 10, interval is 31 days -> next expected is Oct 11
        List<Transaction> txs = List.of(
                buildTx("Internet", new BigDecimal("999.00"), LocalDate.of(2026, 7, 15), TransactionType.EXPENSE, account1, billsCat),
                buildTx("Internet", new BigDecimal("999.00"), LocalDate.of(2026, 8, 15), TransactionType.EXPENSE, account1, billsCat),
                buildTx("Internet", new BigDecimal("999.00"), LocalDate.of(2026, 9, 15), TransactionType.EXPENSE, account1, billsCat)
        );

        List<RecurringPayment> result = detector.detect(testUser, txs);
        assertEquals(1, result.size());
        RecurringPayment rp = result.get(0);
        assertEquals(LocalDate.of(2026, 9, 15), rp.getLastTransactionDate());
        assertEquals(rp.getLastTransactionDate().plusDays(rp.getIntervalDays()), rp.getNextExpectedDate());
    }
}
