package com.meli.meli_ecommerce_orders_api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HealthResponse {
    private String status;
    private String environment;
    private DatabaseStatus database;
    private String timestamp;
    private String service;

    public HealthResponse() {}

    public HealthResponse(String status, String environment, DatabaseStatus database, String timestamp, String service) {
        this.status = status;
        this.environment = environment;
        this.database = database;
        this.timestamp = timestamp;
        this.service = service;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public DatabaseStatus getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseStatus database) {
        this.database = database;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public static class DatabaseStatus {
        private boolean connected;
        private String message;

        public DatabaseStatus() {}

        public DatabaseStatus(boolean connected, String message) {
            this.connected = connected;
            this.message = message;
        }

        public boolean isConnected() {
            return connected;
        }

        public void setConnected(boolean connected) {
            this.connected = connected;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}

