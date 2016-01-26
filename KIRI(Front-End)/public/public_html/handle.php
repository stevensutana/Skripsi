<?php
require_once '../etc/utils.php';
require_once '../etc/constants.php';

// We won't tell the error detail to the world. Ssssh!
$global_hush_hush = true;

//init PHP for header
start_working();

//connect to DB 
init_mysql();

//mode didapat dari post dengan key 'mode'
$mode = retrieve_from_post($proto_mode);

//version dari post dengan key 'version'
$version = retrieve_from_post($proto_version, false);

//jika tidak ada data dari post, isi dengan 1
$version = is_null($version) ? 1 : $version;

//apikey dari post dengan key 'apikey'
$apikey = retrieve_from_post($proto_apikey);

//cek database apikey yang digunakan ada atau tidak. jika ada, cek lg IP komputer tersebut diblok atau tidak.
check_apikey($apikey);

//mode = 'findroute'
if ($mode == $proto_mode_findroute) 
{
	//addslashes method untuk special char tidak dianggap string lain, misal ',",\
	//masukkan ke variabel
	$start = addslashes(retrieve_from_post($proto_routestart));
	$finish = addslashes(retrieve_from_post($proto_routefinish));
	$locale = addslashes(retrieve_from_post($proto_locale));
	
	//cek untuk lokalisasi,jika ada, load file tersebut inggris / indo. kalo 
	if (file_exists("../etc/locale/tirtayasa_$locale.php")) {
		require_once("../etc/locale/tirtayasa_$locale.php");
	} else {
		die_nice("Locale not found: $locale");
	}

	//cek presentasi ada 2, mobile dan dekstop, show error
	$presentation = addslashes(retrieve_from_post($proto_presentation));
	if ($presentation != $proto_presentation_mobile && $presentation != $proto_presentation_desktop) {
		die_nice("Presentation not understood: $presentation");
	}

	// Retrieve from menjangan server.
	$results = array();
	if ($version >= 2) 
	{
		//jika presentasi pakai mobile maka count = 1, jika bkn mobile, size array alternatif = 3
		$count = $presentation == $proto_presentation_mobile ? 1 : sizeof($alternatives);

		//pengulangan sebanyak count
		// menambahkan query url menjangan dengan start,finish dengan value koordinat start dan finish
		// mw,wm,pt masing2 dengan value alternative yang ke i dari array alternative
		//file get content return false jika gagal. Jika berhasil mendapatkan string dengan url dari menjangan, pembacaan dari 1 sebanyak 512001
		//memasukkan ke array results dengan indeks result value = true
		for ($i = 0; $i < $count; $i++) {
			$url = $menjangan_url . "/?start=$start&finish=$finish";
			$url .= '&' . $protokd_maximumwalk . '=' . $alternatives[$i][$protokd_maximumwalk];
			$url .= '&' . $protokd_walkingmultiplier . '=' . $alternatives[$i][$protokd_walkingmultiplier];
			$url .= '&' . $protokd_penaltytransfer . '=' . $alternatives[$i][$protokd_penaltytransfer];
			$result = file_get_contents($url, NULL, NULL, 0, $maximum_http_response_size + 1);
			if ($result == FALSE) {
				die_nice("There's an error while reading the menjangan response.");
			}
			$results[$result] = true;
		}	

	} else 
		//version < 2
	{
		//langsung akses url menjangan dengan query start dan finish saja.
		$result = file_get_contents($menjangan_url . "/?start=$start&finish=$finish", NULL, NULL, 0, $maximum_http_response_size + 1);
		if ($result == FALSE) {
			die_nice("There's an error while reading the menjangan response.");
		}
		$results[$result] = true;
	}

	//pengulangan sebanyak results menjadi dummy[result]
	foreach ($results as $result=>$dummy) {
		$travel_time = 0;
		$route_output = array();
		//memisahkan enter dari result
		$steps = split("\n", $result);
		foreach ($steps as $step) {
			//buang spasi
			$step = trim($step);
			if ($step == '') {
				// Could be the last line, ignore if empty.
				continue;
			}
			// Path is not found, step = none
			if ($step == $protokd_result_none) {
				if (sizeof($results) == 1) {
					// There is not other alternative
					$route_output[] = array("none", "none", array($start, $finish), $message_routenotfound[$presentation], null, null);
					$travel_time = null;
					break;
				} else {
					// There is alternative, hence we just skip this step.
					continue 2;
				}
			}
			//list dari array step yang telah dipisahkan "/". $means = array[0], $means_detail = array[1], $route = array[2], $distance = array[3],$nearbyplaceids = array[4]
			list($means, $means_detail, $route, $distance, $nearbyplaceids) = split("/", $step);
			// jika salah satu kosong, error 
			if (!isset($means) || !isset($route) || !isset($distance) || !isset($nearbyplaceids)) {
				die_nice("Incomplete response in this line: $step");
			}
			//array point didapat dari route yang sudah dipisahkan spasi
			$points = split(" ", $route);
			// from dari points dengan indeks 0
			$from = $points[0];
			// from dari points dengan indeks terakhir
			$to = $points[sizeof($points) - 1];

			// Replace keywords with real location, then construct the detailed path
			for ($i = 0, $size = sizeof($points); $i < $size; $i++) {
				// cek array point[i] == 'start'. jika sama maka mengganti dengan $start
				if ($points[$i] == $protokd_point_start) {
					$points[$i] = $start;
				}

				// cek array point[i] == 'finish'. jika sama maka mengganti dengan $finish
				if ($points[$i] == $protokd_point_finish) {
					$points[$i] = $finish;
				}
			}
	
			// Construct the human readable form of the walk
			$humanized_from = humanize_point($from);
			$humanized_to = humanize_point($to);
			// Convert whole path to human readable form
			if ($means == $protokd_transitmode_walk) {//jika means = 'walk'
				// Remove uneccessary information if not needed.
				if ($humanized_from == $humanized_to) {//suggestion nya ada yg sama
					// When we're in mobile, skip this step (not really necessary)
					if ($presentation == $proto_presentation_mobile) {
						$humanreadable = null;
					} else {
						// masukkan message_walk_samestreet ke variabel humanreadable 
						// replace %street yang ada di humanreadable dengan value humanized_from 
						// replace %distance yang ada di humanreadable dengan value jarak

						$humanreadable = $message_walk_samestreet;
						$humanreadable = str_replace('%street', $humanized_from, $humanreadable);
						$humanreadable = str_replace('%distance', format_distance($distance, $locale), $humanreadable);
					}
				} else { //suggestion tdk ada yg sama
					if ($presentation == $proto_presentation_mobile) {//presentasi = mobile
						//suggestion from tambahkan dengan string %fromicon
						//suggestion to tambahkan dengan string %toicon
						$humanized_from .= ' %fromicon';
						$humanized_to .= ' %toicon';
					}
					// masukkan message_walk ke variabel humanreadable 
					// replace %from yang ada di humanreadable dengan value humanized_from 
					// replace %to yang ada di humanreadable dengan value humanized_to 
					// replace %distance yang ada di humanreadable dengan value jarak

					$humanreadable = $message_walk;
					$humanreadable = str_replace('%from', $humanized_from, $humanreadable);
					$humanreadable = str_replace('%to', $humanized_to, $humanreadable);
					$humanreadable = str_replace('%distance', format_distance($distance, $locale), $humanreadable);
				}
				//menghitung waktu yg dibutuhkan
				$travel_time += $distance / $speed_walk;
				$booking_url = null;
				$editor_url = null;
			} else {//means != walk
				// First the friendly name of the track

				//mencari nama track dari database
				$means_detail = mysqli_escape_string($global_mysqli_link, $means_detail);
				$result = mysqli_query($global_mysqli_link, "SELECT tracks.trackname, tracktypes.name, tracktypes.url, tracks.extraParameters, tracktypes.speed, tracks.internalInfo FROM tracks JOIN tracktypes ON tracktypes.trackTypeId='$means' AND tracks.trackTypeId='$means' AND tracks.trackid='$means_detail'") or
				die_nice("Can't retrieve the track name from database: " . mysqli_error($global_mysqli_link));
				if ($row = mysqli_fetch_row($result)) {//jika ada hasil
					//memasukkan row[0] ke readable_track_name
					//memasukkan row[1] ke track_type_name
					$readable_track_name = $row[0];
					$track_type_name = $row[1];
				} else {//tidak ada hasil
					die_nice("Can't find the track name and/or type with the given track id '$means_detail'");
				}
				// Construct the human readable form of the walk
				if ($presentation == $proto_presentation_mobile) {
					$humanized_from .= ' %fromicon';
					$humanized_to .= ' %toicon';
				}
				$humanreadable = $message_angkot;
				$humanreadable = str_replace('%from', $humanized_from, $humanreadable);
				$humanreadable = str_replace('%to', $humanized_to, $humanreadable);
				$humanreadable = str_replace('%distance', format_distance($distance, $locale), $humanreadable);
				$humanreadable = str_replace('%trackname', $readable_track_name, $humanreadable);
				$humanreadable = str_replace('%tracktype', $track_type_name, $humanreadable);
				
				//speed diambil dari row[4] yang dijadikan integer
				$speed = intval($row[4]);

				//menghitung waktu perjalanan
				$travel_time += $distance / $speed;

				//cek url track dan extraParameter kosong tidak
				if (!is_null($row[2]) && !is_null($row[3])) {
					//booking url = url track + extraParameter
					$booking_url = $row[2] . $row[3];
				} else {
					$booking_url = null;
				}

				//cek internalinfo dimulai dengan string angkotwebid:
				if (startsWith($row[5], 'angkotwebid:')) {
					//token didapat dari internalinfo dengan pemisah ':'
					$token = explode(':', $row[5]);

					//editor_url = awalan url angkotwebid + token[1] + akhiran url angkotwebid
					$editor_url = $angkotwebid_url_prefix . $token[1] . $angkotwebid_url_suffix;
				} else {
					$editor_url = null;
				}

				// compatibility patch for older 3rd party apps
				if ($means == 'bdo_angkot' && intval($version) < 3) {
					$means = 'angkot';
				}
			}
			if (!is_null($humanreadable)) {//humanreadable tidak null
				//memasukkan route_output array dari ......
				$route_output[] = array($means, $means_detail, $points, $humanreadable, $booking_url, $editor_url);
			}
		}//end for each steps as step

		//routing result dengan indeks steps = route_output
		$routing_result[$proto_steps] = $route_output;

		//routing result dengna indeks traveltime = masukkan string brp jam / brp menit dari travel time
		$routing_result[$proto_traveltime] = format_traveltime($travel_time);

		//memasukkan kembali routing_result
		$routing_results[] = $routing_result;
	}//end for each as dummy[result]
	
	//input log statistic
	log_statistic("$apikey", "FINDROUTE", "$start/$finish/" . sizeof($results));
	
	deinit_mysql();

	if (!is_null($version) && $version >= 2) {//version tdk null dan version >= 2
		//print string yang sudah di encode menjadi json dengan status => ok, routingresult => hasil pencarian rute 
		print json_encode(array(
				$proto_status => $proto_status_ok,
				$proto_routingresults => $routing_results
		));
	} else {//version null atau version < 2
		//print string yang sudah di encode menjadi json dengan status => ok, routingresult => langkahnya saja, traveltime => waktu
		print json_encode(array(
				$proto_status => $proto_status_ok,
				$proto_routingresult => $routing_results[0][$proto_steps],
				$proto_traveltime => $routing_results[0][$proto_traveltime]
		));
	}
} elseif ($mode == $proto_mode_search) //mode = search
{
	//querystring dapet dari post
	$querystring = retrieve_from_post($proto_search_querystring);
	//apikey dapet dari post
	$apikey = retrieve_from_post($proto_apikey);
	//region dapet dari post tetapi jika version >=2
	$region = retrieve_from_post($proto_region, $version >= 2);

	//jika null maka region ada di bandung
	$region = is_null($region) ? $proto_region_bandung : $region;
	
	// Check if there is region modifier from the query string
	foreach ($regioninfos as $key => $value) {
		//mencari dalam query string ada pattern "/+searchplace+/i". tampilkan pada array matches
		if (preg_match('/' . $value['searchplace_regex'] . '/i', $querystring, $matches, PREG_OFFSET_CAPTURE)) {
			$region = $key;
			//mengambil sebanyak matches[0][1] dari querystring,langsung berhenti pengulangan
			$querystring = substr($querystring, 0, $matches[0][1]);
			break;
		}
	}
	//mengubah string menjadi format url
	$querystring = urlencode($querystring);
	// The following place search hmethod uses Foursquare venue search service
	// Note: to show "Search powered by Foursquare" before or at search result.
	$cached_searchplace = get_from_cache($cache_searchplace, "$region/$querystring");//mengambil dari cache searchplace dengan key $region/$querystring
	if (!is_null($cached_searchplace)) {
		//kalo ga null siapin json untuk output dengan status => ok, searchresult => ubah json menjadi string,attributions=>null
		$json_output = array(
				$proto_status => $proto_status_ok,
				$proto_search_result => json_decode($cached_searchplace, true),
				$proto_attributions => null
		);

		log_statistic("$apikey", "SEARCHPLACE",  "$querystring/cache");
	} else {//cache = null
		//dapetin lat lon radius dari regioninfos dengan index region
		$city_lat = $regioninfos[$region]['lat'];
		$city_lon = $regioninfos[$region]['lon'];
		$city_radius = $regioninfos[$region]['radius'];
// pascal: switch to use 4sq
// 		$full_url = "$places_url?client_id=$foursq_client_id&client_secret=$foursq_client_secret&intent=browse&limit=10&v=20130611&ll=$city_lat,$city_lon&radius=$city_radius&query=$querystring";
		$full_url = "$places_url?key=$gmaps_server_key&location=$city_lat,$city_lon&radius=$city_radius&keyword=$querystring&types=establishment|route&sensor=true";
		$result = file_get_contents($full_url, NULL, NULL, 0, $maximum_http_response_size + 1);
		if ($result == FALSE) {
			die_nice("There's an error while reading the places response ($full_url).");
		}
		// TODO this checking doesn't seem to work.
		if (sizeof($result) > $maximum_http_response_size) {
			die_nice("Data returned from $full_url is greater than the maximum.");
		}
		//hasil json di decode menjadi string
		$json_result = json_decode($result, true);
		if ($json_result['status'] == 'OK' || $json_result['status'] == 'ZERO_RESULTS') {//hasil dari status = ok atau zero_results
			$search_result = array();
			if ($json_result['status'] == 'ZERO_RESULTS') {//hasil status = zero_results 
				//log error
				log_error("Place search not found: \"$querystring\"");
				$size = 0;
			} else {
				//mengambil yang minimal antara result dan search_maxresult
				$size = min(sizeof($json_result['results']), $search_maxresult);
			}
			//pengulangan sampai size
			for ($i = 0; $i < $size; $i++) {
				//current venue = json result yang ke i
				$current_venue = $json_result['results'][$i];
				//search_result ke i dengan key placename = mengambil current_venue dengan key name
				$search_result[$i][$proto_placename] = $current_venue['name'];
				//search_result ke i dengan key location = string yang sudah di format "%.latlonlf,%.latlonlf" dimana valeunya current_venue geometry location mengambil lat dan lng
				$search_result[$i][$proto_location] = sprintf(
						'%.' . $latlon_precision . 'lf,%.' . $latlon_precision . 'lf',
						$current_venue['geometry']['location']['lat'],
						$current_venue['geometry']['location']['lng']
				);
			}
// pascal: switch to this for 4sq
// 		if ($json_result['meta']['code'] == '200') {
// 			$search_result = array();
// 			$size = min(sizeof($json_result['response']['venues']), $search_maxresult);
// 			for ($i = 0; $i < $size; $i++) {
// 				$current_4sq_venue = $json_result['response']['venues'][$i];
// 				$search_result[$i][$proto_placename] = $current_4sq_venue['name'];
// 				if (isset($current_4sq_venue['location']['address'])) {
// 					$search_result[$i][$proto_placename] .= ' - ' . $current_4sq_venue['location']['address'];
// 				}
// 				$search_result[$i][$proto_location] = sprintf(
// 						'%.' . $latlon_precision . 'lf,%.' . $latlon_precision . 'lf',
// 						$current_4sq_venue['location']['lat'],
// 						$current_4sq_venue['location']['lng']
// 				);
// 			}
// 			if ($size == 0) {
// 				log_error("Place search not found: \"$querystring\"");
// 			}
			$json_output = array(
					$proto_status => $proto_status_ok,
					$proto_search_result => $search_result,
					$proto_attributions => null
			);
	
			//input log statistic
			log_statistic("$apikey", "SEARCHPLACE",  "$querystring/$size");
			// Store to cache
			put_to_cache($cache_searchplace, "$region/$querystring", json_encode($search_result));
			deinit_mysql();
			
		} else {//hasil dari status bukan ok atau zero_results
// switch to use 4sq
// 			die_nice('Foursquare Place Search returned error: ' . $json_result['meta']['code'] . " (for this request: $full_url)", false);
			die_nice('Place Search returned error: ' . $json_result['status'] . " (for this request: $full_url)", false);
		}
	}
	//terakhir keluarkan json 
	print json_encode($json_output);
} elseif ($mode == $proto_mode_reporterror) {//mode = reporterror
	//mendapatkan errorcode
	$errorcode = retrieve_from_post($proto_errorcode);
	log_error("Client reported error: $errorcode");
	//keluarin json dengan status ok, tanpa message lain
	well_done();
} elseif ($mode == $proto_mode_nearbytransports) {//mode = nearbytransport
	//start dapet dari post dengan query = routestart
	$start = retrieve_from_post($proto_routestart);
	if ($version >= 2) {//version >=2
		//mendapatkan string dari menjangan_url + /?start=$start. setelah mendapatkan string tersebut, lakukan pemisahan enter
		$lines = explode("\n", file_get_contents($menjangan_url . "/?start=$start", NULL, NULL, 0, $maximum_http_response_size + 1));
		$nearby_result = array();
		foreach ($lines as $line) {//pengulangan sebanyak %lines
			//pemisahan dari "\" dari line, lalu list menjadi tracktypeid,trackid,distance
			list($trackTypeId, $trackId, $distance) = explode("/", $line);
			//result melakukan sql query mendapatkan trackname dari tabel tracks dimana trackid dan tracktypeid sesuai.
			$result = mysqli_query($global_mysqli_link, "SELECT trackName FROM tracks WHERE trackId='$trackId' AND trackTypeId='$trackTypeId'") or
				die_nice("Can't get nearest track details: " . mysqli_error($global_mysqli_link));
			
			while ($row = mysqli_fetch_array($result)) {
				$trackName = $row[0];
				$nearby_result[] = array (
						$trackTypeId,
						$trackId,
						$trackName,
						$distance 
				);
			}
		}
		usort($nearby_result, "nearby_result_compare");
		log_statistic($apikey, "NEARBYTRANSPORTS", "$start" . sizeof($results));		
		$json_output = array(
				$proto_status => $proto_status_ok,
				$proto_nearbytransports => $nearby_result
		);
		print json_encode($json_output);
	} else {//version <2
		die_nice("Nearby transit is not supported in version 1");
	}
} else {
	die_nice("Mode not understood: \"" . $mode . "\"");
}

/**
 * Replace a location point into a human readable form with most effort.
 * This function requires MySQL connection to be active.
 * For example:
 * <ul>
 * <li>'start' => 'your starting point'.
 * <li>'xxx.xxx,yyy.yyy' => check in cache, or reverse geocode from google if miss
 * </ul>
 * @param string $location the original location constant
 */
function humanize_point($location) {
	global $global_mysqli_link;
	global $protokd_point_start, $protokd_point_finish;
	global $message_start, $message_finish;
	global $gmaps_geocode_url, $gmaps_server_key, $maximum_http_response_size;
	global $cache_geocoding;

	//location = start, return message start
	if ($location == $protokd_point_start) {
		return $message_start;
	} else if ($location == $protokd_point_finish) {
	//location = finish, return message start
		return $message_finish;
	} else {//selain start atau finish
		//escape string location untuk dimasukkan ke query sql
		$location = mysqli_escape_string($global_mysqli_link, $location);

		//cek cache ada dengan key cache_geocoding,value location
		$cached_geocode = get_from_cache($cache_geocoding, $location);
		//kalo tidak null, return langsung
		if (!is_null($cached_geocode)) {
			return $cached_geocode;
		} else {//jika null cachenya

			// query from google.
			$full_url = "$gmaps_geocode_url?key=$gmaps_server_key&latlng=$location&sensor=false";
			$result = file_get_contents($full_url, NULL, NULL, 0, $maximum_http_response_size + 1);
			if ($result == FALSE) {
				die_nice("There's an error while reading the geocoding response from $full_url.");
			}
			// TODO this checking doesn't seem to work.
			if (sizeof($result) > $maximum_http_response_size) {
				die_nice("Data returned from $full_url is greater than the maximum.");
			}
			$json_response = json_decode($result, true);
			if ($json_response == NULL) {
				die_nice("Unable to retrieve JSON response from Google geocoding service.");
			}
			//add sugestion place
			if ($json_response['status'] == 'OK') {
				$bestguess = $location;
				for ($i = 0; $i < count($json_response['results']); $i++) {
					foreach ($json_response['results'][0]['address_components'] as $component) {
						if (in_array('transit_station', $component['types']) || in_array('route', $component['types'])) {
							put_to_cache($cache_geocoding, $location, $component['long_name']);
							return $component['long_name'];
						}
						$bestguess = $component['long_name'];
					}
				}
				log_error("Warning: can't find street name, use best guess $bestguess for $location.");
				put_to_cache($cache_geocoding, $location, $bestguess);
				return $bestguess;
			} else if ($json_response['status'] == 'ZERO_RESULTS') {
				// If not found, return the coordinate.
				log_error("Warning: can't find coordinate for $location.");
				return $location;
			} else {
				die_nice("Problem while geocoding from Google reverse geocoding: " . $result);
			}
		}
	}
}

/**
 * A sorting comparison function to be used in nearby transports
 * @param array $a an array, where index 3 is the distance
 * @param array $b an array, where index 3 is the distance
 * @return number as in usort() spec
 */
function nearby_result_compare($a, $b) {
	if ($a[3] > $b[3]) {
		return +1;
	} else if ($a[3] < $b[3]) {
		return -1;
	} else {
		return 0;
	}
}

/**
 * Format a number into distance
 * @param float $distance The distance
 * @param string $locale The locale code 'en' or 'id'
 */
function format_distance($distance, $locale) {
	if (!is_numeric($distance)) {
		die_nice("Distance is not a floating number: $distance");
	}

	if ($distance < 1) {
		// Less than 1 km, show in meter
		return floor($distance * 1000) . " meter";
	} else {
		// More than 1 km, show in km
		$decimal = $locale == 'id' ? ',' : '.';
		$fdist = floor($distance);
		return $fdist . $decimal . floor(($distance - $fdist) * 10) . " kilometer";
	}
}

/**
 * Format numeric travel time to a human readable one.
 * @param float $time the travel time in hour.
 */
function format_traveltime($time) {
	global $message_hour, $message_min;
	if (is_null($time)) {
		return null;
	} elseif ($time > 1) {
		return round($time) . " $message_hour";
	} else {
		return 5 * ceil($time * 60 / 5) . " $message_min";
	}
}

/**
 * method for check if the api keys is in database.
 * @param string $apikey provided by user to be checked.
 */
function check_apikey ($apikey)
{
	//check message with global_hush_hush
	global $global_hush_hush, $global_mysqli_link;
	$global_hush_hush = false;
	
	//check apikey: is this api key exist in database?	
	$apisqlquerry = mysqli_query($global_mysqli_link, "SELECT apikeys.verifier, apikeys.ipFilter FROM apikeys WHERE
			apikeys.verifier = '$apikey'") or die_nice("failed to execute query on Apikey check.");
		
	// if it exist, it must be found 1 row (because apiKeyOrDomain are unique)
	if ($querry = mysqli_fetch_array($apisqlquerry))
	{
		// check for ip filter	
		if ($_SERVER['REMOTE_ADDR'] != $querry['ipFilter'] && $querry['ipFilter'] != NULL)
		{
			die_nice("IP address is not accepted for this API key.");
		}
	} 
	else
	{
		die_nice("API key is not recognized: $apikey.");
	}
}
?>
