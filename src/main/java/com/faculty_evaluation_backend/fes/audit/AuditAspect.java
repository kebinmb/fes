package com.faculty_evaluation_backend.fes.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.faculty_evaluation_backend.fes.dto.authentication.GoogleLoginRequest;
import com.faculty_evaluation_backend.fes.dto.authentication.LoginRequest;
import com.faculty_evaluation_backend.fes.dto.authentication.StudentAccessCodeRequest;
import com.faculty_evaluation_backend.fes.dto.authentication.StudentLoginRequest;
import com.faculty_evaluation_backend.fes.entities.authentication.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {
    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest request;
    private final ObjectMapper objectMapper;

    @Around("@annotation(auditableAction)")
    public Object logAudit(ProceedingJoinPoint joinPoint,
                           AuditableAction auditableAction) throws Throwable{
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(auditableAction.action());
        auditLog.setEntityType(auditableAction.entity());
        auditLog.setRequestPath(request.getRequestURI());
        auditLog.setRequestMethod(request.getMethod());
        auditLog.setIpAddress(request.getRemoteAddr());
        auditLog.setUserAgent(request.getHeader("User-Agent"));
        auditLog.setCreatedAt(Instant.now());
        setUserInformation(auditLog);
        long start = System.currentTimeMillis();
        try{
            Object result = joinPoint.proceed();
            auditLog.setAction(auditLog.getAction() + "_SUCCESS");
            enrichUserInformationFromResult(auditLog, result);
            if (!applyChangeDetails(auditLog, result)) {
                auditLog.setNewValue(
                        objectMapper.writeValueAsString(
                                Map.of("status", "success")
                        )
                );
            }
            return result;
        }catch (Exception ex){
            auditLog.setAction(auditLog.getAction() + "_FAILED");
            enrichUserInformationFromArgs(auditLog, joinPoint.getArgs());
            auditLog.setNewValue(objectMapper.writeValueAsString(Map.of(
                    "error", ex.getClass().getSimpleName(),
                    "message", safeMessage(ex)
            )));
            throw ex;
        }finally {
            long duration = System.currentTimeMillis() - start;
            auditLog.setExecutionTimeMs(duration);
            try {
                auditLogRepository.save(auditLog);
            } catch (Exception auditException) {
                log.warn("Failed to persist audit log for action {}: {}",
                        auditableAction.action(),
                        auditException.getMessage());
            }
        }
    }

    private void setUserInformation(AuditLog log){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return;
        }
        String username = authentication.getName();
        log.setUsername(username);

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            setNumericUserId(log, userDetails.getUserId());
            log.setUsername(userDetails.getUsername());
            return;
        }

        try {
            log.setUserId(Integer.valueOf(username));
        } catch (NumberFormatException ignored) {
            // Some authentication paths use usernames instead of numeric IDs.
        }
    }

    private boolean applyChangeDetails(AuditLog auditLog, Object result)
            throws Exception {
        Object body = result instanceof ResponseEntity<?> responseEntity
                ? responseEntity.getBody()
                : result;

        if (!(body instanceof AuditableChange change)) {
            return false;
        }

        Long entityId = change.auditEntityId();
        if (entityId != null
                && entityId <= Integer.MAX_VALUE
                && entityId >= Integer.MIN_VALUE) {
            auditLog.setEntityId(entityId.intValue());
        }

        auditLog.setOldValue(
                objectMapper.writeValueAsString(change.auditOldValue())
        );
        auditLog.setNewValue(
                objectMapper.writeValueAsString(change.auditNewValue())
        );
        return true;
    }

    private void setNumericUserId(AuditLog log, Long userId) {
        if (userId == null
                || userId > Integer.MAX_VALUE
                || userId < Integer.MIN_VALUE) {
            return;
        }

        log.setUserId(userId.intValue());
    }

    private String safeMessage(Exception ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            return "No error message provided";
        }
        return message.length() <= 500 ? message : message.substring(0, 500);
    }

    private void enrichUserInformationFromResult(AuditLog auditLog, Object result) {
        Object body = result instanceof ResponseEntity<?> responseEntity
                ? responseEntity.getBody()
                : result;

        if (body instanceof Map<?, ?> map) {
            if (auditLog.getUsername() == null) {
                if (map.containsKey("email")) {
                    auditLog.setUsername(String.valueOf(map.get("email")));
                } else if (map.containsKey("username")) {
                    auditLog.setUsername(String.valueOf(map.get("username")));
                } else if (map.containsKey("studentId")) {
                    auditLog.setUsername(String.valueOf(map.get("studentId")));
                }
            }

            if (auditLog.getUserId() == null) {
                Object idObj = map.get("userId");
                if (idObj == null) {
                    idObj = map.get("evaluatorId");
                }
                if (idObj == null) {
                    idObj = map.get("administratorId");
                }
                if (idObj != null) {
                    try {
                        int parsedId = Integer.parseInt(idObj.toString());
                        auditLog.setUserId(parsedId);
                        if (auditLog.getEntityId() == null) {
                            auditLog.setEntityId(parsedId);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
    }

    private void enrichUserInformationFromArgs(AuditLog auditLog, Object[] args) {
        if (args == null || auditLog.getUsername() != null) {
            return;
        }

        for (Object arg : args) {
            if (arg instanceof LoginRequest loginRequest) {
                auditLog.setUsername(loginRequest.getUsernameOrEmail());
                break;
            } else if (arg instanceof StudentLoginRequest studentLoginRequest) {
                auditLog.setUsername(studentLoginRequest.getStudentId());
                break;
            } else if (arg instanceof StudentAccessCodeRequest accessCodeRequest) {
                auditLog.setUsername(accessCodeRequest.getStudentId());
                break;
            } else if (arg instanceof GoogleLoginRequest) {
                auditLog.setUsername("Google Sign-In Attempt");
                break;
            }
        }
    }
}
