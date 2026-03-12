# Contributing to GrowthPlatform

Thank you for your interest in contributing! This guide will help you get started.

## Getting Started

### Prerequisites

- Java 21 (LTS)
- Maven 3.9+
- MySQL 8.0+
- Redis 7.x
- RocketMQ 5.x (for event/message modules)

### Local Development

1. Fork and clone the repository
2. Import the project into your IDE (IntelliJ IDEA recommended)
3. Copy `application-local.yml.example` to `application-local.yml` and configure your local environment
4. Run `mvn clean install` to build

## How to Contribute

### Reporting Issues

- Use GitHub Issues to report bugs or request features
- Search existing issues before creating a new one
- Use the provided issue templates

### Submitting Changes

1. Create a feature branch from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```
2. Make your changes following the coding conventions below
3. Write or update tests as needed
4. Ensure all tests pass: `mvn clean verify`
5. Commit with a clear message:
   ```bash
   git commit -m "feat(module): add xxx feature"
   ```
6. Push and create a Pull Request

### Commit Message Convention

We follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <subject>

[optional body]
```

**Types:** `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

**Scope:** module name (e.g., `profile`, `campaign`, `coupon`)

**Examples:**
- `feat(coupon): add batch grant API`
- `fix(campaign): resolve DAG cycle detection edge case`
- `docs(profile): update audience rule API documentation`

## Coding Conventions

- Follow standard Java naming conventions
- Use `io.growth.platform.<module>` as base package
- Database tables use `gp_<module>_` prefix
- REST APIs follow `/api/v1/<module>/<resource>` pattern
- Write unit tests for all service layer methods
- Use MapStruct for object mapping

## Code Review

All submissions require review before merging. Reviewers will check:

- Code quality and adherence to conventions
- Test coverage
- Documentation updates
- No breaking changes to public APIs

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.