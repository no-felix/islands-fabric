# Stormbound Isles Coding Style Guide

This document outlines the coding standards, best practices, and architectural patterns used in the Stormbound Isles mod. Following these guidelines ensures consistency, maintainability, and quality throughout the codebase.

## Table of Contents
1. [Java Coding Standards](#java-coding-standards)
2. [Project Structure](#project-structure)
3. [Minecraft Fabric Best Practices](#minecraft-fabric-best-practices)
4. [Git Workflow](#git-workflow)
5. [Documentation](#documentation)
6. [Testing](#testing)
7. [Annotations and Automation](#annotations-and-automation)

## Java Coding Standards

### Java Version
- The project uses **Java 21**
- Take advantage of modern Java features:
  - Text blocks (Java 15+)
  - Records (Java 16+)
  - Pattern matching for instanceof (Java 16+)
  - Switch expressions (Java 17+)
  - Record patterns (Java 21+)
  - String templates (Java 21+)

### Naming Conventions
- **Classes**: PascalCase (e.g., `CommandManager`)
- **Interfaces**: PascalCase (e.g., `CommandCategory`)
- **Methods**: camelCase (e.g., `initializeAll()`)
- **Variables**: camelCase (e.g., `teamName`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `ADMIN_PERMISSION_LEVEL`)
- **Packages**: lowercase (e.g., `de.nofelix.stormboundisles.command`)

### Formatting
- **Indentation**: Tabs (size 4) for Java files
- **Line length**: Aim for 100 characters maximum
- **Braces**: Always use braces for control structures, even for single statements
- **Method chaining**: Put each method call on a new line when chaining multiple calls
- **Field declaration**: One field per line
- **Import statements**: No wildcard imports

### Code Organization
- Group related functionality into packages
- Keep classes focused on a single responsibility
- Limit file size to 500-1000 lines where possible
- Order class members:
  1. Static fields
  2. Instance fields
  3. Constructors
  4. Methods (grouped by functionality)
  5. Inner classes/interfaces

## Project Structure

### Package Organization
- `de.nofelix.stormboundisles` - Root package
  - `.command` - Command system
    - `.categories` - Command implementations
    - `.util` - Command utilities
  - `.data` - Data models and persistence
  - `.disaster` - Disaster system
  - `.game` - Game mechanics
  - `.handler` - Event handlers
  - `.init` - Initialization system
  - `.util` - Shared utilities

### Dependency Patterns
- Use dependency injection where appropriate
- Maintain clear boundaries between systems
- Avoid circular dependencies between packages

### Constants and Configuration
- Use `Constants.java` for all hardcoded strings, messages, and numeric values
- Separate user-configurable settings into configuration classes
- Centralize formatting codes and message templates

## Minecraft Fabric Best Practices

### Mod Architecture
- Use entry points appropriately (client vs. server initialization)
- Register content through Fabric's registry system
- Follow Minecraft's client/server separation principles
- Respect the mod loading lifecycle

### Performance Considerations
- Avoid expensive operations in frequently called methods
- Be mindful of memory usage and object creation
- Use profiling tools to identify bottlenecks
- Cache results when appropriate
- Use Minecraft's scheduler for delayed or periodic tasks

### Compatibility
- Test with different Minecraft versions
- Document version-specific code
- Follow Fabric API conventions
- Include proper mod metadata (`fabric.mod.json`)

## Git Workflow

### Branching Strategy
- `main` - Production-ready code
- `develop` - Integration branch for features
- `feature/*` - Feature branches
- `bugfix/*` - Bug fix branches
- `release/*` - Release preparation branches

### Commit Guidelines
- Use present tense in commit messages ("Add feature" not "Added feature")
- Start with a verb
- Keep commits focused on a single change
- Structure commit messages:
  ```
  <type>: <summary>

  <description>
  ```
  Where `<type>` is one of:
  - `feat`: New feature
  - `fix`: Bug fix
  - `refactor`: Code change that neither fixes a bug nor adds a feature
  - `docs`: Documentation changes
  - `style`: Formatting, missing semicolons, etc; no code change
  - `test`: Adding or refactoring tests
  - `chore`: Updating build tasks, package manager configs, etc.

### Pull Requests
- Reference related issues in PR descriptions
- Provide detailed descriptions of changes
- Keep PRs focused on a single feature/fix
- Ensure all tests pass before merging

## Documentation

### Code Comments
- Use Javadoc for all public classes and methods
- Document parameters, return values, and exceptions
- Include code examples where useful
- Explain "why" not just "what" in implementation comments
- Use `// TODO:` comments for planned improvements

### Class-Level Documentation
- Include Javadoc for all classes explaining their purpose and responsibilities
- Document relationships with other components
- Provide usage examples for complex components

### Project Documentation
- Keep README.md up to date
- Document configuration options
- Include setup instructions and requirements
- Provide troubleshooting guides

## Testing

### Unit Testing
- Write unit tests for core business logic
- Use JUnit for test framework
- Mock dependencies when appropriate
- Aim for high test coverage of critical paths

### Integration Testing
- Test interaction between systems
- Validate end-to-end functionality
- Include save/load tests for data persistence

### Testing in Minecraft
- Use in-game testing where necessary
- Document test procedures for manual verification
- Create test worlds for specific scenarios

## Annotations and Automation

### Use of Annotations
- Use `@Initialize` for automatic initialization of components
- Document annotation purposes and behavior
- Set appropriate priorities for initialization order

### Initialization Pattern
- All component initialization should use the `@Initialize` annotation
- Establish clear initialization dependencies through priority values
- Standard priority levels:
  - 2000-3000: Core systems and APIs (e.g., command suggestions)
  - 1500-1999: Managers and services (e.g., polygon builders)
  - 1000-1499: Game elements (e.g., islands, teams)
  - 500-999: Feature implementations
  - 1-499: Late initialization components
- Avoid manual calls to initialization methods when using annotations
- Document dependencies between initialization methods in Javadoc

### Logging
- Use the mod's logger (`StormboundIslesMod.LOGGER`) for all logging
- Choose appropriate log levels:
  - `ERROR`: Exceptions and failures
  - `WARN`: Potential issues or deprecated usage
  - `INFO`: Normal operation information
  - `DEBUG`: Detailed information for debugging
  - `TRACE`: Fine-grained debugging data