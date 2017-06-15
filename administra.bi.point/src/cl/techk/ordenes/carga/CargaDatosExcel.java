package cl.techk.ordenes.carga;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
import cl.techk.ext.utils.Utils;

/**
 * Servlet implementation class CargaDatosExcel
 */
@WebServlet("/CargaDatosExcel")
public class CargaDatosExcel extends HttpServlet {
	
	// version 1.1
	// last review: 23-05-2016
	// last fixes:
	// Modificadas las funciones de validaciones para que generen los reportes en xlsx y no txt.
	
	private static final long serialVersionUID = 1L;
	static ExecutorService ex = null;
	private static String bdd_name = "";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public CargaDatosExcel() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 *  @param
	 *  tipo: Corresponde a si se van a cargar datos de adjudicadas o de órdenes.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String mercado = "", estado = "";
		if (request.getParameter("mercado") != null && request.getParameter("estado") != null) {
			mercado = request.getParameter("mercado");
			estado = request.getParameter("estado");

			switch (estado) {
			case "prod":
				bdd_name = "point_ordenes";
				break;
			case "stage":
				bdd_name = "stage_point_ordenes";
				break;
			}
			CargaDatosPoint.carga(mercado, "ordenes_mes_act", bdd_name, 59);
		}
	}

	public static void carga(String mercado) {
		Connection cnx = null;
		PreparedStatement stmt = null;

		try {

			String fileName = "/Validaciones/Validacion_Formatos_" + mercado + ".txt";
			String xlsxName = "/Validaciones/Validacion_Formatos_" + mercado + ".xlsx";
			// Utils.printOrdDeb("Archivo: " + fileName);
			FileWriter fileWriter = new FileWriter(fileName);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			XSSFWorkbook workbook = new XSSFWorkbook();
			
			XSSFSheet hoja_formatos = workbook.createSheet("Revisión Formatos");
			
			XSSFRow row_excel;
			
			row_excel = hoja_formatos.createRow((short) 0);
			XLSXheaderFormatos(row_excel, workbook);
			
			int row_hoja_datos = 1, row_hoja_formatos = 1;

			cnx = DBCnx.conexion();

			String nombre_archivo = "/CargaMercados/carga_oc_" + mercado + ".xlsx";
			Utils.printOrdDeb("Archivo Mercado:" + nombre_archivo);

			String select = "INSERT INTO " + bdd_name + ".`ordenes_mes_act`"
					+ "(`numeroOrden`,`estadoOrden`,`fechaEnvio`,`mes`,`anho`,`nombreOrden`,`origenOrden`,"
					+ "`origenOCCorregido`,`numeroLicitacion`,`rutComprador`,`unidadCompra`,"
					+ "`unidadCompraEstandarizada`,`razonSocialComprador`,`razonSocialCompradorEstandarizada`,"
					+ "`comuna`,`region`,`segmentoComprador`,`segmentoCompradorMercado`,`comunaFacturacion`,"
					+ "`rutProveedor`,`proveedor`,`razonSocialProveedor`,`razonSocialProveedorEstandarizada`,"
					+ "`proveedorAsociado`,`codigoONU`,`productoServicio`,`cantidad`,`cantidadCorregida1`,"
					+ "`cantidadCorregida2`,`unidadMedida`,`unidadMedidaCorregida1`,`unidadMedidaCorregida2`,"
					+ "`descripcionProductoConsolidada`,`especificacionComprador`,`especificacionProveedor`,"
					+ "`moneda`,`precioUnitario`,`precioCorregido1`,`precioCorregido2`,`precioUnitarioDolar`,"
					+ "`precioCorregido1Dolar`,`precioCorregido2Dolar`,`descuentos`,`descuentosTotal`,`cargos`,"
					+ "`cargoTotal`,`totalUnitario`,`valorTotal`,`valorTotalDolar`,`mercado`,`categoria`,"
					+ "`subcategoria1`,`subcategoria2`,`subcategoria3`,`subcategoria4`,`subcategoria5`,"
					+ "`subcategoria6`,`subcategoria7`,`subcategoria8`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
					+ "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

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

			// SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss");
			Date ini = new Date();
			// Utils.printOrdDeb("Init query filtros: " + ft.format(dNow));
			for (Row row : reader) {
				Arrays.fill(datos, "");
				// Utils.printOrdDeb("idx: " + ix);
				if (ix == 0) {
					ix++;
					continue;
				}

				datos[0] = row.getCell(0) != null ? row.getCell(0).getStringCellValue() : ""; // nro
																								// orden
				datos[1] = row.getCell(1) != null ? row.getCell(1).getStringCellValue() : "";// estado

				datos[2] = row.getCell(2) != null ? row.getCell(2).getStringCellValue() : null;// fecha
			
//				System.out.println(datos[2]);
				datos[3] = "0";// mes
				datos[4] = "0";// año
				datos[5] = row.getCell(5) != null ? row.getCell(5).getStringCellValue() : "";// nombreorden
				datos[6] = row.getCell(6) != null ? row.getCell(6).getStringCellValue() : "";// origen
																								// OC
				datos[7] = row.getCell(7) != null ? row.getCell(7).getStringCellValue() : "";// origen
																								// OC
																								// corregido
				// validar_dato(String.valueOf(datos[7]), "origenOC",
				// "filtro_origenoc",bufferedWriter);
				datos[8] = row.getCell(8) != null ? row.getCell(8).getStringCellValue() : "";// n°
																								// licitacion
				datos[9] = row.getCell(9) != null ? row.getCell(9).getStringCellValue() : "";// rutComprador
				datos[10] = row.getCell(10) != null ? row.getCell(10).getStringCellValue() : "";// unidad
																								// Compra
				datos[11] = row.getCell(11) != null ? row.getCell(11).getStringCellValue() : "";// Unidad
																								// Compra
																								// Est
				// validar_dato(String.valueOf(datos[11]), "unidad_compra",
				// "filtro_unidad_compra",bufferedWriter);
				datos[12] = row.getCell(12) != null ? row.getCell(12).getStringCellValue() : "";// Razon
																								// Soc
																								// Comprador
				datos[13] = row.getCell(13) != null ? row.getCell(13).getStringCellValue() : "";// Razon
																								// Soc
																								// Comprado
																								// Est
				// validar_dato(String.valueOf(datos[13]),
				// "razonSocialComprador",
				// "filtro_razonsocialcomprador",bufferedWriter);
				datos[14] = row.getCell(14) != null ? row.getCell(14).getStringCellValue() : "";// comuna
				datos[15] = row.getCell(15) != null ? row.getCell(15).getStringCellValue() : "";// region
				datos[16] = row.getCell(16) != null ? row.getCell(16).getStringCellValue() : "";// segmento
																								// Comprador
				datos[17] = row.getCell(17) != null ? row.getCell(17).getStringCellValue() : "";// Segmento
																								// Comprador
																								// Mercado
				datos[18] = row.getCell(18) != null ? row.getCell(18).getStringCellValue() : "";// comuna
																								// facturacion
				datos[19] = row.getCell(19) != null ? row.getCell(19).getStringCellValue() : "";// rutProveedor
				datos[20] = row.getCell(20) != null ? row.getCell(20).getStringCellValue() : "";// proveedor
				datos[21] = row.getCell(21) != null ? row.getCell(21).getStringCellValue() : "";// razon
																								// soc
																								// Prov
				// validar_dato(String.valueOf(datos[21]),
				// "razonSocialProveedor",
				// "filtro_razonsocialproveedor",bufferedWriter);
				datos[22] = row.getCell(22) != null ? row.getCell(22).getStringCellValue() : "";// razon
																								// Social
																								// Prov
																								// Est
				datos[23] = row.getCell(23) != null ? row.getCell(23).getStringCellValue() : "";// proveedor
																								// asociado
				// validar_dato(String.valueOf(datos[23]), "proveedor_asociado",
				// "filtro_proveedor_asociado",bufferedWriter);
				datos[24] = row.getCell(24) != null ? row.getCell(24).getStringCellValue() : "";// codigo
																								// ONU
				datos[25] = row.getCell(25) != null ? row.getCell(25).getStringCellValue() : "";// prod
																								// Servcio
				datos[26] = row.getCell(26) != null ? row.getCell(26).getStringCellValue() : "";// cantidad
				datos[27] = row.getCell(27) != null
						? String.valueOf(row.getCell(27).getStringCellValue().replace(",", ".")) : "";// Cant
				// Corr
				// 1
				datos[28] = row.getCell(28) != null
						? String.valueOf(row.getCell(28).getStringCellValue().replace(",", ".")) : "";// Cant
				// Corr
				// 2
				datos[29] = row.getCell(29) != null ? row.getCell(29).getStringCellValue() : "";// Unidad
																								// Medida
				datos[30] = row.getCell(30) != null ? row.getCell(30).getStringCellValue() : "";// Medida
																								// Corr1
				datos[31] = row.getCell(31) != null ? row.getCell(31).getStringCellValue() : "";// Medida
																								// Corr2
				datos[32] = row.getCell(32) != null ? row.getCell(32).getStringCellValue() : "";// Desc
				datos[32] = datos[32].length() > 255 ? datos[32].substring(0, 254) : datos[32];// Consolidada

				datos[33] = row.getCell(33) != null ? row.getCell(33).getStringCellValue() : "";// Especif
				datos[33] = datos[33].length() > 255 ? datos[33].substring(0, 254) : datos[33];// Comprador

				datos[34] = row.getCell(34) != null ? row.getCell(34).getStringCellValue() : "";// Especif
				datos[34] = datos[34].length() > 254 ? datos[34].substring(0, 254) : datos[34];// Proveedor

				datos[35] = row.getCell(35) != null ? row.getCell(35).getStringCellValue() : "";// moneda

				datos[36] = row.getCell(36) != null
						? String.valueOf(row.getCell(36).getStringCellValue().replace(",", ".")) : "";// prec
				// Unit
				datos[37] = row.getCell(37) != null
						? String.valueOf(row.getCell(37).getStringCellValue().replace(",", ".")) : "";// prec
				// Corr1
				datos[38] = row.getCell(38) != null
						? String.valueOf(row.getCell(38).getStringCellValue().replace(",", ".")) : "";// prec
				// Corr2
				datos[39] = row.getCell(39) != null
						? String.valueOf(row.getCell(39).getStringCellValue().replace(",", ".")) : "";// Prec
				// Unit
				// USD
				datos[40] = row.getCell(40) != null
						? String.valueOf(row.getCell(40).getStringCellValue().replace(",", ".")) : "";// Prec
				// Corr1
				// USD
				datos[41] = row.getCell(41) != null
						? String.valueOf(row.getCell(41).getStringCellValue().replace(",", ".")) : "";// Prec
				// Corr2
				// USD
				datos[42] = row.getCell(42) != null ? row.getCell(42).getStringCellValue() : "";// descuento
				datos[43] = row.getCell(43) != null ? row.getCell(43).getStringCellValue() : "";// dcto
																								// Total
				datos[44] = row.getCell(44) != null ? row.getCell(44).getStringCellValue() : "";// cargos
				datos[45] = row.getCell(45) != null ? row.getCell(45).getStringCellValue() : "";// cargo
																								// Total
				datos[46] = row.getCell(46) != null
						? String.valueOf(row.getCell(46).getStringCellValue().replace(",", ".")) : "";// Total
																										// unit
				datos[47] = row.getCell(47) != null
						? String.valueOf(row.getCell(47).getStringCellValue().replace(",", ".")) : "";// valor
				// Total
				// CLP
				datos[48] = row.getCell(48) != null
						? String.valueOf(row.getCell(48).getStringCellValue().replace(",", ".")) : "";// valorTotal
				// USD
				datos[49] = row.getCell(49) != null ? row.getCell(49).getStringCellValue() : "";// mercado
				// validar_dato(String.valueOf(datos[49]), "mercado",
				// "filtro_mercado",bufferedWriter);
				datos[50] = row.getCell(50) != null ? row.getCell(50).getStringCellValue() : "";// categoria
				// validar_dato(String.valueOf(datos[50]), "categoria",
				// "filtro_categoria",bufferedWriter);
				datos[51] = row.getCell(51) != null ? row.getCell(51).getStringCellValue() : "";// subcategoria1
				// validar_dato(String.valueOf(datos[51]), "subCategoria",
				// "filtro_subcategoria1",bufferedWriter);
				datos[52] = row.getCell(52) != null ? row.getCell(52).getStringCellValue() : "";// subcategoria2
				// validar_dato(String.valueOf(datos[52]), "subCategoria",
				// "filtro_subcategoria2",bufferedWriter);
				datos[53] = row.getCell(53) != null ? row.getCell(53).getStringCellValue() : "";// subcategoria3
				// validar_dato(String.valueOf(datos[53]), "subCategoria",
				// "filtro_subcategoria3",bufferedWriter);
				datos[54] = row.getCell(54) != null ? row.getCell(54).getStringCellValue() : "";// subcategoria4
				// validar_dato(String.valueOf(datos[54]), "subCategoria",
				// "filtro_subcategoria4",bufferedWriter);
				datos[55] = row.getCell(55) != null ? row.getCell(55).getStringCellValue() : "";// subcategoria5
				// validar_dato(String.valueOf(datos[55]), "subCategoria",
				// "filtro_subcategoria5",bufferedWriter);
				datos[56] = row.getCell(56) != null ? row.getCell(56).getStringCellValue() : "";// subcategoria6
				// validar_dato(String.valueOf(datos[56]), "subCategoria",
				// "filtro_subcategoria6",bufferedWriter);
				datos[57] = row.getCell(57) != null ? row.getCell(57).getStringCellValue() : "";// subcategoria7
				// validar_dato(String.valueOf(datos[57]), "subCategoria",
				// "filtro_subcategoria7",bufferedWriter);
				datos[58] = row.getCell(58) != null ? row.getCell(58).getStringCellValue() : "";// subcategoria8
				// validar_dato(String.valueOf(datos[58]), "subCategoria",
				// "filtro_subcategoria8",bufferedWriter);

				//***FALTA POR DESARROLLAR****//
				validar_datos(row_hoja_datos, ix, datos[7], datos[9], datos[13], datos[14], datos[19], datos[23], datos[49], datos[50],
						datos[51], datos[52], datos[53], datos[54], datos[55], datos[56], datos[57], datos[58], workbook);

//				validarFormatos(row_hoja_formatos, ix, datos[0], datos[27], datos[28], datos[37], datos[38], datos[40], datos[41],
//						datos[47], datos[48], datos[2], bufferedWriter);
				
				//***FALTAN AFINAR DETALLES EN LAS COMPRONACIONES***//
				row_hoja_formatos = validarFormatos(row_hoja_formatos, ix, datos[0], datos[27], datos[28], datos[37], datos[38], datos[40], datos[41],
						datos[47], datos[48], datos[2], hoja_formatos);
				try {
					// stmt = cnx.createStatement();|
//					System.out.println(ix+": "+datos[26] + " | " + datos[27]+ " | " + " | " + datos[28]);
					// stmt = cnx.prepareStatement(select);
					stmt.setString(1, datos[0].toString());
					stmt.setString(2, datos[1].toString());
					stmt.setString(3, datos[2].toString());
					stmt.setInt(4, Integer.parseInt(datos[3].toString()));
					stmt.setInt(5, Integer.parseInt(datos[4].toString()));
					stmt.setString(6, datos[5].toString());
					stmt.setString(7, datos[6].toString());
					stmt.setString(8, datos[7].toString());
					stmt.setString(9, datos[8].toString());
					stmt.setString(10, datos[9].toString());
					stmt.setString(11, datos[10].toString());
					stmt.setString(12, datos[11].toString());
					stmt.setString(13, datos[12].toString());
					stmt.setString(14, datos[13].toString());
					stmt.setString(15, datos[14].toString());
					stmt.setString(16, datos[15].toString());
					stmt.setString(17, datos[16].toString());
					stmt.setString(18, datos[17].toString());
					stmt.setString(19, datos[18].toString());
					stmt.setString(20, datos[19].toString());
					stmt.setString(21, datos[20].toString());
					stmt.setString(22, datos[21].toString());
					stmt.setString(23, datos[22].toString());
					stmt.setString(24, datos[23].toString());
					stmt.setString(25, datos[24].toString());
					stmt.setString(26, datos[25].toString());
					stmt.setString(27, datos[26].toString());
					stmt.setString(28, datos[27]);
					stmt.setString(29, datos[28]);
					stmt.setString(30, datos[29]);
					stmt.setString(31, datos[30].toString());
					stmt.setString(32, datos[31].toString());
					stmt.setString(33, datos[32].toString());
					stmt.setString(34, datos[33].toString());
					stmt.setString(35, datos[34].toString());
					stmt.setString(36, datos[35].toString());
					stmt.setString(37, datos[36].toString());
					stmt.setString(38, datos[37].toString());
					stmt.setString(39, datos[38].toString());
					stmt.setString(40, datos[39].toString());
					stmt.setString(41, datos[40].toString());
					stmt.setString(42, datos[41].toString());
					stmt.setString(43, datos[42].toString());
					stmt.setString(44, datos[43].toString());
					stmt.setString(45, datos[44].toString());
					stmt.setString(46, datos[45].toString());
					stmt.setString(47, datos[46].toString());
					stmt.setString(48, datos[47].toString());
					stmt.setString(49, datos[48].toString());
					stmt.setString(50, datos[49].toString());
					stmt.setString(51, datos[50].toString());
					stmt.setString(52, datos[51].toString());
					stmt.setString(53, datos[52].toString());
					stmt.setString(54, datos[53].toString());
					stmt.setString(55, datos[54].toString());
					stmt.setString(56, datos[55].toString());
					stmt.setString(57, datos[56].toString());
					stmt.setString(58, datos[57].toString());
					stmt.setString(59, datos[58].toString());

					stmt.addBatch();
					ix++;

					if (ix % 500 == 0) {
						Utils.printOrdDeb("[Mercado: " + mercado + " - Registro: " + ix + "] ejecutando batch....");
						stmt.executeBatch(); // Execute every 1000 items.
					}
					// stmt.executeUpdate();
				} catch (Exception e) {
					System.out.println(ix+" F:"+datos[2]);
					Utils.printOrdErr("Error1: " + e.getMessage());
				}

			}
			Utils.printOrdDeb("[Mercado: " + mercado + " - Registro: " + ix + "] ejecutando batch....");
			stmt.executeBatch(); // Execute every 1000 items.

			Date fin = new Date();
			double secs = fin.getTime() - ini.getTime();
			double tiempo_total = secs / 60000.0;
			Utils.printOrdErr("Inserción Finalizada (Tiempo: " + tiempo_total + " min)");

			bufferedWriter.close();
			fileWriter.close();
			
			FileOutputStream out;

			out = new FileOutputStream(new File(xlsxName));
			// Utils.printOrdDeb("fileoutputstream");
			workbook.write(out);
			// Utils.printOrdDeb("FIN");
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