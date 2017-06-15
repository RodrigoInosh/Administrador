$(document).ready(function() {
    
	$("#select_dictionary_market").selectpicker("val", "");
	$("#dictionary_market").selectpicker("val", "");

	getDictionaryMarkets();

	$('#btn_open_upload_modal').on('click', function() {
		$("#upload_dictionary_file").modal('toggle');
	});
	
	$('#dictionary_upload').on('click', function() {

		var is_empty_file = validateFileSize();
		var data = new FormData(document.getElementById("dictionary_form"));
		var market_id = $("#dictionary_market").val();

		if (validateFileSize()) {
			setMessageLoadDictionary("Debe seleccionar un archivo");
			return;
		}
		
		if (market_id > 0) {
			$("#btn_open_upload_modal").prop('disabled', true);
			$("#upload_dictionary_file").modal('hide');
			$("#text_load_dictionary").show();
			uploadDictionary(data);
		} else {
			$("#upload_dictionary_file").modal('hide');
			setMessageLoadDictionary("Debe seleccionar un mercado");
		}
	});
	
	$('#select_dictionary_market').on('change', function() {
		var market_id = $('#select_dictionary_market').val();
		$('#dictionary_market').val(market_id);
	});
	

	$('#dictionary_download').on('click', function(){
	   
	    var market_id = $('#select_dictionary_market').val();
	    
	    if(market_id > 0) {
	        $.ajax({
	            type: "GET",
	                url: "/AdministradorBI/v2/dictionary/export/"+market_id,
	                statusCode: {
	                    500: function (data) {
	                        var json_object = jQuery.parseJSON(data.responseText );
	                        setMessageLoadDictionary("Error: " +  json_object.error);
	                    },
	                    200: function () {
	                        window.location.href = "/AdministradorBI/v2/dictionary/export/"+market_id;
	                    }
	                }
	            });
	    } else {
	        setMessageLoadDictionary("Debe seleccionar un mercado");
	    }
	});
});

function uploadDictionary(form_data) {
	$.ajax({
		type : "POST",
		url : "/AdministradorBI/v2/dictionary/upload/",
		data : form_data,
		enctype : 'multipart/form-data',
		async : true,
		processData : false,
		contentType : false,
		dataType : "json",
		success : function(response) {
			setMessageLoadDictionary(response.resp);
		},
		error : function(response) {
			setMessageLoadDictionary(response.resp);
		}
	});
}

function setMessageLoadDictionary(text) {

	$("#btn_open_upload_modal").prop('disabled', false);
	$("#text_load_dictionary").hide();
	swal({
		title : "Sección Diccionario",
		text : text
	});
}

function fillSelectMarket(data) {

	for (var x = 0; x < data.length; x++) {
		$("#select_dictionary_market").append(
				$("<option/>").val(data[x].id).text(data[x].mercado));
	}

	$("#select_dictionary_market").selectpicker("refresh");
}

function getDictionaryMarkets() {

	$.ajax({
		url : "/AdministradorBI/v2/dictionary/dictionary_markets/",
		async : false,
		success : function(result) {
			fillSelectMarket(result.markets);
		}
	});
}

function validateFileSize() {

	var empty_file = true;
	try {
		var upload_file = document.getElementById("file");
		var upload_file_size = upload_file.files[0].size;
		empty_file = false;
	} catch (error) {
		console.log(error);
	}

	return empty_file
}