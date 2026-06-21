package servlets.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.DatabaseConnection;

// Servlet para listar alumnos inscriptos en un examen específico
@WebServlet("/examen/alumnos")
public class AdminListarAlumnoExamen extends HttpServlet {

  // Lista los alumnos inscriptos en el examen recibido por parámetro, con asistencia y nota.
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    if (!util.ServletUtils.checkRol(req, resp, "ADMIN")) return;
    // Valido que idExamen sea un entero válido
    String idExamenStr = req.getParameter("idExamen");

    if (idExamenStr == null || idExamenStr.isBlank()) {
      resp.sendRedirect(req.getContextPath() + "/carrera/listar");
      return;
    }

    int idExamen;
    try {
      idExamen = Integer.parseInt(idExamenStr);
    } catch (NumberFormatException e) {
      resp.sendRedirect(req.getContextPath() + "/carrera/listar");
      return;
    }
    Integer idComision = null;

    try (Connection cn = DatabaseConnection.getConnection()) {

      // Obtener idComision desde la BD usando idExamen para mostrar en encabezado y para usar en el enlace de volver a exámenes
      try (PreparedStatement ps = cn.prepareStatement(
          "SELECT IdComision FROM examen WHERE IdExamen = ?")) {
        ps.setInt(1, idExamen);
        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            idComision = rs.getInt("IdComision");
          }
        }
      }

      // Listar alumnos del examen, junto con su estado y nota, ordenados por apellido y nombre
      String sql = """
          SELECT ea.dniAlumno AS dni,
                 ea.asistencia,
                 ea.nota,
                 p.nombre,
                 p.apellido
            FROM examen_alumno ea
            JOIN persona p ON p.dni = ea.dniAlumno
           WHERE ea.idExamen = ?
           ORDER BY p.apellido, p.nombre
          """;

      List<Map<String, Object>> alumnos = new ArrayList<>();

      try (PreparedStatement ps = cn.prepareStatement(sql)) {
        ps.setInt(1, idExamen);
        try (ResultSet rs = ps.executeQuery()) {
          while (rs.next()) {
            Map<String, Object> alumno = new HashMap<>();
            alumno.put("dni",      rs.getInt("dni"));
            alumno.put("nombre",   rs.getString("nombre"));
            alumno.put("apellido", rs.getString("apellido"));
            alumno.put("asistencia", rs.getString("asistencia") != null ? rs.getString("asistencia") : "-");
            if(rs.getInt("nota") == 0) {
			  alumno.put("nota", "-");
			} else {
				alumno.put("nota", rs.getInt("nota"));
			}
            
            alumnos.add(alumno);
          }
        }
      }

      if (alumnos.isEmpty()) {
        req.setAttribute("errorDB", "No hay alumnos inscriptos en este examen.");
      }
      req.setAttribute("alumnos",    alumnos);
      req.setAttribute("idExamen",   idExamen);
      req.setAttribute("idComision", idComision);
      req.getRequestDispatcher("/admin/examen/alumnos.jsp").forward(req, resp);

    } catch (Exception e) {
      req.setAttribute("alumnos",    List.of());
      req.setAttribute("idExamen",   idExamen);
      req.setAttribute("idComision", idComision);
      req.setAttribute("errorDB",    "Error al acceder a la base de datos");
      req.getRequestDispatcher("/admin/examen/alumnos.jsp").forward(req, resp);
    }
  }
}
