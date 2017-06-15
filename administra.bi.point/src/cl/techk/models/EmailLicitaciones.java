package cl.techk.models;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import cl.techk.data.DataMailDailyLicitation;
import cl.techk.ext.database.DBCnx;
import cl.techk.ext.database.DataManager;
import cl.techk.ext.utils.Messages;
import cl.techk.ext.utils.TemplateMailLoader;
import cl.techk.ext.utils.Utils;
import cl.techk.lib.ExcelUtils;
import cl.techk.mail.rest.Mail;

public class EmailLicitaciones {

    private static final SimpleDateFormat german_date_format = new SimpleDateFormat("yyyy-MM-dd");

    public static void sendClientDailyEmail(String email) {
        sendDailyEmail(email);
    }

    public static String getCurrentDate() {

        Calendar chilean_calendar = Calendar.getInstance();
        chilean_calendar.setTimeZone(TimeZone.getTimeZone("America/Santiago"));
        String current_date = new SimpleDateFormat("dd-MM-yyyy").format(chilean_calendar.getTime());
        return current_date;
    }

    public static String getGermanFormattedDate() {

        Calendar current_date = Utils.getFechaChile();
        String last_publication_licitation_date = "";
        current_date.set(Calendar.HOUR, 0);
        current_date.set(Calendar.MINUTE, 0);
        current_date.set(Calendar.SECOND, 0);
        last_publication_licitation_date = german_date_format.format(current_date.getTime());

        return last_publication_licitation_date;
    }

    public static String getMailSubject(String client_name) {

        String mail_subject = "";
        mail_subject = client_name + " Licitaciones " + getCurrentDate();
        return mail_subject;
    }

    private static void sendDailyEmail(String email) {

        String client_name = "Tech-k";
        String mail_template = "";
        String mail_body = "";

        try {

            String subject = getMailSubject(client_name);

            mail_template = Mail.html_template_notification_daily_licitation;
            DataMailDailyLicitation data_licitation = new DataMailDailyLicitation(client_name, getCurrentDate());
            data_licitation.setMessage(getDailyEmailMessage());

            try {
                mail_body = TemplateMailLoader.loadFilledTemplate(data_licitation, mail_template);
            } catch (IOException e) {
                e.printStackTrace();
            }

            sendMail(email, subject, mail_body);

        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    public static void sendMail(String email, String mail_subject, String mail_body_message) {

        final String username = "AKIAJOBA6LOCRTDEPYJQ";
        final String password = "AtlElUutEJIo1wcar+n+G0D8ocmxoj+EQ3jYYqo3SZ8R";
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "email-smtp.us-east-1.amazonaws.com");
        props.put("mail.smtp.port", "587");
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(Messages.getString("Mail.from")));
            message.addRecipient(Message.RecipientType.BCC, new InternetAddress(email));
            message.setHeader("Content-Type", "text/html; charset=UTF-8");
            message.setSubject(mail_subject);

            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(mail_body_message, "text/html; charset=UTF-8");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            messageBodyPart = new MimeBodyPart();

            message.setContent(multipart);
            Transport.send(message);
        } catch (Exception mex) {
            mex.printStackTrace();
        }
    }

    public static void cleanDailyEmailMessage() {
        updateDailyEmailMessage("");
    }

    public static void updateDailyEmailMessage(String message) {

        Connection cnx = null;
        Statement stmt = null;

        try {
            String query = "UPDATE configuraciones SET value = '" + message + "' "
                    + "WHERE code = 'licitation_daily_message'";

            cnx = DBCnx.conexion();
            stmt = cnx.createStatement();
            stmt.executeUpdate(query);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBCnx.closeAll(null, stmt, cnx);
        }
    }

    public static String getDailyEmailMessage() {
        ResultSet response = null;
        Connection cnx = null;
        Statement stmt = null;

        try {
            String query = "SELECT value FROM configuraciones WHERE code = 'licitation_daily_message'";

            cnx = DBCnx.conexion();
            stmt = cnx.createStatement();
            response = stmt.executeQuery(query);
            response.first();

            return response.getString("value");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            DBCnx.closeAll(response, stmt, cnx);
        }
    }

    public static void LicitationReportFile(String especific_daily_licitation_client) {

        String initial_publication_licitation_date = getLastLicitationMailingDate();
        String last_publication_licitation_date = getGermanFormattedDate();
        Connection cnx = null;
        Statement stmt = null;
        ResultSet client_information = null;

        try {

            String get_clientes = getQueryClientsToGenerateFile(especific_daily_licitation_client);

            cnx = DBCnx.conexion();
            stmt = cnx.createStatement();
            client_information = stmt.executeQuery(get_clientes);
            generateLicitationReportFile(client_information, initial_publication_licitation_date,
                    last_publication_licitation_date);
            client_information.beforeFirst();
            prepareSendDailyLicitationMail(client_information, last_publication_licitation_date);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBCnx.closeAll(client_information, stmt, cnx);
        }
    }

    private static String getLastLicitationMailingDate() {

        String fecha = "";
        String query = "SELECT fecha FROM licitaciones_enviadas_fecha ORDER BY fecha DESC LIMIT 1";
        Connection cnx = null;
        Statement stmt = null;
        ResultSet result = null;

        try {

            cnx = DBCnx.conexion();
            stmt = cnx.createStatement();
            result = stmt.executeQuery(query);

            if (result.next()) {
                fecha = result.getString("fecha");
            }

        } catch (Exception mex) {
            mex.printStackTrace();
        } finally {
            DBCnx.closeAll(result, stmt, cnx);
        }

        return fecha;
    }

    public static void generateLicitationReportFile(ResultSet client_information,
            String initial_publication_licitation_date, String last_publication_licitation_date) {

        ExecutorService pool_file_generation_threads = Executors.newFixedThreadPool(10);
        try {

            while (client_information.next()) {

                final int client_id = client_information.getInt("idcliente");
                final int admin_user_id = client_information.getInt("idusuario");
                final String client_name = client_information.getString("cliente");
                final String uploaded_licitation_date = last_publication_licitation_date;
                
                Thread file_generation_thread = new Thread(new Runnable() {
                    public void run() {
                        getClientLicitations(client_id, client_name, uploaded_licitation_date, admin_user_id);
                    }
                });
                
                pool_file_generation_threads.execute(file_generation_thread);
            }
            
            pool_file_generation_threads.shutdown();
            while (!pool_file_generation_threads.isTerminated()) {

                try {
                    Thread.sleep(1300);
                } catch (InterruptedException e) {
                    Utils.print(e.getMessage());
                }
            }
            
            Utils.print(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()));
            Utils.print("Fin creacion archivos");
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    private static void getClientLicitations(int idcliente, String cliente, String uploaded_licitation_date,
            int id_user) {
        
        Utils.print("Revisando Cliente:" + cliente);
        Connection cnx = null;
        String formatted_current_date = "";
        FileOutputStream fileOut = null;
        Date current_date_german_format = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try {
            current_date_german_format = german_date_format.parse(uploaded_licitation_date);
            Date current_date_chilean_format = new Date(current_date_german_format.getTime());
            formatted_current_date = new SimpleDateFormat("dd-MM-yyyy").format(current_date_chilean_format);
        } catch (ParseException e1) {
            e1.printStackTrace();
        }

        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet licitation_sheet = wb.createSheet("licitaciones");
        XSSFSheet relevant_market_sheet = wb.createSheet("Mercado Relevante");
        licitation_sheet.setDisplayGridlines(false);
        licitation_sheet.setZoom(75);
        licitation_sheet.setAutoFilter(CellRangeAddress.valueOf("A4:V4"));
        licitation_sheet.createFreezePane(0, 4);
        relevant_market_sheet.setDisplayGridlines(false);
        relevant_market_sheet.setZoom(100);
        createRelevantMarketSheet(relevant_market_sheet, idcliente, wb);
        String nombre_archivo = "";
        String ruta = "";
        String query = "select * from (select a.*,(SELECT pl.estado as postulaciones FROM postulacion_licitacion "
                + "pl join postulacion_estado e on (e.valor=pl.estado) where licitacion_idlicitacion=a.id and cliente_idcliente="
                + idcliente + "  order by prioridad asc limit 1) as postulaciones from (select id,tipo,"
                + "nombre,descripcion,unidad_compra,codigo,estado,es_favorita,"
                + "razon_social,rut,region, DATE_FORMAT(  `fecha_cierre` ,  '%d-%m-%Y %H:%i:%s' ) AS fecha_cierre, "
                + "DATE_FORMAT(  `fecha_publicacion` ,  '%d-%m-%Y %H:%i:%s' ) AS fecha_publicacion,fecha_adjudicacion,"
                + " garantias from licitacion l join usuario_licitacion ul on "
                + "(l.id=ul.licitacion_idlicitacion) where uploaded_in_point >= '" + uploaded_licitation_date + "' AND "
                + "ul.usuario_idusuario = " + id_user + " ORDER BY fecha_publicacion ASC) a) b";

        Statement stmt = null;
        ResultSet resultLic = null;
        try {
            cnx = DBCnx.conexion();
            stmt = cnx.createStatement();
            resultLic = stmt.executeQuery(query);

            XSSFRow row = licitation_sheet.createRow((short) 1);
            setTitle(row, wb, "Licitaciones Cargadas el " + formatted_current_date + "");

            row = licitation_sheet.createRow((short) 3);
            ExcelUtils.HeadersXlsx(row, wb,
                    new String[] { "Número Adquisición", "Tipo Adquisición", "Nombre Adquisición", "Descripción",
                            "Razon Social", "Unidad Compra", "Rut", "Región Compradora", "Fecha Publicación",
                            "Fecha Cierre", "Fecha Adjudicacion", "N° Item", "Descripción del producto/servicio",
                            "Unidad de Medida", "Cantidad", "Genérico", "Rubro", "Es Anexo", "Búsqueda 1", "Búsqueda 2",
                            "Búsqueda 3", "Búsqueda 4" });

            XSSFCellStyle estilo = wb.createCellStyle();

            Font font = wb.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);
            estilo.setFont(font);

            int i = 4;
            while (resultLic.next()) {
                Connection cnxDetail = null;
                Statement stmtDetail = null;
                ResultSet resultLicDetail = null;
                try {
                    cnxDetail = DBCnx.conexion();
                    stmtDetail = cnxDetail.createStatement();
                    resultLicDetail = stmtDetail.executeQuery(
                            "select * from licitacion_item li join cliente_item ci on(ci.id_item=li.id) where id_licitacion="
                                    + resultLic.getString("id") + " and ci.id_cliente=" + idcliente);
                    while (resultLicDetail.next()) {

                        XSSFRow row1 = licitation_sheet.createRow((short) i);
                        setLicitationRow(row1, resultLic, resultLicDetail, estilo);
                        i++;

                        if ("si".equals(resultLicDetail.getString("es_anexo"))) {

                            Connection cnxAnexo = null;
                            Statement stmtAnexo = null;
                            ResultSet resultLicAnexo = null;
                            try {
                                cnxAnexo = DBCnx.conexion();
                                stmtAnexo = cnxAnexo.createStatement();
                                resultLicAnexo = stmtAnexo.executeQuery(
                                        "select * from licitacion_item_anexo lia join cliente_anexo ca on(ca.id_anexo=lia.id) where id_item="
                                                + resultLicDetail.getString("id") + " and ca.id_cliente=" + idcliente);

                                while (resultLicAnexo.next()) {
                                    XSSFRow row2 = licitation_sheet.createRow((short) i);
                                    XLSXRowAnexo(row2, resultLic, resultLicAnexo, resultLicDetail, estilo);
                                    i++;
                                }
                            } catch (Exception e) {
                                Utils.print("Error obteniendo anexos");
                                e.printStackTrace();
                            } finally {
                                DBCnx.closeAll(resultLicAnexo, stmtAnexo, cnxAnexo);
                            }
                        }

                    }
                } catch (Exception e) {
                    Utils.print("Error generando excel licitaciones");
                    e.printStackTrace();
                } finally {
                    DBCnx.closeAll(resultLicDetail, stmtDetail, cnxDetail);
                }
                ExcelUtils.setSheetColumnsSize(licitation_sheet, new int[] { 101, 15, 15, 15, 275, 15, 15, 15, 15, 151, 15, 57,
                        468, 99, 64, 15, 15, 27, 100, 100, 100, 100 });

            }
            String fecha = new SimpleDateFormat("dd-MM-yyyy").format(sdf.parse(uploaded_licitation_date));
            nombre_archivo = "Licitaciones_" + cliente + "_" + fecha;
            ruta = "/PointClienteLicitaciones/" + nombre_archivo + ".xlsx";

            fileOut = new FileOutputStream(ruta);
            wb.write(fileOut);
            fileOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBCnx.closeAll(resultLic, stmt, cnx);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void prepareSendDailyLicitationMail(ResultSet client_information,
            String last_publication_licitation_date) {

        int clients_processed_count = 0;
        String mail_template = "";
        String mail_body = "";

        try {
            String date_in_file_name = new SimpleDateFormat("dd-MM-yyyy")
                    .format(german_date_format.parse(last_publication_licitation_date));

            while (client_information.next()) {

                String client_name = client_information.getString("cliente");
                int client_id = client_information.getInt("idcliente");
                String client_licitations_file_name = "Licitaciones_" + client_name + "_" + date_in_file_name;
                String client_licitations_file_directory = "/PointClienteLicitaciones/" + client_licitations_file_name
                        + ".xlsx";
                String subject = getMailSubject(client_name);

                mail_template = Mail.html_template_notification_daily_licitation;
                DataMailDailyLicitation data_licitation = new DataMailDailyLicitation(client_name, getCurrentDate());
                data_licitation.setMessage(EmailLicitaciones.getDailyEmailMessage());

                try {
                    mail_body = TemplateMailLoader.loadFilledTemplate(data_licitation, mail_template);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Mail.sendMail(mail_body, subject, client_licitations_file_directory, client_licitations_file_name,
                        client_id, client_name, 3);
                clients_processed_count++;

                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Utils.print("Clientes procesados: " + clients_processed_count);
            Utils.print(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()));
            Utils.print("Fin envío mail");
            ActualizarUltimaFechaEnviada(last_publication_licitation_date);
            EmailLicitaciones.cleanDailyEmailMessage();
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    private static void setTitle(XSSFRow row, XSSFWorkbook wb, String title) {

        Font font = ExcelUtils.getLicitationsFont(wb, HSSFColor.GREY_40_PERCENT.index, (short) 16);
        XSSFCellStyle style = ExcelUtils.getLicitationsCellStyle(wb, font,
                new XSSFColor(new java.awt.Color(255, 255, 255)));
        ExcelUtils.setCellValue(new String[] { title }, row, style);
    }

    private static void HeaderMercadoRelevante(XSSFRow row, XSSFWorkbook wb) {

        Font font = ExcelUtils.getLicitationsFont(wb, HSSFColor.WHITE.index, (short) 10);
        XSSFCellStyle style = ExcelUtils.getLicitationsCellStyle(wb, font,
                new XSSFColor(new java.awt.Color(90, 90, 90)));

        ExcelUtils.setCellValue(new String[] { "Búsqueda 1", "Búsqueda 1", "Búsqueda 3" }, row, style);
    }

    private static void setLicitationRow(XSSFRow row, ResultSet resultLic, ResultSet resultLicDetail, XSSFCellStyle style) {

        try {
            ExcelUtils.setCellValue(new String[] { resultLic.getString("codigo"), resultLic.getString("tipo"),
                    resultLic.getString("nombre"), resultLic.getString("descripcion"),
                    resultLic.getString("razon_social"), resultLic.getString("unidad_compra"),
                    resultLic.getString("rut"), resultLic.getString("region"), resultLic.getString("fecha_publicacion"),
                    resultLic.getString("fecha_cierre"), resultLic.getString("fecha_adjudicacion"),
                    resultLicDetail.getString("item"), resultLicDetail.getString("descripcion_producto"),
                    resultLicDetail.getString("unidad_medida"), resultLicDetail.getString("cantidad"),
                    resultLicDetail.getString("generico"), resultLicDetail.getString("rubro"),
                    resultLicDetail.getString("es_anexo"), resultLicDetail.getString("search1"),
                    resultLicDetail.getString("search2"), resultLicDetail.getString("search3"),
                    resultLicDetail.getString("search4") }, row, style);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void XLSXRowAnexo(XSSFRow row1, ResultSet resultLic, ResultSet resultLicAnexo,
            ResultSet resultLicDetail, XSSFCellStyle estilo) {
        XSSFCell cell;
        try {
            cell = (XSSFCell) row1.createCell((short) 0);
            cell.setCellValue(resultLic.getString("codigo"));
            cell.setCellStyle(estilo);

            cell = (XSSFCell) row1.createCell((short) 1);
            cell.setCellValue(resultLic.getString("tipo"));
            cell.setCellStyle(estilo);

            cell = (XSSFCell) row1.createCell((short) 2);
            cell.setCellValue(resultLic.getString("nombre"));
            cell.setCellStyle(estilo);

            cell = (XSSFCell) row1.createCell((short) 3);
            cell.setCellValue(resultLic.getString("descripcion"));
            cell.setCellStyle(estilo);

            cell = (XSSFCell) row1.createCell((short) 4);
            cell.setCellValue(resultLic.getString("razon_social"));
            cell.setCellStyle(estilo);

            cell = (XSSFCell) row1.createCell((short) 5);
            cell.setCellValue(resultLic.getString("unidad_compra"));
            cell.setCellStyle(estilo);

            cell = (XSSFCell) row1.createCell((short) 6);
            cell.setCellValue(resultLic.getString("rut"));
            cell.setCellStyle(estilo);

            cell = (XSSFCell) row1.createCell((short) 7);
            cell.setCellValue(resultLic.getString("region"));
            cell.setCellStyle(estilo);

            cell = (XSSFCell) row1.createCell((short) 8);
            cell.setCellValue(resultLic.getString("fecha_publicacion"));
            cell.setCellStyle(estilo);

            cell = (XSSFCell) row1.createCell((short) 9);
            cell.setCellValue(resultLic.getString("fecha_cierre"));
            cell.setCellStyle(estilo);

            cell = (XSSFCell) row1.createCell((short) 10);
            cell.setCellValue(resultLic.getString("fecha_adjudicacion"));
            cell.setCellStyle(estilo);

            cell = (XSSFCell) row1.createCell((short) 11);
            cell.setCellValue(resultLicAnexo.getString("item_anexo"));
            cell.setCellStyle(estilo);

            cell = (XSSFCell) row1.createCell((short) 12);
            cell.setCellValue(resultLicAnexo.getString("descripcion"));
            cell.setCellStyle(estilo);

            cell = (XSSFCell) row1.createCell((short) 13);
            cell.setCellValue(resultLicDetail.getString("unidad_medida"));
            cell.setCellStyle(estilo);

            cell = (XSSFCell) row1.createCell((short) 14);
            cell.setCellValue(resultLicAnexo.getString("cantidad"));
            cell.setCellStyle(estilo);

            cell = (XSSFCell) row1.createCell((short) 15);
            cell.setCellValue(resultLicDetail.getString("generico"));
            cell.setCellStyle(estilo);

            cell = (XSSFCell) row1.createCell((short) 16);
            cell.setCellValue(resultLicAnexo.getString("rubro"));
            cell.setCellStyle(estilo);

            cell = (XSSFCell) row1.createCell((short) 17);
            cell.setCellValue("si");
            cell.setCellStyle(estilo);

            cell = (XSSFCell) row1.createCell((short) 18);
            cell.setCellValue(resultLicAnexo.getString("search1"));
            cell.setCellStyle(estilo);

            cell = (XSSFCell) row1.createCell((short) 19);
            cell.setCellValue(resultLicAnexo.getString("search2"));
            cell.setCellStyle(estilo);

            cell = (XSSFCell) row1.createCell((short) 20);
            cell.setCellValue(resultLicAnexo.getString("search3"));
            cell.setCellStyle(estilo);

            cell = (XSSFCell) row1.createCell((short) 21);
            cell.setCellValue(resultLicAnexo.getString("search4"));
            cell.setCellStyle(estilo);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createRelevantMarketSheet(XSSFSheet sheet, int idcliente, XSSFWorkbook wb) {

        Calendar calendar = Utils.getFechaChile();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        String fecha = sdf.format(calendar.getTime());

        String header = "Mercado Relevante al " + fecha;

        XSSFRow row = sheet.createRow((short) 1);
        setTitle(row, wb, header);

        row = sheet.createRow((short) 3);
        HeaderMercadoRelevante(row, wb);
        XSSFCellStyle estilo = wb.createCellStyle();

        Font font = wb.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        estilo.setFont(font);
        XLSXRows(sheet, idcliente, estilo);

        sheet.setColumnWidth(0, 20 * 256);
        sheet.setColumnWidth(1, 20 * 256);
        sheet.setColumnWidth(2, 20 * 256);

    }

    private static void XLSXRows(XSSFSheet sheet, int idcliente, XSSFCellStyle estilo) {
        PreparedStatement consulta = null;
        ResultSet resultados = null;
        Connection conexion = null;

        try {

            String query = "SELECT search1,search2,search3 FROM cliente_diccionario WHERE id_cliente = ?";
            conexion = DBCnx.conexion();
            consulta = conexion.prepareStatement(query);
            consulta.setInt(1, idcliente);

            resultados = consulta.executeQuery();
            int fila = 4;

            String[] info_usuarios = new String[10];

            XSSFCell cell;

            while (resultados.next()) {

                info_usuarios[2] = resultados.getString("search1");
                info_usuarios[3] = resultados.getString("search2");
                info_usuarios[4] = resultados.getString("search3");

                XSSFRow row = sheet.createRow((short) fila);

                cell = (XSSFCell) row.createCell((short) 0);
                cell.setCellValue(resultados.getString("search1"));
                cell.setCellStyle(estilo);

                cell = (XSSFCell) row.createCell((short) 1);
                cell.setCellValue(resultados.getString("search2"));
                cell.setCellStyle(estilo);

                cell = (XSSFCell) row.createCell((short) 2);
                cell.setCellValue(resultados.getString("search3"));
                cell.setCellStyle(estilo);

                fila++;
            }

        } catch (Exception error) {
            error.printStackTrace();
        } finally {
            DBCnx.closeAll(resultados, consulta, conexion);
        }
    }

    private static void ActualizarUltimaFechaEnviada(String fecha2) {
        String query = "INSERT INTO licitaciones_enviadas_fecha (fecha) VALUES ('" + fecha2 + " 00:00:00')";
        DataManager.insertData(query);

    }

    public static String getQueryClientsToGenerateFile(String especific_daily_licitation_client) {

        String client_where_condition = getClientWhereCondition(especific_daily_licitation_client);
        String query = "SELECT * FROM (SELECT idcliente,c.nombre AS cliente,u.idusuario,u.usuario,"
                + "cliente_idcliente FROM cliente c LEFT JOIN usuario u ON u.cliente_idcliente = c.idcliente "
                + "WHERE licitaciones_diarias = 1 AND u.perfil_idperfil = 1 AND u.habilitado = 1 "
                + client_where_condition + " " + "ORDER BY u.idusuario DESC) AS t GROUP BY t.cliente_idcliente ASC;";

        return query;
    }

    public static String getClientWhereCondition(String especific_daily_licitation_client) {

        String client_where_condition = "";

        if (!especific_daily_licitation_client.equals("0")) {
            client_where_condition = " AND u.cliente_idcliente = " + especific_daily_licitation_client + "";
        }

        return client_where_condition;
    }
}
