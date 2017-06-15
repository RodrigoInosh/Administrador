package cl.techk.ordenes.carga;

public class Maestro_compGAux {

	int idMaestro;
	String rutComprador;
	String razonCompradorGnral;
	String unidadCompra;
	String compradorReducido;
	String comuna;
	String region;
	String segmentoComprador;

	public Maestro_compGAux(int id, String rutComprador,  String razonCompradorGnral, String unidadCompra, String compradorReducido,
			String comuna, String region, String segmentoComprador) {

		this.idMaestro = id;
		this.rutComprador = rutComprador;
		this.razonCompradorGnral = razonCompradorGnral;
		this.unidadCompra = unidadCompra;
		this.compradorReducido = compradorReducido;
		this.comuna = comuna;
		this.region = region;
		this.segmentoComprador = segmentoComprador;
	}
	
	public String getRutComprador() {
		return this.rutComprador;
	}

	public void setRutComprador(String rut) {
		this.rutComprador = rut;
	}

	public int getIdMaestro() {
		return idMaestro;
	}

	public void setIdMaestro(int id) {
		this.idMaestro = id;
	}

	public String getRazonCompradorGnral() {
		return razonCompradorGnral;
	}

	public void setRazonCompradorGnral(String razonCompradorGnral) {
		this.razonCompradorGnral = razonCompradorGnral;
	}

	public String getUnidadCompra() {
		return unidadCompra;
	}

	public void setUnidadCompra(String unidadCompra) {
		this.unidadCompra = unidadCompra;
	}

	public String getCompradorReducido() {
		return compradorReducido;
	}

	public void setCompradorReducido(String compradorReducido) {
		this.compradorReducido = compradorReducido;
	}

	public String getComuna() {
		return comuna;
	}

	public void setComuna(String comuna) {
		this.comuna = comuna;
	}

	public String getSegmentoComprador() {
		return segmentoComprador;
	}

	public void setSegmentoComprador(String segmentoComprador) {
		this.segmentoComprador = segmentoComprador;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}
}
