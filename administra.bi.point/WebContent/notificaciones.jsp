<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<body>
    <div class="panel-body" id="tabs">
        <ul id="notifications_tabs" class="nav nav-tabs">
            <li class="active"><a data-toggle="tab" href="#1" value="by_user">De Cambios</a></li>
            <li class=""><a data-toggle="tab" href="#2" value="by_client">Por Cliente</a></li>
            <li class=""><a data-toggle="tab" href="#3" value="ruts_config">Ruts Proveedores</a></li>
        </ul>
        <div class="tab-content">
            <div id="1" class="tab-pane active"></div>
            <div id="2" class="tab-pane"></div>
            <div id="3" class="tab-pane"></div>
        </div>
        <table>

            <!-- MODULE NOTIFICACIONES POR CLIENTE  -->

            <tr id="tr_clte" class="config_modules">
                <td><b>Seleccionar Cliente:</b><br> <select id="select_clte" name="select_clte" class="selectpicker"
                    title="Seleccione un Cliente">
                </select></td>
                <td><br>&nbsp;&nbsp;&nbsp;&nbsp;
                    <button type="button" disabled class="btn btn-primary" id="import_setting_notifications">Importar Configuración</button></td>
                <td><br>&nbsp;
                    <button type="button" disabled class="btn btn-success" id="export_setting_notifications">Exportar Configuración Actual</button></td>
            </tr>

            <!-- MODULE NOTIFICACIONES DE CAMBIOS  -->

            <tr id="tr_user" class="config_modules">
                <td><b>Seleccionar Usuario:</b><br> <select id="select_user_notif" name="select_user_notif" class="selectpicker"
                    title="Seleccione un Usuario">
                </select></td>
            </tr>
        </table>
        <!-- MODULE CONFIG RUTS  -->
        <div id="tr_config_ruts" class="config_modules">
            <div>
                <b>Seleccione Cliente:</b><br> <select id="select_cliente_config_rut" name="select_cliente_config_rut" class="selectpicker"
                    title="Seleccione un Cliente">
                </select>&nbsp;&nbsp;&nbsp;&nbsp;
                <button id="btn_create_rut" class="btn btn-success" type="button">
                    <i class="fa fa-upload"></i> <span class="bold">Agregar Rut</span>
                </button>
            </div>
            <div align="right" style="width: 550px;">
                <br>
                <table id="table_ruts_by_client" style="display: true" cellspacing="0">
                    <thead>
                        <tr>
                            <th>Id</th>
                            <th>Rut</th>
                            <th>Eliminar</th>
                        </tr>
                    </thead>
                </table>
            </div>
        </div>
        <table id="table_user_notifications" style="display: none" cellspacing="0">
            <thead>
                <tr>
                </tr>
            </thead>
        </table>
        <button type="button" id="save_notificaction_settings" name="save_notificaction_settings" class="btn btn-primary">Guardar</button>
    </div>

    <!-- Modal de Importar configuracion -->

    <div class="modal fade" id="modal_notification_settings" tabindex="-1" role="dialog" aria-hidden="true">
        <div class="modal-dialog modal-sm" style="width: 500px">
            <div class="modal-content">
                <div class="modal-header">
                    <h4 class="modal-title">Cargar Configuración de Notificaciones</h4>
                </div>
                <div class="modal-body">
                    <div class="tab-content">
                        <div id="nofitications_settings">
                            <div class="panel-body">
                                <form method="Post" enctype="multipart/form-data" id="notification_setting_form" name="notification_setting_form">
                                    <b>Seleccione Archivo a Subir:</b> <input type="file" name="notification_setting_file"
                                        id="notification_setting_file"></input> <input type="hidden" name="dest" id="dest" value="/Desarrollo/"></input>
                                    <input type="hidden" name="opc" id="opc" value="3"></input> <input type="hidden" name="client" id="client"
                                        value=""></input>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Cancelar</button>
                    <button type="button" class="btn btn-default" value="" id="upload_notification_settings_file"
                        name="upload_notification_settings_file">Subir</button>
                </div>
            </div>
        </div>
    </div>

    <!-- Modal CREAR RUT -->

    <div class="modal fade" id="modal_create_ruts" tabindex="-1" role="dialog" aria-hidden="true">
        <div class="modal-dialog modal-sm" style="width: 500px">
            <div class="modal-content">
                <div class="modal-header">
                    <h4 class="modal-title">Asignación Rut Proveedor</h4>
                </div>
                <div class="modal-body">
                    <div class="tab-content">
                        Ingrese Rut: <input class="form-control" id="new_rut" type="text" placeholder="Ej: XX.XXX.XXX-X" />
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-danger" data-dismiss="modal">Cancelar</button>
                    <button type="button" class="btn btn-success" id="create_rut">Crear</button>
                </div>
            </div>
        </div>
    </div>

    <!-- CARGA SCRIPTS JS -->

    <script src="scripts/notifications/ruts_config.js"></script>
    <script src="scripts/notificaciones.js"></script>
</body>
</html>