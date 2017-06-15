package cl.techk.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.json.JSONObject;
import com.oreilly.servlet.MultipartRequest;

import cl.techk.lib.CalendarUtils;
import cl.techk.lib.FileUtils;
import cl.techk.models.Dictionary;
import cl.techk.models.DictionaryExport;

@Path("/dictionary")
public class DictionaryController {

    final static int MAX_FILE_SIZE = 104857600;

    @POST
    @Path("/upload/")
    @Produces("application/json; charset=UTF-8")
    public Response upload(@Context final HttpServletRequest request) {

        JSONObject response = new JSONObject();
        JSONObject charge_data = new JSONObject();
        MultipartRequest form_data = null;
        String upload_file_name = "";
        int market_id = 0;
        
        try {
            form_data = Dictionary.getformData(request);
            upload_file_name = Dictionary.getFileNameFromMultipart(form_data);
            market_id = Integer.parseInt(form_data.getParameter("dictionary_market"));
            charge_data = Dictionary.charge(upload_file_name, market_id);
            response = Dictionary.saveData(charge_data, market_id);
        } catch (Exception error) {
            error.printStackTrace();
            response.put("resp", error.getMessage());
            return Response.status(500).entity(response.toString()).build();
        }

        return Response.status(200).entity(response.toString()).build();
    }

    @GET
    @Path("/export/{market_id}/")
    @Produces("application/vnd.ms-excel")
    public Response export(@Context final HttpServletRequest request, @PathParam("market_id") int market_id) {

        InputStream temp_file = null;

        try {
            String file_path = DictionaryExport.generateXlsFile(market_id);
            File file = new File(file_path);
            temp_file = new FileInputStream(file);
            FileUtils.deleteFile(file_path);
        } catch (FileNotFoundException e) {
            JSONObject response = new JSONObject();
            response.put("error", e.getMessage());
            return Response.status(500).entity(response.toString()).type(MediaType.APPLICATION_JSON).build();
        }

        String formatted_date = CalendarUtils.getActualDateInLocalFormat();
        JSONObject market_data = DictionaryExport.getMarketData(market_id);
        ResponseBuilder response = Response.ok(temp_file);
        response.header("Content-Disposition", "attachment; filename=Diccionario_Techk_"
                + market_data.getString("nombre") + "_" + formatted_date + ".xlsx");
        return response.build();
    }

    @GET
    @Path("/dictionary_markets")
    @Produces("application/json; charset=UTF-8")
    public Response dictionary_markets(@Context final HttpServletRequest request) {

        JSONObject response = new JSONObject();
        response.put("markets", Dictionary.getMarkets());

        return Response.status(200).entity(response.toString()).build();
    }
}
