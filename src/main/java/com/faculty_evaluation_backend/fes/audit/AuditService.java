package com.faculty_evaluation_backend.fes.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public void log( String username, String action, String ip){
        auditLogRepository.save(
                AuditLog.builder()
                        .username(username)
                        .action(action)
                        .ipAddress(ip)
                        .createdAt(Instant.now())
                        .build()
        );
    }
}
