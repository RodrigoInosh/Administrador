<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<div class="panel-body">
	<p>
		-(1) <a href="/AdministradorBI/CargaDatos2?estado=stage"
			target="_blank">Carga Datos de Maestros</a>
	</p>
	<p>
		-(2) Carga Archivo <select multiple id="carga_archivo">
			<option value="medicamentos">Medicamentos</option>
			<option value="nutricion">Fórmulas Nutricionales</option>
			<option value="oficina">Suministros de Oficina</option>
			<option value="tecnologia">Tecnología</option>
		</select>&nbsp;&nbsp;
		<button onclick="carga_archivos('CargaDatosExcel', 'carga_archivo')">Ejecutar
			Carga</button>

	</p>
	<div>
		<p>
			-(3) Revision Data Cargada <select multiple id="select_rev">
				<option value="rev_med">Medicamentos</option>
				<option value="rev_nut">Fórmulas Nutricionales</option>
				<option value="rev_ofi">Suministros de Oficina</option>
				<option value="rev_tec">Tecnología</option>
			</select> &nbsp;&nbsp;
			<button onclick="revision('ord')">Ejecutar Revisión</button>
		</p>
	</div>
	<h4>&nbsp;&nbsp;2.1 Proceso de Normalización</h4>
	<select multiple id="select_normalizacion">
		<option value="med_norm">Medicamentos</option>
		<option value="nut_norm">Fórmulas Nutricionales</option>
		<option value="ofi_norm">Suministros de Oficina</option>
		<option value="tec_norm">Tecnología</option>
	</select>
	<p>
		&nbsp;&nbsp;&nbsp;&nbsp;- (0) <a onclick="llamar_servlet(0)"
			href="javascript:void(0);"> Respaldar Históricos</a>
	</p>
	<p>
		&nbsp;&nbsp;&nbsp;&nbsp;- (1) <a onclick="llamar_servlet(1)"
			href="javascript:void(0);"> Insertar Última Fecha</a>
	</p>
	<p>
		&nbsp;&nbsp;&nbsp;&nbsp;- (2) <a onclick="llamar_servlet(2)"
			href="javascript:void(0);"> Generar Filtros</a>
	</p>
	<p>
		&nbsp;&nbsp;&nbsp;&nbsp;- (3) <a onclick="llamar_servlet(3)"
			href="javascript:void(0);"> Normalizar</a>
	</p>
	<p>
		&nbsp;&nbsp;&nbsp;&nbsp;- (4) <a onclick="llamar_servlet(4)"
			href="javascript:void(0);"> Validar datos normalizados</a>
	</p>
	<p>
		&nbsp;&nbsp;&nbsp;&nbsp;- (5) <a onclick="llamar_servlet(5)"
			href="javascript:void(0);"> Trasapasar a Histórico</a>
	</p>
	<p>
		<b>*** Antes de seguir asegúrese de haber realizado los UPDATES
			correspondientes ***</b>
	</p>
	<p>
		&nbsp;&nbsp;&nbsp;&nbsp;- (6) <a onclick="llamar_servlet(6)"
			href="javascript:void(0);"> Generar Combi Filtros</a>
	</p>
	<p>
		&nbsp;&nbsp;&nbsp;&nbsp;- (7) <a onclick="llamar_servlet(7)"
			href="javascript:void(0);"> Generar Combi Valores</a>
	</p>
	<p>
		&nbsp;&nbsp;&nbsp;&nbsp;- (8) <a onclick="llamar_servlet(8)"
			href="javascript:void(0);"> Generar Combi Valores Mes</a>
	</p>
	<p>
		&nbsp;&nbsp;&nbsp;&nbsp;- (9) <a onclick="llamar_servlet(9)"
			href="javascript:void(0);"> Generar Combi Valores YTD</a>
	</p>
	<p>
		&nbsp;&nbsp;&nbsp;&nbsp;- (10) <a onclick="llamar_servlet(10)"
			href="javascript:void(0);"> Generar Gráfico Precalculado</a>
	</p>
	<p>
		&nbsp;&nbsp;&nbsp;&nbsp;- (11) <a onclick="llamar_servlet(11)"
			href="javascript:void(0);"> Traspasar Combi Filtros a Memoria</a>
	</p>
	<p>
		&nbsp;&nbsp;&nbsp;&nbsp;- (12) <a onclick="llamar_servlet(12)"
			href="javascript:void(0);"> Traspasar Datos de Favoritos</a>
	</p>
</div>