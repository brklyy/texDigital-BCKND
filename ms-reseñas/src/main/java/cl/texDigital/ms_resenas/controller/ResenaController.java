package cl.texDigital.ms_resenas.controller;

import cl.texDigital.ms_resenas.dto.ResenaRequestDTO;
import cl.texDigital.ms_resenas.dto.ResenaResponseDTO;
import cl.texDigital.ms_resenas.service.ResenaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Reseñas", description = "Gestión de reseñas de productos. Un cliente solo puede reseñar productos que haya comprado previamente.")
public class ResenaController {

    private final ResenaService resenaService;

    public ResenaController(ResenaService resenaService) {
        this.resenaService = resenaService;
    }

    @Operation(summary = "Listar todas las reseñas", description = "Retorna todas las reseñas registradas con enlaces HATEOAS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente")
    })
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

    @Operation(summary = "Obtener reseña por ID", description = "Retorna una reseña específica por su identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reseña encontrada"),
            @ApiResponse(responseCode = "404", description = "Reseña no encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ResenaResponseDTO>> getById(
            @Parameter(description = "ID de la reseña", example = "1") @PathVariable Long id) {
        log.info("GET /api/resenas/{}", id);
        ResenaResponseDTO dto = resenaService.findById(id);
        return ResponseEntity.ok(EntityModel.of(dto,
                linkTo(methodOn(ResenaController.class).getById(id)).withSelfRel(),
                linkTo(methodOn(ResenaController.class).getAll()).withRel("resenas")));
    }

    @Operation(summary = "Listar reseñas por producto", description = "Retorna todas las reseñas asociadas a un producto específico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reseñas del producto obtenidas")
    })
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<CollectionModel<EntityModel<ResenaResponseDTO>>> getByProductoId(
            @Parameter(description = "ID del producto", example = "3") @PathVariable Long productoId) {
        log.info("GET /api/resenas/producto/{}", productoId);
        List<EntityModel<ResenaResponseDTO>> models = resenaService.findByProductoId(productoId).stream()
                .map(dto -> EntityModel.of(dto,
                        linkTo(methodOn(ResenaController.class).getById(dto.getId())).withSelfRel()))
                .toList();
        return ResponseEntity.ok(CollectionModel.of(models,
                linkTo(methodOn(ResenaController.class).getByProductoId(productoId)).withSelfRel()));
    }

    @Operation(summary = "Listar reseñas por cliente", description = "Retorna todas las reseñas escritas por un cliente específico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reseñas del cliente obtenidas")
    })
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<CollectionModel<EntityModel<ResenaResponseDTO>>> getByClienteId(
            @Parameter(description = "ID del cliente", example = "1") @PathVariable Long clienteId) {
        log.info("GET /api/resenas/cliente/{}", clienteId);
        List<EntityModel<ResenaResponseDTO>> models = resenaService.findByClienteId(clienteId).stream()
                .map(dto -> EntityModel.of(dto,
                        linkTo(methodOn(ResenaController.class).getById(dto.getId())).withSelfRel()))
                .toList();
        return ResponseEntity.ok(CollectionModel.of(models,
                linkTo(methodOn(ResenaController.class).getByClienteId(clienteId)).withSelfRel()));
    }

    @Operation(summary = "Promedio de puntaje de un producto", description = "Calcula el promedio de puntajes de todas las reseñas de un producto.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promedio calculado exitosamente")
    })
    @GetMapping("/producto/{productoId}/promedio")
    public ResponseEntity<Map<String, Double>> getPromedioByProductoId(
            @Parameter(description = "ID del producto", example = "3") @PathVariable Long productoId) {
        log.info("GET /api/resenas/producto/{}/promedio", productoId);
        return ResponseEntity.ok(Map.of("promedio", resenaService.getPromedioByProductoId(productoId)));
    }

    @Operation(summary = "Crear reseña", description = "Crea una nueva reseña. El cliente debe haber comprado el producto previamente; de lo contrario se retorna 400.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reseña creada exitosamente",
                    content = @Content(schema = @Schema(implementation = ResenaResponseDTO.class),
                            examples = @ExampleObject(value = "{\"id\":1,\"clienteId\":1,\"productoId\":3,\"puntaje\":8,\"comentario\":\"Excelente tela, muy buen acabado.\"}"))),
            @ApiResponse(responseCode = "400", description = "Cliente no ha comprado el producto o datos inválidos", content = @Content),
            @ApiResponse(responseCode = "422", description = "Error de validación en los campos", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ResenaResponseDTO> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de la reseña",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"clienteId\":1,\"productoId\":3,\"puntaje\":8,\"comentario\":\"Excelente tela, muy buen acabado.\"}")))
            @Valid @RequestBody ResenaRequestDTO dto) {
        log.info("POST /api/resenas");
        return ResponseEntity.status(HttpStatus.CREATED).body(resenaService.save(dto));
    }

    @Operation(summary = "Actualizar reseña", description = "Actualiza el puntaje y comentario de una reseña existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reseña actualizada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Reseña no encontrada", content = @Content),
            @ApiResponse(responseCode = "422", description = "Error de validación", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<ResenaResponseDTO> update(
            @Parameter(description = "ID de la reseña", example = "1") @PathVariable Long id,
            @Valid @RequestBody ResenaRequestDTO dto) {
        log.info("PUT /api/resenas/{}", id);
        return ResponseEntity.ok(resenaService.update(id, dto));
    }

    @Operation(summary = "Eliminar reseña", description = "Elimina una reseña por su ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Reseña eliminada"),
            @ApiResponse(responseCode = "404", description = "Reseña no encontrada", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID de la reseña", example = "1") @PathVariable Long id) {
        log.info("DELETE /api/resenas/{}", id);
        resenaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
