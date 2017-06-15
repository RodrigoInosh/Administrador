package cl.techk.ext.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.monitorjbl.xlsx.StreamingReader;

import cl.techk.ext.utils.Utils;

public class CargaDatosPoint {

	public static void carga(String mercado, String bdd_table, String bdd_name, int cant_columnas) {
		Connection cnx = null;
		PreparedStatement stmt = null;
		String nombre_archivo_validacion = bdd_table.equals("ordenes_mes_act")
				? "Ordenes_Validacion_Formatos_" + mercado + ".xlsx"
				: "Adjudicadas_Validacion_Formatos_" + mercado + ".xlsx";

		String nombre_archivo = bdd_table.equals("ordenes_mes_act") ? "/CargaMercados/carga_oc_" + mercado + ".xlsx"
				: "/CargaMercados/carga_adjudicadas.xlsx";
		try {

			String xlsxName = "/Validaciones/" + nombre_archivo_validacion;
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet hoja_formatos = workbook.createSheet("Revisión Formatos");

			XSSFRow row_excel;

			row_excel = hoja_formatos.createRow((short) 0);
			XLSXheaderFormatos(row_excel, workbook);

			int row_hoja_datos = 1, row_hoja_formatos = 1;

			cnx = DBCnx.conexion();

			// String nombre_archivo = "/CargaMercados/carga_oc_" + mercado +
			// ".xlsx";
			Utils.printOrdDeb("Archivo Mercado:" + nombre_archivo);

			String select = "INSERT INTO " + bdd_name + "." + bdd_table + " (";

			String values = " VALUES (";

			InputStream is = new FileInputStream(new File(nombre_archivo));
			StreamingReader reader = StreamingReader.builder().rowCacheSize(100).bufferSize(4096).sheetIndex(0)
					.read(is);

			int fila_actual = 0;
			// Array para almacenar los datos de las filas
			String datos[] = new String[cant_columnas];
			for (int i = 0; i < cant_columnas; i++) {
				datos[i] = new String();
			}

			Date ini = new Date();
			for (Row row : reader) {
				Arrays.fill(datos, "");
				// Si es la primera fila, entonces obtengo el nombre de las
				// columnas
				if (fila_actual == 0) {
					fila_actual++;

					int ciclo = 0;
					// Obtengo el nombre de las columnas para armar el insert.
					while (ciclo < cant_columnas) {
						select += row.getCell(ciclo).getStringCellValue() + ",";
						values += "?,";
						ciclo++;
					}
					select += "created_at)";
					values += "now())";

					select = select + values;

					stmt = cnx.prepareStatement(select);
					continue;
				}

				// Obtengo y guardo los valores de las filas del excel
				for (int ciclo = 0; ciclo < cant_columnas; ciclo++) {
					datos[ciclo] = row.getCell(ciclo) != null ? row.getCell(ciclo).getStringCellValue() : "";
				}
				// Valido el formato de los campos relevantes (números y fechas)

				datos = formatNumeros(datos, bdd_table);
				row_hoja_formatos = validar(datos, bdd_table, row_hoja_formatos, hoja_formatos, fila_actual);

				// ***FALTA POR DESARROLLAR****//
				// validar_datos(row_hoja_datos, ix, datos[7], datos[9],
				// datos[13], datos[14], datos[19], datos[23], datos[49],
				// datos[50],
				// datos[51], datos[52], datos[53], datos[54], datos[55],
				// datos[56], datos[57], datos[58], workbook);

				try {
					// Agrego los valores al preparedStatement
					for (int ciclo = 0; ciclo < cant_columnas; ciclo++) {
						stmt.setString(ciclo + 1, datos[ciclo]);
					}
					stmt.addBatch();

					if (fila_actual % 100 == 0) {
						Utils.printOrdDeb(fila_actual + " ejecutando batch....");
						stmt.executeBatch(); // Execute every 1000 items.
					}
					fila_actual++;
				} catch (Exception e) {
//					Utils.printOrdErr(fila_actual + ": " + datos[2]);
					e.printStackTrace();
					break;
				}

			}
			Utils.printOrdDeb("[Mercado: " + mercado + " - Registro: " + (fila_actual - 1) + "] ejecutando batch....");
			stmt.executeBatch(); // Execute every 1000 items.

			Date fin = new Date();
			double secs = fin.getTime() - ini.getTime();
			double tiempo_total = secs / 60000.0;
			Utils.printOrdErr("Inserción Finalizada (Tiempo: " + tiempo_total + " min)");

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

	private static String[] formatNumeros(String[] datos, String bdd_table) {
		int cant_filas = datos.length;
		String[] datos_format = new String[cant_filas];

		System.arraycopy(datos, 0, datos_format, 0, cant_filas);
		if (bdd_table.equals("ordenes_mes_act")) {
			datos_format[3] = "0";
			datos_format[4] = "0";
			datos_format[27] = datos_format[27].replace(",", ".");
			datos_format[28] = datos_format[28].replace(",", ".");
			datos_format[36] = datos_format[36].replace(",", ".");
			datos_format[37] = datos_format[37].replace(",", ".");
			datos_format[38] = datos_format[38].replace(",", ".");
			datos_format[39] = datos_format[39].replace(",", ".");
			datos_format[40] = datos_format[40].replace(",", ".");
			datos_format[41] = datos_format[41].replace(",", ".");
			datos_format[47] = datos_format[47].replace(",", ".");
			datos_format[48] = datos_format[48].replace(",", ".");
		} else if (bdd_table.equals("adjudicadas_aux")) {
			
			datos_format[15] = datos[15].equals("") ? "0" : datos[15].replace(",", ".");//cantidad
			datos_format[16] = datos[16].equals("") ? "0" : datos[16].replace(",", ".");//cant corregida
			datos_format[20] = datos[20].equals("") ? "0" : datos[20].replace(",", ".");//precio unit
			datos_format[21] = datos[21].equals("") ? "0" : datos[21].replace(",", ".");//prec unit corregido
			datos_format[22] = datos[22].equals("") ? "0" : datos[22].replace(",", ".");//valor total
			
			datos_format[14] = datos_format[14].length() > 255 ? datos_format[14].substring(0, 254) : datos_format[14];
			datos_format[17] = datos_format[17].length() > 255 ? datos_format[17].substring(0, 254) : datos_format[17];
			datos_format[18] = datos_format[18].length() > 255 ? datos_format[18].substring(0, 254) : datos_format[18];
		}

		return datos_format;
	}

	private static int validar(String[] datos, String bdd_table, int row_hoja_formatos, XSSFSheet hoja_formatos,
			int fila_actual) {

		if (bdd_table.equals("ordenes_mes_act")) {
			row_hoja_formatos = validarFormatos(row_hoja_formatos, fila_actual, datos[0], datos[27], datos[28],
					datos[37], datos[38], datos[40], datos[41], datos[47], datos[48], datos[2], hoja_formatos);
		} else if (bdd_table.equals("adjudicadas_aux")) {
			row_hoja_formatos = validarFormatosAdjudicadas(row_hoja_formatos, fila_actual, datos[0], datos[15],
					datos[16], datos[20], datos[21], datos[22], datos[2], hoja_formatos);
		}

		return row_hoja_formatos;
	}

	private static int validarFormatos(int row_hoja_formatos, int fila, String nroOrden, String cant1, String cant2,
			String pre1_clp, String pre2_clp, String pre1_usd, String pre2_usd, String total_clp, String total_usd,
			String fecha, XSSFSheet hoja_formatos) {

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

		if (linea_error) {
			cell = (XSSFCell) row.createCell((short) 0);
			cell.setCellValue(fila);

			cell = (XSSFCell) row.createCell((short) 1);
			cell.setCellValue(nroOrden);

			row_hoja_formatos++;
			;
		}

		return row_hoja_formatos;
	}

	private static int validarFormatosAdjudicadas(int row_hoja_formatos, int fila, String nroOrden, String cant,
			String cant_cor, String prec, String prec_cor, String total, String fecha, XSSFSheet hoja_formatos) {

		boolean linea_error = false;
		SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat date_format2 = new SimpleDateFormat("yyyy/MM/dd");

		XSSFRow row;
		XSSFCell cell;

		row = hoja_formatos.createRow((short) row_hoja_formatos);

		try {
			cant = cant.isEmpty() ? "0" : cant;
			double cantidad = Double.parseDouble(cant);
		} catch (Exception e) {
			linea_error = true;
			cell = (XSSFCell) row.createCell((short) 2);
			cell.setCellValue(cant);
		}
		try {
			cant_cor = cant_cor.isEmpty() ? "0" : cant_cor;
			double cantidad2 = Double.parseDouble(cant_cor);
		} catch (Exception e) {
			linea_error = true;
			cell = (XSSFCell) row.createCell((short) 3);
			cell.setCellValue(cant_cor);
		}
		try {
			prec = prec.isEmpty() ? "0" : prec;
			double precio1_clp = Double.parseDouble(prec);
		} catch (Exception e) {
			linea_error = true;
			cell = (XSSFCell) row.createCell((short) 4);
			cell.setCellValue(prec);
		}
		try {
			prec_cor = prec_cor.isEmpty() ? "0" : prec_cor;
			double precio2_clp = Double.parseDouble(prec_cor);
		} catch (Exception e) {
			linea_error = true;
			cell = (XSSFCell) row.createCell((short) 5);
			cell.setCellValue(prec_cor);
		}
		try {
			total = total.isEmpty() ? "0" : total;
			double precio1_usd = Double.parseDouble(total);
		} catch (Exception e) {
			linea_error = true;
			cell = (XSSFCell) row.createCell((short) 6);
			cell.setCellValue(total);
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

		if (linea_error) {
			cell = (XSSFCell) row.createCell((short) 0);
			cell.setCellValue(fila);

			cell = (XSSFCell) row.createCell((short) 1);
			cell.setCellValue(nroOrden);

			row_hoja_formatos++;
		}

		return row_hoja_formatos;
	}
}
