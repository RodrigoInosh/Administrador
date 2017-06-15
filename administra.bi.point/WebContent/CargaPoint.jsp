<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<div class="panel-body">

	<div class="tab-content">
		<p style="color: #b01923; font-size: 20px">
			(1) <b>Carga Archivo Mercados</b>
		</p>
		<form method="Post" enctype="multipart/form-data"
			id="subirArchivoPoint" name="subirArchivoPoint">
			<b>Seleccione Archivo a Subir:</b> <input type="file"
				name="archivoPoint" id="archivoPoint"></input> <input type="hidden"
				name="dest" id="dest" value="/CargaMercados/" /> <input
				type="hidden" name="opc" id="opc" value="0" />
			<button type="button" value="" id="btnUpload" name="btnUpload"
				onClick="uploadFiles('subirArchivoPoint','archivoPoint',false)">Subir</button>
		</form>
	</div>
	<p style="color: #b01923; font-size: 20px">
		(2) <b>Proceso de Actualización</b>
	</p>
	<ul class="nav nav-tabs">
		<li class="active"><a data-toggle="tab" href="#tab-oc">Órdenes
				de Compra</a></li>
		<li class=""><a data-toggle="tab" href="#tab-adj">Adjudicadas</a></li>
	</ul>
	<div class="tab-content">
		<div id="tab-oc" class="tab-pane active">
			<%@include file="Ordenes.jsp"%>
		</div>
		<div id="tab-adj" class="tab-pane">
			<%@include file="Adjudicadas.jsp"%>
		</div>
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
			url : '/AdministradorBI/ValidarDataCargada',
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

	function llamar_servlet(num_paso) {
		var mercados_seleccionados = [];
		var cantidad_mercados_seleccionados = 0;
		$('#select_normalizacion :selected').each(function(i, selected) {
			mercados_seleccionados[i] = $(selected).val();
			cantidad_mercados_seleccionados++;
		})

		var url = "";
		if (cantidad_mercados_seleccionados > 0) {
			switch (num_paso) {
			case 0:
				url = "/AdministradorBI/v2/Normalizar/respaldar/";
				break;
			case 1:
				url = "/AdministradorBI/v2/Normalizar/insert_fecha/";
				break;
			case 2:
				url = "/AdministradorBI/v2/Normalizar/gen_filtros/";
				break;
			case 3:
				url = "/AdministradorBI/v2/Normalizar/normalizar/";
				break;
			case 4:
				url = "/AdministradorBI/v2/Normalizar/validate_normalizacion/";
				break;
			case 5:
				url = "/AdministradorBI/v2/Normalizar/traspasarHistorico/";
				break;
			case 6:
				url = "/AdministradorBI/v2/Normalizar/gen_combi_filtros/";
				break;
			case 7:
				url = "/AdministradorBI/v2/Normalizar/combi_valores/";
				break;
			case 8:
				url = "/AdministradorBI/v2/Normalizar/combi_valores_mes/";
				break;
			case 9:
				url = "/AdministradorBI/v2/Normalizar/combi_valores_ytd/";
				break;
			case 10:
				url = "/AdministradorBI/v2/Normalizar/graf/";
				break;
			case 11:
				url = "/AdministradorBI/v2/Normalizar/trasaparFiltrosMemoria/";
				break;
			case 12:
				url = "/AdministradorBI/v2/Normalizar/trasaparFavoritosInfo/";
				break;
			}

			$.ajax({
				url : url,
				data : {
					info : JSON.stringify(mercados_seleccionados)
				},
				contentType : 'application/json; charset=UTF-8',
				method : 'GET',
				success : function() {
					alert("Carga Mercado:" + mercado + " Finalizada.");
				}
			});
		} else {
			alert("No ha seleccionado ningún mercado");
		}
	}

	function llamar_servlet_adj(num_paso) {
		var mercados_seleccionados = [];
		var cantidad_mercados_seleccionados = 0;
		$('#select_normalizacion_adj :selected').each(function(i, selected) {
			mercados_seleccionados[i] = $(selected).val();
			cantidad_mercados_seleccionados++;
		})

		var url = "";
		/* if (cantidad_mercados_seleccionados > 0) { */
		switch (num_paso) {
		case 1:
			url = "/AdministradorBI/v2/NormalizarAdj/insert_fecha/";
			break;
		case 2:
			url = "/AdministradorBI/v2/NormalizarAdj/generarFiltros/";
			break;
		case 3:
			url = "/AdministradorBI/v2/NormalizarAdj/NormalizaAdj/";
			break;
		case 4:

			break;
		case 5:
			url = "/AdministradorBI/v2/NormalizarAdj/TraspasoHistorico/";
			break;
		case 6:
			url = "/AdministradorBI/v2/NormalizarAdj/TablasTiposLic/";
			break;
		case 7:
			url = "/AdministradorBI/v2/NormalizarAdj/CombiFiltrosAdj/";
			break;
		case 8:
			url = "/AdministradorBI/v2/NormalizarAdj/LicProveedor/";
			break;
		case 9:
			url = "/AdministradorBI/v2/NormalizarAdj/UnivLicitaciones/";
			break;
		case 10:
			url = "/AdministradorBI/v2/NormalizarAdj/LicPostuladasProveedor/";
			break;
		case 11:
			url = "/AdministradorBI/v2/NormalizarAdj/trasaparFavoritosInfo/";
			break;
		}

		$.ajax({
			url : url,
			data : {
				info : JSON.stringify(mercados_seleccionados)
			},
			contentType : 'application/json; charset=UTF-8',
			method : 'GET',
			success : function() {
				alert("Revisión finalizada");
			}
		});
		//} else {
		//	alert("No ha seleccionado ningún mercado");
		//}
	}
</script>