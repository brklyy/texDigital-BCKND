package cl.texDigital.ms_envios.controller;

import cl.texDigital.ms_envios.dto.EnvioRequestDTO;
import cl.texDigital.ms_envios.dto.EnvioResponseDTO;
import cl.texDigital.ms_envios.exception.GlobalExceptionHandler;
import cl.texDigital.ms_envios.exception.ResourceNotFoundException;
import cl.texDigital.ms_envios.model.EstadoEnvio;
import cl.texDigital.ms_envios.service.EnvioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnvioController.class)
@Import(GlobalExceptionHandler.class)
class EnvioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnvioService envioService;

    private ObjectMapper objectMapper;
    private EnvioResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        responseDTO = new EnvioResponseDTO(1L, 10L, "Av. Principal 123",
                "Chilexpress", "ENV-ABC12345", "PENDIENTE",
                LocalDate.now(), LocalDate.now().plusDays(3), null);
    }

    @Test
    void listar_retornaListaConHateoas() throws Exception {
        when(envioService.listarTodos()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/envios").accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.envioResponseDTOList[0].id").value(1))
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void obtenerPorId_existente_retornaDTO() throws Exception {
        when(envioService.obtenerPorId(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/envios/1").accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoSeguimiento").value("ENV-ABC12345"))
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void obtenerPorId_noExistente_retorna404() throws Exception {
        when(envioService.obtenerPorId(99L)).thenThrow(new ResourceNotFoundException("Envio no encontrado con id: 99"));

        mockMvc.perform(get("/api/envios/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorPedido_retornaLista() throws Exception {
        when(envioService.obtenerPorPedido(10L)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/envios/pedido/10").accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.envioResponseDTOList[0].pedidoId").value(10));
    }

    @Test
    void obtenerPorEstado_retornaLista() throws Exception {
        when(envioService.obtenerPorEstado(EstadoEnvio.PENDIENTE)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/envios/estado/PENDIENTE").accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void crear_datosValidos_retorna201() throws Exception {
        EnvioRequestDTO requestDTO = new EnvioRequestDTO(10L, "Av. Principal 123",
                "Chilexpress", LocalDate.now().plusDays(3));
        when(envioService.crear(any(EnvioRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/envios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void crear_datosInvalidos_retorna400() throws Exception {
        EnvioRequestDTO invalido = new EnvioRequestDTO(null, "", "", null);

        mockMvc.perform(post("/api/envios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizar_existente_retornaDTO() throws Exception {
        EnvioRequestDTO requestDTO = new EnvioRequestDTO(10L, "Nueva Dir",
                "DHL", LocalDate.now().plusDays(5));
        when(envioService.actualizar(anyLong(), any(EnvioRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/envios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void cambiarEstado_valido_retornaDTO() throws Exception {
        EnvioResponseDTO enCamino = new EnvioResponseDTO(1L, 10L, "Av. Principal 123",
                "Chilexpress", "ENV-ABC12345", "EN_CAMINO",
                LocalDate.now(), LocalDate.now().plusDays(3), null);
        when(envioService.cambiarEstado(1L, EstadoEnvio.EN_CAMINO)).thenReturn(enCamino);

        mockMvc.perform(patch("/api/envios/1/estado")
                        .param("estado", "EN_CAMINO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_CAMINO"));
    }

    @Test
    void eliminar_existente_retorna204() throws Exception {
        mockMvc.perform(delete("/api/envios/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminar_noExistente_retorna404() throws Exception {
        doThrow(new ResourceNotFoundException("Envio no encontrado con id: 99"))
                .when(envioService).eliminar(99L);

        mockMvc.perform(delete("/api/envios/99"))
                .andExpect(status().isNotFound());
    }
}
