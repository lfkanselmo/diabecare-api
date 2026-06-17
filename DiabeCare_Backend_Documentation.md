# DiabeCare — Backend Architecture & Technical Documentation

> **Spring Boot 3.5 | Arquitectura Hexagonal | REST API**

| Campo | Valor |
|---|---|
| Versión | 2.0.0 |
| Tecnología | Java 17 + Spring Boot 3.5.14 |
| Arquitectura | Hexagonal + Clean Architecture |
| Base de datos | PostgreSQL 15+ |
| Documentación API | OpenAPI 3.0 / Swagger UI |

---

## Tabla de Contenidos

1. [Visión General](#1-visión-general)
2. [Arquitectura del Sistema](#2-arquitectura-del-sistema)
3. [Modelo de Dominio](#3-modelo-de-dominio)
4. [Cálculos Médicos](#4-cálculos-médicos)
5. [Servicios de Dominio](#5-servicios-de-dominio)
6. [API REST — Endpoints](#6-api-rest--endpoints)
7. [Seguridad](#7-seguridad)
8. [Push Notifications](#8-push-notifications)
9. [Tareas Programadas](#9-tareas-programadas)
10. [Rate Limiting](#10-rate-limiting)
11. [Auditoría](#11-auditoría)
12. [Estándares Técnicos y de Código](#12-estándares-técnicos-y-de-código)
13. [Base de Datos](#13-base-de-datos)
14. [Estrategia de Testing](#14-estrategia-de-testing)
15. [Dependencias Principales](#15-dependencias-principales)
16. [Configuración y Despliegue](#16-configuración-y-despliegue)

---

## 1. Visión General

### 1.1 Propósito

DiabeCare es una aplicación web para pacientes diabéticos que permite registrar mediciones clínicas, controlar ingesta calórica, gestionar medicamentos, recibir alertas inteligentes, y generar reportes para consultas médicas.

### 1.2 Objetivos del Sistema

- Registro y monitoreo de glucosa con detección de patrones clínicos
- Conteo de calorías y macronutrientes con 172 alimentos colombianos
- Control de medicamentos con auditoría de cambios
- Signos vitales: presión arterial, peso, IMC, HbA1c estimada
- Alertas clínicas inteligentes (7 tipos + 4 patrones + ciclo menstrual)
- Notificaciones push nativas (Web Push API)
- Resumen semanal automático por paciente
- Exportación de datos en CSV y JSON
- Rate limiting por paciente para proteger integridad de datos

### 1.3 Stack Técnico

| Componente | Tecnología |
|---|---|
| Backend | Java 17 + Spring Boot 3.5.14 |
| Frontend | Angular 21 (documento separado) |
| Base de datos | PostgreSQL 15+ |
| Seguridad | Spring Security 6 + JWT (jjwt 0.12.5) |
| Cache | Caffeine |
| Push | Web Push API + BouncyCastle VAPID |
| Rate Limiting | Bucket4j 8.10.1 + Caffeine |
| Documentación | OpenAPI 3.0 / Swagger UI |

---

## 2. Arquitectura del Sistema

### 2.1 Patrón: Hexagonal + Clean Architecture

| Principio | Aplicación |
|---|---|
| Independencia del framework | El dominio no importa clases de Spring ni JPA |
| Testabilidad | Cada capa se prueba de forma aislada con Mockito |
| Flexibilidad | Cambio de infraestructura sin afectar lógica de negocio |
| SRP | Cada use case tiene una única responsabilidad |
| Métodos atómicos | Detectores de patrón, servicios de exportación y auditoría son métodos pequeños y focalizados |

### 2.2 Capas

| Capa | Responsabilidad |
|---|---|
| **Domain** | Entidades, Value Objects, Domain Services — sin dependencias externas |
| **Application** | Orquestación de flujos, contratos de entrada/salida (ports) |
| **Infrastructure** | JPA, seguridad, push, scheduler, rate limiting, PDF |
| **Presentation** | Controllers REST, DTOs, validación de entrada, manejo de errores |

### 2.3 Estructura de Paquetes

```
com.diabecare
├── domain/
│   ├── model/              # Patient, GlucoseReading, MealEntry, VitalSign,
│   │                       # Medication, ExerciseLog, MenstrualCycle,
│   │                       # Alert, AuditLog, WeeklySummaryData
│   ├── exception/          # Excepciones de dominio + RateLimitExceededException
│   └── service/            # MedicalCalculatorService, PatternDetectorService,
│                           # WeeklySummaryService, GlucoseExportService,
│                           # AuditService, RateLimitService
├── application/
│   ├── port/in/            # Interfaces de casos de uso
│   ├── port/out/           # Puertos de salida (repositorios, notificaciones)
│   └── usecase/            # Implementaciones (1 clase por operación)
├── infrastructure/
│   ├── persistence/        # JPA entities, repositories, adapters, mappers
│   ├── security/           # JWT filter, UserDetailsServiceImpl
│   ├── config/             # DiabeCareProperties, JwtProperties, RateLimitConfig
│   ├── push/               # PushNotificationService, PushNotificationAdapter
│   ├── pdf/                # PdfReportAdapter
│   └── scheduler/          # WeeklySummaryScheduler
└── presentation/
    ├── controller/         # REST Controllers
    ├── dto/                # Request/Response records
    ├── mapper/             # MapStruct mappers
    └── advice/             # GlobalExceptionHandler
```

### 2.4 Reglas de Arquitectura (ArchUnit)

Verificadas automáticamente en cada build:

- El dominio **no puede** importar clases de Spring, JPA o cualquier framework
- Los Use Cases solo pueden depender del dominio y de interfaces (puertos)
- Los Controllers no pueden acceder directamente a repositorios
- Las entidades JPA deben estar en `infrastructure.persistence.entity`

---

## 3. Modelo de Dominio

### 3.1 Patient _(Aggregate Root)_

| Campo | Tipo | Descripción |
|---|---|---|
| `patientId` | UUID | Identificador único |
| `userId` | UUID | Referencia al usuario |
| `fullName` | String | Nombre completo |
| `dateOfBirth` | LocalDate | Fecha de nacimiento |
| `biologicalSex` | Enum | `MALE`, `FEMALE`, `NOT_SPECIFIED` |
| `diabetesType` | Enum | `TYPE_1`, `TYPE_2`, `GESTATIONAL`, `LADA`, `MODY` |
| `targetGlucoseMin` | BigDecimal | Rango objetivo mínimo mg/dL |
| `targetGlucoseMax` | BigDecimal | Rango objetivo máximo mg/dL |
| `dailyCalorieGoal` | Integer | Meta calórica diaria |
| `insulinSensitivityFactor` | BigDecimal | Factor de sensibilidad a insulina |
| `insulinToCarbRatio` | BigDecimal | Ratio insulina:carbohidratos |
| `targetGlucoseCorrection` | BigDecimal | Objetivo de corrección |

### 3.2 GlucoseReading

| Campo | Tipo | Descripción |
|---|---|---|
| `readingId` | UUID | ID único |
| `value` | BigDecimal | Valor en mg/dL o mmol/L |
| `unit` | Enum | `MG_DL`, `MMOL_L` |
| `readingType` | Enum | `FASTING`, `PRE_MEAL`, `POST_MEAL`, `BEDTIME`, `RANDOM` |
| `status` | Enum | `CRITICALLY_LOW`, `LOW`, `NORMAL`, `HIGH`, `CRITICALLY_HIGH` |
| `measuredAt` | LocalDateTime | Fecha y hora de medición |

### 3.3 Alert

| Campo | Tipo | Descripción |
|---|---|---|
| `type` | Enum | `GLUCOSE_OUT_OF_RANGE`, `NO_GLUCOSE_RECORDED`, `HIGH_HBA1C_ESTIMATED`, `NO_MEAL_RECORDED`, `POSITIVE_STREAK`, `GLUCOSE_AVERAGE_HIGH`, `GLUCOSE_PATTERN_DETECTED` |
| `severity` | Enum | `SUCCESS`, `INFO`, `WARNING`, `DANGER` |
| `title` | String | Título de la alerta |
| `message` | String | Mensaje descriptivo |

### 3.4 AuditLog

| Campo | Tipo | Descripción |
|---|---|---|
| `entityType` | String | `PATIENT`, `MEDICATION` |
| `action` | Enum | `CREATE`, `UPDATE`, `DELETE` |
| `fieldName` | String | Campo modificado |
| `oldValue` | String | Valor anterior |
| `newValue` | String | Valor nuevo |
| `performedAt` | LocalDateTime | Fecha y hora del cambio |

### 3.5 WeeklySummaryData _(record)_

```java
public record WeeklySummaryData(
    UUID patientId, String patientName,
    BigDecimal averageGlucose, BigDecimal estimatedHba1c,
    BigDecimal timeInRangePercent,
    long hypoEpisodes, long hyperEpisodes, int totalReadings
) {}
```

---

## 4. Cálculos Médicos

`MedicalCalculatorService` encapsula todas las fórmulas clínicas.

### 4.1 IMC

```
IMC = peso(kg) / altura(m)²
< 18.5 → Bajo peso | 18.5–24.9 → Normal | 25–29.9 → Sobrepeso | ≥30 → Obesidad
```

### 4.2 HbA1c Estimada (Fórmula ADAG)

```
eHbA1c (%) = (Glucosa promedio mg/dL + 46.7) / 28.7
< 5.7% → Normal | 5.7–6.4% → Prediabetes | 6.5–7.0% → Control óptimo
```

### 4.3 Tiempo en Rango (TIR)

```
TIR (%) = (lecturas en rango / total lecturas) × 100
Rango objetivo: configurable por paciente (default 70–180 mg/dL)
```

### 4.4 Coeficiente de Variación (CV)

```
CV (%) = (Desviación estándar / Promedio) × 100
Objetivo: CV < 36% (alta variabilidad = mayor riesgo)
```

### 4.5 Dosis de Insulina

```
Dosis corrección = (Glucosa actual - Objetivo) / Factor de sensibilidad
Dosis comida     = Carbohidratos (g) / Ratio insulina:carbs
Dosis total      = Dosis corrección + Dosis comida
```

---

## 5. Servicios de Dominio

### 5.1 PatternDetectorService

Detecta patrones clínicos en las lecturas de los últimos 14 días:

| Método | Patrón | Umbral |
|---|---|---|
| `detectHighFastingPattern` | Hiperglucemia en ayuno | >60% de ayunos >130 mg/dL, mín 3 lecturas |
| `detectHighPostMealPattern` | Picos postprandiales | >50% postprandiales >180 mg/dL, mín 3 lecturas |
| `detectRecurrentHypoglycemia` | Hipoglucemia recurrente | ≥3 episodios <70 mg/dL |
| `detectHighVariability` | Alta variabilidad | CV ≥36% con ≥7 lecturas |

Cada método retorna `Optional<Alert>` — patrón limpio, testeable unitariamente.

### 5.2 WeeklySummaryService

Construye el resumen semanal por paciente:
- `buildSummary(patient, readings)` → `Optional<WeeklySummaryData>`
- `buildPushTitle()` → String
- `buildPushMessage(data)` → String formateado

### 5.3 GlucoseExportService

Serializa lecturas de glucosa:
- `toCsv(readings)` → String con BOM UTF-8 (compatible con Excel)
- `toJson(readings)` → String JSON

### 5.4 AuditService

Construye entradas de auditoría:
- `buildCreateLog(patientId, entityType, entityId)` → `AuditLog`
- `buildUpdateLog(patientId, entityType, entityId, fieldName, oldValue, newValue)` → `AuditLog`
- `buildDeleteLog(patientId, entityType, entityId)` → `AuditLog`

### 5.5 RateLimitService

Verifica límites por paciente y operación usando Bucket4j:
- `checkLimit(patientId, operation, bucketSupplier)` — lanza `RateLimitExceededException` si se excede

---

## 6. API REST — Endpoints

### Autenticación
```
POST   /api/v1/auth/register
POST   /api/v1/auth/login
POST   /api/v1/auth/refresh
```

### Glucosa
```
POST   /api/v1/glucose/{patientId}
GET    /api/v1/glucose/{patientId}/history?from=&to=
GET    /api/v1/glucose/{patientId}/stats?from=&to=
DELETE /api/v1/glucose/{patientId}/{readingId}
GET    /api/v1/glucose/{patientId}/export/csv?from=&to=
GET    /api/v1/glucose/{patientId}/export/json?from=&to=
```

### Nutrición
```
POST   /api/v1/nutrition/{patientId}/meals
GET    /api/v1/nutrition/{patientId}/summary?date=
GET    /api/v1/nutrition/{patientId}/history?from=&to=
GET    /api/v1/foods/search?query=
```

### Signos vitales
```
POST   /api/v1/vitals/{patientId}
GET    /api/v1/vitals/{patientId}/latest
GET    /api/v1/vitals/{patientId}/history?from=&to=
GET    /api/v1/vitals/{patientId}/hba1c-trend?months=
```

### Ejercicio
```
POST   /api/v1/exercise/{patientId}
GET    /api/v1/exercise/{patientId}/history?from=&to=
```

### Medicamentos
```
POST   /api/v1/medications/{patientId}
GET    /api/v1/medications/{patientId}
DELETE /api/v1/medications/{patientId}/{medicationId}
```

### Calculadora de insulina
```
POST   /api/v1/insulin/{patientId}/calculate
```

### Alertas
```
GET    /api/v1/alerts/{patientId}
```

### Ciclo menstrual
```
POST   /api/v1/menstrual-cycle/{patientId}
GET    /api/v1/menstrual-cycle/{patientId}/status
```

### Paciente
```
GET    /api/v1/patients/{patientId}
PUT    /api/v1/patients/{patientId}
```

### Reportes
```
GET    /api/v1/reports/{patientId}?from=&to=
```

### Push notifications
```
GET    /api/v1/push/vapid-public-key
POST   /api/v1/push/subscribe
DELETE /api/v1/push/unsubscribe
```

### Auditoría
```
GET    /api/v1/audit/{patientId}
GET    /api/v1/audit/{patientId}/{entityType}
```

### Metadatos
```
GET    /api/v1/metadata/reading-types
GET    /api/v1/metadata/glucose-units
GET    /api/v1/metadata/meal-types
GET    /api/v1/metadata/exercise-types
GET    /api/v1/metadata/exercise-intensities
GET    /api/v1/metadata/medication-types
GET    /api/v1/metadata/medication-frequencies
GET    /api/v1/metadata/dose-units
GET    /api/v1/metadata/activity-levels
GET    /api/v1/metadata/diabetes-types
```

---

## 7. Seguridad

### 7.1 JWT

- Access token: 15 minutos (`JWT_ACCESS_EXPIRY_MS=900000`)
- Refresh token: 7 días (`JWT_REFRESH_EXPIRY_MS=604800000`)
- Algoritmo: HMAC-SHA256
- Cabecera: `Authorization: Bearer <token>`

### 7.2 Endpoints públicos

```java
private static final String[] PUBLIC_ENDPOINTS = {
    "/api/v1/auth/**",
    "/swagger-ui/**", "/v3/api-docs/**",
    "/actuator/health"
};
```

Todos los demás requieren JWT válido.

### 7.3 Protección de datos

- Contraseñas: BCrypt con strength factor configurable (`BCRYPT_STRENGTH=12`)
- CORS: orígenes configurables via `CORS_ALLOWED_ORIGINS`
- Sesión: `STATELESS` — sin cookies ni sesiones del servidor

### 7.4 Manejo de errores

```json
{
  "timestamp": "2026-06-13T12:00:00",
  "status": 429,
  "error": "Too Many Requests",
  "code": "RATE_LIMIT_EXCEEDED",
  "message": "Límite de registros excedido para GLUCOSE. Intenta de nuevo más tarde.",
  "path": "/api/v1/glucose/..."
}
```

Códigos de error manejados: `PATIENT_NOT_FOUND`, `GLUCOSE_READING_NOT_FOUND`, `DOMAIN_VALIDATION_ERROR`, `VALIDATION_ERROR`, `RATE_LIMIT_EXCEEDED`, `INTERNAL_ERROR`.

---

## 8. Push Notifications

### 8.1 Flujo

1. Frontend solicita clave pública VAPID (`GET /api/v1/push/vapid-public-key`)
2. Frontend suscribe el navegador al `PushManager` con la clave pública
3. Frontend envía `{ endpoint, p256dh, auth }` al backend (`POST /api/v1/push/subscribe`)
4. Backend guarda la suscripción en `push_subscriptions` asociada al `patient_id`
5. Backend envía notificaciones con `PushNotificationService` usando la librería `web-push`

### 8.2 Generación de claves VAPID

Algoritmo: EC prime256v1 (BouncyCastle). Las claves se configuran via variables de entorno `VAPID_PUBLIC_KEY`, `VAPID_PRIVATE_KEY`, `VAPID_SUBJECT`.

### 8.3 Auto-limpieza

Al recibir respuesta HTTP 410 (Gone) al enviar una notificación, la suscripción se elimina automáticamente de la base de datos.

---

## 9. Tareas Programadas

### 9.1 WeeklySummaryScheduler

```java
@Scheduled(cron = "0 0 8 * * MON", zone = "America/Bogota")
public void sendWeeklySummaries()
```

Ejecuta todos los lunes a las 8am hora de Bogotá. Flujo:

```
WeeklySummaryScheduler
  → SendWeeklySummaryUseCaseImpl.sendToAllPatients()
      → LoadPatientPort.findAll()
      → LoadGlucoseReadingPort.findByPatientIdAndDateRange() (últimos 7 días)
      → WeeklySummaryService.buildSummary()
      → NotifyPatientPort.notify() → PushNotificationAdapter → PushNotificationService
```

El scheduler solo dispara — no orquesta. El caso de uso orquesta. El servicio de dominio calcula. El adaptador notifica. SRP respetado en cada capa.

---

## 10. Rate Limiting

Implementado con **Bucket4j** + **Caffeine Cache**. Un bucket por paciente+operación, almacenado en memoria con expiración de 1 hora.

| Operación | Límite | Período |
|---|---|---|
| Registro de glucosa | 20 | 1 hora |
| Registro de comidas | 15 | 1 hora |
| Registro de ejercicio | 10 | 1 hora |

Al exceder el límite: `HTTP 429 Too Many Requests` con código `RATE_LIMIT_EXCEEDED`.

---

## 11. Auditoría

Registra cambios en perfil del paciente y medicamentos en la tabla `audit_log`.

**Cambios auditados en paciente:**
- `targetGlucoseRange` (rango objetivo de glucosa)
- `dailyCalorieGoal` (meta calórica)
- `activityLevel` (nivel de actividad)
- `preferredGlucoseUnit` (unidad preferida)

**Cambios auditados en medicamentos:**
- CREATE al registrar un medicamento
- DELETE al desactivar un medicamento

Solo se registra cuando el valor realmente cambia (comparación `oldValue != newValue`).

---

## 12. Estándares Técnicos y de Código

### 12.1 Convenciones de Nomenclatura

| Elemento | Convención | Ejemplo |
|---|---|---|
| Clases / Interfaces | PascalCase | `GlucoseReadingService` |
| Métodos / Variables | camelCase | `findByPatientId()` |
| Constantes | UPPER_SNAKE_CASE | `MAX_GLUCOSE_VALUE` |
| Paquetes | lowercase | `com.diabecare.domain.model` |
| Tablas BD | snake_case | `glucose_readings` |
| DTOs Request | PascalCase + `Request` | `RegisterGlucoseRequest` |
| DTOs Response | PascalCase + `Response` | `GlucoseReadingResponse` |
| Puertos entrada | PascalCase + `UseCase` | `RegisterGlucoseReadingUseCase` |
| Puertos salida | PascalCase + `Port` | `LoadPatientPort`, `SaveAuditLogPort` |

### 12.2 Principios SOLID

- **S** — Un use case por operación. Un método por responsabilidad en los domain services
- **O** — Nuevas funcionalidades = nuevos use cases, no modificar existentes
- **L** — Los puertos son contratos; cualquier implementación debe cumplirlos
- **I** — Puertos granulares: `LoadPatientPort`, `SavePatientPort` separados
- **D** — Use Cases dependen de interfaces, nunca de implementaciones concretas

### 12.3 Reglas de código

- Records Java para DTOs inmutables (`request/response records`)
- `@Builder` + `@Getter` en entidades de dominio (nunca setters públicos)
- Factory methods con validación: `GlucoseReading.create(...)`, `Patient.create(...)`
- Métodos privados atómicos en use cases complejos (e.g. `auditGlucoseTarget`, `auditActivityLevel`)
- `Optional<T>` para retornos que pueden ser vacíos (detectores de patrón)
- `List.of()` para colecciones inmutables vacías

---

## 13. Base de Datos

### 13.1 Migraciones Flyway

| Versión | Archivo | Descripción |
|---|---|---|
| V1 | `V1__create_initial_schema.sql` | Tablas base |
| V2 | `V2__remove_version_columns.sql` | Limpieza |
| V3 | `V3__update_foods_table.sql` | Actualización foods |
| V4 | `V4__seed_foods.sql` | 172 alimentos colombianos |
| V5 | `V5__add_insulin_profile.sql` | ISF, ratio, objetivo corrección |
| V6 | `V6__create_exercise_table.sql` | exercise_logs |
| V7 | `V7__add_menstrual_cycle.sql` | biological_sex, menstrual_cycles |
| V8 | `V8__push_subscriptions.sql` | push_subscriptions |
| V9 | `V9__audit_log.sql` | audit_log |

### 13.2 Tablas Principales

| Tabla | Descripción |
|---|---|
| `users` | Credenciales y datos de acceso |
| `patients` | Perfil médico del paciente |
| `glucose_readings` | Lecturas de glucosa |
| `meal_entries` + `meal_items` | Comidas y alimentos individuales |
| `foods` | 172 alimentos colombianos con macros |
| `vital_signs` | Peso, presión, FC, HbA1c medida |
| `medications` | Medicamentos activos |
| `exercise_logs` | Registros de actividad física |
| `menstrual_cycles` | Ciclos menstruales con síntomas |
| `push_subscriptions` | Suscripciones Web Push por paciente |
| `audit_log` | Historial de cambios auditables |

### 13.3 Índices de Rendimiento

- `glucose_readings(patient_id, measured_at DESC)` — consulta más frecuente
- `meal_entries(patient_id, consumed_at DESC)`
- `vital_signs(patient_id, measured_at DESC)`
- `push_subscriptions(patient_id)`
- `audit_log(patient_id)`, `audit_log(entity_type, entity_id)`, `audit_log(performed_at DESC)`
- `foods` — índice de texto completo en `name`

---

## 14. Estrategia de Testing

### 14.1 Pirámide de Tests

| Nivel | Herramienta | Qué prueba |
|---|---|---|
| Unit Tests | JUnit 5 + Mockito | Use cases, Domain services, Calculators, PatternDetector |
| Architecture Tests | ArchUnit | Dependencias entre capas |
| Smoke Test | @SpringBootTest | Arranque del contexto completo |

### 14.2 Suites actuales (31 tests — 100% passing)

| Suite | Tests | Descripción |
|---|---|---|
| `MedicalCalculatorServiceTest` | 14 | IMC, HbA1c, TIR, CV, promedio, SD |
| `RegisterGlucoseReadingUseCaseTest` | 5 | Registro exitoso, paciente no encontrado, validaciones |
| `GetAlertsUseCaseTest` | 4 | Alertas de glucosa, nutrición, racha positiva |
| `ArchitectureTest` | 7 | Reglas de dependencias entre capas |
| `DiabecareApiApplicationTests` | 1 | Smoke test de arranque |

### 14.3 Reglas ArchUnit verificadas

- Dominio no importa Spring, JPA, ni infraestructura
- Use Cases solo dependen de dominio e interfaces
- Controllers no acceden a repositorios directamente
- Entidades JPA solo en `infrastructure.persistence.entity`

---

## 15. Dependencias Principales

| Dependencia | Versión | Propósito |
|---|---|---|
| `spring-boot-starter-web` | 3.5.14 | REST API |
| `spring-boot-starter-security` | 3.5.14 | Autenticación |
| `spring-boot-starter-data-jpa` | 3.5.14 | Persistencia |
| `spring-boot-starter-validation` | 3.5.14 | Bean Validation |
| `spring-boot-starter-cache` | 3.5.14 | Abstracción de caché |
| `jjwt-api` + `jjwt-impl` | 0.12.5 | JWT |
| `postgresql` | 42.7.10 | Driver JDBC |
| `flyway-core` | 11.7.2 | Migraciones |
| `mapstruct` | 1.5.5 | Mapeo entre capas |
| `lombok` | 1.18.30 | Boilerplate |
| `springdoc-openapi-starter` | 2.8.9 | Swagger UI |
| `itext` | 8.0.4 | Generación PDF |
| `caffeine` | 3.2.3 | Cache en memoria |
| `web-push` | 5.1.1 | Notificaciones push |
| `bcprov-jdk15on` | 1.70 | BouncyCastle (VAPID) |
| `bucket4j-core` | 8.10.1 | Rate limiting |
| `archunit-junit5` | 1.3.0 | Tests de arquitectura |

---

## 16. Configuración y Despliegue

### 16.1 Perfiles de Spring

| Perfil | Configuración |
|---|---|
| `dev` | PostgreSQL local, logs DEBUG, Swagger habilitado |
| `prod` | PostgreSQL, logs INFO/WARN, Swagger deshabilitado |

### 16.2 Variables de Entorno Requeridas

```env
# Base de datos
DB_URL=jdbc:postgresql://localhost:5432/diabecare
DB_USERNAME=diabecare_user
DB_PASSWORD=<secret>

# JWT
JWT_SECRET_KEY=<base64-min-256-bits>
JWT_ACCESS_EXPIRY_MS=900000
JWT_REFRESH_EXPIRY_MS=604800000

# Seguridad
CORS_ALLOWED_ORIGINS=https://app.diabecare.com
BCRYPT_STRENGTH=12

# Web Push (VAPID)
VAPID_PUBLIC_KEY=<base64-ec-public-key>
VAPID_PRIVATE_KEY=<base64-ec-private-key>
VAPID_SUBJECT=mailto:admin@diabecare.com
```

### 16.3 Docker Compose (Desarrollo)

```yaml
services:
  backend:
    build: .
    ports: ['8080:8080']
    environment:
      SPRING_PROFILES_ACTIVE: dev
      DB_URL: jdbc:postgresql://postgres:5432/diabecare
      DB_USERNAME: diabecare_user
      DB_PASSWORD: dev_password
      JWT_SECRET_KEY: dev-secret-key-minimo-32-chars
      VAPID_PUBLIC_KEY: ${VAPID_PUBLIC_KEY}
      VAPID_PRIVATE_KEY: ${VAPID_PRIVATE_KEY}
      VAPID_SUBJECT: mailto:admin@diabecare.com
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

*DiabeCare Backend Documentation v2.0*
