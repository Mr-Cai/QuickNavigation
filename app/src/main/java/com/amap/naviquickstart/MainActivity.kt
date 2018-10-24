package com.amap.naviquickstart

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.AMap.*
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.navi.model.NaviLatLng
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.poikeywordsearch_uri.view.*
import java.text.DecimalFormat

class MainActivity : AppCompatActivity(), AMapLocationListener, OnPoiSearchListener,
        OnInfoWindowClickListener, OnMarkerClickListener, InfoWindowAdapter {
    private lateinit var aMap: AMap
    private lateinit var locationClient: AMapLocationClient
    private lateinit var poiOverlay: PoiOverlay
    private lateinit var currentLocation: AMapLocation
    private var poiSearch: PoiSearch? = null
    private var locationMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapView.onCreate(savedInstanceState)
        aMap = mapView.map
        aMap.setOnMarkerClickListener(this)
        aMap.setOnInfoWindowClickListener(this)
        aMap.setInfoWindowAdapter(this)
        val locationOption = AMapLocationClientOption()
        locationOption.locationMode = Hight_Accuracy
        locationOption.isOnceLocation = true
        locationClient = AMapLocationClient(this)
        locationClient.setLocationListener(this)
        locationClient.startLocation()
    }

    private fun initPoiSearch(lat: Double, lon: Double) { //进行兴趣点搜索
        if (poiSearch == null) {
            poiSearch = PoiSearch(this, PoiSearch.Query("", "体育馆"))
            poiSearch!!.bound = PoiSearch.SearchBound(LatLonPoint(lat, lon), 5000)
            poiSearch!!.setOnPoiSearchListener(this)
            poiSearch!!.searchPOIAsyn()
        }
    }

    private fun getFriendlyDistance(m: Int): String {
        if (m < 1000) return m.toString() + "米"
        return DecimalFormat("##0.0").format(m / 1000f.toDouble()) + "公里"
    }

    override fun onLocationChanged(aMapLocation: AMapLocation) {
        currentLocation = aMapLocation
        if (locationMarker == null) {
            val markerOptions = MarkerOptions()
            markerOptions.position(LatLng(aMapLocation.latitude, aMapLocation.longitude))
            markerOptions.anchor(0.5f, 0.5f)
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.location))
            locationMarker = aMap.addMarker(markerOptions)
        }
        initPoiSearch(aMapLocation.latitude, aMapLocation.longitude)
    }

    override fun onPoiSearched(poiResult: PoiResult, code: Int) {
        poiOverlay = PoiOverlay(aMap, poiResult.pois)
        poiOverlay.addToMap()
        poiOverlay.zoomToSpan()
    }

    override fun onPoiItemSearched(poiItem: PoiItem, i: Int) = Unit
    override fun onInfoWindowClick(marker: Marker) = Unit
    override fun onMarkerClick(marker: Marker) = if (locationMarker === marker) false else false
    override fun getInfoWindow(marker: Marker): View { //自定义标记,可点击弹窗内容
        val viewGroup: ViewGroup? = null
        val view = layoutInflater.inflate(R.layout.poikeywordsearch_uri, viewGroup, false)
        view.locationTxT.text = marker.title
        val index = poiOverlay.getPoiIndex(marker)
        val distance = poiOverlay.getDistance(index)
        val showDistance = getFriendlyDistance(distance.toInt())
        view.snippet.text = String.format(getString(R.string.distance), showDistance)
        view.startRoute.setOnClickListener {
            //点击一键导航按钮跳转到导航页面
            val intent = Intent(this, RouteNavActivity::class.java)
            intent.putExtra("gps", false)
            intent.putExtra("start", NaviLatLng(currentLocation.latitude, currentLocation.longitude))
            intent.putExtra("end", NaviLatLng(marker.position.latitude, marker.position.longitude))
            startActivity(intent)
        }
        return view
    }

    override fun getInfoContents(marker: Marker) = null
}

