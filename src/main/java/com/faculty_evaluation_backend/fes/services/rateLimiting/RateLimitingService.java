package com.faculty_evaluation_backend.fes.services.rateLimiting;

import com.faculty_evaluation_backend.fes.audit.AuditService;
import com.faculty_evaluation_backend.fes.exceptions.RateLimitExceededException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service
public class RateLimitingService {
    private static final Set<String> DEFAULT_TRUSTED_PROXIES = Set.of(
            "127.0.0.1",
            "0:0:0:0:0:0:0:1",
            "::1"
    );

    private final AuditService auditService;
    private final HttpServletRequest httpServletRequest;
    private final Set<String> trustedProxies;
    private final Cache<String, Bucket> buckets;

    public RateLimitingService(
            AuditService auditService,
            HttpServletRequest httpServletRequest,
            @Value("${app.security.rate-limiting.trusted-proxies:}") String configuredProxies
    ) {
        this.auditService = auditService;
        this.httpServletRequest = httpServletRequest;
        this.trustedProxies = new HashSet<>(DEFAULT_TRUSTED_PROXIES);
        if (configuredProxies != null && !configuredProxies.isBlank()) {
            Arrays.stream(configuredProxies.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(this.trustedProxies::add);
        }
        this.buckets = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterAccess(Duration.ofMinutes(10))
                .build();
    }

    public void consumeStudentRequest(String studentId, String endpoint) {
        String ip = getClientIp();
        String key = buildStudentKey(studentId, ip, endpoint);
        Bucket bucket = Objects.requireNonNull(
                buckets.get(key, k -> createBucket(endpoint)),
                "Bucket must not be null"
        );
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            String actor = (studentId != null ? studentId : "ANON:" + ip);
            long waitSeconds = Math.max(1, probe.getNanosToWaitForRefill() / 1_000_000_000);
            auditService.log(
                    actor,
                    "RATE_LIMIT_EXCEEDED: " + endpoint +
                            " remaining=" + probe.getRemainingTokens(),
                    ip
            );
            throw new RateLimitExceededException("Too many requests. Try again in " + waitSeconds + " seconds.");
        }
    }

    public void consumeFacultyRequest(String username, String endpoint) {
        String ip = getClientIp();
        String key = buildSupervisorKey(username, ip, endpoint);
        Bucket bucket = Objects.requireNonNull(
                buckets.get(key, k -> createBucket(endpoint)),
                "Bucket must not be null"
        );
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            String actor = (username != null ? username : "ANON:" + ip);
            long waitSeconds = Math.max(1, probe.getNanosToWaitForRefill() / 1_000_000_000);
            auditService.log(
                    actor,
                    "RATE_LIMIT_EXCEEDED: " + endpoint +
                            " remaining=" + probe.getRemainingTokens(),
                    ip
            );
            throw new RateLimitExceededException("Too many requests. Try again in " + waitSeconds + " seconds.");
        }
    }

    private String getClientIp() {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        if (remoteAddr == null || remoteAddr.isBlank()) {
            return "UNKNOWN";
        }

        if (isTrustedProxy(remoteAddr)) {
            String xfHeader = httpServletRequest.getHeader("X-Forwarded-For");
            if (xfHeader != null && !xfHeader.isBlank()) {
                String clientIp = xfHeader.split(",")[0].trim();
                if (!clientIp.isEmpty()) {
                    return clientIp;
                }
            }
        }
        return remoteAddr;
    }

    private boolean isTrustedProxy(String ip) {
        return trustedProxies.contains(ip.trim());
    }

    private String buildStudentKey(String studentId, String ip, String endpoint) {
        String userKey = (studentId != null ? studentId : ip);
        return userKey + ":" + ip + ":" + endpoint;
    }

    private String buildSupervisorKey(String username, String ip, String endpoint) {
        String supervisorKey = (username != null ? username : ip);
        return supervisorKey + ":" + ip + ":" + endpoint;
    }

    private Bucket createBucket(String endpoint) {
        return switch (endpoint) {
            case "GENERATE_ACCESS_CODE" -> newBucket(3, 1, Duration.ofMinutes(1));
            case "AUTHENTICATE_STUDENT", "AUTHENTICATE_SUPERVISOR", "AUTHENTICATE_ADMINISTRATOR" ->
                    newBucket(10, 5, Duration.ofMinutes(1));
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
