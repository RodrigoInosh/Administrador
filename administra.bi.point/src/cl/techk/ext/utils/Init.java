package cl.techk.ext.utils;

import java.util.Timer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import cl.techk.ext.database.DBCnx;
import cl.techk.licitaciones.rest.TaskBoletinLicitaciones;

@WebServlet(value = "/Init", loadOnStartup = 2)
public class Init extends HttpServlet {
    private static final long serialVersionUID = 1L;
    Timer timerBoletinLicitaciones = null;

    public Init() {
        super();
    }

    @Override
    public void init() throws ServletException {
        super.init();

        if (DBCnx.execute_bulletin_task) {
            timerBoletinLicitaciones = new Timer();
            timerBoletinLicitaciones.schedule(new TaskBoletinLicitaciones(), 2000, 60000);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        destroyTimers(timerBoletinLicitaciones);
    }

    public static void destroyTimers(Timer timer) {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }
}