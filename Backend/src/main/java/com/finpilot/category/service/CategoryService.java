package com.finpilot.category.service;

import com.finpilot.category.dto.CategoryRequest;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryGroupRepository categoryGroupRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getUserCategories(User user) {
        return categoryRepository.findByUserOrderBySortOrderAscIdAsc(user).stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(User user, CategoryRequest request) {
        CategoryGroup group = categoryGroupRepository.findByIdAndUser(request.getCategoryGroupId(), user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category group not found"));

        Category category = Category.builder()
                .user(user)
                .categoryGroup(group)
                .name(request.getName().trim())
                .icon(request.getIcon() != null ? request.getIcon() : "Circle")
                .color(request.getColor() != null ? request.getColor() : group.getColor())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .note(request.getNote())
                .build();

        Category saved = categoryRepository.save(category);
        return CategoryResponse.from(saved);
    }

    @Transactional
    public CategoryResponse updateCategory(User user, Long id, CategoryRequest request) {
        Category category = categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        if (request.getCategoryGroupId() != null && !request.getCategoryGroupId().equals(category.getCategoryGroup().getId())) {
            CategoryGroup newGroup = categoryGroupRepository.findByIdAndUser(request.getCategoryGroupId(), user)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target category group not found"));
            category.setCategoryGroup(newGroup);
        }

        category.setName(request.getName().trim());
        if (request.getIcon() != null) category.setIcon(request.getIcon());
        if (request.getColor() != null) category.setColor(request.getColor());
        if (request.getSortOrder() != null) category.setSortOrder(request.getSortOrder());
        if (request.getNote() != null) category.setNote(request.getNote());

        Category updated = categoryRepository.save(category);
        return CategoryResponse.from(updated);
    }

    @Transactional
    public void deleteCategory(User user, Long id) {
        Category category = categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        categoryRepository.delete(category);
    }

    @Transactional
    public CategoryResponse moveCategoryToGroup(User user, Long id, Long targetGroupId) {
        Category category = categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        CategoryGroup newGroup = categoryGroupRepository.findByIdAndUser(targetGroupId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target category group not found"));

        category.setCategoryGroup(newGroup);
        Category updated = categoryRepository.save(category);
        return CategoryResponse.from(updated);
    }
}
