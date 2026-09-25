package com.finpilot.plaid.service;

import com.finpilot.account.entity.Account;
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
import com.finpilot.user.entity.User;
import com.plaid.client.model.*;
import com.plaid.client.request.PlaidApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import retrofit2.Response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaidTransactionSyncService {

    private final PlaidApi plaidApi;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final PlaidCategoryMapper plaidCategoryMapper;
    private final BudgetTransactionService budgetTransactionService;
    private final PlaidConnectionRepository plaidConnectionRepository;

    @Transactional
    public SyncResponse syncTransactions(User user, PlaidConnection connection) {
        log.info("Synchronizing Plaid transactions for connection ID {} (user ID {})", connection.getId(), user.getId());

        String cursor = connection.getCursor();
        boolean hasMore = true;
        int totalAdded = 0;
        int totalModified = 0;
        int totalRemoved = 0;

        Set<Long> affectedAccountIds = new HashSet<>();

        try {
            while (hasMore) {
                TransactionsSyncRequest request = new TransactionsSyncRequest()
                        .accessToken(connection.getAccessToken())
                        .cursor(cursor);

                Response<TransactionsSyncResponse> response = plaidApi.transactionsSync(request).execute();

                if (!response.isSuccessful() || response.body() == null) {
                    String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                    log.error("Plaid /transactions/sync failed with HTTP {}: {}", response.code(), errorBody);

                    if (errorBody.contains("ITEM_LOGIN_REQUIRED")) {
                        connection.setStatus(PlaidConnectionStatus.LOGIN_REQUIRED);
                        plaidConnectionRepository.save(connection);
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bank authentication expired. Reconnection required.");
                    }

                    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Plaid transactions sync failed: " + response.code());
                }

                TransactionsSyncResponse body = response.body();

                // 1. Process Added
                List<com.plaid.client.model.Transaction> addedList = body.getAdded();
                for (com.plaid.client.model.Transaction pt : addedList) {
                    boolean wasProcessed = processAddedTransaction(user, connection, pt, affectedAccountIds);
                    if (wasProcessed) {
                        totalAdded++;
                    } else {
                        totalModified++;
                    }
                }

                // 2. Process Modified
                List<com.plaid.client.model.Transaction> modifiedList = body.getModified();
                for (com.plaid.client.model.Transaction pt : modifiedList) {
                    processModifiedTransaction(user, connection, pt, affectedAccountIds);
                    totalModified++;
                }

                // 3. Process Removed
                List<RemovedTransaction> removedList = body.getRemoved();
                for (RemovedTransaction rt : removedList) {
                    processRemovedTransaction(user, connection, rt.getTransactionId(), affectedAccountIds);
                    totalRemoved++;
                }

                cursor = body.getNextCursor();
                hasMore = body.getHasMore();
            }

            // Save updated cursor and lastSyncedAt
            connection.setCursor(cursor);
            connection.setStatus(PlaidConnectionStatus.ACTIVE);
            LocalDateTime now = LocalDateTime.now();
            connection.setLastSyncedAt(now);
            plaidConnectionRepository.save(connection);

            log.info("Plaid sync finished for connection {}: added={}, modified={}, removed={}",
                    connection.getId(), totalAdded, totalModified, totalRemoved);

            return SyncResponse.builder()
                    .added(totalAdded)
                    .modified(totalModified)
                    .removed(totalRemoved)
                    .accountsUpdated(affectedAccountIds.size())
                    .lastSyncedAt(now)
                    .build();

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during transactions sync for connection {}: {}", connection.getId(), e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to sync transactions: " + e.getMessage());
        }
    }

    private boolean processAddedTransaction(
            User user,
            PlaidConnection connection,
            com.plaid.client.model.Transaction pt,
            Set<Long> affectedAccountIds
    ) {
        String externalTxId = pt.getTransactionId();

        // Duplicate protection: check if transaction already exists for this connection
        Optional<Transaction> existingOpt = transactionRepository
                .findByPlaidConnectionIdAndExternalTransactionId(connection.getId(), externalTxId);

        if (existingOpt.isPresent()) {
            // Already imported: update it rather than inserting a duplicate
            processModifiedTransaction(user, connection, pt, affectedAccountIds);
            return false;
        }

        // Map Account
        Optional<Account> accountOpt = accountRepository
                .findByExternalAccountIdAndUser(pt.getAccountId(), user);

        if (accountOpt.isEmpty()) {
            log.warn("Account with external ID {} not found for user {}. Skipping transaction {}.",
                    pt.getAccountId(), user.getId(), externalTxId);
            return false;
        }

        Account account = accountOpt.get();
        affectedAccountIds.add(account.getId());

        // Map Category
        Category category = plaidCategoryMapper.mapToCategory(
                user, pt.getPersonalFinanceCategory(), pt.getCategory()
        );

        // Map Amount and Type
        TransactionType type = determineTransactionType(pt);
        BigDecimal amount = normalizeAmount(pt.getAmount());
        LocalDate txDate = pt.getDate();
        boolean pending = Boolean.TRUE.equals(pt.getPending());
        String merchant = pt.getMerchantName() != null && !pt.getMerchantName().isBlank()
                ? pt.getMerchantName().trim()
                : (pt.getName() != null ? pt.getName().trim() : "Transaction");

        Transaction transaction = Transaction.builder()
                .user(user)
                .account(account)
                .category(category)
                .merchantName(merchant)
                .amount(amount)
                .transactionDate(txDate)
                .type(type)
                .description(pt.getName())
                .source(TransactionSource.PLAID)
                .externalTransactionId(externalTxId)
                .pending(pending)
                .plaidConnectionId(connection.getId())
                .build();

        // Update account balance
        if (type == TransactionType.EXPENSE) {
            account.setBalance(account.getBalance().subtract(amount));
        } else if (type == TransactionType.INCOME) {
            account.setBalance(account.getBalance().add(amount));
        }
        accountRepository.save(account);

        Transaction saved = transactionRepository.save(transaction);
        log.debug("Imported Plaid transaction ID {} ({}) for account {}", saved.getId(), type, account.getName());

        // Recalculate budget activity if expense
        if (type == TransactionType.EXPENSE && category != null) {
            budgetTransactionService.recalculateCategoryActivity(
                    user, category, txDate.getYear(), txDate.getMonthValue()
            );
        }

        return true;
    }

    private void processModifiedTransaction(
            User user,
            PlaidConnection connection,
            com.plaid.client.model.Transaction pt,
            Set<Long> affectedAccountIds
    ) {
        String externalTxId = pt.getTransactionId();
        Optional<Transaction> existingOpt = transactionRepository
                .findByPlaidConnectionIdAndExternalTransactionId(connection.getId(), externalTxId);

        if (existingOpt.isEmpty()) {
            processAddedTransaction(user, connection, pt, affectedAccountIds);
            return;
        }

        Transaction existing = existingOpt.get();
        Account oldAccount = existing.getAccount();
        Category oldCategory = existing.getCategory();
        BigDecimal oldAmount = existing.getAmount();
        LocalDate oldDate = existing.getTransactionDate();
        TransactionType oldType = existing.getType();

        // Revert old account balance effect
        if (oldType == TransactionType.EXPENSE) {
            oldAccount.setBalance(oldAccount.getBalance().add(oldAmount));
        } else if (oldType == TransactionType.INCOME) {
            oldAccount.setBalance(oldAccount.getBalance().subtract(oldAmount));
        }

        // Map new values
        TransactionType newType = determineTransactionType(pt);
        BigDecimal newAmount = normalizeAmount(pt.getAmount());
        LocalDate newDate = pt.getDate();
        Category newCategory = plaidCategoryMapper.mapToCategory(
                user, pt.getPersonalFinanceCategory(), pt.getCategory()
        );

        existing.setMerchantName(pt.getMerchantName() != null ? pt.getMerchantName() : pt.getName());
        existing.setAmount(newAmount);
        existing.setType(newType);
        existing.setTransactionDate(newDate);
        existing.setCategory(newCategory);
        existing.setPending(Boolean.TRUE.equals(pt.getPending()));
        existing.setDescription(pt.getName());

        // Apply new account balance effect
        if (newType == TransactionType.EXPENSE) {
            oldAccount.setBalance(oldAccount.getBalance().subtract(newAmount));
        } else if (newType == TransactionType.INCOME) {
            oldAccount.setBalance(oldAccount.getBalance().add(newAmount));
        }
        accountRepository.save(oldAccount);
        affectedAccountIds.add(oldAccount.getId());

        transactionRepository.save(existing);

        // Recalculate old budget category if changed
        if (oldType == TransactionType.EXPENSE && oldCategory != null) {
            budgetTransactionService.recalculateCategoryActivity(
                    user, oldCategory, oldDate.getYear(), oldDate.getMonthValue()
            );
        }

        // Recalculate new budget category
        if (newType == TransactionType.EXPENSE && newCategory != null) {
            budgetTransactionService.recalculateCategoryActivity(
                    user, newCategory, newDate.getYear(), newDate.getMonthValue()
            );
        }
    }

    private void processRemovedTransaction(
            User user,
            PlaidConnection connection,
            String externalTxId,
            Set<Long> affectedAccountIds
    ) {
        Optional<Transaction> existingOpt = transactionRepository
                .findByPlaidConnectionIdAndExternalTransactionId(connection.getId(), externalTxId);

        if (existingOpt.isEmpty()) {
            return;
        }

        Transaction existing = existingOpt.get();
        Account account = existing.getAccount();
        Category category = existing.getCategory();
        BigDecimal amount = existing.getAmount();
        LocalDate date = existing.getTransactionDate();
        TransactionType type = existing.getType();

        // Revert account balance effect
        if (type == TransactionType.EXPENSE) {
            account.setBalance(account.getBalance().add(amount));
        } else if (type == TransactionType.INCOME) {
            account.setBalance(account.getBalance().subtract(amount));
        }
        accountRepository.save(account);
        affectedAccountIds.add(account.getId());

        transactionRepository.delete(existing);

        // Recalculate budget activity
        if (type == TransactionType.EXPENSE && category != null) {
            budgetTransactionService.recalculateCategoryActivity(
                    user, category, date.getYear(), date.getMonthValue()
            );
        }
    }

    private TransactionType determineTransactionType(com.plaid.client.model.Transaction pt) {
        if (pt.getPersonalFinanceCategory() != null && pt.getPersonalFinanceCategory().getPrimary() != null) {
            String primary = pt.getPersonalFinanceCategory().getPrimary().toUpperCase();
            if (primary.contains("TRANSFER")) {
                return TransactionType.TRANSFER;
            }
            if (primary.contains("INCOME")) {
                return TransactionType.INCOME;
            }
        }

        // Plaid sign semantics: positive amount is OUTFLOW (EXPENSE), negative is INFLOW (INCOME)
        Double rawAmount = pt.getAmount();
        if (rawAmount != null && rawAmount < 0) {
            return TransactionType.INCOME;
        }
        return TransactionType.EXPENSE;
    }

    private BigDecimal normalizeAmount(Double rawAmount) {
        if (rawAmount == null) {
            return BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);
        }
        // FinPilot amounts are always positive with scale 2
        return BigDecimal.valueOf(Math.abs(rawAmount)).setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
