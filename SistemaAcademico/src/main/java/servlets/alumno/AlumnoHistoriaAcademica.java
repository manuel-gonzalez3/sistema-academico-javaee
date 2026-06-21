package servlets.alumno;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.DatabaseConnection;

// Servlet que muestra los cursos realizados por el alumno con su estado y nota
@WebServlet("/alumno/historiaAcademica")
public class AlumnoHistoriaAcademica extends HttpServlet {

  // Muestra el historial académico del alumno: cursos, estado y nota por comisión.
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    if (!util.ServletUtils.checkRol(req, resp, "ALUMNO")) return;

    String dni = util.ServletUtils.getDniSesion(req, resp);
    if (dni == null) return;


    // Obtener los cursos en los que el alumno estuvo o está inscripto
    String sqlCursos = """
	    SELECT cu.IdCurso,
	           cu.Nombre,
	           ca.estado,
	           ca.nota,
	           DENSE_RANK() OVER (PARTITION BY cu.IdCurso ORDER BY co.IdComision) AS nroComision
	      FROM curso cu                       
	      JOIN comision co ON co.IdCurso = cu.IdCurso
	      JOIN comision_alumno ca ON ca.IdComision = co.IdComision
	     WHERE cu.IdCarrera = ?
	       AND ca.dni = ?
	     ORDER BY cu.Nombre
	    """;

    List<Map<String, Object>> cursos = new ArrayList<>();

    try (Connection cn = DatabaseConnection.getConnection()) {

      entidades.Carrera carreraAlumno = util.ServletUtils.getCarreraInscripta(cn, dni);
      if (carreraAlumno == null) {
        req.setAttribute("error", "No tenés una carrera inscripta.");
        req.getRequestDispatcher("/alumno/home.jsp").forward(req, resp);
        return;
      }
      int idCarrera = carreraAlumno.getIdCarrera();
      req.setAttribute("nombreCarrera", carreraAlumno.getNombre());

        try (PreparedStatement psCursos = cn.prepareStatement(sqlCursos)) {
          psCursos.setInt(1, idCarrera);
          psCursos.setInt(2, Integer.parseInt(dni));

          try (ResultSet rsCursos = psCursos.executeQuery()) {
            while (rsCursos.next()) {
              Map<String, Object> curso = new HashMap<>();
              curso.put("idCurso", rsCursos.getInt("IdCurso"));
              curso.put("nombre", rsCursos.getString("Nombre"));
              curso.put("estado", rsCursos.getString("estado"));
              curso.put("nota", rsCursos.getObject("nota") != null ? rsCursos.getObject("nota") : "-");
              curso.put("nroComision", rsCursos.getInt("nroComision"));
              cursos.add(curso);
            }
          }
        }

      req.setAttribute("cursos", cursos);
      if (cursos.isEmpty()) {
        req.setAttribute("errorDB", "No hay cursos en tu historia académica.");
      }
      req.getRequestDispatcher("/alumno/historiaAcademica.jsp").forward(req, resp);

    } catch (Exception e) {
      req.setAttribute("cursos", List.of());
      req.setAttribute("errorDB", "Error al acceder a la base de datos" );
      req.getRequestDispatcher("/alumno/historiaAcademica.jsp").forward(req, resp);
    }
  }
}
