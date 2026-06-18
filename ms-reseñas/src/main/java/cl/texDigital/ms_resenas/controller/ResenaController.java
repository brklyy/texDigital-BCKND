package cl.texDigital.ms_resenas.controller;

import cl.texDigital.ms_resenas.dto.ResenaRequestDTO;
import cl.texDigital.ms_resenas.dto.ResenaResponseDTO;
import cl.texDigital.ms_resenas.service.ResenaService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Slf4j
@RestController
@RequestMapping("/api/resenas")
public class ResenaController {

    private final ResenaService resenaService;

    public ResenaController(ResenaService resenaService) {
        this.resenaService = resenaService;
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<ResenaResponseDTO>>> getAll() {
        log.info("GET /api/resenas");
        List<EntityModel<ResenaResponseDTO>> models = resenaService.findAll().stream()
                .map(dto -> EntityModel.of(dto,
                        linkTo(methodOn(ResenaController.class).getById(dto.getId())).withSelfRel()))
                .toList();
        return ResponseEntity.ok(CollectionModel.of(models,
                linkTo(methodOn(ResenaController.class).getAll()).withSelfRel()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ResenaResponseDTO>> getById(@PathVariable Long id) {
        log.info("GET /api/resenas/{}", id);
        ResenaResponseDTO dto = resenaService.findById(id);
        return ResponseEntity.ok(EntityModel.of(dto,
                linkTo(methodOn(ResenaController.class).getById(id)).withSelfRel(),
                linkTo(methodOn(ResenaController.class).getAll()).withRel("resenas")));
    }

    @GetMapping("/producto/{productoId}")
    public ResponseEntity<CollectionModel<EntityModel<ResenaResponseDTO>>> getByProductoId(@PathVariable Long productoId) {
        log.info("GET /api/resenas/producto/{}", productoId);
        List<EntityModel<ResenaResponseDTO>> models = resenaService.findByProductoId(productoId).stream()
                .map(dto -> EntityModel.of(dto,
                        linkTo(methodOn(ResenaController.class).getById(dto.getId())).withSelfRel()))
                .toList();
        return ResponseEntity.ok(CollectionModel.of(models,
                linkTo(methodOn(ResenaController.class).getByProductoId(productoId)).withSelfRel()));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<CollectionModel<EntityModel<ResenaResponseDTO>>> getByClienteId(@PathVariable Long clienteId) {
        log.info("GET /api/resenas/cliente/{}", clienteId);
        List<EntityModel<ResenaResponseDTO>> models = resenaService.findByClienteId(clienteId).stream()
                .map(dto -> EntityModel.of(dto,
                        linkTo(methodOn(ResenaController.class).getById(dto.getId())).withSelfRel()))
                .toList();
        return ResponseEntity.ok(CollectionModel.of(models,
                linkTo(methodOn(ResenaController.class).getByClienteId(clienteId)).withSelfRel()));
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
