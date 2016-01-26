<?php
include '../../etc/utils.php';
include '../../etc/constants.php';

init_mysql ();

$apikey = retrieve_from_get ( $proto_apikey, false );
$region = retrieve_from_get ( $proto_region, false );
$locale = retrieve_from_get ( $proto_locale, false );

// Set locale and validate
switch ($locale) {
	case $proto_locale_indonesia:
		break;
	default:
		$locale = $proto_locale_english;
}
include "../../etc/locale/tirtayasa_$locale.php";

$apikey_validity = check_apikey_validity($apikey);
if (is_bool($apikey_validity)) {
	if (!$apikey_validity) {
		header ( 'HTTP/1.0 403 Forbidden' );
		print "Invalid API Key\n";
		exit (0);		
	}
} else {
	header ( 'HTTP/1.0 403 Forbidden' );
	print "$apikey_validity\n";
	log_statistic($apikey, 'WIDGETERROR', $apikey_validity);
	exit (0);
}
log_statistic($apikey, 'WIDGETLOAD', $_SERVER['HTTP_REFERER'] . '/' . $_SERVER['REMOTE_ADDR'] . '/');


// Set region and validate
switch ($region) {
	case $proto_region_jakarta:
		break;
	default:
		$region = $proto_region_bandung;
}
$regioninfo = $regioninfos [$region];

// Set custom images
$logo = null;
if (file_exists ( "images/logo$apikey.png" )) {
	$logo = "images/logo$apikey.png";
}
foreach (array ($proto_routestart,	$proto_routefinish) as $endpoint) {
	if (file_exists("images/$endpoint$apikey.png")) {
		$markerImage[$endpoint] = "images/$endpoint$apikey.png";
	} else {
		$markerImage[$endpoint] = "../images/$endpoint.png";
	}
}

// Retrieve start/finish information
foreach (array ($proto_routestart,	$proto_routefinish) as $endpoint) {
	$startfinish = retrieve_from_get ($endpoint, false);
	if (!is_null($startfinish)) {
		if (preg_match('/^(.+)\\/(-?[0-9.]+,-?[0-9.]+)$/', $startfinish, $matches)) {
			$textual_endpoint [$endpoint] = $matches[1];
			$coordinate_endpoint [$endpoint] = $matches[2];
		} else {
			$textual_endpoint [$endpoint] = $startfinish;
			$coordinate_endpoint [$endpoint] = null;
		}
	} else {
		$textual_endpoint [$endpoint] = null;
		$coordinate_endpoint [$endpoint] = null;
	}
}

deinit_mysql ();
?>
<!DOCTYPE html>
<!--[if IE 8]> 				 <html class="no-js lt-ie9" lang="en" > <![endif]-->
<!--[if gt IE 8]><!-->
<html class="no-js" lang="en">
<!--<![endif]-->

<head>
<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
<meta charset="utf-8">
<meta name="viewport" content="width=device-width">
<title>KIRI Widget</title>

<link rel="stylesheet" href="foundation/css/foundation.css" />
<script src="foundation/js/vendor/modernizr.js"></script>
<link rel="stylesheet" href="css/widget.css">
</head>
<body>
	<div class="row" style="height: 100%; width: 100%; max-width: 100%">
		<div id="controlpanel" class="large-3 large-push-9 columns">
			<div class="row center">
			<?php
				if (!is_null($logo)) {
					print "<img src=\"$logo\"/>\n";
				}
			?>
			&nbsp;
			</div>
			<div class="row">
				<div class="small-3 columns">
					<?php print $widget_from; ?>
				</div>
				<div class="small-9 columns">
					<input type="text" id="startInput" value="">
				</div>
			</div>
			<div class="row">
				<div class="large-12 columns">
					<select id="startSelect" class="hidden"></select>
				</div>
			</div>
			<div class="row">
				<div class="small-3 columns">
					<?php print $widget_to; ?>
				</div>
				<div class="small-9 columns">
					<input type="text" id="finishInput" value="">
				</div>
			</div>
			<div class="row">
				<div class="large-12 columns">
					<select id="finishSelect" class="hidden"></select>
				</div>
			</div>
			<div class="row">
				<div class="small-6 columns">
					<a href="#" class="small button fullwidth" id="findbutton"><?php print $widget_find; ?></a>
				</div>
				<div class="small-6 columns">
					<a href="#" class="small button fullwidth" id="resetbutton"><?php print $widget_reset; ?></a>
				</div>
			</div>
			<!--<div class="row">
				<div class="small-6 columns">
					<a href="#" class="small button fullwidth" id="findbutton"><?php print $widget_find; ?></a>
				</div>
				<div class="small-6 columns">
					<a href="#" class="small button fullwidth" id="resetbutton"><?php print $widget_reset; ?></a>
				</div>
			</div>-->
			<div class="row">
				<div class="large-12 columns" id="routingresults">
					<div id="results-section-container"></div>
				</div>
			</div>
			<div class="row">
				<div class="large-12 columns">
					Powered by <a href="http://kiri.travel" target="_blank"><img width="59" height="20" src="images/kiri-small.png" alt="KIRI"/></a>
				</div>
			</div>
		</div>

		<div id="map-canvas" class="large-9 large-pull-3 columns"></div>
	</div>

	<script src="foundation/js/vendor/jquery.js"></script>
<script src="foundation/js/vendor/fastclick.js"></script>
	
	<script src="foundation/js/foundation.min.js"></script>
	<!--  <script src="foundation/js/foundation/foundation.section.js"></script> -->
	<script src="foundation/js/foundation/foundation.alert.js"></script>
	<script>
    $(document).foundation();    
  </script>

	<script
		src="https://maps.googleapis.com/maps/api/js?key=AIzaSyDGPykLQnl4aq8c0bIvFyx7fn7RMAld2aA&sensor=false"></script>

	<script>
		var mapOptions = {
			center: new google.maps.LatLng(<?php print $regioninfo['lat'] . ',' . $regioninfo['lon']; ?>),
			zoom: <?php print $regioninfo['zoom']; ?>,
			mapTypeId: google.maps.MapTypeId.ROADMAP,
			draggableCursor: 'default'
	  	};
	  	
		var input_text = [], coordinates = [], marker_image = [];
<?php 
	foreach (array ($proto_routestart,	$proto_routefinish) as $endpoint) {
		print "input_text['$endpoint'] = " . (is_null($textual_endpoint[$endpoint]) ? 'null' : "'$textual_endpoint[$endpoint]'") . ";\n";
		print "coordinates['$endpoint'] = " . (is_null($coordinate_endpoint[$endpoint]) ? 'null' : "'$coordinate_endpoint[$endpoint]'") . ";\n";
		print "marker_image['$endpoint'] = '" . $markerImage[$endpoint] . "';\n";
	}
	print "var locale='$locale';\n";
	print "var region='$region';\n";	
	print "var apikey='$apikey';\n";
	print "var messageConnectionError = '$widget_connectionerror';\n";
	print "var messageFillBoth = '$widget_fillboth';\n";
	print "var messageNotFound = '$widget__notfound';\n";
	print "var messageOops = '$widget_oops';\n";
	print "var messagePleaseWait = '<img src=\"images/loading.gif\" alt=\"... \"/> ' + '$widget_pleasewait';\n";
?>  
	</script>
	<script src="js/newprotocol.js"></script>
	<script src="js/index.html.js"></script>
</body>
</html>
