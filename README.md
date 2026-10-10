# bookstore-api

A small dummy Spring Boot 3 (Java 17, Maven) REST API with unit, slice and integration tests,
meant for practising CI/CD pipelines.

## API

| Method | Path                                   | Description                     |
|--------|----------------------------------------|---------------------------------|
| GET    | `/api/books`                           | List books (`?author=` filter)  |
| GET    | `/api/books/{id}`                      | Get one book (404 if missing)   |
| GET    | `/api/books/{id}/discounted-price?percent=10` | Price after discount     |
| POST   | `/api/books`                           | Create (validated, returns 201) |
| PUT    | `/api/books/{id}`                      | Update                          |
| DELETE | `/api/books/{id}`                      | Delete (204)                    |
| GET    | `/actuator/health`                     | Health check                    |

Uses an in-memory H2 database, so no external services are required.

## Tests

| Type | Class | Runner | Command |
|------|-------|--------|---------|
| Unit (Mockito) | `BookServiceTest` | Surefire | `mvn test` |
| Web slice (`@WebMvcTest`) | `BookControllerTest` | Surefire | `mvn test` |
| JPA slice (`@DataJpaTest`) | `BookRepositoryTest` | Surefire | `mvn test` |
| Integration (`@SpringBootTest`) | `BookApiIT` | Failsafe | `mvn verify` |

Naming convention: `*Test` runs in the `test` phase, `*IT` runs in the `verify` phase.

```bash
mvn test        # fast unit + slice tests
mvn verify      # everything + JaCoCo coverage (target/site/jacoco/index.html)
mvn spring-boot:run
```

## CI/CD files included

- `.github/workflows/ci.yml` – GitHub Actions: unit tests, integration tests, coverage, jar artifact, Docker build + smoke test
- `Jenkinsfile` – equivalent Jenkins declarative pipeline
- `Dockerfile` – multi-stage build, non-root user, health check

## Docker

```bash
docker build -t bookstore-api .
docker run -p 8080:8080 bookstore-api
curl localhost:8080/actuator/health
```

## Tips for experimenting with your pipeline

- Break a test on purpose (e.g. change the expected value in `BookServiceTest`) to see the pipeline fail.
- Add a coverage gate with the JaCoCo `check` goal.
- Add stages for SonarQube, Trivy image scanning, or deployment to your target (Kubernetes, ECS, etc.).
