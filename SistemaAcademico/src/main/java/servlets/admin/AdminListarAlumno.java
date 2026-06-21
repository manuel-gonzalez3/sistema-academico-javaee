package servlets.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;
import java.util.*;

import util.DatabaseConnection;

// Lista todos los alumnos con su estado y carrera inscripta. Solo accesible para ADMIN.
@WebServlet("/alumno/listar")
public class AdminListarAlumno extends HttpServlet {

  // Consulta persona + usuario (ALUMNO) + alumno_carrera y muestra el listado.
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    if (!util.ServletUtils.checkRol(req, resp, "ADMIN")) return;

    // Uso Map porque necesito datos de persona, usuario (activo) y carrera
    List<Map<String, Object>> alumnos = new ArrayList<>();

    String sql = """
        SELECT p.dni,
               p.nombre,
               p.apellido,
               u.activo,
               c.nombre AS nombreCarrera
          FROM persona p
          JOIN usuario u ON u.dni = p.dni AND u.tipoUsuario = 'ALUMNO'
          LEFT JOIN alumno_carrera ac ON ac.dni = p.dni AND UPPER(ac.estado) = 'INSCRIPTO'
          LEFT JOIN carrera c ON c.IdCarrera = ac.IdCarrera
         ORDER BY p.apellido, p.nombre
        """;

    try (Connection cn = DatabaseConnection.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        Map<String, Object> alumno = new HashMap<>();
        alumno.put("dni", rs.getString("dni"));
        alumno.put("nombre", rs.getString("nombre"));
        alumno.put("apellido", rs.getString("apellido"));
        alumno.put("carrera", rs.getString("nombreCarrera") != null ? rs.getString("nombreCarrera") : "Sin Carrera");
        alumno.put("activo", rs.getBoolean("activo"));
        alumnos.add(alumno);
      }

      if (alumnos.isEmpty()) {
        req.setAttribute("errorDB", "No hay alumnos para mostrar");
      }

      req.setAttribute("alumnos", alumnos);
      req.getRequestDispatcher("/admin/alumno/listar.jsp").forward(req, resp);

    } catch (Exception e) {
      req.setAttribute("alumnos", Collections.emptyList());
      req.setAttribute("errorDB", "Error al acceder a la base de datos");
      req.getRequestDispatcher("/admin/alumno/listar.jsp").forward(req, resp);
    }
  }
}
