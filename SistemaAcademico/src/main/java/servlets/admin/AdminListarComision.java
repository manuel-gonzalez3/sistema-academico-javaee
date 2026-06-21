package servlets.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;
import java.util.*;

import util.DatabaseConnection;
import entidades.Comision;

// Lista las comisiones de un curso. Solo accesible para ADMIN.
@WebServlet("/comision/listar")
public class AdminListarComision extends HttpServlet {

  // Obtiene las comisiones del curso indicado con su cupo y número de comisión.
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    if (!util.ServletUtils.checkRol(req, resp, "ADMIN")) return;

    // Validación de parámetros
    String idCursoStr = req.getParameter("idCurso");
    if (idCursoStr == null || idCursoStr.isEmpty()) {
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
    List<Comision> lista = new ArrayList<>();
    int idCarrera = -1; // Almaceno el idCarrera para usarlo en el boton volver del jsp
    String sql = """
      SELECT co.IdComision, 
    		  co.CantAlumnos, 
    		  co.IdCurso, 
    		  co.Activo,
    		  c.IdCarrera,
    		  ROW_NUMBER() OVER (PARTITION BY co.IdCurso ORDER BY co.IdComision) AS nroComision
      FROM comision co
      JOIN curso c ON c.IdCurso = co.IdCurso
      WHERE co.IdCurso = ?
      ORDER BY IdComision
    """;
    // Se obtiene la lista de comisiones para el curso dado
    try (Connection cn = DatabaseConnection.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql)) {

      ps.setInt(1, idCurso);
      
      try (ResultSet rs = ps.executeQuery()) {
    	
        while (rs.next()) {
          idCarrera = rs.getInt("IdCarrera"); // Obtengo el idCarrera del curso para usarlo en el boton volver del jsp
          Comision c = new Comision();
          c.setIdComision(rs.getInt("IdComision"));
          c.setCantAlumnos(rs.getInt("CantAlumnos"));
          c.setIdCurso(rs.getInt("IdCurso"));
          c.setActivo(rs.getBoolean("Activo"));
          c.setNroComision(rs.getInt("nroComision"));
          lista.add(c);
        }
      }
      if (lista.isEmpty()) {
          req.setAttribute("errorDB", "No hay comisiones para este curso");
        }
      req.setAttribute("comisiones", lista);
      req.setAttribute("idCurso", idCurso);
      req.setAttribute("idCarrera", idCarrera);
      req.getRequestDispatcher("/admin/carrera/curso/comision/listar.jsp").forward(req, resp);

    } catch (Exception e) {
      req.setAttribute("comisiones", Collections.emptyList());
      req.setAttribute("errorDB", "Error al acceder a la base de datos");
      req.getRequestDispatcher("/admin/carrera/curso/comision/listar.jsp").forward(req, resp);
    }
  }
}
