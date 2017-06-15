<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <link rel="stylesheet" href="vendor/bootstrap-timepicker/css/bootstrap-timepicker.css" />
    <link rel="stylesheet" href="vendor/bootstrap-select-1.11.0/dist/css/bootstrap-select.min.css" />
    <script src="vendor/bootstrap-select-1.11.0/dist/js/bootstrap-select.min.js"></script>
    <script src="vendor/bootstrap-timepicker/js/bootstrap-timepicker.min.js"></script>
</head>
<body>
    <div class="panel-body">
        <div style="border-bottom: 1px solid #F1F3F6; margin: 0;">
            <div id="wrapper" style="margin: 0 0px 0 0;">
                <div class="content animate-panel">
                    <div class="row">
                        <div class="col-md-12">
                            <div class="hpanel">
                                <div class="panel-body">
                                    <h3>Clientes de Point</h3>
                                </div>
                            </div>
                            <div class="hpanel">
                                <div class="panel-body">
                                    <p align="center">
                                        <button class="btn btn-success " onclick="createModalNewClient();" type="button">
                                            <i class="fa fa-upload"></i>
                                            <span class="bold">Agregar Cliente</span>
                                        </button>
                                    </p>
                                    <table id="cliente_list" cellspacing="0">
                                        <thead>
                                            <tr>
                                                <th>ID Cliente</th>
                                                <th>Nombre Cliente</th>
                                                <th>RUT</th>
                                                <th>Razón Social</th>
                                                <th>Licitaciones Diarias</th>
                                                <th>Glosas de Despacho</th>
                                                <th>Opciones</th>
                                            </tr>
                                        </thead>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <!-- MODAL-->
        <div class="modal fade" id="nuevo_cliente" tabindex="-1" role="dialog" aria-hidden="true">
            <div class="modal-dialog modal-sm" style="width: 500px">
                <div class="modal-content">
                    <div class="modal-header">
                        <h4 class="modal-title" id="crear-cliente-nuevo">Nuevo Cliente</h4>
                    </div>
                    <div class="modal-body">
                        <div class="tab-content">
                            <div id="datosClte">
                                <div class="panel-body">
                                    <form id="new_cliente" name="new_cliente">
                                        <div class="input-group bootstrap-touchspin">
                                            <input type="hidden" id="up_event" />
                                            <table>
                                                <tr>
                                                    <td><input type="hidden" id="id_clte" /></td>
                                                </tr>
                                                <tr>
                                                    <td>Nombre :</td>
                                                    <td><input type="text" id="form_nombre_clte" class="form-control" name="form_nombre_clte" /></td>
                                                </tr>
                                                <tr>
                                                    <td></td>
                                                    <td><p id="error_nom"></p></td>
                                                </tr>
                                                <tr>
                                                    <td>RUT :</td>
                                                    <td><input type="text" id="form_rut" class="form-control" name="form_rut" /></td>
                                                </tr>
                                                <tr>
                                                    <td></td>
                                                    <td><p id="error_rut"></p></td>
                                                </tr>
                                                <tr>
                                                    <td>Razón Social :</td>
                                                    <td><input type="text" id="form_razon" class="form-control" name="form_razon" /></td>
                                                </tr>
                                                <tr>
                                                    <td></td>
                                                    <td><p id="error_razon"></p></td>
                                                </tr>
                                                <tr>
                                                    <td>Licitaciones Diarias :</td>
                                                    <td>
                                                        <select class="form-control" id="form_lic_diarias" name="form_lic_diarias">
                                                            <option value="0">No</option>
                                                            <option value="1">Sí</option>
                                                        </select>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td>Glosas de Despacho :</td>
                                                    <td>
                                                        <select class="form-control" id="form_glosas" name="form_lic_diarias">
                                                            <option value="0">Por Región</option>
                                                            <option value="1">Por Comuna</option>
                                                        </select>
                                                    </td>
                                                </tr>
                                            </table>
                                        </div>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default" data-dismiss="modal">Cancelar</button>
                        <button type="button" id="btn_new_clte" name="btn_new_clte" onClick="saveClient(1)" class="btn btn-primary">Crear</button>
                        <button type="button" id="btn_update_clte" name="btn_update_clte" onclick="saveClient(2);" class="btn btn-primary" style="display: none;">Modificar</button>
                    </div>
                </div>
            </div>
        </div>
        <!-- MODAL CARGA ARCHIVO -->
        <div class="modal fade" id="carga_config" tabindex="-1" role="dialog" aria-hidden="true">
            <div class="modal-dialog modal-sm" style="width: 500px">
                <div class="modal-content">
                    <div class="modal-header">
                        <h4 class="modal-title" id="carga-archivo-configuracion">Cargar Archivo</h4>
                    </div>
                    <div class="modal-body">
                        <div class="tab-content">
                            <div id="datosClte">
                                <div class="panel-body">
                                    <form method="Post" enctype="multipart/form-data" id="cargaArchivo" name="cargaArchivo">
                                        <b>Seleccione Archivo a Subir:</b>
                                        <input type="file" name="archivo_config" id="archivo_config"></input>
                                        <input type="hidden" name="dest" id="dest" value="/Desarrollo/"/> 
                                        <input type="hidden" name="opc" id="opc" value="0" /> 
                                        <input type="hidden" name="client" id="client" value="0" />
                                        <input type="hidden" name="tableDest" id="tableDest" value="" />
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default" data-dismiss="modal">Cancelar</button>
                        <button type="button" class="btn btn-default" value="" id="btnUpload" name="btnUpload" onClick="uploadFiles('cargaArchivo','archivo_config',false);cerrar_modal();">Subir</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- MODAL CONFIGURACIÓN DEL BOLETÍN LICITACIONES -->
        <div class="modal fade" id="config_boletin" tabindex="-1" role="dialog" aria-hidden="true">
            <div class="modal-dialog modal-sm" style="width: 500px">
                <div class="modal-content">
                    <div class="modal-header">
                        <h4 class="modal-title" id="configuracion-boletin">Configurar Boletín de Licitaciones</h4>
                    </div>
                    <div class="modal-body">
                        <div class="tab-content">
                            <form id="boletin" name="boletin">
                                <table>
                                    <tbody>
                                        <tr>
                                            <td>Hora :</td>
                                            <td>
                                                <div class="input-group bootstrap-timepicker timepicker">
                                                    <input id="timepicker1" type="text" class="form-control input-small">
                                                    <span class="input-group-addon"><i class="glyphicon glyphicon-time"></i></span>
                                                </div>
                                             </td>
                                        </tr>
                                        <tr>
                                            <td>
                                                <input type="hidden" id="id_clte_boletin"></input>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>Tipos Licitaciones :</td>
                                            <td>
                                                <select class="selectpicker" id="form_tipos_lic" name="form_tipos_lic" multiple>
                                                    <option value="No Guardada">No Guardada</option>
                                                    <option value="Guardada">Guardada</option>
                                                    <option value="Descartada">Descartada</option>
                                                    <option value="Postulada">Postulada</option>
                                                </select>
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </form>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default" data-dismiss="modal">Cancelar</button>
                        <button type="button" class="btn btn-default" value="" id="btnUpload" name="btnUpload" onClick="saveBulletinSetting();">Subir</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <script src="scripts/clientes.js"></script>
</body>
</html>