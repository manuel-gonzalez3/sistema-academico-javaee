package entidades;

public class Curso {

	private int idCurso;
	private String nombre;
	private int idCarrera;
	private boolean activo;
	private int dniDocente; // Asignado al curso, puede ser null
	private String nombreDocente; // Nombre del docente asignado al curso
	public Curso() {
	}

	public Curso(int idCurso, String nombre, int idCarrera) {
		this.idCurso = idCurso;
		this.nombre = nombre;
		this.idCarrera = idCarrera;
	}

	public int getIdCurso() {
		return idCurso;
	}

	public void setIdCurso(int idCurso) {
		this.idCurso = idCurso;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public int getIdCarrera() {
		return idCarrera;
	}

	public void setIdCarrera(int idCarrera) {
		this.idCarrera = idCarrera;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}
	public int getDniDocente() {
		return dniDocente;
	}
	public void setDniDocente(int dniDocente) {
		this.dniDocente = dniDocente;
	}
	public String getNombreDocente() {
		return nombreDocente;
	}
	public void setNombreDocente(String nombreDocente) {
		this.nombreDocente = nombreDocente;
	}
}
