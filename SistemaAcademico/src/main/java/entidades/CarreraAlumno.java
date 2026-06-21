package entidades;

import java.time.LocalDate;

/**
 * Relación entre un alumno y la carrera en la que está inscripto.
 */
public class CarreraAlumno {

  private int dniAlumno;
  private int idCarrera;
  private String estado;
  private LocalDate fechaInscripcion; // fecha en que el alumno se inscribió a la carrera

  public CarreraAlumno() {
  }

  public CarreraAlumno(int dniAlumno, int idCarrera) {
    this.dniAlumno = dniAlumno;
    this.idCarrera = idCarrera;
  }

  public int getDniAlumno() {
    return dniAlumno;
  }

  public void setDniAlumno(int dniAlumno) {
    this.dniAlumno = dniAlumno;
  }

  public int getIdCarrera() {
    return idCarrera;
  }

  public void setIdCarrera(int idCarrera) {
    this.idCarrera = idCarrera;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public LocalDate getFechaInscripcion() {
    return fechaInscripcion;
  }

  public void setFechaInscripcion(LocalDate fechaInscripcion) {
    this.fechaInscripcion = fechaInscripcion;
  }
}
