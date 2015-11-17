/**
 * Created by Node8 on 1/11/15.
 */

var map;
var input = document.getElementById("startInput");
var output = document.getElementById("finishInput");





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


function test(){
    alert("testinggggggg");
}


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

map = new ol.Map({
    target: 'map',
    layers : [ mapLayer, new ol.layer.Vector({source: inputVectorSource}), new ol.layer.Vector({source: resultVectorSource}) ],
    view: new ol.View({
        center: ol.proj.fromLonLat([107.60981,-6.91474]),
        zoom: 12
    })
});

var markers = {start: null, finish: null};


//for map click event
map.on('click', function(event) {
    if (startInput.value === '')
    {
        markers['start'] = new ol.Feature({
            geometry: new ol.geom.Point(event.coordinate)
        })
        markers['start'].setStyle(new ol.style.Style({
            image: new ol.style.Icon({
                src: '/assets/images/start.png',
                anchor: [1.0, 1.0]
            })
        }));
        inputVectorSource.addFeature(markers['start']);
        startInput.value = latLngToString(ol.proj.transform(event.coordinate, 'EPSG:3857', 'EPSG:4326'));
    }else if (finishInput.value === '') {
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
        finishInput.value = latLngToString(ol.proj.transform(event.coordinate, 'EPSG:3857', 'EPSG:4326'));
    }
});



function clearRoutingResultsOnMap() {
    resultVectorSource.clear();
    //updateRegion(region, false);
}

function clearRoutingResultsOnTable() {
    //var tabs = document.get
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
 * Convert lon/lat into text representation
 * @return the lon/lat in string, 5 digits after '.'
 */
function latLngToString(lonLat) {
    return lonLat[1].toFixed(5) + ',' + lonLat[0].toFixed(5);
}

//map.addEventListener("click",function(){
//    //alert("123");
//
//});

document.getElementById("resetbutton").addEventListener("click",function(){
    startInput.value = '';
    finishInput.value = '';
});

var regionSelect = document.getElementById("regionselect");

var updateMap = function(zoom){

//                map.getView().setCenter(ol.proj.transform(arr, 'EPSG:4326', 'EPSG:3857'));
    map.getView().setZoom(zoom);
};

var handlerRegion = function () {
    var regionValue = regionSelect.value;
    if(regionValue=="bdg")
    {
        map.getView().setCenter(ol.proj.transform([107.60981,-6.91474], 'EPSG:4326', 'EPSG:3857'));
        map.getView().setZoom(12);
    }else if(regionValue=="jkt")
    {
        map.getView().setCenter(ol.proj.transform([106.84517,-6.21154], 'EPSG:4326', 'EPSG:3857'));
        map.getView().setZoom(11);
    }else if(regionValue=="sby")
    {
        map.getView().setCenter(ol.proj.transform([112.71908,-7.27421], 'EPSG:4326', 'EPSG:3857'));
        map.getView().setZoom(12);
    }else
    {
        map.getView().setCenter(ol.proj.transform([112.6319264,-7.9812985], 'EPSG:4326', 'EPSG:3857'));
        map.getView().setZoom(13);

    }

};
regionSelect.addEventListener("change",handlerRegion,false);

var localeSelect = document.getElementById("localeselect");
var handlerLocale = function(){
    var localeValue = localeSelect.value;
    if(localeValue=="en")
    {
        window.location.replace("http://localhost:9000");
    }
    else if(localeValue =="id")
    {
        window.location.replace("http://localhost:9000/id");

    }
};
localeSelect.addEventListener("change",handlerLocale,false);

/**
 * Swap the inputs
 */
function swapInput() {
    //var startInput = document.getElementById("startInput");
    //var finishInput = document.getElementById("finishInput");
    var temp = startInput.value;

    startInput.value = finishInput.value;
    finishInput.value = temp;

    coordinates['start'] = null;
    coordinates['finish'] = null;
}

var swapBtn = document.getElementById("swapbutton");
swapBtn.addEventListener("click",swapInput);