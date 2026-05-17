# SportLink &mdash; Search Service

Second microservice of the SportLink project. Provides unified full-text and filter-based search across **users**, **activities**, and **sport facilities**. Indexes data published by the Account Management, Activity Management, and Booking services.

Mirrors the structure of the [`booking-service`](https://github.com/helena-78/booking_service) implementation.

## Stack
- Java 17
- Spring Boot 3.0.2
- Spring Data JPA + PostgreSQL
- Spring WebFlux (`WebClient`) for synchronous calls to peer services
- Springdoc OpenAPI (Swagger UI)
- Lombok

## Project layout
```
search-service/
├── pom.xml
├── mvnw, mvnw.cmd                            # Maven Wrapper (Unix + Windows)
├── .mvn/wrapper/maven-wrapper.properties     # Pinned Maven 3.9.14 distribution
├── .vscode/                                  # VS Code launch + settings
├── RestClientFile.rest                       # HTTP request samples
└── src/main/
    ├── java/com/sportlink/search/
    │   ├── SearchServiceApplication.java     # @SpringBootApplication + WebClient bean
    │   ├── CorsConfig.java                   # WebMvcConfigurer enabling CORS
    │   ├── DatabaseInitializer.java          # Creates searchservice_db on first run
    │   ├── DataInitializer.java              # Seeds facilities (sc-100, sc-101, sc-200), users, activities
    │   ├── controller/SearchController.java
    │   ├── service/SearchService.java
    │   ├── repository/SearchIndexRepository.java
    │   ├── model/
    │   │   ├── SearchIndex.java              # @Entity
    │   │   ├── SearchFilters.java            # @Embeddable - flattened into search_index row
    │   │   └── EntityType.java               # USER / ACTIVITY / FACILITY
    │   ├── dto/
    │   │   ├── SearchIndexDto.java           # Flat response - top-level filter fields
    │   │   ├── SearchFiltersDto.java
    │   │   └── IndexEntityRequest.java
    │   ├── client/
    │   │   └── BookingClient.java            # WebClient -> Booking Service @ :8088
    │   └── exception/
    │       ├── GlobalExceptionHandler.java   # 404 / 400 / 503 mappings
    │       ├── SearchIndexNotFoundException.java
    │       ├── InvalidSearchRequestException.java
    │       └── BookingServiceException.java
    └── resources/application.properties      # port 8086, services.booking.url
```

## REST endpoints

| Endpoint | Method | Description |
|---|---|---|
| `/api/search` | GET | Cross-entity search. Filters: `q`, `entityType`, `sportType`, `location`, `skillLevel`, `activityStatus`, `minRating`, `facilityName` |
| `/api/search/users` | GET | Search only users. Filters: `userName`, `sportType`, `skillLevel`, `location`, `userRatingScore`, `userSportPreferences` |
| `/api/search/activities` | GET | Search only activities. Filters: `activityTitle`, `sportType`, `activityStatus`, `location`, `activityMaxParticipants`, `activityTimeSlots` |
| `/api/search/facilities` | GET | Search only facilities. Filters: `facilityName`, `sportType`, `location`, `sportCenterId`. **`sportCenterId` triggers a synchronous call to the Booking Service.** |
| `/api/search/index` | POST | Upsert a SearchIndex entry. Accepts `entityType`, `entityId`, `displayName`, `keywords`, `filters`. |
| `/api/search/index/{entityType}/{entityId}` | GET | Retrieve a specific index entry. |
| `/api/search/index/{entityType}/{entityId}` | DELETE | Remove an entry from the search index. |

Swagger UI: <http://localhost:8086/swagger-ui/index.html>

## Running

### Prerequisites
- Java 17+
- PostgreSQL running on `localhost:5432` with user `postgres` / password `postgres`
- (For the integration demo) Booking Service running on `:8088`

### Start
On Windows (PowerShell or cmd):
```powershell
cd search-service
.\mvnw.cmd spring-boot:run
```

On macOS / Linux:
```bash
cd search-service
./mvnw spring-boot:run
```

If you have Maven installed system-wide, plain `mvn spring-boot:run` works too.

The first run downloads Apache Maven into `~/.m2/wrapper/dists/` (one-time, takes ~30s). Subsequent runs reuse it.

On first start, `DatabaseInitializer` creates the `searchservice_db` database, then `DataInitializer` seeds 3 facilities, 2 users, 2 activities.

### Running from VS Code
Open the `search-service` folder itself (not the parent monorepo) as the workspace root, so the Java extension sees `pom.xml` at the root. Install **Extension Pack for Java** and **Spring Boot Extension Pack**.

Once Maven import finishes (watch the bottom status bar), use the **"Run SearchServiceApplication (Spring Boot)"** entry in the Run and Debug panel (defined in `.vscode/launch.json`). Do **not** use the inline "Run" code lens above `main()` &mdash; it bypasses Maven and the classpath will be missing Spring Boot.

### Frontend
The frontend lives in the shared `frontend/` folder of the main repository, not inside this service. CORS is enabled service-side (`CorsConfig`) so the shared page can call `http://localhost:8086` from any origin.

## Real integration with the Booking Service

Two real synchronous interactions exist between the Search Service and the Booking Service:

### 1. Booking &rarr; Search (Booking is the caller)
The Booking Service's `SearchClient` calls:
```
GET http://localhost:8086/api/search?entityType=FACILITY&sportType=...&location=...&timeSlotId=...
```
This is the endpoint backing the Booking Service's own `GET /api/bookings/facilities/search`. When `timeSlotId` is supplied, the Search Service routes the request to a booking-aware facility search path; otherwise it performs a plain cross-entity search filtered to facilities.

**Demo:**
```bash
# with both services up
curl 'http://localhost:8088/api/bookings/facilities/search?sportType=BASKETBALL'
# -> booking-service hits search-service, returns Tartu Sport Hall and Kalev Sport Center
```

### 2. Search &rarr; Booking (Search is the caller)
When `/api/search/facilities` is called with a `sportCenterId` query param, the Search Service makes a real HTTP call via `BookingClient`:
```
GET http://localhost:8088/api/bookings?sportCenterId={id}
```
to confirm the Booking Service has booking activity for that sport center. If the Booking Service is down, the endpoint returns HTTP **503 Service Unavailable** with a descriptive message.

**Demo:**
```bash
# Booking-service seeds sc-100 (sport center) with booking bk-001
curl 'http://localhost:8086/api/search/facilities?sportCenterId=sc-100'
# -> search-service calls booking-service, gets 1 booking back,
#    narrows results to the facility with entityId=sc-100
#    (Tartu Sport Hall, BASKETBALL, Tartu)
```

Both calls can also be triggered straight from the frontend page.

## Seeded data

| Entity | ID | Notes |
|---|---|---|
| Facility | `sc-100` | Tartu Sport Hall, BASKETBALL, Tartu - **matches booking-service `bk-001`** |
| Facility | `sc-101` | Tallinn Football Arena, FOOTBALL, Tallinn - **matches booking-service `bk-002`** |
| Facility | `sc-200` | Kalev Sport Center, BASKETBALL, Tallinn |
| User | `usr-100` | Olena K., BASKETBALL, INTERMEDIATE, Tartu, rating 4.6 |
| User | `usr-101` | Kirill M., FOOTBALL, ADVANCED, Tallinn, rating 4.9 |
| Activity | `act-200` | Sunday Pickup Basketball, OPEN, Tartu |
| Activity | `act-201` | Friday Night Football, OPEN, Tallinn |

The two facility IDs are intentionally aligned with the Booking Service's seeded `sportCenterId` values so the integration demo returns meaningful data.

## Checkpoint 2 deliverables coverage

- **A. Second Responsibility** &mdash; full Search Service running on `:8086`.
- **B. Basic Integration** &mdash; real, non-mocked, bidirectional HTTP between Search and Booking. See `BookingClient.java` (Search &rarr; Booking) and the `/api/search` endpoint with `entityType=FACILITY` (Booking &rarr; Search via its own `SearchClient`).
- **C. Frontend** &mdash; lives in the shared `frontend/` folder of the main repo and calls `GET /api/search` (and the other `/api/search/*` endpoints) on this service.
