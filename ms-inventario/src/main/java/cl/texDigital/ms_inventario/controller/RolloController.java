package cl.texDigital.ms_inventario.controller;

import cl.texDigital.ms_inventario.dto.RolloRequestDTO;
import cl.texDigital.ms_inventario.dto.RolloResponseDTO;
import cl.texDigital.ms_inventario.dto.UsarMetrosRequestDTO;
import cl.texDigital.ms_inventario.service.RolloService;
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
@RequestMapping("/api/rollos")
@RequiredArgsConstructor
@Tag(name = "Rollos", description = "Gestion de los rollos de tela (stock) de texDigital")
public class RolloController {

    private final RolloService rolloService;

    @Operation(summary = "Listar todos los rollos",
            description = "Devuelve todos los rollos con enlaces HATEOAS de navegacion.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"))
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<RolloResponseDTO>>> getAll() {
        List<EntityModel<RolloResponseDTO>> rollos = rolloService.findAll().stream()
                .map(this::toModel)
                .toList();
        return ResponseEntity.ok(CollectionModel.of(rollos,
                linkTo(methodOn(RolloController.class).getAll()).withSelfRel()));
    }

    @Operation(summary = "Obtener rollo por id",
            description = "Busca un rollo por su identificador e incluye enlaces HATEOAS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rollo encontrado"),
            @ApiResponse(responseCode = "404", description = "Rollo no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<RolloResponseDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(rolloService.findById(id)));
    }

    @Operation(summary = "Listar rollos por textil",
            description = "Devuelve todos los rollos de un textil especifico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"),
            @ApiResponse(responseCode = "404", description = "Textil no encontrado")
    })
    @GetMapping("/textil/{textilId}")
    public ResponseEntity<CollectionModel<EntityModel<RolloResponseDTO>>> getByTextilId(@PathVariable Long textilId) {
        List<EntityModel<RolloResponseDTO>> rollos = rolloService.findByTextilId(textilId).stream()
                .map(this::toModel)
                .toList();
        return ResponseEntity.ok(CollectionModel.of(rollos,
                linkTo(methodOn(RolloController.class).getByTextilId(textilId)).withSelfRel()));
    }

    @Operation(summary = "Crear un rollo",
            description = "Registra un nuevo rollo asociado a un textil existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Rollo creado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @ApiResponse(responseCode = "404", description = "Textil no encontrado")
    })
    @PostMapping
    public ResponseEntity<RolloResponseDTO> create(@Valid @RequestBody RolloRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rolloService.create(dto));
    }

    @Operation(summary = "Actualizar un rollo",
            description = "Modifica los datos de un rollo existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rollo actualizado"),
            @ApiResponse(responseCode = "404", description = "Rollo o textil no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<RolloResponseDTO> update(@PathVariable Long id,
                                                   @Valid @RequestBody RolloRequestDTO dto) {
        return ResponseEntity.ok(rolloService.update(id, dto));
    }

    @Operation(summary = "Consumir metros de un rollo",
            description = "Descuenta metros del rollo (usado por produccion). Si no quedan metros, lo marca como AGOTADO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Metros consumidos"),
            @ApiResponse(responseCode = "404", description = "Rollo no encontrado"),
            @ApiResponse(responseCode = "409", description = "Metros insuficientes en el rollo")
    })
    @PutMapping("/{id}/usar")
    public ResponseEntity<RolloResponseDTO> usarMetros(@PathVariable Long id,
                                                       @Valid @RequestBody UsarMetrosRequestDTO dto) {
        return ResponseEntity.ok(rolloService.usarMetros(id, dto));
    }

    @Operation(summary = "Eliminar un rollo",
            description = "Elimina un rollo por su identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Rollo eliminado"),
            @ApiResponse(responseCode = "404", description = "Rollo no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        rolloService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<RolloResponseDTO> toModel(RolloResponseDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(RolloController.class).getById(dto.getId())).withSelfRel(),
                linkTo(methodOn(RolloController.class).getByTextilId(dto.getTextilId())).withRel("rollos-del-textil"),
                linkTo(methodOn(RolloController.class).getAll()).withRel("rollos"));
    }
}
