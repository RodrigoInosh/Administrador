package cl.techk.excel.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import com.monitorjbl.xlsx.StreamingReader;
import com.oreilly.servlet.MultipartRequest;
import cl.techk.clasificacion.rest.Clasificacion;
import cl.techk.ext.database.DataManager;
import cl.techk.ext.utils.Utils;
import cl.techk.models.Excel;

@Path("/excel")
@MultipartConfig
public class UploadExcelFile {
    final static int MAX_FILE_SIZE = 104857600;
    final static int MAX_ROW_CACHE_SIZE = 100;
    final static int MAX_STREAMING_BUFFER_SIZE = 4096;
    final static String DEFAULT_FOLDER_UPLOADED_FILE = "/Desarrollo";

    @POST
    @Path("/upload/")
    @Produces("application/json; charset=UTF-8")
    public Response upload(@Context final HttpServletRequest request) {
        
        MultipartRequest post_form_data;
        JSONObject response = new JSONObject();
        String upload_file_name = "";
        String table_insert_data = "";
        String destination_folder = "";
        String upload_option_process = "";
        String client_id = "";
        String is_upload_file = "0";
        String is_ondemand_clasification = "1";
        String is_client_setting_file = "2";
        String is_notification_setting_by_user = "3";
        String is_notification_setting_by_client = "4";
        InputStream is = null;
        StreamingReader reader = null;
        
        try {
            // Tamaño máximo del archivo es de 20mb
            post_form_data = new MultipartRequest(request, DEFAULT_FOLDER_UPLOADED_FILE, MAX_FILE_SIZE);
            Iterator<String> iterator = (Iterator<String>) post_form_data.getFileNames();

            if (iterator.hasNext()) {
                String key = (String) iterator.next();
                upload_file_name = post_form_data.getOriginalFileName(key);
                table_insert_data = post_form_data.getParameter("tableDest");
                destination_folder = post_form_data.getParameter("dest");
                upload_option_process = post_form_data.getParameter("opc");
                client_id = post_form_data.getParameter("client");
            }
            is = new FileInputStream(new File(destination_folder + upload_file_name));
            
            if (is_ondemand_clasification.equals(upload_option_process)) {
                
                Utils.printOrdDeb("Clasificando");
                reader = StreamingReader.builder()
                        .rowCacheSize(MAX_ROW_CACHE_SIZE)
                        .bufferSize(MAX_STREAMING_BUFFER_SIZE)
                        .sheetIndex(0)
                        .read(is);
                Clasificacion.clasificacion_ondemand(response, "ondemand_" + table_insert_data, destination_folder, upload_file_name);
            } else if (is_upload_file.equals(upload_option_process)) {
                
                reader = StreamingReader.builder()
                        .rowCacheSize(MAX_ROW_CACHE_SIZE)
                        .bufferSize(MAX_STREAMING_BUFFER_SIZE)
                        .sheetIndex(0)
                        .read(is);
                Excel.saveFileInDestinationFolder(upload_file_name, destination_folder);
            } else if (is_client_setting_file.equals(upload_option_process)) {

                reader = StreamingReader.builder()
                        .rowCacheSize(MAX_ROW_CACHE_SIZE)
                        .bufferSize(MAX_STREAMING_BUFFER_SIZE)
                        .sheetIndex(0)
                        .read(is);
                String delete_column_condition = "";
                
                if ("motivo_no_postulacion".equals(table_insert_data)) {
                    delete_column_condition = "id_cliente";
                } else {
                    delete_column_condition = "idcliente";
                }
                
                DataManager.deleteData(table_insert_data, delete_column_condition, Integer.parseInt(client_id));
                setResponseMessage(response, Excel.insertMaintainerExcelData(reader, table_insert_data, delete_column_condition, Integer.parseInt(client_id)));
                
            } else if (is_notification_setting_by_user.equals(upload_option_process) || is_notification_setting_by_client.equals(upload_option_process)) {
                
                Workbook notifications_workbook = new XSSFWorkbook(is);
                Excel.readNotificationSettingFile(response, notifications_workbook, Integer.parseInt(client_id), upload_option_process);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Response.status(200).entity(response.put("resp", "error").toString()).build();
        }
        return Response.status(200).entity(response.toString()).build();
    }

    public static void setResponseMessage(JSONObject response, boolean data_inserted) {
        
        if (data_inserted) {
            response.put("resp", "Datos cargados correctamente");
        } else {
            response.put("resp", "Error cargando datos. Favor revisar archivo de carga");
        }
    }
}
