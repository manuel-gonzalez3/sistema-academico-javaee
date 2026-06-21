package servlets.alumno;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.DatabaseConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet implementation class AlumnoExamenInscripto
 */
@WebServlet("/alumno/examenesInscripto")
public class AlumnoExamenInscripto extends HttpServlet {
	// Carga los exámenes inscriptos del alumno e indica si puede darse de baja (> 24hs).
  @Override
	  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	      throws ServletException, IOException {

	    if (!util.ServletUtils.checkRol(req, resp, "ALUMNO")) return;

	    String dni = util.ServletUtils.getDniSesion(req, resp);
	    if (dni == null) return;


	    // Obtener los examenes en los que el alumno esta inscripto, muestra nombrecurso, fecha, hora, su estado, nota . solo los de la carreara que esta inscripto
	    String sqlExamen = """
		       SELECT e.IdExamen,
			       e.fecha,
			       e.hora,
			       cu.Nombre       AS nombreCurso,
			       ea.asistencia   AS asistencia,
			       ea.nota
			  FROM examen_alumno ea
			  JOIN examen e         ON e.IdExamen    = ea.idExamen
			  JOIN comision co      ON co.IdComision = e.IdComision
			  JOIN curso cu         ON cu.IdCurso    = co.IdCurso
			 WHERE ea.dniAlumno = ?
			   AND cu.IdCarrera = ?
			 ORDER BY e.fecha DESC, e.hora DESC
	    		""";

	    List<Map<String, Object>> examanes = new ArrayList<>();

	    try (Connection cn = DatabaseConnection.getConnection()) {

	      entidades.Carrera carreraAlumno = util.ServletUtils.getCarreraInscripta(cn, dni);
	      if (carreraAlumno == null) {
	        req.setAttribute("error", "No tenés una carrera inscripta.");
	        req.getRequestDispatcher("/alumno/home.jsp").forward(req, resp);
	        return;
	      }
	      int idCarrera = carreraAlumno.getIdCarrera();
	      req.setAttribute("nombreCarrera", carreraAlumno.getNombre());

	        try (PreparedStatement psExamenes = cn.prepareStatement(sqlExamen)) {
	          psExamenes.setInt(1, Integer.parseInt(dni));
	          psExamenes.setInt(2, idCarrera);

	          try (ResultSet rsExamenes = psExamenes.executeQuery()) {
	            DateTimeFormatter fmtFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	            DateTimeFormatter fmtHora  = DateTimeFormatter.ofPattern("HH:mm");
	            while (rsExamenes.next()) {
	              Map<String, Object> examenes = new HashMap<>();
	              examenes.put("idExamen", rsExamenes.getInt("IdExamen"));
	              LocalDate fecha = rsExamenes.getDate("fecha").toLocalDate();
	              LocalTime hora  = rsExamenes.getTime("hora").toLocalTime();
	              examenes.put("fecha", fecha.format(fmtFecha));
	              examenes.put("hora", hora.format(fmtHora));
	              examenes.put("asistencia", rsExamenes.getString("asistencia") != null ? rsExamenes.getString("asistencia") : "-");
	              examenes.put("nombreCurso", rsExamenes.getString("nombreCurso"));
	              examenes.put("nota", rsExamenes.getObject("nota") != null ? rsExamenes.getObject("nota") : "-");
	              //si esta a mas de 24 horas del inicio de examen, puede darse de baja,
	              //puedeBaja true: si la fecha y hora del examen es posterior a la fecha y hora actual + 24 horas, false en caso contrario
	              boolean puedeBaja = java.time.LocalDateTime.of(fecha, hora).isAfter(java.time.LocalDateTime.now().plusHours(24));
	              examenes.put("puedeBaja", puedeBaja);
	              examanes.add(examenes);
	            }
	          }
	        }

	      req.setAttribute("examanes", examanes);
	      if (examanes.isEmpty()) {
	        req.setAttribute("errorDB", "No hay examenes inscripto.");
      }
      req.getRequestDispatcher("/alumno/examenesInscripto.jsp").forward(req, resp);

    } catch (Exception e) {
      req.setAttribute("cursos", List.of());
      req.setAttribute("errorDB", "Error al acceder a la base de datos");
      req.getRequestDispatcher("/alumno/examenesInscripto.jsp").forward(req, resp);
    }
  }
}
