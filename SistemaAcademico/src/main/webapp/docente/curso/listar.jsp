<%-- Lista los cursos del docente con sus comisiones, cupo e inscriptos.
     Atributos: cursos (List<Map>), ok, error, errorDB. --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*" %>
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
  List<Map<String, Object>> cursos =
      (List<Map<String, Object>>) request.getAttribute("cursos");
  String errorDB = (String) request.getAttribute("errorDB");
  String ok = (String) request.getAttribute("ok");
  String error = (String) request.getAttribute("error");
%>

<div class="container-page">
  <div class="header">
    <h1 class="header-title">Mis Cursos</h1>
  </div>

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
            <th>Carrera</th>
            <th>Comisión</th>
            <th>Cupo</th>
            <th>Estado</th>
            <th>Acción</th>
          </tr>
        </thead>
        <tbody>
          <%-- Recorre los cursos del docente y arma una fila por cada uno --%>
          <% for (Map<String, Object> c : cursos) {
               Object idComision = c.get("idComision");
               Object nroComision = c.get("nroComision");
          %>
            <tr>
              <td data-label="Curso"><%= c.get("nombre") %></td>
              <td data-label="Carrera"><%= c.get("carrera") %></td>
              <td data-label="Comisión"><%= nroComision != null ? "Comisión " + nroComision : "-" %></td>
              <td data-label="Cupo">
                <%= c.get("inscriptos") + " / " + c.get("cupo") %>
              </td>
              <%-- Muestra el badge según el estado del curso (activo / inhabilitado) --%>
              <td data-label="Estado">
                <% if (Boolean.TRUE.equals(c.get("activo"))) { %>
                  <span class="badge badge-success">Activo</span>
                <% } else { %>
                  <span class="badge badge-secondary">Inhabilitado</span>
                <% } %>
              </td>
              <%-- Solo si hay comisión se ofrece "Cerrar cursado"; si no, muestra "-" --%>
              <td data-label="Acción">
                <% if (idComision != null) { %>
                  <a class="btn btn-primary" href="<%= cp %>/docente/curso/comision/alumnos?idComision=<%= idComision %>">Cerrar cursado</a>
                <% } else { %>
                  -
                <% } %>
              </td>
            </tr>
          <% } %>
        </tbody>
      </table>
    </div>
  <% } %>

  <div class="mt-3 right">
    <a class="btn" href="<%= cp %>/docente/home.jsp">Volver</a>
  </div>
</div>
</body>
</html>