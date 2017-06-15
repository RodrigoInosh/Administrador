package cl.techk.controllers;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Produces;
import cl.techk.models.EmailLicitaciones;

@WebServlet("/ExcelLicitacionesMail")
@Produces("application/vnd.ms-excel")
public class DailyLicitationController extends HttpServlet {
    
    private static final long serialVersionUID = 1L;

    public DailyLicitationController() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String especific_daily_licitation_client = request.getParameter("clte");
        EmailLicitaciones.LicitationReportFile(especific_daily_licitation_client);
    }

   
}
