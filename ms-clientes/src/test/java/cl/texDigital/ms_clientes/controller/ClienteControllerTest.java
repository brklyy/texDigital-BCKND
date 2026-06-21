package cl.texDigital.ms_clientes.controller;

import cl.texDigital.ms_clientes.dto.ClienteResponseDTO;
import cl.texDigital.ms_clientes.exception.ResourceNotFoundException;
import cl.texDigital.ms_clientes.service.ClienteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService clienteService;

    private static final String BODY_VALIDO =
            "{\"nombre\":\"Carlos\",\"apellido\":\"Mendoza\",\"email\":\"carlos@texdigital.cl\",\"telefono\":\"+56912345678\",\"direccion\":\"Av. Principal 123\"}";

    private ClienteResponseDTO clienteEjemplo() {
        return new ClienteResponseDTO(1L, "Carlos", "Mendoza", "carlos@texdigital.cl", "+56912345678", "Av. Principal 123", "ACTIVO");
    }

    @Test
    @DisplayName("GET /api/clientes devuelve 200 con coleccion HATEOAS")
    void getAll_devuelve200() throws Exception {
        when(clienteService.findAll()).thenReturn(List.of(clienteEjemplo()));

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    @DisplayName("GET /api/clientes/{id} devuelve 200 con enlaces HATEOAS")
    void getById_devuelve200() throws Exception {
        when(clienteService.findById(1L)).thenReturn(clienteEjemplo());

        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Carlos"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.clientes.href").exists());
    }

    @Test
    @DisplayName("GET /api/clientes/{id} devuelve 404 cuando no existe")
    void getById_devuelve404() throws Exception {
        when(clienteService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Cliente no encontrado con id: 99"));

        mockMvc.perform(get("/api/clientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/clientes con body valido devuelve 201")
    void create_devuelve201() throws Exception {
        when(clienteService.create(any())).thenReturn(clienteEjemplo());

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    @Test
    @DisplayName("POST /api/clientes con email invalido devuelve 400")
    void create_devuelve400_siEmailInvalido() throws Exception {
        String body = "{\"nombre\":\"Carlos\",\"apellido\":\"Mendoza\",\"email\":\"no-es-email\",\"telefono\":\"+56912345678\"}";

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/clientes devuelve 400 cuando el email ya existe")
    void create_devuelve400_siEmailDuplicado() throws Exception {
        when(clienteService.create(any()))
                .thenThrow(new IllegalStateException("El email ya está registrado"));

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/clientes/{id} devuelve 200")
    void update_devuelve200() throws Exception {
        when(clienteService.update(eq(1L), any())).thenReturn(clienteEjemplo());

        mockMvc.perform(put("/api/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PUT /api/clientes/{id} devuelve 404 cuando no existe")
    void update_devuelve404() throws Exception {
        when(clienteService.update(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Cliente no encontrado con id: 99"));

        mockMvc.perform(put("/api/clientes/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/clientes/{id} devuelve 204")
    void delete_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/clientes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/clientes/{id} devuelve 404 cuando no existe")
    void delete_devuelve404() throws Exception {
        doThrow(new ResourceNotFoundException("Cliente no encontrado con id: 99"))
                .when(clienteService).delete(eq(99L));

        mockMvc.perform(delete("/api/clientes/99"))
                .andExpect(status().isNotFound());
    }
}
