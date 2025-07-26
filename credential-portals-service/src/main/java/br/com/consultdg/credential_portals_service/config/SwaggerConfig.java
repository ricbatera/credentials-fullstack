package br.com.consultdg.credential_portals_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Credentials Portals Service API")
                .description("API para gerenciamento de credenciais de portais de shopping centers")
                .version("1.0.0")
                .contact(new Contact()
                    .name("ConsultDG")
                    .email("contato@consultdg.com.br")
                    .url("https://consultdg.com.br"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")));
    }
}
