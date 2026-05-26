package cl.texDigital.ms_productos.service;

import cl.texDigital.ms_productos.dto.ProductoRequestDTO;
import cl.texDigital.ms_productos.dto.ProductoResponseDTO;
import cl.texDigital.ms_productos.exception.ResourceNotFoundException;
import cl.texDigital.ms_productos.model.Producto;
import cl.texDigital.ms_productos.repository.ProductoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class ProductoService {

    private static final List<String> TIPOS_VALIDOS = List.of(
            "ESTAMPADO", "LIENZO", "CUBRESENSOR", "BANDERA", "BANDERIN",
            "ESTUCHE", "MONEDERO", "MANTEL", "FUNDA_COJIN", "VELO",
            "PANHUELO", "BACKLIGHT", "CAJA_BACKLIGHT"
    );

    private static final List<String> TIPOS_PEARL = List.of("BACKLIGHT", "CAJA_BACKLIGHT");

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> findAll() {
        log.info("Consultando todos los productos");
        return productoRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponseDTO findById(Long id) {
        log.info("Consultando producto con id: {}", id);
        return toResponseDTO(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> findByTipo(String tipo) {
        log.info("Consultando productos por tipo: {}", tipo);
        return productoRepository.findByTipo(tipo.toUpperCase()).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public ProductoResponseDTO save(ProductoRequestDTO dto) {
        validarTipo(dto.getTipo());
        validarTextilPearl(dto.getTipo(), dto.getTextilRequerido());

        Producto producto = new Producto();
        producto.setNombre(dto.getNombre());
        producto.setTipo(dto.getTipo().toUpperCase());
        producto.setTextilRequerido(dto.getTextilRequerido());
        producto.setPrecioBase(dto.getPrecioBase());

        Producto guardado = productoRepository.save(producto);
        log.info("Producto creado con id: {}", guardado.getId());
        return toResponseDTO(guardado);
    }

    public ProductoResponseDTO update(Long id, ProductoRequestDTO dto) {
        Producto producto = getOrThrow(id);
        validarTipo(dto.getTipo());
        validarTextilPearl(dto.getTipo(), dto.getTextilRequerido());

        producto.setNombre(dto.getNombre());
        producto.setTipo(dto.getTipo().toUpperCase());
        producto.setTextilRequerido(dto.getTextilRequerido());
        producto.setPrecioBase(dto.getPrecioBase());

        Producto actualizado = productoRepository.save(producto);
        log.info("Producto actualizado con id: {}", actualizado.getId());
        return toResponseDTO(actualizado);
    }

    public void delete(Long id) {
        Producto producto = getOrThrow(id);
        productoRepository.delete(producto);
        log.info("Producto eliminado con id: {}", id);
    }

    private Producto getOrThrow(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Producto con id {} no encontrado", id);
                    return new ResourceNotFoundException("Producto con id " + id + " no encontrado");
                });
    }

    private ProductoResponseDTO toResponseDTO(Producto producto) {
        return new ProductoResponseDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getTipo(),
                producto.getTextilRequerido(),
                producto.getPrecioBase()
        );
    }

    private void validarTipo(String tipo) {
        if (!TIPOS_VALIDOS.contains(tipo.toUpperCase())) {
            log.warn("Tipo de producto inválido: {}", tipo);
            throw new IllegalArgumentException("Tipo de producto inválido: " + tipo +
                    ". Tipos permitidos: " + TIPOS_VALIDOS);
        }
    }

    private void validarTextilPearl(String tipo, String textilRequerido) {
        if (TIPOS_PEARL.contains(tipo.toUpperCase()) && !"Pearl".equals(textilRequerido)) {
            log.warn("El tipo {} requiere textil Pearl, se recibió: {}", tipo, textilRequerido);
            throw new IllegalArgumentException(
                    "Los productos de tipo " + tipo + " deben usar textilRequerido = 'Pearl'");
        }
    }
}
