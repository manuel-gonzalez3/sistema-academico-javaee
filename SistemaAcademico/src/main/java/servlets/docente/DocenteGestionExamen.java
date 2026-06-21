package servlets.docente;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.DatabaseConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Gestión de exámenes del docente: carga de asistencia y notas.
// Flujo: asistencia → examen pasa a 'rendido' | nota → examen pasa a 'cerrado' y se actualizan comision_alumno.
// Solo accesible para DOCENTE.
@WebServlet("/docente/examen/gestion")
public class DocenteGestionExamen extends HttpServlet {

    // Muestra el formulario de carga según la acción recibida:
    // - 'asistencia' o 'resultado': muestra TODOS los alumnos del examen
    // - 'nota': muestra solo los alumnos con asistencia 'presente' (los únicos que pueden recibir nota)
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!util.ServletUtils.checkRol(req, resp, "DOCENTE")) return;

        // Valido que idExamen sea un entero válido
        String idExamenStr = req.getParameter("idExamen");
        if (idExamenStr == null || idExamenStr.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/docente/examen/listar");
            return;
        }
        int idExamen;
        try {
            idExamen = Integer.parseInt(idExamenStr);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/docente/examen/listar?error=Examen+Invalido");
            return;
        }

        String accion = req.getParameter("accion");
        String sql;

        if ("asistencia".equalsIgnoreCase(accion) || "resultado".equalsIgnoreCase(accion)) {
            // Para cargar asistencia (o ver resultado): traigo todos los alumnos del examen
            sql = """
                SELECT ea.dniAlumno AS dni,
                       ea.asistencia,
                       ea.nota,
                       p.nombre,
                       p.apellido
                  FROM examen_alumno ea
                  JOIN persona p ON p.dni = ea.dniAlumno
                 WHERE ea.idExamen = ?
                 ORDER BY p.apellido, p.nombre
                """;
        } else if ("nota".equalsIgnoreCase(accion)) {
            // Para cargar notas: solo los alumnos que estuvieron presentes
            // (los ausentes no rinden, no tienen nota)
            sql = """
                SELECT ea.dniAlumno AS dni,
                       ea.asistencia,
                       ea.nota,
                       p.nombre,
                       p.apellido
                  FROM examen_alumno ea
                  JOIN persona p ON p.dni = ea.dniAlumno
                 WHERE ea.idExamen = ?
                   AND ea.asistencia = 'presente'
                 ORDER BY p.apellido, p.nombre
                """;
        } else {
            resp.sendRedirect(req.getContextPath() + "/docente/examen/listar?error=Accion+Invalido");
            return;
        }

        try (Connection cn = DatabaseConnection.getConnection()) {

            List<Map<String, Object>> alumnos = new ArrayList<>();
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setInt(1, idExamen);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> alumno = new HashMap<>();
                        alumno.put("dni",        rs.getInt("dni"));
                        alumno.put("nombre",     rs.getString("nombre"));
                        alumno.put("apellido",   rs.getString("apellido"));
                        alumno.put("asistencia", rs.getString("asistencia"));
                        // Si nota es 0 significa que no fue cargada aún → mostrar "-"
                        alumno.put("nota",       rs.getInt("nota") == 0 ? "-" : rs.getInt("nota"));
                        alumnos.add(alumno);
                    }
                }
            }

            if (alumnos.isEmpty()) {
                req.setAttribute("errorDB", "No hay alumnos inscriptos en este examen.");
            }
            req.setAttribute("alumnos",  alumnos);
            req.setAttribute("idExamen", idExamen);
            req.getRequestDispatcher("/docente/examen/gestion.jsp").forward(req, resp);

        } catch (Exception e) {
            req.setAttribute("alumnos",  List.of());
            req.setAttribute("idExamen", idExamen);
            req.setAttribute("errorDB",  "Error al acceder a la base de datos");
            req.getRequestDispatcher("/docente/examen/gestion.jsp").forward(req, resp);
        }
    }

    // Procesa el envío del formulario de asistencia o notas.
    // - 'asistencia': guarda presente/ausente por alumno y pasa el examen a 'rendido'
    // - 'nota': guarda la nota (1-10) de los presentes, pasa el examen a 'cerrado',
    //           actualiza comision_alumno y verifica si algún alumno completó la carrera
    // Todo se ejecuta en una transacción: si algo falla, se hace rollback completo.
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        if (!util.ServletUtils.checkRol(req, resp, "DOCENTE")) return;

        // Valido parámetros básicos
        String idExamenStr = req.getParameter("idExamen");
        String accion      = req.getParameter("accion");
        if (idExamenStr == null || idExamenStr.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/docente/examen/listar?error=Parametros+invalidos");
            return;
        }
        int idExamen;
        try {
            idExamen = Integer.parseInt(idExamenStr);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/docente/examen/listar?error=Parametros+invalidos");
            return;
        }

        // Defino las queries según la acción:
        // - sqlDnis: qué alumnos debo actualizar (todos para asistencia, solo presentes para nota)
        // - sqlExamen: qué estado nuevo toma el examen al confirmar
        String sqlDnis;
        String sqlExamen;
        if ("asistencia".equalsIgnoreCase(accion)) {
            sqlDnis   = "SELECT dniAlumno FROM examen_alumno WHERE idExamen = ?";
            sqlExamen = "UPDATE examen SET estado = 'rendido' WHERE idExamen = ?";

            // Solo permito cargar asistencia si el examen ya ocurrió (fecha+hora en el pasado).
            // Evita que se cargue asistencia antes de que el examen se realice.
            try (Connection cnCheck = DatabaseConnection.getConnection()) {
                String sqlFecha = "SELECT fecha, hora FROM examen WHERE idExamen = ?";
                try (PreparedStatement ps = cnCheck.prepareStatement(sqlFecha)) {
                    ps.setInt(1, idExamen);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            LocalDate fechaExamen = rs.getDate("fecha").toLocalDate();
                            LocalTime horaExamen  = rs.getTime("hora").toLocalTime();
                            if (LocalDateTime.now().isBefore(LocalDateTime.of(fechaExamen, horaExamen))) {
                                resp.sendRedirect(req.getContextPath() + "/docente/examen/listar?error=No+se+puede+cargar+asistencia+antes+de+que+ocurra+el+examen");
                                return;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                resp.sendRedirect(req.getContextPath() + "/docente/examen/listar?error=Error+al+validar+la+fecha+del+examen");
                return;
            }

        } else if ("nota".equalsIgnoreCase(accion)) {
            // Para notas solo proceso los presentes: los ausentes no tienen nota
            sqlDnis   = "SELECT dniAlumno FROM examen_alumno WHERE idExamen = ? AND asistencia = 'presente'";
            sqlExamen = "UPDATE examen SET estado = 'cerrado' WHERE idExamen = ?";
        } else {
            resp.sendRedirect(req.getContextPath() + "/docente/examen/listar?error=Accion+invalida");
            return;
        }

        // UPDATE genérico que reutilizo para asistencia y nota según el valor de 'accion', toman ausente/presente o la nota respectivamente
        String sqlExamenAlumno = "UPDATE examen_alumno SET " + accion + " = ? WHERE idExamen = ? AND dniAlumno = ?";

        try (Connection cn = DatabaseConnection.getConnection()) {

            // Uso transacción para que asistencias/notas y el cambio de estado del examen
            // sean atómicos: o se guardan todos o no se guarda ninguno
            cn.setAutoCommit(false);

            // Obtengo los DNIs de los alumnos que debo actualizar
            List<Integer> dnis = new ArrayList<>();
            try (PreparedStatement ps = cn.prepareStatement(sqlDnis)) {
                ps.setInt(1, idExamen);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) dnis.add(rs.getInt("dniAlumno"));
                }
            }

            // Si no hay alumnos, aborto (no tiene sentido confirmar un examen vacío)
            if (dnis.isEmpty()) {
                cn.rollback();
                resp.sendRedirect(req.getContextPath() + "/docente/examen/gestion?idExamen=" + idExamen + "&accion=" + accion + "&error=No+hay+alumnos+inscriptos");
                return;
            }

            // Lista de pares [dni, nota] de alumnos que aprobaron (nota >= 6).
            // Se usa luego para actualizar comision_alumno y verificar aprobación de carrera.
            List<List<Integer>> alumnosAprobados = new ArrayList<>();

            try (PreparedStatement ps = cn.prepareStatement(sqlExamenAlumno)) {
                for (Integer dni : dnis) {
                    String valor = req.getParameter("dato_" + dni);

                    // Validación del valor recibido para cada alumno:
                    // - no puede ser nulo o vacío
                    // - si es asistencia: debe ser 'presente' o 'ausente'
                    // - si es nota: debe estar entre 1 y 10
                    if (valor == null || valor.isBlank()
                            || ("asistencia".equalsIgnoreCase(accion) && (!"presente".equalsIgnoreCase(valor) && !"ausente".equalsIgnoreCase(valor)))
                            || ("nota".equalsIgnoreCase(accion) && (Integer.parseInt(valor) < 1 || Integer.parseInt(valor) > 10))) {
                        cn.rollback();
                        resp.sendRedirect(req.getContextPath() + "/docente/examen/gestion?idExamen=" + idExamen + "&accion=" + accion + "&error=" + accion + "+invalida+para+el+alumno+con+dni+" + dni);
                        return;
                    }

                    if ("asistencia".equalsIgnoreCase(accion)) {
                        ps.setString(1, valor.toLowerCase()); // "presente" o "ausente"
                    } else {
                        int nota = Integer.parseInt(valor);
                        ps.setInt(1, nota);
                        // Acumulo los aprobados (nota >= 6) para actualizar comision_alumno después
                        if (nota >= 6) {
                            List<Integer> dniNota = new ArrayList<>();
                            dniNota.add(dni);  // índice 0: DNI
                            dniNota.add(nota); // índice 1: nota
                            alumnosAprobados.add(dniNota);
                        }
                    }
                    //hago batch de los updates de examen_alumno para que se ejecuten todos juntos al final
                    ps.setInt(2, idExamen);
                    ps.setInt(3, dni);
                    ps.addBatch();
                }
                // Ejecuto todos los UPDATEs de examen_alumno en un solo batch
                ps.executeBatch();
            }

            // Si hubo aprobados, actualizo comision_alumno y verifico aprobación de carrera
            if ("nota".equalsIgnoreCase(accion) && !alumnosAprobados.isEmpty()) {
                aprobarAlumnos(cn, idExamen, alumnosAprobados);
            }

            // Cambio el estado del examen ('rendido' o 'cerrado' según la acción)
            try (PreparedStatement psExamen = cn.prepareStatement(sqlExamen)) {
                psExamen.setInt(1, idExamen);
                psExamen.executeUpdate();
            }

            cn.commit();

        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/docente/examen/gestion?idExamen=" + idExamen + "&accion=" + accion + "&error=Error+al+guardar");
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/docente/examen/listar?idExamen=" + idExamen + "&ok=Guardado+correctamente");
    }

    // Marca como 'aprobado' en comision_alumno a los alumnos que sacaron nota >= 6,
    // y verifica si con este aprobado completaron todos los cursos de la carrera.
    // Se ejecuta dentro de la transacción del doPost, por eso recibe la Connection abierta.
    private void aprobarAlumnos(Connection cn, int idExamen, List<List<Integer>> alumnosAprobados) throws Exception {

        // Actualizo comision_alumno: estado = 'aprobado' y guardo la nota final
        String sql = """
            UPDATE comision_alumno ca
            JOIN examen e ON e.idComision = ca.IdComision
            SET ca.estado = 'aprobado', ca.nota = ?
            WHERE e.idExamen = ? AND ca.dni = ?
            """;
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (List<Integer> alumno : alumnosAprobados) {
                int dni  = alumno.get(0);
                int nota = alumno.get(1);
                ps.setInt(1, nota);
                ps.setInt(2, idExamen);
                ps.setInt(3, dni);
                ps.addBatch();
            }
            ps.executeBatch();
        }

        // Necesito la carrera del examen para saber qué carrera verificar por alumno.
        // Navego: examen → comision → curso → carrera
        int idCarrera;
        String sqlCarrera = """
            SELECT cu.IdCarrera
              FROM examen e
              JOIN comision co ON co.IdComision = e.idComision
              JOIN curso cu    ON cu.IdCurso    = co.IdCurso
             WHERE e.idExamen = ?
            """;
        try (PreparedStatement ps = cn.prepareStatement(sqlCarrera)) {
            ps.setInt(1, idExamen);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return; // examen sin carrera asociada, no debería ocurrir
                idCarrera = rs.getInt("IdCarrera");
            }
        }

        // Por cada alumno aprobado, verifico si completó todos los cursos de la carrera.
        // Si es así, ServletUtils lo marca como 'aprobaso' en alumno_carrera.
        for (List<Integer> alumno : alumnosAprobados) {
            util.ServletUtils.verificarYAprobarCarrera(cn, alumno.get(0), idCarrera);
        }
    }
}
