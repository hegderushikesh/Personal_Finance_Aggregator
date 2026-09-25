package com.finpilot.account.service;

import com.finpilot.account.dto.AccountResponse;
import com.finpilot.account.dto.AccountSummaryResponse;
import com.finpilot.account.dto.CreateAccountRequest;
import com.finpilot.account.dto.UpdateAccountRequest;
import com.finpilot.account.entity.Account;
import com.finpilot.account.entity.AccountType;
import com.finpilot.account.repository.AccountRepository;
import com.finpilot.transaction.repository.TransactionRepository;
import com.finpilot.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AccountService accountService;

    private User user1;
    private User user2;
    private Account checkingAccount;
    private Account savingsAccount;
    private Account creditCardAccount;

    @BeforeEach
    void setUp() {
        user1 = User.builder().id(1L).email("user1@example.com").name("User One").build();
        user2 = User.builder().id(2L).email("user2@example.com").name("User Two").build();

        checkingAccount = Account.builder()
                .id(101L)
                .user(user1)
                .name("HDFC Checking")
                .type(AccountType.CHECKING)
                .balance(new BigDecimal("25000.00"))
                .institutionName("HDFC Bank")
                .accountNumberLast4("4821")
                .currency("INR")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        savingsAccount = Account.builder()
                .id(102L)
                .user(user1)
                .name("SBI Savings")
                .type(AccountType.SAVINGS)
                .balance(new BigDecimal("50000.00"))
                .institutionName("SBI")
                .accountNumberLast4("9210")
                .currency("INR")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        creditCardAccount = Account.builder()
                .id(103L)
                .user(user1)
                .name("HDFC Credit Card")
                .type(AccountType.CREDIT_CARD)
                .balance(new BigDecimal("10000.00"))
                .institutionName("HDFC Bank")
                .accountNumberLast4("7812")
                .currency("INR")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create account with initial balance and details")
    void createAccount_Success() {
        CreateAccountRequest request = CreateAccountRequest.builder()
                .name("HDFC Checking")
                .type(AccountType.CHECKING)
                .balance(new BigDecimal("25000.00"))
                .institutionName("HDFC Bank")
                .accountNumberLast4("4821")
                .currency("INR")
                .build();

        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
            Account a = inv.getArgument(0);
            a.setId(101L);
            return a;
        });

        AccountResponse response = accountService.createAccount(user1, request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(101L);
        assertThat(response.getName()).isEqualTo("HDFC Checking");
        assertThat(response.getType()).isEqualTo(AccountType.CHECKING);
        assertThat(response.getBalance()).isEqualByComparingTo("25000.00");
        assertThat(response.getInstitutionName()).isEqualTo("HDFC Bank");
        assertThat(response.getAccountNumberLast4()).isEqualTo("4821");
        assertThat(response.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should reject account creation with negative initial balance")
    void createAccount_NegativeBalance_ThrowsBadRequest() {
        CreateAccountRequest request = CreateAccountRequest.builder()
                .name("Invalid Account")
                .type(AccountType.CHECKING)
                .balance(new BigDecimal("-100.00"))
                .build();

        assertThatThrownBy(() -> accountService.createAccount(user1, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should get all accounts for user sorted")
    void getAccounts_Success() {
        when(accountRepository.findByUserOrderByIsActiveDescTypeAscNameAsc(user1))
                .thenReturn(List.of(checkingAccount, savingsAccount));

        List<AccountResponse> result = accountService.getAccounts(user1);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("HDFC Checking");
        assertThat(result.get(1).getName()).isEqualTo("SBI Savings");
    }

    @Test
    @DisplayName("Should get account by ID for owner")
    void getAccountById_Success() {
        when(accountRepository.findByIdAndUser(101L, user1)).thenReturn(Optional.of(checkingAccount));

        AccountResponse response = accountService.getAccountById(101L, user1);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(101L);
        assertThat(response.getName()).isEqualTo("HDFC Checking");
    }

    @Test
    @DisplayName("Should throw 404 when getting account belonging to another user")
    void getAccountById_OtherUser_ThrowsNotFound() {
        when(accountRepository.findByIdAndUser(101L, user2)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccountById(101L, user2))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should update account details while preserving balance")
    void updateAccount_Success_PreservesBalance() {
        when(accountRepository.findByIdAndUser(101L, user1)).thenReturn(Optional.of(checkingAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateAccountRequest request = UpdateAccountRequest.builder()
                .name("HDFC Salary Account")
                .institutionName("HDFC Bank Ltd")
                .accountNumberLast4("9999")
                .isActive(true)
                .build();

        AccountResponse updated = accountService.updateAccount(101L, user1, request);

        assertThat(updated.getName()).isEqualTo("HDFC Salary Account");
        assertThat(updated.getInstitutionName()).isEqualTo("HDFC Bank Ltd");
        assertThat(updated.getAccountNumberLast4()).isEqualTo("9999");
        assertThat(updated.getBalance()).isEqualByComparingTo("25000.00");
    }

    @Test
    @DisplayName("Should delete account when no transactions exist")
    void deleteAccount_NoTransactions_Success() {
        when(accountRepository.findByIdAndUser(101L, user1)).thenReturn(Optional.of(checkingAccount));
        when(transactionRepository.existsByAccount(checkingAccount)).thenReturn(false);

        accountService.deleteAccount(101L, user1);

        verify(accountRepository, times(1)).delete(checkingAccount);
    }

    @Test
    @DisplayName("Should prevent deleting account that has transactions and return 409 Conflict")
    void deleteAccount_HasTransactions_ThrowsConflict() {
        when(accountRepository.findByIdAndUser(101L, user1)).thenReturn(Optional.of(checkingAccount));
        when(transactionRepository.existsByAccount(checkingAccount)).thenReturn(true);

        assertThatThrownBy(() -> accountService.deleteAccount(101L, user1))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT)
                .hasMessageContaining("Cannot delete an account that contains transactions");

        verify(accountRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should compute account summary: Total Cash, Savings, Credit, and Net Worth")
    void getAccountSummary_ComputesCorrectly() {
        when(accountRepository.findByUserOrderByIsActiveDescTypeAscNameAsc(user1))
                .thenReturn(List.of(checkingAccount, savingsAccount, creditCardAccount));

        AccountSummaryResponse summary = accountService.getAccountSummary(user1);

        assertThat(summary).isNotNull();
        // totalCash: Checking (25,000)
        assertThat(summary.getTotalCash()).isEqualByComparingTo("25000.00");
        // totalSavings: Savings (50,000)
        assertThat(summary.getTotalSavings()).isEqualByComparingTo("50000.00");
        // totalCredit: Credit Card (10,000)
        assertThat(summary.getTotalCredit()).isEqualByComparingTo("10000.00");
        // netWorth: 25000 + 50000 - 10000 = 65000
        assertThat(summary.getNetWorth()).isEqualByComparingTo("65000.00");
    }

    @Test
    @DisplayName("Should exclude inactive accounts from summary calculations")
    void getAccountSummary_ExcludesInactiveAccounts() {
        creditCardAccount.setIsActive(false);
        when(accountRepository.findByUserOrderByIsActiveDescTypeAscNameAsc(user1))
                .thenReturn(List.of(checkingAccount, savingsAccount, creditCardAccount));

        AccountSummaryResponse summary = accountService.getAccountSummary(user1);

        assertThat(summary.getTotalCash()).isEqualByComparingTo("25000.00");
        assertThat(summary.getTotalSavings()).isEqualByComparingTo("50000.00");
        assertThat(summary.getTotalCredit()).isEqualByComparingTo("0.00");
        assertThat(summary.getNetWorth()).isEqualByComparingTo("75000.00");
    }
}
