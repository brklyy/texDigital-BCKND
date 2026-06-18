package cl.texDigital.ms_productos.config;

import cl.texDigital.ms_productos.model.Producto;
import cl.texDigital.ms_productos.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final ProductoRepository productoRepository;

    @Override
    public void run(String... args) {
        if (productoRepository.count() > 0) {
            log.info("DataLoader: productos ya existen, omitiendo carga inicial");
            return;
        }

        List<Producto> productos = List.of(
                new Producto(null, "Estampado Corporativo A4", "ESTAMPADO", "Lienzo", 8500.0),
                new Producto(null, "Lienzo Decorativo 50x70", "LIENZO", "Lienzo", 12000.0),
                new Producto(null, "Bandera Institucional 60x90", "BANDERA", "Lienzo", 15000.0),
                new Producto(null, "Mantel Bordado 180x140", "MANTEL", "Lienzo", 22000.0),
                new Producto(null, "Backlight Publicitario 100x200", "BACKLIGHT", "Pearl", 45000.0),
                new Producto(null, "Caja Backlight Retroiluminada", "CAJA_BACKLIGHT", "Pearl", 68000.0),
                new Producto(null, "Funda Cojin Personalizada", "FUNDA_COJIN", "Lienzo", 9500.0)
        );

        productoRepository.saveAll(productos);
        log.info("DataLoader: {} productos cargados correctamente", productos.size());
    }
}
