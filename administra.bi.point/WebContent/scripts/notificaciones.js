var notifications_users_list;
var id_notification_type = "by_user";
var notifications_list = [];
var table_user_notifications;
var table_user_notifications_is_init = false;
var columns_count = 0;


$( document ).ready(function() {
    
    $(".config_modules").hide();
    $("#tr_clte").show();
    $("#tr_user").show();
    fillClientSelect(clientes);
    createTableConfigRuts(1);
});

$("#tabs").tabs({activate : function(event, ui) {

        var active_notification_tab = $("#tabs").tabs("option","active");
        id_notification_type = $("#tabs ul>li a").eq(active_notification_tab).attr("value");

        $(".config_modules").hide();

        if (id_notification_type == "by_client") {
            $("#tr_clte").show();
            $("#save_notificaction_settings").show();

        } else if (id_notification_type == "by_user") {
            $("#tr_clte").show();
            $("#tr_user").show();
            $("#save_notificaction_settings").show();

        } else if (id_notification_type == "ruts_config") {
            $("#tr_config_ruts").show();
            $("#save_notificaction_settings").hide();
            $("#btn_create_rut").prop('disabled', true);
           
        }
    
        clearSelectpickersSelections();
        destroyNotificationsDataTable();
    
    }
});


$("#save_notificaction_settings").on('click', function() {

    var oTable = $("#table_user_notifications").dataTable();
    var client_id = $("#select_clte").val();
    var user_id = $("#select_user_notif").val();
    var table_notifications_data = oTable.fnGetData();
    var url = "/AdministradorBI/v2/funciones/saveNotificationConfiguration/";

    $("#save_notificaction_settings").attr("disabled", true);
    clearConfigurationArray(table_notifications_data);
    setConfigurationArray(table_notifications_data);

    $.ajax({
        url : url,
        type : "POST",
        data : {
            user : user_id,
            clte : client_id,
            notif : id_notification_type,
            datos : JSON.stringify(notifications_list)
        },
        dataType : "json",
        async : false,
        success : function(data) {
            var ajax_response = data.response;
            var swal_title = "Configuración de Notificaciones";

            if (ajax_response != "-1") {
                swal({
                    title : swal_title,
                    text : "Configuración guardada."
                });
            } else {
                swal({
                    title : swal_title,
                    text : "Error al guardar."
                });
            }
            $("#save_notificaction_settings").attr("disabled", false);
        }
    });
});

$("#import_setting_notifications").on("click", function() {
    openModalNotificationSetting();
});

$("#export_setting_notifications").on("click", function() {
    exportNotificationSetting();
});

$("#upload_notification_settings_file").on('click',function() {

    uploadFiles('notification_setting_form','notification_setting_file', false);
    modalClose('modal_notification_settings');
    updateNotificationsSettingsTable();
});

function clearSelectpickersSelections() {

    $(".selectpicker").selectpicker("val", "");
    $(".selectpicker").selectpicker("refresh");
}

$("#select_clte").on(
        "change",
        function() {

            var href = $($("#notifications_tabs li.active a")[0]).attr("href")
                    .replace("#", "");
            var file_import_type = "3";
            if (id_notification_type == "by_user") {
                getUsersByClient();
            } else if (id_notification_type == "by_client") {
                getUsersByClient();
                getNotificationData(href, id_notification_type);
            }
            updateModalNotificationSettingData(file_import_type);
        });

$("#select_user_notif").on(
        "change",
        function() {

            var href = $($("#notifications_tabs li.active a")[0]).attr("href")
                    .replace("#", "");
            getNotificationData(href, id_notification_type);
        });

$("#table_user_notifications").on(
        "click",
        "input.check_row[type=checkbox]",
        function() {

            var table = $("#table_user_notifications").dataTable();
            var id_checkbx = this.id;
            var checkbox_value = "";

            if (this.value == "0") {
                checkbox_value = "1";
            } else if (this.value == "1") {
                checkbox_value = "0";
            }
            var checked = $(this).is(":checked") ? "checked" : "";
            var row_index = table.fnGetPosition($(this).parent()[0])[0];
            var col_index = table.fnGetPosition($(this).parent()[0])[2];
            table.fnUpdate("<input class='check_row' id='" + id_checkbx
                    + "' value='" + checkbox_value + "' " + "type='checkbox' "
                    + checked + ">", row_index, col_index);
        });

function getUsersByClient() {

    var servlet_url = "/AdministradorBI/v2/funciones/get_usuarios_clte/";
    var select_id = "select_user_notif";
    var client_id = $("#select_clte").val();
    notifications_users_list = [];

    jQuery.extend({
        getValues : function(servlet_url) {
            var result = null;
            $.ajax({
                url : servlet_url,
                data : {
                    dato : client_id
                },
                type : "POST",
                dataType : "json",
                async : false,
                success : function(data) {
                    result = data;
                }
            });
            return result;
        }
    });

    var results = $.getValues(servlet_url);
    notifications_users_list = results;

    clearSelectValues(select_id);
    fillSelectValues(select_id, results);
}

function clearSelectValues(select_id) {

    $("#" + select_id + " option").each(function(index, option) {
        $(option).remove();
    });
}

function fillSelectValues(select_id, data) {

    for (var x = 0; x < data.length; x++) {
        $("#" + select_id).append(
                $("<option/>").val(data[x].id).text(data[x].dato));
    }
    $("#" + select_id).selectpicker("refresh");
}

function getNotificationData(alert_id, notification_type) {

    notifications_list = [];
    columns_count = 0;
    $.ajax({
        url : "/AdministradorBI/v2/funciones/get_notificaciones/",
        type : "POST",
        data : {
            tipo : alert_id
        },
        dataType : "json",
        async : false,
        success : function(data) {
            var $ths = [];
            $ths.push($("<th>").text("Id User"));
            $ths.push($("<th>").text("Usuario"));

            for (var ix = 0; ix < data.length; ix++) {
                notifications_list[columns_count] = data[ix];
                $ths.push($("<th>").text(data[ix].dato));

                columns_count++;
            }

            destroyNotificationsDataTable();

            $("#table_user_notifications thead tr").append($ths);

            createNotificationsDataTable();
            getNotificationSettings(alert_id, notification_type);
        }
    });
}

function createNotificationsDataTable() {

    table_user_notifications = null;
    table_user_notifications = $("#table_user_notifications").dataTable(
            {
                "pageLength" : 30,
                "destroy" : true,
                "order" : [ [ 1, "asc" ] ],
                "lengthMenu" : [ [ 30, 50, -1 ], [ 30, 50, 100 ] ],
                "aoColumnDefs" : [ {
                    "aTargets" : [ 0 ],
                    "visible" : false,
                    "searchable" : false

                }, {
                    "className" : "dt-center",
                    "targets" : "_all"
                } ],
                pagingType : "full",
                initComplete : function(settings, json) {
                    table_user_notifications_is_init = true;
                    $("#table_user_notifications_filter input").attr(
                            "placeholder", "Buscar usuario..");
                },
            });
    table_user_notifications.fnDraw();
    $("#table_user_notifications").show();

    for (var i = 0; i < notifications_users_list.length; i++) {
        var row_table_notifications_settings = [];
        var id_user = notifications_users_list[i].id;
        row_table_notifications_settings.push(id_user);
        row_table_notifications_settings.push(notifications_users_list[i].dato);

        for (var ix = 0; ix < notifications_list.length; ix++) {
            var id_notif = notifications_list[ix].id;
            var user_notification_checkbox = "<input class='check_row' type='checkbox' id='"
                    + id_user + "_" + id_notif + "' value='0'>";
            row_table_notifications_settings.push(user_notification_checkbox);
        }
        table_user_notifications.fnAddData(row_table_notifications_settings);
    }
}

function destroyNotificationsDataTable() {

    if (table_user_notifications_is_init) {

        $("#table_user_notifications").dataTable().fnDestroy();
        $("#table_user_notifications thead tr > th").remove();
        $("#table_user_notifications > tbody").remove();
        table_user_notifications_is_init = false;
    }
}

function getNotificationSettings(alert_id, notification_type) {

    var postData = [];
    var client_id = $("#select_clte").val();

    if (notification_type == "by_user") {
        var user_id = $("#select_user_notif").val();
        postData = {
            tipo : alert_id,
            clte : client_id,
            config : "false",
            user : user_id
        };
    } else if (notification_type == "by_client") {
        postData = {
            tipo : alert_id,
            clte : client_id,
            config : "true"
        }
    }

    $
            .ajax({
                url : "/AdministradorBI/v2/funciones/get_configuracion_notificaciones/",
                type : "POST",
                data : postData,
                dataType : "json",
                async : false,
                success : function(data) {
                    setNotificationsSettingsOnTable(data);
                }
            });
}

function setNotificationsSettingsOnTable(data) {

    var table = $("#table_user_notifications").dataTable();

    for (var ix = 0; ix < data.length; ix++) {
        try {
            var id_to_search = data[ix].user + "_" + data[ix].notif;
            var parent = $("#" + id_to_search).parent()[0];
            var row_index = table.fnGetPosition(parent)[0];
            var col_index = table.fnGetPosition(parent)[2];
            table.fnUpdate("<input class='check_row' id='" + id_to_search
                    + "' value='1' type='checkbox' " + "checked>", row_index,
                    col_index);
        } catch (exception) {
            console.log(exception);
        }
    }
}

function modalClose(modal_id) {

    $('#' + modal_id).modal('hide');
}

function clearConfigurationArray(data) {

    $.each(data, function(index, data) {
        for (var ix = 2; ix < data.length; ix++) {
            notifications_list[ix - 2].user_configs = undefined;
        }
    });
}

function setConfigurationArray(data) {

    $.each(data,function(index, data) {
        for (var ix = 2; ix < data.length; ix++) {
            var datos = $(data[ix]);
            var is_check = datos.is(":checked");
            var id_notif = datos[0].id.split("_")[1];
            var id_user = datos[0].id.split("_")[0];
    
            if (is_check === true && typeof notifications_list[ix - 2].user_configs == "undefined") {
                notifications_list[ix - 2].user_configs = id_user+ ",";
            } else if (is_check === true) {
                notifications_list[ix - 2].user_configs += id_user+ ",";
            }
        }
    });
}

function openModalNotificationSetting() {

    $('#modal_notification_settings').modal('show');
}

function updateModalNotificationSettingData(file_import_type) {

    $("#import_setting_notifications").prop('disabled', false);
    $("#export_setting_notifications").prop('disabled', false);

    var client_id = $("#select_clte").val();
    $('#nofitications_settings input[name=client]').val(client_id);
    $('#nofitications_settings input[name=opc]').val(file_import_type);

    var file = document.getElementById("notification_setting_file");
    file.value = file.defaultValue;
}

function updateNotificationsSettingsTable() {

    var notification_tab_href = $($("#notifications_tabs li.active a")[0])
            .attr("href").replace("#", "");

    if (id_notification_type == "by_user" && $("#select_clte").val() !== ""
            && $("#select_user_notif").val() !== "") {

        getNotificationData(notification_tab_href, id_notification_type);

    } else if (id_notification_type == "by_client"
            && $("#select_clte").val() !== "") {

        getNotificationData(notification_tab_href, id_notification_type);
    }
}

function exportNotificationSetting() {

    var notification_tab_href = $($("#notifications_tabs li.active a")[0])
            .attr("href").replace("#", "");
    var export_notification_option = "";

    if (id_notification_type == "by_user") {
        export_notification_option = "user_notification";
    } else if (id_notification_type == "by_client") {
        export_notification_option = "client_notification";
    }
    var client_id = $("#select_clte").val();
    document.location.href = "/AdministradorBI/ExportReports?client="
            + client_id + "&option=" + export_notification_option;
}