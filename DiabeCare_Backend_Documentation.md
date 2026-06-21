# DiabeCare — Backend Architecture & Technical Documentation

> **Spring Boot 3.5 | Arquitectura Hexagonal | REST API**

| Campo | Valor |
|---|---|
| Versión | 4.0.0 |
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
7. [Seguridad y Autenticación](#7-seguridad-y-autenticación)
8. [Push Notifications](#8-push-notifications)
9. [Tareas Programadas](#9-tareas-programadas)
10. [Rate Limiting](#10-rate-limiting)
11. [Auditoría](#11-auditoría)
12. [Configuración Parametrizable (`system_config`)](#12-configuración-parametrizable-system_config)
13. [Gestión de Cuenta](#13-gestión-de-cuenta)
14. [Catálogo de Alimentos (Expansión)](#14-catálogo-de-alimentos-expansión)
15. [Internacionalización (Preparación)](#15-internacionalización-preparación)
16. [Estándares Técnicos y de Código](#16-estándares-técnicos-y-de-código)
17. [Base de Datos](#17-base-de-datos)
18. [Estrategia de Testing](#18-estrategia-de-testing)
19. [Dependencias Principales](#19-dependencias-principales)
20. [Configuración y Despliegue](#20-configuración-y-despliegue)


---

## 1. Visión General

### 1.1 Propósito

DiabeCare es una aplicación web para pacientes diabéticos que permite registrar mediciones clínicas, controlar ingesta calórica, gestionar medicamentos, recibir alertas inteligentes, y generar reportes para consultas médicas.

### 1.2 Objetivos del Sistema

- Registro y monitoreo de glucosa con detección de patrones clínicos
- Conteo de calorías y macronutrientes con 635 alimentos (colombianos, latinoamericanos e internacionales)
- Control de medicamentos con auditoría de cambios
- Signos vitales: presión arterial, peso, IMC, HbA1c estimada
- Alertas clínicas inteligentes (7 tipos + 4 patrones + ciclo menstrual)
- Notificaciones push nativas (Web Push API) y notificaciones inmediatas tras registro
- Resumen semanal automático por paciente
- Exportación de datos en CSV y JSON
- Rate limiting por paciente para proteger integridad de datos
- Parámetros clínicos y operacionales configurables sin redeploy
- Gestión de cuenta (suspensión/eliminación) por el propio usuario
- Sesiones multi-dispositivo con refresh tokens revocables: cierre de sesión individual o de todos los dispositivos, listado de sesiones activas

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
| Mensajes | Spring `MessageSource` (preparación i18n) |
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
| **Infrastructure** | JPA, seguridad, push, scheduler, rate limiting, PDF, mensajes |
| **Presentation** | Controllers REST, DTOs, validación de entrada, manejo de errores |

### 2.3 Estructura de Paquetes

```
com.diabecare
├── domain/
│   ├── model/              # Patient, GlucoseReading, MealEntry, VitalSign,
│   │                       # Medication, ExerciseLog, MenstrualCycle,
│   │                       # Alert, AuditLog, WeeklySummaryData,
│   │                       # User, SystemConfig, RefreshToken
│   ├── exception/          # Excepciones de dominio + RateLimitExceededException,
│   │                       # InvalidRefreshTokenException
│   └── service/             # MedicalCalculatorService, PatternDetectorService,
│                            # WeeklySummaryService, GlucoseExportService,
│                            # AuditService, RateLimitService
├── application/
│   ├── port/in/             # LoginUseCase, RegisterUseCase, RefreshAccessTokenUseCase,
│   │                        # LogoutCurrentSessionUseCase, LogoutAllSessionsUseCase,
│   │                        # GetActiveSessionsUseCase, SuspendAccountUseCase,
│   │                        # DeleteAccountUseCase, GetSystemConfigUseCase, + casos
│   │                        # de uso existentes (glucosa, comidas, etc.)
│   ├── port/out/             # LoadPatientPort, SaveAuditLogPort, NotifyPatientPort,
│   │                         # AuthenticateUserPort, GenerateTokenPort, RefreshTokenPort,
│   │                         # MessageResolverPort, SystemConfigPort, LoadUserPort,
│   │                         # SaveUserPort
│   └── usecase/               # Implementaciones (1 clase por operación)
├── infrastructure/
│   ├── persistence/          # JPA entities, repositories, adapters, mappers
│   │                         # (incluye RefreshTokenEntity)
│   ├── security/              # JWT filter, UserDetailsServiceImpl,
│   │                          # AuthenticateUserAdapter, GenerateTokenAdapter,
│   │                          # RefreshTokenAdapter
│   │   └── handler/            # RestAuthenticationEntryPoint, RestAccessDeniedHandler
│   ├── config/                 # DiabeCareProperties (Security+Push only),
│   │                           # JwtProperties, RateLimitConfig, MessageSourceConfig,
│   │                           # MessageResolverAdapter
│   ├── push/                   # PushNotificationService, PushNotificationAdapter
│   ├── pdf/                    # PdfReportAdapter
│   └── scheduler/               # WeeklySummaryScheduler
└── presentation/
    ├── controller/             # REST Controllers (incluye AccountController,
    │                           # SystemConfigController, AuthController ampliado)
    ├── dto/                     # Request/Response records (incluye ActiveSessionResponse)
    ├── mapper/                  # MapStruct mappers
    ├── util/                     # DeviceLabelResolver
    └── advice/                   # GlobalExceptionHandler
```

### 2.4 Reglas de Arquitectura (ArchUnit)

- El dominio **no puede** importar clases de Spring, JPA o cualquier framework
- Los Use Cases solo pueden depender del dominio y de interfaces (puertos)
- Los Controllers no pueden acceder directamente a repositorios
- Las entidades JPA deben estar en `infrastructure.persistence.entity`

### 2.5 Refactor de Autenticación (sesión de refactor original)

**Antes**: `AuthController` orquestaba directamente múltiples use cases, llamaba `JwtService` y buscaba `userId` — violando SRP y filtrando infraestructura a presentación.

**Después** (ampliado esta sesión con refresh tokens):
```
LoginUseCase (port/in)
  → LoginUseCaseImpl
      → AuthenticateUserPort.authenticate()      → AuthenticateUserAdapter (Spring Security)
      → LoadUserPort.findUserIdByEmail()
      → LoadPatientPort.findByUserId()
      → GenerateTokenPort.generateToken()         → GenerateTokenAdapter (JwtService + JwtProperties)
      → GenerateTokenPort.getExpiresIn()
      → RefreshTokenPort.issue(userId, deviceLabel) → RefreshTokenAdapter

RegisterUseCase (port/in)
  → RegisterUseCaseImpl
      → RegisterUserUseCase.execute()
      → RegisterPatientUseCase.execute()
      → GenerateTokenPort.generateToken()
      → RefreshTokenPort.issue(userId, deviceLabel)
```

`AuthController` ahora solo delega — construye el `Command`, llama al use case, mapea el `Result` a `AuthResponse`. Sin lógica de negocio. El `deviceLabel` (resuelto desde `User-Agent` por `DeviceLabelResolver`) se extrae en el controller y viaja en el `Command` — el use case de aplicación nunca conoce `HttpServletRequest` directamente.

**JWT actualizado**: incluye claim adicional `userId` (UUID), no solo `sub` (email). Esto permite que el frontend identifique al usuario sin un endpoint adicional — necesario para `SuspendAccountUseCase`/`DeleteAccountUseCase`, que operan sobre `userId`, no `patientId`.

### 2.6 Refresh Tokens (esta sesión)

Ver sección 7.6 para el diseño completo. Resumen arquitectónico: `RefreshTokenPort` (puerto out) define el ciclo de vida completo (`issue`, `redeem`, `revokeOne`, `revokeAllForUser`, `findActiveSessions`); `RefreshTokenAdapter` (infraestructura) lo implementa con un valor aleatorio opaco hasheado en BD, nunca un JWT autocontenido. Cuatro casos de uso nuevos consumen el puerto: `RefreshAccessTokenUseCaseImpl`, `LogoutCurrentSessionUseCaseImpl`, `LogoutAllSessionsUseCaseImpl`, `GetActiveSessionsUseCaseImpl` — cada uno con una sola responsabilidad, siguiendo el mismo patrón "1 use case por operación" del resto del proyecto.

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

### 3.2 User _(nuevo — antes solo existía a nivel de persistencia)_

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | UUID | Identificador único |
| `email` | String | Email (anonimizado tras eliminación de cuenta) |
| `role` | String | `PATIENT`, `ADMIN` (futuro) |
| `enabled` | boolean | `false` si suspendida o eliminada |
| `suspendedAt` | LocalDateTime | Fecha de suspensión, null si activa |
| `deletedAt` | LocalDateTime | Fecha de eliminación, null si no eliminada |
| `createdAt` | LocalDateTime | Fecha de creación |

Métodos: `isSuspended()`, `isDeleted()`.

### 3.3 SystemConfig _(nuevo)_

| Campo | Tipo | Descripción |
|---|---|---|
| `key` | String | Clave única (ej. `pattern.fasting_threshold_mgdl`) |
| `value` | String | Valor almacenado como texto |
| `dataType` | Enum | `INTEGER`, `DECIMAL`, `STRING`, `BOOLEAN` |
| `category` | Enum | `ALERTS`, `PATTERNS`, `RATE_LIMIT` |
| `description` | String | Descripción legible |
| `updatedAt` | LocalDateTime | Última actualización |

### 3.4 RefreshToken _(nuevo)_

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | UUID | Identificador único |
| `userId` | UUID | Referencia al usuario |
| `tokenHash` | String | Hash SHA-256 del token crudo — el valor crudo nunca se persiste |
| `deviceLabel` | String | Etiqueta legible del dispositivo (ej. "Chrome en Windows"), resuelta desde `User-Agent` |
| `lastUsedAt` | LocalDateTime | Última vez que se usó (al emitir o rotar) |
| `expiresAt` | LocalDateTime | Fecha de expiración (7 días desde la emisión) |
| `revokedAt` | LocalDateTime | Fecha de revocación, null si sigue activo |
| `createdAt` | LocalDateTime | Fecha de creación |

Métodos: `isExpired()`, `isRevoked()`, `isValid()` (`!isExpired() && !isRevoked()`).

### 3.5 GlucoseReading

| Campo | Tipo | Descripción |
|---|---|---|
| `readingId` | UUID | ID único |
| `value` | BigDecimal | Valor en mg/dL o mmol/L |
| `unit` | Enum | `MG_DL`, `MMOL_L` |
| `readingType` | Enum | `FASTING`, `PRE_MEAL`, `POST_MEAL`, `BEDTIME`, `RANDOM` |
| `status` | Enum | `CRITICALLY_LOW`, `LOW`, `NORMAL`, `HIGH`, `CRITICALLY_HIGH` |
| `measuredAt` | LocalDateTime | Fecha y hora de medición |

### 3.6 Alert

| Campo | Tipo | Descripción |
|---|---|---|
| `type` | Enum | `GLUCOSE_OUT_OF_RANGE`, `NO_GLUCOSE_RECORDED`, `HIGH_HBA1C_ESTIMATED`, `NO_MEAL_RECORDED`, `POSITIVE_STREAK`, `GLUCOSE_AVERAGE_HIGH`, `GLUCOSE_PATTERN_DETECTED` |
| `severity` | Enum | `SUCCESS`, `INFO`, `WARNING`, `DANGER` |
| `title` | String | Título — resuelto vía `MessageResolverPort` |
| `message` | String | Mensaje descriptivo — resuelto vía `MessageResolverPort` |

### 3.7 AuditLog

| Campo | Tipo | Descripción |
|---|---|---|
| `entityType` | String | `PATIENT`, `MEDICATION` |
| `action` | Enum | `CREATE`, `UPDATE`, `DELETE` |
| `fieldName` | String | Campo modificado |
| `oldValue` | String | Valor anterior |
| `newValue` | String | Valor nuevo |
| `performedAt` | LocalDateTime | Fecha y hora del cambio |

### 3.8 WeeklySummaryData _(record)_

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

`MedicalCalculatorService` encapsula todas las fórmulas clínicas. Estas fórmulas son estándares médicos y **no son parametrizables** (a diferencia de los umbrales de alerta).

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
Objetivo: CV < umbral configurable en system_config (default 36%)
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

Detecta patrones clínicos en las lecturas de los últimos N días (configurable via `pattern.days_window`):

| Método | Patrón | Umbral (config key) |
|---|---|---|
| `detectHighFastingPattern` | Hiperglucemia en ayuno | `pattern.fasting_threshold_mgdl` (130), `pattern.fasting_ratio` (0.6) |
| `detectHighPostMealPattern` | Picos postprandiales | `pattern.postmeal_threshold_mgdl` (180), `pattern.postmeal_ratio` (0.5) |
| `detectRecurrentHypoglycemia` | Hipoglucemia recurrente | `pattern.hypo_min_episodes` (3) |
| `detectHighVariability` | Alta variabilidad | `pattern.cv_threshold` (36.0), `pattern.min_readings_variability` (7) |

Cada método retorna `Optional<Alert>` — patrón limpio, testeable unitariamente. Los títulos y mensajes se resuelven vía `MessageResolverPort` (ver sección 15).

**Importante**: inyecta `SystemConfigPort` (no `RateLimitConfig` ni infraestructura) — todos los umbrales vienen de `system_config`, ninguno está hardcodeado en código.

### 5.2 WeeklySummaryService

Construye el resumen semanal por paciente:
- `buildSummary(patient, readings)` → `Optional<WeeklySummaryData>`
- `buildPushTitle()` → resuelve `weekly-summary.push.title` vía `MessageResolverPort`
- `buildPushMessage(data)` → resuelve `weekly-summary.push.message` con interpolación

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

Verifica límites por paciente y operación usando Bucket4j. **Refactorizado esta sesión** — antes los use cases importaban `RateLimitConfig` (infraestructura) directamente, violando arquitectura hexagonal. Ahora:

```java
rateLimitService.checkGlucoseLimit(patientId);
rateLimitService.checkMealLimit(patientId);
rateLimitService.checkExerciseLimit(patientId);
```

Cada método interno construye el bucket con el límite correspondiente leído de `SystemConfigPort`, sin que el use case conozca Bucket4j ni `RateLimitConfig`.

---

## 6. API REST — Endpoints

### Autenticación
```
POST   /api/v1/auth/register
POST   /api/v1/auth/login
POST   /api/v1/auth/refresh         # nuevo access token a partir de un refresh token vigente
POST   /api/v1/auth/logout          # cierra solo la sesión actual (recibe refreshToken)
POST   /api/v1/auth/logout-all      # cierra todas las sesiones del usuario (recibe userId)
GET    /api/v1/auth/sessions/{userId}  # lista las sesiones (dispositivos) activas
```

### Cuenta de usuario _(nuevo)_
```
PATCH  /api/v1/account/{userId}/suspend
DELETE /api/v1/account/{userId}
```

### Configuración del sistema _(nuevo)_
```
GET    /api/v1/system-config
POST   /api/v1/system-config/reload
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

## 7. Seguridad y Autenticación

### 7.1 JWT (access token)

- Access token: 15 minutos (`JWT_ACCESS_EXPIRY_MS=900000`)
- Algoritmo: HMAC-SHA256
- Cabecera: `Authorization: Bearer <token>`
- **Claims**: `sub` (email) + `userId` (UUID) — necesario para que el frontend identifique al usuario sin endpoint adicional

### 7.2 Flujo de Login

```
AuthController.login()
  → LoginUseCase.execute(Command(email, password, deviceLabel))
      → AuthenticateUserPort.authenticate()   // valida credenciales, lanza excepción si falla
      → LoadUserPort.findUserIdByEmail()
      → LoadPatientPort.findByUserId()
      → GenerateTokenPort.generateToken(email, userId)
      → GenerateTokenPort.getExpiresIn()
      → RefreshTokenPort.issue(userId, deviceLabel)
  ← Result(token, expiresIn, refreshToken, refreshExpiresIn, patientId, userId)
```

### 7.3 Endpoints públicos

```java
private static final String[] PUBLIC_ENDPOINTS = {
    "/api/v1/auth/**",
    "/swagger-ui/**", "/v3/api-docs/**",
    "/actuator/health"
};
```

`/api/v1/auth/refresh` necesariamente es público — el cliente lo llama justo porque su access token ya expiró; exigir uno válido sería contradictorio. La validación real de `/refresh` ocurre dentro del propio `redeem()` del refresh token, no a nivel de `SecurityConfig`.

### 7.4 Protección de datos

- Contraseñas: BCrypt con strength configurable (`BCRYPT_STRENGTH=12`)
- CORS: orígenes configurables via `CORS_ALLOWED_ORIGINS`
- Sesión: `STATELESS` — sin cookies ni sesiones del servidor
- Refresh tokens: valor aleatorio de 64 bytes (Base64 URL-safe), hasheado con SHA-256 antes de persistir — el valor crudo nunca se guarda en BD

### 7.5 Manejo de errores de autenticación

| Excepción / caso | Código | Status | Descripción |
|---|---|---|---|
| `DisabledException` | `ACCOUNT_SUSPENDED` | 403 | Cuenta suspendida o eliminada (`enabled=false`) |
| `BadCredentialsException` | `INVALID_CREDENTIALS` | 401 | Email o contraseña incorrectos |
| `InvalidRefreshTokenException` | `INVALID_REFRESH_TOKEN` | 401 | Refresh token inexistente, ya usado (rotado), expirado, o usuario suspendido/eliminado |
| Sin autenticación válida en ruta protegida | `SESSION_EXPIRED` | 401 | Token ausente, mal formado o expirado — manejado por `RestAuthenticationEntryPoint` |
| Autenticado pero sin permiso | `ACCESS_DENIED` | 403 | Manejado por `RestAccessDeniedHandler` |

```json
{
  "timestamp": "2026-06-17T20:00:00",
  "status": 403,
  "error": "Forbidden",
  "code": "ACCOUNT_SUSPENDED",
  "message": "Tu cuenta está suspendida. Contacta soporte para reactivarla.",
  "path": "/api/v1/auth/login"
}
```

Otros códigos: `PATIENT_NOT_FOUND`, `GLUCOSE_READING_NOT_FOUND`, `DOMAIN_VALIDATION_ERROR`, `VALIDATION_ERROR`, `RATE_LIMIT_EXCEEDED`, `INTERNAL_ERROR`.

### 7.6 Corrección de semántica 401 vs 403 (esta sesión)

**Bug reportado**: la aplicación mostraba pantallas con datos faltantes/rotos cuando la sesión expiraba mientras el usuario navegaba, y el Network tab mostraba `403 Forbidden` en lugar de `401 Unauthorized`. Causa raíz: Spring Security en modo `STATELESS`, sin un `AuthenticationEntryPoint` configurado explícitamente, cae a su comportamiento default — que devuelve 403 también cuando *no hay autenticación en absoluto*, mezclando ese caso con el 403 real ("autenticado pero sin permiso").

**Corrección**:
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        // ...
        .exceptionHandling(exceptions -> exceptions
            .authenticationEntryPoint(restAuthenticationEntryPoint)  // 401
            .accessDeniedHandler(restAccessDeniedHandler)            // 403
        )
        // ...
}
```

`RestAuthenticationEntryPoint implements AuthenticationEntryPoint` y `RestAccessDeniedHandler implements AccessDeniedHandler` — ambos serializan un `ApiError` (el mismo record que usa `GlobalExceptionHandler`) manualmente con un `ObjectMapper` inyectado, porque operan en el `ExceptionTranslationFilter` de Spring Security, **antes** de llegar al `DispatcherServlet` — `@RestControllerAdvice` no los intercepta.

Patrón verificado contra la documentación oficial de Spring Security 6.x y fuentes externas (Baeldung) — es el mecanismo estándar de la industria para este problema, no una solución ad-hoc.

### 7.7 Refresh Tokens — diseño completo

#### Por qué opaco y no JWT autocontenido

| Enfoque | Revocable antes de expirar | Detecta robo |
|---|---|---|
| **Elegido**: valor aleatorio, hash SHA-256 en BD | Sí | Sí (reuso de un token ya rotado) |
| Descartado: JWT largo autocontenido | No (sin lista de revocación aparte) | No |

```sql
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    device_label VARCHAR(150),
    last_used_at TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

#### RefreshTokenPort

```java
public interface RefreshTokenPort {
    record IssuedToken(String rawToken, long expiresInMs) {}
    record RedeemedToken(UUID userId, String deviceLabel) {}
    record ActiveSession(UUID id, String deviceLabel, LocalDateTime lastUsedAt, LocalDateTime createdAt) {}

    IssuedToken issue(UUID userId, String deviceLabel);
    Optional<RedeemedToken> redeem(String rawToken);   // valida + ROTA (revoca el usado)
    void revokeOne(String rawToken);
    void revokeAllForUser(UUID userId);
    List<ActiveSession> findActiveSessions(UUID userId);
}
```

`RefreshTokenAdapter` implementa el puerto: `issue()` genera 64 bytes aleatorios (`SecureRandom`), los codifica en Base64 URL-safe, hashea con SHA-256 y persiste solo el hash. `redeem()` busca por hash, valida que no esté revocado ni expirado, y en la **misma operación** lo marca revocado (rotación) antes de devolver `userId`+`deviceLabel` — el caller es responsable de emitir el par nuevo.

#### Flujo de refresco

```
POST /api/v1/auth/refresh { refreshToken }
  → RefreshAccessTokenUseCaseImpl
      → RefreshTokenPort.redeem(rawToken)
      → LoadUserPort.findById(userId)   // valida !suspended && !deleted && enabled
      → GenerateTokenPort.generateToken()
      → RefreshTokenPort.issue(userId, deviceLabel)   // mismo deviceLabel, nuevo token
  ← Result(accessToken, expiresIn, refreshToken, refreshExpiresIn)
```

Si la cuenta fue suspendida/eliminada después de emitir el refresh token, el siguiente refresco falla con `InvalidRefreshTokenException` aunque el token no haya expirado por tiempo.

#### Logout: sesión actual vs todos los dispositivos

Inicialmente se implementó un único `LogoutUseCase` que revocaba *todos* los refresh tokens del usuario — pero esto se ejecutaba también desde el logout normal del navbar, lo cual no es el comportamiento esperado (cerrar sesión en la web no debería desconectar el móvil). Se separó en dos casos de uso:

| Caso de uso | Recibe | Revoca | Endpoint |
|---|---|---|---|
| `LogoutCurrentSessionUseCaseImpl` | `refreshToken` | Solo ese token | `POST /auth/logout` |
| `LogoutAllSessionsUseCaseImpl` | `userId` | Todos los activos del usuario | `POST /auth/logout-all` |

`SuspendAccountUseCaseImpl` y `DeleteAccountUseCaseImpl` llaman `RefreshTokenPort.revokeAllForUser()` directamente (no vía caso de uso de logout) — suspender/eliminar invalida todas las sesiones automáticamente.

#### Sesiones activas y `deviceLabel`

```java
public final class DeviceLabelResolver {
    public static String resolve(String userAgent) {
        // heurísticas simples por substring — sin librería externa de UA parsing
        // ej. "Chrome en Windows", "Safari en iPhone"
    }
}
```

`AuthController` extrae el header `User-Agent` y llama `DeviceLabelResolver.resolve(...)` antes de construir el `Command` — los casos de uso de aplicación nunca conocen `HttpServletRequest`. El `deviceLabel` se persiste en `issue()` y se preserva en cada rotación (`redeem()` lo devuelve para reutilizarlo en el `issue()` siguiente).

`GET /api/v1/auth/sessions/{userId}` expone `findActiveSessions()` — sesiones no expiradas y no revocadas, ordenadas por `last_used_at DESC`. Es la base de datos que necesitaría un futuro listado de "dispositivos conectados" en una app móvil, sin requerir cambios adicionales en el modelo.

---

## 8. Push Notifications

### 8.1 Flujo

1. Frontend solicita clave pública VAPID (`GET /api/v1/push/vapid-public-key`)
2. Frontend suscribe el navegador al `PushManager` con la clave pública
3. Frontend envía `{ endpoint, p256dh, auth }` al backend (`POST /api/v1/push/subscribe`)
4. Backend guarda la suscripción en `push_subscriptions` asociada al `patient_id`
5. Backend envía notificaciones con `PushNotificationService` usando la librería `web-push`

### 8.2 Auto-limpieza

Al recibir respuesta HTTP 410 (Gone) al enviar una notificación, la suscripción se elimina automáticamente de la base de datos.

### 8.3 Complemento: notificaciones inmediatas en frontend (sin WebSockets)

**Decisión de arquitectura**: se evaluó implementar WebSockets para alertas en tiempo real y se **descartó** — las alertas se calculan on-demand (no hay eventos asíncronos espontáneos del servidor que las disparen). En su lugar, el frontend consulta `GET /api/v1/alerts/{patientId}` inmediatamente después de cada registro exitoso (glucosa, comida, ejercicio) y compara contra las alertas previamente conocidas en la sesión del navegador, notificando solo las realmente nuevas.

**Actualización de esta sesión**: el canal de presentación de esas notificaciones en el frontend cambió de `MatSnackBar` a un `NotificationService` global con un banner montado una sola vez en el componente raíz — sin relación con este backend (la API de alertas no cambió), pero corrige un bug donde el overlay compartido de Angular Material (`CdkOverlay`) podía quedar huérfano tras una navegación rápida, bloqueando clics en la página de destino. Ver documentación de frontend, sección de notificaciones, para el detalle completo.

---

## 9. Tareas Programadas

### 9.1 WeeklySummaryScheduler

```java
@Scheduled(cron = "0 0 8 * * MON", zone = "America/Bogota")
public void sendWeeklySummaries()
```

Flujo: `WeeklySummaryScheduler` (solo dispara) → `SendWeeklySummaryUseCaseImpl` (orquesta) → `WeeklySummaryService` (calcula) → `NotifyPatientPort` (notifica). SRP respetado en cada capa.

---

## 10. Rate Limiting

Implementado con **Bucket4j** + **Caffeine Cache**. Los límites son configurables vía `system_config`, no hardcodeados.

| Operación | Config key | Default |
|---|---|---|
| Registro de glucosa | `rate_limit.glucose_per_hour` | 20 |
| Registro de comidas | `rate_limit.meal_per_hour` | 15 |
| Registro de ejercicio | `rate_limit.exercise_per_hour` | 10 |

Al exceder el límite: `HTTP 429 Too Many Requests` con código `RATE_LIMIT_EXCEEDED`.

**Arquitectura**: `RateLimitConfig` solo expone el `@Bean` del `Cache<UUID, Bucket>` — la creación de buckets vive en `RateLimitService` (dominio), que lee el límite de `SystemConfigPort` y construye el `Bucket` dinámicamente. Los use cases de registro (`RegisterGlucoseReadingUseCaseImpl`, etc.) solo llaman `rateLimitService.checkGlucoseLimit(patientId)` — no conocen Bucket4j ni `RateLimitConfig`.

---

## 11. Auditoría

Registra cambios en perfil del paciente y medicamentos en la tabla `audit_log`. Ver `DIABECARE.md` para el detalle completo — sin cambios esta sesión.

---

## 12. Configuración Parametrizable (`system_config`)

### 12.1 Motivación

Antes de esta sesión, los umbrales clínicos (alertas, patrones) y operacionales (rate limiting) estaban hardcodeados en código (`PatternDetectorService`) o en `application.yml` (`DiabeCareProperties.Clinical`). Cualquier ajuste requería un redeploy. Se migró todo a una tabla en BD.

### 12.2 Estructura

```sql
CREATE TABLE system_config (
    key         VARCHAR(100) PRIMARY KEY,
    value       TEXT        NOT NULL,
    data_type   VARCHAR(20) NOT NULL,
    category    VARCHAR(50) NOT NULL,
    description TEXT,
    updated_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);
```

### 12.3 Parámetros (16 totales)

| Categoría | Key | Default | Descripción |
|---|---|---|---|
| ALERTS | `alert.hours_without_glucose` | 8 | Horas sin glucosa para alerta |
| ALERTS | `alert.hba1c_threshold` | 8.0 | HbA1c estimada que dispara alerta |
| ALERTS | `alert.good_tir_threshold` | 70.0 | TIR mínimo para racha positiva |
| ALERTS | `alert.streak_days` | 7 | Días para evaluar racha positiva |
| ALERTS | `alert.min_readings_for_stats` | 3 | Mínimo lecturas para estadísticas |
| PATTERNS | `pattern.days_window` | 14 | Ventana de días para detección de patrones |
| PATTERNS | `pattern.fasting_threshold_mgdl` | 130 | Umbral glucosa en ayuno |
| PATTERNS | `pattern.postmeal_threshold_mgdl` | 180 | Umbral glucosa postprandial |
| PATTERNS | `pattern.fasting_ratio` | 0.6 | Ratio mínimo de ayunos altos |
| PATTERNS | `pattern.postmeal_ratio` | 0.5 | Ratio mínimo de postprandiales altos |
| PATTERNS | `pattern.hypo_min_episodes` | 3 | Mínimo episodios hipoglucemia |
| PATTERNS | `pattern.cv_threshold` | 36.0 | Umbral CV variabilidad alta |
| PATTERNS | `pattern.min_readings_variability` | 7 | Mínimo lecturas para variabilidad |
| RATE_LIMIT | `rate_limit.glucose_per_hour` | 20 | Máx registros glucosa/hora |
| RATE_LIMIT | `rate_limit.meal_per_hour` | 15 | Máx registros comidas/hora |
| RATE_LIMIT | `rate_limit.exercise_per_hour` | 10 | Máx registros ejercicio/hora |

### 12.4 Arquitectura

```
SystemConfigPort (application/port/out)
  → SystemConfigAdapter (infrastructure/persistence/adapter)
      - @PostConstruct: carga toda la tabla a un ConcurrentHashMap<String,String>
      - getInt/getDecimal/getString(key): lee del caché en memoria
      - reload(): vuelve a leer la tabla completa (sin redeploy)
      - findAll(): retorna todos los SystemConfig para el endpoint GET
```

Consumido por: `PatternDetectorService`, `AlertConfigAdapter`, `RateLimitService`, `GetAlertsUseCaseImpl`.

### 12.5 Endpoints

```
GET  /api/v1/system-config           # Lista los 16 parámetros con su descripción
POST /api/v1/system-config/reload    # Recarga la caché desde BD sin redeploy
```

### 12.6 Decisión: qué NO se parametrizó

- Fórmulas médicas (ADAG, IMC) — son estándares internacionales, no deben cambiar
- Claves JWT/VAPID, BCrypt strength — secretos de seguridad, van en variables de entorno
- CORS — configuración de infraestructura

---

## 13. Gestión de Cuenta

### 13.1 Motivación

Funcionalidad estándar esperada en cualquier app — el usuario debe poder suspender o eliminar su propia cuenta sin intervención de soporte.

### 13.2 Modelo

```sql
ALTER TABLE users ADD COLUMN suspended_at TIMESTAMP;
ALTER TABLE users ADD COLUMN deleted_at   TIMESTAMP;
```

### 13.3 Flujo de Suspensión

```
PATCH /api/v1/account/{userId}/suspend
  → SuspendAccountUseCaseImpl
      → LoadUserPort.findById(userId)
      → valida: !user.isDeleted() && !user.isSuspended()
      → SaveUserPort.suspend(user)   // enabled=false, suspended_at=now()
```

### 13.4 Flujo de Eliminación

```
DELETE /api/v1/account/{userId}
  → DeleteAccountUseCaseImpl
      → LoadUserPort.findById(userId)
      → valida: !user.isDeleted()
      → SaveUserPort.delete(user)
          // enabled=false, deleted_at=now()
          // email anonimizado: deleted_{userId}@diabecare.deleted
```

### 13.5 Efecto en autenticación

Una cuenta con `enabled=false` (suspendida o eliminada) no puede iniciar sesión — Spring Security lanza `DisabledException`, capturada por `GlobalExceptionHandler` y traducida a `403 ACCOUNT_SUSPENDED`.

---

## 14. Catálogo de Alimentos (Expansión)

### 14.1 Alcance

El catálogo de alimentos (tabla `foods`) creció de 172 registros (V4) a **635** en esta sesión, repartidos en dos migraciones (V12: +315, V13: +148), sin modificar el esquema de la tabla — `category` siempre fue `VARCHAR` libre, tanto en `Food.java` (dominio) como en `FoodResponse` (presentación/frontend), así que las 7 categorías nuevas no requirieron cambios de código, solo datos.

### 14.2 Categorías nuevas

| Categoría | Migración | Contenido |
|---|---|---|
| `FRUTOS_SECOS` | V12 | Almendras, nueces, semillas, mantequilla de maní |
| `EMBUTIDOS` | V12 | Separada de `CARNES` para no diluir la categoría original — salchichas, jamón, mortadela, chorizo |
| `CONDIMENTOS` | V12 | Salsas, aderezos, caldos — antes sin categoría propia |
| `COMIDA_RAPIDA` | V12, ampliada en V13 | Cadena/franquicia tipo: hamburguesas, pizza, comida mexicana/italiana/asiática/americana |
| `PANADERIA` | V12 | Productos de panadería y snacks dulces/salados industriales |
| `VEGANOS` | V13 | Sustitutos directos de proteína animal y lácteos (hamburguesa vegetal, seitán, quesos/yogures veganos, proteína en polvo) |
| `INDUSTRIALES` | V13 | Marcas de consumo común en Colombia (Alpina, Zenú, Bimbo, Fruco, Postobón) e internacionales (Coca-Cola, Nutella, Doritos) |

`COMIDA_RAPIDA` (ampliación V13) y `PREPARADOS` (ampliación V13) también incorporaron una nueva categoría conceptual de comida de calle colombiana — separada en `COMIDA_CALLE` (choripán, salchipapa, perro caliente especial, arepa con huevo) por tener un perfil de consumo distinto a un plato de almuerzo (`PREPARADOS`) o a una franquicia (`COMIDA_RAPIDA`).

### 14.3 Decisión: alimentos vegetales genéricos no se re-categorizaron

Tofu, tempeh, edamame y las leches vegetales (almendra, soya, avena) ya existían en `LEGUMBRES`/`LACTEOS` antes de esta sesión. Se decidió **no moverlos** a `VEGANOS` — esa categoría se reservó exclusivamente para sustitutos directos pensados para reemplazar un alimento de origen animal específico (ej. "hamburguesa vegetal" reemplaza "hamburguesa de carne"), mientras que tofu o leche de almendra son alimentos con identidad propia, no sustitutos de algo puntual.

### 14.4 Validación realizada (sin Postgres disponible)

Las migraciones se generaron y validaron en un entorno sin acceso a Maven ni PostgreSQL. La validación fue estructural, no funcional:

- Paréntesis y comillas balanceados en cada archivo SQL
- Exactamente 8 columnas por fila de `INSERT`
- Sin nombres duplicados (contra el catálogo acumulado completo, no solo dentro de cada migración)
- Valores nutricionales dentro de rango fisiológico razonable (0–950 kcal/100g, 0–100g por macronutriente)

**No se ejecutó el `INSERT` contra una base real.** La primera vez que se levante el backend con estas migraciones, Flyway debe aplicarlas sin error — esa es la verificación pendiente y definitiva.

---

## 15. Internacionalización (Preparación)

### 15.1 Decisión

Se evaluó implementar i18n completo (multi-idioma) y se **descartó** por falta de caso de uso real: la app es 100% en español, dirigida a Colombia, y buena parte de los 635 alimentos del catálogo (muchos colombianos) no tienen traducción razonable a otros idiomas. El costo de implementación (extracción completa de strings backend + frontend, bundles separados) no se justifica sin un mercado objetivo en otro idioma.

### 15.2 Preparación de bajo costo implementada

En lugar de i18n completo, se centralizaron los mensajes de negocio (alertas, patrones, resumen semanal) en un único punto, sin agregar selector de idioma ni `LocaleResolver`:

```
MessageResolverPort (application/port/out)
  → MessageResolverAdapter (infrastructure/config)
      - usa Spring's MessageSource (ResourceBundleMessageSource)
      - resolve(key, args...) → MessageSource.getMessage(key, args, LocaleContextHolder.getLocale())
```

```
src/main/resources/messages.properties   # Único archivo de mensajes (español)
```

### 15.3 Ejemplo de mensaje con interpolación

```properties
alert.pattern.fasting-high.message={0} de tus últimas {1} lecturas de ayuno superaron {2} mg/dL. Considera ajustar tu insulina basal o consultar a tu médico.
```

```java
messages.resolve("alert.pattern.fasting-high.message", highCount, fasting.size(), threshold)
```

> **Nota técnica**: `MessageSource` usa `MessageFormat`, cuyo patrón de interpolación es `{0}`, `{1}` — distinto de `String.format` (`%d`, `%.0f`). Para números con formato específico se usa la sintaxis extendida de `MessageFormat`, ej. `{0,number,#}` para enteros sin decimales.

### 15.4 Servicios migrados

`PatternDetectorService`, `GetAlertsUseCaseImpl`, `WeeklySummaryService` — ya no contienen strings de negocio hardcodeados ni `String.format` para mensajes al usuario.

### 15.5 Costo de agregar un segundo idioma en el futuro

Crear `messages_en.properties` con las traducciones + configurar un `LocaleResolver` (ej. basado en header `Accept-Language` o preferencia del paciente). **Cero cambios en la lógica de negocio** — esa es la ganancia de esta preparación.

---

## 16. Estándares Técnicos y de Código

### 16.1 Convenciones de Nomenclatura

| Elemento | Convención | Ejemplo |
|---|---|---|
| Clases / Interfaces | PascalCase | `GlucoseReadingService` |
| Métodos / Variables | camelCase | `findByPatientId()` |
| Constantes | UPPER_SNAKE_CASE | `MAX_GLUCOSE_VALUE` |
| Paquetes | lowercase | `com.diabecare.domain.model` |
| Tablas BD | snake_case | `glucose_readings`, `system_config` |
| DTOs Request | PascalCase + `Request` | `RegisterGlucoseRequest` |
| DTOs Response | PascalCase + `Response` | `GlucoseReadingResponse` |
| Puertos entrada | PascalCase + `UseCase` | `LoginUseCase`, `SuspendAccountUseCase` |
| Puertos salida | PascalCase + `Port` | `LoadPatientPort`, `MessageResolverPort` |

### 16.2 Principios SOLID

- **S** — Un use case por operación. `RateLimitService` tiene un método por tipo de operación (`checkGlucoseLimit`, `checkMealLimit`, `checkExerciseLimit`)
- **O** — Nuevas funcionalidades = nuevos use cases, no modificar existentes
- **L** — Los puertos son contratos; cualquier implementación debe cumplirlos
- **I** — Puertos granulares: `LoadPatientPort`, `SavePatientPort`, `LoadUserPort`, `SaveUserPort` separados
- **D** — Use Cases dependen de interfaces, nunca de implementaciones concretas (ej. `LoginUseCaseImpl` no conoce `JwtService` directamente, solo `GenerateTokenPort`)

### 16.3 Reglas de código

- Records Java para DTOs inmutables
- `@Builder` + `@Getter` en entidades de dominio (nunca setters públicos)
- Factory methods con validación: `GlucoseReading.create(...)`, `Patient.create(...)`
- Métodos privados atómicos en use cases complejos
- `Optional<T>` para retornos que pueden ser vacíos (detectores de patrón, `LoadUserPort.findById`)
- Ningún use case importa clases de `infrastructure.*` directamente — siempre vía puerto (regla verificada con ArchUnit y corregida explícitamente en `RateLimitConfig`/`RateLimitService` y en el flujo de autenticación esta sesión)

---

## 17. Base de Datos

### 17.1 Migraciones Flyway

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
| V10 | `V10__system_config.sql` | system_config + seed 16 parámetros |
| V11 | `V11__user_account_management.sql` | users.suspended_at, users.deleted_at |
| V12 | `V12__expand_foods.sql` | +315 alimentos (total 487): FRUTOS_SECOS, EMBUTIDOS, CONDIMENTOS, COMIDA_RAPIDA, PANADERIA |
| V13 | `V13__expand_vegan_and_industrial_foods.sql` | +148 alimentos (total 635): VEGANOS, INDUSTRIALES + ampliación de COMIDA_RAPIDA/PREPARADOS |
| V15 | `V15__refresh_tokens.sql` | Tabla refresh_tokens (sesión multi-dispositivo). *No existe V14 — ver nota abajo* |

> **Nota sobre la numeración**: no existe `V14`. Durante el desarrollo de esta sesión se generó una migración adicional de alimentos como `V14` y se decidió fusionarla dentro de `V13` antes de ejecutar (ninguna de las dos se había aplicado aún), dejando ese número sin usar. Flyway no exige numeración consecutiva, solo orden estrictamente creciente, así que esto no representa un problema funcional.

### 17.2 Tablas Principales

| Tabla | Descripción |
|---|---|
| `users` | Credenciales, `enabled`, `suspended_at`, `deleted_at` |
| `patients` | Perfil médico del paciente |
| `glucose_readings` | Lecturas de glucosa |
| `meal_entries` + `meal_items` | Comidas y alimentos individuales |
| `foods` | 635 alimentos (colombianos, latinoamericanos e internacionales) con macros |
| `vital_signs` | Peso, presión, FC, HbA1c medida |
| `medications` | Medicamentos activos |
| `exercise_logs` | Registros de actividad física |
| `menstrual_cycles` | Ciclos menstruales con síntomas |
| `push_subscriptions` | Suscripciones Web Push por paciente |
| `audit_log` | Historial de cambios auditables |
| `system_config` | Parámetros clínicos y operacionales |
| `refresh_tokens` | Sesiones activas por dispositivo, hash del token, revocación |

### 17.3 Índices de Rendimiento

- `glucose_readings(patient_id, measured_at DESC)`
- `meal_entries(patient_id, consumed_at DESC)`
- `vital_signs(patient_id, measured_at DESC)`
- `push_subscriptions(patient_id)`
- `audit_log(patient_id)`, `audit_log(entity_type, entity_id)`, `audit_log(performed_at DESC)`
- `foods` — índice de texto completo en `name`

---

## 18. Estrategia de Testing

### 18.1 Pirámide de Tests

| Nivel | Herramienta | Qué prueba |
|---|---|---|
| Unit Tests | JUnit 5 + Mockito | Use cases, Domain services, Calculators, PatternDetector |
| Architecture Tests | ArchUnit | Dependencias entre capas |
| Smoke Test | @SpringBootTest | Arranque del contexto completo |

### 18.2 Suites actuales (31 tests — 100% passing)

| Suite | Tests | Descripción |
|---|---|---|
| `MedicalCalculatorServiceTest` | 14 | IMC, HbA1c, TIR, CV, promedio, SD |
| `RegisterGlucoseReadingUseCaseTest` | 5 | Registro exitoso, paciente no encontrado, validaciones |
| `GetAlertsUseCaseTest` | 4 | Alertas de glucosa, nutrición, racha positiva |
| `ArchitectureTest` | 7 | Reglas de dependencias entre capas |
| `DiabecareApiApplicationTests` | 1 | Smoke test de arranque |

> **Nota**: los tests no se actualizaron en esta sesión para cubrir `LoginUseCase`, `RegisterUseCase`, `SuspendAccountUseCase`, `DeleteAccountUseCase`, `SystemConfigAdapter` ni el flujo de `MessageResolverPort`. Pendiente para una sesión futura de testing.

---

## 19. Dependencias Principales

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

## 20. Configuración y Despliegue

### 20.1 Perfiles de Spring

| Perfil | Configuración |
|---|---|
| `dev` | PostgreSQL local, logs DEBUG, Swagger habilitado |
| `prod` | PostgreSQL, logs INFO/WARN, Swagger deshabilitado |

### 20.2 Variables de Entorno Requeridas

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

> Los parámetros clínicos y de rate limiting **no** van aquí — viven en `system_config` (BD), gestionables vía API sin redeploy.

### 20.3 Docker Compose (Desarrollo)

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

*DiabeCare Backend Documentation v4.0*
