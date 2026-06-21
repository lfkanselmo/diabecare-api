# DiabeCare API

Backend de DiabeCare — aplicación de control de salud para pacientes diabéticos. Construido con Java 17 y Spring Boot 3.5 siguiendo arquitectura hexagonal.

---

## Requisitos

- Java 17+
- Maven 3.8+
- PostgreSQL 15+

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
| `JWT_REFRESH_EXPIRY_MS` | `604800000` | Expiración refresh token (7 días) — implementado esta sesión, ver más abajo |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | Orígenes permitidos |
| `BCRYPT_STRENGTH` | `12` | Factor de costo BCrypt |
| `VAPID_PUBLIC_KEY` | `<base64>` | Clave pública VAPID para push |
| `VAPID_PRIVATE_KEY` | `<base64>` | Clave privada VAPID para push |
| `VAPID_SUBJECT` | `mailto:admin@diabecare.com` | Sujeto VAPID |

> **Nota**: los parámetros clínicos (umbrales de alertas, patrones, rate limiting) ya **no** se configuran por variables de entorno ni `application.yml`. Viven en la tabla `system_config` y se gestionan vía API (`GET/POST /api/v1/system-config`) o directamente en BD — ver sección 9 de `DIABECARE.md`.

### 3. Configurar en IntelliJ

1. **Run** → **Edit Configurations** → `DiabeCareApiApplication`
2. **Environment variables**:
```
DB_URL=jdbc:postgresql://localhost:5432/diabecare_dev;DB_USERNAME=diabecare_user;DB_PASSWORD=diabecare_pass;JWT_SECRET_KEY=mi-clave-secreta-de-minimo-32-caracteres;VAPID_PUBLIC_KEY=...;VAPID_PRIVATE_KEY=...;VAPID_SUBJECT=mailto:admin@diabecare.com
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

El servidor arranca en `http://localhost:8080`. Las migraciones Flyway se ejecutan automáticamente, incluyendo el seed de **635 alimentos** (colombianos, latinoamericanos e internacionales — V4+V12+V13), los 16 parámetros iniciales de `system_config`, y la tabla `refresh_tokens` (V15).

> **Pendiente de verificación**: las migraciones V12, V13 y V15 (alimentos + refresh tokens) y el código de autenticación de esta sesión **no se compilaron ni ejecutaron contra una base de datos real** durante su desarrollo — no había Maven ni PostgreSQL disponibles en el entorno de trabajo. La primera vez que levantes el backend tras actualizar, verifica con atención que las migraciones se apliquen sin error.

---

## Tests

```bash
mvn test
mvn test -Dtest=MedicalCalculatorServiceTest
```

| Suite | Tests |
|---|---|
| `MedicalCalculatorServiceTest` | 14 |
| `RegisterGlucoseReadingUseCaseTest` | 5 |
| `GetAlertsUseCaseTest` | 4 |
| `ArchitectureTest` | 7 |
| `DiabecareApiApplicationTests` | 1 |
| **Total** | **31** |

> No hay tests para el flujo de refresh tokens (`RefreshAccessTokenUseCase`, `LogoutCurrentSessionUseCase`, `LogoutAllSessionsUseCase`, `RefreshTokenAdapter`) ni para los handlers de seguridad nuevos (`RestAuthenticationEntryPoint`, `RestAccessDeniedHandler`) — pendiente para una sesión de testing dedicada.

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
│   ├── model/              # Entidades de dominio (incluye User, SystemConfig,
│   │                       #   AuditLog, RefreshToken)
│   ├── exception/          # Excepciones de dominio (incluye InvalidRefreshTokenException)
│   └── service/            # Domain Services (MedicalCalculatorService,
│                            #   PatternDetectorService, WeeklySummaryService,
│                            #   GlucoseExportService, AuditService,
│                            #   RateLimitService)
├── application/
│   ├── port/in/             # Casos de uso (interfaces) — incluye LoginUseCase,
│   │                        #   RegisterUseCase, RefreshAccessTokenUseCase,
│   │                        #   LogoutCurrentSessionUseCase, LogoutAllSessionsUseCase,
│   │                        #   GetActiveSessionsUseCase, SuspendAccountUseCase,
│   │                        #   DeleteAccountUseCase
│   ├── port/out/            # Puertos de salida — incluye AuthenticateUserPort,
│   │                        #   GenerateTokenPort, RefreshTokenPort,
│   │                        #   MessageResolverPort, SystemConfigPort
│   └── usecase/              # Implementaciones de casos de uso
├── infrastructure/
│   ├── persistence/         # JPA, repositorios, adaptadores, mappers
│   ├── security/             # JWT, filtros, AuthenticateUserAdapter,
│   │                        #   GenerateTokenAdapter, RefreshTokenAdapter
│   │   └── handler/          #   RestAuthenticationEntryPoint, RestAccessDeniedHandler
│   ├── config/                # RateLimitConfig, MessageSourceConfig, MessageResolverAdapter
│   ├── push/                 # PushNotificationService, PushNotificationAdapter
│   ├── pdf/                   # Generador de reportes PDF
│   └── scheduler/             # WeeklySummaryScheduler
└── presentation/
    ├── controller/           # Controllers REST — incluye AccountController,
    │                         #   SystemConfigController, AuthController (ampliado)
    ├── dto/                   # Requests y responses
    ├── util/                  # DeviceLabelResolver
    └── advice/                # GlobalExceptionHandler (incluye ACCOUNT_SUSPENDED,
                                #   INVALID_CREDENTIALS, INVALID_REFRESH_TOKEN)
```

Las reglas de arquitectura se verifican automáticamente con **ArchUnit**.

---

## Módulos

| Módulo | Descripción |
|---|---|
| Glucosa | Registro, historial, estadísticas TIR/HbA1c/CV, exportación CSV/JSON |
| Nutrición | Registro de comidas, **635 alimentos** colombianos, latinoamericanos e internacionales |
| Medicamentos | CRUD medicamentos, auditoría de cambios |
| Signos vitales | Peso, presión, HbA1c medida, tendencia |
| Ejercicio | Registro de actividad física por tipo e intensidad |
| Alertas | 7 tipos + 4 alertas de patrón + alertas de ciclo menstrual, mensajes vía `MessageResolverPort` |
| Ciclo menstrual | 5 fases con correlación glucémica y predicción |
| Calculadora insulina | Dosis de corrección y dosis para comida |
| Reportes | PDF con secciones clínicas para el médico |
| Push notifications | Web Push API con claves VAPID, suscripciones por paciente |
| Resumen semanal | Job automático lunes 8am (America/Bogota) via `@Scheduled` |
| Auditoría | Registro de cambios en perfil y medicamentos |
| Rate limiting | Bucket4j + Caffeine, límites configurables en `system_config` |
| **Configuración del sistema** | `system_config`: 16 parámetros clínicos/operacionales en BD, recargables sin redeploy |
| **Gestión de cuenta** | Suspender (`enabled=false`) y eliminar (anonimiza email) la propia cuenta — revoca automáticamente todos los refresh tokens |
| **Autenticación** | `LoginUseCase`/`RegisterUseCase` orquestan; JWT incluye claim `userId` |
| **Sesiones multi-dispositivo (nuevo)** | Refresh tokens con rotación, revocables; logout de la sesión actual o de todos los dispositivos; listado de sesiones activas con etiqueta de dispositivo |

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
| V12 | **+315 alimentos** (total 487): FRUTOS_SECOS, EMBUTIDOS, CONDIMENTOS, COMIDA_RAPIDA, PANADERIA |
| V13 | **+148 alimentos** (total 635): VEGANOS, INDUSTRIALES + ampliación de COMIDA_RAPIDA/PREPARADOS (internacional + calle colombiana) |
| V15 | Tabla `refresh_tokens` (sesión multi-dispositivo). *No existe V14 — numeración no consecutiva intencional, ver `DIABECARE.md`* |

---

## Configuración del sistema (`system_config`)

Tabla en BD con caché en memoria, cargada al arrancar y recargable sin redeploy.

```
GET  /api/v1/system-config           # Lista todos los parámetros
POST /api/v1/system-config/reload    # Recarga la caché desde BD
```

**Categorías:** `ALERTS`, `PATTERNS`, `RATE_LIMIT`. Ver detalle completo en `DIABECARE.md` sección "Parametrización".

---

## Gestión de cuenta

```
PATCH  /api/v1/account/{userId}/suspend   # Suspende (enabled=false, suspended_at) + revoca todos los refresh tokens
DELETE /api/v1/account/{userId}           # Elimina (anonimiza email, deleted_at) + revoca todos los refresh tokens
```

Al intentar iniciar sesión con una cuenta suspendida: `403 ACCOUNT_SUSPENDED`. Credenciales incorrectas: `401 INVALID_CREDENTIALS`.

---

## Autenticación y sesiones (esta sesión)

### Access token + refresh token

```
POST /api/v1/auth/login      # → accessToken (15 min) + refreshToken (7 días)
POST /api/v1/auth/register   # → accessToken (15 min) + refreshToken (7 días)
POST /api/v1/auth/refresh    # refreshToken vigente → nuevo accessToken + nuevo refreshToken (rotación)
```

El refresh token es un valor aleatorio opaco (no JWT), hasheado con SHA-256 antes de persistirse en `refresh_tokens`. Cada uso lo rota: el token usado se revoca y se emite uno nuevo — un token robado y reutilizado tras ser rotado ya no sirve.

### Logout: sesión actual vs todos los dispositivos

```
POST /api/v1/auth/logout       # { refreshToken } → revoca SOLO esa sesión
POST /api/v1/auth/logout-all   # { userId }       → revoca TODAS las sesiones del usuario
```

### Sesiones activas

```
GET /api/v1/auth/sessions/{userId}   # Lista dispositivos con sesión activa
```

Cada sesión incluye `deviceLabel` (resuelto desde el header `User-Agent`, ej. "Chrome en Windows") y `lastUsedAt`.

### Corrección de códigos de estado HTTP

Antes de esta sesión, un JWT expirado devolvía `403 Forbidden` (comportamiento default de Spring Security sin `AuthenticationEntryPoint` configurado), mezclándose con el caso real de "autenticado pero sin permiso". Se agregaron `RestAuthenticationEntryPoint` (401 `SESSION_EXPIRED`) y `RestAccessDeniedHandler` (403 `ACCESS_DENIED`) para separar ambos casos correctamente.

Ver `DIABECARE.md` sección "Refresh Tokens y Sesiones Multi-Dispositivo" para el detalle completo de diseño y flujos.

---

## Internacionalización (preparación, no implementada)

Los mensajes de alertas, patrones y resumen semanal NO están hardcodeados — viven en `src/main/resources/messages.properties` y se resuelven vía `MessageResolverPort` (puerto de dominio) → `MessageResolverAdapter` (usa `ResourceBundleMessageSource` de Spring).

**Estado actual**: solo existe `messages.properties` (español). No hay selector de idioma ni `LocaleResolver` configurado — la app es monolingüe por decisión consciente (ver `DIABECARE.md`). Esta preparación permite agregar un segundo idioma en el futuro sin tocar lógica de negocio.

---

## Tecnologías

| Tecnología | Versión |
|---|---|
| Java | 17 |
| Spring Boot | 3.5.14 |
| Spring Security | 6.x |
| PostgreSQL | 15+ |
| Flyway | 11 |
| MapStruct | 1.5.5 |
| Lombok | 1.18.30 |
| iText | 8.0.4 |
| Caffeine Cache | 3.x |
| jjwt | 0.12.5 |
| Bucket4j | 8.10.1 |
| web-push | 5.1.1 |
| BouncyCastle | 1.70 |
| ArchUnit | 1.3.0 |
| JUnit 5 + Mockito | — |
| springdoc-openapi | 2.8.9 |

---

## Performance

Resultados con JMeter — 50 usuarios simultáneos, 5 iteraciones (medidos antes de esta sesión; no se re-midió tras agregar refresh tokens y el catálogo ampliado de alimentos):

| Endpoint | Promedio | Throughput |
|---|---|---|
| Login | 830 ms | 18.5 req/s |
| Stats glucosa | 10 ms | 22.8 req/s |
| Historial glucosa | 8 ms | 18.9 req/s |
| Buscar alimento | 6 ms | 18.9 req/s |
| Alertas | 11 ms | 19.0 req/s |
| Reporte PDF | 44 ms | 19.0 req/s |

**Throughput total: 110.7 req/s — 0% errores**

> El login ahora hace una escritura adicional (`refresh_tokens`) respecto a la medición original — revalidar el tiempo promedio de 830ms tras esta sesión.
