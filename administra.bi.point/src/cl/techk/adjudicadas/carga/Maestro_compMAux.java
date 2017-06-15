package cl.techk.adjudicadas.carga;

public class Maestro_compMAux {

	int id;
	int idMaestro;
	String mercado;
	String segmentoComprador;

	public Maestro_compMAux(int id, int id_maestro, String mercado, String segmento) {
		super();

		this.id = id;
		this.idMaestro = id_maestro;
		this.mercado = mercado;
		this.segmentoComprador = segmento;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getIdMaestro() {
		return idMaestro;
	}

	public void setIdMaestro(int id) {
		this.idMaestro = id;
	}

	public String getMercado() {
		return mercado;
	}

	public void setMercado(String mercado) {
		this.mercado = mercado;
	}

	public String getSegmentoComprador() {
		return segmentoComprador;
	}

	public void setSegmentoComprador(String segmentoComprador) {
		this.segmentoComprador = segmentoComprador;
	}
}
