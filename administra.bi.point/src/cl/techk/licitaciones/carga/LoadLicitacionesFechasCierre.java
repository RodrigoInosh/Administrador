package cl.techk.licitaciones.carga;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cl.techk.ext.database.DBCnx;
import cl.techk.ext.utils.Utils;

@WebServlet("/LoadLicitacionesFechasCierre")
public class LoadLicitacionesFechasCierre extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public LoadLicitacionesFechasCierre() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String query = "UPDATE licitacion lic INNER JOIN licitacion_fecha_cierre lic_c ON lic.codigo = lic_c.codigo "
				+ "SET lic.fecha_cierre = lic_c.fecha_cierre;";
		Connection cnxupdate = null;
		PreparedStatement stmtupdate = null;
		try {
			cnxupdate = DBCnx.conexion();
			stmtupdate = cnxupdate.prepareStatement(query);
			stmtupdate.executeUpdate();

			Utils.print("Fechas Updateadas");

		} catch (Exception e) {
			Utils.print("error updateando fechas de cierre");
		} finally {
			DBCnx.close(stmtupdate);
			DBCnx.close(cnxupdate);
		}
	}

}
