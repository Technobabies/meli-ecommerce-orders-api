#!/bin/bash
# ---
# Startup Script for the E-commerce Service
# ---
# This script performs the following:
# 1. Sets the Spring Boot profile to 'prod' (to use application-prod.properties).
# 2. Loads all secret environment variables from the '.env' file.
# 3. Finds and runs the application's .jar file.
# ---

echo "Starting the e-commerce service..."

# 1. Set the Spring Boot Profile to 'production'
# This tells Spring to use 'application-prod.properties'
export SPRING_PROFILES_ACTIVE=prod

# 2. Load Environment Variables from .env file
# This file contains all secrets (DB passwords, API keys)
# and MUST NOT be committed to Git.
ENV_FILE=".env"

if [ -f "$ENV_FILE" ]; then
    echo "Loading environment variables from $ENV_FILE file..."
    # 'set -a' exports all variables defined in the file
    # 'source' reads and executes the file in the current shell
    set -a
    source "$ENV_FILE"
    set +a
else
    echo "WARNING: '$ENV_FILE' file not found."
    echo "The application may fail if required variables (PROD_DB_USER, etc.) are not set."
    # We continue, as Spring Boot will "fail fast" if a variable is missing.
fi

# 3. Find the application JAR file in the 'target' directory
# This finds the first file ending in '.jar' in the target dir.
JAR_FILE=$(find target/ -maxdepth 1 -name "*.jar" | head -n 1)

# Check if JAR file exists
if [ -z "$JAR_FILE" ]; then
    echo "ERROR: No .jar file found in the 'target/' directory."
    echo "Please build the project first (e.g., 'mvn clean package')."
    exit 1
fi

# 4. Run the Application
echo "Launching application: $JAR_FILE"
echo "Profile active: ${SPRING_PROFILES_ACTIVE}"
echo "Press Ctrl+C to stop."

# 'exec' replaces the script process with the java process
exec java -jar "$JAR_FILE"