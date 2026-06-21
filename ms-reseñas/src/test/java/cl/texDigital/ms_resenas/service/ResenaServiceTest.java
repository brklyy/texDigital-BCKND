package cl.texDigital.ms_resenas.service;

import cl.texDigital.ms_resenas.dto.DetallePedidoClienteDTO;
import cl.texDigital.ms_resenas.dto.PedidoClienteDTO;
import cl.texDigital.ms_resenas.dto.ResenaRequestDTO;
import cl.texDigital.ms_resenas.dto.ResenaResponseDTO;
import cl.texDigital.ms_resenas.exception.ResourceNotFoundException;
import cl.texDigital.ms_resenas.model.Resena;
import cl.texDigital.ms_resenas.repository.ResenaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResenaServiceTest {

    @Mock
    private ResenaRepository resenaRepository;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WebClient webClientPedidos;

    @InjectMocks
    private ResenaService resenaService;

    private Resena resena;

    @BeforeEach
    void setUp() {
        resena = new Resena(1L, 1L, 1L, 8, "Muy buena tela", LocalDateTime.now());
    }

    @Test
    void findAll_debeRetornarListaDeResenas() {
        when(resenaRepository.findAll()).thenReturn(List.of(resena));

        List<ResenaResponseDTO> result = resenaService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPuntaje()).isEqualTo(8);
        assertThat(result.get(0).getClienteId()).isEqualTo(1L);
    }

    @Test
    void findById_conIdExistente_debeRetornarResena() {
        when(resenaRepository.findById(1L)).thenReturn(Optional.of(resena));

        ResenaResponseDTO result = resenaService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPuntaje()).isEqualTo(8);
    }

    @Test
    void findById_conIdInexistente_debeLanzarResourceNotFoundException() {
        when(resenaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> resenaService.findById(99L));
    }

    @Test
    void getPromedioByProductoId_sinResenas_debeRetornarCero() {
        when(resenaRepository.findPromedioPuntajeByProductoId(1L)).thenReturn(null);

        Double resultado = resenaService.getPromedioByProductoId(1L);

        assertThat(resultado).isEqualTo(0.0);
    }

    @Test
    void getPromedioByProductoId_conResenas_debeRetornarPromedio() {
        when(resenaRepository.findPromedioPuntajeByProductoId(1L)).thenReturn(7.5);

        Double resultado = resenaService.getPromedioByProductoId(1L);

        assertThat(resultado).isEqualTo(7.5);
    }

    @Test
    void calcularEstrellas_puntaje10_debeRetornar5EstrellasCompletas() {
        resena.setPuntaje(10);
        when(resenaRepository.findById(1L)).thenReturn(Optional.of(resena));

        ResenaResponseDTO result = resenaService.findById(1L);

        assertThat(result.getEstrellas()).isEqualTo("★★★★★");
    }

    @Test
    void calcularEstrellas_puntaje7_debeRetornar3EstrellasYMedia() {
        resena.setPuntaje(7);
        when(resenaRepository.findById(1L)).thenReturn(Optional.of(resena));

        ResenaResponseDTO result = resenaService.findById(1L);

        assertThat(result.getEstrellas()).isEqualTo("★★★⯨");
    }

    @Test
    void save_clienteNoHaCompradoProducto_debeLanzarIllegalArgumentException() {
        ResenaRequestDTO requestDTO = new ResenaRequestDTO();
        requestDTO.setClienteId(1L);
        requestDTO.setProductoId(1L);
        requestDTO.setPuntaje(8);
        requestDTO.setComentario("Buena calidad");

        when(webClientPedidos.get()
                .uri(anyString(), any(Object.class))
                .retrieve()
                .bodyToFlux(PedidoClienteDTO.class)
                .collectList()
                .block())
                .thenReturn(List.of());

        assertThrows(IllegalArgumentException.class, () -> resenaService.save(requestDTO));
    }

    @Test
    void save_clienteHaCompradoProducto_debeCrearResena() {
        ResenaRequestDTO requestDTO = new ResenaRequestDTO();
        requestDTO.setClienteId(1L);
        requestDTO.setProductoId(1L);
        requestDTO.setPuntaje(8);
        requestDTO.setComentario("Buena calidad");

        DetallePedidoClienteDTO detalle = new DetallePedidoClienteDTO(1L);
        PedidoClienteDTO pedido = new PedidoClienteDTO(1L, 1L, List.of(detalle));

        when(webClientPedidos.get()
                .uri(anyString(), any(Object.class))
                .retrieve()
                .bodyToFlux(PedidoClienteDTO.class)
                .collectList()
                .block())
                .thenReturn(List.of(pedido));

        when(resenaRepository.save(any(Resena.class))).thenReturn(resena);

        ResenaResponseDTO result = resenaService.save(requestDTO);

        assertThat(result).isNotNull();
        verify(resenaRepository).save(any(Resena.class));
    }

    @Test
    void update_conIdExistente_debeActualizarResena() {
        ResenaRequestDTO requestDTO = new ResenaRequestDTO();
        requestDTO.setClienteId(1L);
        requestDTO.setProductoId(1L);
        requestDTO.setPuntaje(9);
        requestDTO.setComentario("Excelente calidad");

        when(resenaRepository.findById(1L)).thenReturn(Optional.of(resena));
        when(resenaRepository.save(any(Resena.class))).thenReturn(resena);

        ResenaResponseDTO result = resenaService.update(1L, requestDTO);

        assertThat(result).isNotNull();
        verify(resenaRepository).save(any(Resena.class));
    }

    @Test
    void delete_conIdExistente_debeEliminarResena() {
        when(resenaRepository.findById(1L)).thenReturn(Optional.of(resena));

        resenaService.delete(1L);

        verify(resenaRepository).delete(resena);
    }
}
