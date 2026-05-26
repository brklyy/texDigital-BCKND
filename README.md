# texDigital — Backend Microservicios

Sistema de gestión para empresa de productos textiles digitales. Permite administrar inventario de textiles, clientes, pedidos y producción de productos como estampados, lienzos, backlights, banderas y más.

## Integrantes del equipo

| Nombre | Rol |
|---|---|
| Branco Aliaga | ms-inventario, ms-clientes, ms-pedidos |
| Bruno Carrasco | ms-productos, ms-produccion |

---

## Arquitectura

5 microservicios independientes, cada uno con su propia base de datos MySQL.

```
ms-inventario  (8081)  <── ms-produccion (8085)
ms-clientes    (8082)  <─┐
ms-productos   (8084)  <─┴─ ms-pedidos   (8083)
```

---

## Microservicios

### ms-inventario (puerto 8081 | db_inventario)
Gestiona el stock de textiles y rollos de tela.
- **Entidades:** `Textil`, `Rollo` (relación ManyToOne)
- **Endpoints:** `/api/textiles`, `/api/rollos`
- **Endpoints especiales:** `PUT /api/rollos/{id}/usar`, `GET /api/rollos/textil/{textilId}`

### ms-clientes (puerto 8082 | db_clientes)
Gestiona el registro de clientes de la empresa.
- **Entidad:** `Cliente`
- **Endpoints:** `/api/clientes`

### ms-pedidos (puerto 8083 | db_pedidos)
Gestiona los pedidos y sus detalles. Se comunica con ms-clientes y ms-productos.
- **Entidades:** `Pedido`, `DetallePedido` (relación OneToMany)
- **Endpoints:** `/api/pedidos`
- **Endpoints especiales:** `GET /api/pedidos/cliente/{clienteId}`, `GET /api/pedidos/estado/{estado}`
- **Comunicación:** valida cliente en ms-clientes, obtiene precio y nombre de ms-productos

### ms-productos (puerto 8084 | texdigital_productos)
Gestiona el catálogo de productos textiles.
- **Entidad:** `Producto`
- **Endpoints:** `/api/productos`
- **Regla de negocio:** BACKLIGHT y CAJA_BACKLIGHT solo usan textil Pearl

### ms-produccion (puerto 8085 | texdigital_produccion)
Gestiona las órdenes de producción. Se comunica con ms-inventario para descontar metros de rollo.
- **Entidad:** `OrdenProduccion`
- **Endpoints:** `/api/ordenes`
- **Endpoints especiales:** `GET /api/ordenes/pedido/{pedidoId}`, `GET /api/ordenes/stats/metros`
- **Comunicación:** descuenta metros usados en ms-inventario al crear una orden

---

## Stack tecnológico

- **Framework:** Spring Boot 3.5.14
- **Lenguaje:** Java 17
- **ORM:** JPA / Hibernate 6
- **Migraciones:** Liquibase
- **Base de datos:** MySQL (XAMPP)
- **Validaciones:** Bean Validation (JSR 380)
- **Comunicación entre MS:** RestClient / WebClient
- **Logging:** SLF4J
- **Otros:** Lombok, ResponseEntity, @ControllerAdvice

---

## Requisitos previos

- Java 17 o superior
- Maven (incluido via `mvnw`)
- XAMPP con MySQL activo en puerto 3306
- Las bases de datos se crean automáticamente al levantar cada MS

---

## Pasos para ejecutar

1. Clonar el repositorio:
```bash
git clone https://github.com/brklyy/texDigital-BCKND.git
cd texDigital-BCKND
```

2. Iniciar XAMPP y asegurarse que MySQL esté corriendo en el puerto 3306.

3. Levantar cada microservicio en una terminal separada (en este orden):

```bash
# Terminal 1
cd ms-inventario && ./mvnw spring-boot:run

# Terminal 2
cd ms-clientes && ./mvnw spring-boot:run

# Terminal 3
cd ms-productos && ./mvnw spring-boot:run

# Terminal 4
cd ms-pedidos && ./mvnw spring-boot:run

# Terminal 5
cd ms-produccion && ./mvnw spring-boot:run
```

4. Las bases de datos y tablas se crean automáticamente con Liquibase al primer inicio.

---

## Datos de prueba (seed)

| Microservicio | Registros |
|---|---|
| ms-inventario | 7 textiles + 11 rollos |
| ms-clientes | 10 clientes |
| ms-pedidos | 5 pedidos + 9 detalles |
| ms-productos | 13 productos |
| ms-produccion | 3 ordenes |

---

## Control de versiones

- Repositorio: https://github.com/brklyy/texDigital-BCKND
- Tablero Trello: https://trello.com/b/4FjOchp1/texdigital-backend
- Rama principal: `main`
- Estrategia: una rama por capa CSR (`feature/ms-nombre/capa`), merge inmediato por PR
