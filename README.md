# Academic Management System (Java EE)

> 🇬🇧 English version below · 🇪🇸 [Versión en español](#sistema-académico-java-ee)

Web application for university academic management: enrollment in degree programs,
courses and exams, with administrative and teaching staff management. Built as the final
project for the **Advanced Programming** course.

## Features

The application handles three roles with differentiated views and permissions:

- **Student**: enroll/withdraw from degree programs, courses and exams; view academic
  history and registered exams.
- **Teacher**: manage course sections (*comisiones*) and exams, list assigned courses.
- **Administrator**: create/disable/enable people, programs, courses, sections and exams;
  assign teachers; general listings.

## Tech stack

- **Java EE** — Servlets + JSP (MVC pattern: `entidades` / `servlets` / `util`)
- **JDBC** over **MySQL / MariaDB**
- **HTML/CSS** for the views (`webapp`)
- Application server: **Apache Tomcat**
- Eclipse *Dynamic Web Project*

## Structure

```
SistemaAcademico/
  src/main/java/
    entidades/   # models (Persona, Carrera, Curso, Examen, Comision, ...)
    servlets/    # controllers by role (alumno/, docente/, admin/)
    util/        # DatabaseConnection (JDBC)
  src/main/webapp/
    alumno/ docente/ admin/   # JSP views by role
    css/ WEB-INF/
db/schema.sql    # schema and data for the "inscripciones" database
docs/Informe.pdf # full project report — Spanish (description, diagrams, screenshots)
```

## Getting started

1. Create the database by importing `db/schema.sql` into MySQL/MariaDB
   (it creates the `inscripciones` database).
2. Configure the connection in
   `src/main/java/util/DatabaseConnection.java` (JDBC URL, user and password).
3. Add the **MySQL Connector/J** driver to `WEB-INF/lib/` (not included in the repo).
4. Import the project into Eclipse and deploy it on **Tomcat**.

> Note: database credentials are hardcoded for academic purposes. In a real environment
> they should be externalized (environment variables / config file).

> Note: source code and comments are in Spanish.

---

# Sistema Académico (Java EE)

> 🇪🇸 Versión en español · 🇬🇧 [English version above](#academic-management-system-java-ee)

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
docs/Informe.pdf # informe del trabajo (descripción, diagramas y capturas)
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

## Autor / Author

Manuel González Janin
