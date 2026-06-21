package cl.texDigital.ms_pedidos.controller;

import cl.texDigital.ms_pedidos.dto.PedidoRequestDTO;
import cl.texDigital.ms_pedidos.dto.PedidoResponseDTO;
import cl.texDigital.ms_pedidos.service.PedidoService;
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
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Gestion de pedidos y sus detalles en texDigital")
public class PedidoController {

    private final PedidoService pedidoService;

    @Operation(summary = "Listar todos los pedidos",
            description = "Devuelve todos los pedidos con enlaces HATEOAS de navegacion.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"))
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<PedidoResponseDTO>>> getAll() {
        List<EntityModel<PedidoResponseDTO>> pedidos = pedidoService.findAll().stream()
                .map(this::toModel)
                .toList();
        return ResponseEntity.ok(CollectionModel.of(pedidos,
                linkTo(methodOn(PedidoController.class).getAll()).withSelfRel()));
    }

    @Operation(summary = "Obtener pedido por id",
            description = "Busca un pedido por su identificador e incluye enlaces HATEOAS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<PedidoResponseDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(pedidoService.findById(id)));
    }

    @Operation(summary = "Listar pedidos por cliente",
            description = "Devuelve todos los pedidos de un cliente especifico.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"))
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<CollectionModel<EntityModel<PedidoResponseDTO>>> getByClienteId(@PathVariable Long clienteId) {
        List<EntityModel<PedidoResponseDTO>> pedidos = pedidoService.findByClienteId(clienteId).stream()
                .map(this::toModel)
                .toList();
        return ResponseEntity.ok(CollectionModel.of(pedidos,
                linkTo(methodOn(PedidoController.class).getByClienteId(clienteId)).withSelfRel()));
    }

    @Operation(summary = "Listar pedidos por estado",
            description = "Devuelve los pedidos filtrados por estado (PENDIENTE, EN_PROCESO, COMPLETADO, CANCELADO).")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"))
    @GetMapping("/estado/{estado}")
    public ResponseEntity<CollectionModel<EntityModel<PedidoResponseDTO>>> getByEstado(@PathVariable String estado) {
        List<EntityModel<PedidoResponseDTO>> pedidos = pedidoService.findByEstado(estado).stream()
                .map(this::toModel)
                .toList();
        return ResponseEntity.ok(CollectionModel.of(pedidos,
                linkTo(methodOn(PedidoController.class).getByEstado(estado)).withSelfRel()));
    }

    @Operation(summary = "Crear un pedido",
            description = "Crea un pedido. Valida que el cliente exista y este ACTIVO en ms-clientes, "
                    + "y obtiene el precio de cada producto desde ms-productos.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido creado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o cliente inactivo"),
            @ApiResponse(responseCode = "404", description = "Cliente o producto no encontrado"),
            @ApiResponse(responseCode = "503", description = "ms-clientes o ms-productos no disponible")
    })
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> create(@Valid @RequestBody PedidoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.create(dto));
    }

    @Operation(summary = "Actualizar un pedido",
            description = "Reemplaza el cliente y los detalles de un pedido existente, recalculando el total.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o cliente inactivo"),
            @ApiResponse(responseCode = "404", description = "Pedido, cliente o producto no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> update(@PathVariable Long id,
                                                    @Valid @RequestBody PedidoRequestDTO dto) {
        return ResponseEntity.ok(pedidoService.update(id, dto));
    }

    @Operation(summary = "Cambiar el estado de un pedido",
            description = "Actualiza solo el estado del pedido.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    @PatchMapping("/{id}/estado")
    public ResponseEntity<PedidoResponseDTO> updateEstado(@PathVariable Long id,
                                                          @RequestParam String estado) {
        return ResponseEntity.ok(pedidoService.updateEstado(id, estado));
    }

    @Operation(summary = "Eliminar un pedido",
            description = "Elimina un pedido por su identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pedido eliminado"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        pedidoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<PedidoResponseDTO> toModel(PedidoResponseDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(PedidoController.class).getById(dto.getId())).withSelfRel(),
                linkTo(methodOn(PedidoController.class).getByClienteId(dto.getClienteId())).withRel("pedidos-del-cliente"),
                linkTo(methodOn(PedidoController.class).getAll()).withRel("pedidos"));
    }
}
