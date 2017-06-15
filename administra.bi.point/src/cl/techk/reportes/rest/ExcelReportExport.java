package cl.techk.reportes.rest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cl.techk.models.Excel;

@WebServlet("/ExportReports")
public class ExcelReportExport extends HttpServlet {
    
    private static final long serialVersionUID = 1L;

    public ExcelReportExport() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        Calendar calendar = Calendar.getInstance();
        String report_type = request.getParameter("option");
        String client_id = request.getParameter("client");
        String report_name = "";
        response.setContentType("application/vnd.ms-excel");
        
        switch (report_type){
        case "logs":
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String actual_date = sdf.format(calendar.getTime());
            report_name = "ReporteLogs_" + actual_date + ".xlsx\"";
            break;
        case "user_notification":
            report_name = "Notificaciones_por_usuario";
            break;
        case "client_notification":
            report_name = "Notificaciones_por_cliente";
            break;
        default:
            report_name = "Reporte_"+report_type;
            break;
        }
        response.setHeader("Content-Disposition", "inline;filename=\"" + report_name + ".xlsx\"");
        
        Excel.generateExcelFile(response, report_type, client_id);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}