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
import java.util.List;

import entidades.Curso;
import util.DatabaseConnection;

// Gestiona la inscripción del alumno a un curso. Solo accesible para ALUMNO.
// Asigna automáticamente la comisión con cupo disponible.
@WebServlet("/alumno/cursos")
public class AlumnoInscribirCurso extends HttpServlet {

  // Lista los cursos activos de la carrera del alumno que no está cursando ni aprobó.
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    if (!util.ServletUtils.checkRol(req, resp, "ALUMNO")) return;

    String dni = util.ServletUtils.getDniSesion(req, resp);
    if (dni == null) return;

    List<Curso> cursos = new ArrayList<>();


    // Obtener los cursos activos de la carrera que el alumno no haya cursado ni esté cursando
    String sqlCursosPendientes = """
        SELECT cu.IdCurso,
               cu.Nombre,
               cu.Activo,
               p.apellido AS apellidoDocente,
               p.nombre AS nombreDocente
          FROM curso cu
          LEFT JOIN persona p ON p.dni = cu.dniDocente
         WHERE cu.IdCarrera = ?
           AND cu.Activo = 1
           AND NOT EXISTS (
                 SELECT 1
                   FROM comision co
                   JOIN comision_alumno ca ON ca.IdComision = co.IdComision
                  WHERE co.IdCurso = cu.IdCurso
                    AND ca.dni = ?
             )
         ORDER BY cu.Nombre
        """;

    try (Connection cn = DatabaseConnection.getConnection()) {

      entidades.Carrera carreraAlumno = util.ServletUtils.getCarreraInscripta(cn, dni);
      if (carreraAlumno == null) {
        req.setAttribute("error", "No tenés una carrera inscripta.");
        req.getRequestDispatcher("/alumno/home.jsp").forward(req, resp);
        return;
      }
      int idCarrera = carreraAlumno.getIdCarrera();
      req.setAttribute("nombreCarrera", carreraAlumno.getNombre());

        try (PreparedStatement psCursos = cn.prepareStatement(sqlCursosPendientes)) {
          psCursos.setInt(1, idCarrera);
          psCursos.setInt(2, Integer.parseInt(dni));

          try (ResultSet rsCursos = psCursos.executeQuery()) {
            while (rsCursos.next()) {
              Curso curso = new Curso();
              curso.setIdCurso(rsCursos.getInt("IdCurso"));
              curso.setNombre(rsCursos.getString("Nombre"));
              curso.setActivo(rsCursos.getBoolean("Activo"));

              String apellidoDocente = rsCursos.getString("apellidoDocente");
              String nombreDocente   = rsCursos.getString("nombreDocente");
              if (apellidoDocente != null && nombreDocente != null) {
                curso.setNombreDocente(apellidoDocente + ", " + nombreDocente);
              } else {
                curso.setNombreDocente("Sin docente asignado");
              }

              cursos.add(curso);
            }
          }
        }

      req.setAttribute("cursos", cursos);
      if (cursos.isEmpty()) {
        req.setAttribute("errorDB", "No hay cursos pendientes para mostrar.");
      }

      if (req.getParameter("ok") != null) {
        req.setAttribute("ok", req.getParameter("ok"));
      }
      if (req.getParameter("error") != null) {
        req.setAttribute("error", req.getParameter("error"));
      }

      req.getRequestDispatcher("/alumno/cursos.jsp").forward(req, resp);

    } catch (Exception e) {
      req.setAttribute("cursos", List.of());
      req.setAttribute("errorDB", "Error al acceder a la base de datos");
      req.getRequestDispatcher("/alumno/cursos.jsp").forward(req, resp);
    }
  }

  // Valida carrera, curso y cupo; inscribe al alumno en la primera comisión disponible.
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    if (!util.ServletUtils.checkRol(req, resp, "ALUMNO")) return;

    String dni = (String) req.getSession().getAttribute("userDni");
    String idCursoParam = req.getParameter("idCurso");
    if (dni == null || dni.isBlank() || idCursoParam == null || idCursoParam.isBlank()) {
      resp.sendRedirect(req.getContextPath() + "/alumno/cursos?error=Solicitud+inv%C3%A1lida");
      return;
    }

    int idCurso;
    try {
      idCurso = Integer.parseInt(idCursoParam);
    } catch (NumberFormatException e) {
      resp.sendRedirect(req.getContextPath() + "/alumno/cursos?error=Curso+inv%C3%A1lido");
      return;
    }


    // Verificar que el curso exista, esté activo y pertenezca a la carrera del alumno
    String sqlCurso = """
        SELECT IdCurso
          FROM curso
         WHERE IdCurso = ?
           AND IdCarrera = ?
           AND Activo = 1
        """;

    // Verificar que el alumno no esté ya inscripto en el curso (en ninguna comisión)
    String sqlExiste = """
        SELECT 1
          FROM comision co
          JOIN comision_alumno ca ON ca.IdComision = co.IdComision
         WHERE co.IdCurso = ?
           AND ca.dni = ?
         LIMIT 1
        """;

    // Obtener las comisiones del curso con su cantidad de alumnos y cupo, row number me dice 1 o 2 si es la primera o segunda comision del curso
    String sqlComisiones = """
        SELECT co.IdComision,
               co.CantAlumnos,
               COUNT(ca.dni) AS ocupados,
               ROW_NUMBER() OVER (PARTITION BY co.IdCurso ORDER BY co.IdComision) AS nroComision 
          FROM comision co
          LEFT JOIN comision_alumno ca ON ca.IdComision = co.IdComision
         WHERE co.IdCurso = ?
           AND co.Activo = 1
         GROUP BY co.IdComision, co.CantAlumnos
         ORDER BY co.IdComision
        """;

    // Insertar al alumno en la comisión disponible
    String sqlInsert = """
        INSERT INTO comision_alumno (IdComision, dni, estado)
        VALUES (?, ?, 'cursando')
        """;

    try (Connection cn = DatabaseConnection.getConnection()) {

      entidades.Carrera carreraAlumno = util.ServletUtils.getCarreraInscripta(cn, dni);
      if (carreraAlumno == null) {
        req.setAttribute("error", "No tenés una carrera inscripta.");
        req.getRequestDispatcher("/alumno/home.jsp").forward(req, resp);
        return;
      }
      int idCarrera = carreraAlumno.getIdCarrera();

      try (PreparedStatement ps = cn.prepareStatement(sqlCurso)) {
        ps.setInt(1, idCurso);
        ps.setInt(2, idCarrera);
        try (ResultSet rs = ps.executeQuery()) {
          if (!rs.next()) {
            resp.sendRedirect(req.getContextPath() + "/alumno/cursos?error=El+curso+no+pertenece+a+tu+carrera");
            return;
          }
        }
      }

      try (PreparedStatement ps = cn.prepareStatement(sqlExiste)) {
        ps.setInt(1, idCurso);
        ps.setInt(2, Integer.parseInt(dni));
        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            resp.sendRedirect(req.getContextPath() + "/alumno/cursos?error=Ya+est%C3%A1s+inscripto+en+este+curso");
            return;
          }
        }
      }

      // Buscar una comisión con cupo disponible
      Integer idComisionDisponible = null;
      int nroComisionDisponible = 0;
      try (PreparedStatement ps = cn.prepareStatement(sqlComisiones)) {
        ps.setInt(1, idCurso);
        try (ResultSet rs = ps.executeQuery()) {
          int contador = 0;
          while (rs.next()) {
            contador++;
            int cupo    = rs.getInt("CantAlumnos");
            int ocupados = rs.getInt("ocupados");
            if (ocupados < cupo) {
              idComisionDisponible = rs.getInt("IdComision");
              nroComisionDisponible = contador;
              break;
            }
          }
        }
      }

      if (idComisionDisponible == null) {
        resp.sendRedirect(req.getContextPath() + "/alumno/cursos?error=No+hay+cupo+disponible+en+ninguna+comisi%C3%B3n");
        return;
      }

      try (PreparedStatement ps = cn.prepareStatement(sqlInsert)) {
        ps.setInt(1, idComisionDisponible);
        ps.setInt(2, Integer.parseInt(dni));
        ps.executeUpdate();
      }

      resp.sendRedirect(req.getContextPath() + "/alumno/cursos?ok=Te+inscribiste+correctamente+en+la+Comisi%C3%B3n+" + nroComisionDisponible);

    } catch (Exception e) {
      resp.sendRedirect(req.getContextPath() + "/alumno/cursos?error=No+se+pudo+realizar+la+inscripci%C3%B3n");
    }
  }
}
