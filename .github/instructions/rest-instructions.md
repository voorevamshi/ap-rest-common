# AP REST Service — Generic Instructions

Comprehensive guide for developing REST services across AP repositories (aos-rest, umr-rest, gateway-rest, transaction-rest, token-rest, fax-rest, job-rest, security-rest). Follow these rules to ensure consistent, secure, and performant code aligned with repository patterns and Java industry standards.

---

## 1. Purpose & Scope

**Purpose**: Provide authoritative coding conventions for AP REST services.

**Scope**: 
- Controllers, Services, DAOs, ServiceClients
- Request/Response DTOs and contracts
- Caching, transactions, security, logging, error handling
- Testing, configuration, versioning

**Applied To**: All AP microservices following Spring Boot + Spring Data JPA patterns.

---

## 2. Core Dos & Don'ts (MANDATORY)

### 2.1 HTTP Endpoints — POST-ONLY Pattern

**✅ DO**:
- Use `@PostMapping` for ALL endpoints without exception
- Accept complex request bodies (search criteria, filters, nested objects)
- Return JSON Response DTOs

**❌ DON'T**:
- Use `@GetMapping`, `@PutMapping`, `@DeleteMapping`, `@PatchMapping`
- Use generic `@RequestMapping` without `method = RequestMethod.POST`
- Mix HTTP methods in a single endpoint

**Why**: Observed pattern in 100% of AP repos (aos, umr, gateway, transaction, token). Supports complex payloads, consistent security model, and URL path predictability.

**Example**:
```java
// ✅ CORRECT
@PostMapping(value = "/getBenefits", consumes = "application/json", produces = "application/json")
public Response getBenefits(@RequestBody Request req) throws APRestException { }

// ❌ WRONG
@GetMapping(value = "/getBenefits")
@PutMapping(value = "/updateBenefits")
@DeleteMapping(value = "/deleteBenefits")
```

---

### 2.2 Caching — Mandatory by Default

**✅ DO**:
- Implement Caching (Cache.getInstance()) at service layer for ALL read operations
- Check cache BEFORE external/DB calls
- Store results AFTER successful retrieval
- Respect `request.isCacheDisabled()` per-request bypass
- Set `response.setFromCache(true)` on cache hits

**❌ DON'T**:
- Skip caching implementation without justification
- Use Spring @Cacheable or Redis directly (use @OapCache instead)
- Cache sensitive data (tokens, passwords, PHI)
- Cache write operations

**Why**: Performance optimization, reduced external load, request-level control.

**Developer Exception**: Remove caching only if explicit data-freshness requirement exists (documented).

---

### 2.3 @Transactional — Write Operations Only

**✅ DO**:
- Use `@Transactional("managerName")` for write/atomic DB operations (INSERT/UPDATE/DELETE)
- Specify transaction manager when multiple databases exist
- Use class-level `@Transactional` in DAOs for write-capable persistence

**❌ DON'T**:
- Add `@Transactional` to read-only methods with external service calls
- Omit transaction manager specification (let container guess)
- Use `@Transactional(readOnly = true)` unless atomic multi-step reads required

**Why**: Explicit transaction boundaries, multi-DB clarity, performance (no unnecessary TX overhead).

**Decision Tree**:
```
Does method INSERT/UPDATE/DELETE to database?
├─ YES → Use @Transactional("specificManager")
├─ NO: Calls ServiceClient only?
│  └─ NO @Transactional → Let service call execute unmanaged
└─ NO: Complex multi-read requiring atomicity?
   └─ Rare case → @Transactional(readOnly=true) with manager
```

---

### 2.4 Exception Handling & APRestException

**✅ DO**:
- Catch specific exceptions (ServiceException, DataAccessException, etc.)
- Log context (user ID, operation, database) BEFORE throwing
- Wrap and throw `APRestException` consistently
- Never expose internal stack traces to clients

**❌ DON'T**:
- Return null on error
- Swallow exceptions silently
- Expose full stack traces in error responses
- Use generic `throws Exception`

---

### 2.5 Security — @InitBinder 
**✅ DO**:
- Include `@InitBinder` in EVERY controller

**❌ DON'T**:
- Omit `@InitBinder` (mass assignment vulnerability)

---

## 3. Architecture & Layer Responsibilities

### 3.1 Layered Architecture

```
HTTP Request
    ↓
Controller (HTTP routing, basic validation, delegation)
    ↓
Service (business rules, caching, orchestration, request validation)
    ↓
DAO / ServiceClient (persistence or external I/O)
    ↓
Database / External Service
```

### 3.2 Layer Boundaries

| Layer | Responsibility | NOT Responsible For |
|-------|-----------------|-------------------|
| **Controller** | HTTP binding, request/response mapping, delegation | Business logic, DB access, caching, validation |
| **Service** | Business rules, caching, orchestration, validation | HTTP concerns, persistence details |
| **DAO** | SQL execution, entity mapping, transactions | Business logic, external service calls |
| **ServiceClient** | External service calls, I/O | Business rules, caching (cache at service layer) |

### 3.3 Data Flow

1. **Controller** receives `@RequestBody Request`
2. **Service** validates, checks cache, calls DAO/ServiceClient
3. **DAO/ServiceClient** executes operation
4. **Service** caches result, returns `Response`
5. **Controller** returns `Response` to client

---

## 4. Controller Guidelines (Strict)

### 4.1 Base Requirements

- Annotate with `@RestController`
- Extend `BaseController`
- Class-level `@RequestMapping("/ap/rest/{domain}/{subdomain}/v{version}")`
- Include mandatory `@InitBinder`

### 4.2 Method Structure

```java
@RestController
@RequestMapping("/ap/rest/data/benefit/v1.0")
public class BenefitController extends BaseController {
    
    @Autowired
    private BenefitService benefitService;
    
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields((String[]) null);
    }
    
    @PostMapping(value = "/getBenefits", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public Response getBenefits(@RequestBody Request req) throws APRestException {
        try {
            return benefitService.getBenefits(req);
        } catch (Exception ex) {
            throw new APRestException(ex.getMessage());
        }
    }
}
```

### 4.3 Controller Checklist

- [ ] `@RestController` annotation
- [ ] Extends `BaseController`
- [ ] URL pattern: `/ap/rest/{domain}/{subdomain}/v{version}`
- [ ] `@InitBinder` with `setDisallowedFields((String[]) null)`
- [ ] All methods use `@PostMapping` only
- [ ] Service injected via `@Autowired`
- [ ] Methods delegate to service (no logic)
- [ ] Exceptions caught and wrapped in `APRestException`
- [ ] Logging: DEBUG entry/exit, INFO success, ERROR failures

### 4.4 Versioning in Controllers

Create versioned controllers when API changes (breaking changes):

```java
// Version 1 (original)
@RestController
@RequestMapping("/ap/rest/data/benefit/v1.0")
public class BenefitController extends BaseController { }

// Version 2 (new structure)
@RestController
@RequestMapping("/ap/rest/data/benefit/v2.0")
public class BenefitV2Controller extends BaseController { }

// Version 4 (further evolution)
@RestController
@RequestMapping("/ap/rest/data/benefit/v4.0")
public class BenefitV4Controller extends BaseController { }
```

Keep multiple versions active for backward compatibility.

---

## 5. Service Guidelines (Detailed)

### 5.1 Service Structure

```java
@Service("benefitService")
public class BenefitServiceImpl implements BenefitService {
    
    private static final Logger logger = Logger.getLogger(BenefitServiceImpl.class);
    private static final String CACHE_NAMESPACE = "aosBenefitCache";
    
    @Autowired
    private BenefitDao benefitDao;
    
    @Autowired
    private BenefitServiceClient benefitClient;
    
    // Implementation methods follow...
}
```

### 5.2 Mandatory Caching Pattern

Every read operation must implement cache-aside:

```java
@Override
public Response getBenefits(Request req) throws APRestException {
    logger.debug("Entering getBenefits for memberId: " + req.getMemberId());
    
    // Step 1: Validate request early
    validateBenefitRequest(req);
    
    // Step 2: Check cache (MANDATORY)
    if (!req.isCacheDisabled()) {
        Response cached = (Response) Cache.getInstance()
            .get(CACHE_NAMESPACE, req.getCacheKey());
        
        if (cached != null) {
            cached.setFromCache(true);
            logger.debug("Cache hit for key: " + req.getCacheKey());
            return cached;
        }
    }
    
    // Step 3: Fetch from source
    try {
        Response response = benefitClient.getBenefits(req);
        
        // Step 4: Cache result
        if (!req.isCacheDisabled()) {
            Cache.getInstance().put(CACHE_NAMESPACE, req.getCacheKey(), response);
            logger.debug("Cached result for key: " + req.getCacheKey());
        }
        
        logger.info("Successfully retrieved benefits for memberId: " + req.getMemberId());
        return response;
        
    } catch (ServiceException e) {
        logger.error("Service error retrieving benefits for memberId: " + req.getMemberId(), e);
        throw new APRestException("Failed to retrieve benefits", e);
    }
}

private void validateBenefitRequest(Request req) throws APRestException {
    if (req == null) {
        throw new APRestException("Request cannot be null");
    }
    if (StringUtil.isNullOrEmpty(req.getLoginId())) {
        throw new APRestException("Missing required field: loginId");
    }
}
```

### 5.3 Request Validation Pattern

Validate at the start of every service method:

```java
@Override
public Response processTransaction(Request req) throws APRestException {
    // STEP 1: Validate immediately
    validateProcessTransactionRequest(req);
    
    // STEP 2: Business logic
    // ...
}

private void validateProcessTransactionRequest(Request req) throws APRestException {
    if (req == null) {
        throw new APRestException("Request cannot be null");
    }
    if (StringUtil.isNullOrEmpty(req.getLoginId())) {
        throw new APRestException("Missing required field: loginId");
    }
    if (StringUtil.isNullOrEmpty(req.getTransactionId())) {
        throw new APRestException("Missing required field: transactionId");
    }
    if (req.getStartDate() != null && req.getEndDate() != null && 
        req.getStartDate().after(req.getEndDate())) {
        throw new APRestException("startDate cannot be after endDate");
    }
}
```

**Validation Checklist**:
- [ ] Null object check
- [ ] Empty/null required fields
- [ ] Business rule constraints (dates, ranges, enums)
- [ ] Cross-field validation
- [ ] Throw APRestException with meaningful message

### 5.4 Write Operations with @Transactional

```java
@Override
@Transactional("atfTransactionManager")  // Explicit manager for ATF database
public Response saveTransaction(Request req) throws APRestException {
    logger.debug("Entering saveTransaction");
    
    validateTransactionRequest(req);
    
    try {
        TransactionDto dto = transactionDao.save(req);
        auditDao.log("Transaction saved: " + dto.getId());
        
        logger.info("Successfully saved transaction: " + dto.getId());
        
        Response resp = new Response();
        resp.setSuccess(true);
        resp.setTransactionId(dto.getId());
        return resp;
        
    } catch (DataAccessException e) {
        logger.error("Database error saving transaction", e);
        throw new APRestException("Failed to save transaction", e);
    }
}
```

**Multi-DAO Atomicity**:
```java
@Override
@Transactional("atfTransactionManager")  // Single transaction manager
public Response updateWithAudit(Request req) throws APRestException {
    // Both DAOs share same transaction manager
    transactionDao.update(req);
    auditDao.log("Update: " + req.getId());
    return createSuccessResponse();
}
```

### 5.5 Service Checklist

- [ ] Implements service interface
- [ ] Annotated with `@Service("{beanName}")`
- [ ] Logger: `private static final Logger logger`
- [ ] Caching via `Cache.getInstance()` for reads
- [ ] `CACHE_NAMESPACE` defined as constant
- [ ] Request validation at method start
- [ ] `@Transactional` only on write methods with specified manager
- [ ] All exceptions wrapped in `APRestException`
- [ ] DEBUG logging: entry/exit, cache operations
- [ ] INFO logging: business events, success
- [ ] ERROR logging: exceptions with context

---

## 6. DAO Guidelines

### 6.1 DAO Implementation Pattern

```java
@Repository
@Transactional("aosTransactionManager")  // Specify for write-capable DAOs
public class BenefitDaoImpl implements BenefitDao {
    
    private static final Logger logger = Logger.getLogger(BenefitDaoImpl.class);
    private static final String CACHE_NAMESPACE = "aosBenefitDaoCache";  // Optional
    
    @Autowired
    @Qualifier("aosEntityManager")
    private EntityManager entityManager;
    
    @Override
    public BenefitDto findById(String benefitId) {
        logger.debug("Finding benefit by ID: " + benefitId);
        
        // Optional: Check DAO-level cache for expensive reads
        String cacheKey = "benefit_" + benefitId;
        BenefitDto cached = (BenefitDto) Cache.getInstance()
            .get(CACHE_NAMESPACE, cacheKey);
        if (cached != null) return cached;
        
        // Query database
        BenefitDto dto = entityManager.find(BenefitDto.class, benefitId);
        
        // Optional: Cache DAO result
        if (dto != null) {
            Cache.getInstance().put(CACHE_NAMESPACE, cacheKey, dto);
        }
        return dto;
    }
    
    @Override
    public BenefitDto save(BenefitDto dto) {
        logger.debug("Saving benefit: " + dto.getId());
        entityManager.persist(dto);
        return dto;
    }
    
    @Override
    public BenefitDto update(BenefitDto dto) {
        logger.debug("Updating benefit: " + dto.getId());
        return entityManager.merge(dto);
    }
}
```

### 6.2 DAO Requirements

- [ ] `@Repository` annotation
- [ ] `@Transactional` with appropriate transaction manager at class level
- [ ] `@Qualifier` for EntityManager injection (multi-DB support)
- [ ] Named queries for complex operations
- [ ] DAO-level caching optional for expensive reads
- [ ] DEBUG level logging only
- [ ] No business logic (persistence only)
- [ ] Exceptions propagate (not caught)

### 6.3 EntityManager Injection (Multi-DB)

```java
@Autowired
@Qualifier("aosEntityManager")      // AOS database
private EntityManager aosEm;

@Autowired
@Qualifier("atfEntityManager")      // ATF database
private EntityManager atfEm;

@Autowired
@Qualifier("wdtEntityManager")      // WDT database
private EntityManager wdtEm;

@Autowired
@Qualifier("umrEntityManager")      // UMR database
private EntityManager umrEm;
```

---

## 7. ServiceClient Guidelines

### 7.1 ServiceClient Integration

```java
@Service
public class MyServiceImpl implements MyService {
    
    @Override
    public Response getData(Request req) throws APRestException {
        try {
            Response response = BenefitServiceClient
                .getParameterizedInstance("ap-data-service-aos-rest")
                .getBenefits(req);
            return response;
        } catch (ServiceException e) {
            logger.error("ServiceClient error: " + e.getMessage(), e);
            throw new APRestException("External service call failed", e);
        }
    }
}
```

### 7.2 Common Service Names Registry

Use these standardized service names when calling external AP services:

| Service | Name | Repository | Usage |
|---------|------|------------|-------|
| Data Service AOS | `ap-data-service-aos-rest` | aos-rest | Benefit, claim, member data |
| Data Service UMR | `ap-data-service-umr-rest` | umr-rest | Unified medical record data |
| Gateway Service | `ap-gateway-service-rest` | gateway-rest | API orchestration, auth |
| Transaction Service | `ap-transaction-service-rest` | transaction-rest | Enrollment, workflows |
| Token Service | `ap-token-service-rest` | token-rest | Token generation, validation |
| Security Service | `ap-security-service-rest` | security-rest | Authentication, user roles |
| Job Service | `ap-job-service-rest` | job-rest | Background job processing |
| Fax Service | `ap-fax-service-rest` | fax-rest | Document fax operations |

**Usage**:
```java
// Always use getParameterizedInstance with service name
BenefitServiceClient.getParameterizedInstance("ap-data-service-aos-rest").operation(req);
```

---

## 8. DTO & Contract Rules
**IMPORTANT**: Request DTOs, Response DTOs, and Entity classes are managed by **separate client/data components** and are **OUT OF SCOPE** for REST service implementation.

**REST Service Focus**:
- ✅ Controller layer (HTTP routing)
- ✅ Service layer (business logic, caching, orchestration)
- ✅ DAO layer (persistence operations)
- ❌ Request/Response DTOs (managed by client libraries)
- ❌ Entity classes (managed by data model components)

**DTO Dependencies**:
```xml
<!-- Client library provides Request/Response DTOs -->
<dependency>
    <groupId>com.optum.ap</groupId>
    <artifactId>ap-{service}-client</artifactId>
    <version>${client.version}</version>
</dependency>

<!-- Data library provides Entity/DTO classes -->
<dependency>
    <groupId>com.optum.ap</groupId>
    <artifactId>ap-{service}-data</artifactId>
    <version>${data.version}</version>
</dependency>
```

**Usage Pattern**:
```java
// Import from client library
import com.optum.ap.services.client.dto.ClaimRequest;
import com.optum.ap.services.client.dto.ClaimResponse;

// Import from data library
import com.optum.ap.services.data.entity.ClaimDto;

// Use in REST service
@PostMapping("/getClaims")
public ClaimResponse getClaims(@RequestBody ClaimRequest req) {
    // Request/Response from client lib
    // Entity from data lib
}
```

### 8.1 Request DTO Requirements (Reference Only)

**Note**: Implementation is in client libraries. REST services consume these contracts.

Request DTOs MUST implement (defined in client library):
- `getCacheKey()` method for cache key generation
- `isCacheDisabled()` method for cache bypass control
- `loginId` field for audit trail

**Example Contract** (implemented in client library):
```java
// In ap-{service}-client artifact
public class ClaimRequest extends AbstractRequest {
    private String loginId;
    private String memberId;
    private boolean cacheDisabled;
    
    @Override
    public String getCacheKey() {
        return CacheUtil.buildKey(loginId, memberId);
    }
    
    @Override
    public boolean isCacheDisabled() {
        return cacheDisabled;
    }
}
```

### 8.2 Standard Response DTO Structure (Reference Only)

**Note**: Implementation is in client libraries. REST services populate these contracts.

Response DTOs MUST follow (defined in client library):
- `success` flag (operation success/failure)
- `errorMessage` field (populated on failure)
- `fromCache` flag (cache hit indicator)
- `errorCode` field (machine-readable error code)

**Example Contract** (implemented in client library):
```java
// In ap-{service}-client artifact
public class ClaimResponse extends AbstractResponse {
    private boolean success;
    private String errorMessage;
    private boolean fromCache;
    private String errorCode;
    private List<ClaimDto> claims;  // From data library
    
    @Override
    public void setFromCache(boolean fromCache) {
        this.fromCache = fromCache;
    }
}
```

### 8.3 DTO Usage in REST Services

**REST services ONLY**:
- ✅ Import DTOs from client/data libraries
- ✅ Use DTOs in controller signatures
- ✅ Populate response DTOs in service layer
- ✅ Validate request DTOs
- ❌ Create new DTO classes (use client library)
- ❌ Modify DTO structures (managed separately)

**Response Population Pattern**:
```java
// Service layer populates response from client library
@Override
public ClaimResponse getClaimsByMemberId(ClaimRequest request) {
    // Validate using request from client lib
    validateRequest(request);
    
    // Check cache
    String cacheKey = request.getCacheKey();
    ClaimResponse cached = (ClaimResponse) Cache.getInstance().get(CACHE_NAMESPACE, cacheKey);
    if (cached != null) {
        cached.setFromCache(true);  // Use method from client lib
        return cached;
    }
    
    // Fetch entities from data lib
    List<ClaimDto> claims = claimDao.findClaimsByMemberId(request.getMemberId());
    
    // Populate response from client lib
    ClaimResponse response = new ClaimResponse();
    response.setSuccess(true);
    response.setClaims(claims);  // Entities from data lib
    
    return response;
}
```

### 8.4 DTO Validation Rules (REST Service Responsibility)

REST services MUST validate request DTOs even though structure is external:

```java
private void validateClaimRequest(ClaimRequest request) throws APRestException {
    if (request == null) {
        throw new APRestException("Request cannot be null");
    }
    if (StringUtil.isNullOrEmpty(request.getLoginId())) {
        throw new APRestException("Missing required field: loginId");
    }
    if (StringUtil.isNullOrEmpty(request.getMemberId())) {
        throw new APRestException("Missing required field: memberId");
    }
}
```

**Validation Checklist**:
- [ ] Null object check
- [ ] Empty/null required fields (from client lib contract)
- [ ] Business rule constraints
- [ ] Cross-field validation
- [ ] Throw APRestException with meaningful message

### 8.5 Entity Usage from Data Libraries

**REST services**:
- ✅ Import entity classes from data libraries
- ✅ Use entities in DAO layer
- ✅ Map entities to response DTOs
- ❌ Create new entity classes (use data library)
- ❌ Modify entity mappings (managed in data library)

**Example**:
```java
// DAO uses entity from data library
import com.optum.ap.services.data.entity.ClaimDto;

@Repository
@Transactional("aosTransactionManager")
public class ClaimDaoImpl implements ClaimDao {
    
    @Autowired
    @Qualifier("aosEntityManager")
    private EntityManager entityManager;
    
    @Override
    public List<ClaimDto> findClaimsByMemberId(String memberId) {
        // Entity class from data library
        TypedQuery<ClaimDto> query = entityManager.createNamedQuery(
            "ClaimDto.findByMemberId", ClaimDto.class);
        query.setParameter("memberId", memberId);
        return query.getResultList();
    }
}

---

## 9. Security & InitBinder (Required)

### 9.1 @InitBinder — MANDATORY

Present in EVERY controller to prevent mass assignment vulnerabilities:

```java
@InitBinder
public void initBinder(WebDataBinder binder) {
    binder.setDisallowedFields((String[]) null);
}
```

**Purpose**: Protects against mass assignment attacks where unexpected fields could be bound to DTOs.

---

## 10. Logging & Sensitive Data

### 10.1 Logger Setup

Required declaration in every class:

```java
private static final Logger logger = Logger.getLogger(ClassName.class);
```

### 10.2 Logging Levels

| Level | When | Examples |
|-------|------|----------|
| **DEBUG** | Method flow, cache operations, query details | Entry/exit, cache hit/miss, parameters |
| **INFO** | Business events, successful operations | User login, data saved, operation completed |
| **WARN** | Potential issues, fallback scenarios | Cache miss with fallback, deprecated API |
| **ERROR** | Exceptions, operation failures | Service errors, validation failures |

### 10.3 Logging Examples

```java
// Entry/Exit
logger.debug("Entering getBenefits, memberId: " + req.getMemberId());
logger.debug("Exiting getBenefits");

// Business events
logger.info("User login successful for userId: " + userId);
logger.info("Successfully saved transaction: " + transactionId);

// Cache operations
logger.debug("Cache hit for key: " + cacheKey);
logger.debug("Cache miss, fetching from DB");

// Errors with context
logger.error("Service error for memberId: " + memberId + ", operation: getBenefits", e);
```

### 10.4 Sensitive Data Protection — NEVER LOG

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
- Masked data (first/last 4 chars only)
- Operation counts, metrics

**Examples**:
```java
// ❌ WRONG
logger.error("Password failed: " + req.getPassword());
logger.debug("Full token: " + authToken);
logger.info("SSN: " + req.getSSN());

// ✅ CORRECT
logger.error("Authentication failed for userId: " + userId);
logger.debug("Token issued (masked): " + authToken.substring(0, 4) + "...");
```

---

## 11. Exception Handling Pattern

### 11.1 Standard Exception Handling

```java
try {
    return externalService.call(request);
} catch (ServiceException e) {
    logger.error("Service error for userId: " + request.getUserId() + 
                 ", operation: getData", e);
    throw new APRestException("Failed to retrieve data", e);
} catch (DataAccessException e) {
    logger.error("Database error for userId: " + request.getUserId(), e);
    throw new APRestException("Database operation failed", e);
} catch (Exception e) {
    logger.error("Unexpected error: " + e.getMessage(), e);
    throw new APRestException("Operation failed unexpectedly", e);
}
```

### 11.2 Exception Best Practices

- [ ] Catch specific exceptions first
- [ ] Log with context (user ID, operation, database)
- [ ] Include exception cause for debugging
- [ ] Wrap and throw `APRestException`
- [ ] Never expose internal stack traces to clients
- [ ] Use meaningful error messages for users
- [ ] Never swallow exceptions silently

---

## 12. Testing & Quality

### 12.1 Unit Test Requirements

```java
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceClient.class})
public class BenefitServiceImplTest {
    
    @InjectMocks
    private BenefitServiceImpl service;
    
    @Mock
    private BenefitClient client;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testGetBenefits_Success() throws Exception {
        // Arrange
        Request req = createValidRequest();
        Response expected = createExpectedResponse();
        when(client.getBenefits(req)).thenReturn(expected);
        
        // Act
        Response actual = service.getBenefits(req);
        
        // Assert
        assertTrue(actual.isSuccess());
        assertEquals(expected.getId(), actual.getId());
    }
    
    @Test(expected = APRestException.class)
    public void testGetBenefits_NullRequest() throws Exception {
        service.getBenefits(null);
    }
    
    @Test(expected = APRestException.class)
    public void testGetBenefits_MissingMemberId() throws Exception {
        Request req = createRequestWithoutMemberId();
        service.getBenefits(req);
    }
    
    @Test(expected = APRestException.class)
    public void testGetBenefits_ServiceException() throws Exception {
        Request req = createValidRequest();
        when(client.getBenefits(req)).thenThrow(new ServiceException("Error"));
        service.getBenefits(req);
    }
}
```

### 12.2 Test Coverage Requirements

- [ ] Happy path scenarios
- [ ] Null/empty input validation
- [ ] ServiceException handling
- [ ] Unexpected exception handling
- [ ] Business rule validation
- [ ] Cache hit/miss scenarios
- [ ] Multi-database scenarios (if applicable)
- [ ] **Minimum coverage**: 100% on critical modules

---

## 13. Naming & Packaging Conventions

### 13.1 Class Naming

| Type | Pattern | Examples |
|------|---------|----------|
| Controller | `{Resource}Controller` | `BenefitController`, `ClaimController` |
| Versioned Controller | `{Resource}V{X}Controller` | `BenefitV2Controller`, `ClaimV4Controller` |
| Service Interface | `{Resource}Service` | `BenefitService`, `ClaimService` |
| Service Implementation | `{Resource}ServiceImpl` | `BenefitServiceImpl`, `ClaimServiceImpl` |
| DAO Interface | `{Resource}Dao` | `BenefitDao`, `ClaimDao` |
| DAO Implementation | `{Resource}DaoImpl` | `BenefitDaoImpl`, `ClaimDaoImpl` |

### 13.2 Method Naming

**Standard patterns**:
- Retrieval: `getData()`, `findById()`, `searchByCriteria()`
- Creation: `create()`, `save()`, `insert()`
- Update: `update()`, `modify()`, `merge()`
- Delete: `delete()`, `remove()`
- Validation: `validate{Field}()`, `isValid()`
- Boolean: `isEnabled()`, `hasAccess()`, `canProcess()`

### 13.3 Constants

```java
private static final Logger logger = Logger.getLogger(ClassName.class);
private static final String CACHE_NAMESPACE = "aosBenefitCache";
private static final String SERVICE_NAME = "ap-data-service-aos-rest";
private static final String TRANSACTION_MANAGER = "aosTransactionManager";
```

### 13.4 Cache Namespace Naming Convention

Adopt consistent cache namespace naming to avoid collisions:

**Pattern**: `{domain}{Entity}Cache` or `{service}{Operation}Cache`

**Examples**:
```java
// Service-level cache (preferred)
private static final String CACHE_NAMESPACE = "aosBenefitCache";      // aos-rest
private static final String CACHE_NAMESPACE = "atfTransactionCache";  // transaction-rest
private static final String CACHE_NAMESPACE = "umrClaimCache";        // umr-rest
private static final String CACHE_NAMESPACE = "tokenSessionCache";    // token-rest
private static final String CACHE_NAMESPACE = "gatewayMemberCache";   // gateway-rest

// DAO-level cache (if separate from service)
private static final String CACHE_NAMESPACE = "aosBenefitDaoCache";
```

**Guidelines**:
- Use domain/service prefix to avoid cross-service collisions
- Keep namespace concise and meaningful
- One primary namespace per service
- Document namespace in class JavaDoc
- Use camelCase with "Cache" suffix

**Collision Prevention**:
```java
// ✅ CORRECT - Service prefixes prevent collision
private static final String CACHE_NAMESPACE = "aosBenefitCache";      // AOS service
private static final String CACHE_NAMESPACE = "umrBenefitCache";      // UMR service

// ❌ WRONG - Generic name, collision risk
private static final String CACHE_NAMESPACE = "benefitCache";
```

### 13.5 DTO Versioning Strategy

When API contracts change (breaking changes), create versioned DTOs:

**Pattern**: Version both controller AND DTOs together

```
src/main/java/com/optum/ap/services/rest/
├── controller/
│   ├── BenefitController.java          # v1.0
│   ├── BenefitV2Controller.java        # v2.0
│   └── BenefitV4Controller.java        # v4.0
└── dto/
    ├── BenefitRequest.java             # v1.0
    ├── BenefitResponse.java            # v1.0
    ├── BenefitRequestV2.java           # v2.0 (new structure)
    ├── BenefitResponseV2.java          # v2.0
    ├── BenefitRequestV4.java           # v4.0 (further changes)
    └── BenefitResponseV4.java          # v4.0
```

**Example**:
```java
// v1.0 - Original API
@RestController
@RequestMapping("/ap/rest/data/benefit/v1.0")
public class BenefitController {
    @PostMapping("/getBenefits")
    public BenefitResponse getBenefits(@RequestBody BenefitRequest req) { }
}

// v2.0 - Changed response structure
@RestController
@RequestMapping("/ap/rest/data/benefit/v2.0")
public class BenefitV2Controller {
    @PostMapping("/getBenefits")
    public BenefitResponseV2 getBenefits(@RequestBody BenefitRequestV2 req) { }
}

// Service layer can share logic via adapter pattern
@Service
public class BenefitServiceImpl implements BenefitService {
    
    public BenefitResponse getV1(BenefitRequest req) { }
    
    public BenefitResponseV2 getV2(BenefitRequestV2 req) {
        BenefitRequest v1Req = convertToV1(req);      // Adapter
        BenefitResponse v1Resp = getInternal(v1Req);
        return convertFromV1(v1Resp);                 // Back to v2
    }
}
```

**When to Version**:
- ✅ DO version when removing/renaming request fields
- ✅ DO version when changing response structure
- ✅ DO version for significant behavioral changes
- ❌ DON'T version for minor additions (use Optional/nullable)
- ❌ DON'T version if change is backward-compatible

**Backward Compatibility**:
- Keep at least 2 versions active in production (v1, v2)
- Deprecate oldest version after 3-6 months
- Document sunset timeline clearly

---

## 14. Transaction Manager Reference

### 14.1 Database-to-Manager Mapping

Use the correct transaction manager for your repository and database:

| Transaction Manager | Database | Repositories | Usage |
|---------------------|----------|--------------|-------|
| `aosTransactionManager` | AOS | aos-rest, umr-rest | General AOS data (benefits, claims, members) |
| `atfTransactionManager` | ATF | transaction-rest, job-rest | Transactions, enrollments, workflows |
| `apsTransactionManager` | APS/Security DB | security-service-rest | User management, roles, permissions |
| `wdtTransactionManager` | WDT | aos-rest, umr-rest | WDT-specific data operations |
| `swaTransactionManager` | SWA/DLA | aos-rest, umr-rest, data-services | SWA/DLA data access |
| `umrTransactionManager` | UMR | umr-rest (hybrid REST/SOAP) | UMR-specific operations |

### 14.2 Usage in DAO

```java
@Repository
@Transactional("aosTransactionManager")  // ← Specify correct manager
public class BenefitDaoImpl implements BenefitDao { }
```

### 14.3 Multi-DB Service Example

```java
@Service
public class DataServiceImpl implements DataService {
    
    @Autowired
    @Qualifier("aosEntityManager")
    private EntityManager aosEm;
    
    @Autowired
    @Qualifier("wdtEntityManager")
    private EntityManager wdtEm;
    
    @Transactional("aosTransactionManager")
    public void updateAosData(Request req) throws APRestException {
        aosEm.persist(convertToAosEntity(req));
    }
    
    @Transactional("wdtTransactionManager")
    public void updateWdtData(Request req) throws APRestException {
        wdtEm.persist(convertToWdtEntity(req));
    }
    
    // ❌ WRONG - Mixed managers in single transaction
    @Transactional("aosTransactionManager")
    public void updateBothDatabases(Request req) throws APRestException {
        aosEm.persist(convertToAosEntity(req));      // ✅ Correct manager
        wdtEm.persist(convertToWdtEntity(req));      // ❌ Wrong manager - may not commit!
    }
}
```

**Key Rule**: One transaction manager per method. Use separate methods if multiple databases need updates.

---

## 15. Configuration Management

### 15.1 application.yaml Template

```yaml
# application.yaml (default/development)
server:
  port: 8080
  servlet:
    context-path: /ap-service-name-rest

spring:
  application:
    name: ap-service-name-rest
  jpa:
    hibernate:
      ddl-auto: validate
  jackson:
    deserialization:
      fail-on-unknown-properties: false

# Database configuration (multi-DB example)
datasources:
  aos:
    url: jdbc:oracle:thin:@localhost:1521:OASDB
    username: ${AOS_DB_USER:aos_user}
    password: ${AOS_DB_PASSWORD:}
    pool-size: 10
  atf:
    url: jdbc:oracle:thin:@localhost:1521:ATFDB
    username: ${ATF_DB_USER:atf_user}
    password: ${ATF_DB_PASSWORD:}
    pool-size: 10
  wdt:
    url: jdbc:oracle:thin:@localhost:1521:WDTDB
    username: ${WDT_DB_USER:wdt_user}
    password: ${WDT_DB_PASSWORD:}
    pool-size: 10

# Cache configuration
cache:
  enabled: true
  ttl: 3600                    # Default TTL in seconds
  max-size: 10000              # Maximum cached entries

# Logging configuration
logging:
  level:
    com.optum.ap: DEBUG        # ← Change to INFO in production
    org.springframework: WARN
    org.springframework.security: DEBUG
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"

# External service URLs (with defaults for development)
services:
  aos-backend: ${AOS_SERVICE_URL:http://localhost:8081/ap-data-service-aos-rest}
  umr-backend: ${UMR_SERVICE_URL:http://localhost:8082/ap-data-service-umr-rest}
  gateway-service: ${GATEWAY_SERVICE_URL:http://localhost:8083/ap-gateway-service-rest}
  security-service: ${SECURITY_SERVICE_URL:http://localhost:8084/ap-security-service-rest}
  token-service: ${TOKEN_SERVICE_URL:http://localhost:8085/ap-token-service-rest}

# OAuth2 / Security configuration
security:
  oauth2:
    enabled: true
    client-id: ${OAUTH2_CLIENT_ID}
    client-secret: ${OAUTH2_CLIENT_SECRET}
    scope: ${OAUTH2_SCOPE:member-data,claim-data}
```

### 15.2 Environment-Specific Profiles

```yaml
# application-prod.yaml (production overrides)
logging:
  level:
    com.optum.ap: INFO         # ← Less verbose
    org.springframework: WARN

cache:
  ttl: 7200                    # ← Longer cache in production
  max-size: 50000              # ← Larger cache

datasources:
  aos:
    pool-size: 20              # ← More connections

server:
  ssl:
    enabled: true              # ← Enable TLS
    key-store: ${KEYSTORE_PATH}
    key-store-password: ${KEYSTORE_PASSWORD}

# Use specific production endpoints
services:
  aos-backend: https://prod-aos.example.com/ap-data-service-aos-rest
```

### 15.3 Configuration Rules

- [ ] Never commit sensitive data (passwords, keys, tokens)
- [ ] Use environment variables for secrets (${ENV_VAR})
- [ ] Define sensible defaults for development
- [ ] Document all configuration options in README
- [ ] Use profile-specific files (dev, qa, prod)
- [ ] Externalize cache TTL, pool sizes, timeouts
- [ ] Rotate secrets regularly

---

## 16. Async & Batch Processing

### 16.1 When to Use Async

Use `@Async` for long-running or fire-and-forget operations:

- ✅ Long-running batch operations (>5 seconds)
- ✅ Fire-and-forget operations
- ✅ Bulk imports/exports
- ✅ Background job processing
- ❌ Don't use if immediate response required
- ❌ Don't use if failure requires immediate notification to client

### 16.2 Async Implementation Pattern

```java
@Configuration
@EnableAsync
public class AsyncConfiguration {
    
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ap-async-");
        executor.initialize();
        return executor;
    }
}

@Service
public class BatchProcessServiceImpl implements BatchProcessService {
    
    private static final Logger logger = Logger.getLogger(BatchProcessServiceImpl.class);
    
    // Synchronous entry point (user-facing) returns immediately
    @Override
    public Response submitBatchTransaction(Request req) throws APRestException {
        logger.info("Batch submission received for jobId: " + req.getJobId());
        
        validateRequest(req);
        
        // Submit async job and return immediately
        processBatchAsync(req);
        
        Response resp = new Response();
        resp.setSuccess(true);
        resp.setMessage("Batch submitted for processing");
        resp.setJobId(req.getJobId());
        return resp;
    }
    
    // Async processing in background thread
    @Async("taskExecutor")
    @Transactional("atfTransactionManager")  // ← Explicit transaction manager
    public void processBatchAsync(Request req) {
        logger.info("Starting async batch processing for jobId: " + req.getJobId());
        
        try {
            List<TransactionDto> transactions = parseTransactions(req);
            
            for (TransactionDto trans : transactions) {
                processTransaction(trans);  // Each within transaction
            }
            
            updateJobStatus(req.getJobId(), "COMPLETED", null);
            logger.info("Batch processing completed successfully");
            
        } catch (Exception e) {
            logger.error("Batch processing failed for jobId: " + req.getJobId(), e);
            updateJobStatus(req.getJobId(), "FAILED", e.getMessage());
        }
    }
    
    @Transactional("atfTransactionManager")
    private void processTransaction(TransactionDto trans) throws APRestException {
        transactionDao.save(trans);
        auditDao.log("Transaction processed: " + trans.getId());
    }
}
```

### 16.3 Async Best Practices

- [ ] Always have explicit `@Transactional` on async methods
- [ ] Specify transaction manager for async writes
- [ ] Update job status throughout processing
- [ ] Log start, progress, and completion/failure
- [ ] Handle exceptions gracefully (update job to FAILED)
- [ ] Configure thread pool size appropriately
- [ ] Monitor queue depth for bottlenecks

---

## 17. Minimal Code Samples

### 17.1 Complete Service Example

```java
@Service("benefitService")
public class BenefitServiceImpl implements BenefitService {
    
    private static final Logger logger = Logger.getLogger(BenefitServiceImpl.class);
    private static final String CACHE_NAMESPACE = "aosBenefitCache";
    
    @Autowired
    private BenefitDao benefitDao;
    
    @Override
    public Response getBenefits(Request req) throws APRestException {
        logger.debug("Entering getBenefits");
        validateGetBenefitsRequest(req);
        
        if (!req.isCacheDisabled()) {
            Response cached = (Response) Cache.getInstance()
                .get(CACHE_NAMESPACE, req.getCacheKey());
            if (cached != null) {
                cached.setFromCache(true);
                return cached;
            }
        }
        
        try {
            Response response = benefitDao.getBenefits(req);
            if (!req.isCacheDisabled()) {
                Cache.getInstance().put(CACHE_NAMESPACE, req.getCacheKey(), response);
            }
            logger.info("Successfully retrieved benefits");
            return response;
        } catch (DataAccessException e) {
            logger.error("Database error: " + e.getMessage(), e);
            throw new APRestException("Failed to retrieve benefits", e);
        }
    }
    
    @Override
    @Transactional("aosTransactionManager")
    public Response saveBenefits(Request req) throws APRestException {
        logger.debug("Entering saveBenefits");
        validateSaveBenefitsRequest(req);
        
        try {
            benefitDao.save(req);
            logger.info("Successfully saved benefits");
            return createSuccessResponse();
        } catch (DataAccessException e) {
            logger.error("Save error: " + e.getMessage(), e);
            throw new APRestException("Failed to save benefits", e);
        }
    }
    
    private void validateGetBenefitsRequest(Request req) throws APRestException {
        if (req == null) throw new APRestException("Request cannot be null");
        if (StringUtil.isNullOrEmpty(req.getMemberId())) 
            throw new APRestException("memberId is required");
    }
    
    private void validateSaveBenefitsRequest(Request req) throws APRestException {
        if (req == null) throw new APRestException("Request cannot be null");
        if (StringUtil.isNullOrEmpty(req.getMemberId())) 
            throw new APRestException("memberId is required");
    }
}
```

### 17.2 Complete Controller Example

```java
@RestController
@RequestMapping("/ap/rest/data/benefit/v1.0")
public class BenefitController extends BaseController {
    
    @Autowired
    private BenefitService benefitService;
    
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields((String[]) null);
    }
    
    @PostMapping(value = "/getBenefits", 
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public Response getBenefits(@RequestBody Request req) throws APRestException {
        try {
            return benefitService.getBenefits(req);
        } catch (Exception ex) {
            throw new APRestException(ex.getMessage());
        }
    }
    
    @PostMapping(value = "/saveBenefits",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public Response saveBenefits(@RequestBody Request req) throws APRestException {
        try {
            return benefitService.saveBenefits(req);
        } catch (Exception ex) {
            throw new APRestException(ex.getMessage());
        }
    }
}
```

---

## 18. Pre-commit Checklist (Updated)

**Execute before committing code**:

### HTTP & Routing
- [ ] All endpoints use `@PostMapping` only (no @GetMapping, @PutMapping, etc.)
- [ ] URL follows pattern: `/ap/rest/{domain}/{subdomain}/v{version}/{operation}`
- [ ] Methods accept `@RequestBody` and return Response DTOs

### Caching
- [ ] Service-layer caching via `Cache.getInstance()` implemented
- [ ] `CACHE_NAMESPACE` defined as `private static final String`
- [ ] Cache check before external/DB calls
- [ ] Cache store after successful retrieval
- [ ] `request.isCacheDisabled()` respected
- [ ] `response.setFromCache(true)` set on cache hits

### Transactions
- [ ] `@Transactional` present ONLY on write/atomic DB methods
- [ ] Transaction manager explicitly specified (e.g., `@Transactional("aosTransactionManager")`)
- [ ] DAOs use class-level `@Transactional` with manager
- [ ] No `@Transactional` on read-only or ServiceClient-only methods

### Security
- [ ] `@InitBinder` present in all controllers with `setDisallowedFields((String[]) null)`
- [ ] Sensitive data NOT logged (passwords, tokens, PII, PHI)

### Exceptions
- [ ] All exceptions wrapped in `APRestException`
- [ ] Specific exceptions caught (not generic `Exception`)
- [ ] Context logged (user ID, operation) before throwing
- [ ] No stack traces exposed to clients

### Validation & Logging
- [ ] Request validation at service layer method start
- [ ] Meaningful error messages for validation failures
- [ ] Logger declared as `private static final Logger`
- [ ] DEBUG: entry/exit, cache events
- [ ] INFO: business events, success
- [ ] ERROR: exceptions with context

### DTOs
- [ ] Request DTOs implement `getCacheKey()` and `isCacheDisabled()`
- [ ] Response DTOs implement `setFromCache(boolean)` and error fields
- [ ] No null returns (always return Response object)
- [ ] `success` flag set appropriately
- [ ] `errorMessage` included when `success=false`

### Naming & Constants
- [ ] Cache namespace follows convention: `{domain}{Entity}Cache`
- [ ] Service names match registry (e.g., `ap-data-service-aos-rest`)
- [ ] Classes follow naming pattern (ResourceController, ResourceService, ResourceDaoImpl)
- [ ] Constants defined as `private static final`

### Versioning
- [ ] Versioned controllers use consistent scheme (V2Controller, V4Controller)
- [ ] Versioned DTOs match controller versions
- [ ] Multiple versions kept active for backward compatibility

### Testing
- [ ] Unit tests cover: happy path, validation, cache hit/miss, exceptions
- [ ] Test coverage =100% on critical modules
- [ ] JavaDoc on public methods

### Configuration
- [ ] Sensitive data externalized to environment variables
- [ ] Defaults set for development convenience
- [ ] No passwords, keys, tokens in code

---

## 19. References & Alignment

### Alignment with Client-Instructions

Follow `client-instructions.md` for:
- DTO/Request/Response base class patterns
- Service interface contracts
- Message object structures
- Naming conventions for DTOs

### Reference Implementations

Review existing repositories for patterns:

| Repository | Key Examples |
|------------|--------------|
| aos-rest | BenefitController, BenefitServiceImpl, BenefitDaoImpl |
| umr-rest | Hybrid REST/SOAP, version proliferation (V1-V7) |
| gateway-rest | Orchestration, no DAOs, ServiceClient-heavy |
| transaction-rest | Async processing, heavy writes, @Transactional |
| token-rest | Cache-heavy, minimal DB, high-frequency ops |
| security-rest | User management, auth/authz, RBAC |

### Industry Standards Compliance

This document aligns with:
- **REST API**: HTTP methods, JSON payloads, versioned URLs
- **Spring Boot**: Dependency injection, annotations, configuration
- **Spring Data JPA**: EntityManager, named queries, transactions
- **Spring Security**: Role-based access, OAuth2 scopes
- **Java**: SOLID principles, single responsibility, separation of concerns
- **Logging**: Structured logging, appropriate levels, data protection
- **Testing**: Unit tests, mocking, coverage metrics

---

## 20. End Notes

### Key Takeaways

1. **@PostMapping ONLY** — All endpoints use POST exclusively
2. **Cache by Default** — Implement OapCache in every service read
3. **@Transactional for Writes** — Only for DB write operations with explicit manager
4. **Validate Early, Fail Fast** — Validate at service method start
5. **Wrap Exceptions** — Always throw `APRestException` after logging
6. **Security First** — @InitBinder in every controller
7. **Log Carefully** — DEBUG entry/exit, INFO business events, ERROR with context
8. **Version Gracefully** — Create versioned controllers/DTOs for breaking changes
9. **Test Thoroughly** — 80% coverage on critical modules
10. **Externalize Config** — Secrets in environment variables, not code

### Troubleshooting Common Issues

**Cache not working**: Check `request.getCacheKey()` implementation and `CACHE_NAMESPACE` definition.

**Transaction errors**: Verify transaction manager matches target database (aos, atf, wdt, umr, etc.).

**Missing data in logs**: Check logging levels; ensure context (userId, operation) included before throwing.

**Mass assignment vulnerability**: Ensure `@InitBinder` present in ALL controllers.

**Sensitive data exposed**: Audit logs for passwords, SSNs, tokens, PHI; use masked values only.

### Support & Questions

Refer to:
- Client-instructions.md for DTO contracts
- Existing repo implementations (aos, umr, gateway)
- Spring Boot documentation
- Java coding standards

---

### 21. API Documentation (Swagger/OpenAPI)
Controller Documentation:
```java

@RestController
@RequestMapping("/ap/rest/data/benefit/v1.0")
@Tag(name = "Benefit Management", description = "Benefit data operations")
public class BenefitController extends BaseController {
    
    @PostMapping("/getBenefits")
    @Operation(summary = "Get member benefits",
               description = "Retrieve all benefits for a member by memberId")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Benefits retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input parameters"),
        @ApiResponse(responseCode = "404", description = "Member not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getBenefits(
        @RequestBody @Valid BenefitRequest req) throws APRestException {
        return benefitService.getBenefits(req);
    }
}
```
DTO Documentation

```java
@Schema(description = "Benefit request payload")
public class BenefitRequest extends AbstractRequest {
    
    @Schema(description = "Member ID", example = "123456", required = true)
    private String memberId;
    
    @Schema(description = "Benefit type filter", example = "MEDICAL")
    private String benefitType;
    
    @Schema(description = "Start date for benefit period")
    private Date startDate;
}
```
Access Documentation:

Swagger UI: http://localhost:8080/ap-service-name-rest/swagger-ui.html
OpenAPI JSON: http://localhost:8080/ap-service-name-rest/v3/api-docs

**Follow these instructions precisely. They capture architectural patterns across AP services and align with industry Java best practices. Consistency ensures quality, maintainability, and security across the entire AP ecosystem.**