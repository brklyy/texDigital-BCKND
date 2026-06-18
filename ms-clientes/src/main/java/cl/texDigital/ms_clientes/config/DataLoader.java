package cl.texDigital.ms_clientes.config;

import cl.texDigital.ms_clientes.model.Cliente;
import cl.texDigital.ms_clientes.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final ClienteRepository clienteRepository;

    @Override
    public void run(String... args) {
        if (clienteRepository.count() > 0) {
            log.info("DataLoader: clientes ya existen, omitiendo carga inicial");
            return;
        }

        List<Cliente> clientes = List.of(
                new Cliente(null, "Carlos", "Mendoza", "carlos.mendoza@texdigital.cl", "+56912345678", "Av. Providencia 1234, Santiago", "ACTIVO"),
                new Cliente(null, "Ana", "Torres", "ana.torres@texdigital.cl", "+56923456789", "Calle Larga 456, Valparaíso", "ACTIVO"),
                new Cliente(null, "Jorge", "Rojas", "jorge.rojas@texdigital.cl", "+56934567890", "Los Alamos 789, Concepción", "ACTIVO"),
                new Cliente(null, "Valentina", "Soto", "valentina.soto@texdigital.cl", "+56945678901", "Calle Real 321, La Serena", "ACTIVO"),
                new Cliente(null, "Miguel", "Fuentes", "miguel.fuentes@texdigital.cl", "+56956789012", "Av. del Mar 654, Iquique", "INACTIVO")
        );

        clienteRepository.saveAll(clientes);
        log.info("DataLoader: {} clientes cargados correctamente", clientes.size());
    }
}
