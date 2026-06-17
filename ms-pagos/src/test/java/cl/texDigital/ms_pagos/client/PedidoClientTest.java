package cl.texDigital.ms_pagos.client;

import cl.texDigital.ms_pagos.exception.ServicioRemotoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifica el manejo de errores remotos del PedidoClient.
 * Apunta a un puerto inexistente para forzar el fallo de conexion.
 */
class PedidoClientTest {

    @Test
    @DisplayName("findById lanza ServicioRemotoException cuando ms-pedidos no esta disponible")
    void findById_lanzaServicioRemoto_siNoHayConexion() {
        // Given: una URL hacia un puerto donde no hay nada escuchando
        PedidoClient client = new PedidoClient("http://localhost:9");

        // When / Then
        assertThatThrownBy(() -> client.findById(1L))
                .isInstanceOf(ServicioRemotoException.class)
                .hasMessageContaining("ms-pedidos");
    }
}
