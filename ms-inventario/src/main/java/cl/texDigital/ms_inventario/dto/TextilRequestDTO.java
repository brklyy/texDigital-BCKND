package cl.texDigital.ms_inventario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TextilRequestDTO {

    @NotBlank(message = "El nombre del textil es obligatorio")
    private String nombre;

    @NotNull(message = "El ancho de tela es obligatorio")
    @Positive(message = "El ancho debe ser mayor a 0")
    private Double anchoCm;

    private String descripcion;
}
