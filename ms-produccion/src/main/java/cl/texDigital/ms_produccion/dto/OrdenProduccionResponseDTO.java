package cl.texDigital.ms_produccion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdenProduccionResponseDTO {

    private Long id;
    private Long pedidoId;
    private Long productoId;
    private Long textilId;
    private Long rolloId;
    private Double metrosUsados;
    private String estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaFin;
}
