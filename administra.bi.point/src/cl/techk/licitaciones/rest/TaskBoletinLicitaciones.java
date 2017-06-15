package cl.techk.licitaciones.rest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.TimerTask;

import cl.techk.ext.database.DBCnx;
import cl.techk.ext.utils.Utils;

public class TaskBoletinLicitaciones extends TimerTask {
	@Override
	public void run() {
		String timezone = "America/Santiago";
		String hora = "", min = "";

		Calendar cal = Calendar.getInstance();

		Connection cnx = null;
		PreparedStatement stmt = null;
		ResultSet result = null;

		cal.setTimeZone(TimeZone.getTimeZone(timezone));
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone(timezone));

		String fecha[] = sdf.format(cal.getTime()).split(":");

		String hora_fecha = fecha[0];
		String min_fecha = fecha[1];

		int idcliente = 0;
		try {
			cnx = DBCnx.conexion();
			String query = "SELECT m.id_cliente, h.tipos_lic FROM mail_usuarioAlerta m left join "
					+ "boletin_horarios h on m.id_cliente = h.`id_cliente` WHERE id_tipoAlerta = 5 AND "
					+ "h.hora = ? and h.minutos = ? GROUP BY m.id_cliente";
			
			stmt = cnx.prepareStatement(query);
			stmt.setInt(1, Integer.parseInt(hora_fecha));
			stmt.setInt(2, Integer.parseInt(min_fecha));

			result = stmt.executeQuery();

			while (result.next()) {
				idcliente = result.getInt("id_cliente");
				String tipos_lic = result.getString("tipos_lic");
				System.out.println("Boletin Cliente:"+idcliente);
				BoletinLicitaciones.licitationDailyBulletin(idcliente, tipos_lic);
			}
			
		} catch (SQLException e) {
			System.out.println("Error obteniendo id: " + e.getMessage());
		} finally {
			DBCnx.closeAll(result, stmt, cnx);
		}
	}
}
