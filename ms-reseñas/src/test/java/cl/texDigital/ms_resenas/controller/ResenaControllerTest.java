package cl.texDigital.ms_resenas.controller;

import cl.texDigital.ms_resenas.dto.ResenaResponseDTO;
import cl.texDigital.ms_resenas.exception.ResourceNotFoundException;
import cl.texDigital.ms_resenas.service.ResenaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ResenaController.class)
class ResenaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResenaService resenaService;

    private static final String BODY_VALIDO =
            "{\"clienteId\":1,\"productoId\":1,\"puntaje\":8,\"comentario\":\"Muy buena tela\"}";

    private ResenaResponseDTO resenaEjemplo() {
        return new ResenaResponseDTO(1L, 1L, 1L, 8, "★★★★", "Muy buena tela", LocalDateTime.now());
    }

    @Test
    @DisplayName("GET /api/resenas devuelve 200 con coleccion HATEOAS")
    void getAll_devuelve200() throws Exception {
        when(resenaService.findAll()).thenReturn(List.of(resenaEjemplo()));

        mockMvc.perform(get("/api/resenas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    @DisplayName("GET /api/resenas/{id} devuelve 200 con enlaces HATEOAS")
    void getById_devuelve200ConLinks() throws Exception {
        when(resenaService.findById(1L)).thenReturn(resenaEjemplo());

        mockMvc.perform(get("/api/resenas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.puntaje").value(8))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.resenas.href").exists());
    }

    @Test
    @DisplayName("GET /api/resenas/{id} devuelve 404 cuando no existe")
    void getById_devuelve404() throws Exception {
        when(resenaService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Reseña con id 99 no encontrada"));

        mockMvc.perform(get("/api/resenas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/resenas/producto/{productoId} devuelve 200")
    void getByProductoId_devuelve200() throws Exception {
        when(resenaService.findByProductoId(1L)).thenReturn(List.of(resenaEjemplo()));

        mockMvc.perform(get("/api/resenas/producto/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    @DisplayName("GET /api/resenas/cliente/{clienteId} devuelve 200")
    void getByClienteId_devuelve200() throws Exception {
        when(resenaService.findByClienteId(1L)).thenReturn(List.of(resenaEjemplo()));

        mockMvc.perform(get("/api/resenas/cliente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    @DisplayName("GET /api/resenas/producto/{productoId}/promedio devuelve 200 con promedio")
    void getPromedio_devuelve200() throws Exception {
        when(resenaService.getPromedioByProductoId(1L)).thenReturn(7.5);

        mockMvc.perform(get("/api/resenas/producto/1/promedio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promedio").value(7.5));
    }

    @Test
    @DisplayName("POST /api/resenas con body valido devuelve 201")
    void create_devuelve201() throws Exception {
        when(resenaService.save(any())).thenReturn(resenaEjemplo());

        mockMvc.perform(post("/api/resenas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /api/resenas con puntaje invalido devuelve 400")
    void create_devuelve400_siBodyInvalido() throws Exception {
        String body = "{\"clienteId\":1,\"productoId\":1,\"puntaje\":15,\"comentario\":\"x\"}";

        mockMvc.perform(post("/api/resenas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/resenas devuelve 400 cuando el cliente no ha comprado el producto")
    void create_devuelve400_siClienteNoCompro() throws Exception {
        when(resenaService.save(any()))
                .thenThrow(new IllegalArgumentException("El cliente no ha comprado este producto"));

        mockMvc.perform(post("/api/resenas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("PUT /api/resenas/{id} devuelve 200")
    void update_devuelve200() throws Exception {
        when(resenaService.update(eq(1L), any())).thenReturn(resenaEjemplo());

        mockMvc.perform(put("/api/resenas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PUT /api/resenas/{id} devuelve 404 cuando no existe")
    void update_devuelve404() throws Exception {
        when(resenaService.update(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Reseña con id 99 no encontrada"));

        mockMvc.perform(put("/api/resenas/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/resenas/{id} devuelve 204")
    void delete_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/resenas/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/resenas/{id} devuelve 404 cuando no existe")
    void delete_devuelve404() throws Exception {
        doThrow(new ResourceNotFoundException("Reseña con id 99 no encontrada"))
                .when(resenaService).delete(eq(99L));

        mockMvc.perform(delete("/api/resenas/99"))
                .andExpect(status().isNotFound());
    }
}
