<%-- Lista los alumnos inscriptos en una comisión con su estado y nota.
     Atributos: alumnos (List<Map>: dni, nombre, apellido, estado), idComision, idCurso, errorDB. --%>
<%@ page contentType="text/html; charset=UTF-8" import="java.util.*" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Alumnos inscriptos</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>

<%
  String cp = request.getContextPath();
  List<Map<String, Object>> alumnos = (List<Map<String, Object>>) request.getAttribute("alumnos");
  String errorDB   = (String) request.getAttribute("errorDB");
  Integer idComision = (Integer) request.getAttribute("idComision");
  Integer idCurso    = (Integer) request.getAttribute("idCurso");
%>

<div class="container-page">
  <div class="header">
    <h1 class="header-title">Alumnos inscriptos</h1>
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
            <th>Estado de cursado</th>
          </tr>
        </thead>
        <tbody>
        <%-- Recorre los alumnos de la comisión y arma una fila por cada uno --%>
        <% for (Map<String, Object> alumno : alumnos) { %>
          <tr>
            <td data-label="DNI"><%= alumno.get("dni") %></td>
            <td data-label="Nombre"><%= alumno.get("nombre") %></td>
            <td data-label="Apellido"><%= alumno.get("apellido") %></td>
            <td data-label="Estado de cursado"><%= alumno.get("estado") %></td>
          </tr>
        <% } %>
        </tbody>
      </table>
    </div>
  <% } %>

  <div class="mt-3 right">
    <a class="btn"
       href="<%= cp %>/comision/listar?idCurso=<%= idCurso == null ? "" : idCurso %>">
      Volver
    </a>
  </div>
</div>

</body>
</html>
