package cl.techk.models;

import org.json.JSONObject;

import cl.techk.lib.ExcelUtils;
import cl.techk.lib.FileUtils;

public class MainPointUsersExport {

    private static final String file_name = "Listado_Usuarios_Point";
    private static final String excel_file_path = FileUtils.TEMP_FILE_FOLDER + file_name + ".xlsx";
    private static final ExcelUtils.FormatType[] format_types = new ExcelUtils.FormatType[] {
            ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT,
            ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT,
            ExcelUtils.FormatType.TEXT };

    private static String getQueryUsers() {
        
        return "SELECT u.idusuario, u.nombre, u.usuario, p.nombre Perfil, c.idcliente, c.nombre Cliente FROM "
                + "point_licitaciones.usuario u LEFT JOIN point_licitaciones.perfil p ON p.idperfil = u.perfil_idperfil"
                + " LEFT JOIN point_licitaciones.cliente c ON c.idcliente = u.cliente_idcliente WHERE u.habilitado = 1 "
                + "ORDER BY idusuario ASC";
    }

    public static String getExcelFilePath() {
        return excel_file_path;
    }

    public static String getExcelFileName() {
        return file_name;
    }

    public static JSONObject getDataUsersPoint() {

        JSONObject report_data = new JSONObject();
        report_data.put("file_name", file_name);
        report_data.put("file_path", excel_file_path);
        report_data.put("format_types", format_types);
        report_data.put("query", new String[] { getQueryUsers() });
        report_data.put("sheets_names", new String[] { "Usuarios" });

        return report_data;
    }
}
