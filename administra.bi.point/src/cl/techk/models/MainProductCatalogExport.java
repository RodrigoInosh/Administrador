package cl.techk.models;

import org.json.JSONObject;

import cl.techk.lib.DataBaseUtils;
import cl.techk.lib.ExcelUtils;
import cl.techk.lib.FileUtils;

public class MainProductCatalogExport {

    private static final String file_name = "Catalogo_Productos";
    private static final String excel_file_path = FileUtils.TEMP_FILE_FOLDER + file_name + ".xlsx";
    private static final ExcelUtils.FormatType[] format_types = new ExcelUtils.FormatType[] {
            ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT,
            ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.BOOLEAN,
            ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT,
            ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT,
            ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT };

    private static String getQueryProductsCatalog(String client_id) {
        return "SELECT * FROM cliente_catalogo_productos" + DataBaseUtils.getWhereClause(client_id)
                + " ORDER BY id_cliente ASC";
    }

    public static String getExcelFilePath() {
        return excel_file_path;
    }

    public static String getExcelFileName() {
        return file_name;
    }

    public static JSONObject getDataProductsCatalog(String client_id) {

        JSONObject report_data = new JSONObject();
        report_data.put("file_name", file_name);
        report_data.put("file_path", excel_file_path);
        report_data.put("format_types", format_types);
        report_data.put("query", new String[] { getQueryProductsCatalog(client_id) });
        report_data.put("sheets_names", new String[] { "Catalogo_Productos" });

        return report_data;
    }
}
