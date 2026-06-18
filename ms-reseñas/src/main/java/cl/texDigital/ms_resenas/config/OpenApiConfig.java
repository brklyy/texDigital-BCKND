package cl.texDigital.ms_resenas.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI msResenasOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ms-resenas API")
                        .description("Microservicio de gestion de reseñas de texDigital. "
                                + "Permite a los clientes dejar reseñas y puntajes sobre productos adquiridos.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo texDigital")
                                .url("https://github.com/brklyy/texDigital-BCKND"))
                        .license(new License().name("Uso academico - DSY1103")));
    }
}
