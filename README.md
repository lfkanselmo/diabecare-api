# DiabeCare API

Backend de DiabeCare — aplicación de control de salud para pacientes diabéticos. Construido con Java 17 y Spring Boot 3.5 siguiendo arquitectura hexagonal.

---

## Requisitos

- Java 17+
- Maven 3.8+
- PostgreSQL 15

---

## Configuración

### 1. Base de datos

Crea la base de datos y el usuario en PostgreSQL:

```sql
CREATE DATABASE diabecare_dev;
CREATE USER diabecare_user WITH PASSWORD 'diabecare_pass';
GRANT ALL PRIVILEGES ON DATABASE diabecare_dev TO diabecare_user;
```

### 2. Variables de entorno

El proyecto usa variables de entorno para la configuración sensible. Configúralas en tu sistema o en el Run Configuration de IntelliJ:

| Variable | Ejemplo | Descripción |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/diabecare_dev` | URL de conexión a PostgreSQL |
| `DB_USERNAME` | `diabecare_user` | Usuario de la BD |
| `DB_PASSWORD` | `diabecare_pass` | Contraseña de la BD |
| `JWT_SECRET` | `clave-secreta-minimo-256-bits-aqui` | Clave para firmar tokens JWT |

### 3. IntelliJ — configurar variables de entorno

1. Menú **Run** → **Edit Configurations**
2. Selecciona `DiabeCareApiApplication`
3. Campo **Environment variables**, agrega:
```
DB_URL=jdbc:postgresql://localhost:5432/diabecare_dev;DB_USERNAME=diabecare_user;DB_PASSWORD=diabecare_pass;JWT_SECRET=mi-clave-secreta-de-minimo-32-caracteres
```
4. En **VM options** agrega:
```
-Xms512m -Xmx1024m
```

---

## Ejecución

```bash
# Compilar
mvn clean compile

# Ejecutar en perfil de desarrollo
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# O desde IntelliJ, ejecutar DiabeCareApiApplication
```

El servidor arranca en `http://localhost:8080`.

Las migraciones de Flyway se ejecutan automáticamente al arrancar, incluyendo el seed de 172 alimentos colombianos.

---

## Tests

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar una clase específica
mvn test -Dtest=MedicalCalculatorServiceTest
```

### Cobertura de tests

| Suite | Tests |
|---|---|
| `MedicalCalculatorServiceTest` | 14 |
| `RegisterGlucoseReadingUseCaseTest` | 5 |
| `GetAlertsUseCaseTest` | 4 |
| `ArchitectureTest` | 7 |
| **Total** | **31** |

---

## Documentación API

Swagger UI disponible con el servidor corriendo:

```
http://localhost:8080/swagger-ui/index.html
```

---

## Arquitectura

El proyecto sigue **Arquitectura Hexagonal (Ports & Adapters)**:

```
com.diabecare
├── domain/              ← Entidades, servicios y excepciones de dominio
├── application/
│   ├── port/in/         ← Casos de uso (interfaces)
│   ├── port/out/        ← Puertos de salida (interfaces)
│   └── usecase/         ← Implementaciones
├── infrastructure/
│   ├── persistence/     ← JPA, repositorios, adaptadores
│   ├── security/        ← JWT, filtros
│   ├── config/          ← Configuración Spring
│   └── pdf/             ← Generador de reportes
└── presentation/
    ├── controller/      ← Controllers REST
    ├── dto/             ← Requests y responses
    └── advice/          ← Manejo global de excepciones
```

Las reglas de arquitectura se verifican automáticamente en cada build con **ArchUnit**.

---

## Módulos principales

| Módulo | Descripción |
|---|---|
| Glucosa | Registro, historial, estadísticas TIR, HbA1c estimada |
| Nutrición | Registro de comidas, catálogo de 172 alimentos colombianos |
| Medicamentos | CRUD medicamentos + calculadora de dosis de insulina |
| Signos vitales | Peso, presión arterial, HbA1c medida, tendencia |
| Ejercicio | Registro de actividad física por tipo e intensidad |
| Alertas | 5 tipos de alertas clínicas + alertas de ciclo menstrual |
| Ciclo menstrual | Seguimiento de fases con correlación glucémica |
| Reportes | PDF enriquecido con secciones clínicas para el médico |

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

---

## Tecnologías

| Tecnología | Versión |
|---|---|
| Java | 17 |
| Spring Boot | 3.5.14 |
| Spring Security | 6.x |
| PostgreSQL | 15 |
| Flyway | 11 |
| MapStruct | — |
| Lombok | — |
| iText | 8 |
| Caffeine Cache | — |
| jjwt | 0.12.5 |
| ArchUnit | 1.3.0 |
| JUnit 5 + Mockito | — |
