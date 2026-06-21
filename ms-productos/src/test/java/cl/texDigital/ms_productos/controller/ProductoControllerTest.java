package cl.texDigital.ms_productos.controller;

import cl.texDigital.ms_productos.dto.ProductoResponseDTO;
import cl.texDigital.ms_productos.exception.ResourceNotFoundException;
import cl.texDigital.ms_productos.service.ProductoService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    private static final String BODY_VALIDO =
            "{\"nombre\":\"Banner\",\"tipo\":\"ESTAMPADO\",\"textilRequerido\":\"Algodon\",\"precioBase\":15000.0}";

    private ProductoResponseDTO productoEjemplo() {
        return new ProductoResponseDTO(1L, "Banner", "ESTAMPADO", "Algodon", 15000.0);
    }

    @Test
    @DisplayName("GET /api/productos devuelve 200")
    void getAll_devuelve200() throws Exception {
        when(productoService.findAll()).thenReturn(List.of(productoEjemplo()));

        mockMvc.perform(get("/api/productos")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/productos/{id} devuelve 200 con enlaces HATEOAS")
    void getById_devuelve200ConLinks() throws Exception {
        when(productoService.findById(1L)).thenReturn(productoEjemplo());

        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    @DisplayName("GET /api/productos/{id} devuelve 404 cuando no existe")
    void getById_devuelve404() throws Exception {
        when(productoService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Producto con id 99 no encontrado"));

        mockMvc.perform(get("/api/productos/99")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/productos/tipo/{tipo} devuelve 200 con enlaces HATEOAS")
    void getByTipo_devuelve200() throws Exception {
        when(productoService.findByTipo("ESTAMPADO")).thenReturn(List.of(productoEjemplo()));

        mockMvc.perform(get("/api/productos/tipo/ESTAMPADO"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/productos con body valido devuelve 201")
    void create_devuelve201() throws Exception {
        when(productoService.save(any())).thenReturn(productoEjemplo());

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/productos con body invalido devuelve 400")
    void create_devuelve400_siBodyInvalido() throws Exception {
        String body = "{\"tipo\":\"ESTAMPADO\",\"precioBase\":-5}";
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/productos devuelve 400 cuando el tipo es invalido")
    void create_devuelve400_siTipoInvalido() throws Exception {
        when(productoService.save(any()))
                .thenThrow(new IllegalArgumentException("Tipo de producto invalido: TAZA"));

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/productos/{id} devuelve 200")
    void update_devuelve200() throws Exception {
        when(productoService.update(eq(1L), any())).thenReturn(productoEjemplo());

        mockMvc.perform(put("/api/productos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/productos/{id} devuelve 404 cuando no existe")
    void update_devuelve404() throws Exception {
        when(productoService.update(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Producto con id 99 no encontrado"));

        mockMvc.perform(put("/api/productos/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/productos/{id} devuelve 204")
    void delete_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/productos/1")).andExpect(status().isNoContent());
    }
}
