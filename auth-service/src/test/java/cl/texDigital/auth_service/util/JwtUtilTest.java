package cl.texDigital.auth_service.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "clave-secreta-de-prueba-suficientemente-larga-1234");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }

    @Test
    void generateToken_retornaTokenNoNulo() {
        String token = jwtUtil.generateToken("usuario1", "CLIENTE");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractUsername_retornaUsernameCorrect() {
        String token = jwtUtil.generateToken("usuario1", "ADMIN");
        assertEquals("usuario1", jwtUtil.extractUsername(token));
    }

    @Test
    void extractRol_retornaRolCorrecto() {
        String token = jwtUtil.generateToken("usuario1", "OPERADOR");
        assertEquals("OPERADOR", jwtUtil.extractRol(token));
    }

    @Test
    void isTokenValid_tokenValido_retornaTrue() {
        String token = jwtUtil.generateToken("usuario1", "CLIENTE");
        assertTrue(jwtUtil.isTokenValid(token));
    }

    @Test
    void isTokenValid_tokenInvalido_retornaFalse() {
        assertFalse(jwtUtil.isTokenValid("token.invalido.aqui"));
    }

    @Test
    void isTokenValid_tokenExpirado_retornaFalse() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L);
        String token = jwtUtil.generateToken("usuario1", "CLIENTE");
        assertFalse(jwtUtil.isTokenValid(token));
    }
}
