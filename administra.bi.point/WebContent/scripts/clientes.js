var clientes;
var client_table;
var client_table_is_init = false;
var is_no_postulation_motive_module = "2";
var is_glosa_by_region = "3";
var is_glosa_by_commune = "4";
var default_select_box_option = "Seleccione una Opción";

$(document).ready(function() {

    $('#timepicker1').timepicker({
        defaultTime : false,
        minuteStep : 1,
        showMeridian : false
    });    
    getClientsData();
    createTableClients();
});

function createTableClients() {
    
    client_table = $('#cliente_list').DataTable(
            {
                "pageLength" : 15,
                "order" : [ [ 1, "asc" ] ],
                data : clientes,
                columns : [ {
                    data : 'id'
                }, {
                    data : 'c'
                }, {
                    data : 'rut'
                }, {
                    data : 'razon'
                }, {
                    data : 'config'
                } ],
                "lengthMenu" : [ [ 15, 30, 50, -1 ], [ 15, 30, 50, 100 ] ],
                "aoColumnDefs" : [
                        {
                            width : 200,
                            "aTargets" : [ 4 ],
                            "mRender" : function(data, type, full) {
                                var licitaciones = full.lic;

                                if (licitaciones == "0") {
                                    text = "No";
                                } else {
                                    text = "Sí";
                                }
                                return text;
                            }
                        },
                        {
                            width : 200,
                            "aTargets" : [ 5 ],
                            "mRender" : function(data, type, full) {
                                var config = full.config;

                                if (config == "0") {
                                    text = "Por Región";
                                } else {
                                    text = "Por Comuna";
                                }
                                return text;
                            }
                        },
                        {
                            "aTargets" : [ 6 ],
                            "mRender" : function(data, type, full) {
                                return createClientsTableOptions(full);
                            }
                        } ],
                pagingType : 'full',
                drawCallback : function(settings) {
                    if (client_table_is_init) {
                        setPaginationButtons();
                    }
                },
                initComplete : function(settings, json) {
                    client_table_is_init = true;
                    $('#clientes_list_filter input').attr('placeholder', 'Buscar usuario..');
                },
            }
    );
    if (client_table_is_init) {
        client_table.draw();
    }
}

function setPaginationButtons() {
    
    $('#cliente_list_first > a').text('');
    $('#cliente_list_first > a').append(
            '<i class="fa fa-angle-double-left"></i>');
    $('#cliente_list_previous > a').text('');
    $('#cliente_list_previous > a').append('<i class="fa fa-caret-left"></i>');
    $('#cliente_list_next > a').text('');
    $('#cliente_list_next > a').append('<i class="fa fa-caret-right"></i>');
    $('#cliente_list_last > a').text('');
    $('#cliente_list_last > a').append(
            '<i class="fa fa-angle-double-right"></i>');
    var pageInfo = client_table.page.info();
    $('<li id="page_info"></li>').insertBefore('#cliente_list_next');
    $('#page_info').html((pageInfo.page + 1) + ' de ' + pageInfo.pages);
}

function getClientsData() {
    
    $.ajax({
        url : "/AdministradorBI/v2/funciones/get_clientes/",
        async : false,
        success : function(result) {
            if (result.length > 0) {
                clientes = result;
            }
        }
    });
}

function createModalNewClient() {
    
    clearClientModal();
    $("#btn_new_clte").show();
    $("#btn_update_clte").hide();
    $('#nuevo_cliente').modal('toggle');
}

function saveClient(save_option) {
    
    var validate = true;
    var client_id = $("#id_clte").val();
    var client_name = $("#form_nombre_clte").val();
    var client_rut = $("#form_rut").val();
    var client_company_name = $("#form_razon").val();
    var recives_daily_licitations = $("#form_lic_diarias").val();
    var type_glosses = $("#form_glosas").val();
    var is_valid_client_name = validateField("nom", client_name);
    var is_valid_client_rut = validateField("rut", client_rut);
    var is_valid_client_company_name = validateField("razon", client_company_name);

    if (is_valid_client_company_name && is_valid_client_name && is_valid_client_rut) {
        
        $.ajax({
            url : '/AdministradorBI/v2/funciones/cliente',
            data : {
                opc : save_option,
                id : client_id,
                nom : client_name,
                rut : client_rut,
                razon : client_company_name,
                lic_diarias : recives_daily_licitations,
                glosas : type_glosses
            },
            contentType : 'application/json; charset=UTF-8',
            method : 'POST',
            success : function(data) {
                getResponseMessage(data, client_name);
            }
        });
        
        $('#nuevo_cliente').modal('toggle');
    }
}

function getResponseMessage(data, client_name){
    
    var swal_title = "Creación de Cliente"; 
    if (data.resp == "clte_existe") {
        
        swal({
            title : swal_title,
            text : "El cliente " + client_name + " ya existe."
        });
    } else if (data.resp == "ok") {
        
        swal({
            title : swal_title,
            text : "El cliente " + client_name + " creado correctamente."
        });
        updateClientsTableAndSelects();
    } else {
        
        swal({
            title : swal_title,
            text : "Error al intentar crear Cliente."
        });
    }
}

function updateClientsTableAndSelects(){
    
    getClientsData();
    reloadClientsSelectOptions();

    client_table.clear().draw();
    client_table.rows.add(clientes).draw();
}

function validateField(field_name, field_value) {
    
    var validated = false;
    if (field_value == "") {
        $("#error_" + field_name).html("<p style='color:red;'>Campo Obligatorio.</p>");
    } else {
        validated = true;
        $("#error_" + field_name).html("");
    }
    return validated;
}

function reloadClientsSelectOptions() {
    
    $('#select_clte').empty();
    $('#clte_new_user').empty();
    $('#clte_retro').empty();
    $('#select_lic').empty();
    $("#clte_new_user").append($("<option/>").val(0).text(default_select_box_option));
    $("#select_clte").append($("<option/>").val(0).text(default_select_box_option));
    $("#clte_retro").append($("<option/>").val(0).text(default_select_box_option));
    $("#select_lic").append($("<option/>").val(0).text("Todos"));

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

    fillClientSelects(results);
}

function fillClientSelects(clients_data){

    for (var x = 0; x < clients_data.length; x++) {
        
        $("#select_clte").append($("<option/>").val(clients_data[x].id).text(clients_data[x].c));
        $("#clte_new_user").append($("<option/>").val(clients_data[x].id).text(clients_data[x].c));
        $("#clte_retro").append($("<option/>").val(clients_data[x].id).text(clients_data[x].c));

        if (clients_data[x].lic == '1') {
            $("#select_lic").append($("<option/>").val(clients_data[x].id).text(clients_data[x].c));
        }
    }
}

function updateClientModal(idcliente) {

    clearClientModal();
    $("#btn_new_clte").hide();
    $("#btn_update_clte").show();
    
    var datos_cliente = getDatos(idcliente, clientes);
    $("#id_clte").val(idcliente);
    $("#form_nombre_clte").val(datos_cliente.c);
    $("#form_rut").val(datos_cliente.rut);
    $("#form_razon").val(datos_cliente.razon);
    $("#form_lic_diarias").val(datos_cliente.lic).change();
    $("#form_glosas").val(datos_cliente.config).change();
    $('#nuevo_cliente').modal('toggle');
}

function clearClientModal() {
    
    $("#id_clte").val("");
    $("#form_nombre_clte").val("");
    $("#form_rut").val("");
    $("#form_razon").val("");
    $("#form_lic_diarias").val("0").change();
    $("#form_glosas").val("0").change();
}

function loadModalConfigurationFile(client_id, module_option, clte_nombre) {
    
    var title = "Carga Archivo";
    var target_database_table = "";
    var upload_config_file = document.getElementById("archivo_config");
    upload_config_file.value = upload_config_file.defaultValue;

    if (module_option == is_no_postulation_motive_module) {
        
        title += " '<b>Motivos No Postulación'</b><br> Cliente: <b>" + clte_nombre + "</b>";
        target_database_table = "motivo_no_postulacion";
    } else if (module_option == is_glosa_by_region) {
        
        title += " '<b>Glosas por Región'</b><br>Cliente: <b>" + clte_nombre + "</b>";
        target_database_table = "cliente_glosa_region";
    } else if (module_option == is_glosa_by_commune) {
        
        title += " '<b>Glosas por Comuna'</b><br>Cliente: <b>" + clte_nombre + "</b>";
        target_database_table = "cliente_glosa_comuna";
    }

    $('#cargaArchivo input[name=opc]').val("2");
    $('#cargaArchivo input[name=client]').val(client_id);
    $('#cargaArchivo input[name=tableDest]').val(target_database_table);
    $('#carga-archivo-configuracion').html(title);
    $('#carga_config').modal('show');
}

function getBulletinSettings(idcliente) {
    
    $("#id_clte_boletin").val(idcliente);
    $.ajax({
        url : '/AdministradorBI/v2/funciones/get_config_boletin/',
        data : {
            id : idcliente
        },
        type : 'GET',
        dataType : 'json',
        async : false,
        success : function(data) {
            result = data;

            if (result.length > 0) {
                var array = result[0].lic.split(",");
                var hora = result[0].hora;
                var min = result[0].min;
                var time = hora + ":" + min;
                $('.selectpicker').selectpicker('val', array);
                $('.selectpicker').selectpicker('render');
                $('#timepicker1').timepicker('setTime', time);
            } else {
                $('.selectpicker').selectpicker('deselectAll');
                $('.selectpicker').selectpicker('render');
                $('#timepicker1').timepicker('setTime', '');
            }
        }
    });
    $('#config_boletin').modal('toggle');
}

function saveBulletinSetting() {
    
    var idcliente = $("#id_clte_boletin").val();
    var licitaciones = $('#form_tipos_lic').selectpicker('val').join(';');
    var time = $('#timepicker1').val();

    $.ajax({
        url : '/AdministradorBI/v2/funciones/save_config_boletin/',
        data : {
            id : idcliente,
            lic : licitaciones,
            time : time
        },
        type : 'POST',
        dataType : 'json',
        async : false,
        success : function(data) {
            
            var swal_title = "Boletín diario de licitaciones";
            if (data.resp == 'ok') {
                swal({
                    title : swal_title,
                    text : "Cambios guardados correctamente."
                });
            } else if (data.resp == 'err') {
                swal({
                    title : swal_title,
                    text : "Error al guardar cambios."
                });
            }
            $('#config_boletin').modal('toggle');
        }
    });
}

function createClientsTableOptions(client_data){
    
    var options = '<button onclick="updateClientModal(\''+ client_data.id + '\');" class="btn btn-info" type="button">'
        +'<i class="fa fa-edit"></i> <span class="bold"> Modificar</span></button>&nbsp;'
        +'<button onclick="getBulletinSettings(\''+ client_data.id + '\');" class="btn btn-info" type="button">'
        +'<i class="fa fa-list-alt"></i> <span class="bold">Config. Boletín</span></button>&nbsp;'
        +'<button onclick="loadModalConfigurationFile(\''+ client_data.id + '\',2,\''+ client_data.c + '\');" class="btn btn-success" type="button">'
        +'<i class="fa fa-upload"></i> <span class="bold">Motivos</span></button>&nbsp;'
        +'<button onclick="loadModalConfigurationFile(\''+ client_data.id + '\',3,\''+ client_data.c + '\');" class="btn btn-warning" type="button">'
        + '<i class="fa fa-upload"></i> <span class="bold">Glosas Región</span></button>&nbsp;'
        +'<button onclick="loadModalConfigurationFile(\''+ client_data.id + '\',4,\''+ client_data.c + '\');" class="btn btn-warning" type="button">'
        + '<i class="fa fa-upload"></i> <span class="bold">Glosas Comuna</span></button>';
    
    return options;
}