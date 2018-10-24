@file:Suppress("DEPRECATION", "OverridingDeprecatedMember")

package com.amap.naviquickstart

import android.app.Activity
import android.os.Bundle
import android.view.Window
import com.amap.api.navi.AMapNavi
import com.amap.api.navi.AMapNaviListener
import com.amap.api.navi.AMapNaviViewListener
import com.amap.api.navi.model.*
import com.autonavi.tbt.TrafficFacilityInfo
import kotlinx.android.synthetic.main.activity_nav.*
import java.util.*

class RouteNavActivity : Activity(), AMapNaviListener, AMapNaviViewListener {
    private lateinit var aMapNav: AMapNavi
    private var mIsGps: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_nav)
        navi_view.onCreate(savedInstanceState)
        navi_view.setAMapNaviViewListener(this)
        aMapNav = AMapNavi.getInstance(applicationContext)
        aMapNav.addAMapNaviListener(this)
        aMapNav.setUseInnerVoice(true)
        aMapNav.setEmulatorNaviSpeed(60)
        getNavParam()
    }

    private fun getNavParam() { // 获取intent参数并计算路线
        val intent = intent ?: return
        mIsGps = intent.getBooleanExtra("gps", false)
        val start = intent.getParcelableExtra<NaviLatLng>("start")
        val end = intent.getParcelableExtra<NaviLatLng>("end")
        calculateDriveRoute(start, end)
    }

    private fun calculateDriveRoute(start: NaviLatLng, end: NaviLatLng) {//驾车路径规划计算,计算单条路径
        val strategyFlag: Int = aMapNav.strategyConvert(true, false, false, true, false)
        val startList = ArrayList<NaviLatLng>()
        val wayList = ArrayList<NaviLatLng>()
        val endList = ArrayList<NaviLatLng>() //终点坐标集合［建议就一个终点］
        startList.add(start)
        endList.add(end)
        aMapNav.calculateDriveRoute(startList, endList, wayList, strategyFlag)
    }

    override fun onResume() {
        super.onResume()
        navi_view.onResume()
    }

    override fun onPause() {
        super.onPause()
        navi_view.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        navi_view.onDestroy()
        aMapNav.stopNavi()
    }

    override fun onInitNaviFailure() = Unit
    override fun onInitNaviSuccess() = Unit
    override fun onStartNavi(type: Int) = Unit
    override fun onTrafficStatusUpdate() = Unit
    override fun onLocationChange(location: AMapNaviLocation) = Unit
    override fun onGetNavigationText(type: Int, text: String) = Unit
    override fun onGetNavigationText(s: String) = Unit
    override fun onEndEmulatorNavi() {}
    override fun onArriveDestination() {}
    override fun onCalculateRouteFailure(errorInfo: Int) {}
    override fun onReCalculateRouteForYaw() = Unit
    override fun onReCalculateRouteForTrafficJam() = Unit
    override fun onArrivedWayPoint(wayID: Int) = Unit
    override fun onGpsOpenStatus(enabled: Boolean) = Unit
    override fun onNaviSetting() = Unit
    override fun onNaviMapMode(isLock: Int) = Unit
    override fun onNaviCancel() = finish()
    override fun onNaviTurnClick() = Unit
    override fun onNextRoadClick() = Unit
    override fun onScanViewButtonClick() = Unit
    override fun onNaviInfoUpdated(naviInfo: AMapNaviInfo) = Unit
    override fun updateCameraInfo(aMapNaviCameraInfos: Array<AMapNaviCameraInfo>) = Unit
    override fun updateIntervalCameraInfo(cameraInfo: AMapNaviCameraInfo, naviCameraInfo:
    AMapNaviCameraInfo, i: Int) = Unit

    override fun onServiceAreaUpdate(aMapServiceAreaInfos: Array<AMapServiceAreaInfo>) = Unit
    override fun onNaviInfoUpdate(naviinfo: NaviInfo) = Unit
    override fun OnUpdateTrafficFacility(trafficFacilityInfo: TrafficFacilityInfo) = Unit
    override fun OnUpdateTrafficFacility(facilityInfo: AMapNaviTrafficFacilityInfo) = Unit
    override fun showCross(aMapNaviCross: AMapNaviCross) {}
    override fun hideCross() {}
    override fun showModeCross(aMapModelCross: AMapModelCross) = Unit
    override fun hideModeCross() = Unit
    override fun showLaneInfo(infos: Array<AMapLaneInfo>, bytes: ByteArray, info: ByteArray) = Unit
    override fun showLaneInfo(aMapLaneInfo: AMapLaneInfo) = Unit
    override fun hideLaneInfo() = Unit
    override fun onCalculateRouteSuccess(ints: IntArray) {
        if (mIsGps) aMapNav.startNavi(AMapNavi.GPSNaviMode)
        else aMapNav.startNavi(AMapNavi.EmulatorNaviMode)
    }

    override fun notifyParallelRoad(i: Int) = Unit
    override fun OnUpdateTrafficFacility(facilityInfos: Array<AMapNaviTrafficFacilityInfo>) = Unit
    override fun updateAimlessModeStatistics(aimLessModeStat: AimLessModeStat) = Unit
    override fun updateAimlessModeCongestionInfo(congestionInfo: AimLessModeCongestionInfo) = Unit
    override fun onPlayRing(i: Int) = Unit
    override fun onCalculateRouteSuccess(aMapCalcRouteResult: AMapCalcRouteResult) = Unit
    override fun onCalculateRouteFailure(aMapCalcRouteResult: AMapCalcRouteResult) = Unit
    override fun onNaviRouteNotify(aMapNaviRouteNotifyData: AMapNaviRouteNotifyData) = Unit
    override fun onLockMap(isLock: Boolean) = Unit
    override fun onNaviViewLoaded() = Unit
    override fun onMapTypeChanged(i: Int) = Unit
    override fun onNaviViewShowMode(i: Int) = Unit
    override fun onNaviBackClick() = false
}
