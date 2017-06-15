package cl.techk.adjudicadas.carga;

public class Maestro_compM {

	String rutComprador;
	String mercado;
	String segmentoComprador;

	public Maestro_compM(String rut, String mercado, String segmento) {
		super();

		this.rutComprador = rut;
		this.mercado = mercado;
		this.segmentoComprador = segmento;
	}

	public String getRutComptrador() {
		return rutComprador;
	}

	public void setRutComptrador(String rut) {
		this.rutComprador = rut;
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
