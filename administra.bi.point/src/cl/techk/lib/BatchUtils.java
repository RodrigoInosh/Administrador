package cl.techk.lib;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.json.JSONArray;
import cl.techk.ext.database.DBCnx;
import cl.techk.models.ValidateAppendant;

public class BatchUtils {

    public static void executeStatement(int batch_length, PreparedStatement insert_appendant_data)
            throws SQLException {
        if (batch_length % 30 == 0 || batch_length == 0) {
            insert_appendant_data.executeBatch();
        }
    }

    public static boolean insertData(int column_count, JSONArray rows_data, String insert_appendant_query) {

        boolean inserted = false;
        PreparedStatement insert_appendant_data = null;
        Connection database_connection = null;

        try {
            database_connection = DBCnx.conexion();
            insert_appendant_data = database_connection.prepareStatement(insert_appendant_query);
            int length_data_array = rows_data.length();

            for (int index_data = 0; index_data < length_data_array; index_data++) {
                appendDataToStatement(column_count, insert_appendant_data, rows_data.getJSONArray(index_data));
                executeStatement(index_data, insert_appendant_data);
            }
            executeStatement(0, insert_appendant_data);
        } catch (Exception error) {
            error.printStackTrace();
        } finally {
            DBCnx.close(insert_appendant_data);
            DBCnx.close(database_connection);
        }

        return inserted;
    }

    public static void appendDataToStatement(int column_count, PreparedStatement insert_appendant_data, JSONArray rows_data)
            throws SQLException {

        for (int column_index = 0; column_index < column_count; column_index++) {
            insert_appendant_data.setString(column_index + 1, rows_data.getString(column_index));
        }
        insert_appendant_data.addBatch();
    }
}
