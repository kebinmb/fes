package com.faculty_evaluation_backend.fes.repositories.authentication;


import com.faculty_evaluation_backend.fes.entities.authentication.UserLoginOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserLoginOtpRepository extends JpaRepository<UserLoginOtp, Long> {

    /**
     * Find the most recent valid OTP for a user by email
     */
    @Query("SELECT o FROM UserLoginOtp o " +
           "WHERE o.userAccount.email = :email " +
           "AND o.used = false " +
           "AND o.expiresAt > :now " +
           "ORDER BY o.createdAt DESC")
    Optional<UserLoginOtp> findLatestValidOtpByEmail(
            @Param("email") String email,
            @Param("now") Instant now
    );

    /**
     * Find a specific OTP by email and OTP code
     */
    @Query("SELECT o FROM UserLoginOtp o " +
           "WHERE o.userAccount.email = :email " +
           "AND o.otpCode = :otpCode " +
           "AND o.used = false " +
           "ORDER BY o.createdAt DESC")
    Optional<UserLoginOtp> findByEmailAndOtpCode(
            @Param("email") String email,
            @Param("otpCode") String otpCode
    );

    /**
     * Find all OTPs for a user by email
     */
    @Query("SELECT o FROM UserLoginOtp o WHERE o.userAccount.email = :email ORDER BY o.createdAt DESC")
    List<UserLoginOtp> findAllByEmail(@Param("email") String email);

    /**
     * Delete expired OTPs
     */
    @Query("DELETE FROM UserLoginOtp o WHERE o.expiresAt < :now")
    void deleteExpiredOtps(@Param("now") Instant now);

    /**
     * Count active OTPs for a user
     */
    @Query("SELECT COUNT(o) FROM UserLoginOtp o " +
           "WHERE o.userAccount.email = :email " +
           "AND o.used = false " +
           "AND o.expiresAt > :now")
    long countActiveOtpsByEmail(
            @Param("email") String email,
            @Param("now") Instant now
    );

    /**
     * Find all unused OTPs for a user
     */
    @Query("SELECT o FROM UserLoginOtp o " +
           "WHERE o.userAccount.email = :email " +
           "AND o.used = false " +
           "ORDER BY o.createdAt DESC")
    List<UserLoginOtp> findUnusedOtpsByEmail(@Param("email") String email);
}

