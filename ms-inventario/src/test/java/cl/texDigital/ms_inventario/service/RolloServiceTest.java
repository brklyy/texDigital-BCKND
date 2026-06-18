package cl.texDigital.ms_inventario.service;

import cl.texDigital.ms_inventario.dto.RolloRequestDTO;
import cl.texDigital.ms_inventario.dto.RolloResponseDTO;
import cl.texDigital.ms_inventario.dto.UsarMetrosRequestDTO;
import cl.texDigital.ms_inventario.exception.MetrosInsuficientesException;
import cl.texDigital.ms_inventario.exception.ResourceNotFoundException;
import cl.texDigital.ms_inventario.model.Rollo;
import cl.texDigital.ms_inventario.model.Textil;
import cl.texDigital.ms_inventario.repository.RolloRepository;
import cl.texDigital.ms_inventario.repository.TextilRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RolloServiceTest {

    @Mock
    private RolloRepository rolloRepository;

    @Mock
    private TextilRepository textilRepository;

    @InjectMocks
    private RolloService rolloService;

    private Textil textilEjemplo() {
        return new Textil(1L, "Pearl", 150.0, "Tela para backlights");
    }

    private Rollo rolloEjemplo() {
        return new Rollo(1L, textilEjemplo(), 100.0, 100.0, 0.0, LocalDate.now(), "ACTIVO");
    }

    private RolloRequestDTO requestEjemplo() {
        RolloRequestDTO dto = new RolloRequestDTO();
        dto.setTextilId(1L);
        dto.setMetrosTotales(100.0);
        dto.setFechaIngreso(LocalDate.now());
        return dto;
    }

    private UsarMetrosRequestDTO usar(Double metros) {
        UsarMetrosRequestDTO dto = new UsarMetrosRequestDTO();
        dto.setMetros(metros);
        return dto;
    }

    @Test
    @DisplayName("findById devuelve el rollo cuando existe")
    void findById_devuelveRollo() {
        when(rolloRepository.findById(1L)).thenReturn(Optional.of(rolloEjemplo()));

        RolloResponseDTO resultado = rolloService.findById(1L);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getTextilNombre()).isEqualTo("Pearl");
    }

    @Test
    @DisplayName("findById lanza ResourceNotFound cuando no existe")
    void findById_lanzaNotFound() {
        when(rolloRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rolloService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("create inicializa metros restantes y estado ACTIVO cuando el textil existe")
    void create_inicializaRollo() {
        when(textilRepository.findById(1L)).thenReturn(Optional.of(textilEjemplo()));
        when(rolloRepository.save(any(Rollo.class))).thenAnswer(inv -> inv.getArgument(0));

        RolloResponseDTO resultado = rolloService.create(requestEjemplo());

        assertThat(resultado.getMetrosRestantes()).isEqualTo(100.0);
        assertThat(resultado.getMetrosUsados()).isEqualTo(0.0);
        assertThat(resultado.getEstado()).isEqualTo("ACTIVO");
    }

    @Test
    @DisplayName("create lanza ResourceNotFound cuando el textil no existe")
    void create_lanzaNotFound_cuandoTextilNoExiste() {
        when(textilRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rolloService.create(requestEjemplo()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(rolloRepository, never()).save(any());
    }

    @Test
    @DisplayName("update modifica el rollo cuando rollo y textil existen")
    void update_modificaRollo() {
        when(rolloRepository.findById(1L)).thenReturn(Optional.of(rolloEjemplo()));
        when(textilRepository.findById(1L)).thenReturn(Optional.of(textilEjemplo()));
        when(rolloRepository.save(any(Rollo.class))).thenAnswer(inv -> inv.getArgument(0));
        RolloRequestDTO dto = requestEjemplo();
        dto.setMetrosTotales(200.0);

        RolloResponseDTO resultado = rolloService.update(1L, dto);

        assertThat(resultado.getMetrosTotales()).isEqualTo(200.0);
    }

    @Test
    @DisplayName("update lanza ResourceNotFound cuando el rollo no existe")
    void update_lanzaNotFound_cuandoRolloNoExiste() {
        when(rolloRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rolloService.update(99L, requestEjemplo()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("usarMetros descuenta los metros y mantiene ACTIVO si queda stock")
    void usarMetros_descuenta_cuandoHaySuficiente() {
        when(rolloRepository.findById(1L)).thenReturn(Optional.of(rolloEjemplo()));
        when(rolloRepository.save(any(Rollo.class))).thenAnswer(inv -> inv.getArgument(0));

        RolloResponseDTO resultado = rolloService.usarMetros(1L, usar(30.0));

        assertThat(resultado.getMetrosRestantes()).isEqualTo(70.0);
        assertThat(resultado.getMetrosUsados()).isEqualTo(30.0);
        assertThat(resultado.getEstado()).isEqualTo("ACTIVO");
    }

    @Test
    @DisplayName("usarMetros marca el rollo como AGOTADO cuando se consume todo el stock")
    void usarMetros_marcaAgotado_cuandoStockLlegaACero() {
        when(rolloRepository.findById(1L)).thenReturn(Optional.of(rolloEjemplo()));
        when(rolloRepository.save(any(Rollo.class))).thenAnswer(inv -> inv.getArgument(0));

        RolloResponseDTO resultado = rolloService.usarMetros(1L, usar(100.0));

        assertThat(resultado.getMetrosRestantes()).isEqualTo(0.0);
        assertThat(resultado.getEstado()).isEqualTo("AGOTADO");
    }

    @Test
    @DisplayName("usarMetros lanza MetrosInsuficientes cuando se piden mas metros de los disponibles")
    void usarMetros_lanzaMetrosInsuficientes() {
        when(rolloRepository.findById(1L)).thenReturn(Optional.of(rolloEjemplo()));

        assertThatThrownBy(() -> rolloService.usarMetros(1L, usar(150.0)))
                .isInstanceOf(MetrosInsuficientesException.class)
                .hasMessageContaining("insuficientes");
        verify(rolloRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete elimina cuando el rollo existe")
    void delete_eliminaRollo() {
        when(rolloRepository.existsById(1L)).thenReturn(true);

        rolloService.delete(1L);

        verify(rolloRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete lanza ResourceNotFound cuando el rollo no existe")
    void delete_lanzaNotFound() {
        when(rolloRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> rolloService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("findByTextilId devuelve los rollos cuando el textil existe")
    void findByTextilId_devuelveRollos() {
        when(textilRepository.existsById(1L)).thenReturn(true);
        when(rolloRepository.findByTextilId(1L)).thenReturn(List.of(rolloEjemplo()));

        List<RolloResponseDTO> resultado = rolloService.findByTextilId(1L);

        assertThat(resultado).hasSize(1);
    }

    @Test
    @DisplayName("findByTextilId lanza ResourceNotFound cuando el textil no existe")
    void findByTextilId_lanzaNotFound() {
        when(textilRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> rolloService.findByTextilId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
