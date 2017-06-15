package cl.techk.models;

import org.json.JSONObject;
import cl.techk.lib.ExcelUtils;
import cl.techk.lib.FileUtils;

public class MainNotApplyingReasonsExport {

    private static final String file_name = "Motivos_no_Postulacion";
    private static final String excel_file_path = FileUtils.TEMP_FILE_FOLDER + file_name + ".xlsx";
    private static final ExcelUtils.FormatType[] format_types = new ExcelUtils.FormatType[] {
            ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT,
            ExcelUtils.FormatType.TEXT };

    private static String getQueryGlosa() {
        return "SELECT * FROM motivo_no_postulacion";
    }

    public static String getExcelFilePath() {
        return excel_file_path;
    }

    public static String getExcelFileName() {
        return file_name;
    }

    public static JSONObject getDataNotApplyingReasons() {

        JSONObject report_data = new JSONObject();
        report_data.put("file_name", file_name);
        report_data.put("file_path", excel_file_path);
        report_data.put("format_types", format_types);
        report_data.put("query", new String[] { getQueryGlosa() });
        report_data.put("sheets_names", new String[] { "Motivos_no_Postulacion" });

        return report_data;
    }
}
