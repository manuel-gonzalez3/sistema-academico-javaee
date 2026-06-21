<%-- Lista todos los exámenes del sistema con carrera, curso, comisión y docente.
     Atributos: examenes (List<Examen>), errorDB, ok, error. --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List, entidades.Examen" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Exámenes</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>
<%
  String cp      = request.getContextPath();
  String ok      = request.getParameter("ok");
  String error   = request.getParameter("error");
  String errorDB = (String) request.getAttribute("errorDB");
  List<Examen> examenes = (List<Examen>) request.getAttribute("examenes");
%>
<div class="container-page">
  <div class="header">
    <h1 class="header-title">Todos los exámenes</h1>
  </div>
  <% if (ok != null) { %>
    <div class="alert alert-success"><%= ok %></div>
  <% } %>
  <% if (error != null) { %>
    <div class="alert alert-danger"><%= error %></div>
  <% } %>
  <% if (errorDB != null) { %>
    <div class="card card--p alert alert-info"><%= errorDB %></div>
  <% } else { %>
    <div class="card card--p">
      <table class="table">
        <thead>
          <tr>
            <th>Carrera</th>
            <th>Curso</th>
            <th>Comisión</th>
            <th>Fecha</th>
            <th>Hora</th>
            <th>Estado</th>
            <th>Docente</th>
            <th>Alumnos</th>
            <th>Activo</th>
            <th>Acción</th>
          </tr>
        </thead>
        <tbody>
          <%-- Recorre la lista de exámenes y arma una fila por cada uno --%>
          <% for (Examen e : examenes) { %>
          <tr>
            <td data-label="Carrera"><%= e.getNombreCarrera() %></td>
            <td data-label="Curso"><%= e.getNombreCurso() %></td>
            <td data-label="Comisión"><%= e.getNroComision() %></td>
            <td data-label="Fecha"><%= e.getFecha() %></td>
            <td data-label="Hora"><%= e.getHora() %></td>
            <td data-label="Estado"><%= e.getEstado() %></td>
            <td data-label="Docente"><%= e.getNombreDocente() %></td>
            <td data-label="Alumnos">
              <a class="btn btn-sm btn-outline"
                 href="<%= cp %>/examen/alumnos?idExamen=<%= e.getIdExamen() %>">
                Ver alumnos
              </a>
            </td>
            <%-- Muestra el badge según el estado del examen (activo / inhabilitado) --%>
            <td data-label="Activo">
              <% if (e.isActivo()) { %>
                <span class="badge badge-success">Activo</span>
              <% } else { %>
                <span class="badge badge-secondary">Inhabilitado</span>
              <% } %>
            </td>
            <td data-label="Acciones" class="actions">
              <div style="display:flex; gap:4px; flex-wrap:wrap;">
                <%-- Si está activo muestra Inhabilitar, si no Habilitar --%>
                <% if (e.isActivo()) { %>
                <%-- Button Inhabilitar activa el popout, popout submittea el form inhabilitar-examen --%>
                <form id="inhabilitar-examen-<%= e.getIdExamen() %>"
                      style="display:contents" method="post" action="<%= cp %>/objeto/habilitar">
                  <input type="hidden" name="id"     value="<%= e.getIdExamen() %>">
                  <input type="hidden" name="tipo"   value="examen">
                  <input type="hidden" name="action" value="inhabilitar">
                  <button type="button" class="btn btn-sm btn-danger"
                          popovertarget="popup-inhabilitar-examen-<%= e.getIdExamen() %>">Inhabilitar</button>
                </form>
                <div id="popup-inhabilitar-examen-<%= e.getIdExamen() %>" popover>
                  <p class="pop-msg">¿Inhabilitar este examen?</p>
                  <div class="pop-actions">
                    <button type="submit" form="inhabilitar-examen-<%= e.getIdExamen() %>" class="btn btn-sm btn-danger">Confirmar</button>
                    <button type="button" popovertarget="popup-inhabilitar-examen-<%= e.getIdExamen() %>" popovertargetaction="hide" class="btn btn-sm">Cancelar</button>
                  </div>
                </div>
                <% } else { %>
                <%-- Button Habilitar activa el popout, popout submittea el form habilitar-examen --%>
                <form id="habilitar-examen-<%= e.getIdExamen() %>"
                      style="display:contents" method="post" action="<%= cp %>/objeto/habilitar">
                  <input type="hidden" name="id"     value="<%= e.getIdExamen() %>">
                  <input type="hidden" name="tipo"   value="examen">
                  <input type="hidden" name="action" value="habilitar">
                  <button type="button" class="btn btn-sm btn-success"
                          popovertarget="popup-habilitar-examen-<%= e.getIdExamen() %>">Habilitar</button>
                </form>
                <div id="popup-habilitar-examen-<%= e.getIdExamen() %>" popover>
                  <p class="pop-msg">¿Habilitar este examen?</p>
                  <div class="pop-actions">
                    <button type="submit" form="habilitar-examen-<%= e.getIdExamen() %>" class="btn btn-sm btn-success">Confirmar</button>
                    <button type="button" popovertarget="popup-habilitar-examen-<%= e.getIdExamen() %>" popovertargetaction="hide" class="btn btn-sm">Cancelar</button>
                  </div>
                </div>
                <% } %>
                <%-- Button Eliminar activa el popout, popout submittea el form eliminar-examen --%>
                <form id="eliminar-examen-<%= e.getIdExamen() %>"
                      style="display:contents" method="post" action="<%= cp %>/objeto/eliminar">
                  <input type="hidden" name="id"   value="<%= e.getIdExamen() %>">
                  <input type="hidden" name="tipo" value="examen">
                  <button type="button" class="btn btn-sm btn-outline"
                          popovertarget="popup-eliminar-examen-<%= e.getIdExamen() %>">Eliminar</button>
                </form>
                <div id="popup-eliminar-examen-<%= e.getIdExamen() %>" popover>
                  <p class="pop-msg">¿Eliminar este examen?</p>
                  <div class="pop-actions">
                    <button type="submit" form="eliminar-examen-<%= e.getIdExamen() %>" class="btn btn-sm btn-danger">Confirmar</button>
                    <button type="button" popovertarget="popup-eliminar-examen-<%= e.getIdExamen() %>" popovertargetaction="hide" class="btn btn-sm">Cancelar</button>
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
