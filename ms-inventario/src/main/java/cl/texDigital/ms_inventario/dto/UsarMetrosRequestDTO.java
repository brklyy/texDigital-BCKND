package cl.texDigital.ms_inventario.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UsarMetrosRequestDTO {

    @NotNull(message = "La cantidad de metros es obligatoria")
    @Positive(message = "Los metros deben ser mayores a 0")
    private Double metros;
}
