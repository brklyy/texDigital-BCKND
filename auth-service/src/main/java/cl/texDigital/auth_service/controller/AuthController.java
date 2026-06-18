package cl.texDigital.auth_service.controller;

import cl.texDigital.auth_service.dto.AuthResponseDTO;
import cl.texDigital.auth_service.dto.LoginRequestDTO;
import cl.texDigital.auth_service.dto.RegisterRequestDTO;
import cl.texDigital.auth_service.dto.UsuarioResponseDTO;
import cl.texDigital.auth_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticacion", description = "Endpoints de registro y login")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario")
    public ResponseEntity<UsuarioResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(dto));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion y obtener JWT")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }
}
