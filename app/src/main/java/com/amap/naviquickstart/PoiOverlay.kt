package com.amap.naviquickstart

import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.BitmapDescriptorFactory.fromResource
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.services.core.PoiItem
import java.util.*

class PoiOverlay(private val aMap: AMap, private val pointSet: List<PoiItem>) {
    private val poiMarks = ArrayList<Marker>()
    private val latLngBounds: LatLngBounds
        get() {
            val builder = LatLngBounds.builder()
            for (i in pointSet.indices) {
                builder.include(LatLng(pointSet[i].latLonPoint.latitude,
                        pointSet[i].latLonPoint.longitude))
            }
            return builder.build()
        }

    fun addToMap() {
        for (i in pointSet.indices) {
            val marker = aMap.addMarker(getMarkerOptions(i))
            marker.setObject(i)
            poiMarks.add(marker)
        }
    }

    fun zoomToSpan() { // 移动镜头到当前的视角
        if (pointSet.size == 1) {
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(pointSet[0]
                    .latLonPoint.latitude, pointSet[0].latLonPoint.longitude), 18f))
        } else {
            aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 5))
        }
    }

    private fun getMarkerOptions(index: Int) = MarkerOptions().position(LatLng(pointSet[index]
            .latLonPoint.latitude, pointSet[index].latLonPoint.longitude)).title(getTitle(index))
            .snippet(getSnippet(index)).icon(fromResource(R.drawable.flag))

    private fun getTitle(index: Int) = pointSet[index].title //第某个标记标题
    private fun getSnippet(index: Int) = pointSet[index].snippet //第某个标记简介
    fun getDistance(index: Int) = pointSet[index].distance.toFloat()
    fun getPoiIndex(marker: Marker): Int { //从标记中得到兴趣点在集合中的位置
        for (i in poiMarks.indices) if (poiMarks[i] == marker) return i
        return -1
    }
}
