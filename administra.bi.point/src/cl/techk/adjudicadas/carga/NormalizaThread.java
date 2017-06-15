package cl.techk.adjudicadas.carga;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import cl.techk.ext.database.DBCnx;
import cl.techk.ext.database.DataManager;
import cl.techk.ext.utils.Utils;

public class NormalizaThread extends Thread {
	String query;
	String tabla;
	String col;

	public NormalizaThread(String query, String tabla, String col) {
		super();
		this.query = query;
		this.tabla = tabla;
		this.col = col;
	}

	@Override
	public void run() {
		Statement consulta = null;
		ResultSet resultados = null;
		Connection conexion = null;

		String col_fil = this.col.equals("tipo_licitacion") ? "tipo" : this.col;
		try {

			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();

			resultados = consulta.executeQuery(this.query);
			String dato = "";
			while (resultados.next()) {
				dato = resultados.getString("" + col_fil + "");

				String insert = "INSERT INTO stage_point_adjudicadas." + this.tabla + " (" + this.col + ") " + "VALUES (\""
						+ dato + "\")";

				Utils.printAdjErr("Insertando en " + this.tabla + "");
				DataManager.insertData2(insert, "adj");
			}
		} catch (Exception error) {
			Thread.currentThread().interrupt();
			Utils.printAdjErr("Error Normaliza Thread: " + error.getMessage());
		} finally {
			DBCnx.close(resultados);
			DBCnx.close(consulta);
			DBCnx.close(conexion);
		}
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			Utils.printAdjErr("Error ejecutado Thread: " + e.getMessage());
		}
	}
}