# Sistema Académico (Java EE)

Aplicación web de gestión académica universitaria: inscripción a carreras, cursos y
exámenes, con gestión administrativa y docente. Desarrollada como trabajo final de la
materia **Programación Avanzada**.

## Funcionalidades

La aplicación maneja tres roles con vistas y permisos diferenciados:

- **Alumno**: inscripción/baja a carreras, cursos y exámenes; consulta de historia
  académica y de exámenes inscriptos.
- **Docente**: gestión de comisiones y exámenes, listado de cursos a cargo.
- **Administrador**: alta/baja/habilitación de personas, carreras, cursos, comisiones y
  exámenes; asignación de docentes; listados generales.

## Stack

- **Java EE** — Servlets + JSP (patrón MVC: `entidades` / `servlets` / `util`)
- **JDBC** sobre **MySQL / MariaDB**
- **HTML/CSS** en las vistas (`webapp`)
- Servidor de aplicaciones: **Apache Tomcat**
- Proyecto Eclipse *Dynamic Web Project*

## Estructura

```
SistemaAcademico/
  src/main/java/
    entidades/   # modelos (Persona, Carrera, Curso, Examen, Comision, ...)
    servlets/    # controladores por rol (alumno/, docente/, admin/)
    util/        # DatabaseConnection (JDBC)
  src/main/webapp/
    alumno/ docente/ admin/   # vistas JSP por rol
    css/ WEB-INF/
db/schema.sql    # esquema y datos de la base "inscripciones"
docs/Informe.pdf # informe del trabajo
```

## Puesta en marcha

1. Crear la base de datos importando `db/schema.sql` en MySQL/MariaDB
   (crea la base `inscripciones`).
2. Configurar la conexión en
   `src/main/java/util/DatabaseConnection.java` (URL, usuario y password JDBC).
3. Agregar el conector **MySQL Connector/J** en `WEB-INF/lib/`
   (no se incluye en el repo).
4. Importar el proyecto en Eclipse y desplegarlo en **Tomcat**.

> Nota: las credenciales de la base están hardcodeadas con fines académicos.
> En un entorno real deberían externalizarse (variables de entorno / archivo de
> configuración).

## Autor

Manuel González Janin
