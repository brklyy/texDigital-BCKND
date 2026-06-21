package cl.texDigital.ms_pedidos.service;

import cl.texDigital.ms_pedidos.client.ClienteClient;
import cl.texDigital.ms_pedidos.client.ClienteResponse;
import cl.texDigital.ms_pedidos.client.ProductoClient;
import cl.texDigital.ms_pedidos.client.ProductoResponse;
import cl.texDigital.ms_pedidos.dto.DetallePedidoRequestDTO;
import cl.texDigital.ms_pedidos.dto.DetallePedidoResponseDTO;
import cl.texDigital.ms_pedidos.dto.PedidoRequestDTO;
import cl.texDigital.ms_pedidos.dto.PedidoResponseDTO;
import cl.texDigital.ms_pedidos.exception.ResourceNotFoundException;
import cl.texDigital.ms_pedidos.model.DetallePedido;
import cl.texDigital.ms_pedidos.model.Pedido;
import cl.texDigital.ms_pedidos.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteClient clienteClient;
    private final ProductoClient productoClient;

    public List<PedidoResponseDTO> findAll() {
        log.debug("Obteniendo lista de todos los pedidos");
        return pedidoRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public PedidoResponseDTO findById(Long id) {
        log.debug("Buscando pedido con id={}", id);
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + id));
        return toResponseDTO(pedido);
    }

    public List<PedidoResponseDTO> findByClienteId(Long clienteId) {
        log.debug("Buscando pedidos del cliente id={}", clienteId);
        return pedidoRepository.findByClienteId(clienteId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public PedidoResponseDTO create(PedidoRequestDTO dto) {
        log.debug("Creando pedido para clienteId={}", dto.getClienteId());

        ClienteResponse cliente = clienteClient.findById(dto.getClienteId());
        if (!"ACTIVO".equals(cliente.getEstado())) {
            throw new IllegalStateException("El cliente con id " + dto.getClienteId() + " no está activo.");
        }

        Pedido pedido = new Pedido();
        pedido.setClienteId(dto.getClienteId());
        pedido.setFecha(LocalDate.now());
        pedido.setEstado("PENDIENTE");
        pedido.setTotal(0.0);
        pedido.setDetalles(new ArrayList<>());

        double total = 0.0;

        for (DetallePedidoRequestDTO detalleDto : dto.getDetalles()) {
            ProductoResponse producto = productoClient.findById(detalleDto.getProductoId());

            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProductoId(detalleDto.getProductoId());
            detalle.setNombreProducto(producto.getNombre());
            detalle.setCantidad(detalleDto.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecioBase());
            double subtotal = detalleDto.getCantidad() * producto.getPrecioBase();
            detalle.setSubtotal(subtotal);
            total += subtotal;

            pedido.getDetalles().add(detalle);
        }

        pedido.setTotal(total);
        Pedido guardado = pedidoRepository.save(pedido);
        log.debug("Pedido creado con id={}, total={}", guardado.getId(), guardado.getTotal());
        return toResponseDTO(guardado);
    }

    public List<PedidoResponseDTO> findByEstado(String estado) {
        log.debug("Buscando pedidos con estado={}", estado);
        return pedidoRepository.findByEstado(estado).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public PedidoResponseDTO update(Long id, PedidoRequestDTO dto) {
        log.debug("Actualizando pedido id={}", id);
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + id));

        ClienteResponse cliente = clienteClient.findById(dto.getClienteId());
        if (!"ACTIVO".equals(cliente.getEstado())) {
            throw new IllegalStateException("El cliente con id " + dto.getClienteId() + " no está activo.");
        }

        pedido.setClienteId(dto.getClienteId());
        pedido.getDetalles().clear();

        double total = 0.0;
        for (DetallePedidoRequestDTO detalleDto : dto.getDetalles()) {
            ProductoResponse producto = productoClient.findById(detalleDto.getProductoId());
            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProductoId(detalleDto.getProductoId());
            detalle.setNombreProducto(producto.getNombre());
            detalle.setCantidad(detalleDto.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecioBase());
            double subtotal = detalleDto.getCantidad() * producto.getPrecioBase();
            detalle.setSubtotal(subtotal);
            total += subtotal;
            pedido.getDetalles().add(detalle);
        }

        pedido.setTotal(total);
        log.debug("Pedido id={} actualizado, nuevo total={}", id, total);
        return toResponseDTO(pedidoRepository.save(pedido));
    }

    public PedidoResponseDTO updateEstado(Long id, String estado) {
        log.debug("Actualizando estado de pedido id={} a {}", id, estado);
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + id));
        pedido.setEstado(estado);
        return toResponseDTO(pedidoRepository.save(pedido));
    }

    public void delete(Long id) {
        log.debug("Eliminando pedido con id={}", id);
        if (!pedidoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Pedido no encontrado con id: " + id);
        }
        pedidoRepository.deleteById(id);
        log.debug("Pedido eliminado con id={}", id);
    }

    private PedidoResponseDTO toResponseDTO(Pedido pedido) {
        List<DetallePedidoResponseDTO> detalles = pedido.getDetalles().stream()
                .map(d -> new DetallePedidoResponseDTO(
                        d.getId(),
                        d.getProductoId(),
                        d.getNombreProducto(),
                        d.getCantidad(),
                        d.getPrecioUnitario(),
                        d.getSubtotal()
                ))
                .toList();

        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getClienteId(),
                pedido.getFecha(),
                pedido.getEstado(),
                pedido.getTotal(),
                detalles
        );
    }
}
