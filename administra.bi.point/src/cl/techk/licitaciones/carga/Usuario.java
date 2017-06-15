package cl.techk.licitaciones.carga;

public class Usuario {
	int id_usuario;
	int id_perfil;
	
	
	public Usuario(int id_usuario, int id_perfil) {
		super();
		this.id_usuario = id_usuario;
		this.id_perfil = id_perfil;
	}
	public int getId_usuario() {
		return id_usuario;
	}
	public void setId_usuario(int id_usuario) {
		this.id_usuario = id_usuario;
	}
	public int getId_perfil() {
		return id_perfil;
	}
	public void setId_perfil(int id_perfil) {
		this.id_perfil = id_perfil;
	}
	
}
