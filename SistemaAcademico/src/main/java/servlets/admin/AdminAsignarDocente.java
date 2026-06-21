package servlets.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import entidades.Persona;
import util.DatabaseConnection;

// Asigna o quita un docente de un curso. Solo accesible para ADMIN.
@WebServlet("/curso/asignarDocente")
public class AdminAsignarDocente extends HttpServlet {

    // Muestra el formulario de asignación con el docente actual y la lista de docentes activos.
  @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!util.ServletUtils.checkRol(req, resp, "ADMIN")) return;

        String idCursoStr = req.getParameter("idCurso");
        if (idCursoStr == null || idCursoStr.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/carrera/listar");
            return;
        }

        int idCurso;
        try {
            idCurso = Integer.parseInt(idCursoStr);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/carrera/listar");
            return;
        }
        // Consulta para obtener el curso y su docente asignado para mostrar en el formulario.
        String cursoSql = """
            SELECT IdCurso, Nombre, IdCarrera, dniDocente
              FROM curso
             WHERE IdCurso = ?
            """;

        // Solo docentes activos (via tabla usuario)
        String docentesSql = """
            SELECT p.dni, p.nombre, p.apellido
              FROM persona p
              JOIN usuario u ON u.dni = p.dni AND u.tipoUsuario = 'DOCENTE'
             WHERE u.activo = 1
             ORDER BY p.apellido, p.nombre
            """;

        try (Connection cn = DatabaseConnection.getConnection()) {

            try (PreparedStatement ps = cn.prepareStatement(cursoSql)) {
                ps.setInt(1, idCurso);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        resp.sendRedirect(req.getContextPath() + "/carrera/listar");
                        return;
                    }
                    req.setAttribute("cursoNombre", rs.getString("Nombre"));
                    req.setAttribute("idCarrera", rs.getInt("IdCarrera"));
                    // El campo dniDocente puede ser NULL, por eso se maneja con cuidado.
                    String dniDocenteAsignado = rs.getString("dniDocente");
                    //atributo para marcar el radio del docente asignado, o null si no hay docente asignado
                    Integer docenteAsignado = (dniDocenteAsignado == null || dniDocenteAsignado.isBlank())
                        ? null
                        : Integer.valueOf(dniDocenteAsignado);
                    req.setAttribute("docenteAsignado", docenteAsignado);
                }
            }

            // Cargar lista de docentes activos como List<Persona>
            List<Persona> docentes = new ArrayList<>();
            try (PreparedStatement ps = cn.prepareStatement(docentesSql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Persona docente = new Persona();
                    docente.setDni(rs.getInt("dni"));
                    docente.setNombre(rs.getString("nombre"));
                    docente.setApellido(rs.getString("apellido"));
                    docentes.add(docente);
                }
            }

            req.setAttribute("idCurso", idCurso);
            req.setAttribute("docentes", docentes);
            req.getRequestDispatcher("/admin/carrera/curso/asignarDocente.jsp").forward(req, resp);

        } catch (Exception e) {
            req.setAttribute("error", "Error al cargar docentes.");
            req.getRequestDispatcher("/admin/carrera/curso/asignarDocente.jsp").forward(req, resp);
        }
    }

    // Actualiza el campo dniDocente del curso.
    // Si dniDocente llega vacío o ausente → SET NULL (quitar docente).
    // Si llega con valor → SET ese valor (asignar docente).
    // Un único UPDATE, sin distinción por accion.
  @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!util.ServletUtils.checkRol(req, resp, "ADMIN")) return;

        req.setCharacterEncoding("UTF-8");
        String idCursoStr   = req.getParameter("idCurso");
        String idCarreraStr = req.getParameter("idCarrera");
        String dniDocente   = req.getParameter("dniDocente");
        String accion       = req.getParameter("accion");

        int idCurso, idCarrera;
        try {
            idCurso   = Integer.parseInt(idCursoStr);
            idCarrera = Integer.parseInt(idCarreraStr);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/carrera/listar?error=Datos+inv%C3%A1lidos");
            return;
        }

        // El botón "Quitar docente" manda accion=quitar → SET NULL,
        // aunque el radio del docente actual siga marcado.
        boolean quitar = "quitar".equals(accion);
        // Si no se marca ningún docente o se marca el botón de quitar, se interpreta como sin docente.
        boolean sinDocente = quitar || dniDocente == null || dniDocente.isBlank(); 

        // Si se intenta asignar sin seleccionar un radio, se rechaza
        if (!quitar) {
            if (sinDocente) {
                resp.sendRedirect(req.getContextPath()
                    + "/curso/asignarDocente?idCurso=" + idCurso
                    + "&error=Debe+seleccionar+un+docente");
                return;
            }
            try { Integer.parseInt(dniDocente); }
            catch (NumberFormatException e) {
                resp.sendRedirect(req.getContextPath()
                    + "/curso/asignarDocente?idCurso=" + idCurso
                    + "&error=Debe+seleccionar+un+docente");
                return;
            }
        }

        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(
                 "UPDATE curso SET dniDocente = ? WHERE IdCurso = ?")) {
        	// Si sinDocente es true, se asigna NULL, de lo contrario se asigna el DNI del docente seleccionado.
            if (sinDocente) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, Integer.parseInt(dniDocente));
            }
            ps.setInt(2, idCurso);
            ps.executeUpdate();

            resp.sendRedirect(req.getContextPath()
                + "/curso/listar?idCarrera=" + idCarrera
                + "&ok=Docente+modificado+correctamente");

        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath()
                + "/curso/asignarDocente?idCurso=" + idCurso
                + "&error=Error+al+actualizar+docente");
        }
    }
}
