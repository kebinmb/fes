package com.faculty_evaluation_backend.fes.config.email;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
@Component
@RequiredArgsConstructor
public class EmailAccountPool {

    private final EmailProperties emailProperties;

    private final AtomicInteger counter =
            new AtomicInteger(0);

    public EmailAccount getNext() {

        List<EmailAccount> accounts =
                emailProperties.getAccounts();

        int index =
                Math.abs(
                        counter.getAndIncrement()
                );

        return accounts.get(
                index % accounts.size()
        );
    }

    public List<EmailAccount> getAll() {

        return emailProperties.getAccounts();
    }
}