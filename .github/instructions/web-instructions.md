# Web Projects Instructions

This file documents standards, patterns and checklists for **Spring MVC web projects** in this workspace. Use it as the canonical developer reference when creating, modifying or reviewing web UI modules.

> **Note:** Some repositories contain legacy Struts artifacts (struts.xml, struts-*.dtd, .tld files). Struts is **deprecated and NOT supported** for new development. Existing Struts files may remain for migration/backwards-compatibility only. It is acceptable for Spring MVC controllers to be named with the "Action" suffix (e.g., `MemberSearchAction`, `BenefitStartupAction`) to match historical naming conventions — but they **MUST** be implemented with Spring MVC (`@Controller` / `@RestController`), not Struts.

---

## 1. Purpose & Scope

- Covers Spring MVC web apps, JSP views, static assets and web filters found across repositories
- Applies to all tpa/ap and com/optum web modules in the workspace
- Legacy Struts artifacts are tolerated temporarily but **MUST NOT** be extended or used for new flows
- Web layer architecture: Controllers → Managers → ServiceClients → Backend REST Services
- Web layer is a **thin UI facade** — all database operations delegated to backend REST services

---

## 2. Key Patterns (Observed & Required)

### 2.1 Architecture Patterns

- **Spring Boot entrypoints:** Classes named `*WebApplication.java` (e.g., `BenefitsAPWebApplication.java`, `APOpsCorrWebApplication.java`)
- **Controllers:** Spring `@Controller` with handler methods returning view names or `@RestController` for JSON responses
- **Historical naming:** Action suffix allowed on Spring controllers for naming consistency (e.g., `BenefitStartupAction`, `IncentivesAction`)
- **Manager layer:** Intermediary between controllers and services (e.g., `MemberServiceManager.java`) — orchestrates business logic
- **ServiceClient layer:** Calls backend REST services (e.g., `MemberServiceClient.getMemberByKey(req)`)
- **No JPA in web layer:** Web modules must NOT contain direct database access code

### 2.2 View & UI Patterns

- Views under `src/main/webapp/WEB-INF/views/{feature}/...` — JSPs, mobile/responsive variants
- Responsive views: `*Responsive.jsp`, `mobile/` folder structure
- Tiles configuration: `WEB-INF/tiles/` or inline tiles definitions
- Static assets: `images/`, `scripts/`, `styles/` or `css/` folders
- i18n via `messages.properties` and domain-specific property files
- **View technology:** Follow existing project's current technology (JSP primary)
- **No new frameworks:** Do NOT introduce new view frameworks (Thymeleaf, etc.) without a migration plan

### 2.3 Configuration Patterns

- `application.yaml` or `application.properties` for Spring configuration
- Profile-specific overrides: `application-{profile}.yaml` (dev, qa, prod, local)
- Domain-specific config files under `src/main/resources/config/` (e.g., `tpa.ap.benefit.web.properties`)
- **CRITICAL:** Encryption keys, secrets **NOT** committed to repo (use environment variables)
- Property file examples: `caseconverter.properties`, `messages.properties`

### 2.4 Logging & Error Handling

- Logger per-class: `private static final Logger logger = Logger.getLogger(ClassName.class);`
- Error pages under `WEB-INF/error/` (e.g., `errorPage.jsp`, `unknownAppFailure.jsp`)
- Some modules include `GlobalErrorPageRegistrar.java` for centralized error mapping
- Session filters and CSRF handling via Spring Security filters

### 2.5 Security Patterns

- OIDC/OAuth flows: Login JSPs (oidcLoginView.jsp, OptumId.jsp, etc.)
- Session management: `SessionFilter.java`, session timeout dialogs
- CSRF tokens and Spring Security chain
- Session expiry: `sessionExpired.jsp`, `sessionAboutToExpireDialog.jsp`
- User authentication: domain/login variants, account activation

### 2.6 Data Access Pattern (WEB LAYER ONLY)

- **STRICT RULE:** Web modules **MUST NOT** perform direct DB access (no JPA/EntityManager/Repositories/JDBC)
- All database operations **MUST** be delegated to backend REST services
- Calls backend services via `ServiceClient` pattern (inherited from client libraries)
- Request/Response DTOs provided by `ap-{DOMAIN}-client` libraries
- Web layer handles caching via `Cache.getInstance()` for idempotent reads

---

## 3. Project Layout (Canonical)

```
src/main/java/
├── tpa/ap/{domain}/web/
│   ├── {DomainName}WebApplication.java        # Spring Boot entrypoint
│   ├── actions/                               # Spring Controllers (Action suffix allowed)
│   │   ├── BenefitStartupAction.java
│   │   ├── IncentivesAction.java
│   │   └── MemberSearchAction.java
│   ├── manager/
│   │   ├── MemberServiceManager.java
│   │   └── BenefitServiceManager.java
│   └── util/
│       ├── CommonUtility.java
│       └── EnvConfig.java

src/main/resources/
├── application.yaml                           # Default Spring config
├── application-{profile}.yaml                 # Profile overrides (dev, qa, prod)
├── messages.properties                        # i18n messages
├── config/
│   └── {domain}.web.properties                # Domain-specific config
└── caseconverter.properties                   # Domain-specific mappings

src/main/webapp/
├── META-INF/
│   └── MANIFEST.MF
├── WEB-INF/
│   ├── views/
│   │   ├── {feature}/
│   │   │   ├── {feature}.jsp
│   │   │   └── {feature}Responsive.jsp
│   │   ├── error/
│   │   │   ├── errorPage.jsp
│   │   │   └── unknownAppFailure.jsp
│   │   ├── login/
│   │   │   ├── oidcLoginView.jsp
│   │   │   └── OptumId.jsp
│   │   └── mobile/
│   │       └── {feature}/
│   │           └── {feature}.jsp
│   ├── struts/                                # Legacy (DEPRECATED - do not extend)
│   │   └── *.xml
│   └── tiles/
│       └── tiles.xml
├── images/
├── scripts/                                   # JavaScript
│   ├── additional-methods.js
│   ├── form-*.js
│   └── u4me/
│       └── *.js
└── styles/ (or css/)
    ├── {domain}Base.css
    └── *.css

test/
└── java/
    └── tpa/ap/{domain}/web/
        ├── actions/
        └── manager/
```

---

## 4. Coding & Structural Conventions

### 4.1 Controllers (Spring MVC)

**Requirements:**
- Annotate with `@Controller` for view-based, `@RestController` for JSON API responses
- Method names: handler method or `@RequestMapping`-compatible names
- **IMPORTANT:** `@InitBinder` present in controllers that accept form input to prevent mass assignment
- Delegate all business logic to manager/service layer
- Keep controllers thin — validation and orchestration only
- Use typed request/response objects (DTOs from client libraries)
- Validate inputs before calling managers; fail fast with meaningful error messages
- Logging: DEBUG entry/exit, INFO success, ERROR exceptions
- Catch exceptions and forward to error views or return error response

**Example:**
```java
@Controller
@RequestMapping("/benefits")
public class BenefitStartupAction {  // Action suffix allowed for historical naming
    
    @Autowired
    private MemberServiceManager memberManager;
    
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields((String[]) null);  // Prevent mass assignment
    }
    
    @RequestMapping(value = "/view", method = RequestMethod.GET)
    public String getMedicalSummaryView(Model model) {
        try {
            // Delegate to manager
            BenefitData data = memberManager.getMemberBenefits(memberId);
            model.addAttribute("benefits", data);
            return "benefits/view";  // JSP view name
        } catch (Exception e) {
            logger.error("Error retrieving benefits", e);
            return "error/errorPage";
        }
    }
}
```

### 4.2 Manager Layer

**Responsibilities:**
- Orchestrate business workflows
- Call multiple service clients as needed
- Coordinate caching (cache-aside pattern)
- Validate request parameters early
- Map backend errors to user-friendly messages
- Log business events (DEBUG entry/exit, INFO success)

**Requirements:**
- Annotate with `@Component`, `@Service`, or use explicit bean registration
- Extend or implement manager interfaces where applicable
- Throw checked/unchecked exceptions (catch and wrap backend exceptions)
- Use explicit naming: `*Manager` or `*ServiceManager`
- **CRITICAL:** Delegate all database operations to backend REST services (NO direct DB access)
- Do NOT use JPA, EntityManager, repositories, or JDBC connections

**Example:**
```java
@Component
public class MemberServiceManager {
    
    private static final Logger logger = Logger.getLogger(MemberServiceManager.class);
    private static final String CACHE_NAMESPACE = "memberServiceCache";
    
    @Autowired
    private MemberServiceClient memberClient;
    
    public MemberData getMemberByKey(String loginId, String memberId) throws Exception {
        logger.debug("Entering getMemberByKey for memberId: " + memberId);
        
        // Validate early
        if (StringUtil.isNullOrEmpty(memberId)) {
            throw new IllegalArgumentException("memberId cannot be null");
        }
        
        try {
            // Build request (DTO from client library)
            MemberByKeyRequest req = new MemberByKeyRequest();
            req.setLoginId(loginId);
            req.setMemberId(memberId);
            
            // Call backend service (NOT database)
            MemberByKeyResponse response = memberClient.getMemberByKey(req);
            
            logger.info("Successfully retrieved member for memberId: " + memberId);
            return response.getMemberData();
            
        } catch (ServiceException e) {
            logger.error("Service error retrieving member for memberId: " + memberId, e);
            throw new Exception("Failed to retrieve member data", e);
        }
    }
}
```

### 4.3 ServiceClient Integration (from client libraries)

**Web layer receives:**
- `MemberServiceClient`, `BenefitServiceClient`, etc. from `ap-{domain}-client` libraries
- Already implemented singleton pattern
- Request/Response DTOs with proper base classes

**Web layer usage:**
```java
// Inject the client (from client library)
@Autowired
private MemberServiceClient memberClient;

// Call backend service
MemberByKeyResponse response = memberClient.getMemberByKey(memberRequest);
```

**Key Rules:**
- Do NOT create custom clients in web layer
- Use clients from `ap-{domain}-client` dependencies
- Handle `ServiceException` and wrap for UI
- Cache responses at manager layer (see section 6)

---

## 5. Views & JSP Conventions

### 5.1 View File Organization

- JSPs under `src/main/webapp/WEB-INF/views/{feature}/{feature}.jsp`
- Responsive variants: `{feature}Responsive.jsp` or under `mobile/` folder
- Error views: `WEB-INF/error/errorPage.jsp`, `unknownAppFailure.jsp`
- Tiles/layouts: `WEB-INF/views/layouts/*.jsp`
- Follow existing project's view technology patterns
- Progressive enhancement: ensure core functionality works without JavaScript

### 5.2 View Technology Rules

**DO NOT introduce new view frameworks without a migration plan**
- Reuse existing view patterns and technologies in each module
- Follow project's current technology (JSP primary in most modules)
- Avoid inline styles; use CSS classes
- Progressive enhancement: ensure forms work without JavaScript where possible

### 5.3 Accessibility & Responsive Design

- Provide responsive variants (desktop and mobile)
- Use semantic HTML and ARIA attributes
- Test with screen readers (at least manual spot-checks)
- Mobile-first or mobile variants for all major views
- Ensure forms are keyboard-navigable

### 5.4 i18n in Views

- Use message bundles: `messages.properties` and profile-specific overrides
- Use existing framework conventions for message resolution (Spring, Struts taglibs, etc.)
- Define all user-facing messages in bundles, not hardcoded in JSP
- Support multiple locales if configured

---

## 6. Configuration & Profiles

### 6.1 application.yaml Structure

```yaml
# application.yaml (default/development)
server:
  port: 8080
  servlet:
    context-path: /ap-{domain}-web

spring:
  application:
    name: ap-{domain}-web
  mvc:
    view:
      prefix: /WEB-INF/views/
      suffix: .jsp

# Logging (DEVELOPMENT - verbose)
logging:
  level:
    root: WARN
    com.tpa.ap: DEBUG                # ← Change to INFO in production
    tpa.ap: DEBUG                    # ← Change to INFO in production
    org.springframework: INFO
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"

# Caching
cache:
  enabled: true
  ttl: 3600                          # 1 hour default
  max-size: 10000

# External service URLs (development defaults)
services:
  data-backend: ${DATA_SERVICE_URL:http://localhost:8081/ap-data-service-aos-rest}
  security-backend: ${SECURITY_SERVICE_URL:http://localhost:8084/ap-security-service-rest}
  transaction-backend: ${TRANS_SERVICE_URL:http://localhost:8082/ap-transaction-service-rest}

# Session configuration
server:
  servlet:
    session:
      timeout: 30m
      cookie:
        name: JSESSIONID
        http-only: true
        secure: false              # true in production
```

```yaml
# application-prod.yaml (production overrides)
logging:
  level:
    com.tpa.ap: INFO               # Less verbose
    tpa.ap: INFO
    org.springframework: WARN

cache:
  ttl: 7200                        # 2 hours
  max-size: 50000

server:
  ssl:
    enabled: true
    key-store: classpath:keystore.jks
    key-store-password: ${KEYSTORE_PASSWORD}

# Use production endpoints
services:
  data-backend: https://prod-data-api.example.com/ap-data-service-aos-rest
  security-backend: https://prod-security-api.example.com/ap-security-service-rest
  transaction-backend: https://prod-trans-api.example.com/ap-transaction-service-rest
```

### 6.2 Property File Rules

- [ ] NO secrets in version control (use environment variables)
- [ ] Environment-specific files: `application-{profile}.yaml`
- [ ] Document all required environment variables in README
- [ ] Use sensible defaults for local development
- [ ] Document profile activation in deployment guides

---

## 7. Security & Filters

### 7.1 Web Filter Chain

- OIDC/OAuth login filters present in most modules
- Session management via `SessionFilter.java`
- CSRF token validation for state-changing requests
- Spring Security chain handling auth/authz

### 7.2 Input Validation & Output Encoding

- Validate all incoming parameters in controllers/managers (fail fast)
- Sanitize before logging (NO PII or sensitive data in logs)
- Use OWASP encoding for output in JSPs
- Mask sensitive data: show last 4 digits only for SSN, card numbers

### 7.3 OIDC/Login Flows

- Login JSPs: `oidcLoginView.jsp`, `OptumId.jsp`, domain variants
- Account activation and password reset flows
- Session expiry handling: dialogs before timeout
- Logout flows clear session and redirect to login

### 7.4 Session Security

- Session timeout: typically 30 minutes (configurable in `application.yaml`)
- HttpOnly cookies: prevent JavaScript access to session IDs
- Secure flag in production (HTTPS only)
- Session dialog: "Your session will expire in X minutes"

---

## 8. Backend Data Access (Web Layer Guidance - CRITICAL)

### 8.1 Strict Rule: NO Direct DB Access in Web Layer

**CRITICAL:** Web modules MUST NOT contain:
- ❌ JPA `@Entity` classes or `@Table` annotations
- ❌ `EntityManager` or `@PersistenceContext` injections
- ❌ Spring Data `@Repository` interfaces
- ❌ JDBC `Connection`, `PreparedStatement`, or `ResultSet`
- ❌ Direct DAO implementations
- ❌ Database URL connections or datasource management
- ❌ Hibernate `Session` or `Transaction` management
- ❌ `@Transactional` annotations on web layer classes

**All DB operations belong in backend REST services.**

### 8.2 ServiceClient Pattern (Correct Way)

```java
// ✅ CORRECT - Web layer calls backend services
@Component
public class MemberServiceManager {
    
    @Autowired
    private MemberServiceClient memberClient;  // From ap-data-client library
    
    public MemberData getMember(String memberId) throws Exception {
        // Build request (DTO from client library)
        MemberByKeyRequest req = new MemberByKeyRequest();
        req.setLoginId(getCurrentLoginId());
        req.setMemberId(memberId);
        
        // Call backend service via client (REST call)
        MemberByKeyResponse response = memberClient.getMemberByKey(req);
        
        return response.getMemberData();
    }
}
```

### 8.3 Why NO JPA in Web Layer?

**Reasons:**
1. **Separation of concerns:** Web layer is UI facade; backend services own persistence
2. **Scalability:** Multiple web instances share backend DB service
3. **Maintenance:** Database schema changes only affect backend, not web layer
4. **Testing:** Web layer tests mock service clients, not databases
5. **Deployment:** Web and backend scale independently
6. **Security:** Backend services enforce access control and validation

### 8.4 ServiceClient Configuration (Timeouts & Retries)

Configure in backend REST services (not web layer):
- Connection timeout: 5-10 seconds
- Read timeout: 15-30 seconds
- Retry policy: exponential backoff (1s, 2s, 4s...)
- Circuit breaker: fail fast after 5 consecutive failures

### 8.6 Error Handling from Backend Services

```java
try {
    response = memberClient.getMemberByKey(request);
} catch (ServiceException e) {
    // Backend service error - DO NOT expose internals to user
    logger.error("Backend service error for memberId: " + memberId, e);
    
    // Map to user-friendly message
    String userMessage = "Unable to retrieve member information. Please try again.";
    
    // Return error view or response
    throw new WebLayerException(userMessage, e);
}
```

### 8.7 Troubleshooting Backend Errors

**Error: "Could not open JPA EntityManager for transaction"**
- Source: Backend service (NOT web layer)
- Investigate: Backend service logs, database connectivity
- Web layer: Check ServiceClient configuration (timeout, URL)
- Include correlation IDs when escalating to backend team

---

## 9. Caching & Session

### 9.1 Session Management

- **Store in session:** User context (loginId, roles), non-sensitive UI state
- **DO NOT store:** Large objects, backend response payloads (cache instead)
- **Session timeout:** 30 minutes (configurable)
- **Monitor:** Session pool exhaustion, memory leaks
- **Invalidate:** On logout, session expiry, permission changes

---

## 10. Testing

### 10.1 Unit Test Requirements

- Controllers/actions: Use Spring Test `MockMvc` for request/response
- Managers: Mock service clients, test orchestration logic
- Test package structure mirrors source: `src/test/java/tpa/ap/{domain}/web/`
- Use Mockito for mocking dependencies

### 10.2 Test Example

```java
@RunWith(SpringRunner.class)
@WebMvcTest(BenefitStartupAction.class)
public class BenefitStartupActionTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private MemberServiceManager memberManager;
    
    @Test
    public void testGetMedicalSummaryView_Success() throws Exception {
        // Arrange
        BenefitData mockData = createMockBenefitData();
        when(memberManager.getMemberBenefits(anyString()))
            .thenReturn(mockData);
        
        // Act & Assert
        mockMvc.perform(get("/benefits/view")
                .param("memberId", "12345"))
            .andExpect(status().isOk())
            .andExpect(view().name("benefits/view"))
            .andExpect(model().attributeExists("benefits"));
    }
    
    @Test
    public void testGetMedicalSummaryView_Error() throws Exception {
        // Arrange
        when(memberManager.getMemberBenefits(anyString()))
            .thenThrow(new Exception("Service error"));
        
        // Act & Assert
        mockMvc.perform(get("/benefits/view"))
            .andExpect(status().isOk())
            .andExpect(view().name("error/errorPage"));
    }
}
```

### 10.3 Test Coverage

- Happy path scenarios
- Validation errors (null, empty, invalid input)
- Backend service failures (exception handling)
- Cache hits/misses (if applicable)
- **Minimum coverage:** 100% on critical flows

---

## 11. Troubleshooting (Common Issues)

### 11.1 Backend Service Failures

**Symptom:** "Could not open JPA EntityManager for transaction"

**Root cause:** Backend service database connection issue (NOT web layer)

**Diagnosis:**
1. Check backend service logs
2. Verify database connectivity from backend server
3. Check backend service health endpoint
4. Review correlation IDs in logs (if available)

**Fix:** Escalate to backend team; do NOT attempt fixes in web layer

### 11.2 Service Timeout or Unavailability

**Symptom:** Request hangs or "Service Unavailable" error

**Diagnosis:**
1. Verify backend service is running: `curl http://backend:8081/health`
2. Check network connectivity: `ping backend-server`
3. Review ServiceClient timeout configuration
4. Check for circuit breaker (Resilience4j) activation

**Fix:** Restart backend service, check network, increase timeout if needed

### 11.3 View Not Found (404)

**Symptom:** "Could not find JSP view"

**Diagnosis:**
1. Check JSP file exists at `src/main/webapp/WEB-INF/views/{feature}/{feature}.jsp`
2. Verify view name matches exactly (case-sensitive)
3. Check `application.yaml` for JSP prefix/suffix configuration
4. Rebuild project: `mvn clean compile`

**Fix:** Correct JSP path or view name

### 11.4 Authentication/Session Issues

**Symptom:** User logged out unexpectedly, or login not working

**Diagnosis:**
1. Check session timeout in `application.yaml`
2. Review OIDC/login JSP configuration
3. Check browser cookies (session ID present?)
4. Review security filter chain logs

**Fix:** Extend session timeout, verify login flow, check OIDC config

### 11.5 Cache Not Working

**Symptom:** Same request returns different data, or cache not being hit

**Diagnosis:**
1. Verify cache is enabled in `application.yaml`: `cache.enabled: true`
2. Check cache namespace and key generation
3. Review cache TTL (may have expired)
4. Check if `request.isCacheDisabled()` is set

**Fix:** Enable cache, verify key generation, increase TTL if needed

---

## 12. Documentation & README

### 12.1 Required README Sections

Each web module must include a README with:

```markdown
# {Module Name} Web Application

## Purpose
Brief description of module functionality and who uses it.

## Technology Stack
- Spring Boot {version}
- Spring MVC
- JSP (or other view technology)
- Java {version}


## Backend Service Dependencies
- ap-data-service-aos-rest (benefits, claims, members)
- ap-security-service-rest (authentication, authorization)
- ap-transaction-service-rest (transactions, enrollments)

## Health Checks
- Local: `curl http://localhost:8080/actuator/health` (if exposed)

## Troubleshooting
- **Backend service error:** Check backend logs at `/logs/{service-name}/`
- **View not found:** Verify JSP path matches view name
- **Session expired:** Extend timeout in `application.yaml`

## 13. Struts Legacy Handling & Migration Guidance

### 13.1 DO NOT Extend Struts

**CRITICAL:** Do NOT add new Struts code:
- ❌ New `struts.xml` files
- ❌ New `struts-*.dtd` files
- ❌ New `.tld` taglib files
- ❌ New Struts `Action` classes (not Spring MVC)


### 12.3 Pre-commit Rules (Block New Struts)

Git hooks should fail builds if new Struts artifacts added:

```bash
# check-new-struts.sh
if git diff --cached --name-only | grep -E '(struts\.xml|struts-.*\.dtd)'; then
    echo "ERROR: New Struts artifacts not allowed. Use Spring MVC instead."
    exit 1
fi
```

---

## 13. Pre-commit Checklist (Automated + Manual)

**Execute BEFORE committing code:**

### Automated Checks

```bash
# Step 1: Build and compile
mvn clean compile

# Step 2: Run all unit tests
mvn test

# Step 3: Check for warnings
mvn verify

# Step 4: Optional: Run static analysis
mvn checkstyle:check  # if configured
```

### Manual Verification

- [ ] No secrets in code (search: `password`, `token`, `key`, `credential`)
- [ ] **NO JPA/EntityManager/JDBC code** (search: `@Entity`, `@Repository`, `EntityManager`, `Connection`)
- [ ] @InitBinder present in all controllers accepting `@RequestParam` or form data
- [ ] All logging statements sanitized (no PII/PHI)
- [ ] i18n keys used in views exist in `messages.properties`
- [ ] Response DTOs properly initialized (no null collections)
- [ ] Exception handling: specific exceptions caught, wrapped appropriately
- [ ] Manager layer delegates to ServiceClient (no direct service calls in controller)
- [ ] Views follow existing project patterns
- [ ] Views are responsive and accessible

## 14. Developer Daily Checklist

**Follow this checklist during development:**

### Before Coding

- [ ] Read module README and understand purpose
- [ ] Identify which backend services your feature needs to call
- [ ] Review similar features in the codebase (patterns to follow)
- [ ] Ensure backend services are running locally (or available in dev environment)

### While Coding

**Controllers:**
- [ ] Keep methods < 50 lines; delegate to managers
- [ ] Use `@InitBinder` if accepting user input
- [ ] Validate input, fail fast with meaningful errors
- [ ] Add logging: DEBUG entry/exit, INFO success, ERROR failures

**Managers:**
- [ ] Create `@Component` or `@Service` for manager class
- [ ] Inject `ServiceClient` from client library
- [ ] **NEVER** add JPA, EntityManager, repositories, or JDBC code
- [ ] Wrap backend exceptions in checked/unchecked exceptions
- [ ] Add request validation at method start

**Views:**
- [ ] Follow existing project's view patterns (do not introduce new frameworks)
- [ ] Provide mobile/responsive variant if user-facing
- [ ] Use i18n keys from `messages.properties` for all text
- [ ] Test in multiple browsers (Chrome, Firefox, Safari, Edge)

**Testing:**
- [ ] Write unit tests for controllers (MockMvc)
- [ ] Write unit tests for managers (mock service clients)
- [ ] Aim for 70%+ coverage on critical flows
- [ ] Test happy path + error scenarios

### Before Push

- [ ] Run pre-commit checklist (see section 16)
- [ ] Ensure all tests pass
- [ ] Build succeeds: `mvn clean package`
- [ ] No compiler warnings or errors
- [ ] Review your own code first (self-review)
- [ ] Prepare PR description with context, screenshots, tickets

### After Merge

- [ ] Monitor application logs for errors
- [ ] Test feature in deployed environment
- [ ] Watch for backend service failures or timeouts
- [ ] Respond promptly to code review feedback

---

## 15. Performance & Optimization

### 15.1 Lazy Loading & Pagination

- Load list data in chunks (pagination)
- Defer expensive operations until needed
- Use AJAX for progressive data loading on long lists

### 15.2 Backend Service Optimization

**Web layer should NOT care about DB optimization** — backend services handle this.

- If backend service is slow, escalate to backend team
- Ensure backend service exposes filtering/pagination

### 15.4 Session & Memory Management

- Do not store large objects in session
---

## 16. Logging Best Practices

### 16.1 Logger Setup (REQUIRED)

```java
import org.apache.log4j.Logger;

private static final Logger logger = Logger.getLogger(ClassName.class);
```

### 16.2 Logging Levels

| Level | When | Examples |
|-------|------|----------|
| **DEBUG** | Method flow, cache ops, parameters | Entry/exit, "Entering getMemberBenefits", cache hit/miss |
| **INFO** | Business events, success | "Successfully retrieved benefits for memberId: 12345" |
| **WARN** | Unusual situations, fallbacks | Cache miss with fallback, deprecated API usage |
| **ERROR** | Failures, exceptions | "Error retrieving member data: ServiceException" |

### 16.3 Sensitive Data (NEVER Log)

**❌ NEVER log:**
- Passwords (plain or hashed)
- Social Security Numbers (SSN)
- Credit card numbers
- Full authentication tokens
- Personal Health Information (PHI)
- Personal Identifiable Information (PII)

**✅ Safe to log:**
- User IDs (non-sensitive identifiers)
- Timestamps, status codes, operation names
- Masked data (first/last 4 chars: "SSN: ****8901")
- Operation counts, metrics

### 16.4 Logging Examples

```java
// ✅ CORRECT: Debug with context
logger.debug("Entering getMemberBenefits for memberId: " + memberId);

// ✅ CORRECT: Info for business events
logger.info("Successfully retrieved benefits for " + memberCount + " members");

// ✅ CORRECT: Error with context
logger.error("Service error retrieving benefits for memberId: " + memberId, exception);

// ✅ CORRECT: Masked sensitive data
logger.debug("Member SSN (last 4): " + ssn.substring(ssn.length() - 4));

// ❌ WRONG: Full sensitive data
logger.debug("Member password: " + password);
logger.info("User token: " + fullAuthToken);
logger.error("SSN: " + ssn);
```

---

## 17. Naming & Packaging Conventions

### 17.1 Class Naming

| Type | Pattern | Examples |
|------|---------|----------|
| Controller/Action | `{Feature}Action` (Action suffix allowed) | `BenefitStartupAction`, `MemberSearchAction` |
| Spring @Controller | `{Feature}Controller` | `BenefitController`, `MemberController` |
| Manager | `{Feature}ServiceManager` or `{Feature}Manager` | `MemberServiceManager`, `BenefitManager` |
| Utility | `{Name}Utility` or `{Name}Utils` | `CommonUtility`, `DateUtils` |
| Web Application | `{Domain}{Name}WebApplication` | `BenefitsAPWebApplication`, `APOpsCorrWebApplication` |

### 17.2 Package Structure

```
com.tpa.ap.{domain}.web
├── actions/              # Controllers (Action suffix allowed for historical)
├── manager/              # Business logic orchestrators
├── util/                 # Utility classes
└── {service}/            # Optional: service-specific subpackage
```

### 17.3 View File Naming

- Feature name in lowercase: `benefits.jsp`, `member_search.jsp`
- Responsive variant: `benefitsResponsive.jsp`, or under `mobile/`
- Error views: `errorPage.jsp`, `unknownAppFailure.jsp`
- Layouts: `layout.jsp`, `masterLayout.jsp`

### 17.4 Property File Naming

- Main config: `application.yaml`
- Profile overrides: `application-{profile}.yaml` (prod, qa, dev, local)
- Domain config: `config/tpa.ap.{domain}.web.properties`
- i18n: `messages.properties`, `messages_{locale}.properties`

---

## 18. Configuration Management

### 18.1 Externalize All Secrets

**NEVER commit to repository:**
- Database passwords
- API keys and tokens
- Encryption keys
- OAuth2 client secrets
- SSL keystores

### 18.2 Configuration Validation

- Document all required environment variables in README
- Provide sensible defaults for development
- Fail fast if critical config missing (startup validation)
- Log configuration on application start (without secrets)

---

## 19. Common Anti-Patterns to AVOID

### 19.1 Controllers with Business Logic

❌ **WRONG:**
```java
@Controller
public class BenefitAction {
    @RequestMapping("/view")
    public String view(Model model, String memberId) {
        // Business logic in controller - WRONG
        List<Benefit> benefits = calculateBenefits(memberId);
        model.addAttribute("benefits", benefits);
        return "view";
    }
}
```

✅ **CORRECT:**
```java
@Controller
public class BenefitAction {
    @Autowired
    private BenefitManager benefitManager;
    
    @RequestMapping("/view")
    public String view(Model model, String memberId) {
        // Delegate to manager
        List<Benefit> benefits = benefitManager.getBenefits(memberId);
        model.addAttribute("benefits", benefits);
        return "view";
    }
}
```

### 19.2 Direct Database Access (CRITICAL ANTI-PATTERN)

❌ **WRONG:**
```java
@Component
public class MemberManager {
    @Autowired
    private EntityManager em;  // NO! This is DAO layer pattern
    
    public Member getMember(String id) {
        return em.find(Member.class, id);  // Direct DB access - FORBIDDEN
    }
}
```

❌ **ALSO WRONG:**
```java
@Component
public class MemberManager {
    @Autowired
    private MemberRepository memberRepo;  // NO! Web layer CANNOT use repositories
    
    public Member getMember(String id) {
        return memberRepo.findById(id).orElse(null);  // FORBIDDEN
    }
}
```

✅ **CORRECT:**
```java
@Component
public class MemberManager {
    @Autowired
    private MemberServiceClient memberClient;  // From client library
    
    public Member getMember(String id) {
        MemberRequest req = new MemberRequest();
        req.setMemberId(id);
        MemberResponse resp = MemberServiceClient.getInstance().getMember(req);  // Backend service call
        return resp.getMember();
    }
}
```

### 19.3 Logging Sensitive Data

❌ **WRONG:**
```java
logger.debug("User login: " + username + ", password: " + password);
logger.info("SSN: " + ssn);
logger.error("Auth token: " + token);
```

✅ **CORRECT:**
```java
logger.debug("User login attempt for username: " + username);
logger.info("Member SSN (last 4): " + ssn.substring(ssn.length() - 4));
logger.error("Authentication failed for userId: " + userId);
```

### 19.4 Storing Large Objects in Session

❌ **WRONG:**
```java
session.setAttribute("allBenefits", allBenefitsList);  // Large list in session
```

✅ **CORRECT:**
```java
// Store only minimal user context in session
session.setAttribute("userId", userId);
session.setAttribute("roles", userRoles);
```

### 19.5 Using JPA Annotations in Web Layer

❌ **WRONG:**
```java
@Entity
@Table(name = "members")
public class Member {
    @Id
    private String memberId;
}
```

**This does NOT belong in web layer. Move to backend service.**

---

## 20. Summary & Key Rules

### 20.1 Golden Rules

1. **Controllers are THIN** — delegate to managers, < 50 lines per method
2. **Managers ORCHESTRATE** — call ServiceClients, handle caching, validate early
3. **NO DB access in web layer** — all data access via backend services ONLY
4. **Secure by default** — @InitBinder, sanitize inputs, mask sensitive logs
5. **Test thoroughly** — MockMvc for controllers, mock clients for managers
6. **Document clearly** — README, inline comments, commit messages
7. **Escalate appropriately** — backend issues to backend team, frontend to web team

### 20.2 Pre-Commit Checklist (Quick)

- [ ] `mvn clean compile` passes
- [ ] `mvn test` passes
- [ ] No secrets in code
- [ ] **NO JPA/EntityManager/JDBC code**
- [ ] @InitBinder on controllers accepting input
- [ ] Logging sanitized (no PII/PHI)
- [ ] Tests adequate (70%+ coverage)

### 20.3 When in Doubt

1. **Check existing code** — Look at similar feature in same module
2. **Follow patterns** — Use rest-instructions.md and client-instructions.md as reference
3. **Ask team** — Slack, PR comment, or email
---

## 21. Appendix: Quick Start

### 21.1 Create New Feature (Step-by-Step)

```bash
# Step 1: Understand the feature
# - What data do you need?
# - Which backend service provides it?
# - Should results be cached?

# Step 2: Create manager class
# com/tpa/ap/{domain}/web/manager/{Feature}Manager.java

# Step 3: Create controller/action
# com/tpa/ap/{domain}/web/actions/{Feature}Action.java

# Step 4: Create view(s)
# src/main/webapp/WEB-INF/views/{feature}/{feature}.jsp
# src/main/webapp/WEB-INF/views/{feature}/{feature}Responsive.jsp (if needed)

# Step 5: Add unit tests
# src/test/java/tpa/ap/{domain}/web/actions/{Feature}ActionTest.java
# src/test/java/tpa/ap/{domain}/web/manager/{Feature}ManagerTest.java

# Step 6: Add i18n keys to messages.properties
# feature.title=My Feature Title
# feature.description=My Feature Description

# Step 7: Test locally
mvn clean package
mvn spring-boot:run

# Step 8: Create PR with screenshots and test results
```



**END OF WEB-INSTRUCTIONS**

**Remember:** Web layer is a thin UI facade. Managers orchestrate business flows. ServiceClients call backends (never database). Caching for reads only. Logging is sanitized. Tests are thorough. Follow these patterns, and you'll build maintainable, secure, performant web applications.