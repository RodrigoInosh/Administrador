package cl.techk.models;

import org.json.JSONObject;
import cl.techk.ext.database.DBCnx;
import cl.techk.lib.ExcelUtils;

public class MaintainerExport {

    private static final String alias_glosa_dispatch_maintainer = "glosa";
    private static final String alias_no_postulation_motives_maintainer = "motives";
    private static final String alias_oc_maestros = "oc_masters";
    private static final String alias_adjudicadas_maestros = "adj_masters";
    private static final String alias_list_users_point = "list_users";
    private static final String alias_oc_filters_configuration = "filters_config_oc";
    private static final String alias_adj_filters_configuration = "filters_config_adj";
    private static final String alias_daily_licitation_users_list = "list_users_lic";
    private static final String alias_general_relevant_market = "merc_gnral";
    private static final String alias_relevant_market_by_client = "relevant_mkt";
    private static final String alias_product_catalog = "products";
    private static final String alias_user_asign_parameters = "user_asign";
    private static final String alias_user_logs = "logs";

    public static JSONObject generateXlsxFile(String report_id) {

        JSONObject excel_file = new JSONObject();
        JSONObject report_data = new JSONObject();

        switch (report_id) {
            case alias_glosa_dispatch_maintainer:
                report_data = MainGlosaDispatchExport.getDataGlosaDispatch();
                break;
            case alias_no_postulation_motives_maintainer:
                report_data = MainNotApplyingReasonsExport.getDataNotApplyingReasons();
                break;
            case alias_oc_maestros:
                report_data = MainPointMastersExport.getDataPurchaseOrdersMaster(DBCnx.db_orders,
                        "Maestros_Ordenes_de_Compra");
                break;
            case alias_adjudicadas_maestros:
                report_data = MainPointMastersExport.getDataPurchaseOrdersMaster(DBCnx.db_adjudicadas,
                        "Maestros_Adjudicadas");
                break;
            case alias_list_users_point:
                report_data = MainPointUsersExport.getDataUsersPoint();
                break;
            case alias_oc_filters_configuration:
                report_data = MainConfigurationPointFiltersExport.getDataConfigurationFilters(DBCnx.db_orders,
                        "Configuracion_Filtros_Ordenes_de_Compra");
                break;
            case alias_adj_filters_configuration:
                report_data = MainConfigurationPointFiltersExport.getDataConfigurationFilters(DBCnx.db_adjudicadas,
                        "Configuracion_Filtros_Adjudicadas");
                break;
            case alias_daily_licitation_users_list:
                report_data = MainDailyLicitationUsersExport.getDataUsersLicitation();
                break;
            case alias_user_logs:
                report_data = LogReportExport.getDataLogReport();
                break;
            case alias_general_relevant_market:
                String query_relevant_market = MainGeneralRelevantMarket.getQueryRelevantMarket();
                report_data = MainGeneralRelevantMarket.getDataRelevantMarket(query_relevant_market,
                        "Mercado_Relevante_General", "Mercado_Relevante_General");
                break;
            case alias_relevant_market_by_client:
                String query_relevant_market_by_client = MainGeneralRelevantMarket.getQueryRelevantMarketByClient("");
                report_data = MainGeneralRelevantMarket.getDataRelevantMarket(query_relevant_market_by_client,
                        "Mercado_Relevante_Clientes", "Mercado_Relevante_Clientes");
                break;
            case alias_product_catalog:
                report_data = MainProductCatalogExport.getDataProductsCatalog("");
                break;
            case alias_user_asign_parameters:
                report_data = MainUserParametersLicitationAsignExport.getDataUserAsign("");
                break;
            default:
                break;
        }

        excel_file.put("file_path", report_data.getString("file_path"));
        excel_file.put("file_name", report_data.getString("file_name"));
        ExcelUtils.createXlsxFile(report_data);

        return excel_file;
    }
}
