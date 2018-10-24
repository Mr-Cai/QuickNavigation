package com.amap.naviquickstart

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.navi.model.NaviLatLng
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.poikeywordsearch_uri.view.*
import java.text.DecimalFormat

class MainActivity : AppCompatActivity(), AMapLocationListener, PoiSearch.OnPoiSearchListener,
        AMap.OnInfoWindowClickListener, AMap.OnMarkerClickListener, AMap.InfoWindowAdapter {
    private lateinit var aMap: AMap
    private var poiSearch: PoiSearch? = null
    private var locationClient: AMapLocationClient? = null
    private var locationMarker: Marker? = null
    private var poiOverlay: PoiOverlay? = null
    private lateinit var currentLocation: AMapLocation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapView.onCreate(savedInstanceState)
        aMap = mapView.map
        aMap.setOnMarkerClickListener(this)
        aMap.setOnInfoWindowClickListener(this)
        aMap.setInfoWindowAdapter(this)
        initLocation()
    }


    private fun initPoiSearch(lat: Double, lon: Double) { //进行兴趣点搜索
        if (poiSearch == null) {
            poiSearch = PoiSearch(this, PoiSearch.Query("", "宾馆"))
            poiSearch!!.bound = PoiSearch.SearchBound(LatLonPoint(lat, lon), 5000)
            poiSearch!!.setOnPoiSearchListener(this)
            poiSearch!!.searchPOIAsyn()
        }
    }

    private fun initLocation() { //初始化定位
        val mLocationOption = AMapLocationClientOption()
        mLocationOption.locationMode = Hight_Accuracy
        mLocationOption.isOnceLocation = true
        locationClient = AMapLocationClient(this.applicationContext)
        locationClient!!.setLocationListener(this)
        locationClient!!.startLocation()
    }

    override fun onLocationChanged(aMapLocation: AMapLocation) {
        currentLocation = aMapLocation
        val curLatLng = LatLng(aMapLocation.latitude, aMapLocation.longitude)
        if (locationMarker == null) {
            val markerOptions = MarkerOptions()
            markerOptions.position(curLatLng)
            markerOptions.anchor(0.5f, 0.5f)
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_nav))
            locationMarker = aMap.addMarker(markerOptions)
        }
        initPoiSearch(aMapLocation.latitude, aMapLocation.longitude)
    }

    override fun onPoiSearched(poiResult: PoiResult?, i: Int) {
        if (i != AMapException.CODE_AMAP_SUCCESS || poiResult == null) return
        poiOverlay = PoiOverlay(aMap, poiResult.pois)
        poiOverlay!!.addToMap()
        poiOverlay!!.zoomToSpan()
    }

    override fun onPoiItemSearched(poiItem: PoiItem, i: Int) = Unit
    override fun onInfoWindowClick(marker: Marker) = Unit
    override fun onMarkerClick(marker: Marker) = if (locationMarker === marker) false else false
    @SuppressLint("InflateParams")
    override fun getInfoWindow(marker: Marker): View { // 自定义标记,可点击弹窗内容
        val view = layoutInflater.inflate(R.layout.poikeywordsearch_uri, null)
        view.locationTxT.text = marker.title
        val index = poiOverlay!!.getPoiIndex(marker)
        val distance = poiOverlay!!.getDistance(index)
        val showDistance = getFriendlyDistance(distance.toInt())
        view.snippet.text = String.format(getString(R.string.distance), showDistance)
        view.start_amap_app.setOnClickListener { _ -> startAMapNav(marker) }// 调起导航
        return view
    }

    private fun startAMapNav(marker: Marker) { //点击一键导航按钮跳转到导航页面
        val intent = Intent(this, RouteNavActivity::class.java)
        intent.putExtra("gps", false)
        intent.putExtra("start", NaviLatLng(currentLocation.latitude, currentLocation.longitude))
        intent.putExtra("end", NaviLatLng(marker.position.latitude, marker.position.longitude))
        startActivity(intent)
    }

    private fun getFriendlyDistance(m: Int): String {
        if (m < 1000) return m.toString() + "米"
        return DecimalFormat("##0.0").format(m / 1000f.toDouble()) + "公里"
    }

    override fun getInfoContents(marker: Marker) = null
}

