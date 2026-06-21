<%-- Lista todos los docentes con estado activo y accesos a cursos asignados.
     Atributos: docentes (List<Persona>: dni, nombre, apellido, activo), errorDB, ok, error. --%>
<%@ page contentType="text/html; charset=UTF-8" import="java.util.*, entidades.Persona" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Docentes</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>

<%
  String cp = request.getContextPath();
  List<Persona> docentes = (List<Persona>) request.getAttribute("docentes");
  String errorDB = (String) request.getAttribute("errorDB");
  String error = request.getParameter("error");
  String ok    = request.getParameter("ok");
%>

<div class="container-page">
  <div class="header">
    <h1 class="header-title">Docentes</h1>
  </div>
  <div class="toolbar">	
     	<a class="btn btn-primary" href="<%= cp %>/persona/crear?rol=docente">Crear Docente</a>
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
            <th>Cursos</th>
            <th>Estado</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
        <%-- Recorre la lista de docentes y arma una fila por cada uno --%>
        <% for (Persona docente : docentes) { %>
          <tr>
            <td data-label="DNI"><%= docente.getDni() %></td>
            <td data-label="Nombre"><%= docente.getNombre() %></td>
            <td data-label="Apellido"><%= docente.getApellido() %></td>
            <td data-label="Cursos">
              <a class="btn btn-sm btn-outline"
                 href="<%= cp %>/admin/docente/cursos?dni=<%= docente.getDni() %>">
                Ver cursos
              </a>
            </td>
            <%-- Muestra el badge según el estado del docente (activo / inhabilitado) --%>
            <td data-label="Estado">
              <% if (docente.isActivo()) { %>
                <span class="badge badge-success">Activo</span>
              <% } else { %>
                <span class="badge badge-secondary">Inhabilitado</span>
              <% } %>
            </td>
            <td data-label="Acciones" class="actions">
              <div class="actions-col">

                <%-- Button Inhabilitar activa el popout, popout submittea el form inhabilitar-docente.
                     Si el docente ya está inhabilitado el botón queda disabled (no abre popout) --%>
                <form id="inhabilitar-docente-<%= docente.getDni() %>"
                      action="<%= cp %>/persona/habilitar" method="post">
                  <input type="hidden" name="rol" value="docente">
                  <input type="hidden" name="dni" value="<%= docente.getDni() %>">
                  <input type="hidden" name="action" value="inhabilitar">
                  <button type="button" class="btn btn-sm btn-outline"
                          <%= docente.isActivo() ? "popovertarget=\"popup-inhabilitar-docente-" + docente.getDni() + "\"" : "disabled" %>>
                    Inhabilitar
                  </button>
                </form>
                <div id="popup-inhabilitar-docente-<%= docente.getDni() %>" popover>
                  <p class="pop-msg">¿Inhabilitar este docente?</p>
                  <div class="pop-actions">
                    <button type="submit" form="inhabilitar-docente-<%= docente.getDni() %>" class="btn btn-sm btn-danger">Confirmar</button>
                    <button type="button" popovertarget="popup-inhabilitar-docente-<%= docente.getDni() %>" popovertargetaction="hide" class="btn btn-sm">Cancelar</button>
                  </div>
                </div>

                <%-- Button Habilitar activa el popout, popout submittea el form habilitar-docente.
                     Si el docente ya está activo el botón queda disabled (no abre popout) --%>
                <form id="habilitar-docente-<%= docente.getDni() %>"
                      action="<%= cp %>/persona/habilitar" method="post">
                  <input type="hidden" name="rol" value="docente">
                  <input type="hidden" name="dni" value="<%= docente.getDni() %>">
                  <input type="hidden" name="action" value="habilitar">
                  <button type="button" class="btn btn-sm btn-success"
                          <%= docente.isActivo() ? "disabled" : "popovertarget=\"popup-habilitar-docente-" + docente.getDni() + "\"" %>>
                    Habilitar
                  </button>
                </form>
                <div id="popup-habilitar-docente-<%= docente.getDni() %>" popover>
                  <p class="pop-msg">¿Habilitar este docente?</p>
                  <div class="pop-actions">
                    <button type="submit" form="habilitar-docente-<%= docente.getDni() %>" class="btn btn-sm btn-success">Confirmar</button>
                    <button type="button" popovertarget="popup-habilitar-docente-<%= docente.getDni() %>" popovertargetaction="hide" class="btn btn-sm">Cancelar</button>
                  </div>
                </div>

                <%-- Button Eliminar activa el popout, popout submittea el form eliminar-docente --%>
                <form id="eliminar-docente-<%= docente.getDni() %>"
                      action="<%= cp %>/persona/eliminar" method="post">
                  <input type="hidden" name="rol" value="docente">
                  <input type="hidden" name="dni" value="<%= docente.getDni() %>">
                  <button type="button" class="btn btn-sm btn-danger"
                          popovertarget="popup-eliminar-docente-<%= docente.getDni() %>">
                    Eliminar
                  </button>
                </form>
                <div id="popup-eliminar-docente-<%= docente.getDni() %>" popover>
                  <p class="pop-msg">¿Eliminar este docente?</p>
                  <div class="pop-actions">
                    <button type="submit" form="eliminar-docente-<%= docente.getDni() %>" class="btn btn-sm btn-danger">Confirmar</button>
                    <button type="button" popovertarget="popup-eliminar-docente-<%= docente.getDni() %>" popovertargetaction="hide" class="btn btn-sm">Cancelar</button>
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