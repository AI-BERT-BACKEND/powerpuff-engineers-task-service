# Task Service

> **Microservicio de Gestión Inteligente de Tareas Académicas** para la plataforma **AI-BERT**.  
> 
> Un servicio backend especializado en optimizar la productividad estudiantil mediante la creación, organización automática y visualización inteligente de tareas académicas. Implementa algoritmos de priorización y distribución temporal para equilibrar la carga de trabajo del estudiante, prevenir el agotamiento académico (burnout) y mejorar significativamente su rendimiento. Construido con **Spring Boot 3.4**, **Java 21** y una arquitectura hexagonal, garantiza escalabilidad, mantenibilidad y calidad de código con cobertura de tests del 90%.

---

## Tabla de Contenido

- [Descripción General](#descripción-general)
- [Contexto y Problema](#contexto-y-problema)
- [Funcionalidades](#funcionalidades)
- [Arquitectura](#arquitectura)
- [Integraciones](#integraciones)
- [Patrones de Diseño](#patrones-de-diseño)
- [Stack Tecnológico](#stack-tecnológico)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Endpoints Expuestos](#endpoints-expuestos)
- [Modelos de Dominio](#modelos-de-dominio)
- [Perfiles de Ejecución](#perfiles-de-ejecución)
- [Variables de Entorno](#variables-de-entorno)
- [Instalación y Ejecución](#instalación-y-ejecución)
- [Pruebas y Calidad](#pruebas-y-calidad)
- [Diagramas](#diagramas)
- [Requerimientos](#requerimientos)

---

## Descripción General

**task-service** es uno de los microservicios del backend de AI-BERT. Es responsable de todo el ciclo de vida de las tareas académicas de un estudiante: desde su creación hasta su organización automática y visualización en vistas Kanban o Calendario.

El servicio sigue una **arquitectura hexagonal (Ports & Adapters)** y está construido con **Spring Boot 3.4 + Java 21**.

---

![Task Service Logo](./docs/img/logo.png)

---

## Integrantes del Equipo

| Nombre | Rol |
|--------|-----|
| Juan David Valero | Backend - Arquitectura |
| Isaac David Burgos | Líder / Frontend |
| Daniel Felipe Rayo | Backend - DevOps |
| Juan Hernández Moreno | Frontend / Líder |
| Daniel Peña Bonilla | Frontend |

---

## Contexto y Problema

Muchos estudiantes universitarios no fallan por falta de capacidad, sino por una **mala gestión del tiempo**:

- Subestiman el tiempo real que requieren las tareas.
- Priorizan incorrectamente (trabajan en lo urgente, no en lo importante).
- Sobrecargan días específicos y dejan otros vacíos.
- Toman decisiones reactivas en vez de estratégicas.

Esto resulta en estrés acumulado, bajo rendimiento académico y burnout estudiantil.

Este microservicio forma parte de la solución: un sistema que **analiza, prioriza y optimiza** cómo el estudiante usa su tiempo.

---

## Funcionalidades

| ID  | Funcionalidad | Descripción |
|-----|--------------|-------------|
| R11 | **Crear tarea** | Registra una nueva tarea académica asociada a un estudiante y una materia. Valida duplicados y la existencia de la materia. |
| R12 | **Organizar tareas (inteligente)** | Distribuye automáticamente las tareas del estudiante en días disponibles según prioridad, deadline y duración estimada (máx. 240 min/día). |
| R13 | **Vista Kanban / Calendario** | Agrupa las tareas por estado (TODO, IN_PROGRESS, COMPLETED) o filtra por rango de fechas y estado para una vista de calendario. |

---

## Arquitectura

El servicio implementa **Arquitectura Hexagonal** con las siguientes capas:

```
┌────────────────────────────────────────────────────────┐
│                    Entrypoints                         │
│         REST Controller · GlobalExceptionHandler       │
├────────────────────────────────────────────────────────┤
│                  Application Layer                     │
│      Use Cases · DTOs · Mappers · TaskService          │
├────────────────────────────────────────────────────────┤
│                   Domain Layer                         │
│    Task · TaskStatus · TaskPriority · Ports (in/out)   │
├────────────────────────────────────────────────────────┤
│                Infrastructure Layer                    │
│  JPA Adapter · InMemory Adapter · Feign Client         │
│  TaskEntity · TaskEntityMapper · TaskJpaRepository     │
└────────────────────────────────────────────────────────┘
```

### Ports & Adapters

**Puertos de entrada (in):**
- `CreateTaskUseCase`
- `GetTasksUseCase`
- `GetTasksForViewUseCase`
- `UpdateTaskStatusUseCase`
- `OrganizeTasksUseCase`
- `TaskOrganizerUseCase`

**Puertos de salida (out):**
- `TaskRepositoryPort` → implementado por `TaskRepositoryAdapter` (PostgreSQL) o `InMemoryTaskRepository`
- `SubjectValidationPort` → implementado por `SubjectServiceFeignAdapter` (Feign) o `SubjectValidationAdapter` (stub)

---

## Integraciones

### Diagrama de dependencias

```
                        ┌─────────────────────┐
                        │     API Gateway      │
                        │  (valida JWT, inyecta│
                        │   header X-User-Id)  │
                        └─────────┬───────────┘
                                  │ HTTP REST
                                  ▼
                        ┌─────────────────────┐
                        │    task-service      │
                        │     :8084            │
                        └──────────┬──────────┘
                                   │ Feign (HTTP)
                                   │ perfil: feign
                                   ▼
                        ┌──────────────────────────────────┐
                        │       academic-service           │
                        │           :8083                  │
                        │  GET /api/v1/subjects/{subjectId}│
                        └──────────────────────────────────┘

                        ┌─────────────────────┐
                        │      PostgreSQL      │
                        │  :5440 (Docker)      │
                        │  :5432 (local)       │
                        └─────────────────────┘
```

### API Gateway

- **Relación:** consumidor (task-service es llamado por el gateway)
- **Protocolo:** HTTP REST
- **Mecanismo de seguridad:** El gateway valida el JWT y **no reenvía el token** al task-service. En cambio, inyecta el header `X-User-Id` con el ID del estudiante autenticado.
- **Impacto:** el task-service no necesita llamar a ningún auth-service ni validar tokens JWT propios.

### academic-service

- **Relación:** dependencia saliente (task-service llama a academic-service)
- **Protocolo:** HTTP REST vía **OpenFeign**
- **Puerto por defecto:** `8083` (`ACADEMIC_SERVICE_URL`)
- **Cuándo se activa:** solo con el perfil `feign`; sin él se usa un stub que acepta cualquier `subjectId`
- **Resiliencia:** Resilience4j **Circuit Breaker** activo — si academic-service falla, se ejecuta `AcademicServiceFallback`

| Endpoint consumido | Método | Uso |
|-------------------|--------|-----|
| `GET /api/v1/subjects/{subjectId}` | `getSubjectById()` | Validar que la materia existe antes de crear una tarea |

**Comportamiento del fallback (`AcademicServiceFallback`):**

| Método | Respuesta en fallo | Efecto |
|--------|-------------------|--------|
| `getSubjectById()` | Retorna `null` | `SubjectServiceFeignAdapter` detecta el `null` y lanza `SubjectNotFoundException` |

### Base de datos

- **Motor:** PostgreSQL 16
- **Puerto Docker:** `5440` → `5432` (interno)
- **Migraciones:** Flyway — script `V1__create_tasks_table.sql`
- **Solo activa con perfil:** `postgres`

### Resumen de integraciones

| Servicio | Dirección | Protocolo | Perfil requerido | Resiliencia |
|---------|-----------|-----------|-----------------|-------------|
| **API Gateway** | → task-service | HTTP REST | Cualquiera | — |
| **academic-service** | task-service → | HTTP/Feign | `feign` | Circuit Breaker + Fallback (Resilience4j) |
| **PostgreSQL** | task-service → | JDBC/JPA | `postgres` | HikariCP (pool) |
| **auth-service** | — | — | — | task-service **NO** llama a auth-service. El JWT es validado únicamente por el API Gateway. |

---

## Patrones de Diseño

### Arquitectura

| Patrón | Dónde se aplica | Beneficio |
|--------|-----------------|-----------|
| **Hexagonal Architecture** (Ports & Adapters) | Todo el servicio | Desacopla el dominio de la infraestructura; se puede cambiar la BD o el cliente Feign sin tocar la lógica de negocio |
| **Layered Architecture** | Capas domain → application → infrastructure → entrypoints | Cada capa solo conoce la capa inmediatamente inferior |

### Creacionales

| Patrón | Dónde se aplica | Beneficio |
|--------|-----------------|-----------|
| **Builder** | `Task`, `TaskEntity`, `TaskResponse`, `CreateTaskRequest`, `KanbanResponse` (via Lombok `@Builder`) | Construcción legible de objetos complejos sin constructores telescópicos |
| **Factory Method** | `TaskDtoMapper.toModel()` / `TaskEntityMapper.toEntity()` | Centraliza la creación de objetos a partir de otros |

### Estructurales

| Patrón | Dónde se aplica | Beneficio |
|--------|-----------------|-----------|
| **Adapter** | `TaskRepositoryAdapter`, `InMemoryTaskRepository`, `SubjectServiceFeignAdapter`, `SubjectValidationAdapter` | Traduce la interfaz del puerto de dominio a la tecnología concreta (JPA, Feign, memoria) |
| **Proxy / Fallback** | `AcademicServiceFallback` implementa `AcademicServiceClient` | Resilience4j activa el fallback automáticamente ante fallos del servicio externo |

### Comportamentales

| Patrón | Dónde se aplica | Beneficio |
|--------|-----------------|-----------|
| **Strategy** | `TaskOrganizerServiceImpl.buildComparator(SortCriteriaEnum)` | Selecciona en tiempo de ejecución el comparador de ordenamiento (`PRIORITY`, `DEADLINE`, `SUBJECT`) |
| **Template Method** | `OrganizeTasksUseCaseImpl` — pasos fijos: obtener → ordenar → asignar fecha → guardar | Define el esqueleto del algoritmo de organización |
| **Chain of Responsibility** | `GlobalExceptionHandler` con múltiples `@ExceptionHandler` | Cada handler captura su excepción específica, los no capturados suben en la cadena |

### Otros patrones aplicados

| Patrón | Dónde se aplica |
|--------|----------------|
| **DTO (Data Transfer Object)** | `CreateTaskRequest`, `UpdateTaskStatusRequest`, `TaskResponse`, `KanbanResponse`, `SubjectDTO` — evitan exponer el modelo de dominio en la API |
| **Repository** | `TaskRepositoryPort` + sus implementaciones — abstrae el acceso a datos |
| **Mapper** | `TaskDtoMapper`, `TaskEntityMapper` — transformación explícita entre capas |
| **Dependency Injection** | Todo el proyecto vía Spring IoC (`@RequiredArgsConstructor`) |
| **Profile-based Configuration** | Perfiles `inmemory`, `postgres`, `feign` — selección de adaptadores sin recompilar |
| **Circuit Breaker** | Resilience4j en `SubjectServiceFeignAdapter` — corta llamadas a academic-service si falla |

---

## Stack Tecnológico

| Área | Tecnología | Versión |
|------|-----------|---------|
| Lenguaje | Java | 21 |
| Framework | Spring Boot | 3.4.3 |
| Persistencia | Spring Data JPA + PostgreSQL | 16 |
| Migraciones | Flyway | — |
| Comunicación externa | OpenFeign + Resilience4j | Spring Cloud 2024.0.1 |
| Seguridad | Spring Security (CORS) | Spring Boot 3.4.3 |
| Documentación API | SpringDoc OpenAPI (Swagger UI) | 2.8.5 |
| Código boilerplate | Lombok | 1.18.32 |
| Testing | JUnit 5 + Mockito + H2 | — |
| Cobertura | JaCoCo | — |
| Calidad | SonarCloud | — |
| Contenedores | Docker + Docker Compose | — |

---

## Estructura del Proyecto

```
task-service/
├── src/
│   ├── main/
│   │   ├── java/com/aibert/dosw/
│   │   │   ├── application/
│   │   │   │   ├── dto/                    # CreateTaskRequest, TaskResponse, KanbanResponse, SubjectDTO
│   │   │   │   ├── mapper/                 # TaskDtoMapper
│   │   │   │   └── usecase/                # Implementaciones de casos de uso
│   │   │   ├── config/
│   │   │   │   ├── OpenApiConfig.java      # Configuración Swagger/OpenAPI
│   │   │   │   └── SecurityConfig.java     # Spring Security + CORS
│   │   │   ├── domain/
│   │   │   │   ├── exceptions/             # TaskNotFoundException, TaskConflictException, SubjectNotFoundException
│   │   │   │   ├── model/                  # Task, TaskStatus, TaskPriority, SortCriteriaEnum
│   │   │   │   └── ports/
│   │   │   │       ├── in/                 # Interfaces de casos de uso
│   │   │   │       └── out/                # Interfaces de repositorio y validación
│   │   │   ├── entrypoints/
│   │   │   │   ├── advice/                 # GlobalExceptionHandler
│   │   │   │   └── rest/controller/        # TaskController
│   │   │   └── infrastructure/
│   │   │       ├── adapters/               # TaskRepositoryAdapter, InMemoryTaskRepository
│   │   │       │   └── persistence/        # TaskEntity, TaskEntityMapper, TaskJpaRepository
│   │   │       └── external/               # AcademicServiceClient (Feign), AcademicServiceFallback
│   │   └── resources/
│   │       ├── application.yml             # Configuración única con perfiles
│   │       └── db/migration/               # Scripts Flyway (V1__create_tasks_table.sql)
│   └── test/
│       └── java/com/aibert/dosw/           # Tests unitarios con Mockito y H2
├── docs/
│   └── diagrams/
│       ├── db-diagram.puml                 # Diagrama de base de datos
│       └── class-diagram.puml              # Diagrama de clases
├── docker-compose.yml                      # PostgreSQL + app
├── Dockerfile                              # Multi-stage build (JDK builder + JRE runtime)
└── pom.xml
```

---

## Endpoints Expuestos

Base URL: `http://localhost:8084`

> Todos los endpoints requieren el header `X-User-Id` inyectado por el API Gateway (excepto los que reciben `studentId` por path).

---

### `POST /api/tasks` — Crear tarea

Crea una nueva tarea académica para el estudiante autenticado. Valida que la materia exista en academic-service (con perfil `feign`) y que no haya duplicado por título + materia.

**Header de entrada:**

| Header | Tipo | Requerido | Descripción |
|--------|------|-----------|-------------|
| `X-User-Id` | String | ✅ | ID del estudiante autenticado (inyectado por API Gateway) |

**Body de entrada (`application/json`):**

| Campo | Tipo | Requerido | Validación |
|-------|------|-----------|------------|
| `title` | String | ✅ | No vacío |
| `description` | String | ❌ | Opcional |
| `estimatedDurationMinutes` | Integer | ✅ | Mayor a 0 |
| `deadline` | LocalDateTime | ✅ | Debe ser fecha futura |
| `priority` | `LOW \| MEDIUM \| HIGH \| CRITICAL` | ✅ | Enum |
| `subjectId` | String | ✅ | ID de la materia (validado contra academic-service) |

**Respuesta exitosa `201 Created`:**

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | String (UUID) | ID generado de la tarea |
| `studentId` | String | ID del estudiante |
| `subjectId` | String | ID de la materia |
| `title` | String | Título de la tarea |
| `description` | String | Descripción (puede ser null) |
| `estimatedDurationMinutes` | Integer | Duración estimada en minutos |
| `deadline` | LocalDateTime | Fecha límite |
| `priority` | String | Prioridad asignada |
| `status` | String | `TODO` (valor por defecto) |
| `scheduledDate` | LocalDateTime | Fecha programada (null hasta organizar) |
| `completedAt` | LocalDateTime | Fecha de completado (null) |

**Errores:**

| Código | Causa |
|--------|-------|
| `400` | Campos inválidos o faltantes |
| `404` | La materia (`subjectId`) no existe en academic-service |
| `409` | Ya existe una tarea con el mismo título para esa materia |

---

### `GET /api/tasks` — Listar tareas del estudiante

Retorna las tareas del estudiante autenticado. Soporta tres modos según el parámetro `view`.

**Header de entrada:**

| Header | Tipo | Requerido |
|--------|------|-----------|
| `X-User-Id` | String | ✅ |

**Query params:**

| Parámetro | Tipo | Requerido | Descripción |
|-----------|------|-----------|-------------|
| `view` | `kanban \| calendar` | ❌ | Sin este param retorna lista ordenada (R12) |
| `sortBy` | `PRIORITY \| DEADLINE \| SUBJECT` | ❌ | Criterio de ordenamiento (solo sin `view`) |
| `status` | `TODO \| IN_PROGRESS \| COMPLETED` | ❌ | Filtro por estado (solo con `view=calendar`) |
| `startDate` | LocalDateTime (ISO) | ❌ | Inicio del rango (solo con `view=calendar`) |
| `endDate` | LocalDateTime (ISO) | ❌ | Fin del rango (solo con `view=calendar`) |

**Respuesta `200 OK` — Sin `view` o `view=calendar`:** `List<TaskResponse>` (mismos campos que el POST)

**Respuesta `200 OK` — `view=kanban`:**

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `todo` | `List<TaskResponse>` | Tareas con status `TODO` |
| `inProgress` | `List<TaskResponse>` | Tareas con status `IN_PROGRESS` |
| `completed` | `List<TaskResponse>` | Tareas con status `COMPLETED` |

---

### `PATCH /api/tasks/{id}/status` — Actualizar estado

Cambia el estado de una tarea. Al marcar como `COMPLETED` se registra automáticamente `completedAt`.

**Path variable:**

| Variable | Tipo | Descripción |
|----------|------|-------------|
| `id` | String (UUID) | ID de la tarea |

**Body de entrada (`application/json`):**

| Campo | Tipo | Requerido | Valores válidos |
|-------|------|-----------|-----------------|
| `status` | String | ✅ | `TODO \| IN_PROGRESS \| COMPLETED` |

**Respuesta `200 OK`:** `TaskResponse` con el nuevo estado y `completedAt` si aplica.

**Errores:**

| Código | Causa |
|--------|-------|
| `400` | Estado inválido o faltante |
| `404` | Tarea no encontrada |

---

### `GET /api/tasks/student/{studentId}` — Tareas por estudiante

Retorna todas las tareas de un estudiante por su ID de path.

**Path variable:**

| Variable | Tipo | Descripción |
|----------|------|-------------|
| `studentId` | String | ID del estudiante |

**Respuesta `200 OK`:** `List<TaskResponse>`

---

### `POST /api/tasks/student/{studentId}/organize` — Organizar tareas

Distribuye automáticamente las tareas pendientes del estudiante en días disponibles según prioridad, deadline y duración estimada (máximo 240 min/día).

**Path variable:**

| Variable | Tipo | Descripción |
|----------|------|-------------|
| `studentId` | String | ID del estudiante |

**Respuesta `200 OK`:** `List<TaskResponse>` con `scheduledDate` asignado en cada tarea organizada.

---

### Documentación interactiva

- **Swagger UI:** `http://localhost:8084/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8084/api-docs`

---

## Modelos de Dominio

### Task

| Campo | Tipo | Restricción |
|-------|------|-------------|
| `id` | String (UUID) | PK, generado automáticamente |
| `studentId` | String | NOT NULL |
| `subjectId` | String | NOT NULL, validado contra academic-service |
| `title` | String | NOT NULL |
| `description` | String | Opcional |
| `estimatedDurationMinutes` | Integer | NOT NULL, > 0 |
| `deadline` | LocalDateTime | NOT NULL |
| `priority` | TaskPriority | NOT NULL |
| `status` | TaskStatus | DEFAULT: `TODO` |
| `scheduledDate` | LocalDateTime | Asignado por el organizador |
| `completedAt` | LocalDateTime | Asignado al marcar como COMPLETED |

### Enumeraciones

**TaskStatus:** `TODO` · `IN_PROGRESS` · `COMPLETED`

**TaskPriority:** `LOW` · `MEDIUM` · `HIGH` · `CRITICAL`

**SortCriteriaEnum:** `PRIORITY` · `DEADLINE` · `SUBJECT`

### Restricciones de base de datos

- **UNIQUE:** `(student_id, subject_id, title)` — no se permiten tareas duplicadas
- **CHECK:** prioridad y estado solo aceptan los valores de los enums
- **INDEX:** `student_id`, `deadline`, `status`

---

## Perfiles de Ejecución

El servicio usa perfiles de Spring Boot para alternar entre implementaciones:

| Perfil | Repositorio | Validación de materias | Base de datos |
|--------|------------|----------------------|---------------|
| `inmemory` (default) | `InMemoryTaskRepository` | `SubjectValidationAdapter` (stub) | Ninguna |
| `postgres` | `TaskRepositoryAdapter` (JPA) | `SubjectValidationAdapter` (stub) | PostgreSQL |
| `postgres,feign` | `TaskRepositoryAdapter` (JPA) | `SubjectServiceFeignAdapter` (Feign → academic-service) | PostgreSQL |

---

## Variables de Entorno

Solo requeridas con el perfil `postgres`:

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `DB_HOST` | Host de PostgreSQL | `localhost` |
| `DB_PORT` | Puerto de PostgreSQL | `5432` |
| `DB_NAME` | Nombre de la base de datos | `taskdb` |
| `DB_USERNAME` | Usuario de PostgreSQL | `taskuser` |
| `DB_PASSWORD` | Contraseña de PostgreSQL | `secret` |
| `DB_SSL_MODE` | Modo SSL | `disable` / `require` |
| `ACADEMIC_SERVICE_URL` | URL del academic-service | `http://localhost:8083` |

---

## Instalación y Ejecución

### Requisitos previos

- Java 21+
- Maven 3.9+ (o usar el wrapper `./mvnw`)
- Docker & Docker Compose (para el perfil postgres)

### Opción 1: In-Memory (sin base de datos)

Ideal para desarrollo y pruebas locales rápidas. No persiste datos al reiniciar.

```bash
./mvnw spring-boot:run "-Dspring-boot.run.profiles=inmemory"
```

La API queda disponible en `http://localhost:8084`.

### Opción 2: Docker Compose con PostgreSQL (recomendado)

Levanta PostgreSQL y la aplicación con un solo comando.

```bash
# 1. Crear archivo de variables de entorno
cp .env.example .env
# Editar .env con tus credenciales

# 2. Construir y levantar
docker-compose up --build
```

El perfil activo con Docker es `postgres,feign`.

```bash
# Detener
docker-compose down

# Detener y eliminar volumen de datos
docker-compose down -v
```

### Opción 3: Local con PostgreSQL externo

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=taskdb
export DB_USERNAME=taskuser
export DB_PASSWORD=secret
export ACADEMIC_SERVICE_URL=http://localhost:8083

./mvnw spring-boot:run "-Dspring-boot.run.profiles=postgres,feign"
```

### Compilar el JAR

```bash
./mvnw package -DskipTests
java -jar target/task-service-1.0.0.jar
```

---

## Pruebas y Calidad

### Ejecutar tests

```bash
# Solo tests
./mvnw test

# Tests + reporte de cobertura JaCoCo
./mvnw verify
```

El reporte HTML de cobertura se genera en `target/site/jacoco/index.html`.

### Análisis SonarCloud

```bash
./mvnw verify sonar:sonar -Dsonar.token=<TOKEN>
```

### Cobertura actual

| Métrica | Cubierto | Total | Cobertura |
|---------|----------|-------|-----------|
| Líneas | 1964 | 2188 | **90%** |
| Ramas | 509 | 745 | **68%** |
| Métodos | 744 | 794 | **94%** |

### Quality Gate SonarCloud

- Bugs críticos: **0**
- Quality Gate: **✅ Passed**

### Clases excluidas del análisis

Las siguientes clases son excluidas de SonarCloud por ser código generado/configuración:

- `TaskServiceApplication`
- Paquetes `dto`, `config`, `domain/model`, `domain/exceptions`

---

## Diagramas

Los diagramas están en formato PlantUML en `docs/diagrams/`:

| Diagrama | Archivo | Descripción |
|----------|---------|-------------|
| Base de datos | [db-diagram.puml](docs/diagrams/db-diagram.puml) | Estructura de la tabla `tasks` con constraints e índices |
| Clases | [class-diagram.puml](docs/diagrams/class-diagram.puml) | Diagrama de clases completo por capa arquitectónica |

Para visualizar, usa la extensión **PlantUML** en VS Code (`Alt+D`).

---

## Requerimientos

### Funcionales

| ID | Requerimiento | Estado |
|----|--------------|--------|
| R11 | Crear tareas académicas | ✅ Implementado |
| R12 | Organizar tareas inteligentemente | ✅ Implementado |
| R13 | Vista Kanban y Calendario | ✅ Implementado |

### No Funcionales

| ID | Requerimiento | Métrica |
|----|--------------|---------|
| RNF-01 | Disponibilidad | 99.9% |
| RNF-02 | Tiempo de respuesta | < 2s |
| RNF-03 | Seguridad | Spring Security + JWT (validado por API Gateway) |
| RNF-04 | Cobertura de tests | ≥ 80% |

---

## Evidencia Swagger

![Evidencia Swagger](docs/img/captura_swagger.png)
