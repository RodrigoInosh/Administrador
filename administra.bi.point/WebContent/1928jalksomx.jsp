<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
	if (request.getParameter("key") != null) {
		String key = request.getParameter("key");
		if ("ejfinon094j".equals(key)) {
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Point</title>
    <link rel="stylesheet" href="vendor/fontawesome/css/font-awesome.css" />
    <link rel="stylesheet" href="vendor/metisMenu/dist/metisMenu.css" />
    <link rel="stylesheet" href="vendor/animate.css/animate.css" />
    <link rel="stylesheet" href="vendor/bootstrap/dist/css/bootstrap.css" />
    <link rel="stylesheet" href="vendor/multi-select/css/multi-select.css" type="text/css" />
    <link rel="stylesheet" href="//cdn.datatables.net/1.10.9/css/jquery.dataTables.min.css" />
    <link rel="stylesheet" href="fonts/pe-icon-7-stroke/css/pe-icon-7-stroke.css" />
    <link rel="stylesheet" href="fonts/pe-icon-7-stroke/css/helper.css" />
    <link rel="stylesheet" href="vendor/bootstrap/dist/css/bootstrap.css" />
    <link rel="stylesheet" href="vendor/sweetalert/lib/sweet-alert.css" />
    <script src="vendor/sweetalert/lib/sweet-alert.min.js"></script>
    <script src="vendor/jquery/dist/jquery.min.js"></script>
    <script src="vendor/jquery-ui/jquery-ui.min.js"></script>
    <script src="vendor/slimScroll/jquery.slimscroll.min.js"></script>
    <script src="vendor/bootstrap/dist/js/bootstrap.min.js"></script>
    <script src="vendor/metisMenu/dist/metisMenu.min.js"></script>
    <script src="vendor/iCheck/icheck.min.js"></script>
    <script src="vendor/sparkline/index.js"></script>
    <script src="vendor/multi-select/js/jquery.multi-select.js"></script>
    <script src="vendor/jquery-validation/jquery.validate.min.js"></script>
    <script src="vendor/datatables/media/js/jquery.dataTables.min.js"></script>
    <script src="vendor/datatables_plugins/integration/bootstrap/3/dataTables.bootstrap.min.js"></script>
    <script src="//cdn.ckeditor.com/4.5.11/standard/ckeditor.js"></script>
</head>
<body>
	<h1>Manager Point</h1>
	<ul class="nav nav-tabs">
		<li class="active"><a data-toggle="tab" href="#tab-comercial">Área Comercial</a></li>
		<li class=""><a data-toggle="tab" href="#tab-bi">Área BI</a></li>
	</ul>
	<div class="tab-content">
		<div id="tab-comercial" class="tab-pane active">
			<%@include file="seccionComercial.jsp"%>
		</div>
		<div id="tab-bi" class="tab-pane">
			<%@include file="seccionBI.jsp"%>
		</div>
	</div>
	<script src="scripts/admin.js"></script>
</body>
</html>
<%
	}
	}
%>