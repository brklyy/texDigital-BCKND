package cl.texDigital.ms_produccion.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrdenProduccionRequestDTO {

    @NotNull(message = "El pedidoId es obligatorio")
    private Long pedidoId;

    @NotNull(message = "El productoId es obligatorio")
    private Long productoId;

    @NotNull(message = "El textilId es obligatorio")
    private Long textilId;

    @NotNull(message = "El rolloId es obligatorio")
    private Long rolloId;

    @NotNull(message = "Los metros usados son obligatorios")
    @Positive(message = "Los metros usados deben ser mayor a 0")
    private Double metrosUsados;
}
