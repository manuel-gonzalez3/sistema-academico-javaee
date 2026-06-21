package entidades;

public class Carrera {

    private int idCarrera;
    private String nombre;
    private boolean activo;

    
    public Carrera() {}

    public Carrera(int idCarrera, String nombre) {
        this.idCarrera = idCarrera;
        this.nombre = nombre;
    }

    public int getIdCarrera() {
        return idCarrera;
    }

    public void setIdCarrera(int idCarrera) {
        this.idCarrera = idCarrera;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
