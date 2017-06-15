<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<div class="panel-body">
	<ul class="nav nav-tabs">
		<li class="active"><a data-toggle="tab" href="#tab-clasificacionID">Clasificacion OnDemand</a></li>
		<li class=""><a data-toggle="tab" href="#tab-licitacionesID">Licitaciones</a></li>
		<li class=""><a data-toggle="tab" href="#tab-cargaPoint">Actualización Point</a></li>
	</ul>
	<div class="tab-content">
		<div id="tab-clasificacionID" class="tab-pane active">
			<%@include file="clasificacion.jsp"%>
		</div>
		<div id="tab-licitacionesID" class="tab-pane">
			<%@include file="Licitaciones.jsp"%>
		</div>
		<div id="tab-cargaPoint" class="tab-pane">
			<%@include file="CargaPoint.jsp"%>
		</div>
	</div>
</div>