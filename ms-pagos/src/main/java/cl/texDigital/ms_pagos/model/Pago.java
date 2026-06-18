package cl.texDigital.ms_pagos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "pagos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;

    @Column(name = "metodo_pago", nullable = false, length = 20)
    private String metodoPago;

    @Column(name = "monto_base", nullable = false)
    private Double montoBase;

    @Column(name = "porcentaje_descuento", nullable = false)
    private Integer porcentajeDescuento;

    @Column(name = "monto_descuento", nullable = false)
    private Double montoDescuento;

    @Column(name = "monto_neto", nullable = false)
    private Double montoNeto;

    @Column(nullable = false)
    private Double iva;

    @Column(name = "monto_total", nullable = false)
    private Double montoTotal;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDate fechaPago;
}
