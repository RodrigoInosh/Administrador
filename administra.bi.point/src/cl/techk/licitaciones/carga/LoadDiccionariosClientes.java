package cl.techk.licitaciones.carga;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import cl.techk.ext.database.DBCnx;
import cl.techk.ext.utils.Utils;

@WebServlet("/LoadDiccionariosClientes")
public class LoadDiccionariosClientes extends HttpServlet {
	private static final long serialVersionUID = 1L;
	String archivo_rute = "/home/innovacion01/Escritorio/diccionario.xlsx";

	public LoadDiccionariosClientes() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		Utils.print("cargando diccionarios clientes  ...");
		// se debe vaciar la tabla cliente_diccionario
		try {
			FileInputStream file = new FileInputStream(new File(archivo_rute));
			XSSFWorkbook workbook = new XSSFWorkbook(file);
			XSSFSheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();
			rowIterator.next(); // ME SALTO LA CABECERA
			while (rowIterator.hasNext()) {
				// itero los registros de excel
				Row row = rowIterator.next();
				String idcliente = Utils.getValueFromXlsRow(row, 1), search1 = Utils.getValueFromXlsRow(row, 2),
						search2 = Utils.getValueFromXlsRow(row, 3), search3 = Utils.getValueFromXlsRow(row, 4),
						search4 = Utils.getValueFromXlsRow(row, 5), search5 = Utils.getValueFromXlsRow(row, 6),
						search6 = Utils.getValueFromXlsRow(row, 7), search7 = Utils.getValueFromXlsRow(row, 8),
						search8 = Utils.getValueFromXlsRow(row, 9);
				Connection cnxupdate = null;
				PreparedStatement stmtupdate = null;
				try {
					cnxupdate = DBCnx.conexion();
					stmtupdate = cnxupdate
							.prepareStatement(" insert into cliente_diccionario (id_cliente,search1,search2,"
									+ "search3 ,search4 ,search5 ,search6 ,search7 ,search8) values (?,?,?,?,?,?,?,?,?)");
					stmtupdate.setString(1, idcliente);
					stmtupdate.setString(2, search1);
					stmtupdate.setString(3, search2);
					stmtupdate.setString(4, search3);
					stmtupdate.setString(5, search4);
					stmtupdate.setString(6, search5);
					stmtupdate.setString(7, search6);
					stmtupdate.setString(8, search7);
					stmtupdate.setString(9, search8);
					stmtupdate.executeUpdate();
				} catch (Exception e) {
					e.printStackTrace();
					Utils.print("error insertando diccionarios");
				} finally {
					DBCnx.close(stmtupdate);
					DBCnx.close(cnxupdate);
				}
			}
			workbook.close();
			file.close();
		} catch (Exception e) {
			Utils.print("ERROR -> Cargando clasificacion items licitaciones (2)");
		}
	}
}
