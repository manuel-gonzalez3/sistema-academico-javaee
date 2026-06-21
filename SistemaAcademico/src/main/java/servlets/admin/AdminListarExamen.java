package servlets.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import entidades.Examen;
import util.DatabaseConnection;

// Lista todos los exámenes del sistema con carrera, curso, comisión y docente. Solo ADMIN.
@WebServlet("/examen/listar")
public class AdminListarExamen extends HttpServlet {

    // Consulta todos los exámenes ordenados por carrera, curso y fecha descendente.
  @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!util.ServletUtils.checkRol(req, resp, "ADMIN")) return;

        // Obtengo todos los exámenes con carrera, curso, comisión y docente
        String sqlExamenes = """
                SELECT e.idExamen,
                       e.fecha,
                       e.hora,
                       ca.nombre AS nombreCarrera,
                       cu.nombre AS nombreCurso,
                       DENSE_RANK() OVER (PARTITION BY co.IdCurso ORDER BY co.IdComision) AS nroComision,
                       e.estado,
                       e.activo,
                       p.nombre   AS nombreDocente,
                       p.apellido AS apellidoDocente
                  FROM examen e
                  JOIN persona p  ON p.dni         = e.dniDocente
                  JOIN comision co ON co.idComision = e.idComision
                  JOIN curso    cu ON cu.idCurso    = co.idCurso
                  JOIN carrera  ca ON ca.idCarrera  = cu.idCarrera
                 ORDER BY ca.Nombre ASC,
                          cu.Nombre ASC,
                          nroComision ASC,
                          e.fecha DESC
                """;

        List<Examen> examenes = new ArrayList<>();

        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sqlExamenes);
             ResultSet rs = ps.executeQuery()) {

            DateTimeFormatter fmtFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter fmtHora  = DateTimeFormatter.ofPattern("HH:mm");
            while (rs.next()) {
                Examen e = new Examen();
                e.setIdExamen(rs.getInt("idExamen"));
                LocalDate fecha = rs.getDate("fecha").toLocalDate();
                LocalTime hora  = rs.getTime("hora").toLocalTime();
                e.setNombreCarrera(rs.getString("nombreCarrera"));
                e.setNombreCurso(rs.getString("nombreCurso"));
                e.setNroComision(rs.getString("nroComision"));
                e.setFecha(fecha.format(fmtFecha));
                e.setHora(hora.format(fmtHora));
                e.setEstado(rs.getString("estado"));
                e.setNombreDocente(rs.getString("apellidoDocente") + ", " + rs.getString("nombreDocente"));
                e.setActivo(rs.getInt("activo") == 1);
                examenes.add(e);
            }

            if (examenes.isEmpty()) {
                req.setAttribute("errorDB", "No hay exámenes registrados.");
            }

            req.setAttribute("examenes", examenes);
            req.getRequestDispatcher("/admin/examen/listar.jsp").forward(req, resp);

        } catch (Exception e) {
            req.setAttribute("examenes", Collections.emptyList());
            req.setAttribute("errorDB",  "Error al acceder a la base de datos.");
            req.getRequestDispatcher("/admin/examen/listar.jsp").forward(req, resp);
        }
    }
}
