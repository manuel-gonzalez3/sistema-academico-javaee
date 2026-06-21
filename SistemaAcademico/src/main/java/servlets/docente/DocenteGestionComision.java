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
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.DatabaseConnection;

// Gestión de comisión del docente: ver alumnos inscriptos y cerrar el cursado.
// Flujo: el docente asigna a cada alumno un estado final (promocionado/regular/libre)
// y, si corresponde, una nota. Al confirmar, se actualiza comision_alumno y
// se verifica si algún promocionado completó toda la carrera.
// Solo accesible para DOCENTE y solo sobre comisiones que le pertenecen.
@WebServlet("/docente/curso/comision/alumnos")
public class DocenteGestionComision extends HttpServlet {

    // Muestra los alumnos de la comisión con sus estados y notas actuales.
    // Antes de mostrar, valida que la comisión pertenezca a un curso del docente logueado,
    // para evitar que un docente acceda a comisiones de otro.
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        if (!util.ServletUtils.checkRol(req, resp, "DOCENTE")) return;

        // Valido que idComision sea un entero válido
        Integer idComision;
        try {
            idComision = Integer.parseInt(req.getParameter("idComision"));
        } catch (NumberFormatException | NullPointerException e) {
            resp.sendRedirect(req.getContextPath() + "/docente/curso/listar?error=Comisi%C3%B3n+inv%C3%A1lida");
            return;
        }

        String dniDocente = util.ServletUtils.getDniSesion(req, resp);
        if (dniDocente == null) return;

        // Verifico que la comisión exista y que su curso pertenezca al docente logueado.
        // También traigo el nombre del curso, carrera y número de comisión para el encabezado.
        String sqlValidacion = """
            SELECT c.Nombre AS curso,
                   ca.Nombre AS carrera,
                   c.IdCurso,
                   co.IdComision,
                   DENSE_RANK() OVER (PARTITION BY c.IdCurso ORDER BY co.IdComision) AS nroComision
              FROM comision co
              JOIN curso c ON c.IdCurso = co.IdCurso
              JOIN carrera ca ON ca.IdCarrera = c.IdCarrera
             WHERE co.IdComision = ?
               AND c.dniDocente = ?
            """;

        // Traigo todos los alumnos inscriptos en la comisión, ordenados alfabéticamente
        String sqlAlumnos = """
            SELECT ca.dni,
                   p.nombre,
                   p.apellido,
                   ca.estado,
                   ca.nota
              FROM comision_alumno ca
              JOIN persona p ON p.dni = ca.dni
             WHERE ca.IdComision = ?
             ORDER BY p.apellido, p.nombre
            """;

        try (Connection cn = DatabaseConnection.getConnection()) {

            // Si la query no devuelve nada, el docente no tiene permiso sobre esta comisión
            try (PreparedStatement ps = cn.prepareStatement(sqlValidacion)) {
                ps.setInt(1, idComision);
                ps.setString(2, dniDocente);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        resp.sendRedirect(req.getContextPath() + "/docente/curso/listar?error=No+ten%C3%A9s+permiso+sobre+esa+comisi%C3%B3n");
                        return;
                    }
                    req.setAttribute("curso",      rs.getString("curso"));
                    req.setAttribute("carrera",    rs.getString("carrera"));
                    req.setAttribute("idCurso",    rs.getInt("IdCurso"));
                    req.setAttribute("idComision", rs.getInt("IdComision"));
                    req.setAttribute("nroComision",rs.getInt("nroComision"));
                }
            }

            List<Map<String, Object>> alumnos = new ArrayList<>();
            try (PreparedStatement ps = cn.prepareStatement(sqlAlumnos)) {
                ps.setInt(1, idComision);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> alumno = new HashMap<>();
                        alumno.put("dni",      rs.getInt("dni"));
                        alumno.put("nombre",   rs.getString("nombre"));
                        alumno.put("apellido", rs.getString("apellido"));
                        alumno.put("estado",   rs.getString("estado"));
                        // getObject en vez de getInt para que note pueda ser null (aún no asignada)
                        alumno.put("nota",     rs.getObject("nota"));
                        alumnos.add(alumno);
                    }
                }
            }

            req.setAttribute("alumnos", alumnos);
            req.setAttribute("ok",    req.getParameter("ok"));
            req.setAttribute("error", req.getParameter("error"));
            if (alumnos.isEmpty()) {
                req.setAttribute("errorDB", "No hay alumnos inscriptos en esta comisión.");
            }

            req.getRequestDispatcher("/docente/curso/comision/alumnos.jsp").forward(req, resp);

        } catch (Exception e) {
            req.setAttribute("alumnos", List.of());
            req.setAttribute("errorDB", "Error al acceder a la base de datos");
            req.getRequestDispatcher("/docente/curso/comision/alumnos.jsp").forward(req, resp);
        }
    }

    // Cierra el cursado de la comisión: asigna estado final y nota a cada alumno.
    // Estados válidos: 'promocionado' (nota 8-10), 'regular' (sin nota), 'libre' (sin nota).
    // Solo los promocionados pueden activar la verificación de graduación de carrera.
    // Todo se ejecuta en una transacción: si algún dato es inválido, se hace rollback completo.
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

        if (!util.ServletUtils.checkRol(req, resp, "DOCENTE")) return;

        // Valido que idComision sea un entero válido
        Integer idComision;
        try {
            idComision = Integer.parseInt(req.getParameter("idComision"));
        } catch (NumberFormatException | NullPointerException e) {
            resp.sendRedirect(req.getContextPath() + "/docente/curso/listar?error=Comisi%C3%B3n+inv%C3%A1lida");
            return;
        }

        String dniDocente = util.ServletUtils.getDniSesion(req, resp);
        if (dniDocente == null) return;

        // Segunda validación de permiso en el POST: evita que alguien falsifique el parámetro idComision
        String sqlComisionDocente = """
            SELECT 1
              FROM comision co
              JOIN curso c ON c.IdCurso = co.IdCurso
             WHERE co.IdComision = ?
               AND c.dniDocente = ?
            """;

        // Obtengo los DNIs de todos los alumnos de la comisión para procesarlos uno a uno
        String sqlDnis   = "SELECT dni FROM comision_alumno WHERE IdComision = ?";
        // UPDATE genérico: actualiza estado y nota (puede ser null para regular/libre)
        String sqlUpdate = "UPDATE comision_alumno SET estado = ?, nota = ? WHERE IdComision = ? AND dni = ?";

        try (Connection cn = DatabaseConnection.getConnection()) {

            // Uso transacción: o se cierran todos los alumnos correctamente o no se guarda nada
            cn.setAutoCommit(false);

            // Verifico permiso del docente sobre esta comisión
            try (PreparedStatement ps = cn.prepareStatement(sqlComisionDocente)) {
                ps.setInt(1, idComision);
                ps.setString(2, dniDocente);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        cn.rollback();
                        resp.sendRedirect(req.getContextPath() + "/docente/curso/listar?error=No+ten%C3%A9s+permiso+para+cerrar+esa+comisi%C3%B3n");
                        return;
                    }
                }
            }

            // Obtengo los DNIs de los alumnos a actualizar
            List<Integer> dnis = new ArrayList<>();
            try (PreparedStatement ps = cn.prepareStatement(sqlDnis)) {
                ps.setInt(1, idComision);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) dnis.add(rs.getInt("dni"));
                }
            }

            // Si no hay alumnos, no tiene sentido cerrar el cursado
            if (dnis.isEmpty()) {
                cn.rollback();
                resp.sendRedirect(req.getContextPath() + "/docente/curso/comision/alumnos?idComision=" + idComision + "&error=No+hay+alumnos+para+cerrar+el+cursado");
                return;
            }

            // Acumulo los DNIs de los promocionados para verificar graduación después del batch
            List<Integer> promocionados = new ArrayList<>();

            try (PreparedStatement psUpdate = cn.prepareStatement(sqlUpdate)) {
                for (Integer dniAlumno : dnis) {
                    String estado = req.getParameter("estado_" + dniAlumno);

                    // Estado obligatorio y debe ser uno de los tres valores válidos
                    if (estado == null || estado.isBlank()
                        || (!"promocionado".equalsIgnoreCase(estado)
                            && !"regular".equalsIgnoreCase(estado)
                            && !"libre".equalsIgnoreCase(estado))) {
                        cn.rollback();
                        resp.sendRedirect(req.getContextPath() + "/docente/curso/comision/alumnos?idComision=" + idComision + "&error=Condici%C3%B3n+inv%C3%A1lida+para+el+alumno+con+dni+" + dniAlumno);
                        return;
                    }

                    // Solo los promocionados llevan nota; regular y libre quedan con nota null
                    Integer nota = null;
                    if ("promocionado".equalsIgnoreCase(estado)) {
                        try {
                            nota = Integer.parseInt(req.getParameter("nota_" + dniAlumno));
                        } catch (NumberFormatException | NullPointerException ex) {
                            nota = null;
                        }
                        // La nota de promoción debe estar entre 8 y 10
                        if (nota == null || nota < 8 || nota > 10) {
                            cn.rollback();
                            resp.sendRedirect(req.getContextPath() + "/docente/curso/comision/alumnos?idComision=" + idComision + "&error=El+alumno+con+dni+" + dniAlumno + "+promocionado+debe+tener+nota+entre+8+y+10");
                            return;
                        }
                        promocionados.add(dniAlumno);
                    }

                    psUpdate.setString(1, estado.toLowerCase());
                    if (nota == null) {
                        psUpdate.setNull(2, Types.INTEGER); // regular y libre no tienen nota
                    } else {
                        psUpdate.setInt(2, nota);
                    }
                    psUpdate.setInt(3, idComision);
                    psUpdate.setInt(4, dniAlumno);
                    psUpdate.addBatch();
                }
                // Ejecuto todos los UPDATEs de comision_alumno en un solo batch
                psUpdate.executeBatch();
            }

            // Para los promocionados, verifico si completaron todos los cursos de la carrera.
            // Si es así, ServletUtils los marca como 'graduados' en alumno_carrera.
            if (!promocionados.isEmpty()) {
                // Necesito la carrera de la comisión: navego comision → curso → carrera
                int idCarrera;
                String sqlCarrera = """
                    SELECT cu.IdCarrera
                      FROM comision co
                      JOIN curso cu ON cu.IdCurso = co.IdCurso
                     WHERE co.IdComision = ?
                    """;
                try (PreparedStatement ps = cn.prepareStatement(sqlCarrera)) {
                    ps.setInt(1, idComision);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            cn.rollback();
                            resp.sendRedirect(req.getContextPath() + "/docente/curso/listar?error=No+se+pudo+determinar+la+carrera");
                            return;
                        }
                        idCarrera = rs.getInt("IdCarrera");
                    }
                }
                for (Integer dniAlumno : promocionados) {
                    util.ServletUtils.verificarYAprobarCarrera(cn, dniAlumno, idCarrera);
                }
            }

            cn.commit();
            resp.sendRedirect(req.getContextPath() + "/docente/curso/listar?ok=Cursado+cerrado+correctamente");

        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/docente/curso/comision/alumnos?idComision=" + idComision + "&error=No+se+pudo+cerrar+el+cursado");
        }
    }
}
