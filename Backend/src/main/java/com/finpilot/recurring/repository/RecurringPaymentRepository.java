package com.finpilot.recurring.repository;

import com.finpilot.recurring.entity.RecurringFrequency;
import com.finpilot.recurring.entity.RecurringPayment;
import com.finpilot.recurring.entity.RecurringPaymentStatus;
import com.finpilot.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecurringPaymentRepository extends JpaRepository<RecurringPayment, Long> {

    List<RecurringPayment> findByUserOrderByNextExpectedDateAsc(User user);

    List<RecurringPayment> findByUserAndStatusOrderByNextExpectedDateAsc(User user, RecurringPaymentStatus status);

    List<RecurringPayment> findByUserAndFrequencyOrderByNextExpectedDateAsc(User user, RecurringFrequency frequency);

    List<RecurringPayment> findByUserAndNextExpectedDateBetweenOrderByNextExpectedDateAsc(
            User user, LocalDate startDate, LocalDate endDate
    );

    Optional<RecurringPayment> findByUserAndNormalizedMerchantNameAndFrequency(
            User user, String normalizedMerchantName, RecurringFrequency frequency
    );

    Optional<RecurringPayment> findByIdAndUser(Long id, User user);

    void deleteByIdAndUser(Long id, User user);
}
