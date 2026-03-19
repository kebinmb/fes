package com.faculty_evaluation_backend.fes.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {
    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest request;
    private final ObjectMapper objectMapper;

    @Around("@annotation(auditableAction)")
    public Object logAudit(ProceedingJoinPoint joinPoint,
                           AuditableAction auditableAction) throws Throwable{
        AuditLog log = new AuditLog();
        log.setAction(auditableAction.action());
        log.setEntityType(auditableAction.entity());
        log.setRequestPath(request.getRequestURI());
        log.setRequestMethod(request.getMethod());
        log.setIpAddress(request.getRemoteAddr());
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setCreatedAt(Instant.now());
        setUserInformation(log);
        long start = System.currentTimeMillis();
        try{
            Object result = joinPoint.proceed();
            log.setAction(log.getAction() + "_SUCESS");
            log.setNewValue(objectMapper.writeValueAsString(Map.of("status","success")));
            return result;
        }catch (Exception ex){
            log.setAction(log.getAction() + "_FAILED");
            log.setNewValue(objectMapper.writeValueAsString(Map.of("error",ex.getMessage())));
            throw ex;
        }finally {
            long duration = System.currentTimeMillis() - start;
            log.setExecutionTimeMs(duration);
            auditLogRepository.save(log);
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
        //Check student or faculty if it exists in the database
        //Logic is faculty or supervisor logs in using username and password. student logs in using access code
    }
}
