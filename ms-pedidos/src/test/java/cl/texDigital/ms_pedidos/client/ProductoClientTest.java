package cl.texDigital.ms_pedidos.client;

import cl.texDigital.ms_pedidos.exception.ServicioRemotoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductoClientTest {

    @Test
    @DisplayName("findById lanza ServicioRemotoException cuando ms-productos no esta disponible")
    void findById_lanzaServicioRemoto_siNoHayConexion() {
        ProductoClient client = new ProductoClient("http://localhost:9");

        assertThatThrownBy(() -> client.findById(1L))
                .isInstanceOf(ServicioRemotoException.class)
                .hasMessageContaining("ms-productos");
    }
}
