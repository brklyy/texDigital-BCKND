package cl.texDigital.auth_service.service;

import cl.texDigital.auth_service.dto.AuthResponseDTO;
import cl.texDigital.auth_service.dto.LoginRequestDTO;
import cl.texDigital.auth_service.dto.RegisterRequestDTO;
import cl.texDigital.auth_service.dto.UsuarioResponseDTO;
import cl.texDigital.auth_service.model.Usuario;
import cl.texDigital.auth_service.repository.UsuarioRepository;
import cl.texDigital.auth_service.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegisterRequestDTO buildRegisterDTO(String rol) {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setUsername("usuario1");
        dto.setPassword("secret123");
        dto.setEmail("usuario1@mail.com");
        dto.setRol(rol);
        return dto;
    }

    @Test
    void register_exitoso_retornaDTO() {
        RegisterRequestDTO dto = buildRegisterDTO("CLIENTE");
        when(usuarioRepository.existsByUsername(anyString())).thenReturn(false);
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        Usuario saved = new Usuario(1L, "usuario1", "encoded", "usuario1@mail.com", "CLIENTE");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(saved);

        UsuarioResponseDTO result = authService.register(dto);

        assertEquals("usuario1", result.getUsername());
        assertEquals("CLIENTE", result.getRol());
    }

    @Test
    void register_rolInvalido_lanzaIllegalArgument() {
        RegisterRequestDTO dto = buildRegisterDTO("SUPERADMIN");
        assertThrows(IllegalArgumentException.class, () -> authService.register(dto));
    }

    @Test
    void register_usernameExistente_lanzaIllegalState() {
        RegisterRequestDTO dto = buildRegisterDTO("ADMIN");
        when(usuarioRepository.existsByUsername("usuario1")).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> authService.register(dto));
    }

    @Test
    void register_emailExistente_lanzaIllegalState() {
        RegisterRequestDTO dto = buildRegisterDTO("OPERADOR");
        when(usuarioRepository.existsByUsername(anyString())).thenReturn(false);
        when(usuarioRepository.existsByEmail("usuario1@mail.com")).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> authService.register(dto));
    }

    @Test
    void login_exitoso_retornaToken() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername("usuario1");
        dto.setPassword("secret123");

        Usuario usuario = new Usuario(1L, "usuario1", "encoded", "usuario1@mail.com", "CLIENTE");
        when(usuarioRepository.findByUsername("usuario1")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("secret123", "encoded")).thenReturn(true);
        when(jwtUtil.generateToken("usuario1", "CLIENTE")).thenReturn("jwt.token.here");

        AuthResponseDTO result = authService.login(dto);

        assertEquals("jwt.token.here", result.getToken());
        assertEquals("CLIENTE", result.getRol());
    }

    @Test
    void login_usuarioNoExiste_lanzaBadCredentials() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername("noexiste");
        dto.setPassword("pass");
        when(usuarioRepository.findByUsername("noexiste")).thenReturn(Optional.empty());
        assertThrows(BadCredentialsException.class, () -> authService.login(dto));
    }

    @Test
    void login_passwordIncorrecta_lanzaBadCredentials() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername("usuario1");
        dto.setPassword("mala");

        Usuario usuario = new Usuario(1L, "usuario1", "encoded", "usuario1@mail.com", "CLIENTE");
        when(usuarioRepository.findByUsername("usuario1")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("mala", "encoded")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(dto));
    }
}
