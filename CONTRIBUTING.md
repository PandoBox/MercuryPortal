# Contributing to Mercury Messenger Portal

Thank you for your interest in contributing to Mercury Messenger Portal! This guide will help you understand our development process and how to contribute effectively.

## Table of Contents

1. [Code of Conduct](#code-of-conduct)
2. [Getting Started](#getting-started)
3. [Development Setup](#development-setup)
4. [Workflow](#workflow)
5. [Code Style](#code-style)
6. [Testing](#testing)
7. [Commit Messages](#commit-messages)
8. [Pull Requests](#pull-requests)
9. [Review Process](#review-process)
10. [Architecture Guidelines](#architecture-guidelines)

---

## Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inclusive environment for all contributors. We pledge to make participation in this project a harassment-free experience for everyone.

### Expected Behavior

- Use welcoming and inclusive language
- Be respectful of differing opinions and experiences
- Accept constructive criticism gracefully
- Focus on what is best for the community
- Show empathy towards other community members

### Unacceptable Behavior

- Harassment or discrimination based on any characteristic
- Unwelcome sexual attention or advances
- Deliberate intimidation or threats
- Insults or derogatory comments
- Publishing others' private information

### Enforcement

Violations of this code of conduct may result in removal from the project.

---

## Getting Started

### Prerequisites

Before contributing, ensure you have:

- ✅ Read [README.md](README.md) for project overview
- ✅ Reviewed [ARCHITECTURE.md](ARCHITECTURE.md) for system design
- ✅ Completed [DEVELOPER_SETUP.md](DEVELOPER_SETUP.md) for local environment
- ✅ Understood [API_INTEGRATION.md](API_INTEGRATION.md) for backend contracts

### First-Time Contributor

**New to open source?** That's great! Here's how to start:

1. **Find an Issue**
   - Browse [GitHub Issues](https://github.com/PandoBox/MercuryPortal/issues)
   - Look for `good-first-issue` or `help-wanted` labels
   - Read the issue description and requirements

2. **Comment on the Issue**
   ```
   "Hi, I'd like to work on this issue. 
    I'm planning to [describe your approach].
    Any suggestions?"
   ```

3. **Follow the Workflow** (see below)

4. **Create Your First PR**
   - Start small (single feature or bug fix)
   - Ask questions if anything is unclear
   - Be patient with feedback

---

## Development Setup

### 1. Fork and Clone

```bash
# Fork on GitHub (click Fork button)
# Clone your fork
git clone https://github.com/YOUR_USERNAME/MercuryPortal.git
cd MercuryPortal

# Add upstream remote
git remote add upstream https://github.com/PandoBox/MercuryPortal.git
```

### 2. Create Local Environment

```bash
# Copy environment template
cp local.properties.example local.properties

# Edit local.properties with your paths
# - SDK path
# - API endpoints (use dev endpoint)
# - Optional: signing keystore

# Build and test
./gradlew build
./gradlew test
```

### 3. Open in IDE

```bash
# Android Studio
open -a "Android Studio" .

# Or manually: File → Open → Select directory
```

### 4. Verify Setup

```bash
# Run debug build
./gradlew installDebug

# Should see: "BUILD SUCCESSFUL"
# App should launch on emulator/device
```

---

## Workflow

### 1. Create Feature Branch

```bash
# Update main branch
git fetch upstream
git checkout main
git reset --hard upstream/main

# Create feature branch
git checkout -b feature/your-feature-name
# OR for bug fixes
git checkout -b fix/bug-description
# OR for documentation
git checkout -b docs/what-you-are-documenting
```

**Branch Naming Convention:**

```
feature/job-reordering           # New feature
fix/clock-out-button-hang        # Bug fix
docs/api-integration             # Documentation
refactor/repository-cleanup      # Code refactoring
test/add-unit-tests              # Tests
chore/update-dependencies        # Maintenance
```

### 2. Make Changes

```bash
# Edit files as needed
vim app/src/main/java/.../File.kt

# Run tests frequently
./gradlew test

# Check code quality
./gradlew lint

# Build to catch issues early
./gradlew build
```

### 3. Commit Your Changes

```bash
# Stage files
git add path/to/file1.kt path/to/file2.kt

# Commit with clear message
git commit -m "feat: Add job reordering feature

- Implement drag-and-drop for active jobs
- Persist order to database
- Add visual feedback during drag

Fixes #123"
```

**Commit Message Format:**

```
<type>: <subject>

<body>

<footer>
```

**Types:**
- `feat` - New feature
- `fix` - Bug fix
- `docs` - Documentation
- `refactor` - Code refactoring
- `test` - Test additions
- `chore` - Build, deps, configuration

**Rules:**
- First line: ~50 characters max
- Capitalize first word
- Use present tense ("add" not "added")
- Reference issues: "Fixes #123"
- Body: wrap at 72 characters
- Explain *why*, not just *what*

### 4. Push and Create PR

```bash
# Push to your fork
git push origin feature/your-feature-name

# Go to GitHub and click "Compare & pull request"
# Or use GitHub CLI
gh pr create --fill
```

### 5. Respond to Review

```bash
# Make requested changes
# Commit as usual
git add .
git commit -m "refactor: Update implementation based on feedback"

# Push (no need for new PR)
git push origin feature/your-feature-name
```

---

## Code Style

### Kotlin Guidelines

**Format: AndroidKtlint**

```bash
# Auto-format code
./gradlew ktlintFormat

# Check formatting
./gradlew ktlint
```

**Key Rules:**

✅ **Do:**
```kotlin
// 1. Use descriptive names
val isJobCompleted = job.status == JobStatus.COMPLETED

// 2. Prefer when over if-else
when (job.status) {
    JobStatus.ASSIGNED -> {}
    JobStatus.DEPARTED -> {}
    else -> {}
}

// 3. Use extension functions
val address = location.toAddress()

// 4. Leverage Kotlin idioms
jobs.filter { it.status != COMPLETED }
    .sortedBy { it.sequenceOrder }
```

❌ **Avoid:**
```kotlin
// 1. Unclear abbreviations
val jc = isJobComplete

// 2. Deep nesting
if (a) {
    if (b) {
        if (c) {
            // code
        }
    }
}

// 3. Type annotations when obvious
val job: Job = getJob()  // Type is obvious

// 4. Mutable global state
var globalCounter = 0
```

### Naming Conventions

```kotlin
// Classes: PascalCase
class JobDetailViewModel

// Functions: camelCase
fun loadJob(jobId: String)

// Constants: CONSTANT_CASE
companion object {
    const val DEFAULT_TIMEOUT_MS = 30000
}

// Private members: _prefix for backing properties
private val _uiState = MutableStateFlow(...)
val uiState: StateFlow<...> = _uiState.asStateFlow()

// Boolean functions: is/has prefix
fun isJobCompleted(): Boolean
fun hasValidAddress(): Boolean
```

### Compose Guidelines

```kotlin
// Function naming: PascalCase for composables
@Composable
fun JobDetailScreen(...)

// Parameter order: required first, lambdas last
@Composable
fun JobCard(
    job: Job,
    isSharedLocation: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
)

// Modifier always last parameter
Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp)
)
```

### Documentation

```kotlin
/**
 * Updates job status with location verification.
 * 
 * Captures GPS coordinates and optionally uploads a photo
 * before updating the server. Falls back to local persistence
 * if network unavailable.
 *
 * @param job The job to update
 * @param newStatus Target status (COMPLETED, DELAYED, etc.)
 * @param latitude GPS latitude (nullable)
 * @param longitude GPS longitude (nullable)
 * @param photoUrl Photo upload URL (optional)
 * @throws ApiException if server request fails
 * @throws LocationException if GPS capture fails
 */
suspend fun updateJobStatus(
    job: Job,
    newStatus: JobStatus,
    latitude: Double?,
    longitude: Double?,
    photoUrl: String? = null
)
```

**Documentation Rules:**
- Public functions and classes only
- Include parameters and return types
- Explain *why* not just *what*
- Include examples for complex functions

---

## Testing

### Test Types

#### 1. Unit Tests

**Location:** `src/test/kotlin/`

```kotlin
class JobDetailViewModelTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    private val mockRepository = mockk<JobRepository>()
    private val viewModel = JobDetailViewModel(mockRepository)
    
    @Test
    fun testLoadJobSuccess() = runTest {
        val job = Job(id = "1", ...)
        coEvery { mockRepository.observeJob("1") } returns flowOf(job)
        
        viewModel.loadJob("1")
        
        assertEquals(job, viewModel.uiState.value.job)
    }
}
```

**Rules:**
- Mock external dependencies
- Test behavior, not implementation
- Use descriptive names: `test<Function><Scenario><Expected>`
- One assertion per test (ideally)
- Use `runTest` for suspend functions

#### 2. Integration Tests

**Location:** `src/androidTest/kotlin/`

```kotlin
@RunWith(AndroidJUnit4::class)
class JobRepositoryTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun testUpdateJobStatusSavesLocally() = runTest {
        val job = Job(...)
        val result = repository.updateJobStatus(job, COMPLETED, 13.7, 100.5)
        
        assertTrue(result.isSuccess)
        val saved = jobDao.getJob("1")
        assertEquals(COMPLETED, saved.status)
    }
}
```

**Rules:**
- Test with real database (in-memory)
- Test actual integrations
- Slower, so test critical paths
- Clean up data between tests

### Running Tests

```bash
# All unit tests
./gradlew test

# Specific test class
./gradlew test --tests "JobDetailViewModelTest"

# Specific test method
./gradlew test --tests "JobDetailViewModelTest.testLoadJobSuccess"

# Instrumented tests (requires device)
./gradlew connectedAndroidTest
```

### Test Coverage

**Target:** 70%+ code coverage

```bash
# Generate coverage report
./gradlew testDebugUnitTestCoverage

# View report
open app/build/reports/coverage/index.html
```

### What to Test

✅ **Always test:**
- ViewModel state changes
- Repository data transformations
- Error handling paths
- Edge cases (empty lists, null values)

❌ **Don't test:**
- Android framework classes (Room, Retrofit)
- UI rendering (Layout inflation)
- Third-party library behavior
- Trivial getters/setters

---

## Commit Messages

### Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Examples

**Feature:**
```
feat(job-detail): Add delay job functionality

Implement new feature allowing messengers to mark jobs as delayed
with a required reason. Dialog validates that reason is not empty
before submission.

- Add DelayDialog composable with text input
- Add onConfirmDelay() to JobDetailViewModel
- Set justStatusChanged flag to show Depart button
- Save delay reason to job remarks

Fixes #45
```

**Bug Fix:**
```
fix(navigation): Fix Depart Next Job button not appearing

The justStatusChanged flag was being set AFTER the database update,
causing the Flow emission to overwrite it. Fix: set the flag BEFORE
the API call.

Affects: JobDetailViewModel, commitStatusUpdate()

Fixes #42
```

**Documentation:**
```
docs: Update API integration guide

Add request/response examples for all endpoints and clarify
timestamp format (milliseconds since epoch, UTC).

Fixes #89
```

---

## Pull Requests

### PR Title

- Clear and descriptive
- Reference issue number
- Examples:
  - "feat: Add job reordering with drag-and-drop (#34)"
  - "fix: Correct GPS location timestamp (#42)"
  - "docs: Update developer setup guide"

### PR Description Template

```markdown
## Description

[Clear description of changes]

## Type of Change

- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation

## Related Issue

Closes #123

## Testing

[How to test changes]

1. Step 1
2. Step 2
3. Verify result

## Checklist

- [ ] Code follows style guidelines
- [ ] Tests added/updated
- [ ] Documentation updated
- [ ] No new warnings introduced
- [ ] Changes tested locally
```

### PR Guidelines

✅ **Do:**
- One feature or bug fix per PR
- Keep PRs focused and reviewable (<400 lines)
- Include tests for new functionality
- Update documentation if needed
- Link to related issues
- Respond to review feedback promptly

❌ **Avoid:**
- Multiple unrelated changes
- Large refactoring + features (separate!)
- Missing tests for critical paths
- Breaking changes without discussion
- Force-pushing after review started

---

## Review Process

### What to Expect

1. **Automated Checks** (5-10 min)
   - Build succeeds
   - Tests pass
   - Lint passes
   - Code coverage acceptable

2. **Code Review** (24-48 hours)
   - At least one approval required
   - Maintainers check:
     - Code correctness
     - Design alignment
     - Test coverage
     - Documentation quality

3. **Changes Requested** (if needed)
   - Reviewer comments on specific lines
   - Respond and update code
   - Push new commits (don't force-push)
   - Re-request review

4. **Merge**
   - Approved and passing
   - Squash or rebase as needed
   - Update branch if behind main

### Giving Reviews

When reviewing others' PRs:

✅ **Do:**
- Be respectful and constructive
- Explain *why* not just *what*
- Suggest improvements gently
- Approve good work
- Test locally if critical

❌ **Avoid:**
- Nitpicking style (let linters catch it)
- Blocking on personal preference
- Long debates (discuss in comments)
- Approving without reading code

### Getting Your PR Merged

**Before submitting:**
```bash
# Make sure everything passes
./gradlew clean build
./gradlew test
./gradlew lint

# Rebase on latest main
git fetch upstream
git rebase upstream/main

# Force-push to update PR
git push --force-with-lease origin feature/your-feature
```

**Address review feedback promptly:**
- Respond to all comments
- Explain decisions if disagreeing
- Don't take criticism personally
- Ask questions if unclear

---

## Architecture Guidelines

### Follow MVVM Pattern

```kotlin
// ✅ Good: Separation of concerns
@HiltViewModel
class JobDetailViewModel @Inject constructor(
    private val jobRepository: JobRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(JobDetailUiState())
    val uiState: StateFlow<JobDetailUiState> = _uiState.asStateFlow()
    
    fun loadJob(jobId: String) {
        viewModelScope.launch {
            jobRepository.observeJob(jobId).collect { job ->
                _uiState.update { it.copy(job = job) }
            }
        }
    }
}

// ❌ Bad: Tight coupling, direct database access
@Composable
fun JobDetailScreen() {
    val db = Room.databaseBuilder(...).build()
    val job = db.jobDao().getJob(jobId)
    Text(job.title)
}
```

### Use Repository Pattern

```kotlin
// ✅ Good: Abstract data source
@Singleton
class JobRepository @Inject constructor(
    private val jobDao: JobDao,
    private val apiService: ApiService
) {
    fun observeJob(jobId: String): Flow<Job> =
        jobDao.observeJob(jobId).map { it.toDomain() }
}

// ❌ Bad: Direct API calls everywhere
// (same in multiple ViewModels)
```

### Leverage Dependency Injection

```kotlin
// ✅ Good: Constructor injection
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repo1: Repository1,
    private val repo2: Repository2
) : ViewModel()

// ❌ Bad: Service locator pattern
class MyViewModel : ViewModel() {
    val repo = ServiceLocator.getRepository()
}
```

### Use Flows for Reactivity

```kotlin
// ✅ Good: Reactive with Flow
viewModel.uiState
    .collectAsStateWithLifecycle()
    .let { state -> /* update UI */ }

// ❌ Bad: Polling or callbacks
thread {
    while (true) {
        val job = repository.getJob()
        updateUI(job)
        Thread.sleep(1000)
    }
}
```

---

## Common Contributions

### Bug Reports

```markdown
**Description:** [Clear, concise description]

**Steps to reproduce:**
1. Clock in
2. Open a job
3. Click complete

**Expected behavior:** Job marked complete, button disappears

**Actual behavior:** Button keeps spinning

**Environment:**
- Device: Pixel 6
- Android: 13
- App Version: 1.0.2

**Logs:**
[Logcat output, stack trace, etc.]
```

### Feature Requests

```markdown
**Use Case:** [Why is this feature needed?]

**Proposed Solution:** [How would you implement it?]

**Alternatives Considered:** [Other approaches?]

**Additional Context:** [Any other details?]
```

### Documentation

- Improve clarity of existing docs
- Add examples or diagrams
- Fix typos or formatting
- Translate to other languages
- Create tutorials or guides

### Tests

- Increase code coverage
- Test edge cases
- Add integration tests
- Test error scenarios
- Performance tests

---

## Questions & Support

### Getting Help

1. **Check existing docs**
   - [README.md](README.md)
   - [ARCHITECTURE.md](ARCHITECTURE.md)
   - [DEVELOPER_SETUP.md](DEVELOPER_SETUP.md)

2. **Search closed issues**
   - Might have been answered before

3. **Ask in PR/Issue comments**
   - Maintainers monitor discussions

4. **Email maintainers**
   - For sensitive issues

---

## Recognition

### Attribution

Contributors are recognized in:
- [CHANGELOG.md](CHANGELOG.md) - Version contributors
- GitHub contributors page
- Annual community report

### Levels

- **First PR**: Welcome message & thanks
- **5+ Contributions**: Added to contributors list
- **Major Features**: Special recognition
- **Consistent Involvement**: Core team consideration

---

## Final Checklist

Before submitting your PR:

- [ ] Code follows style guidelines
- [ ] Tests added/updated
- [ ] Documentation updated
- [ ] No new warnings or errors
- [ ] Changes tested locally
- [ ] Commit messages are clear
- [ ] Related issues linked
- [ ] No unrelated changes included
- [ ] Branch is up to date with main
- [ ] PR description is complete

---

## Resources

- [Kotlin Style Guide](https://kotlinlang.org/docs/coding-conventions.html)
- [Android Guidelines](https://developer.android.com/guide)
- [Compose Documentation](https://developer.android.com/jetpack/compose)
- [Git Workflow](https://git-scm.com/book/en/v2)
- [Our Architecture](ARCHITECTURE.md)

---

## Thank You!

We truly appreciate your contributions. The success of Mercury Messenger Portal depends on passionate developers like you!

**Happy coding!** 🚀

---

**Last Updated**: April 13, 2026  
**Version**: 1.0  
**Maintained By**: Mercury Portal Team
