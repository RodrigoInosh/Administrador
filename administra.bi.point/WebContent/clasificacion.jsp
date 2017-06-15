<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<div class="panel-body">
	<b>(1) Seleccionar Tipo de Archivo</b> 
    <select id='option_ondemand' name='option_ondemand' onChange="changeValueUploadForm(this, 'uploadForm')">
		<option value='0'>Seleccione Opción</option>
		<option value='lic'>Licitaciones</option>
		<option value='adj'>Adjudicadas</option>
		<option value='oc'>Órdenes de Compra</option>
	</select>
	<form method="Post" enctype="multipart/form-data" id="uploadForm" name="uploadForm">
		Select File:
        <input type="file" name="uploadFile" id="uploadFile"></input>
		<input type="hidden" name="tableDest" id="tableDest" value="0"></input>
		<input type="hidden" name="dest" id="dest" value="/Desarrollo/"></input>
		<input type="hidden" name="opc" id="opc" value="1"></input>
		<button type="button" value="" id="btnUpload" name="btnUpload" onClick="uploadFiles('uploadForm','uploadFile',true)">Upload</button>
	</form>
</div>