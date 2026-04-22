# Java Coding Standards & Patterns

This file documents general-purpose Java coding standards, patterns, and best practices for all Java code in this workspace, covering aspects NOT addressed in web-instructions.md, rest-instructions.md, or client-instructions.md.

---

## 1. Purpose & Scope

- Applies to all Java classes in tpa/ap and com/optum modules
- Covers utility classes, domain models, enums, exceptions, and shared components
- Complements service-layer, web-layer, and client-library patterns
- Establishes consistent code style, naming, and architectural patterns across the workspace

---

## 2. Class Organization & Structure

### 2.1 Class Declaration Order

Organize class members in this order:

```java
public class MyClass {
    
    // 1. Constants (public static final)
    public static final String CONSTANT_NAME = "value";
    private static final Logger logger = Logger.getLogger(MyClass.class);
    
    // 2. Static variables
    private static int staticCounter = 0;
    
    // 3. Instance variables (private, organized by visibility)
    private String instanceVar;
    private List<Item> items;
    
    // 4. Constructors
    public MyClass() { }
    public MyClass(String param) { }
    
    // 5. Public methods
    public void publicMethod() { }
    
    // 6. Protected methods
    protected void protectedMethod() { }
    
    // 7. Package-private methods
    void packagePrivateMethod() { }
    
    // 8. Private methods
    private void privateMethod() { }
    
    // 9. Inner classes / enums
    public enum Status { }
    private static class InnerClass { }
}
```

### 2.2 File Organization

One public class per file (exceptions: nested/inner classes are OK in same file).

**File naming:**
- Match public class name: `MyClass.java` for `public class MyClass`
- Utility classes: `{Name}Utility.java` or `{Name}Utils.java`
- Exception classes: `{Feature}Exception.java` or `{Feature}Error.java`

### 2.3 Imports

- Organize imports: `java.*`, `javax.*`, then third-party, then project-specific
- Use specific imports, NOT wildcard imports (`import java.util.*` is NOT allowed)
- Remove unused imports before commit
- IDE: Configure auto-organize imports

```java
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.apache.log4j.Logger;

import com.tpa.ap.domain.model.Member;
import com.tpa.ap.service.MemberService;
```

---

## 3. Naming Conventions

### 3.1 Package Names

- All lowercase, no hyphens or underscores
- Hierarchical: `com.tpa.ap.{domain}.{layer}.{feature}`
- Examples:
  - `com.tpa.ap.benefits.service` (service layer)
  - `com.tpa.ap.benefits.model` (domain models)
  - `com.tpa.ap.benefits.util` (utilities)
  - `com.tpa.ap.claims.client` (client library)

### 3.2 Class Names

| Type | Pattern | Examples |
|------|---------|----------|
| Regular class | PascalCase | `MemberService`, `BenefitCalculator` |
| Abstract class | `Abstract{Name}` | `AbstractRepository`, `AbstractManager` |
| Exception class | `{Feature}Exception` | `MemberNotFoundException`, `InvalidBenefitException` |
| Enum | PascalCase | `MemberStatus`, `ClaimState` |
| Interface | `I{Name}` or `{Name}Interface` | `IMemberService`, `BenefitInterface` |
| Test class | `{Class}Test` | `MemberServiceTest`, `BenefitCalculatorTest` |
| Utility class | `{Name}Utility` or `{Name}Utils` | `DateUtils`, `StringUtility` |

### 3.3 Method Names

- camelCase (first letter lowercase)
- Verb-based names
- Boolean methods: `is*`, `has*`, `can*`
- Factory methods: `create*`, `build*`, `get*`

```java
// ✅ CORRECT
public void processMember(Member member) { }
public boolean isValid() { }
public boolean hasPermission(String role) { }
public static Member createMember(String id) { }
public Optional<Member> getMemberById(String id) { }

// ❌ WRONG
public void process_member(Member member) { }
public boolean Valid() { }
public boolean MemberValid() { }
```

### 3.4 Variable Names

- camelCase (first letter lowercase)
- Descriptive names (no single letters except loop indices: i, j, k)
- Constants: `UPPER_CASE_WITH_UNDERSCORES`

```java
// ✅ CORRECT
private String memberName;
private int memberCount;
private List<Benefit> benefits;
private static final String DEFAULT_TIMEZONE = "UTC";
for (int i = 0; i < list.size(); i++) { }  // i is acceptable for loops

// ❌ WRONG
private String m;
private int cnt;
private static final String defaultTimezone = "UTC";
for (int index = 0; index < list.size(); index++) { }  // use i instead
```

---

## 4. Code Style & Formatting

### 4.1 Line Length

- Maximum 120 characters per line
- Split long lines at logical points

```java
// ✅ CORRECT: Split long lines
public void processLargeDataSet(
        String memberId, 
        List<Claim> claims, 
        Map<String, BenefitData> benefits) {
    // method body
}

// ❌ WRONG: Line too long
public void processLargeDataSet(String memberId, List<Claim> claims, Map<String, BenefitData> benefits) {
```

### 4.2 Indentation

- Use 4 spaces (NOT tabs)
- Continuation lines: 8 spaces (double indent)

```java
// ✅ CORRECT: 4 space indentation
public class MyClass {
    private String name;
    
    public void method() {
        if (condition) {
            doSomething();
        }
    }
}

// Continuation line: 8 spaces
List<String> longList = Arrays.asList(
        "item1", "item2", "item3");
```

### 4.3 Braces

- Opening brace on same line (Java style, not C++ style)
- Closing brace on new line
- No braces for single-statement blocks... actually **always use braces**

```java
// ✅ CORRECT: Braces on same line
if (condition) {
    doSomething();
} else {
    doOtherThing();
}

for (Item item : items) {
    processItem(item);
}

// ❌ WRONG: No braces for single statements
if (condition)
    doSomething();

// ❌ WRONG: Brace on new line (C++ style)
if (condition)
{
    doSomething();
}
```

### 4.4 Spacing

```java
// ✅ CORRECT: Spaces around operators and keywords
int result = a + b;
if (condition) { }
for (int i = 0; i < 10; i++) { }
Map<String, String> map = new HashMap<>();

// ❌ WRONG: No spaces
int result=a+b;
if(condition){ }
for(int i=0;i<10;i++){ }
```

---

## 5. Immutability & Final

### 5.1 Use `final` Liberally

```java
// ✅ CORRECT: Final variables prevent accidental reassignment
private final String memberId;
private final List<Benefit> benefits;

public MyClass(String memberId, List<Benefit> benefits) {
    this.memberId = memberId;
    this.benefits = new ArrayList<>(benefits);  // Defensive copy
}

public final String getMemberId() {
    return memberId;
}
```

### 5.2 Immutable Collections

Return immutable copies to prevent external modification:

```java
// ✅ CORRECT: Defensive copy + immutable return
private List<Benefit> benefits;

public List<Benefit> getBenefits() {
    return Collections.unmodifiableList(benefits);
}

// ❌ WRONG: Returns mutable reference
public List<Benefit> getBenefits() {
    return benefits;  // Caller can modify!
}
```

### 5.3 Immutable Objects

Prefer immutable classes for data transfer objects:

```java
// ✅ CORRECT: Immutable DTO
public final class MemberDTO {
    private final String memberId;
    private final String name;
    private final LocalDate dateOfBirth;
    
    public MemberDTO(String memberId, String name, LocalDate dateOfBirth) {
        this.memberId = memberId;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getMemberId() { return memberId; }
    public String getName() { return name; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    
    // No setters
}

// ❌ WRONG: Mutable DTO
public class MemberDTO {
    public String memberId;
    public String name;
    
    public void setMemberId(String id) { }  // Can be modified after creation
}
```

---

## 6. Null Safety & Optional

### 6.1 Avoid Null References

Use `java.util.Optional` instead of returning `null`:

```java
// ✅ CORRECT: Use Optional
public Optional<Member> getMemberById(String id) {
    if (id == null || !repository.exists(id)) {
        return Optional.empty();
    }
    return Optional.of(repository.findById(id));
}

// Caller handles Optional
Optional<Member> member = getMemberById("123");
member.ifPresent(m -> logger.info("Found: " + m.getName()));

// ❌ WRONG: Return null
public Member getMemberById(String id) {
    if (id == null || !repository.exists(id)) {
        return null;
    }
    return repository.findById(id);
}

// Caller must check for null
Member member = getMemberById("123");
if (member != null) {
    logger.info("Found: " + member.getName());
}
```

### 6.2 Null Checks

Always validate inputs early:

```java
// ✅ CORRECT: Null check at method start
public void processMember(Member member) {
    if (member == null) {
        throw new IllegalArgumentException("Member cannot be null");
    }
    // Safe to use member
}

// ✅ ALSO CORRECT: Use Objects.requireNonNull
public void processMember(Member member) {
    Objects.requireNonNull(member, "Member cannot be null");
    // Safe to use member
}

// ❌ WRONG: No null check
public void processMember(Member member) {
    String name = member.getName();  // Potential NPE
}
```

---

## 7. Exception Handling

### 7.1 Exception Hierarchy

**Create domain-specific exceptions:**

```java
// ✅ CORRECT: Custom exceptions for domain
public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException(String memberId) {
        super("Member not found: " + memberId);
    }
}

public class InvalidBenefitException extends RuntimeException {
    public InvalidBenefitException(String message, Throwable cause) {
        super(message, cause);
    }
}

// Usage
try {
    Member member = repository.findById(id);
    if (member == null) {
        throw new MemberNotFoundException(id);
    }
} catch (MemberNotFoundException e) {
    logger.error("Error: " + e.getMessage());
}
```

### 7.2 Catch Specific Exceptions

```java
// ✅ CORRECT: Catch specific exceptions
try {
    processMember(member);
} catch (MemberNotFoundException e) {
    logger.error("Member not found", e);
    throw e;
} catch (InvalidBenefitException e) {
    logger.error("Invalid benefit", e);
    // Handle gracefully
} catch (ServiceException e) {
    logger.error("Service error", e);
    throw new RuntimeException("Service unavailable", e);
}

// ❌ WRONG: Catch all exceptions
try {
    processMember(member);
} catch (Exception e) {
    logger.error("Error", e);  // Too broad
}

// ❌ WRONG: Catch throwable
try {
    processMember(member);
} catch (Throwable t) {
    logger.error("Error", t);  // Never catch Throwable
}
```

### 7.3 Exception Logging

Log the full stack trace with context:

```java
// ✅ CORRECT: Log with exception
try {
    processMember(member);
} catch (ServiceException e) {
    logger.error("Error processing member: " + memberId, e);  // Include stack trace
    throw new RuntimeException("Failed to process member", e);
}

// ❌ WRONG: Log only message
try {
    processMember(member);
} catch (ServiceException e) {
    logger.error("Error: " + e.getMessage());  // Lost stack trace
}

// ❌ WRONG: Log without context
try {
    processMember(member);
} catch (ServiceException e) {
    logger.error("Error occurred", e);  // No context
}
```

---

## 8. Collections & Generics

### 8.1 Use Generics

Always specify type parameters:

```java
// ✅ CORRECT: Generic types specified
List<String> names = new ArrayList<>();
Map<String, Integer> ages = new HashMap<>();
Set<Member> members = new HashSet<>();

// ❌ WRONG: Raw types
List names = new ArrayList();  // Raw type
Map ages = new HashMap();      // Raw type
```

### 8.2 Collection Initialization

```java
// ✅ CORRECT: Use diamond operator
List<String> items = new ArrayList<>();

// ✅ ALSO CORRECT: Static initialization (immutable)
List<String> items = Arrays.asList("a", "b", "c");  // For fixed-size lists
List<String> items = new ArrayList<>(Arrays.asList("a", "b", "c"));  // For mutable

// ✅ MODERN: Use List.of (Java 9+)
List<String> items = List.of("a", "b", "c");
Map<String, String> map = Map.of("key1", "value1", "key2", "value2");

// ❌ WRONG: Diamond operator not used
List<String> items = new ArrayList<String>();  // Redundant type
```

### 8.3 Iteration Patterns

```java
// ✅ CORRECT: Enhanced for loop
for (String item : items) {
    processItem(item);
}

// ✅ ALSO CORRECT: Streams (Java 8+)
items.stream()
    .filter(item -> !item.isEmpty())
    .map(String::toUpperCase)
    .forEach(System.out::println);

// ✅ ALSO CORRECT: Lambdas with collections
items.forEach(item -> processItem(item));

// ❌ WRONG: Old-style iterator
Iterator<String> it = items.iterator();
while (it.hasNext()) {
    String item = it.next();
    processItem(item);
}
```

---

## 9. String Operations

### 9.1 String Concatenation

Use string builders for loops or multiple concatenations:

```java
// ✅ CORRECT: StringBuilder for multiple concatenations
StringBuilder sb = new StringBuilder();
for (String item : items) {
    sb.append(item).append(", ");
}
String result = sb.toString();

// ✅ ALSO CORRECT: String.join (cleaner)
String result = String.join(", ", items);

// ✅ ALSO CORRECT: String interpolation (Java 15+)
String message = "Member: %s, Age: %d".formatted(name, age);

// ❌ WRONG: String concatenation in loop
String result = "";
for (String item : items) {
    result += item + ", ";  // Creates new String each iteration
}
```

### 9.2 Null or Empty Checks

```java
// ✅ CORRECT: Use utility method
if (StringUtil.isNullOrEmpty(name)) {
    throw new IllegalArgumentException("Name is required");
}

// ✅ ALSO CORRECT: Java 11+ isBlank
if (name == null || name.isBlank()) {
    throw new IllegalArgumentException("Name is required");
}

// ❌ WRONG: Manual null check
if (name == null || name.equals("")) {
    throw new IllegalArgumentException("Name is required");
}
```

---

## 10. Method Design

### 10.1 Method Length

- Keep methods < 30 lines
- Single responsibility principle
- Extract complex logic to helper methods

```java
// ✅ CORRECT: Single responsibility
public MemberDTO getMemberWithBenefits(String memberId) throws Exception {
    Member member = fetchMember(memberId);
    List<Benefit> benefits = fetchBenefits(memberId);
    return buildMemberDTO(member, benefits);
}

private Member fetchMember(String memberId) throws Exception { }
private List<Benefit> fetchBenefits(String memberId) throws Exception { }
private MemberDTO buildMemberDTO(Member member, List<Benefit> benefits) { }

// ❌ WRONG: Too many responsibilities
public MemberDTO getMemberWithBenefits(String memberId) throws Exception {
    // Fetch member, validate, process, fetch benefits, validate, merge, build DTO...
    // 100+ lines of code
}
```

### 10.2 Parameter Count

- Limit to 3-4 parameters
- Use builder pattern or DTOs for multiple parameters

```java
// ✅ CORRECT: Few parameters
public void processMember(String memberId, String action) { }

// ✅ ALSO CORRECT: Use builder pattern for complex objects
MemberRequest request = MemberRequest.builder()
    .memberId("123")
    .action("process")
    .includeFamily(true)
    .build();

service.processMember(request);

// ❌ WRONG: Too many parameters
public void processMember(String memberId, String action, boolean includeFamily, 
                          String preferredLanguage, Date effectiveDate, 
                          String correlationId, List<String> tags) { }
```

### 10.3 Return Types

Prefer specific return types over generic Object:

```java
// ✅ CORRECT: Specific types
public List<Member> findMembers(String criteria) { }
public Optional<Member> findMember(String id) { }
public MemberDTO getMember(String id) { }

// ❌ WRONG: Generic Object
public Object findMembers(String criteria) { }
public Object findMember(String id) { }
```

---

## 11. Object Equality & Hashing

### 11.1 Implement equals() and hashCode()

Always override both together:

```java
// ✅ CORRECT: Both equals and hashCode
public final class Member {
    private final String memberId;
    private final String name;
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Member member = (Member) obj;
        return memberId.equals(member.memberId) &&
               name.equals(member.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(memberId, name);
    }
}

// ❌ WRONG: Only equals
public final class Member {
    @Override
    public boolean equals(Object obj) {
        // implementation
    }
    // Missing hashCode - inconsistent behavior in HashMap/HashSet
}

// ✅ MODERN: Use IDE to generate or use @Data (Lombok)
@Data
public final class Member {
    private final String memberId;
    private final String name;
}
```

---

## 12. Constants & Enums

### 12.1 Constants

```java
// ✅ CORRECT: Static final constants
public class MemberConstants {
    public static final String DEFAULT_TIMEZONE = "UTC";
    public static final int MAX_RETRIES = 3;
    public static final Pattern EMAIL_PATTERN = Pattern.compile("...");
    
    private MemberConstants() { }  // Prevent instantiation
}

// Usage
String tz = MemberConstants.DEFAULT_TIMEZONE;
```

### 12.2 Enums

Use enums for fixed sets of values:

```java
// ✅ CORRECT: Enum for status
public enum MemberStatus {
    ACTIVE("A", "Active"),
    INACTIVE("I", "Inactive"),
    SUSPENDED("S", "Suspended");
    
    private final String code;
    private final String label;
    
    MemberStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
    
    public String getCode() { return code; }
    public String getLabel() { return label; }
}

// Usage
MemberStatus status = MemberStatus.ACTIVE;
if (status == MemberStatus.SUSPENDED) {
    logger.warn("Member is suspended");
}

// ❌ WRONG: String constants for status
public static final String STATUS_ACTIVE = "ACTIVE";
public static final String STATUS_INACTIVE = "INACTIVE";
```

---

## 13. Logging

### 13.1 Logger Setup

```java
// ✅ CORRECT: Static final logger
import org.apache.log4j.Logger;

public class MyService {
    private static final Logger logger = Logger.getLogger(MyService.class);
    
    public void doSomething() {
        logger.debug("Entering doSomething");
        // implementation
        logger.info("Successfully completed operation");
    }
}
```

### 13.2 Log Levels

| Level | When | Examples |
|-------|------|----------|
| **DEBUG** | Method flow, detailed state | Entry/exit, variable values, cache hits |
| **INFO** | Business events, success | "Successfully created member", "Order processed" |
| **WARN** | Unusual but recoverable | "Retry attempt 2/3", deprecated API usage |
| **ERROR** | Failures, exceptions | "Failed to retrieve data", with stack trace |

### 13.3 Logging DO's and DON'Ts

```java
// ✅ CORRECT: Meaningful context
logger.info("Successfully processed member: " + memberId);
logger.error("Failed to retrieve member: " + memberId, exception);

// ✅ ALSO CORRECT: Use string formatting to avoid concatenation
logger.debug("Processing member {} with status {}", memberId, status);

// ❌ WRONG: No context
logger.info("Done");
logger.error("Error");

// ❌ WRONG: String concatenation for debug (performance)
logger.debug("Value: " + expensiveMethod());  // Evaluated even if DEBUG not enabled
logger.debug("Value: {}", expensiveMethod());  // Better - evaluated only if DEBUG enabled
```

### 13.4 Sensitive Data (NEVER Log)

```java
// ❌ NEVER log:
logger.info("SSN: " + ssn);
logger.info("Password: " + password);
logger.info("Auth token: " + token);
logger.info("Credit card: " + cardNumber);

// ✅ CORRECT: Mask sensitive data
logger.info("Member SSN (last 4): ****" + ssn.substring(ssn.length() - 4));
logger.info("Member updated, userId: " + userId);  // Safe to log
```

---

## 14. Documentation & Comments

### 14.1 Javadoc

Document all public APIs:

```java
/**
 * Retrieves a member by their ID.
 *
 * @param memberId the unique member identifier (non-null)
 * @return an Optional containing the member if found, empty otherwise
 * @throws IllegalArgumentException if memberId is null or empty
 * @throws ServiceException if backend service is unavailable
 * 
 * @see #getMemberByEmail(String)
 */
public Optional<Member> getMemberById(String memberId) throws ServiceException {
    Objects.requireNonNull(memberId, "memberId cannot be null");
    // implementation
}
```

### 14.2 Inline Comments

Explain WHY, not WHAT (code is clear about WHAT):

```java
// ✅ CORRECT: Explains why
// Retry with exponential backoff to handle transient network issues
for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
    try {
        return callRemoteService();
    } catch (TemporaryException e) {
        if (attempt == MAX_RETRIES - 1) throw e;
        Thread.sleep((long) Math.pow(2, attempt) * 1000);  // 1s, 2s, 4s...
    }
}

// ❌ WRONG: Explains what (code is clear)
// Increment counter
counter++;

// ❌ WRONG: Misleading comment
// Call the service  (comment adds no value)
return callRemoteService();
```

### 14.3 No Commented Code

Remove commented code before commit. Use version control to retrieve old code:

```java
// ❌ WRONG: Commented code
// old_value = obj.getValue();
// if (old_value > 100) { }

// ✅ CORRECT: Delete and trust git history
new_value = calculateNewValue(obj);
```

---

## 15. Testing Practices

### 15.1 Test Class Naming

```java
// ✅ CORRECT: Test naming
public class MemberServiceTest { }           // Tests MemberService
public class BenefitCalculatorTest { }       // Tests BenefitCalculator
public class MemberControllerTest { }        // Tests MemberController

// ❌ WRONG: Unclear naming
public class TestMember { }                  // Unclear what's being tested
public class MemberTests { }                 // Non-standard suffix
```

### 15.2 Test Method Naming

Use descriptive names following pattern: `test{MethodName}_{Scenario}_{Expected}`

```java
// ✅ CORRECT: Descriptive test names
@Test
public void testGetMember_WithValidId_ReturnsOptionalWithMember() { }

@Test
public void testGetMember_WithNullId_ThrowsIllegalArgumentException() { }

@Test
public void testProcessMember_WithInactiveStatus_SkipsProcessing() { }

// ❌ WRONG: Unclear test names
@Test
public void testGetMember() { }              // What scenario?
@Test
public void test1() { }                      // No description
@Test
public void testGetMemberWorks() { }         // Too vague
```

### 15.3 Test Structure (AAA Pattern)

```java
@Test
public void testGetMember_WithValidId_ReturnsMember() {
    // Arrange
    String memberId = "12345";
    Member expectedMember = new Member(memberId, "John Doe");
    when(repository.findById(memberId)).thenReturn(Optional.of(expectedMember));
    
    // Act
    Optional<Member> result = service.getMember(memberId);
    
    // Assert
    assertTrue(result.isPresent());
    assertEquals(expectedMember, result.get());
}
```

### 15.4 Mocking & Assertions

```java
// ✅ CORRECT: Use mocking for dependencies
@Test
public void testProcessMember_CallsDependencies() {
    // Mock the repository
    when(repository.save(any(Member.class))).thenReturn(true);
    
    // Act
    service.processMember(member);
    
    // Verify
    verify(repository).save(member);
    verify(logger).info(contains("processed"));
}

// ✅ CORRECT: Use specific assertions
assertEquals(expected, actual);
assertNotNull(result);
assertTrue(result.isEmpty());
assertThrows(IllegalArgumentException.class, () -> service.getMember(null));

// ❌ WRONG: Generic assertions
assertTrue(result != null);            // Use assertNotNull instead
assertTrue(expected.equals(actual));   // Use assertEquals instead
```

---

## 16. Dependency Injection & Annotations

### 16.1 Spring Annotations

```java
// ✅ CORRECT: Use Spring annotations for DI
@Component
public class MemberService {
    
    @Autowired
    private MemberRepository repository;
    
    @Autowired
    private Logger logger;  // Usually provided by factory
    
    public Member getMember(String id) {
        return repository.findById(id);
    }
}

// ✅ ALSO CORRECT: Constructor injection (preferred)
@Component
public class MemberService {
    
    private final MemberRepository repository;
    private final Logger logger;
    
    public MemberService(MemberRepository repository, Logger logger) {
        this.repository = repository;
        this.logger = logger;
    }
}

// ❌ WRONG: Manual instantiation
public class MemberService {
    private MemberRepository repository = new MemberRepository();  // Tight coupling
}
```

### 16.2 Qualifier Annotations

Use when multiple implementations exist:

```java
// ✅ CORRECT: @Qualifier for disambiguation
@Component
public class MemberService {
    
    @Autowired
    @Qualifier("primaryRepository")
    private MemberRepository primaryRepo;
    
    @Autowired
    @Qualifier("cacheRepository")
    private MemberRepository cacheRepo;
}

// Bean definition
@Configuration
public class RepositoryConfig {
    
    @Bean(name = "primaryRepository")
    public MemberRepository primaryRepository() {
        return new MemberRepositoryImpl();
    }
    
    @Bean(name = "cacheRepository")
    public MemberRepository cacheRepository() {
        return new CachedMemberRepository();
    }
}
```

---

## 17. Streams & Functional Programming (Java 8+)

### 17.1 Stream Usage

```java
// ✅ CORRECT: Use streams for transformations
List<String> activeMembers = members.stream()
    .filter(m -> m.getStatus() == Status.ACTIVE)
    .map(Member::getName)
    .sorted()
    .collect(Collectors.toList());

// ✅ ALSO CORRECT: Complex streams with intermediate operations
Map<String, List<Member>> membersByStatus = members.stream()
    .filter(m -> m.getDateOfBirth().isAfter(LocalDate.now().minusYears(65)))
    .collect(Collectors.groupingBy(
        m -> m.getStatus().toString(),
        Collectors.toList()
    ));

// ❌ WRONG: Over-complex streams
List<String> names = members.stream()
    .filter(m -> m != null)
    .map(m -> {
        String name = m.getName();
        if (name != null) return name.toUpperCase();
        return "";
    })
    .filter(n -> !n.isEmpty())
    .collect(Collectors.toList());
// Better: Use a method instead
```

### 17.2 Lambda Functions

```java
// ✅ CORRECT: Simple lambdas
items.forEach(item -> processItem(item));

// ✅ ALSO CORRECT: Method references (cleaner)
items.forEach(this::processItem);

// ✅ ALSO CORRECT: Multi-line lambdas
list.stream()
    .filter(item -> {
        boolean condition = checkCondition(item);
        logger.debug("Checking item: " + condition);
        return condition;
    })
    .collect(Collectors.toList());

// ❌ WRONG: Overly complex lambdas
list.stream()
    .filter(item -> {
        // 20 lines of complex logic
    })
    .collect(Collectors.toList());
// Better: Extract to a method
```

---

## 18. Date & Time (Java 8+ Time API)

### 18.1 Avoid java.util.Date

```java
// ✅ CORRECT: Use java.time API
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.Instant;

LocalDate dob = LocalDate.of(1990, 5, 15);
LocalDateTime createdAt = LocalDateTime.now();
ZonedDateTime zonedTime = ZonedDateTime.now();

// ✅ ALSO CORRECT: For API responses (UTC)
Instant timestamp = Instant.now();
String timestampStr = timestamp.toString();  // ISO-8601 format

// ❌ WRONG: Avoid java.util.Date
Date date = new Date();  // Mutable, not recommended
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  // Not thread-safe
```

### 18.2 Date Calculations

```java
// ✅ CORRECT: Use ChronoUnit or temporal adjusters
LocalDate today = LocalDate.now();
LocalDate yesterday = today.minusDays(1);
LocalDate nextYear = today.plusYears(1);

long daysBetween = ChronoUnit.DAYS.between(date1, date2);
LocalDate nextMonday = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));

// ❌ WRONG: Manual date arithmetic
long MS_PER_DAY = 24 * 60 * 60 * 1000;
Date yesterday = new Date(System.currentTimeMillis() - MS_PER_DAY);  // Error-prone
```

---

## 19. Visibility & Access Modifiers

### 19.1 Principle of Least Privilege

```java
// ✅ CORRECT: Minimal visibility
public class MyClass {
    private static final String CONSTANT = "value";        // Constants: public static final
    private String privateField;                           // Instance fields: private
    protected void helperMethod() { }                       // Protected: only if subclassing needed
    public void publicAPI() { }                            // Public: only essential APIs
}

// ❌ WRONG: Overly exposed
public class MyClass {
    public static String CONSTANT = "value";              // Non-final
    public String field;                                  // Public field
    public void internalHelper() { }                       // Should be private
}
```

### 19.2 Getters & Setters

```java
// ✅ CORRECT: Private fields with getters/setters
private String name;
private List<Item> items;

public String getName() { return name; }
public void setName(String name) { this.name = name; }

public List<Item> getItems() {
    return Collections.unmodifiableList(items);  // Defensive copy
}

public void addItem(Item item) { items.add(item); }

// ❌ WRONG: Public fields
public String name;
public List<Item> items;  // External code can modify directly
```

---

## 20. Common Anti-Patterns to AVOID

### 20.1 God Objects

❌ **WRONG:** Single class with too many responsibilities

```java
public class Member {
    // Member data
    private String memberId;
    private String name;
    
    // Benefits calculation
    public Benefit calculateBenefit() { }
    
    // Claims processing
    public void processClaim(Claim claim) { }
    
    // Database persistence
    public void save() { }
    
    // API communication
    public void callRemoteService() { }
    
    // Validation
    public void validate() { }
    
    // 500+ lines of code
}
```

✅ **CORRECT:** Separate concerns

```java
// Domain model
public class Member {
    private String memberId;
    private String name;
}

// Benefit logic
@Service
public class BenefitCalculator {
    public Benefit calculateBenefit(Member member) { }
}

// Claim processing
@Service
public class ClaimProcessor {
    public void processClaim(Claim claim) { }
}

// Persistence
@Repository
public class MemberRepository {
    public void save(Member member) { }
}
```

### 20.2 Primitive Obsession

❌ **WRONG:** Using primitives instead of domain objects

```java
public void processPayment(String cardNumber, String cvv, double amount, int expiryMonth, int expiryYear) {
    if (!isValidCardNumber(cardNumber)) { }
    if (!isValidCVV(cvv)) { }
    if (amount <= 0) { }
    // 100 lines of validation and processing
}
```

✅ **CORRECT:** Use domain objects

```java
public void processPayment(PaymentRequest payment) {
    if (!payment.isValid()) {
        throw new InvalidPaymentException();
    }
    // Clean, single responsibility
}

public class PaymentRequest {
    private final CardNumber cardNumber;
    private final CVV cvv;
    private final Money amount;
    private final ExpiryDate expiryDate;
    
    public boolean isValid() {
        return cardNumber.isValid() && cvv.isValid() && amount.isPositive() && expiryDate.isNotExpired();
    }
}
```

### 20.3 Long Parameter Lists

❌ **WRONG:** Too many parameters

```java
public Report generateReport(String memberId, LocalDate startDate, LocalDate endDate, 
                            boolean includeFamily, String language, boolean emailResult, 
                            String emailAddress, int pageSize, String sortBy) {
    // Implementation
}
```

✅ **CORRECT:** Use builder pattern or request object

```java
public Report generateReport(ReportRequest request) {
    // Implementation
}

public class ReportRequest {
    public static final class Builder {
        private String memberId;
        private LocalDate startDate;
        private LocalDate endDate;
        // ... other fields
        
        public ReportRequest build() {
            return new ReportRequest(this);
        }
    }
}

// Usage
ReportRequest request = new ReportRequest.Builder()
    .memberId("123")
    .startDate(LocalDate.now().minusMonths(1))
    .endDate(LocalDate.now())
    .includeFamily(true)
    .language("en")
    .build();

Report report = generator.generateReport(request);
```

---

## 21. Build & Compilation

### 21.1 Maven Compiler Settings

```xml
<properties>
    <maven.compiler.source>11</maven.compiler.source>
    <maven.compiler.target>11</maven.compiler.target>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.8.1</version>
            <configuration>
                <source>11</source>
                <target>11</target>
                <failOnWarning>true</failOnWarning>  <!-- Fail on compiler warnings -->
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 21.2 Compile & Test

```bash
# Compile with warnings
mvn clean compile

# Run unit tests
mvn test

# Full build (compile + test + package)
mvn clean package

# Check for code issues
mvn checkstyle:check      # If configured
mvn pmd:check            # If configured
```

---

## 22. Pre-commit Checklist (General Java Code)

Execute BEFORE committing:

- [ ] Code compiles: `mvn clean compile`
- [ ] All tests pass: `mvn test`
- [ ] No compiler warnings (if failOnWarning enabled)
- [ ] No unused imports (IDE: Organize Imports)
- [ ] No commented code (delete or use version control)
- [ ] No hardcoded values (use constants)
- [ ] Proper exception handling (specific exceptions, log with context)
- [ ] No null pointer risks (validate inputs, use Optional)
- [ ] Logging is appropriate (no sensitive data, meaningful messages)
- [ ] Method length < 30 lines
- [ ] Class has single responsibility
- [ ] Javadoc on public methods
- [ ] Tests cover happy path + error cases (70%+ coverage)
- [ ] No magic numbers (use named constants)

---

## 23. Code Review Checklist (For Reviewers)

When reviewing Java code:

- [ ] Code is readable and follows naming conventions
- [ ] Methods are concise (< 30 lines) and focused
- [ ] Exception handling is appropriate (specific exceptions, logged)
- [ ] Null safety: inputs validated, Optional used appropriately
- [ ] No direct DB access if in web/client layer
- [ ] Tests are present and meaningful
- [ ] No commented code or debug statements
- [ ] Logging doesn't expose sensitive data (PII, PHI)
- [ ] Collections properly generic-typed
- [ ] No hardcoded values (use constants/enums)
- [ ] Javadoc present on public APIs
- [ ] Performance considerations (N+1 queries, streaming, caching)

---

## 24. Summary & Key Principles

**SOLID Principles:**
1. **S**ingle Responsibility: Each class has one reason to change
2. **O**pen/Closed: Open for extension, closed for modification
3. **L**iskov Substitution: Subtypes must be substitutable
4. **I**nterface Segregation: Clients depend only on methods they use
5. **D**ependency Inversion: Depend on abstractions, not concretions

**Design Patterns:**
- **Builder Pattern:** For complex object construction
- **Factory Pattern:** For object creation
- **Singleton Pattern:** For shared resources (use Spring @Bean instead)
- **Strategy Pattern:** For pluggable algorithms
- **Observer Pattern:** For event-driven code

**Code Quality Metrics:**
- Cyclomatic Complexity: Aim for < 10
- Test Coverage: Aim for > 70%
- Coupling: Low (prefer composition)
- Cohesion: High (methods in class related)

---

**END OF JAVA CODING STANDARDS**

Remember: Write code for humans first, machines second. Make it readable, testable, and maintainable.
