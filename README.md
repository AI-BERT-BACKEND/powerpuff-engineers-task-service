# Task Service

## Descripción General

Este repositorio contiene el microservicio **task-service**, parte de una plataforma diseñada para ayudar a estudiantes universitarios a organizar su tiempo de forma inteligente, equilibrando la vida académica y personal para mejorar su rendimiento y evitar el estrés.

### Contexto
Muchos estudiantes universitarios no fallan por falta de capacidad, sino por una mala gestión del tiempo y un desbalance entre la vida académica y personal. Los problemas más comunes incluyen:
- Subestimar el tiempo real que requieren las tareas.
- Priorizar incorrectamente (trabajar en lo urgente, no en lo importante).
- Sobrecargar días específicos y dejar otros vacíos.
- No integrar la vida personal (descanso, ocio, social) en la planificación.
- Tomar decisiones reactivas en vez de estratégicas.

Esto resulta en:
- Estrés acumulado.
- Bajo rendimiento académico.
- Pérdida de materias.
- Burnout estudiantil.

La plataforma no es solo una lista de tareas, sino un sistema que analiza, prioriza y optimiza cómo el estudiante usa su tiempo.

## Microservicio: task-service

Este microservicio es responsable de la **Gestión de Tareas** y soporta las siguientes funcionalidades:

- **R11:** Creación de tareas
- **R12:** Organizador inteligente de tareas
- **R13:** Vista de tareas como tablero Kanban o Calendario

Las tres funcionalidades operan sobre el mismo objeto (la tarea) y comparten el mismo modelo de datos.

## Equipo

*Por definir*

---

# Tabla de Contenido

- Descripción General
- Equipo
- Objetivos
- Planteamiento del Problema
- Requerimientos
- Arquitectura
- Stack Tecnológico
- Diagramas
- Gestión del Proyecto
- Pruebas y Calidad
- Demo
- Instalación
- Referencias

---

# Objetivos

## Objetivo General

Construir un sistema que ayude a los estudiantes universitarios a gestionar y equilibrar su tiempo académico y personal, optimizando el rendimiento y reduciendo el estrés.

## Objetivos Específicos

- Permitir la creación y gestión eficiente de tareas académicas y personales.
- Priorizar y organizar tareas de manera inteligente según importancia y carga de trabajo.
- Visualizar tareas en formatos Kanban y Calendario para una mejor planificación.
- Integrar la vida personal en la planificación académica.
- Prevenir el burnout y mejorar el bienestar estudiantil.

---

# Planteamiento del Problema

## Contexto

Los estudiantes universitarios suelen enfrentar dificultades para equilibrar sus responsabilidades académicas y personales, lo que afecta su rendimiento y bienestar.

## Problema

La mala gestión del tiempo y la falta de integración entre vida académica y personal llevan a estrés, bajo rendimiento y burnout.

## Dificultades Actuales

- Subestimación del tiempo necesario para tareas.
- Priorización incorrecta de actividades.
- Sobrecarga de días específicos.
- Falta de integración de actividades personales.
- Decisiones reactivas en vez de estratégicas.

## Solución Propuesta

Un sistema inteligente que ayude a los estudiantes a planificar, priorizar y equilibrar sus tareas académicas y personales, optimizando el uso del tiempo y mejorando el bienestar general.

---

# Requerimientos

## Requerimientos Funcionales

| ID   | Requerimiento                                         | Módulo        |
|------|-------------------------------------------------------|---------------|
| R11  | Crear tareas                                         | task-service  |
| R12  | Organizar tareas de forma inteligente                 | task-service  |
| R13  | Visualizar tareas en Kanban y Calendario              | task-service  |

## Requerimientos No Funcionales

| ID     | Requerimiento         | Métrica         |
|--------|----------------------|-----------------|
| RNF-01 | Disponibilidad       | 99.9%           |
| RNF-02 | Tiempo de respuesta  | < 2s            |
| RNF-03 | Seguridad            | JWT / OAuth     |

---

# Arquitectura

## Arquitectura General

- Microservicios
- Patrón MVC
- Clean Architecture
- Hexagonal Architecture

## Diagrama General

(Agregar diagrama en docs/diagramas/contexto.png)

---

# Stack Tecnológico

| Área      | Tecnologías                  |
|-----------|-----------------------------|
| Backend   | Java 21, Spring Boot        |
| API       | REST, OpenAPI               |
| Seguridad | Spring Security, JWT        |
| Base de datos | PostgreSQL              |
| Testing   | JUnit, Mockito              |
| DevOps    | Docker, Docker Compose, GitHub Actions |
| Calidad   | SonarCloud, JaCoCo          |

---

# Diagramas

- [Diagrama de contexto](docs/diagramas/contexto.png)
- [Casos de uso](docs/diagramas/casos-uso.png)
- [Diagrama de clases](docs/diagramas/clases.png)
- [Diagrama de componentes](docs/diagramas/componentes.png)
- [ER Diagram](docs/diagramas/er.png)

---

# Gestión del Proyecto

## Metodología

- Scrum

## Sprints

| Sprint   | Objetivo           | Estado |
|----------|--------------------|--------|
| Sprint 1 | Setup proyecto     | ✅     |
| Sprint 2 | Core features      | 🚧     |

## Riesgos

| Riesgo         | Impacto | Mitigación |
|----------------|---------|------------|
| Retrasos       | Alto    | Buffer     |
| Bugs críticos  | Medio   | Testing    |

---

# Pruebas y Calidad

## Estrategia

- Pruebas unitarias
- Pruebas de integración
- Pruebas end-to-end

## Cobertura

Reporte generado con **JaCoCo** y analizado con **SonarCloud**

| Métrica | Cubierto | Total | Cobertura |
|---------|----------|-------|-----------|
| Líneas  | 1964     | 2188  | 90%       |
| Ramas   | 509      | 745   | 68%       |
| Métodos | 744      | 794   | 94%       |

## Calidad

- Bugs: 0 críticos
- Code Smells: Bajo
- Deuda técnica: Baja
- Quality Gate: ✅ Passed

---

# Demo

- [Demo módulo](link-demo)

---

# Instalación

## Requisitos

- Java 21
- Maven 3.9+
- Docker & Docker Compose

## Clonar repositorio

```bash
git clone https://github.com/usuario/proyecto.git
cd proyecto
```

## Opción 1: Con Docker (recomendado)

Levanta PostgreSQL + la aplicación en un solo comando:

```bash
# Copiar variables de entorno
cp .env.example .env

# Construir y levantar todos los servicios
docker-compose up --build
```

La API queda disponible en `http://localhost:8080`.
Swagger UI: `http://localhost:8080/swagger-ui.html`

Para detener:
```bash
docker-compose down
```

Para detener y eliminar el volumen de PostgreSQL:
```bash
docker-compose down -v
```

## Opción 2: Local sin Docker (H2 in-memory)

No requiere base de datos externa. Los datos se pierden al reiniciar.

```bash
mvn spring-boot:run
```

---

# Estructura del Proyecto

```
task-service/
├── src/
│   ├── main/
│   │   ├── java/com/aibert/dosw/
│   │   │   ├── application/        # Use cases, DTOs, mappers
│   │   │   ├── config/             # Spring Security config
│   │   │   ├── domain/             # Ports, models, exceptions
│   │   │   ├── entrypoints/        # REST controllers, exception handlers
│   │   │   └── infrastructure/     # JPA adapters, entities, repositories
│   │   └── resources/
│   │       ├── application.yml         # Local dev (H2 in-memory)
│   │       └── application-docker.yml  # Docker (PostgreSQL)
│   └── test/
│       └── java/com/aibert/dosw/   # Unit tests (Mockito)
├── .env.example                    # Template for local Docker credentials
├── docker-compose.yml              # PostgreSQL + app services
├── Dockerfile                      # Multi-stage build
└── pom.xml
```

---

# Referencias

- Documentación oficial Spring
- PostgreSQL docs
- OpenAPI
- Papers / fuentes usadas

# Evidencia Swagger

![Evidencia Swagger](docs/img/captura_swagger.png)

