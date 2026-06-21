package cl.texDigital.ms_productos.controller;

import cl.texDigital.ms_productos.dto.ProductoRequestDTO;
import cl.texDigital.ms_productos.dto.ProductoResponseDTO;
import cl.texDigital.ms_productos.service.ProductoService;
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
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Gestion del catalogo de productos de texDigital")
public class ProductoController {

    private final ProductoService productoService;

    @Operation(summary = "Listar todos los productos",
            description = "Devuelve la lista completa de productos con enlaces HATEOAS de navegacion.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"))
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<ProductoResponseDTO>>> getAll() {
        List<EntityModel<ProductoResponseDTO>> productos = productoService.findAll().stream()
                .map(this::toModel)
                .toList();

        CollectionModel<EntityModel<ProductoResponseDTO>> collection = CollectionModel.of(productos,
                linkTo(methodOn(ProductoController.class).getAll()).withSelfRel());

        return ResponseEntity.ok(collection);
    }

    @Operation(summary = "Obtener producto por id",
            description = "Busca un producto especifico por su identificador e incluye enlaces HATEOAS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ProductoResponseDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(toModel(productoService.findById(id)));
    }

    @Operation(summary = "Listar productos por tipo",
            description = "Filtra productos por tipo. Tipos validos: ESTAMPADO, LIENZO, CUBRESENSOR, BANDERA, BANDERIN, ESTUCHE, MONEDERO, MANTEL, FUNDA_COJIN, VELO, PANHUELO, BACKLIGHT, CAJA_BACKLIGHT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"),
            @ApiResponse(responseCode = "400", description = "Tipo de producto no valido")
    })
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<CollectionModel<EntityModel<ProductoResponseDTO>>> getByTipo(@PathVariable String tipo) {
        List<EntityModel<ProductoResponseDTO>> productos = productoService.findByTipo(tipo).stream()
                .map(this::toModel)
                .toList();

        CollectionModel<EntityModel<ProductoResponseDTO>> collection = CollectionModel.of(productos,
                linkTo(methodOn(ProductoController.class).getByTipo(tipo)).withSelfRel(),
                linkTo(methodOn(ProductoController.class).getAll()).withRel("productos"));

        return ResponseEntity.ok(collection);
    }

    @Operation(summary = "Crear un producto",
            description = "Registra un nuevo producto en el catalogo.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o tipo/textil no permitido")
    })
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> create(@Valid @RequestBody ProductoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.save(dto));
    }

    @Operation(summary = "Actualizar un producto",
            description = "Modifica los datos de un producto existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o tipo/textil no permitido"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> update(@PathVariable Long id,
                                                       @Valid @RequestBody ProductoRequestDTO dto) {
        return ResponseEntity.ok(productoService.update(id, dto));
    }

    @Operation(summary = "Eliminar un producto",
            description = "Elimina un producto del catalogo por su identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto eliminado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<ProductoResponseDTO> toModel(ProductoResponseDTO dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(ProductoController.class).getById(dto.getId())).withSelfRel(),
                linkTo(methodOn(ProductoController.class).getAll()).withRel("productos"));
    }
}
