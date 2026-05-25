package cl.texDigital.ms_inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolloResponseDTO {

    private Long id;
    private Long textilId;
    private String textilNombre;
    private Double metrosTotales;
    private Double metrosRestantes;
    private Double metrosUsados;
    private LocalDate fechaIngreso;
    private String estado;
}
