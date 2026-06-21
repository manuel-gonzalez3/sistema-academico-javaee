<%-- Muestra el historial académico del alumno: cursos, comisión, estado y nota.
     Atributos: cursos (List<Map>: idCurso, nombre, estado, nota, nroComision), nombreCarrera, errorDB. --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List, java.util.Map" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Historia Académica</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>

<%
  String cp = request.getContextPath();
  String nombreCarrera = (String) request.getAttribute("nombreCarrera");
  List<Map<String, Object>> cursos = (List<Map<String, Object>>) request.getAttribute("cursos");
  String errorDB = (String) request.getAttribute("errorDB");
  String ok = (String) request.getAttribute("ok");
  String error = (String) request.getAttribute("error");
%>

<div class="container-page">
  <div class="header">
    <h1 class="header-title">Historia Académica</h1>
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
            <th>Comisión</th>
            <th>Estado</th>
            <th>Nota</th>
          </tr>
        </thead>
        <tbody>
          <%-- Recorre cada curso de la historia académica y arma una fila --%>
          <% for (Map<String, Object> curso : cursos) {
               String estado = String.valueOf(curso.get("estado"));
          %>
          <tr>
            <td data-label="Curso"><%= curso.get("nombre") %></td>
            <td data-label="Comisión">Comisión <%= curso.get("nroComision") %></td>
            <%-- Asigna el color del badge según el estado del curso; si es uno no contemplado lo muestra tal cual --%>
            <td data-label="Estado">
              <% if ("promocionado".equalsIgnoreCase(estado)) { %>
                <span class="badge badge-success">Promocionado</span>
              <% } else if ("regular".equalsIgnoreCase(estado)) { %>
                <span class="badge badge-warning">Regular</span>
              <% } else if ("cursando".equalsIgnoreCase(estado)) { %>
                <span class="badge badge-info">Cursando</span>
              <% } else if ("libre".equalsIgnoreCase(estado)) { %>
                <span class="badge badge-danger">Libre</span>
              <% } else if ("aprobado".equalsIgnoreCase(estado)) { %>
              	<span class="badge badge-success">Aprobado</span>
              <% } else { %>
                <span class="badge badge-secondary"><%= estado %></span>
              <% } %>
            </td>
            <td data-label="Nota"><%= curso.get("nota") != null ? curso.get("nota") : "-" %></td>
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
