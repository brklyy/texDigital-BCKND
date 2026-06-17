package cl.texDigital.ms_inventario.service;

import cl.texDigital.ms_inventario.dto.TextilRequestDTO;
import cl.texDigital.ms_inventario.dto.TextilResponseDTO;
import cl.texDigital.ms_inventario.exception.ResourceNotFoundException;
import cl.texDigital.ms_inventario.model.Textil;
import cl.texDigital.ms_inventario.repository.RolloRepository;
import cl.texDigital.ms_inventario.repository.TextilRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de TextilService. Se mockean ambos repositorios.
 * Convencion: Given - When - Then.
 */
@ExtendWith(MockitoExtension.class)
class TextilServiceTest {

    @Mock
    private TextilRepository textilRepository;

    @Mock
    private RolloRepository rolloRepository;

    @InjectMocks
    private TextilService textilService;

    private Textil textilEjemplo() {
        return new Textil(1L, "Pearl", 150.0, "Tela para backlights");
    }

    private TextilRequestDTO requestEjemplo() {
        TextilRequestDTO dto = new TextilRequestDTO();
        dto.setNombre("Pearl");
        dto.setAnchoCm(150.0);
        dto.setDescripcion("Tela para backlights");
        return dto;
    }

    @Test
    @DisplayName("findAll mapea las entidades a DTOs")
    void findAll_devuelveLista() {
        when(textilRepository.findAll()).thenReturn(List.of(textilEjemplo()));

        List<TextilResponseDTO> resultado = textilService.findAll();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Pearl");
    }

    @Test
    @DisplayName("findById devuelve el textil cuando existe")
    void findById_devuelveTextil() {
        when(textilRepository.findById(1L)).thenReturn(Optional.of(textilEjemplo()));

        TextilResponseDTO resultado = textilService.findById(1L);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getAnchoCm()).isEqualTo(150.0);
    }

    @Test
    @DisplayName("findById lanza ResourceNotFound cuando no existe")
    void findById_lanzaNotFound() {
        when(textilRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> textilService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("create guarda el textil cuando el nombre es nuevo")
    void create_guardaTextil_cuandoNombreNuevo() {
        when(textilRepository.existsByNombre("Pearl")).thenReturn(false);
        when(textilRepository.save(any(Textil.class))).thenReturn(textilEjemplo());

        TextilResponseDTO resultado = textilService.create(requestEjemplo());

        assertThat(resultado.getNombre()).isEqualTo("Pearl");
        verify(textilRepository).save(any(Textil.class));
    }

    @Test
    @DisplayName("create lanza IllegalState cuando el nombre ya existe")
    void create_lanzaIllegalState_cuandoNombreDuplicado() {
        when(textilRepository.existsByNombre("Pearl")).thenReturn(true);

        assertThatThrownBy(() -> textilService.create(requestEjemplo()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Pearl");
        verify(textilRepository, never()).save(any());
    }

    @Test
    @DisplayName("update modifica el textil cuando existe")
    void update_modificaTextil() {
        when(textilRepository.findById(1L)).thenReturn(Optional.of(textilEjemplo()));
        when(textilRepository.save(any(Textil.class))).thenAnswer(inv -> inv.getArgument(0));
        TextilRequestDTO dto = requestEjemplo();
        dto.setDescripcion("Actualizada");

        TextilResponseDTO resultado = textilService.update(1L, dto);

        assertThat(resultado.getDescripcion()).isEqualTo("Actualizada");
    }

    @Test
    @DisplayName("update lanza ResourceNotFound cuando no existe")
    void update_lanzaNotFound() {
        when(textilRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> textilService.update(99L, requestEjemplo()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(textilRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete elimina cuando existe y no tiene rollos asociados")
    void delete_eliminaTextil_cuandoSinRollos() {
        when(textilRepository.existsById(1L)).thenReturn(true);
        when(rolloRepository.existsByTextilId(1L)).thenReturn(false);

        textilService.delete(1L);

        verify(textilRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete lanza IllegalState cuando el textil tiene rollos asociados")
    void delete_lanzaIllegalState_cuandoTieneRollos() {
        when(textilRepository.existsById(1L)).thenReturn(true);
        when(rolloRepository.existsByTextilId(1L)).thenReturn(true);

        assertThatThrownBy(() -> textilService.delete(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("rollos");
        verify(textilRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("delete lanza ResourceNotFound cuando el textil no existe")
    void delete_lanzaNotFound() {
        when(textilRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> textilService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(textilRepository, never()).deleteById(anyLong());
    }
}
