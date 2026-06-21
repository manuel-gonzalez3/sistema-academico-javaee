<%-- Lista los cursos de una carrera con docente asignado y acciones de gestión.
     Atributos: cursos (List<Curso>), idCarrera, ok, error, errorDB. --%>
<%@ page contentType="text/html; charset=UTF-8"
         import="java.util.*, entidades.*" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Cursos</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>

<%
  String cp = request.getContextPath();
  List<Curso> cursos = (List<Curso>) request.getAttribute("cursos");
  String errorDB = (String) request.getAttribute("errorDB");
  String error = request.getParameter("error");
  String ok = request.getParameter("ok");
  Integer idCarrera = (Integer) request.getAttribute("idCarrera");
%>

<div class="container-page">
  <div class="header">
    <h1 class="header-title">Cursos</h1>
    <div class="toolbar">
      <a class="btn btn-primary"
         href="<%= cp %>/curso/crear?idCarrera=<%= idCarrera %>">
        Crear curso
      </a>
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
            <th>Docente</th>
            <th>Estado</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>

        <%-- Recorre la lista de cursos y arma una fila por cada uno --%>
        <% for (Curso c : cursos) { %>
          <tr>
            <td data-label="Nombre">
              <a class="link-carrera"
                 href="<%= cp %>/comision/listar?idCurso=<%= c.getIdCurso() %>">
                <%= c.getNombre() %>
              </a>
            </td>

            <%-- Si el curso no tiene docente (dni == 0) ofrece "Asignar", si tiene ofrece "Modificar" --%>
            <td data-label="Docente">
              <%= c.getNombreDocente() %>
              <a class="btn btn-sm btn-outline"
                 href="<%= cp %>/curso/asignarDocente?idCurso=<%= c.getIdCurso() %>">
                <%= (c.getDniDocente() == 0) ? "Asignar Docente" : "Modificar Docente" %>
              </a>
            </td>

            <%-- Muestra el badge según el estado del curso (activo / inhabilitado) --%>
            <td data-label="Estado">
              <% if (c.isActivo()) { %>
                <span class="badge badge-success">Activo</span>
              <% } else { %>
                <span class="badge badge-secondary">Inhabilitado</span>
              <% } %>
            </td>

            <td data-label="Acciones" class="actions">
              <div class="actions-col">

                <%-- Si está activo muestra Inhabilitar, si no Habilitar --%>
                <% if (c.isActivo()) { %>
                <%-- Button Inhabilitar activa el popout, popout submittea el form inhabilitar-curso --%>
                <form id="inhabilitar-curso-<%= c.getIdCurso() %>"
                      action="<%= cp %>/objeto/habilitar" method="post">
                  <input type="hidden" name="id" value="<%= c.getIdCurso() %>">
                  <input type="hidden" name="tipo" value="curso">
                  <input type="hidden" name="idPadre" value="<%= idCarrera %>">
                  <input type="hidden" name="action" value="inhabilitar">
                  <button type="button" class="btn btn-sm btn-danger"
                          popovertarget="popup-inhabilitar-curso-<%= c.getIdCurso() %>">
                    Inhabilitar
                  </button>
                </form>
                <div id="popup-inhabilitar-curso-<%= c.getIdCurso() %>" popover>
                  <p class="pop-msg">¿Inhabilitar este curso?</p>
                  <div class="pop-actions">
                    <button type="submit" form="inhabilitar-curso-<%= c.getIdCurso() %>" class="btn btn-sm btn-danger">Confirmar</button>
                    <button type="button" popovertarget="popup-inhabilitar-curso-<%= c.getIdCurso() %>" popovertargetaction="hide" class="btn btn-sm">Cancelar</button>
                  </div>
                </div>
                <% } else { %>
                <%-- Button Habilitar activa el popout, popout submittea el form habilitar-curso --%>
                <form id="habilitar-curso-<%= c.getIdCurso() %>"
                      action="<%= cp %>/objeto/habilitar" method="post">
                  <input type="hidden" name="id" value="<%= c.getIdCurso() %>">
                  <input type="hidden" name="tipo" value="curso">
                  <input type="hidden" name="idPadre" value="<%= idCarrera %>">
                  <input type="hidden" name="action" value="habilitar">
                  <button type="button" class="btn btn-sm btn-success"
                          popovertarget="popup-habilitar-curso-<%= c.getIdCurso() %>">
                    Habilitar
                  </button>
                </form>
                <div id="popup-habilitar-curso-<%= c.getIdCurso() %>" popover>
                  <p class="pop-msg">¿Habilitar este curso?</p>
                  <div class="pop-actions">
                    <button type="submit" form="habilitar-curso-<%= c.getIdCurso() %>" class="btn btn-sm btn-success">Confirmar</button>
                    <button type="button" popovertarget="popup-habilitar-curso-<%= c.getIdCurso() %>" popovertargetaction="hide" class="btn btn-sm">Cancelar</button>
                  </div>
                </div>
                <% } %>

                <%-- Button Eliminar activa el popout, popout submittea el form eliminar-curso --%>
                <form id="eliminar-curso-<%= c.getIdCurso() %>"
                      action="<%= cp %>/objeto/eliminar" method="post">
                  <input type="hidden" name="id" value="<%= c.getIdCurso() %>">
                  <input type="hidden" name="tipo" value="curso">
                  <input type="hidden" name="idPadre" value="<%= idCarrera %>">
                  <button type="button" class="btn btn-sm btn-danger"
                          popovertarget="popup-eliminar-curso-<%= c.getIdCurso() %>">
                    Eliminar
                  </button>
                </form>
                <div id="popup-eliminar-curso-<%= c.getIdCurso() %>" popover>
                  <p class="pop-msg">¿Eliminar este curso?</p>
                  <div class="pop-actions">
                    <button type="submit" form="eliminar-curso-<%= c.getIdCurso() %>" class="btn btn-sm btn-danger">Confirmar</button>
                    <button type="button" popovertarget="popup-eliminar-curso-<%= c.getIdCurso() %>" popovertargetaction="hide" class="btn btn-sm">Cancelar</button>
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
    <a class="btn" href="<%= cp %>/carrera/listar">Volver</a>
  </div>
</div>

</body>
</html>
