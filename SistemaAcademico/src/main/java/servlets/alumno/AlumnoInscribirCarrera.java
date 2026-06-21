package servlets.alumno;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import entidades.Carrera;
import util.DatabaseConnection;

// Gestión de inscripción a carrera, un alumno solo puede estar inscripto a una carrera
@WebServlet("/alumno/carrera")
public class AlumnoInscribirCarrera extends HttpServlet {

  // Muestra las carreras activas disponibles; si ya está inscripto, redirige al home.
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    if (!util.ServletUtils.checkRol(req, resp, "ALUMNO")) return;

    String dni = util.ServletUtils.getDniSesion(req, resp);
    if (dni == null) return;

    // Verificar si el alumno ya tiene una carrera inscripta
    String sqlCarreraActual = """
        SELECT ac.IdCarrera, ac.estado, c.Nombre AS nombreCarrera
          FROM alumno_carrera ac
          JOIN carrera c ON c.IdCarrera = ac.IdCarrera
         WHERE ac.dni = ?
           AND UPPER(ac.estado) = 'INSCRIPTO'
         LIMIT 1
        """;

    String sqlCarrerasActivas =
        "SELECT IdCarrera, Nombre FROM carrera WHERE Activo = 1 ORDER BY Nombre";

    List<Carrera> carreras = new ArrayList<>();

    try (Connection cn = DatabaseConnection.getConnection()) {

      try (PreparedStatement ps = cn.prepareStatement(sqlCarreraActual)) {
        ps.setString(1, dni);
        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            req.setAttribute("ok", "Ya estás inscripto a la carrera " + rs.getString("nombreCarrera"));
            req.getRequestDispatcher("/alumno/home.jsp").forward(req, resp);
            return;
          }
        }
      }

      try (PreparedStatement ps = cn.prepareStatement(sqlCarrerasActivas);
           ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Carrera carrera = new Carrera();
          carrera.setIdCarrera(rs.getInt("IdCarrera"));
          carrera.setNombre(rs.getString("Nombre"));
          carreras.add(carrera);
        }
      }

      req.setAttribute("carreras", carreras);
      req.getRequestDispatcher("/alumno/carrera.jsp").forward(req, resp);

    } catch (Exception e) {
      req.setAttribute("carreras", List.of());
      req.setAttribute("errorDB", "Error al acceder a la base de datos");
      req.getRequestDispatcher("/alumno/carrera.jsp").forward(req, resp);
    }
  }

  // Inscribe al alumno en la carrera seleccionada (INSERT con ON DUPLICATE KEY UPDATE para permitir re-inscripción)
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    if (!util.ServletUtils.checkRol(req, resp, "ALUMNO")) return;

    String dni = util.ServletUtils.getDniSesion(req, resp);
    if (dni == null) return;

    String idCarreraStr = req.getParameter("idCarrera");
    if (idCarreraStr == null || idCarreraStr.isBlank()) {
      req.setAttribute("error", "Seleccione una carrera");
      req.getRequestDispatcher("/alumno/carrera.jsp").forward(req, resp);
      return;
    }

    int idCarrera;
    try {
      idCarrera = Integer.parseInt(idCarreraStr);
    } catch (NumberFormatException ex) {
      req.setAttribute("error", "Seleccione una carrera");
      req.getRequestDispatcher("/alumno/carrera.jsp").forward(req, resp);
      return;
    }

    // Inscribir por dni directamente
    String sql = """
        INSERT INTO alumno_carrera (dni, IdCarrera, estado)
        VALUES (?, ?, 'INSCRIPTO')
        ON DUPLICATE KEY UPDATE estado = 'INSCRIPTO'
        """;

    try (Connection cn = DatabaseConnection.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql)) {
      ps.setInt(1, Integer.parseInt(dni));
      ps.setInt(2, idCarrera);
      ps.executeUpdate();

      req.setAttribute("ok", "Inscripción realizada");
      req.getRequestDispatcher("/alumno/home.jsp").forward(req, resp);

    } catch (Exception e) {
      req.setAttribute("error", "Error al guardar la inscripción");
      req.getRequestDispatcher("/alumno/home.jsp").forward(req, resp);
    }
  }
}
