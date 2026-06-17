package cl.texDigital.ms_pagos.controller;

import cl.texDigital.ms_pagos.dto.PagoResponseDTO;
import cl.texDigital.ms_pagos.exception.ResourceNotFoundException;
import cl.texDigital.ms_pagos.exception.ServicioRemotoException;
import cl.texDigital.ms_pagos.service.PagoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de la capa web de PagoController usando @WebMvcTest (sin levantar BD).
 * El servicio se reemplaza por un mock; se valida el ruteo, los codigos HTTP
 * y la traduccion de excepciones del GlobalExceptionHandler.
 */
@WebMvcTest(PagoController.class)
class PagoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PagoService pagoService;

    private static final String BODY_VALIDO =
            "{\"pedidoId\":1,\"metodoPago\":\"TARJETA\",\"porcentajeDescuento\":0}";

    private PagoResponseDTO pagoEjemplo() {
        return new PagoResponseDTO(1L, 1L, "TARJETA", 10000.0, 0, 0.0, 10000.0, 1900.0, 11900.0,
                "PAGADO", LocalDate.now());
    }

    @Test
    @DisplayName("GET /api/pagos/{id} devuelve 200 con el pago y enlaces HATEOAS")
    void getById_devuelve200ConLinks() throws Exception {
        when(pagoService.findById(1L)).thenReturn(pagoEjemplo());

        mockMvc.perform(get("/api/pagos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.iva").value(1900.0))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    @DisplayName("GET /api/pagos/{id} devuelve 404 cuando el pago no existe")
    void getById_devuelve404() throws Exception {
        when(pagoService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Pago no encontrado con id: 99"));

        mockMvc.perform(get("/api/pagos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/pagos devuelve 200")
    void getAll_devuelve200() throws Exception {
        when(pagoService.findAll()).thenReturn(List.of(pagoEjemplo()));

        mockMvc.perform(get("/api/pagos")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/pagos/pedido/{pedidoId} devuelve 200")
    void getByPedido_devuelve200() throws Exception {
        when(pagoService.findByPedidoId(1L)).thenReturn(List.of(pagoEjemplo()));

        mockMvc.perform(get("/api/pagos/pedido/1")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/pagos/estado/{estado} devuelve 200")
    void getByEstado_devuelve200() throws Exception {
        when(pagoService.findByEstado("PAGADO")).thenReturn(List.of(pagoEjemplo()));

        mockMvc.perform(get("/api/pagos/estado/PAGADO")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/pagos con body valido devuelve 201")
    void create_devuelve201() throws Exception {
        when(pagoService.create(any())).thenReturn(pagoEjemplo());

        mockMvc.perform(post("/api/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.montoTotal").value(11900.0));
    }

    @Test
    @DisplayName("POST /api/pagos con body invalido devuelve 400")
    void create_devuelve400_siBodyInvalido() throws Exception {
        // Falta metodoPago, pedidoId negativo y descuento > 100 -> fallan las validaciones
        String body = "{\"pedidoId\":-1,\"porcentajeDescuento\":150}";
        mockMvc.perform(post("/api/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/pagos devuelve 400 cuando el pedido ya esta pagado")
    void create_devuelve400_siReglaDeNegocio() throws Exception {
        when(pagoService.create(any()))
                .thenThrow(new IllegalStateException("El pedido 1 ya tiene un pago registrado como PAGADO."));

        mockMvc.perform(post("/api/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/pagos devuelve 503 cuando ms-pedidos no responde")
    void create_devuelve503_siServicioRemotoCaido() throws Exception {
        when(pagoService.create(any()))
                .thenThrow(new ServicioRemotoException("No se pudo conectar con ms-pedidos."));

        mockMvc.perform(post("/api/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    @DisplayName("PUT /api/pagos/{id} devuelve 200")
    void update_devuelve200() throws Exception {
        when(pagoService.update(eq(1L), any())).thenReturn(pagoEjemplo());

        mockMvc.perform(put("/api/pagos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/pagos/{id}/estado devuelve 200")
    void updateEstado_devuelve200() throws Exception {
        when(pagoService.updateEstado(1L, "REEMBOLSADO")).thenReturn(pagoEjemplo());

        mockMvc.perform(patch("/api/pagos/1/estado").param("estado", "REEMBOLSADO"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/pagos/{id}/estado devuelve 400 cuando el estado es invalido")
    void updateEstado_devuelve400_siEstadoInvalido() throws Exception {
        when(pagoService.updateEstado(eq(1L), anyString()))
                .thenThrow(new IllegalArgumentException("Estado invalido: REGALADO"));

        mockMvc.perform(patch("/api/pagos/1/estado").param("estado", "REGALADO"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/pagos/{id} devuelve 204")
    void delete_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/pagos/1")).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/pagos/{id} devuelve 404 cuando no existe")
    void delete_devuelve404() throws Exception {
        doThrow(new ResourceNotFoundException("Pago no encontrado con id: 99"))
                .when(pagoService).delete(eq(99L));

        mockMvc.perform(delete("/api/pagos/99")).andExpect(status().isNotFound());
    }
}
