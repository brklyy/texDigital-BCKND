package cl.texDigital.ms_pagos.service;

import cl.texDigital.ms_pagos.client.PedidoClient;
import cl.texDigital.ms_pagos.client.PedidoResponse;
import cl.texDigital.ms_pagos.dto.PagoRequestDTO;
import cl.texDigital.ms_pagos.dto.PagoResponseDTO;
import cl.texDigital.ms_pagos.exception.ResourceNotFoundException;
import cl.texDigital.ms_pagos.model.Pago;
import cl.texDigital.ms_pagos.repository.PagoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de PagoService.
 * Se mockean el repositorio (BD) y el cliente de ms-pedidos (comunicacion remota).
 * Convencion: Given - When - Then.
 */
@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private PedidoClient pedidoClient;

    @InjectMocks
    private PagoService pagoService;

    private PagoRequestDTO request(Long pedidoId, String metodo, Integer descuento) {
        PagoRequestDTO dto = new PagoRequestDTO();
        dto.setPedidoId(pedidoId);
        dto.setMetodoPago(metodo);
        dto.setPorcentajeDescuento(descuento);
        return dto;
    }

    private Pago pagoEjemplo() {
        return new Pago(1L, 1L, "EFECTIVO", 10000.0, 0, 0.0, 10000.0, 1900.0, 11900.0,
                "PAGADO", LocalDate.now());
    }

    @Test
    @DisplayName("create calcula el IVA al 19% cuando no hay descuento")
    void create_calculaIvaSinDescuento() {
        // Given: el pedido en ms-pedidos tiene un total de 10000
        when(pagoRepository.existsByPedidoIdAndEstado(1L, "PAGADO")).thenReturn(false);
        when(pedidoClient.findById(1L)).thenReturn(new PedidoResponse(1L, "PENDIENTE", 10000.0));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        PagoResponseDTO resultado = pagoService.create(request(1L, "TARJETA", 0));

        // Then: IVA = 10000 * 0.19 = 1900 ; total = 11900
        assertThat(resultado.getMontoNeto()).isEqualTo(10000.0);
        assertThat(resultado.getIva()).isEqualTo(1900.0);
        assertThat(resultado.getMontoTotal()).isEqualTo(11900.0);
        assertThat(resultado.getEstado()).isEqualTo("PAGADO");
        verify(pedidoClient).actualizarEstado(1L, "PAGADO");
    }

    @Test
    @DisplayName("create aplica el descuento y luego calcula el IVA sobre el neto")
    void create_aplicaDescuento() {
        // Given: total 10000 con 10% de descuento
        when(pagoRepository.existsByPedidoIdAndEstado(1L, "PAGADO")).thenReturn(false);
        when(pedidoClient.findById(1L)).thenReturn(new PedidoResponse(1L, "PENDIENTE", 10000.0));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        PagoResponseDTO resultado = pagoService.create(request(1L, "EFECTIVO", 10));

        // Then: descuento 1000, neto 9000, IVA 1710, total 10710
        assertThat(resultado.getMontoDescuento()).isEqualTo(1000.0);
        assertThat(resultado.getMontoNeto()).isEqualTo(9000.0);
        assertThat(resultado.getIva()).isEqualTo(1710.0);
        assertThat(resultado.getMontoTotal()).isEqualTo(10710.0);
    }

    @Test
    @DisplayName("create rechaza un pedido que ya fue pagado")
    void create_lanzaIllegalState_siPedidoYaPagado() {
        // Given
        when(pagoRepository.existsByPedidoIdAndEstado(1L, "PAGADO")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> pagoService.create(request(1L, "TARJETA", 0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya tiene un pago");
        verify(pedidoClient, never()).findById(anyLong());
        verify(pagoRepository, never()).save(any());
    }

    @Test
    @DisplayName("create propaga ResourceNotFound cuando el pedido no existe en ms-pedidos")
    void create_lanzaNotFound_siPedidoNoExiste() {
        // Given
        when(pagoRepository.existsByPedidoIdAndEstado(1L, "PAGADO")).thenReturn(false);
        when(pedidoClient.findById(1L))
                .thenThrow(new ResourceNotFoundException("Pedido no encontrado con id: 1"));

        // When / Then
        assertThatThrownBy(() -> pagoService.create(request(1L, "TARJETA", 0)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(pagoRepository, never()).save(any());
    }

    @Test
    @DisplayName("create rechaza un metodo de pago invalido antes de tocar la BD o el remoto")
    void create_lanzaIllegalArgument_siMetodoInvalido() {
        // When / Then
        assertThatThrownBy(() -> pagoService.create(request(1L, "BITCOIN", 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Metodo de pago invalido");
        verify(pagoRepository, never()).existsByPedidoIdAndEstado(anyLong(), anyString());
        verify(pedidoClient, never()).findById(anyLong());
        verify(pagoRepository, never()).save(any());
    }

    @Test
    @DisplayName("findById lanza ResourceNotFound cuando el pago no existe")
    void findById_lanzaNotFound() {
        // Given
        when(pagoRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> pagoService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("delete lanza ResourceNotFound cuando el pago no existe")
    void delete_lanzaNotFound() {
        // Given
        when(pagoRepository.existsById(99L)).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> pagoService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(pagoRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("updateEstado normaliza a mayuscula y guarda cuando el estado es valido")
    void updateEstado_actualiza_siValido() {
        // Given
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoEjemplo()));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        PagoResponseDTO resultado = pagoService.updateEstado(1L, "reembolsado");

        // Then
        assertThat(resultado.getEstado()).isEqualTo("REEMBOLSADO");
    }

    @Test
    @DisplayName("updateEstado rechaza un estado fuera del conjunto permitido")
    void updateEstado_lanzaIllegalArgument_siInvalido() {
        // Given
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoEjemplo()));

        // When / Then
        assertThatThrownBy(() -> pagoService.updateEstado(1L, "REGALADO"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Estado invalido");
        verify(pagoRepository, never()).save(any());
    }

    @Test
    @DisplayName("update recalcula neto, IVA y total con el nuevo descuento sobre el mismo monto base")
    void update_recalculaConNuevoDescuento() {
        // Given: pago existente con monto base 10000
        when(pagoRepository.findById(1L)).thenReturn(Optional.of(pagoEjemplo()));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));

        // When: se aplica un 20% de descuento
        PagoResponseDTO resultado = pagoService.update(1L, request(1L, "TARJETA", 20));

        // Then: descuento 2000, neto 8000, IVA 1520, total 9520
        assertThat(resultado.getMontoDescuento()).isEqualTo(2000.0);
        assertThat(resultado.getMontoNeto()).isEqualTo(8000.0);
        assertThat(resultado.getIva()).isEqualTo(1520.0);
        assertThat(resultado.getMontoTotal()).isEqualTo(9520.0);
        assertThat(resultado.getMetodoPago()).isEqualTo("TARJETA");
    }
}
