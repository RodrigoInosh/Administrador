package cl.techk.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sql.rowset.CachedRowSet;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import com.monitorjbl.xlsx.StreamingReader;

public class ExcelUtils {

    final static int MAX_ROW_CACHE_SIZE = 100;
    final static int MAX_STREAMING_BUFFER_SIZE = 4096;
    public final static double ANCHO_COLUMNA = 36.571428571;
    public final static SimpleDateFormat CHILEAN_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

    public static void setSheetColumnsSize(XSSFSheet sheet, int[] sizes) {

        for (int x = 0; x < sizes.length; x++) {
            sheet.setColumnWidth(x, (int) (sizes[x] * ExcelUtils.ANCHO_COLUMNA));
        }
    }

    public static StreamingReader getFileStreamingReader(String file_directory, String sheet_name)
            throws FileNotFoundException {

        InputStream is = new FileInputStream(new File(file_directory));
        StreamingReader reader = StreamingReader.builder().rowCacheSize(MAX_ROW_CACHE_SIZE)
                .bufferSize(MAX_STREAMING_BUFFER_SIZE).sheetName(sheet_name).read(is);

        return reader;
    }

    public static FormatType getFormatType(Class _class) {
        if (_class == Integer.class || _class == Long.class) {
            return FormatType.INTEGER;
        } else if (_class == Float.class || _class == Double.class) {
            return FormatType.FLOAT;
        } else if (_class == Timestamp.class || _class == java.sql.Date.class) {
            return FormatType.DATE;
        } else if (_class == boolean.class) {
            return FormatType.BOOLEAN;
        } else {
            return FormatType.TEXT;
        }
    }

    public enum FormatType {
        TEXT, INTEGER, FLOAT, DATE, MONEY, PERCENTAGE, BOOLEAN
    }

    public static HSSFCellStyle getStyleHrTable(HSSFWorkbook workbook) {

        HSSFCellStyle style = workbook.createCellStyle();
        Font font_title = workbook.createFont();
        font_title.setBold(true);
        font_title.setColor(HSSFColor.WHITE.index);
        font_title.setFontName("Calibri");
        font_title.setFontHeight((short) (11 * 20));
        style.setFont(font_title);
        style.setFillBackgroundColor(HSSFColor.GREY_25_PERCENT.index);
        style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        return style;
    }

    public static void writeCell(XSSFRow row, int column, Object value, FormatType format_type, XSSFFont font,
            XSSFCellStyle style, XSSFWorkbook workbook) throws Exception {
        writeCell(row, column, value, format_type, null, font, style, workbook);
    }

    public static void writeCell(XSSFRow row, int col, Object value, FormatType format_type, Short bgColor,
            XSSFFont font, XSSFCellStyle style, XSSFWorkbook workbook) throws Exception {

        XSSFCell cell = (XSSFCell) CellUtil.createCell(row, col, null);
        if (value == null) {
            return;
        }
        if (style != null) {
            cell.setCellStyle(style);
        }
        switch (format_type) {
            case TEXT:
                cell.setCellValue(value.toString());
                break;
            case FLOAT:
                cell.setCellValue(((Number) value).doubleValue());
                CellUtil.setCellStyleProperty(cell, workbook, CellUtil.DATA_FORMAT,
                        HSSFDataFormat.getBuiltinFormat(("#,##0.00")));
                XSSFDataFormat format = workbook.createDataFormat();
                style.setDataFormat(format.getFormat("#,##0.0"));
                cell.setCellStyle(style);
                break;
            case MONEY:
                value = Double.valueOf(value.toString());
                cell.setCellValue(((Number) value).doubleValue());
                CellUtil.setCellStyleProperty(cell, workbook, CellUtil.DATA_FORMAT,
                        HSSFDataFormat.getBuiltinFormat(("#,##0")));
                break;
            case INTEGER:
                cell.setCellValue(((Number) value).intValue());
                break;
            case PERCENTAGE:
                cell.setCellValue(((Number) value).doubleValue());
                break;
            case BOOLEAN:
                int boolean_value = convertBooleanToInt(value);
                cell.setCellValue(boolean_value);
                break;
            case DATE:
                CreationHelper createHelper = workbook.getCreationHelper();
                style.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
                cell.setCellStyle(style);
                setCellValueToExport(value.toString(), workbook, cell);
                break;
        }
    }

    public static void setCellValueToExport(String date, XSSFWorkbook workbook, XSSFCell cell) throws ParseException {

        if ("00-00-0000".equals(date)) {
            cell.setCellValue("");
        } else {
            Date date_value = CHILEAN_DATE_FORMAT.parse(date);
            cell.setCellValue(date_value);
        }
    }

    public static void createSheet(XSSFWorkbook workbook, CachedRowSet data, String sheet_name,
            ExcelUtils.FormatType[] formatos_columnas) {

        try {

            Font font = getFont(workbook);
            XSSFCellStyle style = workbook.createCellStyle();
            XSSFCellStyle header_style = getHeadersStyle(workbook);
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
                ExcelUtils.writeCell(row, i, title, ExcelUtils.FormatType.TEXT, boldFont, header_style, workbook);
            }
            actual_row++;

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
            sizeColumnsConfigs(numCols, sheet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sizeColumnsConfigs(int numCols, XSSFSheet sheet) {

        for (int i = 0; i < numCols; i++) {
            sheet.autoSizeColumn((short) i);
        }
    }

    public static void createXlsxFile(JSONObject report_data) {

        try {
            String file_path = report_data.getString("file_path");
            String[] query_get_data = (String[]) report_data.get("query");
            String[] file_sheets_names = (String[]) report_data.get("sheets_names");
            ExcelUtils.FormatType[] format_type = (FormatType[]) report_data.get("format_types");

            XSSFWorkbook workbook = new XSSFWorkbook();
            int sheets_count = file_sheets_names.length;
            for (int ix = 0; ix < sheets_count; ix++) {
                CachedRowSet obtained_data = DataBaseUtils.exQuery(new Object[] { query_get_data[ix] });
                ExcelUtils.createSheet(workbook, obtained_data, file_sheets_names[ix], format_type);
            }

            OutputStream outputStream = new FileOutputStream(new File(file_path));
            workbook.write(outputStream);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setGeneralSheetConfigs(XSSFSheet sheet_detalle) {

        // zoom inicial 75%
        sheet_detalle.setZoom(3, 4);
        sheet_detalle.setDefaultRowHeightInPoints(20);
    }

    public static Font getFont(XSSFWorkbook workbook) {

        Font font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeight((short) (11 * 20));

        return font;
    }

    public static void HeadersXlsx(XSSFRow row, XSSFWorkbook wb, String column_data[]) {

        Font font = getLicitationsFont(wb, HSSFColor.WHITE.index, (short) 10);
        XSSFCellStyle style = getLicitationsCellStyle(wb, font, new XSSFColor(new java.awt.Color(90, 90, 90)));
        setCellValue(column_data, row, style);
    }

    public static Font getLicitationsFont(XSSFWorkbook wb, short font_color, short font_size) {

        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(font_color);
        font.setFontName("Arial");
        font.setFontHeightInPoints(font_size);

        return font;
    }

    public static XSSFCellStyle getHeadersStyle(XSSFWorkbook workbook) {

        XSSFCellStyle estilo = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(HSSFColor.WHITE.index);
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        estilo.setFont(font);
        estilo.setFillBackgroundColor(HSSFColor.GREEN.index);
        estilo.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);

        return estilo;
    }

    public static XSSFCellStyle getLicitationsCellStyle(XSSFWorkbook wb, Font font, XSSFColor foreground_color) {

        XSSFCellStyle style = wb.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(foreground_color);
        style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);

        return style;
    }

    public static void setCellValue(String[] cell_values, XSSFRow row, XSSFCellStyle style) {

        int data_length = cell_values.length;

        for (int ix = 0; ix < data_length; ix++) {
            XSSFCell cell = (XSSFCell) row.createCell((short) ix);
            cell.setCellValue(cell_values[ix]);
            cell.setCellStyle(style);
        }
    }

    public static int convertBooleanToInt(Object value) {

        int value_converted = 0;

        if ("true".equals(value.toString())) {
            value_converted = 1;
        }

        return value_converted;
    }
}
