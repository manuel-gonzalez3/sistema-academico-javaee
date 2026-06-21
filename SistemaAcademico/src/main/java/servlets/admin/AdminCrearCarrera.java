package servlets.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import util.DatabaseConnection;

// Crea una nueva carrera en el sistema. Solo accesible para ADMIN.
@WebServlet("/carrera/crear")
public class AdminCrearCarrera extends HttpServlet {
	// Muestra el formulario de alta de carrera.
  @Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {

	  if (!util.ServletUtils.checkRol(req, resp, "ADMIN")) return;

	  req.getRequestDispatcher("/admin/carrera/crear.jsp").forward(req, resp);
	}

  // Valida y persiste la nueva carrera; rechaza nombres duplicados.
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    if (!util.ServletUtils.checkRol(req, resp, "ADMIN")) return;

    req.setCharacterEncoding("UTF-8");

    // Obtiene el nombre ingresado en el formulario
    String nombre = req.getParameter("nombre");
    if (nombre != null) nombre = nombre.trim();

    // Valida que el nombre no esté vacío
    if (nombre == null || nombre.isEmpty()) {
      req.setAttribute("error", "El nombre es obligatorio.");
      req.setAttribute("nombre", nombre);
      req.getRequestDispatcher("/admin/carrera/crear.jsp").forward(req, resp);
      return;
    }

    // Consulta para verificar si ya existe una carrera con el mismo nombre
    String sqlExiste = "SELECT 1 FROM carrera WHERE Nombre = ? LIMIT 1";

    // Consulta para insertar la nueva carrera
    String sqlInsert = "INSERT INTO carrera (Nombre, Activo) VALUES (?, 1)";

    try (Connection cn = DatabaseConnection.getConnection()) {

      // Verifica si la carrera ya existe
      try (PreparedStatement ps1 = cn.prepareStatement(sqlExiste)) {
        ps1.setString(1, nombre);
        try (ResultSet rs = ps1.executeQuery()) {
          if (rs.next()) {
            // Si existe, vuelve al formulario con mensaje de error
            req.setAttribute("error", "Ya existe una carrera con ese nombre.");
            req.setAttribute("nombre", nombre);
            req.getRequestDispatcher("/admin/carrera/crear.jsp").forward(req, resp);
            return;
          }
        }
      }

      // Inserta la nueva carrera
      try (PreparedStatement ps = cn.prepareStatement(sqlInsert)) {
        ps.setString(1, nombre);
        ps.executeUpdate();
      }

      // Redirige al listado con mensaje de éxito
      resp.sendRedirect(req.getContextPath()
          + "/carrera/listar?ok=Carrera+creada+correctamente");

    } catch (Exception e) {
      req.setAttribute("error", "Error al crear la carrera");
      req.getRequestDispatcher("/admin/carrera/crear.jsp").forward(req, resp);
    }
  }
}
