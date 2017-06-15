window.onload = function() {
    
    $("#btn_report").on('click', function() {
        
        var fecha_ini = $("#report_initial_date").val();
        var dateReg = /^\d{4}[-]\d{2}[-]\d{2}$/;
        $("#btn_repo").attr("disabled", true);
        if (!dateReg.test(fecha_ini)) {
            alert("Error en la fecha");
        } else {
            window.location.href = "/AdministradorBI/ReportePostulaciones?fecha_ini="+ fecha_ini;
        }
    });
}