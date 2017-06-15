package cl.techk.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cl.techk.ext.database.DBCnx;
import cl.techk.ext.database.DataManager;
import cl.techk.ext.utils.Utils;

public class Notification {

    public static int saveConfiguration(JSONObject configuration, String id_alert_type, String id_client,
            String id_user, String notification_type) {

        String usuarios_relevantes = configuration.get("user_configs").toString();
        String usuarios_config[];
        int response = -1;
        // Elimino la última "," que viene en el string
        usuarios_config = usuarios_relevantes.substring(0, usuarios_relevantes.length() - 1).split(",");

        // Reviso si la notificación no es del tipo "Notificaciones
        // por Cliente", y por lo tanto lleva
        // asignación de usuarios relevantes (Un usuario resive
        // notificaciones sólo de algunos usuarios).
        if (!notification_type.equals("by_client")) {
            response = saveConfigurationByUser(usuarios_config, id_alert_type, id_client, id_user);
        } else {
            response = saveConfigurationByClient(usuarios_config, id_alert_type, id_client);
        }

        return response;
    }

    public static int saveConfigurationByClient(String[] usuarios_config, String id_alert_type, String id_client) {
        // Si la notificación es del tipo 2, Notificaciones por
        // Cliente, entonces sólo agrego
        // los registros de los usuarios que recibirán los mails
        // de notificaciones a mail_usuarioAlerta
        PreparedStatement stmt_insert_into_usuario_alerta = null;
        Connection connection = DBCnx.conexion();
        int inserted_id = 0;
        String query_insert_into_usuario_alerta = "INSERT INTO mail_usuarioAlerta (id_tipoAlerta, id_mailAdmin, id_cliente)"
                + " VALUES (?,?,?)";

        try {
            for (int ciclo_usuarios = 0; ciclo_usuarios < usuarios_config.length; ciclo_usuarios++) {
                stmt_insert_into_usuario_alerta = connection.prepareStatement(query_insert_into_usuario_alerta,
                        PreparedStatement.RETURN_GENERATED_KEYS);
                inserted_id = execute_insert_user_alert(stmt_insert_into_usuario_alerta, id_alert_type,
                        usuarios_config[ciclo_usuarios], id_client);
            }
        } catch (Exception error) {
            error.printStackTrace();
        }
        return inserted_id;
    }

    public static int saveConfigurationByUser(String[] usuarios_config, String id_alert_type, String id_client,
            String id_user) {

        PreparedStatement stmt_insert_into_usuario_configuracion = null;
        Connection connection = DBCnx.conexion();
        int inserted_id = -1;

        String query_insert_into_usuario_configuracion = "INSERT INTO mail_configuracion_usuario (id_mail_usuario_alerta, id_usuario)"
                + " VALUES (?,?)";

        // Inserto el registro de a quién se le está asignando
        // la notificación actual.
        // A quien le llegará la notificación.
        // valores de mail_usuarioAlerta
        String[] user = new String[1];
        user[0] = id_user;
        inserted_id = saveConfigurationByClient(user, id_alert_type, id_client);

        try {
            // Si es notificación de cambio y además se insertó el
            // registro a la tabla mail_usuarioAlerta,
            // entonces preparo el Statement para agregar valores a
            // mail_configuracion_usuario.
            // Acá se agregan los usuarios de quienes se desea
            // recibir notificaciones de sus acciones.
            if (inserted_id > -1) {
                stmt_insert_into_usuario_configuracion = connection
                        .prepareStatement(query_insert_into_usuario_configuracion);
                for (int ciclo_usuarios = 0; ciclo_usuarios < usuarios_config.length; ciclo_usuarios++) {
                    stmt_insert_into_usuario_configuracion.setInt(1, inserted_id);
                    stmt_insert_into_usuario_configuracion.setString(2, usuarios_config[ciclo_usuarios]);

                    stmt_insert_into_usuario_configuracion.addBatch();
                }
                stmt_insert_into_usuario_configuracion.executeBatch();
                stmt_insert_into_usuario_configuracion.clearBatch();
            }
        } catch (Exception error) {
            inserted_id = -1;
            error.printStackTrace();
        } finally {
            DBCnx.close(stmt_insert_into_usuario_configuracion);
            DBCnx.close(connection);
        }

        return inserted_id;
    }

    private static int execute_insert_user_alert(PreparedStatement statement, String id_tipo_alerta, String user,
            String clte) {
        int inserted_id = 0;
        try {
            statement.setString(1, id_tipo_alerta);
            statement.setString(2, user);
            statement.setString(3, clte);

            inserted_id = DataManager.insertQueryGetId(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return inserted_id;
    }

    public static int deleteConfiguration(String id_tipo_alerta, String user, String clte) {

        Connection connection = null;
        PreparedStatement stmt_delete_config = null;
        int response = 1;
        try {
            connection = DBCnx.conexion();
            // Reviso si ya existe la tupla y la borro. Esto es para cuando se
            // está actualizando la configuración no
            // tener que revisar qué cosas cambiaron, simplemente se borra la
            // configuración anterior y se guarda la nueva.
            if (!user.equals("")) {
                String delete_config_por_usuario = "DELETE FROM mail_usuarioAlerta WHERE "
                        + "id_tipoAlerta = ? AND id_mailAdmin = ? AND id_cliente = ?";

                stmt_delete_config = connection.prepareStatement(delete_config_por_usuario);
                stmt_delete_config.setString(1, id_tipo_alerta);
                stmt_delete_config.setString(2, user);
                stmt_delete_config.setString(3, clte);
            } else {
                String delete_config_por_cliente = "DELETE FROM mail_usuarioAlerta WHERE "
                        + "id_tipoAlerta = ? AND id_cliente = ?";
                stmt_delete_config = connection.prepareStatement(delete_config_por_cliente);
                stmt_delete_config.setString(1, id_tipo_alerta);
                stmt_delete_config.setString(2, clte);
            }
            stmt_delete_config.executeUpdate();
        } catch (Exception e) {
            response = -1;
            e.printStackTrace();
        } finally {
            DBCnx.close(stmt_delete_config);
            DBCnx.close(connection);
        }

        return response;
    }

    public static JSONArray selectConfigurationByClient(String client_id, String notification_type) {
        JSONArray response = new JSONArray();

        String query_config_notif_por_cliente = "SELECT id_mailAdmin id_user, mta.id id_alerta FROM "
                + "`mail_usuarioAlerta` mua LEFT JOIN mail_tipoAlerta mta ON mua.id_tipoAlerta = mta.id "
                + "WHERE id_cliente = ? and tipo = ?";

        PreparedStatement stmt_config_notif_por_cliente = null;
        ResultSet result = null;
        Connection connection = null;

        try {
            connection = DBCnx.conexion();
            stmt_config_notif_por_cliente = connection.prepareStatement(query_config_notif_por_cliente);

            stmt_config_notif_por_cliente.setString(1, client_id);
            stmt_config_notif_por_cliente.setString(2, notification_type);

            result = stmt_config_notif_por_cliente.executeQuery();

            while (result.next()) {
                JSONObject obj = new JSONObject();

                obj.put("user", result.getInt("id_user"));
                obj.put("notif", result.getString("id_alerta"));
                response.put(obj);
            }
        } catch (JSONException e) {
            Utils.printOrdErr("JSONException get users notifications configuration: " + e.getMessage());
        } catch (SQLException e) {
            Utils.printOrdErr("SQLException get users notifications configuration: " + e.getMessage());
        } finally {
            DBCnx.closeAll(result, stmt_config_notif_por_cliente, connection);
        }

        return response;
    }

    public static JSONArray selectConfigurationByUser(String user_id, String client_id, String notification_type) {
        JSONArray response = new JSONArray();

        String query_config_notif_por_usuario = "SELECT id_usuario id_user, id_tipoAlerta id_alerta FROM mail_configuracion_usuario mcu "
                + "LEFT JOIN mail_usuarioAlerta mua ON mcu.id_mail_usuario_alerta = mua.id LEFT JOIN mail_tipoAlerta mta "
                + "ON mta.id = mua.id_tipoAlerta WHERE tipo = ? AND id_cliente = ? AND mua.id_mailAdmin = ?";

        PreparedStatement stmt_config_notif_por_usuario = null;
        ResultSet result = null;
        Connection connection = null;

        try {
            connection = DBCnx.conexion();
            stmt_config_notif_por_usuario = connection.prepareStatement(query_config_notif_por_usuario);

            stmt_config_notif_por_usuario.setString(1, notification_type);
            stmt_config_notif_por_usuario.setString(2, client_id);
            stmt_config_notif_por_usuario.setString(3, user_id);

            result = stmt_config_notif_por_usuario.executeQuery();

            while (result.next()) {
                JSONObject obj = new JSONObject();

                obj.put("user", result.getInt("id_user"));
                obj.put("notif", result.getString("id_alerta"));
                response.put(obj);
            }
        } catch (JSONException e) {
            Utils.printOrdErr("JSONException get users notifications configuration: " + e.getMessage());
        } catch (SQLException e) {
            Utils.printOrdErr("SQLException get users notifications configuration: " + e.getMessage());
        } finally {
            DBCnx.closeAll(result, stmt_config_notif_por_usuario, connection);
        }
        return response;
    }

    public static int preparedFileNotificationSettingToInsert(
            LinkedHashMap<String, LinkedHashMap<Integer, Integer>> user_notification_setting, int client_id) {
        int saved_correctly = 1;

        int mail_receiving_user = 0, user_generates_event = 0;
        Iterator<String> user_notification_setting_keys = user_notification_setting.keySet().iterator();
        
        while (user_notification_setting_keys.hasNext()) {
            String key_to_validate = user_notification_setting_keys.next();
            String splitted_key[] = key_to_validate.split("-");
            mail_receiving_user = Integer.parseInt(splitted_key[0]);
            user_generates_event = Integer.parseInt(splitted_key[1]);

            LinkedHashMap<Integer, Integer> notification_setting = user_notification_setting.get(key_to_validate);
            Iterator<Integer> values_notification_settings_keys = notification_setting.keySet().iterator();

            while (values_notification_settings_keys.hasNext()) {
                int notification_id = values_notification_settings_keys.next();
                int setting_value = notification_setting.get(notification_id);

                if (setting_value == 1) {
                    int id_user_notification = getIdUserNotification(mail_receiving_user, notification_id, client_id);
                    saveNotificationSettingByFile(id_user_notification, user_generates_event, setting_value);
                }
            }
        }
        return saved_correctly;
    }

    public static int getIdUserNotification(int mail_receiving_user, int notification_id, int client_id) {
        int id_user_notification = 0;

        PreparedStatement exist_user_notification = null, stmt_insert_into_usuario_alerta = null;
        Connection connection = null;
        ResultSet result = null;

        try {
            connection = DBCnx.conexion();
            String query_get_id_user_notification = "SELECT id FROM mail_usuarioAlerta WHERE id_tipoAlerta = ? AND id_mailAdmin = ? AND id_cliente = ?";

            exist_user_notification = connection.prepareStatement(query_get_id_user_notification);
            exist_user_notification.setInt(1, notification_id);
            exist_user_notification.setInt(2, mail_receiving_user);
            exist_user_notification.setInt(3, client_id);

            result = exist_user_notification.executeQuery();

            if (result.next()) {
                id_user_notification = result.getInt("id");
            } else {
                stmt_insert_into_usuario_alerta = connection.prepareStatement(
                        "INSERT INTO mail_usuarioAlerta (id_tipoAlerta, id_mailAdmin, id_cliente) VALUES (?,?,?)",
                        PreparedStatement.RETURN_GENERATED_KEYS);
                id_user_notification = execute_insert_user_alert(stmt_insert_into_usuario_alerta,
                        String.valueOf(notification_id), String.valueOf(mail_receiving_user),
                        String.valueOf(client_id));
            }
        } catch (Exception error) {
            error.printStackTrace();
        } finally {
            DBCnx.close(result);
            DBCnx.close(stmt_insert_into_usuario_alerta);
            DBCnx.close(exist_user_notification);
            DBCnx.close(connection);
        }

        return id_user_notification;
    }

    public static int saveNotificationSettingByFile(int id_user_notification, int user_generates_event,
            int setting_value) {
        int saved_correctly = -1;

        PreparedStatement stmt_insert_user_notification_setting = null;
        Connection connection = null;

        String query_insert_user_notification_setting = "INSERT INTO mail_configuracion_usuario (id_mail_usuario_alerta, id_usuario)"
                + " VALUES (?,?)";
        try {
            connection = DBCnx.conexion();
            int id_notification_setting = getIdNotificationSetting(id_user_notification, user_generates_event);

            if (id_notification_setting == 0 && setting_value == 1 && user_generates_event > 0) {
                
                stmt_insert_user_notification_setting = connection.prepareStatement(query_insert_user_notification_setting);
                stmt_insert_user_notification_setting.setInt(1, id_user_notification);
                stmt_insert_user_notification_setting.setInt(2, user_generates_event);
                stmt_insert_user_notification_setting.executeUpdate();
            }
        } catch (Exception error) {
            error.printStackTrace();
        } finally {
            DBCnx.close(stmt_insert_user_notification_setting);
            DBCnx.close(connection);
        }
        return saved_correctly;

    }

    public static int getIdNotificationSetting(int id_user_notification, int user_generates_event) {
        int id_notification_setting = 0;

        PreparedStatement statement_select_configuration_user = null;
        Connection connection = null;
        ResultSet result = null;

        try {
            connection = DBCnx.conexion();

            String select_configuration_user = "SELECT * FROM mail_configuracion_usuario WHERE id_mail_usuario_alerta = ? AND id_usuario = ?";

            statement_select_configuration_user = connection.prepareStatement(select_configuration_user);
            statement_select_configuration_user.setInt(1, id_user_notification);
            statement_select_configuration_user.setInt(2, user_generates_event);

            result = statement_select_configuration_user.executeQuery();

            if (result.next()) {
                id_notification_setting = result.getInt("id");
            }
        } catch (SQLException error) {
            error.printStackTrace();
        } finally {
            DBCnx.closeAll(result, statement_select_configuration_user, connection);
        }

        return id_notification_setting;
    }
}
