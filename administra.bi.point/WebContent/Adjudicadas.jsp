<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<div class="panel-body">
<p>
	-(1) <a href="/AdministradorBI/CargaDatosMaestros" target="_blank">Carga
		Datos de Maestros</a>
</p>
<p>
	-(2) Carga Archivo <select multiple id="carga_adjudicadas">
		<option value="medicamentos">Medicamentos</option>
	</select>&nbsp;&nbsp;
	<button onclick="carga_archivos('CargaExcel', 'carga_adjudicadas')">Ejecutar
		Carga</button>

</p>
<div>
	<p>
		-(3) Revision Data Cargada <select multiple id="select_rev_adj">
			<option value="rev_med">Medicamentos</option>
		</select> &nbsp;&nbsp;
		<button onclick="revision('adj')">Ejecutar Revisión</button>
	</p>
</div>
<h4>&nbsp;&nbsp;2.1 Proceso de Normalización</h4>
<select multiple id="select_normalizacion_adj">
	<option value="med_norm">Medicamentos</option>
</select>
<p>
	&nbsp;&nbsp;&nbsp;&nbsp;- (1) <a onclick="llamar_servlet_adj(1)"
		href="javascript:void(0);"> Insertar Última Fecha</a>
</p>
<p>
	&nbsp;&nbsp;&nbsp;&nbsp;- (2) <a onclick="llamar_servlet_adj(2)"
		href="javascript:void(0);"> Generar Filtros</a>
</p>
<p>
	&nbsp;&nbsp;&nbsp;&nbsp;- (3) <a onclick="llamar_servlet_adj(3)"
		href="javascript:void(0);"> Normalizar</a>
</p>
<p>
	&nbsp;&nbsp;&nbsp;&nbsp;- (4) <a onclick="llamar_servlet_adj(4)"
		href="javascript:void(0);"> Validar datos normalizados</a>
</p>
<p>
	&nbsp;&nbsp;&nbsp;&nbsp;- (5) <a onclick="llamar_servlet_adj(5)"
		href="javascript:void(0);"> Trasapasar a Histórico</a>
</p>
<p>
	<b>*** Antes de seguir asegúrese de haber realizado los UPDATES
		correspondientes ***</b>
</p>
<p>
	&nbsp;&nbsp;&nbsp;&nbsp;- (6) <a onclick="llamar_servlet_adj(6)"
		href="javascript:void(0);"> Generar Tablas Tipos</a>
</p>
<p>
	&nbsp;&nbsp;&nbsp;&nbsp;- (7) <a onclick="llamar_servlet_adj(7)"
		href="javascript:void(0);"> Generar Combinatoria de Filtros</a>
</p>
<p>
	&nbsp;&nbsp;&nbsp;&nbsp;- (8) <a onclick="llamar_servlet_adj(8)"
		href="javascript:void(0);"> Precalcular Licitaciones Proveedor</a>
</p>
<p>
	&nbsp;&nbsp;&nbsp;&nbsp;- (9) <a onclick="llamar_servlet_adj(9)"
		href="javascript:void(0);"> Precalcular Univ Licitaciones</a>
</p>
<p>
	&nbsp;&nbsp;&nbsp;&nbsp;- (10) <a onclick="llamar_servlet_adj(10)"
		href="javascript:void(0);"> Precalcular Lic Postuladas por Prov</a>
</p>
<p>
	&nbsp;&nbsp;&nbsp;&nbsp;- (11) <a onclick="llamar_servlet_adj(11)"
		href="javascript:void(0);"> Traspaso de Favoritos</a>
</p>
</div>