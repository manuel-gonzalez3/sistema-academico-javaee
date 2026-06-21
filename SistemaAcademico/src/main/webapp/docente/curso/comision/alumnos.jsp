<%-- Gestión de cierre de cursado de una comisión. El docente asigna estado y nota a cada alumno.
     Atributos: alumnos (List<Map>), curso, carrera, idComision, nroComision, ok, error, errorDB. --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Cierre de cursado</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>
<%
  String cp = request.getContextPath();
  Integer idComision  = (Integer) request.getAttribute("idComision");
  Integer nroComision = (Integer) request.getAttribute("nroComision");
  String curso   = (String) request.getAttribute("curso");
  String carrera = (String) request.getAttribute("carrera");
  String ok      = (String) request.getAttribute("ok");
  String error   = (String) request.getAttribute("error");
  String errorDB = (String) request.getAttribute("errorDB");
  List<Map<String, Object>> alumnos = (List<Map<String, Object>>) request.getAttribute("alumnos");
%>

<div class="container-page">
  <div class="header">
    <h1 class="header-title">Cierre de cursado - Comisión <%= nroComision != null ? nroComision : "" %></h1>
  </div>

  <% if (curso != null) { %>
    <h2><%= curso %> - <%= carrera %></h2>
  <% } %>

  <% if (ok != null) { %>
    <div class="card card--p alert alert-success"><%= ok %></div>
  <% } %>

  <% if (error != null) { %>
    <div class="card card--p alert alert-danger"><%= error %></div>
  <% } %>

  <% if (errorDB != null) { %>
    <div class="card card--p alert alert-info"><%= errorDB %></div>
  <% } else { %>
    <form id="form-cierre-cursado" method="post" action="<%= cp %>/docente/curso/comision/alumnos">
      <input type="hidden" name="idComision" value="<%= idComision %>">
      <div class="card card--p">
        <table class="table">
          <thead>
            <tr>
              <th>DNI</th>
              <th>Alumno</th>
              <th>Condición final</th>
              <th>Nota (solo promocionado)</th>
            </tr>
          </thead>
          <tbody>
            <%-- Una fila por alumno; cada select/input lleva el dni en el name para distinguirlos al postear --%>
            <% for (Map<String, Object> a : alumnos) { %>
            <tr>
              <td data-label="DNI"><%= a.get("dni") %></td>
              <td data-label="Alumno"><%= a.get("apellido") %>, <%= a.get("nombre") %></td>
              <td data-label="Condición final">
                <%-- Marca como selected la opción que coincide con el estado actual del alumno --%>
                <select name="estado_<%= a.get("dni") %>" required>
                  <option value="libre"        <%= "libre".equalsIgnoreCase(String.valueOf(a.get("estado")))        ? "selected" : "" %>>Libre</option>
                  <option value="regular"      <%= "regular".equalsIgnoreCase(String.valueOf(a.get("estado")))      ? "selected" : "" %>>Regular</option>
                  <option value="promocionado" <%= "promocionado".equalsIgnoreCase(String.valueOf(a.get("estado"))) ? "selected" : "" %>>Promocionado</option>
                </select>
              </td>
              <td data-label="Nota (solo promocionado)" class="celda-nota"> <!-- Solo se habilita si se selecciona "Promocionado", checkeado en css-->
                <%--  El rango 8-10 lo valida el servlet. --%>
                <input type="number" step="1"
                       name="nota_<%= a.get("dni") %>"
                       value="<%= a.get("nota") != null ? a.get("nota") : "" %>">
              </td>
            </tr>
            <% } %>
          </tbody>
        </table>
      </div>
      <div class="mt-3" style="display:flex;gap:12px;">
        <%-- Button Cerrar cursado activa el popout, popout submittea el form form-cierre-cursado --%>
        <button type="button" class="btn btn-primary"
                popovertarget="popup-cierre-cursado">Cerrar cursado y guardar condiciones</button>
        <div id="popup-cierre-cursado" popover>
          <p class="pop-msg">¿Confirma el cierre del cursado? Esta acción no se puede editar.</p>
          <div class="pop-actions">
            <button type="submit" form="form-cierre-cursado" class="btn btn-sm btn-primary">Confirmar</button>
            <button type="button" popovertarget="popup-cierre-cursado" popovertargetaction="hide" class="btn btn-sm">Cancelar</button>
          </div>
        </div>
        <a class="btn" style="margin-left:auto" href="<%= cp %>/docente/curso/listar">Volver</a>
      </div>
    </form>
  <% } %>
</div>
</body>
</html>
