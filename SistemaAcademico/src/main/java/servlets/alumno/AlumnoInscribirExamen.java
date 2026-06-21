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
import java.util.List;

import entidades.Examen;

/**
 * Servlet implementation class AlumnoExamen
 */
@WebServlet("/alumno/examenes")
public class AlumnoInscribirExamen extends HttpServlet {
	// Lista los exámenes disponibles para inscripción (activos, > 24hs, no inscripto).
  @Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	      throws ServletException, IOException {

	    if (!util.ServletUtils.checkRol(req, resp, "ALUMNO")) return;
	    // Obtengo el DNI del alumno desde la sesión
	    String dni = util.ServletUtils.getDniSesion(req, resp);
	    if (dni == null) return;

	    List<Examen> examenes = new ArrayList<>();


	    // Obtener los exámenes activos de la carrera para los que el alumno esté habilitado(regular o libre),
	    // en los que aún no esté inscripto y que tengan más de 24hs de anticipación.
	    String sqlExamenesPendientes = """
		       SELECT e.IdExamen,
			       e.fecha,
			       e.hora,
			       cu.Nombre AS nombreCurso,
			       p.apellido AS apellidoDocente,
			       p.nombre AS nombreDocente
			  FROM examen e
			  JOIN comision co ON co.IdComision = e.IdComision
			  JOIN curso cu ON cu.IdCurso = co.IdCurso
			  JOIN comision_alumno cal ON cal.IdComision = co.IdComision
			  JOIN persona p ON p.dni = cu.dniDocente
			  WHERE cal.dni = ?
			   AND cal.estado IN ('regular', 'libre')
			   AND e.Activo = 1
			   AND TIMESTAMP(e.fecha, e.hora) > DATE_ADD(NOW(), INTERVAL 24 HOUR)
			   AND NOT EXISTS (
			       SELECT 1 FROM examen_alumno ea
			        WHERE ea.idExamen = e.IdExamen
			          AND ea.dniAlumno = cal.dni
			   )
	        """; //obtiene datos del examen, nombre del curso a traves de la comision del examen, nombre del docente a traves del curso y persona
	    		//solo los examen de las comisiones que el alumno este habilitado para rendir (regular o libre)
	           //el examen debe estar activo, faltar más de 24hs y el alumno no debe estar ya inscripto en ese examen

	    try (Connection cn = DatabaseConnection.getConnection()) {

	      entidades.Carrera carreraAlumno = util.ServletUtils.getCarreraInscripta(cn, dni);
	      if (carreraAlumno == null) {
	        req.setAttribute("error", "No tenés una carrera inscripta.");
	        req.getRequestDispatcher("/alumno/home.jsp").forward(req, resp);
	        return;
	      }
	      req.setAttribute("nombreCarrera", carreraAlumno.getNombre());
	      // Obtengo los exámenes pendientes para esa carrera y ese alumno
	        try (PreparedStatement psExamenes = cn.prepareStatement(sqlExamenesPendientes)) {
	          psExamenes.setInt(1, Integer.parseInt(dni));

	          try (ResultSet rsExamenes = psExamenes.executeQuery()) {
	            DateTimeFormatter fmtFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	            DateTimeFormatter fmtHora  = DateTimeFormatter.ofPattern("HH:mm");
	            while (rsExamenes.next()) {
	              LocalDate fecha = rsExamenes.getDate("fecha").toLocalDate();
	              LocalTime hora  = rsExamenes.getTime("hora").toLocalTime();

	              Examen examen = new Examen();
	              examen.setIdExamen(rsExamenes.getInt("IdExamen"));
	              examen.setFecha(fecha.format(fmtFecha));
	              examen.setHora(hora.format(fmtHora));
	              examen.setNombreCurso(rsExamenes.getString("nombreCurso"));
	              examen.setNombreDocente(rsExamenes.getString("apellidoDocente") + ", " + rsExamenes.getString("nombreDocente"));

	              examenes.add(examen);
	            }
	          }
	        }

	      req.setAttribute("examenes", examenes);
	      if (examenes.isEmpty()) {
	        req.setAttribute("errorDB", "No hay exámenes disponibles para inscribirse.");
	      }

	      if (req.getParameter("ok") != null) {
	        req.setAttribute("ok", req.getParameter("ok"));
	      }
	      if (req.getParameter("error") != null) {
	        req.setAttribute("error", req.getParameter("error"));
	      }
	      
	      req.getRequestDispatcher("/alumno/examenes.jsp").forward(req, resp);

	    } catch (Exception e) {
	      req.setAttribute("examenes", List.of());
	      req.setAttribute("errorDB", "Error al acceder a la base de datos" + e.getMessage());
	      req.getRequestDispatcher("/alumno/examenes.jsp").forward(req, resp);
	    }
	  }

	  // Valida carrera, habilitación del alumno y plazo de 24hs antes de inscribir.
  @Override
	  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	      throws ServletException, IOException {

	    if (!util.ServletUtils.checkRol(req, resp, "ALUMNO")) return;
	    // Obtengo el DNI del alumno desde la sesión y valido que el parámetro idExamen sea un entero válido
	    String dni = (String) req.getSession().getAttribute("userDni");
	    String idExamenParam = req.getParameter("idExamen");
	    if (dni == null || dni.isBlank() || idExamenParam == null || idExamenParam.isBlank()) {
	      resp.sendRedirect(req.getContextPath() + "/alumno/examenes?error=Solicitud+inv%C3%A1lida");
	      return;
	    }

	    int idExamen;
	    try {
	      idExamen = Integer.parseInt(idExamenParam);
	    } catch (NumberFormatException e) {
	      resp.sendRedirect(req.getContextPath() + "/alumno/examenes?error=Examen+inv%C3%A1lido");
	      return;
	    }


	    // Verificar que el examen exista, esté activo y sea de una comision del alumno habilitado.
	    // El chequeo de 24hs se aplica en Java.
	    String sqlExamen = """
    		  SELECT e.IdExamen,
    		         e.fecha,
    		         e.hora
			  FROM examen e
			  JOIN comision_alumno cal ON cal.IdComision = e.IdComision
			 WHERE e.IdExamen = ?
			   AND cal.dni = ?
			   AND cal.estado IN ('regular', 'libre')
			   AND e.Activo = 1
			 LIMIT 1
	        """;//

	    // Verificar que el alumno no esté ya inscripto en este examen
	    String sqlExiste = """
	        SELECT 1
	          FROM examen_alumno
	         WHERE idExamen = ?
	           AND dniAlumno = ?
	         LIMIT 1
	        """;

	    // Inscribir al alumno en el examen con estado Pendiente
	    String sqlInsert = """
	        INSERT INTO examen_alumno (idExamen, dniAlumno)
	        VALUES (?, ?)
	        """;

	    try (Connection cn = DatabaseConnection.getConnection()) {

	      entidades.Carrera carreraAlumno = util.ServletUtils.getCarreraInscripta(cn, dni);
	      if (carreraAlumno == null) {
	        req.setAttribute("error", "No tenés una carrera inscripta.");
	        req.getRequestDispatcher("/alumno/home.jsp").forward(req, resp);
	        return;
	      }
	      int idCarrera = carreraAlumno.getIdCarrera();

	      try (PreparedStatement ps = cn.prepareStatement(sqlExamen)) {
	        ps.setInt(1, idExamen);
	        ps.setInt(2, Integer.parseInt(dni));
	        try (ResultSet rs = ps.executeQuery()) {
	          if (!rs.next()) {
	            resp.sendRedirect(req.getContextPath() + "/alumno/examenes?error=El+examen+no+est%C3%A1+disponible");
	            return;
	          }
	          // Validar que falten más de 24hs usando fecha y hora (no solo fecha)
	          LocalDate fecha = rs.getDate("fecha").toLocalDate();
	          LocalTime hora  = rs.getTime("hora").toLocalTime();
	          if (!LocalDateTime.of(fecha, hora).isAfter(LocalDateTime.now().plusHours(24))) {
	            resp.sendRedirect(req.getContextPath() + "/alumno/examenes?error=El+examen+no+est%C3%A1+disponible");
	            return;
	          }
	        }
	      }

	      try (PreparedStatement ps = cn.prepareStatement(sqlExiste)) {
	        ps.setInt(1, idExamen);
	        ps.setInt(2, Integer.parseInt(dni));
	        try (ResultSet rs = ps.executeQuery()) {
	          if (rs.next()) {
	            resp.sendRedirect(req.getContextPath() + "/alumno/examenes?error=Ya+est%C3%A1s+inscripto+en+este+examen");
	            return;
	          }
	        }
	      }

	      try (PreparedStatement ps = cn.prepareStatement(sqlInsert)) {
	        ps.setInt(1, idExamen);
	        ps.setInt(2, Integer.parseInt(dni));
	        ps.executeUpdate();
	      }

	      resp.sendRedirect(req.getContextPath() + "/alumno/examenes?ok=Te+inscribiste+correctamente+en+el+examen");

	    } catch (Exception e) {
	      resp.sendRedirect(req.getContextPath() + "/alumno/examenes?error=No+se+pudo+realizar+la+inscripci%C3%B3n" );
	    }
	  }

}
