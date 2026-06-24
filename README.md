# texDigital — Backend Microservicios

Sistema de gestión para empresa de productos textiles digitales. Permite administrar inventario de textiles, clientes, pedidos, producción, pagos, envíos, reseñas y autenticación de usuarios mediante una arquitectura de microservicios con API Gateway centralizado.

## Integrantes del equipo

| Nombre | Rol |
|---|---|
| Branco Aliaga | ms-inventario, ms-clientes, ms-pedidos, ms-pagos, ms-reseñas, auth-service, api-gateway |
| Bruno Carrasco | ms-productos, ms-produccion |
| Tomás Salas | ms-envios |

---

## Arquitectura

10 servicios independientes (9 MS + API Gateway), cada MS con su propia base de datos MySQL.

```
                        [Cliente / Browser]
                                │
                    ┌───────────▼────────────┐
                    │    api-gateway :8080    │
                    └─────────────────────────┘
                                │
        ┌───────────┬───────────┼───────────┬──────────────┐
        │           │           │           │              │
  auth :8090  clientes:8082  pedidos:8083  pagos:8086  reseñas:8087
                                │    │          │           │
                         clientes  productos  pedidos    pedidos
                                │
                         productos:8084

  inventario:8081 ◄── produccion:8085

  envios:8088 ──► pedidos:8083
```

**Comunicación entre MS:**
- `ms-pedidos` → RestClient → `ms-clientes` (valida estado ACTIVO) + `ms-productos` (precio)
- `ms-pagos` → RestClient → `ms-pedidos` (obtiene monto, aplica IVA 19%)
- `ms-produccion` → WebClient → `ms-inventario` (descuenta metros de rollo)
- `ms-reseñas` → WebClient → `ms-pedidos` (valida que cliente haya comprado el producto)
- `ms-envios` → RestClient → `ms-pedidos` (valida que el pedido existe)

---

## Microservicios

| MS | Puerto | Base de datos | Endpoints base |
|---|---|---|---|
| api-gateway | 8080 | — | Enruta todos los MS |
| auth-service | 8090 | texdigital_auth | `/auth`, `/api/usuarios` |
| ms-inventario | 8081 | texdigital_inventario | `/api/textiles`, `/api/rollos` |
| ms-clientes | 8082 | texdigital_clientes | `/api/clientes` |
| ms-pedidos | 8083 | texdigital_pedidos | `/api/pedidos` |
| ms-productos | 8084 | texdigital_productos | `/api/productos` |
| ms-produccion | 8085 | texdigital_produccion | `/api/ordenes` |
| ms-pagos | 8086 | texdigital_pagos | `/api/pagos` |
| ms-resenas | 8087 | texdigital_resenas | `/api/resenas` |
| ms-envios | 8088 | texdigital_envios | `/api/envios` |

---

## Documentación Swagger UI (ejecución local)

| MS | URL Swagger |
|---|---|
| auth-service | http://localhost:8090/swagger-ui.html |
| ms-inventario | http://localhost:8081/swagger-ui.html |
| ms-clientes | http://localhost:8082/swagger-ui.html |
| ms-pedidos | http://localhost:8083/swagger-ui.html |
| ms-productos | http://localhost:8084/swagger-ui.html |
| ms-produccion | http://localhost:8085/swagger-ui.html |
| ms-pagos | http://localhost:8086/swagger-ui.html |
| ms-resenas | http://localhost:8087/swagger-ui.html |
| ms-envios | http://localhost:8088/swagger-ui.html |

## Documentación Swagger UI (despliegue remoto — Render.com)

| MS | URL Swagger |
|---|---|
| auth-service | https://texdigital-auth-service.onrender.com/swagger-ui.html |
| ms-inventario | https://texdigital-ms-inventario.onrender.com/swagger-ui.html |
| ms-clientes | https://texdigital-ms-clientes.onrender.com/swagger-ui.html |
| ms-pedidos | https://texdigital-ms-pedidos.onrender.com/swagger-ui.html |
| ms-productos | https://texdigital-ms-productos.onrender.com/swagger-ui.html |
| ms-produccion | https://texdigital-ms-produccion.onrender.com/swagger-ui.html |
| ms-pagos | https://texdigital-ms-pagos.onrender.com/swagger-ui.html |
| ms-resenas | https://texdigital-ms-resenas.onrender.com/swagger-ui.html |
| ms-envios | https://texdigital-ms-envios.onrender.com/swagger-ui.html |
| api-gateway | https://texdigital-api-gateway.onrender.com |

---

## Rutas del API Gateway

Todos los endpoints son accesibles a través del Gateway en el puerto 8080:

| Ruta Gateway | MS destino | Puerto directo |
|---|---|---|
| `/auth/**` | auth-service | 8090 |
| `/api/usuarios/**` | auth-service | 8090 |
| `/api/clientes/**` | ms-clientes | 8082 |
| `/api/textiles/**` | ms-inventario | 8081 |
| `/api/rollos/**` | ms-inventario | 8081 |
| `/api/pedidos/**` | ms-pedidos | 8083 |
| `/api/productos/**` | ms-productos | 8084 |
| `/api/ordenes/**` | ms-produccion | 8085 |
| `/api/pagos/**` | ms-pagos | 8086 |
| `/api/resenas/**` | ms-reseñas | 8087 |
| `/api/envios/**` | ms-envios | 8088 |

---

## Stack tecnológico

- **Framework:** Spring Boot 3.5.14 / Java 17
- **API Gateway:** Spring Cloud Gateway 2025.0.0 (WebFlux)
- **Autenticación:** JWT (auth-service, Spring Security + jjwt)
- **ORM:** JPA / Hibernate 6
- **Migraciones:** Liquibase
- **Base de datos:** MySQL
- **Documentación:** Swagger / OpenAPI 3 (springdoc-openapi 2.8.6)
- **HATEOAS:** Spring HATEOAS (EntityModel, CollectionModel)
- **Validaciones:** Bean Validation (JSR 380)
- **Comunicación entre MS:** RestClient / WebClient (Reactor)
- **Tests:** JUnit 5 + Mockito + MockMvc
- **Cobertura:** JaCoCo 0.8.12
- **Contenedores:** Docker (multi-stage build)
- **Logging:** SLF4J + Lombok @Slf4j

---

## Requisitos previos (ejecución local)

**Con Maven:**
- Java 17 o superior
- Maven 3.9+
- MySQL activo en puerto 3306

**Con Docker:**
- Docker y Docker Compose

---

## Ejecución local con Docker Compose (recomendado)

Levanta todos los servicios y la base de datos MySQL con un único comando:

```bash
docker compose up --build
```

> El primer inicio puede tardar varios minutos mientras se compilan los JARs dentro de los contenedores.  
> Las bases de datos se crean automáticamente mediante el script `docker/mysql-init.sql` y Liquibase.

Para detener y limpiar:

```bash
docker compose down -v
```

---

## Ejecución local con Maven

Las bases de datos se crean automáticamente con Liquibase al primer inicio.

Levantar los servicios en el siguiente orden (una terminal por servicio):

```bash
# 1 — Sin dependencias externas
cd ms-inventario  && mvn spring-boot:run
cd ms-clientes    && mvn spring-boot:run
cd ms-productos   && mvn spring-boot:run
cd auth-service   && mvn spring-boot:run

# 2 — Dependen de los anteriores
cd ms-pedidos     && mvn spring-boot:run
cd ms-produccion  && mvn spring-boot:run

# 3 — Dependen de ms-pedidos
cd ms-pagos       && mvn spring-boot:run
cd ms-resenas     && mvn spring-boot:run
cd ms-envios      && mvn spring-boot:run

# 4 — Gateway (último, enruta a todos)
cd api-gateway    && mvn spring-boot:run
```

### Autenticación JWT

Registrar usuario:
```bash
POST http://localhost:8090/auth/register
Content-Type: application/json
{"username": "admin", "password": "admin123", "email": "admin@texdigital.cl", "rol": "ADMIN"}
```

Obtener token:
```bash
POST http://localhost:8090/auth/login
Content-Type: application/json
{"username": "admin", "password": "admin123"}
```

Usar token en solicitudes:
```
Authorization: Bearer <token>
```

---

## Datos de prueba (DataLoader)

Los siguientes MS cargan datos iniciales automáticamente si la tabla está vacía:

| MS | Registros |
|---|---|
| ms-inventario | 7 textiles + 11 rollos (Liquibase) |
| ms-clientes | 5 clientes (DataLoader) |
| ms-pedidos | (Liquibase) |
| ms-productos | 7 productos (DataLoader) |
| ms-produccion | (Liquibase) |
| ms-pagos | 3 pagos (DataLoader) |
| ms-envios | 5 envíos (Liquibase) |

---

## Despliegue remoto (Render.com)

Cada MS y el API Gateway se despliegan como servicio independiente en Render.com con TiDB Cloud Serverless como base de datos MySQL.

### Variables de entorno por servicio

**Todos los MS con base de datos:**
```
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:mysql://<host>.prod.aws.tidbcloud.com:4000/<database>?createDatabaseIfNotExist=true&useSSL=true&serverTimezone=UTC
DB_USERNAME=<usuario>
DB_PASSWORD=<contraseña>
```

**auth-service (adicional):**
```
JWT_SECRET=<clave_secreta_minimo_32_caracteres>
```

**ms-pagos (adicional):**
```
MS_PEDIDOS_URL=https://texdigital-ms-pedidos.onrender.com
```

**ms-pedidos (adicional):**
```
MS_CLIENTES_URL=https://texdigital-ms-clientes.onrender.com
MS_PRODUCTOS_URL=https://texdigital-ms-productos.onrender.com
```

**ms-produccion (adicional):**
```
MS_INVENTARIO_URL=https://texdigital-ms-inventario.onrender.com
```

**ms-resenas (adicional):**
```
MS_PEDIDOS_URL=https://texdigital-ms-pedidos.onrender.com
```

**ms-envios (adicional):**
```
MS_PEDIDOS_URL=https://texdigital-ms-pedidos.onrender.com
```

**api-gateway (adicional):**
```
JWT_SECRET=<clave_secreta_minimo_32_caracteres>
AUTH_SERVICE_URL=https://texdigital-auth-service.onrender.com
MS_CLIENTES_URL=https://texdigital-ms-clientes.onrender.com
MS_INVENTARIO_URL=https://texdigital-ms-inventario.onrender.com
MS_PAGOS_URL=https://texdigital-ms-pagos.onrender.com
MS_PEDIDOS_URL=https://texdigital-ms-pedidos.onrender.com
MS_PRODUCCION_URL=https://texdigital-ms-produccion.onrender.com
MS_PRODUCTOS_URL=https://texdigital-ms-productos.onrender.com
MS_RESENAS_URL=https://texdigital-ms-resenas.onrender.com
MS_ENVIOS_URL=https://texdigital-ms-envios.onrender.com
```

> Render inyecta la variable `PORT` automáticamente. No es necesario configurarla.

---

## Control de versiones

- Repositorio: https://github.com/brklyy/texDigital-BCKND
- Tablero Trello: https://trello.com/b/4FjOchp1/texdigital-backend
- Rama principal: `main`
- Estrategia: una rama por cambio (`feature/ms-nombre/descripcion`), merge a main por PR
