package cl.texDigital.ms_resenas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResenaResponseDTO {

    private Long id;
    private Long clienteId;
    private Long productoId;
    private Integer puntaje;
    private String estrellas;
    private String comentario;
    private LocalDateTime fecha;
}
