# 📦 `oap-dev-ap-rest-common` — Full Project Summary (main branch)

---

## 🔍 What Is This Project?

**`ap-rest-common`** is a **shared reusable JAR library** — NOT a standalone application.  
It is the **common REST framework** for Optum's AP (Administrative Platform) microservices.  
Any AP REST service includes this as a Maven dependency and instantly gets pre-built security, database wiring, exception handling, logging, and Swagger UI.

---

## 🏷️ Project Identity

| Field | Value |
|-------|-------|
| **Artifact ID** | `ap-rest-common` |
| **Group ID** | `com.optum.ap.services.rest.common` |
| **Version** | `4.0.0-SNAPSHOT` |
| **Java** | 21 |
| **Spring Boot** | 4.x (via `ap-parent-pom [4.0.1, 4.99.99)`) |
| **Packaging** | JAR (shared library) |
| **AIDE / Project** | `AIDE_0076365` |

> ⬆️ **Upgraded from previous branch:** Java 17 → **21**, Spring Boot 3.x → **4.x**, version `0.0.6` → `4.0.0`

---

## 📁 Complete Package Structure

```
src/main/java/
│
├── com.optum.ap.services.rest.common/
│   ├── config/
│   │   ├── db/              → 25+ multi-database JPA configurations
│   │   │   ├── AaaOAPDBConfig.java     (base helper — NEW refactored)
│   │   │   ├── AosDBConfig.java
│   │   │   ├── ApsDBConfig.java        (primary DB)
│   │   │   ├── AtfDBConfig.java
│   │   │   ├── BassDBConfig.java
│   │   │   ├── BosDBConfig.java
│   │   │   ├── CaiDBConfig.java
│   │   │   ├── CdhDBConfig.java
│   │   │   ├── CmsDBConfig.java
│   │   │   ├── CpdDBConfig.java        (NEW — OPD2 domain)
│   │   │   ├── CtiDBConfig.java
│   │   │   ├── EmsDBConfig.java
│   │   │   ├── EmsRptDBConfig.java
│   │   │   ├── EobDBConfig.java
│   │   │   ├── FhmDBConfig.java
│   │   │   ├── GpdDBConfig.java        (NEW — OPD3 domain)
│   │   │   ├── IntappDBConfig.java     (NEW — INTAPP domain)
│   │   │   ├── IproDBConfig.java
│   │   │   ├── IvrDBConfig.java
│   │   │   ├── JobDBConfig.java        (NEW — JOB domain)
│   │   │   ├── MsrDBConfig.java
│   │   │   ├── OdsDBConfig.java
│   │   │   ├── OpdDBConfig.java
│   │   │   ├── PhrDBConfig.java        (NEW — PHR domain)
│   │   │   ├── QualityDBConfig.java
│   │   │   ├── RMDDBConfig.java        (NEW — RMD domain)
│   │   │   ├── SlcDBConfig.java
│   │   │   ├── SwaDBConfig.java
│   │   │   ├── TpaDBConfig.java
│   │   │   ├── VADBConfig.java
│   │   │   ├── WdtDBConfig.java
│   │   │   ├── WebDBConfig.java
│   │   │   ├── DBConfigBeanHelper.java
│   │   │   ├── DBPresentCondition.java
│   │   │   ├── DBPresentConditionController.java
│   │   │   ├── ConfigurationPropYaml.java
│   │   │   ├── ContextHelper.java
│   │   │   ├── ServerDBConfig.java
│   │   │   ├── ServerDBPresentCondition.java
│   │   │   └── dto/
│   │   │       ├── APSecretsList.java
│   │   │       ├── DBDefinition.java
│   │   │       └── DbSettings.java
│   │   └── openapi/
│   │       └── OpenAPIConfiguration.java
│   │
│   ├── exception/
│   │   ├── APErrorResponse.java
│   │   ├── ApResponseErrorHandler.java
│   │   ├── APRestException.java
│   │   ├── ExceptionControllerAdvice.java
│   │   └── RestUtil.java
│   │
│   └── security/
│       ├── APSecurityBasicAuthEntryPoint.java
│       ├── basic/
│       │   ├── APSecurityConfig.java
│       │   ├── BasicSecurity.java
│       │   └── ConfigPropSecurity.java
│       ├── dto/
│       │   ├── AuthConfigProperties.java
│       │   ├── AuthUser.java
│       │   ├── OAuthConfigProp.java
│       │   ├── OAuthUser.java
│       │   ├── StargateAuthUser.java
│       │   └── StargateConfigProperties.java
│       ├── filters/
│       │   └── LoggingFilter.java          ← NEW
│       └── stargate/
│           ├── MultiReadRequestWrapper.java
│           ├── StargateAuthenticationToken.java
│           ├── StargateBeanFactory.java
│           ├── StargateJWTValidator.java
│           ├── StargateRequestFilter.java
│           ├── StargateSecurity.java
│           └── StargateSecurityConfig.java
│
├── com.optum.ap.services.rest.service/
│   ├── controller/
│   │   ├── BaseController.java
│   │   ├── OAuthBaseController.java
│   │   └── StargateBaseController.java
│   └── soapWrap/
│       └── SoapBaseWrap.java
│
└── com.optum.ap.task.config/
    ├── ApTaskConfig.java
    ├── ApTaskDetail.java
    └── JobConfigUtil.java
```

---

## 🔧 Module-by-Module Breakdown

---

### 1. 🗄️ Multi-Database Configuration (`config/db/`)

The **biggest and most important** feature of this library.

#### 🆕 Key Architectural Change: `AaaOAPDBConfig` (Base DB Helper)
In this version, a **shared abstract base class** `AaaOAPDBConfig` was introduced that all DB configs now delegate to:
```
AaaOAPDBConfig.setDataSource(name)         → JNDI lookup + JNDI bind
AaaOAPDBConfig.entityManagerFactory(...)   → Hibernate JPA entity manager
AaaOAPDBConfig.transactionManager(...)     → JPA transaction manager
```
This eliminates the old copy-paste boilerplate in every DB config class.

#### All Supported Databases (25+):
| DB Key | Bean Prefix | Domain Packages Scanned |
|--------|-------------|------------------------|
| **APS** ⭐ Primary | `aps` | `com.tpa.ap.domain.aps`, CDH claims |
| TPA | `tpa` | TPA, PSS, KEY, WWW domains |
| AOS | `aos` | AOS domain |
| ATF | `atf` | ATF domain |
| BASS | `bass` | BASS domain |
| BOS | `bos` | BOS domain |
| CAI | `cai` | CAI domain |
| CDH | `cdh` | CDH domain |
| CMS | `cms` | CMS domain |
| **CPD** 🆕 | `cpd` | `com.tpa.ap.domain.opd2` |
| CTI | `cti` | CTI domain |
| EMS | `ems` | EMS domain |
| EMS_RPT | `emsRpt` | EMS reporting |
| EOB | `eob` | EOB domain |
| FHM | `fhm` | FHM domain |
| **GPD** 🆕 | `gpd` | `com.tpa.ap.domain.opd3` |
| **INTAPP** 🆕 | `intapp` | `com.tpa.ap.domain.intapp.entity` |
| IPRO | `ipro` | IPRO domain |
| IVR | `ivr` | IVR domain |
| **JOB** 🆕 | `job` | `com.tpa.ap.domain.job` |
| MSR | `msr` | MSR domain |
| ODS | `ods` | ODS domain |
| OPD | `opd` | OPD domain |
| **PHR** 🆕 | `phr` | `com.ap.domain.phr` |
| Quality | `quality` | Quality domain |
| **RMD** 🆕 | `rmd` | `com.optum.ap.domain.rmd.entity` |
| SLC | `slc` | SLC domain |
| SWA | `swa` | SWA domain |
| VA | `va` | VA domain |
| WDT | `wdt` | WDT domain |
| WEB | `web` | WEB domain |

#### How Conditional Loading Works:
1. App sets `databaseList: APS,TPA,CDH` in YAML.
2. `DBPresentCondition` checks if the DB name is in that list AND its datasource config exists.
3. Only matching DB beans are created — others are skipped entirely.
4. `DBConfigBeanHelper` marks APS beans as `@Primary` automatically.

---

### 2. 🔐 Security — Three Authentication Models

#### a) HTTP Basic Auth (`security/basic/`)
- `APSecurityConfig` — Spring Security filter chain:
  - Whitelists: Swagger UI, `/actuator/**`, `/health`, `/monitor`, `/v3/api-docs/**`
  - Role-based access: roles loaded from YAML (`ap-auth.roles`)
  - Custom `APSecurityBasicAuthEntryPoint` for clean 401 responses
  - Smart `BasicRequestMatcher` to selectively apply Basic Auth prompts
- `BasicSecurity` — `@PreAuthorize` evaluator bean (`@basicSecurity.hasAuthScope`)
- `ConfigPropSecurity` — reads allowed role list from config properties

#### b) OAuth2 (`dto/OAuthConfigProp`, `OAuthBaseController`)
- Users/scopes configured via YAML (`ap-oauth.users`)
- `OAuthBaseController` abstract base for OAuth-protected controllers
- Uses `@PreAuthorize("@authorizationSecurity.hasOAuthScope(authentication)")`

#### c) Stargate API Gateway JWT (`security/stargate/`)
- `StargateRequestFilter` — intercepts all requests:
  - Reads `JWT` header (injected by Optum's Stargate gateway)
  - Skips Basic Auth requests (no JWT header + `Authorization: Basic`)
- `StargateJWTValidator` — full JWT validation pipeline:
  1. Decodes JWT → extracts embedded X.509 certificate (`x5c` header)
  2. Validates cert against truststore
  3. Verifies RSA256 JWT signature using cert's public key
  4. SHA-256 hashes request body and compares with `payloadhash` JWT claim
- `MultiReadRequestWrapper` — allows HTTP request body to be read twice (once for hashing, once for business logic)
- `StargateSecurity` — `@PreAuthorize` evaluator bean + role checking
- `StargateSecurityConfig` — Spring Security filter chain for Stargate requests

#### 🆕 d) Logging Filter (`security/filters/LoggingFilter.java`)
- **Brand new** dedicated servlet filter applied to `/ap/rest/data/*`
- Runs at highest priority (`@Order(Integer.MIN_VALUE)`)
- Detects `optum-cid-ext` header (Optum correlation/tracking ID from Stargate)
- **Logs a warning if any request takes ≥ 5 seconds**
- Provides performance monitoring out-of-the-box for all AP REST services

---

### 3. 📋 Abstract Base Controllers (`service/controller/`)

Three abstract base classes — downstream services simply `extend` one:

| Class | Auth Model | `@PreAuthorize` Bean |
|-------|-----------|----------------------|
| `BaseController` | HTTP Basic | `@basicSecurity.hasAuthScope` |
| `OAuthBaseController` | OAuth2 | `@authorizationSecurity.hasOAuthScope` |
| `StargateBaseController` | Stargate JWT | `@stargateSecurity.hasAuthScope` |

All provide:
- `returnResponse(HttpStatus, Object)` — standardized HTTP response builder
- `allowedInitBinder()` — blocks dangerous field binding

`BaseController` additionally:
- Reads **service version** from JAR manifest
- Builds a **serviceProvider** string (implementation + hostname + instance)
- `getServiceSiteCd()` — queries APS DB (`Aps0036t`) to identify the site/platform code for multi-site deployments

---

### 4. 🧩 SOAP-to-REST Wrapper (`service/soapWrap/SoapBaseWrap`)
- Abstract base for controllers that **wrap legacy SOAP/EJB services** as REST APIs
- Mirrors `BaseController` but targeted at SOAP wrapping patterns
- Same Basic Auth `@PreAuthorize` and `returnResponse()` pattern

---

### 5. ⚠️ Exception Handling (`common/exception/`)
| Class | Role |
|-------|------|
| `APRestException` | Custom checked exception for business errors |
| `APErrorResponse` | Standard JSON error body `{errorCode, message}` |
| `ExceptionControllerAdvice` | Global `@ControllerAdvice` — `APRestException` → 400, all others → 500 |
| `ApResponseErrorHandler` | Custom `RestTemplate` error handler |
| `RestUtil` | Utility: `isError(HttpStatusCode)` |

---

### 6. 📖 OpenAPI / Swagger UI (`config/openapi/`)
- Auto-configures SpringDoc OpenAPI UI
- Configurable via `swagger-auth` property:
  - `bearer` → JWT Bearer token scheme in Swagger UI
  - `basic` (default) → HTTP Basic Auth scheme in Swagger UI
- Tags sorted alphabetically, all sections collapsed by default

---

### 7. ⏰ Scheduled Task/Job Config (`com.optum.ap.task.config/`)
| Class | Role |
|-------|------|
| `ApTaskConfig` | Reads task map from YAML (`ap.tasks.*`) |
| `ApTaskDetail` | Holds per-task: `cronExp`, `jobEnabled`, `taskProps` map |
| `JobConfigUtil` | Utility: formats duration as `"X min Y sec"` for job run logging |

---

## 🔗 Key Dependencies

| Dependency | Purpose |
|-----------|---------|
| `spring-boot-starter-web` | REST layer |
| `spring-boot-starter-security` | Spring Security |
| `spring-boot-starter-actuator` | Health & metrics |
| `spring-boot-starter-data-jpa` | JPA/Hibernate |
| `spring-security-oauth2` | OAuth2 token support |
| `com.auth0:java-jwt` | Stargate JWT decode & verify |
| `springdoc-openapi-starter-webmvc-ui` | Swagger UI |
| `micrometer-registry-prometheus` | Prometheus metrics |
| `log4j2` (full stack) | Logging |
| `tomcat-jdbc` + `spring-boot-starter-tomcat` | Embedded Tomcat |
| `tpa-ap-common-service` | Optum TPA common utilities (StringUtil, ConfigurationBuilder) |
| `tpa-ap-domain-aps-SB3` `v3.3.0` | APS JPA entities (Aps0036t, etc.) |

---

## 🆕 What Changed vs. Previous Branch (SB3 `0.0.6`)

| Area | Before | After (main `4.0.0`) |
|------|--------|----------------------|
| Java version | 17 | **21** |
| Spring Boot | 3.x | **4.x** |
| DB config pattern | Copy-paste boilerplate per class | **Refactored via `AaaOAPDBConfig` base helper** |
| Number of DB configs | 20 | **25+ (CPD, GPD, INTAPP, JOB, PHR, RMD added)** |
| Logging filter | None | **`LoggingFilter` — 5s response time alerting** |
| Artifact ID | `ap-rest-common-SB3` | `ap-rest-common` |
| Dependency versions | Hardcoded in pom | **Version properties from parent POM** |

---

## 🎯 One-Line Summary

> **`oap-dev-ap-rest-common` is Optum AP's shared REST infrastructure library (Java 21 / Spring Boot 4) — it gives any AP microservice plug-and-play support for 25+ database connections, three security models (Basic Auth / OAuth2 / Stargate JWT), automatic 5-second response-time alerting, standardized exception handling, and Swagger UI — all configured purely through YAML.**
