# Minimarket API

Sistema de gestión para un minimarket desarrollado con **Spring Boot**, que expone una API REST para administrar productos, categorías, inventario, ventas, carrito de compras y usuarios. Incluye autenticación basada en sesión con Spring Security y control de acceso por roles.

---

## Tecnologías

| Tecnología | Versión |
|---|---|
| Java | 17 |
| Spring Boot | 3.4.1 |

---

## Estructura del proyecto


```
├── 📁 .mvn
│   └── 📁 wrapper
│       └── 📄 maven-wrapper.properties
├── 📁 src
│   ├── 📁 main
│   │   ├── 📁 java
│   │   │   └── 📁 com
│   │   │       └── 📁 minimarket
│   │   │           ├── 📁 controller       # Controladores REST
│   │   │           ├── 📁 entity           # Entidades JPA
│   │   │           ├── 📁 repository       # Repositorios Spring Data
│   │   │           ├── 📁 security         # Capa de seguridad
│   │   │           │   ├── 📁 config           # Configuración de seguridad
│   │   │           │   ├── 📁 model            # CustomUserDetails, LoginRequest
│   │   │           │   ├── 📁 service          # CustomUserDetailsService
│   │   │           │   └── 📁 util             # JwtUtil
│   │   │           └── 📁 service          # Interfaces de servicio
│   │   │               └── 📁 impl             # Implementaciones
│   │   └── 📁 resources
│   │       ├── 📁 static
│   │       ├── 📁 templates
│   │       ├── 📄 application.properties   # Configuración de la app
│   │       └── 📄 data.sql                 # Datos iniciales (roles y usuarios)
│   └── 📁 test
│       └── 📁 java
│           └── 📁 com
│               └── 📁 minimarket
│                   └── ☕ MinimarketApplicationTests.java
├── ⚙️ .gitattributes
├── ⚙️ .gitignore
├── 📝 README.md
├── 📄 mvnw
├── 📄 mvnw.cmd
└── ⚙️ pom.xml
```

---

## Modelo de datos

```
Categoria ──< Producto >──< Carrito >── Usuario
                 │                         │
                 └──< Inventario           └──< Venta >──< DetalleVenta
```

Las entidades principales son:

- **Categoria** — nombre único; agrupa productos.
- **Producto** — nombre, precio, stock y categoría.
- **Inventario** — movimientos de stock (Entrada / Salida) con fecha.
- **Venta** — encabezado de venta asociado a un usuario y fecha.
- **DetalleVenta** — línea de venta con producto, cantidad y precio unitario.
- **Carrito** — ítems temporales por usuario con cantidad.
- **Usuario** — credenciales y roles asignados.
- **Rol** — `ROLE_ADMIN`, `ROLE_EMPLEADO`, `ROLE_CLIENTE`.

---

## Seguridad y roles

La autenticación usa el formulario de login de Spring Security (`/login`). Las contraseñas se almacenan con **BCrypt**.

| Rol | Acceso permitido |
|---|---|
| `ROLE_ADMIN` | Todo, incluida consola H2 y gestión de usuarios |
| `ROLE_EMPLEADO` | Inventario, ventas y detalle de ventas |
| `ROLE_CLIENTE` | Carrito de compras |
| Todos autenticados | Productos y categorías |
| Público (sin login) | `GET /public/hola`,  |

---

## Usuarios de prueba

Los siguientes usuarios se cargan automáticamente al iniciar la aplicación (vía `data.sql`):

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | ROLE_ADMIN |
| `empleado` | `empleado123` | ROLE_EMPLEADO |
| `cliente` | `cliente123` | ROLE_CLIENTE |

> Las contraseñas están hasheadas con BCrypt en la base de datos.

---

## Configuración

El archivo `src/main/resources/application.properties` usa H2 en memoria:

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.defer-datasource-initialization=true
```

La base de datos se recrea en cada inicio. Para acceder a la consola H2, inicia sesión como `admin` y navega a `http://localhost:8080/h2-console`.

---

## Cómo ejecutar

### Requisitos previos

- Java 17+
- Maven (o usar el wrapper incluido)

### Pasos

```bash
# Clonar o descomprimir el proyecto
cd Minimarket_S1

# Ejecutar con el wrapper de Maven
./mvnw spring-boot:run        # Linux / macOS
mvnw.cmd spring-boot:run      # Windows
```

La aplicación quedará disponible en `http://localhost:8080`.

---

## Endpoints de la API

Todos los endpoints requieren autenticación salvo `/public/**`. El acceso está restringido por rol según la tabla de seguridad anterior.

### Productos — `/api/productos`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/productos` | Listar todos los productos |
| GET | `/api/productos/{id}` | Obtener producto por ID |
| POST | `/api/productos` | Crear producto |
| PUT | `/api/productos/{id}` | Actualizar producto |
| DELETE | `/api/productos/{id}` | Eliminar producto |

### Categorías — `/api/categorias`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/categorias` | Listar categorías |
| GET | `/api/categorias/{id}` | Obtener categoría por ID |
| POST | `/api/categorias` | Crear categoría |
| PUT | `/api/categorias/{id}` | Actualizar categoría |
| DELETE | `/api/categorias/{id}` | Eliminar categoría |

### Inventario — `/api/inventario` *(Admin / Empleado)*

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/inventario` | Listar movimientos |
| GET | `/api/inventario/{id}` | Obtener movimiento por ID |
| POST | `/api/inventario` | Registrar movimiento |
| PUT | `/api/inventario/{id}` | Actualizar movimiento |
| DELETE | `/api/inventario/{id}` | Eliminar movimiento |

### Ventas — `/api/ventas` *(Admin / Empleado)*

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/ventas` | Listar ventas |
| GET | `/api/ventas/{id}` | Obtener venta por ID |
| POST | `/api/ventas` | Registrar venta |

### Detalle de Ventas — `/api/detalle-ventas` *(Admin / Empleado)*

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/detalle-ventas` | Listar detalles |
| GET | `/api/detalle-ventas/{id}` | Obtener detalle por ID |
| POST | `/api/detalle-ventas` | Crear detalle |
| PUT | `/api/detalle-ventas/{id}` | Actualizar detalle |
| DELETE | `/api/detalle-ventas/{id}` | Eliminar detalle |

### Carrito — `/api/carrito` *(Admin / Cliente)*

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/carrito` | Listar ítems del carrito |
| GET | `/api/carrito/{id}` | Obtener ítem por ID |
| POST | `/api/carrito` | Agregar producto al carrito |
| PUT | `/api/carrito/{id}` | Actualizar ítem |
| DELETE | `/api/carrito/{id}` | Eliminar ítem |

### Usuarios — `/api/usuarios` *(Solo Admin)*

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/usuarios` | Listar usuarios |
| GET | `/api/usuarios/{id}` | Obtener usuario por ID |
| POST | `/api/usuarios` | Crear usuario (contraseña se encripta automáticamente) |
| PUT | `/api/usuarios/{id}` | Actualizar usuario |
| DELETE | `/api/usuarios/{id}` | Eliminar usuario |

### Público

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/public/hola` | Endpoint de prueba, sin autenticación |

---

## Notas de desarrollo

- La base de datos es **en memoria**: los datos se pierden al reiniciar. Para persistencia, cambia el datasource a MySQL o PostgreSQL en `application.properties`.
- El módulo `security/util/JwtUtil.java` está presente pero la autenticación actual usa sesión de formulario, no JWT. Su integración completa queda pendiente.
- CSRF está deshabilitado para facilitar pruebas con clientes REST (Postman, curl, etc.).
