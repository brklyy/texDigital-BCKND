package cl.texDigital.ms_produccion.controller;

import cl.texDigital.ms_produccion.dto.OrdenProduccionRequestDTO;
import cl.texDigital.ms_produccion.model.OrdenProduccion;
import cl.texDigital.ms_produccion.service.OrdenProduccionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ordenes")
public class OrdenProduccionController {

    private static final Logger log = LoggerFactory.getLogger(OrdenProduccionController.class);

    private final OrdenProduccionService ordenService;

    public OrdenProduccionController(OrdenProduccionService ordenService) {
        this.ordenService = ordenService;
    }

    @GetMapping
    public ResponseEntity<List<OrdenProduccion>> getAll() {
        log.info("GET /api/ordenes");
        return ResponseEntity.ok(ordenService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenProduccion> getById(@PathVariable Long id) {
        log.info("GET /api/ordenes/{}", id);
        return ResponseEntity.ok(ordenService.findById(id));
    }

    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<List<OrdenProduccion>> getByPedidoId(@PathVariable Long pedidoId) {
        log.info("GET /api/ordenes/pedido/{}", pedidoId);
        return ResponseEntity.ok(ordenService.findByPedidoId(pedidoId));
    }

    @GetMapping("/stats/metros")
    public ResponseEntity<Map<String, Double>> getTotalMetros() {
        log.info("GET /api/ordenes/stats/metros");
        return ResponseEntity.ok(Map.of("totalMetrosUsados", ordenService.getTotalMetros()));
    }

    @PostMapping
    public ResponseEntity<OrdenProduccion> create(@Valid @RequestBody OrdenProduccionRequestDTO dto) {
        log.info("POST /api/ordenes");
        return ResponseEntity.status(HttpStatus.CREATED).body(ordenService.save(dto));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<OrdenProduccion> updateEstado(@PathVariable Long id,
                                                         @RequestBody Map<String, String> body) {
        String nuevoEstado = body.get("estado");
        log.info("PATCH /api/ordenes/{}/estado -> {}", id, nuevoEstado);
        return ResponseEntity.ok(ordenService.updateEstado(id, nuevoEstado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/ordenes/{}", id);
        ordenService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
