package cl.texDigital.ms_pagos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoResponseDTO {

    private Long id;
    private Long pedidoId;
    private String metodoPago;
    private Double montoBase;
    private Integer porcentajeDescuento;
    private Double montoDescuento;
    private Double montoNeto;
    private Double iva;
    private Double montoTotal;
    private String estado;
    private LocalDate fechaPago;
}
