package cl.techk.ordenes.carga;

public class Maestro_provAux {

	int idMaestro;
	String rutProveedor;
	String razonSocial;
	String proveedorSimp;
	String proveedorAsoc;

	public Maestro_provAux(int id,String rutProveedor, String razon, String provS, String provA) {
		super();

		this.rutProveedor = rutProveedor;
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
	
	public String getRutProveedor() {
		return this.rutProveedor;
	}

	public void setRutProveedor(String rutProveedor) {
		this.rutProveedor = rutProveedor;
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
