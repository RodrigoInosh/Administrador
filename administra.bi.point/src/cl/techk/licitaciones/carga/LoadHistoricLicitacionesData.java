package cl.techk.licitaciones.carga;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cl.techk.ext.database.DBCnx;
import cl.techk.ext.database.DataManager;
import cl.techk.ext.utils.Utils;

/**
 * carga de licitaciones retroactivas a todos los cliente , solo de las
 * licitaciones que estan actualmente abiertas
 * 
 * @version version 2.0
 * @author felipe aguilera
 * @fixes 
 * last fixes: A las funciones de asginación de items y anexos se les agregó un parámetro que indica si es una llamada
 * desde la retro o desde la carga de licitaciones.
 * Se cambió el proceso de retro para que funcione por cliente.
 */

@WebServlet("/LoadHistoricLicitacionesData")
public class LoadHistoricLicitacionesData extends HttpServlet {
	private static final long serialVersionUID = 1L;
	String archivo_rute = "/LoadLicitaciones/licitaciones.xlsx";

	public LoadHistoricLicitacionesData() {
		super();
	}

	/**
	 * @param idcliente, cliente al que se le hará la retro
	 *  
	 * */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Calendar inicio = Calendar.getInstance();
		long iniciodata = inicio.getTimeInMillis();

		String idcliente = request.getParameter("clte");
		
		String bd_to_load = "";
		Utils.printRetroLic("Cargando data historica de licitaciones abiertas para nuevo USER/CLIENT/DICCIONARIO.");
		// cargando hashs de configuraciones

		String query_insert_cliente_item = "insert ignore into cliente_item (id_item,id_cliente,fecha_asignacion) "
				+ "values (?,?,now())";

		String query_insert_usuario_licitacion = "insert ignore into usuario_licitacion "
				+ "(usuario_idusuario,licitacion_idlicitacion,fecha_asignacion,es_favorita) values (?,?,now(),0)";

		String query_insert_cliente_anexo = "insert ignore into cliente_anexo (id_anexo,"
				+ "id_cliente,fecha_asignacion) values ((select id from licitacion_item_anexo"
				+ " where id_item=? and item_anexo =?),?,now())";

		Connection cnx = null;
		PreparedStatement stmt_insert_cliente_item = null;
		PreparedStatement stmt_insert_usuario_licitacion = null;
		PreparedStatement stmt_insert_cliente_anexo = null;
		Map<Integer, Map<Integer, Usuario>> usersByCLient = null;
		Map<Integer, UserConfig> configuracionUsuarios = null;
		try {
			cnx = DBCnx.conexion();
			stmt_insert_cliente_item = cnx.prepareStatement(query_insert_cliente_item);
			stmt_insert_usuario_licitacion = cnx.prepareStatement(query_insert_usuario_licitacion);
			stmt_insert_cliente_anexo = cnx.prepareStatement(query_insert_cliente_anexo);

			usersByCLient = LoadExcelLicitaciones.loadUserByCLient(cnx, bd_to_load, idcliente);
			configuracionUsuarios = LoadExcelLicitaciones.loadUserConfigData(cnx, bd_to_load, idcliente);

			insertItems("", "", Integer.parseInt(idcliente), true, configuracionUsuarios, usersByCLient, stmt_insert_cliente_item,
					stmt_insert_usuario_licitacion, stmt_insert_cliente_anexo, cnx, bd_to_load);
			Calendar fin = Calendar.getInstance();

			executeBatchsItemsToBd(stmt_insert_cliente_item, stmt_insert_usuario_licitacion);
			executeBatchAnexosToBd(stmt_insert_cliente_anexo);

			long findata = fin.getTimeInMillis();
			Utils.print("tiempo carga: " + (((findata - iniciodata) / 1000) / 60) + " mins.");
			Utils.sendMailHtml("FIN Proceso Licitaciones Retroactiva para cliente "
					+ ""+DataManager.getCliente(idcliente)+". Tiempo carga: "
					+ (((findata - iniciodata) / 1000) / 60) + " mins.");
			
			Utils.print("carga finalizada  LoadHistoricLicitacionesData.");

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
			// cerrando conexiones
			DBCnx.close(stmt_insert_cliente_item);
			DBCnx.close(stmt_insert_usuario_licitacion);
			DBCnx.close(stmt_insert_cliente_anexo);
			DBCnx.close(cnx);
		}
	}

	public static void executeBatchsItemsToBd(PreparedStatement stmt_insert_cliente_item,
			PreparedStatement stmt_insert_usuario_licitacion) {
		try {
			int[] tot = stmt_insert_cliente_item.executeBatch();
			Utils.print("insertando  stmt_insert_cliente_item... " + tot.length);
			int[] tot2 = stmt_insert_usuario_licitacion.executeBatch();
			Utils.print("insertando  stmt_insert_usuario_licitacion... " + tot2.length);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void executeBatchAnexosToBd(PreparedStatement stmt_insert_cliente_anexo) {
		try {
			int[] tot = stmt_insert_cliente_anexo.executeBatch();
			Utils.print("insertando  stmt_insert_cliente_anexo... " + tot.length);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void insertItems(String datos, String datos_anexos, int idcliente_in, boolean only_this_client,
			Map<Integer, UserConfig> configuracionUsuarios, Map<Integer, Map<Integer, Usuario>> usersByCLient,
			PreparedStatement stmt_insert_cliente_item, PreparedStatement stmt_insert_usuario_licitacion,
			PreparedStatement stmt_insert_cliente_anexo, Connection cnx, String bd_to_load) {

		PreparedStatement stmt = null;
		ResultSet resultLic = null;
		int cant_lic = 0;

		try {
			Utils.printRetroLic("obteniendo licitaciones abiertas");
			// obtengo historico de licitaciones abiertas
			stmt = cnx.prepareStatement("select id from licitacion where fecha_cierre >=now()");
			resultLic = stmt.executeQuery();

			LoadExcelLicitaciones.setStatements();
			Utils.printRetroLic("obteniendo items de licitacion ->");

			while (resultLic.next()) {
				Utils.print("recorriendo registro hist: " + cant_lic);
				int id_lic_interna = resultLic.getInt("id");
				PreparedStatement stmt_data_items = null;
				ResultSet resul_data_titems = null;

				try {
					stmt_data_items = cnx.prepareStatement(
							" select * from licitacion_item where id_licitacion=? " + datos + " order by item asc");
					stmt_data_items.setInt(1, id_lic_interna);
					resul_data_titems = stmt_data_items.executeQuery();

					while (resul_data_titems.next()) {
						// asigno items a clientes y licitaciones a usuarios
						LoadExcelLicitaciones.asignarItemsClientes(resul_data_titems.getInt("item"),
								resul_data_titems.getString("search1"), resul_data_titems.getString("search2"),
								resul_data_titems.getString("search3"), resul_data_titems.getString("search4"),
								resul_data_titems.getString("search5"), resul_data_titems.getString("search6"),
								resul_data_titems.getString("search7"), resul_data_titems.getString("search8"),
								id_lic_interna, idcliente_in, only_this_client, configuracionUsuarios, usersByCLient,
								cnx, stmt_insert_cliente_item, stmt_insert_usuario_licitacion,
								resul_data_titems.getInt("id"), bd_to_load, false, true);
						// GET ANEXOS reviso si el item tiene anexos
						PreparedStatement stmt2 = null;
						ResultSet resultLic2 = null;
						try {
							stmt2 = cnx.prepareStatement(
									"select * from licitacion_item_anexo where id_item =?" + datos_anexos);
							stmt2.setInt(1, resul_data_titems.getInt("id"));

							resultLic2 = stmt2.executeQuery();
							while (resultLic2.next()) {
								LoadExcelLicitaciones.asignarAnexosClientes(resultLic2.getString("item_anexo"),
										resultLic2.getString("search1"), resultLic2.getString("search2"),
										resultLic2.getString("search3"), resultLic2.getString("search4"),
										resultLic2.getString("search5"), resultLic2.getString("search6"),
										resultLic2.getString("search7"), resultLic2.getString("search8"),
										resul_data_titems.getInt("id"), id_lic_interna,
										resul_data_titems.getInt("item"), idcliente_in, only_this_client, configuracionUsuarios,
										usersByCLient, cnx, stmt_insert_cliente_item, stmt_insert_usuario_licitacion,
										stmt_insert_cliente_anexo, bd_to_load, true);
							}
						} catch (Exception e) {
							Utils.print("error obteniendo historico de anexos de item");
							e.printStackTrace();
						} finally {
							DBCnx.closeAll(resultLic2, stmt2);
						}
					}
				} catch (Exception e) {
					Utils.print("error leyendo tabla historica licitaciones");
					e.printStackTrace();
					break;
				} finally {
					DBCnx.closeAll(resul_data_titems, stmt_data_items);
				}
				cant_lic++;
			}
			LoadExcelLicitaciones.executeBatchsItemsToBd();
			LoadExcelLicitaciones.executeBatchAnexosToBd();

		} catch (Exception e) {
			Utils.printErrRetroLic("ERROR -> asignando licitaciones historicas abiertas.");
		} finally {
			// ex.shutdownNow();
			DBCnx.closeAll(resultLic, stmt);
		}
		Utils.printRetroLic(cant_lic + " licitaciones revisadas.");
	}
}
