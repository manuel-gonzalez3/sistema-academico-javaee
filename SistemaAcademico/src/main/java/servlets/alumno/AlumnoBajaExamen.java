package servlets.alumno;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.*;

import java.io.IOException;
import java.sql.*;

import util.DatabaseConnection;

// Da de baja al alumno de un examen inscripto. Solo accesible para ALUMNO.
// Solo permite la baja si faltan más de 24hs para el examen.
@WebServlet("/alumno/bajaExamen")
public class AlumnoBajaExamen extends HttpServlet {

  // Redirige a la lista de exámenes inscriptos.
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    if (!util.ServletUtils.checkRol(req, resp, "ALUMNO")) return;
    resp.sendRedirect(req.getContextPath() + "/alumno/examenesInscripto");
  }

  // Valida el plazo de 24hs y elimina la inscripción de examen_alumno.
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    req.setCharacterEncoding("UTF-8");
    if (!util.ServletUtils.checkRol(req, resp, "ALUMNO")) return;

    String dni = (String) req.getSession().getAttribute("userDni");
    String idExamenParam = req.getParameter("id");

    if (dni == null || dni.isBlank() || idExamenParam == null || idExamenParam.isBlank()) {
      resp.sendRedirect(req.getContextPath() + "/alumno/examenesInscripto?error=Datos+inv%C3%A1lidos");
      return;
    }
    String sqlVerificar = """
        SELECT e.fecha,
               e.hora
          FROM examen e
          JOIN examen_alumno ea ON ea.idExamen = e.IdExamen
         WHERE e.IdExamen = ?
           AND ea.dniAlumno = ?
         LIMIT 1
        """;

    try (Connection cn = DatabaseConnection.getConnection()) {

      // Verificar que el examen exista para este alumno y que no sea el día anterior o el mismo día
      try (PreparedStatement ps = cn.prepareStatement(sqlVerificar)) {
        ps.setInt(1, Integer.parseInt(idExamenParam));
        ps.setInt(2, Integer.parseInt(dni));
        try (ResultSet rs = ps.executeQuery()) {
          if (!rs.next()) {
            resp.sendRedirect(req.getContextPath() + "/alumno/examenesInscripto?error=No+se+encontr%C3%B3+la+inscripci%C3%B3n");
            return;
          }
          // Validar que falten más de 24hs usando fecha y hora (no solo fecha)
          java.time.LocalDate fechaExamen = rs.getDate("fecha").toLocalDate();
          java.time.LocalTime horaExamen  = rs.getTime("hora").toLocalTime();
          if (java.time.LocalDateTime.now().plusHours(24).isAfter(java.time.LocalDateTime.of(fechaExamen, horaExamen))) {
            resp.sendRedirect(req.getContextPath() + "/alumno/examenesInscripto?error=No+se+puede+dar+de+baja+el+d%C3%ADa+anterior+o+el+mismo+d%C3%ADa+del+examen");
            return;
          }
        }
      }

      // Dar de baja al alumno del examen
      try (PreparedStatement ps = cn.prepareStatement(
          "DELETE FROM examen_alumno WHERE idExamen = ? AND dniAlumno = ?")) {
        ps.setInt(1, Integer.parseInt(idExamenParam));
        ps.setInt(2, Integer.parseInt(dni));
        ps.executeUpdate();
      }

      resp.sendRedirect(req.getContextPath() + "/alumno/examenesInscripto?ok=Baja+realizada+correctamente");

    } catch (Exception e) {
      resp.sendRedirect(req.getContextPath() + "/alumno/examenesInscripto?error=Error+al+dar+de+baja");
    }
  }
}
