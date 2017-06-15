package cl.techk.adjudicadas.carga;

public class Maestro_prodAux {

	int id;
	int idMaestro;
	String mercado;
	String categoria;
	String subcat1;
	String subcat2;
	String subcat3;
	String subcat4;
	String subcat5;
	String subcat6;
	String subcat7;
	String subcat8;

	public Maestro_prodAux(int id, int id_maestro, String mercado, String cat, String sub1, String sub2, String sub3,
			String sub4, String sub5, String sub6, String sub7, String sub8) {

		super();

		this.id = id;
		this.idMaestro = id_maestro;
		this.mercado = mercado;
		this.categoria = cat;
		this.subcat1 = sub1;
		this.subcat2 = sub2;
		this.subcat3 = sub3;
		this.subcat4 = sub4;
		this.subcat5 = sub5;
		this.subcat6 = sub6;
		this.subcat7 = sub7;
		this.subcat8 = sub8;
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

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}

	public String getSubcat1() {
		return subcat1;
	}

	public void setSubcat1(String subcat1) {
		this.subcat1 = subcat1;
	}

	public String getSubcat2() {
		return subcat2;
	}

	public void setSubcat2(String subcat2) {
		this.subcat2 = subcat2;
	}

	public String getSubcat3() {
		return subcat3;
	}

	public void setSubcat3(String subcat3) {
		this.subcat3 = subcat3;
	}

	public String getSubcat4() {
		return subcat4;
	}

	public void setSubcat4(String subcat4) {
		this.subcat4 = subcat4;
	}

	public String getSubcat5() {
		return subcat5;
	}

	public void setSubcat5(String subcat5) {
		this.subcat5 = subcat5;
	}

	public String getSubcat6() {
		return subcat6;
	}

	public void setSubcat6(String subcat6) {
		this.subcat6 = subcat6;
	}

	public String getSubcat7() {
		return subcat7;
	}

	public void setSubcat7(String subcat7) {
		this.subcat7 = subcat7;
	}

	public String getSubcat8() {
		return subcat8;
	}

	public void setSubcat8(String subcat8) {
		this.subcat8 = subcat8;
	}
}
