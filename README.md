# 🛍️ MELI Order Management Service

Jared Alexander Trujillo Ortiz  
**Date:** October 16, 2025  

A robust and scalable microservice for managing e-commerce orders, built with **Java** and **Spring Boot** as a solution to critical issues faced by MELI.

---

## 🧩 Project Context: The MELI Challenge

This project was developed to address significant technical failures in MELI's original order management system. The previous system suffered from operational issues caused by environment misconfigurations and database instability, resulting in substantial business losses and customer complaints.

This new service provides a modern, reliable, and scalable replacement. It implements best practices such as:

- Environment-specific configurations (preventing the original error)
- Robust database interactions
- Comprehensive API documentation via Swagger
- Clean, maintainable architecture

Together, these establish a solid foundation for MELI's future e-commerce operations.

---

## 🚀 Key Features

- **Full CRUD Functionality:** Create, Read, Update (via save), and Delete operations for orders.  
- **Multi-Item Orders:** Models real-world carts with multiple products (line items).  
- **Soft Deletion:** Orders are marked as deleted using a timestamp (`deletedAt`) instead of being removed permanently.  
- **Environment Profiles:** Uses Spring Profiles (`dev`, `prod`) for distinct configurations.  
  - `dev`: H2 in-memory database.  
  - `prod`: PostgreSQL database via environment variables.  
- **Interactive API Documentation:** Swagger UI for clear, testable endpoints.  
- **Centralized Exception Handling:** Consistent JSON error responses (e.g., *Order Not Found*).

---

## ⚙️ Tech Stack

| Component | Technology |
|------------|-------------|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.2.5 |
| **Persistence** | Spring Data JPA (Hibernate) |
| **Build Tool** | Maven |
| **Databases** | H2 (Dev) / PostgreSQL (Prod) |
| **API Docs** | SpringDoc OpenAPI (Swagger UI) |

---

## 🧭 Getting Started

### ✅ Prerequisites

- Java JDK 17+ (`java -version`)  
- Apache Maven (`mvn -version`)  
- IDE (IntelliJ IDEA, VS Code, etc.)  
- (Optional) Postman for testing  
- (For Production) Access to a PostgreSQL instance  

---

### 🛠 Installation & Configuration

Clone the repository:
```bash
git clone <your-repository-url>
cd order-service
```

#### Configuration Files
Located in `src/main/resources/`:

- `application.properties` – sets default profile to `dev`  
- `application-dev.properties` – H2 setup (no config needed)  
- `application-prod.properties` – PostgreSQL setup  

> ⚠️ **Important:** In production, never hardcode credentials.  
> Use environment variables or system properties for:
> ```
> spring.datasource.username=${DB_USERNAME}
> spring.datasource.password=${DB_PASSWORD}
> ```

---

## ▶️ Running the Application

### Option 1: Startup Script (Linux/macOS)
```bash
chmod +x startup.sh
./startup.sh
```

### Option 2: Using Maven Directly
```bash
mvn spring-boot:run
```

App starts at: [http://localhost:8080](http://localhost:8080)

---

## Application Profiles

This project uses Spring Boot Profiles to manage distinct configurations for different environments. This allows us to use an in-memory database for fast development and testing, while using a secure cloud database for production.

The configuration for each profile is located in `src/main/resources/`:

* `application.properties`: The base configuration, which sets `dev` as the default.
* `application-dev.properties`: For local development.
* `application-test.properties`: For running automated tests.
* `application-prod.properties`: For the live production deployment.

---

### 1. Development (`dev`)

This is the **default profile** for working on the application locally.

* **File:** `application-dev.properties`
* **Purpose:** Used for day-to-day coding and manual testing on your machine.
* **Database:** Connects to an **in-memory H2 database** (`jdbc:h2:mem:melidb`).
* **Activation:** This profile is active by default. You don't need to do anything. Just run the application in your IDE.
* **Features:**
    * The database is reset every time you restart the application.
    * The H2 Console is enabled at `http://localhost:8080/h2-console` for you to view and query the database directly.
    * Uses `spring.jpa.hibernate.ddl-auto=update` to automatically create/update tables based on your `@Entity` classes.

### 2. Testing (`test`)

This profile is used *only* when running automated tests (like JUnit tests).

* **File:** `application-test.properties`
* **Purpose:** To run the automated test suite in a clean, isolated environment.
* **Database:** Uses a separate, in-memory **H2 database** (`jdbc:h2:mem:testdb`).
* **Activation:** This profile is **automatically activated** by Spring Boot whenever you run your tests (e.g., `mvn test` or clicking "Run Tests" in your IDE).
* **Configuration:**
    * Uses a different database name to avoid all conflicts with your `dev` database.
    * Uses `spring.jpa.hibernate.ddl-auto=create-drop`, which builds the database from scratch when tests start and completely deletes it when they finish. This ensures every test run is clean and repeatable.

### 3. Production (`prod`)

This is the profile for the **live, public-facing application**.

* **File:** `application-prod.properties`
* **Purpose:** For the deployed application that real users will interact with.
* **Database:** Connects to the main **Supabase (PostgreSQL)** production database.
* **Activation:** This profile **must be explicitly set** on the server.
* **Configuration:** This file contains **no secrets**. All sensitive values (database URL, username, password) are loaded from environment variables (e.g., `${PROD_DB_URL}`).

---

## How to Activate a Profile

### On a Local Machine (IDE)

The `dev` profile is active by default.

If you ever need to *test* the production configuration locally, you can force the `prod` profile in your IDE:
1.  Go to **Run > Edit Configurations...**
2.  Add a new Environment Variable:
    * **Name:** `SPRING_PROFILES_ACTIVE`
    * **Value:** `prod`
3.  You must also add **all** the production variables (`PROD_DB_URL`, `PROD_DB_USER`, `PROD_DB_PASSWORD`) in the same place.

### On the Server (Production)

The included `start.sh` script handles this for you.

When you run `./start.sh`, it automatically does two things:
1.  Runs `export SPRING_PROFILES_ACTIVE=prod`.
2.  Reads your `.env` file to securely load all your production database credentials.

---

### Running with Production Profile

#### 1. Package the Application
```bash
mvn clean package
```

Creates `target/order-service-0.0.1-SNAPSHOT.jar`.

#### 2. Set Environment Variables
```bash
export DB_USERNAME=your_prod_db_user
export DB_PASSWORD=your_prod_db_password
```

#### 3. Run with `prod` Profile
```bash
java -jar target/order-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

---

## 📚 Using the API

### Swagger UI
Visit: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### H2 Database Console (Dev Only)
- **URL:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)  
- **JDBC URL:** `jdbc:h2:mem:melidb`  
- **Username:** `sa`  
- **Password:** `password`

### Postman Collection
Import `postman_collection.json` (included in project root).

---

## 🌐 API Endpoints

**Base Path:** `/api/v1`

| Method | Endpoint | Description | Request Body | Success Response |
|---------|-----------|--------------|---------------|------------------|
| GET | `/` | Health Check | N/A | `200 OK (JSON)` |
| POST | `/orders` | Create new order | `CreateOrderRequest` | `201 Created (Order)` |
| GET | `/orders` | Get all active orders | N/A | `200 OK (List<Order>)` |
| GET | `/orders/{id}` | Get order by UUID | N/A | `200 OK (Order)` |
| DELETE | `/orders/{id}` | Soft-delete order | N/A | `204 No Content` |

---

### 🧾 Example: Create Order (`POST /api/v1/orders`)

**Request Body (JSON):**
```json
{
  "createdBy": "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "items": [
    {
      "productId": "a1b2c3d4-e5f6-1234-abcd-556677889900",
      "productName": "Teclado Mecánico Gamer RGB",
      "quantity": 1,
      "pricePerUnit": 1899.99
    },
    {
      "productId": "f1e2d3c4-b5a6-4321-fedc-112233445566",
      "productName": "Mouse Inalámbrico Ergonómico",
      "quantity": 1,
      "pricePerUnit": 850.50
    }
  ]
}
```

**Successful Response (201 Created):**
```json
{
  "id": "c7a8b6e5-4d3c-4b2a-8f1e-9d0c1b2a3f4d",
  "createdBy": "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "items": [
    {
      "id": "e1f2a3b4-c5d6-7890-1234-abcdef123456",
      "productId": "a1b2c3d4-e5f6-1234-abcd-556677889900",
      "productName": "Teclado Mecánico Gamer RGB",
      "quantity": 1,
      "pricePerUnit": 1899.99,
      "totalPrice": 1899.99
    },
    {
      "id": "f6e5d4c3-b2a1-0987-6543-fedcba987654",
      "productId": "f1e2d3c4-b5a6-4321-fedc-112233445566",
      "productName": "Mouse Inalámbrico Ergonómico",
      "quantity": 1,
      "pricePerUnit": 850.50,
      "totalPrice": 850.50
    }
  ],
  "totalPrice": 2750.49,
  "status": "PENDING",
  "deletedAt": null,
  "orderDate": "2025-10-16T19:10:00.123456",
  "lastUpdatedDate": "2025-10-16T19:10:00.123456"
}
```

---

### 🔍 Example: Get Order by ID (`GET /api/v1/orders/{id}`)

Replace `{id}` with a valid Order UUID.

**Success (200 OK):**  
Returns a full JSON object of the order (same as above).

**Failure (404 Not Found):**
```json
{
  "timestamp": "2025-10-16T19:35:00.987654",
  "status": 404,
  "error": "Not Found",
  "message": "Order not found with id: c7a8b6e5-4d3c-4b2a-8f1e-9d0c1b2a3f4d"
}
```

---

## Postman documentation

POST
<img width="1547" height="957" alt="Image" src="https://github.com/user-attachments/assets/55623dfd-fd88-4feb-8217-942f1dc58fe8" />

GET 
<img width="1538" height="927" alt="Image" src="https://github.com/user-attachments/assets/dfe7614c-6253-4322-955e-eee3054fa6dd" />

DELETE
<img width="1506" height="628" alt="Image" src="https://github.com/user-attachments/assets/268e8f22-a331-4918-81fa-8bb31c3a3572" />

## Swagger documentation

GET
<img width="1429" height="796" alt="Image" src="https://github.com/user-attachments/assets/2de0b08d-87fb-4c50-b8ee-5422109358cc" />

POST
<img width="1134" height="826" alt="Image" src="https://github.com/user-attachments/assets/44e6b3ab-22c6-44fb-8096-f85b675a9589" />

GET BY ID
<img width="1278" height="779" alt="Image" src="https://github.com/user-attachments/assets/ef619b3d-c51d-4ed4-b179-8eb2375ecf6b" />

DELETE
<img width="1421" height="451" alt="image" src="https://github.com/user-attachments/assets/edf043a5-44ad-4372-be16-a0a56546233e" />

---
## ✅ Tests
<img width="1853" height="466" alt="image" src="https://github.com/user-attachments/assets/16a306b5-240e-4a46-9668-3eed4caa9e02" />

---
## JaCoCo Coverage
<img width="1910" height="482" alt="image" src="https://github.com/user-attachments/assets/596f20d4-087c-44da-b802-3e28473eea02" />



---

POSTMAN JSON in
https://github.com/JaredTrOr/meli-ecommerce-orders-api/blob/master/MeliECommerce.postman_collection.json
