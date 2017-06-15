package cl.techk.controllers;

import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.json.JSONObject;

import cl.techk.lib.CalendarUtils;
import cl.techk.lib.FileUtils;
import cl.techk.models.MaintainerExport;

@Path("/maintainer")
public class MaintainerController {

    @GET
    @Path("/export/{report_type}/")
    @Produces("application/vnd.ms-excel")
    public Response export(@Context final HttpServletRequest request, @PathParam("report_type") String report_type) {

        InputStream temp_file = null;
        JSONObject data = new JSONObject();
        data = MaintainerExport.generateXlsxFile(report_type);
        temp_file = FileUtils.getTemporalFile(data);
        FileUtils.deleteFile(data.getString("file_path"));
        String formatted_date = CalendarUtils.getActualDateInLocalFormat();
        String file_name = data.getString("file_name");
        ResponseBuilder response = Response.ok(temp_file);
        response.header("Content-Disposition", "attachment; filename=" + file_name + "_" + formatted_date + ".xlsx");

        return response.build();
    }
}
