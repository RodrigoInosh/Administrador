function BILicitations($el) {
	this.$el = $el;
	this.ui = {};
	
	this.init = function() {
		this.showCurrentDailyMessage();
		this.bindingUI();
		
		CKEDITOR.replace('licitation-daily-message');
	};
	
	this.bindingUI = function() {
		this.ui.client_licitations = $('.js-client-licitations', this.$el);
		this.ui.btn_send_client_licitations = $('.js-send-client-licitations', this.$el);
		this.ui.send_customer_daily_notification = $('.js-send-customer-daily-notification', this.$el);

		this.ui.btn_send_client_licitations.on('click', this.sendLicitations);
		
		this.ui.daily_email_message = $('.js-daily-email-message', this.$el);
		this.ui.test_email = $('.js-test-email', this.$el);
		this.ui.btn_test_email = $('.js-send-test-email', this.$el);
		this.ui.btn_save_daily_email_message = $('.js-save-daily-email-message', this.$el);
		this.ui.send_test_notification = $('.js-send-test-notification', this.$el);
		
		this.ui.btn_test_email.on('click', this.sendTestEmail);
		this.ui.btn_save_daily_email_message.on('click', this.updateDailyMessage);
		this.ui.test_email.on('keypress', this.sendTestEmailKeypress);
	};
	
	this.showCurrentDailyMessage = function() {
		$.ajax({
			method: "GET",
			url: 'v2/licitaciones/mensaje_email_diario',
			success: function(response) {
				CKEDITOR.instances['licitation-daily-message'].setData(response.message);
			}
		});
	};
	
	this.sendTestEmailKeypress = (function(_this) {
		return function(e) {
			if (e.which == 13) {
				_this.sendTestEmail(e);
			}
		};
	})(this);
	
	this.sendTestEmail = (function(_this) {
		return function(e) {
			var testEmail = _this.ui.test_email.val();
			var emailMessage = CKEDITOR.instances['licitation-daily-message'].getData();
			e.preventDefault();
			
			if (!_this.validateEmail(testEmail)) {
				_this.ui.test_email.parents('.form-group').addClass('has-error');
				_this.ui.test_email.focus();
				return false;
			} else {
				_this.ui.test_email.parents('.form-group').removeClass('has-error');
			}
			
			_this.ui.btn_test_email.addClass('disabled');
			_this.ui.send_test_notification.text('Enviando correo de prueba');
			
			$.ajax({
				method: "POST",
				url: 'v2/licitaciones/enviar_email_prueba',
				data: {
					email: testEmail,
					message: emailMessage
				},
				success: function(response) {
					_this.ui.btn_test_email.removeClass('disabled');
					_this.ui.send_test_notification.text('');
				}
			});
		};
	})(this);
	
	this.updateDailyMessage = (function(_this) {
		return function(e) {
			e.preventDefault();
			var emailMessage = CKEDITOR.instances['licitation-daily-message'].getData();
			
			_this.ui.btn_save_daily_email_message.addClass('disabled');
			
			$.ajax({
				method: "POST",
				url: 'v2/licitaciones/actualizar_mensaje_email',
				data: {
					message: emailMessage
				},
				success: function(response) {
					_this.ui.btn_save_daily_email_message.removeClass('disabled');
				}
			});
		};
	})(this);
	
	this.validateEmail = function(email) {
	    var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
	    return re.test(email);
	}
	
	this.sendLicitations = (function(_this) {
		return function(e) {
			e.preventDefault();
			var clientId = _this.ui.client_licitations.val();
	
			_this.ui.send_customer_daily_notification.text('Enviando correo');
			
			$.ajax({
				url: '/AdministradorBI/ExcelLicitacionesMail',
				data: {
					clte: clientId
				},
				contentType: 'application/json; charset=UTF-8',
				method: 'GET',
				success: function(response) {
					alert("Fin envío licitaciones");
					_this.ui.send_customer_daily_notification.text('');
					CKEDITOR.instances['licitation-daily-message'].updateElement();
					CKEDITOR.instances['licitation-daily-message'].setData('');
				}
			});
		};
	})(this);
}

$(document).ready(function() {		
	var biLicitations = new BILicitations($('.bi-licitations-section'));
	biLicitations.init();
});