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

    // Referencia al pedido en ms-pedidos (no es una FK fisica, los MS no comparten BD)
    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;

    // Valores posibles: EFECTIVO, TARJETA, TRANSFERENCIA
    @Column(name = "metodo_pago", nullable = false, length = 20)
    private String metodoPago;

    // Monto neto del pedido (sin IVA) traido desde ms-pedidos
    @Column(name = "monto_base", nullable = false)
    private Double montoBase;

    // Porcentaje de descuento aplicado (0 a 100)
    @Column(name = "porcentaje_descuento", nullable = false)
    private Integer porcentajeDescuento;

    // Monto descontado = montoBase * (porcentajeDescuento / 100)
    @Column(name = "monto_descuento", nullable = false)
    private Double montoDescuento;

    // Monto neto luego del descuento = montoBase - montoDescuento
    @Column(name = "monto_neto", nullable = false)
    private Double montoNeto;

    // IVA = montoNeto * 0.19
    @Column(nullable = false)
    private Double iva;

    // Total a pagar = montoNeto + iva
    @Column(name = "monto_total", nullable = false)
    private Double montoTotal;

    // Valores posibles: PENDIENTE, PAGADO, RECHAZADO, REEMBOLSADO
    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDate fechaPago;
}
