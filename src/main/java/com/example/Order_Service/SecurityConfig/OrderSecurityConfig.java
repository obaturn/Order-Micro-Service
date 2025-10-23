package com.example.Order_Service.SecurityConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import java.util.*;

@Configuration
@EnableMethodSecurity
public class OrderSecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(OrderSecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler((req, res, ex1) -> {
                            res.setStatus(HttpStatus.FORBIDDEN.value());
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"Access Denied - You don’t have permission to access this resource\"}");
                        })
                )

                .authorizeHttpRequests(auth -> auth
                        // Vendor endpoints
                        .requestMatchers("/order/vendor/**").hasRole("VENDOR")

                        // Customer endpoints
                        .requestMatchers("/order/customer/**").hasRole("CUSTOMERS")

                        // Admin endpoints
                        .requestMatchers("/order/admin/**").hasRole("ADMIN")

                        // Authenticated test endpoint
                        .requestMatchers("/auth/me").authenticated()

                        // All others are public
                        .anyRequest().permitAll()
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint((req, res, ex) -> {
                            log.error("Authentication failed: {}", ex.getMessage());
                            res.setStatus(HttpStatus.UNAUTHORIZED.value());
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"Unauthorized - Invalid or expired token\"}");
                        })
                );

        return http.build();
    }

    /**
     * ✅ Custom converter that extracts roles from Keycloak JWT
     */
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Set<GrantedAuthority> authorities = new HashSet<>();

            log.info("JWT Claims received: {}", jwt.getClaims());

            // Realm roles
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                ((List<String>) realmAccess.get("roles")).forEach(role ->
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase(Locale.ROOT)))
                );
            }

            // Client roles (e.g., product-service or order-service)
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess != null) {
                log.info("Keys found in resource_access: {}", resourceAccess.keySet());

                String clientId = jwt.getClaimAsString("azp");
                log.info("Using clientId from token (azp): {}", clientId);

                if (clientId != null && resourceAccess.containsKey(clientId)) {
                    Map<String, Object> client = (Map<String, Object>) resourceAccess.get(clientId);
                    List<String> clientRoles = (List<String>) client.get("roles");

                    if (clientRoles != null) {
                        clientRoles.forEach(role ->
                                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase(Locale.ROOT)))
                        );
                    }
                } else {
                    log.warn("No client roles found for clientId: {}", clientId);
                }
            }

            log.info("Extracted Authorities from JWT: {}", authorities);
            return authorities;
        });

        return converter;
    }
}
