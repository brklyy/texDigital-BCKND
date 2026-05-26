package cl.texDigital.ms_pedidos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DetallePedidoRequestDTO {

    @NotNull
    private Long productoId;

    @NotNull
    @Min(1)
    private Integer cantidad;
}
