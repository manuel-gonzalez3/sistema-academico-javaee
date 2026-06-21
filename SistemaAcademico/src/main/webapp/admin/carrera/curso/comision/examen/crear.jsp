<%-- Formulario de alta de examen para una comisión específica.
     Atributos: idComision, idCurso, error. --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.time.LocalDate" %>

<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Crear Examen</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>
<%
  String cp          = request.getContextPath();
  Integer idComision = (Integer) request.getAttribute("idComision");
  Integer idCurso    = (Integer) request.getAttribute("idCurso");
  String error       = request.getParameter("error");
%>

<div class="container-page">
  <div class="header">
    <h1 class="header-title">Crear Examen</h1>
  </div>

  <% if (error != null) { %>
    <div class="alert alert-danger"><%= error %></div>
  <% } %>

  <div class="card card--p">
    <form method="post" action="<%= cp %>/examen/crear" class="form">

      <input type="hidden" name="idComision" value="<%= idComision %>">

      <div class="form-row">
        <label class="label">Fecha</label>
        <%-- min = hoy + 2 días: no se permite crear un examen para hoy ni mañana --%>
        <input class="input" type="date" name="fecha" min="<%=java.time.LocalDate.now().plusDays(2) %>" required>
      </div>

      <div class="form-row">
		<label class="label">Hora</label>
		<select class="input" name="hora" required>
		  <option value="">-- Seleccionar --</option>
		    <%
		      // Genera opciones de hora cada 30 min, de 07:00 a 21:00 inclusive
		      for (int h = 7; h <= 21; h++) {
		        for (int m = 0; m < 60; m += 30) {
		          if (h == 21 && m == 30) break; // para en 21:00
		          String hora = String.format("%02d:%02d", h, m);
		    %>
		      <option value="<%= hora %>"><%= hora %></option>
		    <%
		        }
		      }
		    %>
		</select>
	  </div>
      <div class="toolbar mt-3">
        <button class="btn btn-primary" type="submit">Crear</button>
        <a class="btn btn-outline" href="<%= cp %>/comision/listar?idCurso=<%= idCurso %>">Cancelar</a>
      </div>

    </form>
  </div>
</div>

</body>
</html>
