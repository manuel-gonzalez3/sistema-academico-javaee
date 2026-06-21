<%-- Formulario de alta de carrera.
     Atributos: error (String), nombre (String, para repoblar el campo). --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Nueva carrera</title>
  <!-- Estilos propios -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>
<%
  String cp = request.getContextPath();
  String error = (String) request.getAttribute("error");
  String nombre = (String) request.getAttribute("nombre"); //guarda el nombre de la carrera ingresada
  if (nombre == null) nombre = "";
%>

<div class="container-page">
  <div class="header">
    <h1 class="header-title">Crear nueva carrera</h1>
  </div>

  <% if (error != null) { %>
    <div class="alert alert-danger"><%= error %></div>
  <% } %>

  <div class="card card--p">
    <form action="<%= cp %>/carrera/crear" method="post" accept-charset="UTF-8" autocomplete="off" class="form">
      <div class="form-row cols-1">
        <label class="label" for="nombre">Nombre</label>
        <input id="nombre" type="text" name="nombre" value="<%= nombre %>"
               class="input" maxlength="100" required>
      </div>

      <div class="toolbar mt-3">
        <a class="btn btn-outline" href="<%= cp %>/carrera/listar">Cancelar</a>
        <button type="submit" class="btn btn-primary">Crear</button>
      </div>
    </form>
  </div>
</div>

</body>
</html>
