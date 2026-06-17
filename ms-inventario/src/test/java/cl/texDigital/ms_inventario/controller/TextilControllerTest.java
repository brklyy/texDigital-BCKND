package cl.texDigital.ms_inventario.controller;

import cl.texDigital.ms_inventario.dto.TextilResponseDTO;
import cl.texDigital.ms_inventario.exception.ResourceNotFoundException;
import cl.texDigital.ms_inventario.service.TextilService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TextilController.class)
class TextilControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TextilService textilService;

    private static final String BODY_VALIDO =
            "{\"nombre\":\"Pearl\",\"anchoCm\":150.0,\"descripcion\":\"Tela\"}";

    private TextilResponseDTO textilEjemplo() {
        return new TextilResponseDTO(1L, "Pearl", 150.0, "Tela para backlights");
    }

    @Test
    @DisplayName("GET /api/textiles/{id} devuelve 200 con enlaces HATEOAS")
    void getById_devuelve200ConLinks() throws Exception {
        when(textilService.findById(1L)).thenReturn(textilEjemplo());

        mockMvc.perform(get("/api/textiles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    @DisplayName("GET /api/textiles/{id} devuelve 404 cuando no existe")
    void getById_devuelve404() throws Exception {
        when(textilService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Textil no encontrado con id: 99"));

        mockMvc.perform(get("/api/textiles/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/textiles devuelve 200")
    void getAll_devuelve200() throws Exception {
        when(textilService.findAll()).thenReturn(List.of(textilEjemplo()));

        mockMvc.perform(get("/api/textiles")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/textiles con body valido devuelve 201")
    void create_devuelve201() throws Exception {
        when(textilService.create(any())).thenReturn(textilEjemplo());

        mockMvc.perform(post("/api/textiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Pearl"));
    }

    @Test
    @DisplayName("POST /api/textiles con body invalido devuelve 400")
    void create_devuelve400_siBodyInvalido() throws Exception {
        // nombre en blanco y ancho negativo -> fallan las validaciones
        String body = "{\"nombre\":\"\",\"anchoCm\":-5}";
        mockMvc.perform(post("/api/textiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/textiles/{id} devuelve 204")
    void delete_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/textiles/1")).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/textiles/{id} devuelve 400 cuando el textil tiene rollos")
    void delete_devuelve400_siTieneRollos() throws Exception {
        doThrow(new IllegalStateException("No se puede eliminar el textil porque tiene rollos registrados"))
                .when(textilService).delete(eq(1L));

        mockMvc.perform(delete("/api/textiles/1")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/textiles/{id} devuelve 404 cuando no existe")
    void delete_devuelve404() throws Exception {
        doThrow(new ResourceNotFoundException("Textil no encontrado con id: 99"))
                .when(textilService).delete(eq(99L));

        mockMvc.perform(delete("/api/textiles/99")).andExpect(status().isNotFound());
    }
}
