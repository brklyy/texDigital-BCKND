package cl.texDigital.ms_envios.client;

import cl.texDigital.ms_envios.exception.ResourceNotFoundException;
import cl.texDigital.ms_envios.exception.ServicioRemotoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class PedidoClient {

    private final RestClient restClient;

    public PedidoClient(@Value("${ms-pedidos.url}") String msPedidosUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(3000);

        this.restClient = RestClient.builder()
                .baseUrl(msPedidosUrl)
                .requestFactory(factory)
                .build();
    }

    public PedidoResponse obtenerPedido(Long pedidoId) {
        try {
            return restClient.get()
                    .uri("/api/pedidos/{id}", pedidoId)
                    .retrieve()
                    .body(PedidoResponse.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Pedido no encontrado con id: " + pedidoId);
        } catch (ResourceAccessException e) {
            throw new ServicioRemotoException("No se pudo conectar con ms-pedidos: " + e.getMessage());
        } catch (Exception e) {
            throw new ServicioRemotoException("Error al consultar ms-pedidos: " + e.getMessage());
        }
    }
}
