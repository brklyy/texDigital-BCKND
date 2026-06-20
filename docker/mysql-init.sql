CREATE DATABASE IF NOT EXISTS texdigital_auth;
CREATE DATABASE IF NOT EXISTS texdigital_clientes;
CREATE DATABASE IF NOT EXISTS texdigital_inventario;
CREATE DATABASE IF NOT EXISTS texdigital_pagos;
CREATE DATABASE IF NOT EXISTS texdigital_pedidos;
CREATE DATABASE IF NOT EXISTS texdigital_produccion;
CREATE DATABASE IF NOT EXISTS texdigital_productos;
CREATE DATABASE IF NOT EXISTS texdigital_resenas;

GRANT ALL PRIVILEGES ON texdigital_auth.*       TO 'texdigital'@'%';
GRANT ALL PRIVILEGES ON texdigital_clientes.*   TO 'texdigital'@'%';
GRANT ALL PRIVILEGES ON texdigital_inventario.* TO 'texdigital'@'%';
GRANT ALL PRIVILEGES ON texdigital_pagos.*      TO 'texdigital'@'%';
GRANT ALL PRIVILEGES ON texdigital_pedidos.*    TO 'texdigital'@'%';
GRANT ALL PRIVILEGES ON texdigital_produccion.* TO 'texdigital'@'%';
GRANT ALL PRIVILEGES ON texdigital_productos.*  TO 'texdigital'@'%';
GRANT ALL PRIVILEGES ON texdigital_resenas.*    TO 'texdigital'@'%';
FLUSH PRIVILEGES;
