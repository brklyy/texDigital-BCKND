package cl.texDigital.ms_inventario.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RolloRequestDTO {

    @NotNull(message = "El textil es obligatorio")
    private Long textilId;

    @NotNull(message = "Los metros totales son obligatorios")
    @Positive(message = "Los metros deben ser mayores a 0")
    private Double metrosTotales;

    @NotNull(message = "La fecha de ingreso es obligatoria")
    private LocalDate fechaIngreso;
}
