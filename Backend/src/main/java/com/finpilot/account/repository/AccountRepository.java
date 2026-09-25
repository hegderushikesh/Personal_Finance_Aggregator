package com.finpilot.account.repository;

import com.finpilot.account.entity.Account;
import com.finpilot.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByUserOrderByIsActiveDescTypeAscNameAsc(User user);

    List<Account> findByUserAndIsActiveTrueOrderByNameAsc(User user);

    List<Account> findByUserOrderByNameAsc(User user);

    Optional<Account> findByIdAndUser(Long id, User user);

    boolean existsByUser(User user);

    long countByUserAndIsActiveTrue(User user);

    Optional<Account> findByExternalAccountIdAndUser(String externalAccountId, User user);

    List<Account> findByPlaidConnectionIdAndUser(Long plaidConnectionId, User user);

    List<Account> findByPlaidConnectionId(Long plaidConnectionId);
}
