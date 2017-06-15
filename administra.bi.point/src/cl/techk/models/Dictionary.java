package cl.techk.models;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import com.oreilly.servlet.MultipartRequest;
import cl.techk.ext.database.DBCnx;
import cl.techk.ext.database.DataManager;
import cl.techk.ext.utils.Utils;
import cl.techk.lib.BatchUtils;
import cl.techk.lib.FileUtils;
import cl.techk.mail.rest.Mail;

public class Dictionary {

    private final static int MAX_FILE_SIZE = 104857600;

    public static JSONObject charge(String upload_file_name, int market_id) {

        JSONObject response = new JSONObject();
        response.put("Anexos", ChargeAppendant.charge(upload_file_name));
        response.put("Rubros", ChargeRubro.charge(upload_file_name, market_id));
        response.put("B1", ChargeSearch1.charge(upload_file_name, market_id));
        response.put("B2", ChargeSearch2.charge(upload_file_name, market_id));
        response.put("B3", ChargeSearch3.charge(upload_file_name, market_id));
        
        return response;
    }

    public static JSONObject saveData(JSONObject charge_data, int market_id) {

        JSONObject market_data = Dictionary.getMarketPrefix(market_id);
        JSONObject response = new JSONObject();
        String market_prefix = market_data.getString("prefijo");
        boolean data_with_error = Dictionary.hasErrors(charge_data);
        
        if (!data_with_error) {
            Utils.print("Saving Dictionary data");
            trucateDictionaryTables(market_prefix);
            response = saveDictionaryOnDatabase(charge_data, market_prefix);
        } else {
            Utils.print("Error saving Dictionary data");
            response = readErrorMessages(charge_data);
        }
        
        return response;
    }

    public static JSONObject saveDictionaryOnDatabase(JSONObject charge_data, String market_prefix) {

        Utils.print("Insertando Datos Diccionario");
        JSONObject response = new JSONObject();
        String insert_appendant_query = Dictionary.getInsertAppendantQuery(market_prefix);
        String insert_rubros_query = Dictionary.getInsertRubrosQuery(market_prefix);
        String insert_search1_query = Dictionary.getInsertSearch1Query(market_prefix);
        String insert_search2_query = Dictionary.getInsertSearch2Query(market_prefix);
        String insert_search3_query = Dictionary.getInsertSearch3Query(market_prefix);
        insertToDatabase(ValidateAppendant.COLUMN_COUNT, market_prefix,
                charge_data.getJSONObject("Anexos").getJSONArray("row_data"), insert_appendant_query);
        insertToDatabase(ValidateRubro.COLUMN_COUNT, market_prefix,
                charge_data.getJSONObject("Rubros").getJSONArray("row_data"), insert_rubros_query);
        insertToDatabase(ValidateSearch1.COLUMN_COUNT, market_prefix,
                charge_data.getJSONObject("B1").getJSONArray("row_data"), insert_search1_query);
        insertToDatabase(ValidateSearch2.COLUMN_COUNT, market_prefix,
                charge_data.getJSONObject("B2").getJSONArray("row_data"), insert_search2_query);
        insertToDatabase(ValidateSearch3.COLUMN_COUNT, market_prefix,
                charge_data.getJSONObject("B3").getJSONArray("row_data"), insert_search3_query);
        response.put("resp", "Diccionario guardado correctamente");
        Utils.print("FIN Inserción Datos Diccionario");
        return response;
    }

    public static JSONObject readErrorMessages(JSONObject charge_data) {

        StringBuffer error_messages = new StringBuffer();
        JSONObject response = new JSONObject();
        processErrorMessages(charge_data, "Rubros", error_messages);
        processErrorMessages(charge_data, "B1", error_messages);
        processErrorMessages(charge_data, "B2", error_messages);
        processErrorMessages(charge_data, "B3", error_messages);
        processErrorMessages(charge_data, "Anexos", error_messages);        
        preparedMailNotification(error_messages.toString(), "Errores en Carga de Diccionario");
        response.put("resp", "Carga fallida. Se envió un correo con el detalle.");
        
        return response;
    }
    
    public static void processErrorMessages(JSONObject charge_data, String sheet_name, StringBuffer error_messages) {
        
        JSONArray data = charge_data.getJSONObject(sheet_name).getJSONArray("errors");
        appendErrorMessages(sheet_name, error_messages, data);
    }
    public static void insertToDatabase(int column_count, String market_prefix, JSONArray data, String query_insert) {

        Connection database_connection = null;
        PreparedStatement appendant_statement = null;

        try {
            database_connection = DBCnx.conexion();
            appendant_statement = database_connection.prepareStatement(query_insert);
            int data_length = data.length();
            JSONArray data_row = new JSONArray();

            for (int ix = 0; ix < data_length; ix++) {
                data_row = data.getJSONArray(ix);
                BatchUtils.appendDataToStatement(column_count, appendant_statement, data_row);
                BatchUtils.executeStatement(ix, appendant_statement);
            }

            BatchUtils.executeStatement(0, appendant_statement);
        } catch (Exception error) {
            error.printStackTrace();
        } finally {
            DBCnx.close(appendant_statement);
            DBCnx.close(database_connection);
        }
    }

    public static MultipartRequest getformData(HttpServletRequest request) {

        MultipartRequest post_form_data = null;

        try {
            post_form_data = new MultipartRequest(request, FileUtils.TEMP_FILE_FOLDER, MAX_FILE_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return post_form_data;
    }

    public static String getFileNameFromMultipart(MultipartRequest post_form_data) {

        Iterator<String> iterator = (Iterator<String>) post_form_data.getFileNames();
        String file_name = "";

        if (iterator.hasNext()) {
            String key = (String) iterator.next();
            file_name = post_form_data.getOriginalFileName(key);
        }

        return FileUtils.TEMP_FILE_FOLDER + "/" + file_name;
    }

    public static JSONObject getMarketPrefix(int market_id) {

        JSONObject data_query = new JSONObject();
        DataManager.getDataObject(data_query,
                "SELECT id, mercado, prefijo FROM "+DBCnx.db_clasification+".mercados WHERE id = " + market_id + "");
        
        return data_query;
    }

    public static JSONArray getMarkets() {
        return DataManager.getDataArray("SELECT id, mercado FROM "+DBCnx.db_clasification+".mercados");
    }

    public static String getInsertAppendantQuery(String market_prefix) {

        String query = "INSERT INTO "+DBCnx.db_clasification+".`anexos" + market_prefix
                + "`(`keyword`, `descripcion`, `fecha_cambio`) VALUES (?, ?, ?)";
        
        return query;
    }

    public static String getInsertRubrosQuery(String market_prefix) {

        String query = "INSERT INTO "+DBCnx.db_clasification+".`rubros" + market_prefix
                + "`(`rub_generico`, `rub_rubro`, `fecha_cambio`) VALUES (?, ?, ?)";
        
        return query;
    }

    public static String getInsertSearch1Query(String market_prefix) {

        String query = "INSERT INTO "+DBCnx.db_clasification+".`search_level01" + market_prefix
                + "`(`keywords`, `description`, `rubro`, `fecha_cambio`) VALUES (?, ?, ?, ?)";
        
        return query;
    }

    public static String getInsertSearch2Query(String market_prefix) {

        String query = "INSERT INTO "+DBCnx.db_clasification+".`search_level02" + market_prefix
                + "`(`search_level01`, `keywords`, `description`,`rubro`, `fecha_cambio`) VALUES (?, ?, ?, ?, ?)";
        
        return query;
    }

    public static String getInsertSearch3Query(String market_prefix) {

        String query = "INSERT INTO "+DBCnx.db_clasification+".`search_level03" + market_prefix
                + "`(`search_level01`, `search_level02`, `keywords`, `description`, `rubro`, `fecha_cambio`) VALUES (?, ?, ?, ?, ?, ?)";
        
        return query;
    }

    public static int rubroExists(String market, String rubro) {

        int id_rubro_diccionario = 0;
        Connection database_connection = null;
        PreparedStatement statement_rubro = null;
        ResultSet resultset = null;

        try {
            database_connection = DBCnx.conexion();
            statement_rubro = database_connection.prepareStatement(
                    "SELECT id FROM "+DBCnx.db_clasification+".diccionario_rubros WHERE rubro = ? AND mercado = ?");
            statement_rubro.setString(1, rubro);
            statement_rubro.setString(2, market);
            resultset = statement_rubro.executeQuery();

            if (resultset.next()) {
                id_rubro_diccionario = resultset.getInt("id");
            }
        } catch (Exception error) {
            error.printStackTrace();
        } finally {
            DBCnx.closeAll(resultset, statement_rubro, database_connection);
        }
        
        return id_rubro_diccionario;
    }

    public static boolean hasErrors(JSONObject charge_data) {

        boolean data_with_errors = true;
        Iterator<?> keys = charge_data.keys();

        while (keys.hasNext()) {
            String dictionary_table = (String) keys.next();
            JSONObject data = charge_data.getJSONObject(dictionary_table);

            if (data.getJSONArray("row_data").length() == 0 && data.getJSONArray("errors").length() > 0) {
                data_with_errors = true;
                break;
            }
            data_with_errors = false;
        }
        
        return data_with_errors;
    }

    public static void appendErrorMessages(String dictionary_table, StringBuffer error_messages, JSONArray data) {

        int length_data = data.length();

        for (int ix = 0; ix < length_data; ix++) {
            if (data.getJSONArray(ix).length() > 0) {
                String error = data.getJSONArray(ix).getString(0);
                error_messages.append("En la hoja " + dictionary_table + ": " + error + "<br>");
            }
        }
    }

    public static void preparedMailNotification(String error_message, String mail_subject) {
        Mail.sendMail(error_message, mail_subject, null, null, 5, "Tech-K", 20);
    }

    public static void trucateDictionaryTables(String market_prefix) {

        DataManager.deleteData("TRUNCATE "+DBCnx.db_clasification+".anexos" + market_prefix);
        DataManager.deleteData("TRUNCATE "+DBCnx.db_clasification+".rubros" + market_prefix);
        DataManager.deleteData("TRUNCATE "+DBCnx.db_clasification+".search_level01" + market_prefix);
        DataManager.deleteData("TRUNCATE "+DBCnx.db_clasification+".search_level02" + market_prefix);
        DataManager.deleteData("TRUNCATE "+DBCnx.db_clasification+".search_level03" + market_prefix);
    }
}
