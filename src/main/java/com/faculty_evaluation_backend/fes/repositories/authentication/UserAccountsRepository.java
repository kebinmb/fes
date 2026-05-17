package com.faculty_evaluation_backend.fes.repositories.authentication;


import com.faculty_evaluation_backend.fes.entities.authentication.UserAccounts;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Role;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAccountsRepository extends JpaRepository<UserAccounts, Long> {

    @Query("""
    SELECT u
    FROM UserAccounts u
    WHERE (u.username = :identifier OR u.email = :identifier)
      AND u.isEnabled = true
      AND u.isLocked = false
""")
    Optional<UserAccounts> findActiveUserByUsernameOrEmail(
            @Param("identifier") String identifier
    );
    @Query("SELECT u.role FROM UserAccounts u WHERE u.userId = :userId")
    Optional<Role> findRoleByUserId(@Param("userId") Long userId);

    Optional<UserAccounts> findUserByUserId(Long userId);
    Optional<UserAccounts> findByUsername(String username);
    Optional<UserAccounts> findByEmail(String email);
    @Query("SELECT u FROM UserAccounts u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<UserAccounts> findByUsernameOrEmail(@Param("identifier") String identifier);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<UserAccounts> findByIsEnabledTrue();
    List<UserAccounts> findByRole(Role role);
    Optional<UserAccounts> findByResetToken(String resetToken);

    @Query("SELECT u FROM UserAccounts u WHERE u.email = :email AND u.isEnabled = true AND u.isLocked = false")
    Optional<UserAccounts> findActiveUserByEmail(@Param("email") String email);

    @Query("SELECT u FROM UserAccounts u WHERE u.username = :username AND u.isEnabled = true AND u.isLocked = false")
    Optional<UserAccounts> findActiveUserByUsername(@Param("username") String username);

    Optional<UserAccounts>
    findByEmailAndRoleInAndStatus(
            String email,
            List<Role> roles,
            Status status
    );
}

