package cl.texDigital.ms_envios.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnvioRequestDTO {

    @NotNull(message = "El id del pedido es obligatorio")
    @Positive(message = "El id del pedido debe ser positivo")
    private Long pedidoId;

    @NotBlank(message = "La direccion de entrega es obligatoria")
    private String direccionEntrega;

    @NotBlank(message = "El transportista es obligatorio")
    private String transportista;

    @NotNull(message = "La fecha estimada de entrega es obligatoria")
    @Future(message = "La fecha estimada de entrega debe ser futura")
    private LocalDate fechaEstimadaEntrega;
}
