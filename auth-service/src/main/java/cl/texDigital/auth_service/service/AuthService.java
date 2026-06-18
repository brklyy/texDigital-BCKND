package cl.texDigital.auth_service.service;

import cl.texDigital.auth_service.dto.AuthResponseDTO;
import cl.texDigital.auth_service.dto.LoginRequestDTO;
import cl.texDigital.auth_service.dto.RegisterRequestDTO;
import cl.texDigital.auth_service.dto.UsuarioResponseDTO;
import cl.texDigital.auth_service.model.Usuario;
import cl.texDigital.auth_service.repository.UsuarioRepository;
import cl.texDigital.auth_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private static final List<String> ROLES_VALIDOS = List.of("ADMIN", "OPERADOR", "CLIENTE");

    public UsuarioResponseDTO register(RegisterRequestDTO dto) {
        if (!ROLES_VALIDOS.contains(dto.getRol())) {
            throw new IllegalArgumentException("Rol invalido. Debe ser: ADMIN, OPERADOR o CLIENTE");
        }
        if (usuarioRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalStateException("El username ya esta en uso");
        }
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalStateException("El email ya esta en uso");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setEmail(dto.getEmail());
        usuario.setRol(dto.getRol());

        Usuario saved = usuarioRepository.save(usuario);
        return new UsuarioResponseDTO(saved.getId(), saved.getUsername(), saved.getEmail(), saved.getRol());
    }

    public AuthResponseDTO login(LoginRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));

        if (!passwordEncoder.matches(dto.getPassword(), usuario.getPassword())) {
            throw new BadCredentialsException("Credenciales invalidas");
        }

        String token = jwtUtil.generateToken(usuario.getUsername(), usuario.getRol());
        return new AuthResponseDTO(token, usuario.getUsername(), usuario.getRol());
    }
}
