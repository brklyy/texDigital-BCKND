package cl.texDigital.ms_envios.controller;

import cl.texDigital.ms_envios.dto.EnvioRequestDTO;
import cl.texDigital.ms_envios.dto.EnvioResponseDTO;
import cl.texDigital.ms_envios.model.EstadoEnvio;
import cl.texDigital.ms_envios.service.EnvioService;
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
public class EnvioController {

    private final EnvioService envioService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<EnvioResponseDTO>>> listar() {
        List<EntityModel<EnvioResponseDTO>> envios = envioService.listarTodos().stream()
                .map(e -> EntityModel.of(e,
                        linkTo(methodOn(EnvioController.class).obtenerPorId(e.getId())).withSelfRel(),
                        linkTo(methodOn(EnvioController.class).listar()).withRel("envios")))
                .toList();

        return ResponseEntity.ok(CollectionModel.of(envios,
                linkTo(methodOn(EnvioController.class).listar()).withSelfRel()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<EnvioResponseDTO>> obtenerPorId(@PathVariable Long id) {
        EnvioResponseDTO dto = envioService.obtenerPorId(id);
        return ResponseEntity.ok(EntityModel.of(dto,
                linkTo(methodOn(EnvioController.class).obtenerPorId(id)).withSelfRel(),
                linkTo(methodOn(EnvioController.class).listar()).withRel("envios")));
    }

    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<CollectionModel<EntityModel<EnvioResponseDTO>>> obtenerPorPedido(
            @PathVariable Long pedidoId) {
        List<EntityModel<EnvioResponseDTO>> envios = envioService.obtenerPorPedido(pedidoId).stream()
                .map(e -> EntityModel.of(e,
                        linkTo(methodOn(EnvioController.class).obtenerPorId(e.getId())).withSelfRel()))
                .toList();

        return ResponseEntity.ok(CollectionModel.of(envios,
                linkTo(methodOn(EnvioController.class).obtenerPorPedido(pedidoId)).withSelfRel()));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<CollectionModel<EntityModel<EnvioResponseDTO>>> obtenerPorEstado(
            @PathVariable EstadoEnvio estado) {
        List<EntityModel<EnvioResponseDTO>> envios = envioService.obtenerPorEstado(estado).stream()
                .map(e -> EntityModel.of(e,
                        linkTo(methodOn(EnvioController.class).obtenerPorId(e.getId())).withSelfRel()))
                .toList();

        return ResponseEntity.ok(CollectionModel.of(envios,
                linkTo(methodOn(EnvioController.class).obtenerPorEstado(estado)).withSelfRel()));
    }

    @PostMapping
    public ResponseEntity<EntityModel<EnvioResponseDTO>> crear(@Valid @RequestBody EnvioRequestDTO dto) {
        EnvioResponseDTO creado = envioService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EntityModel.of(creado,
                        linkTo(methodOn(EnvioController.class).obtenerPorId(creado.getId())).withSelfRel(),
                        linkTo(methodOn(EnvioController.class).listar()).withRel("envios")));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<EnvioResponseDTO>> actualizar(
            @PathVariable Long id, @Valid @RequestBody EnvioRequestDTO dto) {
        EnvioResponseDTO actualizado = envioService.actualizar(id, dto);
        return ResponseEntity.ok(EntityModel.of(actualizado,
                linkTo(methodOn(EnvioController.class).obtenerPorId(id)).withSelfRel(),
                linkTo(methodOn(EnvioController.class).listar()).withRel("envios")));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<EntityModel<EnvioResponseDTO>> cambiarEstado(
            @PathVariable Long id, @RequestParam EstadoEnvio estado) {
        EnvioResponseDTO actualizado = envioService.cambiarEstado(id, estado);
        return ResponseEntity.ok(EntityModel.of(actualizado,
                linkTo(methodOn(EnvioController.class).obtenerPorId(id)).withSelfRel(),
                linkTo(methodOn(EnvioController.class).listar()).withRel("envios")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        envioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
