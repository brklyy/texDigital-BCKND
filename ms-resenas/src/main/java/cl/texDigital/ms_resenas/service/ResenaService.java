package cl.texDigital.ms_resenas.service;

import cl.texDigital.ms_resenas.dto.PedidoClienteDTO;
import cl.texDigital.ms_resenas.dto.ResenaRequestDTO;
import cl.texDigital.ms_resenas.dto.ResenaResponseDTO;
import cl.texDigital.ms_resenas.exception.ResourceNotFoundException;
import cl.texDigital.ms_resenas.model.Resena;
import cl.texDigital.ms_resenas.repository.ResenaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
public class ResenaService {

    private final ResenaRepository resenaRepository;
    private final WebClient webClientPedidos;

    public ResenaService(ResenaRepository resenaRepository, WebClient webClientPedidos) {
        this.resenaRepository = resenaRepository;
        this.webClientPedidos = webClientPedidos;
    }

    @Transactional(readOnly = true)
    public List<ResenaResponseDTO> findAll() {
        log.info("Consultando todas las reseñas");
        return resenaRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResenaResponseDTO findById(Long id) {
        log.info("Consultando reseña con id: {}", id);
        return toResponseDTO(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<ResenaResponseDTO> findByProductoId(Long productoId) {
        log.info("Consultando reseñas por productoId: {}", productoId);
        return resenaRepository.findByProductoId(productoId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ResenaResponseDTO> findByClienteId(Long clienteId) {
        log.info("Consultando reseñas por clienteId: {}", clienteId);
        return resenaRepository.findByClienteId(clienteId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Double getPromedioByProductoId(Long productoId) {
        log.info("Calculando promedio de puntaje para productoId: {}", productoId);
        Double promedio = resenaRepository.findPromedioPuntajeByProductoId(productoId);
        return promedio != null ? promedio : 0.0;
    }

    public ResenaResponseDTO save(ResenaRequestDTO dto) {
        log.info("Creando reseña del cliente {} para el producto {}", dto.getClienteId(), dto.getProductoId());
        validarCompra(dto.getClienteId(), dto.getProductoId());

        Resena resena = new Resena();
        resena.setClienteId(dto.getClienteId());
        resena.setProductoId(dto.getProductoId());
        resena.setPuntaje(dto.getPuntaje());
        resena.setComentario(dto.getComentario());
        resena.setFecha(LocalDateTime.now());

        Resena guardada = resenaRepository.save(resena);
        log.info("Reseña creada con id: {}", guardada.getId());
        return toResponseDTO(guardada);
    }

    public ResenaResponseDTO update(Long id, ResenaRequestDTO dto) {
        log.info("Actualizando reseña con id: {}", id);
        Resena resena = getOrThrow(id);
        resena.setPuntaje(dto.getPuntaje());
        resena.setComentario(dto.getComentario());
        Resena actualizada = resenaRepository.save(resena);
        log.info("Reseña con id {} actualizada", id);
        return toResponseDTO(actualizada);
    }

    public void delete(Long id) {
        Resena resena = getOrThrow(id);
        resenaRepository.delete(resena);
        log.info("Reseña eliminada con id: {}", id);
    }

    private Resena getOrThrow(Long id) {
        return resenaRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Reseña con id {} no encontrada", id);
                    return new ResourceNotFoundException("Reseña con id " + id + " no encontrada");
                });
    }

    private void validarCompra(Long clienteId, Long productoId) {
        List<PedidoClienteDTO> pedidos;
        try {
            log.info("Validando compra del producto {} por el cliente {}", productoId, clienteId);
            pedidos = webClientPedidos.get()
                    .uri("/api/pedidos/cliente/{clienteId}", clienteId)
                    .retrieve()
                    .bodyToFlux(PedidoClienteDTO.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("Error al consultar pedidos del cliente {}: {} - {}", clienteId, ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new IllegalStateException("No se pudo validar la compra del producto");
        } catch (Exception ex) {
            log.error("Error de conexión con ms-pedidos: {}", ex.getMessage());
            throw new IllegalStateException("No se pudo conectar con el servicio de pedidos");
        }

        boolean compro = pedidos != null && pedidos.stream()
                .filter(p -> p.getDetalles() != null)
                .flatMap(p -> p.getDetalles().stream())
                .anyMatch(d -> productoId.equals(d.getProductoId()));

        if (!compro) {
            log.warn("El cliente {} no ha comprado el producto {}, no puede dejar una reseña", clienteId, productoId);
            throw new IllegalArgumentException("El cliente no ha comprado este producto, no puede dejar una reseña");
        }
    }

    private ResenaResponseDTO toResponseDTO(Resena resena) {
        return new ResenaResponseDTO(
                resena.getId(),
                resena.getClienteId(),
                resena.getProductoId(),
                resena.getPuntaje(),
                calcularEstrellas(resena.getPuntaje()),
                resena.getComentario(),
                resena.getFecha()
        );
    }

    private String calcularEstrellas(Integer puntaje) {
        int estrellasCompletas = puntaje / 2;
        boolean mediaEstrella = puntaje % 2 == 1;
        return "★".repeat(estrellasCompletas) + (mediaEstrella ? "⯨" : "");
    }
}
