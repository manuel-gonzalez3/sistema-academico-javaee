<%-- Lista los cursos disponibles de la carrera del alumno para inscripción.
     Atributos: cursos (List<Curso>), nombreCarrera, ok, error, errorDB. --%>
<%@ page contentType="text/html; charset=UTF-8" import="java.util.List,entidades.Curso" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Mis cursos</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>

<%
  String cp = request.getContextPath();
  String nombreCarrera = (String) request.getAttribute("nombreCarrera");
  List<Curso> cursos = (List<Curso>) request.getAttribute("cursos");
  String errorDB = (String) request.getAttribute("errorDB");
  String ok = (String) request.getAttribute("ok");
  String error = (String) request.getAttribute("error");
%>

<div class="container-page">
  <div class="header">
    <h1 class="header-title">Cursos pendientes</h1>
  </div>

  <% if (nombreCarrera != null && !nombreCarrera.isBlank()) { %>
    <h2>Carrera: <%= nombreCarrera %></h2>
  <% } %>

  <% if (ok != null) { %>
    <div class="card card--p alert alert-success"><%= ok %></div>
  <% } %>

  <% if (error != null) { %>
    <div class="card card--p alert alert-danger"><%= error %></div>
  <% } %>

  <% if (errorDB != null) { %>
    <div class="card card--p alert alert-info"><%= errorDB %></div>
  <% } else { %>
    <div class="card card--p">
      <table class="table">
        <thead>
          <tr>
            <th>Curso</th>
            <th>Docente</th>
            <th>Acción</th>
          </tr>
        </thead>
        <tbody>
        <%-- Recorre los cursos pendientes y arma una fila con su botón de inscripción --%>
        <% for (Curso curso : cursos) { %>
          <tr>
            <td data-label="Curso"><%= curso.getNombre() %></td>
            <td data-label="Docente"><%= curso.getNombreDocente() %></td>
            <td data-label="Acción">
              <form method="post" action="<%= cp %>/alumno/cursos" style="margin:0;">
                <input type="hidden" name="idCurso" value="<%= curso.getIdCurso() %>">
                <button type="submit" class="btn btn-primary">Inscribirme</button>
              </form>
            </td>
          </tr>
        <% } %>
        </tbody>
      </table>
    </div>
  <% } %>

  <div class="mt-3 right">
    <a class="btn" href="<%= cp %>/alumno/home.jsp">Volver</a>
  </div>
</div>

</body>
</html>