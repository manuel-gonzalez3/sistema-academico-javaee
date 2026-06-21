<%-- Lista los exámenes en los que el alumno está inscripto con asistencia y nota.
     Atributos: examanes (List<Map>), nombreCarrera, ok, error, errorDB. --%>
<%@ page contentType="text/html; charset=UTF-8" import="java.util.List, java.util.Map" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Mis exámenes inscriptos</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>

<%
  String cp = request.getContextPath();
  String nombreCarrera = (String) request.getAttribute("nombreCarrera");
  List<Map<String, Object>> examanes = (List<Map<String, Object>>) request.getAttribute("examanes");
  String errorDB = (String) request.getAttribute("errorDB");
%>

<div class="container-page">
  <div class="header">
    <h1 class="header-title">Mis exámenes inscriptos</h1>
  </div>

  <% if (nombreCarrera != null && !nombreCarrera.isBlank()) { %>
    <h2>Carrera: <%= nombreCarrera %></h2>
  <% } %>

  <% if (errorDB != null) { %>
    <div class="card card--p alert alert-info"><%= errorDB %></div>
  <% } else { %>
    <div class="card card--p">
      <table class="table">
        <thead>
          <tr>
            <th>Curso</th>
            <th>Fecha</th>
            <th>Hora</th>
            <th>Acciones</th>
            <th>Estado</th>
            <th>Nota</th>
          </tr>
        </thead>
        <tbody>
        <%-- Recorre los exámenes inscriptos y arma una fila por cada uno --%>
        <% for (Map<String, Object> examen : examanes) { %>
          <tr>
            <td data-label="Curso"><%= examen.get("nombreCurso") %></td>
            <td data-label="Fecha"><%= examen.get("fecha") %></td>
            <td data-label="Hora"><%= examen.get("hora") %></td>
            <td data-label="Acciones">
              <%-- Solo si puedeBaja (faltan más de 24hs) se muestra el botón.
                   Button Dar de Baja activa el popout, popout submittea el form baja-examen --%>
              <% if ((Boolean) examen.get("puedeBaja")) { %>
                <form id="baja-examen-<%= examen.get("idExamen") %>"
                      action="<%= cp %>/alumno/bajaExamen" method="post">
                  <input type="hidden" name="id" value="<%= examen.get("idExamen") %>">
                  <button type="button" class="btn btn-sm btn-danger"
                          popovertarget="popup-baja-examen-<%= examen.get("idExamen") %>">Dar de Baja</button>
                </form>
                <div id="popup-baja-examen-<%= examen.get("idExamen") %>" popover>
                  <p class="pop-msg">¿Confirma la baja del examen?</p>
                  <div class="pop-actions">
                    <button type="submit" form="baja-examen-<%= examen.get("idExamen") %>" class="btn btn-sm btn-danger">Confirmar</button>
                    <button type="button" popovertarget="popup-baja-examen-<%= examen.get("idExamen") %>" popovertargetaction="hide" class="btn btn-sm">Cancelar</button>
                  </div>
                </div>
              <% } %>
            </td>
            <td data-label="Asistencia"><%= examen.get("asistencia") %></td>
            <td data-label="Nota"><%= examen.get("nota") %></td>
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
