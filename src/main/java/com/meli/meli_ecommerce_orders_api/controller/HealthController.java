package com.meli.meli_ecommerce_orders_api.controller;

import com.meli.meli_ecommerce_orders_api.dto.ApiResponse;
import com.meli.meli_ecommerce_orders_api.dto.HealthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Health check controller to monitor service status and database connectivity.
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final DataSource dataSource;
    private final Environment environment;

    public HealthController(DataSource dataSource, Environment environment) {
        this.dataSource = dataSource;
        this.environment = environment;
    }

    /**
     * Health check endpoint that verifies service status and database connectivity.
     *
     * @return Health check response with service status, environment, and database status
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<HealthResponse>> healthCheck() {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String activeProfile = getActiveProfile();
        
        logger.info("[{}] Health check requested - Environment: {}", timestamp, activeProfile);

        // Check database connectivity
        HealthResponse.DatabaseStatus dbStatus = checkDatabaseConnection();
        
        // Determine overall service status
        String serviceStatus = dbStatus.isConnected() ? "UP" : "DOWN";
        String serviceName = "meli-ecommerce-orders-api";

        HealthResponse healthResponse = new HealthResponse(
            serviceStatus,
            activeProfile,
            dbStatus,
            timestamp,
            serviceName
        );

        // Log the health check result
        if (dbStatus.isConnected()) {
            logger.info("[{}] Health check completed - Status: UP, Environment: {}, Database: Connected", 
                timestamp, activeProfile);
        } else {
            logger.warn("[{}] Health check completed - Status: DOWN, Environment: {}, Database: {} - {}", 
                timestamp, activeProfile, dbStatus.isConnected() ? "Connected" : "Disconnected", dbStatus.getMessage());
        }

        HttpStatus httpStatus = dbStatus.isConnected() ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;

        return ResponseEntity
            .status(httpStatus)
            .body(ApiResponse.success("Health check completed", healthResponse));
    }

    /**
     * Checks database connectivity by attempting to get a connection.
     *
     * @return DatabaseStatus with connection status and message
     */
    private HealthResponse.DatabaseStatus checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(5); // 5 second timeout
            
            if (isValid) {
                String dbProduct = connection.getMetaData().getDatabaseProductName();
                String dbVersion = connection.getMetaData().getDatabaseProductVersion();
                return new HealthResponse.DatabaseStatus(
                    true, 
                    String.format("Connected to %s %s", dbProduct, dbVersion)
                );
            } else {
                return new HealthResponse.DatabaseStatus(false, "Connection validation failed");
            }
        } catch (SQLException e) {
            logger.error("Database connection check failed: {}", e.getMessage());
            return new HealthResponse.DatabaseStatus(false, "Database connection failed: " + e.getMessage());
        }
    }

    /**
     * Ping response entity.
     * Simple ping endpoint for keep-alive checks from other services.
     * Returns a minimal response to reduce overhead.
     *
     * @return the response entity
     */
    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.ok(ApiResponse.success("pong", "Service is alive"));
    }

    /**
     * Gets active profile.
     * Gets the active Spring profile(s).
     *
     * @return the active profile
     */
    private String getActiveProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length > 0) {
            return String.join(",", activeProfiles);
        }
        String defaultProfile = environment.getProperty("spring.profiles.active", "dev");
        return defaultProfile;
    }
}

