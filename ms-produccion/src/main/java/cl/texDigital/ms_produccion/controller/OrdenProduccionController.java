package cl.texDigital.ms_produccion.controller;

import cl.texDigital.ms_produccion.dto.OrdenProduccionRequestDTO;
import cl.texDigital.ms_produccion.dto.OrdenProduccionResponseDTO;
import cl.texDigital.ms_produccion.service.OrdenProduccionService;
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
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/ordenes")
@RequiredArgsConstructor
@Tag(name = "Ordenes de Produccion", description = "Gestion del ciclo de produccion de pedidos textiles")
public class OrdenProduccionController {

    private final OrdenProduccionService ordenService;

    @Operation(summary = "Listar todas las ordenes",
            description = "Devuelve la lista completa de ordenes de produccion con enlaces HATEOAS.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"))
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<OrdenProduccionResponseDTO>>> getAll() {
        List<EntityModel<OrdenProduccionResponseDTO>> ordenes = ordenService.findAll().stream()
                .map(this::toModel)
                .toList();

        CollectionModel<EntityModel<OrdenProduccionResponseDTO>> collection = CollectionModel.of(ordenes,
                linkTo(methodOn(OrdenProduccionController.class).getAll()).withSelfRel());

        return ResponseEntity.ok(collection);
    }

    @Operation(summary = "Obtener orden por id",
            description = "Busca una orden de produccion por su identificador e incluye enlaces HATEOAS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orden encontrada"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<OrdenProduccionResponseDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(ordenService.findById(id)));
    }

    @Operation(summary = "Listar ordenes por pedido",
            description = "Filtra las ordenes de produccion asociadas a un pedido especifico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<CollectionModel<EntityModel<OrdenProduccionResponseDTO>>> getByPedidoId(
            @PathVariable Long pedidoId) {
        List<EntityModel<OrdenProduccionResponseDTO>> ordenes = ordenService.findByPedidoId(pedidoId).stream()
                .map(this::toModel)
                .toList();

        CollectionModel<EntityModel<OrdenProduccionResponseDTO>> collection = CollectionModel.of(ordenes,
                linkTo(methodOn(OrdenProduccionController.class).getByPedidoId(pedidoId)).withSelfRel(),
                linkTo(methodOn(OrdenProduccionController.class).getAll()).withRel("ordenes"));

        return ResponseEntity.ok(collection);
    }

    @Operation(summary = "Total de metros usados en produccion",
            description = "Devuelve la suma de metros usados en todas las ordenes de produccion.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Total calculado correctamente"))
    @GetMapping("/stats/metros")
    public ResponseEntity<Map<String, Double>> getTotalMetros() {
        return ResponseEntity.ok(Map.of("totalMetrosUsados", ordenService.getTotalMetros()));
    }

    @Operation(summary = "Crear una orden de produccion",
            description = "Registra una nueva orden y descuenta metros en ms-inventario.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Orden creada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o stock insuficiente")
    })
    @PostMapping
    public ResponseEntity<OrdenProduccionResponseDTO> create(@Valid @RequestBody OrdenProduccionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ordenService.save(dto));
    }

    @Operation(summary = "Actualizar una orden de produccion",
            description = "Modifica los datos de una orden existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orden actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<OrdenProduccionResponseDTO> update(@PathVariable Long id,
                                                              @Valid @RequestBody OrdenProduccionRequestDTO dto) {
        return ResponseEntity.ok(ordenService.update(id, dto));
    }

    @Operation(summary = "Cambiar estado de una orden",
            description = "Actualiza el estado de una orden. Estados validos: PENDIENTE, EN_PRODUCCION, COMPLETADO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "400", description = "Estado invalido"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    @PatchMapping("/{id}/estado")
    public ResponseEntity<OrdenProduccionResponseDTO> updateEstado(@PathVariable Long id,
                                                                    @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ordenService.updateEstado(id, body.get("estado")));
    }

    @Operation(summary = "Eliminar una orden de produccion",
            description = "Elimina una orden por su identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Orden eliminada"),
            @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ordenService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<OrdenProduccionResponseDTO> toModel(OrdenProduccionResponseDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(OrdenProduccionController.class).getById(dto.getId())).withSelfRel(),
                linkTo(methodOn(OrdenProduccionController.class).getAll()).withRel("ordenes"));
    }
}
