package com.finpilot.category.service;

import com.finpilot.category.dto.CategoryGroupRequest;
import com.finpilot.category.dto.CategoryGroupResponse;
import com.finpilot.category.entity.CategoryGroup;
import com.finpilot.category.repository.CategoryGroupRepository;
import com.finpilot.category.repository.CategoryRepository;
import com.finpilot.user.entity.AuthProvider;
import com.finpilot.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryGroupServiceTest {

    @Mock
    private CategoryGroupRepository categoryGroupRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryGroupService categoryGroupService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Rushikesh")
                .email("rushi@example.com")
                .authProvider(AuthProvider.LOCAL)
                .build();
    }

    @Test
    void testCreateCategoryGroup() {
        CategoryGroupRequest req = CategoryGroupRequest.builder()
                .name("Needs")
                .icon("ShoppingCart")
                .color("#F59E0B")
                .sortOrder(1)
                .build();

        CategoryGroup savedGroup = CategoryGroup.builder()
                .id(10L)
                .user(user)
                .name("Needs")
                .icon("ShoppingCart")
                .color("#F59E0B")
                .sortOrder(1)
                .isCollapsed(false)
                .build();

        when(categoryGroupRepository.save(any(CategoryGroup.class))).thenReturn(savedGroup);

        CategoryGroupResponse res = categoryGroupService.createCategoryGroup(user, req);

        assertNotNull(res);
        assertEquals(10L, res.getId());
        assertEquals("Needs", res.getName());
        verify(categoryGroupRepository, times(1)).save(any(CategoryGroup.class));
    }

    @Test
    void testCreateDefaultCategoryTemplateIfEmpty() {
        when(categoryGroupRepository.findByUserOrderBySortOrderAscIdAsc(user)).thenReturn(Collections.emptyList());
        when(categoryGroupRepository.save(any(CategoryGroup.class))).thenAnswer(i -> {
            CategoryGroup cg = i.getArgument(0);
            cg.setId(99L);
            return cg;
        });

        categoryGroupService.createDefaultCategoryTemplateIfEmpty(user);

        verify(categoryGroupRepository, times(5)).save(any(CategoryGroup.class));
        verify(categoryRepository, atLeast(10)).save(any());
    }
}
