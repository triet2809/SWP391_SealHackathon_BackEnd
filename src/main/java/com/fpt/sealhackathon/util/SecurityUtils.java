package com.fpt.sealhackathon.util;

import com.fpt.sealhackathon.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Extracts the authenticated caller's UUID from the Spring Security context.
 *
 * <p>Supports two principal name formats:
 * <ul>
 *   <li>UUID string — used when BE2 JWT filter stores user ID directly as principal name.</li>
 *   <li>Email string — used for HTTP Basic Auth testing (principal name = email from seed data).</li>
 * </ul>
 *
 * <p>Configure HTTP Basic for local testing in application.properties:
 * <pre>
 *   spring.security.user.name=ec.seal@fpt.edu.vn
 *   spring.security.user.password=test123
 * </pre>
 * Then send: {@code Authorization: Basic <base64(email:password)>}
 */
@Component
public class SecurityUtils {

    private static UserRepository userRepository;

    public SecurityUtils(UserRepository userRepository) {
        SecurityUtils.userRepository = userRepository;
    }

    /**
     * Returns the authenticated caller's UUID.
     * Accepts principal name as either a UUID or an email address.
     *
     * @throws IllegalStateException if unauthenticated or principal cannot be resolved
     */
    public static UUID getCallerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated principal");
        }

        String name = auth.getName();

        if (name == null || name.equals("anonymousUser")) {
            throw new IllegalStateException("Request is not authenticated");
        }

        // Try UUID first — this is what the real JWT filter (BE2) will set
        try {
            return UUID.fromString(name);
        } catch (IllegalArgumentException ignored) {
            // Not a UUID — fall through to email lookup
        }

        // Email lookup — used during local HTTP Basic Auth testing
        return userRepository.findByEmail(name)
                .map(user -> user.getId())
                .orElseThrow(() -> new IllegalStateException("No user found with email: " + name));
    }
}