package cl.techk.adjudicadas.carga;

public class Maestro_provAux {

	int idMaestro;
	String razonSocial;
	String proveedorSimp;
	String proveedorAsoc;

	public Maestro_provAux(int id, String razon, String provS, String provA) {
		super();

		this.idMaestro = id;
		this.razonSocial = razon;
		this.proveedorSimp = provS;
		this.proveedorAsoc = provA;
	}

	public int getIdMaestro() {
		return idMaestro;
	}

	public void setIdMaestro(int id) {
		this.idMaestro = id;
	}

	public String getRazonSocial() {
		return razonSocial;
	}

	public void setRazonSocial(String razonSocial) {
		this.razonSocial = razonSocial;
	}

	public String getProveedorSimplificado() {
		return proveedorSimp;
	}

	public void setProveedorSimplificado(String proveedorSimp) {
		this.proveedorSimp = proveedorSimp;
	}

	public String getProveedorAsociado() {
		return proveedorAsoc;
	}

	public void setProveedorAsociado(String proveedorAsoc) {
		this.proveedorAsoc = proveedorAsoc;
	}
}
