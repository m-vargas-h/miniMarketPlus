# MiniMarket Plus

Backend API REST para la gestión de un minimarket, desarrollada con Spring Boot 3 e integración completa de seguridad mediante Spring Security + JWT. Incluye suite de pruebas unitarias e integración con cobertura medida por JaCoCo. Documentación navegable de la API disponible mediante Swagger UI (OpenAPI 3.0), con anotaciones completas de request/response, ejemplos y códigos de estado en todos los controladores, además de navegación dinámica entre recursos mediante HATEOAS (EntityModel/CollectionModel).

---

## Tecnologías

| Tecnología | Versión | Descripción |
|---|---|---|
| Java | 21 | Lenguaje principal |
| Spring Boot | 3.4.1 | Framework base |
| Spring Security | 6.x | Autenticación y autorización |
| jjwt | 0.11.5 | Generación y validación de JWT |
| springdoc-openapi | 2.3.0 | Documentación Swagger UI / OpenAPI 3.0 |
| Spring HATEOAS | Incluido en spring-boot-starter-hateoas | Enlaces dinámicos EntityModel/CollectionModel |
| H2 Database | Runtime | Base de datos en memoria |
| Lombok | Latest | Reducción de boilerplate |
| JUnit 5 | Incluido en starter-test | Framework de pruebas unitarias |
| Mockito | Incluido en starter-test | Simulación de dependencias |
| JaCoCo | 0.8.11 | Medición de cobertura de código |
| Maven | 3.x | Gestión de dependencias |

---

## Estructura del Proyecto

```
├── 📁 .mvn
│   └── 📁 wrapper
│       └── 📄 maven-wrapper.properties
├── 📁 src
│   ├── 📁 main
│   │   ├── 📁 java
│   │   │   └── 📁 com
│   │   │       └── 📁 minimarket
│   │   │           ├── 📁 controller                   # Controladores REST
│   │   │           ├── 📁 entity                       # Entidades JPA
│   │   │           ├── 📁 repository                   # Interfaces Spring Data JPA
│   │   │           ├── 📁 security                     # Capa de seguridad
│   │   │           │   ├── 📁 config                     # SecurityConfig + OpenApiConfig
│   │   │           │   ├── 📁 filter                     # Filtro JWT por request
│   │   │           │   ├── 📁 model                      # CustomUserDetails, LoginRequest
│   │   │           │   ├── 📁 service                    # CustomUserDetailsService
│   │   │           │   └── 📁 util                       # JwtUtil.java
│   │   │           ├── 📁 service                      # Interfaces de servicios
│   │   │           │   └── 📁 impl                       # Implementaciones
│   │   │           └── ☕ MinimarketApplication.java
│   │   └── 📁 resources
│   │       ├── 📁 static
│   │       ├── 📁 templates
│   │       ├── 📄 application.properties
│   │       └── 📄 data.sql
│   └── 📁 test
│       └── 📁 java
│           └── 📁 com
│               └── 📁 minimarket
│                   ├── 📁 security
│                   │   ├── ☕ SecurityControllerTest.java
│                   │   └── 📁 util
│                   │       └── ☕ JwtUtilTest.java
│                   ├── 📁 service
│                   │   └── 📁 impl
│                   │       ├── ☕ CarritoServiceImplTest.java
│                   │       ├── ☕ DetalleVentaServiceImplTest.java
│                   │       ├── ☕ InventarioServiceImplTest.java
│                   │       ├── ☕ ProductoServiceImplTest.java
│                   │       ├── ☕ UsuarioServiceImplTest.java
│                   │       └── ☕ VentaServiceImplTest.java
│                   ├── ☕ MinimarketApplicationTests.java
│                   └── ☕ MinimarketIntegrationTest.java
├── ⚙️ .gitattributes
├── ⚙️ .gitignore
├── 📝 README.md
├── 📄 mvnw
├── 📄 mvnw.cmd
└── ⚙️ pom.xml
```

---

## Requisitos Previos

- Java 21 o superior
- Maven 3.6 o superior
- IDE recomendado: IntelliJ IDEA o VS Code con Extension Pack for Java
- Postman o similar para pruebas de endpoints

---

## Ejecución

```bash
# Compilar
./mvnw clean install

# Ejecutar
./mvnw spring-boot:run
```

- API disponible en: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Consola H2 (solo ADMIN): `http://localhost:8080/h2-console`

---

## Documentación de la API

La API cuenta con documentación interactiva generada automáticamente mediante **Swagger UI (OpenAPI 3.0)**.

Disponible en: `http://localhost:8080/swagger-ui/index.html`

Todos los controladores (Productos, Categorías, Carrito, Inventario, Ventas, Detalle de Ventas y Usuarios) incluyen documentación completa con `@Operation`, `@ApiResponses` (200/204, 401, 403, 404 según corresponda), `@Parameter` en los `@PathVariable`, y ejemplos de request/response generados mediante `@Schema` en las entidades. Los endpoints protegidos declaran además `@SecurityRequirement(name = "bearerAuth")`, por lo que Swagger UI muestra el candado 🔒 correspondiente y exige el token antes de permitir "Try it out".

Para probar endpoints protegidos directamente desde Swagger:
1. Ejecuta `POST /auth/login` con tus credenciales
2. Copia el token JWT de la respuesta
3. Haz clic en el botón **Authorize 🔒** en la esquina superior derecha
4. Ingresa el token en el formato: `Bearer <token>`
5. Todos los endpoints protegidos quedarán autenticados

### Navegación dinámica con HATEOAS

Todas las respuestas que exponen una entidad (individual o en colección) incluyen enlaces dinámicos generados con `EntityModel`/`CollectionModel`, permitiendo descubrir recursos relacionados sin necesidad de conocer de antemano la estructura de URLs de la API. Cada respuesta incluye como mínimo un enlace `self` y un enlace hacia la colección completa, además de enlaces relacionales explícitos hacia entidades vinculadas (por ejemplo, un producto enlaza hacia su categoría, y una categoría enlaza de vuelta hacia sus productos).

Para habilitar esta navegación bidireccional se agregaron 5 endpoints de filtrado que exponen métodos de servicio existentes:

| Endpoint | Entidad | Descripción |
|---|---|---|
| `GET /api/productos/categoria/{categoriaId}` | Producto | Productos de una categoría específica |
| `GET /api/inventario/producto/{productoId}` | Inventario | Movimientos de inventario de un producto |
| `GET /api/carrito/usuario/{usuarioId}` | Carrito | Items de carrito de un usuario |
| `GET /api/ventas/usuario/{usuarioId}` | Venta | Ventas realizadas por un usuario |
| `GET /api/detalle-ventas/venta/{ventaId}` | DetalleVenta | Líneas de detalle de una venta |

### Exportar y validar el contrato OpenAPI

El JSON completo de la especificación está disponible en `http://localhost:8080/v3/api-docs`. Puede importarse directamente en Postman usando la opción **"OpenAPI 3.0 Specification with a Postman Collection"**, lo que genera una colección completa y mantiene el vínculo con el contrato para validar que las respuestas reales coincidan con lo documentado.

---

## Pruebas

### Ejecutar todas las pruebas

```bash
./mvnw test
```

Ejecuta las **108 pruebas** distribuidas en 10 clases y genera automáticamente el reporte de cobertura JaCoCo en `target/site/jacoco/index.html`.

### Ejecutar una clase específica

```bash
# Pruebas unitarias de servicios (Mockito)
./mvnw test -Dtest=CarritoServiceImplTest
./mvnw test -Dtest=DetalleVentaServiceImplTest
./mvnw test -Dtest=InventarioServiceImplTest
./mvnw test -Dtest=ProductoServiceImplTest
./mvnw test -Dtest=UsuarioServiceImplTest
./mvnw test -Dtest=VentaServiceImplTest

# Pruebas de seguridad
./mvnw test -Dtest=JwtUtilTest
./mvnw test -Dtest=SecurityControllerTest

# Pruebas de integración
./mvnw test -Dtest=MinimarketIntegrationTest
```

### Reporte de cobertura JaCoCo

Disponible en `target/site/jacoco/index.html` tras ejecutar `./mvnw test`.

| Paquete | Cobertura instrucciones | Cobertura ramas |
|---|---|---|
| `com.minimarket.security.config` | 100% | n/a |
| `com.minimarket.security.util` | 100% | 75% |
| `com.minimarket.security.model` | 100% | n/a |
| `com.minimarket.entity` | 94% | n/a |
| `com.minimarket.service.impl` | 84% | 100% |
| `com.minimarket.security.service` | 73% | n/a |
| `com.minimarket` | 37% | n/a |
| `com.minimarket.security.filter` | 36% | 20% |
| `com.minimarket.controller` | 42% | 12% |
| **Total** | **61%** | **20%** |

> **Nota:** la cobertura de `controller` (42%) y la leve baja en `entity`/`service.impl` respecto a semanas anteriores reflejan la incorporación de HATEOAS: se agregaron los métodos `toModel()` en los 7 controladores y 5 endpoints nuevos de filtrado (`/categoria/{id}`, `/producto/{id}`, `/usuario/{id}`, `/venta/{id}`), cuyo código aún no tiene tests unitarios dedicados. Los 108 tests existentes siguen validando correctamente la seguridad, los servicios y los casos base de cada controlador.

> **Pendiente a futuro:** agregar tests unitarios/MockMvc dedicados a los 5 endpoints nuevos de filtrado HATEOAS y a los métodos `toModel()` de cada controlador, para elevar la cobertura del paquete `controller` y confirmar la estructura de los enlaces `_links` generados.

### Resumen de pruebas

| Clase | Tipo | Pruebas | Resultado |
|---|---|---|---|
| `MinimarketApplicationTests` | Contexto Spring | 1 | ✅ Todas pasan |
| `MinimarketIntegrationTest` | Integración (SpringBootTest) | 10 | ✅ Todas pasan |
| `SecurityControllerTest` | Seguridad (MockMvc + @WithMockUser) | 31 | ✅ Todas pasan |
| `JwtUtilTest` | Unitaria (JWT) | 17 | ✅ Todas pasan |
| `CarritoServiceImplTest` | Unitaria (Mockito) | 8 | ✅ Todas pasan |
| `DetalleVentaServiceImplTest` | Unitaria (Mockito) | 7 | ✅ Todas pasan |
| `InventarioServiceImplTest` | Unitaria (Mockito) | 9 | ✅ Todas pasan |
| `ProductoServiceImplTest` | Unitaria (Mockito) | 7 | ✅ Todas pasan |
| `UsuarioServiceImplTest` | Unitaria (Mockito) | 8 | ✅ Todas pasan |
| `VentaServiceImplTest` | Unitaria (Mockito) | 10 | ✅ Todas pasan |
| **Total** | | **108** | **✅ 0 fallos** |

---

### Colección Postman

Disponible en el repositorio: `openapi.json`

Corresponde a la especificación OpenAPI 3.0 completa, exportada directamente desde `http://localhost:8080/v3/api-docs`. Al importarla en Postman (opción **"OpenAPI 3.0 Specification with a Postman Collection"**), genera automáticamente una colección con todas las rutas del backend agrupadas por controlador (Autenticación, Productos, Categorías, Carrito, Inventario, Ventas, Detalle Ventas, Usuarios), manteniendo el vínculo con el contrato para validar que las respuestas reales coincidan con lo documentado en Swagger.

Para ejecutar: importa el archivo en Postman, autentícate primero con `POST /auth/login` para obtener el token JWT, y configúralo como Bearer Token a nivel de colección (o de cada carpeta) para que se propague automáticamente a los endpoints protegidos.

---

## Usuarios de Prueba

Cargados automáticamente por `data.sql` al iniciar la aplicación:

| Usuario | Contraseña | Rol | Acceso |
|---|---|---|---|
| `admin` | `admin123` | ROLE_ADMIN | Acceso total |
| `empleado` | `empleado123` | ROLE_EMPLEADO | Inventario, ventas, detalle ventas |
| `cliente` | `cliente123` | ROLE_CLIENTE | Carrito y consulta de productos |

---

## Endpoints

### Autenticación (públicos)

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/auth/login` | Retorna JWT |
| POST | `/auth/registro` | Registra usuario con ROLE_CLIENTE |

### Productos y Categorías

| Método | Endpoint | Rol requerido |
|---|---|---|
| GET | `/api/productos` | Público |
| GET | `/api/productos/{id}` | Público |
| GET | `/api/productos/categoria/{categoriaId}` | Público |
| POST | `/api/productos` | ADMIN |
| PUT | `/api/productos/{id}` | ADMIN |
| DELETE | `/api/productos/{id}` | ADMIN |
| GET | `/api/categorias` | Público |
| GET | `/api/categorias/{id}` | Público |
| POST | `/api/categorias` | ADMIN |
| PUT | `/api/categorias/{id}` | ADMIN |
| DELETE | `/api/categorias/{id}` | ADMIN |

> Al crear un producto, `categoria.id` debe corresponder a una categoría existente (ver [Datos Iniciales](#datos-iniciales)). Puedes crear una categoría nueva con `POST /api/categorias` si necesitas una distinta.

### Inventario, Ventas y Detalle Ventas

| Método | Endpoint | Rol requerido |
|---|---|---|
| GET / POST / PUT | `/api/inventario/**` | ADMIN, EMPLEADO |
| GET | `/api/inventario/producto/{productoId}` | ADMIN, EMPLEADO |
| DELETE | `/api/inventario/{id}` | ADMIN |
| GET / POST | `/api/ventas/**` | ADMIN, EMPLEADO |
| GET | `/api/ventas/usuario/{usuarioId}` | ADMIN, EMPLEADO |
| GET / POST / PUT | `/api/detalle-ventas/**` | ADMIN, EMPLEADO |
| GET | `/api/detalle-ventas/venta/{ventaId}` | ADMIN, EMPLEADO |
| DELETE | `/api/detalle-ventas/{id}` | ADMIN |

### Carrito

| Método | Endpoint | Rol requerido |
|---|---|---|
| GET / POST / PUT / DELETE | `/api/carrito/**` | ADMIN, CLIENTE |
| GET | `/api/carrito/usuario/{usuarioId}` | ADMIN, CLIENTE |

### Usuarios

| Método | Endpoint | Rol requerido |
|---|---|---|
| GET / POST / PUT / DELETE | `/api/usuarios/**` | ADMIN |

---

## Autenticación

### 1. Obtener el token JWT

```http
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

Respuesta:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 2. Usar el token en requests protegidos

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## Seguridad Implementada

| Mecanismo | Implementación | Protege contra |
|---|---|---|
| JWT Stateless | jjwt 0.11.5 + HS256 | Acceso no autorizado, session hijacking |
| BCrypt | BCryptPasswordEncoder | Robo de contraseñas en BD |
| X-Content-Type-Options | Header HTTP | MIME sniffing / XSS |
| Content-Security-Policy | Header HTTP | XSS, inyección de contenido |
| HSTS | Header HTTP | Downgrade attacks |
| @PreAuthorize | Spring Method Security | Acceso no autorizado por rol |
| Session STATELESS | SessionCreationPolicy | Session fixation attacks |

> **Nota:** el proyecto no define un `AuthenticationEntryPoint` personalizado, por lo que Spring Security responde `403` tanto para peticiones sin token como para peticiones con rol insuficiente (en vez de `401` para el primer caso). Los códigos documentados en Swagger reflejan la semántica REST estándar; el comportamiento real actual se limita a `403` en ambos casos.

---

## Payload del JWT

```json
{
  "sub": "admin",
  "roles": ["ROLE_ADMIN"],
  "iat": 1748000000,
  "exp": 1748036000
}
```

---

## Datos Iniciales

El archivo `data.sql` inserta automáticamente al arrancar:

- 3 roles: `ROLE_ADMIN`, `ROLE_EMPLEADO`, `ROLE_CLIENTE`
- 3 usuarios con contraseñas hasheadas en BCrypt
- Asignación de un rol por usuario
- 4 categorías: `Abarrotes` (id 1), `Bebidas`, `Lácteos`, `Aseo y limpieza`, disponibles para asociar al crear productos vía `POST /api/productos`
- 8 productos distribuidos en las 4 categorías
- 3 movimientos de inventario de ejemplo
- 2 ventas de prueba asociadas al usuario `cliente`, con sus respectivos detalles de venta
