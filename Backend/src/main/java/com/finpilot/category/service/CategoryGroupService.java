package com.finpilot.category.service;

import com.finpilot.category.dto.CategoryGroupRequest;
import com.finpilot.category.dto.CategoryGroupResponse;
import com.finpilot.category.dto.CategoryResponse;
import com.finpilot.category.entity.Category;
import com.finpilot.category.entity.CategoryGroup;
import com.finpilot.category.repository.CategoryGroupRepository;
import com.finpilot.category.repository.CategoryRepository;
import com.finpilot.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryGroupService {

    private final CategoryGroupRepository categoryGroupRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryGroupResponse> getUserCategoryGroups(User user) {
        List<CategoryGroup> groups = categoryGroupRepository.findByUserOrderBySortOrderAscIdAsc(user);
        List<Category> categories = categoryRepository.findByUserOrderBySortOrderAscIdAsc(user);

        Map<Long, List<CategoryResponse>> groupCategoriesMap = categories.stream()
                .map(CategoryResponse::from)
                .collect(Collectors.groupingBy(CategoryResponse::getCategoryGroupId));

        return groups.stream()
                .map(group -> CategoryGroupResponse.from(
                        group,
                        groupCategoriesMap.getOrDefault(group.getId(), new ArrayList<>())
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryGroupResponse createCategoryGroup(User user, CategoryGroupRequest request) {
        CategoryGroup group = CategoryGroup.builder()
                .user(user)
                .name(request.getName().trim())
                .icon(request.getIcon() != null ? request.getIcon() : "Folder")
                .color(request.getColor() != null ? request.getColor() : "#3B82F6")
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .isCollapsed(false)
                .build();

        CategoryGroup saved = categoryGroupRepository.save(group);
        return CategoryGroupResponse.from(saved, new ArrayList<>());
    }

    @Transactional
    public CategoryGroupResponse updateCategoryGroup(User user, Long id, CategoryGroupRequest request) {
        CategoryGroup group = categoryGroupRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category group not found"));

        group.setName(request.getName().trim());
        if (request.getIcon() != null) group.setIcon(request.getIcon());
        if (request.getColor() != null) group.setColor(request.getColor());
        if (request.getSortOrder() != null) group.setSortOrder(request.getSortOrder());

        CategoryGroup updated = categoryGroupRepository.save(group);
        List<CategoryResponse> categories = categoryRepository.findByUserAndCategoryGroupOrderBySortOrderAscIdAsc(user, updated)
                .stream().map(CategoryResponse::from).toList();

        return CategoryGroupResponse.from(updated, categories);
    }

    @Transactional
    public void deleteCategoryGroup(User user, Long id) {
        CategoryGroup group = categoryGroupRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category group not found"));

        categoryGroupRepository.delete(group);
    }

    @Transactional
    public void toggleCollapse(User user, Long id) {
        CategoryGroup group = categoryGroupRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category group not found"));

        group.setIsCollapsed(!Boolean.TRUE.equals(group.getIsCollapsed()));
        categoryGroupRepository.save(group);
    }

    @Transactional
    public void createDefaultCategoryTemplateIfEmpty(User user) {
        List<CategoryGroup> existing = categoryGroupRepository.findByUserOrderBySortOrderAscIdAsc(user);
        if (!existing.isEmpty()) {
            return; // Already initialized
        }

        // 1. Bills
        CategoryGroup bills = categoryGroupRepository.save(CategoryGroup.builder()
                .user(user).name("Bills").icon("FileText").color("#EF4444").sortOrder(1).build());
        categoryRepository.save(Category.builder().user(user).categoryGroup(bills).name("Rent / Mortgage").icon("Home").sortOrder(1).build());
        categoryRepository.save(Category.builder().user(user).categoryGroup(bills).name("Phone & Internet").icon("Wifi").sortOrder(2).build());
        categoryRepository.save(Category.builder().user(user).categoryGroup(bills).name("Utilities").icon("Zap").sortOrder(3).build());
        categoryRepository.save(Category.builder().user(user).categoryGroup(bills).name("Insurance").icon("ShieldCheck").sortOrder(4).build());

        // 2. Needs
        CategoryGroup needs = categoryGroupRepository.save(CategoryGroup.builder()
                .user(user).name("Needs").icon("ShoppingCart").color("#F59E0B").sortOrder(2).build());
        categoryRepository.save(Category.builder().user(user).categoryGroup(needs).name("Groceries").icon("ShoppingBag").sortOrder(1).build());
        categoryRepository.save(Category.builder().user(user).categoryGroup(needs).name("Food").icon("Utensils").sortOrder(2).build());
        categoryRepository.save(Category.builder().user(user).categoryGroup(needs).name("Transportation").icon("Car").sortOrder(3).build());
        categoryRepository.save(Category.builder().user(user).categoryGroup(needs).name("Medical Expenses").icon("Activity").sortOrder(4).build());

        // 3. Wants
        CategoryGroup wants = categoryGroupRepository.save(CategoryGroup.builder()
                .user(user).name("Wants").icon("Tv").color("#8B5CF6").sortOrder(3).build());
        categoryRepository.save(Category.builder().user(user).categoryGroup(wants).name("Entertainment").icon("Film").sortOrder(1).build());
        categoryRepository.save(Category.builder().user(user).categoryGroup(wants).name("Shopping").icon("Tag").sortOrder(2).build());
        categoryRepository.save(Category.builder().user(user).categoryGroup(wants).name("Dining Out").icon("Coffee").sortOrder(3).build());
        categoryRepository.save(Category.builder().user(user).categoryGroup(wants).name("Travel").icon("Plane").sortOrder(4).build());

        // 4. Savings
        CategoryGroup savings = categoryGroupRepository.save(CategoryGroup.builder()
                .user(user).name("Savings").icon("PiggyBank").color("#10B981").sortOrder(4).build());
        categoryRepository.save(Category.builder().user(user).categoryGroup(savings).name("Emergency Fund").icon("Shield").sortOrder(1).build());
        categoryRepository.save(Category.builder().user(user).categoryGroup(savings).name("Vacation").icon("Sun").sortOrder(2).build());
        categoryRepository.save(Category.builder().user(user).categoryGroup(savings).name("Investments").icon("TrendingUp").sortOrder(3).build());

        // 5. Debt
        CategoryGroup debt = categoryGroupRepository.save(CategoryGroup.builder()
                .user(user).name("Debt").icon("CreditCard").color("#64748B").sortOrder(5).build());
        categoryRepository.save(Category.builder().user(user).categoryGroup(debt).name("Credit Card").icon("CreditCard").sortOrder(1).build());
        categoryRepository.save(Category.builder().user(user).categoryGroup(debt).name("Student Loan").icon("BookOpen").sortOrder(2).build());
        categoryRepository.save(Category.builder().user(user).categoryGroup(debt).name("Personal Loan").icon("DollarSign").sortOrder(3).build());
    }
}
