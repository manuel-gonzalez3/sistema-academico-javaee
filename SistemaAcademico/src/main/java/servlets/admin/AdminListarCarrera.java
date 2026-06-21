package servlets.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;
import java.util.*;

import util.DatabaseConnection;
import entidades.Carrera;

/**
 * Servlet que lista todas las carreras.
 * Solo permite acceso a usuarios con rol ADMIN.
 */
@WebServlet("/carrera/listar")
public class AdminListarCarrera extends HttpServlet {

  /**
   * GET /carrera/listar
   * Consulta la base de datos y muestra el listado de carrera.
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // Verifica que el usuario tenga rol ADMIN
    if (!util.ServletUtils.checkRol(req, resp, "ADMIN")) return;

    // Lista donde se van a cargar las carrera obtenidas de la base
    List<Carrera> lista = new ArrayList<>();

    // Consulta para traer todas las carrera ordenadas por ID
    String sql = "SELECT IdCarrera, Nombre, Activo FROM carrera ORDER BY IdCarrera";

    try (Connection cn = DatabaseConnection.getConnection();
         PreparedStatement ps = cn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

      // Recorre el ResultSet y arma los objetos Carrera
      while (rs.next()) {
        Carrera f = new Carrera();
        f.setIdCarrera(rs.getInt("IdCarrera"));
        f.setNombre(rs.getString("Nombre"));
        f.setActivo(rs.getBoolean("Activo"));
        
        lista.add(f);
      }

      // Envía la lista a la JSP
      req.setAttribute("carreras", lista);

      // Si la lista está vacía, envía un mensaje informativo
      if (lista.isEmpty()) {
        req.setAttribute("errorDB", "No hay carreras para mostrar");
      }

      req.getRequestDispatcher("/admin/carrera/listar.jsp").forward(req, resp);

    } catch (Exception e) {
      // En caso de error de base de datos, envía lista vacía y mensaje de error
      req.setAttribute("carreras", Collections.emptyList());
      req.setAttribute("errorDB", "Error al acceder a la base de datos");
      req.getRequestDispatcher("/admin/carrera/listar.jsp").forward(req, resp);
    }
  }
}
