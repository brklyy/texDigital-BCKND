package cl.texDigital.ms_produccion.controller;

import cl.texDigital.ms_produccion.dto.OrdenProduccionResponseDTO;
import cl.texDigital.ms_produccion.exception.ResourceNotFoundException;
import cl.texDigital.ms_produccion.service.OrdenProduccionService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrdenProduccionController.class)
class OrdenProduccionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrdenProduccionService ordenService;

    private static final String BODY_VALIDO =
            "{\"pedidoId\":1,\"productoId\":10,\"textilId\":2,\"rolloId\":5,\"metrosUsados\":20.0}";

    private OrdenProduccionResponseDTO ordenEjemplo() {
        return new OrdenProduccionResponseDTO(1L, 1L, 10L, 2L, 5L, 20.0, "PENDIENTE",
                LocalDateTime.of(2026, 6, 1, 10, 0), null);
    }

    @Test
    @DisplayName("GET /api/ordenes devuelve 200")
    void getAll_devuelve200() throws Exception {
        when(ordenService.findAll()).thenReturn(List.of(ordenEjemplo()));

        mockMvc.perform(get("/api/ordenes")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/ordenes/{id} devuelve 200 con enlaces HATEOAS")
    void getById_devuelve200ConLinks() throws Exception {
        when(ordenService.findById(1L)).thenReturn(ordenEjemplo());

        mockMvc.perform(get("/api/ordenes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    @DisplayName("GET /api/ordenes/{id} devuelve 404 cuando no existe")
    void getById_devuelve404() throws Exception {
        when(ordenService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Orden con id 99 no encontrada"));

        mockMvc.perform(get("/api/ordenes/99")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/ordenes/pedido/{pedidoId} devuelve 200")
    void getByPedidoId_devuelve200() throws Exception {
        when(ordenService.findByPedidoId(1L)).thenReturn(List.of(ordenEjemplo()));

        mockMvc.perform(get("/api/ordenes/pedido/1")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/ordenes/stats/metros devuelve 200 con el total")
    void getTotalMetros_devuelve200() throws Exception {
        when(ordenService.getTotalMetros()).thenReturn(150.0);

        mockMvc.perform(get("/api/ordenes/stats/metros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMetrosUsados").value(150.0));
    }

    @Test
    @DisplayName("POST /api/ordenes con body valido devuelve 201")
    void create_devuelve201() throws Exception {
        when(ordenService.save(any())).thenReturn(ordenEjemplo());

        mockMvc.perform(post("/api/ordenes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/ordenes con body invalido devuelve 400")
    void create_devuelve400_siBodyInvalido() throws Exception {
        String body = "{\"pedidoId\":1}";
        mockMvc.perform(post("/api/ordenes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/ordenes/{id} devuelve 200")
    void update_devuelve200() throws Exception {
        when(ordenService.update(eq(1L), any())).thenReturn(ordenEjemplo());

        mockMvc.perform(put("/api/ordenes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/ordenes/{id} devuelve 404 cuando no existe")
    void update_devuelve404() throws Exception {
        when(ordenService.update(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Orden con id 99 no encontrada"));

        mockMvc.perform(put("/api/ordenes/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/ordenes/{id}/estado devuelve 200")
    void updateEstado_devuelve200() throws Exception {
        when(ordenService.updateEstado(eq(1L), anyString())).thenReturn(ordenEjemplo());

        mockMvc.perform(patch("/api/ordenes/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"COMPLETADO\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/ordenes/{id}/estado devuelve 400 cuando el estado es invalido")
    void updateEstado_devuelve400_siEstadoInvalido() throws Exception {
        when(ordenService.updateEstado(eq(1L), anyString()))
                .thenThrow(new IllegalArgumentException("Estado invalido: CANCELADO"));

        mockMvc.perform(patch("/api/ordenes/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"CANCELADO\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/ordenes/{id} devuelve 204")
    void delete_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/ordenes/1")).andExpect(status().isNoContent());
    }
}
