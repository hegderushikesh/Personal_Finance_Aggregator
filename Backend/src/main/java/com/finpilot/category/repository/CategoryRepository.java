package com.finpilot.category.repository;

import com.finpilot.category.entity.Category;
import com.finpilot.category.entity.CategoryGroup;
import com.finpilot.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserOrderBySortOrderAscIdAsc(User user);

    List<Category> findByUserAndCategoryGroupOrderBySortOrderAscIdAsc(User user, CategoryGroup categoryGroup);

    Optional<Category> findByIdAndUser(Long id, User user);
}
