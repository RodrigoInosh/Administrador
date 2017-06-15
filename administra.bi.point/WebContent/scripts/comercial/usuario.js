var usuarios;
var tablaUsuario;
var tableUsersinit = false;

$("#clte_new_user").append($("<option/>").val(0).text("Seleccione una Opción"));
$("#select_perfil").append($("<option/>").val(0).text("Seleccione una Opción"));
// Obtener los mercados
getData("user_mercados", "get_mercados");
// Obtengo los perfiles de usuarios
getData("select_perfil", "get_perfiles_list");

/** **SECCIÓN DE LA DATATABLE** */

// Obtengo la lista de todos los usuarios en la bdd
getUsuariosAll();

tablaUsuario = $('#productos_cliente')
		.DataTable(
				{
					"pageLength" : 15,
					"order" : [ [ 1, "asc" ] ],
					data : usuarios,
					columns : [ {
						data : 'id'
					}, {
						data : 'idperfil'
					}, {
						data : 'nom'
					}, {
						data : 'idclte'
					}, {
						data : 'clte'
					}, {
						data : 'user'
					}, {
						data : 'mail'
					}, {
						data : 'hab'
					} ],
					"lengthMenu" : [ [ 15, 30, 50, -1 ], [ 15, 30, 50, 100 ] ],
					"aoColumnDefs" : [
							{
								"aTargets" : [ 0 ],
								"visible" : false,
								"searchable" : false
							},
							{
								"aTargets" : [ 1 ],
								"visible" : false,
								"searchable" : false
							},
							{
								"aTargets" : [ 3 ],
								"visible" : false,
								"searchable" : false
							},
							{
								width : 200,
								"aTargets" : [ 8 ],
								"mRender" : function(data, type, full) {
									var habilitado = full.hab;

									if (habilitado == "Sí") {
										text = "Deshabilitar";
										icon = "close";
									} else {
										text = "Habilitar";
										icon = "check-square";
									}
									return '<button onclick="habilitar(\''
											+ full.id
											+ '\', \''
											+ habilitado
											+ '\');" class="btn btn-success" type="button" width="100%">'
											+ '<i class="fa fa-' + icon
											+ '"></i> <span class="bold">' + ''
											+ text + '</span></button>';
								}
							},
							{
								"aTargets" : [ 9 ],
								"mRender" : function(data, type, full) {
									return '<button onclick="update_user(\''
											+ full.id
											+ '\');" class="btn btn-info" type="button">'
											+ '<i class="fa fa-edit"></i> <span class="bold">'
											+ 'Modificar</span></button>';
								}
							} ],
					pagingType : 'full',
					drawCallback : function(settings) {
						if (tableUsersinit) {
							setPaginationButtons();
						}
					},
					initComplete : function(settings, json) {
						tableUsersinit = true;
						$('#productos_cliente_filter input').attr(
								'placeholder', 'Buscar usuario..');
					},
				});
if (tableUsersinit) {
	tablaUsuario.draw();
}

/** ******FIN SECCIÓN DATA TABLE****** */

function setPaginationButtons() {
	// Set Pagination buttons
	$('#productos_cliente_first > a').text('');
	$('#productos_cliente_first > a').append(
			'<i class="fa fa-angle-double-left"></i>');
	$('#productos_cliente_previous > a').text('');
	$('#productos_cliente_previous > a').append(
			'<i class="fa fa-caret-left"></i>');
	$('#productos_cliente_next > a').text('');
	$('#productos_cliente_next > a')
			.append('<i class="fa fa-caret-right"></i>');
	$('#productos_cliente_last > a').text('');
	$('#productos_cliente_last > a').append(
			'<i class="fa fa-angle-double-right"></i>');
	var pageInfo = tablaUsuario.page.info();
	$('<li id="page_info"></li>').insertBefore('#productos_cliente_next');
	$('#page_info').html((pageInfo.page + 1) + ' de ' + pageInfo.pages);
}

function getUsuariosAll() {
	$.ajax({
		url : "/AdministradorBI/v2/funciones/get_usuariosAll/",
		async : false,
		success : function(result) {
			if (result.length > 0) {
				usuarios = result;
			}
		}
	});
}

function create_usuario() {

	var validate = true;
	// Datos del usuario
	var nombre_user = $("#new_user_name").val();
	var email_user = $("#new_user_email").val();
	var usuario = $("#new_user").val();
	/* var pass_user = $("#new_user_pass").val(); */
	var cliente_user = $("#clte_new_user").val();
	var perfil_user = $("#select_perfil").val();

	if (!nombre_user) {
		validate = false;
	}
	if (!email_user || !ValidateEmail(email_user)) {
		validate = false;
	}
	if (!usuario) {
		validate = false;
	}
	/*
	 * if (!pass_user) { validate = false; }
	 */
	if (cliente_user == 0) {
		validate = false;
	}
	if (perfil_user == 0) {
		validate = false;
	}

	// Datos de permisos del usuario
	var mercados_seleccionados = [];
	$('#user_mercados :selected').each(function(i, selected) {
		mercados_seleccionados[i] = $(selected).val();
	});
	var modulos_seleccionados = [];
	$('#user_modulos :selected').each(function(i, selected) {
		modulos_seleccionados[i] = $(selected).val();
	});

	if (mercados_seleccionados.length == 0 || modulos_seleccionados.length == 0) {
		validate = false;
	}

	if (!validate) {
		swal({
			title : "Creación de Usuario",
			text : "Faltan campos por llenar"
		});
	} else {
		$.ajax({
			url : '/AdministradorBI/v2/funciones/crear_usuario',
			data : {
				mercados : JSON.stringify(mercados_seleccionados),
				modulos : JSON.stringify(modulos_seleccionados),
				nombre_user : nombre_user,
				email_user : email_user,
				usuario : usuario,
				/* pass_user : pass_user, */
				cliente_user : cliente_user,
				perfil_user : perfil_user
			},
			contentType : 'application/json; charset=UTF-8',
			method : 'POST',
			success : function(data) {

				if (data.resp == "user_existe") {
					swal({
						title : "Creación de Usuario",
						text : "El usuario " + usuario
								+ " ya existe en el cliente "
								+ $("#clte_new_user option:selected").text()
					});
				} else if (data.resp == "ok") {
					swal({
						title : "Creación de Usuario",
						text : "El usuario " + usuario
								+ " creado correctamente."
					});
					getUsuariosAll();

					tablaUsuario.clear().draw();
					tablaUsuario.rows.add(usuarios).draw();
				}
			}
		});
		$('#nuevo_usuario').modal('toggle');
	}
}
function update_user(idusuario) {
	limpiar_modal_user();
	var url = "/AdministradorBI/v2/funciones/get_mercadosUser";
	$("#btn_crear").hide();
	$("#btn_update").show();
	jQuery.extend({
		getValues : function(url) {
			var result = null;
			$.ajax({
				url : url,
				type : 'POST',
				data : {
					id : idusuario
				},
				dataType : 'json',
				async : false,
				success : function(data) {
					result = data;
				}
			});
			return result;
		}
	});
	var url2 = "/AdministradorBI/v2/funciones/get_modulosUser";
	jQuery.extend({
		getValues : function(url) {
			var result = null;
			$.ajax({
				url : url,
				type : 'POST',
				data : {
					id : idusuario
				},
				dataType : 'json',
				async : false,
				success : function(data) {
					result = data;
				}
			});
			return result;
		}
	});
	var datos_usuario = getDatos(idusuario, usuarios);
	var results_mercados_usuario = $.getValues(url);
	var results_modulos_usuario = $.getValues(url2);

	$("#id_user").val(idusuario);
	$("#user_mercados").val(results_mercados_usuario.m.split(","));
	$("#user_modulos").val(results_modulos_usuario.m.split(","));

	$("#new_user_name").val(datos_usuario.nom);
	$("#new_user_email").val(datos_usuario.mail);
	$("#new_user").val(datos_usuario.user);

	$("#clte_new_user").val(datos_usuario.idclte).change();
	$("#select_perfil").val(datos_usuario.idperfil).change();

	$('#nuevo_usuario').modal('toggle');
}

function updatear() {
	var validate = true;
	// Datos del usuario
	var idusuario = $("#id_user").val();
	var nombre_user = $("#new_user_name").val();
	var email_user = $("#new_user_email").val();
	var usuario = $("#new_user").val();
	var restablecer_pass = $('#pass_chkbox').is(':checked');
	var pass_user = $("#new_user_pass").val();
	var cliente_user = $("#clte_new_user").val();
	var perfil_user = $("#select_perfil").val();

	if (!nombre_user) {
		validate = false;
	}
	if (!email_user || !ValidateEmail(email_user)) {
		validate = false;
	}
	if (!usuario) {
		validate = false;
	}
	if (cliente_user == 0) {
		validate = false;
	}
	if (perfil_user == 0) {
		validate = false;
	}

	// Datos de permisos del usuario
	var mercados_seleccionados = [];
	$('#user_mercados :selected').each(function(i, selected) {
		mercados_seleccionados[i] = $(selected).val();
	})
	var modulos_seleccionados = [];
	$('#user_modulos :selected').each(function(i, selected) {
		modulos_seleccionados[i] = $(selected).val();
	})

	if (mercados_seleccionados.length == 0 || modulos_seleccionados.length == 0) {
		validate = false;
	}

	if (!validate) {
		swal({
			title : "Modificación de Usuario",
			text : "Faltan campos por llenar"
		});
	} else {
		$.ajax({
			url : '/AdministradorBI/v2/funciones/update_usuario',
			data : {
				mercados : JSON.stringify(mercados_seleccionados),
				modulos : JSON.stringify(modulos_seleccionados),
				nombre_user : nombre_user,
				email_user : email_user,
				usuario : usuario,
				pass_user : restablecer_pass,
				cliente_user : cliente_user,
				perfil_user : perfil_user,
				idusuario : idusuario
			},
			contentType : 'application/json; charset=UTF-8',
			method : 'POST',
			success : function(data) {

				if (data.resp == "user_existe") {
					swal({
						title : "Modificación de Usuario",
						text : "El usuario " + usuario
								+ " ya existe en el cliente "
								+ $("#clte_new_user option:selected").text()
					});
				} else if (data.resp == "ok") {
					getUsuariosAll();

					tablaUsuario.clear().draw();
					tablaUsuario.rows.add(usuarios).draw();
					swal({
						title : "Modificación de Usuario",
						text : "El usuario " + usuario
								+ " creado correctamente."
					});
				}
			}
		});
		$('#nuevo_usuario').modal('toggle');
	}
}
function habilitar(idusuario, estado) {

	var estado_new = estado === "No" ? 1 : 0;

	var url = "/AdministradorBI/v2/funciones/habilitar";

	$.ajax({
		url : url,
		data : {
			estado : estado_new,
			user : idusuario
		},
		contentType : 'application/json; charset=UTF-8',
		method : 'GET',
		success : function(data) {
			var resp = data.resp;
			if (resp == "ok") {
				getUsuariosAll();

				tablaUsuario.clear().draw();
				tablaUsuario.rows.add(usuarios).draw();
				alert("Usuario Modificado");
			} else {
				alert("Error");
			}
		},
		error : function(xhr, ajaxOptions, thrownError) {
			alert(xhr.status);
			alert(thrownError);
		}
	});

}
function getData(select, servlet) {
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
	var results = $.getValues("/AdministradorBI/v2/funciones/" + servlet + "/");

	// Se ingresan los nuevos valores al select
	for (var x = 0; x < results.length; x++) {
		$("#" + select).append(
				$("<option/>").val(results[x].id).text(results[x].c));
	}
}

function modal_new_user() {
	limpiar_modal_user();
	$("#btn_crear").show();
	$("#btn_update").hide();
	$('#nuevo_usuario').modal('toggle');
}

function ValidateEmail(mail) {
	if (/^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/.test(mail)) {
		return (true)
	}
	return (false)
}

function getDatosUsuario(id) {
	var resp;
	for (var i = 0; i < usuarios.length; i++) {
		if (usuarios[i].id == id) {
			resp = usuarios[i];
			break;
		}
	}
	return resp;
}

function limpiar_modal_user() {
	$("#id_user").val("");
	$("#new_user_name").val("");
	$("#new_user_email").val("");
	$("#new_user").val("");
	$("#new_user_pass").val("");
	$("#clte_new_user").val("0");
	$("#select_perfil").val("0");
	$("#user_mercados option:selected").removeAttr("selected");
	$("#user_modulos option:selected").removeAttr("selected");
}