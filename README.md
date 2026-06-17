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
| `JWT_REFRESH_EXPIRY_MS` | `604800000` | Expiración refresh token (7 días) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | Orígenes permitidos |
| `BCRYPT_STRENGTH` | `12` | Factor de costo BCrypt |
| `VAPID_PUBLIC_KEY` | `<base64>` | Clave pública VAPID para push |
| `VAPID_PRIVATE_KEY` | `<base64>` | Clave privada VAPID para push |
| `VAPID_SUBJECT` | `mailto:admin@diabecare.com` | Sujeto VAPID |

### 3. Configurar en IntelliJ

1. **Run** → **Edit Configurations** → `DiabeCareApiApplication`
2. **Environment variables**:
```
DB_URL=jdbc:postgresql://localhost:5432/diabecare_dev;DB_USERNAME=diabecare_user;DB_PASSWORD=diabecare_pass;JWT_SECRET_KEY=mi-clave-secreta-de-minimo-32-caracteres;VAPID_PUBLIC_KEY=...;VAPID_PRIVATE_KEY=...;VAPID_SUBJECT=mailto:admin@diabecare.com
```
3. **VM options**: `-Xms512m -Xmx1024m`

### 4. Generar claves VAPID

Ejecuta esta clase temporal una sola vez para generar las claves VAPID:

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
# Compilar
mvn clean compile

# Ejecutar en perfil de desarrollo
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

El servidor arranca en `http://localhost:8080`. Las migraciones Flyway se ejecutan automáticamente, incluyendo el seed de 172 alimentos colombianos.

---

## Tests

```bash
# Todos los tests
mvn test

# Suite específica
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
│   ├── model/              # Entidades de dominio
│   ├── exception/          # Excepciones de dominio
│   └── service/            # Domain Services (MedicalCalculatorService,
│                           #   PatternDetectorService, WeeklySummaryService,
│                           #   GlucoseExportService, AuditService,
│                           #   RateLimitService)
├── application/
│   ├── port/in/            # Casos de uso (interfaces)
│   ├── port/out/           # Puertos de salida (interfaces)
│   └── usecase/            # Implementaciones de casos de uso
├── infrastructure/
│   ├── persistence/        # JPA, repositorios, adaptadores, mappers
│   ├── security/           # JWT, filtros
│   ├── config/             # Configuración Spring (RateLimitConfig, etc.)
│   ├── push/               # PushNotificationService, PushNotificationAdapter
│   ├── pdf/                # Generador de reportes PDF
│   └── scheduler/          # WeeklySummaryScheduler
└── presentation/
    ├── controller/         # Controllers REST
    ├── dto/                # Requests y responses
    ├── mapper/             # Domain <-> DTO mappers
    └── advice/             # GlobalExceptionHandler
```

Las reglas de arquitectura se verifican automáticamente con **ArchUnit**.

---

## Módulos

| Módulo | Descripción |
|---|---|
| Glucosa | Registro, historial, estadísticas TIR/HbA1c/CV, exportación CSV/JSON |
| Nutrición | Registro de comidas, 172 alimentos colombianos |
| Medicamentos | CRUD medicamentos, auditoría de cambios |
| Signos vitales | Peso, presión, HbA1c medida, tendencia |
| Ejercicio | Registro de actividad física por tipo e intensidad |
| Alertas | 7 tipos + 4 alertas de patrón + alertas de ciclo menstrual |
| Ciclo menstrual | 5 fases con correlación glucémica y predicción |
| Calculadora insulina | Dosis de corrección y dosis para comida |
| Reportes | PDF con secciones clínicas para el médico |
| Push notifications | Web Push API con claves VAPID, suscripciones por paciente |
| Resumen semanal | Job automático lunes 8am (America/Bogota) via `@Scheduled` |
| Auditoría | Registro de cambios en perfil y medicamentos |
| Rate limiting | Bucket4j + Caffeine: 20 glucosas, 15 comidas, 10 ejercicios por hora |

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
