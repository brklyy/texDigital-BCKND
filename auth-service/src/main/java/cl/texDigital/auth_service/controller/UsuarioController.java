package cl.texDigital.auth_service.controller;

import cl.texDigital.auth_service.dto.UsuarioResponseDTO;
import cl.texDigital.auth_service.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestion de usuarios (solo ADMIN)")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @Operation(summary = "Listar todos los usuarios (ADMIN)")
    public ResponseEntity<CollectionModel<EntityModel<UsuarioResponseDTO>>> getAll() {
        List<EntityModel<UsuarioResponseDTO>> models = usuarioService.findAll().stream()
                .map(u -> EntityModel.of(u,
                        linkTo(methodOn(UsuarioController.class).getById(u.getId())).withSelfRel(),
                        linkTo(methodOn(UsuarioController.class).getAll()).withRel("usuarios")))
                .toList();
        return ResponseEntity.ok(CollectionModel.of(models,
                linkTo(methodOn(UsuarioController.class).getAll()).withSelfRel()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    public ResponseEntity<EntityModel<UsuarioResponseDTO>> getById(@PathVariable Long id) {
        UsuarioResponseDTO dto = usuarioService.findById(id);
        EntityModel<UsuarioResponseDTO> model = EntityModel.of(dto,
                linkTo(methodOn(UsuarioController.class).getById(id)).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).getAll()).withRel("usuarios"));
        return ResponseEntity.ok(model);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario por ID")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
