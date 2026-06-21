package servlets.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;

import util.DatabaseConnection;

// Crea una persona (alumno o docente) y su cuenta de usuario. Solo accesible para ADMIN.
// La contraseña inicial es igual al DNI.
@WebServlet("/persona/crear")
public class AdminCrearPersona extends HttpServlet {

    // Muestra el formulario de alta según el rol recibido (alumno / docente).
  @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!util.ServletUtils.checkRol(req, resp, "ADMIN")) return;
        String rolPersona = req.getParameter("rol");
        if (rolPersona == null || rolPersona.isBlank()) {
			resp.sendRedirect(req.getContextPath() + "/login.jsp");
			return;
        }

        req.getRequestDispatcher("/admin/"+ rolPersona + "/crear.jsp").forward(req, resp);
    }

    // Valida datos, verifica DNI y email únicos, inserta en persona y usuario.
  @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!util.ServletUtils.checkRol(req, resp, "ADMIN")) return;
        String rolPersona = req.getParameter("rol");
        if (rolPersona == null || rolPersona.isBlank()) {
			resp.sendRedirect(req.getContextPath() + "/persona/crear?error=Rol+no+especificado");
			return;
		}

        req.setCharacterEncoding("UTF-8");
        String dniStr      = req.getParameter("dni");
        String nombre      = req.getParameter("nombre");
        String apellido    = req.getParameter("apellido");
        String email       = req.getParameter("email");
        String direccion   = req.getParameter("direccion");
        String telefono    = req.getParameter("telefono");
        String fechaNacimiento = req.getParameter("fechaNacimiento");
        String sexo        = req.getParameter("sexo");

        if (dniStr == null || dniStr.trim().isEmpty()
                || nombre == null || nombre.trim().isEmpty()
                || apellido == null || apellido.trim().isEmpty()
                || email == null || email.trim().isEmpty()) {
            req.setAttribute("error", "Debe ingresar dni, nombre, apellido y email.");
            req.getRequestDispatcher("/admin/" + rolPersona + "/crear.jsp").forward(req, resp);
            return;
        }

        // Validar que el DNI sea un número entero
        int dni;
        try {
            dni = Integer.parseInt(dniStr.trim());
        } catch (NumberFormatException e) {
            req.setAttribute("error", "El DNI debe ser un número válido.");
            req.getRequestDispatcher("/admin/" + rolPersona + "/crear.jsp").forward(req, resp);
            return;
        }

        String sexoStr = null;
        if (sexo != null && !sexo.trim().isEmpty()) {
            String sexoUp = sexo.trim().toUpperCase();
            if (sexoUp.equals("MASCULINO")) sexoStr = "M";
            else if (sexoUp.equals("FEMENINO")) sexoStr = "F";
            else sexoStr = "O";
        }

        // Verificar que no exista ya una persona con con ese dni en persona
        String sqlExistePersona = "SELECT COUNT(*) FROM persona WHERE dni = ?";
        String sqlExisteMail = "SELECT COUNT(*) FROM persona WHERE email = ?";
        String sqlInsertPersona = "INSERT INTO persona (dni, nombre, apellido, direccion, email, fechaNacimiento, sexo, telefono) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlInsertUsuario = "INSERT INTO usuario (dni, password, tipoUsuario, activo) VALUES (?, ?, ?, 1)";
        
        try (Connection cn = DatabaseConnection.getConnection()) {
			try (PreparedStatement ps = cn.prepareStatement(sqlExistePersona)) {
				ps.setInt(1, dni);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next() && rs.getInt(1) > 0) {
						req.setAttribute("error", "Ya existe una persona con ese DNI.");
						req.getRequestDispatcher("/admin/" + rolPersona + "/crear.jsp").forward(req, resp);
						return;
					}
				}
			}
			try (PreparedStatement ps = cn.prepareStatement(sqlExisteMail)) {
				ps.setString(1, email);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next() && rs.getInt(1) > 0) {
						req.setAttribute("error", "Ya existe una persona con ese email.");
						req.getRequestDispatcher("/admin/" + rolPersona + "/crear.jsp").forward(req, resp);
						return;
					}
				}
			}
			try (PreparedStatement ps = cn.prepareStatement(sqlInsertPersona)) {
				ps.setInt(1, dni);
				ps.setString(2, nombre);
				ps.setString(3, apellido);
				ps.setString(4, direccion);
				ps.setString(5, email);
				ps.setDate(6, (fechaNacimiento != null && !fechaNacimiento.trim().isEmpty()) ? Date.valueOf(fechaNacimiento) : null);
				ps.setString(7, sexoStr);
				ps.setString(8, telefono);
				ps.executeUpdate();
			}
			if ("ALUMNO".equalsIgnoreCase(rolPersona)) {
				try (PreparedStatement ps = cn.prepareStatement(sqlInsertUsuario)) {
					ps.setInt(1, dni);
					ps.setString(2, String.valueOf(dni)); // Contraseña inicial igual al DNI
					ps.setString(3, "ALUMNO");
					ps.executeUpdate();
				}
			}
			else if ("DOCENTE".equalsIgnoreCase(rolPersona)) {
				try (PreparedStatement ps = cn.prepareStatement(sqlInsertUsuario)) {
					ps.setInt(1, dni);
					ps.setString(2, String.valueOf(dni)); // Contraseña inicial igual al DNI
					ps.setString(3, "DOCENTE");
					ps.executeUpdate();
				}
			}
			resp.sendRedirect(req.getContextPath() + "/" + rolPersona + "/listar?ok="
				+ java.net.URLEncoder.encode(rolPersona + " creado correctamente", "UTF-8"));
			} catch (Exception e) {
				req.setAttribute("error", "Error al crear persona." + e.getMessage());
				req.getRequestDispatcher("/admin/" + rolPersona + "/crear.jsp").forward(req, resp);
			}
		}
}