# Movie Watchlist API

A Spring Boot REST API for managing movies and their reviews.

## Tech Stack

- Java 21
- Spring Boot
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway
- Maven
- JUnit / Mockito
- OpenAPI / Swagger UI
- Spring Boot Actuator

---

## Prerequisites

- Java 21
- PostgreSQL 16+ recommended
- Docker and Docker Compose (optional)

Verify Java:

```bash
java -version
```

---

## Getting the Project
Clone the repository:
```azure
git clone https://github.com/rohan-36/movie-watchlist.git
cd movie-watchlist
```


---
## Database Setup

The application supports two ways to run PostgreSQL.

### Option 1: Using Docker

Start PostgreSQL using Docker Compose:

```bash
docker compose up -d
```

The default database configuration is:

```text
Host: localhost
Port: 5432
Database: movie_watchlist
Username: movie_user
Password: movie_password
```

Stop PostgreSQL when finished:

```bash
docker compose down
```

### Option 2: Using Local PostgreSQL

Docker is not required if PostgreSQL is already installed locally.

Create a PostgreSQL database and user, or use your existing PostgreSQL credentials.

Set the following environment variables.

### Linux / macOS

```bash
export DB_HOST=localhost
export POSTGRES_PORT=5432
export POSTGRES_DB=movie_watchlist
export POSTGRES_USER=movie_user
export POSTGRES_PASSWORD=movie_password
```

### Windows PowerShell

```powershell
$env:DB_HOST="localhost"
$env:POSTGRES_PORT="5432"
$env:POSTGRES_DB="movie_watchlist"
$env:POSTGRES_USER="movie_user"
$env:POSTGRES_PASSWORD="movie_password"
```

The `.env.example` file contains the expected configuration variables.

Flyway automatically applies the database migrations when the application starts.

---

## Run the Application

Using the Maven Wrapper:

### Linux / macOS

```bash
./mvnw spring-boot:run
```

### Windows

```cmd
mvnw.cmd spring-boot:run
```

The API will be available at:

```text
http://localhost:8080
```

---

## Run Tests

Run the complete test suite:

### Linux / macOS

```bash
./mvnw clean test
```

### Windows

```cmd
mvnw.cmd clean test
```

---

## API Overview

### Movies

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/movies` | Create a movie |
| GET | `/api/v1/movies/{id}` | Get a movie by ID |
| GET | `/api/v1/movies` | List movies |
| PUT | `/api/v1/movies/{id}` | Update a movie |
| DELETE | `/api/v1/movies/{id}` | Delete a movie |

Movie listing supports pagination and optional genre filtering:

```text
GET /api/v1/movies?page=0&size=20
```

```text
GET /api/v1/movies?genre=Sci-Fi&page=0&size=20
```

### Reviews

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/movies/{movieId}/reviews` | Create a review for a movie |
| GET | `/api/v1/movies/{movieId}/reviews` | List reviews for a movie |
| GET | `/api/v1/reviews/{id}` | Get a review by ID |
| PUT | `/api/v1/reviews/{id}` | Update a review |
| DELETE | `/api/v1/reviews/{id}` | Delete a review |

Movie responses include the calculated average rating and total review count.

---

## Example Requests

### Create a Movie

```http
POST /api/v1/movies
Content-Type: application/json

{
  "title": "Inception",
  "genre": "Sci-Fi",
  "releaseYear": 2010
}
```

### Create a Review

```http
POST /api/v1/movies/{movieId}/reviews
Content-Type: application/json

{
  "reviewerName": "Alice",
  "rating": 5,
  "comment": "Excellent movie"
}
```

---

## Swagger / OpenAPI

Interactive API documentation is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI specification:

```text
http://localhost:8080/v3/api-docs
```

Swagger UI can be used to explore and execute the API endpoints directly.

---

## Health Check

Spring Boot Actuator health endpoint:

```text
http://localhost:8080/actuator/health
```

---

## Project Structure

The project follows a layered architecture separating API handling, business logic, persistence, data transfer, and error handling.

```text
src/
├── main/
│   ├── java/
│   │   └── com/npst/watchlist/
│   │       │
│   │       ├── controller/
│   │       │   ├── MovieController.java
│   │       │   └── ReviewController.java
│   │       │   └── REST API endpoints and HTTP request/response handling
│   │       │
│   │       ├── service/
│   │       │   ├── MovieService.java
│   │       │   ├── MovieServiceImpl.java
│   │       │   ├── ReviewService.java
│   │       │   └── ReviewServiceImpl.java
│   │       │   └── Business logic and transaction orchestration
│   │       │
│   │       ├── domain/
│   │       │   ├── entity/
│   │       │   │   ├── Movie.java
│   │       │   │   └── Review.java
│   │       │   │   └── JPA entities representing database records
│   │       │   │
│   │       │   ├── repository/
│   │       │   │   ├── MovieRepository.java
│   │       │   │   ├── ReviewRepository.java
│   │       │   │   ├── MovieStatsRepositoryCustom.java
│   │       │   │   └── MovieStatsRepositoryCustomImpl.java
│   │       │   │   └── Database access and movie statistics queries
│   │       │   │
│   │       │   └── projection/
│   │       │       ├── MovieStatsProjection.java
│   │       │       └── MovieStatsProjectionImpl.java
│   │       │       └── Read-only projections for aggregated movie data
│   │       │
│   │       ├── dto/
│   │       │   ├── request/
│   │       │   │   ├── CreateMovieRequest.java
│   │       │   │   ├── UpdateMovieRequest.java
│   │       │   │   ├── CreateReviewRequest.java
│   │       │   │   └── UpdateReviewRequest.java
│   │       │   │   └── Validated request payloads
│   │       │   │
│   │       │   └── response/
│   │       │       ├── MovieResponse.java
│   │       │       ├── ReviewResponse.java
│   │       │       ├── PagedResponse.java
│   │       │       └── API response models
│   │       │
│   │       ├── exception/
│   │       │   ├── GlobalExceptionHandler.java
│   │       │   ├── ErrorResponse.java
│   │       │   └── Custom exception classes
│   │       │   └── Centralized exception handling and safe API error responses
│   │       │
│   │       └── config/
│   │           ├── JpaAuditingConfig.java
│   │           └── OpenApiConfig.java
│   │           └── Application configuration and API documentation setup
│   │
│   └── resources/
│       ├── application.yaml
│       │   └── Application and database configuration
│       │
│       └── db/
│           └── migration/
│               └── Flyway SQL migrations
│                   └── Version-controlled database schema changes
│
└── test/
    └── java/
        └── com/npst/watchlist/
            └── Unit and integration tests
```

---

## Database and Migrations

The application uses PostgreSQL as its relational database.

Flyway manages database schema changes through version-controlled migrations.

Hibernate is configured to validate the existing schema rather than create or modify it.

This keeps database schema management under Flyway's control.

---

## Configuration

Database configuration can be supplied through environment variables:

```text
DB_HOST
POSTGRES_PORT
POSTGRES_DB
POSTGRES_USER
POSTGRES_PASSWORD
```

Default values are provided for local development and match the Docker Compose configuration.

See `.env.example` for the expected variables.

---

## Stopping the Application

Stop the Spring Boot application with:

```text
Ctrl + C
```

If PostgreSQL was started using Docker:

```bash
docker compose down
```

---

## Quick Start

For a Docker-based setup:

```bash
docker compose up -d
./mvnw spring-boot:run
```

Then open:

```text
http://localhost:8080/swagger-ui/index.html
```

To run tests:

```bash
./mvnw clean test
```