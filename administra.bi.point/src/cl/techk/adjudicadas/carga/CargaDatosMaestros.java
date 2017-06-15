package cl.techk.adjudicadas.carga;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import cl.techk.adjudicadas.carga.Maestros_Hash;
import cl.techk.ext.database.DBCnx;
import cl.techk.ext.database.DataManager;
import cl.techk.ext.utils.Utils;

@WebServlet("/CargaDatosMaestros")
public class CargaDatosMaestros extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static ExecutorService ex = null;

	public CargaDatosMaestros() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		LoadMaestros.LoadMaestrosProveedorAux();
		LoadMaestros.LoadMaestrosCompradorGeneralAux();
		LoadMaestros.LoadMaestrosCompradorMercadoAux();
		LoadMaestros.LoadMaestrosProductosAux();
		AgregaMaestros();
	}

	private void AgregaMaestros() {
	    AddNewProviders();
	    AddNewBuyers();
	    AddNewBuyersByMarket();
	    AddNewProductCombination();
	}

	private void AddNewProviders() {

		Iterator iterador_prov = Maestros_Hash.MaestrosProvAux.entrySet().iterator();
		while (iterador_prov.hasNext()) {
			int id;

			Map.Entry maestro_prov = (Map.Entry) iterador_prov.next();
			Maestro_provAux dato = Maestros_Hash.MaestrosProvAux.get(maestro_prov.getKey());
			id = dato.getIdMaestro();

			if (id == 0) {

				Connection cnx = null;
				PreparedStatement stmt = null;
				try {
					String query = "INSERT INTO stage_point_adjudicadas.maestros_proveedores (rutProveedor, razonSocialProveedor,"
							+ "proveedorSimplificado, proveedorAsociado) VALUES (?,?,?,?)";
					cnx = DBCnx.conexion();
					stmt = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
					stmt.setString(1, maestro_prov.getKey().toString());
					stmt.setString(2, dato.getRazonSocial());
					stmt.setString(3, dato.getProveedorSimplificado());
					stmt.setString(4, dato.getProveedorAsociado());
					stmt.executeUpdate();

					Maestro_prov maestro = new Maestro_prov(dato.getRazonSocial(), dato.getProveedorSimplificado(),
							dato.getProveedorAsociado());
					Maestros_Hash.MaestrosProv.put(maestro_prov.getKey().toString(), maestro);
					DBCnx.close(stmt);

					String query_delete = "DELETE FROM stage_point_adjudicadas.maestros_proveedores_aux WHERE rutProveedor = ?";
					stmt = cnx.prepareStatement(query_delete);
					stmt.setString(1, maestro_prov.getKey().toString());
					stmt.execute();

					UpdateProvidersFilters(maestro_prov.getKey().toString(), maestro);
				} catch (SQLException e) {
					if (e.getMessage().contains("Duplicate entry")) {
						Utils.printLicDeb(
								"Maestro \"" + maestro_prov.getKey().toString() + "\" Duplicado no se ingresará");
					} else {
						Utils.printLicDeb(e.getLocalizedMessage());
					}
				} finally {
					DBCnx.close(stmt);
					DBCnx.close(cnx);
				}
			}
		}
	}

	public void UpdateProvidersFilters(String rut, Maestro_prov dato) {
		String insert = "INSERT INTO stage_point_adjudicadas.filtro_rutproveedor (rutProveedor) VALUES (\"" + rut + "\")";
		Utils.printLicDeb("Insertando en filtro rut proveedor");
		DataManager.insertData2(insert, "adj");

		int id_rut = DataManager.getFilterId("stage_point_adjudicadas.filtro_rutproveedor", "rutProveedor", rut);

		if (id_rut > 0) {

			insert = "INSERT INTO stage_point_adjudicadas.filtro_proveedor_asociado (proveedor_asociado) " + "VALUES (\""
					+ dato.getProveedorAsociado() + "\")";
			Utils.printLicDeb("Insertando en filtro proveedor asociado");
			DataManager.insertData2(insert, "adj");

			insert = "INSERT INTO stage_point_adjudicadas.filtro_razonsocialproveedor (razonSocialProveedor) " + "VALUES (\""
					+ dato.getRazonSocial() + "\")";
			Utils.printLicDeb("Insertando en filtro razon social proveedor");
			DataManager.insertData2(insert, "adj");
		}
	}

	private void AddNewBuyers() {

		Iterator iterador_comp = Maestros_Hash.MaestrosCompGAux.entrySet().iterator();
		while (iterador_comp.hasNext()) {
		    
			int id;
			Map.Entry maestro_compG = (Map.Entry) iterador_comp.next();
			Maestro_compGAux dato = Maestros_Hash.MaestrosCompGAux.get(maestro_compG.getKey());
			id = dato.getIdMaestro();

			if (id == 0) {
			    
				Utils.printLicDeb("Insertando Maestro Comprador Gnral: " + maestro_compG.getKey());
				Connection cnx = null;
				PreparedStatement stmt = null;
				try {
					String query = "INSERT INTO stage_point_adjudicadas.maestros_compradores_gnral (rutComprador, razonSocialComprador,"
							+ "unidadCompra, compradorReducido, comuna, region, segmentoComprador) VALUES (?,?,?,?,?,?,?)";
					cnx = DBCnx.conexion();

					stmt = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
					stmt.setString(1, maestro_compG.getKey().toString());
					stmt.setString(2, dato.getRazonCompradorGnral());
					stmt.setString(3, dato.getUnidadCompra());
					stmt.setString(4, dato.getCompradorReducido());
					stmt.setString(5, dato.getComuna());
					stmt.setString(6, dato.getRegion());
					stmt.setString(7, dato.getSegmentoComprador());
					stmt.executeUpdate();

					Maestro_compG maestro = new Maestro_compG(dato.getRazonCompradorGnral(), dato.getUnidadCompra(),
							dato.getCompradorReducido(), dato.getComuna(), dato.getRegion(),
							dato.getSegmentoComprador());
					Maestros_Hash.MaestrosCompG.put(maestro_compG.getKey().toString(), maestro);
					DBCnx.close(stmt);
					
					String query_delete = "DELETE FROM stage_point_adjudicadas.maestros_compradores_gnral_aux "
							+ "WHERE rutComprador = ?";

					stmt = cnx.prepareStatement(query_delete);
					stmt.setString(1, maestro_compG.getKey().toString());
					stmt.execute();

					updateBuyersFilters(maestro_compG.getKey().toString(), maestro);
				} catch (SQLException e) {
					if (e.getMessage().contains("Duplicate entry")) {
						Utils.printLicDeb(
								"Maestro " + maestro_compG.getKey().toString() + " Duplicado no se ingresará");
					} else {
						Utils.printLicDeb(e.getMessage());
					}
				} finally {
					DBCnx.close(stmt);
					DBCnx.close(cnx);
				}
			}
		}
	}

	private void AddNewBuyersByMarket() {
		Iterator iterador_comp = Maestros_Hash.MaestrosCompMAux.entrySet().iterator();
		while (iterador_comp.hasNext()) {
			int id_maestro;

			Map.Entry maestro_compM = (Map.Entry) iterador_comp.next();
			Utils.printLicDeb("Key Comprador Mercado:" + maestro_compM.getKey());
			Maestro_compMAux dato = Maestros_Hash.MaestrosCompMAux.get(maestro_compM.getKey());
			id_maestro = dato.getIdMaestro();

			if (id_maestro == 0) {
				Utils.printLicDeb("Insertando Maestro Comprador Mercado: " + maestro_compM.getKey());

				Connection cnx = null;
				PreparedStatement stmt = null;
				PreparedStatement stmt2 = null;
				ResultSet result = null;
				try {
					cnx = DBCnx.conexion();

					String select = "SELECT id FROM stage_point_adjudicadas.maestros_compradores_mercado WHERE rutComprador = ? AND mercado = ? AND segmentoCompradorMercado = ?";
					stmt = cnx.prepareStatement(select);
					stmt.setString(1, maestro_compM.getKey().toString());
					stmt.setString(2, dato.getMercado());
					stmt.setString(3, dato.getSegmentoComprador());
					result = stmt.executeQuery();
					if (!result.next()) {
						Utils.printLicDeb("Insertando maestro mercado");
						String query = "INSERT INTO stage_point_adjudicadas.maestros_compradores_mercado (rutComprador, mercado,"
								+ " segmentoCompradorMercado) VALUES (?,?,?)";

						stmt2 = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
						stmt2.setString(1, maestro_compM.getKey().toString());
						stmt2.setString(2, dato.getMercado());
						stmt2.setString(3, dato.getSegmentoComprador());
						stmt2.executeUpdate();

						DBCnx.close(stmt2);
						DBCnx.close(result);

						query = "SELECT id FROM stage_point_adjudicadas.maestros_compradores_mercado WHERE rutComprador = ? AND mercado = ? AND segmentoCompradorMercado = ?";
						stmt2 = cnx.prepareStatement(query);
						stmt2.setString(1, maestro_compM.getKey().toString());
						stmt2.setString(2, dato.getMercado());
						stmt2.setString(3, dato.getSegmentoComprador());
						result = stmt2.executeQuery();

						int id = 0;
						if (result.next()) {
						    
							id = result.getInt("id");
							Utils.printLicDeb("Nuevo id:" + id);

							Maestro_compM maestro = new Maestro_compM(maestro_compM.getKey().toString(),
									dato.getMercado(), dato.getSegmentoComprador());
							Maestros_Hash.MaestrosCompM.put(id, maestro);

							DBCnx.close(stmt2);

							Utils.printLicDeb("Eliminando maestro mercado auxiliar");
							String query_delete = "DELETE FROM stage_point_adjudicadas.maestros_compradores_mercado_aux "
									+ "WHERE id = ?";

							stmt2 = cnx.prepareStatement(query_delete);
							stmt2.setInt(1, dato.getId());
							stmt2.execute();

							updateBuyersByMarketFilters(maestro_compM.getKey().toString(), maestro);
						}
					}
				} catch (SQLException e) {
					if (e.getMessage().contains("Duplicate entry")) {
						Utils.printLicDeb(
								"Maestro \"" + maestro_compM.getKey().toString() + "\" Duplicado no se ingresará");
					} else {
						Utils.printLicDeb(e.getMessage());
					}
				} finally {
				    DBCnx.close(stmt2);
					DBCnx.closeAll(result, stmt, cnx);
				}
			}
		}
	}

	private void AddNewProductCombination() {
	    
		Iterator iterador_prod = Maestros_Hash.MaestrosProdAux.entrySet().iterator();
		while (iterador_prod.hasNext()) {
		    
			int id_maestro;
			Map.Entry maestro_prod = (Map.Entry) iterador_prod.next();
			Maestro_prodAux dato = Maestros_Hash.MaestrosProdAux.get(maestro_prod.getKey());
			id_maestro = dato.getIdMaestro();

			if (id_maestro == 0) {
			    
				Utils.printLicDeb("Insertando Maestro Jerarquía Producto: " + maestro_prod.getKey());

				Connection cnx = null;
				PreparedStatement stmt = null;
				ResultSet result = null;
				try {
					cnx = DBCnx.conexion();
					String select = "SELECT id FROM stage_point_adjudicadas.maestros_jerarquias_productos WHERE"
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
					if (dato.getSubcat4() != null && !dato.getSubcat4().isEmpty() && !dato.getSubcat4().equals("")) {
						select += " AND Subcategoria4 = \"" + dato.getSubcat4() + "\"";
					}
					if (dato.getSubcat5() != null || !dato.getSubcat5().isEmpty() || !dato.getSubcat5().equals("")) {
						select += " AND Subcategoria5 = \"" + dato.getSubcat5() + "\"";
					}
					if (dato.getSubcat6() != null || !dato.getSubcat6().isEmpty() || !dato.getSubcat6().equals("")) {
						select += " AND Subcategoria6 = \"" + dato.getSubcat6() + "\"";
					}
					if (dato.getSubcat7() != null || !dato.getSubcat7().isEmpty() || !dato.getSubcat7().equals("")) {
						select += " AND Subcategoria7 = \"" + dato.getSubcat7() + "\"";
					}
					if (dato.getSubcat8() != null || !dato.getSubcat8().isEmpty() || !dato.getSubcat8().equals("")) {
						select += " AND Subcategoria8 = \"" + dato.getSubcat8() + "\"";
					}
					Utils.printLicDeb("Buscando Jerarquía de producto");
					stmt = cnx.prepareStatement(select);
					result = stmt.executeQuery();

					if (!result.next()) {
						DBCnx.close(stmt);
						DBCnx.close(result);
						String insert = "INSERT INTO stage_point_adjudicadas.maestros_jerarquias_productos (mercado, categoria, "
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
						Utils.printLicDeb("Insertando Jerarquia");
						stmt.executeUpdate();

						int id_product_combination = getIdProductCombination(dato);
						if (id_product_combination > 0) {

							Maestro_prod maestro = new Maestro_prod(dato.getMercado(), dato.getCategoria(),
									dato.getSubcat1(), dato.getSubcat2(), dato.getSubcat3(), dato.getSubcat4(),
									dato.getSubcat5(), dato.getSubcat6(), dato.getSubcat7(), dato.getSubcat8());
							Maestros_Hash.MaestrosProd.put(id_product_combination, maestro);

							DBCnx.close(stmt);
							Utils.printLicDeb("Eliminando jararquia auxiliar");
							String query_delete = "DELETE FROM stage_point_adjudicadas.maestros_jerarquias_productos_aux "
									+ "WHERE id = ?";

							stmt = cnx.prepareStatement(query_delete);
							stmt.setInt(1, dato.getId());
							stmt.execute();

							UpdateProductsFilters(maestro);
						}
					}
				} catch (SQLException e) {
					if (e.getMessage().contains("Duplicate entry")) {
						Utils.printLicDeb(
								"Maestro \"" + maestro_prod.getKey().toString() + "\" Duplicado no se ingresará");
					} else {
						Utils.printLicDeb(e.getMessage());
					}
				} finally {
					DBCnx.closeAll(result, stmt, cnx);
				}
			}
		}
	}

	private void UpdateProductsFilters(Maestro_prod dato) {

		String insert_new_category = "INSERT INTO stage_point_adjudicadas.filtro_categoria (categoria) " + "VALUES (\""
				+ dato.getCategoria() + "\")";
		Utils.printLicDeb("Insertando categoria");
		DataManager.insertData2(insert_new_category, "adj");

		if (!dato.getSubcat1().isEmpty() || !dato.getSubcat1().equals("")) {
			String insert_new_subcategory1 = "INSERT INTO stage_point_adjudicadas.filtro_subcategoria1 (subCategoria) VALUES " + "(\""
					+ dato.getSubcat1() + "\")";
			Utils.printLicDeb("Insertando sub categoria1");
			DataManager.insertData2(insert_new_subcategory1, "adj");
		}

		if (!dato.getSubcat2().isEmpty() || !dato.getSubcat2().equals("")) {

			String insert_new_subcategory2 = "INSERT INTO stage_point_adjudicadas.filtro_subcategoria2 (subCategoria) VALUES " + "(\""
					+ dato.getSubcat2() + "\")";
			Utils.printLicDeb("Insertando sub categoria2");
			DataManager.insertData2(insert_new_subcategory2, "adj");
		}

		if (!dato.getSubcat3().isEmpty() || !dato.getSubcat3().equals("")) {
			String insert_new_subcategory3 = "INSERT INTO stage_point_adjudicadas.filtro_subcategoria3 (subCategoria) VALUES " + "(\""
					+ dato.getSubcat3() + "\")";
			Utils.printLicDeb("Insertando sub categoria3");
			DataManager.insertData2(insert_new_subcategory3, "adj");
		}

		if (!dato.getSubcat4().isEmpty() || !dato.getSubcat4().equals("")) {
		    String insert_new_subcategory4 = "INSERT INTO stage_point_adjudicadas.filtro_subcategoria4 (subCategoria) VALUES " + "(\""
					+ dato.getSubcat4() + "\")";
			Utils.printLicDeb("Insertando sub categoria4");
			DataManager.insertData2(insert_new_subcategory4, "adj");
		}

		if (!dato.getSubcat5().isEmpty() || !dato.getSubcat5().equals("")) {
			String insert_new_subcategory5 = "INSERT INTO stage_point_adjudicadas.filtro_subcategoria5 (subCategoria) VALUES " + "(\""
					+ dato.getSubcat5() + "\")";
			Utils.printLicDeb("Insertando sub categoria5");
			DataManager.insertData2(insert_new_subcategory5, "adj");
		}

		if (!dato.getSubcat6().isEmpty() || !dato.getSubcat6().equals("")) {
			String insert_new_subcategory6 = "INSERT INTO stage_point_adjudicadas.filtro_subcategoria6 (subCategoria) VALUES " + "(\""
					+ dato.getSubcat6() + "\")";
			Utils.printLicDeb("Insertando sub categoria6");
			DataManager.insertData2(insert_new_subcategory6, "adj");
		}

		if (!dato.getSubcat7().isEmpty() || !dato.getSubcat7().equals("")) {
		    String insert_new_subcategory7 = "INSERT INTO stage_point_adjudicadas.filtro_subcategoria7 (subCategoria) VALUES " + "(\""
					+ dato.getSubcat7() + "\")";
			Utils.printLicDeb("Insertando sub categoria7");
			DataManager.insertData2(insert_new_subcategory7, "adj");
		}

		if (!dato.getSubcat8().isEmpty() || !dato.getSubcat8().equals("")) {
			String insert_new_subcategory8 = "INSERT INTO stage_point_adjudicadas.filtro_subcategoria8 (subCategoria) VALUES " + "(\""
					+ dato.getSubcat8() + "\")";
			Utils.printLicDeb("Insertando sub categoria8");
			DataManager.insertData2(insert_new_subcategory8, "adj");
		}
	}

	private void updateBuyersByMarketFilters(String rut, Maestro_compM dato) {
	    
		Connection cnx = null;
		PreparedStatement stmt = null;
		ResultSet result = null;

		try {
			cnx = DBCnx.conexion();
			String select = "SELECT id FROM stage_point_adjudicadas.filtro_mercado WHERE mercado = ?";
			stmt = cnx.prepareStatement(select);
			stmt.setString(1, dato.getMercado());
			result = stmt.executeQuery();

			if (result.next()) {
			    
				String insert = "INSERT INTO stage_point_adjudicadas.filtro_segmentocompradormercado (segmento_mercado) "
						+ "VALUES (\"" + dato.getSegmentoComprador() + "\")";
				Utils.printLicDeb("Insertando en filtro segmento comprador mercado");
				DataManager.insertData2(insert, "adj");
			} else {
			    
				String insert = "INSERT INTO stage_point_adjudicadas.filtro_mercado (mercado) VALUES (\"" + dato.getMercado()
						+ "\")";
				Utils.printLicDeb("Ojo, Mercado " + dato.getMercado() + " no existe. Se agregó mercado");
				DataManager.insertData2(insert, "adj");

				insert = "INSERT INTO stage_point_adjudicadas.filtro_segmentocompradormercado (segmento_mercado) "
						+ "VALUES (\"" + dato.getSegmentoComprador() + "\")";
				Utils.printLicDeb("Insertando en filtro segmento comprador mercado");
				DataManager.insertData2(insert, "adj");
			}
		} catch (SQLException e) {
			Utils.printLicDeb(e.getMessage());
		} finally {
		    DBCnx.closeAll(result, stmt, cnx);
		}
	}

	public void updateBuyersFilters(String rut, Maestro_compG dato) {

		String insert = "INSERT INTO stage_point_adjudicadas.filtro_rutcomprador (rutComprador) VALUES (\"" + rut + "\")";
		Utils.printLicDeb("Insertando en filtro rut comprador");
		DataManager.insertData2(insert, "adj");
		int id_rut = DataManager.getFilterId("stage_point_adjudicadas.filtro_rutcomprador", "rutComprador", rut);

		if (id_rut > 0) {

			insert = "INSERT INTO stage_point_adjudicadas.filtro_razonsocialcomprador (razonSocialComprador) " + "VALUES (\""
					+ dato.getRazonCompradorGnral() + "\")";
			Utils.printLicDeb("Insertando en filtro razon social comprador");
			DataManager.insertData2(insert, "adj");

			insert = "INSERT INTO stage_point_adjudicadas.filtro_segmentocomprador (segmento) VALUES (\""
					+ dato.getSegmentoComprador() + "\")";
			Utils.printLicDeb("Insertando en filtro segmento comprador");
			DataManager.insertData2(insert, "adj");

			insert = "INSERT INTO stage_point_adjudicadas.filtro_unidad_compra (unidad_compra) VALUES (\""
					+ dato.getUnidadCompra() + "\")";
			Utils.printLicDeb("Insertando en filtro unidad de compra");
			DataManager.insertData2(insert, "adj");

			validateCommune(dato.getComuna());
		}
	}

	private void validateCommune(String comuna) {

		Connection cnx = null;
		PreparedStatement stmt = null;

		try {
		    
			cnx = DBCnx.conexion();
			String insert = "INSERT INTO stage_point_adjudicadas.filtro_comunas (comuna) VALUES (\"" + comuna + "\");";
			Utils.printLicDeb("Insertando en filtro comuna");
			stmt = cnx.prepareStatement(insert);
			stmt.executeUpdate();
		} catch (SQLException e) {
			Utils.printLicDeb(e.getMessage());
		} finally {
		    DBCnx.close(stmt);
			DBCnx.close(cnx);
		}
	}

	private int getIdProductCombination(Maestro_prodAux dato) {
		Connection cnx = null;
		PreparedStatement stmt = null;
		ResultSet result = null;

		int id_product_combination = 0;
		try {
			cnx = DBCnx.conexion();

			String select = "SELECT id FROM stage_point_adjudicadas.maestros_jerarquias_productos WHERE" + " mercado = \""
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
			Utils.printLicDeb("Buscando Jerarquía de producto");
			stmt = cnx.prepareStatement(select);
			result = stmt.executeQuery();

			if (result.next()) {
			    id_product_combination = result.getInt("id");
			}
		} catch (SQLException e) {
			Utils.printLicDeb(e.getMessage());
		} finally {
		    DBCnx.closeAll(result, stmt, cnx);
		}

		return id_product_combination;
	}
}