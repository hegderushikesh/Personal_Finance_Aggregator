package com.finpilot.plaid.service;

import com.finpilot.category.entity.Category;
import com.finpilot.category.repository.CategoryRepository;
import com.finpilot.user.entity.User;
import com.plaid.client.model.PersonalFinanceCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlaidCategoryMapper {

    private final CategoryRepository categoryRepository;

    /**
     * Maps a Plaid transaction's category information to an existing FinPilot user Category.
     * Never creates unlimited duplicate categories.
     */
    public Category mapToCategory(User user, PersonalFinanceCategory personalFinanceCategory, List<String> legacyCategories) {
        List<Category> userCategories = categoryRepository.findByUserOrderBySortOrderAscIdAsc(user);
        if (userCategories.isEmpty()) {
            return null;
        }

        String primaryPlaid = personalFinanceCategory != null && personalFinanceCategory.getPrimary() != null
                ? personalFinanceCategory.getPrimary().toUpperCase(Locale.ROOT)
                : "";

        String detailedPlaid = personalFinanceCategory != null && personalFinanceCategory.getDetailed() != null
                ? personalFinanceCategory.getDetailed().toUpperCase(Locale.ROOT)
                : "";

        // 1. Check primary Plaid personal finance categories
        if (primaryPlaid.contains("FOOD_AND_DRINK")) {
            Optional<Category> match = findMatching(userCategories, "Food", "Dining Out", "Groceries");
            if (match.isPresent()) return match.get();
        }

        if (primaryPlaid.contains("TRANSPORTATION")) {
            Optional<Category> match = findMatching(userCategories, "Transportation", "Travel");
            if (match.isPresent()) return match.get();
        }

        if (primaryPlaid.contains("RENT_AND_UTILITIES")) {
            Optional<Category> match = findMatching(userCategories, "Utilities", "Rent / Mortgage", "Phone & Internet");
            if (match.isPresent()) return match.get();
        }

        if (primaryPlaid.contains("GENERAL_MERCHANDISE") || detailedPlaid.contains("GENERAL_MERCHANDISE")) {
            Optional<Category> match = findMatching(userCategories, "Shopping", "Groceries");
            if (match.isPresent()) return match.get();
        }

        if (primaryPlaid.contains("ENTERTAINMENT")) {
            Optional<Category> match = findMatching(userCategories, "Entertainment");
            if (match.isPresent()) return match.get();
        }

        if (primaryPlaid.contains("TRAVEL")) {
            Optional<Category> match = findMatching(userCategories, "Travel", "Vacation");
            if (match.isPresent()) return match.get();
        }

        if (primaryPlaid.contains("MEDICAL") || primaryPlaid.contains("HEALTHCARE")) {
            Optional<Category> match = findMatching(userCategories, "Medical Expenses");
            if (match.isPresent()) return match.get();
        }

        if (primaryPlaid.contains("LOAN_PAYMENTS")) {
            Optional<Category> match = findMatching(userCategories, "Credit Card", "Student Loan", "Personal Loan");
            if (match.isPresent()) return match.get();
        }

        // 2. Check legacy category strings
        if (legacyCategories != null) {
            for (String legCat : legacyCategories) {
                if (legCat == null) continue;
                for (Category uc : userCategories) {
                    if (legCat.toLowerCase().contains(uc.getName().toLowerCase()) ||
                        uc.getName().toLowerCase().contains(legCat.toLowerCase())) {
                        return uc;
                    }
                }
            }
        }

        // 3. Fallback to category named "Uncategorized", or first available category
        return userCategories.stream()
                .filter(c -> "Uncategorized".equalsIgnoreCase(c.getName()))
                .findFirst()
                .orElse(null);
    }

    private Optional<Category> findMatching(List<Category> categories, String... targetNames) {
        for (String target : targetNames) {
            for (Category c : categories) {
                if (c.getName().equalsIgnoreCase(target)) {
                    return Optional.of(c);
                }
            }
        }
        return Optional.empty();
    }
}
