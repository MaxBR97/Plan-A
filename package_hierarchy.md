# 5.3 Package Hierarchy

## 5.3.1 Overview

The Plan-A backend system follows a layered architecture with clear separation of concerns. The package hierarchy promotes modularity and maintainability through domain-driven design principles.

## 5.3.2 Root Package Structure

```
com.plan.a.backend/
├── model/           # Core model domain classes
├── image/           # Image management and configuration
├── module/          # Module system and organization
├── solver/          # Optimization solver integration
├── repository/      # Data access layer
├── dto/             # Data transfer objects
├── service/         # Business logic services
├── config/          # Configuration and utilities
└── exception/       # Custom exceptions
```

## 5.3.3 Main Package Descriptions

### Model Package (`com.plan.a.backend.model`)
**Purpose:** Core domain model classes for mathematical optimization.

**Key Classes:**
- `Model` - Main model class managing optimization lifecycle
- `ModelComponent` - Abstract base for all model components
- `ModelSet`, `ModelParameter`, `ModelVariable` - Concrete components
- `ModelType` - Type system for components

**Responsibilities:**
- Define fundamental model structure
- Manage component relationships and dependencies
- Handle model type system and validation

### Image Package (`com.plan.a.backend.image`)
**Purpose:** Manages complete model configurations (Images).

**Key Classes:**
- `Image` - Complete model configuration container
- `ImageMetadata` - Image metadata and properties

**Responsibilities:**
- Handle image creation, configuration, and lifecycle
- Manage image metadata and access control
- Coordinate solver configuration and solution processing

### Solver Package (`com.plan.a.backend.solver`)
**Purpose:** Integrates with external optimization solvers.

**Key Classes:**
- `SolverService` - High-level solver service interface
- `ScipProcess` - SCIP solver process management
- `Solution` - Optimization solution representation

**Responsibilities:**
- Manage solver process lifecycle
- Handle solver communication and monitoring
- Process optimization results and solutions

### Module Package (`com.plan.a.backend.module`)
**Purpose:** Organizes model components into logical modules.

**Key Classes:**
- `Module` - Abstract base for all module types
- `ConstraintModule`, `PreferenceModule`, `VariableModule` - Concrete modules

**Responsibilities:**
- Define module types and their relationships
- Manage module configuration and component organization

### Repository Package (`com.plan.a.backend.repository`)
**Purpose:** Data access layer for persistent storage.

**Key Classes:**
- `ImageRepository` - Image data access interface
- `ModelRepository` - Model file storage interface

**Responsibilities:**
- Define repository interfaces and implementations
- Handle data persistence and retrieval operations

### Service Package (`com.plan.a.backend.service`)
**Purpose:** Business logic services orchestrating operations.

**Key Classes:**
- `ImageService` - Image management business logic
- `ModelService` - Model processing business logic
- `SolverService` - Solver integration business logic

**Responsibilities:**
- Implement business logic and use cases
- Coordinate operations between different layers

### DTO Package (`com.plan.a.backend.dto`)
**Purpose:** Data transfer objects for API communication.

**Key Classes:**
- `ModelDTO` - Complete model structure DTO
- `InputDTO` - Model input configuration DTO
- `ImageDTO` - Image configuration DTO

**Responsibilities:**
- Define DTOs for API requests and responses
- Handle data transformation between layers

## 5.3.4 Package Dependencies

### Dependency Rules
1. **Layered Architecture:** Higher-level packages depend on lower-level packages
2. **Domain Isolation:** Domain packages (model, image, module) are independent
3. **Service Layer:** Service package depends on domain and repository packages
4. **DTO Layer:** DTO package is independent and used by service layer

### Dependency Matrix

| Package | Depends On |
|---------|------------|
| model | config, exception |
| image | model, config, exception |
| module | model, image, config, exception |
| solver | model, config, exception |
| repository | model, image, module, config, exception |
| dto | config, exception |
| service | model, image, module, solver, repository, dto, config, exception |
| config | exception |
| exception | (none) |

## 5.3.5 Design Principles

### Single Responsibility Principle
Each package has a single, well-defined responsibility:
- Model package: Core domain model
- Image package: Model configuration management
- Module package: Component organization
- Solver package: Optimization solving
- Repository package: Data access
- Service package: Business logic
- DTO package: Data transfer

### Dependency Inversion Principle
High-level packages depend on abstractions, not concrete implementations:
- Repository interfaces defined in domain packages
- Service interfaces for business logic
- Abstract base classes for model components

### Open/Closed Principle
Packages are designed to be open for extension but closed for modification:
- Abstract base classes in domain packages
- Interface-based repository design
- Pluggable solver implementations
- Extensible module system

This package hierarchy provides a solid foundation for the Plan-A backend system, promoting maintainability, scalability, and clear separation of concerns. 