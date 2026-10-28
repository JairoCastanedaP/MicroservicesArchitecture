# Ticketing — Backend de reserva de entradas a eventos

Proyecto del curso **Microservicios con Spring Boot**. Sistema tipo Ticketmaster / Teleticket construido sesión a sesión.

- **Stack:** Java 21 · Spring Boot 4.1.1 · Maven (wrapper incluido en cada servicio) · H2 (PostgreSQL en Docker desde la Sesión 6)
- **Análisis y roadmap:** [`../docs/00-analisis-temario.md`](../docs/00-analisis-temario.md)
- **Guías de laboratorio:** [`../docs/guias/`](../docs/guias/)

## Servicios

| Servicio | Puerto | Base de datos | Estado |
|----------|--------|---------------|--------|
| event-service | 8081 | `event_db` (H2, `data/event_db.mv.db`) | ✅ Sesión 1 · JPA en Sesión 2 |
| booking-service | 8082 | `booking_db` (H2, `data/booking_db.mv.db`) | ✅ Sesión 2 · clientes HTTP y resiliencia en Sesión 3 |
| payment-service | 8083 | `payment_db` (H2, `data/payment_db.mv.db`) | ✅ Sesión 3 (pagos simulados) |
| user-service | 8084 | `user_db` | Sesión 4 |
| api-gateway | 8080 | — | Sesión 4 |
| config-server / discovery-server | 8888 / 8761 | — | Sesión 5 |
| notification-service | 8085 | — | Sesión 7 |

## 1. Bases de datos (Database per Service)

Cada servicio tiene **su propia base de datos H2**, embebida en el servicio: no hay que instalar nada. Se guarda en un archivo dentro de la carpeta del servicio y se crea sola al primer arranque.

| Servicio | Archivo | Consola web | JDBC URL (usuario `sa`, sin contraseña) |
|----------|---------|-------------|------------------------------------------|
| event-service | `event-service/data/event_db.mv.db` | <http://localhost:8081/h2-console> | `jdbc:h2:file:./data/event_db;AUTO_SERVER=TRUE` |
| booking-service | `booking-service/data/booking_db.mv.db` | <http://localhost:8082/h2-console> | `jdbc:h2:file:./data/booking_db;AUTO_SERVER=TRUE` |
| payment-service | `payment-service/data/payment_db.mv.db` | <http://localhost:8083/h2-console> | `jdbc:h2:file:./data/payment_db;AUTO_SERVER=TRUE` |

Las tablas las crea **Flyway** al arrancar cada servicio (`src/main/resources/db/migration`). Para empezar de cero, detén el servicio y borra su carpeta `data/` (está en `.gitignore`).

- En la consola, copia la JDBC URL tal cual del `application.yml` (si no coincide, H2 responde `Database ... not found [90149]`).
- `./data` es relativo al directorio desde el que arranca el servicio: en IntelliJ, el *Working directory* debe ser la carpeta del servicio.
- Si `/h2-console` responde 404, recarga el proyecto Maven en el IDE (Load Maven Changes) y reinicia.

> En la Sesión 6 las bases pasan a PostgreSQL en Docker (`docker-compose.yml`).

## 2. Ejecutar los servicios

```bash
cd event-service   && ./mvnw spring-boot:run     # Windows: mvnw.cmd spring-boot:run
cd payment-service && ./mvnw spring-boot:run
cd booking-service && ./mvnw spring-boot:run     # llama a los otros dos
```

Desde la Sesión 3, **booking-service depende de los otros dos**: consulta precio y disponibilidad a event-service (OpenFeign) y cobra en payment-service (RestClient + `@HttpExchange`). Si alguno no responde, booking-service devuelve **503** gracias a los timeouts, Retry y Circuit Breaker (Resilience4j). Estado de los circuitos: <http://localhost:8082/actuator/circuitbreakers>.

Variables de entorno útiles:

| Variable | Para qué | Valor por defecto |
|----------|----------|-------------------|
| `SERVER_PORT` | Puerto HTTP | 8081 / 8082 / 8083 |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Conexión a la base de datos | ver `application.yml` |
| `SQL_LOG_LEVEL` | `debug` muestra el SQL de Hibernate; `info` lo oculta | `debug` |
| `BOOKING_HOLD_TIME` | Tiempo de retención de una reserva (booking-service) | `10m` |
| `EVENT_SERVICE_URL` | URL de event-service (booking-service) | `http://localhost:8081` |
| `PAYMENT_SERVICE_URL` | URL de payment-service (booking-service) | `http://localhost:8083` |
| `PAYMENT_MAX_AMOUNT` | Monto máximo aprobado; uno mayor se rechaza (payment-service) | `2000.00` |
| `PAYMENT_FAILURE_RATE` / `PAYMENT_DELAY` | Fallas y demora simuladas al arrancar (payment-service). En caliente: `PUT /api/v1/simulation` | `0.0` / `0ms` |

## 3. Probar

- IntelliJ / VS Code: [`http/event-service.http`](http/event-service.http), [`http/booking-service.http`](http/booking-service.http) y [`http/payment-service.http`](http/payment-service.http).
- Postman: importa las tres colecciones `http/*.postman_collection.json`.
- `curl http://localhost:8081/api/v1/events`

Los errores siguen el estándar **Problem Details (RFC 9457)**:

```json
{ "title": "Recurso no encontrado", "status": 404, "detail": "Evento no encontrado: 999",
  "instance": "/api/v1/events/999", "timestamp": "2026-10-17T21:54:48Z" }
```

## Convenciones

- Cada servicio es un proyecto Maven independiente (sin parent POM compartido) y con **su propia base de datos**.
- Paquete base: `pe.zuncode.ticketing.<servicio>`, organizado por capas: `controller/`, `service/`, `repository/`, `model/`, `dto/`, `exception/`, `config/` y, desde la Sesión 3, `client/` (interfaces y DTOs para llamar a otros servicios).
- API versionada: `/api/v1/<recurso-en-plural>`. La API expone **DTOs** (`record`), nunca entidades JPA.
- Esquema de base de datos versionado con Flyway (`V1__...sql`, `V2__...sql`); Hibernate solo valida (`ddl-auto: validate`).
- Los servicios **no comparten código**: cada uno tiene su copia de los DTOs del otro (*tolerant reader*: solo los campos que usa).
- Llamadas entre servicios siempre con **timeout**. Se reintentan solo las operaciones idempotentes (GET); un pago (POST) no se reintenta.
- Los tests (`./mvnw clean verify`) arrancan el contexto completo contra el mismo archivo H2; `AUTO_SERVER=TRUE` permite ejecutarlos con el servicio corriendo.
