package com.finpilot.target.repository;

import com.finpilot.category.entity.Category;
import com.finpilot.target.entity.Target;
import com.finpilot.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TargetRepository extends JpaRepository<Target, Long> {

    Optional<Target> findByCategory(Category category);

    Optional<Target> findByCategoryIdAndCategoryUser(Long categoryId, User user);

    List<Target> findByCategoryIn(Collection<Category> categories);

    Optional<Target> findByIdAndCategoryUser(Long id, User user);
}
