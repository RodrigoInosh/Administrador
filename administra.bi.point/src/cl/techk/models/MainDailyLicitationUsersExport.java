package cl.techk.models;

import org.json.JSONObject;
import cl.techk.lib.ExcelUtils;
import cl.techk.lib.FileUtils;

public class MainDailyLicitationUsersExport {

    private static final String file_name = "UsuariosLicitacionesDiarios";
    private static final String excel_file_path = FileUtils.TEMP_FILE_FOLDER + file_name + ".xlsx";
    private static final ExcelUtils.FormatType[] format_types = new ExcelUtils.FormatType[] {
            ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT,
            ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT };

    private static String getQueryUsersDailyLicitation() {
        
        return "SELECT u.idusuario ,u.nombre AS Usuario, u.mail, c.nombre Cliente, c.idcliente FROM "
                + "mail_usuarioAlerta AS a LEFT JOIN usuario AS u ON u.idusuario = a.id_mailAdmin LEFT JOIN "
                + "cliente AS c on c.idcliente = a.id_cliente WHERE a.id_tipoAlerta = 3 ORDER BY idcliente, idusuario";
    }

    public static String getExcelFilePath() {
        return excel_file_path;
    }

    public static String getExcelFileName() {
        return file_name;
    }

    public static JSONObject getDataUsersLicitation() {

        JSONObject report_data = new JSONObject();
        report_data.put("file_name", file_name);
        report_data.put("file_path", excel_file_path);
        report_data.put("format_types", format_types);
        report_data.put("sheets_names", new String[] { "UsuariosLicitacionesDiarios" });
        report_data.put("query", new String[] { getQueryUsersDailyLicitation() });

        return report_data;
    }
}
