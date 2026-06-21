package cl.texDigital.ms_envios.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EnvioRequestDTO {

    @NotNull(message = "El id del pedido es obligatorio")
    @Positive(message = "El id del pedido debe ser un numero positivo")
    private Long pedidoId;

    @NotNull(message = "La fecha estimada de entrega es obligatoria")
    @Future(message = "La fecha estimada de entrega debe ser una fecha futura")
    private LocalDate fechaEstimadaEntrega;

    @NotBlank(message = "El transportista es obligatorio")
    private String transportista;

    @NotBlank(message = "El numero de seguimiento es obligatorio")
    private String numeroSeguimiento;

    @NotBlank(message = "La direccion de destino es obligatoria")
    private String direccionDestino;
}
