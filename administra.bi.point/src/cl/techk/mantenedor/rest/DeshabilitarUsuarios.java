package cl.techk.mantenedor.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.TimeZone;

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
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.oreilly.servlet.MultipartRequest;

import cl.techk.ext.database.DBCnx;
import cl.techk.ext.utils.Messages;

@WebServlet("/DeshabilitarUsuarios")
public class DeshabilitarUsuarios extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		MultipartRequest m = new MultipartRequest(request, "/Desarrollo");

		Iterator<String> iterator = (Iterator<String>) m.getFileNames();

		String filename = "/Desarrollo/";
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			filename += m.getOriginalFileName(key);
		}
		// String filename = "/Desarrollo/prueba.xlsx";
		System.out.println(filename);
		File myFile = new File(filename);
		FileInputStream fis = new FileInputStream(myFile);

		// Finds the workbook instance for XLSX file
		XSSFWorkbook myWorkBook = new XSSFWorkbook(fis);

		// Return first sheet from the XLSX workbook
		XSSFSheet mySheet = myWorkBook.getSheetAt(0);

		// Get iterator to all the rows in current sheet
		Iterator<Row> rowIterator = mySheet.iterator();

		boolean filas_vacias = false;

		Connection conexion = DBCnx.conexion();
		PreparedStatement stmt_deshabilitado_usuarios = null;

		String query_update = "UPDATE usuario SET habilitado = 0 WHERE idusuario = ?";

		try {
			stmt_deshabilitado_usuarios = conexion.prepareStatement(query_update);

			int fila = 0;
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				if(fila == 0) {
					fila++;
					continue;
				}
				try {
					long idusuario = row.getCell(0) != null ? Math.round(row.getCell(0).getNumericCellValue()) : -1;
					if (idusuario == -1 || idusuario == 0) {
						Send("Existen Filas vacías");
						filas_vacias = true;
						break;
					}
					stmt_deshabilitado_usuarios.setLong(1, idusuario);
					stmt_deshabilitado_usuarios.addBatch();
					
					fila++;
				} catch (Exception e) {
					Send("Existen Problemas con el archivo. <b>Validar que todos los campos vienen con el formato correcto.");
					e.printStackTrace();
				}
			}
			
			if(!filas_vacias){
				 int[] tot = stmt_deshabilitado_usuarios.executeBatch();
				 Send("Se completó el proceso de Deshabilitado de Usuarios.<br>"
				 		+ " El total de usuarios deshabilitados es de: "+tot.length);
				 System.out.println("Total Usuarios Deshabilitados: " + tot.length);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			DBCnx.close(conexion);
			DBCnx.close(stmt_deshabilitado_usuarios);
			myWorkBook.close();
		}
	}

	private static void Send(String body) {

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

			message.addRecipient(Message.RecipientType.TO, new InternetAddress("rinostroza@techk.cl"));

			TimeZone timeZone = TimeZone.getTimeZone("America/Santiago");

			Calendar fecha_actual = Calendar.getInstance();
			fecha_actual.setTimeZone(timeZone);
			Date date = fecha_actual.getTime();
			SimpleDateFormat format1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			String fechaformat = format1.format(date);

			message.setSubject("Deshabilitado de usuarios " + fechaformat);

			BodyPart messageBodyPart = new MimeBodyPart();

			messageBodyPart.setContent(body, "text/html");

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			message.setContent(multipart);
			Transport.send(message);
			System.out.println("Envio de Mail completado....");
		} catch (MessagingException mex) {
			mex.printStackTrace();
		}
	}
}
