package cl.texDigital.ms_pagos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI msPagosOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ms-pagos API")
                        .description("Microservicio de gestion de pagos de texDigital. "
                                + "Registra el pago de un pedido, aplica descuentos y calcula el IVA (19%). "
                                + "Se comunica con ms-pedidos para obtener el monto y actualizar el estado del pedido.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo texDigital")
                                .url("https://github.com/brklyy/texDigital-BCKND"))
                        .license(new License().name("Uso academico - DSY1103")));
    }
}
