package com.bkap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@Configuration
public class SecurityRepositoryConfig {

    @Bean
    @Qualifier("adminSecurityContextRepository")
    public SecurityContextRepository adminSecurityContextRepository() {
        HttpSessionSecurityContextRepository repo = new HttpSessionSecurityContextRepository();
        repo.setSpringSecurityContextKey("SPRING_SECURITY_CONTEXT_ADMIN");
        return repo;
    }

    @Bean
    @Qualifier("userSecurityContextRepository")
    public SecurityContextRepository userSecurityContextRepository() {
        HttpSessionSecurityContextRepository repo = new HttpSessionSecurityContextRepository();
        repo.setSpringSecurityContextKey("SPRING_SECURITY_CONTEXT_USER");
        return repo;
    }
}

