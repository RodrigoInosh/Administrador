package cl.techk.ordenes.carga;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.json.JSONException;

import cl.techk.ext.database.DBCnx;
import cl.techk.ext.utils.Utils;

@Path("/CargaInfoClientes")
public class CargaInfoClientes {

	@GET
	@Path("/MercadoUsuario/")
	@Produces("application/json; charset=UTF-8")
	public Response carga_mercado(@Context final HttpServletRequest request,
			@CookieParam(value = "POINTSESSIONID") String POINTSESSIONID) {
		Utils.printOrdDeb("Cargando Mercado por Usuario");

		String tipo_bdd = request.getParameter("bdd");

		String bdd_name = "";
		switch (tipo_bdd) {
		case "stage":
			bdd_name = "stage_point_ordenes";
			break;
		case "prod":
			bdd_name = "point_ordenes";
			break;
		default:
			bdd_name = "";
			break;
		}

		if (bdd_name != "") {
			String carga_mercados = "INSERT INTO `" + bdd_name + "`.`mercado_usuario` (`id_usr`,`id_clte`,"
					+ "`id_mer`) SELECT u.id_usr, u.id_clte, m.id id_merc FROM " + "" + bdd_name
					+ ".carga_mercados_usuario u LEFT JOIN " + bdd_name + ".filtro_mercado m "
					+ "ON m.mercado = u.mercado;";

			Connection cnx = null;
			Statement stmt = null;
			try {
				cnx = DBCnx.conexion();
				stmt = cnx.createStatement();
				stmt.executeUpdate(carga_mercados);

			} catch (JSONException | SQLException e) {
				if (e.getMessage().contains("Duplicate entry")) {
					Utils.printOrdErr("Dato Duplicado no se ingresará");
				} else {
					Utils.printOrdErr("Error Cargando Mercados: " + e.getMessage());
				}
			} finally {
				DBCnx.close(stmt);
				DBCnx.close(cnx);
			}

			return Response.status(200).entity("Proceso Completado").build();
		} else {
			return Response.status(400).entity("bad request").build();
		}
	}

	@GET
	@Path("/NombreFiltrosCliente/")
	@Produces("application/json; charset=UTF-8")
	public Response carga_filtros(@Context final HttpServletRequest request,
			@CookieParam(value = "POINTSESSIONID") String POINTSESSIONID) {
		Utils.printOrdDeb("Cargando Nombre de Filtros por Cliente");

		String tipo_bdd = request.getParameter("bdd");

		String bdd_name = "";
		switch (tipo_bdd) {
		case "stage":
			bdd_name = "stage_point_ordenes";
			break;
		case "prod":
			bdd_name = "point_ordenes";
			break;
		default:
			bdd_name = "";
			break;
		}

		if (bdd_name != "") {
			String carga_filtros = "INSERT INTO `stage_point_ordenes`.`nombre_filtros_cliente` (`id_clte`,"
					+ "`id_filtro_generico`,`nombre`) SELECT  c.id_clte, g.id, c.nombre_filtro FROM "
					+ "stage_point_ordenes.carga_nombre_filtros_cliente c LEFT JOIN stage_point_ordenes.filtros_genericos g "
					+ "ON g.nombre_tabla = c.filtro_generico";

			Connection cnx = null;
			Statement stmt = null;
			try {
				cnx = DBCnx.conexion();
				stmt = cnx.createStatement();
				stmt.executeUpdate(carga_filtros);

			} catch (JSONException | SQLException e) {
				if (e.getMessage().contains("Duplicate entry")) {
					Utils.printOrdErr("Dato Duplicado no se ingresará");
				} else {
					Utils.printOrdErr("Error Nombre Filtros: " + e.getMessage());
				}
			} finally {
				DBCnx.close(stmt);
				DBCnx.close(cnx);
			}

			return Response.status(200).entity("Proceso completado").build();
		} else {
			return Response.status(400).entity("bad request").build();
		}
	}

	@GET
	@Path("/ProfundidadCliente/")
	@Produces("application/json; charset=UTF-8")
	public Response profundidad_cliente(@Context final HttpServletRequest request,
			@CookieParam(value = "POINTSESSIONID") String POINTSESSIONID) {
		Utils.printOrdDeb("Cargando Profundidad por Cliente");

		String tipo_bdd = request.getParameter("bdd");

		String bdd_name = "";
		switch (tipo_bdd) {
		case "stage":
			bdd_name = "stage_point_ordenes";
			break;
		case "prod":
			bdd_name = "point_ordenes";
			break;
		default:
			bdd_name = "";
			break;
		}

		if (bdd_name != "") {

			String select_profunidad = "SELECT id_usr, origen, prof_1, prof_2, prof_3, inicial FROM " + "" + bdd_name
					+ ".carga_profundidad_usuario";

			Connection cnx = null;
			PreparedStatement stmt = null;
			ResultSet result = null;
			try {
				cnx = DBCnx.conexion();
				stmt = cnx.prepareStatement(select_profunidad);
				result = stmt.executeQuery();

				int id_usr = 0;
				int inicial = 0;
				String parent = "";
				String prof1 = "";
				String prof2 = "";
				String prof3 = "";

				while (result.next()) {
					id_usr = result.getInt("id_usr");
					// Si es que la combinación corresponde a la profundidad por
					// defecto
					inicial = result.getInt("inicial");

					parent = result.getString("origen");
					prof1 = result.getString("prof_1");
					prof2 = result.getString("prof_2");
					prof3 = result.getString("prof_3");
					Utils.printOrdDeb("Id usr:" + id_usr);
					Utils.printOrdDeb("parent:" + parent);
					Utils.printOrdDeb("prof1:" + prof1);
					Utils.printOrdDeb("prof2:" + prof2);
					Utils.printOrdDeb("prof3:" + prof3);

					insertar_combi_profundidad(id_usr, inicial, parent, prof1, prof2, prof3, bdd_name);
				}
			} catch (SQLException e) {
				Utils.printOrdErr("Error profundidad: " + e.getMessage());
			} finally {
				DBCnx.closeAll(result, stmt, cnx);
			}
			return Response.status(200).entity("Proceso Completo").build();
		}
		return Response.status(400).entity("bad request").build();
	}

	private void insertar_combi_profundidad(int id_usr, int inicial, String parent, String prof1, String prof2,
			String prof3, String bdd_name) {

		if (!parent.equals("") && !prof1.equals("") && !prof2.equals("") && !prof3.equals("")) {

			int id_parent, id_prof1, id_prof2;

			insert_combi(bdd_name, parent, id_usr, inicial, 0);
			id_parent = get_id_prof(bdd_name, parent, id_usr);

			insert_combi(bdd_name, prof1, id_usr, inicial, id_parent);
			id_prof1 = get_id_prof(bdd_name, prof1, id_usr);

			insert_combi(bdd_name, prof2, id_usr, inicial, id_prof1);
			id_prof2 = get_id_prof(bdd_name, prof2, id_usr);

			insert_combi(bdd_name, prof3, id_usr, inicial, id_prof2);

		}
	}

	private void insert_combi(String bdd_name, String alias, int id_usr, int inicial, int parent_id) {
		Connection cnx = null;
		PreparedStatement stmt = null;

		try {
			cnx = DBCnx.conexion();

			String insert_parent = "INSERT INTO " + bdd_name + ".combi_profundidad (usr_id,parent_id, alias, status)"
					+ " VALUES (?,?, ?, ?)";

			stmt = cnx.prepareStatement(insert_parent);
			stmt.setInt(1, id_usr);
			if (parent_id == 0) {
				stmt.setNull(2, java.sql.Types.INTEGER);
			} else {
				stmt.setInt(2, parent_id);
			}
			stmt.setString(3, alias);
			stmt.setInt(4, inicial);

			Utils.printOrdDeb(stmt);
			stmt.execute();
		} catch (SQLException e) {
			Utils.printOrdErr("Error insertando parent: " + e.getMessage());
		} finally {
			DBCnx.close(cnx);
			DBCnx.close(stmt);
		}
	}

	private int get_id_prof(String bdd_name, String parent, int id_usr) {
		Connection cnx = null;
		PreparedStatement stmt = null;
		ResultSet result = null;

		int id = 0;

		try {
			cnx = DBCnx.conexion();

			String query = "SELECT id FROM " + bdd_name + ".combi_profundidad WHERE usr_id = ? AND " + "alias = ?;";

			stmt = cnx.prepareStatement(query);

			stmt.setInt(1, id_usr);
			stmt.setString(2, parent);
			Utils.printOrdDeb(stmt);
			result = stmt.executeQuery();

			if (result.next()) {
				id = result.getInt("id");
			}
		} catch (SQLException e) {
			Utils.printOrdDeb("Error Obteniedo id: " + e.getMessage());
		} finally {
			DBCnx.closeAll(result, stmt, cnx);
		}

		return id;
	}
}
