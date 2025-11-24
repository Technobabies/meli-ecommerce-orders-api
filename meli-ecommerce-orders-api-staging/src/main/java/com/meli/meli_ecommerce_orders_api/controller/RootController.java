package com.meli.meli_ecommerce_orders_api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

/**
 * A simple controller to handle requests to the root URL ("/").
 * This is useful for health checks or providing basic service information.
 */
@RestController // Indicates that is going to handle requests
public class RootController {

    /**
     * Handles GET requests to the root path ("/").
     * @return A Map which will be automatically converted to a JSON object.
     */

    @GetMapping("/")
    public Map<String, String> getRoot() {
        // Collection.singletonMap is a way to create a Map with a single entry
        // Spring Web (with its Jackson library) will automatically convert this Map to the JSON:
        // { "message": "orders api working" }
        return Collections.singletonMap("message", "orders api working");
    }
}
