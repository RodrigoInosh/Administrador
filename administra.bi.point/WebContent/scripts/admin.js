var MAX_FILE_SIZE = 8100000;

// Obtengo la lista de clientes para el select
jQuery.extend({
	getValues : function(url) {
		var result = null;
		$.ajax({
			url : url,
			type : 'GET',
			dataType : 'json',
			async : false,
			success : function(data) {
				result = data;
			}
		});
		return result;
	}
});
var results = $.getValues("/AdministradorBI/v2/funciones/get_clientes/");

// Se ingresan los nuevos valores al select
for (var x = 0; x < results.length; x++) {
	$("#clte_new_user").append(
			$("<option/>").val(results[x].id).text(results[x].c));
	$("#select_clte").append(
			$("<option/>").val(results[x].id).text(results[x].c));
	$("#clte_retro").append(
			$("<option/>").val(results[x].id).text(results[x].c));
	if (results[x].lic == '1') {
		$("#bi-licitacions-clients").append(
				$("<option/>").val(results[x].id).text(results[x].c));
		$("#select_lic").append(
				$("<option/>").val(results[x].id).text(results[x].c));
	}
}

// SECCIÓN DE BI
function changeValueUploadForm(element, id_form) {
	var value = element.value;
	
	switch (value) {
	case "lic":
		$("#"+id_form+" input[id=tableDest]").val("licitaciones");
		break;
	case "adj":
	    $("#"+id_form+" input[id=tableDest]").val("adjudicadas");
		break;
	case "oc":
	    $("#"+id_form+" input[id=tableDest]").val("ordenes");
		break;
	}
}

function uploadFiles(formulario, archivo, is_clasificacion) {
    
	var upload_file = document.getElementById(archivo);
	var upload_file_size = upload_file.files[0].size;
	var formElement = document.getElementById(formulario);
	var data = new FormData(formElement);
	var url = '/AdministradorBI/v2/excel/upload';
	var execute_upload = true;
	var error_message = "";
	
	if (is_clasificacion && $("#option_ondemand").val() == "0") {
	    error_message = "Debe seleccionar un tipo de archivo a clasificar.";
	    execute_upload = false;
	}
	
	if(execute_upload){
    	jQuery.ajax({
    	    url : url,
    		data : data,
    		cache : false,
    		contentType : false,
    		processData : false,
    		async : false,
    		type : 'POST',
    		success : function(data) {
    		    var response_message = data.resp;
    			swal({
    			    title : "Carga de Archivo",
    			    text : response_message
    			});
    		}
    	});
	} else {
	    swal({
            title : "Carga de Archivo",
            text : error_message
        });
	}
	$("#btnUpload").removeAttr('disabled');
}

function getDatos(id, arreglo) {
    var resp;
    for (var i = 0; i < arreglo.length; i++) {
      if (arreglo[i].id == id) {
        resp = arreglo[i];
        break;
      }
    }
    return resp;
  }