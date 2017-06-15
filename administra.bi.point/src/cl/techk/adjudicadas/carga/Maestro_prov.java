package cl.techk.adjudicadas.carga;

public class Maestro_prov {
	String razonSocial;
	String proveedorSimp;
	String proveedorAsoc;

	public Maestro_prov(String razon, String provS, String provA) {
		super();
		this.razonSocial = razon;
		this.proveedorSimp = provS;
		this.proveedorAsoc = provA;
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
