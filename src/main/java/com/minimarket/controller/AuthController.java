package com.minimarket.controller;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RolRepository;
import com.minimarket.security.model.LoginRequest;
import com.minimarket.security.util.JwtUtil;
import com.minimarket.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Endpoints públicos para login y registro de usuarios")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UsuarioService usuarioService;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          UserDetailsService userDetailsService,
                          JwtUtil jwtUtil,
                          UsuarioService usuarioService,
                          RolRepository rolRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.usuarioService = usuarioService;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    @Operation(
        summary = "Iniciar sesión",
        description = "Valida las credenciales del usuario y retorna un token JWT. " +
                      "Usa este token en el botón Authorize (🔒) para acceder a los endpoints protegidos."
    )
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
            )
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/registro")
    @Operation(
        summary = "Registrar nuevo usuario",
        description = "Crea un nuevo usuario con rol ROLE_CLIENTE por defecto. " +
                      "Si el username ya existe retorna 409 CONFLICT."
    )
    public ResponseEntity<?> registro(@RequestBody Usuario usuario) {
        Optional<Usuario> existente = usuarioService.findByUsername(usuario.getUsername());
        if (existente.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El username ya está en uso.");
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        Optional<Rol> rolCliente = rolRepository.findByNombre("ROLE_CLIENTE");
        if (rolCliente.isPresent()) {
            usuario.setRoles(Set.of(rolCliente.get()));
        }

        usuarioService.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Usuario registrado exitosamente.");
    }
}