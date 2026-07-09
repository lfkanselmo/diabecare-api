# DiabeCare — Backend Architecture & Technical Documentation

> **Spring Boot 3.5 | Arquitectura Hexagonal | REST API**

| Campo | Valor |
|---|---|
| Versión de este documento | 5.0.0 |
| Tecnología | Java 21 + Spring Boot 3.5.14 |
| Arquitectura | Hexagonal + Clean Architecture |
| Base de datos | PostgreSQL 15+ |
| Documentación API | OpenAPI 3.0 / Swagger UI |
| Tests | 1059 tests, 180 clases, 0 fallos (`mvn test`) |

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
14. [Cuidadores (Acceso Compartido)](#14-cuidadores-acceso-compartido)
15. [Consentimiento y Cumplimiento (Habeas Data)](#15-consentimiento-y-cumplimiento-habeas-data)
16. [Catálogo de Alimentos y Búsqueda por Código de Barras](#16-catálogo-de-alimentos-y-búsqueda-por-código-de-barras)
17. [Internacionalización](#17-internacionalización)
18. [Estándares Técnicos y de Código](#18-estándares-técnicos-y-de-código)
19. [Base de Datos](#19-base-de-datos)
20. [Estrategia de Testing](#20-estrategia-de-testing)
21. [Dependencias Principales](#21-dependencias-principales)
22. [Configuración y Despliegue](#22-configuración-y-despliegue)
23. [Importación de Lecturas por Dispositivo (API Key)](#23-importación-de-lecturas-por-dispositivo-api-key)

---

## 1. Visión General

### 1.1 Propósito

DiabeCare es una aplicación web para pacientes diabéticos que permite registrar mediciones clínicas, controlar ingesta calórica, gestionar medicamentos, recibir alertas inteligentes, compartir su seguimiento con un cuidador de confianza, y generar reportes para consultas médicas.

### 1.2 Objetivos del Sistema

- Registro y monitoreo de glucosa con detección de patrones clínicos y perfil AGP (Ambulatory Glucose Profile) por hora
- Conteo de calorías y macronutrientes con 635 alimentos (colombianos, latinoamericanos e internacionales), con búsqueda por código de barras vía OpenFoodFacts
- Control de medicamentos con auditoría de cambios y recordatorios automáticos derivados de la frecuencia registrada
- Signos vitales: presión arterial, peso, IMC, HbA1c estimada
- Alertas clínicas inteligentes (7 tipos + 4 patrones + ciclo menstrual)
- Notificaciones push nativas (Web Push API), recordatorios de glucosa configurables por el paciente y resumen semanal automático
- Exportación de datos clínicos en CSV y JSON, y exportación completa de los propios datos de cuenta
- Rate limiting por paciente y por IP para proteger integridad de datos y frenar abuso de autenticación
- Parámetros clínicos y operacionales configurables sin redeploy (`system_config`)
- Gestión de cuenta por el propio usuario: suspensión, eliminación con purga definitiva diferida, y recuperación de contraseña por correo
- Sesiones multi-dispositivo con refresh tokens revocables
- Compartir el seguimiento con un cuidador en modo solo lectura, vía código de invitación
- Registro de consentimiento de tratamiento de datos personales (Ley 1581 de 2012 — Habeas Data, Colombia)
- Panel de administración básico: listado de usuarios y asignación de rol `ADMIN`

### 1.3 Stack Técnico

| Componente | Tecnología |
|---|---|
| Backend | Java 21 + Spring Boot 3.5.14 |
| Frontend | Angular (documento separado) |
| Base de datos | PostgreSQL 15+ |
| Seguridad | Spring Security 6 + JWT (jjwt 0.12.5) |
| Cache | Caffeine |
| Push | Web Push API + BouncyCastle VAPID |
| Correo transaccional | Resend (recuperación de contraseña) |
| Catálogo externo de alimentos | OpenFoodFacts (búsqueda por código de barras) |
| Rate Limiting | Bucket4j 8.10.1 + Caffeine |
| Mensajes | Spring `MessageSource` (español + inglés) |
| PDF | OpenPDF 3.0.5 |
| Documentación | OpenAPI 3.0 / Swagger UI |
| Tests de integración | Testcontainers 1.19.6 (PostgreSQL real) |
| Cobertura | JaCoCo 0.8.12 |

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
| **Infrastructure** | JPA, seguridad, push, correo, catálogo externo de alimentos, scheduler, rate limiting, PDF, mensajes |
| **Presentation** | Controllers REST, DTOs, validación de entrada, manejo de errores |

### 2.3 Estructura de Paquetes

```
com.diabecare
├── domain/
│   ├── model/              # Patient, User, GlucoseReading, MealEntry, VitalSign,
│   │                       #   Medication, ExerciseLog, MenstrualCycle, CycleDayEntry,
│   │                       #   CycleDaySymptomEntry, Alert, AuditLog, SystemConfig,
│   │                       #   RefreshToken, CaregiverInvite, CaregiverLink,
│   │                       #   PasswordResetToken, GlucoseReminder, AgpHourlyBucket,
│   │                       #   AccountExportData, ReportData, WeeklySummaryData,
│   │                       #   ExternalFoodInfo
│   ├── exception/          # DomainException + subclases por caso: InvalidRefreshTokenException,
│   │                       #   InvalidCaregiverInviteException, InvalidPasswordResetTokenException,
│   │                       #   InvalidRoleException, OpenCycleConflictException,
│   │                       #   UnauthorizedResourceAccessException, UserNotFoundException,
│   │                       #   PatientNotFoundException, RateLimitExceededException, etc.
│   └── service/             # MedicalCalculatorService, PatternDetectorService,
│                            #   WeeklySummaryService, GlucoseExportService, AuditService,
│                            #   RateLimitService, AgpProfileService, ExerciseLabelService,
│                            #   MenstrualCycleGuidanceService
├── application/
│   ├── port/in/             # ~55 casos de uso — autenticación, cuenta, cuidadores,
│   │                        #   recordatorios, ciclo menstrual, administración, glucosa,
│   │                        #   nutrición, medicamentos, signos vitales, ejercicio, reportes
│   ├── port/out/             # LoadPatientPort, SavePatientPort, LoadUserPort, SaveUserPort,
│   │                         #   AuthenticateUserPort, GenerateTokenPort, RefreshTokenPort,
│   │                         #   CaregiverInvitePort, PasswordResetTokenPort, SendEmailPort,
│   │                         #   MessageResolverPort, SystemConfigPort, NotifyPatientPort,
│   │                         #   LoadGlucoseReadingPort, SaveAuditLogPort
│   └── usecase/               # Implementaciones (1 clase por operación)
├── infrastructure/
│   ├── persistence/          # JPA entities, repositories, adapters, mappers
│   ├── security/              # JWT filter, UserDetailsServiceImpl, AuthenticateUserAdapter,
│   │                          #   GenerateTokenAdapter, RefreshTokenAdapter
│   │   └── handler/            # RestAuthenticationEntryPoint, RestAccessDeniedHandler
│   ├── config/                 # DiabeCareProperties, JwtProperties, RateLimitConfig,
│   │                           #   MessageSourceConfig, MessageResolverAdapter, CacheConfig
│   ├── food/                   # OpenFoodFactsAdapter (LookupFoodByBarcodeUseCase)
│   ├── mail/                    # ResendEmailAdapter (SendEmailPort)
│   ├── push/                   # PushNotificationService, PushNotificationAdapter
│   ├── pdf/                    # PdfReportAdapter, MedicalReportPdfGenerator (OpenPDF)
│   └── scheduler/               # WeeklySummaryScheduler, GlucoseReminderScheduler,
│                                #   MedicationReminderScheduler, AccountPurgeScheduler
└── presentation/
    ├── controller/             # 21 Controllers REST (ver sección 6)
    ├── dto/                     # Request/Response records
    ├── mapper/                  # MapStruct mappers
    ├── util/                     # DeviceLabelResolver
    └── advice/                   # GlobalExceptionHandler
```

### 2.4 Reglas de Arquitectura (ArchUnit)

Verificadas automáticamente en cada `mvn test` (14 reglas en `ArchitectureTest`):

- El dominio **no puede** importar clases de Spring, JPA o cualquier framework
- Los Use Cases solo pueden depender del dominio y de interfaces (puertos)
- Los Controllers no pueden acceder directamente a repositorios
- Las entidades JPA deben estar en `infrastructure.persistence.entity`
- Los adaptadores de infraestructura implementan puertos, nunca al revés

### 2.5 Flujo de Autenticación

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
      → RegisterUserUseCase.execute()   // persiste termsAccepted/termsVersion — ver sección 15
      → RegisterPatientUseCase.execute()
      → GenerateTokenPort.generateToken()
      → RefreshTokenPort.issue(userId, deviceLabel)
```

`AuthController` solo delega — construye el `Command`, llama al use case, mapea el `Result` a `AuthResponse`. Sin lógica de negocio. El `deviceLabel` (resuelto desde `User-Agent` por `DeviceLabelResolver`) se extrae en el controller y viaja en el `Command`.

**JWT**: incluye claim adicional `userId` (UUID), no solo `sub` (email). Esto permite que el frontend identifique al usuario sin un endpoint adicional — usado por `SuspendAccountUseCase`, `DeleteAccountUseCase`, `ExportAccountDataUseCase`, que operan sobre `userId`, no `patientId`.

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
| `targetGlucoseMin` / `targetGlucoseMax` | BigDecimal | Rango objetivo mg/dL |
| `dailyCalorieGoal` | Integer | Meta calórica diaria |
| `insulinSensitivityFactor` | BigDecimal | Factor de sensibilidad a insulina |
| `insulinToCarbRatio` | BigDecimal | Ratio insulina:carbohidratos |
| `targetGlucoseCorrection` | BigDecimal | Objetivo de corrección |

### 3.2 User

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | UUID | Identificador único |
| `email` | String | Email (anonimizado tras eliminación de cuenta) |
| `role` | String | `PATIENT`, `ADMIN` — asignable vía panel de administración |
| `enabled` | boolean | `false` si suspendida o eliminada |
| `suspendedAt` / `deletedAt` | LocalDateTime | Fechas de suspensión/eliminación, null si no aplica |
| `termsAcceptedAt` | LocalDateTime | Fecha de aceptación de la política de datos (nullable — cuentas anteriores a V20 no la tienen retroactivamente) |
| `termsVersion` | String | Versión de la política aceptada |
| `createdAt` | LocalDateTime | Fecha de creación |

Métodos: `isSuspended()`, `isDeleted()`.

### 3.3 SystemConfig

| Campo | Tipo | Descripción |
|---|---|---|
| `key` | String | Clave única (ej. `pattern.fasting_threshold_mgdl`) |
| `value` | String | Valor almacenado como texto |
| `dataType` | Enum | `INTEGER`, `DECIMAL`, `STRING`, `BOOLEAN` |
| `category` | Enum | `ALERTS`, `PATTERNS`, `RATE_LIMIT` |
| `description` | String | Descripción legible |
| `updatedAt` | LocalDateTime | Última actualización |

### 3.4 RefreshToken

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | UUID | Identificador único |
| `userId` | UUID | Referencia al usuario |
| `tokenHash` | String | Hash SHA-256 del token crudo |
| `deviceLabel` | String | Etiqueta legible del dispositivo, resuelta desde `User-Agent` |
| `lastUsedAt` / `expiresAt` / `revokedAt` | LocalDateTime | Ciclo de vida de la sesión |
| `createdAt` | LocalDateTime | Fecha de creación |

Métodos: `isExpired()`, `isRevoked()`, `isValid()`.

### 3.5 CaregiverInvite y CaregiverLink

| Entidad | Campos clave | Descripción |
|---|---|---|
| `CaregiverInvite` | `patientId`, `codeHash`, `expiresAt` (7 días), `redeemedAt`, `redeemedByUserId`, `revokedAt` | Código opaco de un solo uso generado por el paciente, hasheado con SHA-256 igual que el refresh token |
| `CaregiverLink` | `patientId`, `caregiverUserId`, `status` (`ACTIVE`/revocado), `revokedAt` | Relación de acceso ya activa entre paciente y cuidador |

### 3.6 PasswordResetToken

| Campo | Tipo | Descripción |
|---|---|---|
| `userId` | UUID | Usuario que solicitó el reseteo |
| `tokenHash` | String | Hash SHA-256 — el valor crudo solo viaja por correo |
| `expiresAt` | LocalDateTime | Vigencia de 1 hora desde la emisión |
| `usedAt` | LocalDateTime | No nulo una vez consumido — un token de reseteo es de un solo uso |

### 3.7 GlucoseReminder

| Campo | Tipo | Descripción |
|---|---|---|
| `patientId` | UUID | Paciente dueño del recordatorio |
| `reminderTime` | LocalTime | Hora del día configurada por el paciente |
| `label` | String | Etiqueta libre (ej. "Antes del desayuno") |
| `enabled` | boolean | Si está activo |

`GlucoseReminderScheduler` corre cada minuto y dispara notificación push a los recordatorios cuya hora coincide con la actual.

### 3.8 GlucoseReading

| Campo | Tipo | Descripción |
|---|---|---|
| `readingId` | UUID | ID único |
| `value` | BigDecimal | Valor en mg/dL o mmol/L |
| `unit` | Enum | `MG_DL`, `MMOL_L` |
| `readingType` | Enum | `FASTING`, `PRE_MEAL`, `POST_MEAL`, `BEDTIME`, `RANDOM` |
| `status` | Enum | `CRITICALLY_LOW`, `LOW`, `NORMAL`, `HIGH`, `CRITICALLY_HIGH` |
| `measuredAt` | LocalDateTime | Fecha y hora de medición |

### 3.9 AgpHourlyBucket

Representa un punto del perfil AGP (Ambulatory Glucose Profile): agrega todas las lecturas del paciente en un rango de fechas por hora del día, para mostrar mediana/percentiles y visualizar el patrón glucémico diario típico. Construido por `AgpProfileService` a partir de las lecturas del período consultado.

### 3.10 MenstrualCycle, CycleDayEntry y CycleDaySymptomEntry _(rediseñado, V16)_

El seguimiento pasó de un único registro por ciclo (con `cycleLengthDays`, `periodLengthDays`, `phase` y `symptoms` fijos) a un registro **día a día**:

| Entidad | Campos clave |
|---|---|
| `MenstrualCycle` | `startDate`, `endDate` (nullable mientras el ciclo está abierto) |
| `CycleDayEntry` | `cycleId`, `entryDate`, `flowIntensity`, `notes` — un registro por día del ciclo |
| `CycleDaySymptomEntry` | `dayEntryId`, `symptomCode`, `severity` — síntomas por día, no por ciclo completo |

`OpenCycleConflictException` evita que un paciente tenga dos ciclos abiertos simultáneamente. `alert.days_before_open_cycle_alert` (config) dispara una alerta si un ciclo lleva demasiados días sin cerrarse.

### 3.11 Alert

| Campo | Tipo | Descripción |
|---|---|---|
| `type` | Enum | `GLUCOSE_OUT_OF_RANGE`, `NO_GLUCOSE_RECORDED`, `HIGH_HBA1C_ESTIMATED`, `NO_MEAL_RECORDED`, `POSITIVE_STREAK`, `GLUCOSE_AVERAGE_HIGH`, `GLUCOSE_PATTERN_DETECTED` |
| `severity` | Enum | `SUCCESS`, `INFO`, `WARNING`, `DANGER` |
| `title` / `message` | String | Resueltos vía `MessageResolverPort` |

### 3.12 AuditLog

| Campo | Tipo | Descripción |
|---|---|---|
| `entityType` | String | `PATIENT`, `MEDICATION` |
| `action` | Enum | `CREATE`, `UPDATE`, `DELETE` |
| `fieldName` / `oldValue` / `newValue` | String | Detalle del cambio |
| `performedAt` | LocalDateTime | Fecha y hora del cambio |

### 3.13 AccountExportData

Record que agrega todo lo que `ExportAccountDataUseCase` recopila para el endpoint de exportación de datos del paciente: perfil, lecturas de glucosa, comidas, signos vitales, medicamentos, ejercicio y ciclo menstrual — pensado como respuesta a solicitudes de portabilidad de datos.

---

## 4. Cálculos Médicos

`MedicalCalculatorService` encapsula todas las fórmulas clínicas. Son estándares médicos y **no son parametrizables** (a diferencia de los umbrales de alerta).

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

Cada método retorna `Optional<Alert>`. Inyecta `SystemConfigPort` — todos los umbrales vienen de `system_config`, ninguno está hardcodeado en código.

### 5.2 WeeklySummaryService

- `buildSummary(patient, readings)` → `Optional<WeeklySummaryData>`
- `buildPushTitle()` / `buildPushMessage(data)` → resuelven vía `MessageResolverPort`

### 5.3 GlucoseExportService

- `toCsv(readings)` → String con BOM UTF-8 (compatible con Excel)
- `toJson(readings)` → String JSON

### 5.4 AuditService

- `buildCreateLog` / `buildUpdateLog` / `buildDeleteLog` → construyen entradas de `AuditLog`

### 5.5 RateLimitService

Verifica límites por paciente/IP y operación usando Bucket4j:

```java
rateLimitService.checkGlucoseLimit(patientId);
rateLimitService.checkMealLimit(patientId);
rateLimitService.checkExerciseLimit(patientId);
rateLimitService.checkLoginLimit(clientIp);
rateLimitService.checkRegisterLimit(clientIp);
rateLimitService.checkForgotPasswordLimit(clientIp);
```

Cada método construye el bucket con el límite leído de `SystemConfigPort`. Los use cases no conocen Bucket4j ni `RateLimitConfig`.

### 5.6 AgpProfileService

`buildHourlyProfile(readings)` agrupa las lecturas por hora del día y produce la lista de `AgpHourlyBucket` que consume `GetAgpProfileUseCase` para el perfil ambulatorio de glucosa.

### 5.7 ExerciseLabelService y MenstrualCycleGuidanceService

Servicios de soporte usados por `MedicalReportPdfGenerator` para traducir enums (`ExerciseType`, `ExerciseIntensity`, fases del ciclo) a texto legible dentro del reporte médico en PDF, manteniendo esa lógica de presentación fuera del generador de PDF propiamente dicho.

---

## 6. API REST — Endpoints

### Autenticación
```
POST   /api/v1/auth/register
POST   /api/v1/auth/login
POST   /api/v1/auth/refresh
POST   /api/v1/auth/logout
POST   /api/v1/auth/logout-all
POST   /api/v1/auth/forgot-password
POST   /api/v1/auth/reset-password
GET    /api/v1/auth/sessions/{userId}
```

### Cuenta de usuario
```
PATCH  /api/v1/account/{userId}/suspend
DELETE /api/v1/account/{userId}
GET    /api/v1/account/{userId}/export
```

### Panel de administración _(requiere rol `ADMIN`)_
```
GET    /api/v1/admin/users
PATCH  /api/v1/admin/users/{userId}/role
```

### Cuidadores
```
POST   /api/v1/caregivers/{patientId}/invites
GET    /api/v1/caregivers/{patientId}/links
DELETE /api/v1/caregivers/{patientId}/links/{linkId}
POST   /api/v1/caregivers/redeem
GET    /api/v1/caregivers/my-patients
```

### Configuración del sistema
```
GET    /api/v1/system-config
POST   /api/v1/system-config/reload
```

### Glucosa
```
POST   /api/v1/glucose/{patientId}
GET    /api/v1/glucose/{patientId}/history?from=&to=
GET    /api/v1/glucose/{patientId}/stats?from=&to=
GET    /api/v1/glucose/{patientId}/latest
GET    /api/v1/glucose/{patientId}/agp-profile?from=&to=
DELETE /api/v1/glucose/{patientId}/{readingId}
GET    /api/v1/glucose/{patientId}/export/csv?from=&to=
GET    /api/v1/glucose/{patientId}/export/json?from=&to=
```

### Recordatorios de glucosa
```
GET    /api/v1/glucose-reminders/{patientId}
POST   /api/v1/glucose-reminders/{patientId}
PATCH  /api/v1/glucose-reminders/{patientId}/{reminderId}
DELETE /api/v1/glucose-reminders/{patientId}/{reminderId}
```

### Nutrición
```
POST   /api/v1/nutrition/{patientId}/meals
GET    /api/v1/nutrition/{patientId}/summary?date=
GET    /api/v1/nutrition/{patientId}/meals?from=&to=
GET    /api/v1/foods/search?query=
GET    /api/v1/foods/category/{category}
GET    /api/v1/food-lookup/barcode/{barcode}
```

### Signos vitales
```
POST   /api/v1/vitals/{patientId}
GET    /api/v1/vitals/{patientId}
GET    /api/v1/vitals/{patientId}/latest
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
POST   /api/v1/menstrual-cycle/{patientId}/finish-period
POST   /api/v1/menstrual-cycle/{patientId}/days
GET    /api/v1/menstrual-cycle/{patientId}/status
GET    /api/v1/menstrual-cycle/{patientId}/phase-calendar
```

### Paciente
```
GET    /api/v1/patients/{patientId}
PUT    /api/v1/patients/{patientId}
PATCH  /api/v1/patients/{patientId}/insulin-profile
```

### Reportes
```
GET    /api/v1/reports/{patientId}/medical?from=&to=
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
GET    /api/v1/metadata/glucose-statuses
GET    /api/v1/metadata/meal-types
GET    /api/v1/metadata/exercise-types
GET    /api/v1/metadata/exercise-intensities
GET    /api/v1/metadata/medication-types
GET    /api/v1/metadata/medication-frequencies
GET    /api/v1/metadata/dose-units
GET    /api/v1/metadata/activity-levels
GET    /api/v1/metadata/diabetes-types
GET    /api/v1/metadata/cycle-symptoms
GET    /api/v1/metadata/cycle-phases
GET    /api/v1/metadata/flow-intensities
GET    /api/v1/metadata/symptom-severities
```

---

## 7. Seguridad y Autenticación

### 7.1 JWT (access token)

- Access token: 15 minutos (`JWT_ACCESS_EXPIRY_MS=900000`)
- Algoritmo: HMAC-SHA256
- Cabecera: `Authorization: Bearer <token>`
- **Claims**: `sub` (email) + `userId` (UUID)

### 7.2 Flujo de Login

```
AuthController.login()
  → LoginUseCase.execute(Command(email, password, deviceLabel))
      → AuthenticateUserPort.authenticate()
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

`/api/v1/auth/refresh`, `/forgot-password` y `/reset-password` son necesariamente públicos. La validación real ocurre dentro de `redeem()` (refresh token) y `PasswordResetTokenPort` (reseteo), no a nivel de `SecurityConfig`.

### 7.4 Protección de datos

- Contraseñas: BCrypt con strength configurable (`BCRYPT_STRENGTH=12`)
- CORS: orígenes configurables via `CORS_ALLOWED_ORIGINS`
- Sesión: `STATELESS` — sin cookies ni sesiones del servidor
- Refresh tokens, códigos de invitación de cuidador y tokens de reseteo de contraseña: mismo patrón — valor aleatorio, hasheado con SHA-256 antes de persistir, el valor crudo nunca se guarda en BD

### 7.5 Manejo de errores de autenticación

| Excepción / caso | Código | Status | Descripción |
|---|---|---|---|
| `DisabledException` | `ACCOUNT_SUSPENDED` | 403 | Cuenta suspendida o eliminada (`enabled=false`) |
| `BadCredentialsException` | `INVALID_CREDENTIALS` | 401 | Email o contraseña incorrectos |
| `InvalidRefreshTokenException` | `INVALID_REFRESH_TOKEN` | 401 | Refresh token inexistente, ya usado, expirado, o usuario suspendido/eliminado |
| `InvalidPasswordResetTokenException` | `INVALID_RESET_TOKEN` | 400 | Token de reseteo inexistente, ya usado o expirado (vigencia 1 hora) |
| `InvalidCaregiverInviteException` | `INVALID_CAREGIVER_INVITE` | 400 | Código de invitación inexistente, ya canjeado, revocado o expirado (vigencia 7 días) |
| `InvalidRoleException` | `INVALID_ROLE` | 400 | Rol inexistente al intentar asignarlo desde el panel de administración |
| `UnauthorizedResourceAccessException` | `ACCESS_DENIED` | 403 | Un cuidador intenta acceder a un paciente sobre el que no tiene un `CaregiverLink` activo |
| Sin autenticación válida en ruta protegida | `SESSION_EXPIRED` | 401 | Manejado por `RestAuthenticationEntryPoint` |
| Autenticado pero sin permiso | `ACCESS_DENIED` | 403 | Manejado por `RestAccessDeniedHandler` |

```json
{
  "timestamp": "2026-07-08T20:00:00",
  "status": 403,
  "error": "Forbidden",
  "code": "ACCOUNT_SUSPENDED",
  "message": "Tu cuenta está suspendida. Contacta soporte para reactivarla.",
  "path": "/api/v1/auth/login"
}
```

Otros códigos: `PATIENT_NOT_FOUND`, `GLUCOSE_READING_NOT_FOUND`, `DOMAIN_VALIDATION_ERROR`, `VALIDATION_ERROR`, `RATE_LIMIT_EXCEEDED`, `INTERNAL_ERROR`.

### 7.6 Semántica 401 vs 403

Spring Security en modo `STATELESS`, sin un `AuthenticationEntryPoint` explícito, devuelve 403 tanto para "no autenticado" como para "autenticado sin permiso". Se separan ambos casos con:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .exceptionHandling(exceptions -> exceptions
            .authenticationEntryPoint(restAuthenticationEntryPoint)  // 401
            .accessDeniedHandler(restAccessDeniedHandler)            // 403
        )
        .build();
}
```

`RestAuthenticationEntryPoint` y `RestAccessDeniedHandler` serializan un `ApiError` manualmente con un `ObjectMapper` inyectado, porque operan en el `ExceptionTranslationFilter` de Spring Security, **antes** del `DispatcherServlet` — `@RestControllerAdvice` no los intercepta.

### 7.7 Refresh Tokens — diseño

#### Por qué opaco y no JWT autocontenido

| Enfoque | Revocable antes de expirar | Detecta robo |
|---|---|---|
| **Elegido**: valor aleatorio, hash SHA-256 en BD | Sí | Sí (reuso de un token ya rotado) |
| Descartado: JWT largo autocontenido | No | No |

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

`RefreshTokenAdapter`: `issue()` genera 64 bytes aleatorios (`SecureRandom`), Base64 URL-safe, hashea con SHA-256 y persiste solo el hash. `redeem()` valida y en la misma operación marca revocado (rotación) antes de devolver `userId` + `deviceLabel`.

#### Flujo de refresco

```
POST /api/v1/auth/refresh { refreshToken }
  → RefreshAccessTokenUseCaseImpl
      → RefreshTokenPort.redeem(rawToken)
      → LoadUserPort.findById(userId)   // valida !suspended && !deleted && enabled
      → GenerateTokenPort.generateToken()
      → RefreshTokenPort.issue(userId, deviceLabel)
  ← Result(accessToken, expiresIn, refreshToken, refreshExpiresIn)
```

#### Logout: sesión actual vs todos los dispositivos

| Caso de uso | Recibe | Revoca | Endpoint |
|---|---|---|---|
| `LogoutCurrentSessionUseCaseImpl` | `refreshToken` | Solo ese token | `POST /auth/logout` |
| `LogoutAllSessionsUseCaseImpl` | `userId` | Todos los activos del usuario | `POST /auth/logout-all` |

`SuspendAccountUseCaseImpl` y `DeleteAccountUseCaseImpl` llaman `RefreshTokenPort.revokeAllForUser()` directamente.

#### Sesiones activas y `deviceLabel`

`AuthController` extrae `User-Agent` y llama `DeviceLabelResolver.resolve(...)` antes de construir el `Command`. `GET /api/v1/auth/sessions/{userId}` expone `findActiveSessions()` — sesiones no expiradas y no revocadas, ordenadas por `last_used_at DESC`.

### 7.8 Recuperación de contraseña

```
POST /api/v1/auth/forgot-password { email }
  → RequestPasswordResetUseCaseImpl
      → RateLimitService.checkForgotPasswordLimit(clientIp)
      → LoadUserPort.findUserIdByEmail(email)   // si no existe, retorna igual sin error
      → PasswordResetTokenPort.issue(userId, expiresAt = now + 1h)
      → SendEmailPort.sendPasswordResetEmail(email, rawToken)   // Resend; error se loguea, no se propaga

POST /api/v1/auth/reset-password { token, newPassword }
  → ResetPasswordUseCaseImpl
      → PasswordResetTokenPort.redeem(rawToken)   // valida no usado, no expirado; marca usedAt
      → SaveUserPort.updatePassword(userId, newPasswordHash)
```

**Anti-enumeración de cuentas**: `forgot-password` siempre responde igual, exista o no la cuenta con ese email — nunca revela si un correo está registrado. El envío de correo se envuelve en un `try/catch` silencioso por la misma razón: una falla de Resend no debe traducirse en una respuesta distinta que delate la existencia de la cuenta.

### 7.9 Rol y panel de administración

`ChangeUserRoleUseCaseImpl` valida el rol destino contra los valores permitidos (`PATIENT`, `ADMIN`) antes de persistir — un rol desconocido lanza `InvalidRoleException` (400). `AdminController` está anotado con `@PreAuthorize("hasRole('ADMIN')")` a nivel de clase: solo un usuario con `role=ADMIN` puede listar usuarios o reasignar roles.

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

### 8.3 Consumidores del canal push

Además del resumen semanal, dos schedulers adicionales usan el mismo canal push: `GlucoseReminderScheduler` (recordatorios de medición configurados por el paciente) y `MedicationReminderScheduler` (recordatorios derivados de la frecuencia de cada medicamento activo).

---

## 9. Tareas Programadas

| Scheduler | Cron | Descripción |
|---|---|---|
| `WeeklySummaryScheduler` | `0 0 8 * * MON` (America/Bogota) | Resumen semanal por paciente vía push |
| `GlucoseReminderScheduler` | `0 * * * * *` (cada minuto) | Dispara notificación a los recordatorios de glucosa cuya hora coincide |
| `MedicationReminderScheduler` | `0 * * * * *` (cada minuto) | Recordatorios de medicamento derivados de su frecuencia |
| `AccountPurgeScheduler` | `0 0 3 * * *` (diario, 3am) | Purga definitiva de cuentas eliminadas hace más de 30 días (`PurgeExpiredDeletedAccountsUseCase`) |

Cada scheduler solo dispara — la orquestación real vive en un use case (`SendWeeklySummaryUseCaseImpl`, `SendGlucoseReminderNotificationsUseCaseImpl`, `SendMedicationReminderNotificationsUseCaseImpl`, `PurgeExpiredDeletedAccountsUseCaseImpl`), respetando SRP en cada capa.

---

## 10. Rate Limiting

Implementado con **Bucket4j** + **Caffeine Cache**. Límites configurables vía `system_config`:

| Operación | Config key | Default |
|---|---|---|
| Registro de glucosa | `rate_limit.glucose_per_hour` | 20 |
| Registro de comidas | `rate_limit.meal_per_hour` | 15 |
| Registro de ejercicio | `rate_limit.exercise_per_hour` | 10 |
| Login (por IP) | `rate_limit.login_per_hour` | 10 |
| Registro de cuenta (por IP) | `rate_limit.register_per_hour` | 5 |
| Recuperación de contraseña (por IP) | `rate_limit.forgot_password_per_hour` | 5 |

Al exceder el límite: `HTTP 429 Too Many Requests` con código `RATE_LIMIT_EXCEEDED`.

**Arquitectura**: `RateLimitConfig` solo expone el `@Bean` del `Cache<UUID, Bucket>` / `Cache<String, Bucket>` — la creación de buckets vive en `RateLimitService` (dominio), que lee el límite de `SystemConfigPort` y construye el `Bucket` dinámicamente.

---

## 11. Auditoría

Registra cambios en perfil del paciente y medicamentos en la tabla `audit_log`, expuesta vía `GET /api/v1/audit/{patientId}` y `GET /api/v1/audit/{patientId}/{entityType}`.

---

## 12. Configuración Parametrizable (`system_config`)

### 12.1 Motivación

Los umbrales clínicos (alertas, patrones) y operacionales (rate limiting) viven en una tabla en BD en vez de en código o `application.yaml`. Cualquier ajuste no requiere redeploy.

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

### 12.3 Parámetros (20 totales)

| Categoría | Key | Default | Descripción |
|---|---|---|---|
| ALERTS | `alert.hours_without_glucose` | 8 | Horas sin glucosa para alerta |
| ALERTS | `alert.hba1c_threshold` | 8.0 | HbA1c estimada que dispara alerta |
| ALERTS | `alert.good_tir_threshold` | 70.0 | TIR mínimo para racha positiva |
| ALERTS | `alert.streak_days` | 7 | Días para evaluar racha positiva |
| ALERTS | `alert.min_readings_for_stats` | 3 | Mínimo lecturas para estadísticas |
| ALERTS | `alert.days_before_open_cycle_alert` | 10 | Días sin cerrar un período para alertar al paciente |
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
| RATE_LIMIT | `rate_limit.login_per_hour` | 10 | Máx intentos de login por hora por IP |
| RATE_LIMIT | `rate_limit.register_per_hour` | 5 | Máx registros de cuenta por hora por IP |
| RATE_LIMIT | `rate_limit.forgot_password_per_hour` | 5 | Máx solicitudes de recuperación de contraseña por hora por IP |

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
GET  /api/v1/system-config           # Lista los 20 parámetros con su descripción
POST /api/v1/system-config/reload    # Recarga la caché desde BD sin redeploy
```

### 12.6 Decisión: qué NO se parametrizó

- Fórmulas médicas (ADAG, IMC) — son estándares internacionales, no deben cambiar
- Claves JWT/VAPID, BCrypt strength, credenciales de Resend — secretos de seguridad, van en variables de entorno
- CORS — configuración de infraestructura

---

## 13. Gestión de Cuenta

### 13.1 Modelo

```sql
ALTER TABLE users ADD COLUMN suspended_at TIMESTAMP;
ALTER TABLE users ADD COLUMN deleted_at   TIMESTAMP;
```

### 13.2 Flujo de Suspensión

```
PATCH /api/v1/account/{userId}/suspend
  → SuspendAccountUseCaseImpl
      → LoadUserPort.findById(userId)
      → valida: !user.isDeleted() && !user.isSuspended()
      → SaveUserPort.suspend(user)   // enabled=false, suspended_at=now()
      → RefreshTokenPort.revokeAllForUser(userId)
```

### 13.3 Flujo de Eliminación y Purga Diferida

```
DELETE /api/v1/account/{userId}
  → DeleteAccountUseCaseImpl
      → LoadUserPort.findById(userId)
      → valida: !user.isDeleted()
      → SaveUserPort.delete(user)
          // enabled=false, deleted_at=now()
          // email anonimizado: deleted_{userId}@diabecare.deleted
      → RefreshTokenPort.revokeAllForUser(userId)
```

La eliminación **no borra filas de la base de datos** — solo anonimiza y desactiva. `AccountPurgeScheduler` corre a diario y, tras un período de gracia de **30 días** (`GRACE_PERIOD_DAYS`), `PurgeExpiredDeletedAccountsUseCase` sí elimina definitivamente las cuentas cuyo `deleted_at` superó ese umbral. Este diseño en dos pasos permite deshacer una eliminación accidental o dar tiempo a soporte antes de un borrado irreversible.

### 13.4 Exportación de datos

```
GET /api/v1/account/{userId}/export
  → ExportAccountDataUseCaseImpl
      → recopila perfil, glucosa, comidas, signos vitales, medicamentos, ejercicio y ciclo menstrual
  ← AccountExportData
```

### 13.5 Efecto en autenticación

Una cuenta con `enabled=false` (suspendida o eliminada) no puede iniciar sesión — Spring Security lanza `DisabledException`, traducida a `403 ACCOUNT_SUSPENDED`.

---

## 14. Cuidadores (Acceso Compartido)

### 14.1 Motivación

Permitir que un paciente comparta su seguimiento con un familiar o cuidador, en modo **estrictamente solo lectura**, sin exponer credenciales propias ni requerir que ambos compartan la misma cuenta.

### 14.2 Modelo

```sql
CREATE TABLE caregiver_invites (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id           UUID NOT NULL REFERENCES patients(id),
    code_hash            VARCHAR(255) NOT NULL UNIQUE,
    expires_at           TIMESTAMP NOT NULL,
    redeemed_at          TIMESTAMP,
    redeemed_by_user_id  UUID REFERENCES users(id),
    revoked_at           TIMESTAMP,
    created_at           TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE caregiver_links (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id         UUID NOT NULL REFERENCES patients(id),
    caregiver_user_id  UUID NOT NULL REFERENCES users(id),
    status             VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at         TIMESTAMP NOT NULL DEFAULT NOW(),
    revoked_at         TIMESTAMP,
    UNIQUE (patient_id, caregiver_user_id)
);
```

### 14.3 Flujo

```
POST /api/v1/caregivers/{patientId}/invites
  → CreateCaregiverInviteUseCaseImpl
      → CaregiverInvitePort.issue(patientId, expiresAt = now + 7 días)
  ← { rawCode, expiresAt }   // el código crudo se muestra una sola vez, nunca se persiste

POST /api/v1/caregivers/redeem { code }
  → RedeemCaregiverInviteUseCaseImpl
      → CaregiverInvitePort.redeem(code)   // valida no revocado/expirado/ya canjeado
      → crea CaregiverLink(status=ACTIVE)

GET /api/v1/caregivers/my-patients
  → ListPatientsICareForUseCaseImpl
      → lista los CaregiverLink activos del usuario autenticado como cuidador
```

Igual que el refresh token, el código de invitación es un valor aleatorio hasheado con SHA-256 antes de persistirse — solo el paciente ve el código crudo, y solo una vez.

### 14.4 Control de acceso

Cuando un cuidador consulta datos de un paciente, el use case correspondiente valida que exista un `CaregiverLink` activo entre ambos antes de responder; si no existe, lanza `UnauthorizedResourceAccessException` (403 `ACCESS_DENIED`). El acceso es de solo lectura: no hay endpoints de escritura accesibles a un cuidador sobre datos de otro paciente.

---

## 15. Consentimiento y Cumplimiento (Habeas Data)

### 15.1 Motivación

Colombia regula el tratamiento de datos personales — incluyendo datos de salud, categoría especialmente sensible — bajo la **Ley 1581 de 2012 (Habeas Data)**. El registro debe dejar constancia de que el titular aceptó la política de tratamiento de datos antes de que su información sea procesada.

### 15.2 Modelo

```sql
ALTER TABLE users ADD COLUMN terms_accepted_at TIMESTAMP;
ALTER TABLE users ADD COLUMN terms_version VARCHAR(20);
```

Ambas columnas son **nullable**: los usuarios registrados antes de esta migración nunca aceptaron los términos bajo este mecanismo formal, y no se los fuerza a hacerlo retroactivamente ni se les bloquea el acceso por no tenerlo.

### 15.3 Flujo

`RegisterUserUseCaseImpl` recibe `termsVersion` como parte del `Command` de registro y persiste `termsAcceptedAt = now()` junto con la versión aceptada — la aceptación queda ligada al momento exacto del registro, no a un checkbox suelto sin trazabilidad.

---

## 16. Catálogo de Alimentos y Búsqueda por Código de Barras

### 16.1 Catálogo local

La tabla `foods` tiene **635 registros** (colombianos, latinoamericanos e internacionales), repartidos en tres migraciones: V4 (172, base colombiana), V12 (+315, total 487: `FRUTOS_SECOS`, `EMBUTIDOS`, `CONDIMENTOS`, `COMIDA_RAPIDA`, `PANADERIA`) y V13 (+148, total 635: `VEGANOS`, `INDUSTRIALES`, ampliación de `COMIDA_RAPIDA`/`PREPARADOS` con comida de calle colombiana). `category` es `VARCHAR` libre tanto en dominio como en presentación, así que agregar categorías nuevas nunca requirió cambios de esquema.

### 16.2 Búsqueda por código de barras (OpenFoodFacts)

```
GET /api/v1/food-lookup/barcode/{barcode}
  → LookupFoodByBarcodeUseCaseImpl
      → OpenFoodFactsAdapter (infrastructure/food)   // consulta la API pública de OpenFoodFacts
  ← ExternalFoodInfo   // nombre, macros por 100g, si se encontró
```

Complementa el catálogo local: si un producto industrial empacado no está en los 635 alimentos ya cargados, el paciente puede escanear su código de barras y obtener la información nutricional directamente de OpenFoodFacts sin depender de que el equipo de DiabeCare lo haya precargado.

---

## 17. Internacionalización

### 17.1 Estado actual

Existen dos bundles de mensajes:

```
src/main/resources/messages.properties      # Español (default)
src/main/resources/messages_en.properties   # Inglés
```

Los mensajes de alertas, patrones y resumen semanal se resuelven vía `MessageResolverPort` → `MessageResolverAdapter`:

```java
MessageResolverPort (application/port/out)
  → MessageResolverAdapter (infrastructure/config)
      - usa Spring's MessageSource (ResourceBundleMessageSource)
      - resolve(key, args...) → MessageSource.getMessage(key, args, LocaleContextHolder.getLocale())
```

### 17.2 Resolución automática de idioma

No hay un `LocaleResolver` custom configurado en `MessageSourceConfig` ni en ningún otro `@Configuration`. Spring Boot aplica su autoconfiguración por defecto (`AcceptHeaderLocaleResolver`), que deriva el `Locale` del header HTTP `Accept-Language` de cada request. En la práctica esto significa que **el bundle en inglés ya es funcional sin trabajo adicional**: un cliente que envíe `Accept-Language: en` recibe los mensajes de `messages_en.properties` automáticamente.

### 17.3 Ejemplo de mensaje con interpolación

```properties
alert.pattern.fasting-high.message={0} de tus últimas {1} lecturas de ayuno superaron {2} mg/dL. Considera ajustar tu insulina basal o consultar a tu médico.
```

> **Nota técnica**: `MessageSource` usa `MessageFormat` (`{0}`, `{1}`), no `String.format` (`%d`, `%.0f`). Para enteros sin decimales se usa `{0,number,#}`.

### 17.4 Servicios que resuelven mensajes vía este mecanismo

`PatternDetectorService`, `GetAlertsUseCaseImpl`, `WeeklySummaryService` — sin strings de negocio hardcodeados.

---

## 18. Estándares Técnicos y de Código

### 18.1 Convenciones de Nomenclatura

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

### 18.2 Principios SOLID

- **S** — Un use case por operación
- **O** — Nuevas funcionalidades = nuevos use cases, no modificar existentes
- **L** — Los puertos son contratos; cualquier implementación debe cumplirlos
- **I** — Puertos granulares: `LoadPatientPort`, `SavePatientPort`, `LoadUserPort`, `SaveUserPort` separados
- **D** — Use Cases dependen de interfaces, nunca de implementaciones concretas

### 18.3 Reglas de código

- Records Java para DTOs inmutables
- `@Builder` + `@Getter` en entidades de dominio (nunca setters públicos)
- Factory methods con validación: `GlucoseReading.create(...)`, `Patient.create(...)`
- Métodos privados atómicos en use cases complejos
- `Optional<T>` para retornos que pueden ser vacíos
- Ningún use case importa clases de `infrastructure.*` directamente — siempre vía puerto (regla verificada con ArchUnit)

---

## 19. Base de Datos

### 19.1 Migraciones Flyway

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
| V12 | `V12__expand_foods.sql` | +315 alimentos (total 487) |
| V13 | `V13__expand_vegan_and_industrial_foods.sql` | +148 alimentos (total 635) |
| V15 | `V15__refresh_tokens.sql` | Tabla refresh_tokens |
| V16 | `V16__redesign_menstrual_cycle_tracking.sql` | cycle_day_entries, cycle_day_symptoms; elimina columnas fijas de MenstrualCycle |
| V17 | `V17__add_open_cycle_alert_config.sql` | Parámetro alert.days_before_open_cycle_alert |
| V18 | `V18__add_auth_rate_limits.sql` | Parámetros rate_limit.login_per_hour, rate_limit.register_per_hour |
| V19 | `V19__caregiver_sharing.sql` | caregiver_invites, caregiver_links |
| V20 | `V20__consent_tracking.sql` | users.terms_accepted_at, users.terms_version |
| V21 | `V21__password_reset_tokens.sql` | Tabla password_reset_tokens |
| V22 | `V22__add_forgot_password_rate_limit.sql` | Parámetro rate_limit.forgot_password_per_hour |
| V23 | `V23__glucose_reminders.sql` | Tabla glucose_reminders |

> **Nota sobre la numeración**: no existe `V14`. Se generó una migración adicional de alimentos como `V14` y se fusionó dentro de `V13` antes de aplicarse ninguna de las dos. Flyway no exige numeración consecutiva, solo orden estrictamente creciente.

### 19.2 Tablas Principales

| Tabla | Descripción |
|---|---|
| `users` | Credenciales, `enabled`, `suspended_at`, `deleted_at`, `terms_accepted_at`, `terms_version` |
| `patients` | Perfil médico del paciente |
| `glucose_readings` | Lecturas de glucosa |
| `meal_entries` + `meal_items` | Comidas y alimentos individuales |
| `foods` | 635 alimentos con macros |
| `vital_signs` | Peso, presión, FC, HbA1c medida |
| `medications` | Medicamentos activos |
| `exercise_logs` | Registros de actividad física |
| `menstrual_cycles` + `cycle_day_entries` + `cycle_day_symptoms` | Ciclos menstruales, registro día a día |
| `push_subscriptions` | Suscripciones Web Push por paciente |
| `audit_log` | Historial de cambios auditables |
| `system_config` | Parámetros clínicos y operacionales (20) |
| `refresh_tokens` | Sesiones activas por dispositivo |
| `caregiver_invites` + `caregiver_links` | Invitaciones y accesos activos de cuidadores |
| `password_reset_tokens` | Tokens de recuperación de contraseña (vigencia 1h, un solo uso) |
| `glucose_reminders` | Horarios configurables de recordatorio de medición |

### 19.3 Índices de Rendimiento

- `glucose_readings(patient_id, measured_at DESC)`
- `meal_entries(patient_id, consumed_at DESC)`
- `vital_signs(patient_id, measured_at DESC)`
- `push_subscriptions(patient_id)`
- `audit_log(patient_id)`, `audit_log(entity_type, entity_id)`, `audit_log(performed_at DESC)`
- `cycle_day_entries(patient_id, entry_date)`, `cycle_day_entries(cycle_id)`
- `caregiver_invites(patient_id)`, `caregiver_invites(code_hash)`
- `caregiver_links(patient_id)`, `caregiver_links(caregiver_user_id)`
- `password_reset_tokens(user_id)`, `password_reset_tokens(token_hash)`
- `glucose_reminders(patient_id)`, índice parcial en `glucose_reminders(reminder_time) WHERE enabled`
- `foods` — índice de texto completo en `name`

---

## 20. Estrategia de Testing

### 20.1 Pirámide de Tests

| Nivel | Herramienta | Qué prueba |
|---|---|---|
| Unit Tests | JUnit 5 + Mockito + AssertJ | Use cases, Domain services, Calculators, PatternDetector |
| Integration Tests | Testcontainers (PostgreSQL real) | Adaptadores de persistencia contra una BD real, no H2 |
| Architecture Tests | ArchUnit | Dependencias entre capas (14 reglas) |
| Smoke Test | `@SpringBootTest` | Arranque del contexto completo |

### 20.2 Estado actual: 1059 tests, 180 clases, 0 fallos

Verificado con `mvn test` (Surefire + JaCoCo instrumentando la ejecución): **1059 tests ejecutados en 180 clases de test, 0 fallos, 0 errores, 0 omitidos** — número tomado del resumen agregado que imprime Maven al final de la ejecución (`Tests run: 1059, Failures: 0, Errors: 0, Skipped: 0`). Cobertura visible en `target/site/jacoco/index.html` tras `mvn verify`.

> Nota de diagnóstico: el atributo `tests` de los XML individuales en `target/surefire-reports/TEST-*.xml` subestima el total (suma 1006) para clases que agrupan tests dentro de `@Nested` — no lo uses como fuente; el conteo agregado que imprime Maven en consola es el confiable.

---

## 21. Dependencias Principales

| Dependencia | Versión | Propósito |
|---|---|---|
| `spring-boot-starter-web` | 3.5.14 | REST API |
| `spring-boot-starter-security` | 3.5.14 | Autenticación |
| `spring-boot-starter-data-jpa` | 3.5.14 | Persistencia |
| `spring-boot-starter-validation` | 3.5.14 | Bean Validation |
| `spring-boot-starter-cache` | 3.5.14 | Abstracción de caché |
| `spring-boot-starter-actuator` | 3.5.14 | Health/info/metrics |
| `jjwt-api` + `jjwt-impl` + `jjwt-jackson` | 0.12.5 | JWT |
| `postgresql` | (driver JDBC, gestionado por Spring Boot BOM) | Driver JDBC |
| `h2` | (gestionado por Spring Boot BOM) | Solo para tests que no usan Testcontainers |
| `flyway-core` + `flyway-database-postgresql` | 11.x | Migraciones |
| `mapstruct` | 1.5.5.Final | Mapeo entre capas |
| `lombok` | 1.18.30 | Boilerplate |
| `springdoc-openapi-starter-webmvc-ui` | 2.8.9 | Swagger UI |
| `openpdf` | 3.0.5 | Generación PDF (reemplaza a iText) |
| `caffeine` | (gestionado por Spring Boot BOM) | Cache en memoria |
| `web-push` | 5.1.1 | Notificaciones push |
| `bcprov-jdk15on` | 1.70 | BouncyCastle (VAPID) |
| `bucket4j-core` | 8.10.1 | Rate limiting |
| `archunit-junit5` | 1.2.1 | Tests de arquitectura |
| `testcontainers` (`junit-jupiter`, `postgresql`) + `spring-boot-testcontainers` | 1.19.6 | Tests de integración contra PostgreSQL real |
| `jacoco-maven-plugin` | 0.8.12 | Cobertura de tests |
| `maven-failsafe-plugin` | (gestionado por Spring Boot parent) | Ejecución de integration tests (`*IT`) separada de unit tests |

---

## 22. Configuración y Despliegue

### 22.1 Perfiles de Spring

| Perfil | Configuración |
|---|---|
| `dev` | PostgreSQL local, logs DEBUG, Swagger habilitado |
| `prod` | PostgreSQL, logs INFO/WARN, Swagger deshabilitado |

### 22.2 Variables de Entorno Requeridas

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

# Correo transaccional (Resend) — recuperación de contraseña
RESEND_API_KEY=<secret>
RESEND_FROM_ADDRESS=DiabeCare <onboarding@resend.dev>
FRONTEND_BASE_URL=https://app.diabecare.com
```

> Los parámetros clínicos y de rate limiting **no** van aquí — viven en `system_config` (BD), gestionables vía API sin redeploy.

> **`.env.example` desactualizado**: el archivo real en el repositorio (`C:\Users\lfkan\...\diabecare-api\.env.example`) solo cubre `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET_KEY`, `JWT_ACCESS_EXPIRY_MS`, `JWT_REFRESH_EXPIRY_MS` y `CORS_ALLOWED_ORIGINS`. Faltan `BCRYPT_STRENGTH`, `VAPID_PUBLIC_KEY`, `VAPID_PRIVATE_KEY`, `VAPID_SUBJECT`, `RESEND_API_KEY`, `RESEND_FROM_ADDRESS` y `FRONTEND_BASE_URL`, todas leídas por `application.yaml`. Debe actualizarse para que un entorno nuevo configurado solo a partir de ese archivo no falle en runtime.

### 22.3 Docker Compose (Desarrollo)

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
      RESEND_API_KEY: ${RESEND_API_KEY}
      FRONTEND_BASE_URL: http://localhost:4200
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

## 23. Importación de Lecturas por Dispositivo (API Key)

### 23.1 Por qué existe

Ninguna integración con un fabricante específico (Dexcom, Abbott/Libre) está implementada todavía — no hay usuarios reales para justificar el costo de un partnership o un acuerdo comercial. Lo que sí se dejó construido es el *groundwork* para que, cuando haga falta, conectar un CGM/glucómetro sea solo cuestión de escribir un adaptador, no de rediseñar la autenticación. Investigación previa evaluó Dexcom API (requiere partnership), Abbott/Libre (sin API pública oficial), Web Bluetooth + Glucose Service estándar (gratis, ya viable con la PWA actual — ver frontend), y un puente con Nightscout (cubre indirectamente tanto Dexcom como Libre sin negociar con ninguno).

### 23.2 Modelo: `DeviceApiKey`

Mismo patrón que `RefreshToken`/`CaregiverInvite`: secreto aleatorio con prefijo `dbc_` (32 bytes, Base64 URL-safe), solo se persiste su hash SHA-256, el valor crudo se revela una única vez al generarla.

```
device_api_keys: id, patient_id, label, key_hash, created_at, last_used_at, revoked_at
```

### 23.3 Gestión (JWT, patient-scoped)

```
POST   /api/v1/device-keys/{patientId}         # Genera — GenerateDeviceApiKeyUseCase
GET    /api/v1/device-keys/{patientId}         # Lista — ListDeviceApiKeysUseCase
DELETE /api/v1/device-keys/{patientId}/{keyId} # Revoca — RevokeDeviceApiKeyUseCase
```

### 23.4 Importación (sin JWT)

```
POST /api/v1/glucose/import
X-Device-Api-Key: dbc_...
```

Pública a nivel de `PublicEndpoints`/filtro JWT — igual que `/api/v1/auth/**`, un bridge desatendido no puede hacer login interactivo. `ImportGlucoseReadingsUseCaseImpl` valida la key manualmente (existe, no revocada), resuelve el `patientId` a partir de ella (nunca viaja en el request, evitando que una key robada de un paciente sirva para inyectar datos en otro), aplica un rate limit propio por key (`rate_limit.device_import_per_hour` = 300, mucho más generoso que el límite manual de 20/hora — un CGM real reporta cada 5 minutos) y construye cada `GlucoseReading` directamente con `GlucoseReading.create(...)` (no reutiliza `RegisterGlucoseReadingUseCase` a propósito, para no heredar su rate limit pensado para entrada manual). Cada lectura queda con `deviceSource` = el label de la key.

### 23.5 Limitación conocida

No hay ningún bridge real (Nightscout, Dexcom, etc.) que efectivamente use esta API todavía — es infraestructura preparada, validada end-to-end con `curl` simulando un dispositivo, pero sin una integración real conectada al otro extremo.

---

*DiabeCare Backend Documentation v5.0*
