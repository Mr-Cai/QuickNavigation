package com.amap.naviquickstart

import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.amap.api.services.core.PoiItem
import java.util.*

class PoiOverlay(private val mAMap: AMap?, private val pointSet: List<PoiItem>?) {
    private val poiMarks = ArrayList<Marker>()
    private val latLngBounds: LatLngBounds
        get() {
            val builder = LatLngBounds.builder()
            for (i in pointSet!!.indices) {
                builder.include(LatLng(pointSet[i].latLonPoint.latitude,
                        pointSet[i].latLonPoint.longitude))
            }
            return builder.build()
        }

    private val bitmapDescriptor: BitmapDescriptor? = null //标记图标

    fun addToMap() {
        for (i in pointSet!!.indices) {
            val marker = mAMap!!.addMarker(getMarkerOptions(i))
            marker.setObject(i)
            poiMarks.add(marker)
        }
    }

    fun zoomToSpan() { // 移动镜头到当前的视角
        if (pointSet != null && pointSet.isNotEmpty()) {
            if (mAMap == null) return
            if (pointSet.size == 1) {
                mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(pointSet[0]
                        .latLonPoint.latitude, pointSet[0].latLonPoint.longitude), 18f))
            } else {
                val bounds = latLngBounds
                mAMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 5))
            }
        }
    }

    private fun getMarkerOptions(index: Int) = MarkerOptions().position(LatLng(pointSet!![index]
            .latLonPoint.latitude, pointSet[index].latLonPoint.longitude)).title(getTitle(index))
            .snippet(getSnippet(index)).icon(bitmapDescriptor)

    private fun getTitle(index: Int) = pointSet!![index].title //返回第几个标记图标的标题

    private fun getSnippet(index: Int) = pointSet!![index].snippet //返回第index的Marker的详情。

    fun getDistance(index: Int) = pointSet!![index].distance.toFloat()

    fun getPoiIndex(marker: Marker): Int { //从marker中得到poi在list的位置。
        for (i in poiMarks.indices) if (poiMarks[i] == marker) return i
        return -1
    }
}
