package cl.techk.ordenes.carga;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;

import cl.techk.ext.database.DBCnx;
import cl.techk.ext.database.DataManager;
import cl.techk.ext.utils.InitLog;
import cl.techk.ext.utils.Utils;

/**
 * Servlet implementation class CargaDatos2
 */
@WebServlet("/CargaDatos2")
public class CargaDatos extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static ExecutorService ex = null;
	private static String bdd_name = "";

	public CargaDatos() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String database_to_use = "";
		
		if (request.getParameter("estado") != null) {
		    
		    database_to_use = request.getParameter("estado");
			if (database_to_use.equals("stage")) {
				bdd_name = "stage_point_ordenes";
				Utils.printOrdDeb("stage");
			} else if (database_to_use.equals("prod")) {
				bdd_name = "point_ordenes";
				Utils.printOrdDeb("prod");
			}
			Utils.printOrdDeb(bdd_name);
			LoadMaestros.LoadMaestrosProveedorAux(bdd_name);
			LoadMaestros.LoadMaestrosCompradorGeneralAux(bdd_name);
			LoadMaestros.LoadMaestrosCompradorMercadoAux(bdd_name);
			LoadMaestros.LoadMaestrosProductosAux(bdd_name);

			AgregaMaestros();
		}
	}

	private void AgregaMaestros() {
		ExecutorService ex = null;
		try {
			ex = Executors.newFixedThreadPool(6);

			ex.execute(new CargaThread(1));
			ex.execute(new CargaThread(2));
			ex.execute(new CargaThread(3));
			ex.execute(new CargaThread(4));
			ex.shutdown();

			while (!ex.isTerminated()) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Utils.printOrdErr("Error Cargando MAestros: " + e.getMessage());
				}
			}
		} catch (Exception e) {
			Utils.printOrdErr("Error al cargar datos");
			Utils.printOrdErr(e.getMessage());
		} finally {
			ex.shutdownNow();
		}
	}

	public static void Agregar_prov_nuevos() {

		Iterator<?> iterador_prov = InitLog.MaestrosProvAux.entrySet().iterator();
		while (iterador_prov.hasNext()) {
			int id;

			Map.Entry<?,?> maestro_prov = (Map.Entry<?,?>) iterador_prov.next();
			Maestro_provAux dato = InitLog.MaestrosProvAux.get(maestro_prov.getKey());
			
			//reviso si es una actualizacion de maestro. Si no lo es entonces reviso si ya existe, sino, lo agrego.
			id = dato.getIdMaestro();
			if (id == 0) {

				Connection cnx = null;
				PreparedStatement stmt = null;
				ResultSet result = null;
				try {
					//Busco si el maestro ya existe en la tabla del mantenedor.
					String select = "SELECT id FROM " + bdd_name + ".maestros_proveedores WHERE rutProveedor = ? AND "
							+ "razonSocialProveedor = ? AND proveedorSimplificado = ? AND proveedorAsociado = ?";

					cnx = DBCnx.conexion();

					stmt = cnx.prepareStatement(select, Statement.RETURN_GENERATED_KEYS);
					stmt.setString(1, dato.getRutProveedor());
					stmt.setString(2, dato.getRazonSocial());
					stmt.setString(3, dato.getProveedorSimplificado());
					stmt.setString(4, dato.getProveedorAsociado());
					result = stmt.executeQuery();
					
					int id_ = 0;
					
					if (result.next()) {
						id_ = result.getInt("id");
					}
					DBCnx.closeAll(result, stmt, cnx);
					//Si el maestro no existe en la tabla del mantenedor entonces lo ingreso y genero los filtros
					if (id_ == 0) {
						PreparedStatement stmt2 = null;
						Connection cnx2 = null;
						String query = "INSERT INTO " + bdd_name
								+ ".maestros_proveedores (rutProveedor, razonSocialProveedor,"
								+ "proveedorSimplificado, proveedorAsociado) VALUES (?,?,?,?)";

						cnx2 = DBCnx.conexion();
						stmt2 = cnx2.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
						stmt2.setString(1, dato.getRutProveedor());
						stmt2.setString(2, dato.getRazonSocial());
						stmt2.setString(3, dato.getProveedorSimplificado());
						stmt2.setString(4, dato.getProveedorAsociado());						
						stmt2.executeUpdate();

						Maestro_prov maestro = new Maestro_prov(dato.getRazonSocial(), dato.getProveedorSimplificado(),
								dato.getProveedorAsociado());

						InitLog.MaestrosProv.put(maestro_prov.getKey().toString(), maestro);
						DBCnx.close(stmt2);

						// Elimino el maestro agregado de la tabla auxiliar.
						String query_delete = "DELETE FROM " + bdd_name
								+ ".maestros_proveedores_aux WHERE rutProveedor = ?" + " AND proveedorAsociado = ?";
						stmt2 = cnx2.prepareStatement(query_delete);
						stmt2.setString(1, dato.getRutProveedor());
						stmt2.setString(2, dato.getProveedorAsociado());
						stmt2.execute();
						
						DBCnx.close(stmt2);
						DBCnx.close(cnx2);

						ActualizarFiltrosProveedor(dato.getRutProveedor(), maestro);

					}
				} catch (JSONException | SQLException e) {
					if (e.getMessage().contains("Duplicate entry")) {
						Utils.printOrdErr(
								"Maestro \"" + maestro_prov.getKey().toString() + "\" Duplicado no se ingresará");
					} else {
						e.printStackTrace();
						Utils.printOrdErr("E:"+e.getMessage());
					}
				} finally {
					DBCnx.closeAll(result, stmt, cnx);
				}
			}
		}
	}

	public static void ActualizarFiltrosProveedor(String rut, Maestro_prov dato) {
		// Insert tabla filtro_rutProveedor
		String insert = "INSERT INTO " + bdd_name + ".filtro_rutproveedor (rutProveedor) VALUES (\"" + rut + "\")";
		Utils.printOrdDeb("Insertando en filtro rut proveedor");
		DataManager.insertData2(insert, "ord");

		insert = "INSERT INTO " + bdd_name + ".filtro_proveedor_asociado (proveedor_asociado) VALUES (\""
				+ dato.getProveedorAsociado() + "\")";
		Utils.printOrdDeb("Insertando en filtro proveedor asociado");
		DataManager.insertData2(insert, "ord");

		insert = "INSERT INTO " + bdd_name + ".filtro_razonsocialproveedor (razonSocialProveedor) VALUES (\""
				+ dato.getRazonSocial() + "\")";
		Utils.printOrdDeb("Insertando en filtro razon social proveedor");
		DataManager.insertData2(insert, "ord");

	}

	public static void Agregar_comp_nuevos() {

		Iterator<?> iterador_comp = InitLog.MaestrosCompGAux.entrySet().iterator();
		while (iterador_comp.hasNext()) {
			int id;

			Map.Entry<?,?> maestro_compG = (Map.Entry<?,?>) iterador_comp.next();
			Maestro_compGAux dato = InitLog.MaestrosCompGAux.get(maestro_compG.getKey());
			id = dato.getIdMaestro();

			if (id == 0) {
				Utils.printOrdDeb("Insertando Maestro Comprador Gnral: " + maestro_compG.getKey());

				Connection cnx = null;
				PreparedStatement stmt = null;
				try {
					String query = "INSERT INTO " + bdd_name
							+ ".maestros_compradores_gnral (rutComprador, razonSocialComprador,"
							+ "unidadCompra, compradorReducido, comuna, region, segmentoComprador) VALUES (?,?,?,?,?,?,?)";
					cnx = DBCnx.conexion();

					stmt = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
					stmt.setString(1, dato.getRutComprador());
					stmt.setString(2, dato.getRazonCompradorGnral());
					stmt.setString(3, dato.getUnidadCompra());
					stmt.setString(4, dato.getCompradorReducido());
					stmt.setString(5, dato.getComuna());
					stmt.setString(6, dato.getRegion());
					stmt.setString(7, dato.getSegmentoComprador());
					stmt.executeUpdate();

					// Agrego el maestro al Hashmap de maestros actuales.
					Maestro_compG maestro = new Maestro_compG(dato.getRutComprador(),dato.getRazonCompradorGnral(), dato.getUnidadCompra(),
							dato.getCompradorReducido(), dato.getComuna(), dato.getRegion(),
							dato.getSegmentoComprador());

					InitLog.MaestrosCompG.put(maestro_compG.getKey().toString(), maestro);
					DBCnx.close(stmt);

					// Elimino el maestro agregado de la tabla auxiliar.
					String query_delete = "DELETE FROM " + bdd_name + ".maestros_compradores_gnral_aux "
							+ "WHERE rutComprador = ? AND unidadCompra = ?";

					stmt = cnx.prepareStatement(query_delete);
					stmt.setString(1, dato.getRutComprador());
					stmt.setString(2, dato.getUnidadCompra());
					stmt.execute();
					
					DBCnx.close(stmt);
					DBCnx.close(cnx);
					ActualizarFiltrosCompradorGnral(dato.rutComprador, maestro);
				} catch (JSONException | SQLException e) {
					if (e.getMessage().contains("Duplicate entry")) {
						Utils.printOrdErr(
								"Maestro " + maestro_compG.getKey().toString() + " Duplicado no se ingresará");
					} else {
						Utils.printOrdErr(e.getMessage());
					}
				} finally {
					DBCnx.close(stmt);
					DBCnx.close(cnx);
				}
			}
		}
	}

	public static void Agregar_comp_mercado_nuevo() {
		Iterator<?> iterador_comp = InitLog.MaestrosCompMAux.entrySet().iterator();
		while (iterador_comp.hasNext()) {
			int id_maestro;

			Map.Entry<?,?> maestro_compM = (Map.Entry<?,?>) iterador_comp.next();

			Maestro_compMAux dato = InitLog.MaestrosCompMAux.get(maestro_compM.getKey());
			id_maestro = dato.getIdMaestro();

			if (id_maestro == 0) {
				Utils.printOrdDeb("Insertando Maestro Comprador Mercado: " + maestro_compM.getKey());

				Connection cnx = null;
				PreparedStatement stmt = null;
				PreparedStatement stmt2 = null;
				ResultSet result = null;
				try {
					cnx = DBCnx.conexion();

					String select = "SELECT id FROM " + bdd_name
							+ ".maestros_compradores_mercado WHERE rutComprador = ? AND mercado = ? AND segmentoCompradorMercado = ?";
					stmt = cnx.prepareStatement(select);
					stmt.setString(1, dato.getRutComprador());
					stmt.setString(2, dato.getMercado());
					stmt.setString(3, dato.getSegmentoComprador());
					result = stmt.executeQuery();
					if (!result.next()) {
						Utils.printOrdDeb("Insertando maestro mercado");
						String query = "INSERT INTO " + bdd_name
								+ ".maestros_compradores_mercado (rutComprador, mercado,"
								+ " segmentoCompradorMercado) VALUES (?,?,?)";

						stmt2 = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
						stmt2.setString(1, dato.getRutComprador());
						stmt2.setString(2, dato.getMercado());
						stmt2.setString(3, dato.getSegmentoComprador());
						stmt2.executeUpdate();

						DBCnx.close(stmt2);
						DBCnx.close(result);

						query = "SELECT id FROM " + bdd_name
								+ ".maestros_compradores_mercado WHERE rutComprador = ? AND mercado = ? AND segmentoCompradorMercado = ?";
						stmt2 = cnx.prepareStatement(query);
						stmt2.setString(1, dato.getRutComprador());
						stmt2.setString(2, dato.getMercado());
						stmt2.setString(3, dato.getSegmentoComprador());
						result = stmt2.executeQuery();

						int id = 0;
						if (result.next()) {
						    
							id = result.getInt("id");
							Maestro_compM maestro = new Maestro_compM(dato.getRutComprador(),
									dato.getMercado(), dato.getSegmentoComprador());

							InitLog.MaestrosCompM.put(id, maestro);
							DBCnx.close(stmt2);

							Utils.printOrdDeb("Eliminando maestro mercado auxiliar");
							String query_delete = "DELETE FROM " + bdd_name + ".maestros_compradores_mercado_aux "
									+ "WHERE id = ?";

							stmt2 = cnx.prepareStatement(query_delete);
							stmt2.setInt(1, dato.getId());
							stmt2.execute();
							DBCnx.close(stmt2);
							ActualizarFiltrosCompradorMercado(maestro_compM.getKey().toString(), maestro);
						}
					}
				} catch (JSONException | SQLException e) {
					if (e.getMessage().contains("Duplicate entry")) {
						Utils.printOrdErr(
								"Maestro \"" + maestro_compM.getKey().toString() + "\" Duplicado no se ingresará");
					} else {
						Utils.printOrdErr(e.getMessage());
					}
				} finally {
					DBCnx.closeAll(result, stmt, cnx);
				}
			}
		}
	}

	public static void Agregar_jerarquias_prod() {
		Iterator<?> iterador_prod = InitLog.MaestrosProdAux.entrySet().iterator();
		while (iterador_prod.hasNext()) {
			int id_maestro;

			Map.Entry<?,?> maestro_prod = (Map.Entry<?,?>) iterador_prod.next();
			Maestro_prodAux dato = InitLog.MaestrosProdAux.get(maestro_prod.getKey());
			id_maestro = dato.getIdMaestro();

			if (id_maestro == 0) {
				Utils.printOrdDeb("Insertando Maestro Jerarquía Producto: " + maestro_prod.getKey());

				Connection cnx = null;
				PreparedStatement stmt = null;
				ResultSet result = null;
				try {
					cnx = DBCnx.conexion();

					String select = "SELECT id FROM " + bdd_name + ".maestros_jerarquias_productos WHERE"
							+ " mercado = \"" + dato.getMercado() + "\" AND categoria = \"" + dato.getCategoria()
							+ "\" ";

					if (!dato.getSubcat1().isEmpty() || !dato.getSubcat1().equals("")) {
						select += " AND Subcategoria1 = \"" + dato.getSubcat1() + "\"";
					}
					if (!dato.getSubcat2().isEmpty() || !dato.getSubcat2().equals("")) {
						select += " AND Subcategoria2 = \"" + dato.getSubcat2() + "\"";
					}
					if (!dato.getSubcat3().isEmpty() || !dato.getSubcat3().equals("")) {
						select += " AND Subcategoria3 = \"" + dato.getSubcat3() + "\"";
					}
					if (!dato.getSubcat4().isEmpty() || !dato.getSubcat4().equals("")) {
						select += " AND Subcategoria4 = \"" + dato.getSubcat4() + "\"";
					}
					if (!dato.getSubcat5().isEmpty() || !dato.getSubcat5().equals("")) {
						select += " AND Subcategoria5 = \"" + dato.getSubcat5() + "\"";
					}
					if (!dato.getSubcat6().isEmpty() || !dato.getSubcat6().equals("")) {
						select += " AND Subcategoria6 = \"" + dato.getSubcat6() + "\"";
					}
					if (!dato.getSubcat7().isEmpty() || !dato.getSubcat7().equals("")) {
						select += " AND Subcategoria7 = \"" + dato.getSubcat7() + "\"";
					}
					if (!dato.getSubcat8().isEmpty() || !dato.getSubcat8().equals("")) {
						select += " AND Subcategoria8 = \"" + dato.getSubcat8() + "\"";
					}
					Utils.printOrdDeb("Buscando Jerarquía de producto");
					stmt = cnx.prepareStatement(select);
					result = stmt.executeQuery();

					if (!result.next()) {
						DBCnx.close(result);
						DBCnx.close(stmt);
						String insert = "INSERT INTO " + bdd_name
								+ ".maestros_jerarquias_productos (mercado, categoria, "
								+ "Subcategoria1, Subcategoria2, Subcategoria3, Subcategoria4, Subcategoria5, Subcategoria6, "
								+ "Subcategoria7, Subcategoria8) VALUES (?,?,?,?,?,?,?,?,?,?)";

						stmt = cnx.prepareStatement(insert);
						stmt.setString(1, dato.getMercado());
						stmt.setString(2, dato.getCategoria());
						if (!dato.getSubcat1().isEmpty() || !dato.getSubcat1().equals("")) {
							stmt.setString(3, dato.getSubcat1());
						} else {
							stmt.setNull(3, java.sql.Types.VARCHAR);
						}
						if (!dato.getSubcat2().isEmpty() || !dato.getSubcat2().equals("")) {
							stmt.setString(4, dato.getSubcat2());
						} else {
							stmt.setNull(4, java.sql.Types.VARCHAR);
						}
						if (!dato.getSubcat3().isEmpty() || !dato.getSubcat3().equals("")) {
							stmt.setString(5, dato.getSubcat3());
						} else {
							stmt.setNull(5, java.sql.Types.VARCHAR);
						}
						if (!dato.getSubcat4().isEmpty() || !dato.getSubcat4().equals("")) {
							stmt.setString(6, dato.getSubcat4());
						} else {
							stmt.setNull(6, java.sql.Types.VARCHAR);
						}
						if (!dato.getSubcat5().isEmpty() || !dato.getSubcat5().equals("")) {
							stmt.setString(7, dato.getSubcat5());
						} else {
							stmt.setNull(7, java.sql.Types.VARCHAR);
						}
						if (!dato.getSubcat6().isEmpty() || !dato.getSubcat6().equals("")) {
							stmt.setString(8, dato.getSubcat6());
						} else {
							stmt.setNull(8, java.sql.Types.VARCHAR);
						}
						if (!dato.getSubcat7().isEmpty() || !dato.getSubcat7().equals("")) {
							stmt.setString(9, dato.getSubcat7());
						} else {
							stmt.setNull(9, java.sql.Types.VARCHAR);
						}
						if (!dato.getSubcat8().isEmpty() || !dato.getSubcat8().equals("")) {
							stmt.setString(10, dato.getSubcat8());
						} else {
							stmt.setNull(10, java.sql.Types.VARCHAR);
						}
						Utils.printOrdDeb("Insertando Jerarquia");
						stmt.executeUpdate();

						int id = getIdProductCombination(dato);
						if (id > 0) {
							Maestro_prod maestro = new Maestro_prod(dato.getMercado(), dato.getCategoria(),
									dato.getSubcat1(), dato.getSubcat2(), dato.getSubcat3(), dato.getSubcat4(),
									dato.getSubcat5(), dato.getSubcat6(), dato.getSubcat7(), dato.getSubcat8());

							InitLog.MaestrosProd.put(id, maestro);
							DBCnx.close(stmt);

							Utils.printOrdDeb("Eliminando jararquia auxiliar");
							String query_delete = "DELETE FROM " + bdd_name + ".maestros_jerarquias_productos_aux "
									+ "WHERE id = ?";

							stmt = cnx.prepareStatement(query_delete);
							stmt.setInt(1, dato.getId());
							stmt.execute();

							ActualizarFiltrosProductos(maestro);
						}
					}
				} catch (JSONException | SQLException e) {
					if (e.getMessage().contains("Duplicate entry")) {
						Utils.printOrdErr(
								"Maestro \"" + maestro_prod.getKey().toString() + "\" Duplicado no se ingresará");
					} else {
						e.printStackTrace();
					}
				} finally {
					DBCnx.closeAll(result, stmt, cnx);
				}
			}
		}
	}

	public static void ActualizarFiltrosProductos(Maestro_prod dato) {
		try {

			String insert = "INSERT INTO " + bdd_name + ".filtro_categoria (categoria) " + "VALUES (\""
					+ dato.getCategoria() + "\")";
			Utils.printOrdDeb("Insertando categoria");
			DataManager.insertData2(insert, "ord");

			if (!dato.getSubcat1().isEmpty() || !dato.getSubcat1().equals("")) {
				insert = "INSERT INTO " + bdd_name + ".filtro_subcategoria1 (subCategoria) VALUES " + "(\""
						+ dato.getSubcat1() + "\")";
				Utils.printOrdDeb("Insertando sub categoria1");
				DataManager.insertData2(insert, "ord");
			}

			if (!dato.getSubcat2().isEmpty() || !dato.getSubcat2().equals("")) {

				insert = "INSERT INTO " + bdd_name + ".filtro_subcategoria2 (subCategoria) VALUES " + "(\""
						+ dato.getSubcat2() + "\")";
				Utils.printOrdDeb("Insertando sub categoria2");
				DataManager.insertData2(insert, "ord");
			}

			if (!dato.getSubcat3().isEmpty() || !dato.getSubcat3().equals("")) {
				insert = "INSERT INTO " + bdd_name + ".filtro_subcategoria3 (subCategoria) VALUES " + "(\""
						+ dato.getSubcat3() + "\")";
				Utils.printOrdDeb("Insertando sub categoria3");
				DataManager.insertData2(insert, "ord");
			}

			if (!dato.getSubcat4().isEmpty() || !dato.getSubcat4().equals("")) {
				insert = "INSERT INTO " + bdd_name + ".filtro_subcategoria4 (subCategoria) VALUES " + "(\""
						+ dato.getSubcat4() + "\")";
				Utils.printOrdDeb("Insertando sub categoria4");
				DataManager.insertData2(insert, "ord");
			}

			if (!dato.getSubcat5().isEmpty() || !dato.getSubcat5().equals("")) {
				insert = "INSERT INTO " + bdd_name + ".filtro_subcategoria5 (subCategoria) VALUES " + "(\""
						+ dato.getSubcat5() + "\")";
				Utils.printOrdDeb("Insertando sub categoria5");
				DataManager.insertData2(insert, "ord");
			}

			if (!dato.getSubcat6().isEmpty() || !dato.getSubcat6().equals("")) {
				insert = "INSERT INTO " + bdd_name + ".filtro_subcategoria6 (subCategoria) VALUES " + "(\""
						+ dato.getSubcat6() + "\")";
				Utils.printOrdDeb("Insertando sub categoria6");
				DataManager.insertData2(insert, "ord");
			}

			if (!dato.getSubcat7().isEmpty() || !dato.getSubcat7().equals("")) {
				insert = "INSERT INTO " + bdd_name + ".filtro_subcategoria7 (subCategoria) VALUES " + "(\""
						+ dato.getSubcat7() + "\")";
				Utils.printOrdDeb("Insertando sub categoria7");
				DataManager.insertData2(insert, "ord");
			}

			if (!dato.getSubcat8().isEmpty() || !dato.getSubcat8().equals("")) {
				insert = "INSERT INTO " + bdd_name + ".filtro_subcategoria8 (subCategoria) VALUES " + "(\""
						+ dato.getSubcat8() + "\")";
				Utils.printOrdDeb("Insertando sub categoria8");
				DataManager.insertData2(insert, "ord");
			}
		} catch (JSONException e) {
			Utils.printOrdErr(e.getMessage());
		}
	}

	public static void ActualizarFiltrosCompradorMercado(String rut, Maestro_compM dato) {
		Connection cnx = null;
		PreparedStatement stmt = null;
		ResultSet result = null;

		try {
			cnx = DBCnx.conexion();

			String select = "SELECT id FROM " + bdd_name + ".filtro_mercado WHERE mercado = ?";
			stmt = cnx.prepareStatement(select);
			stmt.setString(1, dato.getMercado());
			result = stmt.executeQuery();

			if (result.next()) {

				String insert = "INSERT INTO " + bdd_name + ".filtro_segmentocompradormercado (segmento_mercado) "
						+ "VALUES (\"" + dato.getSegmentoComprador() + "\")";
				Utils.printOrdDeb("Insertando en filtro segmento comprador mercado");
				DataManager.insertData2(insert, "ord");
			} else {
				String insert = "INSERT INTO " + bdd_name + ".filtro_mercado (mercado) VALUES (\"" + dato.getMercado()
						+ "\")";
				Utils.printOrdDeb("Ojo, Mercado " + dato.getMercado() + " no existe. Se agregó mercado");
				DataManager.insertData2(insert, "ord");

				insert = "INSERT INTO " + bdd_name + ".filtro_segmentocompradormercado (segmento_mercado) "
						+ "VALUES (\"" + dato.getSegmentoComprador() + "\")";
				Utils.printOrdDeb("Insertando en filtro segmento comprador mercado");
				DataManager.insertData2(insert, "ord");
			}
		} catch (JSONException | SQLException e) {
			Utils.printOrdDeb(e.getMessage());
		} finally {
			DBCnx.closeAll(result, stmt, cnx);
		}
	}

	public static void ActualizarFiltrosCompradorGnral(String rut, Maestro_compG dato) {

		String insert = "INSERT INTO " + bdd_name + ".filtro_rutcomprador (rutComprador) VALUES (\"" + dato.getRutComprador() + "\")";
		Utils.printOrdDeb("Insertando en filtro rut comprador");
		DataManager.insertData2(insert, "ord");

		insert = "INSERT INTO " + bdd_name + ".filtro_razonsocialcomprador (razonSocialComprador) " + "VALUES (\""
				+ dato.getRazonCompradorGnral() + "\")";
		Utils.printOrdDeb("Insertando en filtro razon social comprador");
		DataManager.insertData2(insert, "ord");

		insert = "INSERT INTO " + bdd_name + ".filtro_segmentocomprador (segmento) VALUES (\""
				+ dato.getSegmentoComprador() + "\")";
		Utils.printOrdDeb("Insertando en filtro segmento comprador");
		DataManager.insertData2(insert, "ord");

		insert = "INSERT INTO " + bdd_name + ".filtro_unidad_compra (unidad_compra) VALUES (\"" + dato.getUnidadCompra()
				+ "\")";
		Utils.printOrdDeb("Insertando en filtro unidad de compra");
		DataManager.insertData2(insert, "ord");

		ValidarComuna(dato.getComuna());

	}

	public static void ValidarComuna(String comuna) {

		Connection cnx = null;
		PreparedStatement stmt = null;
		try {
			cnx = DBCnx.conexion();
			String insert = "INSERT INTO " + bdd_name + ".filtro_comunas (comuna) VALUES (\"" + comuna + "\");";
			Utils.printOrdDeb("Insertando en filtro comuna");
			stmt = cnx.prepareStatement(insert);
			stmt.executeUpdate();
		} catch (SQLException e) {
			Utils.printOrdErr(e.getMessage());
		} finally {
			DBCnx.close(cnx);
			DBCnx.close(stmt);
		}
	}

	public static int getIdProductCombination(Maestro_prodAux dato) {
		Connection cnx = null;
		PreparedStatement stmt = null;
		ResultSet result = null;

		int id = 0;
		try {
			cnx = DBCnx.conexion();

			String select = "SELECT id FROM " + bdd_name + ".maestros_jerarquias_productos WHERE" + " mercado = \""
					+ dato.getMercado() + "\" AND categoria = \"" + dato.getCategoria() + "\" ";

			if (!dato.getSubcat1().isEmpty() || !dato.getSubcat1().equals("")) {
				select += " AND Subcategoria1 = \"" + dato.getSubcat1() + "\"";
			}
			if (!dato.getSubcat2().isEmpty() || !dato.getSubcat2().equals("")) {
				select += " AND Subcategoria2 = \"" + dato.getSubcat2() + "\"";
			}
			if (!dato.getSubcat3().isEmpty() || !dato.getSubcat3().equals("")) {
				select += " AND Subcategoria3 = \"" + dato.getSubcat3() + "\"";
			}
			if (!dato.getSubcat4().isEmpty() || !dato.getSubcat4().equals("")) {
				select += " AND Subcategoria4 = \"" + dato.getSubcat4() + "\"";
			}
			if (!dato.getSubcat5().isEmpty() || !dato.getSubcat5().equals("")) {
				select += " AND Subcategoria5 = \"" + dato.getSubcat5() + "\"";
			}
			if (!dato.getSubcat6().isEmpty() || !dato.getSubcat6().equals("")) {
				select += " AND Subcategoria6 = \"" + dato.getSubcat6() + "\"";
			}
			if (!dato.getSubcat7().isEmpty() || !dato.getSubcat7().equals("")) {
				select += " AND Subcategoria7 = \"" + dato.getSubcat7() + "\"";
			}
			if (!dato.getSubcat8().isEmpty() || !dato.getSubcat8().equals("")) {
				select += " AND Subcategoria8 = \"" + dato.getSubcat8() + "\"";
			}
			Utils.printOrdDeb("Buscando Jerarquía de producto");
			stmt = cnx.prepareStatement(select);
			result = stmt.executeQuery();

			if (result.next()) {
				id = result.getInt("id");
			}
		} catch (JSONException | SQLException e) {
			Utils.printOrdErr(e.getMessage());
		} finally {
			DBCnx.closeAll(result, stmt, cnx);
		}

		return id;
	}
}