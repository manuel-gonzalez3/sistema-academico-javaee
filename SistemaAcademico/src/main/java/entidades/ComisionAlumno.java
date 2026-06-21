package entidades;

public class ComisionAlumno {

    private int idComision;
    private String dniAlumno;
    private String estado;

    public ComisionAlumno() {
    }

    public ComisionAlumno(int idComision, String dniAlumno) {
        this.idComision = idComision;
        this.dniAlumno = dniAlumno;
    }

    public int getIdComision() {
        return idComision;
    }

    public void setIdComision(int idComision) {
        this.idComision = idComision;
    }

    public String getDniAlumno() {
        return dniAlumno;
    }

    public void setDniAlumno(String dniAlumno) {
        this.dniAlumno = dniAlumno;
    }
    public String getEstado() {
		return estado;
	}
	public void setEstado(String estado) {
		this.estado = estado;
	}
}
