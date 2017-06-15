<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<div class="panel-body">
	<div class="tab-content col-md-12 bi-licitations-section">
		<div class="row">
			<div class="col-md-9">
				<div id="send-customer-daily-email">
		  			<div class="form-group">
						<label>Enviar e-mail licitaciones diarias (enviar antes de las 10:50 am)</label>
						<select id="bi-licitacions-clients" class="form-control js-client-licitations" name="bi-licitacions-clients">
							<option value="0">Todos</option>
						</select>
					</div>
					<div class="form-group">
						<a href="#" class="btn btn-default js-send-client-licitations">Enviar</a>
						<strong class="js-send-customer-daily-notification"></strong>
					</div>
				</div>
				<hr>
				<div id ="edit-daily-message">
					<div class="form-group">
						<label>Editar mensaje e-mail licitaciones diarias</label>
						<textarea name="licitation-daily-message" class="form-control js-daily-email-message"></textarea>
					</div>
					<div class="form-group">
						<a href="#" class="btn btn-default js-save-daily-email-message">Guardar</a>
					</div>
					<div class="form-group">
						<input class="form-control js-test-email" type="text" placeholder="example@techk.cl" />
					</div>
					<div class="form-group">
						<a href="#" class="btn btn-default js-send-test-email">Enviar prueba</a>
						<strong class="js-send-test-notification"></strong>
					</div>
				</div>
			</div>
			<div class="col-md-3">
				<div class="panel panel-default">
  					<div class="panel-body">
						<ul class="nav">
							<li>
								<a href="#send-customer-daily-email">Enviar e-mail licitaciones diarias</a>
							</li>
							<li>
								<a href="#edit-daily-message">Editar mensaje e-mail licitaciones diarias</a>
							</li>
						</ul>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>

<script type="text/javascript" src="scripts/bi/licitaciones.js"></script>
