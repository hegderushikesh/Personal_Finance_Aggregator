package com.finpilot.recurring.service;

import com.finpilot.account.entity.Account;
import com.finpilot.category.entity.Category;
import com.finpilot.recurring.entity.RecurringFrequency;
import com.finpilot.recurring.entity.RecurringPayment;
import com.finpilot.recurring.entity.RecurringPaymentStatus;
import com.finpilot.transaction.entity.Transaction;
import com.finpilot.transaction.entity.TransactionType;
import com.finpilot.user.entity.User;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Deterministic rules-based recurring payment detector.
 * NO ML, AI, LLMs, or heuristic prediction models are used.
 * Detection is based strictly on: Same Merchant + Similar Amount + Regular Interval.
 */
@Service
@Slf4j
@Getter
@Setter
public class RecurringPaymentDetector {

    private final MerchantNormalizer merchantNormalizer;

    @Value("${recurring.tolerance.base-amount:50.00}")
    private BigDecimal toleranceBaseAmount = new BigDecimal("50.00");

    @Value("${recurring.tolerance.percentage:0.10}")
    private BigDecimal tolerancePercentage = new BigDecimal("0.10");

    public RecurringPaymentDetector(MerchantNormalizer merchantNormalizer) {
        this.merchantNormalizer = merchantNormalizer;
    }

    /**
     * Analyzes transactions and detects potential recurring payments.
     */
    public List<RecurringPayment> detect(User user, List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return Collections.emptyList();
        }

        // STEP 1 & STEP 2: Filter eligible expense transactions and normalize merchant
        List<Transaction> eligibleTransactions = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> t.getAmount() != null && t.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .filter(t -> t.getMerchantName() != null && !t.getMerchantName().trim().isEmpty())
                .filter(t -> t.getTransactionDate() != null)
                .collect(Collectors.toList());

        // STEP 3: Group transactions by normalized merchant name
        Map<String, List<Transaction>> merchantGroups = new HashMap<>();
        for (Transaction tx : eligibleTransactions) {
            String norm = merchantNormalizer.normalize(tx.getMerchantName());
            if (!norm.isEmpty()) {
                merchantGroups.computeIfAbsent(norm, k -> new ArrayList<>()).add(tx);
            }
        }

        List<RecurringPayment> detected = new ArrayList<>();

        for (Map.Entry<String, List<Transaction>> entry : merchantGroups.entrySet()) {
            String normalizedMerchant = entry.getKey();
            List<Transaction> group = entry.getValue();

            // STEP 4: Require minimum 3 occurrences
            if (group.size() < 3) {
                continue;
            }

            // STEP 5: Sort group by transactionDate ASC
            group.sort(Comparator.comparing(Transaction::getTransactionDate));

            // STEP 6: Calculate amount similarity
            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal minAmount = group.get(0).getAmount();
            BigDecimal maxAmount = group.get(0).getAmount();

            for (Transaction tx : group) {
                BigDecimal amt = tx.getAmount();
                totalAmount = totalAmount.add(amt);
                if (amt.compareTo(minAmount) < 0) minAmount = amt;
                if (amt.compareTo(maxAmount) > 0) maxAmount = amt;
            }

            BigDecimal averageAmount = totalAmount.divide(BigDecimal.valueOf(group.size()), 2, RoundingMode.HALF_UP);
            BigDecimal amountVariance = maxAmount.subtract(minAmount);

            // Configurable tolerance rule: max(toleranceBaseAmount, averageAmount * tolerancePercentage)
            BigDecimal percentTolerance = averageAmount.multiply(tolerancePercentage).setScale(2, RoundingMode.HALF_UP);
            BigDecimal tolerance = toleranceBaseAmount.max(percentTolerance);

            boolean amountsSimilar = true;
            for (Transaction tx : group) {
                BigDecimal diff = tx.getAmount().subtract(averageAmount).abs();
                if (diff.compareTo(tolerance) > 0) {
                    amountsSimilar = false;
                    break;
                }
            }

            if (!amountsSimilar) {
                log.debug("Merchant {} failed amount similarity test (variance: {}, tolerance: {})",
                        normalizedMerchant, amountVariance, tolerance);
                continue;
            }

            // STEP 7: Calculate date intervals between consecutive transactions
            List<Long> intervals = new ArrayList<>();
            for (int i = 1; i < group.size(); i++) {
                long days = ChronoUnit.DAYS.between(
                        group.get(i - 1).getTransactionDate(),
                        group.get(i).getTransactionDate()
                );
                intervals.add(days);
            }

            double avgIntervalDouble = intervals.stream().mapToLong(Long::longValue).average().orElse(0.0);
            int averageIntervalDays = (int) Math.round(avgIntervalDouble);

            // STEP 8: Determine frequency based on average interval
            RecurringFrequency frequency = determineFrequency(averageIntervalDays);
            if (frequency == null) {
                log.debug("Merchant {} failed frequency determination with average interval of {} days",
                        normalizedMerchant, averageIntervalDays);
                continue;
            }

            // STEP 9: Verify regularity
            boolean isRegular = verifyRegularity(intervals, frequency);
            if (!isRegular) {
                log.debug("Merchant {} failed interval regularity check for frequency {}",
                        normalizedMerchant, frequency);
                continue;
            }

            // STEP 10: Build RecurringPayment entity
            Transaction lastTx = group.get(group.size() - 1);
            LocalDate lastTransactionDate = lastTx.getTransactionDate();
            BigDecimal lastAmount = lastTx.getAmount();
            LocalDate nextExpectedDate = lastTransactionDate.plusDays(averageIntervalDays);

            // Status: PAUSED if today is past nextExpectedDate + intervalDays
            LocalDate today = LocalDate.now();
            RecurringPaymentStatus status = RecurringPaymentStatus.ACTIVE;
            if (today.isAfter(nextExpectedDate.plusDays(averageIntervalDays))) {
                status = RecurringPaymentStatus.PAUSED;
            }

            // Deterministic Confidence Score (Rule-based, 0-100)
            int confidence = 0;
            if (group.size() >= 3) confidence += 30; // 3+ matching occurrences
            if (amountsSimilar) confidence += 30;    // amount similarity passed
            if (isRegular) confidence += 30;         // regular intervals verified
            if (group.size() >= 4) confidence += 10; // 4+ historical occurrences
            confidence = Math.min(100, confidence);

            // Category: Most frequent category among matching transactions
            Category category = resolveMostFrequentCategory(group);

            // Account: Single account if all match, or null if multiple accounts
            Account account = resolveAccount(group);

            // Canonical merchant display name (most frequent or latest)
            String displayMerchantName = resolveMerchantDisplayName(group);

            RecurringPayment rp = RecurringPayment.builder()
                    .user(user)
                    .merchantName(displayMerchantName)
                    .normalizedMerchantName(normalizedMerchant)
                    .category(category)
                    .account(account)
                    .averageAmount(averageAmount)
                    .lastAmount(lastAmount)
                    .frequency(frequency)
                    .intervalDays(averageIntervalDays)
                    .nextExpectedDate(nextExpectedDate)
                    .lastTransactionDate(lastTransactionDate)
                    .occurrenceCount(group.size())
                    .amountVariance(amountVariance)
                    .status(status)
                    .confidence(confidence)
                    .build();

            detected.add(rp);
        }

        return detected;
    }

    private RecurringFrequency determineFrequency(int avgDays) {
        if (avgDays >= 6 && avgDays <= 8) {
            return RecurringFrequency.WEEKLY;
        }
        if (avgDays >= 13 && avgDays <= 15) {
            return RecurringFrequency.BIWEEKLY;
        }
        if (avgDays >= 27 && avgDays <= 35) {
            return RecurringFrequency.MONTHLY;
        }
        if (avgDays >= 80 && avgDays <= 100) {
            return RecurringFrequency.QUARTERLY;
        }
        if (avgDays >= 330 && avgDays <= 400) {
            return RecurringFrequency.YEARLY;
        }
        return null;
    }

    private boolean isWithinFrequencyRange(long days, RecurringFrequency frequency) {
        return switch (frequency) {
            case WEEKLY -> days >= 6 && days <= 8;
            case BIWEEKLY -> days >= 13 && days <= 15;
            case MONTHLY -> days >= 27 && days <= 35;
            case QUARTERLY -> days >= 80 && days <= 100;
            case YEARLY -> days >= 330 && days <= 400;
        };
    }

    private boolean verifyRegularity(List<Long> intervals, RecurringFrequency frequency) {
        if (intervals.isEmpty()) {
            return false;
        }

        // For exactly 2 intervals (3 transactions), BOTH intervals must fall within the range
        if (intervals.size() <= 2) {
            return intervals.stream().allMatch(d -> isWithinFrequencyRange(d, frequency));
        }

        // For larger transaction histories, majority of intervals must match
        long matchCount = intervals.stream().filter(d -> isWithinFrequencyRange(d, frequency)).count();
        double matchRatio = (double) matchCount / intervals.size();
        return matchRatio >= 0.60;
    }

    private Category resolveMostFrequentCategory(List<Transaction> group) {
        Map<Category, Long> frequencyMap = new HashMap<>();
        for (Transaction tx : group) {
            if (tx.getCategory() != null) {
                frequencyMap.put(tx.getCategory(), frequencyMap.getOrDefault(tx.getCategory(), 0L) + 1);
            }
        }
        return frequencyMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private Account resolveAccount(List<Transaction> group) {
        Set<Long> accountIds = new HashSet<>();
        Account firstAccount = null;
        for (Transaction tx : group) {
            if (tx.getAccount() != null) {
                accountIds.add(tx.getAccount().getId());
                if (firstAccount == null) {
                    firstAccount = tx.getAccount();
                }
            }
        }
        // If all transactions are from the exact same account, return it; otherwise return null
        return accountIds.size() == 1 ? firstAccount : null;
    }

    private String resolveMerchantDisplayName(List<Transaction> group) {
        Map<String, Long> countMap = new HashMap<>();
        for (Transaction tx : group) {
            String name = tx.getMerchantName().trim();
            countMap.put(name, countMap.getOrDefault(name, 0L) + 1);
        }
        return countMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(group.get(group.size() - 1).getMerchantName().trim());
    }
}
