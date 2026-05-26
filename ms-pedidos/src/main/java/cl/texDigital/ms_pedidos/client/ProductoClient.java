package cl.texDigital.ms_pedidos.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class ProductoClient {

    private final RestClient restClient;

    public ProductoClient(@Value("${ms-productos.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public ProductoResponse findById(Long id) {
        try {
            return restClient.get()
                    .uri("/api/productos/{id}", id)
                    .retrieve()
                    .body(ProductoResponse.class);
        } catch (Exception ex) {
            log.error("Error al consultar ms-productos para id={}: {}", id, ex.getMessage());
            throw new IllegalStateException("No se pudo consultar el producto con id: " + id + ". Verifique que ms-productos esté activo.");
        }
    }
}
