package cl.texDigital.ms_pagos.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PagoRequestDTO {

    @NotNull(message = "El pedidoId es obligatorio")
    @Positive(message = "El pedidoId debe ser positivo")
    private Long pedidoId;

    @NotBlank(message = "El metodo de pago es obligatorio")
    @Size(max = 20)
    private String metodoPago;

    @NotNull(message = "El porcentaje de descuento es obligatorio (use 0 si no aplica)")
    @Min(value = 0, message = "El descuento minimo es 0")
    @Max(value = 100, message = "El descuento maximo es 100")
    private Integer porcentajeDescuento;
}
