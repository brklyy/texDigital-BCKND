package cl.texDigital.ms_inventario.controller;

import cl.texDigital.ms_inventario.dto.RolloResponseDTO;
import cl.texDigital.ms_inventario.exception.MetrosInsuficientesException;
import cl.texDigital.ms_inventario.exception.ResourceNotFoundException;
import cl.texDigital.ms_inventario.service.RolloService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RolloController.class)
class RolloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RolloService rolloService;

    private static final String BODY_ROLLO =
            "{\"textilId\":1,\"metrosTotales\":100.0,\"fechaIngreso\":\"2026-05-01\"}";

    private RolloResponseDTO rolloEjemplo() {
        return new RolloResponseDTO(1L, 1L, "Pearl", 100.0, 100.0, 0.0, LocalDate.now(), "ACTIVO");
    }

    @Test
    @DisplayName("GET /api/rollos/{id} devuelve 200 con enlaces HATEOAS")
    void getById_devuelve200ConLinks() throws Exception {
        when(rolloService.findById(1L)).thenReturn(rolloEjemplo());

        mockMvc.perform(get("/api/rollos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    @DisplayName("GET /api/rollos/{id} devuelve 404 cuando no existe")
    void getById_devuelve404() throws Exception {
        when(rolloService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Rollo no encontrado con id: 99"));

        mockMvc.perform(get("/api/rollos/99")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/rollos devuelve 200")
    void getAll_devuelve200() throws Exception {
        when(rolloService.findAll()).thenReturn(List.of(rolloEjemplo()));

        mockMvc.perform(get("/api/rollos")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/rollos/textil/{textilId} devuelve 200")
    void getByTextil_devuelve200() throws Exception {
        when(rolloService.findByTextilId(1L)).thenReturn(List.of(rolloEjemplo()));

        mockMvc.perform(get("/api/rollos/textil/1")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/rollos/textil/{textilId} devuelve 404 cuando el textil no existe")
    void getByTextil_devuelve404() throws Exception {
        when(rolloService.findByTextilId(99L))
                .thenThrow(new ResourceNotFoundException("Textil no encontrado con id: 99"));

        mockMvc.perform(get("/api/rollos/textil/99")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/rollos con body valido devuelve 201")
    void create_devuelve201() throws Exception {
        when(rolloService.create(any())).thenReturn(rolloEjemplo());

        mockMvc.perform(post("/api/rollos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_ROLLO))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/rollos con body invalido devuelve 400")
    void create_devuelve400_siBodyInvalido() throws Exception {
        String body = "{\"metrosTotales\":-10}";
        mockMvc.perform(post("/api/rollos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/rollos/{id} devuelve 200")
    void update_devuelve200() throws Exception {
        when(rolloService.update(eq(1L), any())).thenReturn(rolloEjemplo());

        mockMvc.perform(put("/api/rollos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_ROLLO))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/rollos/{id}/usar devuelve 200 cuando hay stock")
    void usarMetros_devuelve200() throws Exception {
        when(rolloService.usarMetros(eq(1L), any())).thenReturn(rolloEjemplo());

        mockMvc.perform(put("/api/rollos/1/usar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"metros\":30.0}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/rollos/{id}/usar devuelve 409 cuando faltan metros")
    void usarMetros_devuelve409() throws Exception {
        when(rolloService.usarMetros(eq(1L), any()))
                .thenThrow(new MetrosInsuficientesException("Metros insuficientes."));

        mockMvc.perform(put("/api/rollos/1/usar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"metros\":150.0}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("DELETE /api/rollos/{id} devuelve 204")
    void delete_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/rollos/1")).andExpect(status().isNoContent());
    }
}
