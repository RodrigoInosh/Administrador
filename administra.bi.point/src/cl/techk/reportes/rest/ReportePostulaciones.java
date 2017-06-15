package cl.techk.reportes.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import cl.techk.ext.database.DBCnx;
import cl.techk.ext.utils.Utils;
import cl.techk.models.Excel;

@WebServlet("/ReportePostulaciones")
public class ReportePostulaciones extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public ReportePostulaciones() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
	    
		String initial_report_date = request.getParameter("fecha_ini");
		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "inline;filename=\"Postulaciones.xlsx\"");
		createPostulationReport(initial_report_date, response);
	}

	public static void createPostulationReport(String initial_report_date, HttpServletResponse response) {

		Workbook wb = new SXSSFWorkbook();
		try {
			String sheet_postulation_name = "Postulaciones";
			String sheet_user_modules_name = "Modulo Usuarios";
			String sheet_users_name = "Usuarios";
			SXSSFSheet sheet1 = (SXSSFSheet) wb.createSheet(sheet_postulation_name);
			SXSSFSheet sheet2 = (SXSSFSheet) wb.createSheet(sheet_user_modules_name);
			SXSSFSheet sheet3 = (SXSSFSheet) wb.createSheet(sheet_users_name);
			Row row_postulation_header = sheet1.createRow(0);
			Row row_user_modules_header = sheet2.createRow(0);
			Row row_users_header = sheet3.createRow(0);
			
			getXLSHeaderPostulationSheet(row_postulation_header);
			getXLSHeaderModulesByUserSheet(row_user_modules_header);
			getXLSHeaderUserSheet(row_users_header);
			addPostulationsDataOnSheet(sheet1, initial_report_date);
			addModulesUserDataOnSheet(sheet2);
			addUsersDataOnSheet(sheet3);
			OutputStream out = response.getOutputStream();
			wb.write(out);
			wb.close();
			out.close();
		} catch (Exception e) {
			try {
				wb.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}

	public static void addPostulationsDataOnSheet(SXSSFSheet report_sheet, String fecha_ini) {
	    
	    Statement statement_postulation_information = null;
        ResultSet postulation_information= null;
        Connection database_connection = null;
		int contador_licitaciones = 0;
        int row_index = 1;
        String[] info_postulaciones = new String[9];
		try {
		    database_connection = DBCnx.conexion();
		    statement_postulation_information = database_connection.createStatement();
			String query = "SELECT  l.codigo NroLicitacion, l.fecha_cierre, c.nombre Cliente, CASE WHEN pl.usuario_idusuario < 0 THEN 'Sin Usuario' "
			        + "ELSE u2.nombre END Usuario, CASE WHEN pl.estado = 'Ingresada' THEN 'Postulada' WHEN pl.estado IS NULL THEN 'No Guardada' "
			        + "WHEN pl.estado = 'No Postulada' THEN 'Descartada' ELSE pl.estado END Estado, pl.fecha fecha_ultima_actualizacion, po.nombre Origen, "
			        + "CASE WHEN pl.estado NOT IN ('No Postulada') THEN '' ELSE m.nombre END Motivo, CASE WHEN pl.subida_mp = 1 THEN 'Sí' "
			        + "WHEN pl.subida_mp = 0 THEN 'No' ELSE pl.subida_mp END subida_mp FROM (SELECT id_licitacion, id_cliente FROM cliente_item AS ci "
			        + "LEFT JOIN licitacion_item AS li ON ci.id_item = li.id GROUP BY id_cliente, id_licitacion) AS t LEFT JOIN point_licitaciones.postulacion_licitacion pl "
			        + "ON t.id_licitacion = pl.licitacion_idlicitacion AND t.id_cliente = pl.cliente_idcliente LEFT JOIN point_licitaciones.usuario u2 "
			        + "ON pl.usuario_idusuario = u2.idusuario LEFT JOIN point_licitaciones.licitacion l ON l.id = t.id_licitacion LEFT JOIN "
			        + "point_licitaciones.postulacion_origen po ON po.idorigen = pl.origen LEFT JOIN point_licitaciones.motivo_no_postulacion m ON m.id = pl.motivo_no_postulacion "
			        + "LEFT JOIN point_licitaciones.cliente c ON c.idcliente = t.id_cliente WHERE fecha_cierre >= '"+fecha_ini+" 00:00:00';";
			postulation_information = statement_postulation_information.executeQuery(query);
			while (postulation_information.next()) {

				info_postulaciones[0] = postulation_information.getString("NroLicitacion");
				info_postulaciones[1] = postulation_information.getString("fecha_cierre");
				info_postulaciones[2] = postulation_information.getString("Cliente");
				info_postulaciones[3] = postulation_information.getString("Usuario");
				info_postulaciones[4] = postulation_information.getString("Estado");
				info_postulaciones[5] = postulation_information.getString("fecha_ultima_actualizacion");
				info_postulaciones[6] = postulation_information.getString("Origen");
				info_postulaciones[7] = postulation_information.getString("Motivo");
				info_postulaciones[8] = postulation_information.getString("subida_mp");
				Excel.setRowsValues(report_sheet, row_index, 9, info_postulaciones);
				row_index++;
				contador_licitaciones++;
			}
			Utils.print(contador_licitaciones + " Postulaciones cargadas.");

		} catch (Exception error) {
			error.printStackTrace();
		} finally {
			DBCnx.closeAll(postulation_information, statement_postulation_information, database_connection);
		}
	}

	public static void addModulesUserDataOnSheet(SXSSFSheet report_sheet) {
	    
	    Statement statement_user_information = null;
        ResultSet user_information = null;
        Connection database_connection = null;
		int contador_licitaciones = 0;
        int row_index = 1;
        String[] info_usuarios = new String[10];
		try {

		    database_connection = DBCnx.conexion();
		    statement_user_information = database_connection.createStatement();
			String query = "SELECT u.idusuario,u.usuario,licitaciones Licitaciones,adj_resumen Adjudicadas,"
					+ "oc_vistaCla Ordenes,reportes Reportes,mantenedor_catologo,mantenedor_mrelevantes,"
					+ "mantenedor_asignacion, mantenedor_glosas FROM point_licitaciones.modulo_usuario m "
					+ "LEFT JOIN point_licitaciones.usuario u ON u.idusuario = m.id_usr";
			user_information = statement_user_information.executeQuery(query);
			while (user_information.next()) {

				info_usuarios[0] = user_information.getString("idusuario");
				info_usuarios[1] = user_information.getString("usuario");
				info_usuarios[2] = user_information.getString("Licitaciones");
				info_usuarios[3] = user_information.getString("Adjudicadas");
				info_usuarios[4] = user_information.getString("Ordenes");
				info_usuarios[5] = user_information.getString("Reportes");
				info_usuarios[6] = user_information.getString("mantenedor_catologo");
				info_usuarios[7] = user_information.getString("mantenedor_mrelevantes");
				info_usuarios[8] = user_information.getString("mantenedor_asignacion");
				info_usuarios[9] = user_information.getString("mantenedor_glosas");
				Excel.setRowsValues(report_sheet, row_index, 10, info_usuarios);
				row_index++;
				contador_licitaciones++;
			}
			Utils.print(contador_licitaciones + " Usuarios cargados.");

		} catch (Exception error) {
			error.printStackTrace();
		} finally {
			DBCnx.closeAll(user_information, statement_user_information, database_connection);
		}
	}

	public static void addUsersDataOnSheet(SXSSFSheet report_sheet) {
	    
		Statement statement_user_information = null;
		ResultSet user_information = null;
		Connection database_connection = null;
		int contador_licitaciones = 0;
        int row_index = 1;
        String[] info_usuarios = new String[7];
		try {

		    database_connection = DBCnx.conexion();
		    statement_user_information = database_connection.createStatement();
			String query = "SELECT c.idcliente,c.nombre cliente,u.idusuario,u.nombre,u.usuario,p.nombre perfil,"
					+ "u.habilitado FROM point_licitaciones.usuario u LEFT JOIN point_licitaciones.perfil p "
					+ "ON p.idperfil = u.perfil_idperfil LEFT JOIN point_licitaciones.cliente c ON "
					+ "c.idcliente = u.cliente_idcliente ORDER BY u.idusuario ASC;";
			user_information = statement_user_information.executeQuery(query);
			while (user_information.next()) {

				info_usuarios[0] = user_information.getString("idcliente");
				info_usuarios[1] = user_information.getString("cliente");
				info_usuarios[2] = user_information.getString("idusuario");
				info_usuarios[3] = user_information.getString("nombre");
				info_usuarios[4] = user_information.getString("usuario");
				info_usuarios[5] = user_information.getString("perfil");
				info_usuarios[6] = user_information.getString("habilitado");
				Excel.setRowsValues(report_sheet, row_index, 7, info_usuarios);
				row_index++;
				contador_licitaciones++;
			}
			Utils.print(contador_licitaciones + " Usuarios cargados.");

		} catch (Exception error) {
			error.printStackTrace();
		} finally {
			DBCnx.closeAll(user_information, statement_user_information, database_connection);
		}
	}

	public static void getXLSHeaderPostulationSheet(Row row_header) {
	    
		row_header.createCell(0).setCellValue("NroLicitacion");
		row_header.createCell(1).setCellValue("Fecha Cierre");
		row_header.createCell(2).setCellValue("Cliente");
		row_header.createCell(3).setCellValue("Usuario");
		row_header.createCell(4).setCellValue("Estado");
		row_header.createCell(5).setCellValue("Fecha Última Actualización");
		row_header.createCell(6).setCellValue("Origen");
		row_header.createCell(7).setCellValue("Motivo No Postulación");
		row_header.createCell(8).setCellValue("Subida a MP");
	}

	public static void getXLSHeaderModulesByUserSheet(Row row_header) {
	    
		row_header.createCell(0).setCellValue("Id Usuario");
		row_header.createCell(1).setCellValue("Usuario");
		row_header.createCell(2).setCellValue("Licitaciones");
		row_header.createCell(3).setCellValue("Adjudicadas");
		row_header.createCell(4).setCellValue("Ordenes");
		row_header.createCell(5).setCellValue("Reportes");
		row_header.createCell(6).setCellValue("Mantenedor Catologo");
		row_header.createCell(7).setCellValue("Mantenedor Merc. Relevantes");
		row_header.createCell(8).setCellValue("Mantenedor Asig. Usuarios");
		row_header.createCell(9).setCellValue("Mantenedor Glosa Región");
	}

	public static void getXLSHeaderUserSheet(Row row_header) {
	    
		row_header.createCell(0).setCellValue("Id Cliente");
		row_header.createCell(1).setCellValue("Cliente");
		row_header.createCell(2).setCellValue("ID Usuario");
		row_header.createCell(3).setCellValue("Nombre");
		row_header.createCell(4).setCellValue("Usuario");
		row_header.createCell(5).setCellValue("Perfil");
		row_header.createCell(6).setCellValue("Habilitado");
	}
}
