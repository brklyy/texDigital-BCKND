package cl.texDigital.ms_productos.service;

import cl.texDigital.ms_productos.dto.ProductoRequestDTO;
import cl.texDigital.ms_productos.dto.ProductoResponseDTO;
import cl.texDigital.ms_productos.exception.ResourceNotFoundException;
import cl.texDigital.ms_productos.model.Producto;
import cl.texDigital.ms_productos.repository.ProductoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto productoEjemplo() {
        return new Producto(1L, "Banner grande", "ESTAMPADO", "Algodon", 15000.0);
    }

    private ProductoRequestDTO requestEjemplo() {
        ProductoRequestDTO dto = new ProductoRequestDTO();
        dto.setNombre("Banner grande");
        dto.setTipo("ESTAMPADO");
        dto.setTextilRequerido("Algodon");
        dto.setPrecioBase(15000.0);
        return dto;
    }

    @Test
    @DisplayName("findAll mapea todos los productos a DTOs")
    void findAll_devuelveLista() {
        when(productoRepository.findAll()).thenReturn(List.of(productoEjemplo()));

        List<ProductoResponseDTO> resultado = productoService.findAll();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Banner grande");
    }

    @Test
    @DisplayName("findById devuelve el producto cuando existe")
    void findById_devuelveProducto() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEjemplo()));

        ProductoResponseDTO resultado = productoService.findById(1L);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getTipo()).isEqualTo("ESTAMPADO");
    }

    @Test
    @DisplayName("findById lanza ResourceNotFound cuando no existe")
    void findById_lanzaNotFound() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("findByTipo filtra los productos normalizando el tipo a mayuscula")
    void findByTipo_devuelveLista() {
        when(productoRepository.findByTipo("ESTAMPADO")).thenReturn(List.of(productoEjemplo()));

        List<ProductoResponseDTO> resultado = productoService.findByTipo("estampado");

        assertThat(resultado).hasSize(1);
    }

    @Test
    @DisplayName("save crea el producto correctamente para un tipo valido")
    void save_creaProducto() {
        when(productoRepository.save(any(Producto.class))).thenReturn(productoEjemplo());

        ProductoResponseDTO resultado = productoService.save(requestEjemplo());

        assertThat(resultado.getNombre()).isEqualTo("Banner grande");
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    @DisplayName("save lanza IllegalArgument si el tipo no esta en TIPOS_VALIDOS")
    void save_lanzaIllegalArgument_siTipoInvalido() {
        ProductoRequestDTO dto = requestEjemplo();
        dto.setTipo("TAZA");

        assertThatThrownBy(() -> productoService.save(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo de producto");
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("save acepta BACKLIGHT cuando textilRequerido es Pearl")
    void save_aceptaBacklight_siTextilEsPearl() {
        ProductoRequestDTO dto = new ProductoRequestDTO();
        dto.setNombre("Backlight A4");
        dto.setTipo("BACKLIGHT");
        dto.setTextilRequerido("Pearl");
        dto.setPrecioBase(5000.0);

        Producto guardado = new Producto(2L, "Backlight A4", "BACKLIGHT", "Pearl", 5000.0);
        when(productoRepository.save(any(Producto.class))).thenReturn(guardado);

        ProductoResponseDTO resultado = productoService.save(dto);

        assertThat(resultado.getTipo()).isEqualTo("BACKLIGHT");
        assertThat(resultado.getTextilRequerido()).isEqualTo("Pearl");
    }

    @Test
    @DisplayName("save lanza IllegalArgument si BACKLIGHT usa textil distinto de Pearl")
    void save_lanzaIllegalArgument_siBacklightSinPearl() {
        ProductoRequestDTO dto = new ProductoRequestDTO();
        dto.setNombre("Backlight A4");
        dto.setTipo("BACKLIGHT");
        dto.setTextilRequerido("Algodon");
        dto.setPrecioBase(5000.0);

        assertThatThrownBy(() -> productoService.save(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Pearl");
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("save lanza IllegalArgument si CAJA_BACKLIGHT usa textil distinto de Pearl")
    void save_lanzaIllegalArgument_siCajaBacklightSinPearl() {
        ProductoRequestDTO dto = new ProductoRequestDTO();
        dto.setNombre("Caja Backlight A3");
        dto.setTipo("CAJA_BACKLIGHT");
        dto.setTextilRequerido("Lino");
        dto.setPrecioBase(8000.0);

        assertThatThrownBy(() -> productoService.save(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Pearl");
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("update modifica el producto cuando existe y los datos son validos")
    void update_modificaProducto() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEjemplo()));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
        ProductoRequestDTO dto = requestEjemplo();
        dto.setNombre("Banner XL");

        ProductoResponseDTO resultado = productoService.update(1L, dto);

        assertThat(resultado.getNombre()).isEqualTo("Banner XL");
    }

    @Test
    @DisplayName("update lanza ResourceNotFound cuando el producto no existe")
    void update_lanzaNotFound() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.update(99L, requestEjemplo()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete elimina cuando el producto existe")
    void delete_eliminaProducto() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEjemplo()));

        productoService.delete(1L);

        verify(productoRepository).delete(any(Producto.class));
    }

    @Test
    @DisplayName("delete lanza ResourceNotFound cuando el producto no existe")
    void delete_lanzaNotFound() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(productoRepository, never()).delete(any());
    }
}
