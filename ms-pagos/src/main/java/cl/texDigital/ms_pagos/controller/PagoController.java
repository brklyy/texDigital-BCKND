package cl.texDigital.ms_pagos.controller;

import cl.texDigital.ms_pagos.dto.PagoRequestDTO;
import cl.texDigital.ms_pagos.dto.PagoResponseDTO;
import cl.texDigital.ms_pagos.service.PagoService;
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
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
@Tag(name = "Pagos", description = "Gestion de pagos de pedidos de texDigital")
public class PagoController {

    private final PagoService pagoService;

    @Operation(summary = "Listar todos los pagos",
            description = "Devuelve todos los pagos con enlaces HATEOAS de navegacion.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"))
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<PagoResponseDTO>>> getAll() {
        List<EntityModel<PagoResponseDTO>> pagos = pagoService.findAll().stream()
                .map(this::toModel)
                .toList();
        return ResponseEntity.ok(CollectionModel.of(pagos,
                linkTo(methodOn(PagoController.class).getAll()).withSelfRel()));
    }

    @Operation(summary = "Obtener pago por id",
            description = "Busca un pago por su identificador e incluye enlaces HATEOAS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago encontrado"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<PagoResponseDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(pagoService.findById(id)));
    }

    @Operation(summary = "Listar pagos por pedido",
            description = "Devuelve todos los pagos asociados a un pedido.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"))
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<CollectionModel<EntityModel<PagoResponseDTO>>> getByPedido(@PathVariable Long pedidoId) {
        List<EntityModel<PagoResponseDTO>> pagos = pagoService.findByPedidoId(pedidoId).stream()
                .map(this::toModel)
                .toList();
        return ResponseEntity.ok(CollectionModel.of(pagos,
                linkTo(methodOn(PagoController.class).getByPedido(pedidoId)).withSelfRel()));
    }

    @Operation(summary = "Listar pagos por estado",
            description = "Devuelve los pagos filtrados por estado (PENDIENTE, PAGADO, RECHAZADO, REEMBOLSADO).")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"))
    @GetMapping("/estado/{estado}")
    public ResponseEntity<CollectionModel<EntityModel<PagoResponseDTO>>> getByEstado(@PathVariable String estado) {
        List<EntityModel<PagoResponseDTO>> pagos = pagoService.findByEstado(estado).stream()
                .map(this::toModel)
                .toList();
        return ResponseEntity.ok(CollectionModel.of(pagos,
                linkTo(methodOn(PagoController.class).getByEstado(estado)).withSelfRel()));
    }

    @Operation(summary = "Registrar un pago",
            description = "Crea un pago para un pedido. Toma el monto desde ms-pedidos, aplica el descuento "
                    + "indicado y calcula el IVA (19%).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pago registrado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o pedido ya pagado"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado en ms-pedidos"),
            @ApiResponse(responseCode = "503", description = "ms-pedidos no disponible")
    })
    @PostMapping
    public ResponseEntity<PagoResponseDTO> create(@Valid @RequestBody PagoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.create(dto));
    }

    @Operation(summary = "Actualizar un pago",
            description = "Modifica el metodo de pago y el descuento, recalculando neto, IVA y total.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PagoResponseDTO> update(@PathVariable Long id,
                                                  @Valid @RequestBody PagoRequestDTO dto) {
        return ResponseEntity.ok(pagoService.update(id, dto));
    }

    @Operation(summary = "Cambiar el estado de un pago",
            description = "Actualiza solo el estado del pago (por ejemplo, a REEMBOLSADO).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "400", description = "Estado invalido"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    @PatchMapping("/{id}/estado")
    public ResponseEntity<PagoResponseDTO> updateEstado(@PathVariable Long id,
                                                        @RequestParam String estado) {
        return ResponseEntity.ok(pagoService.updateEstado(id, estado));
    }

    @Operation(summary = "Eliminar un pago",
            description = "Elimina un pago por su identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pago eliminado"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        pagoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<PagoResponseDTO> toModel(PagoResponseDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(PagoController.class).getById(dto.getId())).withSelfRel(),
                linkTo(methodOn(PagoController.class).getByPedido(dto.getPedidoId())).withRel("pagos-del-pedido"),
                linkTo(methodOn(PagoController.class).getAll()).withRel("pagos"));
    }
}
