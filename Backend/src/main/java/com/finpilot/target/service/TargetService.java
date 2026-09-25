package com.finpilot.target.service;

import com.finpilot.category.entity.Category;
import com.finpilot.category.repository.CategoryRepository;
import com.finpilot.target.dto.TargetRequest;
import com.finpilot.target.dto.TargetResponse;
import com.finpilot.target.entity.Target;
import com.finpilot.target.entity.TargetFrequency;
import com.finpilot.target.repository.TargetRepository;
import com.finpilot.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TargetService {

    private final TargetRepository targetRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<TargetResponse> getUserTargets(User user) {
        List<Category> categories = categoryRepository.findByUserOrderBySortOrderAscIdAsc(user);
        List<Target> targets = targetRepository.findByCategoryIn(categories);

        YearMonth now = YearMonth.now();
        return targets.stream()
                .map(t -> TargetResponse.buildFromTargetAndAvailable(t, null, now))
                .toList();
    }

    @Transactional(readOnly = true)
    public TargetResponse getTargetByCategoryId(User user, Long categoryId) {
        Category category = categoryRepository.findByIdAndUser(categoryId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        Target target = targetRepository.findByCategory(category).orElse(null);
        if (target == null) return null;
        return TargetResponse.buildFromTargetAndAvailable(target, null, YearMonth.now());
    }

    @Transactional
    public TargetResponse createOrUpdateTarget(User user, TargetRequest request) {
        Category category = categoryRepository.findByIdAndUser(request.getCategoryId(), user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        Target target = targetRepository.findByCategory(category)
                .orElse(Target.builder().category(category).build());

        target.setType(request.getType());
        target.setAmount(request.getAmount());
        target.setTargetDate(request.getTargetDate());
        target.setFrequency(request.getFrequency() != null ? request.getFrequency() : TargetFrequency.MONTHLY);
        target.setMonthlyAmount(request.getMonthlyAmount() != null ? request.getMonthlyAmount() : request.getAmount());
        target.setSnoozed(false);

        Target saved = targetRepository.save(target);
        return TargetResponse.buildFromTargetAndAvailable(saved, null, YearMonth.now());
    }

    @Transactional
    public void deleteTarget(User user, Long targetId) {
        Target target = targetRepository.findByIdAndCategoryUser(targetId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target not found"));

        targetRepository.delete(target);
    }

    @Transactional
    public TargetResponse snoozeTarget(User user, Long targetId) {
        Target target = targetRepository.findByIdAndCategoryUser(targetId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target not found"));

        target.setSnoozed(true);
        Target saved = targetRepository.save(target);
        return TargetResponse.buildFromTargetAndAvailable(saved, null, YearMonth.now());
    }

    @Transactional
    public TargetResponse unsnoozeTarget(User user, Long targetId) {
        Target target = targetRepository.findByIdAndCategoryUser(targetId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target not found"));

        target.setSnoozed(false);
        Target saved = targetRepository.save(target);
        return TargetResponse.buildFromTargetAndAvailable(saved, null, YearMonth.now());
    }
}
