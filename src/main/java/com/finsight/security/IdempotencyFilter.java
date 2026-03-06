package com.finsight.security;

import com.finsight.model.IdempotencyRecord;
import com.finsight.model.User;
import com.finsight.repository.IdempotencyRepository;
import com.finsight.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Idempotency filter for POST endpoints.
 * When a request includes an X-Idempotency-Key header, the filter checks if that key
 * has been seen before for this user+endpoint. If so, it returns the cached response.
 * Otherwise, it processes the request and stores the response for future deduplication.
 */
@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Idempotency-Key";

    private final IdempotencyRepository idempotencyRepository;
    private final UserRepository userRepository;

    public IdempotencyFilter(IdempotencyRepository idempotencyRepository, UserRepository userRepository) {
        this.idempotencyRepository = idempotencyRepository;
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Only apply to POST requests that carry the idempotency header
        return !"POST".equalsIgnoreCase(request.getMethod())
                || request.getHeader(HEADER) == null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String key = request.getHeader(HEADER);
        String endpoint = request.getRequestURI();
        Long userId = resolveUserId();

        if (userId == null) {
            // Unauthenticated — let the request proceed (auth filter will reject if needed)
            chain.doFilter(request, response);
            return;
        }

        // Check for existing record
        Optional<IdempotencyRecord> existing =
                idempotencyRepository.findByIdempotencyKeyAndUserIdAndEndpoint(key, userId, endpoint);

        if (existing.isPresent()) {
            IdempotencyRecord record = existing.get();
            response.setStatus(record.getStatusCode());
            response.setContentType("application/json");
            if (record.getResponseBody() != null) {
                response.getWriter().write(record.getResponseBody());
            }
            return;
        }

        // Wrap response to capture body
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        chain.doFilter(request, wrappedResponse);

        // Store the result
        String body = new String(wrappedResponse.getContentAsByteArray(), StandardCharsets.UTF_8);
        IdempotencyRecord record = new IdempotencyRecord();
        record.setIdempotencyKey(key);
        record.setUserId(userId);
        record.setEndpoint(endpoint);
        record.setResponseBody(body);
        record.setStatusCode(wrappedResponse.getStatus());
        idempotencyRepository.save(record);

        wrappedResponse.copyBodyToResponse();
    }

    private Long resolveUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        String email = ((UserDetails) auth.getPrincipal()).getUsername();
        return userRepository.findByEmail(email).map(User::getId).orElse(null);
    }
}
