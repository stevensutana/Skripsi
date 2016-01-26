$(document).ready(function() {
	var protocol = new CicaheumLedengProtocol(apikey, function(message) {
		clearSecondaryAlerts();
		showAlert(messageConnectionError, 'alert');
	});
	// input_text['start'] and input_text['finish'] is set at index.php
	// coordinates['start'] and coordinates['finish'] is set at index.php
	// locale is set at index.php
	var markers = {start: null, finish: null};
	var resultOverlays = [];

    markers['start'] = new google.maps.Marker({icon: {url: marker_image['start'], anchor: new google.maps.Point(34, 29)}});
    markers['finish'] = new google.maps.Marker({icon: {url: marker_image['finish'], anchor: new google.maps.Point(0, 29)}});
    var map = new google.maps.Map(document.getElementById("map-canvas"),
        mapOptions);
    var mapCanvas = $('#map-canvas');
    mapCanvas.height($(window).height() - mapCanvas.position().top);

	// Initialize input & select boxes.
	$.each(['start', 'finish'], function(sfIndex, sfValue) {
		var placeInput = $('#' + sfValue + 'Input');
		var placeSelect = $('#' + sfValue + 'Select');
		
		google.maps.event.addListener(markers[sfValue], 'dragend', function(e) {
			placeInput.val(latLngToString(e.latLng));
		});
		placeInput.change(function() {
			addMarker (null, false, null, markers[sfValue]);
			coordinates[sfValue] = null;
		});
		placeSelect.change(function() {
			coordinates[sfValue] = $(this).val();
			checkCoordinatesThenRoute(coordinates);
		});
		
	});
	
	resetScreen();
	
	$('#findbutton').click(findRouteClicked);
	$('input').keyup(function(e) {
	    if ( e.keyCode === 13 ) {
	    	findRouteClicked();
	    }
	});
	$('#resetbutton').click(resetScreen);
	
	google.maps.event.addListener(map, 'click', function(e)
	{
		if ($('#startInput').val() === '') {
			$('#startInput').val(latLngToString(e.latLng));
			addMarker(e.latLng, true, map, markers['start']);			
		} else if ($('#finishInput').val() === '') {
			$('#finishInput').val(latLngToString(e.latLng));
			addMarker(e.latLng, true, map, markers['finish']);
		}
	});

	/**
	 * Add marker to map
	 * @param position the lat/lng of the marker or null
	 * @param draggable boolean to set if the marker is draggable or not.
	 * @param map the map to set
	 * @param marker the marker to modify
	 */
	function addMarker (position, draggable, map, marker)
	{
		marker.setMap(map);
		if (position != null) {
			marker.setPosition(position);
		}
		marker.setDraggable(draggable);
	}
	
	/**
	 * Check if coordinates are complete. If yes, then start routing.
	 * @param coordinates the coordinates to check.
	 */
	function checkCoordinatesThenRoute(coordinates) {
		if (coordinates['start'] != null && coordinates['finish'] != null) {
			protocol.findRoute(
					coordinates['start'],
					coordinates['finish'],
					locale,
					function(results) {
						if (results.status === 'ok') {
							showRoutingResults(results);
						} else {
							clearSecondaryAlerts();
							showAlert(messageConnectionError, 'alert');
						}
					});
		}
	}	
	
	function clearRoutingResultsOnMap() {
		$.each(resultOverlays, function (overlayIndex, overlay) {
			overlay.setMap(null);
		});
		resultOverlays = [];
	}
	
	function clearRoutingResultsOnTable() {
		$('.tabs').remove();
		$('.tabs-content').remove();
	}
	
	function clearAlerts() {
		$('.alert-box').remove();
	}
	
	function clearSecondaryAlerts() {
		$('.alert-box.secondary').fadeOut();
	}
	
	function clearStartFinishMarker() {
	    markers['start'].setMap(null);
	    markers['finish'].setMap(null);		
	}
	
	/**
	 * A function that will be called when find route button is clicked
	 * (or triggered by another means)
	 */
	function findRouteClicked() {
		// Validate
		var cancel = false;
		$.each(['start', 'finish'], function(sfIndex, sfValue) {
			if ($('#' + sfValue + 'Input').val() === '') {
				clearSecondaryAlerts();
				showAlert(messageFillBoth, 'alert');
				cancel = true;		
				return;
			}
		});
		if (cancel) {
			return;
		}
		
		clearAlerts();
		clearRoutingResultsOnTable();
		showAlert(messagePleaseWait, 'secondary');
		$.each(['start', 'finish'], function(sfIndex, sfValue) {
			var placeInput = $('#' + sfValue + 'Input');
			var placeSelect = $('#' + sfValue + 'Select');
			if (isLatLng(placeInput.val())) {
				coordinates[sfValue] = placeInput.val();
				checkCoordinatesThenRoute(coordinates);
			} else {
				if (coordinates[sfValue] == null) {
					// Coordinates not yet ready, we do a search place
					protocol.searchPlace(
							placeInput.val(),
							region,
							function(result) {
								placeSelect.empty();
								placeSelect.addClass('hidden');
								if (result.searchresult.length > 0) {
									$.each(result.searchresult, function(index, value) {
										var placeSelect = $('#' + sfValue + 'Select');
										placeSelect
									         .append($('<option></option>')
									         .attr('value',value['location'])
									         .text(value['placename']));
										placeSelect.removeClass('hidden');
									});
									coordinates[sfValue] = result.searchresult[0]['location'];
									checkCoordinatesThenRoute(coordinates);
								} else {
									clearSecondaryAlerts();
									showAlert(placeInput.val() + messageNotFound, 'alert');
								}
							});
				} else {
					// Coordinates is already available, skip searching
					checkCoordinatesThenRoute(coordinates);
				}
			}
		});
	}
	
	/**
	 * Convert lat/lng into text representation
	 * @return the lat/lng in string, 5 digits after '.'
	 */
	function latLngToString(latLng) {
		return latLng.lat().toFixed(5) + ',' + latLng.lng().toFixed(5);
	}
	
	/**
	 * Checks if the text provided is in a lat/lng format.
	 * @return true if it is, false otherwise.
	 */
	function isLatLng(text) {
		return text.match('-?[0-9.]+,-?[0-9.]+');
	}
	
	function resetScreen() {
		var mapCenter = mapOptions.center; 
		clearRoutingResultsOnTable();
		clearRoutingResultsOnMap();
		clearAlerts();
		$.each(['start', 'finish'], function(sfIndex, sfValue) {
			var placeInput = $('#' + sfValue + 'Input');

			if (input_text[sfValue] != null) {
				placeInput.val(input_text[sfValue]);
				if (coordinates[sfValue] != null) {
					placeInput.prop('disabled', true);
					var latLng = stringToLatLng(coordinates[sfValue]);
					addMarker(latLng, false, map, markers[sfValue]);
					mapCenter = latLng;
				}
			} else {
				placeInput.val('');	
			}
			$('#' + sfValue + 'Select').addClass('hidden');
		});
		map.setCenter(mapCenter);
		map.setZoom(mapOptions.zoom);
	}
	
	/**
	 * Show alert message
	 * @param message the message
	 * @param cssClass the foundation css class ('success', 'alert', 'secondary')
	 */
	function showAlert(message, cssClass) {
		var alert = $('<div data-alert class="alert-box ' + cssClass + ' round">' + message + '<a href="#" class="close">&times;</a></div>');
		$('#routingresults').prepend(alert);
	}

	/**
	 * Shows the routing result on panel an map
	 * @param results the routing result JSON response
	 */
	function showRoutingResults(results) {
		clearStartFinishMarker();
		clearRoutingResultsOnTable();
		clearSecondaryAlerts();
		var sectionContainer = $('<div></div>');
		var temp1 = $('<dl class="tabs" data-tab=""></dl>');
		var temp2 = $('<div class="tabs-content"></div>');
		$('#routingresults').append(sectionContainer);
		$.each(results.routingresults, function(resultIndex, result) {
			var resultHTML1 = resultIndex === 0 ? '<dd class="active">' : '<dd class="">';
			resultHTML1 += '<a href="#panel1-' + (resultIndex + 1) + '">' + (result.traveltime === null ? messageOops : result.traveltime) + '</a></dd>';
			var resultHTML2 = '<div id="panel1-' + (resultIndex + 1)+'"';
			resultHTML2 += resultIndex === 0 ? ' class="content active"><table>' : ' class="content"><table>';
			$.each(result.steps, function (stepIndex, step) {
				resultHTML2 += '<tr><td><img src="../images/means/' + step[0]+ '/' + step[1] + '.png" alt="' + step[1] + '"/></td><td>';
				resultHTML2 += step[3] + '</td></tr>';
			});
			resultHTML2 += '</table></div>';
			temp1.append(resultHTML1);
			temp2.append(resultHTML2);
			
		});
		sectionContainer.append(temp1);
		sectionContainer.append(temp2);
	    $(document).foundation();    
		showSingleRoutingResultOnMap(results.routingresults[0]);
	}
	
	/**
	 * Shows a single routing result on map
	 * @param result the JSON array for one result
	 */
	function showSingleRoutingResultOnMap(result) {
		clearRoutingResultsOnMap();
		var bounds = null;
		$.each(result.steps, function (stepIndex, step) {
			if (step[0] === 'walk') {
				resultOverlays.push(new google.maps.Polyline({
					path: stringArrayToLatLngArray(step[2]),
					strokeColor: '#A00000'
				}));
			} else {
				resultOverlays.push(new google.maps.Polyline({
					path: stringArrayToLatLngArray(step[2]),
					strokeColor: '#008000'
				}));
			}
			
			if (stepIndex === 0) {
				var latLng = stringToLatLng(step[2][0]);
				resultOverlays.push(new google.maps.Marker({
				    position: latLng,
				    icon: {url: marker_image['start'], anchor: new google.maps.Point(34, 29)}, 
				}));
				bounds = new google.maps.LatLngBounds(latLng, latLng);
			} else {
				var latLng = stringToLatLng(step[2][0]);
				resultOverlays.push(new google.maps.Marker({
				    position: latLng,
				    icon: {url: '../images/means/' + step[0] + '/baloon/' + step[1] + '.png', anchor: new google.maps.Point(step[0] === 'walk' ? 34 : 0, 29)},
				}));
				bounds.extend(latLng);
			}
			
			if (stepIndex === result.steps.length - 1) {
				var latLng = stringToLatLng(step[2][step[2].length - 1]);
				resultOverlays.push(new google.maps.Marker({
				    position: latLng,
				    icon: {url: marker_image['finish'], anchor: new google.maps.Point(0, 29)},
				}));
				bounds.extend(latLng);
			}
		});
		$.each(resultOverlays, function (overlayIndex, overlay) {
			overlay.setMap(map);
		});
		map.fitBounds(bounds);
	}	
	
	/**
	 * Converts "lat,lng" array into LatLng object array.
	 * @return the converted LatLng array object
	 */
	function stringArrayToLatLngArray(textArray) {
		var latLngArray = new Array();
		$.each(textArray, function(index, value) {
			latLngArray[index] = stringToLatLng(value);
		});
		return latLngArray;
	}	
	
	/**
	 * Converts "lat,lng" into LatLng object.
	 * @return the converted LatLng object
	 */
	function stringToLatLng(text) {
		var latlng = text.split(/,\s*/);
		return new google.maps.LatLng(parseFloat(latlng[0]),parseFloat(latlng[1]));
	}
});

