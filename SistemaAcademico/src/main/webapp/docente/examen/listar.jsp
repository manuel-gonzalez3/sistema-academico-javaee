<%-- Lista los exámenes activos del docente con estado y acciones de gestión.
     Atributos: examenes (List<Examen>), ok, error, errorDB. --%>
<%@ page contentType="text/html; charset=UTF-8" import="java.util.List, entidades.Examen, java.time.LocalDate, java.time.format.DateTimeFormatter" %>
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
  List<Examen> examenes = (List<Examen>) request.getAttribute("examenes");
  String errorDB = (String) request.getAttribute("errorDB");
  String ok = (String) request.getAttribute("ok");
  String error = (String) request.getAttribute("error");
%>

<div class="container-page">
  <div class="header">
    <h1 class="header-title">Exámenes disponibles</h1>
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
            <th>Carrera</th>
            <th>Curso</th>
            <th>Comision</th>
            <th>Fecha</th>
            <th>Hora</th>
            <th>Estado</th>
            <th>Acción</th>
          </tr>
        </thead>
        <tbody>
        <%-- Recorre los exámenes del docente y arma una fila por cada uno --%>
        <% for (Examen examen : examenes) { %>
          <tr>
            <td data-label="Carrera"><%= examen.getNombreCarrera() %></td>
            <td data-label="Curso"><%= examen.getNombreCurso() %></td>
            <td data-label="Comision"><%= examen.getNroComision() %></td>
            <td data-label="Fecha"><%= examen.getFecha() %></td>
            <td data-label="Hora"><%= examen.getHora() %></td>
            <td data-label="Estado"><%= examen.getEstado() %></td>

            <td data-label="Acción">
              <%
                // fechaPasada = true si la fecha del examen ya llegó o pasó (hoy no es anterior a esa fecha)
                boolean fechaPasada = !LocalDate.now().isBefore(
                    LocalDate.parse(examen.getFecha(), DateTimeFormatter.ofPattern("dd/MM/yyyy")));
              %>
              <%-- Según estado + fecha, ofrece la acción correspondiente:
                   PENDIENTE y ya pasó -> cargar asistencia; PENDIENTE y falta -> esperar fecha;
                   RENDIDO -> cargar notas; CERRADO -> ver resultado --%>
              <% if ("PENDIENTE".equalsIgnoreCase(examen.getEstado()) && fechaPasada) { %>
                <a class="btn btn-primary" href="<%= cp %>/docente/examen/gestion?idExamen=<%= examen.getIdExamen() %>&accion=Asistencia">Cargar asistencia</a>
              <% } else if ("PENDIENTE".equalsIgnoreCase(examen.getEstado()) && !fechaPasada) { %>
                <span class="text-muted">Pendiente de fecha</span>
              <% } else if ("RENDIDO".equalsIgnoreCase(examen.getEstado())) { %>
                <a class="btn btn-primary" href="<%= cp %>/docente/examen/gestion?idExamen=<%= examen.getIdExamen() %>&accion=Nota">Cargar notas</a>
              <% } else if ("CERRADO".equalsIgnoreCase(examen.getEstado())) { %>
                <a class="btn btn-primary" href="<%= cp %>/docente/examen/gestion?idExamen=<%= examen.getIdExamen() %>&accion=Resultado">Ver resultado</a>
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
