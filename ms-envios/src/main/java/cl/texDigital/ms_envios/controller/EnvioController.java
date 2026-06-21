package cl.texDigital.ms_envios.controller;

import cl.texDigital.ms_envios.dto.EnvioRequestDTO;
import cl.texDigital.ms_envios.dto.EnvioResponseDTO;
import cl.texDigital.ms_envios.service.EnvioService;
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
@RequestMapping("/api/envios")
@RequiredArgsConstructor
@Tag(name = "Envios", description = "Gestion de envios de pedidos en texDigital")
public class EnvioController {

    private final EnvioService envioService;

    @Operation(summary = "Listar todos los envios",
            description = "Devuelve todos los envios con datos enriquecidos del cliente y enlaces HATEOAS.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"))
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<EnvioResponseDTO>>> getAll() {
        List<EntityModel<EnvioResponseDTO>> envios = envioService.findAll().stream()
                .map(this::toModel)
                .toList();
        return ResponseEntity.ok(CollectionModel.of(envios,
                linkTo(methodOn(EnvioController.class).getAll()).withSelfRel()));
    }

    @Operation(summary = "Obtener envio por id",
            description = "Busca un envio por su identificador e incluye datos del cliente y enlaces HATEOAS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Envio encontrado"),
            @ApiResponse(responseCode = "404", description = "Envio no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<EnvioResponseDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(envioService.findById(id)));
    }

    @Operation(summary = "Listar envios por pedido",
            description = "Devuelve todos los envios asociados a un pedido especifico.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"))
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<CollectionModel<EntityModel<EnvioResponseDTO>>> getByPedidoId(@PathVariable Long pedidoId) {
        List<EntityModel<EnvioResponseDTO>> envios = envioService.findByPedidoId(pedidoId).stream()
                .map(this::toModel)
                .toList();
        return ResponseEntity.ok(CollectionModel.of(envios,
                linkTo(methodOn(EnvioController.class).getByPedidoId(pedidoId)).withSelfRel()));
    }

    @Operation(summary = "Listar envios por cliente",
            description = "Devuelve todos los envios de un cliente especifico.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"))
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<CollectionModel<EntityModel<EnvioResponseDTO>>> getByClienteId(@PathVariable Long clienteId) {
        List<EntityModel<EnvioResponseDTO>> envios = envioService.findByClienteId(clienteId).stream()
                .map(this::toModel)
                .toList();
        return ResponseEntity.ok(CollectionModel.of(envios,
                linkTo(methodOn(EnvioController.class).getByClienteId(clienteId)).withSelfRel()));
    }

    @Operation(summary = "Listar envios por estado",
            description = "Devuelve los envios filtrados por estado (PREPARANDO, EN_CAMINO, ENTREGADO, DEVUELTO).")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"))
    @GetMapping("/estado/{estado}")
    public ResponseEntity<CollectionModel<EntityModel<EnvioResponseDTO>>> getByEstado(@PathVariable String estado) {
        List<EntityModel<EnvioResponseDTO>> envios = envioService.findByEstado(estado).stream()
                .map(this::toModel)
                .toList();
        return ResponseEntity.ok(CollectionModel.of(envios,
                linkTo(methodOn(EnvioController.class).getByEstado(estado)).withSelfRel()));
    }

    @Operation(summary = "Crear un envio",
            description = "Crea un envio para un pedido. Valida que el pedido no este COMPLETADO ni CANCELADO, "
                    + "obtiene el clienteId desde ms-pedidos y actualiza el estado del pedido a EN_PROCESO.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Envio creado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o pedido en estado no permitido"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado"),
            @ApiResponse(responseCode = "503", description = "ms-pedidos o ms-clientes no disponible")
    })
    @PostMapping
    public ResponseEntity<EnvioResponseDTO> create(@Valid @RequestBody EnvioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(envioService.create(dto));
    }

    @Operation(summary = "Cambiar el estado de un envio",
            description = "Actualiza el estado del envio. Si pasa a ENTREGADO, actualiza el pedido a COMPLETADO. "
                    + "Si pasa a DEVUELTO, actualiza el pedido a CANCELADO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "404", description = "Envio no encontrado"),
            @ApiResponse(responseCode = "503", description = "ms-pedidos no disponible")
    })
    @PatchMapping("/{id}/estado")
    public ResponseEntity<EnvioResponseDTO> updateEstado(@PathVariable Long id,
                                                         @RequestParam String estado) {
        return ResponseEntity.ok(envioService.updateEstado(id, estado));
    }

    @Operation(summary = "Eliminar un envio",
            description = "Elimina un envio por su identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Envio eliminado"),
            @ApiResponse(responseCode = "404", description = "Envio no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        envioService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<EnvioResponseDTO> toModel(EnvioResponseDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(EnvioController.class).getById(dto.getId())).withSelfRel(),
                linkTo(methodOn(EnvioController.class).getByPedidoId(dto.getPedidoId())).withRel("envios-del-pedido"),
                linkTo(methodOn(EnvioController.class).getByClienteId(dto.getClienteId())).withRel("envios-del-cliente"),
                linkTo(methodOn(EnvioController.class).getAll()).withRel("envios"));
    }
}
