package cl.texDigital.auth_service.controller;

import cl.texDigital.auth_service.dto.AuthResponseDTO;
import cl.texDigital.auth_service.dto.LoginRequestDTO;
import cl.texDigital.auth_service.dto.RegisterRequestDTO;
import cl.texDigital.auth_service.dto.UsuarioResponseDTO;
import cl.texDigital.auth_service.security.JwtFilter;
import cl.texDigital.auth_service.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtFilter jwtFilter;

    @BeforeEach
    void setupFilter() throws Exception {
        doAnswer(inv -> {
            inv.<FilterChain>getArgument(2).doFilter(
                    inv.<ServletRequest>getArgument(0),
                    inv.<ServletResponse>getArgument(1));
            return null;
        }).when(jwtFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
    }

    private RegisterRequestDTO buildRegisterDTO() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setUsername("usuario1");
        dto.setPassword("secret123");
        dto.setEmail("usuario1@mail.com");
        dto.setRol("CLIENTE");
        return dto;
    }

    @Test
    void register_exitoso_retorna201() throws Exception {
        UsuarioResponseDTO response = new UsuarioResponseDTO(1L, "usuario1", "usuario1@mail.com", "CLIENTE");
        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRegisterDTO())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("usuario1"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
    }

    @Test
    void register_camposVacios_retorna400() throws Exception {
        RegisterRequestDTO dto = new RegisterRequestDTO();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_rolInvalido_retorna400() throws Exception {
        when(authService.register(any(RegisterRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Rol invalido"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRegisterDTO())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_usernameExistente_retorna400() throws Exception {
        when(authService.register(any(RegisterRequestDTO.class)))
                .thenThrow(new IllegalStateException("El username ya esta en uso"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRegisterDTO())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_exitoso_retorna200ConToken() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername("usuario1");
        dto.setPassword("secret123");

        AuthResponseDTO response = new AuthResponseDTO("jwt.token.here", "usuario1", "CLIENTE");
        when(authService.login(any(LoginRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.token.here"))
                .andExpect(jsonPath("$.username").value("usuario1"));
    }

    @Test
    void login_credencialesInvalidas_retorna401() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername("usuario1");
        dto.setPassword("mala");

        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new BadCredentialsException("Credenciales invalidas"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
}
