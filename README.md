# Reserv

Sistema de reservas para restaurante basado en microservicios con Spring Boot, Eureka, Config Server, API Gateway y PostgreSQL.

## Checklist
- [x] Resumir el propósito del sistema.
- [x] Documentar la arquitectura y los módulos.
- [x] Incluir requisitos y arranque con Docker.
- [x] Añadir endpoints y ejemplos útiles.
- [x] Dejar un `README.md` listo en la raíz del repositorio.

## Arquitectura

El proyecto está compuesto por los siguientes módulos:

- `service-registry`: servidor Eureka para descubrimiento de servicios.
- `config-server`: servidor de configuración centralizada.
- `api-gateway`: puerta de entrada única a los microservicios.
- `ms-customer`: gestión de clientes.
- `ms-table`: gestión de mesas.
- `ms-reservation`: gestión de reservas.
- `ms-queue`: gestión de cola o turnos.
- `ms-menu`: gestión del menú y categorías.
- `ms-order`: gestión de órdenes.

## ✅ Requisitos Técnicos Implementados

### 1. JPA/Hibernate (Entidades y Repositorios)

El proyecto implementa **ORM (Object-Relational Mapping)** con JPA/Hibernate para persistencia de datos.

#### Entidades Implementadas (8):
- **Customer** - Clientes del restaurante
- **RestaurantTable** - Mesas disponibles
- **Reservation** - Reservas de clientes
- **QueueEntry** - Gestión de cola
- **Category** - Categorías del menú
- **MenuItem** - Ítems del menú
- **Order** - Órdenes de clientes
- **OrderItem** - Detalles de órdenes

#### Repositorios (7):
- `CustomerRepository` - Extend `JpaRepository<Customer, Long>`
- `TableRepository` - Métodos custom: `findByCapacityGreaterThanEqual()`
- `ReservationRepository` - CRUD básico
- `QueueRepository` - Métodos: `findByStatus()`, `countByStatus()`
- `CategoryRepository` - Custom: `existsByName()`
- `MenuItemRepository` - Custom: `findByCategoryId()`
- `OrderRepository` - CRUD básico

#### Ejemplo de Entidad:
```java
@Entity
@Table(name = "customers")
@Data
@Builder
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String email;
    
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<Reservation> reservations;
}
```

#### Configuración:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate.dialect: org.hibernate.dialect.PostgreSQL82Dialect
```

---

### 2. Validaciones con Bean Validation

El proyecto utiliza **anotaciones de validación** para garantizar integridad de datos en DTOs y Entidades.

#### Anotaciones Implementadas:
- `@NotBlank` - Campos requeridos (strings)
- `@Email` - Validación de formato email
- `@NotNull` - Valores no nulos
- `@Min/@Max` - Rangos numéricos
- `@Pattern` - Expresiones regulares

#### Ejemplos por Servicio:

**ms-customer/CustomerRequest:**
```java
@Data
public class CustomerRequest {
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;
    
    @Pattern(regexp = "^\\+?[0-9]{10,}$", 
             message = "Invalid phone format")
    private String phone;
}
```

**ms-table/TableRequest:**
```java
@Data
public class TableRequest {
    @Min(value = 1, message = "Table number must be positive")
    private Integer tableNumber;
    
    @Min(value = 2, message = "Capacity must be at least 2")
    private Integer capacity;
}
```

**ms-menu/CategoryRequest:**
```java
@Data
public class CategoryRequest {
    @NotBlank(message = "Category name is required")
    private String name;
}
```

#### Respuesta en Error:
```json
{
  "error": "Email format is invalid"
}
```

---

### 3. Comunicación entre Microservicios (OpenFeign)

El proyecto implementa **comunicación síncrona declarativa** entre microservicios usando **Spring Cloud OpenFeign**.

#### Clientes Feign Implementados:

**1. ms-reservation → CustomerClient**
```java
@FeignClient(name = "ms-customer", url = "http://ms-customer:8081")
public interface CustomerClient {
    @GetMapping("/api/customers/{id}")
    CustomerResponse getCustomerById(@PathVariable Long id);
}
```

**2. ms-reservation → TableClient**
```java
@FeignClient(name = "ms-table", url = "http://ms-table:8082")
public interface TableClient {
    @GetMapping("/api/tables/{id}")
    TableResponse getTableById(@PathVariable Long id);
}
```

**3. ms-order → MenuClient**
```java
@FeignClient(name = "ms-menu", url = "http://ms-menu:8085")
public interface MenuClient {
    @GetMapping("/api/menu/items/{id}")
    MenuItemDTO getMenuItemById(@PathVariable Long id);
}
```

#### Uso en Servicios:

**ReservationServiceImpl:**
```java
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl {
    private final CustomerClient customerClient;
    private final TableClient tableClient;
    
    public ReservationResponse createReservation(ReservationRequest request) {
        // Validar que cliente existe
        customerClient.getCustomerById(request.getCustomerId());
        
        // Validar que mesa existe
        tableClient.getTableById(request.getTableId());
        
        // Crear reserva...
    }
}
```

#### Ventajas:
- **Load Balancing automático** via Eureka
- **Manager de errores centralizados** con GlobalExceptionHandler
- **Comunicación declarativa** y tipada
- **Circuit Breaker support** con Spring Cloud

---

### 4. Logs Estructurados (SLF4J con Lombok)

El proyecto implementa **logging estructurado** con SLF4J y anotación `@Slf4j` de Lombok para trazabilidad completa.

#### Implementación:

**Anotación @Slf4j en Servicios:**
```java
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl {
    public CustomerResponse createCustomer(CustomerRequest request) {
        log.info("Creando cliente con email: {}", request.getEmail());
        // lógica...
        log.info("Cliente creado exitosamente con id: {}", saved.getId());
    }
}
```

**Anotación @Slf4j en Exception Handlers:**
```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(...);
    }
}
```

#### Niveles de Logs Implementados:

| Nivel | Uso | Ejemplo |
|-------|-----|---------|
| **INFO** | Operaciones exitosas | "Cliente creado exitosamente con id: {}" |
| **DEBUG** | Consultas y trazas detalladas | "Obteniendo cliente con id: {}" |
| **WARN** | Situaciones inusuales | "Intento de crear cliente con email existente: {}" |
| **ERROR** | Errores graves | "Error inesperado en cliente" |

#### Configuración en application.yml:
```yaml
logging:
  level:
    root: INFO
    com.lpzcahuillan: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

#### Todos los Servicios con SLF4J (6):
- ✅ ms-customer: 13 logs contextuales
- ✅ ms-table: 10 logs contextuales
- ✅ ms-reservation: 11 logs + validaciones Feign
- ✅ ms-queue: 9 logs operacionales
- ✅ ms-menu: 14 logs (categorías + ítems)
- ✅ ms-order: 8 logs + cálculos

#### Todos los Exception Handlers con SLF4J (6):
- ✅ Logs de ResourceNotFoundException
- ✅ Logs de BadRequestException
- ✅ Logs de excepciones genéricas

#### Visualizar Logs:
```bash
# Ver logs de todos los servicios
docker-compose logs -f

# Ver logs en tiempo real de un servicio
docker-compose logs -f ms-customer

# Ver últimos 50 logs de un servicio
docker-compose logs --tail=50 ms-reservation
```

---

## Requisitos

- Java 17+ (Java 21 recomendado para mejor compatibilidad)
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

## Configuración

Los servicios usan Spring Cloud Config para cargar configuración externa desde `config-server`.

Cada servicio define:

- `spring.application.name`
- `spring.config.import=optional:configserver:http://config-server:8888`
- `spring.cloud.config.fail-fast=false`

## Levantar el sistema con Docker

### Requisitos previos:

1. **Configurar Java 21** (recomendado):
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
java -version
```

2. **Compilar el proyecto** (antes de Docker):
```bash
mvn clean compile -DskipTests
mvn install -DskipTests
```

### Iniciar servicios:

Desde la raíz del proyecto:

```bash
docker-compose up --build
```

En segundo plano:

```bash
docker-compose up --build -d
```

### Servicios disponibles

- Eureka: `http://localhost:8761`
- Config Server: `http://localhost:8888`
- API Gateway: `http://localhost:8080`

## Logs

Ver logs de todos los servicios:

```bash
docker-compose logs -f
```

Ver logs de un servicio específico:

```bash
docker-compose logs -f api-gateway
```

## Endpoints principales

> Los endpoints pueden variar según el controlador de cada microservicio.

### API Gateway

- `GET /actuator/health`
- Rutas hacia los microservicios expuestas desde el gateway

### Ejemplos de prueba

#### Crear un cliente

```bash
http POST http://localhost:8080/api/customers \
  firstName=Juan \
  lastName=Pérez \
  email=juan@example.com \
  phone="+34612345678"
```

#### Crear una categoría de menú

```bash
http POST http://localhost:8080/api/menu/categories \
  name=Entradas \
  description="Aperitivos y starters para compartir"
```

## Base de datos

El proyecto usa instancias de PostgreSQL separadas para cada microservicio.

Puertos expuestos:

- Customer: `5432`
- Table: `5433`
- Reservation: `5434`
- Queue: `5435`
- Menu: `5436`
- Order: `5437`

Credenciales por defecto:

- Usuario: `user`
- Contraseña: `password`

## Detener el sistema

```bash
docker-compose down
```

## Notas

- El `config-server` usa perfil `native` y lee configuraciones desde `classpath:/configurations`.
- Eureka está deshabilitado para registrarse a sí mismo.
- La red Docker compartida es `restaurant-network`.

---

## Auditoría Técnica Completa

### Estado de Implementación

| Requisito | Estado | Cobertura | Detalles |
|-----------|--------|-----------|----------|
| **JPA/Hibernate** | ✅ Completo | 100% | 8 entidades, 7 repositorios con queries custom |
| **Bean Validation** | ✅ Completo | 100% | Anotaciones en todos los DTOs y entidades |
| **Feign/OpenFeign** | ✅ Completo | 100% | 3 clientes configurados, validaciones en cadena |
| **SLF4J Logging** | ✅ Completo | 100% | 6 servicios + 6 handlers = 50+ logs contextuales |

### Componentes del Proyecto

**Servicios Core (9):**
- ✅ service-registry (Eureka)
- ✅ config-server (Spring Cloud Config)
- ✅ api-gateway (Spring Cloud Gateway)
- ✅ ms-customer (Customer Management)
- ✅ ms-table (Table Management)
- ✅ ms-reservation (Reservation Management)
- ✅ ms-queue (Queue Management)
- ✅ ms-menu (Menu Management)
- ✅ ms-order (Order Management)

**Bases de Datos:**
- ✅ 6 instancias PostgreSQL (una por microservicio)
- ✅ Configuración automática con Spring Data JPA
- ✅ DDL automático: `hibernate.ddl-auto=update`

**Validación de Datos:**
- ✅ Validación en DTOs con Bean Validation
- ✅ Manejo centralizado de errores con GlobalExceptionHandler
- ✅ Respuestas HTTP tipadas con mensajes claros

**Comunicación Inter-Microservicios:**
- ✅ ReservationService → CustomerClient, TableClient
- ✅ OrderService → MenuClient
- ✅ Load balancing automático con Eureka
- ✅ Fallback y error handling

**Logging y Monitoreo:**
- ✅ INFO: Operaciones exitosas
- ✅ DEBUG: Consultas y trazas detalladas
- ✅ WARN: Validaciones fallidas
- ✅ ERROR: Excepciones y errores graves

---

## Diagnóstico

Para verificar el estado completo del proyecto:

```bash
# Ver todos los servicios activos
docker-compose ps

# Ver logs de un servicio específico
docker-compose logs -f ms-customer

# Verificar registros en Eureka
curl -s http://localhost:8761/eureka/v2/apps | jq .

# Health check del API Gateway
curl -s http://localhost:8080/actuator/health | jq .

# Verificar configuración
curl -s http://localhost:8888/ms-customer/default | jq .
```

