package cl.techk.ext.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import cl.techk.ext.utils.Utils;

public class DataManager {

    public static JSONArray getDataArray(String query) {
        JSONArray array = new JSONArray();
        Connection cnx = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {

            cnx = DBCnx.conexion();
            stmt = cnx.createStatement();
            rs = stmt.executeQuery(query);

            while (rs.next()) {
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnsNumber = rsmd.getColumnCount();
                JSONObject json_data = new JSONObject();
                for (int i = 1; i <= columnsNumber; i++) {
                    json_data.put(rsmd.getColumnLabel(i), rs.getString(rsmd.getColumnLabel(i)));
                }
                array.put(json_data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCnx.closeAll(rs, stmt, cnx);
        }
        return array;
    }

    public static void getDataObject(JSONObject object, String query) {
        Connection cnx = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            cnx = DBCnx.conexion();
            stmt = cnx.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnsNumber = rsmd.getColumnCount();
                for (int i = 1; i <= columnsNumber; i++) {
                    object.put(rsmd.getColumnLabel(i), rs.getString(rsmd.getColumnLabel(i)));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCnx.closeAll(rs, stmt, cnx);
        }
    }

    public static void deleteData(JSONObject object, String query) {
        Connection cnx = null;
        Statement stmt = null;
        try {
            cnx = DBCnx.conexion();
            stmt = cnx.createStatement();
            stmt.executeUpdate(query);
        } catch (JSONException e) {
            Utils.printOrdErr("Error borrando data ");
        } catch (SQLException e) {
            Utils.printOrdErr("Error borrando data ");
        } finally {
            DBCnx.close(stmt);
            DBCnx.close(cnx);
        }
    }

    public static String deleteData(String query) {
        String result = "No se ejecuto la acción.";
        Connection cnx = null;
        Statement stmt = null;
        try {
            cnx = DBCnx.conexion();
            stmt = cnx.createStatement();
            stmt.executeUpdate(query);
            result = "Ejecutado correctamente.";

        } catch (SQLException e) {
            Utils.printOrdErr("Error ejecutando proceso.");
            result = "Error borrando data. Error code:" + e.getErrorCode();
            e.printStackTrace();
        } finally {
            DBCnx.close(stmt);
            DBCnx.close(cnx);
        }
        return result;
    }

    public static void deleteData(String tabla, String col, int id) {

        String query = "DELETE FROM " + tabla + " WHERE " + col + " = ?";
        Connection cnx = null;
        PreparedStatement stmt = null;

        try {
            cnx = DBCnx.conexion();
            stmt = cnx.prepareStatement(query);
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (JSONException | SQLException e) {
            Utils.printOrdErr("Error borrando data en " + tabla + ": " + e.getMessage());
        } finally {
            DBCnx.close(stmt);
            DBCnx.close(cnx);
        }
    }

    public static int getFilterId(String table, String columna, String valor) {
        Connection cnx = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        int id = 0;
        try {
            cnx = DBCnx.conexion();
            String query = "SELECT id FROM " + table + " AS com WHERE com." + columna + " = '" + valor + "'";
            stmt = cnx.prepareStatement(query);
            result = stmt.executeQuery(query);

            if (result.next()) {
                id = result.getInt("id");
            }
        } catch (SQLException e) {
            Utils.printOrdErr("Error obteniendo id: " + e.getMessage());
        } finally {
            DBCnx.closeAll(result, stmt, cnx);
        }
        return id;
    }

    public static void insertData(String query) {
        Connection cnx = null;
        Statement stmt = null;
        try {
            cnx = DBCnx.conexion();
            stmt = cnx.createStatement();
            stmt.executeUpdate(query);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                Utils.printOrdErr("Dato Duplicado no se ingresará");
            } else {
                e.printStackTrace();
            }
        } finally {
            DBCnx.close(stmt);
            DBCnx.close(cnx);
        }
    }

    public static void insertData2(String query, String mod) {
        Connection cnx = null;
        Statement stmt = null;
        try {
            cnx = DBCnx.conexion();
            stmt = cnx.createStatement();
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                if (mod.equals("adj")) {
                    Utils.printAdjErr("Dato Duplicado no se ingresará");
                } else if (mod.equals("ord")) {
                    Utils.printOrdErr("Dato Duplicado no se ingresará");
                } else {
                    Utils.printLicErr("Dato Duplicado no se ingresará");
                }
            } else {
                if (mod.equals("adj")) {
                    Utils.printAdjErr(e.getMessage());
                } else if (mod.equals("ord")) {
                    Utils.printOrdErr(e.getMessage());
                } else {
                    Utils.printLicErr(e.getMessage());
                }
            }
        } finally {
            DBCnx.close(stmt);
            DBCnx.close(cnx);
        }
    }

    public static String getLastDate(String bdd) {
        Statement consulta = null;
        ResultSet resultados = null;
        Connection conexion = null;

        String fecha = "";

        try {
            conexion = DBCnx.conexion();
            consulta = conexion.createStatement();

            String query = "SELECT LAST_DAY(fecha_fin) fechaEnvio FROM " + bdd + ".ultima_fecha;";

            resultados = consulta.executeQuery(query);
            while (resultados.next()) {
                fecha = resultados.getString("fechaEnvio");
            }
        } catch (Exception error) {
            Utils.printOrdErr("Error Obteniendo f: " + error.getMessage());
        } finally {
            DBCnx.closeAll(resultados, consulta, conexion);
        }

        return fecha;
    }

    public static String traspasarFavoritos(String bdd) {
        Statement consulta = null;
        Connection conexion = null;

        String response = "";

        Date ini = new Date();
        try {
            conexion = DBCnx.conexion();
            consulta = conexion.createStatement();

            Utils.printOrdDeb("Borrando datos de favoritos");
            String query = "DELETE FROM stage_" + bdd + ".favorito_datos;";
            consulta.executeUpdate(query);
            Utils.printOrdDeb("Traspasando datos de favoritos");
            String traspaso_fav_datos = "INSERT INTO stage_" + bdd + ".`favorito_datos`(`id`, `id_fav`, `id_usuario`, "
                    + "`alias`, `id_dato`, `dato`) SELECT `id`, `id_fav`, `id_usuario`, `alias`, `id_dato`, `dato` FROM "
                    + "" + bdd + ".`favorito_datos`;";
            consulta.executeUpdate(traspaso_fav_datos);

            Utils.printOrdDeb("Borrando favoritos de usuarios");
            String delete_user_favs = "DELETE FROM stage_" + bdd + ".`usuario_favoritos`;";
            consulta.executeUpdate(delete_user_favs);

            Utils.printOrdDeb("Traspasando favoritos de usuarios");
            String traspaso_user_favs = "INSERT INTO stage_" + bdd + ".`usuario_favoritos`(`id`, `id_usr`, `nombre`) "
                    + "SELECT `id`, `id_usr`, `nombre` FROM " + bdd + ".`usuario_favoritos`";
            consulta.executeUpdate(traspaso_user_favs);

            Utils.printOrdDeb("Borrando límites de usuarios");
            String delete_user_limits = "DELETE FROM stage_" + bdd + ".limites;";
            consulta.executeUpdate(delete_user_limits);

            Utils.printOrdDeb("Traspasando límites de usuarios");
            String traspaso_user_limits = "INSERT INTO stage_" + bdd
                    + ".`limites`(`id`, `id_usuario`, `nombre_tabla_filtro`, `id_dato`, `id_mer`) "
                    + "SELECT `id`, `id_usuario`, `nombre_tabla_filtro`, `id_dato`, `id_mer` FROM " + bdd
                    + ".`limites`";
            consulta.executeUpdate(traspaso_user_limits);

            Utils.printOrdDeb("Borrando mercados de usuarios");
            String delete_user_mercados = "DELETE FROM stage_" + bdd + ".mercado_usuario;";
            consulta.executeUpdate(delete_user_mercados);

            Utils.printOrdDeb("Traspasando mercados de usuarios");
            String traspaso_user_mercados = "INSERT INTO stage_" + bdd
                    + ".`mercado_usuario`(`id`, `id_usr`, `id_clte`, `id_mer`) "
                    + "SELECT `id`, `id_usr`, `id_clte`, `id_mer` FROM " + bdd + ".`mercado_usuario`";
            consulta.executeUpdate(traspaso_user_mercados);

            Date fin = new Date();
            double tiempo_total = (fin.getTime() - ini.getTime()) / 1000 / 60;
            Utils.printOrdDeb("Tiempo Traspaso de favoritos: " + tiempo_total);
            response = "ok";
        } catch (Exception e) {
            Utils.printOrdErr("Error Traspasando a favoritos:" + e.getMessage());
            response = "error";
        } finally {
            DBCnx.close(consulta);
            DBCnx.close(conexion);
        }

        return response;
    }

    public static String getCliente(String idcliente) {
        String cliente = "";

        Statement consulta = null;
        ResultSet resultados = null;
        Connection conexion = null;

        try {
            conexion = DBCnx.conexion();
            consulta = conexion.createStatement();

            String query = "SELECT nombre FROM cliente WHERE idcliente = " + idcliente + ";";

            resultados = consulta.executeQuery(query);
            while (resultados.next()) {
                cliente = resultados.getString("nombre");
            }
        } catch (Exception error) {
            Utils.printOrdErr("Error Obteniendo cliente: " + error.getMessage());
        } finally {
            DBCnx.closeAll(resultados, consulta, conexion);
        }

        return cliente;
    }

    public static String get_diferencia_array(String tipos1, String tipos2) {
        String dif = "";
        List<String> diferencia = new ArrayList<String>();

        List<String> lista_tipos1 = new ArrayList<String>(Arrays.asList(tipos1.split(";")));
        List<String> lista_tipos2 = new ArrayList<String>(Arrays.asList(tipos2.split(";")));

        for (String s : lista_tipos2) {
            if (!lista_tipos1.contains(s)) {
                diferencia.add(s);
                dif += s + ";";
            }
        }
        if (dif != "") {
            dif = dif.substring(0, dif.length() - 1);
        }

        return dif;
    }

    public static int insertQueryGetId(PreparedStatement statement) {
        int inserted_id = -1;

        Connection connection = null;
        ResultSet resultSet = null;
        try {
            connection = DBCnx.conexion();
            statement.executeUpdate();

            resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                inserted_id = resultSet.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            inserted_id = -1;
        } finally {
            DBCnx.closeAll(resultSet, statement, connection);
        }
        return inserted_id;
    }

    public static int userExists(String username) {
        int user_id = 0;

        Connection connection = null;
        PreparedStatement statement_insert_data = null;
        ResultSet result = null;

        String query_find_user = "SELECT idusuario FROM usuario WHERE usuario = ?";
        try {
            connection = DBCnx.conexion();
            statement_insert_data = connection.prepareStatement(query_find_user);
            statement_insert_data.setString(1, username);

            result = statement_insert_data.executeQuery();
            if (result.next()) {
                user_id = result.getInt("idusuario");
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            DBCnx.closeAll(result, statement_insert_data, connection);
        }
        return user_id;
    }
}
