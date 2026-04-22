package com.optum.ap.services.rest.common.config.openapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@OpenAPIDefinition(servers = { @Server(url = "${server.servlet.context-path}", description = "Default Server URL") })

@Configuration
public class OpenAPIConfiguration {
	@Value("${swagger-auth:basic}")
	private String swaggerAuthType;

	@Bean
	public OpenAPI customOpenAPI(@Value("${springdoc.version:3.0}") String appVersion) {
		// Couldn't find a way to do this in another programmatic way; sort controllers
		// alphabetically, have all controllers closed at the start
		System.getProperties().put("springdoc.swagger-ui.tagsSorter", "alpha");
		System.getProperties().put("springdoc.swagger-ui.doc-expansion", "none");

		if (swaggerAuthType.equalsIgnoreCase("bearer")) {

			return new OpenAPI().addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
					.components(new Components().addSecuritySchemes("bearerAuth",
							new SecurityScheme().name("bearerAuth").type(SecurityScheme.Type.HTTP).scheme("bearer")
									.bearerFormat("JWT")))

					.tags(null).info(new Info().title("Api Documentation").version(appVersion)
							.license(new License().name("Apache 2.0").url("http://springdoc.org")));
		} else {
			return new OpenAPI()
					.components(new Components().addSecuritySchemes("basicAuth",
							new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")))
					.tags(null).info(new Info().title("Api Documentation").version(appVersion)
							.license(new License().name("Apache 2.0").url("http://springdoc.org")));
		}

	}
}
