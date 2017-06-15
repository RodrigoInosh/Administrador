package cl.techk.mantenedor.rest;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cl.techk.ext.database.DBCnx;
import cl.techk.ext.database.DataManager;
import cl.techk.ext.utils.Utils;
import cl.techk.mail.rest.Mail;
import cl.techk.models.Notification;

@Path("/funciones")
public class Funciones {
    // version 1.3
    // last review: 30-08-2016 last fixes: Agregada la función
    // insertConfigCliente que ingresa la configuración del cliente en la tabla
    // cliente_configuración. Esta tabla sirve para saber si el cliente utiliza
    // glosas por región o glosas por comuna.

    private static PreparedStatement statement_insert_usuarios = null;
    private static PreparedStatement statement_insert_modulos_usuarios = null;
    private static PreparedStatement statement_insert_limites_usuarios = null;
    private static PreparedStatement statement_insert_mercado_usuarios = null;

    @GET
    @Path("/get_clientes/")
    @Produces("application/json; charset=UTF-8")
    public Response get_clientes(@Context final HttpServletRequest request) {

        String query = "SELECT idcliente, nombre, rut, razon_social, licitaciones_diarias, glosa_despacho FROM "
                + "cliente c LEFT JOIN cliente_configuracion cc ON c.idcliente = cc.id_cliente ORDER BY nombre ASC";
        JSONArray clientes_json = new JSONArray();

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            connection = DBCnx.conexion();
            statement = connection.prepareStatement(query);
            result = statement.executeQuery();

            while (result.next()) {
                JSONObject obj = new JSONObject();

                obj.put("id", result.getInt("idcliente"));
                obj.put("c", result.getString("nombre"));
                obj.put("rut", result.getString("rut"));
                obj.put("razon", result.getString("razon_social"));
                obj.put("lic", result.getInt("licitaciones_diarias"));
                obj.put("config", result.getInt("glosa_despacho"));
                clientes_json.put(obj);
            }

        } catch (JSONException e) {
            Utils.printOrdErr("JSONException get clients: " + e.getMessage());
        } catch (SQLException e) {
            Utils.printOrdErr("SQLException get clients: " + e.getMessage());
        } finally {
            DBCnx.closeAll(result, statement, connection);
        }

        return Response.status(200).entity(clientes_json.toString()).build();
    }

    @POST
    @Path("/get_usuarios/")
    @Produces("application/json; charset=UTF-8")
    public Response get_usuarios(@FormParam("dato") String id_cliente, @FormParam("notif") String notif,
            @Context final HttpServletRequest request) {

        String query = "SELECT u.idusuario, u.usuario, if(t.id IS NOT NULL, 1, 0) AS n FROM usuario u LEFT JOIN "
                + "(SELECT * FROM mail_usuarioAlerta WHERE id_tipoAlerta = ? AND id_cliente = ?) AS t ON "
                + "u.idusuario = t.id_mailAdmin WHERE u.cliente_idcliente = ? AND habilitado = 1 ORDER BY u.usuario ASC";

        JSONArray json_usuarios = new JSONArray();

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            connection = DBCnx.conexion();
            statement = connection.prepareStatement(query);

            statement.setInt(1, Integer.parseInt(notif));
            statement.setInt(2, Integer.parseInt(id_cliente));
            statement.setInt(3, Integer.parseInt(id_cliente));
            result = statement.executeQuery();

            while (result.next()) {
                JSONObject obj = new JSONObject();

                obj.put("id", result.getInt("idusuario"));
                obj.put("dato", result.getString("usuario"));
                obj.put("notif", result.getString("n"));
                json_usuarios.put(obj);
            }

        } catch (JSONException e) {
            Utils.printOrdErr("JSONException get users: " + e.getMessage());
        } catch (SQLException e) {
            Utils.printOrdErr("SQLException get users: " + e.getMessage());
        } finally {
            DBCnx.closeAll(result, statement, connection);
        }

        return Response.status(200).entity(json_usuarios.toString()).build();
    }

    @POST
    @Path("/get_usuarios_clte/")
    @Produces("application/json; charset=UTF-8")
    public Response get_usuarios_clte(@FormParam("dato") String id_cliente, @Context final HttpServletRequest request) {

        String query = "SELECT idusuario, usuario FROM usuario WHERE cliente_idcliente = ? AND habilitado = 1 "
                + "ORDER BY usuario ASC";

        JSONArray json_usuarios = new JSONArray();

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            connection = DBCnx.conexion();
            statement = connection.prepareStatement(query);

            statement.setInt(1, Integer.parseInt(id_cliente));
            result = statement.executeQuery();

            while (result.next()) {
                JSONObject obj = new JSONObject();

                obj.put("id", result.getInt("idusuario"));
                obj.put("dato", result.getString("usuario"));
                json_usuarios.put(obj);
            }

        } catch (JSONException e) {
            Utils.printOrdErr("JSONException get users by client: " + e.getMessage());
        } catch (SQLException e) {
            Utils.printOrdErr("SQLException get users by client: " + e.getMessage());
        } finally {
            DBCnx.closeAll(result, statement, connection);
        }

        return Response.status(200).entity(json_usuarios.toString()).build();
    }

    @POST
    @Path("/saveNotificationConfiguration/")
    @Produces("application/json; charset=UTF-8")
    /**
     * @param user
     *            es el id del usuario que se va al que se le van a configurar
     *            notificaciones. Sólo va este valor cuando la notificación
     *            tiene una configuración por usuario. Si es por cliente,
     *            entonces este parámetro va vacío.
     * @param clte
     *            es el id del cliente al que pertenece el usuario o los
     *            usuarios al que se le van a configurar notificaciones.
     * @param notif
     *            corresponde al tipo de notificación, los tipos están en la
     *            tabla "tipo_notificacion".
     * @param datos
     *            son los valores de la configuración de la notificación.
     */
    public Response saveNotificationConfiguration(@FormParam("user") String id_user,
            @FormParam("clte") String id_client, @FormParam("notif") String notification_type,
            @FormParam("datos") String datos, @Context final HttpServletRequest request) {

        JSONObject json_response = new JSONObject();
        JSONArray config_notificacion = new JSONArray(datos);

        int largo = config_notificacion.length();
        int response = -1;
        String id_alert_type;

        for (int ix = 0; ix < largo; ix++) {
            JSONObject json = config_notificacion.getJSONObject(ix);

            id_alert_type = json.get("id").toString();

            response = Notification.deleteConfiguration(id_alert_type, id_user, id_client);
            if (json.has("user_configs")) {
                response = Notification.saveConfiguration(json, id_alert_type, id_client, id_user, notification_type);
            } else {
                // response = Notification.deleteConfiguration(id_alert_type,
                // id_user, id_client);
            }

        }
        json_response.put("response", response);
        return Response.status(200).entity(json_response.toString()).build();
    }

    @POST
    @Path("/save_config_boletin/")
    @Produces("application/json; charset=UTF-8")
    public Response save_config_boletin(@FormParam("id") String id, @FormParam("lic") String lic,
            @FormParam("time") String time, @Context final HttpServletRequest request) {

        String delete_config = "DELETE FROM boletin_horarios WHERE id_cliente = ?";

        String query = "INSERT INTO boletin_horarios (id_cliente, hora, minutos, timezone, tipos_lic) VALUES (?,?,?, 'America/Santiago' , ?)";
        JSONObject json = new JSONObject();

        Connection connection = null;
        PreparedStatement statement_delete_config = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            connection = DBCnx.conexion();
            statement = connection.prepareStatement(query);
            statement_delete_config = connection.prepareStatement(delete_config);

            statement_delete_config.setInt(1, Integer.parseInt(id));
            statement_delete_config.executeUpdate();

            String date[] = time.split(":");

            statement.setInt(1, Integer.parseInt(id));
            statement.setInt(2, Integer.parseInt(date[0]));
            statement.setInt(3, Integer.parseInt(date[1]));
            statement.setString(4, lic);
            statement.executeUpdate();

            json.put("resp", "ok");
        } catch (JSONException e) {
            json.put("resp", "err");
            Utils.printOrdErr("JSONException guardando configuración del boletín: " + e.getMessage());
        } catch (SQLException e) {
            json.put("resp", "err");
            Utils.printOrdErr("JSONException guardando configuración del boletín: " + e.getMessage());
        } finally {
            DBCnx.close(statement);
            DBCnx.close(statement_delete_config);
            DBCnx.close(result);
            DBCnx.close(connection);
        }

        return Response.status(200).entity(json.toString()).build();
    }

    @POST
    @Path("/get_notificaciones/")
    @Produces("application/json; charset=UTF-8")
    public Response get_notificaciones(@Context final HttpServletRequest request,
            @FormParam("tipo") String tipo_notif) {

        String query = "SELECT id, alerta, tipo FROM mail_tipoAlerta WHERE tipo = ? ORDER BY id";
        JSONArray json_usuarios = new JSONArray();

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            connection = DBCnx.conexion();
            statement = connection.prepareStatement(query);
            statement.setString(1, tipo_notif);

            result = statement.executeQuery();

            while (result.next()) {
                JSONObject obj = new JSONObject();

                obj.put("id", result.getInt("id"));
                obj.put("dato", result.getString("alerta"));
                obj.put("tipo", result.getString("tipo"));
                json_usuarios.put(obj);
            }

        } catch (JSONException e) {
            Utils.printOrdErr("JSONException get_notificaciones: " + e.getMessage());
        } catch (SQLException e) {
            Utils.printOrdErr("JSONException get_notificaciones: " + e.getMessage());
        } finally {
            DBCnx.closeAll(result, statement, connection);
        }

        return Response.status(200).entity(json_usuarios.toString()).build();
    }

    @POST
    @Path("/get_configuracion_notificaciones/")
    @Produces("application/json; charset=UTF-8")
    public Response get_configuracion_notificaciones(@Context final HttpServletRequest request,
            @FormParam("tipo") String notification_type, @FormParam("clte") String client_id,
            @FormParam("user") String user_id, @FormParam("config") String config_por_cliente) {

        JSONArray notification_configuration = new JSONArray();

        if (config_por_cliente.equals("true")) {
            notification_configuration = Notification.selectConfigurationByClient(client_id, notification_type);
        } else if (config_por_cliente.equals("false")) {
            notification_configuration = Notification.selectConfigurationByUser(user_id, client_id, notification_type);
        }

        return Response.status(200).entity(notification_configuration.toString()).build();
    }

    /**
     * Retorna los usuarios de la plataforma Point, que están activos. Se
     * utiliza para la sección "Usuarios" del administrador.
     */
    @GET
    @Path("/get_usuariosAll/")
    @Produces("application/json; charset=UTF-8")
    public Response get_usuariosAll(@Context final HttpServletRequest request) {

        String query = "SELECT idusuario, perfil_idperfil, u.nombre, c.idcliente idcliente,c.nombre cliente, u.usuario, u.mail, u.habilitado FROM usuario u left join cliente c "
                + "on u.cliente_idcliente = c.idcliente ORDER BY cliente_idcliente,usuario ASC;";
        JSONArray json_usuarios = new JSONArray();

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            connection = DBCnx.conexion();
            statement = connection.prepareStatement(query);

            result = statement.executeQuery();

            while (result.next()) {

                JSONObject obj = new JSONObject();

                obj.put("id", result.getString("idusuario"));
                obj.put("idperfil", result.getString("perfil_idperfil"));
                obj.put("nom", result.getString("nombre"));
                obj.put("idclte", result.getString("idcliente"));
                obj.put("clte", result.getString("cliente"));
                obj.put("user", result.getString("usuario"));
                obj.put("mail", result.getString("mail"));

                String hab = "";
                if (result.getInt("habilitado") == 0) {
                    hab = "No";
                } else {
                    hab = "Sí";
                }

                obj.put("hab", hab);
                json_usuarios.put(obj);
            }

        } catch (JSONException e) {
            Utils.printOrdErr("JSONException get_usuariosAll: " + e.getMessage());
        } catch (SQLException e) {
            Utils.printOrdErr("SQLException get_usuariosAll: " + e.getMessage());
        } finally {
            DBCnx.closeAll(result, statement, connection);
        }

        return Response.status(200).entity(json_usuarios.toString()).build();
    }

    @GET
    @Path("/get_mercados/")
    @Produces("application/json; charset=UTF-8")
    public Response get_mercados(@Context final HttpServletRequest request) {

        String query = "SELECT id, mercado FROM " + DBCnx.db_orders + ".filtro_mercado ORDER BY mercado ASC;";
        JSONArray json_mercados = new JSONArray();

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            connection = DBCnx.conexion();
            statement = connection.prepareStatement(query);

            result = statement.executeQuery();

            while (result.next()) {
                JSONObject obj = new JSONObject();

                obj.put("id", result.getInt("id"));
                obj.put("c", result.getString("mercado"));
                json_mercados.put(obj);
            }

        } catch (JSONException e) {
            Utils.printOrdErr("JSONException get_mercados: " + e.getMessage());
        } catch (SQLException e) {
            Utils.printOrdErr("SQLException get_mercados: " + e.getMessage());
        } finally {
            DBCnx.closeAll(result, statement, connection);
        }

        return Response.status(200).entity(json_mercados.toString()).build();
    }

    @POST
    @Path("/get_mercadosUser/")
    @Produces("application/json; charset=UTF-8")
    public Response get_mercadosUser(@FormParam("id") String id, @Context final HttpServletRequest request) {

        String query = "SELECT id_usr, id_mer FROM " + DBCnx.db_orders + ".mercado_usuario WHERE id_usr = ?;";
        JSONObject json_mercados = new JSONObject();

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            connection = DBCnx.conexion();
            statement = connection.prepareStatement(query);
            statement.setString(1, id);

            result = statement.executeQuery();

            String mercados = "";
            while (result.next()) {
                mercados += result.getString("id_mer") + ",";
            }
            if (mercados.length() > 0) {
                mercados = mercados.substring(0, mercados.length() - 1);
            }
            json_mercados.put("m", mercados);

        } catch (JSONException e) {
            Utils.printOrdErr("JSONException get_mercados_user: " + e.getMessage());
        } catch (SQLException e) {
            Utils.printOrdErr("SQLException get_mercados_user: " + e.getMessage());
        } finally {
            DBCnx.closeAll(result, statement, connection);
        }

        return Response.status(200).entity(json_mercados.toString()).build();
    }

    @POST
    @Path("/get_modulosUser/")
    @Produces("application/json; charset=UTF-8")
    public Response get_modulosUser(@FormParam("id") String id, @Context final HttpServletRequest request) {

        String query = "SELECT `licitaciones`, `adj_resumen`, `adj_vistaDin`, `adj_descarga`, `oc_vistaCla`, "
                + "`oc_vistaDin`, `oc_descarga`, `reportes`, `mantenedor_catologo`, `mantenedor_mrelevantes`, "
                + "`mantenedor_asignacion`, `mantenedor_glosas` FROM `modulo_usuario` WHERE `id_usr` = ?;";
        JSONObject json_mercados = new JSONObject();

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            connection = DBCnx.conexion();
            statement = connection.prepareStatement(query);
            statement.setString(1, id);

            result = statement.executeQuery();

            String modulos = "";
            if (result.next()) {
                if (result.getInt("licitaciones") == 1) {
                    modulos += "lic,";
                }
                if (result.getInt("adj_resumen") == 1) {
                    modulos += "adj_res,";
                }
                if (result.getInt("adj_vistaDin") == 1) {
                    modulos += "adj_din,";
                }
                if (result.getInt("adj_descarga") == 1) {
                    modulos += "adj_des,";
                }
                if (result.getInt("oc_vistaCla") == 1) {
                    modulos += "oc,";
                }
                if (result.getInt("oc_vistaDin") == 1) {
                    modulos += "oc_din,";
                }
                if (result.getInt("oc_descarga") == 1) {
                    modulos += "oc_Des,";
                }
                if (result.getInt("reportes") == 1) {
                    modulos += "repo,";
                }
                if (result.getInt("mantenedor_catologo") == 1) {
                    modulos += "mant_cat,";
                }
                if (result.getInt("mantenedor_mrelevantes") == 1) {
                    modulos += "mant_rel,";
                }
                if (result.getInt("mantenedor_asignacion") == 1) {
                    modulos += "mant_asig,";
                }
                if (result.getInt("mantenedor_glosas") == 1) {
                    modulos += "mant_glo,";
                }
                if (modulos.length() > 0) {
                    modulos = modulos.substring(0, modulos.length() - 1);
                }
                json_mercados.put("m", modulos);
            }

        } catch (JSONException e) {
            Utils.printOrdErr("JSONException get Modules by user: " + e.getMessage());
        } catch (SQLException e) {
            Utils.printOrdErr("SQLException get Modules by user: " + e.getMessage());
        } finally {
            DBCnx.closeAll(result, statement, connection);
        }

        return Response.status(200).entity(json_mercados.toString()).build();
    }

    @GET
    @Path("/get_perfiles_list/")
    @Produces("application/json; charset=UTF-8")
    public Response get_perfiles(@Context final HttpServletRequest request) {

        String query = "SELECT idperfil, nombre FROM perfil ORDER BY idperfil ASC;";
        JSONArray json_perfiles = new JSONArray();

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            connection = DBCnx.conexion();
            statement = connection.prepareStatement(query);

            result = statement.executeQuery();

            while (result.next()) {
                JSONObject obj = new JSONObject();

                obj.put("id", result.getInt("idperfil"));
                obj.put("c", result.getString("nombre"));
                json_perfiles.put(obj);
            }

        } catch (JSONException e) {
            Utils.printOrdErr("JSONException get perfiles list: " + e.getMessage());
        } catch (SQLException e) {
            Utils.printOrdErr("SQLException get perfiles list: " + e.getMessage());
        } finally {
            DBCnx.closeAll(result, statement, connection);
        }

        return Response.status(200).entity(json_perfiles.toString()).build();
    }

    @GET
    @Path("/get_config_boletin/")
    @Produces("application/json; charset=UTF-8")
    public Response get_config_boletin(@Context final HttpServletRequest request) {

        JSONArray response = new JSONArray();

        int idcliente = Integer.parseInt(request.getParameter("id"));

        String select_config_boletin = "SELECT hora, minutos, tipos_lic FROM boletin_horarios WHERE id_cliente = ?";

        Connection connection = null;
        PreparedStatement statement_config_boletin = null;
        ResultSet result2 = null;

        try {
            connection = DBCnx.conexion();
            statement_config_boletin = connection.prepareStatement(select_config_boletin);
            statement_config_boletin.setInt(1, idcliente);
            result2 = statement_config_boletin.executeQuery();

            while (result2.next()) {
                JSONObject obj = new JSONObject();

                obj.put("hora", result2.getInt("hora"));
                obj.put("min", result2.getInt("minutos"));
                obj.put("lic", result2.getString("tipos_lic").replaceAll(";", ","));
                response.put(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBCnx.closeAll(result2, statement_config_boletin, connection);
        }
        return Response.status(200).entity(response.toString()).build();
    }

    @GET
    @Path("/habilitar/")
    @Produces("application/json; charset=UTF-8")
    public Response habilitar(@Context final HttpServletRequest request) {

        String idusuario = request.getParameter("user");
        String estado = request.getParameter("estado");

        String query = "UPDATE usuario SET habilitado = ? WHERE idusuario = ?";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        JSONObject json = new JSONObject();
        try {
            connection = DBCnx.conexion();
            statement = connection.prepareStatement(query);
            statement.setString(1, estado);
            statement.setString(2, idusuario);

            statement.execute();
            json.put("resp", "ok");
        } catch (JSONException e) {
            Utils.printOrdErr("JSONException habilite user: " + e.getMessage());
        } catch (SQLException e) {
            Utils.printOrdErr("SQLException habilite user: " + e.getMessage());
        } finally {
            DBCnx.closeAll(result, statement, connection);
        }

        return Response.status(200).entity(json.toString()).build();
    }

    @POST
    @Path("/crear_usuario/")
    @Produces("application/json; charset=UTF-8")
    public Response crear_usuario(@FormParam("nombre_user") String nombre_user, @FormParam("usuario") String usuario,
            @FormParam("email_user") String email_user, @FormParam("pass_user") String pass_user,
            @FormParam("cliente_user") String cliente_user, @FormParam("perfil_user") String perfil_user,
            @FormParam("mercados") String mercados, @FormParam("modulos") String modulos,
            @Context final HttpServletRequest request) {

        JSONObject json = new JSONObject();

        boolean existe_usuario = validarUsuario(Integer.parseInt(cliente_user), usuario);

        if (existe_usuario) {
            json.put("resp", "user_existe");
        } else {
            try {
                pass_user = getCadenaAlfanumAleatoria(8).toLowerCase();
                Usuario new_usuario = new Usuario(perfil_user, Integer.parseInt(cliente_user), "", nombre_user, usuario,
                        pass_user, email_user, mercados);

                ModuloUsuario modulos_usuario = get_array_modulos(modulos);

                new_usuario.setModulos(modulos_usuario);
                insertarDatos(new_usuario);

                String body = getBody(0, nombre_user, usuario, pass_user);

                Mail.SendCambiosContraseña(body, email_user);
                json.put("resp", "ok");
            } catch (Exception e) {
                json.put("resp", "error");
                e.printStackTrace();
            }
        }
        return Response.status(200).entity(json.toString()).build();
    }

    @POST
    @Path("/eliminar_usuario/")
    @Produces("application/json; charset=UTF-8")
    public Response eliminar_usuario(@FormParam("id") String id, @Context final HttpServletRequest request) {

        JSONObject json = new JSONObject();

        Connection connection = null;
        PreparedStatement statement_delete_relacion_modulo = null;
        PreparedStatement statement_delete_relacion_mercado = null;
        PreparedStatement statement_delete_relacion_limites = null;
        PreparedStatement statement_delete_usuario = null;

        String query_relacion_modulo = "DELETE FROM modulo_usuario WHERE id_usr = ?";
        String query_relacion_limites = "DELETE FROM " + DBCnx.db_orders + ".limites WHERE id_usuario = ?";
        String query_relacion_mercado = "DELETE FROM " + DBCnx.db_orders + ".mercado_usuario WHERE id_usr = ?";
        String query_usuario = "DELETE FROM usuario WHERE idusuario = ?";
        try {
            connection = DBCnx.conexion();
            connection.setAutoCommit(false);

            statement_delete_relacion_modulo = connection.prepareStatement(query_relacion_modulo);
            statement_delete_relacion_modulo.setString(1, id);

            statement_delete_relacion_mercado = connection.prepareStatement(query_relacion_mercado);
            statement_delete_relacion_mercado.setString(1, id);

            statement_delete_relacion_limites = connection.prepareStatement(query_relacion_limites);
            statement_delete_relacion_limites.setString(1, id);

            statement_delete_usuario = connection.prepareStatement(query_usuario);
            statement_delete_usuario.setString(1, id);

            statement_delete_relacion_modulo.executeUpdate();
            statement_delete_relacion_mercado.executeUpdate();
            statement_delete_relacion_limites.executeUpdate();
            statement_delete_usuario.executeUpdate();

            connection.commit();
            json.put("resp", "ok");
        } catch (Exception e) {
            json.put("resp", "error");
            e.printStackTrace();
        } finally {
            DBCnx.close(statement_delete_usuario);
            DBCnx.close(statement_delete_relacion_modulo);
            DBCnx.close(statement_delete_relacion_mercado);
            DBCnx.close(statement_delete_relacion_limites);
            DBCnx.close(connection);

        }

        return Response.status(200).entity(json.toString()).build();
    }

    @POST
    @Path("/update_usuario/")
    @Produces("application/json; charset=UTF-8")
    public Response update_usuario(@FormParam("idusuario") String idusuario,
            @FormParam("nombre_user") String nombre_user, @FormParam("usuario") String usuario,
            @FormParam("email_user") String email_user, @FormParam("pass_user") String pass_user,
            @FormParam("cliente_user") String cliente_user, @FormParam("perfil_user") String perfil_user,
            @FormParam("mercados") String mercados, @FormParam("modulos") String modulos,
            @Context final HttpServletRequest request) {

        JSONObject json = new JSONObject();

        Usuario new_usuario = new Usuario(perfil_user, Integer.parseInt(cliente_user), "", nombre_user, usuario,
                pass_user, email_user, mercados);
        ModuloUsuario modulos_usuario = get_array_modulos(modulos);

        PreparedStatement statement_update_user = null;
        PreparedStatement statement_delete_modulo_usuario = null;
        PreparedStatement statement_delete_mercado_usuario = null;
        PreparedStatement statement_delete_limites_usuario = null;

        int id_usuario = Integer.parseInt(idusuario);

        String password = "";

        Connection connection = null;

        String update_usuario = "UPDATE usuario SET perfil_idperfil = ?, cliente_idcliente=?,nombre=?, "
                + "usuario=?,password=?,mail=? WHERE idusuario =?";

        String query_insert_modulos = "INSERT INTO `modulo_usuario`(`id_usr`, `licitaciones`, "
                + "`adj_resumen`, `adj_vistaDin`, `adj_descarga`, `oc_vistaCla`, `oc_vistaDin`, "
                + "`oc_descarga`, `reportes`, `mantenedor_catologo`, `mantenedor_mrelevantes`, "
                + "`mantenedor_asignacion`, `mantenedor_glosas`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

        String query_insert_mercado_usuario = "INSERT INTO " + DBCnx.db_orders + ".`mercado_usuario`(`id_usr`, "
                + "`id_clte`, `id_mer`) VALUES (?, ?, ?)";

        String query_insert_limites_usuario = "INSERT INTO " + DBCnx.db_orders + ".`limites`(`id_usuario`, "
                + "`nombre_tabla_filtro`, `id_dato`, `id_mer`) VALUES (?, ?, ?, ?)";

        String query_delete_modulo_usuario = "DELETE FROM modulo_usuario WHERE id_usr = ?";
        String query_delete_mercado_usuario = "DELETE FROM " + DBCnx.db_orders + ".mercado_usuario WHERE id_usr = ?";
        String query_delete_limites_usuario = "DELETE FROM " + DBCnx.db_orders + ".limites WHERE id_usuario = ?";

        String password_autogenerada = "";
        // Valido si se marcó la opción de autogenerar una contraseña para el
        // usuario.
        if (pass_user.equals("true")) {
            // Autogenero una contraseña encriptada en MD5
            try {
                password_autogenerada = getCadenaAlfanumAleatoria(8).toLowerCase();

                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] messageDigest = md.digest(password_autogenerada.getBytes());
                BigInteger number = new BigInteger(1, messageDigest);
                String hashtext = number.toString(16);
                // Now we need to zero pad it if you actually want the full 32
                // chars.
                while (hashtext.length() < 32) {
                    hashtext = "0" + hashtext;
                }
                password = hashtext;

            } catch (NoSuchAlgorithmException e) {
                json.put("resp", "error");
                throw new RuntimeException(e);
            }
        } else {
            String password_old = getPassword(Integer.parseInt(cliente_user), id_usuario);
            password = password_old;
        }

        try {
            connection = DBCnx.conexion();
            connection.setAutoCommit(false);
            statement_insert_modulos_usuarios = connection.prepareStatement(query_insert_modulos);
            statement_insert_mercado_usuarios = connection.prepareStatement(query_insert_mercado_usuario);
            statement_insert_limites_usuarios = connection.prepareStatement(query_insert_limites_usuario);

            // Elimino las relaciones anteriores a los cambios
            statement_delete_modulo_usuario = connection.prepareStatement(query_delete_modulo_usuario);
            statement_delete_mercado_usuario = connection.prepareStatement(query_delete_mercado_usuario);
            statement_delete_limites_usuario = connection.prepareStatement(query_delete_limites_usuario);

            statement_delete_modulo_usuario.setInt(1, id_usuario);
            statement_delete_mercado_usuario.setInt(1, id_usuario);
            statement_delete_limites_usuario.setInt(1, id_usuario);

            statement_delete_modulo_usuario.execute();
            statement_delete_mercado_usuario.execute();
            statement_delete_limites_usuario.execute();

            // Preparo las consultas para la inserción de la nueva data
            statement_update_user = connection.prepareStatement(update_usuario);

            // Agrego los datos para la actualización del usuario
            statement_update_user.setInt(1, Integer.parseInt(perfil_user));
            statement_update_user.setInt(2, Integer.parseInt(cliente_user));
            statement_update_user.setString(3, nombre_user);
            statement_update_user.setString(4, usuario);
            statement_update_user.setString(5, password);
            statement_update_user.setString(6, email_user);
            statement_update_user.setInt(7, id_usuario);

            insertDataModulos(id_usuario, modulos_usuario);
            insertDataMercados(id_usuario, new_usuario);
            // Cuando se agreguen límites al usuario en el excel se debe
            // modificar esta función
            insertDataLimites(id_usuario, new_usuario);

            statement_update_user.execute();
            statement_insert_modulos_usuarios.executeBatch();
            statement_insert_mercado_usuarios.executeBatch();
            statement_insert_limites_usuarios.executeBatch();

            connection.commit();
            json.put("resp", "ok");

            if (pass_user.equals("true")) {
                String body = getBody(0, nombre_user, usuario, password_autogenerada);

                Mail.SendCambiosContraseña(body, email_user);
            }
        } catch (Exception e) {
            json.put("resp", "error");
            e.printStackTrace();
        } finally {
            DBCnx.close(statement_update_user);
            DBCnx.close(statement_insert_limites_usuarios);
            DBCnx.close(statement_insert_mercado_usuarios);
            DBCnx.close(statement_insert_modulos_usuarios);
            DBCnx.close(connection);
        }

        return Response.status(200).entity(json.toString()).build();
    }

    @POST
    @Path("/cliente/")
    @Produces("application/json; charset=UTF-8")
    public Response cliente(@FormParam("opc") String opc, @FormParam("id") String id, @FormParam("nom") String nombre,
            @FormParam("rut") String rut, @FormParam("razon") String razon,
            @FormParam("lic_diarias") String lic_diarias, @FormParam("glosas") String glosas,
            @Context final HttpServletRequest request) {

        JSONObject json = new JSONObject();

        PreparedStatement statement_create_clte = null;
        Connection connection = null;

        boolean clte_existe = existe_cliente(id, nombre);
        if (opc.equals("1")) {
            if (!clte_existe) {
                String query_create_clte = "INSERT INTO `cliente`(`nombre`, `rut`, `razon_social`, `licitaciones_diarias`, "
                        + "`created_at`) VALUES (?,?,?,?, now())";

                try {
                    connection = DBCnx.conexion();
                    statement_create_clte = connection.prepareStatement(query_create_clte);

                    // Agrego los datos para la actualización del usuario
                    statement_create_clte.setString(1, nombre);
                    statement_create_clte.setString(2, rut);
                    statement_create_clte.setString(3, razon);
                    statement_create_clte.setInt(4, Integer.parseInt(lic_diarias));

                    statement_create_clte.execute();

                    insertConfigCliente(nombre, glosas, 0);
                    json.put("resp", "ok");
                } catch (Exception e) {
                    json.put("resp", "error");
                    e.printStackTrace();
                } finally {
                    DBCnx.close(statement_create_clte);
                    DBCnx.close(connection);
                }
            } else {
                json.put("resp", "clte_existe");
            }
        } else if (opc.equals("2")) {
            if (!clte_existe) {
                String query_create_clte = "UPDATE cliente set nombre = ?, rut = ?, razon_social = ?, licitaciones_diarias = ?"
                        + " WHERE idcliente = ?";
                try {
                    connection = DBCnx.conexion();
                    connection.setAutoCommit(false);
                    statement_create_clte = connection.prepareStatement(query_create_clte);

                    // Agrego los datos para la actualización del usuario
                    statement_create_clte.setString(1, nombre);
                    statement_create_clte.setString(2, rut);
                    statement_create_clte.setString(3, razon);
                    statement_create_clte.setInt(4, Integer.parseInt(lic_diarias));
                    statement_create_clte.setString(5, id);

                    statement_create_clte.execute();
                    insertConfigCliente(nombre, glosas, Integer.parseInt(id));
                    connection.commit();
                    json.put("resp", "ok");
                } catch (Exception e) {
                    json.put("resp", "error");
                    e.printStackTrace();
                } finally {
                    DBCnx.close(statement_create_clte);
                    DBCnx.close(connection);
                }
            } else {
                json.put("resp", "clte_existe");
            }
        }

        return Response.status(200).entity(json.toString()).build();
    }

    private static boolean existe_cliente(String id, String nombre) {

        boolean existe = false;

        PreparedStatement consulta = null;
        ResultSet resultados = null;
        Connection conexion = null;

        try {
            String query = "";
            // Si el id es vacío entonces es una validación para un nuevo
            // cliente.
            if (id.equals("")) {
                query = "SELECT idcliente FROM cliente WHERE nombre = ?";
                conexion = DBCnx.conexion();
                consulta = conexion.prepareStatement(query);
                consulta.setString(1, nombre);
            } else {
                // Si el id no es vacío entonces la validación es para un update
                // de cliente.
                query = "SELECT idcliente FROM cliente WHERE nombre = ? AND idcliente != ?";
                conexion = DBCnx.conexion();
                consulta = conexion.prepareStatement(query);
                consulta.setString(1, nombre);
                consulta.setString(2, id);
            }

            resultados = consulta.executeQuery();

            if (resultados.next()) {
                existe = true;
            }
        } catch (Exception error) {
            error.printStackTrace();
        } finally {
            DBCnx.closeAll(resultados, consulta, conexion);
        }
        return existe;
    }

    private static void insertConfigCliente(String nombre, String glosa, int id) {

        PreparedStatement statement_select_clte = null;
        PreparedStatement statement_insert_config = null;
        Connection connection = null;
        ResultSet result = null;

        String query_get_cliente = "SELECT idcliente FROM cliente WHERE nombre = ?";
        String query_insert_config_cliente = "INSERT INTO `cliente_configuracion`(`id_cliente`, "
                + "`glosa_despacho`) VALUES (?, ?)";
        String query_update_config_cliente = "UPDATE `cliente_configuracion` SET glosa_despacho = ? "
                + "WHERE id_cliente = ?";
        if (id == 0) {
            try {
                connection = DBCnx.conexion();
                statement_select_clte = connection.prepareStatement(query_get_cliente);
                statement_insert_config = connection.prepareStatement(query_insert_config_cliente);
                // Agrego los datos para la actualización del usuario
                statement_select_clte.setString(1, nombre);

                result = statement_select_clte.executeQuery();
                if (result.next()) {
                    int idcliente = result.getInt("idcliente");
                    statement_insert_config.setInt(1, idcliente);
                    statement_insert_config.setInt(2, Integer.parseInt(glosa));
                    statement_insert_config.execute();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                DBCnx.close(statement_insert_config);
                DBCnx.close(statement_select_clte);
                DBCnx.close(result);
                DBCnx.close(connection);
            }
        } else {
            try {
                connection = DBCnx.conexion();
                statement_insert_config = connection.prepareStatement(query_update_config_cliente);

                statement_insert_config.setInt(1, Integer.parseInt(glosa));
                statement_insert_config.setInt(2, id);
                statement_insert_config.execute();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                DBCnx.close(statement_insert_config);
                DBCnx.close(connection);
            }
        }
    }

    private static ModuloUsuario get_array_modulos(String modulos) {
        ModuloUsuario modulo = new ModuloUsuario(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

        String mod_aux[] = modulos.replace("[", "").replace("]", "").replace("\"", "").split(",");

        for (int ix = 0; ix < mod_aux.length; ix++) {
            switch (mod_aux[ix]) {
            case "lic":
                modulo.setLic(1);
                break;
            case "adj_res":
                modulo.setAdj_resumen(1);
                break;
            case "adj_din":
                modulo.setAdj_dinamico(1);
                break;
            case "adj_des":
                modulo.setAdj_resumen(1);
                break;
            case "oc":
                modulo.setOc_clasico(1);
                break;
            case "oc_din":
                modulo.setOc_dinamico(1);
                break;
            case "oc_Des":
                modulo.setOc_descargas(1);
                break;
            case "repo":
                modulo.setReportes(1);
                break;
            case "mant_cat":
                modulo.setMantenedor_catalogo(1);
                break;
            case "mant_rel":
                modulo.setMantenedor_mercado(1);
                break;
            case "mant_asig":
                modulo.setMantenedor_asignacion(1);
                break;
            case "mant_glo":
                modulo.setMantenedor_glosas(1);
                break;
            }
        }

        return modulo;
    }

    private static void insertarDatos(Usuario u) {

        int last_id = getLastId();

        Connection conexion = null;

        String query_insert_usuarios = "INSERT INTO `usuario`(`idusuario`, `perfil_idperfil`, "
                + "`cliente_idcliente`, `nombre`, `usuario`, `password`, `mail`, `habilitado`, "
                + "`created_at`) VALUES (?, ?, ?, ?, ?, md5(?), ?, 1, now())";

        String query_insert_modulos = "INSERT INTO `modulo_usuario`(`id_usr`, `licitaciones`, "
                + "`adj_resumen`, `adj_vistaDin`, `adj_descarga`, `oc_vistaCla`, `oc_vistaDin`, "
                + "`oc_descarga`, `reportes`, `mantenedor_catologo`, `mantenedor_mrelevantes`, "
                + "`mantenedor_asignacion`, `mantenedor_glosas`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

        String query_insert_mercado_usuario = "INSERT INTO " + DBCnx.db_orders + ".`mercado_usuario`(`id_usr`, "
                + "`id_clte`, `id_mer`) VALUES (?, ?, ?)";

        String query_insert_limites_usuario = "INSERT INTO " + DBCnx.db_orders + ".`limites`(`id_usuario`, "
                + "`nombre_tabla_filtro`, `id_dato`, `id_mer`) VALUES (?, ?, ?, ?)";

        try {

            conexion = DBCnx.conexion();
            statement_insert_usuarios = conexion.prepareStatement(query_insert_usuarios);
            statement_insert_modulos_usuarios = conexion.prepareStatement(query_insert_modulos);
            statement_insert_mercado_usuarios = conexion.prepareStatement(query_insert_mercado_usuario);
            statement_insert_limites_usuarios = conexion.prepareStatement(query_insert_limites_usuario);

            ModuloUsuario modulos = u.getModulos();

            int id_perfil = Integer.parseInt(u.getPerfil());

            // Batch con la informacion del usuario
            statement_insert_usuarios.setInt(1, last_id);
            statement_insert_usuarios.setInt(2, id_perfil);
            statement_insert_usuarios.setInt(3, u.getIdcliente());
            statement_insert_usuarios.setString(4, u.getNombre());
            statement_insert_usuarios.setString(5, u.getUsuario());
            statement_insert_usuarios.setString(6, u.getContraseña());
            statement_insert_usuarios.setString(7, u.getEmail());

            statement_insert_usuarios.addBatch();

            insertDataModulos(last_id, modulos);
            insertDataMercados(last_id, u);
            // Cuando se agreguen límites al usuario en el excel se debe
            // modificar esta función
            insertDataLimites(last_id, u);

            statement_insert_usuarios.executeBatch();
            statement_insert_modulos_usuarios.executeBatch();
            statement_insert_mercado_usuarios.executeBatch();
            statement_insert_limites_usuarios.executeBatch();
        } catch (Exception error) {
            error.printStackTrace();
        } finally {
            DBCnx.close(statement_insert_usuarios);
            DBCnx.close(statement_insert_modulos_usuarios);
            DBCnx.close(statement_insert_mercado_usuarios);
            DBCnx.close(statement_insert_limites_usuarios);
            DBCnx.close(conexion);
        }
    }

    private static int getLastId() {

        int last_id = 0;

        PreparedStatement consulta = null;
        ResultSet resultados = null;
        Connection conexion = null;

        try {

            String query = "SELECT MAX(idusuario) last_id FROM usuario";
            conexion = DBCnx.conexion();
            consulta = conexion.prepareStatement(query);

            resultados = consulta.executeQuery();

            if (resultados.next()) {
                last_id = resultados.getInt("last_id") + 1;
            }
        } catch (Exception error) {
            error.printStackTrace();
        } finally {
            DBCnx.closeAll(resultados, consulta, conexion);
        }

        return last_id;
    }

    private static void insertDataModulos(int id, ModuloUsuario modulos) {
        // Batch con la información de los modulos del usuario
        try {
            statement_insert_modulos_usuarios.setInt(1, id);
            statement_insert_modulos_usuarios.setInt(2, modulos.getLic());
            statement_insert_modulos_usuarios.setInt(3, modulos.getAdj_resumen());
            statement_insert_modulos_usuarios.setInt(4, modulos.getAdj_dinamico());
            statement_insert_modulos_usuarios.setInt(5, modulos.getAdj_descarga());
            statement_insert_modulos_usuarios.setInt(6, modulos.getOc_clasico());
            statement_insert_modulos_usuarios.setInt(7, modulos.getOc_dinamico());
            statement_insert_modulos_usuarios.setInt(8, modulos.getOc_descargas());
            statement_insert_modulos_usuarios.setInt(9, modulos.getReportes());
            statement_insert_modulos_usuarios.setInt(10, modulos.getMantenedor_catalogo());
            statement_insert_modulos_usuarios.setInt(11, modulos.getMantenedor_mercado());
            statement_insert_modulos_usuarios.setInt(12, modulos.getMantenedor_asignacion());
            statement_insert_modulos_usuarios.setInt(13, modulos.getMantenedor_glosas());

            statement_insert_modulos_usuarios.addBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertDataMercados(int id, Usuario usuario) {
        // Batch con la información de los modulos del usuario
        try {
            String mercado = usuario.getMercado();
            String m[] = mercado.replace("[", "").replace("]", "").replace("\"", "").split(",");

            for (int ix = 0; ix < m.length; ix++) {
                statement_insert_mercado_usuarios.setInt(1, id);
                statement_insert_mercado_usuarios.setInt(2, usuario.getIdcliente());
                statement_insert_mercado_usuarios.setInt(3, Integer.parseInt(m[ix]));

                statement_insert_mercado_usuarios.addBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertDataLimites(int id, Usuario usuario) {
        // Batch con la información de los límites del usuario
        try {
            String mercado[] = usuario.getMercado().replace("[", "").replace("]", "").replace("\"", "").split(",");

            for (int ix = 0; ix < mercado.length; ix++) {

                int iddato = Integer.parseInt(mercado[ix]);

                statement_insert_limites_usuarios.setInt(1, id);
                // Acá se debe cambiar para cuando los usuarios tengan límites.
                // Se
                // debe poner el
                // nombre de la tabla del límites. El id del dato en dicha
                // tabla, y
                // el id del mercado al
                // que corresponde el dato.
                statement_insert_limites_usuarios.setString(2, "filtro_mercado");
                statement_insert_limites_usuarios.setInt(3, iddato);
                statement_insert_limites_usuarios.setInt(4, iddato);

                statement_insert_limites_usuarios.addBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean validarUsuario(int idcliente, String usuario) {
        boolean existe = false;

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        String query = "SELECT password FROM `usuario` WHERE usuario = ? AND cliente_idcliente = ?;";
        try {
            connection = DBCnx.conexion();

            statement = connection.prepareStatement(query);
            statement.setString(1, usuario);
            statement.setInt(2, idcliente);

            result = statement.executeQuery();

            if (result.next()) {
                existe = true;
            }
        } catch (SQLException e) {
            Utils.print("Error obteniendo id: " + e.getMessage());
        } finally {
            DBCnx.closeAll(result, statement, connection);
        }

        return existe;
    }

    private static String getPassword(int idcliente, int idusuario) {
        String password = "";

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        String query = "SELECT password FROM `usuario` WHERE idusuario = ? AND cliente_idcliente = ?;";
        try {
            connection = DBCnx.conexion();

            statement = connection.prepareStatement(query);
            statement.setInt(1, idusuario);
            statement.setInt(2, idcliente);

            result = statement.executeQuery();

            if (result.next()) {
                password = result.getString("password");
            }
        } catch (SQLException e) {
            Utils.print("Error obteniendo id: " + e.getMessage());
        } finally {
            DBCnx.closeAll(result, statement, connection);
        }

        return password;
    }

    String getCadenaAlfanumAleatoria(int longitud) {
        String cadenaAleatoria = "";
        long milis = new java.util.GregorianCalendar().getTimeInMillis();
        Random r = new Random(milis);
        int i = 0;
        while (i < longitud) {
            char c = (char) r.nextInt(255);
            if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z')) {
                cadenaAleatoria += c;
                i++;
            }
        }
        return cadenaAleatoria;
    }

    /*
     * OPC: Corresponde a si el body es del mail para la función de crear o
     * modifcar usuarios- 0: Crear 1: Modificar
     */
    private static String getBody(int opc, String nombre, String user, String pass) {
        String body = "<div><table style='width:600px;border-collapse:collapse' border='0' "
                + "cellpadding='0' cellspacing='0' width='100%'><tbody><tr><td style='padding:6.75pt 0cm;"
                + "background-image:initial;background-repeat:initial' valign='top'><table style='width:600px;"
                + "border-collapse:collapse' border='0' cellpadding='0' cellspacing='0' width='100%'>"
                + "<tbody><tr><td style='padding:0cm' valign='top'><table style='width:600px;border-collapse:collapse' "
                + "align='left' border='0' cellpadding='0' cellspacing='0' width='100%'><tbody><tr><td style='padding:0cm' "
                + "valign='top'><p class='MsoNormal' style='text-align:center' align='center'><img class='CToWUd' "
                + "src='https://ci5.googleusercontent.com/proxy/vlKPD6AoAs6hQ6674ufz9gS3HZ623ibces_bHTsGvsMu53tMBrSU3M8vnzUaC9uVNmmtT1F56j6qKuojcSFAwn8gwCgq6TDOgFSZmdlmTH5-GSXs96a31TqCUEXjxursUV7hAj_PEE5Qqp7088yfBqF_9FfkdPgNLQ7jjwA=s0-d-e1-ft#https://gallery.mailchimp.com/7844194257932eb587e09ffed/images/f9c74e32-9545-4462-a022-5b0e024c0ec4.png' "
                + "border='0' width='600'><u></u><u></u></p><p class='MsoNormal' style='text-align:center' "
                + "align='center'><br></p></td></tr></tbody></table></td></tr></tbody></table></td></tr>";

        String saludo_user = " <tr><td style='padding:0cm 0cm 15pt;background:rgb(248,249,252)' "
                + "valign='top'><table style='width:600px;border-collapse:collapse;min-width:100%' "
                + "border='0' cellpadding='0' cellspacing='0' width='100%'><tbody><tr><td style='padding:13.5pt;"
                + "min-width:100%'><table style='width:564px;border-collapse:collapse;min-width:100%' "
                + "border='0' cellpadding='0' cellspacing='0' width='100%'><tbody><tr><td style='padding:0cm;"
                + "min-width:100%'></td></tr></tbody></table></td></tr></tbody></table><p class='MsoNormal'>"
                + "<u></u>&nbsp;<u></u></p><table style='width:600px;border-collapse:collapse;min-width:100%' "
                + "border='0' cellpadding='0' cellspacing='0' width='100%'><tbody><tr><td style='padding:0cm' "
                + "valign='top'><table style='width:600px;border-collapse:collapse' align='left' border='0' "
                + "cellpadding='0' cellspacing='0' width='100%'><tbody><tr><td style='padding:6.75pt 13.5pt;"
                + "min-width:100%' valign='top'><h1>Hola " + nombre + "<u></u></h1></td></tr></tbody></table></td></tr>"
                + "</tbody></table></td></tr>";

        String datos_user1 = "<tr><td style='border-style:none none solid;border-bottom-color:rgb(234,234,234);border-bottom-width:1.5pt;padding:0cm 0cm 6.75pt;background:rgb(248,249,252)' valign='top'><table style='width:600px;border-collapse:collapse;min-width:100%' border='0' cellpadding='0' cellspacing='0' width='100%'><tbody><tr><td style='padding:0cm;min-width:100%' valign='top'><table style='width:600px;border-collapse:collapse' align='left' border='0' cellpadding='0' cellspacing='0' width='100%'><tbody><tr><td style='padding:6.75pt 13.5pt;min-width:100%' valign='top'><p class='MsoNormal'><font face='Helvetica, sans-serif' color='#202020'><span style='line-height:19.5px'>";
        String msje = "";
        if (opc == 0) {
            msje = "Tus nuevas claves de acceso a&nbsp;Point son las siguientes:&nbsp;</span></font><br>"
                    + "<font face='Helvetica, sans-serif' color='#202020'><span style='line-height:19.5px'>"
                    + "Usuario: <b>" + user + "</b>&nbsp;</span></font><br><font face='Helvetica, "
                    + "sans-serif' color='#202020'><span style='line-height:19.5px'>Clave: <b>" + pass + "</b>"
                    + "</span></font><br>";
        } else if (opc == 1) {
            msje = "Tus nuevas claves de acceso a&nbsp;Point son las siguientes:&nbsp;</span></font><br>"
                    + "<font face='Helvetica,sans-serif' color='#202020'><span style='line-height:19.5px'>"
                    + "Clave: <b>" + pass + "</b></span></font><br>";
        }
        String footer = "<br><font face='Helvetica, sans-serif' color='#202020'><span style='line-height:19.5px'>Si tienes algún problema&nbsp;para ingresar, no dudes en contactarnos a&nbsp;</span></font><a href='mailto:bi@techk.cl' style='color:rgb(32,32,32);font-family:Helvetica,sans-serif;line-height:19.5px' target='_blank'>bi@techk.cl</a><br><br><span style='line-height:23px;font-size:11.5pt;font-family:Helvetica,sans-serif;color:rgb(32,32,32)'>Saluda Atte.<br><em><b>Equipo Tech-</b></em></span><em style='line-height:19.5px'><b><span style='font-size:11.5pt;line-height:23px;font-family:Helvetica,sans-serif;color:red'>K</span></b></em><span style='line-height:19.5px;font-family:Helvetica,sans-serif;color:rgb(32,32,32)'><u></u><u></u></span></p></td></tr></tbody></table></td></tr></tbody></table><p class='MsoNormal'><u></u>&nbsp;<u></u></p><table style='width:600px;border-collapse:collapse;min-width:100%' border='0' cellpadding='0' cellspacing='0' width='100%'><tbody><tr><td style='padding:13.5pt;min-width:100%'><table style='width:564px;border-collapse:collapse;min-width:100%' border='0' cellpadding='0' cellspacing='0' width='100%'><tbody><tr><td style='padding:0cm;min-width:100%'></td></tr></tbody></table></td></tr></tbody></table><p class='MsoNormal'><u></u>&nbsp;<u></u></p><table style='width:600px;border-collapse:collapse;min-width:100%' border='0' cellpadding='0' cellspacing='0' width='100%'><tbody><tr><td style='padding:0cm 13.5pt 13.5pt' valign='top'><div align='center'><table style='border-collapse:collapse;background:rgb(236,14,14)' border='0' cellpadding='0' cellspacing='0'><tbody><tr><td style='padding:11.25pt;border-radius:3px'><p class='MsoNormal' style='text-align:center' align='center'><span style='font-family:Arial,sans-serif'><a href='https://point.techk.cl' title='Ingresar' target='_blank' data-saferedirecturl='https://www.google.com/url?hl=es&amp;q=http://techk.us10.list-manage.com/track/click?u%3D7844194257932eb587e09ffed%26id%3Daeaa754034%26e%3Df6fa41120e&amp;source=gmail&amp;ust=1468955418735000&amp;usg=AFQjCNHoUBVWb8wW7NElJKJ0k2DSGvR2zg'><b><span style='color:white;text-decoration:none'>Ingresar</span></b></a>&nbsp;<u></u><u></u></span></p></td></tr></tbody></table></div></td></tr></tbody></table><p class='MsoNormal'><u></u>&nbsp;<u></u></p><table style='width:600px;border-collapse:collapse;min-width:100%' border='0' cellpadding='0' cellspacing='0' width='100%'><tbody><tr><td style='padding:0cm' valign='top'><table style='width:600px;border-collapse:collapse' align='left' border='0' cellpadding='0' cellspacing='0' width='100%'><tbody><tr><td style='padding:0cm;min-width:100%' valign='top'><p class='MsoNormal' style='text-align:center' align='center'><img class='CToWUd' src='https://ci4.googleusercontent.com/proxy/wJvHASPJDUKjK8xRE-o5mklThzHqs4dAUGQe1OL5OJuSkgFnH_TBc7Epr3k6ubki6RinCZ7fVdA_5M1WuEs5UCvxyySD_Oh8kniJJdyJO9Of1d-odDGxKv-MP0upj0HyOMrdmsfeI8Rdm2n47Ju7eXrG6ViXm7wDLeMl0EI=s0-d-e1-ft#https://gallery.mailchimp.com/7844194257932eb587e09ffed/images/8f607460-8a25-4087-82dd-94e16bc6d39a.png' border='0' width='600'><b style='font-size:12.8px'><span style='font-size:14pt;font-family:Arial,sans-serif'>Quieres capacitarte en el uso de&nbsp;<font color='#000000'>P</font><font color='#ff0000'>O</font><font color='#000000'>INT</font></span></b><span style='font-size:14pt;font-family:Arial,sans-serif'>?&nbsp;</span><b style='font-size:12.8px'><span style='font-size:14pt;font-family:Arial,sans-serif'>Tenemos un programa para ti!!!</span></b><u></u><u></u></p><p class='MsoNormal' style='font-size:12.8px'><span style='font-size:9.5pt;font-family:Arial,sans-serif'>&nbsp;</span></p><p class='MsoNormal' style='font-size:12.8px'><span style='font-size:9.5pt;font-family:Arial,sans-serif'>&nbsp;</span></p><p class='MsoNormal' style='text-align:center;font-size:12.8px'><span style='font-size:9.5pt;font-family:Arial,sans-serif'>Escribe un correo a &nbsp;<a href='mailto:bi@techk.cl' target='_blank'>bi@techk.cl</a>&nbsp;e indícanos qué día desea asistir.<u></u><u></u></span></p><p class='MsoNormal' style='font-size:12.8px'><span style='font-size:9.5pt;font-family:Arial,sans-serif'>&nbsp;</span></p></td></tr></tbody></table></td></tr></tbody></table></td></tr><tr><td style='padding:18.75pt 0cm 6.75pt;background-image:initial;background-repeat:initial' valign='top'><table style='width:600px;border-collapse:collapse;min-width:100%' border='0' cellpadding='0' cellspacing='0' width='100%'><tbody><tr><td style='padding:0cm;min-width:100%' valign='top'><table style='width:600px;border-collapse:collapse' align='left' border='0' cellpadding='0' cellspacing='0' width='100%'><tbody><tr><td style='padding:6.75pt 13.5pt;min-width:100%' valign='top'><p class='MsoNormal' style='font-size:12.8px;text-align:center;line-height:19.5px' align='center'><em><span style='font-size:9pt;line-height:18px;font-family:Helvetica,sans-serif;color:rgb(101,101,101)'>Copyright © 2016 Tech-K, Todos los derechos reservados.&nbsp;</span></em><span style='font-size:9pt;line-height:18px;font-family:Helvetica,sans-serif;color:rgb(101,101,101)'><br>Estás suscrito a la lista de Tech-K<br><br><strong>Nuestra dirección:</strong><u></u><u></u></span></p><div style='font-size:12.8px'><p class='MsoNormal' style='text-align:center;line-height:19.5px' align='center'><span style='font-size:9pt;line-height:18px;font-family:Helvetica,sans-serif;color:rgb(101,101,101)'>Tech-K</span><span style='font-size:9pt;line-height:18px;font-family:Helvetica,sans-serif;color:rgb(101,101,101)'><u></u><u></u></span></p><div><div><p class='MsoNormal' style='text-align:center;line-height:19.5px' align='center'><span style='font-size:9pt;line-height:18px;font-family:Helvetica,sans-serif;color:rgb(101,101,101)'>Cerro el Plomo 5420, oficina 601, Las Condes, Santiago.<u></u><u></u></span></p></div><p class='MsoNormal' style='text-align:center;line-height:19.5px' align='center'><span style='font-size:9pt;line-height:18px;font-family:Helvetica,sans-serif;color:rgb(101,101,101)'>Santiago</span><span style='font-size:9pt;line-height:18px;font-family:Helvetica,sans-serif;color:rgb(101,101,101)'>&nbsp;7560742<u></u><u></u></span></p><div><p class='MsoNormal' style='text-align:center;line-height:19.5px' align='center'><span style='font-size:9pt;line-height:18px;font-family:Helvetica,sans-serif;color:rgb(101,101,101)'>Chile<u></u><u></u></span></p></div></div></div></td></tr></tbody></table></td></tr></tbody></table></td></tr></tbody></table></div>";

        body = body + saludo_user + datos_user1 + msje + footer;

        return body;
    }

}
