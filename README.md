# Servidor Switch

Un servidor TCP multi-base de datos que actúa como gateway para enrutar consultas SQL a distintas bases de datos (PostgreSQL y Firebird), utilizando un protocolo XML sobre sockets.

## Descripción

Servidor Switch recibe consultas SQL de los clientes en formato XML, las dirige a la base de datos indicada y devuelve los resultados también en formato XML. Está pensado como ejercicio de programación de sockets en Java con soporte para múltiples bases de datos.

### Características

- Servidor TCP multihilo (pool de 20 hilos) escuchando en el puerto 5000
- Enrutamiento de consultas a **PostgreSQL** (`personal`) o **Firebird** (`facturacion`)
- Pool de conexiones con **HikariCP**
- Protocolo basado en XML
- Cliente interactivo de línea de comandos
- Despliegue con **Docker Compose**

## Tecnologías

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 25 |
| Build | Maven |
| Base de datos 1 | PostgreSQL 16 |
| Base de datos 2 | Firebird 3.0 |
| Pool de conexiones | HikariCP 5.0.1 |
| Contenedores | Docker / Docker Compose |

## Arquitectura

```
Cliente (Cliente.java)
      │
      │  XML sobre TCP (puerto 5000)
      ▼
Servidor Switch (Server.java)
      │
      ├──▶ DbRouter ──▶ PostgreSQL  (base: personal)
      │
      └──▶ DbRouter ──▶ Firebird    (base: facturacion)
```

### Protocolo XML

**Solicitud del cliente:**

```xml
<query>
  <database>personal</database>
  <sql>SELECT * FROM empleados;</sql>
</query>
```

**Respuesta del servidor:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<query>
  <cols>
    <id>id</id>
    <nombre>nombre</nombre>
  </cols>
  <rows>
    <row1>
      <col1>1</col1>
      <col2>Ana</col2>
    </row1>
  </rows>
</query>
```

## Estructura del proyecto

```
servidor-switch/
├── switch/                  # Proyecto Maven (fuente Java)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/bd2/
│       │   ├── Server.java          # Entry point del servidor
│       │   ├── RequestHandler.java  # Manejo de conexiones entrantes
│       │   ├── DbRouter.java        # Enrutador y pool de conexiones
│       │   ├── XmlUtils.java        # Serialización/deserialización XML
│       │   └── Cliente.java         # Cliente interactivo
│       └── resources/
│           └── switch.properties    # Configuración del servidor
├── dbs/
│   ├── postgres/personal.sql        # Schema y datos PostgreSQL
│   └── firebird/firebird.sql        # Schema y datos Firebird
├── Dockerfile.server                # Imagen Docker del servidor
├── docker-compose.yml               # Orquestación de servicios
├── switch.jar                       # JAR precompilado
└── cliente.bat                      # Lanzador del cliente (Windows)
```

## Requisitos previos

- **Java 25** (Eclipse Temurin o equivalente)
- **Maven 3.6+**
- **Docker y Docker Compose** (para el despliegue en contenedores)

## Instalación y ejecución

### Con Docker Compose (recomendado)

Levanta el servidor junto con ambas bases de datos con un solo comando:

```bash
docker-compose up
```

Esto inicializa automáticamente:
- PostgreSQL en `10.0.0.2:5432` con la base de datos `personal`
- Firebird en `10.0.0.3:3050` con la base de datos `facturacion`
- El servidor Switch en `10.0.0.4:5000`

### Compilar desde el código fuente

```bash
cd switch
mvn clean package
```

El JAR resultante estará en `switch/target/switch-shaded.jar`.

### Ejecutar el servidor manualmente

```bash
java -cp switch.jar com.bd2.Server
```

> Asegúrate de que las bases de datos estén corriendo y que `switch.properties` apunte a las direcciones correctas.

## Configuración

Edita `switch/src/main/resources/switch.properties` antes de compilar:

```properties
server.port=5000

postgres.url=jdbc:postgresql://10.0.0.2:5432/personal
postgres.user=postgres
postgres.pass=masterkey

firebird.url=jdbc:firebirdsql://10.0.0.3:3050/facturacion
firebird.user=SYSDBA
firebird.pass=masterkey

pool.size=10
```

## Uso del cliente

### Windows

```batch
cliente.bat
```

### Linux / macOS

```bash
java -cp switch.jar com.bd2.Cliente
```

### Ejemplo de sesión

```
Nombre de la base de datos: personal
Consulta SQL: SELECT * FROM empleados;
[respuesta XML con los datos]

Consulta SQL: exit
```

Bases de datos disponibles: `personal` (PostgreSQL) y `facturacion` (Firebird).

## Bases de datos de ejemplo

### personal (PostgreSQL)

| Tabla | Descripción |
|---|---|
| `departamentos` | Departamentos de la empresa |
| `empleados` | Empleados con salario y departamento |

### facturacion (Firebird)

| Tabla | Descripción |
|---|---|
| `clientes` | Clientes |
| `productos` | Productos |
| `facturas` | Cabecera de facturas |
| `detalle_factura` | Detalle de cada factura |

## Licencia

Este proyecto no cuenta con una licencia especificada.
