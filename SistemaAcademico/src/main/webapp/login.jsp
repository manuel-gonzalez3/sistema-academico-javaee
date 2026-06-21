<%-- Vista de login. Muestra el formulario de acceso al sistema.
     Atributos: error (String) — mensaje de error de autenticación. --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Login</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
  
</head>
<body class="page-login">
  <div class="card card--p login-box">
    <h1 class="header-title mb-2">Ingresar</h1>
    <p class="subtitle mb-3">Usá tu email y contraseña</p>

    <% String error = (String) request.getAttribute("error"); %>
    <% if (error != null) { %>
      <div class="alert alert-danger mb-3"><%= error %></div>
    <% } %>

    <form class="form" method="post"
          action="${pageContext.request.contextPath}/LoginServlet"
          accept-charset="UTF-8" autocomplete="off">
      <div class="form-row">
        <label class="label" for="usuario">Email</label>
        <input class="input" type="email" id="usuario" name="usuario" required>
      </div>

      <div class="form-row">
        <label class="label" for="clave">Contraseña</label>
        <input class="input" type="password" id="clave" name="clave" required>
      </div>

      <div class="mt-3">
        <button type="submit" class="btn btn-primary w-100">Entrar</button>
      </div>
    </form>
  </div>
</body>
</html>
