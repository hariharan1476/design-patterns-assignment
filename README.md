# COVID-19 Disease Statistics Tracker — Design Patterns Implementation

A production-ready Spring Boot REST API that provides disease case statistics and calculates infected population ratios for India using multiple data sources (**Disease.sh** and **Johns Hopkins University**).

This project demonstrates the practical implementation of **5 classic Gang of Four (GoF) Software Design Patterns** in Java.

---

## Table of Contents
- [Architecture Overview](#architecture-overview)
- [Design Patterns Implemented](#design-patterns-implemented)
- [Codebase Structure & Package Documentation](#codebase-structure--package-documentation)
- [Data Sources](#data-sources)
- [Authentication Specification](#authentication-specification)
- [API Endpoints & Usage](#api-endpoints--usage)
- [Setup & Execution Guide](#setup--execution-guide)
- [Automated Testing](#automated-testing)

---

## Architecture Overview

```mermaid
graph TD
    Client["Client HTTP Request"] --> AuthFilter["AuthFilter (Servlet Filter)"]
    
    subgraph "Chain of Responsibility (Authentication)"
        AuthFilter --> Authenticator["Authenticator.GetAuthProcessor()"]
        Authenticator --> JwtProc["JwtAuthProcessor (Checks Bearer token)"]
        JwtProc -->|If not JWT| BasicProc["BasicAuthProcessor (Checks Username/Password)"]
    end

    AuthFilter -->|Authorized (200)| Controller["DiseaseController (/v1/disease)"]
    AuthFilter -->|Unauthorized| AuthFail["HTTP 401 Unauthorized"]

    subgraph "Facade Pattern"
        Controller --> Facade["DiseaseCountFacade"]
    end

    subgraph "Factory Pattern"
        Facade --> Factory["IndiaDiseaseStatFactory.GetInstance(sourceType)"]
    end

    subgraph "Strategy Pattern"
        Factory -->|SourceType.DiseaseSh| Strat1["DiseaseShStrategy"]
        Factory -->|SourceType.JohnHopkins| Strat2["JohnHopkinsStrategy"]
    end

    subgraph "Singleton Pattern"
        Strat1 --> Singleton["RestServiceGenerator.GetInstance() (RestTemplate)"]
        Strat2 --> Singleton
    end

    subgraph "Data Storage"
        Strat1 --> Json1[("DiseaseSh.json")]
        Strat2 --> Json2[("JohnHopkins.json")]
    end
```

---

## Design Patterns Implemented

| Design Pattern | Key Classes | Implementation & Role |
|---|---|---|
| **Singleton Pattern** | `RestServiceGenerator` | Ensures a single `RestTemplate` client instance is instantiated once with custom request/response logging interceptors and reused across the entire application lifecycle via `GetInstance()`. |
| **Strategy Pattern** | `IndianDiseaseStat`, `DiseaseShStrategy`, `JohnHopkinsStrategy` | Encapsulates provider-specific data parsing algorithms behind a common interface (`GetActiveCount()`), allowing runtime interchangeable data resolution. |
| **Factory Pattern** | `IndiaDiseaseStatFactory`, `SourceType` | Decouples strategy creation from consumers. Dynamically resolves and instantiates the correct strategy based on the `SourceType` enum parameter (`DiseaseSh` or `JohnHopkins`). |
| **Facade Pattern** | `DiseaseCountFacade` | Shields the `DiseaseController` from internal business logic, strategy resolution, and infection ratio math, exposing simplified methods: `getDiseaseShCount()`, `getJohnHopkinCount()`, and `getInfectedRatio(sourceType)`. |
| **Chain of Responsibility** | `Authenticator`, `AuthenticationProcessor`, `JwtAuthProcessor`, `BasicAuthProcessor`, `AuthFilter` | Dispatches incoming request authentication sequentially: first attempts JWT token validation; if missing, falls back to Basic Username/Password validation before passing through `AuthFilter`. |

---

## Codebase Structure & Package Documentation

```
src/main/java/com/upgrad/patterns/
├── DiseaseApplication.java               # Main Spring Boot application entry point
│
├── Authentication/
│   ├── AuthenticationProvider.java      # Abstract provider with Authenticate() contract
│   ├── Authenticator.java               # Factory & helper chaining processors and extracting headers
│   ├── BasicAuthProvider.java           # Basic auth credentials holder & validator (India / India)
│   └── JwtAuthProvider.java             # JWT token holder & validator (Bearer upgrad)
│
├── Config/
│   ├── DiseaseAppConfig.java            # SSL Trust context and hostname verifier configuration
│   └── RestServiceGenerator.java        # Singleton RestTemplate provider
│
├── Constants/
│   └── SourceType.java                  # Enum declaring supported sources: DiseaseSh, JohnHopkins
│
├── Controllers/
│   └── DiseaseController.java           # Spring REST Controller exposing /v1/disease endpoints
│
├── Entity/
│   ├── DiseaseShResponse.java           # Jackson POJO mapping DiseaseSh JSON structure
│   ├── JohnHopkinResponse.java          # Jackson POJO mapping Johns Hopkins JSON structure
│   └── Stat.java                        # Nested statistics POJO for Johns Hopkins counts
│
├── Filters/
│   └── AuthFilter.java                  # Intercepts requests, executes Chain of Responsibility
│
├── Interfaces/
│   └── IndianDiseaseStat.java           # Strategy interface declaring GetActiveCount()
│
├── Middleware/
│   ├── AuthenticationProcessor.java     # Abstract handler in Chain of Responsibility
│   ├── BasicAuthProcessor.java          # Concrete handler validating BasicAuthProvider
│   └── JwtAuthProcessor.java            # Concrete handler validating JwtAuthProvider
│
├── Service/
│   ├── DiseaseCountFacade.java          # Facade simplifying strategy calls and calculating ratio
│   └── IndiaDiseaseStatFactory.java     # Factory resolving strategies based on SourceType
│
└── Strategies/
    ├── DiseaseShStrategy.java           # Strategy parsing DiseaseSh.json
    └── JohnHopkinsStrategy.java         # Strategy parsing JohnHopkins.json
```

---

## Data Sources

The project utilizes standardized mock JSON datasets placed under `src/main/resources/`:

1. **`DiseaseSh.json`**: Provides global and Indian COVID statistics.
   - Extracts active case count using `cases: 44997780`.
2. **`JohnHopkins.json`**: Provides time-series hospital statistics for India.
   - Filters entries where `country == "India"`, maps `stats.confirmed: 10000.5`, and aggregates the sum rounded to `10001`.

---

## Authentication Specification

All incoming HTTP requests pass through `AuthFilter`. Requests must supply one of two authentication methods:

1. **JWT Authentication (Preferred)**
   - Header: `Authorization: Bearer upgrad`
2. **Basic Authentication**
   - Header: `Username: India`
   - Header: `Password: India`

*If neither credential set is provided or invalid credentials are submitted, the server halts execution and returns `HTTP 401 Unauthorized` with body message `"Authentication headers missing"`.*

---

## API Endpoints & Usage

Base URL: `http://localhost:8080`

### 1. Get Count from Disease.sh
- **URL:** `/v1/disease/disease-sh-io/count`
- **Method:** `GET`
- **Headers:** `Authorization: Bearer upgrad`
- **Example Request:**
  ```bash
  curl -X GET http://localhost:8080/v1/disease/disease-sh-io/count \
       -H "Authorization: Bearer upgrad"
  ```
- **Response:**
  ```
  44997780
  ```

---

### 2. Get Count from Johns Hopkins
- **URL:** `/v1/disease/john-hopkins/count`
- **Method:** `GET`
- **Headers:** `Username: India`, `Password: India` (or `Authorization: Bearer upgrad`)
- **Example Request:**
  ```bash
  curl -X GET http://localhost:8080/v1/disease/john-hopkins/count \
       -H "Username: India" \
       -H "Password: India"
  ```
- **Response:**
  ```
  10001
  ```

---

### 3. Calculate Infected Population Ratio
Calculates `(activeCases / 900,000,000) * 100` formatted to 3 decimal places.

- **URL:** `/v1/disease/infected-ratio?sourceType={sourceType}`
- **Method:** `GET`
- **Headers:** `Authorization: Bearer upgrad`

#### A. Using Disease.sh:
```bash
curl -X GET "http://localhost:8080/v1/disease/infected-ratio?sourceType=DiseaseSh" \
     -H "Authorization: Bearer upgrad"
```
**Response (HTTP 200):**
```
5.000
```

#### B. Using Johns Hopkins:
```bash
curl -X GET "http://localhost:8080/v1/disease/infected-ratio?sourceType=JohnHopkins" \
     -H "Authorization: Bearer upgrad"
```
**Response (HTTP 200):**
```
0.001
```

#### C. Using Invalid Source (Error Handling):
```bash
curl -X GET "http://localhost:8080/v1/disease/infected-ratio?sourceType=InvalidSource" \
     -H "Authorization: Bearer upgrad"
```
**Response (HTTP 502 Bad Gateway):**
```
Invalid source type specified. Available source type (DiseaseSh, JohnHopkins)
```

---

## Setup & Execution Guide

### Prerequisites
- **Java JDK 8 or higher** (supports up to JDK 24)
- **Maven 3.6+** (Maven Wrapper `./mvnw` is included in the project)

### Build the Project
```bash
./mvnw clean compile
```

### Run Locally
```bash
./mvnw spring-boot:run
```
The server will boot on `http://localhost:8080`.

### Create Standalone Runnable JAR
```bash
./mvnw package
java -jar target/patterns-0.0.1-SNAPSHOT.jar
```

---

## Automated Testing

The project includes an end-to-end integration and unit test suite in `src/test/java/com/upgrad/patterns/DemoApplicationTests.java`.

### Running Tests
```bash
./mvnw test
```

### Verified Scenarios:
1. **Singleton Verification:** Confirms `RestServiceGenerator.GetInstance()` consistently returns the identical reference instance.
2. **Authentication Chain:** Tests valid JWT (`Bearer upgrad`), invalid JWT, valid Basic Auth (`India`/`India`), wrong credentials, and absent headers.
3. **Factory & Strategy Resolution:** Confirms dynamic resolution of `DiseaseShStrategy` and `JohnHopkinsStrategy` and proper exception handling.
4. **Data Calculation:** Validates rounding logic and correct numbers extracted from `DiseaseSh.json` and `JohnHopkins.json`.
5. **REST API Endpoints:** Tests HTTP 200 OK responses, HTTP 401 Unauthorized handling, and HTTP 502 Bad Gateway error scenarios.

**Test Run Output:**
```text
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
