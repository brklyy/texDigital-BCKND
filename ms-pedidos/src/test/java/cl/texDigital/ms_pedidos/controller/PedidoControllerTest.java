package cl.texDigital.ms_pedidos.controller;

import cl.texDigital.ms_pedidos.dto.PedidoResponseDTO;
import cl.texDigital.ms_pedidos.exception.ResourceNotFoundException;
import cl.texDigital.ms_pedidos.exception.ServicioRemotoException;
import cl.texDigital.ms_pedidos.service.PedidoService;
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

@WebMvcTest(PedidoController.class)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoService pedidoService;

    private static final String BODY_VALIDO =
            "{\"clienteId\":1,\"detalles\":[{\"productoId\":10,\"cantidad\":2}]}";

    private PedidoResponseDTO pedidoEjemplo() {
        return new PedidoResponseDTO(1L, 1L, LocalDate.now(), "PENDIENTE", 7000.0, List.of());
    }

    @Test
    @DisplayName("GET /api/pedidos/{id} devuelve 200 con enlaces HATEOAS")
    void getById_devuelve200ConLinks() throws Exception {
        when(pedidoService.findById(1L)).thenReturn(pedidoEjemplo());

        mockMvc.perform(get("/api/pedidos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    @DisplayName("GET /api/pedidos/{id} devuelve 404 cuando no existe")
    void getById_devuelve404() throws Exception {
        when(pedidoService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Pedido no encontrado con id: 99"));

        mockMvc.perform(get("/api/pedidos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/pedidos devuelve 200")
    void getAll_devuelve200() throws Exception {
        when(pedidoService.findAll()).thenReturn(List.of(pedidoEjemplo()));

        mockMvc.perform(get("/api/pedidos")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/pedidos/cliente/{clienteId} devuelve 200")
    void getByCliente_devuelve200() throws Exception {
        when(pedidoService.findByClienteId(1L)).thenReturn(List.of(pedidoEjemplo()));

        mockMvc.perform(get("/api/pedidos/cliente/1")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/pedidos/estado/{estado} devuelve 200")
    void getByEstado_devuelve200() throws Exception {
        when(pedidoService.findByEstado("PENDIENTE")).thenReturn(List.of(pedidoEjemplo()));

        mockMvc.perform(get("/api/pedidos/estado/PENDIENTE")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/pedidos con body valido devuelve 201")
    void create_devuelve201() throws Exception {
        when(pedidoService.create(any())).thenReturn(pedidoEjemplo());

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(7000.0));
    }

    @Test
    @DisplayName("POST /api/pedidos con body invalido devuelve 400")
    void create_devuelve400_siBodyInvalido() throws Exception {
        // falta clienteId y la lista de detalles esta vacia
        String body = "{\"detalles\":[]}";
        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/pedidos devuelve 400 cuando el cliente esta inactivo")
    void create_devuelve400_siClienteInactivo() throws Exception {
        when(pedidoService.create(any()))
                .thenThrow(new IllegalStateException("El cliente con id 1 no está activo."));

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/pedidos devuelve 503 cuando un servicio remoto no responde")
    void create_devuelve503_siServicioRemotoCaido() throws Exception {
        when(pedidoService.create(any()))
                .thenThrow(new ServicioRemotoException("No se pudo conectar con ms-clientes."));

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    @DisplayName("PUT /api/pedidos/{id} devuelve 200")
    void update_devuelve200() throws Exception {
        when(pedidoService.update(eq(1L), any())).thenReturn(pedidoEjemplo());

        mockMvc.perform(put("/api/pedidos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/pedidos/{id}/estado devuelve 200")
    void updateEstado_devuelve200() throws Exception {
        when(pedidoService.updateEstado(1L, "COMPLETADO")).thenReturn(pedidoEjemplo());

        mockMvc.perform(patch("/api/pedidos/1/estado").param("estado", "COMPLETADO"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/pedidos/{id} devuelve 204")
    void delete_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/pedidos/1")).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/pedidos/{id} devuelve 404 cuando no existe")
    void delete_devuelve404() throws Exception {
        doThrow(new ResourceNotFoundException("Pedido no encontrado con id: 99"))
                .when(pedidoService).delete(eq(99L));

        mockMvc.perform(delete("/api/pedidos/99")).andExpect(status().isNotFound());
    }
}
