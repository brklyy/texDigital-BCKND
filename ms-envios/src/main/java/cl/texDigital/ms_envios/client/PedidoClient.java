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
public class PedidoClient {

    private final RestClient restClient;

    public PedidoClient(@Value("${ms-pedidos.url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(3));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    public PedidoResponse findById(Long id) {
        try {
            return restClient.get()
                    .uri("/api/pedidos/{id}", id)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw new ResourceNotFoundException(
                                "Pedido no encontrado con id: " + id + " en ms-pedidos");
                    })
                    .body(PedidoResponse.class);
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (ResourceAccessException ex) {
            log.error("Timeout o conexion fallida con ms-pedidos para id={}: {}", id, ex.getMessage());
            throw new ServicioRemotoException("No se pudo conectar con ms-pedidos. Intente mas tarde.");
        } catch (Exception ex) {
            log.error("Error consultando ms-pedidos para id={}: {}", id, ex.getMessage());
            throw new ServicioRemotoException("Error al consultar ms-pedidos: " + ex.getMessage());
        }
    }

    public void actualizarEstado(Long id, String estado) {
        try {
            restClient.patch()
                    .uri("/api/pedidos/{id}/estado?estado={estado}", id, estado)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw new ResourceNotFoundException(
                                "Pedido no encontrado con id: " + id + " en ms-pedidos");
                    })
                    .toBodilessEntity();
            log.debug("Estado del pedido id={} actualizado a {} en ms-pedidos", id, estado);
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (ResourceAccessException ex) {
            log.error("Timeout o conexion fallida con ms-pedidos al actualizar estado id={}: {}", id, ex.getMessage());
            throw new ServicioRemotoException("No se pudo conectar con ms-pedidos. Intente mas tarde.");
        } catch (Exception ex) {
            log.error("Error actualizando estado en ms-pedidos para id={}: {}", id, ex.getMessage());
            throw new ServicioRemotoException("Error al actualizar estado en ms-pedidos: " + ex.getMessage());
        }
    }
}
