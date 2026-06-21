package cl.texDigital.auth_service.service;

import cl.texDigital.auth_service.dto.UsuarioResponseDTO;
import cl.texDigital.auth_service.exception.ResourceNotFoundException;
import cl.texDigital.auth_service.model.Usuario;
import cl.texDigital.auth_service.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario(Long id) {
        return new Usuario(id, "user" + id, "encoded", "user" + id + "@mail.com", "CLIENTE");
    }

    @Test
    void loadUserByUsername_usuarioExiste_retornaUserDetails() {
        when(usuarioRepository.findByUsername("user1")).thenReturn(Optional.of(usuario(1L)));
        var details = usuarioService.loadUserByUsername("user1");
        assertEquals("user1", details.getUsername());
    }

    @Test
    void loadUserByUsername_usuarioNoExiste_lanzaException() {
        when(usuarioRepository.findByUsername("noexiste")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> usuarioService.loadUserByUsername("noexiste"));
    }

    @Test
    void findAll_retornaListaDTO() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario(1L), usuario(2L)));
        List<UsuarioResponseDTO> result = usuarioService.findAll();
        assertEquals(2, result.size());
    }

    @Test
    void findById_existente_retornaDTO() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L)));
        UsuarioResponseDTO result = usuarioService.findById(1L);
        assertEquals(1L, result.getId());
        assertEquals("user1", result.getUsername());
    }

    @Test
    void findById_noExistente_lanzaException() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> usuarioService.findById(99L));
    }

    @Test
    void delete_existente_eliminaCorrectamente() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        assertDoesNotThrow(() -> usuarioService.delete(1L));
        verify(usuarioRepository).deleteById(1L);
    }

    @Test
    void delete_noExistente_lanzaException() {
        when(usuarioRepository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> usuarioService.delete(99L));
    }
}
