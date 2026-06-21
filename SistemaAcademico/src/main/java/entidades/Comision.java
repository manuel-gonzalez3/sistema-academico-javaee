package entidades;

public class Comision {

    private int idComision;
    private int cantAlumnos;
    private int idCurso;
    private boolean activo;
    private int nroComision; // Número de comisión para mostrar en el JSP
    public Comision() {}

    public Comision(int idComision, int cantAlumnos, int idCurso, boolean activo, int nroComision) {
        this.idComision = idComision;
        this.cantAlumnos = cantAlumnos;
        this.idCurso = idCurso;
        this.activo = activo;
        this.nroComision = nroComision;
    }

    public int getIdComision() {
        return idComision;
    }

    public void setIdComision(int idComision) {
        this.idComision = idComision;
    }

    public int getCantAlumnos() {
        return cantAlumnos;
    }

    public void setCantAlumnos(int cantAlumnos) {
        this.cantAlumnos = cantAlumnos;
    }

    public int getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(int idCurso) {
        this.idCurso = idCurso;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    public int getNroComision() {
		return nroComision;
	}
    public void setNroComision(int nroComision) {
		this.nroComision = nroComision;
    }
}
