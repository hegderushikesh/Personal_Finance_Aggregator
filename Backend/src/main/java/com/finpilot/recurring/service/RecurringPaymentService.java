package com.finpilot.recurring.service;

import com.finpilot.category.entity.Category;
import com.finpilot.category.repository.CategoryRepository;
import com.finpilot.recurring.dto.DetectRecurringRequest;
import com.finpilot.recurring.dto.DetectRecurringResponse;
import com.finpilot.recurring.dto.RecurringPaymentResponse;
import com.finpilot.recurring.dto.UpdateRecurringPaymentRequest;
import com.finpilot.recurring.entity.RecurringFrequency;
import com.finpilot.recurring.entity.RecurringPayment;
import com.finpilot.recurring.entity.RecurringPaymentStatus;
import com.finpilot.recurring.repository.RecurringPaymentRepository;
import com.finpilot.transaction.entity.Transaction;
import com.finpilot.transaction.entity.TransactionType;
import com.finpilot.transaction.repository.TransactionRepository;
import com.finpilot.user.entity.User;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecurringPaymentService {

    private final RecurringPaymentRepository recurringPaymentRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final RecurringPaymentDetector recurringPaymentDetector;
    private final MerchantNormalizer merchantNormalizer;

    @Transactional
    public DetectRecurringResponse detectRecurringPayments(User user, DetectRecurringRequest request) {
        LocalDate start = (request != null && request.getStartDate() != null)
                ? request.getStartDate()
                : LocalDate.now().minusMonths(12);

        LocalDate end = (request != null && request.getEndDate() != null)
                ? request.getEndDate()
                : LocalDate.now();

        Specification<Transaction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user"), user));
            predicates.add(cb.equal(root.get("type"), TransactionType.EXPENSE));
            predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), start));
            predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), end));
            predicates.add(cb.isNotNull(root.get("merchantName")));
            predicates.add(cb.gt(root.get("amount"), BigDecimal.ZERO));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<Transaction> transactions = transactionRepository.findAll(spec);
        List<RecurringPayment> detectedList = recurringPaymentDetector.detect(user, transactions);

        List<RecurringPayment> savedList = new ArrayList<>();

        for (RecurringPayment detected : detectedList) {
            Optional<RecurringPayment> existingOpt = recurringPaymentRepository
                    .findByUserAndNormalizedMerchantNameAndFrequency(
                            user, detected.getNormalizedMerchantName(), detected.getFrequency()
                    );

            RecurringPayment toSave;
            if (existingOpt.isPresent()) {
                toSave = existingOpt.get();
                toSave.setMerchantName(detected.getMerchantName());
                toSave.setCategory(detected.getCategory());
                toSave.setAccount(detected.getAccount());
                toSave.setAverageAmount(detected.getAverageAmount());
                toSave.setLastAmount(detected.getLastAmount());
                toSave.setIntervalDays(detected.getIntervalDays());
                toSave.setNextExpectedDate(detected.getNextExpectedDate());
                toSave.setLastTransactionDate(detected.getLastTransactionDate());
                toSave.setOccurrenceCount(detected.getOccurrenceCount());
                toSave.setAmountVariance(detected.getAmountVariance());
                toSave.setStatus(detected.getStatus());
                toSave.setConfidence(detected.getConfidence());
            } else {
                toSave = detected;
            }

            savedList.add(recurringPaymentRepository.save(toSave));
        }

        List<RecurringPaymentResponse> responseList = savedList.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return DetectRecurringResponse.builder()
                .detected(responseList.size())
                .recurringPayments(responseList)
                .build();
    }

    @Transactional(readOnly = true)
    public List<RecurringPaymentResponse> getRecurringPayments(
            User user,
            RecurringPaymentStatus status,
            RecurringFrequency frequency,
            Boolean upcoming
    ) {
        List<RecurringPayment> list = recurringPaymentRepository.findByUserOrderByNextExpectedDateAsc(user);

        LocalDate today = LocalDate.now();
        LocalDate upcomingEnd = today.plusDays(30);

        return list.stream()
                .filter(rp -> status == null || rp.getStatus() == status)
                .filter(rp -> frequency == null || rp.getFrequency() == frequency)
                .filter(rp -> {
                    if (Boolean.TRUE.equals(upcoming)) {
                        LocalDate next = rp.getNextExpectedDate();
                        return next != null && !next.isBefore(today) && !next.isAfter(upcomingEnd);
                    }
                    return true;
                })
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RecurringPaymentResponse getRecurringPaymentById(User user, Long id) {
        RecurringPayment rp = recurringPaymentRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurring payment not found"));
        return mapToResponse(rp);
    }

    @Transactional
    public RecurringPaymentResponse updateRecurringPayment(User user, Long id, UpdateRecurringPaymentRequest request) {
        RecurringPayment rp = recurringPaymentRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurring payment not found"));

        if (request.getStatus() != null) {
            rp.setStatus(request.getStatus());
        }
        if (request.getMerchantName() != null && !request.getMerchantName().isBlank()) {
            rp.setMerchantName(request.getMerchantName().trim());
            rp.setNormalizedMerchantName(merchantNormalizer.normalize(request.getMerchantName()));
        }
        if (request.getFrequency() != null) {
            rp.setFrequency(request.getFrequency());
        }
        if (request.getNextExpectedDate() != null) {
            rp.setNextExpectedDate(request.getNextExpectedDate());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndUser(request.getCategoryId(), user)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found or access denied"));
            rp.setCategory(category);
        }

        RecurringPayment updated = recurringPaymentRepository.save(rp);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteRecurringPayment(User user, Long id) {
        RecurringPayment rp = recurringPaymentRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurring payment not found"));
        recurringPaymentRepository.delete(rp);
    }

    public RecurringPaymentResponse mapToResponse(RecurringPayment rp) {
        return RecurringPaymentResponse.builder()
                .id(rp.getId())
                .merchantName(rp.getMerchantName())
                .normalizedMerchantName(rp.getNormalizedMerchantName())
                .categoryId(rp.getCategory() != null ? rp.getCategory().getId() : null)
                .categoryName(rp.getCategory() != null ? rp.getCategory().getName() : null)
                .categoryColor(rp.getCategory() != null ? rp.getCategory().getColor() : null)
                .categoryIcon(rp.getCategory() != null ? rp.getCategory().getIcon() : null)
                .accountId(rp.getAccount() != null ? rp.getAccount().getId() : null)
                .accountName(rp.getAccount() != null ? rp.getAccount().getName() : "Multiple accounts")
                .averageAmount(rp.getAverageAmount())
                .lastAmount(rp.getLastAmount())
                .frequency(rp.getFrequency())
                .intervalDays(rp.getIntervalDays())
                .nextExpectedDate(rp.getNextExpectedDate())
                .lastTransactionDate(rp.getLastTransactionDate())
                .occurrenceCount(rp.getOccurrenceCount())
                .amountVariance(rp.getAmountVariance())
                .status(rp.getStatus())
                .confidence(rp.getConfidence())
                .createdAt(rp.getCreatedAt())
                .updatedAt(rp.getUpdatedAt())
                .build();
    }
}
