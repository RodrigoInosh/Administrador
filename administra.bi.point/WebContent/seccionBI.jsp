<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div class="panel-body">
    <ul class="nav nav-tabs">
        <li class="active"><a data-toggle="tab" href="#tab-reportesBI">Reportes</a></li>
        <li><a data-toggle="tab" href="#tab-clasificacion">Clasificacion OnDemand</a></li>
        <li><a data-toggle="tab" href="#tab-licitacionBI">Licitaciones</a></li>
        <li><a data-toggle="tab" href="#tab-diccionario">Diccionario</a></li>
    </ul>
    <div class="tab-content">
        <div id="tab-reportesBI" class="tab-pane active">
            <%@include file="ReportesBI.jsp"%>
        </div>
        <div id="tab-clasificacion" class="tab-pane">
            <%@include file="clasificacion.jsp"%>
        </div>
        <div id="tab-licitacionBI" class="tab-pane">
            <div class="row">
                <%@include file="bi/licitaciones.jsp"%>
            </div>
        </div>
        <div id="tab-diccionario" class="tab-pane">
            <div class="row">
                <%@include file="bi/diccionario.jsp"%>
            </div>
        </div>
    </div>
</div>
