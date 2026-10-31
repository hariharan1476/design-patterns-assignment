# Design Patterns Assignment - Disease Statistics Application

A Spring Boot application implementing classic GoF Software Design Patterns to track and calculate disease statistics for India using multiple data sources (Disease.sh and Johns Hopkins).

## Design Patterns Implemented

1. **Singleton Pattern** (`com.upgrad.patterns.Config.RestServiceGenerator`)
   - Instantiates a single `RestTemplate` client instance with logging interceptors and shares it across the application.

2. **Strategy Pattern** (`com.upgrad.patterns.Strategies.DiseaseShStrategy` and `com.upgrad.patterns.Strategies.JohnHopkinsStrategy`)
   - Implements `com.upgrad.patterns.Interfaces.IndianDiseaseStat` to encapsulate data extraction from different sources (`DiseaseSh.json` and `JohnHopkins.json`).

3. **Factory Pattern** (`com.upgrad.patterns.Service.IndiaDiseaseStatFactory`)
   - Dynamically resolves and provides the appropriate strategy based on the requested `SourceType` (`DiseaseSh` or `JohnHopkins`).

4. **Facade Pattern** (`com.upgrad.patterns.Service.DiseaseCountFacade`)
   - Provides a clean, simplified interface for the controller layer to get disease counts and calculate infection ratios without exposing strategy resolution details.

5. **Chain of Responsibility Pattern** (`com.upgrad.patterns.Authentication.Authenticator` and `com.upgrad.patterns.Middleware`)
   - Chains authentication processors (`JwtAuthProcessor` tried first, then `BasicAuthProcessor`) invoked via `com.upgrad.patterns.Filters.AuthFilter`.

## Running and Testing

- **Run Application:** `./mvnw spring-boot:run`
- **Run Tests:** `./mvnw test`
