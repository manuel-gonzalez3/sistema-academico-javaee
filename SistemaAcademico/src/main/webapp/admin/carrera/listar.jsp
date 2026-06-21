<%-- Lista todas las carreras con acciones de habilitar/inhabilitar y eliminar.
     Atributos: carreras (List<Carrera>), errorDB, ok, error. --%>
<%@ page contentType="text/html; charset=UTF-8"
         import="java.util.*,entidades.Carrera" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Carreras</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>

<%
  String cp = request.getContextPath();
  List<Carrera> carreras = (List<Carrera>) request.getAttribute("carreras");
  String errorDB = (String) request.getAttribute("errorDB");
  String error = request.getParameter("error");
  String ok    = request.getParameter("ok");
%>

<div class="container-page">
  <div class="header">
    <h1 class="header-title">Carreras</h1>
    <div class="toolbar">
      <a class="btn btn-primary" href="<%= cp %>/carrera/crear">Crear nueva</a>
    </div>
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
            <th>Nombre</th>
            <th>Estado</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
        <% for (Carrera c : carreras) { %>
          <tr>
            <td data-label="Nombre">
              <a class="link-carrera"
                 href="<%= cp %>/curso/listar?idCarrera=<%= c.getIdCarrera() %>">
                <%= c.getNombre() %>
              </a>
            </td>

            <td data-label="Estado">
              <% if (c.isActivo()) { %>
                <span class="badge badge-success">Activa</span>
              <% } else { %>
                <span class="badge badge-secondary">Inhabilitada</span>
              <% } %>
            </td>

            <td data-label="Acciones" class="actions">
              <div class="actions-col">

                <%--
                  CONFIRMACIÓN SIN JS — usando el atributo HTML "popover":
                  1. El botón visible tiene type="button" (no submitea) y apunta al popup con popovertarget.
                  2. El <div popover> es un overlay nativo del browser, sin DOM.
                  3. El botón "Confirmar" dentro del popup usa el atributo form="id-del-form"
                     para submitear el form aunque esté físicamente afuera de él (HTML puro).
                  4. El botón "Cancelar" cierra el popup con popovertargetaction="hide".
                  El id del form sigue la convención: accion-entidad-id (ej: inhabilitar-carrera-3)
                --%>

                <% if (c.isActivo()) { %>
                <!-- INHABILITAR -->
                <form id="inhabilitar-carrera-<%= c.getIdCarrera() %>"
                      action="<%= cp %>/objeto/habilitar" method="post">
                  <input type="hidden" name="id" value="<%= c.getIdCarrera() %>">
                  <input type="hidden" name="tipo" value="carrera">
                  <input type="hidden" name="action" value="inhabilitar">
                  <button type="button" class="btn btn-sm btn-danger"
                          popovertarget="popup-inhabilitar-carrera-<%= c.getIdCarrera() %>">
                    Inhabilitar
                  </button>
                </form>
                <div id="popup-inhabilitar-carrera-<%= c.getIdCarrera() %>" popover>
                  <p class="pop-msg">¿Inhabilitar esta carrera?</p>
                  <div class="pop-actions">
                    <button type="submit" form="inhabilitar-carrera-<%= c.getIdCarrera() %>" class="btn btn-sm btn-danger">Confirmar</button>
                    <button type="button" popovertarget="popup-inhabilitar-carrera-<%= c.getIdCarrera() %>" popovertargetaction="hide" class="btn btn-sm">Cancelar</button>
                  </div>
                </div>
                <% } else { %>
                <!-- HABILITAR -->
                <form id="habilitar-carrera-<%= c.getIdCarrera() %>"
                      action="<%= cp %>/objeto/habilitar" method="post">
                  <input type="hidden" name="id" value="<%= c.getIdCarrera() %>">
                  <input type="hidden" name="tipo" value="carrera">
                  <input type="hidden" name="action" value="habilitar">
                  <button type="button" class="btn btn-sm btn-success"
                          popovertarget="popup-habilitar-carrera-<%= c.getIdCarrera() %>">
                    Habilitar
                  </button>
                </form>
                <div id="popup-habilitar-carrera-<%= c.getIdCarrera() %>" popover>
                  <p class="pop-msg">¿Habilitar esta carrera?</p>
                  <div class="pop-actions">
                    <button type="submit" form="habilitar-carrera-<%= c.getIdCarrera() %>" class="btn btn-sm btn-success">Confirmar</button>
                    <button type="button" popovertarget="popup-habilitar-carrera-<%= c.getIdCarrera() %>" popovertargetaction="hide" class="btn btn-sm">Cancelar</button>
                  </div>
                </div>
                <% } %>

                <!-- ELIMINAR -->
                <form id="eliminar-carrera-<%= c.getIdCarrera() %>"
                      action="<%= cp %>/objeto/eliminar" method="post">
                  <input type="hidden" name="id" value="<%= c.getIdCarrera() %>">
                  <input type="hidden" name="tipo" value="carrera">
                  <button type="button" class="btn btn-sm btn-danger"
                          popovertarget="popup-eliminar-carrera-<%= c.getIdCarrera() %>">
                    Eliminar
                  </button>
                </form>
                <div id="popup-eliminar-carrera-<%= c.getIdCarrera() %>" popover>
                  <p class="pop-msg">¿Eliminar esta carrera?</p>
                  <div class="pop-actions">
                    <button type="submit" form="eliminar-carrera-<%= c.getIdCarrera() %>" class="btn btn-sm btn-danger">Confirmar</button>
                    <button type="button" popovertarget="popup-eliminar-carrera-<%= c.getIdCarrera() %>" popovertargetaction="hide" class="btn btn-sm">Cancelar</button>
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

  <div class="mt-3">
    <a class="btn" href="<%= cp %>/admin/home.jsp">Volver</a>
  </div>
</div>

</body>
</html>
