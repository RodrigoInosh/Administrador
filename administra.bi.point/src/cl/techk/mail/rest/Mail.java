package cl.techk.mail.rest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import cl.techk.ext.database.DBCnx;
import cl.techk.ext.database.DataManager;
import cl.techk.ext.utils.Messages;
import cl.techk.ext.utils.Utils;

public class Mail {
    
    final static int IS_DAILY_NOTIFICATION = 3;
    private static String mail_templates_folder = "cl/techk/mail/templates/";
    public static String html_template_notification_licitation_bulletin = mail_templates_folder
            + "notification_licitation_bulletin.html";
    public static String html_template_notification_daily_licitation = mail_templates_folder
            + "notification_daily_licitation.html";

    public static void sendMail(String mail_body_message, String mail_subject, String attached_file_directory,
            String attached_file_name, int client_id, String client_name, int notification_id) {

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
        Connection database_connection = null;
        PreparedStatement statement_get_recipients_mails = null;
        ResultSet recipients_mails = null;

        try {
            MimeMessage message = new MimeMessage(session);
            InternetAddress from_mail_address = new InternetAddress(Messages.getString("Mail.from"));
            from_mail_address.setPersonal("Point");
            message.setFrom(from_mail_address);

            String get_recipients_mails = "SELECT u.mail FROM mail_usuarioAlerta ua LEFT JOIN usuario u ON "
                    + "ua.id_mailAdmin = u.idusuario WHERE id_cliente = ? and id_tipoAlerta = ?;";
            database_connection = DBCnx.conexion();
            statement_get_recipients_mails = database_connection.prepareStatement(get_recipients_mails);
            statement_get_recipients_mails.setInt(1, client_id);
            statement_get_recipients_mails.setInt(2, notification_id);
            recipients_mails = statement_get_recipients_mails.executeQuery();

            while (recipients_mails.next()) {
                String mail = recipients_mails.getString("mail");
                message.addRecipient(Message.RecipientType.BCC, new InternetAddress(mail));
            }
            message.setHeader("Content-Type", "text/html; charset=UTF-8");
            message.setSubject(mail_subject);

            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(mail_body_message, "text/html; charset=UTF-8");
            
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            messageBodyPart = new MimeBodyPart();
            
            if (attached_file_name != null) {
                
                DataSource source = new FileDataSource(attached_file_directory);
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(attached_file_name + ".xlsx");
                multipart.addBodyPart(messageBodyPart);
            }
            message.setContent(multipart);
            Transport.send(message);
            System.out.println("Envio de Mail completado....");

            insertSendMailLog(client_id, client_name, notification_id);
        } catch (Exception mex) {
            Utils.printOrdErr("Error Enviando Correo a Cliente: " + client_name);
            mex.printStackTrace();
        } finally {
            DBCnx.closeAll(recipients_mails, statement_get_recipients_mails, database_connection);
        }
    }
    
    public static void insertSendMailLog(int client_id, String client_name, int notification_id) {
        if (IS_DAILY_NOTIFICATION == notification_id) {
            String insert_log_enviado = "INSERT INTO `log_mail_licitaciones_enviados` (`idcliente`,`cliente`,"
                    + "`fecha_enviado`) VALUES (" + client_id + ",'" + client_name + "',now());";
            DataManager.insertData(insert_log_enviado);
        }
    }
    
    public static String getBodyMail(String cliente, String fecha) {
        String body = "<table class='templateContainer' border='0' cellpadding='0' cellspacing='0' "
                + "width='100%'><tbody>";

        String seccion_header_logo = "<tr><td ><table border='0' cellpadding='0' cellspacing='0' width='100%'><tbody><tr><td ><img alt='' src='https://gallery.mailchimp.com/7844194257932eb587e09ffed/images/8025d98f-0ac9-4231-bea3-7c3fd4164a5c.png' align='right' width='164'></td></tr></tbody></table></td></tr>";
        String seccion_nombre_clte = "<tr><td id='templateBody' valign='top'><table class='mcnTextBlock' "
                + "style='min-width:100%;' border='0' cellpadding='0' cellspacing='0' width='100%'>"
                + "<tbody class='mcnTextBlockOuter'><tr><td class='mcnTextBlockInner' "
                + "style='padding-top:9px;' valign='top'><table style='max-width:100%;min-width:100%;"
                + "'class='mcnTextContentContainer' "
                + "align='left' border='0' cellpadding='0' cellspacing='0' width='100%'><tbody><tr>"
                + "<td class='mcnTextContent' style='padding-top:0;padding-right:18px;padding-bottom:9px;"
                + "padding-left:18px;' valign='top'><br><br>Les informamos que "
                + "ya se encuentran actualizadas las&nbsp;licitaciones disponibles al día<strong>" + "&nbsp;" + fecha
                + "&nbsp;</strong>.<br></br><br></br>Para visualizarlas puede <strong> ingresar directamente a nuestra plataforma </strong>"
                + "<a href='https://point.techk.cl/' style='color:rgb(236,24,24)' target='_blank'>Point</a> o abrir el"
                + "<strong> archivo adjunto con el resumen de licitaciones para descargar</strong>.<br></br><br></br>"
                + "Ante cualquier duda, favor contactar a&nbsp;<a href='mailto:bi@techk.cl' target='_blank'>"
                + "bi@techk.cl</a><br><br> Saludos y buen día<br><strong><em>Equipo Tech-K</em></strong><br> "
                + "&nbsp;</p></td></tr></tbody></table></td></tr></tbody></table>";

        String seccion_footer = "<tr><td id='templateFooter' valign='top'><table class='mcnTextBlock' "
                + "style='min-width:100%;' border='0' cellpadding='0' cellspacing='0' width='100%'>"
                + "<tbody class='mcnTextBlockOuter'><tr><td class='mcnTextBlockInner' style='padding-top:9px;"
                + "' valign='top'><!--[if mso]><table align='left' border='0' cellspacing='0' "
                + "cellpadding='0' width='100%' style='width:100%;'><tr><![endif]--><!--[if mso]>"
                + "<td valign='top' width='600' style='width:600px;'><![endif]--><table style='max-width:100%; "
                + "min-width:100%;' class='mcnTextContentContainer' align='left' border='0' cellpadding='0' "
                + "cellspacing='0' width='100%'><tbody><tr><td class='mcnTextContent' style='padding-top:0; "
                + "padding-right:18px; padding-bottom:9px; padding-left:18px;' valign='top'><em>Tech-K 2016, "
                + "Todos los derechos reservados.</em><br><br><strong>Nuestra dirección:</strong><br>Cerro el "
                + "Plomo 5420, oficina 601,<br>Las Condes. Santiago, RM.<br>+562 22441121</td></tr></tbody></table><!--[if mso]></td><![endif]--><!--[if mso]></tr></table><![endif]--></td></tr></tbody></table></td></tr>";

        body += seccion_header_logo + seccion_nombre_clte + seccion_footer;
        body += "</tbody></table>";
        return body;
    }

    public static void SendCambiosContraseña(String body, String email) {
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

            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));

            message.setSubject("Notificación acceso plataforma Point");

            BodyPart messageBodyPart = new MimeBodyPart();

            messageBodyPart.setContent(body, "text/html");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            messageBodyPart = new MimeBodyPart();

            message.setContent(multipart);
            Transport.send(message);
            Utils.printOrdDeb("Envio de Mail completado....");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }
    
    public static String getClasificationSQLErrorMessage(String error_message) {
        
        String message = "";
        if(error_message.contains("Unknown column")){
            message = "Se debe eliminar la columna";
        } else if(error_message.contains("Duplicate entry") && error_message.contains("PRIMARY")) {
            message = "Se debe eliminar la columna ID";
        }
        return message;
    }
}
