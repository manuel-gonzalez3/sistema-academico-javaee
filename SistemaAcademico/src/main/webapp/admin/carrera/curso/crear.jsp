<%-- Formulario de alta de curso. Crea el curso con dos comisiones.
     Atributos: error (String). --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Crear Curso</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>

<%
  String cp = request.getContextPath();
  String error = (String) request.getAttribute("error");
  String nombre = (String) request.getAttribute("nombre");
  String idCarrera = request.getParameter("idCarrera");
%>

<div class="container-page">
  <div class="header">
    <h1 class="header-title">Crear Curso</h1>
  </div>

  <% if (error != null) { %>
    <div class="alert alert-danger"><%= error %></div>
  <% } %>

  <div class="card card--p">
    <form method="post" action="<%= cp %>/curso/crear" class="form">

      <input type="hidden" name="idCarrera" value="<%= idCarrera %>">

      <div class="form-row">
        <label class="label">Nombre</label>
        <input class="input" type="text" name="nombre"
               value="<%= nombre != null ? nombre : "" %>" required>
      </div>

      <div class="form-row">
        <label class="label">Cantidad Comisión 1</label>
        <input class="input" type="number" name="cantComision1" min="1" required>
      </div>

      <div class="form-row">
        <label class="label">Cantidad Comisión 2</label>
        <input class="input" type="number" name="cantComision2" min="1" required>
      </div>

      <div class="toolbar mt-3">
        <button class="btn btn-primary">Crear</button>
        <a class="btn btn-outline"
           href="<%= cp %>/curso/listar?idCarrera=<%= idCarrera %>">
          Cancelar
        </a>
      </div>
    </form>
  </div>
</div>

</body>
</html>
