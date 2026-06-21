package entidades;

public class ExamenAlumno {

    private int idExamen;
    private int dniAlumno;
    private String asistencia; // "presente", "ausente", "pendiente"
    private int nota;

    public ExamenAlumno() {
    }

    public ExamenAlumno(int idExamen, int dniAlumno, String asistencia, int nota) {
        this.idExamen = idExamen;
        this.dniAlumno = dniAlumno;
        this.asistencia = asistencia;
        this.nota = nota;
    }

    public int getIdExamen() {
        return idExamen;
    }

    public void setIdExamen(int idExamen) {
        this.idExamen = idExamen;
    }

    public int getDniAlumno() {
        return dniAlumno;
    }

    public void setDniAlumno(int dniAlumno) {
        this.dniAlumno = dniAlumno;
    }

    public String getAsistencia() {
        return asistencia;
    }

    public void setAsistencia(String asistencia) {
        this.asistencia = asistencia;
    }

    public int getNota() {
        return nota;
    }

    public void setNota(int nota) {
        this.nota = nota;
    }
}
