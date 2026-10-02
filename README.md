# api-automation

A REST API test automation framework built with **Java 17**, **REST Assured** and **TestNG**.

It tests the public [JSONPlaceholder](https://jsonplaceholder.typicode.com) API. The included test finds a user by username, fetches every post that user wrote, fetches every comment on those posts, and checks that each comment's email address is correctly formatted.

## Tech stack

| Tool | Purpose |
|------|---------|
| Java 17 | Language |
| Maven | Build and dependency management |
| TestNG | Test runner, assertions and soft assertions |
| REST Assured | Sending HTTP requests |
| Jackson | Mapping JSON responses onto Java objects |
| org.json | Parsing JSON arrays |
| Log4j2 | Logging to the console and `applog.txt` |
| CircleCI | Continuous integration |

## Project structure

```
api-automation
├── .circleci/config.yml            CircleCI pipeline: runs `mvn clean test`
├── pom.xml                         Maven dependencies and plugins
├── testng.xml                      TestNG suite (list of test classes to run)
└── src
    ├── main/java/com/automation/api
    │   ├── builder
    │   │   ├── RequestBuilder      Builds and sends HTTP requests
    │   │   └── ResponseBuilder     Maps JSON responses onto Java objects
    │   ├── constants
    │   │   ├── StatusCode          Expected HTTP status codes
    │   │   └── URL                 Base URL and endpoint paths
    │   ├── manager                 One class per API resource
    │   │   ├── Manager             Gives tests access to all managers
    │   │   ├── UserManager         /users
    │   │   ├── PostManager         /posts
    │   │   └── CommentManager      /comments
    │   ├── response                POJOs for the JSON responses
    │   │   ├── User
    │   │   ├── Post
    │   │   └── Comment
    │   ├── testdata
    │   │   └── TestDataLoader      Loads data.properties into typed fields
    │   └── utils
    │       ├── JsonHelper          JSON parsing helpers
    │       ├── PropertiesReader    Reads data.properties
    │       └── Util                Regex matching and logging
    ├── main/resources
    │   └── log4j2.properties       Logging configuration
    ├── resources
    │   └── data.properties         Test data (e.g. username)
    └── test/java/com/automation/api/tests
        ├── BaseTest                Parent class of all tests
        └── CommentTest             Email format test on comments
```

## How it works

The framework has three layers:

1. **Tests** (`tests`) describe the scenario in business terms and contain no HTTP code.
2. **Managers** (`manager`) wrap one API resource each. They send the request, check the status code, and return Java objects.
3. **Builders** (`builder`) do the low-level work: `RequestBuilder` sends requests with REST Assured, and `ResponseBuilder` turns JSON into POJOs with Jackson.

Test classes extend `BaseTest`, so they can reach the managers (`getUserManager()`, `getPostManager()`, `getCommentManager()`) and the test data (`getUsername()`) directly.

### Test scenario: `CommentTest.validateEmailFormatOnComments`

1. Search for the user by the username in `data.properties` (`GET /users?username=Samantha`) and assert that exactly one user matches.
2. Get all posts written by that user (`GET /posts?userId={id}`).
3. Get all comments on each post (`GET /comments?postId={id}`).
4. Check every comment's email against an email regex. A soft assert collects all failures, so one run reports every badly formatted email.

## Prerequisites

- JDK 17 or newer
- Maven 3.6 or newer
- Internet access (the tests call the live JSONPlaceholder API)

## Running the tests

From the project root:

```bash
mvn clean test
```

Maven runs the suite in `testng.xml`. Results are written to:

- `target/surefire-reports/`: TestNG/Surefire reports
- `applog.txt`: the full request and assertion log

The tests must be run from the project root, because `PropertiesReader` resolves `src/resources/data.properties` relative to the working directory.

To run from an IDE, right-click `testng.xml` or `CommentTest` and choose **Run**.

## Configuration

| What | Where |
|------|-------|
| Username under test | `src/resources/data.properties` → `username` |
| Base URL and endpoints | `src/main/java/com/automation/api/constants/URL.java` |
| Email format rule | `Util.EMAIL_REGEX` |
| Log format and output | `src/main/resources/log4j2.properties` |

## Adding a new test

1. Add the endpoint to `URL`.
2. If the response is a new resource, add a POJO under `response`.
3. Add a manager under `manager` (extend `Manager`) with an `...API(...)` method that sends the request and checks the status code, plus a method that returns POJOs. Expose the manager through a static getter in `Manager`.
4. Write a test class under `tests` that extends `BaseTest`.
5. Add the test class to `testng.xml`.

## Continuous integration

`.circleci/config.yml` runs the suite on every push using the `cimg/openjdk:17.0` image, caching Maven dependencies between builds. `CircleCI_Execution_Report.PNG` shows a sample run.
