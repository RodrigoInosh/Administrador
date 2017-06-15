package cl.techk.ordenes.carga;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;

import cl.techk.ext.database.DBCnx;
import cl.techk.ext.database.DataManager;
import cl.techk.ext.utils.InitLog;
import cl.techk.ext.utils.Utils;

public class ActualizaMaestros {

	public static void Actualizar_proveedores() {

		Iterator<?> iterador_prov = InitLog.MaestrosProvAux.entrySet().iterator();
		while (iterador_prov.hasNext()) {
			int id;

			Map.Entry<?,?> maestro_prov = (Map.Entry<?,?>) iterador_prov.next();
			Maestro_provAux dato = InitLog.MaestrosProvAux.get(maestro_prov.getKey());
			id = dato.getIdMaestro();

			if (id > 0) {

				Connection cnx = null;
				PreparedStatement stmt = null;
				ResultSet result = null;
				try {
					cnx = DBCnx.conexion();

					String select = "SELECT * FROM stage_point_ordenes.maestros_proveedores WHERE id = ?";
					stmt = cnx.prepareStatement(select);
					stmt.setInt(1, dato.getIdMaestro());
					result = stmt.executeQuery();

					if (result.next()) {
						int id_maestro = result.getInt("id");
						String rutP = result.getString("rutProveedor");
						String razonSocial = result.getString("razonSocialProveedor");
						String provSimp = result.getString("proveedorSimplificado");
						String provAsoc = result.getString("proveedorAsociado");

						DBCnx.close(stmt);

						boolean cambios = false;
						String update = "UPDATE stage_point_ordenes.maestros_proveedores SET";

						if (!rutP.equals(maestro_prov.getKey().toString())) {
							update += " rutProveedor =\"" + maestro_prov.getKey().toString() + "\",";
							cambios = true;
						}

						if (!razonSocial.equals(dato.getRazonSocial())) {
							update += " razonSocialProveedor = \"" + dato.getRazonSocial() + "\",";
							cambios = true;
						}

						if (!provSimp.equals(dato.getProveedorSimplificado())) {
							update += " proveedorSimplificado = \"" + dato.getProveedorSimplificado() + "\",";
							cambios = true;
						}

						if (!provAsoc.equals(dato.getProveedorAsociado())) {
							update += " proveedorAsociado = \"" + dato.getProveedorAsociado() + "\",";
							cambios = true;
						}
						String query_update = update.substring(0, update.length() - 1);
						query_update += " WHERE id = " + id_maestro + "";

						Utils.printOrdDeb("Updating maestro proveedor");
						stmt = cnx.prepareStatement(query_update);
						if (cambios) {

							stmt.executeUpdate();
							//
							// Modifico la información del maestro actual
							Maestro_prov maestro = new Maestro_prov(dato.getRazonSocial(),
									dato.getProveedorSimplificado(), dato.getProveedorAsociado());

							String key_maestro = maestro_prov.getKey().toString();

							InitLog.MaestrosProv.remove(key_maestro);
							InitLog.MaestrosProv.put(key_maestro, maestro);
							// InitLog.MaestrosProdAux.remove(key_maestro);

							DBCnx.close(stmt);

							// Elimino el maestro agregado de la tabla auxiliar.
							String query_delete = "DELETE FROM stage_point_ordenes.maestros_proveedores_aux WHERE rutProveedor = ?";
							stmt = cnx.prepareStatement(query_delete);
							stmt.setString(1, key_maestro);
							stmt.execute();

							ActualizarFiltrosProv(rutP, razonSocial, provAsoc, provSimp, maestro, key_maestro);
						}
					}
				} catch (JSONException | SQLException e) {
					if (e.getMessage().contains("Duplicate entry")) {
						Utils.printOrdErr(
								"Maestro \"" + maestro_prov.getKey().toString() + "\" Duplicado no se ingresará");
					} else {
						Utils.printOrdErr(e.getMessage());
					}
				} finally {
					DBCnx.close(stmt);
					DBCnx.close(cnx);
					DBCnx.close(result);
				}
			}
		}
	}

	public static void Actualizar_compradores_gnral() {

		Iterator<?> iterador_compG = InitLog.MaestrosCompGAux.entrySet().iterator();
		while (iterador_compG.hasNext()) {
			int id;

			Map.Entry<?,?> maestro_comp_aux = (Map.Entry<?,?>) iterador_compG.next();
			Maestro_compGAux dato = InitLog.MaestrosCompGAux.get(maestro_comp_aux.getKey());
			id = dato.getIdMaestro();
			Utils.printOrdDeb("Actualizando Maestro Comprador");
			if (id > 0) {

				Connection cnx = null;
				PreparedStatement stmt = null;
				ResultSet result = null;
				try {
					cnx = DBCnx.conexion();

					String select = "SELECT * FROM stage_point_ordenes.maestros_compradores_gnral WHERE id = ?";
					stmt = cnx.prepareStatement(select);
					stmt.setInt(1, dato.getIdMaestro());
					result = stmt.executeQuery();

					if (result.next()) {
						int id_maestro = result.getInt("id");
						String rutC = result.getString("rutComprador");
						String razonSocial = result.getString("razonSocialComprador");
						String unidad = result.getString("unidadCompra");
						String compradorReducido = result.getString("compradorReducido");
						String comuna = result.getString("comuna");
						String region = result.getString("region");
						String segmento = result.getString("segmentoComprador");

						DBCnx.close(stmt);

						boolean cambios = false;
						String update = "UPDATE stage_point_ordenes.maestros_compradores_gnral SET";
//						if (!rutC.equals(maestro_comp_aux.getKey().toString())) {
						if (!rutC.equals(dato.getRutComprador())) {
							update += " rutComprador = \"" + maestro_comp_aux.getKey().toString() + "\",";
							cambios = true;
						}
						if (!razonSocial.equals(dato.getRazonCompradorGnral())) {
							update += " razonSocialComprador = \"" + dato.getRazonCompradorGnral() + "\",";
							cambios = true;
						}
						if (!unidad.equals(dato.getUnidadCompra())) {
							update += " unidadCompra = \"" + dato.getUnidadCompra() + "\",";
							cambios = true;
						}
						if (!compradorReducido.equals(dato.getCompradorReducido())) {
							update += " compradorReducido = \"" + dato.getCompradorReducido() + "\",";
							cambios = true;
						}
						if (!comuna.equals(dato.getComuna())) {
							update += " comuna = \"" + dato.getComuna() + "\",";
							cambios = true;
						}
						if (!region.equals(dato.getRegion())) {
							update += " region = \"" + dato.getRegion() + "\",";
							cambios = true;
						}
						if (!segmento.equals(dato.getSegmentoComprador())) {
							update += " segmentoComprador = \"" + dato.getSegmentoComprador() + "\",";
							cambios = true;
						}
						String query_update = update.substring(0, update.length() - 1);
						query_update += " WHERE id = " + id_maestro + "";

						Utils.printOrdDeb("Updating maestro comprador gnral");
						stmt = cnx.prepareStatement(query_update);
						if (cambios) {
							stmt.executeUpdate();

							// Modifico la información del maestro actual
							Maestro_compG maestro = new Maestro_compG(dato.getRutComprador(), dato.getRazonCompradorGnral(),
									dato.getUnidadCompra(), dato.getCompradorReducido(), dato.getComuna(),
									dato.getRegion(), dato.getSegmentoComprador());

							Utils.printOrdDeb("Actualizando Hashmaps de maestros");
							String key_maestro = maestro_comp_aux.getKey().toString();
							InitLog.MaestrosCompG.remove(key_maestro);
							InitLog.MaestrosCompG.put(key_maestro, maestro);
							// InitLog.MaestrosCompGAux.remove(key_maestro);

							DBCnx.close(stmt);

							// Elimino el maestro agregado de la tabla auxiliar.
							String query_delete = "DELETE FROM stage_point_ordenes.maestros_compradores_gnral_aux WHERE"
									+ " rutComprador = ? AND id_maestro_gnral = ?";
							stmt = cnx.prepareStatement(query_delete);
//							stmt.setString(1, key_maestro);
							stmt.setString(1, dato.getRutComprador());
							stmt.setInt(2, dato.getIdMaestro());
							Utils.printOrdDeb("Eliminando comprador auxiliar");
							stmt.execute();

							ActualizarFiltrosCompG(rutC, razonSocial, unidad, compradorReducido, comuna, region,
									segmento, maestro, key_maestro);
						}
					}
				} catch (JSONException | SQLException e) {
					Utils.printOrdErr(e.getMessage());

				} finally {
					DBCnx.close(stmt);
					DBCnx.close(cnx);
					DBCnx.close(result);
				}
			}
		}
	}

	public static void Actualizar_compradores_mercado() {

		Iterator iterador_compM = InitLog.MaestrosCompMAux.entrySet().iterator();
		while (iterador_compM.hasNext()) {
			int id;

			Map.Entry maestro_comp_aux = (Map.Entry) iterador_compM.next();
			Maestro_compMAux dato = InitLog.MaestrosCompMAux.get(maestro_comp_aux.getKey());
			id = dato.getIdMaestro();
			// Utils.printOrdDeb("Actualizando Maestro Comprador Mercado");
			if (id > 0) {

				Connection cnx = null;
				PreparedStatement stmt = null;
				ResultSet result = null;
				try {
					cnx = DBCnx.conexion();

					String select = "SELECT id, rutComprador, mercado, segmentoCompradorMercado FROM "
							+ "stage_point_ordenes.maestros_compradores_mercado WHERE id = ?";
					stmt = cnx.prepareStatement(select);
					stmt.setInt(1, dato.getIdMaestro());
					result = stmt.executeQuery();

					if (result.next()) {
						int id_maestro = result.getInt("id");
						String rutC = result.getString("rutComprador");
						String mercado = result.getString("mercado");
						String segmento = result.getString("segmentoCompradorMercado");

						DBCnx.close(stmt);

						boolean cambios = false;
						String update = "UPDATE stage_point_ordenes.maestros_compradores_gnral SET";
						if (!rutC.equals(maestro_comp_aux.getKey().toString())) {
							update += " rutComprador = \"" + maestro_comp_aux.getKey().toString() + "\",";
							cambios = true;
						}
						if (!mercado.equals(dato.getMercado())) {
							update += " razonSocialComprador = \"" + dato.getMercado() + "\",";
							cambios = true;
						}
						if (!segmento.equals(dato.getSegmentoComprador())) {
							update += " segmentoComprador = \"" + dato.getSegmentoComprador() + "\",";
							cambios = true;
						}
						String query_update = update.substring(0, update.length() - 1);
						query_update += " WHERE id = " + id_maestro + "";

						// Utils.printOrdDeb("Updating maestro comprador
						// mercado");
						stmt = cnx.prepareStatement(query_update);
						if (cambios) {
							stmt.executeUpdate();

							// Modifico la información del maestro actual
							Maestro_compM maestro = new Maestro_compM(maestro_comp_aux.getKey().toString(),
									dato.getMercado(), dato.getSegmentoComprador());

							// Utils.printOrdDeb("Actualizando Hashmaps de
							// maestros");
							String key_maestro = maestro_comp_aux.getKey().toString();
							InitLog.MaestrosCompM.remove(id_maestro);
							InitLog.MaestrosCompM.put(id_maestro, maestro);
							// InitLog.MaestrosCompMAux.remove(key_maestro);

							DBCnx.close(stmt);

							// Elimino el maestro agregado de la tabla auxiliar.
							String query_delete = "DELETE FROM stage_point_ordenes.maestros_compradores_mercado_aux WHERE"
									+ " rutComprador = ? AND id_maestro_gnral = ?";
							stmt = cnx.prepareStatement(query_delete);
							stmt.setString(1, key_maestro);
							stmt.setInt(2, dato.getIdMaestro());
							// Utils.printOrdDeb("Eliminando comprador mercado
							// auxiliar");
							stmt.execute();

							ActualizarFiltrosCompM(rutC, mercado, segmento, maestro, key_maestro);
						}
					}
				} catch (JSONException | SQLException e) {
					Utils.printOrdErr(e.getMessage());

				} finally {
					DBCnx.close(stmt);
					DBCnx.close(cnx);
					DBCnx.close(result);
				}
			}
		}
	}

	public static void Actualizar_jerarquia_productos() {

		Iterator iterador_prod = InitLog.MaestrosProdAux.entrySet().iterator();
		while (iterador_prod.hasNext()) {
			int id;

			Map.Entry maestro_prod = (Map.Entry) iterador_prod.next();
			Maestro_prodAux dato = InitLog.MaestrosProdAux.get(maestro_prod.getKey());
			id = dato.getIdMaestro();

			if (id > 0) {

				Connection cnx = null;
				PreparedStatement stmt = null;
				ResultSet result = null;
				try {
					cnx = DBCnx.conexion();

					String select = "SELECT * FROM stage_point_ordenes.maestros_jerarquias_productos WHERE id = ?";
					stmt = cnx.prepareStatement(select);
					stmt.setInt(1, dato.getIdMaestro());
					result = stmt.executeQuery();

					if (result.next()) {
						int id_maestro = result.getInt("id");
						String mercado = result.getString("mercado");
						String categoria = result.getString("categoria");
						String subcat1 = result.getString("Subcategoria1");
						String subcat2 = result.getString("Subcategoria2");
						String subcat3 = result.getString("Subcategoria3");
						String subcat4 = result.getString("Subcategoria4");
						String subcat5 = result.getString("Subcategoria5");
						String subcat6 = result.getString("Subcategoria6");
						String subcat7 = result.getString("Subcategoria7");
						String subcat8 = result.getString("Subcategoria8");

						DBCnx.close(stmt);

						boolean cambios = false;
						String update = "UPDATE stage_point_ordenes.maestros_jerarquias_productos SET";

						if (!mercado.equals(dato.getMercado())) {
							update += " mercado = \"" + dato.getMercado() + "\",";
							cambios = true;
						}

						if (!categoria.equals(dato.getCategoria())) {
							update += " categoria = \"" + dato.getCategoria() + "\",";
							cambios = true;
						}

						if (dato.getSubcat1() != null && !subcat1.equals(dato.getSubcat1())) {
							update += " Subcategoria1 = \"" + dato.getSubcat1() + "\",";
							cambios = true;
						}

						if (dato.getSubcat2() != null && !subcat2.equals(dato.getSubcat2())) {
							update += " Subcategoria2 = \"" + dato.getSubcat2() + "\",";
							cambios = true;
						}
						if (dato.getSubcat3() != null && !subcat3.equals(dato.getSubcat3())) {
							update += " Subcategoria3 = \"" + dato.getSubcat3() + "\",";
							cambios = true;
						}
						if (dato.getSubcat4() != null && !subcat4.equals(dato.getSubcat4())) {
							update += " Subcategoria4 = \"" + dato.getSubcat4() + "\",";
							cambios = true;
						}
						if (dato.getSubcat5() != null && !subcat5.equals(dato.getSubcat5())) {
							update += " Subcategoria5 = \"" + dato.getSubcat5() + "\",";
							cambios = true;
						}
						if (dato.getSubcat6() != null && !subcat6.equals(dato.getSubcat6())) {
							update += " Subcategoria6 = \"" + dato.getSubcat6() + "\",";
							cambios = true;
						}
						if (dato.getSubcat7() != null && !subcat7.equals(dato.getSubcat7())) {
							update += " Subcategoria7 = \"" + dato.getSubcat7() + "\"',";
							cambios = true;
						}
						if (dato.getSubcat8() != null && !subcat8.equals(dato.getSubcat8())) {
							update += " Subcategoria8 = \"" + dato.getSubcat8() + "\",";
							cambios = true;
						}
						String query_update = update.substring(0, update.length() - 1);
						query_update += " WHERE id = " + id_maestro + "";

						// Utils.printOrdDeb("Updating maestro jerarquias
						// productos");
						stmt = cnx.prepareStatement(query_update);
						if (cambios) {

							// Modifico la información del maestro actual
							Maestro_prod maestro = new Maestro_prod(dato.getMercado(), dato.getCategoria(),
									dato.getSubcat1(), dato.getSubcat2(), dato.getSubcat3(), dato.getSubcat4(),
									dato.getSubcat5(), dato.getSubcat6(), dato.getSubcat7(), dato.getSubcat8());

							String key_maestro = maestro_prod.getKey().toString();

							InitLog.MaestrosProd.remove(id_maestro);
							InitLog.MaestrosProd.put(id_maestro, maestro);
							// InitLog.MaestrosProdAux.remove(key_maestro);

							DBCnx.close(stmt);

							// Elimino el maestro agregado de la tabla auxiliar.
							String query_delete = "DELETE FROM stage_point_ordenes.maestros_jerarquias_productos_aux "
									+ "WHERE id = ?";
							stmt = cnx.prepareStatement(query_delete);
							stmt.setString(1, key_maestro);
							// stmt.execute();

							ActualizarFiltrosProd(mercado, categoria, subcat1, subcat2, subcat3, subcat4, subcat5,
									subcat6, subcat7, subcat8, maestro, key_maestro);
						}
					}
				} catch (JSONException | SQLException e) {
					Utils.printOrdDeb(e.getMessage());
				} finally {
					DBCnx.close(stmt);
					DBCnx.close(result);
					DBCnx.close(cnx);
				}
			}
		}
	}

	private static void ActualizarFiltrosProv(String rut, String razon, String pAsoc, String pSimp, Maestro_prov dato,
			String key) {
		Connection cnx = null;
		PreparedStatement stmt = null;
		Utils.printOrdDeb("Actualizando prov");
		try {
			cnx = DBCnx.conexion();

			int id = get_id("stage_point_ordenes.filtro_rutproveedor", "rutProveedor", rut);
			if (!rut.equals(key)) {
				if (id > 0) {
					String update = "UPDATE stage_point_ordenes.filtro_rutproveedor SET rutProveedor = ? WHERE id = ?";
					stmt = cnx.prepareStatement(update);
					stmt.setString(1, key);
					stmt.setInt(2, id);
					stmt.executeUpdate();
					// Utils.printOrdDeb("Actualizando filtro rut proveedor");
					DBCnx.close(stmt);
				}
			}
			if (!razon.equals(dato.getRazonSocial())) {
				int id_razon = get_id("stage_point_ordenes.filtro_razonsocialproveedor", "id_rut_proveedor", id,
						"razonSocialProveedor", razon);
				if (id_razon > 0) {
					String update = "UPDATE stage_point_ordenes.filtro_razonsocialproveedor SET razonSocialProveedor = ? "
							+ "WHERE id = ?";
					stmt = cnx.prepareStatement(update);
					stmt.setString(1, dato.getRazonSocial());
					stmt.setInt(2, id_razon);
					stmt.executeUpdate();
					Utils.printOrdDeb("Actualizando filtro razon social proveedor");
					DBCnx.close(stmt);
				}
			}
			if (!pSimp.equals(dato.getProveedorSimplificado())) {
				int id_pSimp = get_id("stage_point_ordenes.filtro_proveedor", "id_rut_proveedor", id, "nombre_proveedor",
						pSimp);
				if (id_pSimp > 0) {
					String update = "UPDATE stage_point_ordenes.filtro_proveedor SET nombre_proveedor = ? WHERE id = ?";
					stmt = cnx.prepareStatement(update);
					stmt.setString(1, dato.getProveedorSimplificado());
					stmt.setInt(2, id_pSimp);
					stmt.executeUpdate();
					// Utils.printOrdDeb("Actualizando filtro proveedor simp");
					DBCnx.close(stmt);
				}
			}
			if (!pAsoc.equals(dato.getProveedorAsociado())) {
				int id_pAsoc = get_id("stage_point_ordenes.filtro_proveedor_asociado", "id_rut_proveedor", id,
						"proveedor_asociado", pAsoc);
				if (id_pAsoc > 0) {
					String update = "UPDATE stage_point_ordenes.filtro_proveedor_asociado SET proveedor_asociado = ? WHERE"
							+ "id = ?";
					stmt = cnx.prepareStatement(update);
					stmt.setString(1, dato.getProveedorAsociado());
					stmt.setInt(2, id_pAsoc);
					stmt.executeUpdate();
					// Utils.printOrdDeb("Actualizando filtro proveedor
					// asociado");
					DBCnx.close(stmt);
				}
			}
		} catch (JSONException | SQLException e) {
			Utils.printOrdErr(e.getMessage());
		} finally {
			DBCnx.close(stmt);
			DBCnx.close(cnx);
		}
	}

	private static void ActualizarFiltrosCompG(String rut, String razon, String unidad, String compradorReducido,
			String comuna, String region, String segmento, Maestro_compG dato, String key) {
		Connection cnx = null;
		PreparedStatement stmt = null;
		// Utils.printOrdDeb("Iniciando actualizacion de filtros comprador");
		try {
			cnx = DBCnx.conexion();

			int id = get_id("stage_point_ordenes.filtro_rutcomprador", "rutComprador", rut);
			if (!rut.equals(key)) {
				String update = "UPDATE stage_point_ordenes.filtro_rutcomprador SET rutComprador = ? WHERE id = ?";
				stmt = cnx.prepareStatement(update);
				stmt.setString(1, key);
				stmt.setInt(2, id);
				stmt.executeUpdate();
				// Utils.printOrdDeb("Actualizando filtro rut comprador");
				DBCnx.close(stmt);
			}
			int id_razonComprador = get_id("stage_point_ordenes.filtro_razonsocialcomprador", "id_rut_comprador", id,
					"razonSocialComprador", razon);
			if (!razon.equals(dato.getRazonCompradorGnral())) {
				String update = "UPDATE stage_point_ordenes.filtro_razonsocialcomprador SET razonSocialComprador = ? "
						+ "WHERE id = ?";
				stmt = cnx.prepareStatement(update);
				stmt.setString(1, dato.getRazonCompradorGnral());
				stmt.setInt(2, id_razonComprador);
				stmt.executeUpdate();
				// Utils.printOrdDeb("Actualizando filtro razon comprador");
				DBCnx.close(stmt);
			}
			int id_unidadCompra = get_id("stage_point_ordenes.filtro_unidad_compra", "unidad_compra", unidad);
			if (!unidad.equals(dato.getUnidadCompra())) {
				String update = "UPDATE stage_point_ordenes.filtro_unidad_compra SET unidad_compra = ? WHERE id = ?";
				stmt = cnx.prepareStatement(update);
				stmt.setString(1, dato.getUnidadCompra());
				stmt.setInt(2, id_unidadCompra);
				stmt.executeUpdate();
				Utils.printOrdDeb("Actualizando filtro unidad compra");
				DBCnx.close(stmt);
			}

			int id_comuna = get_id("stage_point_ordenes.filtro_comunas", "id_filtro_region", Integer.parseInt(region),
					"comuna", comuna);
			if (!comuna.equals(dato.getComuna())) {
				String update = "UPDATE stage_point_ordenes.filtro_comunas SET comuna = ? WHERE id = ?";
				stmt = cnx.prepareStatement(update);
				stmt.setString(1, dato.getComuna());
				stmt.setInt(2, id_comuna);
				stmt.executeUpdate();

				Utils.printOrdDeb("Actualizando filtro comuna");
				DBCnx.close(stmt);
			}

			int id_segmento = get_id("stage_point_ordenes.filtro_segmentocomprador", "segmento", segmento);
			if (!segmento.equals(dato.getSegmentoComprador())) {
				String update = "UPDATE stage_point_ordenes.filtro_segmentocomprador SET segmento = ? WHERE id = ?";
				stmt = cnx.prepareStatement(update);
				stmt.setString(1, dato.getSegmentoComprador());
				stmt.setInt(2, id_segmento);
				stmt.executeUpdate();

				Utils.printOrdDeb("Actualizando filtro segmento comprador");
				DBCnx.close(stmt);
			}
		} catch (JSONException | SQLException e) {
			Utils.printOrdErr(e.getMessage());
		} finally {
			DBCnx.close(stmt);
			DBCnx.close(cnx);
		}
	}

	private static void ActualizarFiltrosCompM(String rut, String mercado, String segmento, Maestro_compM dato,
			String key) {
		Connection cnx = null;
		PreparedStatement stmt = null;
		ResultSet result = null;
		// Utils.printOrdDeb("Iniciando actualizacion de filtros comprador
		// mercado");
		try {
			cnx = DBCnx.conexion();

			int id_mercado = get_id("stage_point_ordenes.filtro_mercado", "mercado", dato.getMercado());
			if (mercado.equals(dato.getMercado()) && !segmento.equals(dato.getSegmentoComprador())) {
				String query = "SELECT id FROM stage_point_ordenes.filtro_segmentocompradormercado WHERE id_mercado = ? AND"
						+ "segmento_mercado = ?";
				stmt = cnx.prepareStatement(query);
				stmt.setInt(1, id_mercado);
				stmt.setString(2, dato.getSegmentoComprador());
				result = stmt.executeQuery(query);

				if (!result.next()) {
					DBCnx.close(stmt);

					String update = "UPDATE stage_point_ordenes.filtro_segmentocompradormercado SET segmento_mercado = ? "
							+ "WHERE id_mercado = ? AND segmento_mercado = ?";
					stmt = cnx.prepareStatement(update);
					stmt.setString(1, dato.getSegmentoComprador());
					stmt.setInt(2, id_mercado);
					stmt.setString(3, segmento);
					stmt.executeUpdate();

					Utils.printOrdDeb("Actualizando filtro segmento mercado");
					DBCnx.close(stmt);

				} else {
					Utils.printOrdErr("Segmento Comprador Mercado ya existente.");
				}
			}
		} catch (JSONException | SQLException e) {
			Utils.printOrdErr(e.getMessage());
		} finally {
			DBCnx.close(stmt);
			DBCnx.close(cnx);
			DBCnx.close(result);
		}
	}

	private static void ActualizarFiltrosProd(String mercado, String cat, String subcat1, String subcat2,
			String subcat3, String subcat4, String subcat5, String subcat6, String subcat7, String subcat8,
			Maestro_prod dato, String key) {

		Connection cnx = null;
		PreparedStatement stmt = null;
		// Utils.printOrdDeb("Iniciando actualizacion de filtros comprador
		// mercado");
		try {
			cnx = DBCnx.conexion();

			int id_mercado = get_id("stage_point_ordenes.filtro_mercado", "mercado", mercado);
			if (!mercado.equals(dato.getMercado())) {
				if (id_mercado > 0) {
					String update = "UPDATE stage_point_ordenes.filtro_mercado SET mercado = ? WHERE id = ?";
					stmt = cnx.prepareStatement(update);
					stmt.setString(1, dato.getMercado());
					stmt.setInt(2, id_mercado);
					stmt.executeUpdate();

					Utils.printOrdDeb("Actualizando filtro mercado");
					DBCnx.close(stmt);
				} else {
					String insert = "INSERT INTO stage_point_ordenes.filtro_mercado (mercado) VALUES " + "(\""
							+ dato.getMercado() + "\")";
					DataManager.insertData2(insert, "ord");
					id_mercado = get_id("stage_point_ordenes.filtro_mercado", "mercado", dato.getMercado());
					Utils.printOrdDeb("Ojo, mercado no existe. Se agregó el mercado");
				}
			}

			int id_cat = get_id("stage_point_ordenes.filtro_categoria", "id_mercado", id_mercado, "categoria", cat);

			if (!cat.equals(dato.getCategoria())) {
				if (id_cat > 0) {
					String update = "UPDATE stage_point_ordenes.filtro_categoria SET categoria = ? WHERE id = ?";
					stmt = cnx.prepareStatement(update);
					stmt.setString(1, dato.getCategoria());
					stmt.setInt(2, id_cat);
					stmt.executeUpdate();

					Utils.printOrdDeb("Actualizando filtro categoria");
					DBCnx.close(stmt);
				} else {
					String insert = "INSERT INTO stage_point_ordenes.filtro_categoria (id_mercado, categoria) " + "VALUES ("
							+ id_mercado + ",\"" + dato.getCategoria() + "\")";
					DataManager.insertData2(insert, "ord");
					id_cat = get_id("stage_point_ordenes.filtro_categoria", "id_mercado", id_mercado, "categoria",
							dato.getCategoria());
					Utils.printOrdDeb("Ojo, categoria no existe. Se agregó la categoría");
				}
			}

			int id_subcat1 = get_id("stage_point_ordenes.filtro_subcategoria1", "id_categoria", id_cat, "subCategoria",
					subcat1);

			if (dato.getSubcat1() != null && subcat1 != null && !subcat1.equals(dato.getSubcat1())) {
				if (id_subcat1 > 0) {
					String update = "UPDATE stage_point_ordenes.filtro_subcategoria1 SET subCategoria = ? WHERE id = ?";
					stmt = cnx.prepareStatement(update);
					stmt.setString(1, dato.getSubcat1());
					stmt.setInt(2, id_subcat1);
					stmt.executeUpdate();

					Utils.printOrdDeb("Actualizando filtro subCategoria1");
					DBCnx.close(stmt);
				} else {
					String insert = "INSERT INTO stage_point_ordenes.filtro_subcategoria1 (id_categoria, subCategoria) "
							+ "VALUES (" + id_cat + ",\"" + dato.getSubcat1() + "\")";
					DataManager.insertData2(insert, "ord");
					id_subcat1 = get_id("stage_point_ordenes.filtro_categoria", "id_categoria", id_cat, "subCategoria",
							dato.getSubcat1());
					Utils.printOrdDeb("Ojo, subCategoria1 no existe. Se agregó la subCategoria");
				}
			}

			int id_subcat2 = get_id("stage_point_ordenes.filtro_subcategoria2", "id_subcategoria1", id_subcat1,
					"subCategoria", subcat2);

			if (dato.getSubcat2() != null && subcat2 != null && !subcat2.equals(dato.getSubcat2())) {
				if (id_subcat2 > 0) {
					String update = "UPDATE stage_point_ordenes.filtro_subcategoria2 SET subCategoria = ? WHERE id = ?";
					stmt = cnx.prepareStatement(update);
					stmt.setString(1, dato.getSubcat2());
					stmt.setInt(2, id_subcat2);
					stmt.executeUpdate();

					Utils.printOrdDeb("Actualizando filtro subCategoria2");
					DBCnx.close(stmt);
				} else {
					String insert = "INSERT INTO stage_point_ordenes.filtro_subcategoria2 (id_subcategoria1, subCategoria) "
							+ "VALUES (" + id_subcat1 + ",\"" + dato.getSubcat2() + "\")";
					DataManager.insertData2(insert, "ord");
					id_subcat2 = get_id("stage_point_ordenes.filtro_subcategoria2", "id_subcategoria1", id_subcat1,
							"subCategoria", dato.getSubcat2());
					Utils.printOrdDeb("Ojo, subCategoria2 no existe. Se agregó la subCategoria2");
				}
			}

			int id_subcat3 = get_id("stage_point_ordenes.filtro_subcategoria3", "id_subcategoria2", id_subcat2,
					"subCategoria", subcat3);

			if (dato.getSubcat3() != null && subcat3 != null && !subcat3.equals(dato.getSubcat3())) {
				if (id_subcat3 > 0) {
					String update = "UPDATE stage_point_ordenes.filtro_subcategoria3 SET subCategoria = ? WHERE id = ?";
					stmt = cnx.prepareStatement(update);
					stmt.setString(1, dato.getSubcat3());
					stmt.setInt(2, id_subcat3);
					stmt.executeUpdate();

					Utils.printOrdDeb("Actualizando filtro subCategoria3");
					DBCnx.close(stmt);
				} else {
					String insert = "INSERT INTO stage_point_ordenes.filtro_subcategoria3 (id_subcategoria2, subCategoria) "
							+ "VALUES (" + id_subcat2 + ",\"" + dato.getSubcat3() + "\")";
					DataManager.insertData2(insert, "ord");
					id_subcat3 = get_id("stage_point_ordenes.filtro_subcategoria3", "id_subcategoria2", id_subcat2,
							"subCategoria", dato.getSubcat3());
					Utils.printOrdDeb("Ojo, subCategoria3 no existe. Se agregó la subCategoria3");
				}
			}

			int id_subcat4 = get_id("stage_point_ordenes.filtro_subcategoria4", "id_subcategoria3", id_subcat3,
					"subCategoria", subcat4);

			if (dato.getSubcat4() != null && subcat4 != null && !subcat4.equals(dato.getSubcat4())) {
				if (id_subcat4 > 0) {
					String update = "UPDATE stage_point_ordenes.filtro_subcategoria4 SET subCategoria = ? WHERE id = ?";
					stmt = cnx.prepareStatement(update);
					stmt.setString(1, dato.getSubcat4());
					stmt.setInt(2, id_subcat4);
					stmt.executeUpdate();

					// Utils.printOrdDeb("Actualizando filtro subCategoria4");
					DBCnx.close(stmt);
				} else {
					String insert = "INSERT INTO stage_point_ordenes.filtro_subcategoria4 (id_subcategoria3, subCategoria) "
							+ "VALUES (" + id_subcat3 + ",\"" + dato.getSubcat4() + "\")";
					DataManager.insertData2(insert, "ord");
					id_subcat4 = get_id("stage_point_ordenes.filtro_subcategoria4", "id_subcategoria3", id_subcat3,
							"subCategoria", dato.getSubcat4());
					Utils.printOrdDeb("Ojo, subCategoria4 no existe. Se agregó la subCategoria4");
				}
			}

			int id_subcat5 = get_id("stage_point_ordenes.filtro_subcategoria5", "id_subcategoria4", id_subcat4,
					"subCategoria", subcat5);

			if (dato.getSubcat5() != null && subcat5 != null && !subcat5.equals(dato.getSubcat5())) {
				if (id_subcat5 > 0) {
					String update = "UPDATE stage_point_ordenes.filtro_subcategoria5 SET subCategoria = ? WHERE id = ?";
					stmt = cnx.prepareStatement(update);
					stmt.setString(1, dato.getSubcat5());
					stmt.setInt(2, id_subcat5);
					stmt.executeUpdate();

					// Utils.printOrdDeb("Actualizando filtro subCategoria5");
					DBCnx.close(stmt);
				} else {
					String insert = "INSERT INTO stage_point_ordenes.filtro_subcategoria5 (id_subcategoria4, subCategoria) "
							+ "VALUES (" + id_subcat4 + ",\"" + dato.getSubcat5() + "\")";
					DataManager.insertData2(insert, "ord");
					id_subcat5 = get_id("stage_point_ordenes.filtro_subcategoria5", "id_subcategoria4", id_subcat4,
							"subCategoria", dato.getSubcat5());
					Utils.printOrdDeb("Ojo, subCategoria5 no existe. Se agregó la subCategoria5");
				}
			}

			int id_subcat6 = get_id("stage_point_ordenes.filtro_subcategoria6", "id_subcategoria5", id_subcat5,
					"subCategoria", subcat6);

			if (dato.getSubcat6() != null && subcat6 != null && !subcat6.equals(dato.getSubcat6())) {
				if (id_subcat6 > 0) {
					String update = "UPDATE stage_point_ordenes.filtro_subcategoria6 SET subCategoria = ? WHERE id = ?";
					stmt = cnx.prepareStatement(update);
					stmt.setString(1, dato.getSubcat6());
					stmt.setInt(2, id_subcat6);
					stmt.executeUpdate();

					// Utils.printOrdDeb("Actualizando filtro subCategoria6");
					DBCnx.close(stmt);
				} else {
					String insert = "INSERT INTO stage_point_ordenes.filtro_subcategoria6 (id_subcategoria5, subCategoria) "
							+ "VALUES (" + id_subcat5 + ",\"" + dato.getSubcat6() + "\")";
					DataManager.insertData2(insert, "ord");
					id_subcat6 = get_id("stage_point_ordenes.filtro_subcategoria6", "id_subcategoria5", id_subcat5,
							"subCategoria", dato.getSubcat6());
					Utils.printOrdDeb("Ojo, subCategoria6 no existe. Se agregó la subCategoria6");
				}
			}

			int id_subcat7 = get_id("stage_point_ordenes.filtro_subcategoria7", "id_subcategoria6", id_subcat6,
					"subCategoria", subcat7);

			if (dato.getSubcat7() != null && subcat7 != null && !subcat7.equals(dato.getSubcat7())) {
				if (id_subcat7 > 0) {
					String update = "UPDATE stage_point_ordenes.filtro_subcategoria7 SET subCategoria = ? WHERE id = ?";
					stmt = cnx.prepareStatement(update);
					stmt.setString(1, dato.getSubcat7());
					stmt.setInt(2, id_subcat7);
					stmt.executeUpdate();

					// Utils.printOrdDeb("Actualizando filtro subCategoria7");
					DBCnx.close(stmt);
				} else {
					String insert = "INSERT INTO stage_point_ordenes.filtro_subcategoria7 (id_subcategoria6, subCategoria) "
							+ "VALUES (" + id_subcat6 + ",\"" + dato.getSubcat7() + "\")";
					DataManager.insertData2(insert, "ord");
					id_subcat7 = get_id("stage_point_ordenes.filtro_subcategoria7", "id_subcategoria6", id_subcat6,
							"subCategoria", dato.getSubcat7());
					Utils.printOrdDeb("Ojo, subCategoria7 no existe. Se agregó la subCategoria7");
				}
			}

			int id_subcat8 = get_id("stage_point_ordenes.filtro_subcategoria8", "id_subcategoria7", id_subcat7,
					"subCategoria", subcat8);

			if (dato.getSubcat8() != null && subcat8 != null && !subcat8.equals(dato.getSubcat8())) {
				if (id_subcat8 > 0) {
					String update = "UPDATE stage_point_ordenes.filtro_subcategoria8 SET subCategoria = ? WHERE id = ?";
					stmt = cnx.prepareStatement(update);
					stmt.setString(1, dato.getSubcat8());
					stmt.setInt(2, id_subcat8);
					stmt.executeUpdate();

					// Utils.printOrdDeb("Actualizando filtro subCategoria8");
					DBCnx.close(stmt);
				} else {
					String insert = "INSERT INTO stage_point_ordenes.filtro_subcategoria8 (id_subcategoria7, subCategoria) "
							+ "VALUES (" + id_subcat7 + ",\"" + dato.getSubcat8() + "\")";
					DataManager.insertData2(insert, "ord");
					id_subcat8 = get_id("stage_point_ordenes.filtro_subcategoria8", "id_subcategoria7", id_subcat7,
							"subCategoria", dato.getSubcat8());
					Utils.printOrdDeb("Ojo, subCategoria8 no existe. Se agregó la subCategoria8");
				}
			}

		} catch (JSONException | SQLException e) {
			Utils.printOrdDeb(e.getMessage());
		} finally {
			DBCnx.close(stmt);
			DBCnx.close(cnx);
		}
	}

	public static int get_id(String tabla, String col1, String value1) {
		int id = 0;
		Connection cnx = null;
		PreparedStatement stmt = null;
		ResultSet result = null;
		try {
			cnx = DBCnx.conexion();

			String select = "SELECT id FROM " + tabla + " WHERE " + col1 + " = \"" + value1 + "\"";
			stmt = cnx.prepareStatement(select);

			result = stmt.executeQuery();

			if (result.next()) {
				id = result.getInt("id");
			}
		} catch (JSONException | SQLException e) {
			Utils.printOrdErr(e.getMessage());
			e.printStackTrace();
		} finally {
			DBCnx.close(stmt);
			DBCnx.close(cnx);
			DBCnx.close(result);
		}
		return id;
	}

	public static int get_id(String tabla, String col1, int value1, String col2, String value2) {
		int id = 0;
		Connection cnx = null;
		PreparedStatement stmt = null;
		ResultSet result = null;
		try {
			cnx = DBCnx.conexion();

			String select = "SELECT id FROM " + tabla + " WHERE " + col1 + " = " + value1 + " AND " + "" + col2
					+ " = \"" + value2 + "\"";
			stmt = cnx.prepareStatement(select);
			// Utils.printOrdDebOrdErr(stmt);
			result = stmt.executeQuery();

			if (result.next()) {
				id = result.getInt("id");
			}
		} catch (JSONException | SQLException e) {
			Utils.printOrdErr(e.getMessage());
		} finally {
			DBCnx.close(stmt);
			DBCnx.close(cnx);
			DBCnx.close(result);
		}
		return id;
	}
}
