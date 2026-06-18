package com.minimarket.security.config;

import com.minimarket.security.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())

            // Se reemplaza .anyRequest().authenticated() por reglas especificas según el rol de cada usuario
            .authorizeHttpRequests(auth -> auth

                // Acceso publico: cualquiera puede acceder sin autenticarse
                .requestMatchers("/public/**").permitAll()

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

                // Productos y categorías accesibles para todos los autenticados
                .requestMatchers("/api/productos/**").authenticated()
                .requestMatchers("/api/categorias/**").authenticated()

                // Cualquier otra solicitud requiere autenticación
                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .defaultSuccessUrl("/public/hola", true)
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/public/hola")
                .permitAll()
            )

            // Permite que la consola H2 use frames en el navegador
            .headers(headers -> headers
                .frameOptions(frame -> frame.disable())
            );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Configuración de encriptación de contraseñas
    }
}
