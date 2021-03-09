package kr.ac.kumoh.s20171278.map_map_challenge

import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionCheck(val permissionActivity: Activity, val requiresPermissions: Array<String>) {
    private val permissionRequestCode = 100

    // 권한 체크용
    public fun permissionCheck(){
        val failRequestPermissionList = ArrayList<String>()
        for (permission in requiresPermissions){
            if(ContextCompat.checkSelfPermission(permissionActivity.applicationContext, permission) != PackageManager.PERMISSION_GRANTED){
                failRequestPermissionList.add(permission)
                Log.d("ssss fail", permission)
            }
        }

        if (failRequestPermissionList.isNotEmpty()){
            val array = arrayOfNulls<String>(failRequestPermissionList.size)
            ActivityCompat.requestPermissions(permissionActivity, failRequestPermissionList.toArray(array), permissionRequestCode)
        }
    }

}