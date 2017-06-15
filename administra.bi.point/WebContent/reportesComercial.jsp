<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<script src="scripts/comercial/reportes_comercial.js"></script>
<div class="panel-body">
    <h2>Reportes</h2>
    <br>
    <p>
        <b>(1) Envío Reporte de Postulaciones a Comercial</b> <br> Desde:<input type="text" id="report_initial_date" size="10"
            placeholder="2016-04-01">
        <button id="btn_report" name="btn_report">Enviar Reporte</button>
        <br> <i>* Se debe ejecutar todos los Lunes antes de las 9:30 am.</i>
    </p>
    <p>
        <b>(2) Reporte de Logs </b> <a href="v2/maintainer/export/logs" target="_blank">Descargar Reporte</a>
    </p>
    <p>
		<b>(3) Listado Usuarios Lic Diarias</b> <a
			href="v2/maintainer/export/list_users_lic" target="_blank"> Descargar Reporte</a>
	</p>
</div>