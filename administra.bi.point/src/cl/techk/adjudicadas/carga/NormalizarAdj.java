package cl.techk.adjudicadas.carga;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;

import cl.techk.ext.database.DBCnx;
import cl.techk.ext.database.DataManager;
import cl.techk.ext.utils.Utils;

/**
 * Servlet implementation class CargaDatos2
 */
@Path("/NormalizarAdj")
public class NormalizarAdj {

	@GET
	@Path("/insert_fecha/")
	@Produces("application/json; charset=UTF-8")
	public Response insert_fecha(@Context final HttpServletRequest request) throws JSONException {

		JSONObject response = new JSONObject();

		Statement consulta = null;
		ResultSet resultados = null;
		Connection conexion = null;

		Date ini = new Date();
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();

			String query = "SELECT LAST_DAY(MAX(fechaAdjudicada)) fechaEnvio FROM stage_point_adjudicadas.adjudicadas_aux adj;";
			Utils.printAdjDeb(query);
			resultados = consulta.executeQuery(query);
			if (resultados.next()) {

				// Insertar datos en la tabla última fecha
				Date fecha = resultados.getDate("fechaEnvio");
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String insert = "UPDATE stage_point_adjudicadas.ultima_fecha SET fecha_fin = '" + sdf.format(fecha)
						+ "'";

				Utils.printAdjDeb("Insertando Última fecha");
				consulta.executeUpdate(insert);

				// Insertar datos en la tabla meses
				Calendar cal = Calendar.getInstance();
				cal.setTime(fecha);
				int mes = cal.get(Calendar.MONTH)+1;
				int year = cal.get(Calendar.YEAR);
				String f = new SimpleDateFormat("yyyy-MM-01").format(fecha);

				String delete_meses = "DELETE FROM stage_point_adjudicadas.meses WHERE mes = "+mes+" AND "
						+ "anho = "+year+" AND fecha = '"+f+"'";

				Utils.printAdjDeb("Borrando mes actual");
				consulta.executeUpdate(delete_meses);
				
				String insert_meses = "INSERT INTO stage_point_adjudicadas.meses (`mes`, `anho`, `fecha`) VALUES ("+mes+","+year+",'"+f+"')";
				Utils.printAdjDeb("insertando mes actual");
				consulta.executeUpdate(insert_meses);
			}
			Date fin = new Date();
			double tiempo_total = (fin.getTime() - ini.getTime()) / 1000 / 60;
			Utils.printAdjDeb("Tiempo Ingresando Fecha: " + tiempo_total);
		} catch (Exception error) {
			error.printStackTrace();
			Utils.printAdjErr("Error Insertando Ultima Fecha: " + error.getMessage());
		} finally {
			DBCnx.closeAll(resultados, consulta, conexion);
		}
		return Response.status(200).entity(response.toString()).build();
	}

	@GET
	@Path("/generarFiltros/")
	@Produces("application/json; charset=UTF-8")
	public Response generarFiltros(@Context final HttpServletRequest request) throws JSONException {
		JSONObject response = new JSONObject();

		int cant = 0;
		ExecutorService ex = null;
		Date ini = new Date();
		try {
			ex = Executors.newFixedThreadPool(3);
			NormalizaThread t;
			String query = "";
			Utils.printAdjDeb("Generando Filtros");

			ArrayList<Integer> tipos_lic_inicial = getTiposLic();

			query = "SELECT estado FROM stage_point_adjudicadas.adjudicadas_aux GROUP BY estado";
			t = new NormalizaThread(query, "filtro_estado", "estado");
			ex.execute(t);

			query = "SELECT tipo FROM stage_point_adjudicadas.adjudicadas_aux GROUP BY tipo";
			t = new NormalizaThread(query, "filtro_tipolicitacion", "tipo_licitacion");
			ex.execute(t);

			query = "SELECT observacion FROM stage_point_adjudicadas.adjudicadas_aux GROUP BY observacion";
			t = new NormalizaThread(query, "filtro_observacion", "observacion");
			ex.execute(t);

			ex.shutdown();
			while (!ex.isTerminated()) {
				// Utils.print("esperando .." + ex.isTerminated());
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Utils.printAdjErr(e.getMessage());
				}
			}

			ArrayList<Integer> tipos_lic_fin = getTiposLic();

			if (tipos_lic_inicial.size() != tipos_lic_fin.size()) {
				tipos_lic_fin.removeAll(tipos_lic_inicial);

				Statement consulta = null;
				Connection conexion = null;

				try {
					conexion = DBCnx.conexion();
					consulta = conexion.createStatement();
					int ciclo_tipos = 0;
					while (ciclo_tipos < tipos_lic_fin.size()) {
						int id_tipo = tipos_lic_fin.get(ciclo_tipos);

						String delete_combis = "CREATE TABLE stage_point_adjudicadas.adjudicadas_tipo" + id_tipo + " "
								+ "LIKE stage_point_adjudicadas.adjudicadas_tipo1";

						ciclo_tipos++;
						consulta.executeUpdate(delete_combis);
					}
				} catch (Exception error) {
					Utils.printAdjErr("Error Combinando filtros: " + error.getMessage());
				} finally {
					DBCnx.close(consulta);
					DBCnx.close(conexion);
				}
			}
			Date fin = new Date();
			double tiempo_total = (fin.getTime() - ini.getTime()) / 1000 / 60;
			Utils.printAdjDeb("Tiempo generación filtros: " + tiempo_total);
		} catch (Exception error) {
			Utils.printAdjErr(error.getMessage());
			Utils.printAdjErr("error -------> obteniendo Adj: " + cant);
		} finally {
			ex.shutdownNow();
		}
		return Response.status(200).entity(response.toString()).build();
	}

	@GET
	@Path("/NormalizaAdj/")
	@Produces("application/json; charset=UTF-8")
	public Response NormalizarLicitacionesAdj(@Context final HttpServletRequest request) throws JSONException {
		JSONObject response = new JSONObject();
		Utils.printAdjDeb("Normalizando");

		Statement consulta = null;
		// ResultSet resultados = null;
		Connection conexion = null;
		Date ini = new Date();
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();
			String insert = "INSERT INTO `stage_point_adjudicadas`.`adjudicadas_mes_normalizada` (`tipo`,`fechaAdjudicada`,"
					+ "`mes`,`anho`,`numeroLicitacion`,`rutComprador`,`unidadCompraEstandarizada`,"
					+ "`razonSocialCompradorEstandarizada`,`comuna`,`region`,`segmentoComprador`,"
					+ "`segmentoCompradorMercado`,`rutProveedor`,`razonSocialProveedorEstandarizada`,"
					+ "`proveedorAsociado`,`item`,`cantidad`,`cantidadCorregida1`,`cantidadCorregida2`,"
					+ "`moneda`,"
					+ "`precioUnitario`,`precioCorregido1`,`precioCorregido2`,`valorTotal`, `estado`, "
					+ "`mercado`,`categoria`,`subcategoria1`,`subcategoria2`,`subcategoria3`,"
					+ "`subcategoria4`,`subcategoria5`,`subcategoria6`,`subcategoria7`,`subcategoria8`,"
					+ "`observacion`) SELECT tipo.`id` tipo,adj.`fechaAdjudicada`, month(adj.`fechaAdjudicada`), "
					+ "year(adj.`fechaAdjudicada`), adj.`numeroLicitacion`, rutc.id rutComprador, "
					+ "uni.id unidadCompraEstandarizada, razc.id razonSocialCompradorEstandarizada, "
					+ "com.id comuna, mcom.region, seg.id segmentoComprador, segm.id segmentoCompradorMercado, "
					+ "rutp.id `rutProveedor`, razp.id razonSocialProveedorEstandarizada, pasoc.id provAsoc, "
					+ "adj.item, adj.`cantidad`, adj.`cantidadCorregida1`,adj.`cantidadCorregida1`, "
					+ "adj.`moneda`, adj.`precioUnitario`, adj.`precioCorregido1`,adj.`precioCorregido1`, "
					+ "adj.`valorTotal`, est.id estado, mer.id `mercado`, cat.id `categoria`, "
					+ "scat1.id `subcategoria1`, scat2.id `subcategoria2`, scat3.id `subcategoria3`, "
					+ "scat4.id `subcategori4`, scat5.id `subcategoria5`, scat6.id `subcategoria6`, "
					+ "scat7.id `subcategoria7`, scat8.id `subcategoria8`, obs.id observacion "
					+ "FROM stage_point_adjudicadas.adjudicadas_aux adj "
					+ "LEFT JOIN stage_point_adjudicadas.maestros_compradores_gnral mcom ON mcom.rutComprador = adj.rutComprador "
					+ "LEFT JOIN stage_point_adjudicadas.maestros_compradores_mercado mcomM ON mcomM.rutComprador = adj.rutComprador "
					+ "LEFT JOIN stage_point_adjudicadas.maestros_proveedores mprov ON mprov.rutProveedor = adj.rutProveedor "
					+ "LEFT JOIN stage_point_adjudicadas.filtro_mercado mer ON mer.mercado = adj.mercado "
					+ "LEFT JOIN stage_point_adjudicadas.filtro_tipolicitacion tipo ON tipo.tipo_licitacion = adj.tipo "
					+ "LEFT JOIN stage_point_adjudicadas.filtro_estado est ON est.estado = adj.estado "
					+ "LEFT JOIN stage_point_adjudicadas.filtro_observacion obs ON obs.observacion = adj.observacion "
					+ "LEFT JOIN stage_point_adjudicadas.filtro_rutcomprador rutc ON rutc.rutComprador = adj.rutComprador "
					+ "LEFT JOIN stage_point_adjudicadas.filtro_rutproveedor rutp ON rutp.rutProveedor = adj.rutProveedor "
					+ "LEFT JOIN stage_point_adjudicadas.filtro_unidad_compra uni ON uni.unidad_compra = mcom.unidadCompra "
					+ "LEFT JOIN stage_point_adjudicadas.filtro_categoria cat ON cat.categoria = adj.categoria "
					+ "LEFT JOIN stage_point_adjudicadas.filtro_comunas com ON com.comuna = adj.comuna "
					+ "lEFT JOIN stage_point_adjudicadas.filtro_proveedor_asociado pasoc ON pasoc.proveedor_asociado = adj.proveedorAsociado "
					+ "LEFT JOIN stage_point_adjudicadas.filtro_razonsocialcomprador razc ON razc.razonSocialComprador = mcom.razonSocialComprador "
					+ "LEFT JOIN stage_point_adjudicadas.filtro_razonsocialproveedor razp ON razp.razonSocialProveedor = mprov.razonSocialProveedor "
					+ "LEFT JOIN stage_point_adjudicadas.filtro_segmentocomprador seg ON seg.segmento = mcom.segmentoComprador "
					+ "LEFT JOIN stage_point_adjudicadas.filtro_segmentocompradormercado segm ON mcomM.mercado = adj.mercado AND segm.segmento_mercado = mcomM.segmentoCompradorMercado "
					+ "LEFT JOIN stage_point_adjudicadas.filtro_subcategoria1 scat1 ON scat1.subCategoria = adj.Subcategoria1 "
					+ "LEFT JOIN stage_point_adjudicadas.filtro_subcategoria2 scat2 ON scat2.subCategoria = adj.Subcategoria2 "
					+ "LEFT JOIN stage_point_adjudicadas.filtro_subcategoria3 scat3 ON scat3.subCategoria = adj.Subcategoria3 "
					+ "LEFT JOIN stage_point_adjudicadas.filtro_subcategoria4 scat4 ON scat4.subCategoria = adj.Subcategoria4 "
					+ "LEFT JOIN stage_point_adjudicadas.filtro_subcategoria5 scat5 ON scat5.subCategoria = adj.Subcategoria5 "
					+ "LEFT JOIN stage_point_adjudicadas.filtro_subcategoria6 scat6 ON scat6.subCategoria = adj.Subcategoria6 "
					+ "LEFT JOIN stage_point_adjudicadas.filtro_subcategoria7 scat7 ON scat7.subCategoria = adj.Subcategoria7 "
					+ "LEFT JOIN stage_point_adjudicadas.filtro_subcategoria8 scat8 ON scat8.subCategoria = adj.Subcategoria8 ";
			System.out.println(insert);
			consulta.executeUpdate(insert);
			
		} catch (Exception error) {
			Utils.printAdjErr(error.getLocalizedMessage());
		} finally {
			DBCnx.close(consulta);
			DBCnx.close(conexion);
		}
		Date fin = new Date();
		double tiempo_total = (fin.getTime() - ini.getTime()) / 1000 / 60;
		Utils.printAdjDeb("Tiempo normalización: " + tiempo_total);
		return Response.status(200).entity(response.toString()).build();
	}
	
	@GET
	@Path("/TraspasoHistorico/")
	@Produces("application/json; charset=UTF-8")
	public Response TraspasoHistorico(@Context final HttpServletRequest request) throws JSONException {
		JSONObject response = new JSONObject();
		Utils.printAdjDeb("Traspasando a Historico");

		Statement consulta = null;
		Connection conexion = null;
		Date ini = new Date();
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();
			String insert = "INSERT INTO `stage_point_adjudicadas`.`adjudicadas` (`tipo`,`fechaAdjudicada`,"
					+ "`mes`,`anho`,`numeroLicitacion`,`rutComprador`,`unidadCompraEstandarizada`,"
					+ "`razonSocialCompradorEstandarizada`,`comuna`,`region`,`segmentoComprador`,"
					+ "`segmentoCompradorMercado`,`rutProveedor`,`razonSocialProveedorEstandarizada`,"
					+ "`proveedorAsociado`,`item`,`cantidad`,`cantidadCorregida1`,`cantidadCorregida2`,"
					+ "`moneda`,"
					+ "`precioUnitario`,`precioCorregido1`,`precioCorregido2`,`valorTotal`, `estado`, "
					+ "`mercado`,`categoria`,`subcategoria1`,`subcategoria2`,`subcategoria3`,"
					+ "`subcategoria4`,`subcategoria5`,`subcategoria6`,`subcategoria7`,`subcategoria8`,"
					+ "`observacion`, created_at) SELECT `tipo`, `fechaAdjudicada`, `mes`, `anho`, `numeroLicitacion`, "
					+ "`rutComprador`, `unidadCompraEstandarizada`, `razonSocialCompradorEstandarizada`, "
					+ "`comuna`, `region`, `segmentoComprador`, `segmentoCompradorMercado`, `rutProveedor`, "
					+ "`razonSocialProveedorEstandarizada`, `proveedorAsociado`, `item`, `cantidad`, "
					+ "`cantidadCorregida1`, `cantidadCorregida2`, `moneda`, `precioUnitario`, `precioCorregido1`, "
					+ "`precioCorregido2`, `valorTotal`, `estado`, `mercado`, `categoria`, `subcategoria1`, "
					+ "`subcategoria2`, `subcategoria3`, `subcategoria4`, `subcategoria5`, `subcategoria6`, "
					+ "`subcategoria7`, `subcategoria8`, `observacion`, now() FROM stage_point_adjudicadas.`adjudicadas_mes_normalizada`";
			System.out.println(insert);
			consulta.executeUpdate(insert);
			
		} catch (Exception error) {
			Utils.printAdjErr(error.getLocalizedMessage());
		} finally {
			DBCnx.close(consulta);
			DBCnx.close(conexion);
		}
		Date fin = new Date();
		double tiempo_total = (fin.getTime() - ini.getTime()) / 1000 / 60;
		Utils.printAdjDeb("Tiempo traspaso a histórico: " + tiempo_total);
		return Response.status(200).entity(response.toString()).build();
	}
	
	@GET
	@Path("/TablasTiposLic/")
	@Produces("application/json; charset=UTF-8")
	public Response generar_tablas_lic(@Context final HttpServletRequest request) throws JSONException {
		JSONObject response = new JSONObject();

		Statement consulta = null;
		Statement insert_datos = null;
		Connection conexion = null;
		ResultSet result = null;
		Date ini = new Date();
		try {
			
			String get_tipos_lic = "SELECT id FROM stage_point_adjudicadas.filtro_tipolicitacion;";
			
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();
			insert_datos = conexion.createStatement();
			result = consulta.executeQuery(get_tipos_lic);
			
			String select = "SELECT `tipo`, `fechaAdjudicada`, `mes`, `anho`, `numeroLicitacion`, `rutComprador`, "
					+ "`unidadCompraEstandarizada`, `razonSocialCompradorEstandarizada`, `comuna`, `region`, "
					+ "`segmentoComprador`, `segmentoCompradorMercado`, `rutProveedor`, `razonSocialProveedorEstandarizada`, "
					+ "`proveedorAsociado`, `item`, `cantidad`, `cantidadCorregida1`, `cantidadCorregida2`, "
					+ "`unidadMedida`, `unidadMedidaCorregida1`, `unidadMedidaCorregida2`, `moneda`, `precioUnitario`, "
					+ "`precioCorregido1`, `precioCorregido2`, `valorTotal`, `estado`, `mercado`, `categoria`, "
					+ "`subcategoria1`, `subcategoria2`, `subcategoria3`, `subcategoria4`, `subcategoria5`, "
					+ "`subcategoria6`, `subcategoria7`, `subcategoria8`, `observacion`, now() FROM stage_point_adjudicadas.adjudicadas ";
			
			String insert_into = " (`tipo`, `fechaAdjudicada`, `mes`, `anho`, `numeroLicitacion`, `rutComprador`, "
					+ "`unidadCompraEstandarizada`, `razonSocialCompradorEstandarizada`, `comuna`, `region`, "
					+ "`segmentoComprador`, `segmentoCompradorMercado`, `rutProveedor`, `razonSocialProveedorEstandarizada`, "
					+ "`proveedorAsociado`, `item`, `cantidad`, `cantidadCorregida1`, `cantidadCorregida2`, "
					+ "`unidadMedida`, `unidadMedidaCorregida1`, `unidadMedidaCorregida2`, `moneda`, `precioUnitario`, "
					+ "`precioCorregido1`, `precioCorregido2`, `valorTotal`, `estado`, `mercado`, `categoria`, "
					+ "`subcategoria1`, `subcategoria2`, `subcategoria3`, `subcategoria4`, `subcategoria5`, "
					+ "`subcategoria6`, `subcategoria7`, `subcategoria8`, `observacion`, `created_at`)";
			while(result.next()){
				int id_tipo = result.getInt("id");
				String where = " WHERE tipo = "+id_tipo;
				String query = " INSERT INTO stage_point_adjudicadas.adjudicadas_tipo"+id_tipo + insert_into + select + where;
				System.out.println(query);
				insert_datos.executeUpdate(query);
				
			}
		} catch (Exception error) {
			error.printStackTrace();
			Utils.printAdjErr(error.getLocalizedMessage());
		} finally {
			DBCnx.close(consulta);
			DBCnx.close(conexion);
		}
		Date fin = new Date();
		double tiempo_total = (fin.getTime() - ini.getTime()) / 1000 / 60;
		Utils.printAdjDeb("Tiempo generación tablas tipos licitaciones: " + tiempo_total);
		return Response.status(200).entity(response.toString()).build();
	}

	@GET
	@Path("/CombiFiltrosAdj/")
	@Produces("application/json; charset=UTF-8")
	public Response generar_combi_filtros(@Context final HttpServletRequest request) throws JSONException {
		JSONObject response = new JSONObject();

		Statement consulta = null;
		Connection conexion = null;
		String query_combi_filtros = "INSERT INTO `stage_point_adjudicadas`.`combi_filtros` (`id_fil_tipo`,"
				+ "`id_fil_rutc`,`id_fil_uni`,`id_fil_razc`,`id_fil_comu`,`id_fil_region`,"
				+ "`id_fil_segc`,`id_fil_segcm`, `id_fil_rutp`,`id_fil_razp`,`id_fil_pasoc`,"
				+ "`id_fil_estado`,`id_fil_mer`,`id_fil_cat`,`id_fil_scat1`,`id_fil_scat2`, "
				+ "`id_fil_scat3`,`id_fil_scat4`,`id_fil_scat5`,`id_fil_scat6`,`id_fil_scat7`,"
				+ "`id_fil_scat8`,`id_fil_obs`) SELECT tipo,rutComprador,unidadCompraEstandarizada,"
				+ "razonSocialCompradorEstandarizada,comuna,region,segmentoComprador,segmentoCompradorMercado,"
				+ "rutProveedor,razonSocialProveedorEstandarizada,proveedorAsociado,estado,mercado,"
				+ "categoria,subcategoria1,subcategoria2,subcategoria3,subcategoria4,subcategoria5,"
				+ "subcategoria6,subcategoria7,subcategoria8,observacion FROM "
				+ "stage_point_adjudicadas.adjudicadas GROUP BY tipo,rutComprador,unidadCompraEstandarizada,"
				+ "razonSocialCompradorEstandarizada,comuna,region,segmentoComprador,"
				+ "segmentoCompradorMercado,rutProveedor,razonSocialProveedorEstandarizada,"
				+ "proveedorAsociado,estado,mercado,categoria,subcategoria1,subcategoria2,subcategoria3,"
				+ "subcategoria4,subcategoria5,subcategoria6,subcategoria7,subcategoria8,observacion";
		
		String delete_combi_filtros = "DELETE FROM stage_point_adjudicadas.combi_filtros;";
		
		Date ini = new Date();
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();
			Utils.printAdjDeb("Borrando Combi Filtros");
			consulta.executeUpdate(delete_combi_filtros);
			Utils.printAdjDeb("Generando Combi Filtros");
			consulta.executeUpdate(query_combi_filtros);
		} catch (Exception error) {
			Utils.printAdjErr(error.getLocalizedMessage());
		} finally {
			DBCnx.close(consulta);
			DBCnx.close(conexion);
		}
		Date fin = new Date();
		double tiempo_total = (fin.getTime() - ini.getTime()) / 1000 / 60;
		Utils.printAdjDeb("Tiempo generación combi filtros: " + tiempo_total);
		return Response.status(200).entity(response.toString()).build();
	}

	@GET
	@Path("/LicProveedor/")
	@Produces("application/json; charset=UTF-8")
	public Response licitaciones_proveedor(@Context final HttpServletRequest request) throws JSONException {
		JSONObject response = new JSONObject();

		Statement consulta = null;
		Connection conexion = null;

		String fecha_fin = DataManager.getLastDate("stage_point_adjudicadas");
		Date ini = new Date();
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();

			Utils.printAdjDeb("Borrando licitaciones_proveedor.....");
			String delete_combis = "DELETE FROM stage_point_adjudicadas.licitaciones_proveedor;";
			consulta.executeUpdate(delete_combis);

			Utils.printAdjDeb("Generando nuevo precálculo licitaciones_proveedor.....");
			String query = "INSERT INTO `stage_point_adjudicadas`.`licitaciones_proveedor` (`id_proveedor`,"
					+ "`nroLicitacion`,`item`,`valorTotal`,`precio`,`cantidad`,`estado`,`mercado`,`categoria`,"
					+ "`subcategoria1`,`subcategoria2`,`subcategoria3`,`subcategoria4`,`subcategoria5`,"
					+ "`subcategoria6`,`subcategoria7`,`subcategoria8`) SELECT proveedorAsociado,"
					+ "numeroLicitacion,item,SUM(valorTotal),SUM(precioCorregido1),SUM(cantidadCorregida1),"
					+ "estado,mercado,categoria,subcategoria1,subcategoria2,subcategoria3,subcategoria4,"
					+ "subcategoria5,subcategoria6,subcategoria7,subcategoria8 FROM stage_point_adjudicadas.adjudicadas "
					+ "WHERE fechaAdjudicada > date_sub('" + fecha_fin + "', INTERVAL 1 YEAR) AND "
					+ "fechaAdjudicada < DATE_ADD('" + fecha_fin
					+ "',INTERVAL 1 DAY) GROUP BY proveedorAsociado , numeroLicitacion , item";
			Utils.printAdjDeb(query);
			consulta.executeUpdate(query);
		} catch (Exception error) {
			Utils.printAdjErr("Error Borrando licitaciones_proveedor: " + error.getMessage());
		} finally {
			DBCnx.close(consulta);
			DBCnx.close(conexion);
		}
		Date fin = new Date();
		double tiempo_total = (fin.getTime() - ini.getTime()) / 1000 / 60;
		Utils.printAdjDeb("Tiempo precálculo licitaciones_proveedor: " + tiempo_total);

		return Response.status(200).entity(response.toString()).build();
	}

	@GET
	@Path("/UnivLicitaciones/")
	@Produces("application/json; charset=UTF-8")
	public Response universo_licitaciones(@Context final HttpServletRequest request) throws JSONException {
		JSONObject response = new JSONObject();

		Statement consulta = null;
		Connection conexion = null;

		Date ini = new Date();
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();

			Utils.printAdjDeb("Borrando universo_licitaciones.....");
			String delete_combis = "DELETE FROM stage_point_adjudicadas.universo_licitaciones;";
			consulta.executeUpdate(delete_combis);

			Utils.printAdjDeb("Generando nuevo precálculo universo_licitaciones.....");
			String query = "INSERT INTO `stage_point_adjudicadas`.`universo_licitaciones` (`numeroLicitacion`,"
					+ "`fecha`) SELECT numeroLicitacion,CONCAT(anho, '-', mes, '-01') fecha FROM "
					+ "stage_point_adjudicadas.adjudicadas adj GROUP BY numeroLicitacion, mes, anho";
			Utils.printAdjDeb(query);
			consulta.executeUpdate(query);
		} catch (Exception error) {
			Utils.printAdjErr("Error Borrando universo_licitaciones: " + error.getMessage());
		} finally {
			DBCnx.close(consulta);
			DBCnx.close(conexion);
		}
		Date fin = new Date();
		double tiempo_total = (fin.getTime() - ini.getTime()) / 1000 / 60;
		Utils.printAdjDeb("Tiempo precálculo universo_licitaciones: " + tiempo_total);

		return Response.status(200).entity(response.toString()).build();
	}

	@GET
	@Path("/LicPostuladasProveedor/")
	@Produces("application/json; charset=UTF-8")
	public Response licitaciones_postuladas_proveedor(@Context final HttpServletRequest request) throws JSONException {
		JSONObject response = new JSONObject();

		Statement consulta = null;
		Connection conexion = null;

		Date ini = new Date();
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();

			Utils.printAdjDeb("Borrando licitaciones_postuladas_proveedor.....");
			String delete_combis = "DELETE FROM stage_point_adjudicadas.licitaciones_postuladas_proveedor;";
			consulta.executeUpdate(delete_combis);

			Utils.printAdjDeb("Generando nuevo precálculo licitaciones_postuladas_proveedor.....");
			String query = "INSERT INTO `stage_point_adjudicadas`.`licitaciones_postuladas_proveedor` "
					+ "(`id_proveedor`,`cant_lic`, fecha) SELECT proveedorAsociado,COUNT(numeroLicitacion) c,"
					+ "CONCAT(anho, '-', mes, '-01') fecha FROM (SELECT proveedorAsociado, numeroLicitacion, "
					+ "mes, anho FROM stage_point_adjudicadas.adjudicadas adj GROUP BY proveedorAsociado, "
					+ "numeroLicitacion,mes,anho) AS t GROUP BY t.proveedorAsociado,mes,anho";
			Utils.printAdjDeb(query);
			consulta.executeUpdate(query);
		} catch (Exception error) {
			Utils.printAdjErr("Error Borrando licitaciones_postuladas_proveedor: " + error.getMessage());
		} finally {
			DBCnx.close(consulta);
			DBCnx.close(conexion);
		}
		Date fin = new Date();
		double tiempo_total = (fin.getTime() - ini.getTime()) / 1000 / 60;
		Utils.printAdjDeb("Tiempo precálculo licitaciones_postuladas_proveedor: " + tiempo_total);

		return Response.status(200).entity(response.toString()).build();
	}

	@GET
	@Path("/trasaparFavoritosInfo/")
	@Produces("application/json; charset=UTF-8")
	public Response trasaparFavoritosInfo(@Context final HttpServletRequest request) throws JSONException {
		JSONObject resp = new JSONObject();

		String response = DataManager.traspasarFavoritos("point_adjudicadas");
		resp.put("resp", response);

		return Response.status(200).entity(resp.toString()).build();
	}

	private static ArrayList<Integer> getTiposLic() {
		ArrayList<Integer> id_tipos = new ArrayList<Integer>();
		PreparedStatement consulta = null;
		ResultSet resultados = null;
		Connection conexion = null;

		try {

			String query = "SELECT `id` FROM stage_point_adjudicadas.`filtro_tipolicitacion`";
			conexion = DBCnx.conexion();
			consulta = conexion.prepareStatement(query);

			resultados = consulta.executeQuery();

			while (resultados.next()) {
				id_tipos.add(resultados.getInt("id"));
			}
		} catch (Exception error) {
			error.printStackTrace();
		} finally {
			DBCnx.closeAll(resultados, consulta, conexion);
		}

		return id_tipos;
	}

	private static String get_where_mercados(String request) {
		String where = "";
		String valores_where = "";

		String mercados_alias[] = request.replace("[", "").replace("]", "").replace("\"", "").split(",");

		int cant_mercados = mercados_alias.length;
		for (int ciclo = 0; ciclo < cant_mercados; ciclo++) {
			switch (mercados_alias[ciclo]) {
			case "med_norm":
				if (valores_where.equals("")) {
					valores_where += "'Medicamentos'";
				} else {
					valores_where += ",'Medicamentos'";
				}
				break;
			case "nut_norm":
				if (valores_where.equals("")) {
					valores_where += "'Formulas Nutricionales'";
				} else {
					valores_where += ",'Formulas Nutricionales'";
				}
				break;
			case "ofi_norm":
				if (valores_where.equals("")) {
					valores_where += "'Suministros de Oficina'";
				} else {
					valores_where += ",'Suministros de Oficina'";
				}
				break;
			case "tec_norm":
				if (valores_where.equals("")) {
					valores_where += "'Tecnologia'";
				} else {
					valores_where += ",'Tecnologia'";
				}
				break;
			}
		}

		if (!valores_where.equals("")) {
			where = "WHERE adj.mercado IN (" + valores_where + ")";
		}

		return where;
	}
}
