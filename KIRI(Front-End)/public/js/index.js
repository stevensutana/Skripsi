var map;
var input = document.getElementById("startInput");
var output = document.getElementById("finishInput");

// IE Fix
window.app = {};
var kiri = window.app;
var geolocation = null;
/**
 * @constructor
 * @extends {ol.control.Control}
 * @param {Object=} opt_options Control options.
 * method for made a button at bing maps
 */
kiri.CustomControl = function(opt_options) {
    var options = opt_options || {};
    var anchor = document.createElement('a');
    anchor.href = '#gps-button';
    anchor.innerHTML = '<img src="assets/images/gpstrack.png"/>';

    var control = this;
    var gpsButtonClicked = function(e) {
        e.preventDefault();
        var map = control.getMap();
        // Start geolocation tracking routine
        if (geolocation == null) {
            geolocation = new ol.Geolocation({
                projection: map.getView().getProjection()
            });

            var positionFeature = new ol.Feature();
            positionFeature.bindTo('geometry', geolocation, 'position')
                .transform(function() {}, function(coordinates) {
                    return coordinates ? new ol.geom.Point(coordinates) : null;
                });
            var featuresOverlay = new ol.FeatureOverlay({
                map: map,
                features: [positionFeature]
            });

            geolocation.setTracking(true);
        }
        if (typeof geolocation.getPosition() == 'undefined') {
            geolocation.once('change', function(evt) {
                map.getView().setCenter(geolocation.getPosition());
            });
        } else {
            map.getView().setCenter(geolocation.getPosition());
        }
    };

    anchor.addEventListener('click', gpsButtonClicked, false);
    anchor.addEventListener('touchstart', gpsButtonClicked, false);

    var element = document.createElement('div');
    element.className = 'gps-button ol-unselectable';
    element.appendChild(anchor);

    ol.control.Control.call(this, {
        element: element,
        target: options.target
    });

};
ol.inherits(kiri.CustomControl, ol.control.Control);

//var resultOverlays = [];
var trackStrokeStyles = [
    new ol.style.Style({
        stroke: new ol.style.Stroke({
            color : '#339933',
            width : 5
        })
    }),
    new ol.style.Style({
        stroke: new ol.style.Stroke({
            color : '#8BB33B',
            width : 5
        })
    }),
    new ol.style.Style({
        stroke: new ol.style.Stroke({
            color : '#267373',
            width : 5
        })
    })
];

var walkStrokeStyle = new ol.style.Style({
    stroke: new ol.style.Stroke({
        color : '#CC3333',
        width : 5
    })
});

$(document).ready(function() {
    var protocol = new CicaheumLedengProtocol("02428203D4526448", function(message) {
        clearSecondaryAlerts();
        showAlert(messageConnectionError, 'alert');
    });

    var mapLayer = new ol.layer.Tile(
        {
            source : new ol.source.BingMaps(
                {
                    key : 'AuV7xXD6_UMiQ5BLoZr0xkpjLpzWqMT55772Q8XtLIQeuDebHPKiNXSlZXxEr1GA',
                    imagerySet : 'Road'
                })
        });
    var resultVectorSource = new ol.source.Vector();
    var inputVectorSource = new ol.source.Vector();

    var map = new ol.Map(
        {
            controls: ol.control.defaults({
                attributionOptions: /** @type {olx.control.AttributionOptions} */ ({
                    collapsible: false
                })
            }).extend([
                new kiri.CustomControl()
            ]),
            layers : [ mapLayer, new ol.layer.Vector({source: inputVectorSource}), new ol.layer.Vector({source: resultVectorSource}) ],
            target : 'map'
        });

    var markers = {start: null, finish: null};
    updateRegion(region, false);

    $.each(['start', 'finish'], function(sfIndex, sfValue) {
        var placeInput = $('#' + sfValue + 'Input');
        var placeSelect = $('#' + sfValue + 'Select');

        if (input_text[sfValue] != null) {
            placeInput.val(input_text[sfValue]);
            if (coordinates[sfValue] != null) {
                placeInput.prop('disabled', true);
                var lonlat = stringToLonLat(coordinates[sfValue]);
                mapCenter = lonlat;
            }
        }
        $('#' + sfValue + 'Select').addClass('hidden');

        placeInput.change(function() {
            coordinates[sfValue] = null;
            if (markers[sfValue] != null) {
                inputVectorSource.removeFeature(markers[sfValue]);
                markers[sfValue] = null;
            }
        });
        placeSelect.change(function() {
            clearAlerts();
            showAlert(messagePleaseWait, 'secondary');
            coordinates[sfValue] = $(this).val();
            checkCoordinatesThenRoute(coordinates);
        });

    });

    // Event handlers
    var localeSelect = $('#localeselect');
    localeSelect.change(function() {
        // IE fix: when window.location.origin is not available
        if (!window.location.origin) {
            window.location.origin = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port: '');
        }
        window.location.replace(window.location.origin + "?locale=" + localeSelect.val());
    });
    var regionSelect = $('#regionselect');
    regionSelect.change(function() {
        updateRegion(regionSelect.val(), true);
        coordinates['start'] = null;
        coordinates['finish'] = null;
    });
    $('#findbutton').click(findRouteClicked);
    $('input').keyup(function(e) {
        if ( e.keyCode === 13 ) {
            findRouteClicked();
        }
    });
    $('#resetbutton').click(resetScreen);
    $('#swapbutton').click(swapInput);

    // Map click event
    map.on('click', function(event) {
        if ($('#startInput').val() === '') {
            markers['start'] = new ol.Feature({
                geometry: new ol.geom.Point(event.coordinate)
            })
            markers['start'].setStyle(new ol.style.Style({
                image: new ol.style.Icon({
                    src: 'assets/images/start.png',
                    anchor: [1.0, 1.0]
                })
            }));
            inputVectorSource.addFeature(markers['start']);
            $('#startInput').val(latLngToString(ol.proj.transform(event.coordinate, 'EPSG:3857', 'EPSG:4326')));
        } else if ($('#finishInput').val() === '') {
            markers['finish'] = new ol.Feature({
                geometry: new ol.geom.Point(event.coordinate)
            })
            markers['finish'].setStyle(new ol.style.Style({
                image: new ol.style.Icon({
                    src: 'assets/images/finish.png',
                    anchor: [0.0, 1.0]
                })
            }));
            inputVectorSource.addFeature(markers['finish']);
            $('#finishInput').val(latLngToString(ol.proj.transform(event.coordinate, 'EPSG:3857', 'EPSG:4326')));
        }
    });

    // Lastly, execute search if both start and finish are ready
    if ($('#startInput').val() != '' && $('#finishInput').val() != '' ) {
        findRouteClicked();
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
        resultVectorSource.clear();
        updateRegion(region, false);
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
        if (markers['start'] != null) {
            markers['start'] = null;
        }
        if (markers['finish'] != null) {
            markers['finish'] = null;
        }
        inputVectorSource.clear();
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
                cancel = true;
                return;
            }
        });
        if (cancel) {
            showAlert(messageFillBoth, 'alert');
            return;
        }

        clearAlerts();
        clearRoutingResultsOnTable();
        showAlert(messagePleaseWait, 'secondary');

        var completedLatLon = 0;
        $.each(['start', 'finish'], function(sfIndex, sfValue) {
            var placeInput = $('#' + sfValue + 'Input');
            var placeSelect = $('#' + sfValue + 'Select');
            if (isLatLng(placeInput.val())) {
                coordinates[sfValue] = placeInput.val();
                completedLatLon++;
            } else {
                if (coordinates[sfValue] == null) {
                    // Coordinates not yet ready, we do a search place
                    protocol.searchPlace(
                        placeInput.val(),
                        region,
                        function(result) {
                            placeSelect.empty();
                            placeSelect.addClass('hidden');
                            if (result.status != 'error') {
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
                                    clearRoutingResultsOnMap();
                                    showAlert(placeInput.val() + messageNotFound, 'alert');
                                }
                            } else {
                                clearSecondaryAlerts();
                                clearRoutingResultsOnMap();
                                showAlert(messageConnectionError, 'alert');
                            }
                        });
                } else {
                    // Coordinates are already available, skip searching
                    checkCoordinatesThenRoute(coordinates);
                }
            }
        });
        if (completedLatLon == 2) {
            checkCoordinatesThenRoute(coordinates);
        }
    }

    /**
     * Convert lon/lat into text representation
     * @return the lon/lat in string, 5 digits after '.'
     */
    function latLngToString(lonLat) {
        return lonLat[1].toFixed(5) + ',' + lonLat[0].toFixed(5);
    }

    /**
     * Checks if the text provided is in a lat/lng format.
     * @return true if it is, false otherwise.
     */
    function isLatLng(text) {
        return text.match('-?[0-9.]+,-?[0-9.]+');
    }

    function resetScreen() {
        clearRoutingResultsOnTable();
        clearRoutingResultsOnMap();
        clearAlerts();
        clearStartFinishMarker();
        $.each(['start', 'finish'], function(sfIndex, sfValue) {
            var placeInput = $('#' + sfValue + 'Input');
            placeInput.val('');
            placeInput.prop('disabled', false);
            $('#' + sfValue + 'Select').addClass('hidden');
        });
    }

    /**
     * Sets a cookie in browser, adapted from http://www.w3schools.com/js/js_cookies.asp
     */
    function setCookie(cname,cvalue)	{
        var d = new Date();
        d.setTime(d.getTime()+(365*24*60*60*1000));
        var expires = "expires="+d.toGMTString();
        document.cookie = cname + "=" + cvalue + "; " + expires;
    }

    /**
     * Show alert message
     * @param message the message
     * @param cssClass the foundation css class ('success', 'alert', 'secondary')
     */
    function showAlert(message, cssClass) {
        var alert = $('<div data-alert class="alert-box ' + cssClass + ' round">' + message + '<a href="#" class="close">&times;</a></div>');
        $('#routingresults').prepend(alert);
        $(document).foundation();
    }

    /**
     * Shows the routing result on panel an map
     * @param results the routing result JSON response
     */
    function showRoutingResults(results) {
        clearStartFinishMarker();
        clearRoutingResultsOnTable();
        clearSecondaryAlerts();
        var kiriURL = encodeURIComponent('http://kiri.travel?start=' + encodeURIComponent($('#startInput').val()) + '&finish=' + encodeURIComponent($('#finishInput').val()) + '&region=' + region);
        var kiriMessage = encodeURIComponent(messageITake.replace('%finish%', $('#finishInput').val()).replace('%start%', $('#startInput').val()));
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
                resultHTML2 += '<tr><td><img src="assets/images/means/' + step[0]+ '/' + step[1] + '.png" alt="' + step[1] + '"/></td><td>' + step[3];
                if (step[4] != null) {
                    resultHTML2 += ' <a class="ticket" href="' + step[4] + '" target="_blank">' + messageBuyTicket + '</a></td></tr>';
                }
                if (step[5] != null) {
                    resultHTML2 += ' <a href="' + step[5] + '" target="_blank"><img src="assets/images/edit.png" class="fontsize" alt="edit"/></a></td></tr>';
                }
                resultHTML2 += '</td></tr>';
            });
            resultHTML2 += "<tr><td class=\"center\" colspan=\"2\">";
            resultHTML2 += "<a target=\"_blank\" href=\"https://www.facebook.com/sharer/sharer.php?u=" + kiriURL + "\"><img alt=\"Share to Facebook\" src=\"assets/images/fb-large.png\"/></a> &nbsp; &nbsp; ";
            resultHTML2 += "<a target=\"_blank\" href=\"https://twitter.com/intent/tweet?via=kiriupdate&text=" + kiriMessage + "+" + kiriURL + "\"><img alt=\"Tweet\" src=\"assets/images/twitter-large.png\"/></a>";
            resultHTML2 += "</td></tr>\n";
            resultHTML2 += '</table></div>';
            temp1.append(resultHTML1);
            temp2.append(resultHTML2);
        });
        sectionContainer.append(temp1);
        sectionContainer.append(temp2);

        $.each(results.routingresults, function(resultIndex, result) {
            $('a[href="#panel1-' + (resultIndex + 1) + '"]').click(function() {
                showSingleRoutingResultOnMap(result);
            });
        });
        $(document).foundation();
        showSingleRoutingResultOnMap(results.routingresults[0]);
    }

    /**
     * Shows a single routing result on map
     * @param result the JSON array for one result
     */
    function showSingleRoutingResultOnMap(result) {
        clearRoutingResultsOnMap();

        var trackCounter = 0;
        $.each(result.steps, function (stepIndex, step) {
            if (step[0] === 'none') {
                // Don't draw line
            } else {
                var lineFeature = new ol.Feature({
                    geometry: new ol.geom.LineString(stringArrayToPointArray(step[2])),
                });
                lineFeature.setStyle(step[0] == 'walk' ? walkStrokeStyle : trackStrokeStyles[trackCounter++ % trackStrokeStyles.length]);
                resultVectorSource.addFeature(lineFeature);
            }

            if (stepIndex === 0) {
                var pointFeature = new ol.Feature({
                    geometry: new ol.geom.Point(ol.proj.transform(stringToLonLat(step[2][0]), 'EPSG:4326', 'EPSG:3857'))
                })
                pointFeature.setStyle(new ol.style.Style({
                    image: new ol.style.Icon({
                        src: 'assets/images/start.png',
                        anchor: [1.0, 1.0]
                    })
                }));
                resultVectorSource.addFeature(pointFeature);
            } else {
                var lonlat = stringToLonLat(step[2][0]);
                if(step[0] != "walk"){
                    var pointFeature = new ol.Feature({
                        geometry: new ol.geom.Point(ol.proj.transform(lonlat, 'EPSG:4326', 'EPSG:3857'))
                    })
                    pointFeature.setStyle(new ol.style.Style({
                        image: new ol.style.Icon({
                            src: 'assets/images/means/' + step[0] + '/baloon/' + step[1] + '.png',
                            anchor: [0.0, 1.0]
                        })
                    }));
                    resultVectorSource.addFeature(pointFeature);
                } else {
                    var pointFeature = new ol.Feature({
                        geometry: new ol.geom.Point(ol.proj.transform(lonlat, 'EPSG:4326', 'EPSG:3857'))
                    })
                    pointFeature.setStyle(new ol.style.Style({
                        image: new ol.style.Icon({
                            src: 'assets/images/means/walk/baloon/walk.png',
                            anchor: [1.0, 1.0]
                        })
                    }));
                    resultVectorSource.addFeature(pointFeature);
                }
            }

            if (stepIndex === result.steps.length - 1) {
                var lonlat = stringToLonLat(step[2][step[2].length - 1]);
                var pointFeature = new ol.Feature({
                    geometry: new ol.geom.Point(ol.proj.transform(lonlat, 'EPSG:4326', 'EPSG:3857'))
                })
                pointFeature.setStyle(new ol.style.Style({
                    image: new ol.style.Icon({
                        src: 'assets/images/finish.png',
                        anchor: [0.0, 1.0]
                    })
                }));
                resultVectorSource.addFeature(pointFeature);
            }
        });
        map.getView().fitExtent(resultVectorSource.getExtent(), map.getSize());
    }

    /**
     * Converts "lat,lon" array into coordinate object array.
     * @return the converted Point array object
     */
    function stringArrayToPointArray(textArray) {
        var lonlatArray = new Array();
        $.each(textArray, function(index, value) {
            lonlatArray[index] = ol.proj.transform(stringToLonLat(value), 'EPSG:4326', 'EPSG:3857');
        });
        return lonlatArray;
    }

    /**
     * Converts "lat,lng" into lonlat array
     * @return the converted lonlat array
     */
    function stringToLonLat(text) {
        var latlon = text.split(/,\s*/);
        return [parseFloat(latlon[1]), parseFloat(latlon[0])];
    }

    /**
     * Swap the inputs
     */
    function swapInput() {
        var startInput = $('#startInput');
        var finishInput = $('#finishInput');
        var temp = startInput.val();
        startInput.val(finishInput.val());
        finishInput.val(temp);
        coordinates['start'] = null;
        coordinates['finish'] = null;
        if (startInput.val() != '' && finishInput.val() != '') {
            findRouteClicked();
        }
    }

    /**
     * Updates the region information in this page.
     */
    function updateRegion(newRegion, updateCookie) {
        region = newRegion;
        setCookie('region', region);
        var point = stringToLonLat(regions[region].center);
        map.getView().setCenter(ol.proj.transform(point, 'EPSG:4326', 'EPSG:3857'));
        map.getView().setZoom(regions[region].zoom);
    }
});

