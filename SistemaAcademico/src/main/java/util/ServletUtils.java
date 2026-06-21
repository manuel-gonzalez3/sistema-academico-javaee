package util;

import entidades.Carrera;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Utilidades comunes para los servlets del proyecto.
 * Centraliza validacion de sesion y logica academica compartida entre servlets.
 */
public class ServletUtils {


    private ServletUtils() {}

    /**
     * Verifica que el usuario en sesion tenga el rol esperado.
     * Si no lo tiene (o no hay sesion), redirige a login.jsp y retorna false.
     */
    public static boolean checkRol(HttpServletRequest req,
                                   HttpServletResponse resp,
                                   String rolEsperado) throws IOException {
        Object rol = req.getSession().getAttribute("userRol");
        if (rol == null || !rolEsperado.equals(String.valueOf(rol))) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return false;
        }
        return true;
    }

    /**
     * Obtiene el DNI del usuario en sesion.
     * Si la sesion no tiene DNI, redirige a login.jsp y retorna null.
     */
    public static String getDniSesion(HttpServletRequest req,
                                      HttpServletResponse resp) throws IOException {
        String dni = (String) req.getSession().getAttribute("userDni");
        if (dni == null || dni.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return null;
        }
        return dni;
    }

    /**
     * Busca la carrera activa en la que el alumno esta inscripto.
     * Retorna null si el alumno no tiene ninguna carrera con estado INSCRIPTO.
     */
    public static Carrera getCarreraInscripta(Connection cn, String dni) throws SQLException {
        String sql = "SELECT ac.IdCarrera, c.Nombre AS nombreCarrera"
                + " FROM alumno_carrera ac"
                + " JOIN carrera c ON c.IdCarrera = ac.IdCarrera"
                + " WHERE ac.dni = ? AND UPPER(ac.estado) = 'INSCRIPTO'"
                + " LIMIT 1";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Carrera carrera = new Carrera();
                carrera.setIdCarrera(rs.getInt("IdCarrera"));
                carrera.setNombre(rs.getString("nombreCarrera"));
                return carrera;
            }
        }
    }

    /**
     * Verifica si un alumno aprobo todos los cursos activos de una carrera
     * y, de ser asi, actualiza su estado en alumno_carrera a APROBADO.
     */
    public static void verificarYAprobarCarrera(Connection cn, int dni, int idCarrera) throws Exception {
    	// Consulta para contar el total de cursos activos en la carrera y los cursos aprobados por el alumno
    	//count distinct para evitar contar varias veces el mismo curso si el alumno se inscribió a varias comisiones del mismo curso
    	// Solo se cuentan como aprobados los cursos en los que el alumno tiene estado 'aprobado' o 'promocionado'
    	
        String sql = "SELECT"
                + " (SELECT COUNT(DISTINCT IdCurso) FROM curso WHERE IdCarrera = ? AND Activo = 1) AS totalCursos,"
                + " COUNT(DISTINCT cu.IdCurso) AS cursosAprobados"
                + " FROM curso cu"
                + " JOIN comision co ON co.IdCurso = cu.IdCurso"
                + " JOIN comision_alumno ca ON ca.IdComision = co.IdComision"
                + " WHERE cu.IdCarrera = ? AND ca.dni = ?"
                + " AND ca.estado IN ('aprobado', 'promocionado')";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCarrera);
            ps.setInt(2, idCarrera);
            ps.setInt(3, dni);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total     = rs.getInt("totalCursos");
                    int aprobados = rs.getInt("cursosAprobados");
                    // Si el alumno aprobó todos los cursos activos de la carrera, se actualiza su estado a APROBADO
                    if (total > 0 && total == aprobados) {
                        String upd = "UPDATE alumno_carrera SET estado = 'APROBADO' WHERE dni = ? AND IdCarrera = ?";
                        try (PreparedStatement psUp = cn.prepareStatement(upd)) {
                            psUp.setInt(1, dni);
                            psUp.setInt(2, idCarrera);
                            psUp.executeUpdate();
                        }
                    }
                }
            }
        }
    }
}
