package com.finsight.util;

import com.finsight.exception.UnauthorizedException;
import com.finsight.model.User;
import com.finsight.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Utility to extract the currently authenticated user's details
 * from the Spring Security context.
 */
@Component
public class SecurityUtil {

    private final UserRepository userRepository;

    public SecurityUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns the ID of the currently authenticated user.
     * Extracts the email from SecurityContext and looks up the user.
     *
     * @throws UnauthorizedException if no authenticated user found
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("No authenticated user in security context");
        }
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found in database"));

        return user.getId();
    }

    /** Returns the email of the currently authenticated user. */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("No authenticated user in security context");
        }
        return ((UserDetails) authentication.getPrincipal()).getUsername();
    }
}

