package cl.techk.licitaciones.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import cl.techk.models.EmailLicitaciones;

@Path("/licitaciones")
public class Licitaciones {

    @POST
    @Path("/enviar_email_prueba/")
    @Produces("application/json; charset=UTF-8")
    public Response sendTestEmail(@FormParam("email") String email, @FormParam("message") String message,
            @Context final HttpServletRequest request) {
        JSONObject resp = new JSONObject();
        resp.put("email", email);
        resp.put("message", message);
        EmailLicitaciones.updateDailyEmailMessage(message);
        EmailLicitaciones.sendClientDailyEmail(email);
        return Response.status(200).entity(resp.toString()).build();
    }

    @POST
    @Path("/actualizar_mensaje_email/")
    @Produces("application/json; charset=UTF-8")
    public Response updateEmailMessage(@FormParam("message") String message,
            @Context final HttpServletRequest request) {
        JSONObject resp = new JSONObject();
        resp.put("message", message);
        EmailLicitaciones.updateDailyEmailMessage(message);
        return Response.status(200).entity(resp.toString()).build();
    }

    @GET
    @Path("/mensaje_email_diario/")
    @Produces("application/json; charset=UTF-8")
    public Response updateEmailMessage(@Context final HttpServletRequest request) {
        JSONObject resp = new JSONObject();
        String message = EmailLicitaciones.getDailyEmailMessage();
        resp.put("message", message);
        return Response.status(200).entity(resp.toString()).build();
    }
}
