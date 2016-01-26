function CicaheumLedengProtocol(apikey, errorHandler) {
	// IE fix: when window.location.origin is not available 
	if (!window.location.origin) {
		window.location.origin = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port: '');
	}
	var HANDLE_URL = window.location.origin + '/handle.php';
	this.searchPlace = function(query, region, successHandler) {
		$.ajax({
			url: HANDLE_URL,
			type: "POST",
			data: {
				mode: "searchplace",
				version: "2",
				region: region,
				apikey: apikey,
				querystring: query
			},
			success: function(data) {
				successHandler(data);
			},
			error: function(jqxhr, textStatus, error) {
				errorHandler();
			}
		});
	};
	this.findRoute = function(start, finish, locale, successHandler) {
		$.ajax({
			url: HANDLE_URL,
			type: "POST",
			data: {
				mode: "findroute",
				version: "2",
				apikey: apikey,
				start: start,
				finish: finish,
				locale: locale,
				presentation: "desktop"
			},
			success: function(data) {
				successHandler(data);
			},
			error: function(jqxhr, textStatus, error) {
				errorHandler();
			}
		});
	};	
}