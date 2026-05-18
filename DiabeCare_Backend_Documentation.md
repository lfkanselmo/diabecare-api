# DiabeCare — Backend Architecture & Technical Documentation

> **Spring Boot | Arquitectura Hexagonal | REST API**

| Campo | Valor |
|---|---|
| Versión | 1.0.0 |
| Tecnología | Java 17 + Spring Boot 3.x |
| Arquitectura | Hexagonal + Clean Architecture |
| Base de datos | PostgreSQL 15 |
| Estado | Documento Base — Proyecto de Práctica |

---

## Tabla de Contenidos

1. [Visión General](#1-visión-general)
2. [Arquitectura del Sistema](#2-arquitectura-del-sistema)
3. [Modelo de Dominio](#3-modelo-de-dominio)
4. [Cálculos Médicos](#4-cálculos-médicos)
5. [API REST — Endpoints](#5-api-rest--endpoints)
6. [Seguridad](#6-seguridad)
7. [Estándares Técnicos y de Código](#7-estándares-técnicos-y-de-código)
8. [Base de Datos](#8-base-de-datos)
9. [Estrategia de Testing](#9-estrategia-de-testing)
10. [Dependencias Principales](#10-dependencias-principales)
11. [Configuración y Despliegue](#11-configuración-y-despliegue)

---

## 1. Visión General

### 1.1 Propósito

DiabeCare es una aplicación web diseñada para ayudar a pacientes diabéticos a llevar un control riguroso de su salud. El sistema permite registrar mediciones clínicas, controlar la ingesta calórica, gestionar medicamentos y generar reportes para consultas médicas.

### 1.2 Objetivos del Sistema

- Registro y monitoreo de glucosa en sangre (ayuno, preprandial, postprandial, nocturna)
- Conteo de calorías y macronutrientes con base de datos nutricional
- Control de medicamentos e insulina con recordatorios
- Registro de signos vitales: presión arterial, peso, IMC, HbA1c estimada
- Generación de reportes y métricas para control médico
- Alertas inteligentes por valores fuera de rango

### 1.3 Alcance Técnico

| Componente | Tecnología |
|---|---|
| Backend | Java 17 + Spring Boot 3.x |
| Frontend | Angular 17+ (documento separado) |
| Base de datos | PostgreSQL 15 (producción) + H2 (tests) |
| Seguridad | Spring Security + JWT + OAuth2 |
| Despliegue | Docker + Docker Compose |
| Documentación API | OpenAPI 3.0 / Swagger UI |

---

## 2. Arquitectura del Sistema

### 2.1 Patrón Arquitectural: Hexagonal + Clean Architecture

El backend de DiabeCare implementa **Arquitectura Hexagonal (Ports & Adapters)** combinada con los principios de **Clean Architecture** de Robert C. Martin. Esta elección garantiza:

- **Independencia del framework**: el dominio no depende de Spring Boot
- **Testabilidad**: cada capa puede probarse de forma aislada
- **Flexibilidad**: cambio de infraestructura sin afectar la lógica de negocio
- **Mantenibilidad**: separación clara de responsabilidades

### 2.2 Capas de la Arquitectura

| Capa | Componente | Responsabilidad |
|---|---|---|
| **Domain** | Entities, Value Objects | Reglas de negocio puras, sin dependencias externas |
| **Application** | Use Cases, Ports (interfaces) | Orquestación de flujos, contratos de entrada/salida |
| **Infrastructure** | Repositories, Adapters, Config | Implementaciones técnicas (JPA, REST, messaging) |
| **Presentation** | Controllers, DTOs, Mappers | Exposición de API REST, validación de entrada |

### 2.3 Estructura de Paquetes

```
com.diabecare
├── domain/
│   ├── model/              # Entidades y Value Objects
│   ├── exception/          # Excepciones de dominio
│   └── service/            # Domain Services
├── application/
│   ├── port/
│   │   ├── in/             # Puertos de entrada (Use Case interfaces)
│   │   └── out/            # Puertos de salida (Repository interfaces)
│   ├── usecase/            # Implementaciones de casos de uso
│   └── dto/                # Objetos de transferencia (internos)
├── infrastructure/
│   ├── persistence/
│   │   ├── entity/         # JPA Entities (@Entity)
│   │   ├── repository/     # Spring Data JPA interfaces
│   │   ├── adapter/        # Implementaciones de puertos de salida
│   │   └── mapper/         # Domain <-> JPA Entity mappers
│   ├── security/           # Spring Security, JWT, filtros
│   ├── config/             # Beans de configuración Spring
│   └── external/           # Clientes externos (APIs nutricionales)
└── presentation/
    ├── controller/         # REST Controllers
    ├── dto/                # Request/Response DTOs
    ├── mapper/             # Domain <-> DTO mappers
    └── advice/             # GlobalExceptionHandler
```

---

## 3. Modelo de Dominio

### 3.1.1 Patient _(Aggregate Root)_

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `patientId` | UUID | Sí | Identificador único |
| `userId` | UUID | Sí | Referencia al usuario |
| `dateOfBirth` | LocalDate | Sí | Fecha de nacimiento |
| `diabetesType` | Enum | Sí | `TIPO_1`, `TIPO_2`, `GESTACIONAL`, `LADA`, `MODY` |
| `diagnosisDate` | LocalDate | Sí | Fecha de diagnóstico |
| `height` | BigDecimal (cm) | Sí | Talla en centímetros |
| `targetGlucoseMin` | BigDecimal | Sí | Rango objetivo mínimo mg/dL |
| `targetGlucoseMax` | BigDecimal | Sí | Rango objetivo máximo mg/dL |
| `dailyCalorieGoal` | Integer | No | Meta calórica diaria |

### 3.1.2 GlucoseReading

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `readingId` | UUID | Sí | ID único |
| `patientId` | UUID | Sí | Referencia al paciente |
| `value` | BigDecimal | Sí | Valor en mg/dL o mmol/L |
| `unit` | Enum | Sí | `MG_DL`, `MMOL_L` |
| `readingType` | Enum | Sí | `FASTING`, `PRE_MEAL`, `POST_MEAL`, `BEDTIME`, `RANDOM` |
| `measuredAt` | LocalDateTime | Sí | Fecha y hora de medición |
| `notes` | String | No | Notas opcionales |
| `deviceSource` | String | No | Fuente: glucómetro, CGM, manual |

### 3.1.3 MealEntry _(Registro de Comidas)_

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `mealId` | UUID | Sí | ID único |
| `patientId` | UUID | Sí | Referencia al paciente |
| `mealType` | Enum | Sí | `BREAKFAST`, `LUNCH`, `DINNER`, `SNACK` |
| `consumedAt` | LocalDateTime | Sí | Fecha y hora del consumo |
| `totalCalories` | BigDecimal | Calculado | Calculado de items |
| `totalCarbs` | BigDecimal | Calculado | Carbohidratos totales (g) |
| `totalProteins` | BigDecimal | No | Proteínas totales (g) |
| `totalFats` | BigDecimal | No | Grasas totales (g) |
| `items` | `List<MealItem>` | Sí | Alimentos individuales de la comida |

### 3.1.4 VitalSign _(Signos Vitales)_

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `vitalId` | UUID | Sí | ID único |
| `patientId` | UUID | Sí | Referencia paciente |
| `weight` | BigDecimal (kg) | No | Peso en kilogramos |
| `bmi` | BigDecimal | Calculado | IMC calculado automáticamente |
| `systolicBP` | Integer (mmHg) | No | Presión sistólica |
| `diastolicBP` | Integer (mmHg) | No | Presión diastólica |
| `heartRate` | Integer (bpm) | No | Frecuencia cardíaca |
| `hba1c` | BigDecimal (%) | No | Hemoglobina glicosilada |
| `measuredAt` | LocalDateTime | Sí | Fecha y hora |

### 3.1.5 Medication _(Medicamentos e Insulina)_

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `medicationId` | UUID | Sí | ID único |
| `patientId` | UUID | Sí | Referencia paciente |
| `name` | String | Sí | Nombre del medicamento |
| `type` | Enum | Sí | `INSULIN_BASAL`, `INSULIN_BOLUS`, `ORAL`, `INJECTABLE` |
| `dose` | BigDecimal | Sí | Dosis prescrita |
| `doseUnit` | Enum | Sí | `MG`, `ML`, `UNITS` (insulina) |
| `frequency` | Enum | Sí | `ONCE_DAILY`, `TWICE_DAILY`, `WITH_MEALS`, etc. |
| `active` | Boolean | Sí | Medicamento activo |

---

## 4. Cálculos Médicos

El módulo de cálculos médicos (`MedicalCalculatorService`) encapsula todas las fórmulas clínicas utilizadas en el sistema.

### 4.1 Índice de Masa Corporal (IMC)

```
IMC = peso(kg) / altura(m)²

Clasificación:
  < 18.5        → Bajo peso
  18.5 - 24.9   → Normal
  25.0 - 29.9   → Sobrepeso
  >= 30.0       → Obesidad
```

### 4.2 HbA1c Estimada (eHbA1c)

Estimación basada en el promedio de glucosa de los últimos 90 días — **Fórmula ADAG**:

```
eHbA1c (%) = (Glucosa promedio mg/dL + 46.7) / 28.7

Interpretación:
  < 5.7%        → Normal
  5.7 - 6.4%    → Prediabetes
  6.5 - 7.0%    → Control óptimo (diabético)
  7.1 - 8.0%    → Control aceptable
  > 8.0%        → Control deficiente
```

### 4.3 Tiempo en Rango (TIR)

```
TIR = (lecturas dentro del rango objetivo / total de lecturas) * 100

Rangos estándar recomendados (ADA):
  Hiperglucemia severa:  > 250 mg/dL   → objetivo < 5%
  Hiperglucemia:         > 180 mg/dL   → objetivo < 25%
  En rango objetivo:   70 - 180 mg/dL  → objetivo > 70%
  Hipoglucemia:          < 70 mg/dL    → objetivo < 4%
  Hipoglucemia severa:   < 54 mg/dL    → objetivo < 1%
```

### 4.4 Requerimiento Calórico Diario (TMB)

Ecuación de **Mifflin-St Jeor**:

```
Hombres: TMB = (10 × peso_kg) + (6.25 × altura_cm) - (5 × edad) + 5
Mujeres: TMB = (10 × peso_kg) + (6.25 × altura_cm) - (5 × edad) - 161

Factor de actividad (TDEE = TMB × factor):
  Sedentario:            1.2
  Ligeramente activo:    1.375
  Moderadamente activo:  1.55
  Muy activo:            1.725
```

### 4.5 Coeficiente de Variación de Glucosa (CV)

```
CV (%) = (Desviación estándar / Media de glucosa) * 100

Interpretación:
  CV < 36%   → Variabilidad glucémica aceptable
  CV >= 36%  → Variabilidad glucémica elevada (mayor riesgo)
```

---

## 5. API REST — Endpoints

> URL base: `/api/v1`

### 5.1 Autenticación — `/api/v1/auth`

| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| `POST` | `/auth/register` | No | Registro de usuario y perfil paciente |
| `POST` | `/auth/login` | No | Login, retorna JWT token |
| `POST` | `/auth/refresh` | JWT | Renovar access token |
| `POST` | `/auth/logout` | JWT | Invalidar token |
| `POST` | `/auth/forgot-password` | No | Solicitar reset de contraseña |

### 5.2 Glucosa — `/api/v1/glucose`

| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| `GET` | `/glucose` | JWT | Listar lecturas (paginado, filtros) |
| `POST` | `/glucose` | JWT | Registrar nueva lectura |
| `GET` | `/glucose/{id}` | JWT | Obtener lectura por ID |
| `PUT` | `/glucose/{id}` | JWT | Actualizar lectura |
| `DELETE` | `/glucose/{id}` | JWT | Eliminar lectura |
| `GET` | `/glucose/stats/daily` | JWT | Estadísticas diarias |
| `GET` | `/glucose/stats/monthly` | JWT | Estadísticas mensuales + TIR |
| `GET` | `/glucose/stats/hba1c` | JWT | HbA1c estimada |

### 5.3 Nutrición — `/api/v1/meals`

| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| `GET` | `/meals` | JWT | Listar comidas del día |
| `POST` | `/meals` | JWT | Registrar comida con alimentos |
| `GET` | `/meals/{id}` | JWT | Detalle de comida |
| `PUT` | `/meals/{id}` | JWT | Actualizar comida |
| `DELETE` | `/meals/{id}` | JWT | Eliminar registro |
| `GET` | `/meals/summary/daily` | JWT | Resumen calórico del día |
| `GET` | `/foods/search` | JWT | Buscar alimentos (USDA + custom) |

### 5.4 Signos Vitales — `/api/v1/vitals`

| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| `GET` | `/vitals` | JWT | Historial de signos vitales |
| `POST` | `/vitals` | JWT | Registrar signos vitales |
| `GET` | `/vitals/latest` | JWT | Última medición |
| `GET` | `/vitals/bmi/trend` | JWT | Tendencia de IMC |

### 5.5 Medicamentos — `/api/v1/medications`

| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| `GET` | `/medications` | JWT | Listar medicamentos activos |
| `POST` | `/medications` | JWT | Registrar medicamento |
| `PUT` | `/medications/{id}` | JWT | Actualizar medicamento |
| `DELETE` | `/medications/{id}` | JWT | Desactivar medicamento |
| `POST` | `/medications/{id}/log` | JWT | Registrar toma del medicamento |

### 5.6 Reportes — `/api/v1/reports`

| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| `GET` | `/reports/medical-summary` | JWT | Resumen para consulta médica |
| `GET` | `/reports/glucose-pdf` | JWT | Reporte PDF de glucosa |
| `GET` | `/reports/nutrition-pdf` | JWT | Reporte PDF nutricional |
| `GET` | `/reports/dashboard` | JWT | Datos del dashboard principal |

---

## 6. Seguridad

### 6.1 Autenticación con JWT

- **Access Token**: duración 15 minutos, firmado con RS256 (clave asimétrica)
- **Refresh Token**: duración 7 días, almacenado en tabla `refresh_tokens` con rotación
- **Blacklist de tokens**: mediante Redis o tabla DB con TTL
- **Cabecera**: `Authorization: Bearer <token>`

### 6.2 Roles y Permisos

| Rol | Alcance |
|---|---|
| `ROLE_PATIENT` | Acceso exclusivo a sus propios datos |
| `ROLE_ADMIN` | Gestión del sistema (futuro) |

### 6.3 Reglas de Acceso a Datos

- Todos los recursos están asociados a `patientId`
- Validación a nivel de servicio: el `patientId` del token debe coincidir con el del recurso
- Uso de `@PreAuthorize` con SpEL para validación declarativa
- CORS configurado explícitamente: solo orígenes del frontend en whitelist

### 6.4 Protección de Datos Sensibles

- **Contraseñas**: BCrypt con strength factor 12
- **Datos médicos**: cifrado AES-256 en columnas sensibles (futuro)
- **HTTPS** obligatorio en producción (TLS 1.2+)
- **Rate limiting** en endpoints de autenticación: 5 intentos por minuto por IP

---

## 7. Estándares Técnicos y de Código

### 7.1 Convenciones de Nomenclatura

| Elemento | Convención | Ejemplo |
|---|---|---|
| Clases / Interfaces | PascalCase | `GlucoseReadingService`, `PatientRepository` |
| Métodos / Variables | camelCase | `findByPatientId()`, `glucoseValue` |
| Constantes | UPPER_SNAKE_CASE | `MAX_GLUCOSE_VALUE`, `TOKEN_EXPIRY_MS` |
| Paquetes | lowercase | `com.diabecare.domain.model` |
| Tablas BD | snake_case | `glucose_readings`, `meal_entries` |
| Columnas BD | snake_case | `patient_id`, `measured_at`, `hba1c_value` |
| DTOs Request | PascalCase + `Request` | `CreateGlucoseRequest`, `UpdateMealRequest` |
| DTOs Response | PascalCase + `Response` | `GlucoseReadingResponse`, `PatientResponse` |
| Endpoints REST | kebab-case | `/glucose-readings`, `/meal-entries` |
| Excepciones dominio | PascalCase + `Exception` | `GlucoseNotFoundException`, `InvalidRangeException` |

### 7.2 Principios SOLID Aplicados

- **S — Single Responsibility**: Cada clase tiene una única razón de cambio. Los Use Cases son granulares (`RegisterGlucoseUseCase`, `GetGlucoseStatsUseCase`)
- **O — Open/Closed**: Las entidades de dominio son inmutables. Se extiende comportamiento con nuevos use cases sin modificar existentes
- **L — Liskov Substitution**: Los repositorios son interfaces; cualquier implementación debe cumplir el contrato del puerto
- **I — Interface Segregation**: Puertos de entrada granulares por operación, no un repositorio genérico para todo
- **D — Dependency Inversion**: Los Use Cases dependen de interfaces (puertos), nunca de implementaciones concretas de Spring Data

### 7.3 Patrones de Diseño Utilizados

| Patrón | Tipo | Aplicación en DiabeCare |
|---|---|---|
| Repository | Arquitectural | Abstracción del acceso a datos via puertos de salida |
| Use Case / Interactor | Arquitectural | Un use case por operación de negocio |
| Factory Method | Creacional | `GlucoseReading.create()` para construcción con validación |
| Builder | Creacional | Para construcción de reportes y DTOs complejos |
| Strategy | Comportamiento | Algoritmos de cálculo de métricas intercambiables |
| Observer / Events | Comportamiento | Spring Events para notificaciones de alertas |
| Mapper (Adapter) | Estructural | MapStruct para conversión entre capas |
| Specification | Comportamiento | Criterios de búsqueda composable (JPA Specifications) |

### 7.4 Manejo de Errores

Estructura estándar de error response (`@RestControllerAdvice`):

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 404,
  "error": "NOT_FOUND",
  "code": "GLUCOSE_READING_NOT_FOUND",
  "message": "No se encontró la lectura de glucosa con id: abc-123",
  "path": "/api/v1/glucose/abc-123"
}
```

### 7.5 Validaciones

- Jakarta Bean Validation (`@Valid`) en todos los DTOs de request
- Validaciones de dominio en los constructores/factory methods de entidades
- Rangos médicos validados: glucosa 20–600 mg/dL, presión 50–250 mmHg, peso 20–500 kg
- Mensajes de validación en español para el usuario final

---

## 8. Base de Datos

### 8.1 Estrategia de Migración

- **Flyway** para versionado de esquema: `V1__create_users.sql`, `V2__create_patients.sql`, etc.
- Prohibido usar Hibernate DDL auto en producción (`spring.jpa.hibernate.ddl-auto=validate`)
- Todas las tablas incluyen: `created_at`, `updated_at`, `created_by`, `version` (optimistic locking)

### 8.2 Tablas Principales

| Tabla | Descripción |
|---|---|
| `users` | Credenciales y datos de acceso |
| `patients` | Perfil médico del paciente |
| `glucose_readings` | Lecturas de glucosa en sangre |
| `meal_entries` | Registros de comidas |
| `meal_items` | Alimentos individuales de cada comida |
| `foods` | Base de datos de alimentos con información nutricional |
| `vital_signs` | Signos vitales: peso, presión, FC |
| `medications` | Medicamentos activos del paciente |
| `medication_logs` | Historial de tomas de medicamentos |
| `alerts` | Alertas generadas por valores fuera de rango |
| `refresh_tokens` | Gestión de JWT refresh tokens |

### 8.3 Índices de Rendimiento

- `glucose_readings`: índice compuesto `(patient_id, measured_at DESC)` — consulta más frecuente
- `meal_entries`: índice compuesto `(patient_id, consumed_at DESC)`
- `vital_signs`: índice compuesto `(patient_id, measured_at DESC)`
- `foods`: índice de texto completo en columna `name` para búsqueda por nombre

---

## 9. Estrategia de Testing

### 9.1 Pirámide de Tests

| Nivel | Herramienta | Cobertura objetivo | Qué prueba |
|---|---|---|---|
| Unit Tests | JUnit 5 + Mockito | > 80% | Use cases, Domain services, Calculators |
| Integration Tests | @SpringBootTest + Testcontainers | Flujos críticos | Repositorios con PostgreSQL real |
| API Tests | MockMvc / RestAssured | Todos los endpoints | Controllers, validaciones, auth |
| Architecture Tests | ArchUnit | Reglas de arquitectura | Dependencias entre capas |

### 9.2 Reglas ArchUnit

- El dominio **no puede** importar clases de Spring, JPA o cualquier framework
- Los Use Cases solo pueden depender del dominio y de interfaces (puertos)
- Los Controllers no pueden acceder directamente a repositorios
- Las entidades JPA deben estar en el paquete `infrastructure.persistence.entity`

---

## 10. Dependencias Principales

```xml
<!-- pom.xml — dependencias clave -->
```

| Dependencia | Versión | Propósito |
|---|---|---|
| `spring-boot-starter-web` | 3.x | REST API + Tomcat embebido |
| `spring-boot-starter-security` | 3.x | Autenticación y autorización |
| `spring-boot-starter-data-jpa` | 3.x | Persistencia con Hibernate |
| `spring-boot-starter-validation` | 3.x | Bean Validation |
| `jjwt-api` + `jjwt-impl` | 0.12.x | Generación y validación JWT |
| `postgresql` | 42.x | Driver JDBC PostgreSQL |
| `flyway-core` | 9.x | Migraciones de base de datos |
| `mapstruct` | 1.5.x | Mapeo entre capas |
| `lombok` | 1.18.x | Reducción de boilerplate |
| `springdoc-openapi-starter` | 2.x | Documentación Swagger UI |
| `testcontainers-postgresql` | 1.19.x | Tests de integración con BD real |
| `archunit-junit5` | 1.x | Tests de arquitectura |

---

## 11. Configuración y Despliegue

### 11.1 Perfiles de Spring

| Perfil | Configuración |
|---|---|
| `dev` | H2 en memoria, logs DEBUG, Swagger habilitado, sin HTTPS |
| `test` | Testcontainers, datos de prueba con `@Sql` |
| `prod` | PostgreSQL, logs INFO/WARN, Swagger deshabilitado, HTTPS obligatorio |

### 11.2 Variables de Entorno Requeridas (Producción)

```env
DB_URL=jdbc:postgresql://localhost:5432/diabecare
DB_USERNAME=diabecare_user
DB_PASSWORD=<secret>
JWT_SECRET_KEY=<base64-encoded-rsa-private-key>
JWT_ACCESS_EXPIRY_MS=900000
JWT_REFRESH_EXPIRY_MS=604800000
CORS_ALLOWED_ORIGINS=https://app.diabecare.com
```

### 11.3 Docker Compose (Desarrollo)

```yaml
services:
  backend:
    build: .
    ports: ['8080:8080']
    environment:
      - SPRING_PROFILES_ACTIVE=dev
    depends_on: [postgres]

  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: diabecare
      POSTGRES_USER: diabecare_user
      POSTGRES_PASSWORD: dev_password
    ports: ['5432:5432']
    volumes: ['postgres_data:/var/lib/postgresql/data']
```

---

*DiabeCare Backend Documentation v1.0*
