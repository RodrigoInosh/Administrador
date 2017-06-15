package cl.techk.ordenes.carga;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import cl.techk.ext.database.DBCnx;
import cl.techk.ext.utils.Utils;

/**
 * Servlet implementation class ValidarArchivo
 */
@WebServlet("/ValidarDataCargada")
public class ValidarDataCargada extends HttpServlet {
	
	// version 1.1
	// last review: 23-05-2016
	// last fixes:
	// Modificadas las validaciones para que generen un reporte en xlsx
	// Corregida la query para la validación de la jerarquía de productos.
	
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ValidarDataCargada() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String mercado = "";

		mercado = request.getParameter("info");

		String mercados[] = mercado.replace("[", "").replace("]", "").split(",");
		String where_mercados = "";
		for (int ix = 0; ix < mercados.length; ix++) {
			mercados[ix] = mercados[ix].replace("\"", "");

			switch (mercados[ix]) {
			case "rev_med":
				if (where_mercados.equals("")) {
					where_mercados += "'Medicamentos'";
				} else {
					where_mercados += ",'Medicamentos'";
				}
				break;
			case "rev_nut":
				if (where_mercados.equals("")) {
					where_mercados += "'Formulas Nutricionales'";
				} else {
					where_mercados += ",'Formulas Nutricionales'";
				}
				break;
			case "rev_ofi":
				if (where_mercados.equals("")) {
					where_mercados += "'Suministros de Oficina'";
				} else {
					where_mercados += ",'Suministros de Oficina'";
				}
				break;
			case "rev_tec":
				if (where_mercados.equals("")) {
					where_mercados += "'Tecnologia'";
				} else {
					where_mercados += ",'Tecnologia'";
				}
				break;
			}
		}

		if (!where_mercados.equals("")) {
			where_mercados = "WHERE mercado IN (" + where_mercados + ")";
		}

		String ruta = "/Validaciones/Validacion_Data_Cargada.xlsx";
		try {

			File myFile = new File(ruta);
			if (!myFile.exists()) {
				myFile.createNewFile();
			}

			XSSFWorkbook workbook = new XSSFWorkbook();

			validarDatosProveedor(workbook, where_mercados);
			validarDatosComprador(workbook, where_mercados);
			validarDatosCompradorMercado(workbook, where_mercados);
			validarJerarquiaProductos(workbook, where_mercados);
			
			FileOutputStream out;

			out = new FileOutputStream(new File(ruta));
			workbook.write(out);
			out.close();

		} catch (Exception e) {
			Utils.printOrdErr("Error creando excel: " + e.getMessage());
		}
	}
	
	private static void validarDatosProveedor(XSSFWorkbook workbook, String where) {
		Utils.printOrdDeb("Revisando Maestro Proveedor");

		String query = "SELECT t.rutProveedor RutProveedorData, t.proveedorAsociado ProveedorAsocData, "
				+ "mprov.id, mprov.rutProveedor rutProveedorMaestro, mprov.proveedorAsociado ProveedorAsociadoMaestro "
				+ "FROM (SELECT rutProveedor, proveedorAsociado FROM stage_point_ordenes.ordenes_mes_act ord " + where
				+ " GROUP BY "
				+ "rutProveedor , proveedorAsociado) AS t LEFT JOIN stage_point_ordenes.maestros_proveedores mprov ON "
				+ "mprov.rutProveedor = t.rutProveedor AND mprov.proveedorAsociado = t.proveedorAsociado where "
				+ "mprov.id is null;";

		Connection cnx = null;
		PreparedStatement stmt = null;
		ResultSet result = null;

		try {

			XSSFSheet spreadsheet = workbook.createSheet("Proveedores");
			XSSFRow row;
			XSSFCell cell;
			
			row = spreadsheet.createRow((short) 0);
			XLSXheaderProveedores(row, workbook);
			
			cnx = DBCnx.conexion();
			stmt = cnx.prepareStatement(query);
			result = stmt.executeQuery();

			int row_excel = 1;
			while (result.next()) {
				row = spreadsheet.createRow((short) row_excel);

				String rut_prov_data = result.getString("RutProveedorData");
				String prov_asoc_data = result.getString("ProveedorAsocData");
				
				cell = (XSSFCell) row.createCell((short) 0);
				cell.setCellValue(rut_prov_data);

				cell = (XSSFCell) row.createCell((short) 1);
				cell.setCellValue(prov_asoc_data);
				
				row_excel++;
			}
			Utils.printOrdDeb("Revisión finalizada");
		} catch (Exception e) {
			e.printStackTrace();
			Utils.printOrdErr("Error validarDatosProveedor:" + e.getMessage());
		} finally {
			DBCnx.closeAll(result, stmt, cnx);
		}
	}
	
	private static void validarDatosComprador(XSSFWorkbook workbook, String where) {

		String query = "SELECT t.rutComprador,mcom.rutComprador rutCompradorMaestro FROM (SELECT "
				+ "rutComprador FROM stage_point_ordenes.ordenes_mes_act ord " + where + " GROUP BY rutComprador) AS t "
				+ "LEFT JOIN stage_point_ordenes.maestros_compradores_gnral  mcom ON mcom.rutComprador = "
				+ "t.rutComprador WHERE mcom.rutComprador IS NULL;";

		Connection cnx = null;
		PreparedStatement stmt = null;
		ResultSet result = null;
		
		Utils.printOrdDeb("Revisando Maestro Comprador");
		try {
			XSSFSheet spreadsheet = workbook.createSheet("Comprador Gnral");
			XSSFRow row;
			XSSFCell cell;
			
			row = spreadsheet.createRow((short) 0);
			
			cell = (XSSFCell) row.createCell((short) 0);
			cell.setCellValue("Rut Comprador");
			
			cnx = DBCnx.conexion();
			stmt = cnx.prepareStatement(query);
			result = stmt.executeQuery();

			int row_excel = 1;
			while (result.next()) {
				row = spreadsheet.createRow((short) row_excel);
				
				String rut_comp_data = result.getString("rutComprador");
				
				cell = (XSSFCell) row.createCell((short) 0);
				cell.setCellValue(rut_comp_data);
				
				row_excel++;
			}
			Utils.printOrdDeb("Revisión finalizada");
		} catch (Exception e) {
			e.printStackTrace();
			Utils.printOrdErr("Error validarDatosComprador:" + e.getMessage());
		} finally {
			DBCnx.closeAll(result, stmt, cnx);
		}
	}
	
	private static void validarDatosCompradorMercado(XSSFWorkbook workbook, String where) {

		String query = "SELECT t.rutComprador,t.mercado,mcomM.id,mcomM.rutComprador rutCompradorMaestro,"
				+ "mcomM.mercado FROM (SELECT rutComprador, mercado FROM stage_point_ordenes.ordenes_mes_act ord "
				+ where
				+ " GROUP BY rutComprador , mercado) AS t LEFT JOIN stage_point_ordenes.maestros_compradores_mercado mcomM "
				+ "ON mcomM.rutComprador = t.rutComprador AND mcomM.mercado = t.mercado WHERE mcomM.id IS NULL";

		Utils.printOrdDeb("Revisando Maestro Comprador Mercado");
		Connection cnx = null;
		PreparedStatement stmt = null;
		ResultSet result = null;

		try {
			
			XSSFSheet spreadsheet = workbook.createSheet("Comprador Mercado");
			XSSFRow row;
			XSSFCell cell;
			
			row = spreadsheet.createRow((short) 0);
			XLSXheaderProveedores(row, workbook);
			
			cell = (XSSFCell) row.createCell((short) 0);
			cell.setCellValue("Rut Comprador");
			
			cell = (XSSFCell) row.createCell((short) 1);
			cell.setCellValue("Mercado");

			cnx = DBCnx.conexion();
			stmt = cnx.prepareStatement(query);
			result = stmt.executeQuery();

			int row_excel = 1;
			while (result.next()) {
				row = spreadsheet.createRow((short) row_excel);

				String rut_comp_data = result.getString("rutComprador");
				String mercado_data = result.getString("t.mercado");
//				String rut_comp_maestro = result.getString("rutCompradorMaestro");
//				String mercado_maestro = result.getString("mcomM.mercado");
				
				cell = (XSSFCell) row.createCell((short) 0);
				cell.setCellValue(rut_comp_data);
				
				cell = (XSSFCell) row.createCell((short) 1);
				cell.setCellValue(mercado_data);
				row_excel++;
			}
			Utils.printOrdDeb("Revisión finalizada");
		} catch (Exception e) {
			e.printStackTrace();
			Utils.printOrdErr("Error validarDatosCompradorMercado:" + e.getMessage());
		} finally {
			DBCnx.closeAll(result, stmt, cnx);
		}
	}

	private static void validarJerarquiaProductos(XSSFWorkbook workbook, String where) {
		Utils.printOrdDeb("Revisando Jerarquia de Productos");

		String query_cabeceras = "SELECT t.mercado,t.categoria,t.subcategoria1,t.subcategoria2,"
				+ "t.subcategoria3,t.subcategoria4,t.subcategoria5,t.subcategoria6,"
				+ "t.subcategoria7,t.subcategoria8 FROM ";
		
		String datos_mes = "(SELECT ord.mercado,ord.categoria,ord.subcategoria1,ord.subcategoria2,ord.subcategoria3,"
				+ "ord.subcategoria4,ord.subcategoria5,ord.subcategoria6,ord.subcategoria7,ord.subcategoria8 "
				+ "FROM stage_point_ordenes.ordenes_mes_act ord "+where+" GROUP BY ord.mercado,ord.categoria,"
				+ "ord.subcategoria1,ord.subcategoria2,ord.subcategoria3,ord.subcategoria4,ord.subcategoria5,"
				+ "ord.subcategoria6,ord.subcategoria7,ord.subcategoria8) AS t";
		
		String datos_maestra = "(SELECT id,mercado,categoria,Subcategoria1,Subcategoria2,Subcategoria3,"
				+ "CASE WHEN Subcategoria4 IS NULL THEN '' ELSE Subcategoria4 END Subcategoria4,"
				+ "CASE WHEN Subcategoria5 IS NULL THEN '' ELSE Subcategoria5 END Subcategoria5,"
				+ "CASE WHEN Subcategoria6 IS NULL THEN '' ELSE Subcategoria6 END Subcategoria6,"
				+ "CASE WHEN Subcategoria7 IS NULL THEN '' ELSE Subcategoria7 END Subcategoria7,"
				+ "CASE WHEN Subcategoria8 IS NULL THEN '' ELSE Subcategoria8 END Subcategoria8 "
				+ "FROM stage_point_ordenes.maestros_jerarquias_productos) AS t2";
		
		String left_join_on = " ON t.mercado = t2.mercado AND t.categoria = t2.categoria AND t.subcategoria1 = "
				+ "t2.subcategoria1 AND t.subcategoria2 = t2.subcategoria2 AND t.subcategoria3 = t2.subcategoria3 "
				+ "AND t.subcategoria4 = t2.subcategoria4 AND t.subcategoria5 = t2.subcategoria5 AND t.subcategoria6"
				+ " = t2.subcategoria6 AND t.subcategoria7 = t2.subcategoria7 AND t.subcategoria8 = t2.subcategoria8 WHERE t2.id IS NULL";

		String query = query_cabeceras + datos_mes + " LEFT JOIN " + datos_maestra + left_join_on;
		
		Connection cnx = null;
		PreparedStatement stmt = null;
		ResultSet result = null;

		try {
			XSSFSheet spreadsheet = workbook.createSheet("Jerarquía Productos");
			XSSFRow row;
			XSSFCell cell;
			
			row = spreadsheet.createRow((short) 0);
			
			cell = (XSSFCell) row.createCell((short) 0);
			cell.setCellValue("Mercado");
			cell = (XSSFCell) row.createCell((short) 1);
			cell.setCellValue("Categoria");
			cell = (XSSFCell) row.createCell((short) 2);
			cell.setCellValue("SubCategoria1");
			cell = (XSSFCell) row.createCell((short) 3);
			cell.setCellValue("SubCategoria2");
			cell = (XSSFCell) row.createCell((short) 4);
			cell.setCellValue("SubCategoria3");
			cell = (XSSFCell) row.createCell((short) 5);
			cell.setCellValue("SubCategoria4");
			cell = (XSSFCell) row.createCell((short) 6);
			cell.setCellValue("SubCategoria5");
			cell = (XSSFCell) row.createCell((short) 7);
			cell.setCellValue("SubCategoria6");
			cell = (XSSFCell) row.createCell((short) 8);
			cell.setCellValue("SubCategoria7");
			cell = (XSSFCell) row.createCell((short) 9);
			cell.setCellValue("SubCategoria8");

			cnx = DBCnx.conexion();
			stmt = cnx.prepareStatement(query);
			result = stmt.executeQuery();

			int row_excel = 1;
			while (result.next()) {
				row = spreadsheet.createRow((short) row_excel);

				String mercado = result.getString("t.mercado");
				String categoria = result.getString("t.categoria");
				String scat1 = result.getString("t.subcategoria1");
				String scat2 = result.getString("t.subcategoria2");
				String scat3 = result.getString("t.subcategoria3");
				String scat4 = result.getString("t.subcategoria4");
				String scat5 = result.getString("t.subcategoria5");
				String scat6 = result.getString("t.subcategoria6");
				String scat7 = result.getString("t.subcategoria7");
				String scat8 = result.getString("t.subcategoria8");
				
				cell = (XSSFCell) row.createCell((short) 0);
				cell.setCellValue(mercado);
				cell = (XSSFCell) row.createCell((short) 1);
				cell.setCellValue(categoria);
				cell = (XSSFCell) row.createCell((short) 2);
				cell.setCellValue(scat1);
				cell = (XSSFCell) row.createCell((short) 3);
				cell.setCellValue(scat2);
				cell = (XSSFCell) row.createCell((short) 4);
				cell.setCellValue(scat3);
				cell = (XSSFCell) row.createCell((short) 5);
				cell.setCellValue(scat4);
				cell = (XSSFCell) row.createCell((short) 6);
				cell.setCellValue(scat5);
				cell = (XSSFCell) row.createCell((short) 7);
				cell.setCellValue(scat6);
				cell = (XSSFCell) row.createCell((short) 8);
				cell.setCellValue(scat7);
				cell = (XSSFCell) row.createCell((short) 9);
				cell.setCellValue(scat8);
				
				row_excel++;
			}
			Utils.printOrdDeb("Revisión Finalizada");
		} catch (Exception e) {
			e.printStackTrace();
			Utils.printOrdErr("Error validarDatosCompradorMercado:" + e.getMessage());
		} finally {
			DBCnx.closeAll(result, stmt, cnx);
		}
	}
	
	public static void XLSXheaderProveedores(Row row_header, XSSFWorkbook wb) {

		XSSFCell cell;
		
		cell = (XSSFCell) row_header.createCell((short) 0);
		cell.setCellValue("Rut Proveedor");

		cell = (XSSFCell) row_header.createCell((short) 1);
		cell.setCellValue("Proveedor Asociado");

	}

}
