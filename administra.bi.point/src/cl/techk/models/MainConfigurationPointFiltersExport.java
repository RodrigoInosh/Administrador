package cl.techk.models;

import org.json.JSONObject;

import cl.techk.lib.ExcelUtils;
import cl.techk.lib.FileUtils;

public class MainConfigurationPointFiltersExport {

    private static String file_name;
    private static String excel_file_path;
    private static final ExcelUtils.FormatType[] format_types = new ExcelUtils.FormatType[] {
            ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT,
            ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT };

    private static String getConfigurationFilters(String point_module_database) {

        return "SELECT nom.`id`, nom.`id_clte`, clte.nombre AS Cliente, nom.`id_filtro_generico`, gen.col_tabla_asoc "
                + "AS Filtro, nom.`nombre` FROM " + point_module_database + ".`nombre_filtros_cliente` nom LEFT JOIN "
                + point_module_database + ".filtros_genericos gen ON nom.`id_filtro_generico` = gen.id LEFT JOIN "
                + "cliente clte ON nom.id_clte = clte.idcliente ORDER BY id_clte, `id_filtro_generico`";
    }

    public static String getExcelFilePath() {
        return excel_file_path;
    }

    public static String getExcelFileName() {
        return file_name;
    }

    public static void setFileName(String name) {
        file_name = name;
        excel_file_path = FileUtils.TEMP_FILE_FOLDER + file_name + ".xlsx";
    }

    public static JSONObject getDataConfigurationFilters(String point_module_database, String file_name) {

        setFileName(file_name);
        JSONObject report_data = new JSONObject();
        report_data.put("file_name", file_name);
        report_data.put("file_path", excel_file_path);
        report_data.put("format_types", format_types);
        report_data.put("query", new String[] { getConfigurationFilters(point_module_database) });
        report_data.put("sheets_names", new String[] { "Configuracion Filtros" });

        return report_data;
    }
}
