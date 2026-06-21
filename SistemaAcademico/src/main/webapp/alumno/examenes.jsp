<%-- Lista los exámenes disponibles para inscripción (activos, > 24hs, habilitado).
     Atributos: examenes (List<Examen>), nombreCarrera, ok, error, errorDB. --%>
<%@ page contentType="text/html; charset=UTF-8" import="java.util.List,entidades.Examen" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Mis exámenes</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>

<%
  String cp = request.getContextPath();
  String nombreCarrera = (String) request.getAttribute("nombreCarrera");
  List<Examen> examenes = (List<Examen>) request.getAttribute("examenes");
  String errorDB = (String) request.getAttribute("errorDB");
  String ok = (String) request.getAttribute("ok");
  String error = (String) request.getAttribute("error");
%>

<div class="container-page">
  <div class="header">
    <h1 class="header-title">Exámenes disponibles</h1>
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
            <th>Fecha</th>
            <th>Hora</th>
            <th>Acción</th>
          </tr>
        </thead>
        <tbody>
        <%-- Recorre los exámenes disponibles y arma una fila con su botón de inscripción --%>
        <% for (Examen examen : examenes) { %>
          <tr>
            <td data-label="Curso"><%= examen.getNombreCurso() %></td>
            <td data-label="Docente"><%= examen.getNombreDocente() %></td>
            <td data-label="Fecha"><%= examen.getFecha() %></td>
            <td data-label="Hora"><%= examen.getHora() %></td>
            <td data-label="Acción">
              <form method="post" action="<%= cp %>/alumno/examenes" style="margin:0;">
                <input type="hidden" name="idExamen" value="<%= examen.getIdExamen() %>">
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
