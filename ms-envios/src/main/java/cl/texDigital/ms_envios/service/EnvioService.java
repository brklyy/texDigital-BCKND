package cl.texDigital.ms_envios.service;

import cl.texDigital.ms_envios.client.PedidoClient;
import cl.texDigital.ms_envios.dto.EnvioRequestDTO;
import cl.texDigital.ms_envios.dto.EnvioResponseDTO;
import cl.texDigital.ms_envios.exception.EstadoInvalidoException;
import cl.texDigital.ms_envios.exception.ResourceNotFoundException;
import cl.texDigital.ms_envios.model.Envio;
import cl.texDigital.ms_envios.model.EstadoEnvio;
import cl.texDigital.ms_envios.repository.EnvioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnvioService {

    private final EnvioRepository envioRepository;
    private final PedidoClient pedidoClient;

    public List<EnvioResponseDTO> listarTodos() {
        return envioRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public EnvioResponseDTO obtenerPorId(Long id) {
        return toDTO(findOrThrow(id));
    }

    public List<EnvioResponseDTO> obtenerPorPedido(Long pedidoId) {
        return envioRepository.findByPedidoId(pedidoId).stream()
                .map(this::toDTO)
                .toList();
    }

    public List<EnvioResponseDTO> obtenerPorEstado(EstadoEnvio estado) {
        return envioRepository.findByEstado(estado).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public EnvioResponseDTO crear(EnvioRequestDTO dto) {
        pedidoClient.obtenerPedido(dto.getPedidoId());

        Envio envio = new Envio();
        envio.setPedidoId(dto.getPedidoId());
        envio.setDireccionEntrega(dto.getDireccionEntrega());
        envio.setTransportista(dto.getTransportista());
        envio.setCodigoSeguimiento(generarCodigo());
        envio.setEstado(EstadoEnvio.PENDIENTE);
        envio.setFechaCreacion(LocalDate.now());
        envio.setFechaEstimadaEntrega(dto.getFechaEstimadaEntrega());

        return toDTO(envioRepository.save(envio));
    }

    @Transactional
    public EnvioResponseDTO actualizar(Long id, EnvioRequestDTO dto) {
        Envio envio = findOrThrow(id);
        validarEstadoNoTerminal(envio);

        pedidoClient.obtenerPedido(dto.getPedidoId());

        envio.setPedidoId(dto.getPedidoId());
        envio.setDireccionEntrega(dto.getDireccionEntrega());
        envio.setTransportista(dto.getTransportista());
        envio.setFechaEstimadaEntrega(dto.getFechaEstimadaEntrega());

        return toDTO(envioRepository.save(envio));
    }

    @Transactional
    public EnvioResponseDTO cambiarEstado(Long id, EstadoEnvio nuevoEstado) {
        Envio envio = findOrThrow(id);
        validarTransicion(envio.getEstado(), nuevoEstado);

        envio.setEstado(nuevoEstado);
        if (nuevoEstado == EstadoEnvio.ENTREGADO) {
            envio.setFechaEntregaReal(LocalDate.now());
        }

        return toDTO(envioRepository.save(envio));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!envioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Envio no encontrado con id: " + id);
        }
        envioRepository.deleteById(id);
    }

    private Envio findOrThrow(Long id) {
        return envioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Envio no encontrado con id: " + id));
    }

    private void validarEstadoNoTerminal(Envio envio) {
        if (envio.getEstado() == EstadoEnvio.ENTREGADO || envio.getEstado() == EstadoEnvio.CANCELADO) {
            throw new EstadoInvalidoException("No se puede modificar un envio en estado " + envio.getEstado());
        }
    }

    private void validarTransicion(EstadoEnvio actual, EstadoEnvio nuevo) {
        if (actual == EstadoEnvio.ENTREGADO || actual == EstadoEnvio.CANCELADO) {
            throw new EstadoInvalidoException(
                    "No se puede cambiar el estado de un envio en estado terminal: " + actual);
        }
        if (nuevo == EstadoEnvio.PENDIENTE) {
            throw new EstadoInvalidoException("No se puede volver al estado PENDIENTE");
        }
    }

    private String generarCodigo() {
        String codigo;
        do {
            codigo = "ENV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (envioRepository.findByCodigoSeguimiento(codigo).isPresent());
        return codigo;
    }

    private EnvioResponseDTO toDTO(Envio e) {
        return new EnvioResponseDTO(
                e.getId(),
                e.getPedidoId(),
                e.getDireccionEntrega(),
                e.getTransportista(),
                e.getCodigoSeguimiento(),
                e.getEstado().name(),
                e.getFechaCreacion(),
                e.getFechaEstimadaEntrega(),
                e.getFechaEntregaReal()
        );
    }
}
