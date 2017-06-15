package cl.techk.licitaciones.carga;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import cl.techk.ext.database.DBCnx;
import cl.techk.ext.utils.Utils;

@WebServlet("/LoadExcelLicitaciones")
public class LoadExcelLicitaciones extends HttpServlet {

    private static final long serialVersionUID = 1L;
    String file_directory = "/LoadLicitaciones/licitaciones.xlsx";
    static int cantidad_insert_cliente_item = 0;
    static int cantidad_insert_usuario_licitacion = 0;
    static int cantidad_insert_licitacion_item_anexo = 0;
    static int cantidad_insert_cliente_anexo = 0;

    private static PreparedStatement g_stmt_insert_cliente_item = null;
    private static PreparedStatement g_stmt_insert_usuario_licitacion = null;
    private static PreparedStatement g_stmt_update_clasificacion = null;
    private static PreparedStatement g_stmt_insert_licitacion_item_anexo = null;
    private static PreparedStatement g_stmt_insert_cliente_anexo = null;

    private static final String query_insert_cliente_item = "insert ignore into cliente_item (id_item,id_cliente,fecha_asignacion) values (?,?,now())";

    private static final String query_insert_usuario_licitacion = "insert ignore into usuario_licitacion "
            + "(usuario_idusuario,licitacion_idlicitacion,fecha_asignacion,es_favorita) values (?,?,now(),0)";

    private static final String query_update_clasificacion = "update licitacion_item set es_anexo=?,rubro=?,search1=?,search2=? ,"
            + "search3=? ,search4=? ,search5=? ,search6=? ,search7=? ,search8=? where id_licitacion=? and item=?";

    private static final String query_insert_licitacion_item_anexo = "insert ignore into licitacion_item_anexo (id_item,item_anexo,descripcion,rubro,"
            + "search1,search2,search3,search4,search5,search6,search7,search8,"
            + " cantidad) values (?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String query_insert_cliente_anexo = "insert ignore into cliente_anexo (id_anexo,"
            + "id_cliente,fecha_asignacion) values ((select id from licitacion_item_anexo"
            + " where id_item=? and item_anexo =?),?,now())";

    static int dataInsertCont = 1;

    public LoadExcelLicitaciones() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String bd_to_load = "";
        if (request.getParameter("bd") != null && !"".equals(request.getParameter("bd"))) {
            bd_to_load = request.getParameter("bd");

        }

        if (!"".equals(bd_to_load)) {

            Utils.printCargaLic("Cargando licitaciones diarias.  v2.6");
            Utils.printCargaLic("review: 05-12-2016");

            Calendar inicio = Calendar.getInstance();
            long iniciodata = inicio.getTimeInMillis();
            // creo conexion global para todo el proceso de carga

            Connection cnx = null;
            Map<Integer, Map<Integer, Usuario>> usersByCLient = null;
            Map<Integer, UserConfig> configuracionUsuarios = null;

            try {
                String dbClass = "com.mysql.jdbc.Driver";
                Class.forName(dbClass);
                cnx = DriverManager.getConnection(
                        DBCnx.url, DBCnx.user, DBCnx.pass);

                // cargando hashs de configuraciones
                // El "0", indica que no es de un cliente específico (Variable
                // utilizada para la retro)
                usersByCLient = loadUserByCLient(cnx, bd_to_load, "0");
                configuracionUsuarios = loadUserConfigData(cnx, bd_to_load, "0");

                // creando statments
                g_stmt_insert_cliente_item = cnx.prepareStatement(query_insert_cliente_item);
                g_stmt_insert_usuario_licitacion = cnx.prepareStatement(query_insert_usuario_licitacion);
                g_stmt_update_clasificacion = cnx.prepareStatement(query_update_clasificacion);
                g_stmt_insert_licitacion_item_anexo = cnx.prepareStatement(query_insert_licitacion_item_anexo);
                g_stmt_insert_cliente_anexo = cnx.prepareStatement(query_insert_cliente_anexo);

                // insertando items
                insertItems(configuracionUsuarios, usersByCLient, cnx, g_stmt_insert_cliente_item,
                        g_stmt_insert_usuario_licitacion, g_stmt_update_clasificacion, bd_to_load);
                executeBatchsItemsToBd();

                resetearContadores();

                // insertando anexos
                insertAnexos(configuracionUsuarios, usersByCLient, cnx, g_stmt_insert_cliente_item,
                        g_stmt_insert_usuario_licitacion, g_stmt_insert_licitacion_item_anexo,
                        g_stmt_insert_cliente_anexo, bd_to_load);
                executeBatchAnexosToBd();

                resetearContadores();

                // obtener estadistica de carga dia actual
                Calendar fin = Calendar.getInstance();
                long findata = fin.getTimeInMillis();
                /// envio de mail con resumen de carga
                Utils.print("generando mail resumen...");

                String resultado = getResumenCargaDiaria(findata, iniciodata, cnx, bd_to_load);
                 Utils.sendMailHtml(resultado);
                Utils.print("LoadExcelLicitaciones finalizada...");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // limpia hashs de configuraciones
                if (configuracionUsuarios != null) {
                    Utils.print("limpiando hash configuracionUsuarios");
                    configuracionUsuarios.clear();
                    configuracionUsuarios = null;
                }
                if (usersByCLient != null) {
                    Utils.print("limpiando hash usersByCLient");
                    usersByCLient.clear();
                    usersByCLient = null;
                }
                // cierra statments y conexion a bd
                Utils.print("cerrando conexiones y statments");
                DBCnx.close(g_stmt_insert_cliente_item);
                DBCnx.close(g_stmt_insert_usuario_licitacion);
                DBCnx.close(g_stmt_update_clasificacion);
                DBCnx.close(g_stmt_insert_licitacion_item_anexo);
                DBCnx.close(g_stmt_insert_cliente_anexo);
                DBCnx.close(cnx);
            }
        } else {
            Utils.printCargaLic("sin bd");
        }
    }

    public static void executeBatchsItemsToBd() {

        try {
            int[] tot = g_stmt_update_clasificacion.executeBatch();
            Utils.print("insertando  stmt_update_clasificacion... " + tot.length);
        } catch (Exception e) {
            Utils.print("error updateando clasificacion");
        }

        try {
            int[] tot2 = g_stmt_insert_cliente_item.executeBatch();
            Utils.print("insertando  stmt_insert_cliente_item... " + tot2.length);
        } catch (Exception e) {
            e.printStackTrace();
            Utils.print("error insertando cliente_item");
        }
        try {
            int[] tot3 = g_stmt_insert_usuario_licitacion.executeBatch();
            Utils.print("insertando  stmt_insert_usuario_licitacion... " + tot3.length);
        } catch (Exception e) {
            Utils.print("error insertando usuario_licitacion");
        }
    }

    public static void executeBatchAnexosToBd() {

        try {
            int[] tot = g_stmt_insert_licitacion_item_anexo.executeBatch();
            Utils.print("insertando  stmt_insert_licitacion_item_anexo... " + tot.length);
        } catch (Exception e) {
            Utils.print("error insertando  stmt_insert_licitacion_item_anexo... ");
        }
        try {
            int[] tot1 = g_stmt_insert_cliente_anexo.executeBatch();
            Utils.print("insertando  stmt_insert_cliente_anexo... " + tot1.length);
        } catch (Exception e) {
            Utils.print("error insertando  stmt_insert_cliente_anexo... ");
        }
        try {
            int[] tot2 = g_stmt_insert_cliente_item.executeBatch();
            Utils.print("insertando  stmt_insert_cliente_item..." + tot2.length);
        } catch (Exception e) {
            Utils.print("error insertando  stmt_insert_cliente_item... ");
        }
        try {
            int[] tot3 = g_stmt_insert_usuario_licitacion.executeBatch();
            Utils.print("insertando  stmt_insert_usuario_licitacion... " + tot3.length);
        } catch (Exception e) {
            Utils.print("error insertando  stmt_insert_usuario_licitacion... ");
        }
    }

    private static String getResumenCargaDiaria(long findata, long iniciodata, Connection cnx, String bd_to_load) {
        String resultado = "Carga licitaciones completada !, tiempo: " + (((findata - iniciodata) / 1000) / 60)
                + " mins.<br><table>";
        PreparedStatement stmtResume = null;
        ResultSet resultResume = null;
        try {
            stmtResume = cnx.prepareStatement("SELECT c.nombre, u.usuario, COUNT( * ) AS cargadas FROM cliente c JOIN "
                    + "(`usuario_licitacion` ul JOIN usuario u ON ( ul.usuario_idusuario = u.idusuario "
                    + ")) ON ( c.idcliente = u.cliente_idcliente ) WHERE u.perfil_idperfil =1 AND "
                    + "ul.fecha_asignacion >= DATE_FORMAT( NOW( ) ,  '%Y-%m-%d 00:00:00' ) GROUP BY "
                    + "c.nombre, u.usuario");
            resultResume = stmtResume.executeQuery();
            resultado = resultado + "<tr><th>cliente</th> <th>usuario</th><th> cargadas</th><tr>";
            while (resultResume.next()) {
                String cliente = resultResume.getString("nombre");
                String usuario = resultResume.getString("usuario");
                String cargadas = resultResume.getString("cargadas");
                resultado = resultado + "<tr><th>" + cliente + "</th> <th>" + usuario + "</th> <th>" + cargadas
                        + "</th><tr>";
            }
            resultado = resultado + "</table>";
        } catch (SQLException e) {
            Utils.print("error obteniendo resumen de carga");
        } finally {
            DBCnx.closeAll(resultResume, stmtResume);
        }
        return resultado;
    }

    public static Map<Integer, Map<Integer, Usuario>> loadUserByCLient(Connection cnx, String bd_to_load,
            String idcliente) {
        Utils.printCargaLic("obteniendo listado usuarios by cliente");
        // cargo config de todos los users
        Map<Integer, Map<Integer, Usuario>> HashUsuarios = new HashMap<Integer, Map<Integer, Usuario>>();
        PreparedStatement stmtconfigs = null;
        ResultSet resultconfigs = null;
        try {
            String por_cliente = !idcliente.equals("0") ? " AND u.cliente_idcliente =" + idcliente : "";
            stmtconfigs = cnx.prepareStatement("select idusuario,perfil_idperfil,cliente_idcliente from usuario u "
                    + "join modulo_usuario mu on(u.idusuario=mu.id_usr) where mu.licitaciones=1 and u.habilitado=1 "
                    + por_cliente);

            resultconfigs = stmtconfigs.executeQuery();
            while (resultconfigs.next()) {
                int id_usuario = resultconfigs.getInt("idusuario");
                int id_perfil = resultconfigs.getInt("perfil_idperfil");
                int id_cliente = resultconfigs.getInt("cliente_idcliente");
                Usuario userActual = new Usuario(id_usuario, id_perfil);
                if (HashUsuarios.containsKey(id_cliente)) {
                    // si ya existe el cliente, obtengo y actualizo
                    Map<Integer, Usuario> usuariosCLiente = HashUsuarios.get(id_cliente);
                    usuariosCLiente.put(id_usuario, userActual);
                    HashUsuarios.put(id_cliente, usuariosCLiente);
                } else {
                    // si no existe lo creo y agrego
                    Map<Integer, Usuario> usuariosCLiente = new HashMap<Integer, Usuario>();
                    usuariosCLiente.put(id_usuario, userActual);
                    HashUsuarios.put(id_cliente, usuariosCLiente);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCnx.closeAll(resultconfigs, stmtconfigs);
        }
        return HashUsuarios;
    }

    public static Map<Integer, UserConfig> loadUserConfigData(Connection cnx, String bd_to_load, String idcliente) {
        Utils.printCargaLic("obteniendo configuracion de usuario");
        // cargo config de todos los users
        Map<Integer, UserConfig> configuracionUsuarios = new HashMap<Integer, UserConfig>();
        PreparedStatement stmtconfigs = null;
        ResultSet resultconfigs = null;

        try {
            String por_cliente = !idcliente.equals("0") ? " WHERE id_cliente =" + idcliente : "";
            stmtconfigs = cnx.prepareStatement("select * from usuario_parametro_asignacion_lic " + por_cliente);
            resultconfigs = stmtconfigs.executeQuery();

            while (resultconfigs.next()) {
                int id_usuario = resultconfigs.getInt("id_usuario");
                int id_cliente = resultconfigs.getInt("id_cliente");
                // creo la config para el user
                String[] ruts = null;

                if (resultconfigs.getString("ruts") != null && !resultconfigs.getString("ruts").equals("")) {
                    ruts = resultconfigs.getString("ruts").split(";");
                }

                Map<String, Integer> rutsHash = new HashMap<String, Integer>();

                if (ruts != null) {
                    for (int i = 0; i < ruts.length; i++) {
                        rutsHash.put(ruts[i].toLowerCase(), id_usuario);
                    }
                }

                String[] tipos = null;

                if (resultconfigs.getString("tipos_licitacion") != null
                        && !resultconfigs.getString("tipos_licitacion").equals("")) {

                    tipos = resultconfigs.getString("tipos_licitacion").split(";");
                }

                Map<String, Integer> tiposHash = new HashMap<String, Integer>();

                if (tipos != null) {
                    for (int i = 0; i < tipos.length; i++) {
                        tiposHash.put(tipos[i].toLowerCase(), id_usuario);
                    }
                }

                UserConfig usuarioActual = new UserConfig(id_usuario, id_cliente, rutsHash, tiposHash);
                configuracionUsuarios.put(id_usuario, usuarioActual);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCnx.closeAll(resultconfigs, stmtconfigs);
        }

        return configuracionUsuarios;
    }

    private void insertItems(Map<Integer, UserConfig> configuracionUsuarios,
            Map<Integer, Map<Integer, Usuario>> usersByCLient, Connection cnx,
            PreparedStatement stmt_insert_cliente_item, PreparedStatement stmt_insert_usuario_licitacion,
            PreparedStatement stmt_update_clasificacion, String bd_to_load) {

        try {

            HashMap<String, String> licitations_uploaded = new HashMap<String, String>();
            FileInputStream file = new FileInputStream(new File(file_directory));
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            // saltar cabecera
            rowIterator.next();
            int filaActual = 1;
            // int dataInsertCont = 1;
            dataInsertCont = 1;

            while (rowIterator.hasNext()) {
                Utils.printCargaLic("registro item: " + filaActual);
                // itero los registros de excel
                Row row = rowIterator.next();
                // valido que hayan datos en la final del excel

                String codigo_st = null;

                try {
                    codigo_st = row.getCell(0).getStringCellValue();
                } catch (Exception e) {
                    Utils.print("error get codigo");
                }

                if (codigo_st != null) {
                    if (!"".equals(codigo_st)) {

                        licitations_uploaded.put(codigo_st, codigo_st);
                        String rubro = "", anexo = "", search1 = Utils.getValueFromXlsRow(row, 4),
                                search2 = Utils.getValueFromXlsRow(row, 5), search3 = Utils.getValueFromXlsRow(row, 6),
                                search4 = Utils.getValueFromXlsRow(row, 7), search5 = Utils.getValueFromXlsRow(row, 8),
                                search6 = Utils.getValueFromXlsRow(row, 9), search7 = Utils.getValueFromXlsRow(row, 10),
                                search8 = Utils.getValueFromXlsRow(row, 11), codigo = null, item = null;

                        try {
                            codigo = row.getCell(0).getStringCellValue();
                        } catch (Exception e) {
                        }
                        try {
                            item = String.valueOf((int) row.getCell(1).getNumericCellValue());
                        } catch (Exception e) {
                        }
                        try {
                            rubro = row.getCell(2).getStringCellValue();
                        } catch (Exception e) {
                        }
                        try {
                            anexo = row.getCell(3).getStringCellValue();
                        } catch (Exception e) {
                        }
                        // UPDATEO CLASIFICACION DE LICITACION
                        PreparedStatement stmt = null;
                        ResultSet resultLic = null;

                        PreparedStatement stmt_lic_item = null;
                        ResultSet resultLic_lic_item = null;

                        try {
                            // obtengo id interna de licitacion en BD
                            // Se usa para updatear en base al id_licitación y
                            // el n° item.
                            stmt = cnx.prepareStatement("select id from licitacion where codigo =?");
                            stmt.setString(1, codigo);
                            resultLic = stmt.executeQuery();
                            if (resultLic.next()) {
                                int id_lic_interna = resultLic.getInt("id");
                                try {
                                    stmt_update_clasificacion.setString(1, anexo);
                                    stmt_update_clasificacion.setString(2, rubro);
                                    stmt_update_clasificacion.setString(3, search1);
                                    stmt_update_clasificacion.setString(4, search2);
                                    stmt_update_clasificacion.setString(5, search3);
                                    stmt_update_clasificacion.setString(6, search4);
                                    stmt_update_clasificacion.setString(7, search5);
                                    stmt_update_clasificacion.setString(8, search6);
                                    stmt_update_clasificacion.setString(9, search7);
                                    stmt_update_clasificacion.setString(10, search8);
                                    stmt_update_clasificacion.setInt(11, id_lic_interna);
                                    stmt_update_clasificacion.setString(12, item);
                                    stmt_update_clasificacion.addBatch();

                                    // select id licitacion item interna
                                    // Obtengo el id de la licitacion_item para
                                    // poder asignarle a
                                    // los clientes correspondientes el item.
                                    int id_lic_item_interno = -1;
                                    stmt_lic_item = cnx.prepareStatement(
                                            "select id from licitacion_item where id_licitacion=? and item =?");
                                    stmt_lic_item.setInt(1, id_lic_interna);
                                    stmt_lic_item.setInt(2, Integer.valueOf(item));
                                    resultLic_lic_item = stmt_lic_item.executeQuery();
                                    if (resultLic_lic_item.next()) {
                                        id_lic_item_interno = resultLic_lic_item.getInt("id");
                                    }
                                    // asigno items a clientes
                                    asignarItemsClientes(Integer.valueOf(item), search1, search2, search3, search4,
                                            search5, search6, search7, search8, id_lic_interna, -1, false,
                                            configuracionUsuarios, usersByCLient, cnx, stmt_insert_cliente_item,
                                            stmt_insert_usuario_licitacion, id_lic_item_interno, bd_to_load, false,
                                            false);
                                } catch (Exception e) {
                                    Utils.printErrCargaLic("error updateando clasificacion de item");
                                } finally {
                                    DBCnx.close(resultLic_lic_item);
                                    DBCnx.close(stmt_lic_item);
                                }
                            } else {
                                Utils.printCargaLic("la licitacion no existe ");
                            }
                        } catch (Exception e) {
                            Utils.printErrCargaLic("ERROR -> Cargando clasificacion items licitaciones (1)");
                        } finally {
                            DBCnx.closeAll(resultLic, stmt);
                        }
                    } else {
                        Utils.print("registro vacio");
                    }
                } else {
                    Utils.print("registro null");
                }
                filaActual++;
                comprobarCantidadInsertItems(dataInsertCont);
                dataInsertCont++;
            }

            workbook.close();
            file.close();

            updateUploadsLicitations(licitations_uploaded);
        } catch (Exception e) {
            Utils.printErrCargaLic("ERROR -> Cargando clasificacion items licitaciones (2)");
            e.printStackTrace();
        }
    }

    public static void asignarItemsClientes(int item, String licitacion_search1, String licitacion_search2,
            String licitacion_search3, String licitacion_search4, String licitacion_search5, String licitacion_search6,
            String licitacion_search7, String licitacion_search8, int idLicitacion, int idcliente_in,
            boolean only_this_client, Map<Integer, UserConfig> configuracionUsuarios,
            Map<Integer, Map<Integer, Usuario>> usersByCLient, Connection cnx,
            PreparedStatement stmt_insert_cliente_item, PreparedStatement stmt_insert_usuario_licitacion,
            int id_lic_item_interno, String bd_to_load, boolean is_validacion_anexo, boolean is_retro) {

        PreparedStatement stmt = null;
        ResultSet resultLic = null;

        try {

            if (is_retro) {
                g_stmt_insert_cliente_item = stmt_insert_cliente_item;
                g_stmt_insert_usuario_licitacion = stmt_insert_usuario_licitacion;
            }

            // OBTENGO QUERY PARA MATCHEAR CON DICCIONARIO
            String query_clientes_id = getQueryMatchDiccionario(licitacion_search1, licitacion_search2,
                    licitacion_search3, licitacion_search4, licitacion_search5, licitacion_search6, licitacion_search7,
                    licitacion_search8, bd_to_load);

            // add para asignacion a cliente especifco
            // Esta opción se usa para el caso de cuando se agrega un mercado
            // relevante desde la plataforma
            if (only_this_client) {
                query_clientes_id = query_clientes_id + " and id_cliente=" + idcliente_in;
            }

            query_clientes_id += " GROUP BY id_cliente";
            stmt = cnx.prepareStatement(query_clientes_id);

            resultLic = stmt.executeQuery();

            while (resultLic.next()) {
                // si existen clientes tienen esa
                // combinacion de keywords
                // OBTENGO ID CLIENTE
                int idcliente = resultLic.getInt("id_cliente");

                // ASIGNO RELACION CLIENTE_ITEM
                insertRelacionClienteItem(idLicitacion, item, idcliente, cnx, stmt_insert_cliente_item,
                        id_lic_item_interno, is_validacion_anexo);
                // ASIGNO LICITACION A USUARIOS DE ESE CLIENTE SEGUN SU CONFIG
                try {
                    // Reviso si existen usuarios para este ciente en el hash
                    if (usersByCLient.containsKey(idcliente)) {
                        // Obtengo los usuarios del cliente
                        Map<Integer, Usuario> usuariosDelCLiente = usersByCLient.get(idcliente);
                        Iterator<?> it = usuariosDelCLiente.entrySet().iterator();

                        // Recorro cada usuario para revisar si se le debe
                        // asignar la licitacion
                        while (it.hasNext()) {
                            Map.Entry<?, ?> e = (Map.Entry<?, ?>) it.next();
                            Usuario userActual = usuariosDelCLiente.get(e.getKey());

                            int idUsuario = userActual.getId_usuario();
                            if (userActual.getId_perfil() == 1) {
                                // si es admin se insertan todos
                                insertRelacionUsuarioLicitacion(idUsuario, idLicitacion, cnx,
                                        stmt_insert_usuario_licitacion, is_validacion_anexo);
                            } else {
                                // no es admin se obtiene su configuracion de
                                // asignacion
                                if (configuracionUsuarios.containsKey(idUsuario)) {

                                    UserConfig userSelected = configuracionUsuarios.get(idUsuario);
                                    // obtengo detalle licitacion
                                    PreparedStatement stmtlicdet = cnx
                                            .prepareStatement("select codigo,rut from licitacion where id=?");
                                    stmtlicdet.setInt(1, idLicitacion);
                                    ResultSet resultlicdet = stmtlicdet.executeQuery();
                                    if (resultlicdet.next()) {
                                        boolean insertarRut = false;
                                        if (userSelected.getRuts().size() > 0) {
                                            if (userSelected.getRuts()
                                                    .containsKey(resultlicdet.getString("rut").toLowerCase())) {
                                                insertarRut = true;
                                            }
                                        } else {
                                            // no piden rut solo tipo
                                            insertarRut = true;
                                        }
                                        boolean insertarTipo = false;
                                        if (insertarRut) {

                                            String tipoLicCLearArray[] = resultlicdet.getString("codigo").split("-");
                                            String tipoLicCLear = tipoLicCLearArray[2];
                                            tipoLicCLear = tipoLicCLear.substring(0, 2).toLowerCase();

                                            if (userSelected.getTipos().size() > 0) {
                                                if (userSelected.getTipos().containsKey(tipoLicCLear)) {
                                                    insertarTipo = true;
                                                }
                                            } else {
                                                // no piden tipo solo rut
                                                insertarTipo = true;
                                            }
                                            if (insertarTipo && insertarRut) {
                                                insertRelacionUsuarioLicitacion(idUsuario, idLicitacion, cnx,
                                                        stmt_insert_usuario_licitacion, is_validacion_anexo);
                                            }
                                        }
                                    } else {
                                        Utils.print("no existen datos de esa licitacion");
                                    }
                                } else {
                                    // SI EL USER NO S ADMIN Y NO TINE CONFIG ,
                                    // SE
                                    // ASIGNA IGUAL QUE FUERA ADMIN
                                    // UPDATED - 02-12-2015
                                    insertRelacionUsuarioLicitacion(idUsuario, idLicitacion, cnx,
                                            stmt_insert_usuario_licitacion, is_validacion_anexo);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Utils.printErrCargaLic("error insertando relacion licitacion a usuario ");
                } finally {
                }
            }
        } catch (Exception e) {
            Utils.printErrCargaLic("ERROR -> obteniendo clientes  asociados (1)");
        } finally {
            DBCnx.closeAll(resultLic, stmt);
        }
    }

    private static String getQueryMatchDiccionario(String licitacion_search1, String licitacion_search2,
            String licitacion_search3, String licitacion_search4, String licitacion_search5, String licitacion_search6,
            String licitacion_search7, String licitacion_search8, String bd_to_load) {
        // si los niveles de busqueda encontrados no son todos no se consideran
        // desde ese en adelante para el match con el diccionario
        String query_clientes = "select id_cliente from cliente_diccionario where search1='" + licitacion_search1 + "'";
        // si la clasificacion tiene nivel 2
        if (!"".equals(licitacion_search2)) {
            // agrego a busq en diccionario + la opcion de busq de "vacios"
            query_clientes = query_clientes + " and (search2='" + licitacion_search2 + "' or search2='')";
            // busco los que tienen el detalle de search2 o tienen search2
            // vacio, (significa que qieren todo sin importar la
            // profundidad)
            if (!"".equals(licitacion_search3)) {
                query_clientes = query_clientes + " and (search3='" + licitacion_search3 + "' or search3='')";
                if (!"".equals(licitacion_search4)) {
                    query_clientes = query_clientes + " and (search4='" + licitacion_search4 + "' or search4='')";
                    if (!"".equals(licitacion_search5)) {
                        query_clientes = query_clientes + " and (search5='" + licitacion_search5 + "' or search5='')";
                        if (!"".equals(licitacion_search6)) {
                            query_clientes = query_clientes + " and (search6='" + licitacion_search6
                                    + "' or search6='')";
                            if (!"".equals(licitacion_search7)) {
                                query_clientes = query_clientes + " and (search7='" + licitacion_search7
                                        + "' or search7='')";
                                if (!"".equals(licitacion_search8)) {
                                    query_clientes = query_clientes + " and (search8='" + licitacion_search8
                                            + "' or search8='')";
                                }
                            }
                        }
                    }
                }

            }
        }
        return query_clientes;
    }

    public static void insertRelacionClienteItem(int idLicitacion, int item, int idcliente, Connection cnx,
            PreparedStatement stmt_insert_cliente_item, int id_licitacion_item, boolean is_anexo_validacion) {

        try {
            stmt_insert_cliente_item.setInt(1, id_licitacion_item);
            stmt_insert_cliente_item.setInt(2, idcliente);
            stmt_insert_cliente_item.addBatch();

            cantidad_insert_cliente_item++;

            if (is_anexo_validacion) {
                comprobarCantidadInsertAnexo(dataInsertCont);
            } else {
                comprobarCantidadInsertItems(dataInsertCont);
            }
        } catch (MySQLIntegrityConstraintViolationException e) {
            Utils.print("relacion cliente _item ya existe: cliente: " + idcliente + " - item : " + item);
        } catch (Exception e) {
            Utils.printErrCargaLic("error insertando relacion cliente_item");
        } finally {
        }
    }

    private static void insertRelacionUsuarioLicitacion(int idUsuario, int idLicitacion, Connection cnx,
            PreparedStatement stmt_insert_usuario_licitacion, boolean is_validacion_anexo) {

        try {
            stmt_insert_usuario_licitacion.setInt(1, idUsuario);
            stmt_insert_usuario_licitacion.setInt(2, idLicitacion);
            stmt_insert_usuario_licitacion.addBatch();

            cantidad_insert_usuario_licitacion++;

            if (is_validacion_anexo) {
                comprobarCantidadInsertAnexo(dataInsertCont);
            } else {
                comprobarCantidadInsertItems(dataInsertCont);
            }
        } catch (MySQLIntegrityConstraintViolationException e) {
            Utils.print(
                    "la relacion usuario_licitacion ya existe user: " + idUsuario + "- licitacion: " + idLicitacion);
        } catch (Exception e) {
            Utils.printErrCargaLic("error insertando relacion usuario_licitacion");
        }
    }

    private void insertAnexos(Map<Integer, UserConfig> configuracionUsuarios,
            Map<Integer, Map<Integer, Usuario>> usersByCLient, Connection cnx,
            PreparedStatement stmt_insert_cliente_item, PreparedStatement stmt_insert_usuario_licitacion,
            PreparedStatement stmt_insert_licitacion_item_anexo, PreparedStatement stmt_insert_cliente_anexo,
            String bd_to_load) {
        try {

            FileInputStream file = new FileInputStream(new File(file_directory));
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheetAt(1);
            Iterator<Row> rowIterator = sheet.iterator();
            rowIterator.next(); // ME SALTO LA CABECERA
            int filaActual = 1;

            dataInsertCont = 1;
            while (rowIterator.hasNext()) {

                Row row = rowIterator.next();
                Utils.printCargaLic("registro anexo: " + filaActual);
                String codigo = null, item = null, item_anexo = null, descripcion = "", cantidad = "", rubro = "";
                String search1 = Utils.getValueFromXlsRow(row, 6), search2 = Utils.getValueFromXlsRow(row, 7),
                        search3 = Utils.getValueFromXlsRow(row, 8), search4 = Utils.getValueFromXlsRow(row, 9),
                        search5 = Utils.getValueFromXlsRow(row, 10), search6 = Utils.getValueFromXlsRow(row, 11),
                        search7 = Utils.getValueFromXlsRow(row, 12), search8 = Utils.getValueFromXlsRow(row, 13);
                // salto header
                try {
                    codigo = row.getCell(0).getStringCellValue();
                } catch (Exception e) {
                }
                try {
                    item = String.valueOf((int) row.getCell(1).getNumericCellValue());
                } catch (Exception e) {
                }
                try {
                    item_anexo = String.valueOf((int) row.getCell(2).getNumericCellValue());
                } catch (Exception e) {
                }
                try {
                    descripcion = row.getCell(3).getStringCellValue();
                } catch (Exception e) {
                }
                try {
                    cantidad = String.valueOf((int) row.getCell(4).getNumericCellValue());
                } catch (Exception e) {
                }
                try {
                    rubro = row.getCell(5).getStringCellValue();
                } catch (Exception e) {
                }

                PreparedStatement stmt = null;
                ResultSet resultLic = null;
                try {
                    // Obtengo el id de la licitación para poder buscar los
                    // items de ésta
                    stmt = cnx.prepareStatement("select id from licitacion where codigo =?");
                    stmt.setString(1, codigo);
                    resultLic = stmt.executeQuery();
                    if (resultLic.next()) {
                        int id_lic_interna = resultLic.getInt("id");
                        PreparedStatement stmt2 = null;
                        ResultSet resultLic2 = null;
                        try {
                            // Obtengo el id del item de la licitación para
                            // asignar el anexo a dicho item en la
                            // tabla licitacion_item_anexo
                            stmt2 = cnx.prepareStatement(
                                    "select id from licitacion_item where id_licitacion =? and item = ?");
                            stmt2.setInt(1, id_lic_interna);
                            stmt2.setString(2, item);
                            resultLic2 = stmt2.executeQuery();
                            if (resultLic2.next()) {
                                int id_interna_item_licitacion = resultLic2.getInt("id");
                                try {
                                    stmt_insert_licitacion_item_anexo.setInt(1, id_interna_item_licitacion);
                                    stmt_insert_licitacion_item_anexo.setString(2, item_anexo);
                                    stmt_insert_licitacion_item_anexo.setString(3, descripcion);
                                    stmt_insert_licitacion_item_anexo.setString(4, rubro);
                                    stmt_insert_licitacion_item_anexo.setString(5, search1);
                                    stmt_insert_licitacion_item_anexo.setString(6, search2);
                                    stmt_insert_licitacion_item_anexo.setString(7, search3);
                                    stmt_insert_licitacion_item_anexo.setString(8, search4);
                                    stmt_insert_licitacion_item_anexo.setString(9, search5);
                                    stmt_insert_licitacion_item_anexo.setString(10, search6);
                                    stmt_insert_licitacion_item_anexo.setString(11, search7);
                                    stmt_insert_licitacion_item_anexo.setString(12, search8);
                                    stmt_insert_licitacion_item_anexo.setString(13, cantidad);
                                    stmt_insert_licitacion_item_anexo.addBatch();
                                    asignarAnexosClientes(item_anexo, search1, search2, search3, search4, search5,
                                            search6, search7, search8, id_interna_item_licitacion, id_lic_interna,
                                            Integer.valueOf(item), -1, false, configuracionUsuarios, usersByCLient, cnx,
                                            stmt_insert_cliente_item, stmt_insert_usuario_licitacion,
                                            stmt_insert_cliente_anexo, bd_to_load, false);
                                } catch (MySQLIntegrityConstraintViolationException ex) {
                                    Utils.printErrCargaLic("el anexo ya existe para ese item");
                                }
                            }
                        } catch (Exception e) {
                            Utils.printErrCargaLic("el item no existe ");
                        } finally {
                            DBCnx.close(stmt2);
                        }
                    } else {
                        Utils.printErrCargaLic("la licitacion no existe ");
                    }
                } catch (Exception e) {
                    Utils.printErrCargaLic("ERROR -> Cargando anexos de items (1)");
                } finally {
                    DBCnx.closeAll(resultLic, stmt);
                }

                filaActual++;
                comprobarCantidadInsertAnexo(dataInsertCont);
                dataInsertCont++;
            }
            workbook.close();
            file.close();
        } catch (Exception e) {
            Utils.printErrCargaLic("ERROR -> Cargando anexos de items (2)");
        }
    }

    public static void asignarAnexosClientes(String item_anexo, String licitacion_search1, String licitacion_search2,
            String licitacion_search3, String licitacion_search4, String licitacion_search5, String licitacion_search6,
            String licitacion_search7, String licitacion_search8, int id_interna_item_licitacion, int id_lic_interna,
            int item, int idcliente_in, boolean only_this_client, Map<Integer, UserConfig> configuracionUsuarios,
            Map<Integer, Map<Integer, Usuario>> usersByCLient, Connection cnx,
            PreparedStatement stmt_insert_cliente_item, PreparedStatement stmt_insert_usuario_licitacion,
            PreparedStatement stmt_insert_cliente_anexo, String bd_to_load, boolean is_retro) {

        PreparedStatement stmt = null;
        ResultSet resultLic = null;
        try {

            if (is_retro) {
                g_stmt_insert_cliente_item = stmt_insert_cliente_item;
                g_stmt_insert_usuario_licitacion = stmt_insert_usuario_licitacion;
                g_stmt_insert_cliente_anexo = stmt_insert_cliente_anexo;
            }

            // obtengo id interna de licitacion

            // Obtengo la query para los clientes que hacen match su mercado
            // relevante y la clasificación
            // del item anexo
            String query_clientes_id = getQueryMatchDiccionario(licitacion_search1, licitacion_search2,
                    licitacion_search3, licitacion_search4, licitacion_search5, licitacion_search6, licitacion_search7,
                    licitacion_search8, bd_to_load);
            // add para asignacion a cliente especifco
            // Validación útil para cuando se agrega un mercado relevante desde
            // la plataforma
            if (only_this_client) {
                query_clientes_id = query_clientes_id + " and id_cliente=" + idcliente_in;
            }
            // Agrupo por clientes para evitar que se ejecute el ciclo de más
            // abajo con el mismo cliente
            // varias veces
            query_clientes_id += " GROUP BY id_cliente";
            stmt = cnx.prepareStatement(query_clientes_id);
            resultLic = stmt.executeQuery();

            while (resultLic.next()) {
                int idcliente = resultLic.getInt("id_cliente");

                try {
                    // INSERTANDO EN CLIENTE ANEXO
                    stmt_insert_cliente_anexo.setInt(1, id_interna_item_licitacion);
                    stmt_insert_cliente_anexo.setInt(2, Integer.valueOf(item_anexo));
                    stmt_insert_cliente_anexo.setInt(3, idcliente);
                    stmt_insert_cliente_anexo.addBatch();

                    cantidad_insert_cliente_anexo++;
                    comprobarCantidadInsertAnexo(dataInsertCont);
                } catch (MySQLIntegrityConstraintViolationException e) {
                    Utils.printErrCargaLic("error la relacion cliente_anexo ya existe cliente: " + idcliente
                            + " - anexo : " + item_anexo);
                }

                // AGREGADO
                // asignar relacion item cliente
                // y asignar relacion licitacion usuario
                insertRelacionClienteItem(id_lic_interna, item, idcliente, cnx, stmt_insert_cliente_item,
                        id_interna_item_licitacion, true);
                asignarItemsClientes(item, licitacion_search1, licitacion_search2, licitacion_search3,
                        licitacion_search4, licitacion_search5, licitacion_search6, licitacion_search7,
                        licitacion_search8, id_lic_interna, -1, false, configuracionUsuarios, usersByCLient, cnx,
                        stmt_insert_cliente_item, stmt_insert_usuario_licitacion, id_interna_item_licitacion,
                        bd_to_load, true, false);
            }
        } catch (Exception e) {
            Utils.printErrCargaLic("ERROR -> obteniendo clientes  asociados (1)");
            e.printStackTrace();
        } finally {
            DBCnx.closeAll(resultLic, stmt);
        }
    }

    private static void comprobarCantidadInsertItems(int cantidad_updates) {
        if (cantidad_updates == 1500 || cantidad_insert_cliente_item >= 1500
                || cantidad_insert_usuario_licitacion >= 1500) {

            executeBatchsItemsToBd();
            cantidad_updates = 0;
            cantidad_insert_cliente_item = 0;
            cantidad_insert_usuario_licitacion = 0;
        }
    }

    private static void comprobarCantidadInsertAnexo(int cantidad_updates) {
        if (dataInsertCont == 1500 || cantidad_insert_licitacion_item_anexo >= 1500
                || cantidad_insert_cliente_anexo >= 1500 || cantidad_insert_cliente_item >= 1500
                || cantidad_insert_usuario_licitacion >= 1500) {

            executeBatchAnexosToBd();
            cantidad_insert_licitacion_item_anexo = 0;
            cantidad_insert_cliente_anexo = 0;
            cantidad_insert_cliente_item = 0;
            cantidad_insert_usuario_licitacion = 0;
            dataInsertCont = 0;
        }
    }

    private static void resetearContadores() {
        cantidad_insert_licitacion_item_anexo = 0;
        cantidad_insert_cliente_anexo = 0;
        cantidad_insert_cliente_item = 0;
        cantidad_insert_usuario_licitacion = 0;
    }

    public static void setStatements() {
        Connection cnx = null;

        try {
            cnx = DBCnx.conexion();
            g_stmt_insert_cliente_item = cnx.prepareStatement(query_insert_cliente_item);
            g_stmt_insert_usuario_licitacion = cnx.prepareStatement(query_insert_usuario_licitacion);
            g_stmt_insert_cliente_anexo = cnx.prepareStatement(query_insert_cliente_anexo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateUploadsLicitations(HashMap<String, String> licitations_uploaded) {

        String query_update_upload_licitation_date = "UPDATE licitacion SET uploaded_in_point = NOW() WHERE codigo = ?";
        Iterator<String> licitations_codes = licitations_uploaded.keySet().iterator();
        PreparedStatement stmt_update_upload_licitation_date = null;
        Connection database_connection = null;

        try {
            database_connection = DBCnx.conexion();
            stmt_update_upload_licitation_date = database_connection.prepareStatement(query_update_upload_licitation_date);
            
            while (licitations_codes.hasNext()) {
                stmt_update_upload_licitation_date.setString(1, licitations_codes.next());
                stmt_update_upload_licitation_date.addBatch();
            }
            
            int uploaded_licitations[] = stmt_update_upload_licitation_date.executeBatch();
            Utils.print("Licitaciones updateadas:"+uploaded_licitations.length);
        } catch (Exception error) {
            error.printStackTrace();
        } finally {
            DBCnx.close(stmt_update_upload_licitation_date);
            DBCnx.close(database_connection);
        }
    }
}
