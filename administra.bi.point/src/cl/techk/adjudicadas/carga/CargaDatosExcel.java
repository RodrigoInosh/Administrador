package cl.techk.adjudicadas.carga;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutorService;

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

import com.monitorjbl.xlsx.StreamingReader;

import cl.techk.ext.database.CargaDatosPoint;
import cl.techk.ext.database.DBCnx;
import cl.techk.ext.database.DataManager;
import cl.techk.ext.utils.Utils;

@WebServlet("/CargaExcel")
public class CargaDatosExcel extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	static ExecutorService ex = null;
	private static String bdd_name = "";

	public CargaDatosExcel() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String mercado = "", estado = "";
		if (request.getParameter("mercado") != null && request.getParameter("estado") != null) {
			mercado = request.getParameter("mercado");
			estado = request.getParameter("estado");

			switch (estado) {
			case "prod":
				bdd_name = "point_adjudicadas";
				break;
			case "stage":
				bdd_name = "stage_point_adjudicadas";
				break;
			}
			CargaDatosPoint.carga(mercado, "adjudicadas_aux", bdd_name, 35);
		}
	}

	public static void carga(String mercado) {
		Connection cnx = null;
		PreparedStatement stmt = null;

		try {

			String xlsxName = "/Validaciones/Adjudicadas_Validacion_Formatos_" + mercado + ".xlsx";

			XSSFWorkbook workbook = new XSSFWorkbook();
			
			XSSFSheet hoja_formatos = workbook.createSheet("Revisión Formatos");
			
			XSSFRow row_excel;
			
			row_excel = hoja_formatos.createRow((short) 0);
			XLSXheaderFormatos(row_excel, workbook);
			
			int row_hoja_datos = 1, row_hoja_formatos = 1;

			cnx = DBCnx.conexion();

			String nombre_archivo = "/CargaMercados/carga_oc_" + mercado + ".xlsx";
			Utils.printOrdDeb("Archivo Mercado:" + nombre_archivo);

			String select = "INSERT INTO " + bdd_name + ".`adjudicadas_aux`(`tipo`, `fechaAdjudicada`, `mes`, `anho`, "
					+ "`numeroLicitacion`, `rutComprador`, `unidadCompra`, `razonSocialComprador`, `comuna`, `region`, "
					+ "`segmentoComprador`, `rutProveedor`, `proveedor`, `razonSocialProveedor`, `proveedorAsociado`, "
					+ "`item`, `productoServicio`, `cantidad`, `cantidadCorregida1`, `especificacionComprador`, "
					+ "`especificacionProveedor`, `moneda`, `precioUnitario`, `precioCorregido1`, `valorTotal`, "
					+ "`estado`, `mercado`, `categoria`, `subcategoria1`, `subcategoria2`, `subcategoria3`, "
					+ "`subcategoria4`, `subcategoria5`, `subcategoria6`, `subcategoria7`, `subcategoria8`, "
					+ "`observacion`, `created_at`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
					+ "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,now())";

			stmt = cnx.prepareStatement(select);

			InputStream is = new FileInputStream(new File(nombre_archivo));
			StreamingReader reader = StreamingReader.builder().rowCacheSize(100).bufferSize(4096).sheetIndex(0)
					.read(is);

			int ix = 0;
			Utils.printOrdDeb("Ciclo");
			String datos[] = new String[59];
			for (int i = 0; i < 59; i++) {
				datos[i] = new String();
			}

			Date ini = new Date();

			for (Row row : reader) {
				Arrays.fill(datos, "");

				if (ix == 0) {
					ix++;
					continue;
				}

				datos[0] = row.getCell(0) != null ? row.getCell(0).getStringCellValue() : ""; // nro lic
				datos[1] = row.getCell(1) != null ? row.getCell(1).getStringCellValue() : "";// tipo
				datos[2] = row.getCell(2) != null ? row.getCell(2).getStringCellValue() : null;//fecha
				datos[3] = "0";// mes
				datos[4] = "0";// año
				datos[5] = row.getCell(3) != null ? row.getCell(3).getStringCellValue() : "";// unidad compra
				datos[6] = row.getCell(4) != null ? row.getCell(4).getStringCellValue() : "";// razon social comp
				datos[7] = row.getCell(5) != null ? row.getCell(5).getStringCellValue() : "";// rut comprador
//				datos[8] = row.getCell(6) != null ? row.getCell(6).getStringCellValue() : "";// comprador reducido
				datos[8] = row.getCell(7) != null ? row.getCell(7).getStringCellValue() : "";// comuna
				datos[9] = row.getCell(8) != null ? row.getCell(8).getStringCellValue() : "";// region
				datos[10] = row.getCell(9) != null ? row.getCell(9).getStringCellValue() : "";// segmento
				datos[11] = row.getCell(10) != null ? row.getCell(10).getStringCellValue() : "";// item
				datos[12] = row.getCell(11) != null ? row.getCell(11).getStringCellValue() : "";// producto
				datos[13] = row.getCell(12) != null ? row.getCell(12).getStringCellValue() : "";// esp. comprador
				datos[14] = row.getCell(13) != null ? row.getCell(13).getStringCellValue() : "";// rut proveedor
				datos[15] = row.getCell(14) != null ? row.getCell(14).getStringCellValue() : "";// prov estandarizado
				datos[16] = row.getCell(15) != null ? row.getCell(15).getStringCellValue() : "";// razon soc prov
//				datos[18] = row.getCell(16) != null ? row.getCell(16).getStringCellValue() : "";// proveedor
				datos[17] = row.getCell(17) != null ? row.getCell(17).getStringCellValue() : "";// prov asociado
				datos[18] = row.getCell(18) != null ? row.getCell(18).getStringCellValue() : "";// esp proveedor
				datos[19] = row.getCell(19) != null ? row.getCell(19).getStringCellValue() : "";// monto unitario
				datos[20] = row.getCell(20) != null ? row.getCell(20).getStringCellValue() : "";// prec corregido
//				datos[23] = row.getCell(21) != null ? row.getCell(21).getStringCellValue() : "";// factor
				datos[21] = row.getCell(22) != null ? row.getCell(22).getStringCellValue() : "";// cantidad
				datos[22] = row.getCell(23) != null ? row.getCell(23).getStringCellValue() : "";// cantidad corregida
				datos[23] = row.getCell(24) != null ? row.getCell(24).getStringCellValue() : "";// valor total
				datos[24] = row.getCell(25) != null ? row.getCell(25).getStringCellValue() : "";// estado
				datos[25] = row.getCell(26) != null ? row.getCell(26).getStringCellValue() : "";// observacion
				datos[26] = row.getCell(27) != null ? row.getCell(27).getStringCellValue() : "";// mercado
				datos[27] = row.getCell(28) != null ? row.getCell(28).getStringCellValue() : "";// categoria
				datos[28] = row.getCell(29) != null ? row.getCell(29).getStringCellValue() : "";// subcategoria1
				datos[29] = row.getCell(30) != null ? row.getCell(30).getStringCellValue() : "";// subcategoria2
				datos[30] = row.getCell(31) != null ? row.getCell(31).getStringCellValue() : "";// subcategoria3
				datos[31] = row.getCell(32) != null ? row.getCell(32).getStringCellValue() : "";// subcategoria4
				datos[32] = row.getCell(33) != null ? row.getCell(33).getStringCellValue() : "";// subcategoria5
				datos[33] = row.getCell(34) != null ? row.getCell(34).getStringCellValue() : "";// subcategoria6
				datos[34] = row.getCell(35) != null ? row.getCell(35).getStringCellValue() : "";// subcategoria7
				datos[35] = row.getCell(36) != null ? row.getCell(36).getStringCellValue() : "";// subcategoria8

				try {
					stmt.setString(1, datos[1]);//tipo
					stmt.setString(2, datos[2]);//fecha adjudicacion
					stmt.setString(3, datos[3]);//mes
					stmt.setString(4, datos[4]);//anho
					stmt.setString(5, datos[4]);
					stmt.setString(6, datos[4]);
					stmt.setString(7, datos[5]);
					stmt.setString(8, datos[6]);
					stmt.setString(9, datos[7]);
					stmt.setString(10, datos[8]);
					stmt.setString(11, datos[9]);
					stmt.setString(12, datos[10]);
					stmt.setString(13, datos[11]);
					stmt.setString(14, datos[12]);
					stmt.setString(15, datos[13]);
					stmt.setString(16, datos[14]);
					stmt.setString(17, datos[15]);
					stmt.setString(18, datos[16]);
					stmt.setString(19, datos[17]);
					stmt.setString(20, datos[18]);
					stmt.setString(21, datos[19]);
					stmt.setString(22, datos[20]);
					stmt.setString(23, datos[21]);
					stmt.setString(24, datos[22]);
					stmt.setString(25, datos[23]);
					stmt.setString(26, datos[24]);
					stmt.setString(27, datos[25]);
					stmt.setString(28, datos[26]);
					stmt.setString(29, datos[27]);
					stmt.setString(30, datos[28]);
					stmt.setString(31, datos[29]);
					stmt.setString(32, datos[30]);
					stmt.setString(33, datos[31]);
					stmt.setString(34, datos[32]);
					stmt.setString(35, datos[33]);
					stmt.setString(36, datos[34]);
					stmt.setString(37, datos[35]);

					stmt.addBatch();
					ix++;

					if (ix % 500 == 0) {
						Utils.printOrdDeb("[Mercado: " + mercado + " - Registro: " + ix + "] ejecutando batch....");
						stmt.executeBatch();
					}
				} catch (Exception e) {
					System.out.print(ix+": "+datos[26] + " | " + datos[27]+ " | " + " | " + datos[28]);
					Utils.printOrdErr("Error1: " + e.getMessage());
				}

			}
			Utils.printOrdDeb("[Mercado: " + mercado + " - Registro: " + ix + "] ejecutando batch....");
			stmt.executeBatch(); // Execute every 1000 items.

			Date fin = new Date();
			double secs = fin.getTime() - ini.getTime();
			double tiempo_total = secs / 60000.0;
			Utils.printOrdErr("Inserción Finalizada (Tiempo: " + tiempo_total + " min)");
			
			FileOutputStream out;

			out = new FileOutputStream(new File(xlsxName));
			workbook.write(out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			Utils.printOrdErr("Error Cargando Excel: " + e.getMessage());
		} finally {
			DBCnx.close(stmt);
			DBCnx.close(cnx);
		}
	}

	private static void validar_datos(int row_hoja_datos, int fila, String origenOC, String RutComp, String RazComp, String Comuna,
			String RutProv, String ProvAsoc, String Mercado, String Categoria, String SubCat1, String SubCat2,
			String SubCat3, String SubCat4, String SubCat5, String SubCat6, String SubCat7, String SubCat8,
			XSSFWorkbook workbook) {

		
	}
	
	public static boolean isValidDate(String inDate) {
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    dateFormat.setLenient(false);
	    try {
	      dateFormat.parse(inDate.trim());
	    } catch (ParseException pe) {
	      return false;
	    }
	    return true;
	  }
	
	private static int validarFormatos(int row_hoja_formatos, int fila, String nroOrden, String cant1, String cant2, String pre1_clp,
			String pre2_clp, String pre1_usd, String pre2_usd, String total_clp, String total_usd, String fecha,
			XSSFSheet hoja_formatos) {

		boolean linea_error = false;
		SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat date_format2 = new SimpleDateFormat("yyyy/MM/dd");
		
		XSSFRow row;
		XSSFCell cell;
		
		row = hoja_formatos.createRow((short) row_hoja_formatos);

		try {
			double cantidad = Double.parseDouble(cant1);
		} catch (Exception e) {
			linea_error = true;
			
			String val = cant1.isEmpty() ? "Vacío" : cant1;
			cell = (XSSFCell) row.createCell((short) 2);
			cell.setCellValue(val);
		}
		try {
			double cantidad2 = Double.parseDouble(cant2);
		} catch (Exception e) {
			linea_error = true;
			
			String val = cant2.isEmpty() ? "Vacío" : cant2;
			cell = (XSSFCell) row.createCell((short) 3);
			cell.setCellValue(val);
		}
		try {
			double precio1_clp = Double.parseDouble(pre1_clp);
		} catch (Exception e) {
			linea_error = true;
			
			String val = pre1_clp.isEmpty() ? "Vacío" : pre1_clp;
			cell = (XSSFCell) row.createCell((short) 4);
			cell.setCellValue(val);
		}
		try {
			double precio2_clp = Double.parseDouble(pre2_clp);
		} catch (Exception e) {
			linea_error = true;
			
			String val = pre2_clp.isEmpty() ? "Vacío" : pre2_clp;
			cell = (XSSFCell) row.createCell((short) 5);
			cell.setCellValue(val);
		}
		try {
			double precio1_usd = Double.parseDouble(pre1_usd);
		} catch (Exception e) {
			linea_error = true;
			
			String val = pre1_usd.isEmpty() ? "Vacío" : pre1_usd;
			cell = (XSSFCell) row.createCell((short) 6);
			cell.setCellValue(val);
		}
		try {
			double precio2_usd = Double.parseDouble(pre2_usd);
		} catch (Exception e) {
			linea_error = true;
			
			String val = pre2_usd.isEmpty() ? "Vacío" : pre2_usd;
			cell = (XSSFCell) row.createCell((short) 7);
			cell.setCellValue(val);
		}
		try {
			double vtotal_clp = Double.parseDouble(total_clp);
		} catch (Exception e) {
			linea_error = true;
			
			String val = total_clp.isEmpty() ? "Vacío" : total_clp;
			cell = (XSSFCell) row.createCell((short) 8);
			cell.setCellValue(val);
		}
		try {
			double vtotal_usd = Double.parseDouble(total_usd);
		} catch (Exception e) {
			linea_error = true;
			
			String val = total_usd.isEmpty() ? "Vacío" : total_usd;
			cell = (XSSFCell) row.createCell((short) 9);
			cell.setCellValue(val);
		}

		boolean error_fecha = false, error_fecha2 = false;
		try {
			Date date = date_format.parse(fecha);
		} catch (Exception e) {
			error_fecha = true;
			// error += "Fecha: " + fecha + "\n";
		}

		try {
			Date date = date_format2.parse(fecha);
		} catch (Exception e) {
			error_fecha2 = true;
			// error += "Fecha: " + fecha + "\n";
		}

		if (error_fecha == true && error_fecha2 == true) {
			
			String val = fecha.isEmpty() ? "Vacío" : fecha;
			
			cell = (XSSFCell) row.createCell((short) 10);
			cell.setCellValue(val);
		}
		
		if(linea_error){
			cell = (XSSFCell) row.createCell((short) 0);
			cell.setCellValue(fila);
			
			cell = (XSSFCell) row.createCell((short) 1);
			cell.setCellValue(nroOrden);
			
			row_hoja_formatos++;;
		}
		
		return row_hoja_formatos;
	}
	
	public static void XLSXheaderFormatos(Row row_header, XSSFWorkbook wb) {

		XSSFCell cell;
		
		cell = (XSSFCell) row_header.createCell((short) 0);
		cell.setCellValue("Fila");

		cell = (XSSFCell) row_header.createCell((short) 1);
		cell.setCellValue("Nro Orden");
		
		cell = (XSSFCell) row_header.createCell((short) 2);
		cell.setCellValue("Cantidad 1");
		
		cell = (XSSFCell) row_header.createCell((short) 3);
		cell.setCellValue("Cantidad 2");
		
		cell = (XSSFCell) row_header.createCell((short) 4);
		cell.setCellValue("Precio1 CLP");
		
		cell = (XSSFCell) row_header.createCell((short) 5);
		cell.setCellValue("Precio2 CLP");
		
		cell = (XSSFCell) row_header.createCell((short) 6);
		cell.setCellValue("Precio1 USD");
		
		cell = (XSSFCell) row_header.createCell((short) 7);
		cell.setCellValue("Precio2 USD");
		
		cell = (XSSFCell) row_header.createCell((short) 8);
		cell.setCellValue("Total CLP");
		
		cell = (XSSFCell) row_header.createCell((short) 9);
		cell.setCellValue("Total USD");
		
		cell = (XSSFCell) row_header.createCell((short) 10);
		cell.setCellValue("Fecha");

	}
}