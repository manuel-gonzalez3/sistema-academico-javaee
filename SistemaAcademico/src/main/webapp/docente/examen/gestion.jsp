<%-- Gestión de asistencia o notas de un examen. Muestra la lista de alumnos y el formulario de carga.
     La vista cambia según 'accion':
       - 'asistencia': select presente/ausente por alumno
       - 'nota':       input numérico 1-10 (solo alumnos presentes, los ausentes no aparecen)
       - 'resultado':  solo lectura; muestra nota si estuvo presente, "ausente" si no
     Atributos: alumnos (List<Map>: dni, nombre, apellido, asistencia, nota), idExamen, accion, errorDB. --%>
<%@ page contentType="text/html; charset=UTF-8" import="java.util.*" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Alumnos inscriptos en examen</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>

<%
  String cp        = request.getContextPath();
  List<Map<String, Object>> alumnos = (List<Map<String, Object>>) request.getAttribute("alumnos");
  String errorDB   = (String)  request.getAttribute("errorDB");
  Integer idExamen = (Integer) request.getAttribute("idExamen");
  String ok     = request.getParameter("ok");
  String error  = request.getParameter("error");
  String accion = request.getParameter("accion");
%>

<div class="container-page">
  <div class="header">
    <h1 class="header-title">Alumnos inscriptos en examen</h1>
  </div>

  <% if (ok != null) { %>
    <div class="card card--p alert alert-success"><%= ok %></div>
  <% } %>
  <% if (error != null) { %>
    <div class="card card--p alert alert-danger"><%= error %></div>
  <% } %>

  <% if (errorDB != null) { %>
    <%-- Error de BD o sin alumnos: no muestro el formulario --%>
    <div class="card card--p alert alert-info"><%= errorDB %></div>
  <% } else { %>

    <%-- El form envuelve la tabla en todos los modos. En 'resultado' no tiene botón submit,
         pero lo incluyo igual para no romper la estructura de tags con el cierre condicional de abajo. --%>
    <form id="gestion-examen"
          method="post"
          action="<%= cp %>/docente/examen/gestion">

      <input type="hidden" name="idExamen" value="<%= idExamen %>">
      <input type="hidden" name="accion"   value="<%= accion %>">

      <div class="card card--p">
        <table class="table">
          <thead>
            <tr>
              <th>DNI</th>
              <th>Nombre</th>
              <th><%= accion %></th>
            </tr>
          </thead>
          <tbody>
          <% for (Map<String, Object> alumno : alumnos) { %>
            <tr>
              <td data-label="DNI"><%= alumno.get("dni") %></td>
              <td data-label="Nombre"><%= alumno.get("apellido") %>, <%= alumno.get("nombre") %></td>

              <% if ("asistencia".equalsIgnoreCase(accion)) { %>
                <%-- El name usa el DNI para que el servlet pueda leer cada valor como "dato_{dni}" --%>
                <td data-label="Asistencia">
                  <select name="dato_<%= alumno.get("dni") %>" class="select" required>
                    <option value="" disabled selected>Seleccionar</option>
                    <option value="presente">Presente</option>
                    <option value="ausente">Ausente</option>
                  </select>
                </td>

              <% } else if ("nota".equalsIgnoreCase(accion)) { %>
                <%-- El servlet valida que la nota esté entre 1 y 10; el min/max del input es una segunda capa --%>
                <td data-label="Nota">
                  <input type="number" name="dato_<%= alumno.get("dni") %>"
                         class="input" min="1" max="10" required
                         placeholder="1-10">
                </td>

              <% } else if ("resultado".equalsIgnoreCase(accion)) {
                   // Vista de solo lectura:
                   // - presente → muestro la nota (puede ser "-" si aún no fue cargada)
                   // - ausente  → muestro "ausente"
                   String dato = ("presente".equals(alumno.get("asistencia")))
                                   ? String.valueOf(alumno.get("nota"))
                                   : String.valueOf(alumno.get("asistencia"));
              %>
                <td data-label="Resultado"><%= dato %></td>
              <% } %>

            </tr>
          <% } %>
          </tbody>
        </table>
      </div>

    <% } /* fin else errorDB */ %>

    <%-- En modo 'resultado' no hay nada que confirmar: es solo lectura --%>
    <% if (!"resultado".equalsIgnoreCase(accion)) { %>
      <div class="mt-3">
        <%-- Button Confirmar activa el popout, popout submittea el form gestion-examen --%>
        <button type="button" class="btn btn-primary"
                popovertarget="popup-gestion-examen">Confirmar <%= accion %></button>
        <div id="popup-gestion-examen" popover>
          <p class="pop-msg">¿Cargar los datos? Esta acción no se puede editar.</p>
          <div class="pop-actions">
            <button type="submit" form="gestion-examen" class="btn btn-sm btn-primary">Confirmar</button>
            <button type="button" popovertarget="popup-gestion-examen" popovertargetaction="hide" class="btn btn-sm">Cancelar</button>
          </div>
        </div>
      </div>
    </form>
    <% } %>

  <div class="mt-3">
    <a class="btn" href="<%= cp %>/docente/examen/listar">Volver</a>
  </div>
</div>

</body>
</html>
