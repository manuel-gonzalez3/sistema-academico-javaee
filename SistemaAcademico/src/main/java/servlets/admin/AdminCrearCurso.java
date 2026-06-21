package servlets.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;

import util.DatabaseConnection;

// Crea un nuevo curso junto con sus dos comisiones. Solo accesible para ADMIN.
@WebServlet("/curso/crear")
public class AdminCrearCurso extends HttpServlet {

    // Muestra el formulario de alta de curso.
  @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!util.ServletUtils.checkRol(req, resp, "ADMIN")) return;

        req.getRequestDispatcher("/admin/carrera/curso/crear.jsp").forward(req, resp);
    }

    // Valida campos, verifica nombre único por carrera e inserta el curso y sus dos comisiones.
  @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!util.ServletUtils.checkRol(req, resp, "ADMIN")) return;

        req.setCharacterEncoding("UTF-8");
        //crear curso y comisiones asociadas
        String nombre = req.getParameter("nombre");
        String idCarreraStr = req.getParameter("idCarrera");
        String cant1Str = req.getParameter("cantComision1");
        String cant2Str = req.getParameter("cantComision2");

        if (nombre == null || nombre.trim().isEmpty()
                || idCarreraStr == null
                || cant1Str == null
                || cant2Str == null) {

            req.setAttribute("error", "Todos los campos son obligatorios.");
            req.getRequestDispatcher("/admin/carrera/curso/crear.jsp").forward(req, resp);
            return;
        }

        // Validar que idCarrera, cant1 y cant2 sean enteros positivos
        int idCarrera, cant1, cant2;
        try {
            idCarrera = Integer.parseInt(idCarreraStr);
            cant1     = Integer.parseInt(cant1Str);
            cant2     = Integer.parseInt(cant2Str);
        } catch (NumberFormatException e) {
            req.setAttribute("error", "Los campos numéricos son inválidos.");
            req.getRequestDispatcher("/admin/carrera/curso/crear.jsp").forward(req, resp);
            return;
        }
        if (cant1 <= 0 || cant2 <= 0) {
            req.setAttribute("error", "El cupo de cada comisión debe ser mayor a 0.");
            req.getRequestDispatcher("/admin/carrera/curso/crear.jsp").forward(req, resp);
            return;
        }
        String sqlExiste =
			"SELECT COUNT(*) FROM CURSO WHERE Nombre = ? AND IdCarrera = ?";
        
        String sqlCurso =
            "INSERT INTO CURSO (Nombre, IdCarrera, Activo) VALUES (?, ?, 1)";

        String sqlComision =
            "INSERT INTO COMISION (IdCurso, CantAlumnos, Activo) VALUES (?, ?, 1)";

        try (Connection cn = DatabaseConnection.getConnection()) {

            //verificar que no exista un curso con el mismo nombre en la misma carrera
        	try (PreparedStatement psExiste = cn.prepareStatement(sqlExiste)) {
        		psExiste.setString(1, nombre.trim());
				psExiste.setInt(2, idCarrera);
				try (ResultSet rs = psExiste.executeQuery()) {
					rs.next();
					if (rs.getInt(1) > 0) {
						req.setAttribute("error", "Ya existe un curso con ese nombre en la carrera seleccionada.");
						req.getRequestDispatcher("/admin/carrera/curso/crear.jsp").forward(req, resp);
						return;
					}
				}
        	}

            int idCurso;

            // Crear curso
            try (PreparedStatement psCurso =
                     cn.prepareStatement(sqlCurso, Statement.RETURN_GENERATED_KEYS)) { //el insert de curso y obtengo el id generado por el autoincrement, para luego crear comisiones

                psCurso.setString(1, nombre.trim());
                psCurso.setInt(2, idCarrera);
                psCurso.executeUpdate();

                try (ResultSet rs = psCurso.getGeneratedKeys()) {
                    rs.next();
                    idCurso = rs.getInt(1); //obtengo el id generado
                }
            }

            //Crear comisiones con el id de curso generado y las cantidades ingresadas
            try (PreparedStatement psCom =
                     cn.prepareStatement(sqlComision)) {
                psCom.setInt(1, idCurso);
                psCom.setInt(2, cant1);
                psCom.executeUpdate();

                psCom.setInt(1, idCurso);
                psCom.setInt(2, cant2);
                psCom.executeUpdate();
            }

            resp.sendRedirect(req.getContextPath() +
                "/curso/listar?idCarrera=" + idCarrera +
                "&ok=Curso+creado+correctamente");

        } catch (Exception e) {
            req.setAttribute("error", "Error al crear el curso");
            req.getRequestDispatcher("/admin/carrera/curso/crear.jsp").forward(req, resp);
        }
    }
}
