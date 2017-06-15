package cl.techk.models;

import org.json.JSONObject;
import cl.techk.lib.CalendarUtils;
import cl.techk.lib.FileUtils;
import cl.techk.lib.ExcelUtils.FormatType;

public class LogReportExport {

    private static final String FILE_NAME = "ReporteLogs" + CalendarUtils.getActualDateInMilis();
    private static final String EXCEL_FILE_PATH = FileUtils.TEMP_FILE_FOLDER + FILE_NAME + ".xlsx";
    private static final FormatType[] FORMAT_TYPE = new FormatType[] { FormatType.TEXT, FormatType.TEXT,
            FormatType.TEXT, FormatType.TEXT, FormatType.TEXT, FormatType.TEXT, FormatType.TEXT };

    private static String getQueryUsersLog() {
        
        String query_log_data = "SELECT c.idcliente 'Id Cliente',c.nombre 'Nombre Cliente',u.idusuario 'Id Usuario',"
                + "u.nombre 'Nombre Usuario',p.nombre 'Perfil',l.fecha_acceso 'Fecha' FROM "
                + "log_acceso_modulos l LEFT JOIN usuario u ON l.id_usuario = u.idusuario "
                + "LEFT JOIN cliente c ON c.idcliente = l.id_cliente left join perfil p "
                + "ON p.idperfil = u.perfil_idperfil WHERE modulo = 'login' ORDER BY fecha_acceso ASC;";

        return query_log_data;
    }

    private static String getQueryUserModulesAccessed() {
        
        String query_modules_access = "SELECT c.idcliente 'Id Cliente',c.nombre 'Nombre Cliente',"
                + "u.idusuario 'Id Usuario',u.nombre 'Nombre Usuario',l.fecha_acceso 'Fecha',"
                + "l.modulo 'Módulo', '' Específico FROM log_acceso_modulos l LEFT JOIN "
                + "usuario u ON l.id_usuario = u.idusuario LEFT JOIN cliente c ON c.idcliente = l.id_cliente "
                + "left join perfil p ON p.idperfil = u.perfil_idperfil WHERE modulo not in ('login', 'Reportes')";

        // Obtengo la información de los reportes descargados
        String query_report_zone_access = "SELECT c.idcliente 'Id Cliente',c.nombre 'Nombre Cliente',"
                + "u.idusuario 'Id Usuario',u.nombre 'Nombre Usuario', r.fecha_descarga 'Fecha',"
                + "'Reportes' 'Módulo', r.nombre_reporte 'Específico' "
                + "FROM log_descarga_reportes r LEFT JOIN usuario u ON u.idusuario = r.id_usuario "
                + "LEFT JOIN cliente c ON c.idcliente = r.id_cliente";

        // Obtengo la información total de los módulos (Accesos y descarga
        // de reportes)
        String query_users_sections_access = "(" + query_modules_access + ") UNION (" + query_report_zone_access + ")";

        return query_users_sections_access;
    }

    private static String getQueryUserMaintainerActions() {
        
        // Obtengo la información del log de Mercado Relevante
        String query_relevant_market = "SELECT c.idcliente 'Id Cliente',c.nombre 'Nombre Cliente',"
                + "u.idusuario 'Id Usuario',u.nombre 'Nombre Usuario',mr.fecha 'Fecha',"
                + "'Mercado Relevante' Sección, case when mr.accion = 'Create' then "
                + "'Creación' when mr.accion = 'Update' then 'Modificacion' when mr.accion = "
                + "'Delete' then 'Eliminación' END Acción FROM log_mantenedor_clienteDiccionario mr "
                + "LEFT JOIN usuario u ON mr.id_usuario = u.idusuario LEFT JOIN cliente c ON mr.id_cliente = c.idcliente";

        // Obtengo la información de los logs de cambios en el mantenedor
        // Glosa Región
        String query_region_glosa = "SELECT c.idcliente 'Id Cliente',c.nombre 'Nombre Cliente',"
                + "u.idusuario 'Id Usuario',u.nombre 'Nombre Usuario',mr.fecha 'Fecha','Glosas de Despacho' Sección, case when mr.accion = 'Create' then 'Creación' "
                + "when mr.accion = 'Update' then 'Modificacion' when mr.accion = 'Delete' then "
                + "'Eliminación' end Acción FROM log_mantenedor_glosaRegion  mr LEFT JOIN usuario u ON "
                + "mr.id_usuario = u.idusuario LEFT JOIN cliente c ON mr.id_cliente = c.idcliente";

        // Obtengo la información de los logs de cambios en el mantenedor
        // catalogo de productos
        String query_products_catalog = "SELECT c.idcliente 'Id Cliente',c.nombre 'Nombre Cliente',"
                + "u.idusuario 'Id Usuario',u.nombre 'Nombre Usuario',mr.fecha 'Fecha',"
                + "'Catálogo de Productos' Sección, case when mr.accion = 'Create' then 'Creación' "
                + "when mr.accion = 'Update' then 'Modificacion' when mr.accion = 'Delete' then 'Eliminación' "
                + "end Acción FROM log_mantenedor_productos  mr LEFT JOIN usuario u ON mr.id_usuario = u.idusuario "
                + "LEFT JOIN cliente c ON mr.id_cliente = c.idcliente";

        // Obtengo la información de los logs de cambios en el mantenedor
        // responsable licitacion
        String query_licitation_responsible = "SELECT c.idcliente 'Id Cliente',c.nombre 'Nombre Cliente',"
                + "u.idusuario 'Id Usuario',u.nombre 'Nombre Usuario',mr.fecha 'Fecha',"
                + "'Responsable Licitación' Sección,case when mr.accion = 'Create' then 'Creación' "
                + "when mr.accion = 'Update' then 'Modificacion' when mr.accion = 'Delete' then 'Eliminación' "
                + "end Acción FROM `log_mantenedor_usuarioParametroAsignaciion` mr LEFT JOIN usuario u ON "
                + "mr.usuario_idusuario = u.idusuario LEFT JOIN cliente c ON mr.cliente_idcliente = c.idcliente";

        // Obtengo la información total de los módulos (Accesos y descarga
        // de reportes)
        String query_users_maintainer_actions = "(" + query_relevant_market + ") UNION (" + query_region_glosa + ") "
                + "UNION (" + query_products_catalog + ") UNION (" + query_licitation_responsible + ") ORDER BY fecha;";

        return query_users_maintainer_actions;
    }

    public static JSONObject getDataLogReport() {

        JSONObject report_data = new JSONObject();
        report_data.put("file_name", FILE_NAME);
        report_data.put("file_path", EXCEL_FILE_PATH);
        report_data.put("format_types", FORMAT_TYPE);
        report_data.put("sheets_names", new String[] { "Logins", "Modulo", "Mantenedor" });
        report_data.put("query", new String[] { getQueryUsersLog(), getQueryUserModulesAccessed(), getQueryUserMaintainerActions() });
        
        return report_data;
    }
}
