package com.finsight.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.finsight.config.AuditLogFilter;

/**
 * Spring Security configuration:
 * - Stateless session (JWT)
 * - CORS enabled for frontend
 * - Public auth endpoints
 * - All other endpoints require authentication
 * - BCrypt password encoding
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CorsConfigurationSource corsConfigurationSource;
    private final RateLimitingFilter rateLimitingFilter;
    private final AuditLogFilter auditLogFilter;
    private final IdempotencyFilter idempotencyFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                          CorsConfigurationSource corsConfigurationSource,
                          RateLimitingFilter rateLimitingFilter,
                          AuditLogFilter auditLogFilter,
                          IdempotencyFilter idempotencyFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.corsConfigurationSource = corsConfigurationSource;
        this.rateLimitingFilter = rateLimitingFilter;
        this.auditLogFilter = auditLogFilter;
        this.idempotencyFilter = idempotencyFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        // Swagger / OpenAPI docs
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        // Actuator health endpoint
                        .requestMatchers("/actuator/health").permitAll()
                        // All other requests require authentication
                        .anyRequest().authenticated()
                );

        // Add rate limiter before JWT filter
        http.addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class);
        // Add JWT filter before the standard username/password filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        // Add audit filter after JWT filter so SecurityContext is available
        http.addFilterAfter(auditLogFilter, JwtAuthenticationFilter.class);
        // Add idempotency filter after JWT (needs authenticated user)
        http.addFilterAfter(idempotencyFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    /** Prevent AuditLogFilter from being auto-registered as a servlet filter. */
    @Bean
    public FilterRegistrationBean<AuditLogFilter> auditLogFilterRegistration() {
        FilterRegistrationBean<AuditLogFilter> reg = new FilterRegistrationBean<>(auditLogFilter);
        reg.setEnabled(false);
        return reg;
    }

    /** Prevent IdempotencyFilter from being auto-registered as a servlet filter. */
    @Bean
    public FilterRegistrationBean<IdempotencyFilter> idempotencyFilterRegistration() {
        FilterRegistrationBean<IdempotencyFilter> reg = new FilterRegistrationBean<>(idempotencyFilter);
        reg.setEnabled(false);
        return reg;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
