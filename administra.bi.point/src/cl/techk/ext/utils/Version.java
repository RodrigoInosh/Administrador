package cl.techk.ext.utils;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * clase version entrega la ultima version subida a produccion
 * @version version 3.4
 * @author felipe aguilera
 */

@WebServlet("/version")
public class Version extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public Version() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.getWriter().append("v:0.2->date:13/04/2016 18:30:00");
		response.getWriter().append("v:0.1->date:07/04/2016 18:30:00");
	}
}
