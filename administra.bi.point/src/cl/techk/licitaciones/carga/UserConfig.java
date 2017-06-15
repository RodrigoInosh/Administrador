package cl.techk.licitaciones.carga;

import java.util.Map;

public class UserConfig {
	int id_user;
	int id_cliente;
	Map<String, Integer> ruts;
	Map<String, Integer> tipos;

	public UserConfig(int id_user, int id_cliente, Map<String, Integer> ruts, Map<String, Integer> tipos) {
		super();
		this.id_user = id_user;
		this.id_cliente = id_cliente;
		this.ruts = ruts;
		this.tipos = tipos;
	}

	public int getId_user() {
		return id_user;
	}

	public void setId_user(int id_user) {
		this.id_user = id_user;
	}

	public int getId_cliente() {
		return id_cliente;
	}

	public void setId_cliente(int id_cliente) {
		this.id_cliente = id_cliente;
	}

	public Map<String, Integer> getRuts() {
		return ruts;
	}

	public void setRuts(Map<String, Integer> ruts) {
		this.ruts = ruts;
	}

	public Map<String, Integer> getTipos() {
		return tipos;
	}

	public void setTipos(Map<String, Integer> tipos) {
		this.tipos = tipos;
	}
}
