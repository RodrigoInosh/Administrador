package cl.techk.models;

import org.json.JSONObject;
import cl.techk.lib.ExcelUtils;
import cl.techk.lib.FileUtils;

public class MainPointMastersExport {

    private static String file_name;
    private static String excel_file_path;
    private static final String[] sheets_names = new String[] { "Compradores General", "Compradores Mercado",
            "Proveedores", "Jerarquia Productos" };
    private static final ExcelUtils.FormatType[] format_types = new ExcelUtils.FormatType[] {
            ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT,
            ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT,
            ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT,
            ExcelUtils.FormatType.TEXT, ExcelUtils.FormatType.TEXT };

    private static String getQueryGeneralBuyersMaster(String point_module_database) {
        return "SELECT * FROM " + point_module_database + ".maestros_compradores_gnral";
    }

    private static String getQueryBuyersByMarket(String point_module_database) {
        return "SELECT * FROM " + point_module_database + ".maestros_compradores_mercado";
    }

    private static String getQueryProviders(String point_module_database) {
        return "SELECT * FROM " + point_module_database + ".maestros_proveedores";
    }

    private static String getQueryProductsMaster(String point_module_database) {
        return "SELECT * FROM " + point_module_database + ".maestros_jerarquias_productos";
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

    public static JSONObject getDataPurchaseOrdersMaster(String point_module_database, String file_name) {

        setFileName(file_name);
        JSONObject report_data = new JSONObject();
        report_data.put("file_name", file_name);
        report_data.put("file_path", excel_file_path);
        report_data.put("format_types", format_types);
        report_data.put("query", new String[] { getQueryGeneralBuyersMaster(point_module_database), getQueryBuyersByMarket(point_module_database),
                getQueryProviders(point_module_database), getQueryProductsMaster(point_module_database) });
        report_data.put("sheets_names", sheets_names);

        return report_data;
    }
}
