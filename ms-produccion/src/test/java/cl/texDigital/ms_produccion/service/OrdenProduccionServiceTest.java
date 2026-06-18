package cl.texDigital.ms_produccion.service;

import cl.texDigital.ms_produccion.dto.OrdenProduccionRequestDTO;
import cl.texDigital.ms_produccion.dto.OrdenProduccionResponseDTO;
import cl.texDigital.ms_produccion.exception.ResourceNotFoundException;
import cl.texDigital.ms_produccion.model.OrdenProduccion;
import cl.texDigital.ms_produccion.repository.OrdenProduccionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdenProduccionServiceTest {

    @Mock
    private OrdenProduccionRepository ordenRepository;

    @Mock
    private WebClient webClientInventario;

    @InjectMocks
    private OrdenProduccionService ordenService;

    private OrdenProduccionRequestDTO requestEjemplo() {
        OrdenProduccionRequestDTO dto = new OrdenProduccionRequestDTO();
        dto.setPedidoId(1L);
        dto.setProductoId(10L);
        dto.setTextilId(2L);
        dto.setRolloId(5L);
        dto.setMetrosUsados(20.0);
        return dto;
    }

    private OrdenProduccion ordenEjemplo() {
        return new OrdenProduccion(1L, 1L, 10L, 2L, 5L, 20.0, "PENDIENTE",
                LocalDateTime.of(2026, 6, 1, 10, 0), null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void mockInventarioExito() {
        WebClient.RequestBodyUriSpec putSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClientInventario.put()).thenReturn(putSpec);
        when(putSpec.uri(anyString(), any(Object[].class))).thenReturn(bodySpec);
        when(bodySpec.bodyValue(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.just(ResponseEntity.ok().build()));
    }

    @Test
    @DisplayName("findAll mapea todas las ordenes a DTOs")
    void findAll_devuelveLista() {
        when(ordenRepository.findAll()).thenReturn(List.of(ordenEjemplo()));

        List<OrdenProduccionResponseDTO> resultado = ordenService.findAll();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getEstado()).isEqualTo("PENDIENTE");
    }

    @Test
    @DisplayName("findById devuelve la orden cuando existe")
    void findById_devuelveOrden() {
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(ordenEjemplo()));

        OrdenProduccionResponseDTO resultado = ordenService.findById(1L);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getPedidoId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findById lanza ResourceNotFound cuando no existe")
    void findById_lanzaNotFound() {
        when(ordenRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordenService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("findByPedidoId devuelve las ordenes del pedido")
    void findByPedidoId_devuelveLista() {
        when(ordenRepository.findByPedidoId(1L)).thenReturn(List.of(ordenEjemplo()));

        List<OrdenProduccionResponseDTO> resultado = ordenService.findByPedidoId(1L);

        assertThat(resultado).hasSize(1);
    }

    @Test
    @DisplayName("getTotalMetros devuelve la suma cuando hay ordenes")
    void getTotalMetros_devuelveSuma() {
        when(ordenRepository.sumTotalMetrosUsados()).thenReturn(150.0);

        Double resultado = ordenService.getTotalMetros();

        assertThat(resultado).isEqualTo(150.0);
    }

    @Test
    @DisplayName("getTotalMetros devuelve 0.0 cuando no hay ordenes")
    void getTotalMetros_devuelveCero_siNull() {
        when(ordenRepository.sumTotalMetrosUsados()).thenReturn(null);

        Double resultado = ordenService.getTotalMetros();

        assertThat(resultado).isEqualTo(0.0);
    }

    @Test
    @DisplayName("save crea la orden y descuenta metros en ms-inventario")
    void save_creaOrden_siInventarioResponde() {
        mockInventarioExito();
        when(ordenRepository.save(any(OrdenProduccion.class))).thenAnswer(inv -> inv.getArgument(0));

        OrdenProduccionResponseDTO resultado = ordenService.save(requestEjemplo());

        assertThat(resultado.getEstado()).isEqualTo("PENDIENTE");
        assertThat(resultado.getPedidoId()).isEqualTo(1L);
        verify(ordenRepository).save(any(OrdenProduccion.class));
    }

    @Test
    @DisplayName("save lanza IllegalState cuando ms-inventario responde con error de negocio")
    void save_lanzaIllegalState_siInventarioRetornaError() {
        when(webClientInventario.put()).thenThrow(
                WebClientResponseException.create(409, "Conflict", HttpHeaders.EMPTY, new byte[0], null));

        assertThatThrownBy(() -> ordenService.save(requestEjemplo()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("inventario");
        verify(ordenRepository, never()).save(any());
    }

    @Test
    @DisplayName("save lanza IllegalState cuando ms-inventario no esta disponible")
    void save_lanzaIllegalState_siInventarioNoDisponible() {
        when(webClientInventario.put()).thenThrow(new RuntimeException("Connection refused"));

        assertThatThrownBy(() -> ordenService.save(requestEjemplo()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("inventario");
        verify(ordenRepository, never()).save(any());
    }

    @Test
    @DisplayName("update modifica la orden cuando existe y los datos son validos")
    void update_modificaOrden() {
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(ordenEjemplo()));
        when(ordenRepository.save(any(OrdenProduccion.class))).thenAnswer(inv -> inv.getArgument(0));
        OrdenProduccionRequestDTO dto = requestEjemplo();
        dto.setMetrosUsados(30.0);

        OrdenProduccionResponseDTO resultado = ordenService.update(1L, dto);

        assertThat(resultado.getMetrosUsados()).isEqualTo(30.0);
    }

    @Test
    @DisplayName("update lanza ResourceNotFound cuando la orden no existe")
    void update_lanzaNotFound() {
        when(ordenRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordenService.update(99L, requestEjemplo()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(ordenRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateEstado a COMPLETADO registra la fechaFin")
    void updateEstado_poneFechaFin_siCompletado() {
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(ordenEjemplo()));
        when(ordenRepository.save(any(OrdenProduccion.class))).thenAnswer(inv -> inv.getArgument(0));

        OrdenProduccionResponseDTO resultado = ordenService.updateEstado(1L, "COMPLETADO");

        assertThat(resultado.getEstado()).isEqualTo("COMPLETADO");
        assertThat(resultado.getFechaFin()).isNotNull();
    }

    @Test
    @DisplayName("updateEstado lanza IllegalArgument si el estado no esta en ESTADOS_VALIDOS")
    void updateEstado_lanzaIllegalArgument_siEstadoInvalido() {
        assertThatThrownBy(() -> ordenService.updateEstado(1L, "CANCELADO"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Estado");
        verify(ordenRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete elimina la orden cuando existe")
    void delete_eliminaOrden() {
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(ordenEjemplo()));

        ordenService.delete(1L);

        verify(ordenRepository).delete(any(OrdenProduccion.class));
    }

    @Test
    @DisplayName("delete lanza ResourceNotFound cuando la orden no existe")
    void delete_lanzaNotFound() {
        when(ordenRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ordenService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(ordenRepository, never()).delete(any());
    }
}
