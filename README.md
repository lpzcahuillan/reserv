# Reserv

Sistema de reservas para restaurante basado en microservicios con Spring Boot, Eureka, Config Server, API Gateway y PostgreSQL.

## Resumen

Esta solución centraliza la gestión de un restaurante en varios microservicios especializados:

- clientes
- mesas
- reservas
- cola de atención
- menú y categorías
- órdenes

La comunicación se apoya en:

- `service-registry` para descubrimiento de servicios
- `config-server` para configuración centralizada
- `api-gateway` como punto de entrada único
- PostgreSQL por servicio

## Checklist

- [x] Describir el propósito del sistema.
- [x] Documentar la arquitectura general.
- [x] Explicar cada módulo y su función.
- [x] Incluir requisitos y arranque con Docker.
- [x] Documentar rutas y ejemplos de uso.
- [x] Añadir notas y troubleshooting básico.

## Arquitectura

```text
Cliente -> API Gateway -> Microservicios -> PostgreSQL
                    \-> Eureka / Config Server
```

### Componentes

- `service-registry`: servidor Eureka para registrar y descubrir servicios.
- `config-server`: expone configuración compartida desde `classpath:/configurations`.
- `api-gateway`: enruta peticiones hacia los microservicios usando Discovery Locator.
- `ms-customer`: CRUD de clientes.
- `ms-table`: CRUD de mesas y búsqueda por capacidad.
- `ms-reservation`: CRUD de reservas.
- `ms-queue`: administración de la cola/turnos.
- `ms-menu`: categorías y productos del menú.
- `ms-order`: CRUD de órdenes y actualización de estado.

## Requisitos

- Java 17
- Maven 3.9+
- Docker y Docker Compose

## Estructura del proyecto

```text
reserv/
├── api-gateway/
├── config-server/
├── ms-customer/
├── ms-menu/
├── ms-order/
├── ms-queue/
├── ms-reservation/
├── ms-table/
├── service-registry/
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Configuración global

Todos los servicios Spring Boot consumen configuración desde el `config-server`.

Patrón usado:

- `spring.application.name`: nombre lógico del servicio
- `spring.config.import=optional:configserver:http://config-server:8888`
- `spring.cloud.config.fail-fast=false`

El `config-server` usa perfil `native` y lee archivos de:

- `classpath:/configurations`

## Configuración de Docker

El archivo `docker-compose.yml` levanta:

- 6 bases de datos PostgreSQL
- Eureka
- Config Server
- API Gateway
- los 6 microservicios de negocio

### Puertos expuestos

- Eureka: `8761`
- Config Server: `8888`
- API Gateway: `8080`
- `ms-customer`: `8081`
- `ms-table`: `8082`
- `ms-reservation`: `8083`
- `ms-queue`: `8084`
- `ms-menu`: `8085`
- `ms-order`: `8086`

### Bases de datos PostgreSQL

| Servicio | Base de datos | Puerto host |
| --- | --- | --- |
| customer | `db_customer` | `5432` |
| table | `db_table` | `5433` |
| reservation | `db_reservation` | `5434` |
| queue | `db_queue` | `5435` |
| menu | `db_menu` | `5436` |
| order | `db_order` | `5437` |

Credenciales por defecto:

- Usuario: `user`
- Contraseña: `password`

## Arranque rápido con Docker

Desde la raíz del proyecto:

```bash
docker-compose up --build -d
```

Ver estado de los contenedores:

```bash
docker-compose ps
```

Ver logs de todos los servicios:

```bash
docker-compose logs -f
```

Ver logs de un servicio concreto:

```bash
docker-compose logs -f api-gateway
```

Detener todo:

```bash
docker-compose down
```

## Servicios disponibles

### Eureka

- URL: `http://localhost:8761`
- Uso: comprobar que todos los servicios están registrados.

### Config Server

- URL: `http://localhost:8888`
- Uso: validar que entrega las configuraciones de cada microservicio.

### API Gateway

- URL: `http://localhost:8080`
- Uso: punto de entrada único para las peticiones HTTP.

## Rutas del API Gateway

Las rutas del gateway están definidas en `config-server/src/main/resources/configurations/api-gateway.yml`.

Rutas configuradas:

- `/api/customers/**` -> `ms-customer`
- `/api/tables/**` -> `ms-table`
- `/api/reservations/**` -> `ms-reservation`
- `/api/queues/**` -> `ms-queue`
- `/api/menu/**` -> `ms-menu`
- `/api/orders/**` -> `ms-order`

> Nota: el controlador de `ms-queue` expone `/api/queue`, mientras que el gateway está configurado con `/api/queues/**`.

## Endpoints por microservicio

### `ms-customer`

- `POST /api/customers`
- `GET /api/customers`
- `GET /api/customers/{id}`
- `PUT /api/customers/{id}`
- `DELETE /api/customers/{id}`

### `ms-table`

- `POST /api/tables`
- `GET /api/tables`
- `GET /api/tables/{id}`
- `GET /api/tables/capacity/{capacity}`
- `PUT /api/tables/{id}`
- `DELETE /api/tables/{id}`

### `ms-reservation`

- `POST /api/reservations`
- `GET /api/reservations`
- `GET /api/reservations/{id}`
- `PUT /api/reservations/{id}`
- `DELETE /api/reservations/{id}`

### `ms-queue`

- `POST /api/queue`
- `POST /api/queue/next`
- `GET /api/queue/{id}`
- `GET /api/queue/waiting`
- `PATCH /api/queue/{id}/status?status=...`
- `DELETE /api/queue/{id}`

### `ms-menu`

#### Categorías

- `POST /api/menu/categories`
- `GET /api/menu/categories`
- `GET /api/menu/categories/{id}`
- `DELETE /api/menu/categories/{id}`

#### Ítems del menú

- `POST /api/menu/items`
- `GET /api/menu/items`
- `GET /api/menu/items/{id}`
- `GET /api/menu/items/category/{categoryId}`
- `PUT /api/menu/items/{id}`
- `DELETE /api/menu/items/{id}`

### `ms-order`

- `POST /api/orders`
- `GET /api/orders`
- `GET /api/orders/{id}`
- `PATCH /api/orders/{id}/status?status=...`
- `DELETE /api/orders/{id}`

## Ejemplos de uso

### Health check del gateway

```bash
http GET http://localhost:8080/actuator/health
```

### Crear cliente

```bash
http POST http://localhost:8080/api/customers \
  firstName=Juan \
  lastName=Pérez \
  email=juan@example.com \
  phone="+34612345678"
```

### Crear mesa

```bash
http POST http://localhost:8080/api/tables \
  tableNumber="Mesa-01" \
  capacity:=4 \
  status=AVAILABLE
```

### Crear categoría de menú

```bash
http POST http://localhost:8080/api/menu/categories \
  name=Entradas \
  description="Aperitivos y starters para compartir"
```

### Crear ítem de menú

```bash
http POST http://localhost:8080/api/menu/items \
  name="Pasta Carbonara" \
  description="Pasta italiana tradicional" \
  price:=12.50 \
  status=AVAILABLE \
  categoryId:=1
```

### Crear reserva

```bash
http POST http://localhost:8080/api/reservations \
  customerId:=1 \
  tableId:=1 \
  reservationTime="2026-05-13T20:00:00" \
  numberOfPeople:=4 \
  status=PENDING
```

### Crear orden

```bash
http POST http://localhost:8080/api/orders \
  tableId:=1 \
  status=PENDING \
  items:='[{"menuItemId":1,"quantity":2},{"menuItemId":2,"quantity":1}]'
```

## Respuestas y estados

Los controladores devuelven normalmente:

- `201 Created` al crear recursos
- `200 OK` para consultas y actualizaciones
- `204 No Content` al eliminar recursos

## Manejo de errores

Cada microservicio incluye un `GlobalExceptionHandler` para responder ante errores de negocio y recursos no encontrados.

Errores comunes:

- recurso no encontrado
- validación inválida de request
- error de conexión con base de datos
- configuración no cargada desde el `config-server`

## Troubleshooting

### El gateway no arranca porque no encuentra configuración

Verifica que exista en `api-gateway/src/main/resources/application.yml`:

- `spring.config.import=optional:configserver:http://config-server:8888`

### Eureka no muestra servicios

Comprueba:

- que `service-registry` esté levantado
- que `config-server` pueda resolver `service-registry:8761`
- que los microservicios estén dentro de la red `restaurant-network`

### Fallos al conectar a PostgreSQL

Comprueba que el contenedor de base de datos correspondiente esté en ejecución y que el puerto host no esté ocupado.

### Quiero reiniciar desde cero

```bash
docker-compose down -v
docker-compose up --build -d
```

## Notas técnicas

- La red compartida de Docker es `restaurant-network`.
- Eureka está configurado para no registrarse a sí mismo.
- El `config-server` usa configuración local (`native`).
- El gateway usa `discovery.locator.enabled=true`, por lo que también puede resolver servicios registrados en Eureka.

## Sugerencia de flujo de prueba

1. Levantar `service-registry`.
2. Levantar `config-server`.
3. Levantar `api-gateway`.
4. Levantar los microservicios de negocio.
5. Probar `GET /actuator/health`.
6. Crear primero clientes, mesas y categorías.
7. Luego probar reservas, órdenes y cola.

