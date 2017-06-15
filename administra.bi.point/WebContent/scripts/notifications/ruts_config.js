var table_ruts;
var array_ruts_client = null;

function getClientsRutsConfig(client_id) {

    $.ajax({
        url : "/AdministradorBI/v2/notifications_by_rut/get_list/" + client_id,
        async : false,
        success : function(result) {
            array_ruts_client = result.results;
        }
    });
}

function fillClientSelect(clients_data) {
    
    for (var x = 0; x < clients_data.length; x++) {
        $("#select_cliente_config_rut").append(
                $("<option/>").val(clients_data[x].id).text(clients_data[x].c));
    }
}

$("#select_cliente_config_rut").on("change", function() {

    var client_id = $("#select_cliente_config_rut").val();
    getClientsRutsConfig(client_id);
    table_ruts.clear().draw();
    table_ruts.rows.add(array_ruts_client).draw();
    $("#btn_create_rut").prop('disabled', false);
});

$("#btn_create_rut").click(function() {
    
    $('#modal_create_ruts').modal('show');
});

$("#create_rut").click(function() {
    
    var client_id = $('#select_cliente_config_rut').val();
    var rut = $('#new_rut').val();
    saveNewRut(client_id, rut);
});


function reloadRutsConfigTable(client_id) {
    
    getClientsRutsConfig(client_id);
    table_ruts.clear().draw();
    table_ruts.rows.add(array_ruts_client).draw();
}


function saveNewRut(client_id, rut) {
    
    $.ajax({
        url : "/AdministradorBI/v2/notifications_by_rut/create/",
        method : "POST",
        data : {
            client_id : client_id,
            rut : rut,
        },
        async : false,
        success : function(result) {
            reloadRutsConfigTable(client_id);
            modalClose('modal_create_ruts');
            if(result.succesfull){
                generateResponseAlert('Nuevo rut',"Rut agregado correctamente.");
            }else{
                generateResponseAlert('Nuevo rut',"Error creando rut. "+result.error);
            }
        }
    });
}

function generateResponseAlert(title, description){
    
    swal({
        title : title,
        text : description
    });
}

function deleteRutConfig(row_id) {
    $.ajax({
        url : "/AdministradorBI/v2/notifications_by_rut/delete/",
        method : "POST",
        data : {
            row_id : row_id,
        },
        async : false,
        success : function(result) {
            var client_id = $('#select_cliente_config_rut').val();
            if(result.succesfull){
                generateResponseAlert('Eliminación de Rut',"Rut eliminado correctamente.");
            }else{
                generateResponseAlert('Eliminación de Rut',"Error eliminando rut.");
            }         
            reloadRutsConfigTable(client_id);
            modalClose('modal_create_ruts');
        }
    });
}

function createTableConfigRuts() {
    table_ruts = $('#table_ruts_by_client').DataTable({
        "pageLength" : 15,
        "order" : [ [ 1, "asc" ] ],
        data : array_ruts_client,
        columns : [ {
            data : 'id'
        }, {
            data : 'rut'
        }, {
            data : 'id_cliente'
        } ],
        "lengthMenu" : [ [ 10, 40, 20 ], [ 10, 40, 20 ] ],
        "aoColumnDefs" : [
        {
            "aTargets" : [ 0 ],
            "visible" : true,
            "searchable" : false
        },
        {
            "aTargets" : [ 1 ],
            "visible" : true,
            "searchable" : false
        },
        {
            "aTargets" : [ 2 ],
            "mRender" : function(data, type, full) {
                return '<div align="right"><button onclick="deleteRutConfig(\''
                    + full.id + '\');" class="btn btn-danger" type="button">'
                    + '<i class="fa fa-trash-o"></i> <span class="bold">'
                    + 'Eliminar</span></button></div>';
            }
        } ],
        pagingType : 'full',
        drawCallback : function(settings) {
            setPaginationButtons("table_ruts_by_client");
        }
    });
}

function setPaginationButtons(table_id) {
    
    $('#'+table_id+'_first > a').text('');
    $('#'+table_id+'_first > a').html(
            '<i class="fa fa-angle-double-left"></i>');
    $('#'+table_id+'_previous > a').text('');
    $('#'+table_id+'_previous > a').append('<i class="fa fa-caret-left"></i>');
    $('#'+table_id+'_next > a').text('');
    $('#'+table_id+'_next > a').append('<i class="fa fa-caret-right"></i>');
    $('#'+table_id+'_last > a').text('');
    $('#'+table_id+'_last > a').append(
            '<i class="fa fa-angle-double-right"></i>');
    var pageInfo = client_table.page.info();
    $('<li id="page_info"></li>').insertBefore('#'+table_id+'_next');
    $('#page_info').html((pageInfo.page + 1) + ' de ' + pageInfo.pages);
}