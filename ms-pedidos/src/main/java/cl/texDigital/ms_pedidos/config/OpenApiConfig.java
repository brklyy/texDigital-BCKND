package cl.texDigital.ms_pedidos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI msPedidosOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ms-pedidos API")
                        .description("Microservicio de pedidos de texDigital. "
                                + "Gestiona los pedidos y sus detalles, validando el cliente en ms-clientes "
                                + "y obteniendo el precio de los productos desde ms-productos.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo texDigital")
                                .url("https://github.com/brklyy/texDigital-BCKND"))
                        .license(new License().name("Uso academico - DSY1103")));
    }
}
