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
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth

                // Endpoints públicos: login, registro y ruta pública
                .requestMatchers("/auth/**", "/public/**").permitAll()
                .requestMatchers("/api/productos/**").permitAll()
                .requestMatchers("/api/categorias/**").permitAll()

                // Consola H2 solo para administradores
                .requestMatchers("/h2-console/**").hasRole("ADMIN")

                // Gestión de usuarios solo para administradores
                .requestMatchers("/api/usuarios/**").hasRole("ADMIN")

                // Inventario y ventas para admin y empleados
                .requestMatchers("/api/inventario/**").hasAnyRole("ADMIN", "EMPLEADO")
                .requestMatchers("/api/ventas/**").hasAnyRole("ADMIN", "EMPLEADO")
                .requestMatchers("/api/detalle-ventas/**").hasAnyRole("ADMIN", "EMPLEADO")

                // Carrito accesible para admin y clientes
                .requestMatchers("/api/carrito/**").hasAnyRole("ADMIN", "CLIENTE")

                // Cualquier otra solicitud requiere autenticación
                .anyRequest().authenticated()
            )

            // Permite que la consola H2 use frames en el navegador
            .headers(headers -> headers
                .frameOptions(frame -> frame.disable())
            )

            // Registrar el filtro JWT antes del filtro estándar de Spring Security
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