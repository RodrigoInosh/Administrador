package cl.techk.ordenes.carga;

import cl.techk.ext.utils.Utils;

public class CargaThread extends Thread {
	int opc;

	public CargaThread(int opc) {
		super();
		this.opc = opc;
	}

	@Override
	public void run() {
		switch (opc) {
		case 1:
			CargaDatos.Agregar_prov_nuevos();
			break;
		case 2:
			CargaDatos.Agregar_comp_nuevos();
			break;
		case 3:
			CargaDatos.Agregar_comp_mercado_nuevo();
			break;
		case 4: 
			CargaDatos.Agregar_jerarquias_prod();
			break;
		} 
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			Utils.printOrdErr("Error Carga Thread;"+e.getMessage());
		}
	}
}
