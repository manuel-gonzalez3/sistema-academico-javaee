<%-- Lista los cursos asignados a un docente con carrera y estado.
     Atributos: cursos (List<Map>: nombre, activo, carrera), errorDB. --%>
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
%>

<div class="container-page">
  <div class="header">
    <h1 class="header-title">Cursos del docente</h1>
  </div>

  <% if (errorDB != null) { %>
    <div class="card card--p alert alert-info"><%= errorDB %></div>
  <% } else { %>
    <div class="card card--p">
      <table class="table">
        <thead>
          <tr>
            <th>Carrera</th>
            <th>Curso</th>
            <th>Estado</th>
          </tr>
        </thead>
        <tbody>
          <%-- Recorre los cursos del docente y arma una fila por cada uno --%>
          <% for (Map<String, Object> c : cursos) { %>
            <tr>
              <td data-label="Carrera"><%= c.get("carrera") %></td>
              <td data-label="Curso"><%= c.get("nombre") %></td>
              <%-- Muestra el badge según el estado del curso (activo / inactivo) --%>
              <td data-label="Estado">
                <% if (Boolean.TRUE.equals(c.get("activo"))) { %>
                  <span class="badge badge-success">Activo</span>
                  <% } else { %>
                  <span class="badge badge-secondary">Inactivo</span>
                  <% } %>
           	  </td>
            </tr>
          <% } %>
        </tbody>
      </table>
    </div>
  <% } %>

  <div class="mt-3 right">
    <a class="btn" href="<%= cp %>/docente/listar">Volver</a>
  </div>
</div>
</body>
</html>