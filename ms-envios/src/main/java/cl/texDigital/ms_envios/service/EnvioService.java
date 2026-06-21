package cl.texDigital.ms_envios.service;

import cl.texDigital.ms_envios.client.ClienteClient;
import cl.texDigital.ms_envios.client.ClienteResponse;
import cl.texDigital.ms_envios.client.PedidoClient;
import cl.texDigital.ms_envios.client.PedidoResponse;
import cl.texDigital.ms_envios.dto.EnvioRequestDTO;
import cl.texDigital.ms_envios.dto.EnvioResponseDTO;
import cl.texDigital.ms_envios.exception.ResourceNotFoundException;
import cl.texDigital.ms_envios.model.Envio;
import cl.texDigital.ms_envios.repository.EnvioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class EnvioService {

    private final EnvioRepository envioRepository;
    private final PedidoClient pedidoClient;
    private final ClienteClient clienteClient;

    public List<EnvioResponseDTO> findAll() {
        log.debug("Obteniendo lista de todos los envios");
        return envioRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public EnvioResponseDTO findById(Long id) {
        log.debug("Buscando envio con id={}", id);
        Envio envio = envioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Envio no encontrado con id: " + id));
        return toResponseDTO(envio);
    }

    public List<EnvioResponseDTO> findByPedidoId(Long pedidoId) {
        log.debug("Buscando envios del pedido id={}", pedidoId);
        return envioRepository.findByPedidoId(pedidoId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<EnvioResponseDTO> findByClienteId(Long clienteId) {
        log.debug("Buscando envios del cliente id={}", clienteId);
        return envioRepository.findByClienteId(clienteId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<EnvioResponseDTO> findByEstado(String estado) {
        log.debug("Buscando envios con estado={}", estado);
        return envioRepository.findByEstado(estado).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public EnvioResponseDTO create(EnvioRequestDTO dto) {
        log.debug("Creando envio para pedidoId={}", dto.getPedidoId());

        PedidoResponse pedido = pedidoClient.findById(dto.getPedidoId());

        if ("COMPLETADO".equals(pedido.getEstado()) || "CANCELADO".equals(pedido.getEstado())) {
            throw new IllegalStateException(
                    "No se puede crear un envio para un pedido en estado: " + pedido.getEstado());
        }

        Envio envio = new Envio();
        envio.setPedidoId(dto.getPedidoId());
        envio.setClienteId(pedido.getClienteId());
        envio.setFechaEnvio(LocalDate.now());
        envio.setFechaEstimadaEntrega(dto.getFechaEstimadaEntrega());
        envio.setEstado("PREPARANDO");
        envio.setTransportista(dto.getTransportista());
        envio.setNumeroSeguimiento(dto.getNumeroSeguimiento());
        envio.setDireccionDestino(dto.getDireccionDestino());

        Envio guardado = envioRepository.save(envio);
        log.debug("Envio creado con id={}", guardado.getId());

        pedidoClient.actualizarEstado(dto.getPedidoId(), "EN_PROCESO");
        log.debug("Pedido id={} actualizado a EN_PROCESO", dto.getPedidoId());

        return toResponseDTO(guardado);
    }

    public EnvioResponseDTO updateEstado(Long id, String estado) {
        log.debug("Actualizando estado de envio id={} a {}", id, estado);
        Envio envio = envioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Envio no encontrado con id: " + id));

        envio.setEstado(estado);
        Envio actualizado = envioRepository.save(envio);

        if ("ENTREGADO".equals(estado)) {
            pedidoClient.actualizarEstado(envio.getPedidoId(), "COMPLETADO");
            log.debug("Pedido id={} actualizado a COMPLETADO", envio.getPedidoId());
        } else if ("DEVUELTO".equals(estado)) {
            pedidoClient.actualizarEstado(envio.getPedidoId(), "CANCELADO");
            log.debug("Pedido id={} actualizado a CANCELADO", envio.getPedidoId());
        }

        return toResponseDTO(actualizado);
    }

    public void delete(Long id) {
        log.debug("Eliminando envio con id={}", id);
        if (!envioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Envio no encontrado con id: " + id);
        }
        envioRepository.deleteById(id);
        log.debug("Envio eliminado con id={}", id);
    }

    private EnvioResponseDTO toResponseDTO(Envio envio) {
        ClienteResponse cliente = clienteClient.findById(envio.getClienteId());
        return new EnvioResponseDTO(
                envio.getId(),
                envio.getPedidoId(),
                envio.getClienteId(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getEmail(),
                envio.getFechaEnvio(),
                envio.getFechaEstimadaEntrega(),
                envio.getEstado(),
                envio.getTransportista(),
                envio.getNumeroSeguimiento(),
                envio.getDireccionDestino()
        );
    }
}
