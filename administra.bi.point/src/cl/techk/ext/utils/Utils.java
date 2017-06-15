package cl.techk.ext.utils;

import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Locale;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.json.JSONArray;

import cl.techk.ext.database.DBCnx;
import cl.techk.lib.CalendarUtils;

public class Utils {

    static boolean printconsole = true, printdebug = false;
    static DataFormatter formatter = new DataFormatter(Locale.US);
    public static String BREAK_LINE = System.getProperty("line.separator");

    public static void sendMailHtml(String data) {
        final String username = "AKIAJOBA6LOCRTDEPYJQ";
        final String password = "AtlElUutEJIo1wcar+n+G0D8ocmxoj+EQ3jYYqo3SZ8R";
        java.util.Properties props = new java.util.Properties();
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
            message.setFrom(new InternetAddress("point@techk.cl"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress("faguilera@techk.cl"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress("rinostroza@techk.cl"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress("jsoto@techk.cl"));
            Calendar cal = Calendar.getInstance();
            message.setSubject("RESUMEN CARGA LICITACIONES DIARIAS " + CalendarUtils.getFechaChile());
            message.setContent(data, "text/html");
            Transport.send(message);
        } catch (MessagingException mex) {
            Utils.print("error enviando mail " + mex.getMessage());
        }
    }

    public static String md5(String string) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] hash = messageDigest.digest(string.getBytes("UTF-8"));
            StringBuilder builder = new StringBuilder(2 * hash.length);
            for (byte b : hash)
                builder.append(String.format("%02x", b & 0xff));
            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void print(String dato) {
        InitLog.logApiPointLic.error(dato);
        if (printconsole) {
            System.out.println(dato);
        }
    }

    public static void printLicErr(Object dato) {
        InitLog.logApiPointLic.error(dato);
        if (printconsole) {
            System.out.println(dato);
        }
    }

    public static void printAdjErr(Object dato) {
        InitLog.logApiPointAdj.error(dato);
        if (printconsole) {
            System.out.println(dato);
        }
    }

    public static void printOrdErr(Object dato) {
        InitLog.logApiPointOrd.error(dato);
        if (printconsole) {
            System.out.println(dato);
        }
    }

    public static void printLicDeb(Object dato) {
        InitLog.logApiPointLic.debug(dato);
        if (printdebug) {
            System.out.println(dato);
        }
    }

    public static void printAdjDeb(Object dato) {
        InitLog.logApiPointAdj.debug(dato);
        if (printdebug) {
            System.out.println(dato);
        }
    }

    public static void printOrdDeb(Object dato) {
        InitLog.logApiPointOrd.debug(dato);
        if (printdebug) {
            System.out.println(dato);
        }
    }

    public static void printCargaLic(Object dato) {
        InitLog.logCargaLicitacion.info(dato);
        System.out.println(dato);
    }

    public static void printErrRetroLic(Object dato) {
        InitLog.logHistoricoLic.error(dato);
    }

    public static void printErrCargaLic(Object dato) {
        InitLog.logCargaLicitacion.error(dato);
        System.out.println(dato);
    }

    public static void printRetroLic(Object dato) {
        InitLog.logHistoricoLic.info(dato);
    }

    public static String getValueFromXlsRow(Row row, int columna) {
        String respuesta = "";
        try {
            respuesta = formatter.formatCellValue(row.getCell(columna));
        } catch (Exception e) {
            Utils.print("error obteniendo valor columna " + columna);
        }
        return respuesta;
    }

    public static Calendar getFechaChile() {

        Calendar fechaChile = Calendar.getInstance();
        fechaChile.add(Calendar.HOUR, Integer.parseInt(DBCnx.time_zone));
        return fechaChile;
    }

    public static String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1).toLowerCase();
    }

    public static String getCutText(String text_to_cut) {

        String cut_text = "";
        if (text_to_cut.length() > 35) {
            cut_text = text_to_cut.substring(0, 35) + "...";
        } else {
            cut_text = text_to_cut;
        }
        cut_text = capitalize(cut_text);
        return cut_text;
    }

    public static String addBreaklineToString(String long_text) {
        return long_text.replaceAll("(.{12})", "$1\n");
    }

    public static String removeLastCharacter(String value) {
        if (value.length() > 0) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
    
    public static void putDataOnArray(String value, JSONArray json_array) {
        json_array.put(value);
    }
}
