package cl.techk.mantenedor.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

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
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.oreilly.servlet.MultipartRequest;

import cl.techk.ext.database.DBCnx;
import cl.techk.ext.utils.Messages;

@WebServlet("/Usuarios")
public class Usuarios extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static LinkedHashMap<String, Integer> perfiles = new LinkedHashMap<String, Integer>();
	private static LinkedHashMap<String, Integer> mercados = new LinkedHashMap<String, Integer>();
	private static LinkedHashMap<String, String> clientes = new LinkedHashMap<String, String>();

	private static PreparedStatement stmt_insert_usuarios = null;
	private static PreparedStatement stmt_insert_modulos_usuarios = null;
	private static PreparedStatement stmt_insert_limites_usuarios = null;
	private static PreparedStatement stmt_insert_mercado_usuarios = null;

	private static final String nombreArchivo = "/Desarrollo/Usuarios_nuevos.xlsx";

	public Usuarios() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		MultipartRequest m = new MultipartRequest(request, "/Desarrollo");

		Iterator<String> iterator = (Iterator<String>) m.getFileNames();

		String filename = "/Desarrollo/";
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			filename += m.getOriginalFileName(key);
		}
		File myFile = new File(filename);
		FileInputStream fis = new FileInputStream(myFile);

		// Finds the workbook instance for XLSX file
		XSSFWorkbook myWorkBook = new XSSFWorkbook(fis);

		// Return first sheet from the XLSX workbook
		XSSFSheet mySheet = myWorkBook.getSheetAt(0);

		// Get iterator to all the rows in current sheet
		Iterator<Row> rowIterator = mySheet.iterator();

		// Traversing over each row of XLSX file
		int fila = 0;

		cargarPerfiles();
		cargarMercados();
		cargarCliente();

		LinkedHashMap<Integer, Usuario> usuarios = new LinkedHashMap<Integer, Usuario>();
		boolean dato_vacio = false;

		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (fila == 0) {
				fila++;
			} else {
				Usuario usuario;
				ModuloUsuario modulos;

				String nombre = "";
				String apellido = "";
				String user = "";
				String mail = "";
				String mercado = "";
				String perfil = "";
				String pass = "";
				String cliente = "";

				// Modulos Usuario
				int lic = 0, ordenes = 0, reportes = 0, adjudicadas = 0;
				int mantenedor_mercado = 0, mantenedor_productos = 0, mantenedor_asignacion = 0, mantenedor_glosas = 0;
				try {
					cliente = String.valueOf(Math.round(row.getCell(0).getNumericCellValue()));
					nombre = row.getCell(1).getStringCellValue();
					apellido = row.getCell(2).getStringCellValue();
					mail = row.getCell(3).getStringCellValue();
					mercado = row.getCell(4).getStringCellValue();
					perfil = row.getCell(5).getStringCellValue();

					lic = row.getCell(6) != null ? (int) row.getCell(6).getNumericCellValue() : -1;
					ordenes = row.getCell(7) != null ? (int) row.getCell(7).getNumericCellValue() : -1;
					adjudicadas = row.getCell(8) != null ? (int) row.getCell(8).getNumericCellValue() : -1;
					reportes = row.getCell(9) != null ? (int) row.getCell(9).getNumericCellValue() : -1;

					mantenedor_productos = row.getCell(10) != null ? (int) row.getCell(10).getNumericCellValue() : -1;
					mantenedor_mercado = row.getCell(11) != null ? (int) row.getCell(11).getNumericCellValue() : -1;
					mantenedor_asignacion = row.getCell(12) != null ? (int) row.getCell(12).getNumericCellValue() : -1;
					mantenedor_glosas = row.getCell(13) != null ? (int) row.getCell(13).getNumericCellValue() : -1;

					dato_vacio = validaCampos(cliente, nombre, apellido, mail, mercado, perfil, lic, ordenes,
							adjudicadas, reportes, mantenedor_productos, mantenedor_mercado, mantenedor_asignacion,
							mantenedor_glosas, dato_vacio);

				} catch (Exception e) {
					e.printStackTrace();
					dato_vacio = true;
				}

				if (dato_vacio) {
					Send("Hay Errores en la data a cargar", "", 0);
					break;
				}

				modulos = new ModuloUsuario(lic, adjudicadas, 0, 0, ordenes, 0, 0, reportes, mantenedor_productos,
						mantenedor_mercado, mantenedor_asignacion, mantenedor_glosas);

				String datos[] = getDatosUserPass(fila, cliente, nombre, apellido, user).split(";");

				user = datos[0];
				pass = datos[1];
				String nombre_clte = clientes.get(cliente);

				usuario = new Usuario(perfil, Integer.parseInt(cliente), nombre_clte, nombre, user, pass, mail,
						mercado);
				usuario.setModulos(modulos);

				usuarios.put(fila, usuario);
				fila++;
			}
		}
		if (!dato_vacio) {
			insertarDatos(usuarios);
		}
		generarExcel(usuarios);
		Send("Creación de usuarios Completada. Se adjunta la información.", nombreArchivo, 1);
		usuarios.clear();
		mercados.clear();
		perfiles.clear();
		clientes.clear();
		myWorkBook.close();
	}

	private static String getDatosUserPass(int fila, String idcliente, String nombre, String apellido, String usuario) {
		String pass = "";
		String nombre_clte = "";
		int cantidad = 0;

		PreparedStatement consulta = null;
		ResultSet resultados = null;
		Connection conexion = null;

		try {

			String query = "SELECT count(*) cantidad, usuario FROM usuario WHERE cliente_idcliente = ? GROUP BY cliente_idcliente";
			conexion = DBCnx.conexion();
			consulta = conexion.prepareStatement(query);
			consulta.setString(1, idcliente);

			resultados = consulta.executeQuery();

			if (resultados.next()) {
				cantidad = resultados.getInt("cantidad");
				nombre_clte = resultados.getString("usuario");

				cantidad = cantidad + fila;
				pass = nombre_clte.split("\\.")[0] + cantidad;

				usuario = getUsuario(nombre, apellido, nombre_clte.split("\\.")[0]);
			}
		} catch (Exception error) {
			error.printStackTrace();
		} finally {
			DBCnx.closeAll(resultados, consulta, conexion);
		}

		return usuario + ";" + pass;
	}

	private static String getUsuario(String nombre, String apellido, String cliente) {
		String user = "";

		nombre = nombre.split(" ")[0];
		user = cliente.toLowerCase() + "." + nombre.toLowerCase().charAt(0) + apellido.toLowerCase();

		return user;
	}

	private static boolean validaCampos(String cliente, String nombre, String apellido, String mail, String mercado,
			String perfil, int lic, int ordenes, int adjudicadas, int reportes, int mantenedor_productos,
			int mantenedor_mercado, int mantenedor_asignacion, int mantenedor_glosas, boolean datos_vacios) {

		datos_vacios = cliente.isEmpty() || cliente.equals("") ? true : datos_vacios;
		datos_vacios = clientes.get(cliente) == null ? true : datos_vacios;

		datos_vacios = nombre.isEmpty() || nombre.equals("") ? true : datos_vacios;
		datos_vacios = apellido.isEmpty() || apellido.equals("") ? true : datos_vacios;
		datos_vacios = mail.isEmpty() || mail.equals("") ? true : datos_vacios;
		datos_vacios = mercado.isEmpty() || mercado.equals("") ? true : datos_vacios;
		String m[] = mercado.split(";");
		for (int ix = 0; ix < m.length; ix++) {
			datos_vacios = mercados.get(m[ix]) == null ? true : datos_vacios;
		}

		datos_vacios = perfil.isEmpty() || perfil.equals("") ? true : datos_vacios;
		datos_vacios = perfiles.get(perfil) == null ? true : datos_vacios;

		datos_vacios = lic == -1 ? true : datos_vacios;
		datos_vacios = ordenes == -1 ? true : datos_vacios;
		datos_vacios = adjudicadas == -1 ? true : datos_vacios;
		datos_vacios = reportes == -1 ? true : datos_vacios;
		datos_vacios = mantenedor_productos == -1 ? true : datos_vacios;
		datos_vacios = mantenedor_mercado == -1 ? true : datos_vacios;
		datos_vacios = mantenedor_asignacion == -1 ? true : datos_vacios;
		datos_vacios = mantenedor_glosas == -1 ? true : datos_vacios;

		return datos_vacios;
	}

	private static void Send(String body, String filename, int status) {

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

			message.setSubject("Carga usuarios " + fechaformat);

			BodyPart messageBodyPart = new MimeBodyPart();

			messageBodyPart.setContent(body, "text/html");

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			if (status == 1) {
				messageBodyPart = new MimeBodyPart();
				DataSource source = new FileDataSource(filename);
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName("Usuarios.xlsx");
				multipart.addBodyPart(messageBodyPart);
			}
			message.setContent(multipart);
			Transport.send(message);
			System.out.println("Envio de Mail completado....");
		} catch (MessagingException mex) {
			mex.printStackTrace();
		}
	}

	private static int getLastId() {

		int last_id = 0;

		PreparedStatement consulta = null;
		ResultSet resultados = null;
		Connection conexion = null;

		try {

			String query = "SELECT MAX(idusuario) last_id FROM usuario";
			conexion = DBCnx.conexion();
			consulta = conexion.prepareStatement(query);

			resultados = consulta.executeQuery();

			if (resultados.next()) {
				last_id = resultados.getInt("last_id") + 1;
			}
		} catch (Exception error) {
			error.printStackTrace();
		} finally {
			DBCnx.closeAll(resultados, consulta, conexion);
		}

		return last_id;
	}

	private static void insertarDatos(LinkedHashMap<Integer, Usuario> usuarios) {

		int last_id = getLastId();

		Connection conexion = null;

		String query_insert_usuarios = "INSERT INTO `usuario`(`idusuario`, `perfil_idperfil`, "
				+ "`cliente_idcliente`, `nombre`, `usuario`, `password`, `mail`, `habilitado`, "
				+ "`created_at`) VALUES (?, ?, ?, ?, ?, md5(?), ?, 1, now())";

		String query_insert_modulos = "INSERT INTO `modulo_usuario`(`id_usr`, `licitaciones`, "
				+ "`adj_resumen`, `adj_vistaDin`, `adj_descarga`, `oc_vistaCla`, `oc_vistaDin`, "
				+ "`oc_descarga`, `reportes`, `mantenedor_catologo`, `mantenedor_mrelevantes`, "
				+ "`mantenedor_asignacion`, `mantenedor_glosas`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

		String query_insert_mercado_usuario = "INSERT INTO point_ordenes.`mercado_usuario`(`id_usr`, "
				+ "`id_clte`, `id_mer`) VALUES (?, ?, ?)";

		String query_insert_limites_usuario = "INSERT INTO point_ordenes.`limites`(`id_usuario`, "
				+ "`nombre_tabla_filtro`, `id_dato`, `id_mer`) VALUES (?, ?, ?, ?)";

		try {

			conexion = DBCnx.conexion();
			stmt_insert_usuarios = conexion.prepareStatement(query_insert_usuarios);
			stmt_insert_modulos_usuarios = conexion.prepareStatement(query_insert_modulos);
			stmt_insert_mercado_usuarios = conexion.prepareStatement(query_insert_mercado_usuario);
			stmt_insert_limites_usuarios = conexion.prepareStatement(query_insert_limites_usuario);

			int idx = 1;
			Iterator<?> itera = usuarios.entrySet().iterator();
			while (itera.hasNext()) {
				int id = last_id + idx;
				Map.Entry<?, ?> key = (Map.Entry<?, ?>) itera.next();

				Usuario u = usuarios.get(key.getKey());
				ModuloUsuario modulos = u.getModulos();

				int id_perfil = perfiles.get(u.getPerfil());
				// guardo el id del usuario para luego usarlo en la creación del
				// excel resumen
				u.setIdusuario(id);
				// Batch con la informacion del usuario
				stmt_insert_usuarios.setInt(1, id);
				stmt_insert_usuarios.setInt(2, id_perfil);
				stmt_insert_usuarios.setInt(3, u.getIdcliente());
				stmt_insert_usuarios.setString(4, u.getNombre());
				stmt_insert_usuarios.setString(5, u.getUsuario());
				stmt_insert_usuarios.setString(6, u.getContraseña());
				stmt_insert_usuarios.setString(7, u.getEmail());

				stmt_insert_usuarios.addBatch();

				insertDataModulos(id, modulos);
				insertDataMercados(id, u);
				// Cuando se agreguen límites al usuario en el excel se debe
				// modificar esta función
				insertDataLimites(id, u);

				idx++;
			}

			 int[] tot = stmt_insert_usuarios.executeBatch();
			 int[] tot2 = stmt_insert_modulos_usuarios.executeBatch();
			 int[] tot3 = stmt_insert_mercado_usuarios.executeBatch();
			 int[] tot4 = stmt_insert_limites_usuarios.executeBatch();
			 
			 System.out.println("Total Usuarios Creados: " + tot.length);
			 System.out.println("Total Usuarios Modulos Asignados: " +tot2.length);
			 System.out.println("Total Mercados Usuario Asignados: " +tot3.length);
			 System.out.println("Total Limites de usuarios Asignados: " +tot4.length);
		} catch (Exception error) {
			error.printStackTrace();
		} finally {
			DBCnx.close(conexion);
			DBCnx.close(stmt_insert_usuarios);
			DBCnx.close(stmt_insert_modulos_usuarios);
			DBCnx.close(stmt_insert_mercado_usuarios);
			DBCnx.close(stmt_insert_limites_usuarios);
		}
	}

	private static void cargarPerfiles() {
		PreparedStatement consulta = null;
		ResultSet resultados = null;
		Connection conexion = null;

		try {

			String query = "SELECT * FROM perfil";
			conexion = DBCnx.conexion();
			consulta = conexion.prepareStatement(query);

			resultados = consulta.executeQuery();

			while (resultados.next()) {
				perfiles.put(resultados.getString("nombre"), resultados.getInt("idperfil"));
			}
		} catch (Exception error) {
			error.printStackTrace();
		} finally {
			DBCnx.closeAll(resultados, consulta, conexion);
		}
	}

	private static void cargarMercados() {
		PreparedStatement consulta = null;
		ResultSet resultados = null;
		Connection conexion = null;

		try {

			String query = "SELECT * FROM point_ordenes.filtro_mercado";
			conexion = DBCnx.conexion();
			consulta = conexion.prepareStatement(query);

			resultados = consulta.executeQuery();

			while (resultados.next()) {
				mercados.put(resultados.getString("mercado"), resultados.getInt("id"));
			}
		} catch (Exception error) {
			error.printStackTrace();
		} finally {
			DBCnx.closeAll(resultados, consulta, conexion);
		}
	}

	private static void cargarCliente() {
		PreparedStatement consulta = null;
		ResultSet resultados = null;
		Connection conexion = null;

		try {

			String query = "SELECT idcliente, nombre FROM cliente";
			conexion = DBCnx.conexion();
			consulta = conexion.prepareStatement(query);

			resultados = consulta.executeQuery();

			while (resultados.next()) {
				clientes.put(resultados.getString("idcliente"), resultados.getString("nombre"));
			}
		} catch (Exception error) {
			error.printStackTrace();
		} finally {
			DBCnx.closeAll(resultados, consulta, conexion);
		}
	}

	private static void insertDataModulos(int id, ModuloUsuario modulos) {
		// Batch con la información de los modulos del usuario
		try {
			stmt_insert_modulos_usuarios.setInt(1, id);
			stmt_insert_modulos_usuarios.setInt(2, modulos.getLic());
			stmt_insert_modulos_usuarios.setInt(3, modulos.getAdj_resumen());
			stmt_insert_modulos_usuarios.setInt(4, modulos.getAdj_dinamico());
			stmt_insert_modulos_usuarios.setInt(5, modulos.getAdj_descarga());
			stmt_insert_modulos_usuarios.setInt(6, modulos.getOc_clasico());
			stmt_insert_modulos_usuarios.setInt(7, modulos.getOc_dinamico());
			stmt_insert_modulos_usuarios.setInt(8, modulos.getOc_descargas());
			stmt_insert_modulos_usuarios.setInt(9, modulos.getReportes());
			stmt_insert_modulos_usuarios.setInt(10, modulos.getMantenedor_catalogo());
			stmt_insert_modulos_usuarios.setInt(11, modulos.getMantenedor_mercado());
			stmt_insert_modulos_usuarios.setInt(12, modulos.getMantenedor_asignacion());
			stmt_insert_modulos_usuarios.setInt(13, modulos.getMantenedor_glosas());

			stmt_insert_modulos_usuarios.addBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void insertDataMercados(int id, Usuario usuario) {
		// Batch con la información de los modulos del usuario
		try {
			String mercado = usuario.getMercado();
			String m[] = mercado.split(";");

			for (int ix = 0; ix < m.length; ix++) {
				stmt_insert_mercado_usuarios.setInt(1, id);
				stmt_insert_mercado_usuarios.setInt(2, usuario.getIdcliente());
				stmt_insert_mercado_usuarios.setInt(3, mercados.get(m[ix]));

				stmt_insert_mercado_usuarios.addBatch();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void insertDataLimites(int id, Usuario usuario) {
		// Batch con la información de los límites del usuario
		try {
			String mercado[] = usuario.getMercado().split(";");

			for (int ix = 0; ix < mercado.length; ix++) {

				int iddato = mercados.get(mercado[ix]);

				stmt_insert_limites_usuarios.setInt(1, id);
				// Acá se debe cambiar para cuando los usuarios tengan límites.
				// Se
				// debe poner el
				// nombre de la tabla del límites. El id del dato en dicha
				// tabla, y
				// el id del mercado al
				// que corresponde el dato.
				stmt_insert_limites_usuarios.setString(2, "filtro_mercado");
				stmt_insert_limites_usuarios.setInt(3, iddato);
				stmt_insert_limites_usuarios.setInt(4, iddato);

				stmt_insert_limites_usuarios.addBatch();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void generarExcel(LinkedHashMap<Integer, Usuario> usuarios) {
		Workbook wb = new SXSSFWorkbook();
		try {
			String nombreHoja1 = "Usuarios";

			SXSSFSheet sheet1 = null;

			sheet1 = (SXSSFSheet) wb.createSheet(nombreHoja1);

			Row row_header = sheet1.createRow(0);

			getXLSHeader(row_header);
			hoja_usuarios(sheet1, usuarios);

			FileOutputStream out = new FileOutputStream(nombreArchivo);
			wb.write(out);
			out.close();
			wb.close();
		} catch (Exception e) {
			try {
				wb.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}

	public static void getXLSHeader(Row row_header) {
		row_header.createCell(0).setCellValue("Id Cliente");
		row_header.createCell(1).setCellValue("Cliente");
		row_header.createCell(2).setCellValue("Id Usuario");
		row_header.createCell(3).setCellValue("Usuario");
		row_header.createCell(4).setCellValue("Contraseña");
	}

	public static void hoja_usuarios(SXSSFSheet sheet1, LinkedHashMap<Integer, Usuario> usuarios) {

		try {
			int contador_licitaciones = 0;

			int fila = 1;

			String[] info_usuarios = new String[5];
			Iterator<?> itera = usuarios.entrySet().iterator();
			while (itera.hasNext()) {
				Map.Entry<?, ?> key = (Map.Entry<?, ?>) itera.next();

				Usuario u = usuarios.get(key.getKey());

				info_usuarios[0] = String.valueOf(u.getIdcliente());
				info_usuarios[1] = u.getCliente();
				info_usuarios[2] = String.valueOf(u.getIdusuario());
				info_usuarios[3] = u.getUsuario();
				info_usuarios[4] = u.getContraseña();

				Row row = sheet1.createRow(fila);
				getXLSRow(row, 5, info_usuarios);

				fila++;
				contador_licitaciones++;
			}
			System.out.println(contador_licitaciones + " Usuarios cargados.");

		} catch (Exception error) {
			error.printStackTrace();
		}
	}

	public static void getXLSRow(Row row, int cant_columnas, String info_postulaciones[]) {
		for (int ix = 0; ix < cant_columnas; ix++) {
			row.createCell(ix).setCellValue(info_postulaciones[ix]);
		}
	}
}
