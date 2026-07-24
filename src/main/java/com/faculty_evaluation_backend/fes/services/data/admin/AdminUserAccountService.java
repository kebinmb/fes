package com.faculty_evaluation_backend.fes.services.data.admin;

import com.faculty_evaluation_backend.fes.dto.response.FetchUserAccountsResponse;
import com.faculty_evaluation_backend.fes.dto.student.PageResponse;
import com.faculty_evaluation_backend.fes.dto.user_accounts.CreateUserAccountDTO;
import com.faculty_evaluation_backend.fes.dto.user_accounts.UpdateUserAccountDTO;
import com.faculty_evaluation_backend.fes.dto.user_accounts.UpdateUserPasswordDTO;
import com.faculty_evaluation_backend.fes.entities.authentication.UserAccounts;
import com.faculty_evaluation_backend.fes.exceptions.BadRequestException;
import com.faculty_evaluation_backend.fes.exceptions.ResourceNotFoundException;
import com.faculty_evaluation_backend.fes.repositories.authentication.UserAccountsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
class AdminUserAccountService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    private final UserAccountsRepository userAccountsRepository;
    private final PasswordEncoder passwordEncoder;

    PageResponse<FetchUserAccountsResponse> accountList(int page, int size, String search) {
        Page<UserAccounts> userAccountsPage = userAccountsRepository.searchUserAccounts(
                normalizeOptional(search),
                PageRequest.of(
                        safePage(page),
                        safePageSize(size),
                        Sort.by(Sort.Order.asc("username"), Sort.Order.asc("userId"))
                )
        );

        List<FetchUserAccountsResponse> responseList = userAccountsPage.getContent()
                .stream()
                .map(user -> FetchUserAccountsResponse.builder()
                        .userId(user.getUserId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .status(user.getStatus())
                        .college(user.getCollege())
                        .programs(user.getPrograms())
                        .majors(user.getMajors())
                        .isEnabled(user.getIsEnabled())
                        .isLocked(user.getIsLocked())
                        .lastLoginAt(user.getLastLoginAt())
                        .dataSource(user.getDataSource())
                        .build())
                .toList();

        return PageResponse.<FetchUserAccountsResponse>builder()
                .content(responseList)
                .page(userAccountsPage.getNumber())
                .size(userAccountsPage.getSize())
                .totalElements(userAccountsPage.getTotalElements())
                .totalPages(userAccountsPage.getTotalPages())
                .build();
    }

    String createUserAccount(CreateUserAccountDTO dto) {
        validateCreateUser(dto);

        if (userAccountsRepository.existsByUsername(dto.getUsername().trim())) {
            throw new BadRequestException("Username already exists.");
        }

        if (userAccountsRepository.existsByEmail(dto.getEmail().trim())) {
            throw new BadRequestException("Email already exists.");
        }

        UserAccounts user = UserAccounts.builder()
                .username(dto.getUsername().trim())
                .email(dto.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .college(dto.getCollege())
                .programs(dto.getPrograms())
                .majors(dto.getMajors())
                .dataSource(normalizeOptional(dto.getDataSource()))
                .status(dto.getStatus())
                .isEnabled(true)
                .isLocked(false)
                .build();

        userAccountsRepository.save(user);

        return "User account created successfully.";
    }

    String updateUserAccount(UpdateUserAccountDTO dto) {
        validateUpdateUser(dto);

        UserAccounts existingUser = userAccountsRepository
                .findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        Optional<UserAccounts> usernameCheck =
                userAccountsRepository.findByUsername(dto.getUsername());

        if (usernameCheck.isPresent()
                && !usernameCheck.get().getUserId().equals(dto.getUserId())) {
            throw new BadRequestException("Username already exists.");
        }

        Optional<UserAccounts> emailCheck =
                userAccountsRepository.findByEmail(dto.getEmail());

        if (emailCheck.isPresent()
                && !emailCheck.get().getUserId().equals(dto.getUserId())) {
            throw new BadRequestException("Email already exists.");
        }

        existingUser.setUsername(dto.getUsername().trim());
        existingUser.setEmail(dto.getEmail().trim().toLowerCase());
        existingUser.setRole(dto.getRole());
        existingUser.setCollege(dto.getCollege());
        existingUser.setPrograms(dto.getPrograms());
        existingUser.setMajors(dto.getMajors());
        existingUser.setDataSource(normalizeOptional(dto.getDataSource()));
        existingUser.setStatus(dto.getStatus());
        existingUser.setIsEnabled(dto.getIsEnabled());
        existingUser.setIsLocked(dto.getIsLocked());

        userAccountsRepository.save(existingUser);

        return "User account updated successfully.";
    }

    String updateUserPassword(UpdateUserPasswordDTO dto) {
        if (dto.getNewPassword() == null || dto.getNewPassword().trim().isEmpty()) {
            throw new BadRequestException("Password is required.");
        }

        if (dto.getNewPassword().length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters.");
        }

        UserAccounts user = userAccountsRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));

        userAccountsRepository.save(user);

        return "Password updated successfully.";
    }

    private void validateCreateUser(CreateUserAccountDTO dto) {
        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            throw new BadRequestException("Username is required.");
        }

        if (dto.getUsername().trim().length() < 4) {
            throw new BadRequestException("Username must be at least 4 characters.");
        }

        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email is required.");
        }

        if (!dto.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new BadRequestException("Invalid email address.");
        }

        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            throw new BadRequestException("Password is required.");
        }

        if (dto.getPassword().length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters.");
        }

        validateDataSource(dto.getDataSource());
    }

    private void validateUpdateUser(UpdateUserAccountDTO dto) {
        if (dto.getUserId() == null) {
            throw new BadRequestException("User ID is required.");
        }

        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            throw new BadRequestException("Username is required.");
        }

        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email is required.");
        }

        if (!dto.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new BadRequestException("Invalid email address.");
        }

        validateDataSource(dto.getDataSource());
    }

    private void validateDataSource(String dataSource) {
        String normalized = normalizeOptional(dataSource);
        if (normalized != null && normalized.length() > 100) {
            throw new BadRequestException("Data source must not exceed 100 characters.");
        }
    }

    private String normalizeOptional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    private int safePage(int page) {
        return Math.max(page, 0);
    }

    private int safePageSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }

        return Math.min(size, MAX_PAGE_SIZE);
    }
}
