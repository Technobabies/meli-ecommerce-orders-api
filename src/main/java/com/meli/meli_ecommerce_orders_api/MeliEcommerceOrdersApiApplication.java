package com.meli.meli_ecommerce_orders_api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
        title = "Meli E-commerce Orders API",
        description = "API for managing orders",
        version = "0.0.1-SNAPSHOT",
        contact = @Contact(
                name = "Jared Trujillo",
                email = "trujillojaredalexander@gmail.com"
        ),
        license = @License(
                name = "MIT",
                url = "https://opensource.org/licenses/MIT"
        )
))
public class MeliEcommerceOrdersApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MeliEcommerceOrdersApiApplication.class, args);
	}

}
