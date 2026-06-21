package entidades;

public class Examen {

    private int idExamen;
    private int dniDocente;
    private int idComision;
    private String fecha; // Formato: "YYYY-MM-DD"
    private String hora;  // Formato: "HH:MM"
    private String estado; // Nuevo campo para el estado del examen
    private String nombreDocente; // Nombre del docente asignado al examen
    private String nombreCurso;    // Nombre del curso al que pertenece la comisión
    private String nombreCarrera;
    private String nroComision;
    private boolean activo;

    public Examen() {
    }

    public Examen(int idExamen, int dniDocente, int idComision, String fecha, String hora) {
        this.idExamen = idExamen;
        this.dniDocente = dniDocente;
        this.idComision = idComision;
        this.fecha = fecha;
        this.hora = hora;
    }

    public int getIdExamen() {
        return idExamen;
    }

    public void setIdExamen(int idExamen) {
        this.idExamen = idExamen;
    }

    public int getDniDocente() {
        return dniDocente;
    }

    public void setDniDocente(int dniDocente) {
        this.dniDocente = dniDocente;
    }

    public int getIdComision() {
        return idComision;
    }

    public void setIdComision(int idComision) {
        this.idComision = idComision;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

	public String getNombreDocente() {
		return nombreDocente;
	}

	public void setNombreDocente(String nombreDocente) {
		this.nombreDocente = nombreDocente;
	}

	public String getNombreCurso() {
		return nombreCurso;
	}

	public void setNombreCurso(String nombreCurso) {
		this.nombreCurso = nombreCurso;
	}

	public String getNombreCarrera() {
		return nombreCarrera;
	}

	public void setNombreCarrera(String nombreCarrera) {
		this.nombreCarrera = nombreCarrera;
	}

	public String getNroComision() {
		return nroComision;
	}

	public void setNroComision(String nroComision) {
		this.nroComision = nroComision;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

}