package entidades;

import java.time.LocalDate;

/**
 * Entidad que representa a una persona en el sistema.
 * Combina datos de las tablas persona y usuario (activo, tipoUsuario).
 */
public class Persona {
	private int dni;
	private String nombre;
	private String apellido;
	private String direccion;
	private String telefono;
	private String email;
	private char sexo;
	private LocalDate fechaNacimiento;  // fecha de nacimiento de la persona
	private boolean activo;             // viene de usuario.activo
	private String tipoUsuario;         // viene de usuario.tipoUsuario ("ALUMNO" | "DOCENTE" | "ADMIN")

	public Persona() {
	}

	public Persona(int dni, String nombre, String apellido, String direccion, String telefono,
			String email, char sexo, LocalDate fechaNacimiento) {
		this.dni = dni;
		this.nombre = nombre;
		this.apellido = apellido;
		this.direccion = direccion;
		this.telefono = telefono;
		this.email = email;
		this.sexo = sexo;
		this.fechaNacimiento = fechaNacimiento;
	}

	public int getDni() {
		return dni;
	}

	public void setDni(int dni) {
		this.dni = dni;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getApellido() {
		return apellido;
	}

	public void setApellido(String apellido) {
		this.apellido = apellido;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public char getSexo() {
		return sexo;
	}

	public void setSexo(char sexo) {
		this.sexo = sexo;
	}

	public LocalDate getFechaNacimiento() {
		return fechaNacimiento;
	}

	public void setFechaNacimiento(LocalDate fechaNacimiento) {
		this.fechaNacimiento = fechaNacimiento;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public String getTipoUsuario() {
		return tipoUsuario;
	}

	public void setTipoUsuario(String tipoUsuario) {
		this.tipoUsuario = tipoUsuario;
	}
}
