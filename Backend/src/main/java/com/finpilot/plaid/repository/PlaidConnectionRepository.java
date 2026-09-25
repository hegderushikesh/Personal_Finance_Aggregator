package com.finpilot.plaid.repository;

import com.finpilot.plaid.entity.PlaidConnection;
import com.finpilot.plaid.entity.PlaidConnectionStatus;
import com.finpilot.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaidConnectionRepository extends JpaRepository<PlaidConnection, Long> {

    List<PlaidConnection> findByUserOrderByCreatedAtDesc(User user);

    Optional<PlaidConnection> findByIdAndUser(Long id, User user);

    Optional<PlaidConnection> findByItemId(String itemId);

    Optional<PlaidConnection> findByItemIdAndUser(String itemId, User user);

    long countByUserAndStatus(User user, PlaidConnectionStatus status);
}
