package com.faculty_evaluation_backend.fes.services.rateLimiting;

import com.faculty_evaluation_backend.fes.audit.AuditService;
import com.faculty_evaluation_backend.fes.exceptions.RateLimitExceededException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RateLimitingService {
    private final AuditService auditService;
    private final HttpServletRequest httpServletRequest;
    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterAccess(Duration.ofMinutes(10))
            .build();

    public void consumeStudentRequest(String studentId, String endpoint){
        String ip = getClientIp();
        String key = buildStudentKey(studentId, ip, endpoint);
        Bucket bucket = Objects.requireNonNull(
                buckets.get(key, k -> createBucket(endpoint)),
                "Bucket must not be null"
        );
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if(!probe.isConsumed()){
            String actor = (studentId != null ? studentId : "ANON:" + ip);
            long waitSeconds =Math.max(1, probe.getNanosToWaitForRefill() / 1_000_000_000);
            auditService.log(
                    actor,
                    "RATE_LIMIT_EXCEEDED: " + endpoint +
                    " remaining=" + probe.getRemainingTokens(),
                    ip
            );
            throw new RateLimitExceededException("Too many requests. Try again in " + waitSeconds + " seconds.");
        }
    }
    public void consumeFacultyRequest(String username, String endpoint){
        String ip = getClientIp();
        String key = buildSupervisorKey(username,ip,endpoint);
        Bucket bucket = Objects.requireNonNull(
                buckets.get(key,k->createBucket(endpoint)),
                "Bucket must not be null"
        );
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if(!probe.isConsumed()){
            String actor = (username != null ? username : "ANON:" + ip);
            long waitSeconds = Math.max(1, probe.getNanosToWaitForRefill()/1_000_000_000);
            auditService.log(
                    actor,
                    "RATE_LIMIT_EXCEEDED: " + endpoint +
                            " remaining=" + probe.getRemainingTokens(),
                    ip
            );
            throw new RateLimitExceededException("Too many requests. Try again in " + waitSeconds + " seconds.");
        }
    }
    private String getClientIp(){
        String xfHeader = httpServletRequest.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isBlank()){
            return xfHeader.split(",")[0];
        }
        return httpServletRequest.getRemoteAddr();
    }
    private String buildStudentKey(String studentId, String ip, String endpoint){
        String userKey = (studentId != null ? studentId : ip);
        return (userKey)
                + ":" + ip
                + ":" + endpoint;
    }
    private String buildSupervisorKey(String username, String ip, String endpoint){
        String supervisorKey = (username != null ? username : ip);
        return (supervisorKey)
                + ":" + ip
                + ":" + endpoint;
    }
    private Bucket createBucket(String endpoint){
        return switch (endpoint) {
            case "GENERATE_ACCESS_CODE" -> newBucket(3, 1, Duration.ofMinutes(1));
            case "AUTHENTICATE_STUDENT", "AUTHENTICATE_SUPERVISOR", "AUTHENTICATE_ADMINISTRATOR" -> newBucket(10, 5, Duration.ofMinutes(1));
            default -> newBucket(20, 10, Duration.ofMinutes(1));
        };
    }

    private Bucket newBucket(int capacity, int refillTokens, Duration duration) {
        return Bucket.builder()
                .addLimit(
                        Bandwidth.builder()
                                .capacity(capacity)
                                .refillGreedy(refillTokens, duration)
                                .build()
                )
                .addLimit(Bandwidth.builder()
                        .capacity(capacity * 3L)
                        .refillGreedy(refillTokens, Duration.ofMinutes(10))
                        .build())
                .build();
    }



}
