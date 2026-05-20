package com.financeiro.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI openAPI() {
		SecurityScheme securityScheme = new SecurityScheme()
			.type(SecurityScheme.Type.HTTP)
			.scheme("bearer")
			.bearerFormat("JWT")
			.in(SecurityScheme.In.HEADER)
			.name("Authorization");

		return new OpenAPI()
			.info(new Info()
				.title("Financeiro API")
				.description("API REST do sistema de controle financeiro pessoal e compartilhado.")
				.version("1.0.0")
				.contact(new Contact().name("Equipe Financeiro")))
			.components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
			.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
	}
}
