package com.kshrd.kroya_api.config;

import com.kshrd.kroya_api.enums.ResponseMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
//@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;
    private final JwtService jwtService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/api/v1/fileView/**",
                                "/v2/api-docs",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-resources",
                                "/swagger-resources/**",
                                "/configuration/ui",
                                "/configuration/security",
                                "/swagger-ui/**",
                                "/webjars/**",
                                "/swagger-ui.html"
                        )
                        .permitAll()

                        // Address controller
                        .requestMatchers(HttpMethod.PUT, "/api/v1/address/update/{id}").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/address/create").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/address/{id}").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/address/list").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/address/delete/{id}").hasRole("USER")

                        // Food sell controller
                        .requestMatchers(HttpMethod.POST, "/api/v1/food-sell/post-food-sell").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/food-sell/list").hasAnyRole("GUEST", "USER")

                        // Food Recipe controller
                        .requestMatchers(HttpMethod.POST, "/api/v1/food-recipe/post-food-recipe").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/food-recipe/list").hasAnyRole("GUEST", "USER")

                        // Favorite controller
                        .requestMatchers(HttpMethod.POST, "/api/v1/favorite/add-favorite").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/favorite/remove-favorite").hasRole("USER")

                        // User controller
                        .requestMatchers(HttpMethod.GET, "/api/v1/user/favorites/food-recipes").hasRole("USER")

                        .anyRequest().authenticated())
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                .accessDeniedHandler(this::accessDeniedHandler)
                                .authenticationEntryPoint(this::unauthorizedHandler)
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(log -> log
                        .logoutUrl("/api/v1/auth/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
                );

        return http.build();
    }

    private void accessDeniedHandler(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) {
        jwtService.jwtExceptionHandler(response, ResponseMessage.FORBIDDEN);
    }

    public void unauthorizedHandler(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        jwtService.jwtExceptionHandler(response, ResponseMessage.UNAUTHORIZED);
    }
}
