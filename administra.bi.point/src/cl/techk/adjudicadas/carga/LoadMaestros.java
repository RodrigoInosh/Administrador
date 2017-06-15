package cl.techk.adjudicadas.carga;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import cl.techk.adjudicadas.carga.Maestros_Hash;
import cl.techk.ext.database.DBCnx;
import cl.techk.ext.utils.Utils;

public class LoadMaestros {

	public static void LoadMaestrosProveedor() {
		if (Maestros_Hash.MaestrosProv != null) {
			Maestros_Hash.MaestrosProv.clear();
		}

		Statement consulta = null;
		ResultSet resultados = null;
		Connection conn = null;
		int cant = 0;
		try {
			conn = DBCnx.conexion();
			consulta = conn.createStatement();
			resultados = consulta.executeQuery("SELECT * FROM stage_point_adjudicadas.maestros_proveedores");
			while (resultados.next()) {

				String rut = resultados.getString("rutProveedor");
				String razonSocial = resultados.getString("razonSocialProveedor");
				String proveedorSimp = resultados.getString("proveedorSimplificado");
				String proveedorAsoc = resultados.getString("proveedorAsociado");

				Maestro_prov maestro = new Maestro_prov(razonSocial, proveedorSimp, proveedorAsoc);
				Maestros_Hash.MaestrosProv.put(rut, maestro);
				cant++;
			}
		} catch (Exception error) {
			Utils.printAdjErr(error.getMessage());
			Utils.printAdjErr("error -------> obteniendo Proveedor: " + cant);
		} finally {
			DBCnx.close(resultados);
			DBCnx.close(consulta);
			DBCnx.close(conn);
			Utils.printAdjDeb("cargados " + cant + " Proveedores");
		}
	}

	public static void LoadMaestrosCompradorGeneral() {
		if (Maestros_Hash.MaestrosCompG != null) {
			Maestros_Hash.MaestrosCompG.clear();
		}

		Statement consulta = null;
		ResultSet resultados = null;
		Connection conexion = null;
		int cant = 0;
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();
			resultados = consulta.executeQuery("SELECT * FROM stage_point_adjudicadas.maestros_compradores_gnral");
			while (resultados.next()) {
				String rutComprador = resultados.getString("rutComprador");
				String razonSocialComprador = resultados.getString("razonSocialComprador");
				String unidadCompra = resultados.getString("unidadCompra");
				String compradorReducido = resultados.getString("compradorReducido");
				String comuna = resultados.getString("comuna");
				String region = resultados.getString("region");
				String segmentoComprador = resultados.getString("segmentoComprador");

				Maestro_compG maestro = new Maestro_compG(razonSocialComprador, unidadCompra, compradorReducido, comuna,
						region, segmentoComprador);
				Maestros_Hash.MaestrosCompG.put(rutComprador, maestro);
				cant++;
			}
		} catch (Exception error) {
			Utils.printAdjErr(error.getMessage());
			Utils.printAdjErr("error -------> obteniendo Maestros Compradores Gnral: " + cant);
		} finally {
			DBCnx.close(resultados);
			DBCnx.close(consulta);
			DBCnx.close(conexion);
			Utils.printAdjDeb("cargados " + cant + " Maestros Compradores Gnral");
		}
	}

	public static void LoadMaestrosCompradorMercado() {
		if (Maestros_Hash.MaestrosCompM != null) {
			Maestros_Hash.MaestrosCompM.clear();
		}

		Statement consulta = null;
		ResultSet resultados = null;
		Connection conexion = null;
		int cant = 0;
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();
			resultados = consulta.executeQuery("SELECT * FROM stage_point_adjudicadas.maestros_compradores_mercado");
			while (resultados.next()) {
				int id = resultados.getInt("id");
				String rutComprador = resultados.getString("rutComprador");
				String mercado = resultados.getString("mercado");
				String segmentoCompradorMercado = resultados.getString("segmentoCompradorMercado");

				Maestro_compM maestro = new Maestro_compM(rutComprador, mercado, segmentoCompradorMercado);
				Maestros_Hash.MaestrosCompM.put(id, maestro);
				cant++;
			}
		} catch (Exception error) {
			Utils.printAdjErr(error.getMessage());
			Utils.printAdjErr("error -------> obteniendo Maestros Compradores Mercado: " + cant);
		} finally {
			DBCnx.close(resultados);
			DBCnx.close(consulta);
			DBCnx.close(conexion);
			Utils.printAdjDeb("cargados " + cant + " Maestros Compradores Mercado");
		}
	}

	public static void LoadMaestrosProductos() {
		if (Maestros_Hash.MaestrosProd != null) {
			Maestros_Hash.MaestrosProd.clear();
		}

		Statement consulta = null;
		ResultSet resultados = null;
		Connection conexion = null;
		int cant = 0;
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();
			resultados = consulta.executeQuery("SELECT * FROM stage_point_adjudicadas.maestros_jerarquias_productos");
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
				Maestros_Hash.MaestrosProd.put(id, maestro);
				cant++;
			}
		} catch (Exception error) {
			Utils.printAdjErr(error.getMessage());
			Utils.printAdjErr("error -------> obteniendo Maestros de Productos: " + cant);
		} finally {
			DBCnx.close(resultados);
			DBCnx.close(consulta);
			DBCnx.close(conexion);
			Utils.printAdjDeb("cargados " + cant + " Maestros de Produtos");
		}
	}

	public static void LoadMaestrosProveedorAux() {
		if (Maestros_Hash.MaestrosProvAux != null) {
			Maestros_Hash.MaestrosProvAux.clear();
		}

		Statement consulta = null;
		ResultSet resultados = null;
		Connection conexion = null;
		int cant = 0;
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();
			resultados = consulta.executeQuery("SELECT * FROM stage_point_adjudicadas.maestros_proveedores_aux");
			while (resultados.next()) {

				int idmaestro = resultados.getInt("id_maestro_gnral");
				String rut = resultados.getString("rutProveedor");
				String razonSocial = resultados.getString("razonSocialProveedor");
				String proveedorSimp = resultados.getString("proveedorSimplificado");
				String proveedorAsoc = resultados.getString("proveedorAsociado");

				Maestro_provAux maestro = new Maestro_provAux(idmaestro, razonSocial, proveedorSimp, proveedorAsoc);
				Maestros_Hash.MaestrosProvAux.put(rut, maestro);
				cant++;
			}
		} catch (Exception error) {
			Utils.printAdjErr(error.getMessage());
			Utils.printAdjErr("error -------> obteniendo Proveedor Aux: " + cant);
		} finally {
			DBCnx.close(resultados);
			DBCnx.close(consulta);
			DBCnx.close(conexion);
			Utils.printAdjDeb("cargados " + cant + " Proveedores Aux");
		}
	}

	public static void LoadMaestrosCompradorGeneralAux() {
		if (Maestros_Hash.MaestrosCompGAux != null) {
			Maestros_Hash.MaestrosCompGAux.clear();
		}

		Statement consulta = null;
		ResultSet resultados = null;
		Connection conexion = null;
		int cant = 0;
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();
			resultados = consulta.executeQuery("SELECT * FROM stage_point_adjudicadas.maestros_compradores_gnral_aux");
			while (resultados.next()) {

				int idmaestro = resultados.getInt("id_maestro_gnral");
				String rutComprador = resultados.getString("rutComprador");
				String razonSocialComprador = resultados.getString("razonSocialComprador");
				String unidadCompra = resultados.getString("unidadCompra");
				String compradorReducido = resultados.getString("compradorReducido");
				String comuna = resultados.getString("comuna");
				String region = resultados.getString("region");
				String segmentoComprador = resultados.getString("segmentoComprador");

				Maestro_compGAux maestro = new Maestro_compGAux(idmaestro, razonSocialComprador, unidadCompra,
						compradorReducido, comuna, region, segmentoComprador);
				Maestros_Hash.MaestrosCompGAux.put(rutComprador, maestro);
				cant++;
			}
		} catch (Exception error) {
			Utils.printAdjErr(error.getMessage());
			Utils.printAdjErr("error -------> obteniendo Maestros Compradores Gnral aux: " + cant);
		} finally {
			DBCnx.close(resultados);
			DBCnx.close(consulta);
			DBCnx.close(conexion);
			Utils.printAdjDeb("cargados " + cant + " Maestros Compradores Gnral aux");
		}
	}

	public static void LoadMaestrosCompradorMercadoAux() {
		if (Maestros_Hash.MaestrosCompMAux != null) {
			Maestros_Hash.MaestrosCompMAux.clear();
		}

		Statement consulta = null;
		ResultSet resultados = null;
		Connection conexion = null;
		int cant = 0;
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();
			resultados = consulta.executeQuery("SELECT * FROM stage_point_adjudicadas.maestros_compradores_mercado_aux");
			while (resultados.next()) {

				int id = resultados.getInt("id");

				int idmaestro = resultados.getInt("id_maestro_gnral");
				String rutComprador = resultados.getString("rutComprador");
				String mercado = resultados.getString("mercado");
				String segmentoCompradorMercado = resultados.getString("segmentoCompradorMercado");

				Maestro_compMAux maestro = new Maestro_compMAux(id, idmaestro, mercado, segmentoCompradorMercado);

				Maestros_Hash.MaestrosCompMAux.put(rutComprador, maestro);
				cant++;
			}
		} catch (Exception error) {
			Utils.printAdjErr(error.getMessage());
			Utils.printAdjErr("error -------> obteniendo Maestros Compradores Mercado aux: " + cant);
		} finally {
			DBCnx.close(resultados);
			DBCnx.close(consulta);
			DBCnx.close(conexion);
			Utils.printAdjDeb("cargados " + cant + " Maestros Compradores Mercado aux");
		}
	}

	public static void LoadMaestrosProductosAux() {
		if (Maestros_Hash.MaestrosProdAux != null) {
			Maestros_Hash.MaestrosProdAux.clear();
		}

		Statement consulta = null;
		ResultSet resultados = null;
		Connection conexion = null;
		int cant = 0;
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();
			resultados = consulta.executeQuery("SELECT * FROM stage_point_adjudicadas.maestros_jerarquias_productos_aux");
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

				Maestros_Hash.MaestrosProdAux.put(id, maestro);
				cant++;
			}
		} catch (Exception error) {
			Utils.printAdjErr(error.getMessage());
			Utils.printAdjErr("error -------> obteniendo Maestros de Productos aux: " + cant);
		} finally {
			DBCnx.close(resultados);
			DBCnx.close(consulta);
			DBCnx.close(conexion);
			Utils.printAdjDeb("cargados " + cant + " Maestros de Produtos aux");
		}
	}
}
