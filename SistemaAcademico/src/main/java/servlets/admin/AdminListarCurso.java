package servlets.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;
import java.util.*;

import util.DatabaseConnection;
import entidades.Curso;

// Lista los cursos de una carrera con el docente asignado. Solo accesible para ADMIN.
@WebServlet("/curso/listar")
public class AdminListarCurso extends HttpServlet {
    // Consulta cursos de la carrera indicada incluyendo el nombre del docente (puede ser nulo).
  @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!util.ServletUtils.checkRol(req, resp, "ADMIN")) return;
        // Validación de parámetros
        String idCarreraStr = req.getParameter("idCarrera");
        if (idCarreraStr == null || idCarreraStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/carrera/listar");
            return;
        }
        int idCarrera;
        try {
        	idCarrera = Integer.parseInt(idCarreraStr);
		} catch (NumberFormatException e) {
			resp.sendRedirect(req.getContextPath() + "/carrera/listar");
			return;
		}
        
        
        List<Curso> cursos = new ArrayList<>();
        String sql = """
            SELECT c.IdCurso, c.Nombre, c.IdCarrera, c.Activo,
                   c.dniDocente,
                   p.apellido AS apellidoDocente,
                   p.nombre AS nombreDocente
            FROM curso c
            LEFT JOIN persona p ON p.dni = c.dniDocente
            WHERE c.IdCarrera = ?
            ORDER BY c.Nombre
        """;
        // Se obtiene la lista de cursos para la carrera dada, incluyendo el nombre del docente asignado (si lo hay)
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCarrera);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Curso curso = new Curso();
                    curso.setIdCurso(rs.getInt("IdCurso"));
                    curso.setNombre(rs.getString("Nombre"));
                    curso.setIdCarrera(rs.getInt("IdCarrera"));
                    curso.setActivo(rs.getBoolean("Activo"));
                    curso.setDniDocente(rs.getInt("dniDocente"));
                    String ape = rs.getString("apellidoDocente");
                    String nom = rs.getString("nombreDocente");
                    if (ape != null && nom != null) {
                        curso.setNombreDocente(ape + ", " + nom);
                    } else {
                        curso.setNombreDocente("No asignado");
                    }
                    cursos.add(curso);
                }
            }
            if (cursos.isEmpty()) {
                req.setAttribute("errorDB", "No hay cursos para esta carrera");
            }
            req.setAttribute("cursos", cursos);
            req.setAttribute("idCarrera", idCarrera);
            req.getRequestDispatcher("/admin/carrera/curso/listar.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("cursos", Collections.emptyList());
            req.setAttribute("idCarrera", idCarrera);
            req.setAttribute("errorDB", "Error al acceder a la base de datos");
            req.getRequestDispatcher("/admin/carrera/curso/listar.jsp").forward(req, resp);
        }
    }
}