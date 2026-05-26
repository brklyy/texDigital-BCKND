package cl.texDigital.ms_pedidos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PedidoRequestDTO {

    @NotNull
    private Long clienteId;

    @NotNull
    @NotEmpty
    @Valid
    private List<DetallePedidoRequestDTO> detalles;
}
