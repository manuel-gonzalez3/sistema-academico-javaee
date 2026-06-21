<%-- Home del alumno. Muestra accesos a inscripción a carrera, cursos, exámenes e historia académica.
     Atributos: ok (String), error (String). --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%
  // Si no hay sesión o el rol no es ALUMNO, redirige al login y corta el render de la página
  String rol = (String) session.getAttribute("userRol");
  String nombre = (String) session.getAttribute("userNombre");
  if (rol == null) {
    response.sendRedirect(request.getContextPath() + "/login.jsp");
    return;
  }
  if (!"ALUMNO".equals(rol)) {
    response.sendRedirect(request.getContextPath() + "/login.jsp");
    return;
  }
  String cp = request.getContextPath();
  String error = (String) request.getAttribute("error");
  String ok = (String) request.getAttribute("ok");
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Panel ALUMNO</title>
  <link rel="stylesheet" href="<%= cp %>/css/styles.css">
</head>
<body>
  <div class="container-page">
    <div class="header">
      <div>
        <h1 class="header-title">Panel ALUMNO</h1>
        <div class="subtitle">Hola, <%= nombre %></div>
      </div>

      <a class="btn btn-outline" href="<%= cp %>/login.jsp">Salir</a>
    </div>
	<%if (ok != null) { %>
      <div class="alert alert-success"><%= ok %></div>
      <% } %>
    <% if (error != null) { %>
      <div class="alert alert-danger"><%= error %></div>
    <% } %>

    <div class="card card--p">
      <h2 class="mb-3">Gestión académica</h2>
      <div class="toolbar">
        <a class="btn btn-primary" href="<%= cp %>/alumno/carrera">Inscripcción a una carrera</a>
        <a class="btn" href="<%= cp %>/alumno/cursos">Inscripcción a un curso</a>
        <a class="btn" href="<%= cp %>/alumno/examenes">Inscripción a un examen</a>
        <a class="btn" href="<%= cp %>/alumno/historiaAcademica">Historia académica</a>
        <a class="btn" href="<%= cp %>/alumno/examenesInscripto">Mis Exámenes</a>
        <%-- Button Baja de Carrera activa el popout, popout submittea el form baja-carrera --%>
        <form id="baja-carrera" method="post" action="<%= cp %>/alumno/carrera/baja">
          <button type="button" class="btn btn-sm btn-danger"
                  popovertarget="popup-baja-carrera">Baja de Carrera</button>
        </form>
        <div id="popup-baja-carrera" popover>
          <p class="pop-msg">¿Confirma la baja de la carrera?</p>
          <div class="pop-actions">
            <button type="submit" form="baja-carrera" class="btn btn-sm btn-danger">Confirmar</button>
            <button type="button" popovertarget="popup-baja-carrera" popovertargetaction="hide" class="btn btn-sm">Cancelar</button>
          </div>
        </div>
      </div>
    </div>
  </div>

</body>
</html>