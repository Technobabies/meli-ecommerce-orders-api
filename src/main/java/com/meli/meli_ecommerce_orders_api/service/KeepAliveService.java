package com.meli.meli_ecommerce_orders_api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * The type Keep alive service.
 * Service to keep other backend services alive by periodically calling their health endpoints.
 * This prevents free-tier services from going to sleep due to inactivity.
 */
@Service
public class KeepAliveService {

    private static final Logger logger = LoggerFactory.getLogger(KeepAliveService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestTemplate restTemplate;
    
    @Value("${keepalive.enabled:false}")
    private boolean keepAliveEnabled;
    
    @Value("${keepalive.endpoints:}")
    private List<String> endpoints;

    /**
     * Instantiates a new Keep alive service.
     *
     * @param restTemplate the rest template
     */
    @Autowired
    public KeepAliveService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Ping services.
     * Periodically pings configured health endpoints to keep services alive.
     * Runs every 4 minutes (240000 ms) to prevent services from going idle.
     */
    @Scheduled(fixedRate = 240000)
    public void pingServices() {
        if (!keepAliveEnabled) {
            return;
        }

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        logger.info("[{}] Starting keep-alive ping to {} service(s)", timestamp, endpoints.size());

        for (String endpoint : endpoints) {
            pingEndpoint(endpoint, timestamp);
        }
    }

    /**
     * Ping endpoint.
     * Pings a single health endpoint.
     *
     * @param endpoint  the endpoint
     * @param timestamp the timestamp
     */
    private void pingEndpoint(String endpoint, String timestamp) {
        try {
            logger.debug("[{}] Pinging endpoint: {}", timestamp, endpoint);
            restTemplate.getForObject(endpoint, String.class);
            logger.info("[{}] Successfully pinged: {} - Response received", timestamp, endpoint);
        } catch (ResourceAccessException resourceException) {
            logger.warn("[{}] Failed to ping {} - Timeout or connection error: {}", 
                timestamp, endpoint, resourceException.getMessage());
        } catch (Exception exception) {
            logger.error("[{}] Error pinging {} - {}: {}", 
                timestamp, endpoint, exception.getClass().getSimpleName(), exception.getMessage());
        }
    }

    /**
     * Trigger manual ping.
     * Manually trigger a ping to all configured endpoints.
     * Can be called via API for testing purposes.
     */
    public void triggerManualPing() {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        logger.info("[{}] Manual keep-alive ping triggered", timestamp);
        pingServices();
    }
}
