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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import entidades.Examen;
import util.DatabaseConnection;

// Servlet para que el docente vea los alumnos inscriptos en una comisión y pueda cerrar el cursado
// Lista los exámenes activos del docente. Solo accesible para DOCENTE.
@WebServlet("/docente/examen/listar")
public class DocenteListarExamen extends HttpServlet {

  // Consulta exámenes del docente ordenados por carrera, curso, comisión y fecha.
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    if (!util.ServletUtils.checkRol(req, resp, "DOCENTE")) return;


    String dniDocente = util.ServletUtils.getDniSesion(req, resp);
    if (dniDocente == null) return;
    List<Examen> examenes = new ArrayList<>();
    // Validar que la comisión pertenezca a un curso del docente
    String sqlExamen = """
        SELECT e.IdExamen,
               e.fecha,
               e.hora,
               e.estado,
               ca.Nombre AS nombreCarrera,
               cu.Nombre AS nombreCurso,
               DENSE_RANK() OVER (PARTITION BY co.IdCurso ORDER BY co.IdComision) AS nroComision
          FROM examen e
          JOIN comision co ON co.IdComision = e.IdComision
          JOIN curso    cu ON cu.IdCurso    = co.IdCurso
          JOIN carrera  ca ON ca.IdCarrera  = cu.IdCarrera
         WHERE cu.dniDocente = ?
           AND e.Activo = 1
         ORDER BY ca.Nombre ASC,
                  cu.Nombre ASC,
                  nroComision ASC,
                  e.fecha DESC
        """;

    try (Connection cn = DatabaseConnection.getConnection()) {
      try (PreparedStatement ps = cn.prepareStatement(sqlExamen)) {
        ps.setString(1, dniDocente);
        try (ResultSet rs = ps.executeQuery()) {
    	  DateTimeFormatter fmtFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
          DateTimeFormatter fmtHora  = DateTimeFormatter.ofPattern("HH:mm");
          while (rs.next()) {
            LocalDate fecha = rs.getDate("fecha").toLocalDate();
            LocalTime hora  = rs.getTime("hora").toLocalTime();
            Examen examen = new Examen();
            examen.setIdExamen(rs.getInt("IdExamen"));
            examen.setFecha(fecha.format(fmtFecha));
            examen.setHora(hora.format(fmtHora));
            examen.setEstado(rs.getString("estado"));
            examen.setNombreCarrera(rs.getString("nombreCarrera"));
            examen.setNombreCurso(rs.getString("nombreCurso"));
            examen.setNroComision(rs.getString("nroComision"));
            examenes.add(examen);
          }
        }
      }
      req.setAttribute("examenes", examenes);
      req.setAttribute("ok", req.getParameter("ok"));
      req.setAttribute("error", req.getParameter("error"));
      if (examenes.isEmpty()) {
        req.setAttribute("errorDB", "No tenés exámenes asignados.");
      }

      req.getRequestDispatcher("/docente/examen/listar.jsp").forward(req, resp);

    } catch (Exception e) {
      req.setAttribute("examenes", List.of());
      req.setAttribute("errorDB", "Error al acceder a la base de datos");
      req.getRequestDispatcher("/docente/examen/listar.jsp").forward(req, resp);
    }
  }


}
