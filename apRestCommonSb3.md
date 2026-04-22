# 📦 `oap-dev-ap-rest-common` — Project Analysis

## 🔍 What Is This Project?

**`ap-rest-common-SB3`** is a **shared library / framework** for Optum's AP (Administrative Platform) REST microservices built on **Spring Boot 3 + Java 17**. It is NOT a standalone application — it is a **reusable JAR** (version `0.0.6-SNAPSHOT`) that other AP REST services include as a dependency. It provides common infrastructure so individual services don't have to re-implement it each time.

---

## 🏗️ Project Identity

| Field | Value |
|-------|-------|
| Group ID | `com.optum.ap.services.rest.common` |
| Artifact ID | `ap-rest-common-SB3` |
| Version | `0.0.6-SNAPSHOT` |
| Java | 17 |
| Spring Boot | 3.x (via parent POM `ap-parent-pom`) |
| Packaging | JAR (shared library) |

---

## 📁 Project Structure & What Each Part Does

```
src/main/java/
├── com.optum.ap.services.rest.common/
│   ├── config/
│   │   ├── db/          → Multi-DataSource database configuration
│   │   └── openapi/     → Swagger/OpenAPI UI configuration
│   ├── exception/       → Global REST exception handling
│   └── security/
│       ├── basic/       → HTTP Basic Auth security
│       ├── dto/         → Auth config data objects
│       └── stargate/    → Stargate API Gateway JWT security
│
├── com.optum.ap.services.rest.service/
│   ├── controller/      → Abstract base controllers (3 types)
│   └── soapWrap/        → SOAP-to-REST wrapping base class
│
└── com.optum.ap.task.config/
    └──                  → Scheduled job/task configuration utilities
```

---

## 🔧 Key Features / Modules

### 1. 🗄️ Multi-Database Configuration (`config/db/`)
The most complex part of the project. It provides **plug-and-play JPA datasource beans** for over 20 enterprise databases:

| DB Name | Bean Name | Purpose |
|---------|-----------|---------|
| APS | `apsDataSource` | Core AP Services DB (set as **primary**) |
| TPA | `tpaDataSource` | Third Party Admin DB |
| AOS, ATF, BASS, BOS, CAI, CDH, CMS, CTI, EMS, EOB, FHM, IPRO, IVR, MSR, ODS, OPD, Quality, SLC, SWA, VA, WDT, WEB | ... | Various enterprise data domains |

- Each DB config is **conditionally loaded** via `@DBPresentConditionController` — a database bean is only created if that DB name appears in the app's `databaseList` config property AND its datasource config exists.
- `ConfigurationPropYaml` reads a YAML `databaseList` to decide which databases to activate.
- `DBConfigBeanHelper` automatically marks `apsDataSource` as `@Primary` when present.
- All datasources use **JNDI lookups** (`java:comp/env/jdbc/<DBNAME>`) typical of Tomcat/JEE deployments.

### 2. 🔐 Security — Three Authentication Models (`security/`)

#### a) HTTP Basic Auth (`basic/`)
- `APSecurityConfig` — Spring Security filter chain for Basic Auth.
  - Whitelists Swagger UI, actuator, health endpoints.
  - Roles are read from YAML config (`ap-auth.roles`).
  - Uses `APSecurityBasicAuthEntryPoint` for custom 401 responses.
  - Has a smart `BasicRequestMatcher` to selectively prompt for credentials.
- `BasicSecurity` — checks if authenticated user has the required scope/role.
- `ConfigPropSecurity` — reads service role list from config.

#### b) OAuth2 (`dto/OAuthConfigProp`, `OAuthBaseController`)
- Loads OAuth users/scopes from YAML (`ap-oauth.users`).
- `OAuthBaseController` is the abstract base for OAuth-secured controllers.
- Uses `@PreAuthorize("@authorizationSecurity.hasOAuthScope(authentication)")`.

#### c) Stargate API Gateway JWT (`stargate/`)
- `StargateRequestFilter` — a servlet filter that intercepts every incoming HTTP request.
  - Reads the `JWT` header (Optum's Stargate gateway injects this).
  - Bypasses Basic Auth requests (when `Authorization: Basic` header is present and no JWT).
- `StargateJWTValidator` — validates Stargate JWT tokens:
  1. Decodes the JWT and extracts the embedded X.509 certificate (`x5c` header claim).
  2. Validates that certificate against a truststore.
  3. Verifies JWT signature using the cert's RSA public key.
  4. Validates payload hash (`payloadhash` claim vs SHA-256 of request body).
- `StargateSecurity` — Spring Security bean that checks `StargateAuthenticationToken` for `@PreAuthorize`.
- `StargateSecurityConfig` — Spring Security filter chain for Stargate-based requests.
- `MultiReadRequestWrapper` — wraps `HttpServletRequest` to allow the body to be read multiple times (needed for payload hash validation).

### 3. 📋 Abstract Base Controllers (`service/controller/`)

Three abstract base classes that downstream services extend:

| Class | Auth Type | PreAuthorize Bean |
|-------|-----------|-------------------|
| `BaseController` | HTTP Basic | `@basicSecurity.hasAuthScope` |
| `OAuthBaseController` | OAuth2 | `@authorizationSecurity.hasOAuthScope` |
| `StargateBaseController` | Stargate JWT | `@stargateSecurity.hasAuthScope` |

All three provide:
- `returnResponse(HttpStatus, Object)` — standardized HTTP response builder.
- `allowedInitBinder()` — prevents binding of disallowed fields.

`BaseController` additionally:
- Reads service version from JAR manifest.
- Builds `serviceProvider` string from system properties (server name, hostname, instance).
- Has `getServiceSiteCd()` — queries the APS DB (`Aps0036t`) to determine the site/platform code.

### 4. 🧩 SOAP-to-REST Wrapper (`service/soapWrap/SoapBaseWrap`)
- Abstract base class for controllers that **wrap legacy SOAP services** and expose them as REST.
- Mirrors `BaseController` functionality but for SOAP wrapping use cases.
- Uses the same Basic Auth `@PreAuthorize`.

### 5. ⚠️ Exception Handling (`common/exception/`)
- `APRestException` — custom checked exception for REST business errors.
- `APErrorResponse` — standard error response body (errorCode + message).
- `ExceptionControllerAdvice` — global `@ControllerAdvice` that:
  - Handles `APRestException` → HTTP 400 Bad Request.
  - Handles all other `Exception` → HTTP 500 Internal Server Error.
- `ApResponseErrorHandler` — custom `ResponseErrorHandler` for `RestTemplate`.
- `RestUtil` — utility to check if an HTTP status code is an error.

### 6. 📖 OpenAPI / Swagger (`config/openapi/`)
- `OpenAPIConfiguration` — configures SpringDoc OpenAPI UI.
  - Supports **two auth modes** via `swagger-auth` property:
    - `bearer` → adds `bearerAuth` JWT scheme to Swagger UI.
    - `basic` (default) → adds HTTP Basic Auth scheme.
  - Sets tags sorted alphabetically, all collapsed by default.

### 7. ⏰ Task/Job Configuration (`com.optum.ap.task.config/`)
- `ApTaskConfig` — reads scheduled task definitions from YAML (`ap.tasks.*`).
- `ApTaskDetail` — holds task metadata: cron expression, enabled flag, custom properties map.
- `JobConfigUtil` — utility to format duration as "X min Y sec" strings for job execution logging.

---

## 🔗 External Dependencies

| Dependency | Purpose |
|-----------|---------|
| `spring-boot-starter-web` | REST web layer |
| `spring-boot-starter-security` | Spring Security |
| `spring-boot-starter-actuator` | Health/metrics endpoints |
| `spring-boot-starter-data-jpa` | JPA/Hibernate support |
| `spring-security-oauth2` | OAuth2 token support |
| `com.auth0:java-jwt` | JWT decoding/verification for Stargate |
| `springdoc-openapi-starter-webmvc-ui` | Swagger UI |
| `micrometer-registry-prometheus` | Prometheus metrics |
| `log4j2` (full stack) | Logging framework |
| `groovy-jsr223` | Groovy scripting for Log4j2 |
| `tomcat-jdbc` + `spring-boot-starter-tomcat` | Embedded Tomcat + JDBC pool |
| `com.optum.tpa:tpa-ap-common-service-SB3` | Optum TPA common services (config, ConfigurationBuilder) |
| `com.optum.tpa:tpa-ap-domain-aps-SB3` | Optum APS domain entities (e.g., `Aps0036t`) |

---

## 🎯 Summary — What This Project Does

> **`oap-dev-ap-rest-common` is Optum AP's shared REST framework library.** Any AP microservice that exposes REST endpoints extends this library to get — out of the box — multi-database JPA wiring, three security models (Basic/OAuth/Stargate-JWT), standardized exception handling, Swagger UI, scheduled task config support, and SOAP wrapping capabilities. Services just configure which databases/auth mode they need in YAML and extend the appropriate base controller.
