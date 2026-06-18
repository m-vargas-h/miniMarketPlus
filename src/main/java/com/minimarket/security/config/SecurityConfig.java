package com.minimarket.security.config;

import com.minimarket.security.filter.JwtAuthFilter;
import com.minimarket.security.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                          JwtAuthFilter jwtAuthFilter) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
        
            // Deshabilita CSRF para APIs REST (si usas JWT, no necesitas CSRF)
            .csrf(csrf -> csrf.disable())

            // Sesión stateless — el JWT reemplaza la sesión
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Headers de seguridad HTTP
            .headers(headers -> headers
                // Evita que el navegador infiera el tipo de contenido (protección XSS)
                .contentTypeOptions(contentType -> {})
                // Permite frames solo desde el mismo origen (necesario para H2 console)
                .frameOptions(frame -> frame.sameOrigin())
                // HTTP Strict Transport Security — fuerza HTTPS
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000)
                )
                // Content Security Policy — restringe fuentes de contenido
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; frame-ancestors 'self'")
                )
            )

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**", "/public/**").permitAll()
                .requestMatchers("/api/productos/**").permitAll()
                .requestMatchers("/api/categorias/**").permitAll()
                .requestMatchers("/h2-console/**").hasRole("ADMIN")
                .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                .requestMatchers("/api/inventario/**").hasAnyRole("ADMIN", "EMPLEADO")
                .requestMatchers("/api/ventas/**").hasAnyRole("ADMIN", "EMPLEADO")
                .requestMatchers("/api/detalle-ventas/**").hasAnyRole("ADMIN", "EMPLEADO")
                .requestMatchers("/api/carrito/**").hasAnyRole("ADMIN", "CLIENTE")
                .anyRequest().authenticated()
            )

            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}