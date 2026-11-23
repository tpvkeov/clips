package com.example.utils

import android.app.Activity
import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateType
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppUpdateManager(private val context: Context) {

    private val updateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)
    private var updateJob: Job? = null

    fun checkForUpdate(activity: Activity, requestCode: Int = 1001) {
        updateJob?.cancel()
        updateJob = CoroutineScope(Dispatchers.IO).launch {
            val appUpdateInfoResult = updateManager.appUpdateInfo.await()
            if (appUpdateInfoResult.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfoResult.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                withContext(Dispatchers.Main) {
                    try {
                        updateManager.startUpdateFlowForResult(
                            appUpdateInfoResult,
                            AppUpdateType.IMMEDIATE,
                            activity,
                            requestCode
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun checkForFlexibleUpdate(activity: Activity, onUpdateAvailable: () -> Unit) {
        updateJob?.cancel()
        updateJob = CoroutineScope(Dispatchers.IO).launch {
            val appUpdateInfoResult = updateManager.appUpdateInfo.await()
            if (appUpdateInfoResult.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfoResult.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                withContext(Dispatchers.Main) {
                    onUpdateAvailable()
                }
            }
        }
    }

    fun destroy() {
        updateJob?.cancel()
    }
}