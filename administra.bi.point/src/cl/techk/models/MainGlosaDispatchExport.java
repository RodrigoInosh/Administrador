package cl.techk.models;

import org.json.JSONObject;
import cl.techk.lib.ExcelUtils;
import cl.techk.lib.FileUtils;

public class MainGlosaDispatchExport {

    private static final String file_name = "Glosas_Despacho";
    private static final String excel_file_path = FileUtils.TEMP_FILE_FOLDER + file_name + ".xlsx";
    private static final ExcelUtils.FormatType[] format_types = new ExcelUtils.FormatType[] {
            ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT,
            ExcelUtils.FormatType.TEXT };

    private static String getQueryGlosa() {
        return "SELECT * FROM cliente_glosa_region";
    }
    
    public static String getExcelFilePath() {
        return excel_file_path;
    }
    
    public static String getExcelFileName() {
        return file_name;
    }
    
    public static JSONObject getDataGlosaDispatch() {
        
        JSONObject report_data = new JSONObject();
        report_data.put("file_name", file_name);
        report_data.put("file_path", excel_file_path);
        report_data.put("format_types", format_types);
        report_data.put("query", new String[] {getQueryGlosa()});
        report_data.put("sheets_names", new String[] {"Glosas_Despacho"});

        return report_data;
    }
}
