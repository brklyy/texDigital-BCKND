package cl.texDigital.ms_pedidos.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class ClienteClient {

    private final RestClient restClient;

    public ClienteClient(@Value("${ms-clientes.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public ClienteResponse findById(Long id) {
        try {
            return restClient.get()
                    .uri("/api/clientes/{id}", id)
                    .retrieve()
                    .body(ClienteResponse.class);
        } catch (Exception ex) {
            log.error("Error al consultar ms-clientes para id={}: {}", id, ex.getMessage());
            throw new IllegalStateException("No se pudo consultar el cliente con id: " + id + ". Verifique que ms-clientes esté activo.");
        }
    }
}
