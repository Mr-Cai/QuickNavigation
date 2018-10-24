package com.amap.naviquickstart

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.SupportMapFragment
import com.amap.api.maps.model.*
import com.amap.api.navi.model.NaviLatLng
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import kotlinx.android.synthetic.main.poikeywordsearch_uri.view.*
import java.text.DecimalFormat

@Suppress("CAST_NEVER_SUCCEEDS")
class MainActivity : AppCompatActivity(), AMapLocationListener, PoiSearch.OnPoiSearchListener,
        AMap.OnInfoWindowClickListener, AMap.OnMarkerClickListener, AMap.InfoWindowAdapter {
    private var mMap: AMap? = null
    private var mPoiSearch: PoiSearch? = null
    private var mLocationClient: AMapLocationClient? = null
    private var mLocationMarker: Marker? = null
    private var mLocationCircle: Circle? = null
    private var mPoiOverlay: PoiOverlay? = null
    private var mCurrentLocation: AMapLocation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpMapIfNeeded()
        initLocation()
    }

    override fun onResume() {
        super.onResume()
        setUpMapIfNeeded()
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyLocation()
    }

    private fun setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).map
            mMap!!.setOnMarkerClickListener(this)
            mMap!!.setOnInfoWindowClickListener(this)
            mMap!!.setInfoWindowAdapter(this)
        }
    }

    private fun initPoiSearch(lat: Double, lon: Double) { //进行poi搜索
        if (mPoiSearch == null) {
            val poiQuery = PoiSearch.Query("", "餐饮服务")
            val centerPoint = LatLonPoint(lat, lon)
            val searchBound = PoiSearch.SearchBound(centerPoint, 5000)
            mPoiSearch = PoiSearch(this.applicationContext, poiQuery)
            mPoiSearch!!.bound = searchBound
            mPoiSearch!!.setOnPoiSearchListener(this)
            mPoiSearch!!.searchPOIAsyn()
        }
    }


    private fun destroyLocation() {
        if (mLocationClient != null) {
            mLocationClient!!.unRegisterLocationListener(this)
            mLocationClient!!.onDestroy()
        }
    }

    private fun initLocation() { //初始化定位
        val mLocationOption = AMapLocationClientOption()
        mLocationOption.locationMode = Hight_Accuracy
        mLocationOption.isOnceLocation = true
        mLocationClient = AMapLocationClient(this.applicationContext)
        mLocationClient!!.setLocationListener(this)
        mLocationClient!!.startLocation()
    }

    override fun onLocationChanged(aMapLocation: AMapLocation?) {
        if (aMapLocation == null || aMapLocation.errorCode != AMapLocation.LOCATION_SUCCESS) {
            Toast.makeText(this, aMapLocation!!.errorInfo + "  " + aMapLocation.errorCode,
                    Toast.LENGTH_LONG).show()
            return
        }
        mCurrentLocation = aMapLocation
        val curLatLng = LatLng(aMapLocation.latitude, aMapLocation.longitude)
        if (mLocationMarker == null) {
            val markerOptions = MarkerOptions()
            markerOptions.position(curLatLng)
            markerOptions.anchor(0.5f, 0.5f)
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.navi_map_gps_locked))
            mLocationMarker = mMap!!.addMarker(markerOptions)
        }
        if (mLocationCircle == null) {
            val circleOptions = CircleOptions()
            circleOptions.center(curLatLng)
            circleOptions.radius(aMapLocation.accuracy.toDouble())
            circleOptions.strokeWidth(2f)
            circleOptions.strokeColor(Color.BLUE)
            circleOptions.fillColor(Color.GRAY)
            mLocationCircle = mMap!!.addCircle(circleOptions)
        }
        initPoiSearch(aMapLocation.latitude, aMapLocation.longitude)
    }

    override fun onPoiSearched(poiResult: PoiResult?, i: Int) {
        if (i != AMapException.CODE_AMAP_SUCCESS || poiResult == null) return
        mPoiOverlay = PoiOverlay(mMap, poiResult.pois)
        mPoiOverlay!!.addToMap()
        mPoiOverlay!!.zoomToSpan()
    }

    override fun onPoiItemSearched(poiItem: PoiItem, i: Int) = Unit
    override fun onInfoWindowClick(marker: Marker) = Unit
    override fun onMarkerClick(marker: Marker) = if (mLocationMarker === marker) false else false
    @SuppressLint("InflateParams")
    override fun getInfoWindow(marker: Marker): View { // 自定义marker点击弹窗内容
        val view = layoutInflater.inflate(R.layout.poikeywordsearch_uri, null)
        view.locationTxT.text = marker.title
        val index = mPoiOverlay!!.getPoiIndex(marker)
        val distance = mPoiOverlay!!.getDistance(index)
        val showDistance = getFriendlyDistance(distance.toInt())
        view.snippet.text = String.format(getString(R.string.distance), showDistance)
        view.start_amap_app.setOnClickListener { _ -> startAMapNav(marker) }// 调起导航
        return view
    }

    private fun startAMapNav(marker: Marker) { //点击一键导航按钮跳转到导航页面
        if (mCurrentLocation == null) return
        val intent = Intent(this, RouteNavActivity::class.java)
        intent.putExtra("gps", false)
        intent.putExtra("start", NaviLatLng(mCurrentLocation!!.latitude, mCurrentLocation!!.longitude))
        intent.putExtra("end", NaviLatLng(marker.position.latitude, marker.position.longitude))
        startActivity(intent)
    }

    private fun getFriendlyDistance(m: Int): String {
        if (m < 1000) return m.toString() + "米"
        return DecimalFormat("##0.0").format(m / 1000f.toDouble()) + "公里"
    }

    override fun getInfoContents(marker: Marker) = null
}

