package com.faculty_evaluation_backend.fes.config.email;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "email")
public class EmailProperties {

    private List<EmailAccount> accounts;

    private Smtp smtp = new Smtp();

    @Getter
    @Setter
    public static class Smtp {

        private String host;

        private int port;

        private boolean auth;

        private boolean starttls;
    }
}
