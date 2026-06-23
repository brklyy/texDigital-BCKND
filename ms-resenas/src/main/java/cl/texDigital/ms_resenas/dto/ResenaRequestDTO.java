package cl.texDigital.ms_resenas.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResenaRequestDTO {

    @NotNull(message = "El clienteId es obligatorio")
    private Long clienteId;

    @NotNull(message = "El productoId es obligatorio")
    private Long productoId;

    @NotNull(message = "El puntaje es obligatorio")
    @Min(value = 1, message = "El puntaje minimo es 1")
    @Max(value = 10, message = "El puntaje maximo es 10")
    private Integer puntaje;

    @NotBlank(message = "El comentario es obligatorio")
    @Size(max = 500, message = "El comentario no puede superar los 500 caracteres")
    private String comentario;
}
