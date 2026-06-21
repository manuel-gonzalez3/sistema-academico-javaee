package servlets.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;

import util.DatabaseConnection;

//Servlet para eliminar carrera o curso, comision(se elimina en cascada con curso), y examen .
@WebServlet("/objeto/eliminar")
public class AdminEliminarCaCuComEx extends HttpServlet {

    // Redirige al listado de carreras si se accede por GET directamente.
  @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        // Si intentan acceder por URL directa, redirige al listado
        resp.sendRedirect(req.getContextPath() + "/carrera/listar");
    }

    // Elimina carrera, curso o examen solo si no tienen datos asociados (cursos, docente, alumnos).
  @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        // Verifica rol ADMIN
        if (!util.ServletUtils.checkRol(req, resp, "ADMIN")) return;

        // Obtiene ID desde el formulario
        String idStr = req.getParameter("id");
        String tipo = req.getParameter("tipo"); // carrera | curso | comision | examen
        String idPadre = null; // para redireccionar al nivel superior
        String tipoPadre = null; // para redireccionar al nivel superior
       
        if (idStr == null || idStr.isBlank() || tipo == null || tipo.isBlank()) {
            resp.sendRedirect(req.getContextPath() +
                    "/carrera/listar?error=Faltan+datos");
            return;
        }
        // Verifico que el tipo sea válido
        if(!"carrera".equals(tipo) && !"curso".equals(tipo) && !"examen".equals(tipo)){
        	resp.sendRedirect(req.getContextPath()
        			  + "/carrera/listar?error=Datos+invalidos");
        		  return;
        }
        // Si es curso necesito el idPadre para redireccionar al nivel superior
        // Carrera y examen no tienen padre
        if("curso".equals(tipo)){
        	
        	idPadre = req.getParameter("idPadre");
        	if(idPadre == null || idPadre.isBlank()) {
    			resp.sendRedirect(req.getContextPath() + "/carrera/listar?error=Datos+invalidos");
    			return;
        	}
        	try {
				Integer.parseInt(idPadre); //valido el idPadre
				tipoPadre = "Carrera";
			} catch (NumberFormatException e) {
				resp.sendRedirect(req.getContextPath() + "/carrera/listar?error=Datos+invalidos");
				return;
			}
    	}
        
        // Verifico que el id sea un número, redirige al listado del nivel superior con mensaje de error
        int id;
        try {
            id = Integer.parseInt(idStr.trim());
        } catch (NumberFormatException e) {
            if ("curso".equals(tipo)) {
            	// curso: vuelve al listado de cursos de la carrera
				resp.sendRedirect(req.getContextPath() +
						"/" + tipo + "/listar?id" + tipoPadre + "=" + idPadre +
						"&error=ID+inválido");
			} else {
				// carrera o examen: vuelve al listado propio
				resp.sendRedirect(req.getContextPath() +
						"/" + tipo + "/listar?error=ID+inválido");
			}
			return;
        }
        
        try (Connection cn = DatabaseConnection.getConnection()) {

            // Verifica si existen datos asociados antes de eliminar
        	String errorValidacion = ValidarDatos(cn, tipo, id);
        	//si hay error de validación, redirijo al listado del mismo nivel con mensaje de error, verificando si necesito idPadre
        	if(errorValidacion != null) {
        		if ("curso".equals(tipo)) {
        			// curso: vuelve al listado de cursos de la carrera
        			resp.sendRedirect(req.getContextPath() + "/" + tipo + "/listar?id" + tipoPadre + "=" + idPadre + "&error=" + java.net.URLEncoder.encode(errorValidacion, "UTF-8"));
        		} else {
        			// carrera o examen: vuelve al listado propio
        			resp.sendRedirect(req.getContextPath() + "/" + tipo + "/listar?error=" + java.net.URLEncoder.encode(errorValidacion, "UTF-8"));
        		}
        		return;
        	}

            // Ejecuta DELETE
            try (PreparedStatement ps = cn.prepareStatement(
                    "DELETE FROM " + tipo +  " WHERE Id" + tipo + " = ?")) {

                ps.setInt(1, id);
                ps.executeUpdate();
            }

            // Redirige al listado del mismo nivel con mensaje de éxito
            if ("curso".equals(tipo)) {
            	// curso: vuelve al listado de cursos de la carrera
            	resp.sendRedirect(req.getContextPath() + "/" + tipo + "/listar?id" + tipoPadre + "=" + idPadre + "&ok=" + java.net.URLEncoder.encode(tipo + " eliminado/a correctamente", "UTF-8"));
            } else {
            	// carrera o examen: vuelve al listado propio
            	resp.sendRedirect(req.getContextPath() + "/" + tipo + "/listar?ok=" + java.net.URLEncoder.encode(tipo + " eliminado/a correctamente", "UTF-8"));
            }

        } catch (Exception e) {
        	if ("curso".equals(tipo)) {
        		// curso: vuelve al listado de cursos de la carrera
        		resp.sendRedirect(req.getContextPath() + "/" + tipo + "/listar?id" + tipoPadre + "=" + idPadre + "&error=Error+al+eliminar");
        	} else {
        		// carrera o examen: vuelve al listado propio
        		resp.sendRedirect(req.getContextPath() + "/" + tipo + "/listar?error=Error+al+eliminar" +e.getMessage());
        	}
        }
    }
    
    //funcion para validar que no existan datos asociados a la carrera , curso o examenque se quiere eliminar, dependiendo del tipo recibido por parámetro
    private String ValidarDatos(Connection cn, String tipo, int id) throws SQLException{
    	// Si es carrera, verifico que no tenga cursos asociados
    	if("carrera".equals(tipo)) {
    		try (PreparedStatement ps = cn.prepareStatement(
					"SELECT COUNT(*) FROM curso WHERE IdCarrera = ?")) {
				ps.setInt(1, id);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next() && rs.getInt(1) > 0) { //rs.next() para mover el cursor al primer resultado, luego getInt(1) para obtener el conteo
						return "No se puede eliminar porque hay cursos asociados";
					}
				}
			}
    	}
    	// Si es curso, verifico que no tenga docente asignado ni alumnos inscriptos en comisiones del curso
    	else if("curso".equals(tipo)) {
    		String sqlDocente = "SELECT dniDocente FROM curso WHERE IdCurso = ?"; //consulta para verificar si hay docente asignado al curso
    		String sqlAlumnos= """
    			      SELECT COUNT(*)
    			      FROM comision_alumno ca
    			      JOIN comision c ON c.IdComision = ca.IdComision
    			      WHERE c.IdCurso = ?
    			    """; //consulta para verificar si hay alumnos inscriptos en comisiones del curso
    		
    		//Verifico si hay docente asignado al curso
    		try(PreparedStatement psDocente = cn.prepareStatement(sqlDocente)){
    			psDocente.setInt(1, id);
				try(ResultSet rsDocente = psDocente.executeQuery()){
					if(rsDocente.next() && rsDocente.getObject("dniDocente") != null) {
						return "No se puede eliminar porque hay un docente asignado";
					}
				}
    		}
    		//Verifico si hay alumnos inscriptos en comisiones del curso
			try(PreparedStatement psAlumnos = cn.prepareStatement(sqlAlumnos)){
				psAlumnos.setInt(1, id);
				try(ResultSet rsAlumnos = psAlumnos.executeQuery()){
					if(rsAlumnos.next() && rsAlumnos.getInt(1) > 0) {
						return "No se puede eliminar porque hay alumnos inscriptos";
					}
				}
			}
    	}
    	// Si es examen, verifico que no haya alumnos inscriptos en el examen y este pendiente
    	else if("examen".equals(tipo)) {
    		//consulta para verificar si hay alumnos inscriptos en el examen
			String sqlAlumnos= """
			      SELECT COUNT(*)
			      FROM examen_alumno ea
			      WHERE ea.IdExamen = ?
				  AND ea.asistencia = 'pendiente'
			    """; 
			//consulta para verificar si hay alumnos inscriptos en el examen y este pendiente
			String sqlPendiente = "SELECT estado FROM examen WHERE IdExamen = ?"; //consulta para verificar si el examen esta pendiente
			try(PreparedStatement psAlumnos = cn.prepareStatement(sqlAlumnos)){
				psAlumnos.setInt(1, id);
				try(ResultSet rsAlumnos = psAlumnos.executeQuery()){
					if(rsAlumnos.next() && rsAlumnos.getInt(1) > 0) {
						//Si hay alumnos inscriptos, envio mensaje
						return "No se puede eliminar porque hay alumnos inscriptos";
					}
				}
			}
			//Verifico si el examen esta pendiente, si no lo esta, envio mensaje
			try(PreparedStatement psPendiente = cn.prepareStatement(sqlPendiente)){
				psPendiente.setInt(1, id);
				try(ResultSet rsPendiente = psPendiente.executeQuery()){
					if(rsPendiente.next() && !"pendiente".equalsIgnoreCase(rsPendiente.getString("estado"))) {
						//Si el examen esta pendiente, envio mensaje
						return "No se puede eliminar porque el examen no esta pendiente";
					}
				}
			}
    	}
    	return null;
    }
}
