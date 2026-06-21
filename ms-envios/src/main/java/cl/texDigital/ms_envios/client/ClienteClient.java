package cl.texDigital.ms_envios.client;

import cl.texDigital.ms_envios.exception.ResourceNotFoundException;
import cl.texDigital.ms_envios.exception.ServicioRemotoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Component
@Slf4j
public class ClienteClient {

    private final RestClient restClient;

    public ClienteClient(@Value("${ms-clientes.url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(3));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    public ClienteResponse findById(Long id) {
        try {
            return restClient.get()
                    .uri("/api/clientes/{id}", id)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw new ResourceNotFoundException(
                                "Cliente no encontrado con id: " + id + " en ms-clientes");
                    })
                    .body(ClienteResponse.class);
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (ResourceAccessException ex) {
            log.error("Timeout o conexion fallida con ms-clientes para id={}: {}", id, ex.getMessage());
            throw new ServicioRemotoException("No se pudo conectar con ms-clientes. Intente mas tarde.");
        } catch (Exception ex) {
            log.error("Error consultando ms-clientes para id={}: {}", id, ex.getMessage());
            throw new ServicioRemotoException("Error al consultar ms-clientes: " + ex.getMessage());
        }
    }
}
