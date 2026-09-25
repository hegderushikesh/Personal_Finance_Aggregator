package com.finpilot.category.repository;

import com.finpilot.category.entity.CategoryGroup;
import com.finpilot.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryGroupRepository extends JpaRepository<CategoryGroup, Long> {

    List<CategoryGroup> findByUserOrderBySortOrderAscIdAsc(User user);

    Optional<CategoryGroup> findByIdAndUser(Long id, User user);
}
