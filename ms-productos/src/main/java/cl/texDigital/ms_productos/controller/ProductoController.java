package cl.texDigital.ms_productos.controller;

import cl.texDigital.ms_productos.dto.ProductoRequestDTO;
import cl.texDigital.ms_productos.dto.ProductoResponseDTO;
import cl.texDigital.ms_productos.service.ProductoService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> findAll() {
        log.info("GET /api/productos");
        return ResponseEntity.ok(productoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> findById(@PathVariable Long id) {
        log.info("GET /api/productos/{}", id);
        return ResponseEntity.ok(productoService.findById(id));
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<ProductoResponseDTO>> findByTipo(@PathVariable String tipo) {
        log.info("GET /api/productos/tipo/{}", tipo);
        return ResponseEntity.ok(productoService.findByTipo(tipo));
    }

    @PostMapping
    public ResponseEntity<ProductoResponseDTO> save(@Valid @RequestBody ProductoRequestDTO dto) {
        log.info("POST /api/productos - nombre: {}", dto.getNombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> update(@PathVariable Long id,
                                                       @Valid @RequestBody ProductoRequestDTO dto) {
        log.info("PUT /api/productos/{}", id);
        return ResponseEntity.ok(productoService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/productos/{}", id);
        productoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
