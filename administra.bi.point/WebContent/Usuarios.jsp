<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<body>
	<div class="panel-body">
		<p>
		<div style="border-bottom: 1px solid #F1F3F6; margin: 0;">
			<div id="wrapper" style="margin: 0 0px 0 0;">
				<div class="content animate-panel">
					<div class="row">
						<div class="col-md-12">
							<div class="hpanel">
								<div class="panel-body">
									<h3>Usuarios de Point</h3>
								</div>
							</div>
							<div class="hpanel">
								<div class="panel-body">
									<p align="center">
										<button class="btn btn-success " onclick="modal_new_user();"
											type="button">
											<i class="fa fa-upload"></i> <span class="bold">Agregar
												Usuario</span>
										</button>
									</p>
									<table id="productos_cliente" cellspacing="0">
										<thead>
											<tr>
												<th>Id user</th>
												<th>perfil</th>
												<th>Nombre Usuario</th>
												<th>id Cliente</th>
												<th>Cliente</th>
												<th>Usuario</th>
												<th>E-Mail</th>
												<th>Habilitado</th>
												<th>Deshabilitar</th>
												<th>Modificar</th>
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
		<div class="modal fade" id="nuevo_usuario" tabindex="-1" role="dialog"
			aria-hidden="true">
			<div class="modal-dialog modal-sm" style="width: 500px">
				<div class="modal-content">
					<div class="modal-header">
						<h4 class="modal-title" id="licitacion-id-crear-simul">Nuevo
							Usuario</h4>
					</div>
					<div class="modal-body">
						<ul class="nav nav-tabs">
							<li class="active"><a data-toggle="tab"
								href="#tab-userDatos">Datos Usuario</a></li>
							<li class=""><a data-toggle="tab" href="#tab-modulosUser">Módulos
									Usuario</a></li>
						</ul>
						<div class="tab-content">
							<div id="tab-userDatos" class="tab-pane active">
								<div class="panel-body">
									<form id="tester">
										<div class="input-group bootstrap-touchspin">
											<input type="hidden" id="up_event" />
											<table>
												<tr>
													<td><input type="hidden" id="id_user" /></td>
												</tr>
												<tr>
													<td>Nombre :</td>
													<td><input type="text" id="new_user_name"
														class="form-control" name="new_user_name" /></td>
												</tr>
												<tr>
													<td>Email :</td>
													<td><input type="text" id="new_user_email"
														class="form-control" name="new_user_email" /></td>
												</tr>
												<tr>
													<td>Usuario :</td>
													<td><input type="text" id="new_user"
														class="form-control" name="new_user" /></td>
												</tr>
												<tr>
													<td>Password:</td>
													<td><input type="text" id="new_user_pass"
														class="form-control" name="new_user_pass" disabled /></td>
													<td><label><input type="checkbox"
															id="pass_chkbox" name="pass_chkbox" value="pass_chkbox">Reestablecer</label></td>
												</tr>
												<tr>
													<td></td>
													<td></td>
													<td><span style="color: red">Sólo cuando se
															modifica usuario</span></td>
												</tr>
												<tr>
													<td>Cliente :</td>
													<td><select class="form-control" id="clte_new_user"
														name="clte_new_user">
													</select></td>
												</tr>
												<tr>
													<td>Perfil :</td>
													<td><select class="form-control" id="select_perfil"
														name="select_perfil">
													</select></td>
												</tr>
											</table>
										</div>
									</form>
								</div>
							</div>
							<div id="tab-modulosUser" class="tab-pane">
								<div class="panel-body">
									<form id="tester">
										<div class="input-group bootstrap-touchspin">
											<input type="hidden" id="up_event" />
											<table>
												<tr>
													<td>Mercado :</td>
													<td><select multiple id="user_mercados"></select></td>
												</tr>
												<tr>
													<td>Módulos :</td>
													<td><select multiple id="user_modulos">
															<option value="lic">Licitaciones</option>
															<option value="adj_res">Adjudicadas</option>
															<option value="adj_din">Adjudicadas Vista
																Dinámica</option>
															<option value="adj_des">Zona de Descarga Adj</option>
															<option value="oc">Órdenes de Compra</option>
															<option value="oc_din">Órdenes de Compra Vista
																Dinámica</option>
															<option value="oc_Des">Zona de Descarga OC</option>
															<option value="repo">Reportes</option>
															<option value="mant_cat">Mantenedor Catálogo
																Productos</option>
															<option value="mant_rel">Mantenedor Merc
																Relevante</option>
															<option value="mant_asig">Mantenedor Responsable
																Lic</option>
															<option value="mant_glo">Mantenedor Glosa de
																Despacho</option>
													</select></td>
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
						<button type="button" id="btn_crear" name="btn_crear"
							onclick="create_usuario();" class="btn btn-primary">Crear</button>
						<button type="button" id="btn_update" name="btn_update"
							onclick="updatear();" class="btn btn-primary"
							style="display: none;">Modificar</button>
					</div>
				</div>
			</div>
		</div>
	</div>
	<script src="scripts/comercial/usuario.js"></script>
</body>
</html>