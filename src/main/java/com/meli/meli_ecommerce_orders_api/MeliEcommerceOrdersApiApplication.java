package com.meli.meli_ecommerce_orders_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MeliEcommerceOrdersApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MeliEcommerceOrdersApiApplication.class, args);
	}

}
