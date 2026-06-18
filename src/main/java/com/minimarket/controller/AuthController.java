package com.minimarket.controller;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RolRepository;
import com.minimarket.security.model.LoginRequest;
import com.minimarket.security.util.JwtUtil;
import com.minimarket.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/auth")
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

    // POST /auth/login — valida credenciales y devuelve el JWT
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        System.out.println(">>> INTENTO LOGIN: " + loginRequest.getUsername());
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
        } catch (Exception e) {
            System.out.println(">>> ERROR AUTH: " + e.getMessage());
            throw e;
        } // Si llegamos aquí, la autenticación fue exitosa
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

    // POST /auth/registro — registra un nuevo usuario con rol ROLE_CLIENTE por defecto
    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody Usuario usuario) {
        // Verificar si el username ya existe
        Optional<Usuario> existente = usuarioService.findByUsername(usuario.getUsername());
        if (existente.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El username ya está en uso.");
        }

        // Encriptar contraseña
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // Asignar rol ROLE_CLIENTE por defecto
        Optional<Rol> rolCliente = rolRepository.findByNombre("ROLE_CLIENTE");
        if (rolCliente.isPresent()) {
            usuario.setRoles(Set.of(rolCliente.get()));
        }

        usuarioService.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Usuario registrado exitosamente.");
    }

    //! nota para mi: el endpoint de registro no es parte de la API real, es solo para facilitar pruebas. 
    //! En producción, el registro se haría desde una interfaz de administración o similar, no estaría abierto al público.
    // endpoint de prueba para generar hashes de contraseñas (no es parte de la API real)
    //@GetMapping("/test-hash")
    //public ResponseEntity<?> testHash() {
    //    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    //    Map<String, String> hashes = new HashMap<>();
    //    hashes.put("admin", encoder.encode("admin123"));
    //    hashes.put("empleado", encoder.encode("empleado123"));
    //    hashes.put("cliente", encoder.encode("cliente123"));
    //    return ResponseEntity.ok(hashes);
    //}
}