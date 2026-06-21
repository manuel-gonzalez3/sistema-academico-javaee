package servlets.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import util.DatabaseConnection;

// Crea un examen para una comisión. Solo accesible para ADMIN.
@WebServlet("/examen/crear")
public class AdminCrearExamen extends HttpServlet {

    // Muestra el formulario de alta de examen para la comisión recibida por parámetro.
  @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!util.ServletUtils.checkRol(req, resp, "ADMIN")) return;

        String idComisionStr = req.getParameter("idComision");
        if (idComisionStr == null || idComisionStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/carrera/listar");
            return;
        }
        int idComision;
        try {
            idComision = Integer.parseInt(idComisionStr);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/carrera/listar");
            return;
        }

        String idCursoStr = req.getParameter("idCurso");
        int idCurso = 0;
        try {
            idCurso = Integer.parseInt(idCursoStr);
        } catch (NumberFormatException e) {
            // si no viene o es inválido, el Cancelar volverá al listar de carreras
        }

        req.setAttribute("idComision", idComision);
        req.setAttribute("idCurso", idCurso);
        req.getRequestDispatcher("/admin/carrera/curso/comision/examen/crear.jsp").forward(req, resp);
    }

    // Valida fecha/hora, verifica que no haya otro examen el mismo día y persiste el examen.
  @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        if (!util.ServletUtils.checkRol(req, resp, "ADMIN")) return;

        // Validar idComision
        String idComisionStr = req.getParameter("idComision");
        if (idComisionStr == null || idComisionStr.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/examen/crear?error=Datos+invalidos");
            return;
        }
        int idComision;
        try {
            idComision = Integer.parseInt(idComisionStr);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/examen/crear?error=Datos+invalidos");
            return;
        }

        // Validar fecha y hora
        String fecha = req.getParameter("fecha");
        String hora  = req.getParameter("hora");
        if (fecha == null || fecha.isEmpty() || hora == null || hora.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/examen/crear?error=Fecha+y+hora+son+requeridos");
            return;
        }
        if (!fecha.matches("\\d{4}-\\d{2}-\\d{2}")) {
            resp.sendRedirect(req.getContextPath() + "/examen/crear?error=Formato+de+fecha+inv%C3%A1lido+(YYYY-MM-DD)");
            return;
        }
        if (!hora.matches("\\d{2}:\\d{2}")) {
            resp.sendRedirect(req.getContextPath() + "/examen/crear?error=Formato+de+hora+inv%C3%A1lido+(HH:MM)");
            return;
        }

        DateTimeFormatter fmtFecha  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate         fechaExamen = LocalDate.parse(fecha);
        LocalDate         fechaMinima = LocalDate.now().plusDays(2);

        if (fechaExamen.isBefore(fechaMinima)) {
            resp.sendRedirect(req.getContextPath() + "/examen/crear?error=Fecha+debe+ser+a+partir+del+"
                    + fechaMinima.format(fmtFecha));
            return;
        }

        String sqlValidarFecha = """
                SELECT COUNT(*) AS count
                FROM examen
                WHERE idComision = ?
                  AND fecha = ?
                LIMIT 1
                """;

        String sqlDocente = """
                SELECT c.dniDocente
                  FROM comision co
                  JOIN curso c ON c.IdCurso = co.IdCurso
                 WHERE co.IdComision = ?
                """;

        String sqlInsert = "INSERT INTO examen (fecha, hora, dniDocente, idComision) VALUES (?, ?, ?, ?)";

        try (Connection cn = DatabaseConnection.getConnection()) {

            // Verificar que no exista otro examen para la misma comisión en la misma fecha
            try (PreparedStatement ps = cn.prepareStatement(sqlValidarFecha)) {
                ps.setInt(1, idComision);
                ps.setString(2, fecha);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt("count") > 0) {
                        resp.sendRedirect(req.getContextPath() + "/examen/crear?error=Ya+existe+un+ex%C3%A1men+para+esa+fecha");
                        return;
                    }
                }
            }

            // Obtener docente de la comisión
            int dniDocente;
            try (PreparedStatement ps = cn.prepareStatement(sqlDocente)) {
                ps.setInt(1, idComision);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        resp.sendRedirect(req.getContextPath() + "/examen/crear?idComision=" + idComision + "&error=No+se+encontr%C3%B3+la+comisi%C3%B3n");
                        return;
                    }
                    dniDocente = rs.getInt("dniDocente");
                    // dniDocente NULL en la BDD: getInt devuelve 0; sin este chequeo se insertaría
                    if (rs.wasNull()) {
                        resp.sendRedirect(req.getContextPath() + "/examen/crear?idComision=" + idComision + "&error=El+curso+no+tiene+docente+asignado.+Asign%C3%A1+un+docente+antes+de+crear+el+examen");
                        return;
                    }
                }
            }

            // Insertar examen
            try (PreparedStatement ps = cn.prepareStatement(sqlInsert)) {
                ps.setString(1, fecha);
                ps.setString(2, hora);
                ps.setInt(3, dniDocente);
                ps.setInt(4, idComision);
                ps.executeUpdate();
            }

            resp.sendRedirect(req.getContextPath() + "/examen/listar?ok=Ex%C3%A1men+creado+correctamente");

        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/examen/crear?error=Error+al+crear+el+ex%C3%A1men");
        }
    }
}
