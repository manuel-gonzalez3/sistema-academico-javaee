package servlets.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;
import java.util.*;

import entidades.Persona;
import util.DatabaseConnection;

// Servlet para listar todos los docentes del sistema. Solo accesible para ADMIN.
@WebServlet("/docente/listar")
public class AdminListarDocente extends HttpServlet {

  // Lista todos los docentes con su estado activo.
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    if (!util.ServletUtils.checkRol(req, resp, "ADMIN")) return;

    List<Persona> docentes = new ArrayList<>();

    // Une persona con usuario para obtener el estado activo del docente
    String sql = """
        SELECT p.dni,
               p.nombre,
               p.apellido,
               u.activo
          FROM persona p
          JOIN usuario u ON u.dni = p.dni AND u.tipoUsuario = 'DOCENTE'
         ORDER BY p.apellido, p.nombre
        """;

    try (Connection cn = DatabaseConnection.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        Persona docente = new Persona();
        docente.setDni(rs.getInt("dni"));
        docente.setNombre(rs.getString("nombre"));
        docente.setApellido(rs.getString("apellido"));
        docente.setActivo(rs.getBoolean("activo"));
        docentes.add(docente);
      }

      req.setAttribute("docentes", docentes);
      if (docentes.isEmpty()) {
        req.setAttribute("errorDB", "No hay docentes para mostrar");
      }

      req.getRequestDispatcher("/admin/docente/listar.jsp").forward(req, resp);

    } catch (Exception e) {
      req.setAttribute("docentes", Collections.emptyList());
      req.setAttribute("errorDB", "Error al acceder a la base de datos");
      req.getRequestDispatcher("/admin/docente/listar.jsp").forward(req, resp);
    }
  }
}
