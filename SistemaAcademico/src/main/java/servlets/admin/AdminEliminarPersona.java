package servlets.admin;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.*;

import java.io.IOException;
import java.sql.*;

import util.DatabaseConnection;

// Elimina una persona (alumno o docente) y su usuario en cascada. Solo accesible para ADMIN.
// Solo permite eliminar si la persona no tiene datos asociados.
@WebServlet("/persona/eliminar")
public class AdminEliminarPersona extends HttpServlet {

  // Redirige al listado correspondiente según el rol recibido.
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    String rolPersona = req.getParameter("rol");
    // Validar que rolPersona sea un valor esperado antes de usarlo en la URL
    if ("alumno".equalsIgnoreCase(rolPersona)) {
      resp.sendRedirect(req.getContextPath() + "/alumno/listar");
    } else if ("docente".equalsIgnoreCase(rolPersona)) {
      resp.sendRedirect(req.getContextPath() + "/docente/listar");
    } else {
      resp.sendRedirect(req.getContextPath() + "/admin/home.jsp");
    }
  }

  // Valida asociaciones (carrera / cursos) antes de eliminar de la tabla persona.
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");
    if (!util.ServletUtils.checkRol(req, resp, "ADMIN")) return;
    String rolPersona = req.getParameter("rol");
    String dni = req.getParameter("dni");

    if (dni == null || dni.isBlank()) {
      resp.sendRedirect(req.getContextPath() + "/" + rolPersona + "/listar?error=Faltan+datos");
      return;
    }
    if(!"ALUMNO".equalsIgnoreCase(rolPersona) && !"DOCENTE".equalsIgnoreCase(rolPersona)) {
		resp.sendRedirect(req.getContextPath() + "/admin/home?error=Rol+invalido");
		return;
	}
    try (Connection cn = DatabaseConnection.getConnection()) {

      //Validar persona si tiene datos asociados
	  String errorValidacion = validarDatos(cn, rolPersona, dni);
	  	  if (errorValidacion != null) {
		resp.sendRedirect(req.getContextPath() + "/" + rolPersona + "/listar?error="
			+ java.net.URLEncoder.encode(errorValidacion, "UTF-8"));
		return;
	  }

	  
      // Eliminar de persona (cascada elimina usuario también)
      try (PreparedStatement ps = cn.prepareStatement(
          "DELETE FROM persona WHERE dni = ?")) {
        ps.setString(1, dni);
        ps.executeUpdate();
      }

      resp.sendRedirect(req.getContextPath()
          + "/" + rolPersona + "/listar?ok="
          + java.net.URLEncoder.encode(rolPersona + " eliminado correctamente", "UTF-8"));

    } catch (Exception e) {
      resp.sendRedirect(req.getContextPath() + "/" + rolPersona +"/listar?error=Error+al+eliminar");
    }
  }
  
  //// Verificar si el alumno estuvo asociado a alguna carrera o si el docente tiene cursos asociados
  private String validarDatos(Connection cn, String rolPersona, String dni) throws SQLException {
	    if ("ALUMNO".equalsIgnoreCase(rolPersona)) {
	        try (PreparedStatement ps = cn.prepareStatement(
	                "SELECT COUNT(*) FROM alumno_carrera WHERE dni = ?")) {
	            ps.setString(1, dni);
	            try (ResultSet rs = ps.executeQuery()) {
	                if (rs.next() && rs.getInt(1) > 0) {
	                    return "No se puede eliminar porque está asociado a una carrera";
	                }
	            }
	        }
	    } else if ("DOCENTE".equalsIgnoreCase(rolPersona)) {
	        try (PreparedStatement ps = cn.prepareStatement(
	                "SELECT COUNT(*) FROM curso WHERE dniDocente = ?")) {
	            ps.setString(1, dni);
	            try (ResultSet rs =ps.executeQuery()) {
	                if (rs.next() && rs.getInt(1) > 0) {
	                    return "No se puede eliminar porque tiene cursos asociados";
	                }
	            }
	        }
	    }
	    return null;
	}
}
