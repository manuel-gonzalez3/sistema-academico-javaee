<%-- Home del docente. Muestra accesos a gestión de cursos y exámenes. --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%
  // Si no hay sesión o el rol no es DOCENTE, redirige al login y corta el render de la página
  String rol = (String) session.getAttribute("userRol");
  String nombre = (String) session.getAttribute("userNombre");
  if (rol == null || !"DOCENTE".equals(rol)) {
    response.sendRedirect(request.getContextPath() + "/login.jsp");
    return;
  }
  String cp = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Panel Docente</title>
  <link rel="stylesheet" href="<%= cp %>/css/styles.css">
</head>
<body>
  <div class="container-page">
    <div class="header">
      <div>
        <h1 class="header-title">Panel Docente</h1>
        <div class="subtitle">Hola, <%=nombre%></div>
      </div>

      <form action="<%= cp %>/login.jsp" method="post" class="inline-flex">
        <button class="btn btn-outline" type="submit">Salir</button>
      </form>
    </div>
    <div class="card card--p">
      <h2 class="mb-3">Gestión académica</h2>
      <div class="toolbar">
        <a class="btn btn-primary" href="<%= cp %>/docente/curso/listar">Mis cursos</a>
        <a class="btn btn-primary" href="<%= cp %>/docente/examen/listar">Mis exámenes</a>
      </div>
    </div>

  </div>
</body>
</html>
