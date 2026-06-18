package cl.texDigital.ms_pagos.config;

import cl.texDigital.ms_pagos.model.Pago;
import cl.texDigital.ms_pagos.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final PagoRepository pagoRepository;

    @Override
    public void run(String... args) {
        if (pagoRepository.count() > 0) {
            log.info("DataLoader: pagos ya existen, omitiendo carga inicial");
            return;
        }

        List<Pago> pagos = List.of(
                new Pago(null, 1L, "TARJETA",
                        50000.0, 10, 5000.0, 45000.0, 8550.0, 53550.0,
                        "PAGADO", LocalDate.of(2026, 5, 10)),
                new Pago(null, 2L, "TRANSFERENCIA",
                        80000.0, 0, 0.0, 80000.0, 15200.0, 95200.0,
                        "PAGADO", LocalDate.of(2026, 5, 18)),
                new Pago(null, 3L, "EFECTIVO",
                        120000.0, 15, 18000.0, 102000.0, 19380.0, 121380.0,
                        "PENDIENTE", LocalDate.of(2026, 6, 1))
        );

        pagoRepository.saveAll(pagos);
        log.info("DataLoader: {} pagos cargados correctamente", pagos.size());
    }
}
