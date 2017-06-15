<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<div class="panel-body">
    <ul class="nav nav-tabs">
        <li class="active"><a data-toggle="tab" href="#tab-reportesComercial">Reportes</a></li>
        <li class=""><a data-toggle="tab" href="#tab-clientes">Clientes</a></li>
        <li class=""><a data-toggle="tab" href="#tab-usuarios">Usuarios</a></li>
        <li class=""><a data-toggle="tab" href="#tab-notificaciones">Notificaciones</a></li>
    </ul>
    <div class="tab-content">
        <div id="tab-reportesComercial" class="tab-pane active">
            <%@include file="reportesComercial.jsp"%>
        </div>
        <div id="tab-clientes" class="tab-pane">
            <%@include file="clientes.jsp"%>
        </div>
        <div id="tab-usuarios" class="tab-pane">
            <%@include file="Usuarios.jsp"%>
        </div>
        <div id="tab-notificaciones" class="tab-pane">
            <%@include file="notificaciones.jsp"%>
        </div>
    </div>
</div>
