package cl.techk.models;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.sql.rowset.CachedRowSet;

import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;

import cl.techk.ext.database.DBCnx;
import cl.techk.lib.CalendarUtils;
import cl.techk.lib.DataBaseUtils;
import cl.techk.lib.ExcelUtils;
import cl.techk.lib.FileUtils;

public class DictionaryExport {

    public static String generateXlsFile(int id_mercado) {

        String excel_file_path = FileUtils.TEMP_FILE_FOLDER + "dictionary_" + CalendarUtils.getActualDateInMilis()
                + ".xlsx";

        try {

            XSSFWorkbook workbook = new XSSFWorkbook();
            JSONObject market_data = getMarketData(id_mercado);
            CachedRowSet data_anexos = getDataAnexos(market_data);
            CachedRowSet data_rubros = getDataRubros(market_data);
            CachedRowSet data_search_1 = getDataSearch1(market_data);
            CachedRowSet data_search_2 = getDataSearch2(market_data);
            CachedRowSet data_search_3 = getDataSearch3(market_data);

            createSheet(workbook, data_rubros, "Rubros", new ExcelUtils.FormatType[] { ExcelUtils.FormatType.TEXT,
                    ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.DATE});
            createSheet(workbook, data_search_1, "B1", new ExcelUtils.FormatType[] { ExcelUtils.FormatType.TEXT,
                    ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.DATE});
            createSheet(workbook, data_search_2, "B2", new ExcelUtils.FormatType[] { ExcelUtils.FormatType.TEXT,
                    ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT, 
                    ExcelUtils.FormatType.DATE});
            createSheet(workbook, data_search_3, "B3", new ExcelUtils.FormatType[] { ExcelUtils.FormatType.TEXT,
                    ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT, 
                    ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.DATE});
            createSheet(workbook, data_anexos, "Anexos", new ExcelUtils.FormatType[] { ExcelUtils.FormatType.TEXT,
                    ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.DATE});

            ExcelUtils.setSheetColumnsSize(workbook.getSheet("Rubros"), new int[] { 400, 200, 100 });
            ExcelUtils.setSheetColumnsSize(workbook.getSheet("B1"), new int[] { 300, 200, 300, 100 });
            ExcelUtils.setSheetColumnsSize(workbook.getSheet("B2"), new int[] { 200, 200, 200, 300, 100 });
            ExcelUtils.setSheetColumnsSize(workbook.getSheet("B3"), new int[] { 200, 200, 200, 200, 300, 100 });
            ExcelUtils.setSheetColumnsSize(workbook.getSheet("Anexos"), new int[] { 150, 150, 100 });

            OutputStream outputStream = new FileOutputStream(new File(excel_file_path));
            workbook.write(outputStream);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return excel_file_path;
    }

    public static JSONObject getMarketData(int id_mercado) {

        JSONObject response = new JSONObject();
        Object[] query_get_market = {
                "select id,mercado,prefijo from " + DBCnx.db_clasification + ".mercados where id=?", id_mercado };
        CachedRowSet market_data = DataBaseUtils.exQuery(query_get_market);
        try {
            if (market_data.next()) {
                response.put("id", market_data.getString("id"));
                response.put("nombre", market_data.getString("mercado"));
                response.put("prefijo", market_data.getString("prefijo"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return response;
    }

    public static CachedRowSet getDataRubros(JSONObject market_data) {

        Object[] query_get_rubros = { "select rub_generico as 'Producto/Servicio o Generico', rub_rubro as 'Rubro', "
                + "DATE_FORMAT(  `fecha_cambio` ,  '%d-%m-%Y' ) as 'Fecha_Incorporacion' from " + DBCnx.db_clasification
                + ".rubros" + market_data.getString("prefijo") + "  order by rub_id asc" };

        return DataBaseUtils.exQuery(query_get_rubros);
    }

    public static CachedRowSet getDataSearch1(JSONObject market_data) {

        Object[] query_get_search1 = {
                "select keywords as 'Clave Busqueda',description as 'Busqueda 1',rubro as 'Rubro', "
                        + "DATE_FORMAT(  `fecha_cambio` ,  '%d-%m-%Y' ) as 'Fecha_Incorporacion' from "
                        + DBCnx.db_clasification + ".search_level01" + market_data.getString("prefijo")
                        + "  order by id asc" };

        return DataBaseUtils.exQuery(query_get_search1);
    }

    public static CachedRowSet getDataSearch2(JSONObject market_data) {

        Object[] query_get_search2 = {
                "select search_level01 as 'Busqueda 1',keywords as 'Clave Busqueda',description  as 'Busqueda 2',"
                        + "rubro as 'Rubro', DATE_FORMAT(  `fecha_cambio` ,  '%d-%m-%Y' ) as 'Fecha_Incorporacion' from "
                        + DBCnx.db_clasification + ".search_level02" + market_data.getString("prefijo")
                        + "  order by id asc" };

        return DataBaseUtils.exQuery(query_get_search2);
    }

    public static CachedRowSet getDataSearch3(JSONObject market_data) {

        Object[] query_get_search3 = {
                "select search_level01 as 'Busqueda 1',search_level02 as 'Busqueda 2',keywords as 'Clave Busqueda',"
                        + "description as 'Busqueda 3',rubro as 'Rubro',DATE_FORMAT(  `fecha_cambio` ,  '%d-%m-%Y' ) as 'Fecha_Incorporacion' from "
                        + DBCnx.db_clasification + ".search_level03" + market_data.getString("prefijo")
                        + "  order by id asc" };

        return DataBaseUtils.exQuery(query_get_search3);
    }

    public static CachedRowSet getDataAnexos(JSONObject market_data) {

        Object[] query_get_anexos_data = {
                "select keyword as 'Clave Busqueda',descripcion as 'Anexos',DATE_FORMAT(  `fecha_cambio` ,  '%d-%m-%Y' ) as 'Fecha_Incorporacion' from "
                        + DBCnx.db_clasification + ".anexos" + market_data.getString("prefijo") + "  order by id asc" };

        return DataBaseUtils.exQuery(query_get_anexos_data);
    }

    private static void createSheet(XSSFWorkbook workbook, CachedRowSet data, String sheet_name, ExcelUtils.FormatType[] formatos_columnas) {

        try {

            XSSFCellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setFontName("Calibri");
            font.setFontHeight((short) (11 * 20));
            style.setFont(font);

            XSSFSheet sheet = workbook.createSheet(sheet_name);
            int actual_row = 0;

            // header

            ResultSetMetaData resultSetMetaData = data.getMetaData();
            int numCols = resultSetMetaData.getColumnCount();
            XSSFRow row = sheet.createRow(actual_row);
            XSSFFont boldFont = workbook.createFont();
            boldFont.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
            for (int i = 0; i < numCols; i++) {
                String title = resultSetMetaData.getColumnLabel(i + 1);
                ExcelUtils.writeCell(row, i, title, ExcelUtils.FormatType.TEXT, boldFont, style, workbook);
            }
            actual_row++;

            // data

            while (data.next()) {
                row = sheet.createRow(actual_row++);
                row.setHeightInPoints(18);

                for (int i = 0; i < numCols; i++) {
                    try {
                        Object value = data.getObject(i + 1);
                        ExcelUtils.writeCell(row, i, value, formatos_columnas[i], null, style, workbook);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            setGeneralSheetConfigs(sheet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setGeneralSheetConfigs(XSSFSheet sheet_detalle) {

        // zoom inicial 75%
        sheet_detalle.setZoom(3, 4);
        sheet_detalle.setDefaultRowHeightInPoints(20);
    }
}
