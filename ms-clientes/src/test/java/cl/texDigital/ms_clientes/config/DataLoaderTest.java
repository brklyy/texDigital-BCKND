package cl.texDigital.ms_clientes.config;

import cl.texDigital.ms_clientes.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataLoaderTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private DataLoader dataLoader;

    @Test
    void run_omiteCargaSiYaExistenDatos() throws Exception {
        when(clienteRepository.count()).thenReturn(3L);
        dataLoader.run();
        verify(clienteRepository, never()).saveAll(anyList());
    }

    @Test
    void run_insertaClientesSiNoHayDatos() throws Exception {
        when(clienteRepository.count()).thenReturn(0L);
        dataLoader.run();
        verify(clienteRepository).saveAll(anyList());
    }
}
