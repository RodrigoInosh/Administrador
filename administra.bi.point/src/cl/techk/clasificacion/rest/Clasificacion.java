package cl.techk.clasificacion.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;
import com.monitorjbl.xlsx.StreamingReader;
import cl.techk.ext.database.DBCnx;
import cl.techk.ext.database.DataManager;
import cl.techk.ext.utils.Utils;
import cl.techk.mail.rest.Mail;
import cl.techk.models.Excel;

public class Clasificacion {

    final static int MAX_ROW_CACHE_SIZE = 100;
    final static int MAX_STREAMING_BUFFER_SIZE = 4096;

    public static JSONObject clasificacion_ondemand(JSONObject response, String tabla, String destination_folder,
            String file_name) {

        boolean executing_clasification = validateClasificationStatus(tabla);
        boolean data_inserted_ok = false;
        String type_clasification_file = "";
        String ondemand_table = "";
        InputStream clasification_file_stream = null;
        StreamingReader clasification_file_reader = null;
        StringBuffer error_mail_message = new StringBuffer("");

        try {
            if (!executing_clasification) {
                clasification_file_stream = new FileInputStream(new File(destination_folder + file_name));
                clasification_file_reader = StreamingReader.builder().rowCacheSize(MAX_ROW_CACHE_SIZE)
                        .bufferSize(MAX_STREAMING_BUFFER_SIZE).sheetIndex(0).read(clasification_file_stream);
                // separar
                if ("0".equals(tabla)) {
                    return response.put("resp", "Falta seleccionar tipo de Archivo.");
                } else if ("ondemand_licitaciones".equals(tabla)) {
                    ondemand_table = "clasificacion_java.ondemand_licitaciones";
                    type_clasification_file = "lic";
                } else if ("ondemand_adjudicadas".equals(tabla)) {
                    ondemand_table = "clasificacion_java.ondemand_adjudicadas";
                    type_clasification_file = "adj";
                } else if ("ondemand_ordenes".equals(tabla)) {
                    ondemand_table = "clasificacion_java.ondemand_ordenes";
                    type_clasification_file = "ord";
                }
                data_inserted_ok = Excel.getValidationInsertDataOndemand(clasification_file_reader, ondemand_table,
                        type_clasification_file, error_mail_message);
            } else {
                return response.put("resp", "Hay una ejecución en proceso, espere que termine y vuelva a intentarlo.");
            }
        } catch (Exception e) {
            data_inserted_ok = false;
            e.printStackTrace();
        }

        if (data_inserted_ok) {
            executeClasification(type_clasification_file, ondemand_table, response, error_mail_message);
        } else {
            return response.put("resp",
                    "El proceso terminó de forma inesperada. Favor revisar el e-mail enviado con el detalle del error.");
        }
        return response;
    }

    private static void executeClasification(String type_clasification_file, String ondemand_table, JSONObject response,
            StringBuffer error_mail_message) {
        URL url;
        String mail_message = error_mail_message.toString();
        String response_message = "";
        BufferedReader in = null;
        HttpURLConnection con = null;

        try {
            Utils.printOrdDeb("Enviando a clasificar");
            url = new URL("http://" + DBCnx.ip_server + ":8080/Pacman/ClasificatorService?tipo="
                    + type_clasification_file + "&cleandb=true&procesa=ondemand");
            con = (HttpURLConnection) url.openConnection();
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String linea = null;
            while ((linea = in.readLine()) != null) {
            }
            in.close();
            if (mail_message.length() > 0) {
                String subject = "[ONDEMAND][" + type_clasification_file.toUpperCase()
                        + "] Observaciones carga archivo OnDemand";
                Mail.sendMail(mail_message, subject, null, null, 5, "Tech-K", 20);
                response_message = "El proceso terminó de forma correcta, pero con observaciones. Más información en el correo enviado";
            } else {
                response_message = "El proceso terminó de forma correcta. Correo enviado";
            }
        } catch (MalformedURLException error) {
            error.printStackTrace();
            response_message = "Error URL de Pacman";
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            response_message = "Error de comunicación con Pacman";
            DataManager.deleteData("TRUNCATE " + ondemand_table);
        } catch (IOException e) {
            response_message = "Error recibiendo respuesta de Pacman";
            e.printStackTrace();
        }
        
        response.put("resp", response_message);
    }

    private static boolean validateClasificationStatus(String tabla) {

        boolean en_ejecucion = false;
        String query = "SELECT COUNT(*) AS rows_count FROM clasificacion_java." + tabla + ";";
        Connection connection = null;
        PreparedStatement statement_count_data = null;
        ResultSet result = null;

        try {
            connection = DBCnx.conexion();
            statement_count_data = connection.prepareStatement(query);
            result = statement_count_data.executeQuery();

            if (result.next()) {
                if (result.getInt("rows_count") > 0) {
                    en_ejecucion = true;
                }
            }
        } catch (SQLException e) {
            Utils.printOrdErr("Error revisando tabla ondemand: " + e.getMessage());
        } finally {
            DBCnx.closeAll(result, statement_count_data, connection);
        }
        Utils.printOrdDeb("Tabla con datos:" + en_ejecucion);
        return en_ejecucion;
    }
}