<%-- Home del administrador. Menú principal con accesos a gestión de carreras, docentes y alumnos. --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%
  // Si no hay sesión o el rol no es ADMIN, redirige al login y corta el render de la página
  String rol = (String) session.getAttribute("userRol");
  String nombre = (String) session.getAttribute("userNombre");
  if (rol == null || !"ADMIN".equals(rol)) {
    response.sendRedirect(request.getContextPath() + "/login.jsp");
    return;
  }
  String error = request.getParameter("error");
  String cp = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Panel ADMIN</title>
  <link rel="stylesheet" href="<%= cp %>/css/styles.css">
</head>
<body>
  <div class="container-page">
    <div class="header">
      <div>
        <h1 class="header-title">Panel ADMIN</h1>
        <div class="subtitle">Hola, <%=nombre%></div>
      </div>
	
      <form action="<%= cp %>/login.jsp" method="post" class="inline-flex">
        <button class="btn btn-outline" type="submit">Salir</button>
      </form>
    </div>
    <div class="card card--p">
      <% if (error != null && !error.isEmpty()) { %>
        <div class="error-msg"><%=error%></div>
	  <% } %>
      <h2 class="mb-3">Gestión académica</h2>
      <div class="toolbar">
        <a class="btn btn-primary" href="<%= cp %>/carrera/listar">Carreras</a>
        <a class="btn" href="<%= cp %>/alumno/listar">Alumnos</a>
        <a class="btn" href="<%= cp %>/docente/listar">Docentes</a>
        <a class="btn" href="<%= cp %>/examen/listar">Exámenes</a>
      </div>
    </div>

  </div>
</body>
</html>
