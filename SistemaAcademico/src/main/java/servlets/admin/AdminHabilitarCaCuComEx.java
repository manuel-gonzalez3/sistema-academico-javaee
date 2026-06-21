package servlets.admin;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.IOException;
import java.sql.*;
import util.DatabaseConnection;

// Servlet para habilitar e inhabilitar carrera, curso, comision y examen. El proceso es el mismo, solo cambia la tabla y el mensaje.
@WebServlet("/objeto/habilitar")
public class AdminHabilitarCaCuComEx extends HttpServlet {

  // Redirige al listado de carreras si se accede por GET directamente.
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    // Si intentan acceder por URL directa, redirige al listado
    resp.sendRedirect(req.getContextPath() + "/carrera/listar");
  }

  // Activa o desactiva carrera, curso, comisión o examen según la acción recibida.
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");
    // Verifica rol ADMIN
    if (!util.ServletUtils.checkRol(req, resp, "ADMIN")) return;

    // Obtiene parámetros desde el formulario
    String tipo    = req.getParameter("tipo");   // carrera | curso | comision | examen
    String idStr   = req.getParameter("id");
    String action  = req.getParameter("action"); // habilitar | inhabilitar
    String idPadre  = null; // para redireccionar al nivel superior
    String tipoPadre = null; // para redireccionar al nivel superior

    if (idStr == null || idStr.isBlank() ||
        action == null || action.isBlank() || tipo == null || tipo.isBlank()) {
      resp.sendRedirect(req.getContextPath() + "/carrera/listar?error=Datos+invalidos");
      return;
    }

    // Verifico que el tipo sea válido
    if(!"carrera".equals(tipo) && !"curso".equals(tipo) && !"comision".equals(tipo) && !"examen".equals(tipo)){
      resp.sendRedirect(req.getContextPath() + "/carrera/listar?error=Datos+invalidos");
      return;
    }

    // Si es curso o comision, necesito el idPadre para redireccionar al nivel superior
    // Carrera y examen no tienen padre
    if("curso".equals(tipo) || "comision".equals(tipo)) {
      idPadre = req.getParameter("idPadre");
      if(idPadre == null || idPadre.isBlank()) {
        resp.sendRedirect(req.getContextPath() + "/carrera/listar?error=Datos+invalidos");
        return;
      }
      try {
        Integer.parseInt(idPadre);
        tipoPadre = "curso".equals(tipo) ? "Carrera" : "Curso";
      } catch (NumberFormatException e) {
        resp.sendRedirect(req.getContextPath() + "/carrera/listar?error=Datos+invalidos");
        return;
      }
    }

    // Verifico que el id sea un número, redirige al listado del nivel superior con mensaje de error
    int id;
    try {
      id = Integer.parseInt(idStr.trim());
    } catch (NumberFormatException e) {
      if ("curso".equals(tipo) || "comision".equals(tipo)) {
        // para curso, comision o examen con comision
        resp.sendRedirect(req.getContextPath() +
            "/" + tipo + "/listar?id" + tipoPadre + "=" + idPadre + "&error=ID+inválido");
      } else {
        // carrera o examen: vuelve al listado propio sin comision
        resp.sendRedirect(req.getContextPath() +
            "/" + tipo + "/listar?error=ID+inválido");
      }
      return;
    }

    // Verifico la accion y seteo variable activo y el mensaje de confirmacion
    int activo;
    String mensajeOK;

    if ("habilitar".equals(action)) {
      activo = 1;
      mensajeOK = tipo + " habilitado/a correctamente";
    } else if ("inhabilitar".equals(action)) {
      activo = 0;
      mensajeOK = tipo + " inhabilitado/a correctamente";
    } else {
      // Accion inválida, redirige al listado del nivel superior con mensaje de error
      if ("curso".equals(tipo) || "comision".equals(tipo)) {
        // curso o comision: vuelve al listado con el padre
        resp.sendRedirect(req.getContextPath() + "/" + tipo + "/listar?id" + tipoPadre + "=" + idPadre + "&error=Accion+invalida");
      } else {
        // carrera o examen: vuelve al listado propio
        resp.sendRedirect(req.getContextPath() + "/" + tipo + "/listar?error=Accion+invalida");
      }
      return;
    }

    // Actualiza el estado en la BD
    try (Connection cn = DatabaseConnection.getConnection();
         PreparedStatement ps = cn.prepareStatement(
             "UPDATE " + tipo + " SET Activo = ? WHERE Id" + tipo + " = ?")) {

      ps.setInt(1, activo);
      ps.setInt(2, id);
      ps.executeUpdate();

      if ("curso".equals(tipo) || "comision".equals(tipo)) {
        // curso o comision: vuelve al listado con el padre
        resp.sendRedirect(req.getContextPath() + "/" + tipo + "/listar?id" + tipoPadre + "=" + idPadre + "&ok=" + java.net.URLEncoder.encode(mensajeOK, "UTF-8"));
      } else {
        // carrera o examen: vuelve al listado propio
        resp.sendRedirect(req.getContextPath() + "/" + tipo + "/listar?ok=" + java.net.URLEncoder.encode(mensajeOK, "UTF-8"));
      }

    } catch (Exception e) {
      if ("curso".equals(tipo) || "comision".equals(tipo)) {
        // curso o comision: vuelve al listado con el padre
        resp.sendRedirect(req.getContextPath() + "/" + tipo + "/listar?id" + tipoPadre + "=" + idPadre + "&error=No+se+pudo+actualizar+el+estado");
      } else {
        // carrera o examen: vuelve al listado propio
        resp.sendRedirect(req.getContextPath() + "/" + tipo + "/listar?error=No+se+pudo+actualizar+el+estado");
      }
    }
  }
}
