package cl.texDigital.ms_pedidos.client;

import cl.texDigital.ms_pedidos.exception.ResourceNotFoundException;
import cl.texDigital.ms_pedidos.exception.ServicioRemotoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Cliente REST hacia ms-productos.
 * Configura timeouts y traduce los errores remotos a excepciones propias
 * para que el GlobalExceptionHandler responda con el codigo HTTP adecuado.
 */
@Component
@Slf4j
public class ProductoClient {

    private final RestClient restClient;

    public ProductoClient(@Value("${ms-productos.url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(3));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    public ProductoResponse findById(Long id) {
        try {
            return restClient.get()
                    .uri("/api/productos/{id}", id)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw new ResourceNotFoundException(
                                "Producto no encontrado con id: " + id + " en ms-productos");
                    })
                    .body(ProductoResponse.class);
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (ResourceAccessException ex) {
            log.error("Timeout o conexion fallida con ms-productos para id={}: {}", id, ex.getMessage());
            throw new ServicioRemotoException("No se pudo conectar con ms-productos. Intente mas tarde.");
        } catch (Exception ex) {
            log.error("Error consultando ms-productos para id={}: {}", id, ex.getMessage());
            throw new ServicioRemotoException("Error al consultar ms-productos: " + ex.getMessage());
        }
    }
}
