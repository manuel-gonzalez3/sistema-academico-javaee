package servlets;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebListener
public class ExamenListener implements ServletContextListener {

    private ScheduledExecutorService scheduler;

    // Este método se ejecuta automáticamente cuando Tomcat arranca la aplicación
    @Override
    public void contextInitialized(ServletContextEvent sce) {

        // Crea un scheduler con un solo hilo
        scheduler = Executors.newSingleThreadScheduledExecutor();

        // Programa la tarea: el primer argumento es lo que se ejecuta (lambda),
        //  el delay inicial (0 = corre ni bien arranca la app),
        //  cada cuánto se repite (1), y la unidad de tiempo (HOURS)
        scheduler.scheduleAtFixedRate(() -> {

         

            // UPDATE para exámenes que deben pasar a CANCELADO:
            // - estado actual es 'pendiente'
            // - ya pasó la fecha y hora del examen
            // - NO existe ningún alumno inscripto  (primer NOT EXISTS), hasta el dia anterior (tiempo de inscripccion)
            String sqlNoinscriptos = """
                UPDATE examen SET estado = 'cancelado'
                WHERE estado = 'pendiente'
                  AND fecha <= CURDATE()
                  AND (
                    NOT EXISTS (SELECT 1 FROM examen_alumno ea WHERE ea.idExamen = examen.idExamen)
                  )
            """;
            
            // UPDATE para exámenes que deben pasar a CANCELADO:
            // - estado actual es 'pendiente'
            // - PASARON 72 HS DEL EXAMEN Y NO SE CARGO NADIE PRESENTE
            String sqlAlumnosPresentes = """
                UPDATE examen SET estado = 'cancelado'
                WHERE estado = 'pendiente'
                  AND TIMESTAMP(fecha, hora) <= (NOW() - INTERVAL 72 HOUR) 
                  AND (
                    NOT EXISTS (SELECT 1 FROM examen_alumno ea WHERE ea.idExamen = examen.idExamen AND asistencia = 'presente')
                  )
            """;
            
            ///UPDATE SI NO SE CARGAN LAS ASISTENCIAS DESPUES DE 72 HORAS PASA A TODOS COMO AUSENTES
            String sqlAlumnosAusentes = """
                    UPDATE examen_alumno ea
					JOIN examen e ON ea.IdExamen = e.IdExamen
					SET ea.asistencia = 'ausente'
					WHERE ea.asistencia = 'pendiente'
            		 AND TIMESTAMP(e.fecha, e.hora) <= NOW() - INTERVAL 72 HOUR     
                """;
            // Abre la conexión a la BD y ejecuta los  UPDATEs en orden:
            //
            try (Connection cn = DatabaseConnection.getConnection()) {
                
                try (PreparedStatement ps = cn.prepareStatement(sqlNoinscriptos)) {
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = cn.prepareStatement(sqlAlumnosPresentes)) {
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = cn.prepareStatement(sqlAlumnosAusentes)) {
                    ps.executeUpdate();
                }
            } catch (Exception e) {
                // Si algo falla (BD caída, error SQL, etc.) lo logueamos
                // pero NO tiramos la excepción para que el scheduler
                // siga corriendo en la próxima hora
            }

        }, 0, 1, TimeUnit.HOURS);
    }

    // Es importante parar el scheduler acá para no dejar hilos colgados
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) {
            // shutdownNow() cancela la tarea si está corriendo y libera el hilo
            scheduler.shutdownNow();
        }
    }
}
