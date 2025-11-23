# Manage Projects

Aplicación Spring Boot para la gestión de proyectos, tareas y usuarios. Incluye auditoría de entidades con Hibernate Envers, Redis como caché y PostgreSQL como base de datos.

## Requisitos

- Java 21
- Maven 3.9+ (o el wrapper incluido `mvnw` / `mvnw.cmd`)
- Docker y Docker Compose (para levantar Postgres y Redis rápidamente)

## Configuración

La configuración por defecto está en `src/main/resources/application.yaml`. Puedes sobreescribirla con variables de entorno:

- DB_HOST (por defecto: `localhost`)
- DB_PORT (por defecto: `5432`)
- DB_NAME (por defecto: `manageprojects`)
- DB_USER (por defecto: `manage`)
- DB_PASSWORD (por defecto: `manage`)
- SERVER_PORT (por defecto: `8080`)

## Arranque de dependencias con Docker Compose

El repositorio incluye `docker-compose.yml` para levantar PostgreSQL y Redis.

1. Inicia los contenedores (Windows PowerShell):
   - `docker compose up -d`

2. Comprueba que están sanos:
   - `docker ps`

Esto expondrá:
- Postgres en `localhost:5432`
- Redis en `localhost:6379`

## Ejecutar la aplicación

Opción A: Ejecutar con Maven (recomendado en desarrollo)

- Windows (PowerShell): `./mvnw.cmd spring-boot:run`
- Linux/macOS: `./mvnw spring-boot:run`

Opción B: Compilar JAR y ejecutar

1. Compila: `./mvnw.cmd clean package -DskipTests`
2. Ejecuta: `java -jar target/manage-projects-0.0.1-SNAPSHOT.jar`

Opción C: Construir imagen Docker con Spring Boot (Paketo Buildpacks)

1. Construye la imagen: `./mvnw.cmd spring-boot:build-image -DskipTests`
   - La imagen se llamará `docker.io/library/manage-projects:0.0.1-SNAPSHOT` (o similar)
2. Con dependencias por Compose ya levantadas, ejecuta el contenedor:
   - `docker run --rm -p 8080:8080 --name manage-app --network manage-projects_manage-net -e DB_HOST=postgres -e DB_PORT=5432 -e DB_NAME=manageprojects -e DB_USER=manage -e DB_PASSWORD=manage manage-projects:0.0.1-SNAPSHOT`
   - Nota: La red puede variar. Consulta `docker network ls` y ajusta el nombre o usa `--network host` en Linux.

## Endpoints principales (REST)

Base path: `/api/v1`

- Proyectos (`/proyectos`)
  - POST `/` crear proyecto
  - PUT `/{id}` actualizar
  - GET `/{id}` obtener por id
  - GET `/` listar todos
  - DELETE `/{id}` eliminar
  - GET `/activos?desde=2025-01-01T00:00:00&hasta=2025-12-31T23:59:59` activos en rango
  - GET `/page` paginado con `ProyectoFilter`
  - GET `/por-tareas` filtrar por atributos de tareas (estado, fechas, título, asignadoEmail, etiqueta)
  - GET `/por-tareas-stream` igual que anterior usando streams
  - POST `/cerrar-completos?fechaFin=2025-01-01T00:00:00` cerrar por fecha fin
  - DELETE `/sin-tareas?desde=...&hasta=...` purgar proyectos sin tareas
  - GET `/by-miembros?rolEnProyecto=...&emailLike=...` búsqueda por miembros
  - GET `/min-tareas?estado=...&minTareas=10&etiquetaNombre=...` con mínimo de tareas
  - POST `/reabrir-pendientes` reabrir pendientes
  - POST `/init-fecha-inicio?fecha=...` inicializar fecha inicio
  - GET `/sin-comentarios-miembro?rol=OWNER` sin comentarios por rol de miembro
  - DELETE `/presupuesto-inconsistente` eliminar presupuestos inconsistentes

- Tareas (`/tareas`)
  - POST `/` crear
  - PUT `/{id}` actualizar
  - GET `/{id}` obtener por id
  - GET `/` listar todos
  - DELETE `/{id}` eliminar
  - GET `/search?estado=PENDIENTE&etiquetaId=1&asignadoId=2&proyectoId=3`
  - GET `/page` paginado con `TareaFilter`
  - POST `/bulk/update-estado?estadoOrigen=...&estadoDestino=...&proyectoId=...&fechaLimiteAntes=...` actualizar masivo
  - DELETE `/bulk/by-estado-fecha?estado=...&fechaLimiteAntes=...` borrar masivo
  - POST `/bulk/reasignar?fromUsuarioId=1&toUsuarioId=2&proyectoId=...&estado=...` reasignar

- Usuarios (`/usuarios`)
  - POST `/` crear
  - PUT `/{id}` actualizar
  - GET `/{id}` obtener
  - GET `/` listar todos
  - DELETE `/{id}` eliminar
  - GET `/search?rol=ADMIN&desde=2025-01-01T00:00:00&hasta=2025-12-31T23:59:59` buscar por rol y rango de creación

Notas:
- Todas las fechas usan formato ISO-8601: `yyyy-MM-dd'T'HH:mm:ss`.
- Respuestas usan JSON y vistas Jackson según `com.alejandro.manageprojects.view.View`.

## Auditoría con Hibernate Envers

Todas las entidades principales están anotadas con `@Audited`. Al arrancar, Hibernate creará las tablas `*_AUD` y `REVINFO`.

Utilidad disponible: `com.alejandro.manageprojects.view.AuditUtils` expone métodos para consultar el historial:

- `getRevisionNumbers(Class<T>, ID)` → números de revisión
- `getRevisions(Class<T>, ID)` → snapshots en cada revisión
- `getAtRevision(Class<T>, ID, Number)` → versión en revisión concreta
- `getLastRevision(Class<T>, ID)` → última versión
- `getAtDate(Class<T>, ID, LocalDateTime)` → estado en una fecha
- `getRevisionNumbersBetween(Class<T>, ID, from, to)` → revisiones entre fechas
- `getRevisionsBetween(Class<T>, ID, from, to)` → versiones entre fechas
- `getRevisionTimestamps(Class<?>, Object)` → mapa rev → fecha
- `getFirstChangeDate(Class<?>, Object)` y `getLastChangeDate(Class<?>, Object)`

Ejemplo de uso (pseudo):

```java
@Autowired AuditUtils auditUtils;

List<Number> revs = auditUtils.getRevisionNumbers(Proyecto.class, 10L);
Proyecto al10Ene = auditUtils.getAtDate(Proyecto.class, 10L, LocalDateTime.parse("2025-01-10T00:00:00"));
```

## Guía rápida de uso (con ejemplos)

A continuación algunos ejemplos prácticos usando curl. Todas las fechas usan ISO-8601.

Notas para Windows PowerShell: al pasar JSON, utiliza comillas simples para el cuerpo y dobles dentro del JSON, por ejemplo: `-d '{"campo":"valor"}'`.

1) Crear un usuario

- curl (Linux/macOS):

```
curl -X POST http://localhost:8080/api/v1/usuarios \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ana@example.com",
    "nombre": "Ana",
    "apellido": "Pérez",
    "perfil": { "telefono": "+34 600 111 222", "direccion": "C/ Sol 1" }
  }'
```

2) Crear un proyecto con presupuesto

```
curl -X POST http://localhost:8080/api/v1/proyectos \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Lanzamiento Web",
    "descripcion": "Nuevo sitio",
    "fechaInicio": "2025-01-10T09:00:00",
    "presupuesto": { "montoTotal": 15000 }
  }'
```

3) Crear una tarea asociada al proyecto y asignarla a un usuario

Nota: los DTOs de ejemplo incluyen `proyecto` y `asignadoA` completos. Dado que los mappers evitan back-references automáticas, la capa de servicio se encarga de resolver la asociación por id; asegúrate de enviar al menos el id.

```
curl -X POST http://localhost:8080/api/v1/tareas \
  -H "Content-Type: application/json" \
  -d '{
    "titulo": "Maquetar landing",
    "descripcion": "HTML + CSS",
    "estado": "PENDIENTE",
    "fechaLimite": "2025-02-01T18:00:00",
    "proyecto": { "id": 1 },
    "asignadoA": { "id": 1 }
  }'
```

4) Filtrar tareas por estado y proyecto

```
curl "http://localhost:8080/api/v1/tareas/search?estado=PENDIENTE&proyectoId=1"
```

5) Paginación de proyectos (page=0, size=10)

```
curl "http://localhost:8080/api/v1/proyectos/page?page=0&size=10"
```

6) Actualizar un proyecto (por ejemplo, establecer fecha de fin)

```
curl -X PUT http://localhost:8080/api/v1/proyectos/1 \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "nombre": "Lanzamiento Web",
    "fechaFin": "2025-03-15T17:00:00"
  }'
```

7) Consultar auditoría (Hibernate Envers) vía API

- Revisiones existentes de un proyecto id=1:

```
curl "http://localhost:8080/api/v1/audit/proyecto/1/revisions"
```

- Timestamps de revisiones:

```
curl "http://localhost:8080/api/v1/audit/proyecto/1/timestamps"
```

- Snapshot del proyecto en la revisión 2:

```
curl "http://localhost:8080/api/v1/audit/proyecto/1/at-revision/2"
```

- Snapshot del proyecto a fecha/hora concreta:

```
curl "http://localhost:8080/api/v1/audit/proyecto/1/at-date?dateTime=2025-02-01T12:00:00"
```

- Snapshots entre fechas:

```
curl "http://localhost:8080/api/v1/audit/proyecto/1/between?from=2025-01-01T00:00:00&to=2025-12-31T23:59:59"
```

Los endpoints devuelven mapas planos seguros para JSON: los campos simples aparecen directamente; las relaciones se representan como `campoId` o `campoIds` para colecciones.

## Pruebas rápidas

1. Levanta Postgres y Redis con Docker Compose.
2. Ejecuta la app con Maven.
3. Crea entidades con los endpoints y verifica tablas `_AUD` en la base de datos.

## Troubleshooting

- Si no conecta a Postgres, valida variables `DB_*` y que el contenedor `manage-postgres` esté UP.
- Si usas otro puerto de base de datos, ajusta `DB_PORT`.
- Para ver SQLs: incrementa `logging.level.org.hibernate.SQL=debug` en `application.yaml`.
