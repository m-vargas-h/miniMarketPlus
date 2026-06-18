package com.minimarket.security.filter;

import com.minimarket.security.service.CustomUserDetailsService;
import com.minimarket.security.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Leer el header Authorization
        String authHeader = request.getHeader("Authorization");

        String token = null;
        String username = null;

        // 2. Verificar que el header tenga el formato "Bearer <token>"
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            username = jwtUtil.extractUsername(token);
            System.out.println(">>> USERNAME EXTRAIDO: " + username); //! Debug: Imprime el username extraído del token
        }

        // 3. Si se extrajo un username y no hay autenticación activa en el contexto
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            System.out.println(">>> AUTHORITIES: " + userDetails.getAuthorities()); //! Debug: Imprime las autoridades del usuario cargado

            // 4. Validar el token
            if (jwtUtil.validateToken(token, userDetails)) {
                System.out.println(">>> TOKEN VALIDO"); //! Debug: Indica que el token es válido
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                // 5. Registrar la autenticación en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                System.out.println(">>> TOKEN INVALIDO"); //! Debug: Indica que el token no es válido
            }
        }

        filterChain.doFilter(request, response);
    }
}