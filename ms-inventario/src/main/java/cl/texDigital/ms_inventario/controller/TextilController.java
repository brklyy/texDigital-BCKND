package cl.texDigital.ms_inventario.controller;

import cl.texDigital.ms_inventario.dto.TextilRequestDTO;
import cl.texDigital.ms_inventario.dto.TextilResponseDTO;
import cl.texDigital.ms_inventario.service.TextilService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/textiles")
@RequiredArgsConstructor
@Tag(name = "Textiles", description = "Gestion de los tipos de tela (textiles) de texDigital")
public class TextilController {

    private final TextilService textilService;

    @Operation(summary = "Listar todos los textiles",
            description = "Devuelve todos los textiles con enlaces HATEOAS de navegacion.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"))
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<TextilResponseDTO>>> getAll() {
        List<EntityModel<TextilResponseDTO>> textiles = textilService.findAll().stream()
                .map(this::toModel)
                .toList();
        return ResponseEntity.ok(CollectionModel.of(textiles,
                linkTo(methodOn(TextilController.class).getAll()).withSelfRel()));
    }

    @Operation(summary = "Obtener textil por id",
            description = "Busca un textil por su identificador e incluye enlaces HATEOAS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Textil encontrado"),
            @ApiResponse(responseCode = "404", description = "Textil no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<TextilResponseDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(textilService.findById(id)));
    }

    @Operation(summary = "Crear un textil",
            description = "Registra un nuevo textil. El nombre debe ser unico.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Textil creado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o nombre ya existente")
    })
    @PostMapping
    public ResponseEntity<TextilResponseDTO> create(@Valid @RequestBody TextilRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(textilService.create(dto));
    }

    @Operation(summary = "Actualizar un textil",
            description = "Modifica los datos de un textil existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Textil actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @ApiResponse(responseCode = "404", description = "Textil no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TextilResponseDTO> update(@PathVariable Long id,
                                                    @Valid @RequestBody TextilRequestDTO dto) {
        return ResponseEntity.ok(textilService.update(id, dto));
    }

    @Operation(summary = "Eliminar un textil",
            description = "Elimina un textil. Falla si tiene rollos asociados.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Textil eliminado"),
            @ApiResponse(responseCode = "400", description = "El textil tiene rollos asociados"),
            @ApiResponse(responseCode = "404", description = "Textil no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        textilService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<TextilResponseDTO> toModel(TextilResponseDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(TextilController.class).getById(dto.getId())).withSelfRel(),
                linkTo(methodOn(TextilController.class).getAll()).withRel("textiles"));
    }
}
