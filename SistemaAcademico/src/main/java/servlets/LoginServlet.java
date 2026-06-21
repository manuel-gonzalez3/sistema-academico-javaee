package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import util.DatabaseConnection;
//
//USUARIOS DE PRUEBA:
//[(admin@demo; admin), (docente@demo; docente), (alumno@demo; alumno)]
//
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
	// Muestra el formulario de login.
  @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException{
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }
    @Override 
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Forzamos a que los caracteres (ñ, tildes) se lean correctamente
        req.setCharacterEncoding("UTF-8");

        // Leemos los parámetros del formulario: "usuario" y "clave"
        // Si vienen nulos (no estaban en el form), los reemplazamos con "" (cadena vacía)
        // Luego usamos .trim() para sacar espacios en blanco al inicio o al final
        String email = (req.getParameter("usuario") == null ? "" : req.getParameter("usuario")).trim();
        String pass  = (req.getParameter("clave") == null ? "" : req.getParameter("clave")).trim();

        // Consulta SQL: busca un usuario en la tabla con ese email y esa clave
        String sql =
        		"SELECT u.dni AS dni, " +
        	            "       CONCAT(p.nombre, ' ', p.apellido) AS nombre, " +
        	            "       u.tipoUsuario AS rol, " +
        	            "       u.activo as activo " +
        	            "FROM   USUARIO u " +
        	            "JOIN PERSONA p ON p.dni = u.dni " +
        	            "WHERE  p.email = ? AND u.password = ?" ;     
        try (
            // Obtenemos conexión a la base usando nuestra clase DatabaseConnection
            Connection cn = DatabaseConnection.getConnection();

            // Preparamos la consulta con placeholders (?) para evitar SQL injection
            PreparedStatement ps = cn.prepareStatement(sql)
        ) {
            // Reemplazamos el primer "?" por el email escrito
            ps.setString(1, email);
            // Reemplazamos el segundo "?" por la clave escrita
            ps.setString(2, pass);

            // Ejecutamos la consulta
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Obtenemos (o creamos) la sesión del usuario
                    String rol = rs.getString("rol");
                    if (rol != null) {
                        rol = rol.trim().toUpperCase();
                    }
                    if(rs.getBoolean("activo")) {
                    	var s = req.getSession();
                        s.setAttribute("userDni", rs.getString("dni"));
                        s.setAttribute("userNombre", rs.getString("nombre"));
                        s.setAttribute("userRol", rol);
                        
                        // redirección según rol
                        switch (rol) {
                          case "ADMIN":
                            resp.sendRedirect(req.getContextPath() + "/admin/home.jsp");
                            return;
                          case "DOCENTE":
                        	
                            resp.sendRedirect(req.getContextPath() + "/docente/home.jsp");
                            return;
                          case "ALUMNO":
                            resp.sendRedirect(req.getContextPath() + "/alumno/home.jsp");
                            return;
                        }

                    }
                    else {
                    	req.setAttribute("error", "Usuario inactivo.");
                        req.getRequestDispatcher("/login.jsp").forward(req, resp);
                    }
                       
                                   
                }
                else {
                	req.setAttribute("error", "Usuario o contraseña incorrecto");
                    req.getRequestDispatcher("/login.jsp").forward(req, resp);
                }
                
            }
        } catch (Exception e) {
            req.setAttribute("error", "No es posible acceder a la base de datos");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
        
        
    }
    
}