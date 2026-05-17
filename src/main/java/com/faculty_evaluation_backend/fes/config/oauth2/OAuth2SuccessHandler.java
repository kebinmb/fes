package com.faculty_evaluation_backend.fes.config.oauth2;

import com.faculty_evaluation_backend.fes.config.jwt.JwtConfig;
import com.faculty_evaluation_backend.fes.entities.authentication.UserAccounts;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Role;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import com.faculty_evaluation_backend.fes.exceptions.UnauthorizedException;
import com.faculty_evaluation_backend.fes.repositories.authentication.UserAccountsRepository;
import com.faculty_evaluation_backend.fes.services.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserAccountsRepository userAccountsRepository;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        List<Role> allowedRoles = List.of(Role.DEAN, Role.PROGRAM_CHAIR, Role.ADMIN);
        UserAccounts user = userAccountsRepository.findByEmailAndRoleInAndStatus(email, allowedRoles, Status.ACTIVE).orElseThrow(() -> new UnauthorizedException("Unauthorized Google Account"));
        if (!Boolean.TRUE.equals(user.getIsEnabled())) {
            throw new UnauthorizedException("Account disabled");
        }

        if (Boolean.TRUE.equals(user.getIsLocked())) {
            throw new UnauthorizedException("Account locked");
        }
        String accessToken;
        String refreshToken;

        String accessCookieName;
        String refreshCookieName;

        if (user.getRole() == Role.ADMIN) {

            accessToken = jwtService.generateAccessTokenForAdministrator(user.getUserId());

            refreshToken = jwtService.generateRefreshTokenForAdministrator(user.getUserId());

            accessCookieName = "administrator_access";

            refreshCookieName = "administrator_refresh";

        } else {

            accessToken = jwtService.generateAccessTokenForSupervisor(user.getUserId());

            refreshToken = jwtService.generateRefreshTokenForSupervisor(user.getUserId());

            accessCookieName = "supervisor_access";

            refreshCookieName = "supervisor_refresh";
        }

        ResponseCookie accessCookie = ResponseCookie.from(accessCookieName, accessToken).httpOnly(true).secure(false).path("/").sameSite("Lax").maxAge(Math.max(1, jwtConfig.getExpiration() / 1000)).build();

        ResponseCookie refreshCookie = ResponseCookie.from(refreshCookieName, refreshToken).httpOnly(true).secure(false).path("/").sameSite("Lax").maxAge(jwtConfig.getRefreshExpiration() / 1000).build();

        response.addHeader("Set-Cookie", accessCookie.toString());

        response.addHeader("Set-Cookie", refreshCookie.toString());

        response.sendRedirect("https://dev-feva.chmsu.edu.ph/oauth-succes");
    }
}


