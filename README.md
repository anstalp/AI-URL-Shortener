# 🔗 MiniLink







MiniLink is an AI-powered URL shortener built to generate meaningful, human-readable short links instead of opaque random hashes. By combining Spring Boot, PostgreSQL, and Google's Gemini API, it transforms long URLs into contextual aliases such as `/netflix-browse`, making links easier to recognize, share, and trust.

## ✨ Key Features

- **AI-generated aliases**: Sends the original URL to Google's Gemini API (`gemini-3.5-flash`) and requests a structured JSON response containing a contextual, human-readable short code.
- **Readable short links**: Replaces traditional random identifiers with memorable aliases derived from the content and intent of the original URL.
- **Secure API integration**: Uses Spring's `RestClient` to communicate with Gemini, while the API key is injected from `application.properties` via `@Value` and passed securely through HTTP headers.
- **Structured JSON parsing**: Uses Jackson `ObjectMapper` to parse the AI response and extract the final short code cleanly and reliably.
- **Input validation layer**: Validates incoming requests through a dedicated `UrlRequestDto` using Jakarta Validation annotations such as `@NotBlank` and `@URL` before calling external services or writing to the database.
- **Database persistence**: Stores URL mappings in PostgreSQL, running in a Docker container for reproducible local development and deployment workflows.
- **Automatic schema management**: Leverages Hibernate with `ddl-auto=update` so schema evolution can be handled automatically during development.
- **Resilient fallback strategy**: If the Gemini API is unavailable, misconfigured, or returns an error, MiniLink falls back to generating a secure random short code so the application continues serving requests.
- **Interactive API testing**: Includes Springdoc OpenAPI with Swagger UI so developers can explore and test endpoints visually at `http://localhost:8080/swagger-ui/index.html`.
- **Container-friendly setup**: Uses Docker Compose to provision PostgreSQL with explicit port mapping (`5433:5432`) for fast onboarding.

## 🧠 System Architecture

MiniLink follows a straightforward request-processing pipeline that combines validation, AI enrichment, persistence, and redirection:

1. A client sends a long URL to the backend using the `POST /api/urls` endpoint.
2. The request is validated using `UrlRequestDto` with `@NotBlank` and `@URL` constraints.
3. The service layer forwards the original URL to the Gemini API using Spring's `RestClient`.
4. Gemini analyzes the URL context and returns structured JSON describing a suitable alias.
5. The backend parses the JSON using `ObjectMapper` and extracts the contextual short code.
6. If the AI request fails for any reason, the application generates a secure fallback short code.
7. The final mapping between the original URL and short code is persisted in PostgreSQL through Spring Data JPA.
8. When a user visits `GET /{shortCode}`, the application looks up the record and redirects the user to the original destination.

### Request Flow

```text
Client Request
   ↓
Spring Boot REST Controller
   ↓
Request Validation (UrlRequestDto + Jakarta Validation)
   ↓
URL Shortening Service
   ↓
Gemini API via RestClient
   ↓
JSON Parsing with ObjectMapper
   ↓
Fallback to Random Code (on AI failure)
   ↓
PostgreSQL Persistence via Spring Data JPA
   ↓
Short URL Response / Redirect Handling
```

## 🛠️ Technology Stack

### Backend

- Java 17+
- Spring Boot 3.x
- Spring Data JPA
- Spring Web
- Spring `RestClient`
- Jakarta Validation
- Jackson `ObjectMapper`
- Springdoc OpenAPI (Swagger UI)

### Database

- PostgreSQL
- Hibernate ORM
- Schema auto-update via `spring.jpa.hibernate.ddl-auto=update`

### AI

- Google Gemini API
- Model: `gemini-3.5-flash`
- Secure API key injection from `application.properties`

### Tools & DevOps

- Docker
- Docker Compose
- Maven or Gradle (depending on project setup)
- Git & GitHub

##  Prerequisites

Before running MiniLink locally, make sure the following are installed and available:

- Java 17 or newer
- Docker and Docker Compose
- Git
- A valid Google Gemini API key
- A local IDE such as IntelliJ IDEA or VS Code

## 🚀 Installation & Local Setup

### 1. Clone the repository

```bash
git clone https://github.com/your-username/minilink.git
cd minilink
```

### 2. Start PostgreSQL with Docker Compose

Make sure the project contains a `docker-compose.yml` similar to the following configuration:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16
    container_name: minilink-postgres
    environment:
      POSTGRES_DB: minilink
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5433:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

Run the container in detached mode:

```bash
docker-compose up -d
```

### 3. Configure application properties

Create or update `src/main/resources/application.properties`:

```properties
spring.application.name=MiniLink

server.port=8080

spring.datasource.url=jdbc:postgresql://localhost:5433/minilink
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

springdoc.swagger-ui.path=/swagger-ui/index.html

gemini.api.key=YOUR_GEMINI_API_KEY
gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent
```

### 4. Inject the Gemini API key in the service

The application reads the Gemini API key from properties and injects it using `@Value`, then forwards it securely as an HTTP header when calling Gemini with `RestClient`.

```java
@Value("${gemini.api.key}")
private String geminiApiKey;
```

### 5. Run the Spring Boot application

Using Maven:

```bash
./mvnw spring-boot:run
```

Or, if Maven is installed globally:

```bash
mvn spring-boot:run
```

### 6. Open Swagger UI

Once the application is running, open:

```text
http://localhost:8080/swagger-ui/index.html
```

This interface allows developers to test the URL shortening and redirection endpoints directly from the browser.

##  API Documentation

MiniLink exposes a simple API for creating and resolving short URLs.

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/urls` | Accepts the original long URL, validates it, requests an AI-generated alias, persists the mapping, and returns the shortened URL. |
| GET | `/{shortCode}` | Looks up the short code in PostgreSQL and redirects the client to the original URL. |

### Example request

```bash
curl -X POST http://localhost:8080/api/urls \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://www.netflix.com/browse/genre/34399"
  }'
```

### Example request body

```json
{
  "url": "https://www.netflix.com/browse/genre/34399"
}
```

### Expected behavior

- The backend validates the URL.
- Gemini analyzes the destination and proposes a contextual alias.
- The application parses the JSON response.
- A short URL such as `/netflix-browse` may be returned.
- If AI generation fails, a random fallback code is generated automatically.

##  Validation, Reliability, and Error Handling

MiniLink is designed to remain stable even when external dependencies fail.

- **Validation first**: Incoming payloads are checked through a custom `UrlRequestDto`, using `@NotBlank` and `@URL` constraints to reject malformed or empty input early.
- **Safer processing**: Only validated URLs are allowed to proceed to the AI layer and persistence layer, reducing the risk of storing broken or malicious input.
- **Graceful degradation**: When the Gemini API is unreachable, rate-limited, or backed by an expired key, the service catches the exception and switches to a secure random short code generator.
- **No hard failure path**: This fallback strategy ensures the URL shortener remains operational even during AI outages.

##  Future Enhancements

There are several strong directions for extending MiniLink into a broader AI-enabled platform:

- Add click analytics and dashboard reporting for shortened links.
- Introduce user authentication and per-user URL management.
- Support custom aliases with conflict detection.
- Add expiration dates and one-time-use links.
- Implement QR code generation for every shortened URL.
- Add PDF resume parsing to generate smart portfolio or profile-based short links.
- Add rate limiting and abuse protection for public deployments.
- Support deployment profiles for staging and production environments.


