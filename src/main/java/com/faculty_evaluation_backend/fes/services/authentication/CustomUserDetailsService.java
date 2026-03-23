package com.faculty_evaluation_backend.fes.services.authentication;

import com.faculty_evaluation_backend.fes.entities.authentication.CustomUserDetails;
import com.faculty_evaluation_backend.fes.entities.authentication.UserAccounts;
import com.faculty_evaluation_backend.fes.repositories.authentication.UserAccountsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserAccountsRepository userAccountsRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        UserAccounts userAccounts = userAccountsRepository.findActiveUserByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(()-> new UsernameNotFoundException("User not found with username: " + usernameOrEmail));
        return new CustomUserDetails(userAccounts);
    }
}
