package cl.texDigital.ms_pagos.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representacion parcial del pedido que devuelve ms-pedidos.
 * Solo mapeamos los campos que ms-pagos necesita; el resto se ignora.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PedidoResponse {

    private Long id;
    private String estado;
    private Double total;
}
