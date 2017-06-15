package cl.techk.ext.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Properties;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import cl.techk.ext.database.DBCnx;
import cl.techk.ordenes.carga.Maestro_compG;
import cl.techk.ordenes.carga.Maestro_compGAux;
import cl.techk.ordenes.carga.Maestro_compM;
import cl.techk.ordenes.carga.Maestro_compMAux;
import cl.techk.ordenes.carga.Maestro_prod;
import cl.techk.ordenes.carga.Maestro_prodAux;
import cl.techk.ordenes.carga.Maestro_prov;
import cl.techk.ordenes.carga.Maestro_provAux;

@WebServlet(value = "/InitLog", loadOnStartup = 1)
public class InitLog extends HttpServlet {
    private static final long serialVersionUID = 1L;
    Date fecha = new Date();
    public static boolean develop = true;
    public static Logger logApiPointLic, logApiPointAdj, logApiPointOrd, logCargaLicitacion, logHistoricoLic;
    // MAESTROS PRINCIPALES
    public static LinkedHashMap<String, Maestro_prov> MaestrosProv = new LinkedHashMap<String, Maestro_prov>();
    public static LinkedHashMap<String, Maestro_compG> MaestrosCompG = new LinkedHashMap<String, Maestro_compG>();
    public static LinkedHashMap<Integer, Maestro_compM> MaestrosCompM = new LinkedHashMap<Integer, Maestro_compM>();
    public static LinkedHashMap<Integer, Maestro_prod> MaestrosProd = new LinkedHashMap<Integer, Maestro_prod>();
    // LISTA DE MAESTROS NUEVOS O MODIFICADOS.
    public static LinkedHashMap<String, Maestro_provAux> MaestrosProvAux = new LinkedHashMap<String, Maestro_provAux>();
    public static LinkedHashMap<String, Maestro_compGAux> MaestrosCompGAux = new LinkedHashMap<String, Maestro_compGAux>();
    public static LinkedHashMap<String, Maestro_compMAux> MaestrosCompMAux = new LinkedHashMap<String, Maestro_compMAux>();
    public static LinkedHashMap<Integer, Maestro_prodAux> MaestrosProdAux = new LinkedHashMap<Integer, Maestro_prodAux>();

    public InitLog() {
        super();

        // get config properties

        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = new FileInputStream("/configPoint/config.properties");
            prop.load(input);
            DBCnx.url = prop.getProperty("url");
            DBCnx.user = prop.getProperty("user");
            DBCnx.pass = prop.getProperty("pass");
            DBCnx.db_orders = prop.getProperty("ord");
            DBCnx.db_adjudicadas = prop.getProperty("adj");
            DBCnx.db_clasification = prop.getProperty("db_clasification");
            DBCnx.db_downloads = prop.getProperty("db_downloads");
            DBCnx.db_server = prop.getProperty("database_server");
            DBCnx.connection_params = prop.getProperty("connection_params");
            DBCnx.execute_bulletin_task = Boolean.parseBoolean(prop.getProperty("execute_bulletin_task"));
            DBCnx.time_zone = prop.getProperty("time_zone");
            DBCnx.ip_server = prop.getProperty("classificator_host");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        configurelogOrdenes();
        configurelogAdjudicadas();
        configurelogLicitaciones();
        configurelogCargaLicitaciones();
        configurelogCargaHistLicitaciones();
    }

    private void configurelogLicitaciones() {
        logApiPointLic = Logger.getLogger("logLicitaciones");
        SimpleDateFormat formato = new SimpleDateFormat("_MM_yyyy");
        String fechaAc = formato.format(fecha);
        PatternLayout defaultLayout = new PatternLayout("%p: %d{HH:mm:ss} --> %m%n");
        RollingFileAppender rollingFileAppender = new RollingFileAppender();

        try {
            rollingFileAppender.setFile("/logPoint/log_lic_" + fechaAc + ".log", true, false, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        rollingFileAppender.setLayout(defaultLayout);
        logApiPointLic.removeAllAppenders();
        logApiPointLic.addAppender(rollingFileAppender);
        logApiPointLic.setAdditivity(false);
    }

    private void configurelogOrdenes() {
        logApiPointOrd = Logger.getLogger("logOrdenes");
        SimpleDateFormat formato = new SimpleDateFormat("_MM_yyyy");
        String fechaAc = formato.format(fecha);
        PatternLayout defaultLayout = new PatternLayout("%p: %d{HH:mm:ss} --> %m%n");
        RollingFileAppender rollingFileAppender = new RollingFileAppender();

        try {
            rollingFileAppender.setFile("/logPoint/log_ord_" + fechaAc + ".log", true, false, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        rollingFileAppender.setLayout(defaultLayout);
        logApiPointOrd.removeAllAppenders();
        logApiPointOrd.addAppender(rollingFileAppender);
        logApiPointOrd.setAdditivity(false);
    }

    private void configurelogAdjudicadas() {
        logApiPointAdj = Logger.getLogger("logAdjudicadas");
        SimpleDateFormat formato = new SimpleDateFormat("_MM_yyyy");
        String fechaAc = formato.format(fecha);
        PatternLayout defaultLayout = new PatternLayout("%p: %d{HH:mm:ss} --> %m%n");
        RollingFileAppender rollingFileAppender = new RollingFileAppender();

        try {
            rollingFileAppender.setFile("/logPoint/log_adj_" + fechaAc + ".log", true, false, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        rollingFileAppender.setLayout(defaultLayout);
        logApiPointAdj.removeAllAppenders();
        logApiPointAdj.addAppender(rollingFileAppender);
        logApiPointAdj.setAdditivity(false);
    }

    private void configurelogCargaLicitaciones() {
        logCargaLicitacion = Logger.getLogger("logCargaLicitaciones");
        SimpleDateFormat formato = new SimpleDateFormat("_MM_yyyy");
        String fechaAc = formato.format(fecha);
        PatternLayout defaultLayout = new PatternLayout("%p: %d{HH:mm:ss} --> %m%n");
        RollingFileAppender rollingFileAppender = new RollingFileAppender();

        try {
            rollingFileAppender.setFile("/logPoint/log_cargaLic_" + fechaAc + ".log", true, false, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        rollingFileAppender.setLayout(defaultLayout);
        logCargaLicitacion.removeAllAppenders();
        logCargaLicitacion.addAppender(rollingFileAppender);
        logCargaLicitacion.setAdditivity(false);
    }

    private void configurelogCargaHistLicitaciones() {
        logHistoricoLic = Logger.getLogger("logCargaHistLicitaciones");
        SimpleDateFormat formato = new SimpleDateFormat("_MM_yyyy");
        String fechaAc = formato.format(fecha);
        PatternLayout defaultLayout = new PatternLayout("%p: %d{HH:mm:ss} --> %m%n");
        RollingFileAppender rollingFileAppender = new RollingFileAppender();

        try {
            rollingFileAppender.setFile("/logPoint/log_retroLic_" + fechaAc + ".log", true, false, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        rollingFileAppender.setLayout(defaultLayout);
        logHistoricoLic.removeAllAppenders();
        logHistoricoLic.addAppender(rollingFileAppender);
        logHistoricoLic.setAdditivity(false);
    }
}
