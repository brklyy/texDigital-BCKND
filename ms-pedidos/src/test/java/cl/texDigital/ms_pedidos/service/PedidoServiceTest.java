package cl.texDigital.ms_pedidos.service;

import cl.texDigital.ms_pedidos.client.ClienteClient;
import cl.texDigital.ms_pedidos.client.ClienteResponse;
import cl.texDigital.ms_pedidos.client.ProductoClient;
import cl.texDigital.ms_pedidos.client.ProductoResponse;
import cl.texDigital.ms_pedidos.dto.DetallePedidoRequestDTO;
import cl.texDigital.ms_pedidos.dto.PedidoRequestDTO;
import cl.texDigital.ms_pedidos.dto.PedidoResponseDTO;
import cl.texDigital.ms_pedidos.exception.ResourceNotFoundException;
import cl.texDigital.ms_pedidos.model.DetallePedido;
import cl.texDigital.ms_pedidos.model.Pedido;
import cl.texDigital.ms_pedidos.repository.PedidoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ClienteClient clienteClient;

    @Mock
    private ProductoClient productoClient;

    @InjectMocks
    private PedidoService pedidoService;

    private ClienteResponse cliente(String estado) {
        ClienteResponse c = new ClienteResponse();
        c.setId(1L);
        c.setEstado(estado);
        return c;
    }

    private ProductoResponse producto(Long id, String nombre, Double precio) {
        ProductoResponse p = new ProductoResponse();
        p.setId(id);
        p.setNombre(nombre);
        p.setPrecioBase(precio);
        return p;
    }

    private DetallePedidoRequestDTO detalle(Long productoId, Integer cantidad) {
        DetallePedidoRequestDTO d = new DetallePedidoRequestDTO();
        d.setProductoId(productoId);
        d.setCantidad(cantidad);
        return d;
    }

    private PedidoRequestDTO requestEjemplo() {
        PedidoRequestDTO dto = new PedidoRequestDTO();
        dto.setClienteId(1L);
        dto.setDetalles(List.of(detalle(10L, 2), detalle(20L, 3)));
        return dto;
    }

    private Pedido pedidoEjemplo() {
        DetallePedido d = new DetallePedido(1L, null, 10L, "Estampado", 2, 3500.0, 7000.0);
        return new Pedido(1L, 1L, LocalDate.now(), "PENDIENTE", 7000.0,
                new ArrayList<>(List.of(d)));
    }

    @Test
    @DisplayName("create calcula subtotales y total a partir de los precios de ms-productos")
    void create_calculaTotalYSubtotales() {
        when(clienteClient.findById(1L)).thenReturn(cliente("ACTIVO"));
        when(productoClient.findById(10L)).thenReturn(producto(10L, "Estampado", 3500.0));
        when(productoClient.findById(20L)).thenReturn(producto(20L, "Lienzo", 8000.0));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoResponseDTO resultado = pedidoService.create(requestEjemplo());

        assertThat(resultado.getEstado()).isEqualTo("PENDIENTE");
        assertThat(resultado.getTotal()).isEqualTo(31000.0);
        assertThat(resultado.getDetalles()).hasSize(2);
        assertThat(resultado.getDetalles().get(0).getSubtotal()).isEqualTo(7000.0);
        assertThat(resultado.getDetalles().get(1).getSubtotal()).isEqualTo(24000.0);
    }

    @Test
    @DisplayName("create lanza IllegalState cuando el cliente no esta ACTIVO")
    void create_lanzaIllegalState_siClienteInactivo() {
        when(clienteClient.findById(1L)).thenReturn(cliente("INACTIVO"));

        assertThatThrownBy(() -> pedidoService.create(requestEjemplo()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("activo");
        verify(productoClient, never()).findById(anyLong());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("create propaga ResourceNotFound cuando el cliente no existe")
    void create_propagaNotFound_siClienteNoExiste() {
        when(clienteClient.findById(1L))
                .thenThrow(new ResourceNotFoundException("Cliente no encontrado con id: 1"));

        assertThatThrownBy(() -> pedidoService.create(requestEjemplo()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("update revalida el cliente y recalcula el total con los nuevos detalles")
    void update_recalculaTotal() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoEjemplo()));
        when(clienteClient.findById(1L)).thenReturn(cliente("ACTIVO"));
        when(productoClient.findById(10L)).thenReturn(producto(10L, "Estampado", 3500.0));
        when(productoClient.findById(20L)).thenReturn(producto(20L, "Lienzo", 8000.0));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoResponseDTO resultado = pedidoService.update(1L, requestEjemplo());

        assertThat(resultado.getTotal()).isEqualTo(31000.0);
        assertThat(resultado.getDetalles()).hasSize(2);
    }

    @Test
    @DisplayName("findAll mapea todos los pedidos a DTOs")
    void findAll_devuelveLista() {
        when(pedidoRepository.findAll()).thenReturn(List.of(pedidoEjemplo()));

        List<PedidoResponseDTO> resultado = pedidoService.findAll();

        assertThat(resultado).hasSize(1);
    }

    @Test
    @DisplayName("findById devuelve el pedido cuando existe")
    void findById_devuelvePedido() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoEjemplo()));

        PedidoResponseDTO resultado = pedidoService.findById(1L);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getDetalles()).hasSize(1);
    }

    @Test
    @DisplayName("findById lanza ResourceNotFound cuando no existe")
    void findById_lanzaNotFound() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("updateEstado cambia el estado del pedido")
    void updateEstado_actualiza() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoEjemplo()));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoResponseDTO resultado = pedidoService.updateEstado(1L, "COMPLETADO");

        assertThat(resultado.getEstado()).isEqualTo("COMPLETADO");
    }

    @Test
    @DisplayName("findByClienteId devuelve los pedidos del cliente")
    void findByClienteId_devuelveLista() {
        when(pedidoRepository.findByClienteId(1L)).thenReturn(List.of(pedidoEjemplo()));

        List<PedidoResponseDTO> resultado = pedidoService.findByClienteId(1L);

        assertThat(resultado).hasSize(1);
    }

    @Test
    @DisplayName("findByEstado devuelve los pedidos en ese estado")
    void findByEstado_devuelveLista() {
        when(pedidoRepository.findByEstado("PENDIENTE")).thenReturn(List.of(pedidoEjemplo()));

        List<PedidoResponseDTO> resultado = pedidoService.findByEstado("PENDIENTE");

        assertThat(resultado).hasSize(1);
    }

    @Test
    @DisplayName("delete lanza ResourceNotFound cuando el pedido no existe")
    void delete_lanzaNotFound() {
        when(pedidoRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> pedidoService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(pedidoRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("delete elimina cuando el pedido existe")
    void delete_eliminaPedido() {
        when(pedidoRepository.existsById(1L)).thenReturn(true);

        pedidoService.delete(1L);

        verify(pedidoRepository).deleteById(1L);
    }
}
