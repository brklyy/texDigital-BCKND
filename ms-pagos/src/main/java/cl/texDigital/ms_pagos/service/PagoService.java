package cl.texDigital.ms_pagos.service;

import cl.texDigital.ms_pagos.client.PedidoClient;
import cl.texDigital.ms_pagos.client.PedidoResponse;
import cl.texDigital.ms_pagos.dto.PagoRequestDTO;
import cl.texDigital.ms_pagos.dto.PagoResponseDTO;
import cl.texDigital.ms_pagos.exception.ResourceNotFoundException;
import cl.texDigital.ms_pagos.model.Pago;
import cl.texDigital.ms_pagos.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PagoService {

    private static final double IVA_RATE = 0.19;
    private static final Set<String> METODOS_VALIDOS = Set.of("EFECTIVO", "TARJETA", "TRANSFERENCIA");
    private static final Set<String> ESTADOS_VALIDOS = Set.of("PENDIENTE", "PAGADO", "RECHAZADO", "REEMBOLSADO");

    private final PagoRepository pagoRepository;
    private final PedidoClient pedidoClient;

    public List<PagoResponseDTO> findAll() {
        log.debug("Obteniendo lista de todos los pagos");
        return pagoRepository.findAll().stream().map(this::toResponseDTO).toList();
    }

    public PagoResponseDTO findById(Long id) {
        log.debug("Buscando pago con id={}", id);
        return toResponseDTO(buscarPago(id));
    }

    public List<PagoResponseDTO> findByPedidoId(Long pedidoId) {
        log.debug("Buscando pagos del pedido id={}", pedidoId);
        return pagoRepository.findByPedidoId(pedidoId).stream().map(this::toResponseDTO).toList();
    }

    public List<PagoResponseDTO> findByEstado(String estado) {
        log.debug("Buscando pagos con estado={}", estado);
        return pagoRepository.findByEstado(estado.toUpperCase()).stream().map(this::toResponseDTO).toList();
    }

    public PagoResponseDTO create(PagoRequestDTO dto) {
        log.debug("Creando pago para pedidoId={}", dto.getPedidoId());

        validarMetodo(dto.getMetodoPago());

        if (pagoRepository.existsByPedidoIdAndEstado(dto.getPedidoId(), "PAGADO")) {
            throw new IllegalStateException(
                    "El pedido " + dto.getPedidoId() + " ya tiene un pago registrado como PAGADO.");
        }

        PedidoResponse pedido = pedidoClient.findById(dto.getPedidoId());

        Pago pago = new Pago();
        pago.setPedidoId(dto.getPedidoId());
        pago.setMetodoPago(dto.getMetodoPago().toUpperCase());
        aplicarCalculos(pago, pedido.getTotal(), dto.getPorcentajeDescuento());
        pago.setEstado("PAGADO");
        pago.setFechaPago(LocalDate.now());

        Pago guardado = pagoRepository.save(pago);
        log.debug("Pago creado con id={}, total={}", guardado.getId(), guardado.getMontoTotal());

        pedidoClient.actualizarEstado(dto.getPedidoId(), "PAGADO");

        return toResponseDTO(guardado);
    }

    public PagoResponseDTO update(Long id, PagoRequestDTO dto) {
        log.debug("Actualizando pago id={}", id);
        Pago pago = buscarPago(id);
        validarMetodo(dto.getMetodoPago());

        pago.setMetodoPago(dto.getMetodoPago().toUpperCase());
        aplicarCalculos(pago, pago.getMontoBase(), dto.getPorcentajeDescuento());

        return toResponseDTO(pagoRepository.save(pago));
    }

    public PagoResponseDTO updateEstado(Long id, String estado) {
        log.debug("Actualizando estado de pago id={} a {}", id, estado);
        Pago pago = buscarPago(id);
        String nuevoEstado = estado == null ? "" : estado.toUpperCase();
        if (!ESTADOS_VALIDOS.contains(nuevoEstado)) {
            throw new IllegalArgumentException(
                    "Estado invalido: " + estado + ". Use PENDIENTE, PAGADO, RECHAZADO o REEMBOLSADO.");
        }
        pago.setEstado(nuevoEstado);
        return toResponseDTO(pagoRepository.save(pago));
    }

    public void delete(Long id) {
        log.debug("Eliminando pago con id={}", id);
        if (!pagoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Pago no encontrado con id: " + id);
        }
        pagoRepository.deleteById(id);
        log.debug("Pago eliminado con id={}", id);
    }

    private void aplicarCalculos(Pago pago, double montoBase, int porcentajeDescuento) {
        double montoDescuento = redondear(montoBase * (porcentajeDescuento / 100.0));
        double montoNeto = montoBase - montoDescuento;
        double iva = redondear(montoNeto * IVA_RATE);
        double montoTotal = montoNeto + iva;

        pago.setMontoBase(montoBase);
        pago.setPorcentajeDescuento(porcentajeDescuento);
        pago.setMontoDescuento(montoDescuento);
        pago.setMontoNeto(montoNeto);
        pago.setIva(iva);
        pago.setMontoTotal(montoTotal);
    }

    private double redondear(double valor) {
        return Math.round(valor);
    }

    private void validarMetodo(String metodoPago) {
        if (metodoPago == null || !METODOS_VALIDOS.contains(metodoPago.toUpperCase())) {
            throw new IllegalArgumentException(
                    "Metodo de pago invalido: " + metodoPago + ". Use EFECTIVO, TARJETA o TRANSFERENCIA.");
        }
    }

    private Pago buscarPago(Long id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con id: " + id));
    }

    private PagoResponseDTO toResponseDTO(Pago pago) {
        return new PagoResponseDTO(
                pago.getId(),
                pago.getPedidoId(),
                pago.getMetodoPago(),
                pago.getMontoBase(),
                pago.getPorcentajeDescuento(),
                pago.getMontoDescuento(),
                pago.getMontoNeto(),
                pago.getIva(),
                pago.getMontoTotal(),
                pago.getEstado(),
                pago.getFechaPago()
        );
    }
}
