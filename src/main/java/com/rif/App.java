package com.rif;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(title = "RIF Company", version = "1.0",
                description = "CrowdFunding APP APIS", contact = @Contact(
                name = "RIF",
                email = "moneem.suibgu@gmail.com",
                url = "https://github.com/MoneemSuibgui/CrowdFunding-Project-RIF"
        ))
        , servers = {
        @Server(
                description = "Local Environment",
                url = "http://localhost:8080"
        )}
)
// this annotation for the configuration of security JWT in swagger ui
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT auth description",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
