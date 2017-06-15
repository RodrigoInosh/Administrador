package cl.techk.licitaciones.rest;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import cl.techk.data.DataMailLicitationBulletin;
import cl.techk.ext.database.DBCnx;
import cl.techk.ext.utils.TemplateMailLoader;
import cl.techk.ext.utils.Utils;
import cl.techk.mail.rest.Mail;

public class BoletinLicitaciones {

    public static void licitationDailyBulletin(int client_id, String tipos_lic) {

        String licitation_types = getFormattedLicitationTypes(tipos_lic);
        String mail_templates = "";
        String mail_body = "";
        JSONObject datos = getIdUsuario(client_id);
        int user_id = datos.getInt("id");
        String client_name = datos.getString("clte");
        mail_templates = Mail.html_template_notification_licitation_bulletin;
        DataMailLicitationBulletin data_bulletin = getBulletinData(client_id, client_name, user_id, licitation_types);
        try {
            mail_body = TemplateMailLoader.loadFilledTemplate(data_bulletin, mail_templates);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Mail.sendMail(mail_body, "Status licitaciones que vencen hoy", null, null, client_id, client_name, 5);
    }

    public static DataMailLicitationBulletin getBulletinData(int client_id, String client_name, int user_id,
            String licitation_types) {

        Connection database_connection = null;
        Statement licitation_statement = null;
        ResultSet client_licitations_data = null;
        DataMailLicitationBulletin data_bulletin = null;
        try {

            database_connection = DBCnx.conexion();
            licitation_statement = database_connection.createStatement();
            ArrayList<Map<String, String>> today_expire_licitation_table = new ArrayList<Map<String, String>>();
            ArrayList<Map<String, String>> today_expire_favorite_licitation_table = new ArrayList<Map<String, String>>();
            client_licitations_data = licitation_statement
                    .executeQuery(getQueryLicitaciones(client_id, user_id, licitation_types));
            while (client_licitations_data.next()) {

                HashMap<String, String> data = getLicitationQueryData(client_licitations_data);
                String asignated_users = getUsersAsignatedToLicitation(client_id, client_licitations_data.getInt("id"),
                        database_connection);
                data.put("users", asignated_users);

                if (client_licitations_data.getInt("es_favorita") == 0) {
                    today_expire_licitation_table.add(data);
                } else {
                    today_expire_favorite_licitation_table.add(data);
                }
            }

            SimpleDateFormat chilean_date_format = new SimpleDateFormat("dd-MM-yyyy");
            String current_date = chilean_date_format.format(Utils.getFechaChile().getTime());
            data_bulletin = new DataMailLicitationBulletin(today_expire_licitation_table,
                    today_expire_favorite_licitation_table, today_expire_licitation_table.size(),
                    today_expire_favorite_licitation_table.size(), client_name, licitation_types, current_date);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error obteniendo datos licitaciones");
        } finally {
            DBCnx.closeAll(client_licitations_data, licitation_statement, database_connection);
        }

        return data_bulletin;
    }

    public static HashMap<String, String> getLicitationQueryData(ResultSet resultLic) {

        HashMap<String, String> data = new HashMap<String, String>();
        try {
            String[] codigo_arr = resultLic.getString("codigo").split("-");
            String tipo_lic = codigo_arr[2].substring(0, 2);
            String licitation_name = Utils.getCutText(resultLic.getString("nombre"));
            String buyer_company_name = Utils.getCutText(resultLic.getString("razon_social"));
            data.put("id", resultLic.getString("id"));
            data.put("codigo", resultLic.getString("codigo"));
            data.put("nombre", licitation_name);
            data.put("fecha_cierre", resultLic.getString("fecha_cierre"));
            data.put("fecha_publicacion", resultLic.getString("fecha_publicacion"));
            data.put("favorita", resultLic.getString("es_favorita"));
            data.put("comprador", buyer_company_name);
            data.put("tipo_lic", tipo_lic);
            data.put("garantias", resultLic.getString("garantias"));
            data.put("postulaciones", resultLic.getString("postulaciones"));
        } catch (Exception error) {
            error.printStackTrace();
        }

        return data;
    }

    public static String getUsersAsignatedToLicitation(int client_id, int licitation_id,
            Connection database_connection) {
        String asignated_users = "";
        String get_licitation_asignated_users = "SELECT u.usuario from usuario_licitacion ul join usuario "
                + "u on(ul.usuario_idusuario=u.idusuario) where ul.licitacion_idlicitacion=" + licitation_id
                + " and u.cliente_idcliente=" + client_id + " and u.perfil_idperfil=2";
        Statement stmt2 = null;
        ResultSet resultPostul = null;

        try {
            stmt2 = database_connection.createStatement();
            resultPostul = stmt2.executeQuery(get_licitation_asignated_users);
            if (resultPostul.next()) {
                asignated_users = resultPostul.getString("u.usuario").split("\\.")[1];
            } else {
                asignated_users = "No";
            }
        } catch (Exception error) {
            error.printStackTrace();
        } finally {
            DBCnx.closeAll(resultPostul, stmt2);
        }
        return asignated_users;
    }

    public static String getQueryLicitaciones(int id_cliente, int id_usuario, String tipos_lic) {

        System.out.println("Obteniendo licitaciones");
        String get_client_licitation = "";
        String estado_licitacion = " ";
        estado_licitacion = " fecha_cierre >= DATE_FORMAT(now(), '%Y-%m-%d 00:00:00') AND l.fecha_cierre <= DATE_FORMAT(now(), '%Y-%m-%d 23:59:59') AND ";
        String campos = "id,nombre, descripcion,codigo,es_favorita,razon_social,fecha_cierre,fecha_publicacion,garantias,"
                + "CASE WHEN postulaciones = 'Ingresada' THEN 'Postulada' WHEN postulaciones IS NULL THEN 'No Guardada' "
                + "WHEN postulaciones = 'No Postulada' THEN 'Descartada' ELSE postulaciones END postulaciones";

        get_client_licitation = "SELECT * FROM (select " + campos
                + " from (select a.*,(SELECT pl.estado as postulaciones FROM postulacion_licitacion "
                + "pl join postulacion_estado e on (e.valor=pl.estado) where licitacion_idlicitacion=a.id and cliente_idcliente="
                + id_cliente + "  order by prioridad asc limit 1) as postulaciones from (select id,tipo,"
                + "nombre,descripcion,unidad_compra,codigo,estado,es_favorita,"
                + "razon_social,rut,region, DATE_FORMAT(  `fecha_cierre` ,  '%d-%m-%Y %H:%i:%s' ) AS fecha_cierre, "
                + "DATE_FORMAT(  `fecha_publicacion` ,  '%d-%m-%Y %H:%i:%s' ) AS fecha_publicacion,fecha_adjudicacion,"
                + " garantias from licitacion l join usuario_licitacion ul on "
                + "(l.id=ul.licitacion_idlicitacion) where  " + estado_licitacion + " ul.usuario_idusuario="
                + id_usuario + " order by fecha_cierre ASC) a) b) as T WHERE postulaciones in (" + tipos_lic + ")";

        return get_client_licitation;
    }

    private static JSONObject getIdUsuario(int id_cliente) {

        System.out.println("Obteniendo datos usuario");
        JSONObject obj = new JSONObject();
        String query = "SELECT u.idusuario,u.usuario,c.nombre,"
                + "cliente_idcliente FROM cliente c LEFT JOIN usuario u ON u.cliente_idcliente = c.idcliente "
                + "WHERE u.perfil_idperfil = 1 AND u.habilitado = 1 AND u.cliente_idcliente = " + id_cliente
                + " ORDER BY u.idusuario ASC LIMIT 1";
        Connection cnx = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            cnx = DBCnx.conexion();
            stmt = cnx.prepareStatement(query);
            result = stmt.executeQuery(query);
            if (result.next()) {

                obj.put("id", result.getInt("idusuario"));
                obj.put("clte", result.getString("nombre"));
            }
        } catch (SQLException e) {
            System.out.println("Error obteniendo id: " + e.getMessage());
        } finally {
            DBCnx.closeAll(result, stmt, cnx);
        }

        return obj;
    }

    private static String getFormattedLicitationTypes(String tipos_lic) {

        String tipos = "";
        String[] aux = tipos_lic.split(";");
        for (int ix = 0; ix < aux.length; ix++) {
            tipos += "'" + aux[ix] + "', ";
        }
        if (!tipos.equals("")) {
            tipos = tipos.substring(0, tipos.length() - 2);
        }
        return tipos;
    }
}
