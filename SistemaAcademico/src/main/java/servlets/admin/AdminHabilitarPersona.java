package servlets.admin;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.*;

import java.io.IOException;
import java.sql.*;

import util.DatabaseConnection;

// Habilita o inhabilita un alumno o docente (campo activo en tabla usuario). Solo ADMIN.
@WebServlet("/persona/habilitar")
public class AdminHabilitarPersona extends HttpServlet {

  // Redirige al listado correspondiente según el rol.
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    // Redirige al listado según el rol recibido; por defecto a alumno
    String rolPersona = req.getParameter("rol");
    if ("docente".equalsIgnoreCase(rolPersona)) {
      resp.sendRedirect(req.getContextPath() + "/docente/listar");
    } else {
      resp.sendRedirect(req.getContextPath() + "/alumno/listar");
    }
  }

  // Actualiza el campo activo en la tabla usuario para el DNI recibido.
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");
    if (!util.ServletUtils.checkRol(req, resp, "ADMIN")) return;
    String rolPersona = req.getParameter("rol");
    String dni    = req.getParameter("dni");
    String action = req.getParameter("action");

    if (dni == null || dni.isBlank() || action == null || action.isBlank() || rolPersona == null || rolPersona.isBlank()) {
      resp.sendRedirect(req.getContextPath() + "/" + rolPersona + "/listar?error=Datos+invalidos");
      return;
    }

    int activo;
    String mensajeOK;

    if ("habilitar".equals(action)) {
      activo = 1;
      mensajeOK = rolPersona + " habilitado correctamente";
    } else if ("inhabilitar".equals(action)) {
      activo = 0;
      mensajeOK = rolPersona + " inhabilitado correctamente";
    } else {
      resp.sendRedirect(req.getContextPath() + "/" + rolPersona +"/listar?error=Accion+invalida");
      return;
    }

    // El estado activo vive únicamente en usuario
    try (Connection cn = DatabaseConnection.getConnection()) {
      try (PreparedStatement ps = cn.prepareStatement(
          "UPDATE usuario SET activo = ? WHERE dni = ?")) {
        ps.setInt(1, activo);
        ps.setString(2, dni);
        ps.executeUpdate();
      }

      resp.sendRedirect(req.getContextPath()
          + "/" + rolPersona + "/listar?ok="
          + java.net.URLEncoder.encode(mensajeOK, "UTF-8"));

    } catch (Exception e) {
      resp.sendRedirect(req.getContextPath() + "/" + rolPersona + "/listar?error=No+se+pudo+actualizar+el+estado");
    }
  }
}