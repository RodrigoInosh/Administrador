<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script src="scripts/bi/diccionario.js"></script>
<title>Point</title>
</head>
<body>
	<div style="padding-left: 30px; padding-top: 30px;">
		<div class="form-group">
			<label for="select_dictionary_market">Seleccione el mercado: </label>
			<select id='select_dictionary_market' name='select_dictionary_market'>
				<option value='0'>Seleccione una opción</option>
			</select>
		</div>
		<button type="button" class="btn btn-primary" id="btn_open_upload_modal"
			name="dictionary_upload">
			<i class="fa fa-upload" aria-hidden="true"></i> Cargar Diccionario
		</button>
		<strong hidden id="text_load_dictionary">Cargando
			diccionario....</strong>
		<button type="button" class="btn btn-success" id="dictionary_download"
			name="dictionary_download">
			<i class="fa fa-download" aria-hidden="true"></i> Exportar
			Diccionario
		</button>
	</div>

	<!-- MODAL UPLOAD DICTIONARY -->
	<div class="modal fade" id="upload_dictionary_file" tabindex="-1"
		role="dialog" aria-hidden="true">
		<div class="modal-dialog modal-sm" style="width: 500px">
			<div class="modal-content">
				<div class="modal-header">
					<h4 class="modal-title">Cargar Diccionario</h4>
				</div>
				<div class="modal-body">
					<div class="tab-content">
						<form enctype="multipart/form-data" accept-charset="utf-8"
							method="POST" id="dictionary_form">
							<div hidden class="form-group">
								<label for="dictionary_market">Seleccione el mercado: </label>
								<input id='dictionary_market' name='dictionary_market'>
								</input>
							</div>
							<div class="form-group">
								<input id="file" type="file" name="file">
							</div>
						</form>
					</div>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-danger" data-dismiss="modal">Cancelar</button>
					<button type="button" class="btn btn-primary" id="dictionary_upload">Subir</button>
				</div>
			</div>
		</div>
	</div>
</body>
</html>