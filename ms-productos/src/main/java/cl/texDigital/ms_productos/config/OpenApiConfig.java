package cl.texDigital.ms_productos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI msProductosOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ms-productos API")
                        .description("Microservicio de gestion del catalogo de productos de texDigital. "
                                + "Administra los productos textiles disponibles para produccion y venta.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo texDigital")
                                .url("https://github.com/brklyy/texDigital-BCKND"))
                        .license(new License().name("Uso academico - DSY1103")));
    }
}
