package cl.techk.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import cl.techk.models.NotificationsByRuts;

@Path("/notifications_by_rut")
public class NotificationsByRutsController {

    @GET
    @Path("/get_list/{client_id}/")
    @Produces("application/json; charset=UTF-8")
    public Response getList(@Context final HttpServletRequest request, @PathParam("client_id") int client_id) {

        JSONObject response = NotificationsByRuts.getListOfRuts(client_id);
        return Response.status(200).entity(response.toString()).build();
    }

    @POST
    @Path("/create/")
    @Produces("application/json; charset=UTF-8")
    public Response create(@Context final HttpServletRequest request, @FormParam("client_id") int client_id,
            @FormParam("rut") String rut) {

        JSONObject response = NotificationsByRuts.createRow(client_id, rut);
        return Response.status(200).entity(response.toString()).build();
    }

    @POST
    @Path("/delete/")
    @Produces("application/json; charset=UTF-8")
    public Response delete(@Context final HttpServletRequest request, @FormParam("row_id") int row_id) {

        JSONObject response = NotificationsByRuts.deleteRow(row_id);
        return Response.status(200).entity(response.toString()).build();
    }
}
