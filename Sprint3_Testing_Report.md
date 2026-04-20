# Sprint 3 Testing Activities Report

## Project Information
- Project: Smart University Transport Management System (SUTMS)
- Sprint: Sprint 3
- Module Focus: Fee Management Module
- Report Date: 2026-04-20
- Test Plan ID: STP-03

---

## A) Software Test Plan - Sprint 3

### 1. Scope
Testing focused on Sprint 3 fee management features:
- Route fee configuration
- Auto-challan generation on student approval
- Student challan screen state rendering
- Payment proof upload
- Admin fee management panel
- Payment confirmation flow
- Updated student lookup behavior tied to challan/payment state

### 2. Out of Scope
- Sprint 1 and Sprint 2 modules (only regression smoke checks)
- Admin authentication flow (assumed already stable)

### 3. Test Types
- Black-box functional testing
  - Equivalence Partitioning (EP)
  - Boundary Value Analysis (BVA)
  - Error Guessing
- White-box structural testing
  - Branch coverage
  - Statement/path coverage

### 4. Test Environment
- Application: Java desktop application (Swing-based UI)
- JDK: Java 17+
- Database: MySQL 8.x (localhost, transport_db)
- Libraries:
  - mysql-connector-j-8.0.33.jar
  - junit-4.13.2.jar
  - hamcrest-core-1.3.jar
- Test data baseline:
  - Minimum 3 routes with fee values
  - Minimum 5 student accounts
  - Minimum 3 approved registrations

### 5. Entry Criteria
- Sprint 3 development tasks marked complete
- Application compiles successfully
- Database available and reachable
- Test datasets prepared

### 6. Exit Criteria
- All critical test cases pass
- No Severity-1 defects open
- Defects logged with status
- Test evidence captured (terminal output/screenshots)

### 7. Roles
- Test Lead: Muhammad Saad

---

## B) Black-Box Test Cases

## B.1 Route Fee Input Validation (TC-01 to TC-06)
Techniques: EP + BVA + Error Guessing

| TC ID | Input / Scenario | Technique | Expected Result | Status |
|---|---|---|---|---|
| TC-01 | Fee = 15000 | EP Valid | Route saved with fee_amount=15000.00; success shown | PASS |
| TC-02 | Fee = 0 | BVA Boundary | Validation error for value must be greater than 0 | PASS |
| TC-03 | Fee = -500 | EP Invalid | Validation error for value must be greater than 0 | PASS |
| TC-04 | Fee = empty | EP Invalid | Validation error for value must be greater than 0 | PASS |
| TC-05 | Fee = abc | Error Guessing | Non-numeric input rejected with validation error | PASS |
| TC-06 | Fee = 0.01 | BVA Min Valid | Route saved with fee_amount=0.01; success shown | PASS |

Implemented evidence test class:
- test/RouteFeeValidationTest.java

## B.2 Auto-Challan Generation on Approval (TC-07 to TC-10)
Techniques: EP + Error Guessing

| TC ID | Scenario | Technique | Expected Result | Status |
|---|---|---|---|---|
| TC-07 | Approve PENDING request with route fee 12000 | EP Valid | Student APPROVED and challan created as UNPAID with amount=12000 | PASS |
| TC-08 | Simulated DB issue during challan INSERT | Error Guessing | Rollback behavior; approval does not complete partially | PASS |
| TC-09 | Route fee = 0.00 | EP Edge | Challan supports amount_due=0.00 without crash | PASS |
| TC-10 | Duplicate challan attempt | Error Guessing | Existing challan handling prevents inconsistent duplicate processing | PASS |

Execution was validated through DAO and approval-flow behavior checks.

## B.3 Student Payment Proof Upload (TC-11 to TC-15)
Techniques: EP + Error Guessing

| TC ID | Scenario | Technique | Expected Result | Status |
|---|---|---|---|---|
| TC-11 | Upload valid image (.jpg/.png) | EP Valid | File copied to receipts and status changes to PROOF_SUBMITTED | PASS |
| TC-12 | Submit without selecting file | EP Invalid | User receives error; submission blocked | PASS |
| TC-13 | Upload PDF proof | EP Valid PDF | PDF accepted and path stored | PASS |
| TC-14 | Upload attempt when status PROOF_SUBMITTED | Error Guessing | Upload UI hidden/blocked for this state | PASS |
| TC-15 | Upload attempt when status PAID | Error Guessing | Upload option absent in paid state | PASS |

## B.4 Admin Payment Confirmation (TC-16 to TC-19)
Techniques: EP + Error Guessing

| TC ID | Scenario | Technique | Expected Result | Status |
|---|---|---|---|---|
| TC-16 | Confirm payment for PROOF_SUBMITTED challan | EP Valid | Status set to PAID with paid_at timestamp | PASS |
| TC-17 | Close confirmation dialog without confirming | EP Cancel | No data change; status remains unchanged | PASS |
| TC-18 | Missing receipt file on disk | Error Guessing | User-friendly error shown, no crash | PASS |
| TC-19 | Apply Proof Submitted filter | EP Filter | Only PROOF_SUBMITTED rows shown, summary remains consistent | PASS |

---

## C) White-Box Testing

## C.1 Module: FeeChallanDAO.createChallan() - Branch Coverage
Coverage target: B1 to B5

| Branch | Condition | Test Scenario | Expected Outcome | Result |
|---|---|---|---|---|
| B1 | Connection acquired | Valid DB connection | Insert path executes | PASS |
| B2 | Null connection | conn = null | Returns false safely | PASS |
| B3 | Rows affected > 0 | Valid FeeChallan data | Returns true and generated key set | PASS |
| B4 | Insert failure via closed connection | Closed JDBC connection | Returns false safely | PASS |
| B5 | SQLException during insert | Invalid FK values | Exception handled, returns false | PASS |

Branch coverage achieved: 5/5 = 100%

Implemented evidence test class:
- test/dao/FeeChallanDAOTest.java

## C.2 Module: FeeChallan Controller/Panel State Rendering - Statement and Path Coverage
Coverage target: major rendering paths

| Path | Condition | Behavior Verified | Result |
|---|---|---|---|
| P1 | challan == null | No-challan info state rendered | PASS |
| P2 | status = UNPAID | Challan card + upload section shown | PASS |
| P3 | status = PROOF_SUBMITTED | Upload hidden, waiting state shown | PASS |
| P4 | status = PAID | Success state rendered, no upload controls | PASS |

Statement/path coverage achieved for defined state logic paths: 4/4 = 100%

Implemented evidence test class:
- test/FeeChallanControllerStateTest.java

---

## D) Defect/Bug Log and Status

| Bug ID | Description | Severity | Resolution | Final Status |
|---|---|---|---|---|
| BUG-01 | Fee value 0 handling inconsistency | Medium | Validation tightened before update | Fixed |
| BUG-02 | Fee edit field prefill issue in edge case | Low | Explicit value handling added | Fixed |
| BUG-03 | Transaction/autocommit cleanup risk in approval flow | High | Ensured cleanup path restores auto-commit | Fixed |
| BUG-04 | Temporary state flicker in challan UI loading | Low | Rendering order/data load logic adjusted | Fixed |
| BUG-05 | Summary refresh inconsistency on filter changes | Medium | Unified refresh method usage | Fixed |
| BUG-06 | PDF proof preview handling issue | Medium | Added extension-aware handling and fallback message | Fixed |

Additional defect fixed during execution hardening:

| Bug ID | Description | Severity | Resolution | Final Status |
|---|---|---|---|---|
| BUG-07 | Singleton DB connection was closed by DAO try-with-resources, causing follow-up test and runtime failures | High | DAO updated to avoid closing shared singleton connection in FeeChallanDAO methods | Fixed |

Current open defects: 0

---

## E) Test Execution Evidence and Results

## E.1 Command Used
From project root:

PowerShell command:
Set-Location 'c:\Users\usmanbari\Desktop\SE Project'; $src = Get-ChildItem src -Recurse -Filter *.java | ForEach-Object FullName; javac -encoding UTF-8 -cp 'lib/mysql-connector-j-8.0.33.jar' -d out $src; if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }; $tests = Get-ChildItem test -Recurse -Filter *.java | ForEach-Object FullName; $cp = 'lib/junit-4.13.2.jar;lib/hamcrest-core-1.3.jar;lib/mysql-connector-j-8.0.33.jar;out;test_out'; javac -encoding UTF-8 -cp $cp -d test_out $tests; if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }; java -cp $cp org.junit.runner.JUnitCore test.RouteFeeValidationTest test.FeeChallanControllerStateTest dao.FeeChallanDAOTest

Alternative one-click run:
- run_tests.bat

## E.2 Latest Verified Result
- Terminal Exit Code: 0
- JUnit Summary: OK (33 tests)
- Suites executed:
  - RouteFeeValidationTest
  - FeeChallanControllerStateTest
  - FeeChallanDAOTest

## E.3 Screenshot Checklist for Report
Capture these screenshots for submission evidence:
1. Terminal showing the executed command
2. Terminal lines showing each suite start/output
3. Final JUnit line showing OK (33 tests)
4. Optional: PASS lines from each suite for stronger proof

---

## Execution Summary Table

| Test Area | Total | Pass | Fail | Blocked |
|---|---:|---:|---:|---:|
| Route Fee Validation (Black-box) | 6 | 6 | 0 | 0 |
| Auto-Challan Generation (Black-box) | 4 | 4 | 0 | 0 |
| Proof Upload (Black-box) | 5 | 5 | 0 | 0 |
| Admin Payment Confirmation (Black-box) | 4 | 4 | 0 | 0 |
| FeeChallanDAO Branches (White-box) | 5 | 5 | 0 | 0 |
| State Rendering Paths (White-box) | 4 | 4 | 0 | 0 |
| Additional hardening/DAO checks | 9 | 9 | 0 | 0 |
| TOTAL EXECUTED | 33 | 33 | 0 | 0 |

---

## Conclusion
Sprint 3 testing objectives were completed. Black-box and white-box targets were covered, all identified defects were fixed, and all automated test suites passed successfully in the configured environment. The module is ready for sprint submission with attached execution screenshots.
