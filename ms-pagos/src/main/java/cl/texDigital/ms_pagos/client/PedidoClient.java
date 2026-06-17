package cl.texDigital.ms_pagos.client;

import cl.texDigital.ms_pagos.exception.ResourceNotFoundException;
import cl.texDigital.ms_pagos.exception.ServicioRemotoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Cliente REST hacia ms-pedidos.
 * Configura timeouts y traduce los errores remotos a excepciones propias
 * para que el GlobalExceptionHandler responda con el codigo HTTP adecuado.
 */
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

    /**
     * Notifica a ms-pedidos el cambio de estado del pedido. Es best-effort:
     * si falla, se registra pero no se aborta el pago ya guardado.
     */
    public void actualizarEstado(Long id, String estado) {
        try {
            restClient.patch()
                    .uri(builder -> builder.path("/api/pedidos/{id}/estado")
                            .queryParam("estado", estado)
                            .build(id))
                    .retrieve()
                    .toBodilessEntity();
            log.debug("Estado del pedido {} actualizado a {} en ms-pedidos", id, estado);
        } catch (Exception ex) {
            log.warn("No se pudo actualizar el estado del pedido {} en ms-pedidos: {}", id, ex.getMessage());
        }
    }
}
