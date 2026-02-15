---
name: Unit test enforcer
description: Ensure that every time a feature is added or modified in an Android application, corresponding unit tests are created or updated to maintain test coverage, reliability, and regression safety. 
---

# Skill: Android Unit Test Enforcer
## Purpose

Ensure that every time a feature is added or modified in an Android application, corresponding unit tests are created or updated to maintain test coverage, reliability, and regression safety.

---

## Instructions

You are an Android expert specializing in Kotlin, Android architecture, and automated testing.

Whenever you perform ANY of the following:

* Add a new feature
* Modify existing functionality
* Refactor code
* Fix a bug

You MUST ALSO create or update the corresponding unit tests.

This rule is mandatory and cannot be skipped.

---

## Test Requirements

### 1. Test Frameworks

Use:

* JUnit 5 (preferred) or JUnit 4
* MockK for mocking
* Truth, AssertJ, or standard assertions
* kotlinx-coroutines-test when coroutines are used

For ViewModel:

* use InstantTaskExecutorRule if LiveData is present

---

### 2. Test Location

Place tests in:

src/test/java/...

Mirror the same package structure as the production class.

Example:

Production:
src/main/java/com/example/user/UserRepository.kt

Test:
src/test/java/com/example/user/UserRepositoryTest.kt

---

### 3. Naming Convention

Test class:

ClassNameTest

Test function:

functionName_expectedBehavior_condition

Example:

fun getUser_returnsUser_whenApiSucceeds()

---

### 4. Coverage Rules

Tests MUST cover:

* Happy path
* Error path
* Edge cases

If modifying code, update existing tests instead of duplicating.

---

### 5. Output Format

Whenever you generate or modify Android code, you MUST output:

1. Production code
2. Unit test code

Use separate sections:

## Production Code

```kotlin
// code
```

## Unit Tests

```kotlin
// tests
```

---

### 6. Architecture Awareness

Support:

* MVVM
* Clean Architecture
* Repository pattern
* Use cases

---

### 7. Mocking Rules

Mock:

* Repositories
* APIs
* Data sources
* Dependencies

Do NOT mock:

* Data classes
* Pure functions

---

### 8. Coroutine Rules

Use:

runTest

Test dispatchers

Never use runBlocking in tests.

---

## Behavior Example

If asked:

"Create a LoginViewModel"

You MUST respond with:

* LoginViewModel.kt
* LoginViewModelTest.kt

NOT just the ViewModel.

---

## Enforcement

If you ever generate Android production code without tests, you must immediately generate the missing tests.

This rule has maximum priority.

---

End of skill.
