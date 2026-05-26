package cl.texDigital.ms_produccion.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${ms-inventario.url}")
    private String msInventarioUrl;

    @Bean
    public WebClient webClientInventario() {
        return WebClient.builder()
                .baseUrl(msInventarioUrl)
                .build();
    }
}
