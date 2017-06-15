package cl.techk.mantenedor.rest;

public class ModuloUsuario {

	// Datos para la tabla modulo_usuario
	int lic, adj_resumen, adj_dinamico, adj_descarga, oc_clasico, oc_dinamico, oc_descargas, reportes,
			mantenedor_catalogo, mantenedor_mercado, mantenedor_asignacion, mantenedor_glosas;
	
	public ModuloUsuario(int lic, int adj_resumen, int adj_dinamico, int adj_descarga, int oc_clasico, 
			int oc_dinamico, int oc_descargas, int reportes, int mantenedor_catalogo, int mantenedor_mercado, 
			int mantenedor_asignacion, int mantenedor_glosas){
		
		this.lic = lic;
		this.adj_resumen = adj_resumen;
		this.adj_dinamico = adj_dinamico;
		this.adj_descarga = adj_descarga;
		this.oc_clasico = oc_clasico;
		this.oc_dinamico = oc_dinamico;
		this.oc_descargas = oc_descargas;
		this.reportes = reportes;
		this.mantenedor_catalogo = mantenedor_catalogo;
		this.mantenedor_mercado = mantenedor_mercado;
		this.mantenedor_asignacion = mantenedor_asignacion;
		this.mantenedor_glosas = mantenedor_glosas;
	}
	
	public int getLic() {
		return lic;
	}

	public void setLic(int lic) {
		this.lic = lic;
	}

	public int getAdj_resumen() {
		return adj_resumen;
	}

	public void setAdj_resumen(int adj_resumen) {
		this.adj_resumen = adj_resumen;
	}

	public int getAdj_dinamico() {
		return adj_dinamico;
	}

	public void setAdj_dinamico(int adj_dinamico) {
		this.adj_dinamico = adj_dinamico;
	}

	public int getAdj_descarga() {
		return adj_descarga;
	}

	public void setAdj_descarga(int adj_descarga) {
		this.adj_descarga = adj_descarga;
	}

	public int getOc_clasico() {
		return oc_clasico;
	}

	public void setOc_clasico(int oc_clasico) {
		this.oc_clasico = oc_clasico;
	}

	public int getOc_dinamico() {
		return oc_dinamico;
	}

	public void setOc_dinamico(int oc_dinamico) {
		this.oc_dinamico = oc_dinamico;
	}

	public int getOc_descargas() {
		return oc_descargas;
	}

	public void setOc_descargas(int oc_descargas) {
		this.oc_descargas = oc_descargas;
	}

	public int getReportes() {
		return reportes;
	}

	public void setReportes(int reportes) {
		this.reportes = reportes;
	}

	public int getMantenedor_catalogo() {
		return mantenedor_catalogo;
	}

	public void setMantenedor_catalogo(int mantenedor_catalogo) {
		this.mantenedor_catalogo = mantenedor_catalogo;
	}

	public int getMantenedor_mercado() {
		return mantenedor_mercado;
	}

	public void setMantenedor_mercado(int mantenedor_mercado) {
		this.mantenedor_mercado = mantenedor_mercado;
	}

	public int getMantenedor_asignacion() {
		return mantenedor_asignacion;
	}

	public void setMantenedor_asignacion(int mantenedor_asignacion) {
		this.mantenedor_asignacion = mantenedor_asignacion;
	}

	public int getMantenedor_glosas() {
		return mantenedor_glosas;
	}

	public void setMantenedor_glosas(int mantenedor_glosas) {
		this.mantenedor_glosas = mantenedor_glosas;
	}
}
