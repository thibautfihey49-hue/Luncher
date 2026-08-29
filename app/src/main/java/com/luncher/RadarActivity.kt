
package com.luncher

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class RadarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val webView = WebView(this)
        setContentView(webView)
        val lat = intent.getDoubleExtra("lat", 47.6859)
        val lon = intent.getDoubleExtra("lon", -0.87)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = WebViewClient()
        val html = """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
<link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
<style>html,body,#map{height:100%;margin:0;padding:0;background:#000} .info{position:absolute;top:10px;left:50%;transform:translateX(-50%);z-index:1000;background:rgba(0,0,0,0.7);color:white;padding:6px 12px;border-radius:20px;font-family:sans-serif;font-size:12px}</style>
</head>
<body>
<div class="info">Radar pluie temps reel - Segre</div>
<div id="map"></div>
<script>
var map = L.map('map').setView([$lat, $lon], 8);
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{attribution:'OSM'}).addTo(map);
L.marker([$lat, $lon]).addTo(map).bindPopup("Segre");
fetch('https://api.rainviewer.com/public/weather-maps.json')
.then(r=>r.json())
.then(data=>{
  var frames = data.radar.past;
  if(frames && frames.length>0){
    var last = frames[frames.length-1];
    var ts = last.time;
    L.tileLayer('https://tilecache.rainviewer.com/v2/radar/'+ts+'/256/{z}/{x}/{y}/2/1_1.png',{opacity:0.7}).addTo(map);
    var idx=frames.length-6; if(idx<0) idx=0;
    var layers=[];
    for(var i=idx;i<frames.length;i++){
      layers.push(L.tileLayer('https://tilecache.rainviewer.com/v2/radar/'+frames[i].time+'/256/{z}/{x}/{y}/2/1_1.png',{opacity:0.7}));
    }
    var cur=0;
    setInterval(function(){
      map.eachLayer(function(l){ if(l._url && l._url.includes('rainviewer')) map.removeLayer(l); });
      layers[cur].addTo(map);
      cur=(cur+1)%layers.length;
    },800);
  }
}).catch(e=>console.log(e));
</script>
</body>
</html>
        """.trimIndent()
        webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
    }
}

