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

/**
 * Servlet implementation class AlumnoCarreraBaja
 */
@WebServlet("/alumno/carrera/baja")
public class AlumnoBajaCarrera extends HttpServlet {
	// Redirige a la lista de exámenes inscriptos si se accede por GET.
  @Override
	  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	      throws ServletException, IOException {
		if (!util.ServletUtils.checkRol(req, resp, "ALUMNO")) return;
	    resp.sendRedirect(req.getContextPath() + "/alumno/examenesInscripto");
	  }
	
	
	// Verifica inscripción activa y cambia el estado a BAJA.
  @Override
	  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	      throws ServletException, IOException {

	    req.setCharacterEncoding("UTF-8");
	    if (!util.ServletUtils.checkRol(req, resp, "ALUMNO")) return;

	    String dni = util.ServletUtils.getDniSesion(req, resp);
	    if (dni == null) return;

	    String sqlBajaCarrera = """
	    		UPDATE alumno_carrera ac SET estado = 'BAJA'
	    		WHERE ac.dni = ? AND ac.IdCarrera = ?
	    		""";
	    int idCarrera;
	    try (Connection cn = DatabaseConnection.getConnection()) {

	        entidades.Carrera carreraAlumno = util.ServletUtils.getCarreraInscripta(cn, dni);
	        if (carreraAlumno == null) {
	          req.setAttribute("error", "No estás inscripto en ninguna carrera.");
	          req.getRequestDispatcher("/alumno/home.jsp").forward(req, resp);
	          return;
	        }
	        idCarrera = carreraAlumno.getIdCarrera();
	        try (PreparedStatement ps = cn.prepareStatement(sqlBajaCarrera)) {
		          ps.setInt(1, Integer.parseInt(dni));
		          ps.setInt(2, idCarrera);
		          ps.executeUpdate();

		          req.setAttribute("ok", "Baja realizada");
		          req.getRequestDispatcher("/alumno/home.jsp").forward(req, resp);

		    }
		}
	    catch(Exception e) {
	    	req.setAttribute("errorDB", "No se pudo gestionar la baja");
            req.getRequestDispatcher("/alumno/home.jsp").forward(req, resp);
            return;
	    }
	}
}
