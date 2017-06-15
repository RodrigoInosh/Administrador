package cl.techk.ordenes.carga;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.concurrent.ExecutorService;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONException;
import org.json.JSONObject;

import cl.techk.ext.database.DBCnx;
import cl.techk.ext.database.DataManager;
import cl.techk.ext.utils.Utils;

/**
 * Servlet implementation class CargaDatos2
 */
@Path("/Normalizar")
public class Normalizar {
	static ExecutorService ex = null;

	@GET
	@Path("/insert_fecha/")
	@Produces("application/json; charset=UTF-8")
	public Response insert_fecha(@Context final HttpServletRequest request) throws JSONException {

		JSONObject response = new JSONObject();
		String req = request.getParameter("info");
		String where_mercado = get_where_mercados(req);

		try {
			insertLastDate(where_mercado);
			response.put("resp", "Fecha Fin Insertada");
			return Response.status(200).entity(response.toString()).build();
		} catch (Exception e) {
			Utils.printOrdErr("Error insert last date:" + e.getMessage());
			response.put("resp", "Error Insertando Ultima Fecha");
			return Response.status(201).entity(response.toString()).build();
		}
	}

	@GET
	@Path("/gen_filtros/")
	@Produces("application/json; charset=UTF-8")
	public Response gen_filtros(@Context final HttpServletRequest request) throws JSONException {

		String req = request.getParameter("info");
		String where_mercado = get_where_mercados(req);

		try {
			generarFiltros(where_mercado);
			return Response.status(200).entity("Data Insertada").build();
		} catch (Exception e) {
			Utils.printOrdErr("Error generando filtros:" + e.getMessage());
			return Response.status(201).entity("Error generando filtros").build();
		}
	}

	@GET
	@Path("/normalizar/")
	@Produces("application/json; charset=UTF-8")
	public Response normalizar(@Context final HttpServletRequest request) throws JSONException {

		String req = request.getParameter("info");
		String where_mercado = get_where_mercados(req);
		String where_mercado_normalizado = get_where_mercados_normalizados(req, "mercado");
		try {
			NormalizarOrdenesCompra(where_mercado, where_mercado_normalizado);
			return Response.status(200).entity("Data Insertada").build();
		} catch (Exception e) {
			Utils.printOrdErr("Error normalizando:" + e.getMessage());
			return Response.status(201).entity("Error normalizando").build();
		}
	}

	@GET
	@Path("/validate_normalizacion/")
	@Produces("application/json; charset=UTF-8")
	public Response validate_normalizacion(@Context final HttpServletRequest request) throws JSONException {

		String req = request.getParameter("info");
		String[] mercados = get_mercados(req);

		try {
			validarDatosNormalizados(mercados);
			return Response.status(200).entity("Validación Completada").build();
		} catch (Exception e) {
			Utils.printOrdErr("Error gerando tabla gráfico:" + e.getMessage());
			return Response.status(201).entity("Error generando tabla gráfico").build();
		}
	}

	@GET
	@Path("/traspasarHistorico/")
	@Produces("application/json; charset=UTF-8")
	public Response traspasarHistorico(@Context final HttpServletRequest request) throws JSONException {

		String req = request.getParameter("info");
		String where_mercado = get_where_mercados(req);
		String where_mercado_normalizado = get_where_mercados_normalizados(req, "mercado");
		try {
			traspasarHistorico(where_mercado, where_mercado_normalizado);
			return Response.status(200).entity("Traspaso Completado").build();
		} catch (Exception e) {
			Utils.printOrdErr("Error traspando a historico:" + e.getMessage());
			return Response.status(201).entity("Error traspando a historico").build();
		}
	}

	@GET
	@Path("/gen_combi_filtros/")
	@Produces("application/json; charset=UTF-8")
	public Response gen_combi_filtros(@Context final HttpServletRequest request) throws JSONException {

		String req = request.getParameter("info");
		String where_mercado = get_where_mercados_normalizados(req, "mercado");
		String where_mercado_combi = get_where_mercados_normalizados(req, "id_fil_mer");
		try {
			generarCombinatoriaFiltros(where_mercado, where_mercado_combi);
			return Response.status(200).entity("Data Insertada").build();
		} catch (Exception e) {
			Utils.printOrdErr("Error gerando combi_filtros:" + e.getMessage());
			return Response.status(201).entity("Error generando combi_filtros").build();
		}

	}

	@GET
	@Path("/combi_valores/")
	@Produces("application/json; charset=UTF-8")
	public Response combi_valores(@Context final HttpServletRequest request) throws JSONException {

		String req = request.getParameter("info");
		String where_mercado = get_where_mercados_normalizados(req, "mercado");
		String where_mercado_combi = get_where_mercados_normalizados(req, "id_fil_mer");
		try {
			generarCombinatoriaValores(where_mercado, where_mercado_combi);
			return Response.status(200).entity("Data Insertada").build();
		} catch (Exception e) {
			Utils.printOrdErr("Error gerando combi_valores:" + e.getMessage());
			return Response.status(201).entity("Error generando combi_valores").build();
		}
	}

	@GET
	@Path("/combi_valores_mes/")
	@Produces("application/json; charset=UTF-8")
	public Response combi_valores_mes(@Context final HttpServletRequest request) throws JSONException {
		String req = request.getParameter("info");
		String where_mercado = get_where_mercados_normalizados(req, "id_fil_mer");
		try {
			insertCombiValoresMes(where_mercado);
			return Response.status(200).entity("Data Insertada").build();
		} catch (Exception e) {
			Utils.printOrdErr("Error gerando combi_valores:" + e.getMessage());
			return Response.status(201).entity("Error generando combi_valores").build();
		}
	}

	@GET
	@Path("/combi_valores_ytd/")
	@Produces("application/json; charset=UTF-8")
	public Response combi_valores_ytd(@Context final HttpServletRequest request) throws JSONException {

		String req = request.getParameter("info");
		String where_mercado = get_where_mercados_normalizados(req, "id_fil_mer");
		try {
			insertCombiValoresYTD(where_mercado);
			return Response.status(200).entity("Data Insertada").build();
		} catch (Exception e) {
			Utils.printOrdErr("Error gerando combi_valores:" + e.getMessage());
			return Response.status(201).entity("Error generando combi_valores").build();
		}
	}

	@GET
	@Path("/graf/")
	@Produces("application/json; charset=UTF-8")
	public Response graf(@Context final HttpServletRequest request) throws JSONException {

		String req = request.getParameter("info");
		String where_mercado = get_where_mercados_normalizados(req, "mercado");
		String where_mercado_combi = get_where_mercados_normalizados(req, "id_mer");
		try {
			generarGraficoPrecalculado(where_mercado, where_mercado_combi);
			return Response.status(200).entity("Data Insertada").build();
		} catch (Exception e) {
			Utils.printOrdErr("Error gerando tabla gráfico:" + e.getMessage());
			return Response.status(201).entity("Error generando tabla gráfico").build();
		}
	}

	@GET
	@Path("/trasaparFiltrosMemoria/")
	@Produces("application/json; charset=UTF-8")
	public Response trasaparFiltrosMemoria(@Context final HttpServletRequest request) throws JSONException {

		String req = request.getParameter("info");
		String where_mercado = get_where_mercados_normalizados(req, "id_fil_mer");
		try {
			traspasar_memoria(where_mercado);
			return Response.status(200).entity("Data Insertada").build();
		} catch (Exception e) {
			Utils.printOrdErr("Error gerando tabla gráfico:" + e.getMessage());
			return Response.status(201).entity("Error generando tabla gráfico").build();
		}
	}

	@GET
	@Path("/trasaparFavoritosInfo/")
	@Produces("application/json; charset=UTF-8")
	public Response trasaparFavoritosInfo(@Context final HttpServletRequest request) throws JSONException {
		JSONObject resp = new JSONObject();
		
		String response = DataManager.traspasarFavoritos("point_ordenes");
		resp.put("resp", response);
		
		return Response.status(200).entity(resp.toString()).build();
//		Statement consulta = null;
//		Connection conexion = null;
//
//		Date ini = new Date();
//		try {
//			conexion = DBCnx.conexion();
//			consulta = conexion.createStatement();
//
//			Utils.printOrdDeb("Borrando datos de favoritos");
//			String query = "DELETE FROM stage_point_ordenes.favorito_datos;";
//			consulta.executeUpdate(query);
//			Utils.printOrdDeb("Traspasando datos de favoritos");
//			String traspaso_fav_datos = "INSERT INTO stage_point_ordenes.`favorito_datos`(`id`, `id_fav`, `id_usuario`, "
//					+ "`alias`, `id_dato`, `dato`) SELECT `id`, `id_fav`, `id_usuario`, `alias`, `id_dato`, `dato` FROM "
//					+ "point_ordenes.`favorito_datos`;";
//			consulta.executeUpdate(traspaso_fav_datos);
//			
//			Utils.printOrdDeb("Borrando favoritos de usuarios");
//			String delete_user_favs = "DELETE FROM stage_point_ordenes.`usuario_favoritos`;";
//			consulta.executeUpdate(delete_user_favs);
//			
//			Utils.printOrdDeb("Traspasando favoritos de usuarios");
//			String traspaso_user_favs = "INSERT INTO stage_point_ordenes.`usuario_favoritos`(`id`, `id_usr`, `nombre`) "
//					+ "SELECT `id`, `id_usr`, `nombre` FROM point_ordenes.`usuario_favoritos`";
//			consulta.executeUpdate(traspaso_user_favs);
//			
//			Utils.printOrdDeb("Borrando límites de usuarios");
//			String delete_user_limits = "DELETE FROM stage_point_ordenes.limites;";
//			consulta.executeUpdate(delete_user_limits);
//
//			Utils.printOrdDeb("Traspasando límites de usuarios");
//			String traspaso_user_limits = "INSERT INTO stage_point_ordenes.`limites`(`id`, `id_usuario`, `nombre_tabla_filtro`, `id_dato`, `id_mer`) "
//					+ "SELECT `id`, `id_usuario`, `nombre_tabla_filtro`, `id_dato`, `id_mer` FROM point_ordenes.`limites`";
//			consulta.executeUpdate(traspaso_user_limits);
//
//			Utils.printOrdDeb("Borrando mercados de usuarios");
//			String delete_user_mercados = "DELETE FROM stage_point_ordenes.mercado_usuario;";
//			consulta.executeUpdate(delete_user_mercados);
//
//			Utils.printOrdDeb("Traspasando mercados de usuarios");
//			String traspaso_user_mercados = "INSERT INTO stage_point_ordenes.`mercado_usuario`(`id`, `id_usr`, `id_clte`, `id_mer`) "
//					+ "SELECT `id`, `id_usr`, `id_clte`, `id_mer` FROM point_ordenes.`mercado_usuario`";
//			consulta.executeUpdate(traspaso_user_mercados);
//			
//			Date fin = new Date();
//			double tiempo_total = (fin.getTime() - ini.getTime()) / 1000 / 60;
//			Utils.printOrdDeb("Tiempo Traspaso de favoritos: " + tiempo_total);
//			return Response.status(200).entity("Data Insertada").build();
//		} catch (Exception e) {
//			Utils.printOrdErr("Error Traspasando a favoritos:" + e.getMessage());
//			return Response.status(201).entity("Error trapasando favoritos").build();
//		}
	}

	private void insertLastDate(String where_mercado) {
		Statement consulta = null;
		ResultSet resultados = null;
		Connection conexion = null;

		Date ini = new Date();
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();

			String trucate_table = "TRUNCATE TABLE stage_point_ordenes.ultima_fecha;";
			consulta.executeUpdate(trucate_table);

			String query = "SELECT LAST_DAY(MAX(fechaEnvio)) fechaEnvio FROM stage_point_ordenes.ordenes_mes_act ord "
					+ where_mercado + ";";
			Utils.printOrdDeb(query);
			resultados = consulta.executeQuery(query);
			while (resultados.next()) {
				Date fecha = resultados.getDate("fechaEnvio");
				String insert = "INSERT INTO stage_point_ordenes.ultima_fecha (id, fecha_fin) VALUES (1,\"" + fecha
						+ "\")";
				Utils.printOrdDeb("Insertando Última fecha");

				DataManager.insertData2(insert, "ord");
			}
			Date fin = new Date();
			double tiempo_total = (fin.getTime() - ini.getTime()) / 1000 / 60;
			Utils.printOrdDeb("Tiempo Ingresando Fecha: " + tiempo_total);
		} catch (Exception error) {
			Utils.printOrdErr("Error Insertando Ultima Fecha: " + error.getMessage());
		} finally {
			DBCnx.closeAll(resultados, consulta, conexion);
		}
	}

	private void generarFiltros(String where_mercado) {
		Statement consulta = null;
		ResultSet resultados = null;
		Connection conexion = null;
		Date ini = new Date();
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();
			String query = "SELECT proveedorAsociado FROM stage_point_ordenes.ordenes_mes_act ord " + where_mercado
					+ " GROUP BY proveedorAsociado";

			resultados = consulta.executeQuery(query);
			while (resultados.next()) {
				String insert = "INSERT INTO stage_point_ordenes.filtro_proveedor_asociado (proveedor_asociado) "
						+ "VALUES (\"" + resultados.getString("proveedorAsociado") + "\")";
				Utils.printOrdDeb("Insertando en filtro proveedor asociado");

				DataManager.insertData2(insert, "ord");
			}
			DBCnx.close(resultados);

			query = "SELECT origenOCCorregido FROM stage_point_ordenes.ordenes_mes_act ord " + where_mercado
					+ " GROUP BY origenOCCorregido";

			resultados = consulta.executeQuery(query);
			while (resultados.next()) {
				String insert = "INSERT INTO stage_point_ordenes.filtro_origenoc (origenOC) " + "VALUES (\""
						+ resultados.getString("origenOCCorregido") + "\")";
				Utils.printOrdDeb("Insertando en filtro origenOC");

				DataManager.insertData2(insert, "ord");
			}
			Date fin = new Date();
			double tiempo_total = (fin.getTime() - ini.getTime()) / 1000 / 60;
			Utils.printOrdDeb("Tiempo Generando Filtros: " + tiempo_total);
		} catch (Exception error) {
			Utils.printOrdErr(error.getMessage());
			Utils.printOrdErr("Error Generando Filtros: " + error.getMessage());
		} finally {
			DBCnx.closeAll(resultados, consulta, conexion);
		}
	}

	private void NormalizarOrdenesCompra(String where_mercado, String where_mercado_normalizado) {
		Statement consulta = null;
		Connection conexion = null;
		Date ini = new Date();
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();

			Utils.printOrdDeb("Borrando datos normalizados.....");
			String sub_string_where = where_mercado_normalizado.substring(4, where_mercado_normalizado.length());
			String delete_combis = "DELETE ord FROM stage_point_ordenes.ordenes_mes_normalizado ord WHERE "
					+ sub_string_where;
			System.out.println(delete_combis);
			consulta.executeUpdate(delete_combis);

			Utils.printOrdDeb("Normalizando.....");
			String query = "INSERT INTO stage_point_ordenes.ordenes_mes_normalizado (numeroOrden,estadoOrden,fechaEnvio,"
					+ "mes,anho,nombreOrden,origenOrden,origenOCCorregido,numeroLicitacion,"
					+ "rutComprador,unidadCompra,unidadCompraEstandarizada,razonSocialComprador,razonSocialCompradorEstandarizada,comuna,"
					+ "region,segmentoComprador,segmentoCompradorMercado,comunaFacturacion,rutProveedor,proveedor,razonSocialProveedor,razonSocialProveedorEstandarizada,"
					+ "proveedorAsociado,codigoONU,productoServicio,cantidad,cantidadCorregida1,cantidadCorregida2,unidadMedida,unidadMedidaCorregida1,"
					+ "unidadMedidaCorregida2,descripcionProductoConsolidada,especificacionComprador,especificacionProveedor,moneda,precioUnitario,"
					+ "precioCorregido1,precioCorregido2,precioCorregido1_usd,precioCorregido2_usd,descuentos,descuentosTotal,cargos,cargoTotal,totalUnitario,valorTotal,valorTotal_usd,mercado,"
					+ "categoria,subcategoria1,subcategoria2,subcategoria3,subcategoria4,subcategoria5,subcategoria6,subcategoria7,subcategoria8)"
					+ "SELECT ord.numeroOrden,ord.estadoOrden,ord.fechaEnvio, month(ord.fechaEnvio), year(ord.fechaEnvio), ord.nombreOrden,"
					+ "ord.origenOrden origenOC,ori.id origenOCCorregido,ord.numeroLicitacion, rutc.id rutComprador,uni.unidad_compra unidadCompra, uni.id unidadCompraEstandarizada,"
					+ "razc.razonSocialComprador, razc.id razonSocialCompradorEstandarizada, com.id, mcom.region, seg.id segmentoComprador, segm.id segmentoCompradorMercado,"
					+ "ord.comunaFacturacion, rutp.id rutProveedor, ord.proveedor,razp.razonSocialProveedor, razp.id razonSocialProveedorEstandarizada, "
					+ "pasoc.id proveedorAsociado, ord.codigoONU, ord.productoServicio, ord.cantidad, ord.cantidadCorregida1, ord.cantidadCorregida2,"
					+ "ord.unidadMedida,ord.unidadMedidaCorregida1, ord.unidadMedidaCorregida2,ord.descripcionProductoConsolidada, ord.especificacionComprador,"
					+ "ord.especificacionProveedor,ord.moneda, ord.precioUnitario, ord.precioCorregido1, ord.precioCorregido2, ord.precioCorregido1Dolar, ord.precioCorregido2Dolar, ord.descuentos, "
					+ "ord.descuentosTotal,ord.cargos, ord.cargoTotal, ord.totalUnitario, REPLACE(ord.valorTotal, ',', '.'), ord.valorTotalDolar, mer.id mercado,cat.id categoria, "
					+ "scat1.id subcategoria1,scat2.id subcategoria2, scat3.id subcategoria3, scat4.id subcategori4, scat5.id subcategoria5, "
					+ "scat6.id subcategoria6, scat7.id subcategoria7, scat8.id subcategoria8 "
					+ "FROM stage_point_ordenes.ordenes_mes_act ord LEFT JOIN (select * from "
					+ "stage_point_ordenes.maestros_compradores_gnral group by rutComprador) mcom ON mcom.rutComprador "
					+ "= ord.rutComprador LEFT JOIN (select * from stage_point_ordenes.maestros_compradores_mercado group by rutComprador,mercado) mcomM ON mcomM.rutComprador = ord.rutComprador "
					+ "AND mcomM.mercado = ord.mercado"
					+ " LEFT JOIN (select * from stage_point_ordenes.maestros_proveedores GROUP BY rutProveedor,proveedorAsociado) mprov ON mprov.rutProveedor = ord.rutProveedor "
					+ "AND mprov.proveedorAsociado = ord.proveedorAsociado LEFT JOIN stage_point_ordenes.filtro_mercado mer "
					+ "ON mer.mercado = ord.mercado LEFT JOIN stage_point_ordenes.filtro_origenoc ori "
					+ "ON ori.origenOC = ord.origenOCCorregido LEFT JOIN stage_point_ordenes.filtro_rutcomprador rutc "
					+ "ON rutc.rutComprador = ord.rutComprador LEFT JOIN stage_point_ordenes.filtro_rutproveedor rutp "
					+ "ON rutp.rutProveedor = ord.rutProveedor LEFT JOIN stage_point_ordenes.filtro_unidad_compra uni "
					+ "ON uni.unidad_compra = mcom.unidadCompra LEFT JOIN stage_point_ordenes.filtro_categoria cat "
					+ "ON cat.categoria = ord.categoria LEFT JOIN stage_point_ordenes.filtro_comunas com "
					+ "ON com.comuna = mcom.comuna lEFT JOIN stage_point_ordenes.filtro_proveedor_asociado pasoc "
					+ "ON pasoc.proveedor_asociado = mprov.proveedorAsociado LEFT JOIN stage_point_ordenes.filtro_razonsocialcomprador razc "
					+ "ON razc.razonSocialComprador = mcom.razonSocialComprador LEFT JOIN stage_point_ordenes.filtro_razonsocialproveedor razp ON razp.razonSocialProveedor = mprov.razonSocialProveedor"
					+ " LEFT JOIN stage_point_ordenes.filtro_segmentocomprador seg ON seg.segmento = mcom.segmentoComprador "
					+ "LEFT JOIN stage_point_ordenes.filtro_segmentocompradormercado segm ON mcomM.mercado = ord.mercado AND segm.segmento_mercado = mcomM.segmentoCompradorMercado"
					+ " LEFT JOIN stage_point_ordenes.filtro_subcategoria1 scat1 ON scat1.subCategoria = ord.Subcategoria1"
					+ " LEFT JOIN stage_point_ordenes.filtro_subcategoria2 scat2 ON scat2.subCategoria = ord.Subcategoria2"
					+ " LEFT JOIN stage_point_ordenes.filtro_subcategoria3 scat3 ON scat3.subCategoria = ord.Subcategoria3"
					+ " LEFT JOIN stage_point_ordenes.filtro_subcategoria4 scat4 ON scat4.subCategoria = ord.Subcategoria4"
					+ " LEFT JOIN stage_point_ordenes.filtro_subcategoria5 scat5 ON scat5.subCategoria = ord.Subcategoria5"
					+ " LEFT JOIN stage_point_ordenes.filtro_subcategoria6 scat6 ON scat6.subCategoria = ord.Subcategoria6"
					+ " LEFT JOIN stage_point_ordenes.filtro_subcategoria7 scat7 ON scat7.subCategoria = ord.Subcategoria7"
					+ " LEFT JOIN stage_point_ordenes.filtro_subcategoria8 scat8 ON scat8.subCategoria = ord.Subcategoria8"
					+ " " + where_mercado + "";

			consulta.executeUpdate(query);
		} catch (Exception error) {
			Utils.printOrdErr("Error Normalizando: " + error.getMessage());
		} finally {
			DBCnx.close(consulta);
			DBCnx.close(conexion);
		}
		Date fin = new Date();
		double tiempo_total = (fin.getTime() - ini.getTime()) / 1000 / 60;
		Utils.printOrdDeb("Tiempo Normalización: " + tiempo_total);
	}

	private void validarDatosNormalizados(String[] mercados) {
		String fileName = "/Validaciones/Validacion_Datos_Normalizados.txt";
		Utils.printOrdDeb("Archivo: " + fileName);
		FileWriter fileWriter;

		boolean info_correcta = true, validacion_funcion = true;

		String ruta = "/Validaciones/Validacion_Datos_Normalizados.xlsx";
		try {
			File myFile = new File(ruta);
			if (!myFile.exists()) {
				myFile.createNewFile();
			}
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet spreadsheet = workbook.createSheet("Validacion");
			XSSFRow row;

			row = spreadsheet.createRow((short) 0);
			getXLSHeader(row, workbook);

			int excel_row_idx = 1;
			for (int ciclo = 0; ciclo < mercados.length; ciclo++) {
				row = spreadsheet.createRow((short) excel_row_idx);

				XSSFCell cell;

				cell = (XSSFCell) row.createCell((short) 0);
				cell.setCellValue(mercados[ciclo]);

				if (!validarFiltroNormalizado(mercados[ciclo], "origenOCCorregido")) {
					cell = (XSSFCell) row.createCell((short) 1);
					cell.setCellValue("Valores Nulos");
					info_correcta = false;
				}
				if (!validarFiltroNormalizado(mercados[ciclo], "rutComprador")) {
					cell = (XSSFCell) row.createCell((short) 2);
					cell.setCellValue("Valores Nulos");
					info_correcta = false;
				}
				if (!validarFiltroNormalizado(mercados[ciclo], "UnidadCompraEstandarizada")) {
					cell = (XSSFCell) row.createCell((short) 3);
					cell.setCellValue("Valores Nulos");
					info_correcta = false;
				}
				if (!validarFiltroNormalizado(mercados[ciclo], "razonSocialCompradorEstandarizada")) {
					cell = (XSSFCell) row.createCell((short) 4);
					cell.setCellValue("Valores Nulos");
					info_correcta = false;
				}
				if (!validarFiltroNormalizado(mercados[ciclo], "comuna")) {
					cell = (XSSFCell) row.createCell((short) 5);
					cell.setCellValue("Valores Nulos");
					info_correcta = false;
				}
				if (!validarFiltroNormalizado(mercados[ciclo], "region")) {
					cell = (XSSFCell) row.createCell((short) 6);
					cell.setCellValue("Valores Nulos");
					info_correcta = false;
				}
				if (!validarFiltroNormalizado(mercados[ciclo], "segmentoComprador")) {
					cell = (XSSFCell) row.createCell((short) 7);
					cell.setCellValue("Valores Nulos");
					info_correcta = false;
				}
				if (!validarFiltroNormalizado(mercados[ciclo], "segmentoCompradorMercado")) {
					cell = (XSSFCell) row.createCell((short) 8);
					cell.setCellValue("Valores Nulos");
					info_correcta = false;
				}
				if (!validarFiltroNormalizado(mercados[ciclo], "rutProveedor")) {
					cell = (XSSFCell) row.createCell((short) 9);
					cell.setCellValue("Valores Nulos");
					info_correcta = false;
				}
				if (!validarFiltroNormalizado(mercados[ciclo], "razonSocialProveedorEstandarizada")) {
					cell = (XSSFCell) row.createCell((short) 10);
					cell.setCellValue("Valores Nulos");
					info_correcta = false;
				}
				if (!validarFiltroNormalizado(mercados[ciclo], "proveedorAsociado")) {
					cell = (XSSFCell) row.createCell((short) 11);
					cell.setCellValue("Valores Nulos");
					info_correcta = false;
				}
				if (!validarFiltroNormalizado(mercados[ciclo], "categoria")) {
					cell = (XSSFCell) row.createCell((short) 12);
					cell.setCellValue("Valores Nulos");
					info_correcta = false;
				}
				if (!validarFiltroNormalizado(mercados[ciclo], "subcategoria1")) {
					cell = (XSSFCell) row.createCell((short) 13);
					cell.setCellValue("Valores Nulos");
					info_correcta = false;
				}
				if (!validarFiltroNormalizado(mercados[ciclo], "subcategoria2")) {
					cell = (XSSFCell) row.createCell((short) 14);
					cell.setCellValue("Valores Nulos");
					info_correcta = false;
				}
				if (!validarFiltroNormalizado(mercados[ciclo], "subcategoria3")) {
					cell = (XSSFCell) row.createCell((short) 15);
					cell.setCellValue("Valores Nulos");
					info_correcta = false;
				}
				switch (mercados[ciclo]) {
				case "1":
					if (!validarFiltroNormalizado(mercados[ciclo], "subcategoria4")) {
						cell = (XSSFCell) row.createCell((short) 16);
						cell.setCellValue("Valores Nulos");
						info_correcta = false;
					}
					if (!validarFiltroNormalizado(mercados[ciclo], "subcategoria5")) {
						cell = (XSSFCell) row.createCell((short) 17);
						cell.setCellValue("Valores Nulos");
						info_correcta = false;
					}
					if (!validarFiltroNormalizado(mercados[ciclo], "subcategoria6")) {
						cell = (XSSFCell) row.createCell((short) 18);
						cell.setCellValue("Valores Nulos");
						info_correcta = false;
					}
					break;
				case "2":
					break;
				case "3":
					if (!validarFiltroNormalizado(mercados[ciclo], "subcategoria4")) {
						cell = (XSSFCell) row.createCell((short) 16);
						info_correcta = false;
						cell.setCellValue("Valores Nulos");
					}
					if (!validarFiltroNormalizado(mercados[ciclo], "subcategoria5")) {
						cell = (XSSFCell) row.createCell((short) 17);
						cell.setCellValue("Valores Nulos");
						info_correcta = false;
					}
					break;
				case "4":
					if (!validarFiltroNormalizado(mercados[ciclo], "subcategoria4")) {
						cell = (XSSFCell) row.createCell((short) 16);
						cell.setCellValue("Valores Nulos");
						info_correcta = false;
					}
					if (!validarFiltroNormalizado(mercados[ciclo], "subcategoria5")) {
						cell = (XSSFCell) row.createCell((short) 17);
						cell.setCellValue("Valores Nulos");
						info_correcta = false;
					}
					break;
				}
				excel_row_idx++;
			}

			FileOutputStream out;

			out = new FileOutputStream(new File(ruta));
			workbook.write(out);
			out.close();
		} catch (IOException e) {
			Utils.printOrdErr("Error creando excel: " + e.getMessage());
		}

		Utils.printOrdDeb("Validacion: " + info_correcta);
	}

	private static void traspasarHistorico(String where_mercado, String where_mercado_normalizado) {
		Statement consulta = null;
		Connection conexion = null;

		Date ini = new Date();
		try {
			conexion = DBCnx.conexion();
			conexion.setAutoCommit(false);
			consulta = conexion.createStatement();

			// Traspaso de datos a histórico sin normalizar
			Utils.printOrdDeb("Insertando a histórico normal.....");
			String query = "INSERT INTO stage_point_ordenes.ordenes_historico (numeroOrden, estadoOrden, fechaEnvio, mes, anho, "
					+ "nombreOrden, origenOrden, origenOCCorregido, numeroLicitacion, rutComprador, "
					+ "unidadCompra, unidadCompraEstandarizada, razonSocialComprador, razonSocialCompradorEstandarizada, "
					+ "comuna, region, segmentoComprador, segmentoCompradorMercado, comunaFacturacion, "
					+ "rutProveedor, proveedor, razonSocialProveedor, razonSocialProveedorEstandarizada, "
					+ "proveedorAsociado, codigoONU, productoServicio, cantidad, cantidadCorregida1, "
					+ "cantidadCorregida2, unidadMedida, unidadMedidaCorregida1, unidadMedidaCorregida2, "
					+ "descripcionProductoConsolidada, especificacionComprador, especificacionProveedor, moneda, "
					+ "precioUnitario, precioCorregido1, precioCorregido2, precioUnitarioDolar, precioCorregido1Dolar, "
					+ "precioCorregido2Dolar, descuentos, descuentosTotal, cargos, cargoTotal, totalUnitario, "
					+ "valorTotal, valorTotalDolar, mercado, categoria, subcategoria1, subcategoria2, "
					+ "subcategoria3, subcategoria4, subcategoria5, subcategoria6, subcategoria7, subcategoria8, "
					+ "created_at) ";

			String select1 = "SELECT numeroOrden, estadoOrden, fechaEnvio, mes, anho, nombreOrden, "
					+ "origenOrden, origenOCCorregido, numeroLicitacion, rutComprador, unidadCompra, "
					+ "unidadCompraEstandarizada, razonSocialComprador, razonSocialCompradorEstandarizada, comuna, "
					+ "region, segmentoComprador, segmentoCompradorMercado, comunaFacturacion, rutProveedor, "
					+ "proveedor, razonSocialProveedor, razonSocialProveedorEstandarizada, proveedorAsociado, "
					+ "codigoONU, productoServicio, cantidad, cantidadCorregida1, cantidadCorregida2, "
					+ "unidadMedida, unidadMedidaCorregida1, unidadMedidaCorregida2, descripcionProductoConsolidada, "
					+ "especificacionComprador, especificacionProveedor, moneda, precioUnitario, precioCorregido1, "
					+ "precioCorregido2, precioUnitarioDolar, precioCorregido1Dolar, precioCorregido2Dolar, descuentos, "
					+ "descuentosTotal, cargos, cargoTotal, totalUnitario, valorTotal, valorTotalDolar, mercado, "
					+ "categoria, subcategoria1, subcategoria2, subcategoria3, subcategoria4, subcategoria5, "
					+ "subcategoria6, subcategoria7, subcategoria8, now() FROM stage_point_ordenes.ordenes_mes_act ord "+where_mercado;

			String trasfer_data_to_historic = query + select1;
			Utils.printOrdDeb(trasfer_data_to_historic);
			consulta.executeUpdate(trasfer_data_to_historic);

			String query2 = "INSERT INTO stage_point_ordenes.ordenes_historico_normalizado(numeroOrden, estadoOrden, fechaEnvio, mes, "
					+ "anho, nombreOrden, origenOrden, origenOCCorregido, numeroLicitacion, rutComprador, "
					+ "unidadCompra, unidadCompraEstandarizada, razonSocialComprador, razonSocialCompradorEstandarizada, "
					+ "comuna, region, segmentoComprador, segmentoCompradorMercado, comunaFacturacion, rutProveedor, "
					+ "proveedor, razonSocialProveedor, razonSocialProveedorEstandarizada, proveedorAsociado, "
					+ "codigoONU, productoServicio, cantidad, cantidadCorregida1, cantidadCorregida2, unidadMedida, "
					+ "unidadMedidaCorregida1, unidadMedidaCorregida2, descripcionProductoConsolidada, especificacionComprador, "
					+ "especificacionProveedor, moneda, precioUnitario, precioCorregido1, precioCorregido2, "
					+ "precioCorregido1_usd, precioCorregido2_usd, descuentos, descuentosTotal, cargos, cargoTotal, "
					+ "totalUnitario, valorTotal, valorTotal_usd, mercado, categoria, subcategoria1, subcategoria2, "
					+ "subcategoria3, subcategoria4, subcategoria5, subcategoria6, subcategoria7, subcategoria8, "
					+ "created_at)";

			where_mercado_normalizado = where_mercado_normalizado.replaceAll("AND", "");
			
			String select2 = "SELECT numeroOrden, estadoOrden, fechaEnvio, mes, anho, nombreOrden, origenOrden, "
					+ "origenOCCorregido, numeroLicitacion, rutComprador, unidadCompra, unidadCompraEstandarizada, "
					+ "razonSocialComprador, razonSocialCompradorEstandarizada, comuna, region, segmentoComprador, "
					+ "segmentoCompradorMercado, comunaFacturacion, rutProveedor, proveedor, razonSocialProveedor, "
					+ "razonSocialProveedorEstandarizada, proveedorAsociado, codigoONU, productoServicio, cantidad, "
					+ "cantidadCorregida1, cantidadCorregida2, unidadMedida, unidadMedidaCorregida1, unidadMedidaCorregida2, "
					+ "descripcionProductoConsolidada, especificacionComprador, especificacionProveedor, moneda, "
					+ "precioUnitario, precioCorregido1, precioCorregido2, precioCorregido1_usd, precioCorregido2_usd, "
					+ "descuentos, descuentosTotal, cargos, cargoTotal, totalUnitario, valorTotal, valorTotal_usd, "
					+ "mercado, categoria, subcategoria1, subcategoria2, subcategoria3, subcategoria4, subcategoria5, "
					+ "subcategoria6, subcategoria7, subcategoria8, now() FROM stage_point_ordenes.ordenes_mes_normalizado ord WHERE "+where_mercado_normalizado;

			String trasfer_data_to_normalized_historic = query2 + select2;
			Utils.printOrdDeb(trasfer_data_to_normalized_historic);
			consulta.executeUpdate(trasfer_data_to_normalized_historic);
		} catch (Exception error) {
			if (conexion != null) {
				try {
					Utils.printOrdDeb("Transaction is being rolled back");
					conexion.rollback();
				} catch (SQLException excep) {
					Utils.printOrdErr(excep.getMessage());
				}
			}
			Utils.printOrdErr("Error Traspasando a Histórico: " + error.getMessage());
		} finally {
			DBCnx.close(consulta);
			try {
				conexion.setAutoCommit(true);
			} catch (SQLException e) {
				Utils.printOrdDeb("Transaction commited");
			}
			DBCnx.close(conexion);
		}
		Date fin = new Date();
		double tiempo_total = (fin.getTime() - ini.getTime()) / 1000 / 60;
		Utils.printOrdDeb("Tiempo traspaso a histórico: " + tiempo_total);
	}

	private boolean validarFiltroNormalizado(String id_mercado, String columna_filtro, BufferedWriter bufferedWriter) {
		Statement consulta = null;
		Connection conexion = null;
		ResultSet result = null;

		boolean info_correcta = true;
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();
			String query = "SELECT * FROM stage_point_ordenes.ordenes_mes_normalizado WHERE mercado = " + id_mercado
					+ " AND (" + columna_filtro + " IS NULL OR " + columna_filtro + " = 0);";

			result = consulta.executeQuery(query);

			if (result.next()) {
				info_correcta = false;
				bufferedWriter
						.write("Para el mercado [" + id_mercado + "] el campo [" + columna_filtro + "] está null");
				bufferedWriter.newLine();
			} else {
				info_correcta = true;
			}

		} catch (Exception error) {
			Utils.printOrdErr("Error validando normalización:" + error.getMessage());
		} finally {
			DBCnx.closeAll(result, consulta, conexion);
		}
		return info_correcta;
	}

	private boolean validarFiltroNormalizado(String id_mercado, String columna_filtro) {
		Statement consulta = null;
		Connection conexion = null;
		ResultSet result = null;

		boolean info_correcta = true;
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();
			String query = "SELECT * FROM stage_point_ordenes.ordenes_mes_normalizado WHERE mercado = " + id_mercado
					+ " AND (" + columna_filtro + " IS NULL OR " + columna_filtro + " = 0);";

			result = consulta.executeQuery(query);

			if (result.next()) {
				info_correcta = false;
			} else {
				info_correcta = true;
			}

		} catch (Exception error) {
			Utils.printOrdErr("Error validando normalización:" + error.getMessage());
		} finally {
			DBCnx.closeAll(result, consulta, conexion);
		}
		return info_correcta;
	}

	private void generarCombinatoriaFiltros(String where_mercado, String where_mercado_combi) {
		Statement consulta = null;
		Connection conexion = null;

		String fecha_fin = DataManager.getLastDate("stage_point_ordenes");
		Date ini = new Date();
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();

			Utils.printOrdDeb("Borrando combinatorias de filtros.....");
			String sub_string_where = where_mercado_combi.substring(4, where_mercado_combi.length());
			String delete_combis = "DELETE ord FROM stage_point_ordenes.combi_filtros_base ord WHERE "
					+ sub_string_where;
			System.out.println(delete_combis);
			consulta.executeUpdate(delete_combis);

			Utils.printOrdDeb("Generando nuevas combinatorias de filtros.....");
			String query = "INSERT INTO stage_point_ordenes.combi_filtros_base"
					+ "(id_fil_cat,id_fil_com,id_fil_mer,id_fil_ori,id_fil_pasoc,id_fil_razc,id_fil_razp,id_fil_reg,id_fil_rutc,id_fil_rutp,"
					+ "id_fil_seg,id_fil_segcm,id_fil_scat1,id_fil_scat2,id_fil_scat3,id_fil_scat4,id_fil_scat5,id_fil_scat6,id_fil_scat7,"
					+ "id_fil_scat8,id_fil_uni) SELECT categoria,comuna,mercado,origenOCCorregido,"
					+ " proveedorAsociado,razonSocialCompradorEstandarizada,"
					+ "razonSocialProveedorEstandarizada, region,rutComprador,rutProveedor,"
					+ "segmentoComprador, segmentoCompradorMercado, subcategoria1, subcategoria2,"
					+ "subcategoria3,subcategoria4,subcategoria5,subcategoria6,subcategoria7,"
					+ "subcategoria8,unidadCompraEstandarizada FROM stage_point_ordenes.ordenes_historico_normalizado ord "
					+ "WHERE fechaEnvio >= DATE_SUB(DATE_ADD('" + fecha_fin
					+ "', INTERVAL 1 DAY), INTERVAL 2 YEAR) AND fechaEnvio < DATE_ADD('" + fecha_fin
					+ "', INTERVAL 1 DAY) " + where_mercado
					+ " GROUP BY origenOCCorregido, rutComprador, unidadCompraEstandarizada, "
					+ "razonSocialCompradorEstandarizada, comuna, region,segmentoComprador, "
					+ "segmentoCompradorMercado, rutProveedor, razonSocialProveedorEstandarizada, "
					+ "proveedorAsociado, mercado, categoria, subcategoria1, subcategoria2, "
					+ "subcategoria3, subcategoria4, subcategoria5, subcategoria6, subcategoria7, subcategoria8";
			// Utils.printOrdDeb(query);
			consulta.executeUpdate(query);
		} catch (Exception error) {
			Utils.printOrdErr("Error Combinando filtros: " + error.getMessage());
		} finally {
			DBCnx.close(consulta);
			DBCnx.close(conexion);
		}
		Date fin = new Date();
		double tiempo_total = (fin.getTime() - ini.getTime()) / 1000 / 60;
		Utils.printOrdDeb("Tiempo Generación combi filtros: " + tiempo_total);
	}

	private void generarCombinatoriaValores(String where_mercado, String where_mercado_combi) {
		Statement consulta = null;
		Connection conexion = null;

		String fecha_fin = DataManager.getLastDate("stage_point_ordenes");
		Date ini = new Date();
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();

			Utils.printOrdDeb("Borrando combinatorias de valores.....");
			String sub_string_where = where_mercado_combi.substring(4, where_mercado_combi.length());
			String delete_combis = "DELETE ord FROM stage_point_ordenes.combi_valores ord WHERE " + sub_string_where;
			System.out.println(delete_combis);
			consulta.executeUpdate(delete_combis);

			Utils.printOrdDeb("Generando combi valores.....");
			String query = "INSERT INTO stage_point_ordenes.combi_valores"
					+ "(id_fil_cat,id_fil_com,id_fil_mer,id_fil_ori,id_fil_pasoc,id_fil_razc,"
					+ "id_fil_razp,id_fil_reg,id_fil_rutc,id_fil_rutp,id_fil_seg,id_fil_segcm,"
					+ "id_fil_scat1,id_fil_scat2,id_fil_scat3,id_fil_scat4,id_fil_scat5,"
					+ "id_fil_scat6,id_fil_scat7,id_fil_scat8,id_fil_uni,mes,anho,fechaEnvio,"
					+ "valorTotal,valorTotal_usd,totalCant1,totalCant2) SELECT categoria,comuna,"
					+ "mercado,origenOCCorregido,proveedorAsociado,razonSocialCompradorEstandarizada,"
					+ "razonSocialProveedorEstandarizada,region,rutComprador,rutProveedor,segmentoComprador,"
					+ "segmentoCompradorMercado,subcategoria1,subcategoria2,subcategoria3,subcategoria4,"
					+ "subcategoria5,subcategoria6,subcategoria7,subcategoria8,unidadCompraEstandarizada,"
					+ "mes,anho,concat(anho,'-', mes, '-','01'),sum(replace(valorTotal,',','.')),sum(replace(valorTotal_usd,',','.')),"
					+ "sum(replace(cantidadCorregida1,',','.')),sum(replace(cantidadCorregida2,',','.')) FROM stage_point_ordenes.ordenes_historico_normalizado ord "
					+ "WHERE fechaEnvio >= DATE_SUB(DATE_ADD('" + fecha_fin
					+ "', INTERVAL 1 DAY), INTERVAL 2 YEAR) AND fechaEnvio < DATE_ADD('" + fecha_fin
					+ "', INTERVAL 1 DAY) " + where_mercado + " GROUP BY mes,anho,"
					+ "origenOCCorregido,rutComprador,unidadCompraEstandarizada,razonSocialCompradorEstandarizada,"
					+ "comuna,region,segmentoComprador,segmentoCompradorMercado,rutProveedor,razonSocialProveedorEstandarizada,"
					+ "proveedorAsociado,mercado,categoria,subcategoria1,subcategoria2,subcategoria3,subcategoria4,"
					+ "subcategoria5,subcategoria6,subcategoria7,subcategoria8";
			System.out.println(query);
			consulta.executeUpdate(query);
		} catch (Exception error) {
			Utils.printOrdErr("Error Combinando para valores: " + error.getMessage());
		} finally {
			DBCnx.close(consulta);
			DBCnx.close(conexion);
		}
		Date fin = new Date();
		double tiempo_total = (fin.getTime() - ini.getTime()) / 1000 / 60;
		Utils.printOrdDeb("Tiempo Generando Combi Valores: " + tiempo_total);
	}

	private void generarGraficoPrecalculado(String where_mercado, String where_mercado_combi) {
		Statement consulta = null;
		Connection conexion = null;

		String fecha_fin = DataManager.getLastDate("stage_point_ordenes");
		Date ini = new Date();
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();

			Utils.printOrdDeb("Borrando precalculo del gráfico.....");
			String sub_string_where = where_mercado_combi.substring(4, where_mercado_combi.length());
			String delete_combis = "DELETE ord FROM stage_point_ordenes.grafico_gnral_pre_merc ord WHERE "
					+ sub_string_where;
			consulta.executeUpdate(delete_combis);

			Utils.printOrdDeb("Precalculando grafico inicial.....");
			String query = "INSERT INTO stage_point_ordenes.grafico_gnral_pre_merc (id_mer,fechaEnvio,"
					+ "anho,mes,total_ventas,total_cantidad1,total_cantidad2, total_ventas_usd) "
					+ "SELECT mercado,fechaEnvio,anho,mes,SUM(replace(valorTotal,',','.')),SUM(replace(cantidadCorregida1,',','.')),"
					+ "SUM(replace(cantidadCorregida2,',','.')),SUM(replace(valorTotal_usd,',','.')) FROM stage_point_ordenes.ordenes_historico_normalizado ord "
					+ "WHERE fechaEnvio >= DATE_SUB(DATE_ADD('" + fecha_fin
					+ "', INTERVAL 1 DAY), INTERVAL 2 YEAR) AND fechaEnvio < DATE_ADD('" + fecha_fin
					+ "', INTERVAL 1 DAY) " + where_mercado + " GROUP BY mercado, mes , anho ORDER BY fechaEnvio DESC;";

			consulta.executeUpdate(query);
		} catch (Exception error) {
			Utils.printOrdErr("error Generando grafico prec: " + error.getMessage());
		} finally {
			DBCnx.close(consulta);
			DBCnx.close(conexion);
		}
		Date fin = new Date();
		double tiempo_total = (fin.getTime() - ini.getTime()) / 1000 / 60;
		Utils.printOrdDeb("Tiempo Generación de Gráfico precalculado: " + tiempo_total);
	}

	private void insertCombiValoresMes(String where_mercado) {
		Statement consulta = null;
		Connection conexion = null;

		String fecha[] = DataManager.getLastDate("stage_point_ordenes").split("-");
		int mes = Integer.parseInt(fecha[1]);
		int anho_act = Integer.parseInt(fecha[0]);
		int anho_ant = Integer.parseInt(fecha[0]) - 1;

		Date ini = new Date();
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();

			Utils.printOrdDeb("Borrando combi valores mes.....");
			String sub_string_where = where_mercado.substring(4, where_mercado.length());
			String delete_combis = "DELETE ord FROM stage_point_ordenes.combi_valores_mes ord WHERE "
					+ sub_string_where;
			consulta.executeUpdate(delete_combis);

			Utils.printOrdDeb("Generando combi valores mes.....");
			String query = "INSERT INTO stage_point_ordenes.combi_valores_mes (id_fil_cat,id_fil_com,"
					+ "id_fil_mer,id_fil_ori,id_fil_pasoc,id_fil_razc,id_fil_razp,id_fil_reg,"
					+ "id_fil_rutc,id_fil_rutp,id_fil_seg,id_fil_segcm,id_fil_scat1,id_fil_scat2,"
					+ "id_fil_scat3,id_fil_scat4,id_fil_scat5,id_fil_scat6,id_fil_scat7,id_fil_scat8,"
					+ "id_fil_uni,mes,anho,fechaEnvio,valorTotal,valorTotal_usd,totalCant1,totalCant2) ";

			String select1 = "SELECT id_fil_cat,id_fil_com,id_fil_mer,id_fil_ori,id_fil_pasoc,"
					+ "id_fil_razc,id_fil_razp,id_fil_reg,id_fil_rutc,id_fil_rutp,id_fil_seg,"
					+ "id_fil_segcm,id_fil_scat1,id_fil_scat2,id_fil_scat3,id_fil_scat4,id_fil_scat5,"
					+ "id_fil_scat6,id_fil_scat7,id_fil_scat8,id_fil_uni,mes,anho,fechaEnvio,"
					+ "valorTotal,valorTotal_usd,totalCant1,totalCant2 FROM stage_point_ordenes.combi_valores ord WHERE mes = "
					+ mes + " AND anho = " + anho_act + " " + where_mercado + ";";

			String select2 = "SELECT id_fil_cat,id_fil_com,id_fil_mer,id_fil_ori,id_fil_pasoc,"
					+ "id_fil_razc,id_fil_razp,id_fil_reg,id_fil_rutc,id_fil_rutp,id_fil_seg,"
					+ "id_fil_segcm,id_fil_scat1,id_fil_scat2,id_fil_scat3,id_fil_scat4,id_fil_scat5,"
					+ "id_fil_scat6,id_fil_scat7,id_fil_scat8,id_fil_uni,mes,anho,fechaEnvio,"
					+ "valorTotal,valorTotal_usd,totalCant1,totalCant2 FROM stage_point_ordenes.combi_valores ord WHERE mes = "
					+ mes + " AND anho = " + anho_ant + " " + where_mercado + ";";

			String q1 = query + select1;
			String q2 = query + select2;

			consulta.executeUpdate(q1);
			consulta.executeUpdate(q2);
		} catch (Exception error) {
			Utils.printOrdErr("Error Combinando para valores: " + error.getMessage());
		} finally {
			DBCnx.close(consulta);
			DBCnx.close(conexion);
		}
		Date fin = new Date();
		double tiempo_total = (fin.getTime() - ini.getTime()) / 1000 / 60;
		Utils.printOrdDeb("Tiempo Generación de Combi Valores Mes: " + tiempo_total);
	}

	private void insertCombiValoresYTD(String where_mercado) {
		Statement consulta = null;
		Connection conexion = null;

		String fecha_fin1 = DataManager.getLastDate("stage_point_ordenes");
		String fecha[] = fecha_fin1.split("-");

		int anho_act = Integer.parseInt(fecha[0]);
		int anho_ant = Integer.parseInt(fecha[0]) - 1;

		if (fecha[1].equals("2") && fecha[2].equals("29")) {
			fecha[2] = "28";
		}
		String fecha_fin2 = anho_ant + "-" + fecha[1] + "-" + fecha[2];

		String fecha_ini1 = anho_act + "-01-01";
		String fecha_ini2 = anho_ant + "-01-01";

		Date ini = new Date();
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();

			Utils.printOrdDeb("Borrando combi valores ytd.....");
			String sub_string_where = where_mercado.substring(4, where_mercado.length());
			String delete_combis = "DELETE ord FROM stage_point_ordenes.combi_valores_ytd ord WHERE "
					+ sub_string_where;
			consulta.executeUpdate(delete_combis);

			String query = "INSERT INTO stage_point_ordenes.combi_valores_ytd (id_fil_cat,id_fil_com,"
					+ "id_fil_mer,id_fil_ori,id_fil_pasoc,id_fil_razc,id_fil_razp,id_fil_reg,"
					+ "id_fil_rutc,id_fil_rutp,id_fil_seg,id_fil_segcm,id_fil_scat1,id_fil_scat2,"
					+ "id_fil_scat3,id_fil_scat4,id_fil_scat5,id_fil_scat6,id_fil_scat7,id_fil_scat8,"
					+ "id_fil_uni,mes,anho,fechaEnvio,valorTotal,valorTotal_usd,totalCant1,totalCant2) ";

			String select1 = "SELECT id_fil_cat,id_fil_com,id_fil_mer,id_fil_ori,id_fil_pasoc,"
					+ "id_fil_razc,id_fil_razp,id_fil_reg,id_fil_rutc,id_fil_rutp,id_fil_seg,"
					+ "id_fil_segcm,id_fil_scat1,id_fil_scat2,id_fil_scat3,id_fil_scat4,id_fil_scat5,"
					+ "id_fil_scat6,id_fil_scat7,id_fil_scat8,id_fil_uni,mes,anho,fechaEnvio,"
					+ "valorTotal,valorTotal_usd,totalCant1,totalCant2 FROM stage_point_ordenes.combi_valores ord WHERE fechaEnvio >= '"
					+ fecha_ini1 + "' AND fechaEnvio < DATE_ADD('" + fecha_fin1 + "', INTERVAL 1 DAY) " + where_mercado
					+ ";";

			String select2 = "SELECT id_fil_cat,id_fil_com,id_fil_mer,id_fil_ori,id_fil_pasoc,"
					+ "id_fil_razc,id_fil_razp,id_fil_reg,id_fil_rutc,id_fil_rutp,id_fil_seg,"
					+ "id_fil_segcm,id_fil_scat1,id_fil_scat2,id_fil_scat3,id_fil_scat4,id_fil_scat5,"
					+ "id_fil_scat6,id_fil_scat7,id_fil_scat8,id_fil_uni,mes,anho,fechaEnvio,"
					+ "valorTotal,valorTotal_usd,totalCant1,totalCant2 FROM stage_point_ordenes.combi_valores ord WHERE fechaEnvio >= '"
					+ fecha_ini2 + "' AND fechaEnvio < DATE_ADD('" + fecha_fin2 + "', INTERVAL 1 DAY) " + where_mercado
					+ ";";

			String q1 = query + select1;
			String q2 = query + select2;

			Utils.printOrdDeb("Generando combi ytd anho actual.....");
			consulta.executeUpdate(q1);
			Utils.printOrdDeb("Generando combi ytd anho anterior.....");
			consulta.executeUpdate(q2);
		} catch (Exception error) {
			Utils.printOrdErr("Error Combinando para valores: " + error.getMessage());
		} finally {
			DBCnx.close(consulta);
			DBCnx.close(conexion);
		}
		Date fin = new Date();
		double tiempo_total = (fin.getTime() - ini.getTime()) / 1000 / 60;
		Utils.printOrdDeb("Tiempo Generación de Combi Valores YTD: " + tiempo_total);
	}

	private static void traspasar_memoria(String where_mercado) {
		Statement consulta = null;
		Connection conexion = null;

		Date ini = new Date();
		try {
			conexion = DBCnx.conexion();
			consulta = conexion.createStatement();

			Utils.printOrdDeb("Borrando combi valores ytd.....");
			System.out.println(where_mercado);
			String sub_string_where = where_mercado.substring(4, where_mercado.length());
			String delete_combis = "DELETE ord FROM stage_point_ordenes.combi_filtros ord WHERE " + sub_string_where;
			System.out.println(delete_combis);
			consulta.executeUpdate(delete_combis);

			String query = "INSERT INTO stage_point_ordenes.`combi_filtros`(`id`, `id_fil_cat`, `id_fil_com`, `id_fil_mer`, "
					+ "`id_fil_ori`, `id_fil_pasoc`, `id_fil_razc`, `id_fil_razp`, `id_fil_reg`, `id_fil_rutc`, `id_fil_rutp`, "
					+ "`id_fil_seg`, `id_fil_segcm`, `id_fil_scat1`, `id_fil_scat2`, `id_fil_scat3`, `id_fil_scat4`, "
					+ "`id_fil_scat5`, `id_fil_scat6`, `id_fil_scat7`, `id_fil_scat8`, `id_fil_uni`)";

			String select1 = "SELECT `id`, `id_fil_cat`, `id_fil_com`, `id_fil_mer`, `id_fil_ori`, `id_fil_pasoc`, "
					+ "`id_fil_razc`, `id_fil_razp`, `id_fil_reg`, `id_fil_rutc`, `id_fil_rutp`, `id_fil_seg`, "
					+ "`id_fil_segcm`, `id_fil_scat1`, `id_fil_scat2`, `id_fil_scat3`, `id_fil_scat4`, `id_fil_scat5`, "
					+ "`id_fil_scat6`, `id_fil_scat7`, `id_fil_scat8`, `id_fil_uni` FROM "
					+ "stage_point_ordenes.`combi_filtros_base` ord WHERE " + sub_string_where;

			String q1 = query + select1;

			Utils.printOrdDeb("Traspasando filtros a memoria.....");
			consulta.executeUpdate(q1);
		} catch (Exception error) {
			Utils.printOrdErr("Error traspasando a memoria: " + error.getMessage());
		} finally {
			DBCnx.close(consulta);
			DBCnx.close(conexion);
		}
		Date fin = new Date();
		double tiempo_total = (fin.getTime() - ini.getTime()) / 1000 / 60;
		Utils.printOrdDeb("Tiempo traspaso a memoria: " + tiempo_total);
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
			where = "WHERE ord.mercado IN (" + valores_where + ")";
		}

		return where;
	}

	private static String get_where_mercados_normalizados(String request, String nombre_columna) {
		String where = "";
		String valores_where = "";

		String mercados_alias[] = request.replace("[", "").replace("]", "").replace("\"", "").split(",");

		int cant_mercados = mercados_alias.length;
		for (int ciclo = 0; ciclo < cant_mercados; ciclo++) {
			switch (mercados_alias[ciclo]) {
			case "med_norm":
				if (valores_where.equals("")) {
					valores_where += "2";
				} else {
					valores_where += ",2";
				}
				break;
			case "nut_norm":
				if (valores_where.equals("")) {
					valores_where += "1";
				} else {
					valores_where += ",1";
				}
				break;
			case "ofi_norm":
				if (valores_where.equals("")) {
					valores_where += "3";
				} else {
					valores_where += ",3";
				}
				break;
			case "tec_norm":
				if (valores_where.equals("")) {
					valores_where += "4";
				} else {
					valores_where += ",4";
				}
				break;
			}
		}

		if (!valores_where.equals("")) {
			where = "AND ord." + nombre_columna + " IN (" + valores_where + ")";
		}

		return where;
	}

	private static String[] get_mercados(String request) {
		String mercados_alias[] = request.replace("[", "").replace("]", "").replace("\"", "").split(",");

		int cant_mercados = mercados_alias.length;
		String[] mercados = new String[cant_mercados];

		for (int ciclo = 0; ciclo < cant_mercados; ciclo++) {
			switch (mercados_alias[ciclo]) {
			case "med_norm":
				mercados[ciclo] = "2";
				break;
			case "nut_norm":
				mercados[ciclo] = "1";
				break;
			case "ofi_norm":
				mercados[ciclo] = "3";
				break;
			case "tec_norm":
				mercados[ciclo] = "4";
				break;
			}
		}

		return mercados;
	}

	public static void getXLSHeader(Row row_header, XSSFWorkbook wb) {

		XSSFCell cell;

		cell = (XSSFCell) row_header.createCell((short) 0);
		cell.setCellValue("Mercado");

		cell = (XSSFCell) row_header.createCell((short) 1);
		cell.setCellValue("Origen OC");

		cell = (XSSFCell) row_header.createCell((short) 2);
		cell.setCellValue("RutComprador");

		cell = (XSSFCell) row_header.createCell((short) 3);
		cell.setCellValue("Unidad Compra");

		cell = (XSSFCell) row_header.createCell((short) 4);
		cell.setCellValue("RazonSocialComp");

		cell = (XSSFCell) row_header.createCell((short) 5);
		cell.setCellValue("Comuna");

		cell = (XSSFCell) row_header.createCell((short) 6);
		cell.setCellValue("Region");

		cell = (XSSFCell) row_header.createCell((short) 7);
		cell.setCellValue("Segmento Comp");

		cell = (XSSFCell) row_header.createCell((short) 8);
		cell.setCellValue("Segm Comp Mer");

		cell = (XSSFCell) row_header.createCell((short) 9);
		cell.setCellValue("rut Prov");

		cell = (XSSFCell) row_header.createCell((short) 10);
		cell.setCellValue("RazonSocialProv");

		cell = (XSSFCell) row_header.createCell((short) 11);
		cell.setCellValue("Prov Asoc");

		cell = (XSSFCell) row_header.createCell((short) 12);
		cell.setCellValue("Categoria");

		cell = (XSSFCell) row_header.createCell((short) 13);
		cell.setCellValue("Scat1");

		cell = (XSSFCell) row_header.createCell((short) 14);
		cell.setCellValue("Scat2");

		cell = (XSSFCell) row_header.createCell((short) 15);
		cell.setCellValue("Scat3");

		cell = (XSSFCell) row_header.createCell((short) 16);
		cell.setCellValue("Scat4");

		cell = (XSSFCell) row_header.createCell((short) 17);
		cell.setCellValue("Scat5");

		cell = (XSSFCell) row_header.createCell((short) 18);
		cell.setCellValue("Scat6");

		cell = (XSSFCell) row_header.createCell((short) 19);
		cell.setCellValue("Scat7");

		cell = (XSSFCell) row_header.createCell((short) 20);
		cell.setCellValue("Scat8");
	}
}
