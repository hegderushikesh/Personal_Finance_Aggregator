package com.finpilot.transaction.service;

import com.finpilot.account.entity.Account;
import com.finpilot.account.repository.AccountRepository;
import com.finpilot.budget.service.BudgetTransactionService;
import com.finpilot.category.entity.Category;
import com.finpilot.category.repository.CategoryRepository;
import com.finpilot.transaction.dto.*;
import com.finpilot.transaction.entity.Transaction;
import com.finpilot.transaction.entity.TransactionType;
import com.finpilot.transaction.repository.TransactionRepository;
import com.finpilot.user.entity.User;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetTransactionService budgetTransactionService;

    @Transactional
    public TransactionResponse createTransaction(User user, CreateTransactionRequest request) {
        validateTransactionRequest(request);

        Account account = accountRepository.findByIdAndUser(request.getAccountId(), user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found or access denied"));

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findByIdAndUser(request.getCategoryId(), user)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found or access denied"));
        } else if (request.getType() == TransactionType.EXPENSE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category is required for expense transactions");
        }

        Transaction transaction = Transaction.builder()
                .user(user)
                .account(account)
                .category(category)
                .merchantName(request.getMerchantName().trim())
                .amount(request.getAmount())
                .transactionDate(request.getTransactionDate())
                .type(request.getType())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .build();

        // Update account balance
        if (request.getType() == TransactionType.EXPENSE) {
            account.setBalance(account.getBalance().subtract(request.getAmount()));
        } else if (request.getType() == TransactionType.INCOME) {
            account.setBalance(account.getBalance().add(request.getAmount()));
        }
        accountRepository.save(account);

        Transaction saved = transactionRepository.save(transaction);
        log.info("Created transaction ID {} ({}) of ₹{} for user ID {}",
                saved.getId(), saved.getType(), saved.getAmount(), user.getId());

        // Update budget activity if expense
        if (saved.getType() == TransactionType.EXPENSE && saved.getCategory() != null) {
            budgetTransactionService.recalculateCategoryActivity(
                    user, saved.getCategory(), saved.getTransactionDate().getYear(), saved.getTransactionDate().getMonthValue()
            );
        }

        return TransactionResponse.from(saved);
    }

    @Transactional
    public TransactionResponse updateTransaction(User user, Long id, UpdateTransactionRequest request) {
        validateTransactionRequest(request);

        Transaction existing = transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));

        Account oldAccount = existing.getAccount();
        Category oldCategory = existing.getCategory();
        BigDecimal oldAmount = existing.getAmount();
        LocalDate oldDate = existing.getTransactionDate();
        TransactionType oldType = existing.getType();

        Account newAccount = accountRepository.findByIdAndUser(request.getAccountId(), user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found or access denied"));

        Category newCategory = null;
        if (request.getCategoryId() != null) {
            newCategory = categoryRepository.findByIdAndUser(request.getCategoryId(), user)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found or access denied"));
        } else if (request.getType() == TransactionType.EXPENSE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category is required for expense transactions");
        }

        // Revert old account effect
        if (oldType == TransactionType.EXPENSE) {
            oldAccount.setBalance(oldAccount.getBalance().add(oldAmount));
        } else if (oldType == TransactionType.INCOME) {
            oldAccount.setBalance(oldAccount.getBalance().subtract(oldAmount));
        }
        accountRepository.save(oldAccount);

        // Apply new account effect
        if (newAccount.getId().equals(oldAccount.getId())) {
            newAccount = oldAccount;
        }
        if (request.getType() == TransactionType.EXPENSE) {
            newAccount.setBalance(newAccount.getBalance().subtract(request.getAmount()));
        } else if (request.getType() == TransactionType.INCOME) {
            newAccount.setBalance(newAccount.getBalance().add(request.getAmount()));
        }
        accountRepository.save(newAccount);

        // Update transaction fields
        existing.setAccount(newAccount);
        existing.setCategory(newCategory);
        existing.setMerchantName(request.getMerchantName().trim());
        existing.setAmount(request.getAmount());
        existing.setTransactionDate(request.getTransactionDate());
        existing.setType(request.getType());
        existing.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        Transaction updated = transactionRepository.save(existing);
        log.info("Updated transaction ID {} for user ID {}", updated.getId(), user.getId());

        // Recalculate previous month/category if changed
        if (oldType == TransactionType.EXPENSE && oldCategory != null) {
            budgetTransactionService.recalculateCategoryActivity(
                    user, oldCategory, oldDate.getYear(), oldDate.getMonthValue()
            );
        }

        // Recalculate new month/category
        if (updated.getType() == TransactionType.EXPENSE && updated.getCategory() != null) {
            budgetTransactionService.recalculateCategoryActivity(
                    user, updated.getCategory(), updated.getTransactionDate().getYear(), updated.getTransactionDate().getMonthValue()
            );
        }

        return TransactionResponse.from(updated);
    }

    @Transactional
    public void deleteTransaction(User user, Long id) {
        Transaction existing = transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));

        Account account = existing.getAccount();
        BigDecimal amount = existing.getAmount();
        TransactionType type = existing.getType();
        Category category = existing.getCategory();
        LocalDate date = existing.getTransactionDate();

        // Revert account balance
        if (type == TransactionType.EXPENSE) {
            account.setBalance(account.getBalance().add(amount));
        } else if (type == TransactionType.INCOME) {
            account.setBalance(account.getBalance().subtract(amount));
        }
        accountRepository.save(account);

        transactionRepository.delete(existing);
        log.info("Deleted transaction ID {} for user ID {}", id, user.getId());

        // Recalculate budget activity
        if (type == TransactionType.EXPENSE && category != null) {
            budgetTransactionService.recalculateCategoryActivity(
                    user, category, date.getYear(), date.getMonthValue()
            );
        }
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(User user, Long id) {
        Transaction tx = transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
        return TransactionResponse.from(tx);
    }

    @Transactional(readOnly = true)
    public TransactionPageResponse getTransactions(
            User user,
            Integer month,
            Integer year,
            Long categoryId,
            Long accountId,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate,
            String search,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("createdAt")));

        Specification<Transaction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user"), user));

            if (year != null && month != null) {
                YearMonth ym = YearMonth.of(year, month);
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), ym.atDay(1)));
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), ym.atEndOfMonth()));
            } else {
                if (startDate != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), startDate));
                }
                if (endDate != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), endDate));
                }
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (accountId != null) {
                predicates.add(cb.equal(root.get("account").get("id"), accountId));
            }

            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate merchantMatch = cb.like(cb.lower(root.get("merchantName")), searchPattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), searchPattern);
                predicates.add(cb.or(merchantMatch, descMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Transaction> txPage = transactionRepository.findAll(spec, pageable);

        List<TransactionResponse> content = txPage.getContent().stream()
                .map(TransactionResponse::from)
                .collect(Collectors.toList());

        return TransactionPageResponse.builder()
                .content(content)
                .page(txPage.getNumber())
                .size(txPage.getSize())
                .totalElements(txPage.getTotalElements())
                .totalPages(txPage.getTotalPages())
                .last(txPage.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public TransactionSummaryResponse getTransactionSummary(
            User user,
            Integer month,
            Integer year,
            LocalDate startDate,
            LocalDate endDate
    ) {
        LocalDate start = startDate;
        LocalDate end = endDate;

        if (year != null && month != null) {
            YearMonth ym = YearMonth.of(year, month);
            start = ym.atDay(1);
            end = ym.atEndOfMonth();
        } else if (start == null || end == null) {
            YearMonth ym = YearMonth.now();
            start = ym.atDay(1);
            end = ym.atEndOfMonth();
        }

        BigDecimal totalIncome = transactionRepository.sumAmountByUserAndTypeAndDateBetween(
                user, TransactionType.INCOME, start, end
        );
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;

        BigDecimal totalExpenses = transactionRepository.sumAmountByUserAndTypeAndDateBetween(
                user, TransactionType.EXPENSE, start, end
        );
        if (totalExpenses == null) totalExpenses = BigDecimal.ZERO;

        BigDecimal netCashFlow = totalIncome.subtract(totalExpenses);
        long count = transactionRepository.countByUserAndTransactionDateBetween(user, start, end);

        return TransactionSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netCashFlow(netCashFlow)
                .transactionCount(count)
                .build();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getRecentTransactions(User user, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("createdAt")));
        return transactionRepository.findByUser(user, pageable).getContent().stream()
                .map(TransactionResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getRecentCategoryTransactions(User user, Long categoryId, int limit) {
        Category category = categoryRepository.findByIdAndUser(categoryId, user)
                .orElse(null);
        if (category == null) return List.of();

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("createdAt")));
        Specification<Transaction> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("user"), user),
                cb.equal(root.get("category"), category)
        );
        return transactionRepository.findAll(spec, pageable).getContent().stream()
                .map(TransactionResponse::from)
                .collect(Collectors.toList());
    }

    private void validateTransactionRequest(CreateTransactionRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
        }
    }

    private void validateTransactionRequest(UpdateTransactionRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
        }
    }
}
