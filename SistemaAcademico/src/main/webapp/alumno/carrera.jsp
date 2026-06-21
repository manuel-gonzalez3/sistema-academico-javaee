<%-- Formulario de inscripción a carrera. Muestra las carreras activas disponibles.
     Atributos: carreras (List<Carrera>), ok, error, errorDB. --%>
<%@ page contentType="text/html; charset=UTF-8" import="java.util.*" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Mi carrera</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>

<%
  String cp = request.getContextPath();
  List<entidades.Carrera> carreras =
      (List<entidades.Carrera>) request.getAttribute("carreras");

  String errorDB = (String) request.getAttribute("errorDB");
  String error = (String) request.getAttribute("error");
  String ok = (String) request.getAttribute("ok");
%>

<div class="container-page">
  <div class="header">
    <h1 class="header-title">Mi carrera</h1>
  </div>

  <% if (error != null) { %>
    <div class="alert alert-danger"><%= error %></div>
  <% } %>

  <% if (ok != null) { %>
    <div class="alert alert-success"><%= ok %></div>
  <% } %>

  <% if (errorDB != null) { %>
    <div class="card card--p alert alert-info"><%= errorDB %></div>
  <% } %>

  <div class="card card--p">
    <form id="form-inscribir-carrera" method="post" action="<%= cp %>/alumno/carrera" class="form">

      <div class="form-row">
        <label class="label" for="idCarrera">Seleccionar carrera</label>
        <select id="idCarrera" name="idCarrera" class="select" required>
          <option value="">Seleccione...</option>

          <%-- Carga un <option> por cada carrera activa disponible --%>
          <% if (carreras != null) {
               for (entidades.Carrera carrera : carreras) { %>
            <option value="<%= carrera.getIdCarrera() %>">
              <%= carrera.getNombre() %>
            </option>
          <%   }
             } %>

        </select>
      </div>

      <%-- Si no hay carreras el botón queda disabled; si hay, abre el popout de confirmación --%>
      <% if (carreras == null || carreras.isEmpty()) { %>
        <div class="alert alert-warning mt-2">
          No hay carreras activas disponibles para inscripción.
        </div>

        <button type="button" class="btn btn-primary mt-2" disabled>
          Inscribirme
        </button>
      <% } else { %>
        <%-- Button Inscribirme activa el popout, popout submittea el form form-inscribir-carrera --%>
        <button type="button" class="btn btn-primary mt-2"
                popovertarget="popup-inscribir-carrera">
          Inscribirme
        </button>
        <div id="popup-inscribir-carrera" popover>
          <p class="pop-msg">¿Confirma la inscripción a la carrera?</p>
          <div class="pop-actions">
            <button type="submit" form="form-inscribir-carrera" class="btn btn-sm btn-primary">Confirmar</button>
            <button type="button" popovertarget="popup-inscribir-carrera" popovertargetaction="hide" class="btn btn-sm">Cancelar</button>
          </div>
        </div>
      <% } %>

    </form>
  </div>

  <div class="mt-3 right">
    <a class="btn" href="<%= cp %>/alumno/home.jsp">Volver</a>
  </div>
</div>

</body>
</html>
