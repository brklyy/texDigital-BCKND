package cl.texDigital.ms_clientes.controller;

import cl.texDigital.ms_clientes.dto.ClienteRequestDTO;
import cl.texDigital.ms_clientes.dto.ClienteResponseDTO;
import cl.texDigital.ms_clientes.service.ClienteService;
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
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gestion de clientes de texDigital")
public class ClienteController {

    private final ClienteService clienteService;

    @Operation(summary = "Listar todos los clientes",
            description = "Devuelve la lista completa de clientes con enlaces HATEOAS de navegacion.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"))
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<ClienteResponseDTO>>> getAll() {
        List<EntityModel<ClienteResponseDTO>> clientes = clienteService.findAll().stream()
                .map(this::toModel)
                .toList();

        CollectionModel<EntityModel<ClienteResponseDTO>> collection = CollectionModel.of(clientes,
                linkTo(methodOn(ClienteController.class).getAll()).withSelfRel());

        return ResponseEntity.ok(collection);
    }

    @Operation(summary = "Obtener cliente por id",
            description = "Busca un cliente especifico por su identificador e incluye enlaces HATEOAS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ClienteResponseDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(clienteService.findById(id)));
    }

    @Operation(summary = "Crear un cliente",
            description = "Registra un nuevo cliente. El email debe ser unico.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente creado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o email ya existente")
    })
    @PostMapping
    public ResponseEntity<ClienteResponseDTO> create(@Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.create(dto));
    }

    @Operation(summary = "Actualizar un cliente",
            description = "Modifica los datos de un cliente existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o email ya existente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> update(@PathVariable Long id,
                                                     @Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.ok(clienteService.update(id, dto));
    }

    @Operation(summary = "Eliminar un cliente",
            description = "Elimina un cliente por su identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cliente eliminado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clienteService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<ClienteResponseDTO> toModel(ClienteResponseDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(ClienteController.class).getById(dto.getId())).withSelfRel(),
                linkTo(methodOn(ClienteController.class).getAll()).withRel("clientes"));
    }
}
