<%-- Lista las comisiones de un curso con cupo y estado.
     Atributos: comisiones (List<Comision>), idCurso, idCarrera, errorDB. --%>
<%@ page contentType="text/html; charset=UTF-8"
         import="java.util.*, entidades.Comision" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Comisiones</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>

<%
  String cp = request.getContextPath();

  List<Comision> comisiones = (List<Comision>) request.getAttribute("comisiones");
  String errorDB = (String) request.getAttribute("errorDB");

  String error = request.getParameter("error");
  String ok    = request.getParameter("ok");

  Integer idCurso   = (Integer) request.getAttribute("idCurso");
  Integer idCarrera = (Integer) request.getAttribute("idCarrera");
%>

<div class="container-page">
  <div class="header">
    <h1 class="header-title">Comisiones</h1>
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
            <th>Número</th>
            <th>Capacidad de alumnos</th>
            <th>Estado</th>
            <th>Inscriptos</th>
            <th>Acciones</th>
          </tr>
          </tr>
        </thead>
        <tbody>
        <%-- Recorre la lista de comisiones y arma una fila por cada una --%>
        <%
          for (Comision c : comisiones) {
        %>
          <tr>
            <td data-label="Número"><%= c.getNroComision() %></td>
            <td data-label="Cant. Alumnos"><%= c.getCantAlumnos() %></td>

            <%-- Muestra el badge según el estado de la comisión (activa / inhabilitada) --%>
            <td data-label="Estado">
              <% if (c.isActivo()) { %>
                <span class="badge badge-success">Activa</span>
              <% } else { %>
                <span class="badge badge-secondary">Inhabilitada</span>
              <% } %>
            </td>
            <td data-label="Inscriptos">
              <a class="btn btn-sm btn-outline"
                 href="<%= cp %>/comision/alumnos?idComision=<%= c.getIdComision() %>">
                Ver alumnos
              </a>
              <a class="btn btn-sm btn-primary"
                 href="<%= cp %>/examen/crear?idComision=<%= c.getIdComision() %>&idCurso=<%= idCurso %>">
                Crear examen
              </a>
            </td>

            <td data-label="Acciones" class="actions">
              <div class="actions-col">

                <%-- Si está activa muestra Inhabilitar, si no Habilitar --%>
                <% if (c.isActivo()) { %>
                <%-- Button Inhabilitar activa el popout, popout submittea el form inhabilitar-comision --%>
                <form id="inhabilitar-comision-<%= c.getIdComision() %>"
                      method="post" action="<%= cp %>/objeto/habilitar">
                  <input type="hidden" name="id" value="<%= c.getIdComision() %>">
                  <input type="hidden" name="tipo" value="comision">
                  <input type="hidden" name="idPadre" value="<%= idCurso %>">
                  <input type="hidden" name="action" value="inhabilitar">
                  <button type="button" class="btn btn-sm btn-danger"
                          popovertarget="popup-inhabilitar-comision-<%= c.getIdComision() %>">
                    Inhabilitar
                  </button>
                </form>
                <div id="popup-inhabilitar-comision-<%= c.getIdComision() %>" popover>
                  <p class="pop-msg">¿Inhabilitar esta comisión?</p>
                  <div class="pop-actions">
                    <button type="submit" form="inhabilitar-comision-<%= c.getIdComision() %>" class="btn btn-sm btn-danger">Confirmar</button>
                    <button type="button" popovertarget="popup-inhabilitar-comision-<%= c.getIdComision() %>" popovertargetaction="hide" class="btn btn-sm">Cancelar</button>
                  </div>
                </div>
                <% } else { %>
                <%-- Button Habilitar activa el popout, popout submittea el form habilitar-comision --%>
                <form id="habilitar-comision-<%= c.getIdComision() %>"
                      method="post" action="<%= cp %>/objeto/habilitar">
                  <input type="hidden" name="id" value="<%= c.getIdComision() %>">
                  <input type="hidden" name="tipo" value="comision">
                  <input type="hidden" name="idPadre" value="<%= idCurso %>">
                  <input type="hidden" name="action" value="habilitar">
                  <button type="button" class="btn btn-sm btn-success"
                          popovertarget="popup-habilitar-comision-<%= c.getIdComision() %>">
                    Habilitar
                  </button>
                </form>
                <div id="popup-habilitar-comision-<%= c.getIdComision() %>" popover>
                  <p class="pop-msg">¿Habilitar esta comisión?</p>
                  <div class="pop-actions">
                    <button type="submit" form="habilitar-comision-<%= c.getIdComision() %>" class="btn btn-sm btn-success">Confirmar</button>
                    <button type="button" popovertarget="popup-habilitar-comision-<%= c.getIdComision() %>" popovertargetaction="hide" class="btn btn-sm">Cancelar</button>
                  </div>
                </div>
                <% } %>

              </div>
            </td>
          </tr>
        <% 
          }
        %>
        </tbody>
      </table>
    </div>
  <% } %>

  <div class="mt-3 right">
    <a class="btn"
       href="<%= cp %>/curso/listar?idCarrera=<%= idCarrera %>">
      Volver
    </a>
  </div>
</div>

</body>
</html>
