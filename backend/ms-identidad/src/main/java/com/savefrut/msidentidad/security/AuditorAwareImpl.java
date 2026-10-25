package com.savefrut.msidentidad.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("No authenticated user found for audit, returning 'SYSTEM'");
            return Optional.of("SYSTEM");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetailsImpl userDetails) {
            String email = userDetails.getUsername();
            log.debug("Auditing operation for user: {}", email);
            return Optional.of(email);
        }

        if (principal instanceof String stringPrincipal) {
            return Optional.of(stringPrincipal);
        }

        log.warn("Unknown principal type for auditing: {}", principal.getClass().getName());
        return Optional.of("UNKNOWN");
    }
}
