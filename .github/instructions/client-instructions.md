# Client Instructions

## 1. Repository Overview

This is a **Java client library** for healthcare administration systems, providing DTOs (Data Transfer Objects), service interfaces, and message objects for managing domain-specific operations. This is a **library project** (not an application) that will be consumed by other services.

**Repository Stats:**
- Language: Java
- Build Tool: Maven
- Size: ~200 source files across 50+ packages
- No external runtime - this is a dependency library

---

## 2. Step 1: Repository Identification & Context Discovery

### Identify Your Repository Type

**Primary repository patterns found:**
1. **Standard Domain Clients** (ap-{DOMAIN}-client pattern)
   - `ap-data-client`: Data operations
   - `ap-security-client`: Security operations
   - `ap-transaction-client`: Transaction operations (trans)
   - `ap-ems-client`: Event Management System
   - `ap-provdir-client`: Provider Directory
   - `ap-job-client`: Job operations
   - `ap-gateway-client`: API Gateway

### Automatic Context Detection Commands

**Run these commands to identify your context:**

```bash
# Step 1: Identify base package structure
echo "=== Checking Base Package Structure ==="
ls src/main/java/com/tpa/ap/ 2>/dev/null || ls src/main/java/com/optum/ap/ 2>/dev/null || echo "Non-standard structure"

# Step 2: Identify if it's a standard domain client
echo "=== Domain Check ==="
if [ -d "src/main/java/com/tpa/ap" ]; then
    echo "Standard TPA domain client"
    DOMAIN=$(ls src/main/java/com/tpa/ap/ | head -1)
    echo "Domain: $DOMAIN"
else
    echo "Specialized client (Optum or other structure)"
fi

# Step 3: Find AbstractDto location
echo "=== Finding Base Classes ==="
find src/main/java -name "AbstractDto.java" 2>/dev/null

# Step 4: Find AbstractRequest location
find src/main/java -name "AbstractRequest.java" 2>/dev/null

# Step 5: Find Service interface
find src/main/java -name "*Service.java" -path "*/service/*" | grep -E "(Service\.java|Manager\.java)$" 2>/dev/null

# Step 6: Check for subdomains/modules
echo "=== Checking Subdomains ==="
ls src/main/java/com/tpa/ap/*/dto/ 2>/dev/null || ls src/main/java/com/optum/ap/*/dto/ 2>/dev/null
```

### Package Structure Patterns by Repository Type

**Examples from workspace (TPA structure):**
- **ap-security-client**: `com.tpa.ap.security.*`
- **ap-transaction-client**: `com.tpa.ap.trans.*` (Note: uses 'trans' not 'transaction')
- **ap-ems-client**: `com.tpa.ap.mw.ems.*` (Note: middleware structure)
- **ap-provdir-client**: `com.tpa.ap.data.*` and `com.tpa.ap.provdir.*` (hybrid)

**Examples from workspace (Optum structure):**
- **ap-data-client-aerial**: `com.optum.ap.data.aerial.*`
- **ap-gateway-client**: `com.optum.ap.gateway.*`

---

## 3. Step 2: Locate Base Classes for Your Repository

### Generic Base Class Discovery

**Method 1: Automated Discovery Script**

```bash
#!/bin/bash
echo "=== Base Class Discovery Report ==="
echo ""

# Check for standard TPA structure
if [ -d "src/main/java/com/tpa/ap" ]; then
    echo "Repository Type: Standard TPA Domain Client"
    DOMAIN=$(ls src/main/java/com/tpa/ap/ | head -1)
    echo "Detected Domain: $DOMAIN"
    echo ""
    
    # Find AbstractRequest
    REQ_PATH=$(find src/main/java -name "AbstractRequest.java" | grep "com/tpa/ap/$DOMAIN")
    if [ -n "$REQ_PATH" ]; then
        echo "✓ AbstractRequest found: $REQ_PATH"
        REQ_PACKAGE=$(echo $REQ_PATH | sed 's|src/main/java/||' | sed 's|/|.|g' | sed 's|.java||')
        echo "  Import: import ${REQ_PACKAGE};"
    else
        echo "✗ AbstractRequest not found - Check message package structure"
    fi
    echo ""
    
    # Find Service interfaces
    echo "Service Interfaces found:"
    find src/main/java -path "*/service/*Service.java" -o -path "*/service/*Manager.java" | while read service; do
        echo "  - $service"
    done
    
else
    echo "Repository Type: Specialized Client (Non-standard TPA structure)"
    echo ""
    echo "Package roots found:"
    find src/main/java -maxdepth 5 -type d -name "dto" -o -name "reqResp" -o -name "message" | while read dir; do
        echo "  - $dir"
    done
fi
```

---

## 4. Critical Coding Rules (ALWAYS follow)

### 4.1 When Creating DTOs

1. MUST extend `AbstractDto` (import `com.tpa.ap.common.service.dto.AbstractDto`)
2. MUST implement `Serializable` (import `java.io.Serializable`)
3. MUST include: `private static final long serialVersionUID = 1L;`
4. MUST use wrapper types (Integer, Boolean, BigDecimal) for nullable values
5. MUST initialize collections inline (e.g., `private List<MemberDto> members = new ArrayList<>();`)
6. MUST override `equals()`, `hashCode()`, `toString()`
7. SHOULD annotate with `@ApiModel("MemberDtoV1")` (import `io.swagger.annotations.ApiModel`)
8. AVOID exposing internal collection references (return empty list if null; consider defensive copies if mutation risk)
9. **DO NOT include pagination fields by default** - add only if the API specifically requires pagination


### 4.2 When Creating Request Objects

1. MUST extend `AbstractRequest` (import `com.tpa.ap.common.service.message.AbstractRequest`)
2. MUST include `loginId` (String) - audit trail for who made the request
3. MUST override `getCacheKey()` - returns unique identifier for caching
4. MUST implement `Serializable` and `serialVersionUID = 1L`
5. Fields MUST be private with getters/setters
6. MUST override `toString()`
7. Maintain field declarations in **alphabetical order**
8. Use wrapper types for optional numeric/boolean fields
9. **DO NOT include pagination fields by default** - add only if the API specifically requires pagination

**Pagination Rule:**
- ❌ **Do NOT include** `pageNumber`, `pageSize`, `limit`, `offset` by default
- ✅ **Add ONLY if** the backend API explicitly supports pagination
- ❌ **Never include pagination in getCacheKey()** - if pagination is added later

### 4.3 When Creating Response Objects

1. MUST extend `AbstractResponse` (import `com.tpa.ap.common.service.message.AbstractResponse`)
2. MUST implement `Serializable` and `serialVersionUID = 1L`
3. MUST initialize collections (never return null)
4. Collection setters MUST be null-safe (set empty list when input is null)
5. Maintain field declarations in **alphabetical order**
6. SHOULD NOT include `loginId` field (that's request metadata, not response data)
7. **DO NOT include totalCount by default** - add only if the API specifically requires it
8. **DO NOT include pagination fields by default** - add only if the API specifically requires pagination


### 4.4 When Creating Service Interfaces

1. MUST extend `Service` (import `com.tpa.ap.common.service.Service`)
2. ALL methods throw `ServiceException` (import `com.tpa.ap.common.service.Service.ServiceException`)
3. Method parameters/return types use domain Request/Response classes
4. Order methods **alphabetically by method name**
5. NO business logic (pure contract)

### 4.5 Naming Conventions (STRICTLY enforce)

| Artifact | Pattern | Examples |
|----------|---------|----------|
| DTOs | `{SUBDOMAIN}{Purpose}Dto` | `MemberCOBDto`, `UserProfileDto` |
| Requests | `{SUBDOMAIN}{Action}Request` | `ClaimSearchRequest`, `AuthenticationRequest` |
| Responses | `{SUBDOMAIN}{Action}Response` | `ClaimSearchResponse`, `AuthenticationResponse` |
| Services | `{SUBDOMAIN}Service` or `{SUBDOMAIN}Manager` | `ClaimService`, `MemberManager` |
| Clients | `{SUBDOMAIN}ServiceClient` | `ClaimServiceClient`, `MemberServiceClient` |

---

## 5. Placeholder Naming Convention (Standardized)

Use these consistently throughout all instructions and code:

| Placeholder | Meaning | Example | Usage |
|------------|---------|---------|-------|
| `{DOMAIN}` | Domain name (lowercase) | `data`, `security`, `trans`, `ems` | Java packages, URLs, paths |
| `{SUBDOMAIN}` | Subdomain PascalCase | `Claim`, `Member`, `User`, `Role` | Class names, interface names |
| `{subdomain}` | Subdomain lowercase | `claim`, `member`, `user`, `role` | Package paths, URL segments |

**Correct Usage Examples:**
- Class name: `{SUBDOMAIN}ServiceClient` → `ClaimServiceClient`
- Package: `com.tpa.ap.{DOMAIN}.client.ejb` → `com.tpa.ap.data.client.ejb`
- URL path: `/ap/rest/{DOMAIN}/{subdomain}/v1.0` → `/ap/rest/data/claim/v1.0`
- Import: `com.tpa.ap.{DOMAIN}.service.{SUBDOMAIN}Service` → `com.tpa.ap.data.service.ClaimService`

---

## 6. Domain Version Strategy

**CRITICAL:** Check your specific domain for versioning before creating any artifacts.

### 6.1 Automatic Version Detection Script (Bash)

```bash
#!/bin/bash
# Version Detection Script
# Usage: ./detect-version.sh {DOMAIN} {subdomain}
# Example: ./detect-version.sh data claim

DOMAIN=${1:-"data"}
SUBDOMAIN=${2:-"member"}

echo "=== Version Detection for $DOMAIN/$SUBDOMAIN ==="
echo ""

# Define base paths to check
DTO_BASE="src/main/java/com/tpa/ap/$DOMAIN/dto/$SUBDOMAIN"
MESSAGE_BASE="src/main/java/com/tpa/ap/$DOMAIN/message/$SUBDOMAIN"

# Function to find highest version
find_latest_version() {
    local base_path=$1
    local versions=()
    
    if [ -d "$base_path" ]; then
        # Look for v2, v3, v4, v5, etc. directories
        for v in $(ls -d "$base_path"/v[0-9]* 2>/dev/null | sort -V); do
            version_num=$(basename "$v" | sed 's/v//')
            versions+=($version_num)
        done
        
        if [ ${#versions[@]} -eq 0 ]; then
            echo "NONE (use root package)"
        else
            echo "v${versions[-1]}"  # Return highest version
        fi
    else
        echo "PATH_NOT_FOUND"
    fi
}

# Check DTO versioning
echo "DTO Package Check:"
echo "  Path: $DTO_BASE"
DTO_VERSION=$(find_latest_version "$DTO_BASE")
echo "  Latest Version: $DTO_VERSION"
echo ""

# Check Message versioning
echo "Message Package Check:"
echo "  Path: $MESSAGE_BASE"
MESSAGE_VERSION=$(find_latest_version "$MESSAGE_BASE")
echo "  Latest Version: $MESSAGE_VERSION"
echo ""

# Provide recommendation
echo "=== RECOMMENDATION ==="
if [ "$DTO_VERSION" = "NONE" ] && [ "$MESSAGE_VERSION" = "NONE" ]; then
    echo "✓ No versioning detected - use root packages:"
    echo "  DTOs: $DTO_BASE/"
    echo "  Messages: $MESSAGE_BASE/"
elif [ "$DTO_VERSION" = "PATH_NOT_FOUND" ]; then
    echo "⚠ Subdomain '$SUBDOMAIN' not found in domain '$DOMAIN'"
    echo "  Run: ls src/main/java/com/tpa/ap/$DOMAIN/dto/"
    echo "  to see available subdomains"
else
    echo "✓ Use latest versions:"
    [ "$DTO_VERSION" != "NONE" ] && echo "  DTOs: $DTO_BASE/$DTO_VERSION/"
    [ "$MESSAGE_VERSION" != "NONE" ] && echo "  Messages: $MESSAGE_BASE/$MESSAGE_VERSION/"
fi
```

### 6.2 PowerShell Version Detection (Windows)

```powershell
# PowerShell Version Detection
$domain = "data"
$subdomain = "claim"

$dtoPath = "src\main\java\com\tpa\ap\$domain\dto\$subdomain"
$messagePath = "src\main\java\com\tpa\ap\$domain\message\$subdomain"

Write-Host "=== Version Detection for $domain/$subdomain ===" -ForegroundColor Cyan
Write-Host ""

# Find DTO versions
if (Test-Path $dtoPath) {
    $dtoVersions = Get-ChildItem -Path $dtoPath -Directory -Filter "v*" | 
                   Where-Object { $_.Name -match "^v\d+$" } |
                   Sort-Object { [int]($_.Name -replace 'v','') }
    
    if ($dtoVersions) {
        $latestDto = $dtoVersions[-1].Name
        Write-Host "DTO Latest Version: $latestDto" -ForegroundColor Green
        Write-Host "  Path: $dtoPath\$latestDto" -ForegroundColor Gray
    } else {
        Write-Host "DTO Version: NONE (use root)" -ForegroundColor Yellow
    }
} else {
    Write-Host "DTO Path not found: $dtoPath" -ForegroundColor Red
}

Write-Host ""

# Find Message versions
if (Test-Path $messagePath) {
    $msgVersions = Get-ChildItem -Path $messagePath -Directory -Filter "v*" |
                   Where-Object { $_.Name -match "^v\d+$" } |
                   Sort-Object { [int]($_.Name -replace 'v','') }
    
    if ($msgVersions) {
        $latestMsg = $msgVersions[-1].Name
        Write-Host "Message Latest Version: $latestMsg" -ForegroundColor Green
        Write-Host "  Path: $messagePath\$latestMsg" -ForegroundColor Gray
    } else {
        Write-Host "Message Version: NONE (use root)" -ForegroundColor Yellow
    }
} else {
    Write-Host "Message Path not found: $messagePath" -ForegroundColor Red
}
```

### 6.3 Quick One-Line Version Check

```bash
# Check DTO versions for a subdomain
ls -d src/main/java/com/tpa/ap/{DOMAIN}/dto/{subdomain}/v* 2>/dev/null | sort -V | tail -1

# Check Message versions for a subdomain
ls -d src/main/java/com/tpa/ap/{DOMAIN}/message/{subdomain}/v* 2>/dev/null | sort -V | tail -1

# Example for claim in data domain:
ls -d src/main/java/com/tpa/ap/data/dto/claim/v* 2>/dev/null | sort -V | tail -1
# Output: src/main/java/com/tpa/ap/data/dto/claim/v4  ← USE v4
```

### 6.4 Version Placement Rules

| Rule | Structure | Use When |
|------|-----------|----------|
| **Same Version** | DTOs and Messages both in v4 | Standard pattern - most common |
| **Mixed Version** | DTOs in v3, Messages in root | DTO evolved, messages stable |
| **No Versioning** | Root package for both | Stable domain, no breaking changes |
| **Create New** | Use HIGHEST version number | v4 > v3 > v2 |
| **New Domain** | Use root package | First version - don't create v1 |

### 6.5 Version Decision Tree

```
START: Need to create new DTO/Request/Response
  |
  ├─→ Run version detection script (see 6.1 or 6.2)
  |    OR quick check (see 6.3)
  |
  ├─→ Do version directories exist?
  |    |
  |    ├─→ YES: Which is highest? (v4 > v3 > v2)
  |    |    └─→ USE: src/.../dto/{subdomain}/v{highest}/
  |    |         AND: src/.../message/{subdomain}/v{highest}/
  |    |
  |    └─→ NO: Use root package
  |         └─→ USE: src/.../dto/{subdomain}/
  |              AND: src/.../message/{subdomain}/
  |
  └─→ Update package statements to match path
       └─→ package com.tpa.ap.{DOMAIN}.dto.{subdomain}.v{X};
```

### 6.6 Version Examples

**Example 1: Claim in ap-data-client (versioned)**
```bash
$ ls -d src/main/java/com/tpa/ap/data/dto/claim/v*
v2/  v3/  v4/

# Result: Use v4
# Package: com.tpa.ap.data.dto.claim.v4
# Filepath: src/main/java/com/tpa/ap/data/dto/claim/v4/ClaimDto.java
```

**Example 2: User in ap-security-client (not versioned)**
```bash
$ ls -d src/main/java/com/tpa/ap/security/dto/user/v*
# (no output - not versioned)

# Result: Use root package
# Package: com.tpa.ap.security.dto.user
# Filepath: src/main/java/com/tpa/ap/security/dto/user/UserDto.java
```

**Example 3: Transaction in ap-transaction-client (not versioned)**
```bash
$ ls -d src/main/java/com/tpa/ap/trans/dto/*/v*
# (no output - trans domain typically not versioned)

# Result: Use root package for all subdomains
# Package: com.tpa.ap.trans.dto.{subdomain}
# Filepath: src/main/java/com/tpa/ap/trans/dto/{subdomain}/TransactionDto.java
```

---

## 7. Data Type Rules (NEVER violate)

| Data Type | Rule | Examples |
|-----------|------|----------|
| **Dates** | Use `String` (format: "yyyy-MM-dd") | `"2024-01-15"` |
| **Money** | Use `BigDecimal` (NEVER double/float) | `new BigDecimal("1234.56")` |
| **Booleans** | Use `Boolean` wrapper for DTOs, `char` ('Y'/'N') for DB matching | DTOs: `Boolean isActive`, DB: `char flag` |
| **Collections** | Initialize to empty, never return null | `new ArrayList<>()`, `new HashMap<>()` |
| **Nullable numbers** | Use wrapper types (Integer, Long, Double) | `Integer pageNumber` not `int pageNumber` |
| **Required numbers** | Primitives acceptable only if truly required | `int count` only if always present |

---

## 8. Field Ordering Rules

### 8.1 Fields MUST be Alphabetical

Field declarations in DTOs, Requests, and Responses **MUST** be in strict alphabetical order:

```java
public class ClaimSearchRequest extends AbstractRequest {
    private Boolean includeHistory;      // i - ALPHABETICAL ORDER
    private String  loginId;             // l - REQUIRED on all Requests  
    private String  claimNumber;         // c - comes AFTER p alphabetically? NO - FIX THIS
    private String  effectiveDate;       // e - comes AFTER p alphabetically? NO - FIX THIS
}
```

**CORRECT ordering:**
```java
public class ClaimSearchRequest extends AbstractRequest {
    private String  claimNumber;         // c
    private String  effectiveDate;       // e
    private Boolean includeHistory;      // i
    private String  loginId;             // l - REQUIRED
  
}
```

### 8.2 Methods MUST be Alphabetical

Method declarations in Service interfaces and Client implementations **MUST** be in strict alphabetical order by method name:

```java
public interface ClaimService extends Service {
    // Methods ordered A-Z by method name
    ClaimDetailListResponse getClaimAllDataList(...) throws ServiceException;
    ClaimAdvanceSearchResponse getClaimsByTinNumber(...) throws ServiceException;
    ClaimDetailResponse getClaimDetailByKey(...) throws ServiceException;
    ClaimDetailListResponse getClaimDetailList(...) throws ServiceException;
    // ... rest in alphabetical order
}
```

---

## 9. Common Imports (Quick Reference)

**Required for DTOs:**
```java
import java.io.Serializable;  // REQUIRED
import java.math.BigDecimal;  // For money amounts
import java.util.ArrayList;   // Collection initialization
import java.util.HashMap;     // For Map fields
import java.util.List;        // Collection types
import java.util.Map;         // Collection types
import java.util.Objects;     // For equals/hashCode
import java.util.Set;         // Collection types

import com.tpa.ap.common.service.dto.AbstractDto;  // REQUIRED
import io.swagger.annotations.ApiModel;  // RECOMMENDED for API docs
```

**Required for Requests:**
```java
import java.io.Serializable;  // REQUIRED
import java.math.BigDecimal;  // For numeric fields
import java.util.ArrayList;   // Collection initialization
import java.util.List;        // Collection types
import com.tpa.ap.common.service.message.AbstractRequest;  // REQUIRED
```

**Required for Responses:**
```java
import java.io.Serializable;  // REQUIRED
import java.util.ArrayList;   // Collection initialization
import java.util.HashMap;     // For Map fields
import java.util.List;        // Collection types
import java.util.Map;         // Collection types
import com.tpa.ap.common.service.message.AbstractResponse;  // REQUIRED
```

**Required for Services:**
```java
import com.tpa.ap.common.service.Service;  // REQUIRED
import com.tpa.ap.common.service.Service.ServiceException;  // REQUIRED
```

**Required for Clients:**
```java
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.LoggerFactory;  // REQUIRED for logging
import com.optum.ap.services.rest.common.client.ClientConfig;
import com.optum.ap.services.rest.common.client.ServiceClient;
import com.tpa.ap.common.service.Service.ServiceException;
```

---

## 10. Exception Handling Pattern (Consolidated)

### 10.1 Standard Pattern (Use This)

**ALL client methods MUST follow this exact pattern:**

```java
@Override
public {SUBDOMAIN}Response {methodName}({SUBDOMAIN}Request request) 
        throws ServiceException {
    return ({SUBDOMAIN}Response) callPostServiceMethodforObject(request);
}
```

### 10.2 Key Rules

| Rule | Status | Details |
|------|--------|---------|
| ALWAYS declare `throws ServiceException` | ✅ REQUIRED | Checked exception - caller must handle |
| ALWAYS use `@Override` annotation | ✅ REQUIRED | Compiler validation |
| ALWAYS cast return value to Response type | ✅ REQUIRED | Type safety |
| DO NOT add try-catch blocks | ❌ WRONG | Let exceptions propagate |
| DO NOT add custom logging | ❌ WRONG | Base class handles automatically |
| DO NOT declare APRestException | ❌ OPTIONAL | Unchecked runtime exception |

### 10.3 Real Example from ClaimServiceClient

```java
@Override
public ClaimSummaryResponse getClaimSummaryByKey(ClaimSummaryKeyRequest req) 
        throws ServiceException {
    return (ClaimSummaryResponse) callPostServiceMethodforObject(req);
}
```

### 10.4 Common Mistakes to AVOID

❌ **WRONG: Adding try-catch blocks**
```java
// DON'T DO THIS - let exceptions propagate
public ClaimSummaryResponse getClaimSummaryByKey(ClaimSummaryKeyRequest req) {
    try {
        return (ClaimSummaryResponse) callPostServiceMethodforObject(req);
    } catch (Exception e) {
        logger.error("Error occurred", e);  // Redundant
        throw e;
    }
}
```

❌ **WRONG: Adding custom logging**
```java
// DON'T DO THIS - base class logs automatically
public ClaimSummaryResponse getClaimSummaryByKey(ClaimSummaryKeyRequest req) 
        throws ServiceException {
    logger.debug("Calling getClaimSummaryByKey");  // Redundant
    ClaimSummaryResponse response = (ClaimSummaryResponse) callPostServiceMethodforObject(req);
    logger.debug("Received response: " + response);  // Redundant
    return response;
}
```

❌ **WRONG: Missing exception declaration**
```java
// DON'T DO THIS - must declare ServiceException
public ClaimSummaryResponse getClaimSummaryByKey(ClaimSummaryKeyRequest req) {
    return (ClaimSummaryResponse) callPostServiceMethodforObject(req);
}
```

✅ **CORRECT: Simple delegation**
```java
@Override
public ClaimSummaryResponse getClaimSummaryByKey(ClaimSummaryKeyRequest req) 
        throws ServiceException {
    return (ClaimSummaryResponse) callPostServiceMethodforObject(req);
}
```

---

## 11. Logging Pattern

### 11.1 Logger Setup (REQUIRED)

```java
// 1. Import SLF4J logger
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

// 2. Initialize in constructor (inherited from ServiceClient)
private {SUBDOMAIN}ServiceClient(String serviceName) {
    clientConfig = new ClientConfig(serviceName, SERVICE_CLASS, BASE_URL);
    logger = LoggerFactory.getLogger({SUBDOMAIN}ServiceClient.class);  // ← CRITICAL
}
```

### 11.2 Where Logging is Used

| Location | Handler | Level |
|----------|---------|-------|
| Request/response | ServiceClient base class | DEBUG |
| Exceptions | ServiceClient base class | ERROR |
| Performance metrics | ServiceClient base class | TRACE |
| Client methods | NOT NEEDED | (handled automatically) |

### 11.3 Key Rule

**NO explicit logging needed in client methods** - the `ServiceClient` base class handles all logging automatically.

---

## 12. ServiceClient Method Explanation

### 12.1 `callPostServiceMethodforObject(request)` - Primary REST Call Method

**Purpose:** Primary method for making REST API calls to backend services.

**What It Does:**
1. Serializes the request object to JSON
2. Makes HTTP POST call to configured BASE_URL endpoint
3. Deserializes JSON response to expected response type
4. Handles HTTP errors and wraps them appropriately
5. Logs request/response for debugging

**How It Determines Response Type:**
- Uses Java reflection to inspect method's return type
- Automatically deserializes to correct Response class
- No manual type specification needed

**Example Flow:**
```java
// 1. Client method called
public ClaimSummaryResponse getClaimSummaryByKey(ClaimSummaryKeyRequest req) 
        throws ServiceException {
    // 2. Calls base class method
    return (ClaimSummaryResponse) callPostServiceMethodforObject(req);
    
    // What ServiceClient base class does:
    // - Serializes ClaimSummaryKeyRequest to JSON
    // - POST to: {baseUrl}/ap/rest/data/claim/v1.0/getClaimSummaryByKey
    // - Receives JSON response
    // - Deserializes to ClaimSummaryResponse
    // - Logs request/response (DEBUG level)
    // - Returns typed object
}
```

**Critical Rules:**
- ALWAYS cast return value to expected Response type
- Method name in client MUST match service interface method name
- Request object MUST match service interface parameter type

---

## 13. Client Implementation Guidelines

### 13.1 Client Overview

Client classes implement service interfaces to make REST API calls to backend services. They extend `ServiceClient` and follow a singleton pattern.

### 13.2 Client Structure Pattern

**When creating Client classes:**
1. MUST extend `ServiceClient` (from `com.optum.ap.services.rest.common.client.ServiceClient`)
2. MUST implement corresponding Service interface from your domain
3. MUST follow singleton pattern with instance caching
4. MUST include service metadata constants
5. MUST handle `ServiceException`

### 13.3 Critical Client Rules

| Rule | Details |
|------|---------|
| **Naming Convention** | `{SUBDOMAIN}ServiceClient` (e.g., `ClaimServiceClient`) |
| **Extends** | `ServiceClient` |
| **Implements** | Corresponding `{SUBDOMAIN}Service` interface |
| **Package** | `com.tpa.ap.{DOMAIN}.client.ejb` |
| **Singleton Pattern** | Instance caching with thread-safe map |

---

## 14. Complete Client Example

```java
// filepath: src/main/java/com/tpa/ap/{DOMAIN}/client/ejb/{SUBDOMAIN}ServiceClient.java
package com.tpa.ap.{DOMAIN}.client.ejb;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.LoggerFactory;

import com.tpa.ap.{DOMAIN}.service.{SUBDOMAIN}Service;
import com.tpa.ap.{DOMAIN}.message.{subdomain}.{SUBDOMAIN}Request;
import com.tpa.ap.{DOMAIN}.message.{subdomain}.{SUBDOMAIN}Response;
import com.optum.ap.services.rest.common.client.ClientConfig;
import com.optum.ap.services.rest.common.client.ServiceClient;
import com.tpa.ap.common.service.Service.ServiceException;

/**
 * REST client implementation for {subdomain} service operations.
 * 
 * <p>This client implements the singleton pattern to ensure only one instance
 * per service name exists. It delegates all service calls to the backend
 * REST API endpoint configured in BASE_URL.
 * 
 * @author Generated via GitHub Copilot
 * @version 1.0
 * @see ServiceClient
 * @see {SUBDOMAIN}Service
 */
public class {SUBDOMAIN}ServiceClient extends ServiceClient 
        implements {SUBDOMAIN}Service {

    /** Service name identifier - format: ap-{DOMAIN}-service-rest */
    private static final String SERVICE_NAME = "ap-{DOMAIN}-service-rest";
    
    /** Service interface class reference for configuration */
    private static final Class SERVICE_CLASS = {SUBDOMAIN}Service.class;
    
    /** Base URL path - format: /ap/rest/{DOMAIN}/{subdomain}/v1.0 */
    private static final String BASE_URL = "/ap/rest/{DOMAIN}/{subdomain}/v1.0";
    
    /** Thread-safe cache of client instances keyed by service name */
    private static final Map<String, {SUBDOMAIN}ServiceClient> instanceMap = 
        new ConcurrentHashMap<String, {SUBDOMAIN}ServiceClient>();

    /**
     * Private constructor to enforce singleton pattern.
     * 
     * @param serviceName the service name for configuration lookup
     */
    private {SUBDOMAIN}ServiceClient(String serviceName) {
        clientConfig = new ClientConfig(serviceName, SERVICE_CLASS, BASE_URL);
        logger = LoggerFactory.getLogger({SUBDOMAIN}ServiceClient.class);
    }

    /**
     * Gets the singleton instance using default service name.
     * 
     * @return the singleton client instance
     */
    public static {SUBDOMAIN}ServiceClient getInstance() {
        return getParameterizedInstance(SERVICE_NAME);
    }

    /**
     * Gets singleton instance for specified service name.
     * 
     * @param serviceName the service name for configuration lookup
     * @return the singleton client instance for this service name
     */
    public static {SUBDOMAIN}ServiceClient getParameterizedInstance(
            String serviceName) {
        if (!instanceMap.containsKey(serviceName)) {
            instanceMap.put(serviceName, 
                new {SUBDOMAIN}ServiceClient(serviceName));
        }
        return instanceMap.get(serviceName);
    }

    // ****************************************************
    // *** Service Methods (in alphabetical order) ***
    // ****************************************************

    @Override
    public {SUBDOMAIN}Response get{SUBDOMAIN}Data({SUBDOMAIN}Request request) 
            throws ServiceException {
        return ({SUBDOMAIN}Response) callPostServiceMethodforObject(request);
    }
}
```

---

## 15. getCacheKey() Pattern (UPDATED)

### 15.1 Purpose and Rule

The `getCacheKey()` method returns a unique string identifier for caching.

**Pattern:** Use `CacheUtil.getCacheString()` to format values and combine with StringBuilder.

**Rule:** Combine class name + `loginId` + all **search/filter fields** (NOT pagination fields)

### 15.2 Implementation Pattern

```java
@Override
public String getCacheKey() {
    StringBuilder key = new StringBuilder();
    
    // 1. Class name for clarity
    key.append(CacheUtil.getCacheString(this.getClass().getSimpleName()));
    
    // 2. LoginId (required - audit trail)
    key.append(CacheUtil.getCacheString(loginId));
    
    // 3. Search/filter fields (business criteria)
    key.append(CacheUtil.getCacheString(memberId));
    key.append(CacheUtil.getCacheString(groupId, true));  // boolean = required flag
    
    // 4. DO NOT include pagination
    // key.append(CacheUtil.getCacheString(pageNumber));  // ❌ WRONG
    // key.append(CacheUtil.getCacheString(pageSize));    // ❌ WRONG
    
    return key.toString();
}
```

### 15.3 Common Mistakes to AVOID

❌ **WRONG: Including pagination in cache key**
```java
// DON'T DO THIS - cache key should NOT include pagination
@Override
public String getCacheKey() {
    return loginId + ":" + claimNumber + ":" + pageNumber + ":" + pageSize;
    // ↑ pageNumber and pageSize should NOT be here
    // Each page would have different key = no cache hit for other pages
}
```

✅ **CORRECT: Exclude pagination fields**
```java
// DO THIS - pagination is not part of the cache key
@Override
public String getCacheKey() {
    return loginId + ":" + claimNumber + ":" + effectiveDate;
    // Pagination affects WHICH results to return, not WHICH request was made
}
```

---

## 16. Response Collection Setter Pattern

### 16.1 Null-Safe Setters

Collections MUST handle null inputs safely:

```java
public class ClaimSearchResponse extends AbstractResponse {
    private List<ClaimDto> claims = new ArrayList<>();
    
    // Getter: Never returns null
    public List<ClaimDto> getClaims() {
        return claims;
    }
    
    // Setter: Null-safe - converts null to empty ArrayList
    public void setClaims(List<ClaimDto> claims) {
        this.claims = (claims != null) ? claims : new ArrayList<>();
    }
}
```

### 16.2 Key Rules

| Rule | Details |
|------|---------|
| **Initialize** | `private List<...> items = new ArrayList<>();` |
| **Getter** | Returns collection, never null |
| **Setter** | Checks for null, converts to empty collection |
| **Applies to** | List, Set, Map fields in Response classes |

### 16.3 Examples with Multiple Collections

```java
public class ComplexResponse extends AbstractResponse {
    private List<ClaimDto> claims = new ArrayList<>();
    private Map<String, String> metadata = new HashMap<>();
    private Set<String> claimIds = new HashSet<>();
    
    // List setter
    public void setClaims(List<ClaimDto> claims) {
        this.claims = (claims != null) ? claims : new ArrayList<>();
    }
    
    // Map setter
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = (metadata != null) ? metadata : new HashMap<>();
    }
    
    // Set setter
    public void setClaimIds(Set<String> claimIds) {
        this.claimIds = (claimIds != null) ? claimIds : new HashSet<>();
    }
}
```

---

## 17. Service Interface Example

### 17.1 Complete Service Interface with Methods in Alphabetical Order

```java
// filepath: src/main/java/com/tpa/ap/{DOMAIN}/service/{SUBDOMAIN}Service.java
package com.tpa.ap.{DOMAIN}.service;

import com.tpa.ap.common.service.Service;
import com.tpa.ap.common.service.Service.ServiceException;
import com.tpa.ap.{DOMAIN}.message.{subdomain}.{SUBDOMAIN}Request;
import com.tpa.ap.{DOMAIN}.message.{subdomain}.{SUBDOMAIN}Response;

/**
 * Service interface for {subdomain} operations.
 * 
 * <p>Defines contract for {subdomain} service operations. All implementations
 * MUST throw ServiceException for any processing failures.
 * 
 * @author Generated via GitHub Copilot
 * @version 1.0
 */
public interface {SUBDOMAIN}Service extends Service {
    
    /**
     * Get claim data for multiple claims in a single request.
     * 
     * @param req request containing search criteria
     * @return response with claim data list
     * @throws ServiceException if operation fails
     */
    ClaimDetailListResponse getClaimAllDataList(ClaimSummaryRequest req) 
            throws ServiceException;
    
    /**
     * Search claims by TIN number using advanced search criteria.
     * 
     * @param req advanced search request with TIN
     * @return response with matching claims
     * @throws ServiceException if operation fails
     */
    ClaimAdvanceSearchResponse getClaimsByTinNumber(
            ClaimAdvanceSearchRequest req)
            throws ServiceException;
    
    /**
     * Retrieve claim detail for a specific claim key.
     * 
     * @param req request with claim identifier
     * @return response with claim detail
     * @throws ServiceException if operation fails
     */
    ClaimDetailResponse getClaimDetailByKey(ClaimDetailKeyRequest req) 
            throws ServiceException;
    
    /**
     * Retrieve list of claim details based on search criteria.
     * 
     * @param req request with search criteria
     * @return response with claim detail list
     * @throws ServiceException if operation fails
     */
    ClaimDetailListResponse getClaimDetailList(ClaimDetailListRequest req) 
            throws ServiceException;
    
    /**
     * Retrieve v2 format claim detail list.
     * 
     * @param req request with search criteria
     * @return response with claim detail list (v2 format)
     * @throws ServiceException if operation fails
     */
    ClaimDetailListV2Response getClaimDetailListV2(ClaimDetailListRequest req) 
            throws ServiceException;
    
    /**
     * Get lookup codes for claims.
     * 
     * @param req codes lookup request
     * @return response with codes
     * @throws ServiceException if operation fails
     */
    CodesResponse getCodes(CodesRequest req) 
            throws ServiceException;
    
    /**
     * Get distinct claim coverage codes.
     * 
     * @param req request with coverage criteria
     * @return response with distinct coverage codes
     * @throws ServiceException if operation fails
     */
    ClaimCodeSummaryListResponse getDistinctClaimCoverage(
            ClaimSummaryRequest req) 
            throws ServiceException;
    
    /**
     * Retrieve claim summary by claim key.
     * 
     * @param req request with claim identifier
     * @return response with claim summary
     * @throws ServiceException if operation fails
     */
    ClaimSummaryResponse getClaimSummaryByKey(ClaimSummaryKeyRequest req) 
            throws ServiceException;
    
    /**
     * Retrieve list of claim summaries.
     * 
     * @param req request with search criteria
     * @return response with claim summary list
     * @throws ServiceException if operation fails
     */
    ClaimSummaryListResponse getClaimSummaryList(ClaimSummaryRequest req) 
            throws ServiceException;
}
```

**Notice:** Methods are ordered alphabetically: getClaimAllDataList → getClaimsByTinNumber → getClaimDetailByKey → ... → getClaimSummaryList

---

## 18. loginId Requirement (Clarified)

### 18.1 The Rule

| Artifact | Rule | Reason |
|----------|------|--------|
| **Request** | MUST include `loginId: String` | Audit trail - identifies who made request |
| **Response** | SHOULD NOT include `loginId` | Response is results, not request metadata |

### 18.2 Example

```java
// Request class - HAS loginId
public class ClaimSearchRequest extends AbstractRequest {
    private String loginId;           // ← REQUIRED
    private String claimNumber;
    private String effectiveDate;
    // ...
}

// Response class - NO loginId
public class ClaimSearchResponse extends AbstractResponse {
    private List<ClaimDto> claims;    // ← Response data
    private Integer totalCount;
    // Note: NO loginId field here
    // ...
}
```

### 18.3 Rationale

- **Request:** `loginId` identifies who made the request (security/audit)
- **Response:** Contains business results, not request metadata
- **Separation:** Keep request and response concerns separate
- **Reusability:** Response can be used independent of who requested it

---

## 19. equals() and hashCode() Pattern (WITH IMPORTS)

### 19.1 Required Import

```java
import java.util.Objects;  // ← REQUIRED for equals/hashCode
```

### 19.2 Implementation Pattern

Use stable business key fields only (typically the ID or primary identifier):

```java
@Override
public int hashCode() { 
    return Objects.hash(claimId);
}

@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ClaimDto)) return false;
    ClaimDto other = (ClaimDto) o;
    return Objects.equals(this.claimId, other.claimId);
}
```

### 19.3 Important Rules

| Rule | Details |
|------|---------|
| **Avoid collections** | Performance impact, recursion risk |
| **Avoid mutable fields** | Use immutable identifiers only |
| **Use business key** | claimId, memberId, etc. - not all fields |
| **Immutability** | Never change hashCode after object creation |

### 19.4 Increment serialVersionUID if Changed

When you modify `equals()` or `hashCode()`:
```java
private static final long serialVersionUID = 2L;  // ← Increment from 1L
```

---

## 20. @Override Annotation Rule

### 20.1 The Rule

**ALWAYS use `@Override` annotation on ALL method implementations:**

### 20.2 For Service Interface Methods

```java
@Override
public ClaimSummaryResponse getClaimSummaryByKey(ClaimSummaryKeyRequest req) 
        throws ServiceException {
    return (ClaimSummaryResponse) callPostServiceMethodforObject(req);
}
```

### 20.3 For Base Class Method Overrides

```java
@Override
public String toString() {
    return "ClaimSearchRequest{...}";
}

@Override
public int hashCode() {
    return Objects.hash(claimId);
}

@Override
public boolean equals(Object o) {
    // ...
}
```

### 20.4 Benefits

| Benefit | Details |
|---------|---------|
| **Compiler Validation** | Catches typos and signature mismatches |
| **IDE Support** | Better refactoring and navigation |
| **Code Clarity** | Makes intent explicit |
| **Best Practice** | Standard in professional Java code |

---

## 21. BASE_URL Patterns (Complete Table)

### 21.1 Standard Pattern

**Format:** `/ap/rest/{DOMAIN}/{subdomain}/v{VERSION}`

### 21.2 Domain-Specific URLs

| Domain | Example BASE_URL | Structure | Notes |
|--------|------------------|-----------|-------|
| **data** | `/ap/rest/data/claim/v1.0` | `/ap/rest/{DOMAIN}/{subdomain}/v1.0` | Standard format |
| **security** | `/ap/rest/security/user/v1.0` | `/ap/rest/{DOMAIN}/{subdomain}/v1.0` | Standard format |
| **trans** | `/ap/rest/trans/transaction/v1.0` | `/ap/rest/{DOMAIN}/{subdomain}/v1.0` | Uses 'trans' not 'transaction' |
| **ems** | `/ap/rest/mw/ems/event/v1.0` | `/ap/rest/mw/{DOMAIN}/{subdomain}/v1.0` | Middleware path with 'mw' prefix |
| **provdir** | `/ap/rest/provdir/provider/v1.0` | `/ap/rest/{DOMAIN}/{subdomain}/v1.0` | Provider directory |
| **job** | `/ap/rest/job/batch/v1.0` | `/ap/rest/{DOMAIN}/{subdomain}/v1.0` | Job management |
| **gateway** | `/ap/rest/gateway/api/v1.0` | `/ap/rest/{DOMAIN}/{subdomain}/v1.0` | API gateway |

### 21.3 Important Notes

- BASE_URL does NOT include hostname (added by ServiceClient base class)
- Replace `{DOMAIN}` and `{subdomain}` with actual values
- Version segment (v1.0) may vary by domain

---

## 22. Required Constants

### 22.1 Service Metadata Constants

**Replace `{DOMAIN}`, `{SUBDOMAIN}`, and `{subdomain}` with actual values:**

```java
private static final String SERVICE_NAME = "ap-{DOMAIN}-service-rest";
private static final Class SERVICE_CLASS = {SUBDOMAIN}Service.class;
private static final String BASE_URL = "/ap/rest/{DOMAIN}/{subdomain}/v1.0";
private static final Map<String, {SUBDOMAIN}ServiceClient> instanceMap = 
    new ConcurrentHashMap<String, {SUBDOMAIN}ServiceClient>();
```

### 22.2 Real Examples

**ap-data-client - MemberClient:**
```java
private static final String SERVICE_NAME = "ap-data-service-rest";
private static final Class SERVICE_CLASS = MemberService.class;
private static final String BASE_URL = "/ap/rest/data/member/v1.0";
private static final Map<String, MemberServiceClient> instanceMap = 
    new ConcurrentHashMap<String, MemberServiceClient>();
```

**ap-security-client - UserClient:**
```java
private static final String SERVICE_NAME = "ap-security-service-rest";
private static final Class SERVICE_CLASS = UserService.class;
private static final String BASE_URL = "/ap/rest/security/user/v1.0";
private static final Map<String, UserServiceClient> instanceMap = 
    new ConcurrentHashMap<String, UserServiceClient>();
```

---

## 23. Singleton Pattern

### 23.1 Private Constructor

```java
/**
 * Private constructor to enforce singleton pattern.
 * 
 * @param serviceName the service name for configuration lookup
 */
private {SUBDOMAIN}ServiceClient(String serviceName) {
    clientConfig = new ClientConfig(serviceName, SERVICE_CLASS, BASE_URL);
    logger = LoggerFactory.getLogger({SUBDOMAIN}ServiceClient.class);
}
```

### 23.2 Default getInstance (Thread-Safe)

```java
/**
 * Gets the singleton instance using default service name.
 * 
 * @return the singleton client instance
 */
public static {SUBDOMAIN}ServiceClient getInstance() {
    return getParameterizedInstance(SERVICE_NAME);
}
```

### 23.3 Parameterized getInstance with Instance Caching

```java
/**
 * Gets singleton instance for specified service name.
 * 
 * @param serviceName the service name for configuration lookup
 * @return the singleton client instance for this service name
 */
public static {SUBDOMAIN}ServiceClient getParameterizedInstance(
        String serviceName) {
    if (!instanceMap.containsKey(serviceName)) {
        instanceMap.put(serviceName, new {SUBDOMAIN}ServiceClient(serviceName));
    }
    return instanceMap.get(serviceName);
}
```

### 23.4 Benefits of Singleton Pattern

| Benefit | Details |
|---------|---------|
| **Single Instance** | Only one client per service name |
| **Resource Efficient** | Reuses connections and configuration |
| **Thread-Safe** | ConcurrentHashMap prevents race conditions |
| **Lazy Initialization** | Instance created on first use |

---

## 24. Common Mistakes to AVOID

### 24.1 DTO Mistakes

❌ **WRONG: Returning null for collections**
```java
public List<ClaimDto> getClaims() {
    return null;  // NEVER do this
}
```

✅ **CORRECT: Return empty collection**
```java
private List<ClaimDto> claims = new ArrayList<>();  // Initialize

public List<ClaimDto> getClaims() {
    return claims;  // Never null
}
```

---

### 24.2 Request Mistakes

❌ **WRONG: Missing loginId**
```java
public class ClaimSearchRequest extends AbstractRequest {
    private String claimNumber;
    private String effectiveDate;
    // Missing loginId!
}
```

✅ **CORRECT: Include loginId**
```java
public class ClaimSearchRequest extends AbstractRequest {
    private String claimNumber;
    private String effectiveDate;
    private String loginId;  // ← REQUIRED
}
```

---

### 24.3 Response Mistakes

❌ **WRONG: Not null-safe for collections**
```java
public void setClaims(List<ClaimDto> claims) {
    this.claims = claims;  // Will fail if null
}
```

✅ **CORRECT: Null-safe**
```java
public void setClaims(List<ClaimDto> claims) {
    this.claims = (claims != null) ? claims : new ArrayList<>();
}
```

---

### 24.4 Service Mistakes

❌ **WRONG: Including business logic**
```java
public interface ClaimService extends Service {
    ClaimSummaryResponse getClaimSummary(ClaimSummaryRequest req) 
            throws ServiceException;
    
    void validateClaimData(ClaimDto claim) {  // ← NO - business logic
        // ...
    }
}
```

✅ **CORRECT: Pure contract**
```java
public interface ClaimService extends Service {
    ClaimSummaryResponse getClaimSummary(ClaimSummaryRequest req) 
            throws ServiceException;
    
    // No implementations, no business logic
}
```

---

### 24.5 Client Mistakes

❌ **WRONG: Adding try-catch**
```java
@Override
public ClaimSummaryResponse getClaimSummaryByKey(ClaimSummaryKeyRequest req) 
        throws ServiceException {
    try {
        return (ClaimSummaryResponse) callPostServiceMethodforObject(req);
    } catch (Exception e) {
        logger.error("Error", e);  // Redundant
        throw e;
    }
}
```

✅ **CORRECT: Simple delegation**
```java
@Override
public ClaimSummaryResponse getClaimSummaryByKey(ClaimSummaryKeyRequest req) 
        throws ServiceException {
    return (ClaimSummaryResponse) callPostServiceMethodforObject(req);
}
```

---

## 25. Prompt Checklist (Before Code Generation)

**ALWAYS provide this information before asking for code generation:**

### 25.1 Essential Context

- [ ] **Domain:** Which domain? (data, security, trans, ems, etc.)
- [ ] **Subdomain:** Which subdomain? (claim, member, user, etc.)
- [ ] **Artifact Type:** DTO, Request, Response, Service, Client?
- [ ] **Version:** Run version detection (see section 6)
- [ ] **Fields:** List of field names and types

### 25.2 Additional Details

- [ ] **Field Descriptions:** For JavaDoc comments
- [ ] **Cache Key Fields:** For Request.getCacheKey() (see section 15)
- [ ] **Business Key Fields:** For equals/hashCode (see section 19)
- [ ] **Collection Fields:** Names and element types
- [ ] **Special Behaviors:** Any custom logic needed?

### 25.3 Example Complete Prompt

```
Create ClaimSearchRequest in data domain, claim subdomain, v4.

Fields:
- claimNumber: String (required - search criteria)
- effectiveDate: String (optional - filter date)
- includeHistory: Boolean (optional - include historical data)
- loginId: String (required - audit trail)

Cache key: loginId + ":" + claimNumber + ":" + effectiveDate
Business key for equals/hashCode: claimNumber

Follow instructions from client-instructions.md.
```

---

## 26. Summary & Trust These Instructions

### 26.1 What These Instructions Cover

These instructions have been validated against:
- All actual repository code patterns
- Base class implementations
- Domain structure and naming conventions
- Version strategy across all repositories
- Build and compilation requirements

### 26.2 When to Trust These Instructions

✅ **TRUST for:**
- Creating DTOs, Requests, Responses
- Creating Service interfaces
- Creating Client implementations
- Naming conventions and packages
- Field ordering and type rules
- Exception handling patterns
- Singleton pattern implementation

### 26.3 When to Search the Codebase

🔍 **SEARCH the repo if:**
- Information here is incomplete for your specific task
- You encounter an error not documented here
- You need to understand complex domain business logic
- You need to verify package structures for new domains

### 26.4 Quick Start (One-Time Setup)

```bash
# Step 1: Identify your domain
ls src/main/java/com/tpa/ap/

# Step 2: Detect version
ls -d src/main/java/com/tpa/ap/{DOMAIN}/dto/{subdomain}/v* 2>/dev/null | sort -V | tail -1

# Step 3: Compile to verify setup
mvn clean compile

# Step 4: Use prompts from section 25 to generate code
```

### 26.5 Key Takeaways

| Item | Remember |
|------|----------|
| **Placeholder Usage** | {DOMAIN} lowercase, {SUBDOMAIN} PascalCase, {subdomain} lowercase |
| **Alphabetical Order** | Always for fields and methods |
| **Collections** | Initialize to empty, never return null |
| **Exceptions** | Declare ServiceException, let it propagate |
| **Logging** | Base class handles it automatically |
| **Singleton** | Use instance caching with ConcurrentHashMap |
| **Cache Key** | loginId + search fields (NOT pagination) |
| **Version** | Use HIGHEST existing version |

---

**Remember:** Always identify your {DOMAIN} first by checking `src/main/java/com/tpa/ap/` and replace placeholder values throughout these instructions with your actual domain name.

**Trust these instructions. Follow them precisely. Ask Copilot to generate code using prompts from section 25.**
