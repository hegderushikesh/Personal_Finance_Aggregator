package com.finpilot.plaid.service;

import com.finpilot.account.entity.Account;
import com.finpilot.account.entity.AccountSource;
import com.finpilot.account.entity.AccountType;
import com.finpilot.account.repository.AccountRepository;
import com.finpilot.budget.service.BudgetTransactionService;
import com.finpilot.category.entity.Category;
import com.finpilot.plaid.dto.SyncResponse;
import com.finpilot.plaid.entity.PlaidConnection;
import com.finpilot.plaid.entity.PlaidConnectionStatus;
import com.finpilot.plaid.repository.PlaidConnectionRepository;
import com.finpilot.transaction.entity.Transaction;
import com.finpilot.transaction.entity.TransactionSource;
import com.finpilot.transaction.entity.TransactionType;
import com.finpilot.transaction.repository.TransactionRepository;
import com.finpilot.user.entity.AuthProvider;
import com.finpilot.user.entity.User;
import com.plaid.client.model.*;
import com.plaid.client.request.PlaidApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaidTransactionSyncServiceTest {

    @Mock private PlaidApi plaidApi;
    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private PlaidCategoryMapper plaidCategoryMapper;
    @Mock private BudgetTransactionService budgetTransactionService;
    @Mock private PlaidConnectionRepository plaidConnectionRepository;

    @InjectMocks
    private PlaidTransactionSyncService plaidTransactionSyncService;

    private User user;
    private PlaidConnection connection;
    private Account account;
    private Category foodCategory;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("user@example.com").authProvider(AuthProvider.LOCAL).build();

        connection = PlaidConnection.builder()
                .id(10L)
                .user(user)
                .itemId("item_abc")
                .accessToken("access_token_123")
                .cursor("cursor_old")
                .status(PlaidConnectionStatus.ACTIVE)
                .build();

        account = Account.builder()
                .id(100L)
                .user(user)
                .name("Checking")
                .type(AccountType.CHECKING)
                .balance(new BigDecimal("10000.00"))
                .source(AccountSource.PLAID)
                .externalAccountId("acc_ext_1")
                .plaidConnectionId(10L)
                .build();

        foodCategory = Category.builder()
                .id(50L)
                .user(user)
                .name("Food")
                .build();
    }

    @Test
    @DisplayName("1. Plaid positive amount maps to EXPENSE and reduces account balance")
    void testSyncTransactions_Expense() throws IOException {
        com.plaid.client.model.Transaction plaidTx = new com.plaid.client.model.Transaction();
        plaidTx.setTransactionId("tx_1");
        plaidTx.setAccountId("acc_ext_1");
        plaidTx.setAmount(450.0); // Plaid positive = EXPENSE
        plaidTx.setDate(LocalDate.of(2026, 9, 10));
        plaidTx.setName("Swiggy");
        plaidTx.setMerchantName("Swiggy");
        plaidTx.setPending(false);

        TransactionsSyncResponse mockBody = new TransactionsSyncResponse();
        mockBody.setAdded(List.of(plaidTx));
        mockBody.setModified(Collections.emptyList());
        mockBody.setRemoved(Collections.emptyList());
        mockBody.setNextCursor("cursor_new");
        mockBody.setHasMore(false);

        Call<TransactionsSyncResponse> mockCall = mock(Call.class);
        when(mockCall.execute()).thenReturn(Response.success(mockBody));
        when(plaidApi.transactionsSync(any())).thenReturn(mockCall);

        when(accountRepository.findByExternalAccountIdAndUser("acc_ext_1", user))
                .thenReturn(Optional.of(account));
        when(transactionRepository.findByPlaidConnectionIdAndExternalTransactionId(10L, "tx_1"))
                .thenReturn(Optional.empty());
        when(plaidCategoryMapper.mapToCategory(eq(user), any(), any()))
                .thenReturn(foodCategory);

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        SyncResponse response = plaidTransactionSyncService.syncTransactions(user, connection);

        assertEquals(1, response.getAdded());
        assertEquals(0, response.getModified());
        assertEquals(0, response.getRemoved());

        // Verify balance reduced: 10000 - 450 = 9550
        assertEquals(0, new BigDecimal("9550.00").compareTo(account.getBalance()));
        verify(accountRepository).save(account);

        // Verify budget activity recalculation was called
        verify(budgetTransactionService).recalculateCategoryActivity(user, foodCategory, 2026, 9);

        // Verify cursor was updated
        assertEquals("cursor_new", connection.getCursor());
        verify(plaidConnectionRepository).save(connection);
    }

    @Test
    @DisplayName("2. Plaid negative amount maps to INCOME and increases account balance")
    void testSyncTransactions_Income() throws IOException {
        com.plaid.client.model.Transaction plaidTx = new com.plaid.client.model.Transaction();
        plaidTx.setTransactionId("tx_2");
        plaidTx.setAccountId("acc_ext_1");
        plaidTx.setAmount(-50000.0); // Plaid negative = INCOME
        plaidTx.setDate(LocalDate.of(2026, 9, 1));
        plaidTx.setName("Salary Deposit");
        plaidTx.setPending(false);

        TransactionsSyncResponse mockBody = new TransactionsSyncResponse();
        mockBody.setAdded(List.of(plaidTx));
        mockBody.setModified(Collections.emptyList());
        mockBody.setRemoved(Collections.emptyList());
        mockBody.setNextCursor("cursor_new_2");
        mockBody.setHasMore(false);

        Call<TransactionsSyncResponse> mockCall = mock(Call.class);
        when(mockCall.execute()).thenReturn(Response.success(mockBody));
        when(plaidApi.transactionsSync(any())).thenReturn(mockCall);

        when(accountRepository.findByExternalAccountIdAndUser("acc_ext_1", user))
                .thenReturn(Optional.of(account));
        when(transactionRepository.findByPlaidConnectionIdAndExternalTransactionId(10L, "tx_2"))
                .thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        SyncResponse response = plaidTransactionSyncService.syncTransactions(user, connection);

        assertEquals(1, response.getAdded());
        // Balance increased: 10000 + 50000 = 60000
        assertEquals(0, new BigDecimal("60000.00").compareTo(account.getBalance()));
    }

    @Test
    @DisplayName("3. Duplicate protection: previously seen transaction is updated, never duplicated")
    void testSyncTransactions_DuplicateProtection() throws IOException {
        com.plaid.client.model.Transaction plaidTx = new com.plaid.client.model.Transaction();
        plaidTx.setTransactionId("tx_existing");
        plaidTx.setAccountId("acc_ext_1");
        plaidTx.setAmount(120.0);
        plaidTx.setDate(LocalDate.of(2026, 9, 5));
        plaidTx.setName("Coffee");

        TransactionsSyncResponse mockBody = new TransactionsSyncResponse();
        mockBody.setAdded(List.of(plaidTx));
        mockBody.setModified(Collections.emptyList());
        mockBody.setRemoved(Collections.emptyList());
        mockBody.setNextCursor("cursor_after_dup");
        mockBody.setHasMore(false);

        Call<TransactionsSyncResponse> mockCall = mock(Call.class);
        when(mockCall.execute()).thenReturn(Response.success(mockBody));
        when(plaidApi.transactionsSync(any())).thenReturn(mockCall);

        Transaction existingRecord = Transaction.builder()
                .id(999L)
                .user(user)
                .account(account)
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.EXPENSE)
                .transactionDate(LocalDate.of(2026, 9, 5))
                .externalTransactionId("tx_existing")
                .plaidConnectionId(10L)
                .build();

        when(transactionRepository.findByPlaidConnectionIdAndExternalTransactionId(10L, "tx_existing"))
                .thenReturn(Optional.of(existingRecord));

        SyncResponse response = plaidTransactionSyncService.syncTransactions(user, connection);

        // Should count as modified instead of added
        assertEquals(0, response.getAdded());
        assertEquals(1, response.getModified());
        verify(transactionRepository, never()).save(argThat(tx -> tx != existingRecord));
    }

    @Test
    @DisplayName("4. Removed transaction reverts balance and is deleted from repository")
    void testSyncTransactions_Removed() throws IOException {
        RemovedTransaction rt = new RemovedTransaction();
        rt.setTransactionId("tx_to_remove");

        TransactionsSyncResponse mockBody = new TransactionsSyncResponse();
        mockBody.setAdded(Collections.emptyList());
        mockBody.setModified(Collections.emptyList());
        mockBody.setRemoved(List.of(rt));
        mockBody.setNextCursor("cursor_after_remove");
        mockBody.setHasMore(false);

        Call<TransactionsSyncResponse> mockCall = mock(Call.class);
        when(mockCall.execute()).thenReturn(Response.success(mockBody));
        when(plaidApi.transactionsSync(any())).thenReturn(mockCall);

        Transaction existingRecord = Transaction.builder()
                .id(888L)
                .user(user)
                .account(account)
                .amount(new BigDecimal("500.00"))
                .type(TransactionType.EXPENSE)
                .category(foodCategory)
                .transactionDate(LocalDate.of(2026, 9, 7))
                .externalTransactionId("tx_to_remove")
                .plaidConnectionId(10L)
                .build();

        when(transactionRepository.findByPlaidConnectionIdAndExternalTransactionId(10L, "tx_to_remove"))
                .thenReturn(Optional.of(existingRecord));

        SyncResponse response = plaidTransactionSyncService.syncTransactions(user, connection);

        assertEquals(1, response.getRemoved());
        // Balance reverted: 10000 + 500 = 10500
        assertEquals(new BigDecimal("10500.00"), account.getBalance());
        verify(transactionRepository).delete(existingRecord);
        verify(budgetTransactionService).recalculateCategoryActivity(user, foodCategory, 2026, 9);
    }
}
