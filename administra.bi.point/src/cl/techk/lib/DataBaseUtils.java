package cl.techk.lib;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.rowset.CachedRowSet;

import com.sun.rowset.CachedRowSetImpl;

import cl.techk.ext.database.DBCnx;

public class DataBaseUtils {
    
    public static CachedRowSet exQuery(Object[] params) {
        return exQuery(params, null);
    }

    public static CachedRowSet exQuery(Object[] params, Connection cnx_in) {

        CachedRowSet rowset = null;
        PreparedStatement st = null;
        Connection cnx = null;
        ResultSet rs = null;
        try {
            if (cnx_in != null) {
                cnx = cnx_in;
            } else {
                cnx = DBCnx.conexion();
            }
            st = cnx.prepareStatement(params[0].toString());
            for (int i = 1; i < params.length; i++) {
                if (params[i] == null) {
                    st.setNull(i, java.sql.Types.NULL);
                } else if (params[i] instanceof String) {
                    st.setString(i, (String) params[i]);
                } else if (params[i] instanceof Integer) {
                    st.setInt(i, ((Integer) params[i]).intValue());
                } else if (params[i] instanceof Long) {
                    st.setLong(i, ((Long) params[i]).intValue());
                } else if (params[i].getClass().isArray()) {
                    String[] array = (String[]) params[i];
                    for (int idx = 0; idx < array.length; ++idx) {
                        st.setString(i, array[idx]);
                        i++;
                    }
                }
            }
            rs = st.executeQuery();
            rowset = new CachedRowSetImpl();
            rowset.populate(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBCnx.close(rs);
            DBCnx.close(st);
            if (cnx_in == null) {
                DBCnx.close(cnx);
            }
        }
        return rowset;
    }
    
    public static String getWhereClause(String client_id) {

        String where_clause = "";

        if (!"".equals(client_id)) {
            where_clause = " WHERE id_cliente = " + client_id;
        }

        return where_clause;
    }

}
