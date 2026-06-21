<%-- Formulario de alta de docente. Llamado desde AdminCrearPersona con ?rol=docente.
     Atributos: error (String). --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Crear Docente</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>

<%
  String cp = request.getContextPath();
  String error = (String) request.getAttribute("error");

  // Repoblo cada campo con lo que vino en el request (o "" si es null) para no perder lo cargado si la validación falla
  String dni = request.getParameter("dni") != null ? request.getParameter("dni") : "";
  String nombre = request.getParameter("nombre") != null ? request.getParameter("nombre") : "";
  String apellido = request.getParameter("apellido") != null ? request.getParameter("apellido") : "";
  String direccion = request.getParameter("direccion") != null ? request.getParameter("direccion") : "";
  String email = request.getParameter("email") != null ? request.getParameter("email") : "";
  String fechaNacimiento = request.getParameter("fechaNacimiento") != null ? request.getParameter("fechaNacimiento") : "";
  String sexo = request.getParameter("sexo") != null ? request.getParameter("sexo") : "";
  String telefono = request.getParameter("telefono") != null ? request.getParameter("telefono") : "";
%>

<div class="container-page">
  <div class="header">
    <h1 class="header-title">Crear Docente</h1>
  </div>

  <% if (error != null) { %>
    <div class="alert alert-danger"><%= error %></div>
  <% } %>

  <div class="card card--p">
    <form method="post" action="<%= cp %>/persona/crear" class="form">
      <input type="hidden" name="rol" value="docente">
      <div class="form-row">
        <label class="label">DNI*</label>
        <input class="input" type="number" name="dni" min="1" max="99999999"
               value="<%= dni %>" required>
      </div>

      <div class="form-row">
        <label class="label">Nombre*</label>
        <input class="input" type="text" name="nombre"
               value="<%= nombre %>" required>
      </div>

      <div class="form-row">
        <label class="label">Apellido*</label>
        <input class="input" type="text" name="apellido"
               value="<%= apellido %>" required>
      </div>

      <div class="form-row">
        <label class="label">Dirección</label>
        <input class="input" type="text" name="direccion"
               value="<%= direccion %>">
      </div>

      <div class="form-row">
        <label class="label">Email*</label>
        <input class="input" type="email" name="email"
               value="<%= email %>" required>
      </div>

      <div class="form-row">
        <label class="label">Fecha de Nacimiento</label>
        <input class="input" type="date" name="fechaNacimiento"
               value="<%= fechaNacimiento %>">
      </div>

      <div class="form-row">
        <label class="label">Sexo</label>
        <%-- Deja preseleccionada la opción que coincide con el sexo ya cargado --%>
        <select class="input" name="sexo">
          <option value="">Seleccione...</option>
          <option value="MASCULINO" <%= "MASCULINO".equalsIgnoreCase(sexo) ? "selected" : "" %>>Masculino</option>
          <option value="FEMENINO" <%= "FEMENINO".equalsIgnoreCase(sexo) ? "selected" : "" %>>Femenino</option>
          <option value="OTRO" <%= "OTRO".equalsIgnoreCase(sexo) ? "selected" : "" %>>Otro</option>
        </select>
      </div>

      <div class="form-row">
        <label class="label">Teléfono</label>
        <input class="input" type="text" name="telefono"
               value="<%= telefono %>">
      </div>

      <div class="toolbar mt-3">
        <button class="btn btn-primary">Crear</button>
        <a class="btn btn-outline" href="<%= cp %>/docente/listar">
          Cancelar
        </a>
      </div>
    </form>
  </div>
</div>

</body>
</html>