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

// Lista los cursos asignados a un docente desde la vista de admin. Solo accesible para ADMIN.
@WebServlet("/admin/docente/cursos")
public class AdminListarCursoDocente extends HttpServlet {

  // Consulta los cursos del docente indicado por DNI, incluyendo el nombre de la carrera.
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    if (!util.ServletUtils.checkRol(req, resp, "ADMIN")) return;
     // Validación de parámetros
    String dni = req.getParameter("dni");
    if (dni == null || dni.isBlank()) {
      resp.sendRedirect(req.getContextPath() + "/docente/listar");
      return;
    }

    String sql = """
        SELECT c.IdCurso,
               c.Nombre AS nombreCurso,
               c.Activo,
               ca.Nombre AS nombreCarrera
          FROM curso c
          JOIN carrera ca ON ca.IdCarrera = c.IdCarrera
         WHERE c.dniDocente = ?
         ORDER BY ca.Nombre, c.Nombre
        """;

    // Uso Map para almacenar el nombre de la carrera
    List<Map<String, Object>> cursos = new ArrayList<>();
    
    // Se obtiene la lista de cursos asignados al docente, incluyendo el nombre de la carrera a la que pertenecen
    try (Connection cn = DatabaseConnection.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql)) {

      ps.setString(1, dni);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Map<String, Object> curso = new HashMap<>();
          curso.put("nombre", rs.getString("nombreCurso"));
          curso.put("activo", rs.getBoolean("Activo"));
          curso.put("carrera", rs.getString("nombreCarrera"));
          cursos.add(curso);
        }
      }
      if (cursos.isEmpty()) {
          req.setAttribute("errorDB", "El docente no tiene cursos asignados.");
        }
      req.setAttribute("cursos", cursos);
      req.getRequestDispatcher("/admin/docente/cursos.jsp").forward(req, resp);

    } catch (Exception e) {
      req.setAttribute("cursos", List.of());
      req.setAttribute("errorDB", "Error al acceder a la base de datos");
      req.getRequestDispatcher("/admin/docente/cursos.jsp").forward(req, resp);
    }
  }
}