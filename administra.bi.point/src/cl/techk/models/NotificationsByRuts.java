package cl.techk.models;

import org.json.JSONArray;
import org.json.JSONObject;

import cl.techk.ext.database.DataManager;

public class NotificationsByRuts {

    public static JSONObject getListOfRuts(int client_id) {

        JSONObject response = new JSONObject();
        response.put("client_id", client_id);
        String query = "select * from cliente_ruts_notificaciones where id_cliente=" + client_id;
        JSONArray results_array = DataManager.getDataArray(query);
        response.put("results", results_array);

        return response;
    }

    public static JSONObject createRow(int client_id, String rut) {

        JSONObject response = new JSONObject();
        JSONObject exist_objeto = new JSONObject();
        DataManager.getDataObject(exist_objeto,
                "select count(*) as cantidad from cliente_ruts_notificaciones where id_cliente=" + client_id
                        + " and rut='" + rut + "'");
        if (exist_objeto.getInt("cantidad") == 0) {
            try {
                DataManager.insertData("insert into cliente_ruts_notificaciones (id_cliente, rut) values (" + client_id
                        + ",'" + rut + "')");
                response.put("succesfull", true);
            } catch (Exception e) {
                e.printStackTrace();
                response.put("succesfull", false);
            }
        } else {
            response.put("succesfull", false);
            response.put("error", "El rut ya existe.");
        }
        return response;
    }

    public static JSONObject deleteRow(int row_id) {

        JSONObject response = new JSONObject();

        try {
            String error = DataManager.deleteData("delete from cliente_ruts_notificaciones where id=" + row_id);
            response.put("error", error);
            response.put("succesfull", true);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("succesfull", false);
        }

        return response;
    }

}
