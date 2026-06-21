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

// Servlet para listar alumnos inscriptos en una comisión específica
@WebServlet("/comision/alumnos")
public class AdminListarAlumnoComision extends HttpServlet {

  // Lista los alumnos inscriptos en la comisión recibida por parámetro.
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    if (!util.ServletUtils.checkRol(req, resp, "ADMIN")) return;

    String idComisionStr = req.getParameter("idComision");

    if (idComisionStr == null || idComisionStr.isBlank()) {
      resp.sendRedirect(req.getContextPath() + "/carrera/listar");
      return;
    }

    int idComision;
    try {
      idComision = Integer.parseInt(idComisionStr);
    } catch (NumberFormatException e) {
      resp.sendRedirect(req.getContextPath() + "/carrera/listar");
      return;
    }
    Integer idCurso = null;

    try (Connection cn = DatabaseConnection.getConnection()) {

      // Obtener idCurso desde la BD usando idComision
      try (PreparedStatement ps = cn.prepareStatement(
          "SELECT IdCurso FROM comision WHERE IdComision = ?")) {
        ps.setInt(1, idComision);
        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            idCurso = rs.getInt("IdCurso");
          }
        }
      }

      // Listar alumnos de la comisión
      String sql = """
          SELECT ca.dni,
                 ca.estado,
                 p.nombre,
                 p.apellido
            FROM comision_alumno ca
            JOIN persona p ON p.dni = ca.dni
           WHERE ca.IdComision = ?
           ORDER BY p.apellido, p.nombre
          """;

      List<Map<String, Object>> alumnos = new ArrayList<>();

      try (PreparedStatement ps = cn.prepareStatement(sql)) {
        ps.setInt(1, idComision);
        try (ResultSet rs = ps.executeQuery()) {
          while (rs.next()) {
            Map<String, Object> alumno = new HashMap<>();
            alumno.put("dni",      rs.getInt("dni"));
            alumno.put("nombre",   rs.getString("nombre"));
            alumno.put("apellido", rs.getString("apellido"));
            alumno.put("estado",   rs.getString("estado"));
            alumnos.add(alumno);
          }
        }
      }

      if (alumnos.isEmpty()) {
        req.setAttribute("errorDB", "No hay alumnos inscriptos en esta comisión.");
      }
      req.setAttribute("alumnos",    alumnos);
      req.setAttribute("idComision", idComision);
      req.setAttribute("idCurso",    idCurso);
      req.getRequestDispatcher("/admin/carrera/curso/comision/alumnos.jsp").forward(req, resp);

    } catch (Exception e) {
      req.setAttribute("alumnos",    List.of());
      req.setAttribute("idComision", idComision);
      req.setAttribute("idCurso",    idCurso);
      req.setAttribute("errorDB",    "Error al acceder a la base de datos");
      req.getRequestDispatcher("/admin/carrera/curso/comision/alumnos.jsp").forward(req, resp);
    }
  }
}
