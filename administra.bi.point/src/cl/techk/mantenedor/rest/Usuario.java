package cl.techk.mantenedor.rest;

import java.util.LinkedHashMap;

public class Usuario {

	//Datos para tabla usuario
	String perfil;
	int idcliente;
	int idusuario;
	String cliente;
	String nombre;
	String usuario;
	String contraseña;
	String email;
	String mercado;
	
	LinkedHashMap<Integer, String[]> notification_settings = new LinkedHashMap<Integer, String[]>();
	//Datos de accesos del usuario a los módulos.
	ModuloUsuario modulos;
	
	public Usuario(String perfil, int idcliente, String cliente, String nombre, String usuario, String contraseña, String email,
			String mercado) {
		super();
		this.perfil = perfil;
		this.idcliente = idcliente;
		this.cliente = cliente;
		this.nombre = nombre;
		this.usuario = usuario;
		this.contraseña = contraseña;
		this.email = email;
		this.mercado = mercado;
	}
	
	public String getPerfil() {
		return perfil;
	}

	public void setPerfil(String perfil) {
		this.perfil = perfil;
	}
	
	public String getCliente() {
		return cliente;
	}

	public void setCliente(String cliente) {
		this.cliente = cliente;
	}

	public int getIdcliente() {
		return idcliente;
	}

	public void setIdcliente(int idcliente) {
		this.idcliente = idcliente;
	}
	
	public int getIdusuario() {
		return idusuario;
	}

	public void setIdusuario(int idusuario) {
		this.idusuario = idusuario;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getContraseña() {
		return contraseña;
	}

	public void setContraseña(String contraseña) {
		this.contraseña = contraseña;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMercado() {
		return mercado;
	}

	public void setMercado(String mercado) {
		this.mercado = mercado;
	}

	public ModuloUsuario getModulos() {
		return modulos;
	}

	public void setModulos(ModuloUsuario modulos) {
		this.modulos = modulos;
	}
}
