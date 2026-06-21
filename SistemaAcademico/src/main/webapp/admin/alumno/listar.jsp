<%-- Lista todos los alumnos del sistema con estado activo y carrera inscripta.
     Atributos: alumnos (List<Map>: dni, nombre, apellido, carrera, activo), errorDB, ok, error. --%>
<%@ page contentType="text/html; charset=UTF-8" import="java.util.*" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Alumnos</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>

<%
  String cp = request.getContextPath();
  List<Map<String, Object>> alumnos = (List<Map<String, Object>>) request.getAttribute("alumnos");
  String errorDB = (String) request.getAttribute("errorDB");
  String error = request.getParameter("error");
  String ok    = request.getParameter("ok");
%>

<div class="container-page">
  <div class="header">
    <h1 class="header-title">Alumnos</h1>
  </div>
  <div class="toolbar">
    <a class="btn btn-primary" href="<%= cp %>/persona/crear?rol=alumno">Crear Alumno</a>
  </div>

  <% if (error != null) { %>
    <div class="alert alert-danger"><%= error %></div>
  <% } %>

  <% if (ok != null) { %>
    <div class="alert alert-success"><%= ok %></div>
  <% } %>

  <% if (errorDB != null) { %>
    <div class="card card--p alert alert-info"><%= errorDB %></div>
  <% } else { %>

    <div class="card card--p">
      <table class="table">
        <thead>
          <tr>
            <th>DNI</th>
            <th>Nombre</th>
            <th>Apellido</th>
            <th>Carrera</th>
            <th>Estado</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
        <%-- Recorre la lista de alumnos y arma una fila por cada uno --%>
        <% for (Map<String, Object> alumno : alumnos) { %>
          <tr>
            <td data-label="DNI"><%= alumno.get("dni") %></td>
            <td data-label="Nombre"><%= alumno.get("nombre") %></td>
            <td data-label="Apellido"><%= alumno.get("apellido") %></td>
            <td data-label="Carrera"><%= alumno.get("carrera") == null ? "-" : alumno.get("carrera") %></td>
            <%-- Muestra el badge según el estado del alumno (activo / inhabilitado) --%>
            <td data-label="Estado">
              <% if (Boolean.TRUE.equals(alumno.get("activo"))) { %>
                <span class="badge badge-success">Activo</span>
              <% } else { %>
                <span class="badge badge-secondary">Inhabilitado</span>
              <% } %>
            </td>
            <td data-label="Acciones" class="actions">
              <div class="actions-col">

                <%-- Button Inhabilitar activa el popout, popout submittea el form inhabilitar-alumno.
                     Si el alumno ya está inhabilitado el botón queda disabled (no abre popout) --%>
                <form id="inhabilitar-alumno-<%= alumno.get("dni") %>"
                      action="<%= cp %>/persona/habilitar" method="post">
                  <input type="hidden" name="rol" value="alumno">
                  <input type="hidden" name="dni" value="<%= alumno.get("dni") %>">
                  <input type="hidden" name="action" value="inhabilitar">
                  <button type="button" class="btn btn-sm btn-outline"
                          <%= Boolean.TRUE.equals(alumno.get("activo")) ? "popovertarget=\"popup-inhabilitar-alumno-" + alumno.get("dni") + "\"" : "disabled" %>>
                    Inhabilitar
                  </button>
                </form>
                <div id="popup-inhabilitar-alumno-<%= alumno.get("dni") %>" popover>
                  <p class="pop-msg">¿Inhabilitar este alumno?</p>
                  <div class="pop-actions">
                    <button type="submit" form="inhabilitar-alumno-<%= alumno.get("dni") %>" class="btn btn-sm btn-danger">Confirmar</button>
                    <button type="button" popovertarget="popup-inhabilitar-alumno-<%= alumno.get("dni") %>" popovertargetaction="hide" class="btn btn-sm">Cancelar</button>
                  </div>
                </div>

                <%-- Button Habilitar activa el popout, popout submittea el form habilitar-alumno.
                     Si el alumno ya está activo el botón queda disabled (no abre popout) --%>
                <form id="habilitar-alumno-<%= alumno.get("dni") %>"
                      action="<%= cp %>/persona/habilitar" method="post">
                  <input type="hidden" name="rol" value="alumno">
                  <input type="hidden" name="dni" value="<%= alumno.get("dni") %>">
                  <input type="hidden" name="action" value="habilitar">
                  <button type="button" class="btn btn-sm btn-success"
                          <%= Boolean.TRUE.equals(alumno.get("activo")) ? "disabled" : "popovertarget=\"popup-habilitar-alumno-" + alumno.get("dni") + "\"" %>>
                    Habilitar
                  </button>
                </form>
                <div id="popup-habilitar-alumno-<%= alumno.get("dni") %>" popover>
                  <p class="pop-msg">¿Habilitar este alumno?</p>
                  <div class="pop-actions">
                    <button type="submit" form="habilitar-alumno-<%= alumno.get("dni") %>" class="btn btn-sm btn-success">Confirmar</button>
                    <button type="button" popovertarget="popup-habilitar-alumno-<%= alumno.get("dni") %>" popovertargetaction="hide" class="btn btn-sm">Cancelar</button>
                  </div>
                </div>

                <%-- Button Eliminar activa el popout, popout submittea el form eliminar-alumno --%>
                <form id="eliminar-alumno-<%= alumno.get("dni") %>"
                      action="<%= cp %>/persona/eliminar" method="post">
                  <input type="hidden" name="rol" value="alumno">
                  <input type="hidden" name="dni" value="<%= alumno.get("dni") %>">
                  <button type="button" class="btn btn-sm btn-danger"
                          popovertarget="popup-eliminar-alumno-<%= alumno.get("dni") %>">Eliminar</button>
                </form>
                <div id="popup-eliminar-alumno-<%= alumno.get("dni") %>" popover>
                  <p class="pop-msg">¿Eliminar este alumno?</p>
                  <div class="pop-actions">
                    <button type="submit" form="eliminar-alumno-<%= alumno.get("dni") %>" class="btn btn-sm btn-danger">Confirmar</button>
                    <button type="button" popovertarget="popup-eliminar-alumno-<%= alumno.get("dni") %>" popovertargetaction="hide" class="btn btn-sm">Cancelar</button>
                  </div>
                </div>

              </div>
            </td>
          </tr>
        <% } %>
        </tbody>
      </table>
    </div>
  <% } %>

  <div class="mt-3 right">
    <a class="btn" href="<%= cp %>/admin/home.jsp">Volver</a>
  </div>
</div>

</body>
</html>
