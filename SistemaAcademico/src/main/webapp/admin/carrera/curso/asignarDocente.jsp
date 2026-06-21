<%-- Formulario para asignar o modificar el docente de un curso.
     El botón "Quitar docente" envía sin dniDocente → el servlet hace SET NULL.
     Atributos: docentes (List<Persona>), idCurso, idCarrera, cursoNombre, docenteAsignado. --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Asignar Docente</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>
<%
  String cp = request.getContextPath();
  Integer idCurso = (Integer) request.getAttribute("idCurso");
  Integer idCarrera = (Integer) request.getAttribute("idCarrera");
  String cursoNombre = (String) request.getAttribute("cursoNombre");

  List<entidades.Persona> docentes = (List<entidades.Persona>) request.getAttribute("docentes");

  Integer docenteAsignado = (Integer) request.getAttribute("docenteAsignado");

  String error = request.getParameter("error");
  String ok = request.getParameter("ok");
  String errorAttr = (String) request.getAttribute("error");
%>

<div class="container-page">
  <div class="header">
    <h1 class="header-title">Asignar Docente</h1>
  </div>

  <% if (cursoNombre != null) { %>
    <div class="card card--p alert alert-info">
      Curso: <strong><%= cursoNombre %></strong>
    </div>
  <% } %>

  <% if (error != null) { %>
    <div class="alert alert-danger"><%= error %></div>
  <% } %>

  <% if (errorAttr != null) { %>
    <div class="alert alert-danger"><%= errorAttr %></div>
  <% } %>

  <% if (ok != null) { %>
    <div class="alert alert-success"><%= ok %></div>
  <% } %>

  <div class="card card--p">
    <form action="<%= cp %>/curso/asignarDocente" method="post">

      <input type="hidden" name="idCurso" value="<%= idCurso %>">
      <input type="hidden" name="idCarrera" value="<%= idCarrera %>">

      <table class="table">
        <thead>
          <tr>
            <th>Seleccionar</th>
            <th>Docente</th>
            <th>DNI</th>
          </tr>
        </thead>
        <tbody>
        <%-- Si hay docentes muestra un radio por cada uno; si no, una fila avisando que no hay --%>
        <% if (docentes != null && !docentes.isEmpty()) { %>
          <% for (entidades.Persona d : docentes) { %>
            <tr>
              <td data-label="Seleccionar">
                <input type="radio"
			       name="dniDocente"
			       value="<%= d.getDni() %>"
			       <%= (docenteAsignado != null && docenteAsignado.equals(d.getDni())) ? "checked" : "" %>> <!-- Marca el docente asignado -->
		       </td>
              <td data-label="Docente">
                <%= d.getApellido() %>, <%= d.getNombre() %>
              </td>
              <td data-label="DNI"><%= d.getDni()%></td>
            </tr>
          <% } %>
        <% } else { %>
          <tr>
            <td colspan="3">No hay docentes disponibles.</td>
          </tr>
        <% } %>
        </tbody>
      </table>

      <div class="actions">
        <button class="btn btn-primary"
                type="submit"
                name="accion"
                value="asignar">
          Asignar docente
        </button>

        <% if (docenteAsignado != null) { %>
          <%-- accion=quitar → el servlet hace SET NULL aunque el radio siga marcado --%>
          <button type="submit" class="btn btn-danger"
                  name="accion" value="quitar">
            Quitar docente
          </button>
        <% } %>

        <a class="btn" style="margin-left:auto" href="<%= cp %>/curso/listar?idCarrera=<%= idCarrera %>">Volver</a>

  </div>
</div>

</body>
</html>
