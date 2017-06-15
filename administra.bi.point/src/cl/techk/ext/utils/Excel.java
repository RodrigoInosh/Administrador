package cl.techk.ext.utils;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFCellUtil;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;

public class Excel {

    private static FormatType getFormatType(Class _class) {
        if (_class == Integer.class || _class == Long.class) {
            return FormatType.INTEGER;
        } else if (_class == Float.class || _class == Double.class) {
            return FormatType.FLOAT;
        } else if (_class == Timestamp.class || _class == java.sql.Date.class) {
            return FormatType.DATE;
        } else {
            return FormatType.TEXT;
        }
    }

    public static HSSFSheet generateSheet(ResultSet resultSet, FormatType[] formatTypes, HSSFSheet sheet,
            HSSFCellStyle style, int currentRow, HSSFWorkbook workbook) throws Exception {

        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        if (formatTypes != null && formatTypes.length != resultSetMetaData.getColumnCount()) {
            throw new IllegalStateException(
                    "Number of types is not identical to number of resultset columns. " + "Number of types: "
                            + formatTypes.length + ". Number of columns: " + resultSetMetaData.getColumnCount());
        }

        HSSFRow row = sheet.createRow(currentRow);
        int numCols = resultSetMetaData.getColumnCount();
        boolean isAutoDecideFormatTypes;
        if (isAutoDecideFormatTypes = (formatTypes == null)) {
            formatTypes = new FormatType[numCols];
        }

        // defino estilo titulo

        HSSFCellStyle estilo = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(HSSFColor.WHITE.index);
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        estilo.setFont(font);
        estilo.setFillBackgroundColor(HSSFColor.GREEN.index);
        estilo.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);

        for (int i = 0; i < numCols; i++) {
            String title = resultSetMetaData.getColumnName(i + 1);
            writeCell(row, i, title, FormatType.TEXT, null, estilo, workbook);
            if (isAutoDecideFormatTypes) {
                Class _class = Class.forName(resultSetMetaData.getColumnClassName(i + 1));
                formatTypes[i] = getFormatType(_class);
            }
        }
        currentRow++;
        // Write report rows
        while (resultSet.next()) {
            row = sheet.createRow(currentRow++);
            row.setHeightInPoints(18);
            for (int i = 0; i < numCols; i++) {
                Object value = resultSet.getObject(i + 1);
                writeCell(row, i, value, formatTypes[i], null, style, workbook);
            }
        }
        // Autosize columns
        for (int i = 0; i < numCols; i++) {
            sheet.autoSizeColumn((short) i);
        }
        return sheet;
    }

    public static void writeCell(HSSFRow row, int col, Object value, FormatType formatType, HSSFFont font,
            HSSFCellStyle style, HSSFWorkbook workbook) throws Exception {
        writeCell(row, col, value, formatType, null, font, style, workbook);
    }

    private static void writeCell(HSSFRow row, int col, Object value, FormatType formatType, Short bgColor,
            HSSFFont font, HSSFCellStyle style, HSSFWorkbook workbook) throws Exception {
        HSSFCell cell = HSSFCellUtil.createCell(row, col, null);

        // si no viene un valor se termina

        if (value == null) {
            return;
        }

        if (style != null) {
            cell.setCellStyle(style);
        }

        switch (formatType) {
        case TEXT:
            cell.setCellValue(value.toString());
            break;
        case FLOAT:
            cell.setCellValue(((Number) value).doubleValue());
            HSSFCellUtil.setCellStyleProperty(cell, workbook, CellUtil.DATA_FORMAT,
                    HSSFDataFormat.getBuiltinFormat(("#,##0.00")));
            HSSFDataFormat format = workbook.createDataFormat();
            style.setDataFormat(format.getFormat("#,##0.0"));
            cell.setCellStyle(style);
            break;
        case MONEY:
            value = Double.valueOf((String) value);
            cell.setCellValue(((Number) value).doubleValue());
            HSSFCellUtil.setCellStyleProperty(cell, workbook, CellUtil.DATA_FORMAT,
                    HSSFDataFormat.getBuiltinFormat(("#,##0")));
            break;
        case INTEGER:
            cell.setCellValue(((Number) value).intValue());
            break;
        case PERCENTAGE:
            cell.setCellValue(((Number) value).doubleValue());
        case DATE:
            cell.setCellValue((Timestamp) value);
            break;
        }
    }

    public enum FormatType {
        TEXT, INTEGER, FLOAT, DATE, MONEY, PERCENTAGE
    }
}
