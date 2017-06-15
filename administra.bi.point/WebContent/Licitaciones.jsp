<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<div class="panel-body">
	<div class="tab-content">
		<p>
			<a href="/AdministradorBI/TraspasoLicitacionesAPoint" target="_blank"
				style="color: #b01923; font-size: 18px;">(1) <b>Traspasar
					Licitaciones a BD Point</b></a>
		</p>
		<p style="color: #b01923; font-size: 18px">
			(2) <b>Subir archivo licitaciones diarias</b>
		</p>
		<form method="Post" enctype="multipart/form-data" id="subirArchivo"
			name="subirArchivo">
			<b>Seleccione Archivo a Subir:</b><input type="file"
				name="archivo_subir" id="archivo_subir"></input> <input
				type="hidden" name="dest" id="dest" value="/LoadLicitaciones/" /> <input
				type="hidden" name="opc" id="opc" value="0" />
			<button type="button" value="" id="btnUpload" name="btnUpload"
				onClick="uploadFiles('subirArchivo','archivo_subir',false)">Subir</button>
		</form>
		<p>
			<a
				href="/AdministradorBI/LoadExcelLicitaciones?bd=point_licitaciones"
				target="_blank" style="color: #b01923; font-size: 18px;">(3) <b>Cargar
					Licitaciones Diarias</b></a> - * cargar antes el excel en carpeta
			/LoadLicitaciones/ del server.
		</p>
		<p>
			<span style="color: #b01923; font-size: 18px;"> (4) <b>Cargar
					Licitaciones Histórica</b>
			</span> <select id="clte_retro" name="clte_retro">
				<option value="0">Seleccione Cliente</option>
			</select>&nbsp;&nbsp;
			<button onclick="ejecutar_retro()">Ejecutar Carga</button>
			<br>&nbsp;<b>(*) Cargar Licitaciones Retroactivas (abiertas)
				* Se deberia ejecutar preferentemente cuando se carguen los maestros
				de clientes y/o keywords</b>
		</p>
		<p>
			<span style="color: #b01923; font-size: 18px;">(5) <b>Envío
					Mail Licitaciones Diarias</b></span> <select id="select_lic" name="select_lic">
				<option value="0">Todos</option>
			</select>&nbsp;&nbsp;
			<button onclick="enviar_licDiarias()">Enviar</button>
		</p>

	</div>
</div>

<script type="text/javascript">
	function revision(tipo) {

		var select_name = "select_rev";
		if (tipo != "ord") {
			select_name += "_" + tipo;
		}
		var mercados_seleccionados = [];
		$('#' + select_name + ' :selected').each(function(i, selected) {
			mercados_seleccionados[i] = $(selected).val();
		})

		$.ajax({
			url : '/AdministradorBI/v2/ValidarData/validar/',
			data : {
				info : JSON.stringify(mercados_seleccionados),
				tipo : tipo
			},
			contentType : 'application/json; charset=UTF-8',
			method : 'GET',
			success : function() {
				alert("Revisión completa");
			}
		});
	}

	function carga_archivos(servlet, multiselect) {
		$('#' + multiselect + ' :selected').each(
				function(i, selected) {
					var mercado = $(selected).val();
					var url = '/AdministradorBI/' + servlet
							+ '?estado=stage&mercado=' + mercado;

					$.ajax({
						url : url,
						contentType : 'application/json; charset=UTF-8',
						method : 'GET',
						success : function() {
							alert("Carga Mercado:" + mercado + " Finalizada.");
						}
					});
				})
	}

	function envio_reporte() {
		var fecha_ini = $("#fecha_ini").val();
		var fecha_fin = $("#fecha_fin").val();
		var dateReg = /^\d{4}[-]\d{2}[-]\d{2}$/;

		if (!dateReg.test(fecha_ini) || !dateReg.test(fecha_fin)) {
			alert("Error en una de las fechas");
		} else if (fecha_ini > fecha_fin) {
			alert("Fecha 'Hasta' debe ser mayor a 'Desde'");
		} else {
			var url = "/AdministradorBI/ReportePostulaciones?fecha_ini="
					+ fecha_ini + "&fecha_fin=" + fecha_fin;

			$.ajax({
				url : url,
				contentType : 'application/json; charset=UTF-8',
				method : 'GET',
				success : function() {
					alert("Fin Carga");
				}
			});
		}
	}

	function enviar_licDiarias() {
		var id_cliente = $("#select_lic").val();

		$.ajax({
			url : '/AdministradorBI/ExcelLicitacionesMail',
			data : {
				clte : id_cliente
			},
			contentType : 'application/json; charset=UTF-8',
			method : 'GET',
			success : function() {
				alert("Fin envío licitaciones");
			}
		});
	}

	function ejecutar_retro(element) {
		var id_cliente = $("#clte_retro").val();
		$.ajax({
			url : '/AdministradorBI/LoadHistoricLicitacionesData',
			data : {
				clte : id_cliente
			},
			contentType : 'application/json; charset=UTF-8',
			method : 'GET',
			success : function() {
				alert("Fin retro");
			}
		});
	}
</script>