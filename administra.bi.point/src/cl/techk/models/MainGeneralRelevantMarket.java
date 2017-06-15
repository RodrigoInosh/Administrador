package cl.techk.models;

import org.json.JSONObject;

import cl.techk.lib.DataBaseUtils;
import cl.techk.lib.ExcelUtils;
import cl.techk.lib.FileUtils;

public class MainGeneralRelevantMarket {

    private static String file_name = "";
    private static String excel_file_path = "";
    private static final ExcelUtils.FormatType[] format_types = new ExcelUtils.FormatType[] {
            ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT,
            ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT,
            ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT,
            ExcelUtils.FormatType.TEXT };

    public static String getQueryRelevantMarket() {
        return "SELECT * FROM maestro_keywords";
    }

    public static String getQueryRelevantMarketByClient(String client_id) {

        String where_clause = DataBaseUtils.getWhereClause(client_id);
        return "SELECT * FROM cliente_diccionario " + where_clause + " ORDER BY id_cliente ASC";
    }

    public static String getExcelFilePath() {
        return excel_file_path;
    }

    public static String getExcelFileName() {
        return file_name;
    }
    
    public static void setExcelFileName(String name) {
        file_name = name;
        excel_file_path = FileUtils.TEMP_FILE_FOLDER + file_name + ".xlsx";
    }

    public static JSONObject getDataRelevantMarket(String query_relevant_market, String file_name, String sheet_name) {

        setExcelFileName(file_name);
        JSONObject report_data = new JSONObject();
        report_data.put("file_name", file_name);
        report_data.put("file_path", excel_file_path);
        report_data.put("format_types", format_types);
        report_data.put("query", new String[] { query_relevant_market });
        report_data.put("sheets_names", new String[] { sheet_name });

        return report_data;
    }
}
