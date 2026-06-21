package cl.texDigital.ms_produccion.service;

import cl.texDigital.ms_produccion.dto.OrdenProduccionRequestDTO;
import cl.texDigital.ms_produccion.dto.OrdenProduccionResponseDTO;
import cl.texDigital.ms_produccion.exception.ResourceNotFoundException;
import cl.texDigital.ms_produccion.model.OrdenProduccion;
import cl.texDigital.ms_produccion.repository.OrdenProduccionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
public class OrdenProduccionService {

    private static final List<String> ESTADOS_VALIDOS = List.of(
            "PENDIENTE", "EN_PRODUCCION", "COMPLETADO"
    );

    private final OrdenProduccionRepository ordenRepository;
    private final WebClient webClientInventario;

    public OrdenProduccionService(OrdenProduccionRepository ordenRepository,
                                   WebClient webClientInventario) {
        this.ordenRepository = ordenRepository;
        this.webClientInventario = webClientInventario;
    }

    @Transactional(readOnly = true)
    public List<OrdenProduccionResponseDTO> findAll() {
        log.info("Consultando todas las órdenes de producción");
        return ordenRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrdenProduccionResponseDTO findById(Long id) {
        log.info("Consultando orden de producción con id: {}", id);
        return toResponseDTO(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<OrdenProduccionResponseDTO> findByPedidoId(Long pedidoId) {
        log.info("Consultando órdenes de producción por pedidoId: {}", pedidoId);
        return ordenRepository.findByPedidoId(pedidoId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Double getTotalMetros() {
        log.info("Calculando total de metros usados en producción");
        Double total = ordenRepository.sumTotalMetrosUsados();
        return total != null ? total : 0.0;
    }

    public OrdenProduccionResponseDTO save(OrdenProduccionRequestDTO dto) {
        log.info("Creando orden de producción para pedidoId: {}", dto.getPedidoId());
        descontarMetrosEnInventario(dto.getRolloId(), dto.getMetrosUsados());

        OrdenProduccion orden = new OrdenProduccion();
        orden.setPedidoId(dto.getPedidoId());
        orden.setProductoId(dto.getProductoId());
        orden.setTextilId(dto.getTextilId());
        orden.setRolloId(dto.getRolloId());
        orden.setMetrosUsados(dto.getMetrosUsados());
        orden.setEstado("PENDIENTE");
        orden.setFechaCreacion(LocalDateTime.now());

        OrdenProduccion guardada = ordenRepository.save(orden);
        log.info("Orden de producción creada con id: {}", guardada.getId());
        return toResponseDTO(guardada);
    }

    public OrdenProduccionResponseDTO update(Long id, OrdenProduccionRequestDTO dto) {
        log.info("Actualizando orden de producción con id: {}", id);
        OrdenProduccion orden = getOrThrow(id);
        orden.setPedidoId(dto.getPedidoId());
        orden.setProductoId(dto.getProductoId());
        orden.setTextilId(dto.getTextilId());
        orden.setRolloId(dto.getRolloId());
        orden.setMetrosUsados(dto.getMetrosUsados());
        OrdenProduccion actualizada = ordenRepository.save(orden);
        log.info("Orden de producción con id {} actualizada", id);
        return toResponseDTO(actualizada);
    }

    public OrdenProduccionResponseDTO updateEstado(Long id, String nuevoEstado) {
        if (!ESTADOS_VALIDOS.contains(nuevoEstado.toUpperCase())) {
            log.warn("Estado inválido recibido: {}", nuevoEstado);
            throw new IllegalArgumentException("Estado inválido: " + nuevoEstado +
                    ". Estados permitidos: " + ESTADOS_VALIDOS);
        }

        OrdenProduccion orden = getOrThrow(id);
        orden.setEstado(nuevoEstado.toUpperCase());

        if ("COMPLETADO".equals(nuevoEstado.toUpperCase())) {
            orden.setFechaFin(LocalDateTime.now());
        }

        OrdenProduccion actualizada = ordenRepository.save(orden);
        log.info("Estado de orden {} actualizado a: {}", id, nuevoEstado);
        return toResponseDTO(actualizada);
    }

    public void delete(Long id) {
        OrdenProduccion orden = getOrThrow(id);
        ordenRepository.delete(orden);
        log.info("Orden de producción eliminada con id: {}", id);
    }

    private OrdenProduccion getOrThrow(Long id) {
        return ordenRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Orden de producción con id {} no encontrada", id);
                    return new ResourceNotFoundException("Orden de producción con id " + id + " no encontrada");
                });
    }

    private OrdenProduccionResponseDTO toResponseDTO(OrdenProduccion orden) {
        return new OrdenProduccionResponseDTO(
                orden.getId(),
                orden.getPedidoId(),
                orden.getProductoId(),
                orden.getTextilId(),
                orden.getRolloId(),
                orden.getMetrosUsados(),
                orden.getEstado(),
                orden.getFechaCreacion(),
                orden.getFechaFin()
        );
    }

    private void descontarMetrosEnInventario(Long rolloId, Double metros) {
        try {
            log.info("Llamando a ms-inventario para descontar {} metros del rollo {}", metros, rolloId);
            webClientInventario.put()
                    .uri("/api/rollos/{id}/usar", rolloId)
                    .bodyValue(Map.of("metros", metros))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Metros descontados correctamente en ms-inventario");
        } catch (WebClientResponseException ex) {
            log.error("Error al descontar metros en ms-inventario: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new IllegalStateException("Error al descontar metros en inventario: " + ex.getResponseBodyAsString());
        } catch (Exception ex) {
            log.error("Error de conexión con ms-inventario: {}", ex.getMessage());
            throw new IllegalStateException("No se pudo conectar con el servicio de inventario");
        }
    }
}
