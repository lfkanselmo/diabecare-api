# DiabeCare API

Backend de DiabeCare — aplicación de control de salud para pacientes diabéticos. Construido con Java 21 y Spring Boot 3.5 siguiendo arquitectura hexagonal.

---

## Requisitos

- Java 21+
- Maven 3.8+
- PostgreSQL 15+
- Docker (opcional, solo para los tests de integración con Testcontainers)

---

## Configuración

### 1. Base de datos

```sql
CREATE DATABASE diabecare_dev;
CREATE USER diabecare_user WITH PASSWORD 'diabecare_pass';
GRANT ALL PRIVILEGES ON DATABASE diabecare_dev TO diabecare_user;
```

### 2. Variables de entorno

| Variable | Ejemplo | Descripción |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/diabecare_dev` | URL de conexión |
| `DB_USERNAME` | `diabecare_user` | Usuario de la BD |
| `DB_PASSWORD` | `diabecare_pass` | Contraseña de la BD |
| `JWT_SECRET_KEY` | `clave-secreta-minimo-256-bits` | Clave JWT |
| `JWT_ACCESS_EXPIRY_MS` | `900000` | Expiración access token (15 min) |
| `JWT_REFRESH_EXPIRY_MS` | `604800000` | Expiración refresh token (7 días) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | Orígenes permitidos |
| `BCRYPT_STRENGTH` | `12` | Factor de costo BCrypt |
| `VAPID_PUBLIC_KEY` | `<base64>` | Clave pública VAPID para push |
| `VAPID_PRIVATE_KEY` | `<base64>` | Clave privada VAPID para push |
| `VAPID_SUBJECT` | `mailto:admin@diabecare.com` | Sujeto VAPID |
| `RESEND_API_KEY` | `re_xxx` | API key de Resend, para el correo de recuperación de contraseña |
| `RESEND_FROM_ADDRESS` | `DiabeCare <onboarding@resend.dev>` | Remitente de los correos transaccionales |
| `FRONTEND_BASE_URL` | `http://localhost:4200` | Base para construir el link de reseteo de contraseña que se envía por correo |

> **`.env.example` desactualizado**: el archivo `.env.example` del repo solo lista `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET_KEY`, `JWT_ACCESS_EXPIRY_MS`, `JWT_REFRESH_EXPIRY_MS` y `CORS_ALLOWED_ORIGINS` — le faltan `BCRYPT_STRENGTH`, `VAPID_PUBLIC_KEY`, `VAPID_PRIVATE_KEY`, `VAPID_SUBJECT`, `RESEND_API_KEY`, `RESEND_FROM_ADDRESS` y `FRONTEND_BASE_URL`, todas ya consumidas por `application.yaml`. Actualizarlo antes de repartirlo a un nuevo desarrollador.

> **Nota**: los parámetros clínicos (umbrales de alertas, patrones, rate limiting) **no** se configuran por variables de entorno ni `application.yaml`. Viven en la tabla `system_config` y se gestionan vía API (`GET/POST /api/v1/system-config`) o directamente en BD — ver sección 12 de `DiabeCare_Backend_Documentation.md`.

### 3. Configurar en IntelliJ

1. **Run** → **Edit Configurations** → `DiabeCareApiApplication`
2. **Environment variables**:
```
DB_URL=jdbc:postgresql://localhost:5432/diabecare_dev;DB_USERNAME=diabecare_user;DB_PASSWORD=diabecare_pass;JWT_SECRET_KEY=mi-clave-secreta-de-minimo-32-caracteres;VAPID_PUBLIC_KEY=...;VAPID_PRIVATE_KEY=...;VAPID_SUBJECT=mailto:admin@diabecare.com;RESEND_API_KEY=...;FRONTEND_BASE_URL=http://localhost:4200
```
3. **VM options**: `-Xms512m -Xmx1024m`

### 4. Generar claves VAPID

```java
Security.addProvider(new BouncyCastleProvider());
KeyPairGenerator gen = KeyPairGenerator.getInstance("EC", "BC");
gen.initialize(new ECGenParameterSpec("prime256v1"));
KeyPair keyPair = gen.generateKeyPair();
System.out.println("Public:  " + Base64.toBase64String(keyPair.getPublic().getEncoded()));
System.out.println("Private: " + Base64.toBase64String(keyPair.getPrivate().getEncoded()));
```

---

## Ejecución

```bash
mvn clean compile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

El servidor arranca en `http://localhost:8080`. Las migraciones Flyway (V1 a V23, sin V14 — ver sección 17.1 de la documentación técnica) se ejecutan automáticamente, incluyendo el seed de **635 alimentos** (colombianos, latinoamericanos e internacionales), los 20 parámetros de `system_config`, y las tablas de sesiones, cuidadores, consentimiento, recuperación de contraseña y recordatorios de glucosa.

---

## Tests

```bash
mvn test
mvn test -Dtest=MedicalCalculatorServiceTest
```

Suite completa verificada: **1059 tests en 180 clases, 0 fallos, 0 errores, 0 omitidos** (`mvn test` limpio). Incluye:

- Tests unitarios (JUnit 5 + Mockito + AssertJ) de use cases, servicios de dominio y calculadoras
- Tests de arquitectura con **ArchUnit** (14 reglas)
- Tests de integración con **Testcontainers** (PostgreSQL real, no H2) para adaptadores de persistencia
- Smoke test de arranque del contexto completo (`DiabecareApiApplicationTests`)

Cobertura medida con **JaCoCo** (`mvn verify` genera el reporte en `target/site/jacoco/index.html`).

---

## Documentación API

```
http://localhost:8080/swagger-ui/index.html
```

Solo disponible en perfil `dev`.

---

## Arquitectura

Arquitectura **Hexagonal (Ports & Adapters)**:

```
com.diabecare
├── domain/
│   ├── model/              # Patient, User, GlucoseReading, MealEntry, VitalSign,
│   │                       #   Medication, ExerciseLog, MenstrualCycle + CycleDayEntry,
│   │                       #   Alert, AuditLog, SystemConfig, RefreshToken,
│   │                       #   CaregiverInvite, CaregiverLink, PasswordResetToken,
│   │                       #   GlucoseReminder, AgpHourlyBucket, AccountExportData, ReportData,
│   │                       #   DeviceApiKey
│   ├── exception/          # Excepciones de dominio (InvalidRefreshTokenException,
│   │                       #   InvalidCaregiverInviteException, InvalidPasswordResetTokenException,
│   │                       #   InvalidRoleException, OpenCycleConflictException,
│   │                       #   InvalidDeviceApiKeyException,
│   │                       #   UnauthorizedResourceAccessException, UserNotFoundException...)
│   └── service/             # MedicalCalculatorService, PatternDetectorService,
│                            #   WeeklySummaryService, GlucoseExportService, AuditService,
│                            #   RateLimitService, AgpProfileService, ExerciseLabelService,
│                            #   MenstrualCycleGuidanceService
├── application/
│   ├── port/in/             # ~55 casos de uso (uno por operación) — auth, cuenta,
│   │                        #   cuidadores, recordatorios, ciclo menstrual, admin, etc.
│   ├── port/out/             # Puertos de salida — Load*/Save*Port, GenerateTokenPort,
│   │                         #   RefreshTokenPort, CaregiverInvitePort, PasswordResetTokenPort,
│   │                         #   SendEmailPort, MessageResolverPort, SystemConfigPort,
│   │                         #   DeviceApiKeyPort
│   └── usecase/              # Implementaciones (1 clase por operación)
├── infrastructure/
│   ├── persistence/         # JPA, repositorios, adaptadores, mappers
│   ├── security/             # JWT, filtros, AuthenticateUserAdapter,
│   │                        #   GenerateTokenAdapter, RefreshTokenAdapter
│   │   └── handler/          #   RestAuthenticationEntryPoint, RestAccessDeniedHandler
│   ├── config/                # RateLimitConfig, MessageSourceConfig, MessageResolverAdapter,
│   │                          #   CacheConfig
│   ├── food/                  # OpenFoodFactsAdapter (búsqueda por código de barras)
│   ├── mail/                   # ResendEmailAdapter (SendEmailPort)
│   ├── push/                 # PushNotificationService, PushNotificationAdapter
│   ├── pdf/                   # PdfReportAdapter + MedicalReportPdfGenerator (OpenPDF)
│   └── scheduler/             # WeeklySummaryScheduler, GlucoseReminderScheduler,
│                              #   MedicationReminderScheduler, AccountPurgeScheduler
└── presentation/
    ├── controller/           # 23 Controllers REST (ver módulos abajo)
    ├── dto/                   # Requests y responses
    ├── util/                  # DeviceLabelResolver
    └── advice/                # GlobalExceptionHandler
```

Las reglas de arquitectura se verifican automáticamente con **ArchUnit**.

---

## Módulos

| Módulo | Descripción |
|---|---|
| Glucosa | Registro, historial, estadísticas TIR/HbA1c/CV, perfil AGP por hora, exportación CSV/JSON |
| Nutrición | Registro de comidas, **635 alimentos**, búsqueda por código de barras vía OpenFoodFacts |
| Medicamentos | CRUD medicamentos, auditoría de cambios, recordatorios automáticos por frecuencia |
| Signos vitales | Peso, presión, HbA1c medida, tendencia |
| Ejercicio | Registro de actividad física por tipo e intensidad |
| Alertas | 7 tipos + 4 alertas de patrón + alertas de ciclo menstrual, mensajes vía `MessageResolverPort` |
| Ciclo menstrual | Registro día a día (flujo + síntomas), fases y calendario, correlación glucémica |
| Calculadora insulina | Dosis de corrección y dosis para comida |
| Reportes | PDF con secciones clínicas para el médico (OpenPDF) |
| Push notifications | Web Push API con claves VAPID, suscripciones por paciente |
| Recordatorios de glucosa | Horarios configurables por el paciente, notificación push cada minuto que corresponda |
| Resumen semanal | Job automático lunes 8am (America/Bogota) via `@Scheduled` |
| Auditoría | Registro de cambios en perfil y medicamentos |
| Rate limiting | Bucket4j + Caffeine, límites configurables en `system_config` (incluye login, registro y recuperación de contraseña por IP) |
| Configuración del sistema | `system_config`: 20 parámetros clínicos/operacionales en BD, recargables sin redeploy |
| Gestión de cuenta | Suspender, eliminar (con purga definitiva a los 30 días) y exportar los propios datos |
| Autenticación | Login/registro con JWT + refresh tokens rotables, sesiones multi-dispositivo, recuperación de contraseña por correo (Resend) |
| Cuidadores | Compartir el propio historial en modo solo lectura vía código de invitación de un solo uso |
| Consentimiento | Registro de aceptación de la política de tratamiento de datos (Ley 1581 de 2012, Habeas Data) |
| Panel de administración | Listado de usuarios y asignación de rol `ADMIN`, protegido con `@PreAuthorize` |
| Internacionalización | `messages.properties` (español) + `messages_en.properties` (inglés), resueltos automáticamente según el header `Accept-Language` |
| Importación de dispositivos | API key opaca por paciente (revocable) para que un bridge externo (CGM, Nightscout, un glucómetro) importe lecturas sin login interactivo — ver sección dedicada |

---

## Migraciones Flyway

| Versión | Descripción |
|---|---|
| V1 | Schema inicial: users, patients, glucose_readings, meal_entries, meal_items, vital_signs, medications |
| V2 | Eliminar columnas de versión |
| V3 | Actualización tabla foods |
| V4 | Seed de 172 alimentos colombianos |
| V5 | Perfil de insulina (ISF, ratio, objetivo) |
| V6 | Tabla exercise_logs |
| V7 | biological_sex + menstrual_cycles |
| V8 | push_subscriptions |
| V9 | audit_log |
| V10 | system_config (16 parámetros) |
| V11 | users: suspended_at, deleted_at |
| V12 | +315 alimentos (total 487): FRUTOS_SECOS, EMBUTIDOS, CONDIMENTOS, COMIDA_RAPIDA, PANADERIA |
| V13 | +148 alimentos (total 635): VEGANOS, INDUSTRIALES + ampliación de COMIDA_RAPIDA/PREPARADOS |
| V15 | Tabla `refresh_tokens` (sesión multi-dispositivo) |
| V16 | Rediseño del seguimiento de ciclo menstrual: registro día a día (`cycle_day_entries`, `cycle_day_symptoms`) en vez de un único registro por ciclo |
| V17 | Parámetro `alert.days_before_open_cycle_alert` en `system_config` |
| V18 | Parámetros `rate_limit.login_per_hour` y `rate_limit.register_per_hour` |
| V19 | Compartir con cuidadores: `caregiver_invites`, `caregiver_links` |
| V20 | Consentimiento: `users.terms_accepted_at`, `users.terms_version` |
| V21 | Tabla `password_reset_tokens` |
| V22 | Parámetro `rate_limit.forgot_password_per_hour` |
| V23 | Tabla `glucose_reminders` |
| V24 | Tabla `device_api_keys` + parámetro `rate_limit.device_import_per_hour` |

*No existe V14 — durante el desarrollo se generó una migración adicional de alimentos que se fusionó dentro de V13 antes de aplicarse; Flyway no exige numeración consecutiva, solo orden creciente.*

---

## Configuración del sistema (`system_config`)

Tabla en BD con caché en memoria, cargada al arrancar y recargable sin redeploy.

```
GET  /api/v1/system-config           # Lista todos los parámetros
POST /api/v1/system-config/reload    # Recarga la caché desde BD
```

**Categorías:** `ALERTS`, `PATTERNS`, `RATE_LIMIT`. 20 parámetros en total. Ver detalle completo en `DiabeCare_Backend_Documentation.md` sección 12.

---

## Gestión de cuenta

```
PATCH  /api/v1/account/{userId}/suspend   # Suspende (enabled=false) + revoca todos los refresh tokens
DELETE /api/v1/account/{userId}           # Marca para eliminación (anonimiza email) + revoca sesiones
GET    /api/v1/account/{userId}/export    # Exporta todos los datos del paciente (perfil, lecturas, comidas, etc.)
```

Una cuenta eliminada se purga definitivamente de la base de datos 30 días después (`AccountPurgeScheduler`, diario 3am America/Bogota) — antes de eso, `DeleteAccountUseCase` solo la anonimiza y desactiva.

Al intentar iniciar sesión con una cuenta suspendida o eliminada: `403 ACCOUNT_SUSPENDED`. Credenciales incorrectas: `401 INVALID_CREDENTIALS`.

---

## Autenticación y sesiones

### Access token + refresh token

```
POST /api/v1/auth/login             # → accessToken (15 min) + refreshToken (7 días)
POST /api/v1/auth/register          # → accessToken (15 min) + refreshToken (7 días)
POST /api/v1/auth/refresh           # refreshToken vigente → nuevo accessToken + nuevo refreshToken (rotación)
POST /api/v1/auth/forgot-password   # solicita correo de recuperación (Resend); no revela si el email existe
POST /api/v1/auth/reset-password    # cambia la contraseña con el token recibido por correo
```

El refresh token es un valor aleatorio opaco (no JWT), hasheado con SHA-256 antes de persistirse en `refresh_tokens`. Cada uso lo rota: el token usado se revoca y se emite uno nuevo.

### Logout: sesión actual vs todos los dispositivos

```
POST /api/v1/auth/logout       # { refreshToken } → revoca SOLO esa sesión
POST /api/v1/auth/logout-all   # { userId }       → revoca TODAS las sesiones del usuario
```

### Sesiones activas

```
GET /api/v1/auth/sessions/{userId}   # Lista dispositivos con sesión activa
```

Cada sesión incluye `deviceLabel` (resuelto desde el header `User-Agent`) y `lastUsedAt`.

### Rate limiting de autenticación

Login, registro y recuperación de contraseña están limitados por IP (`rate_limit.login_per_hour`=10, `rate_limit.register_per_hour`=5, `rate_limit.forgot_password_per_hour`=5) para frenar fuerza bruta y abuso del correo transaccional.

---

## Cuidadores (acceso de solo lectura)

```
POST   /api/v1/caregivers/{patientId}/invites        # Genera un código de invitación de un solo uso (vigencia 7 días)
GET    /api/v1/caregivers/{patientId}/links           # Lista los cuidadores con acceso activo
DELETE /api/v1/caregivers/{patientId}/links/{linkId}  # Revoca el acceso de un cuidador
POST   /api/v1/caregivers/redeem                       # El cuidador canjea el código y obtiene acceso
GET    /api/v1/caregivers/my-patients                  # Lista los pacientes a los que el usuario tiene acceso como cuidador
```

El código de invitación sigue el mismo patrón que el refresh token: valor aleatorio, hash SHA-256 persistido, el valor crudo nunca se guarda.

---

## Importación de lecturas por dispositivo (API key)

Groundwork preparado para automatizar la carga de glucosa desde CGMs/glucómetros a futuro (Dexcom API, un puente con Nightscout, un glucómetro Bluetooth, etc.) sin tener todavía una integración con ningún fabricante específico — ver la investigación de mercado que originó esto para el detalle de las opciones evaluadas (Dexcom API, Web Bluetooth Glucose Service, Nightscout, Terra).

```
POST   /api/v1/device-keys/{patientId}        # Genera una API key (JWT, la revela una sola vez)
GET    /api/v1/device-keys/{patientId}        # Lista las keys del paciente (sin exponer el hash)
DELETE /api/v1/device-keys/{patientId}/{keyId} # Revoca una key

POST   /api/v1/glucose/import                  # Importa lecturas — SIN JWT, header X-Device-Api-Key
```

`/api/v1/glucose/import` es pública a nivel del filtro de Spring Security (igual que `/auth/**`) porque un bridge externo desatendido no puede hacer login interactivo — se autentica con la API key, validada manualmente dentro de `ImportGlucoseReadingsUseCase`. Cada lectura importada queda marcada con `deviceSource` = el label de la key (ej. "Dexcom G6"), reutilizando el campo que ya usa el registro manual. Rate limit propio y más generoso que el manual (`rate_limit.device_import_per_hour` = 300, contado por key, no por paciente) para no asfixiar un CGM real que reporta cada 5 minutos.

---

## Internacionalización

Los mensajes de alertas, patrones y resumen semanal se resuelven vía `MessageResolverPort` (puerto de dominio) → `MessageResolverAdapter` (usa `ResourceBundleMessageSource` de Spring).

```
src/main/resources/messages.properties      # Español (default)
src/main/resources/messages_en.properties   # Inglés
```

No hay un `LocaleResolver` custom configurado — Spring Boot usa por defecto `AcceptHeaderLocaleResolver`, así que un cliente que envíe `Accept-Language: en` ya recibe los mensajes en inglés sin configuración adicional.

---

## Tecnologías

| Tecnología | Versión |
|---|---|
| Java | 17 |
| Spring Boot | 3.5.14 |
| Spring Security | 6.x |
| PostgreSQL | 15+ |
| Flyway | 11.7.2 |
| MapStruct | 1.5.5 |
| Lombok | 1.18.30 |
| OpenPDF | 3.0.5 |
| Caffeine Cache | 3.2.3 |
| jjwt | 0.12.5 |
| Bucket4j | 8.10.1 |
| web-push | 5.1.1 |
| BouncyCastle | 1.70 |
| ArchUnit | 1.2.1 |
| Testcontainers | 1.19.6 |
| JaCoCo | 0.8.12 |
| JUnit 5 + Mockito + AssertJ | — |
| springdoc-openapi | 2.8.9 |

> **OpenPDF, no iText**: la generación de reportes usa `org.openpdf` (paquete `org.openpdf.text.*`), el fork libre y mantenido de iText 4/5 — no la librería comercial `com.itextpdf` (iText 7+). No confundir ambas al buscar documentación.

---

## Performance

Resultados con JMeter — 50 usuarios simultáneos, 5 iteraciones:

| Endpoint | Promedio | Throughput |
|---|---|---|
| Login | 830 ms | 18.5 req/s |
| Stats glucosa | 10 ms | 22.8 req/s |
| Historial glucosa | 8 ms | 18.9 req/s |
| Buscar alimento | 6 ms | 18.9 req/s |
| Alertas | 11 ms | 19.0 req/s |
| Reporte PDF | 44 ms | 19.0 req/s |

**Throughput total: 110.7 req/s — 0% errores**

> Medición histórica, previa a la incorporación de cuidadores, consentimiento, recuperación de contraseña, recordatorios de glucosa y AGP. No se ha vuelto a correr JMeter contra el estado actual del backend.
