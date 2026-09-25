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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public List<AccountResponse> getAccounts(User user) {
        List<Account> accounts = accountRepository.findByUserOrderByIsActiveDescTypeAscNameAsc(user);
        if (accounts.isEmpty()) {
            accounts = createDefaultAccountsIfEmpty(user);
        }
        return accounts.stream().map(AccountResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long id, User user) {
        Account account = getAccountEntity(id, user);
        return AccountResponse.from(account);
    }

    @Transactional(readOnly = true)
    public Account getAccountEntity(Long id, User user) {
        return accountRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found or access denied"));
    }

    @Transactional
    public AccountResponse createAccount(User user, CreateAccountRequest request) {
        if (request.getBalance() != null && request.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Initial balance cannot be negative");
        }

        BigDecimal balance = request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO;
        String currency = (request.getCurrency() != null && !request.getCurrency().isBlank()) 
                ? request.getCurrency().trim() 
                : "INR";

        Account account = Account.builder()
                .user(user)
                .name(request.getName().trim())
                .type(request.getType() != null ? request.getType() : AccountType.CHECKING)
                .balance(balance)
                .institutionName(request.getInstitutionName() != null ? request.getInstitutionName().trim() : null)
                .accountNumberLast4(request.getAccountNumberLast4() != null ? request.getAccountNumberLast4().trim() : null)
                .currency(currency)
                .isActive(true)
                .build();

        Account saved = accountRepository.save(account);
        log.info("Created account '{}' (ID: {}) for user ID {}", saved.getName(), saved.getId(), user.getId());
        return AccountResponse.from(saved);
    }

    @Transactional
    public AccountResponse updateAccount(Long id, User user, UpdateAccountRequest request) {
        Account account = getAccountEntity(id, user);

        if (request.getName() != null && !request.getName().isBlank()) {
            account.setName(request.getName().trim());
        }
        if (request.getType() != null) {
            account.setType(request.getType());
        }
        if (request.getInstitutionName() != null) {
            account.setInstitutionName(request.getInstitutionName().trim());
        }
        if (request.getAccountNumberLast4() != null) {
            account.setAccountNumberLast4(request.getAccountNumberLast4().trim());
        }
        if (request.getCurrency() != null && !request.getCurrency().isBlank()) {
            account.setCurrency(request.getCurrency().trim());
        }
        if (request.getIsActive() != null) {
            account.setIsActive(request.getIsActive());
        }

        Account updated = accountRepository.save(account);
        log.info("Updated account ID {} for user ID {}", id, user.getId());
        return AccountResponse.from(updated);
    }

    @Transactional
    public void deleteAccount(Long id, User user) {
        Account account = getAccountEntity(id, user);

        if (transactionRepository.existsByAccount(account)) {
            log.warn("Cannot delete account ID {} because it has associated transactions", id);
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete an account that contains transactions. Deactivate the account instead."
            );
        }

        accountRepository.delete(account);
        log.info("Deleted account ID {} for user ID {}", id, user.getId());
    }

    @Transactional(readOnly = true)
    public AccountSummaryResponse getAccountSummary(User user) {
        List<Account> accounts = accountRepository.findByUserOrderByIsActiveDescTypeAscNameAsc(user);

        BigDecimal totalCash = BigDecimal.ZERO;
        BigDecimal totalSavings = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (Account acc : accounts) {
            if (!Boolean.TRUE.equals(acc.getIsActive())) {
                continue;
            }

            BigDecimal balance = acc.getBalance() != null ? acc.getBalance() : BigDecimal.ZERO;
            AccountType type = acc.getType() != null ? acc.getType() : AccountType.CHECKING;

            switch (type) {
                case CASH, CHECKING -> totalCash = totalCash.add(balance);
                case SAVINGS -> totalSavings = totalSavings.add(balance);
                case CREDIT_CARD -> totalCredit = totalCredit.add(balance);
                default -> totalCash = totalCash.add(balance);
            }
        }

        BigDecimal netWorth = totalCash.add(totalSavings).subtract(totalCredit);

        return AccountSummaryResponse.builder()
                .totalCash(totalCash)
                .totalSavings(totalSavings)
                .totalCredit(totalCredit)
                .netWorth(netWorth)
                .build();
    }

    @Transactional
    public List<Account> createDefaultAccountsIfEmpty(User user) {
        if (accountRepository.existsByUser(user)) {
            return accountRepository.findByUserOrderByIsActiveDescTypeAscNameAsc(user);
        }

        Account hdfc = Account.builder()
                .user(user)
                .name("HDFC Bank")
                .type(AccountType.CHECKING)
                .balance(BigDecimal.ZERO)
                .institutionName("HDFC Bank")
                .currency("INR")
                .isActive(true)
                .build();

        Account cash = Account.builder()
                .user(user)
                .name("Cash Wallet")
                .type(AccountType.CASH)
                .balance(BigDecimal.ZERO)
                .currency("INR")
                .isActive(true)
                .build();

        List<Account> defaults = accountRepository.saveAll(List.of(hdfc, cash));
        log.info("Provisioned default accounts for user ID {}", user.getId());
        return defaults;
    }
}
