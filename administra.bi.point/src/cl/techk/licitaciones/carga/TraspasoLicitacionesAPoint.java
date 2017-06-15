package cl.techk.licitaciones.carga;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import cl.techk.ext.database.DBCnx;
import cl.techk.ext.utils.Utils;

@WebServlet("/TraspasoLicitacionesAPoint")
public class TraspasoLicitacionesAPoint extends HttpServlet {
    private static final long serialVersionUID = 1L;
    String move_licitacion = "insert ignore into licitacion (`id`, `codigo`, `nombre`, `tipo`, `descripcion`,"
            + " `estado`, `tipo_convocatoria`, `moneda`, `toma_razon_contraloria`, `monto_total_estimado`, `contrato_con_renovacion`, "
            + "`tiempo_contrato`, `plazo_pago`, `fecha_publicacion`, `fecha_cierre`, `fecha_adjudicacion`, `fecha_inicio_preguntas`, "
            + "`fecha_fin_preguntas`, `fecha_publicacion_respuestas`, `fecha_firma_contrato`, `tiempo_evaluacion_ofertas`, `razon_social`,"
            + " `unidad_compra`, `rut`, `region`, `direccion`, `comuna`, `garantias`, `status`, `created_at`, `updated_at`) select `id`,"
            + " `codigo`, `nombre`, `tipo`, `descripcion`, `estado`, `tipo_convocatoria`, `moneda`, `toma_razon_contraloria`, "
            + "`monto_total_estimado`, `contrato_con_renovacion`, `tiempo_contrato`, `plazo_pago`, `fecha_publicacion`, `fecha_cierre`, "
            + "`fecha_adjudicacion`, `fecha_inicio_preguntas`, `fecha_fin_preguntas`, `fecha_publicacion_respuestas`, `fecha_firma_contrato`,"
            + " `tiempo_evaluacion_ofertas`, `razon_social`, `unidad_compra`, `rut`, `region`, `direccion`, `comuna`, `garantias`, `status`, "
            + "`created_at`, `updated_at` from "+DBCnx.db_downloads+".licitacion";

    String move_licitacion_criterio = "insert ignore into licitacion_criterio_evaluacion (select * "
            + "from "+DBCnx.db_downloads+".licitacion_criterio_evaluacion)";
    String move_licitacion_garantia = "insert ignore into licitacion_garantia (select * from "
            + ""+DBCnx.db_downloads+".licitacion_garantia)";
    String move_licitacion_item = "insert ignore into licitacion_item (id,id_licitacion,item,descripcion"
            + "_producto,unidad_medida,cantidad,generico,codigo_categoria) (select id,id_licitacion,item,descripcion"
            + "_producto,unidad_medida,cantidad,generico,codigo_categoria from "+DBCnx.db_downloads+"."
            + "licitacion_item)";

    public TraspasoLicitacionesAPoint() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        executeMove(move_licitacion, " licitaciones");
        executeMove(move_licitacion_criterio, " criterios");
        executeMove(move_licitacion_garantia, " garantias");
        executeMove(move_licitacion_item, " items");
    }

    private void executeMove(String query, String dato) {
        Connection cnxupdatelicuser = null;
        PreparedStatement stmtupdatelicuser = null;
        int datosinserted = -1;
        try {
            cnxupdatelicuser = DBCnx.conexion();
            stmtupdatelicuser = cnxupdatelicuser.prepareStatement(query);
            datosinserted = stmtupdatelicuser.executeUpdate();
            Utils.print(dato + " insertados: " + datosinserted);
        } catch (MySQLIntegrityConstraintViolationException e) {
            e.printStackTrace();
            Utils.print("error  traspasando datos");
        } catch (Exception e) {
            e.printStackTrace();
            Utils.print("error traspasando datos");
        } finally {
            DBCnx.close(stmtupdatelicuser);
            DBCnx.close(cnxupdatelicuser);
        }
    }
}