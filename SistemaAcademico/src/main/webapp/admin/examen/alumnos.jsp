<%-- Lista los alumnos de un examen con asistencia y nota.
     Atributos: alumnos (List<Map>), idExamen, idComision, errorDB. --%>
<%@ page contentType="text/html; charset=UTF-8" import="java.util.*" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Alumnos inscriptos en examen</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>

<%
  String cp = request.getContextPath();
  List<Map<String, Object>> alumnos = (List<Map<String, Object>>) request.getAttribute("alumnos");
  String errorDB    = (String)  request.getAttribute("errorDB");
  Integer idExamen   = (Integer) request.getAttribute("idExamen");
  Integer idComision = (Integer) request.getAttribute("idComision");
%>

<div class="container-page">
  <div class="header">
    <h1 class="header-title">Alumnos inscriptos en examen</h1>
  </div>

  <% if (errorDB != null) { %>
    <div class="card card--p alert alert-info"><%= errorDB %></div>
  <% } else { %>
    <div class="card card--p">
      <table class="table">
        <thead>
          <tr>
            <th>DNI</th>
            <th>Nombre</th>
            <th>Apellido</th>
            <th>Estado</th>
            <th>Nota</th>
          </tr>
        </thead>
        <tbody>
        <%-- Recorre los alumnos inscriptos al examen y arma una fila por cada uno --%>
        <% for (Map<String, Object> alumno : alumnos) { %>
          <tr>
            <td data-label="DNI"><%= alumno.get("dni") %></td>
            <td data-label="Nombre"><%= alumno.get("nombre") %></td>
            <td data-label="Apellido"><%= alumno.get("apellido") %></td>
            <td data-label="Asistencia"><%= alumno.get("asistencia") %></td>
            <td data-label="Nota"><%= alumno.get("nota") %></td>
          </tr>
        <% } %>
        </tbody>
      </table>
    </div>
  <% } %>

  <div class="mt-3 right">
    <a class="btn"
       href="<%= cp %>/examen/listar?idComision=<%= idComision == null ? "" : idComision %>">
      Volver
    </a>
  </div>
</div>

</body>
</html>
