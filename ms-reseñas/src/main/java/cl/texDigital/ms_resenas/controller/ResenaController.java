package cl.texDigital.ms_resenas.controller;

import cl.texDigital.ms_resenas.dto.ResenaRequestDTO;
import cl.texDigital.ms_resenas.dto.ResenaResponseDTO;
import cl.texDigital.ms_resenas.service.ResenaService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/resenas")
public class ResenaController {

    private final ResenaService resenaService;

    public ResenaController(ResenaService resenaService) {
        this.resenaService = resenaService;
    }

    @GetMapping
    public ResponseEntity<List<ResenaResponseDTO>> getAll() {
        log.info("GET /api/resenas");
        return ResponseEntity.ok(resenaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResenaResponseDTO> getById(@PathVariable Long id) {
        log.info("GET /api/resenas/{}", id);
        return ResponseEntity.ok(resenaService.findById(id));
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<ResenaResponseDTO>> getByProductoId(@PathVariable Long productoId) {
        log.info("GET /api/resenas/producto/{}", productoId);
        return ResponseEntity.ok(resenaService.findByProductoId(productoId));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<ResenaResponseDTO>> getByClienteId(@PathVariable Long clienteId) {
        log.info("GET /api/resenas/cliente/{}", clienteId);
        return ResponseEntity.ok(resenaService.findByClienteId(clienteId));
    }

    @GetMapping("/producto/{productoId}/promedio")
    public ResponseEntity<Map<String, Double>> getPromedioByProductoId(@PathVariable Long productoId) {
        log.info("GET /api/resenas/producto/{}/promedio", productoId);
        return ResponseEntity.ok(Map.of("promedio", resenaService.getPromedioByProductoId(productoId)));
    }

    @PostMapping
    public ResponseEntity<ResenaResponseDTO> create(@Valid @RequestBody ResenaRequestDTO dto) {
        log.info("POST /api/resenas");
        return ResponseEntity.status(HttpStatus.CREATED).body(resenaService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResenaResponseDTO> update(@PathVariable Long id,
                                                      @Valid @RequestBody ResenaRequestDTO dto) {
        log.info("PUT /api/resenas/{}", id);
        return ResponseEntity.ok(resenaService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/resenas/{}", id);
        resenaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
