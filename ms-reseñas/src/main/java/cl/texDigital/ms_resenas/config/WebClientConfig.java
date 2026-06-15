package cl.texDigital.ms_resenas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${ms-pedidos.url}")
    private String msPedidosUrl;

    @Bean
    public WebClient webClientPedidos() {
        return WebClient.builder()
                .baseUrl(msPedidosUrl)
                .build();
    }
}
