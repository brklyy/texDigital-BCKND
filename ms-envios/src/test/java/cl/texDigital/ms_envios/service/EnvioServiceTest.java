package cl.texDigital.ms_envios.service;

import cl.texDigital.ms_envios.client.PedidoClient;
import cl.texDigital.ms_envios.client.PedidoResponse;
import cl.texDigital.ms_envios.dto.EnvioRequestDTO;
import cl.texDigital.ms_envios.dto.EnvioResponseDTO;
import cl.texDigital.ms_envios.exception.EstadoInvalidoException;
import cl.texDigital.ms_envios.exception.ResourceNotFoundException;
import cl.texDigital.ms_envios.model.Envio;
import cl.texDigital.ms_envios.model.EstadoEnvio;
import cl.texDigital.ms_envios.repository.EnvioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnvioServiceTest {

    @Mock
    private EnvioRepository envioRepository;

    @Mock
    private PedidoClient pedidoClient;

    @InjectMocks
    private EnvioService envioService;

    private Envio envio;
    private EnvioRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        envio = new Envio(1L, 10L, "Av. Principal 123", "Chilexpress",
                "ENV-ABC12345", EstadoEnvio.PENDIENTE,
                LocalDate.now(), LocalDate.now().plusDays(3), null);

        requestDTO = new EnvioRequestDTO(10L, "Av. Principal 123",
                "Chilexpress", LocalDate.now().plusDays(3));
    }

    @Test
    void listarTodos_retornaLista() {
        when(envioRepository.findAll()).thenReturn(List.of(envio));

        List<EnvioResponseDTO> resultado = envioService.listarTodos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getCodigoSeguimiento()).isEqualTo("ENV-ABC12345");
    }

    @Test
    void obtenerPorId_existente_retornaDTO() {
        when(envioRepository.findById(1L)).thenReturn(Optional.of(envio));

        EnvioResponseDTO resultado = envioService.obtenerPorId(1L);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getEstado()).isEqualTo("PENDIENTE");
    }

    @Test
    void obtenerPorId_noExistente_lanzaException() {
        when(envioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> envioService.obtenerPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void obtenerPorPedido_retornaLista() {
        when(envioRepository.findByPedidoId(10L)).thenReturn(List.of(envio));

        List<EnvioResponseDTO> resultado = envioService.obtenerPorPedido(10L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getPedidoId()).isEqualTo(10L);
    }

    @Test
    void obtenerPorEstado_retornaLista() {
        when(envioRepository.findByEstado(EstadoEnvio.PENDIENTE)).thenReturn(List.of(envio));

        List<EnvioResponseDTO> resultado = envioService.obtenerPorEstado(EstadoEnvio.PENDIENTE);

        assertThat(resultado).hasSize(1);
    }

    @Test
    void crear_pedidoValido_creaEnvio() {
        when(pedidoClient.obtenerPedido(10L)).thenReturn(new PedidoResponse());
        when(envioRepository.findByCodigoSeguimiento(anyString())).thenReturn(Optional.empty());
        when(envioRepository.save(any(Envio.class))).thenReturn(envio);

        EnvioResponseDTO resultado = envioService.crear(requestDTO);

        assertThat(resultado).isNotNull();
        verify(pedidoClient).obtenerPedido(10L);
        verify(envioRepository).save(any(Envio.class));
    }

    @Test
    void actualizar_estadoTerminal_lanzaException() {
        envio.setEstado(EstadoEnvio.ENTREGADO);
        when(envioRepository.findById(1L)).thenReturn(Optional.of(envio));

        assertThatThrownBy(() -> envioService.actualizar(1L, requestDTO))
                .isInstanceOf(EstadoInvalidoException.class);
    }

    @Test
    void cambiarEstado_dePendienteAEnCamino_exito() {
        when(envioRepository.findById(1L)).thenReturn(Optional.of(envio));
        when(envioRepository.save(any(Envio.class))).thenAnswer(inv -> inv.getArgument(0));

        EnvioResponseDTO resultado = envioService.cambiarEstado(1L, EstadoEnvio.EN_CAMINO);

        assertThat(resultado.getEstado()).isEqualTo("EN_CAMINO");
    }

    @Test
    void cambiarEstado_aEntregado_setFechaEntregaReal() {
        when(envioRepository.findById(1L)).thenReturn(Optional.of(envio));
        when(envioRepository.save(any(Envio.class))).thenAnswer(inv -> inv.getArgument(0));

        EnvioResponseDTO resultado = envioService.cambiarEstado(1L, EstadoEnvio.ENTREGADO);

        assertThat(resultado.getEstado()).isEqualTo("ENTREGADO");
        assertThat(resultado.getFechaEntregaReal()).isNotNull();
    }

    @Test
    void cambiarEstado_desdeTerminal_lanzaException() {
        envio.setEstado(EstadoEnvio.CANCELADO);
        when(envioRepository.findById(1L)).thenReturn(Optional.of(envio));

        assertThatThrownBy(() -> envioService.cambiarEstado(1L, EstadoEnvio.EN_CAMINO))
                .isInstanceOf(EstadoInvalidoException.class);
    }

    @Test
    void cambiarEstado_aPendiente_lanzaException() {
        when(envioRepository.findById(1L)).thenReturn(Optional.of(envio));

        assertThatThrownBy(() -> envioService.cambiarEstado(1L, EstadoEnvio.PENDIENTE))
                .isInstanceOf(EstadoInvalidoException.class);
    }

    @Test
    void eliminar_existente_elimina() {
        when(envioRepository.existsById(1L)).thenReturn(true);

        envioService.eliminar(1L);

        verify(envioRepository).deleteById(1L);
    }

    @Test
    void eliminar_noExistente_lanzaException() {
        when(envioRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> envioService.eliminar(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
