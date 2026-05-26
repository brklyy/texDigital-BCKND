package cl.texDigital.ms_productos.controller;

import cl.texDigital.ms_productos.dto.ProductoRequestDTO;
import cl.texDigital.ms_productos.model.Producto;
import cl.texDigital.ms_productos.service.ProductoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<List<Producto>> findAll() {
        log.info("GET /api/productos");
        return ResponseEntity.ok(productoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> findById(@PathVariable Long id) {
        log.info("GET /api/productos/{}", id);
        return ResponseEntity.ok(productoService.findById(id));
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Producto>> findByTipo(@PathVariable String tipo) {
        log.info("GET /api/productos/tipo/{}", tipo);
        return ResponseEntity.ok(productoService.findByTipo(tipo));
    }

    @PostMapping
    public ResponseEntity<Producto> save(@Valid @RequestBody ProductoRequestDTO dto) {
        log.info("POST /api/productos - nombre: {}", dto.getNombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> update(@PathVariable Long id,
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
