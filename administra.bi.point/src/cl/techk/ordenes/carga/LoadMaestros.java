package cl.techk.ordenes.carga;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import cl.techk.ext.database.DBCnx;
import cl.techk.ext.utils.InitLog;
import cl.techk.ext.utils.Utils;

public class LoadMaestros {

	public static void LoadMaestrosProveedor(String bdd_name) {
		if (InitLog.MaestrosProv != null) {
			InitLog.MaestrosProv.clear();
		}

		Statement consulta = null;
		ResultSet resultados = null;
		Connection conn = null;
		int cant = 0;
		try {
			conn = DBCnx.conexion();
			consulta = conn.createStatement();
			resultados = consulta.executeQuery("SELECT * FROM "+bdd_name+".maestros_proveedores");
			while (resultados.next()) {

				String rut = resultados.getString("rutProveedor");
				String razonSocial = resultados.getString("razonSocialProveedor");
				String proveedorSimp = resultados.getString("proveedorSimplificado");
				String proveedorAsoc = resultados.getString("proveedorAsociado");

				Maestro_prov maestro = new Maestro_prov(razonSocial, proveedorSimp, proveedorAsoc);
				InitLog.MaestrosProv.put(rut, maestro);
				cant++;
			}
		} catch (Exception error) {
			Utils.printOrdErr(error.getMessage());
			Utils.printOrdErr("error -------> obteniendo Proveedor: " + cant);
		} finally {
			DBCnx.closeAll(resultados, consulta, conn);
			Utils.printOrdDeb("cargados " + cant + " Proveedores");
		}
	}

	public static void LoadMaestrosCompradorGeneral(String bdd_name) {
		if (InitLog.MaestrosCompG != null) {
			InitLog.MaestrosCompG.clear();
		}

		Statement consulta = null;
		ResultSet resultados = null;
		Connection conexion = null;
		int cant = 0;
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();
			resultados = consulta.executeQuery("SELECT * FROM "+bdd_name+".maestros_compradores_gnral");
			while (resultados.next()) {
				String id = resultados.getString("id");
				String rutComprador = resultados.getString("rutComprador");
				String razonSocialComprador = resultados.getString("razonSocialComprador");
				String unidadCompra = resultados.getString("unidadCompra");
				String compradorReducido = resultados.getString("compradorReducido");
				String comuna = resultados.getString("comuna");
				String region = resultados.getString("region");
				String segmentoComprador = resultados.getString("segmentoComprador");

				Maestro_compG maestro = new Maestro_compG(rutComprador, razonSocialComprador, unidadCompra, compradorReducido, comuna,
						region, segmentoComprador);
				InitLog.MaestrosCompG.put(id, maestro);
				cant++;
			}
		} catch (Exception error) {
			Utils.printOrdErr(error.getMessage());
			Utils.printOrdErr("error -------> obteniendo Maestros Compradores Gnral: " + cant);
		} finally {
			DBCnx.closeAll(resultados, consulta, conexion);
			Utils.printOrdDeb("cargados " + cant + " Maestros Compradores Gnral");
		}
	}

	public static void LoadMaestrosCompradorMercado(String bdd_name) {
		if (InitLog.MaestrosCompM != null) {
			InitLog.MaestrosCompM.clear();
		}

		Statement consulta = null;
		ResultSet resultados = null;
		Connection conexion = null;
		int cant = 0;
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();
			resultados = consulta.executeQuery("SELECT * FROM "+bdd_name+".maestros_compradores_mercado");
			while (resultados.next()) {
				int id = resultados.getInt("id");
				String rutComprador = resultados.getString("rutComprador");
				String mercado = resultados.getString("mercado");
				String segmentoCompradorMercado = resultados.getString("segmentoCompradorMercado");

				Maestro_compM maestro = new Maestro_compM(rutComprador, mercado, segmentoCompradorMercado);
				InitLog.MaestrosCompM.put(id, maestro);
				cant++;
			}
		} catch (Exception error) {
			Utils.printOrdErr(error.getMessage());
			Utils.printOrdErr("error -------> obteniendo Maestros Compradores Mercado: " + cant);
		} finally {
			DBCnx.closeAll(resultados, consulta, conexion);
			Utils.printOrdDeb("cargados " + cant + " Maestros Compradores Mercado");
		}
	}

	public static void LoadMaestrosProductos(String bdd_name) {
		if (InitLog.MaestrosProd != null) {
			InitLog.MaestrosProd.clear();
		}

		Statement consulta = null;
		ResultSet resultados = null;
		Connection conexion = null;
		int cant = 0;
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();
			resultados = consulta.executeQuery("SELECT * FROM "+bdd_name+".maestros_jerarquias_productos");
			while (resultados.next()) {
				int id = resultados.getInt("id");
				String mercado = resultados.getString("mercado");
				String categoria = resultados.getString("categoria");
				String subcategoria1 = resultados.getString("subcategoria1");
				String subcategoria2 = resultados.getString("subcategoria2");
				String subcategoria3 = resultados.getString("subcategoria3");
				String subcategoria4 = resultados.getString("subcategoria4");
				String subcategoria5 = resultados.getString("subcategoria5");
				String subcategoria6 = resultados.getString("subcategoria6");
				String subcategoria7 = resultados.getString("subcategoria7");
				String subcategoria8 = resultados.getString("subcategoria8");

				Maestro_prod maestro = new Maestro_prod(mercado, categoria, subcategoria1, subcategoria2, subcategoria3,
						subcategoria4, subcategoria5, subcategoria6, subcategoria7, subcategoria8);
				InitLog.MaestrosProd.put(id, maestro);
				cant++;
			}
		} catch (Exception error) {
			Utils.printOrdErr(error.getMessage());
			Utils.printOrdErr("error -------> obteniendo Maestros de Productos: " + cant);
		} finally {
			DBCnx.closeAll(resultados, consulta, conexion);
			Utils.printOrdDeb("cargados " + cant + " Maestros de Produtos");
		}
	}

	public static void LoadMaestrosProveedorAux(String bdd_name) {
		if (InitLog.MaestrosProvAux != null) {
			InitLog.MaestrosProvAux.clear();
		}

		Statement consulta = null;
		ResultSet resultados = null;
		Connection conexion = null;
		int cant = 0;
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();
			resultados = consulta.executeQuery("SELECT * FROM "+bdd_name+".maestros_proveedores_aux");
			while (resultados.next()) {

				String id = resultados.getString("id");
				int idmaestro = resultados.getInt("id_maestro_gnral");
				String rut = resultados.getString("rutProveedor");
				String razonSocial = resultados.getString("razonSocialProveedor");
				String proveedorSimp = resultados.getString("proveedorSimplificado");
				String proveedorAsoc = resultados.getString("proveedorAsociado");

				Maestro_provAux maestro = new Maestro_provAux(idmaestro,rut, razonSocial, proveedorSimp, proveedorAsoc);
				InitLog.MaestrosProvAux.put(id, maestro);
				cant++;
			}
		} catch (Exception error) {
			Utils.printOrdErr(error.getMessage());
			Utils.printOrdErr("error -------> obteniendo Proveedor Aux: " + cant);
		} finally {
			DBCnx.closeAll(resultados, consulta, conexion);
			Utils.printOrdDeb("cargados " + cant + " Proveedores Aux");
		}
	}

	public static void LoadMaestrosCompradorGeneralAux(String bdd_name) {
		if (InitLog.MaestrosCompGAux != null) {
			InitLog.MaestrosCompGAux.clear();
		}

		Statement consulta = null;
		ResultSet resultados = null;
		Connection conexion = null;
		int cant = 0;
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();
			resultados = consulta.executeQuery("SELECT * FROM "+bdd_name+".maestros_compradores_gnral_aux");
			while (resultados.next()) {

				String id = resultados.getString("id");
				int idmaestro = resultados.getInt("id_maestro_gnral");
				String rutComprador = resultados.getString("rutComprador");
				String razonSocialComprador = resultados.getString("razonSocialComprador");
				String unidadCompra = resultados.getString("unidadCompra");
				String compradorReducido = resultados.getString("compradorReducido");
				String comuna = resultados.getString("comuna");
				String region = resultados.getString("region");
				String segmentoComprador = resultados.getString("segmentoComprador");

				Maestro_compGAux maestro = new Maestro_compGAux(idmaestro,rutComprador, razonSocialComprador, unidadCompra,
						compradorReducido, comuna, region, segmentoComprador);
				InitLog.MaestrosCompGAux.put(id, maestro);
				cant++;
			}
		} catch (Exception error) {
			Utils.printOrdErr(error.getMessage());
			Utils.printOrdErr("error -------> obteniendo Maestros Compradores Gnral aux: " + cant);
		} finally {
			DBCnx.closeAll(resultados, consulta, conexion);
			Utils.printOrdDeb("cargados " + cant + " Maestros Compradores Gnral aux");
		}
	}

	public static void LoadMaestrosCompradorMercadoAux(String bdd_name) {
		if (InitLog.MaestrosCompMAux != null) {
			InitLog.MaestrosCompMAux.clear();
		}

		Statement consulta = null;
		ResultSet resultados = null;
		Connection conexion = null;
		int cant = 0;
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();
			resultados = consulta.executeQuery("SELECT * FROM "+bdd_name+".maestros_compradores_mercado_aux");
			while (resultados.next()) {

				int id = resultados.getInt("id");

				int idmaestro = resultados.getInt("id_maestro_gnral");
				String rutComprador = resultados.getString("rutComprador");
				String mercado = resultados.getString("mercado");
				String segmentoCompradorMercado = resultados.getString("segmentoCompradorMercado");

				Maestro_compMAux maestro = new Maestro_compMAux(id, idmaestro,rutComprador, mercado, segmentoCompradorMercado);

				InitLog.MaestrosCompMAux.put(String.valueOf(id), maestro);
				cant++;
			}
		} catch (Exception error) {
			Utils.printOrdErr(error.getMessage());
			Utils.printOrdErr("error -------> obteniendo Maestros Compradores Mercado aux: " + cant);
		} finally {
			DBCnx.closeAll(resultados, consulta, conexion);
			Utils.printOrdDeb("cargados " + cant + " Maestros Compradores Mercado aux");
		}
	}

	public static void LoadMaestrosProductosAux(String bdd_name) {
		if (InitLog.MaestrosProdAux != null) {
			InitLog.MaestrosProdAux.clear();
		}

		Statement consulta = null;
		ResultSet resultados = null;
		Connection conexion = null;
		int cant = 0;
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();
			resultados = consulta.executeQuery("SELECT * FROM "+bdd_name+".maestros_jerarquias_productos_aux");
			while (resultados.next()) {

				int id = resultados.getInt("id");
				int idmaestro = resultados.getInt("id_maestro_gnral");
				String mercado = resultados.getString("mercado");
				String categoria = resultados.getString("categoria");
				String subcategoria1 = resultados.getString("subcategoria1");
				String subcategoria2 = resultados.getString("subcategoria2");
				String subcategoria3 = resultados.getString("subcategoria3");
				String subcategoria4 = resultados.getString("subcategoria4");
				String subcategoria5 = resultados.getString("subcategoria5");
				String subcategoria6 = resultados.getString("subcategoria6");
				String subcategoria7 = resultados.getString("subcategoria7");
				String subcategoria8 = resultados.getString("subcategoria8");

				Maestro_prodAux maestro = new Maestro_prodAux(id, idmaestro, mercado, categoria, subcategoria1,
						subcategoria2, subcategoria3, subcategoria4, subcategoria5, subcategoria6, subcategoria7,
						subcategoria8);

				InitLog.MaestrosProdAux.put(id, maestro);
				cant++;
			}
		} catch (Exception error) {
			Utils.printOrdErr(error.getMessage());
			Utils.printOrdErr("error -------> obteniendo Maestros de Productos aux: " + cant);
		} finally {
			DBCnx.closeAll(resultados, consulta, conexion);
			Utils.printOrdDeb("cargados " + cant + " Maestros de Produtos aux");
		}
	}
}
