package servlets.docente;

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

// Servlet para que el docente vea los cursos que tiene asignados
@WebServlet("/docente/curso/listar")
public class DocenteListarCurso extends HttpServlet {

  // Lista los cursos del docente con sus comisiones activas, cupo e inscriptos.
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    if (!util.ServletUtils.checkRol(req, resp, "DOCENTE")) return;

    String dni = util.ServletUtils.getDniSesion(req, resp);
    if (dni == null) return;

    String sql = """
        SELECT c.IdCurso,
               c.Nombre AS nombreCurso,
               c.Activo,
               ca.Nombre AS nombreCarrera,
               co.IdComision,
               co.CantAlumnos,
               COUNT(cal.dni) AS alumnosInscriptos,
               ROW_NUMBER() OVER (PARTITION BY c.IdCurso ORDER BY co.IdComision) AS nroComision
          FROM curso c
          JOIN carrera ca ON ca.IdCarrera = c.IdCarrera
          LEFT JOIN comision co ON co.IdCurso = c.IdCurso AND co.Activo = 1
          LEFT JOIN comision_alumno cal ON cal.IdComision = co.IdComision
         WHERE c.dniDocente = ?
         GROUP BY c.IdCurso, c.Nombre, c.Activo, ca.Nombre, co.IdComision, co.CantAlumnos
         ORDER BY ca.Nombre, c.Nombre, co.IdComision
        """;

    List<Map<String, Object>> cursos = new ArrayList<>();

    try (Connection cn = DatabaseConnection.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql)) {

      ps.setString(1, dni);

      try (ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
          int idCursoActual = rs.getInt("IdCurso");

          Map<String, Object> curso = new HashMap<>();
          curso.put("idCurso", idCursoActual);
          curso.put("nombre", rs.getString("nombreCurso"));
          curso.put("activo", rs.getBoolean("Activo"));
          curso.put("carrera", rs.getString("nombreCarrera"));
          curso.put("idComision", rs.getObject("IdComision")); // Puede ser null si no hay comisiones activas
          curso.put("cupo", rs.getObject("CantAlumnos"));
          curso.put("inscriptos", rs.getInt("alumnosInscriptos"));
          curso.put("nroComision", rs.getInt("nroComision"));
          cursos.add(curso);
        }
      }

      req.setAttribute("cursos", cursos);
      req.setAttribute("ok", req.getParameter("ok"));
      req.setAttribute("error", req.getParameter("error"));
      if (cursos.isEmpty()) {
        req.setAttribute("errorDB", "No tiene cursos asignados.");
      }
      req.getRequestDispatcher("/docente/curso/listar.jsp").forward(req, resp);

    } catch (Exception e) {
      req.setAttribute("cursos", List.of());
      req.setAttribute("errorDB", "Error al acceder a la base de datos");
      req.getRequestDispatcher("/docente/curso/listar.jsp").forward(req, resp);
    }
  }
}
