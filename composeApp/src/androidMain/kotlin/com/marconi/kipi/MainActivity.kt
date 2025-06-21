package com.marconi.kipi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.marconi.kipi.snackbar_utils.SnackbarController
import com.marconi.kipi.snackbar_utils.SnackbarEvent
import kipi.composeapp.generated.resources.Res
import kipi.composeapp.generated.resources.app_updated_error
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get

class MainActivity : ComponentActivity() {
    private lateinit var appUpdateManager: AppUpdateManager
    private val updateType = AppUpdateType.IMMEDIATE
    private val requestCode = 123

    private var _snackbarController: SnackbarController? = null
    private val snackbarController: SnackbarController
        get() = _snackbarController!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _snackbarController = get<SnackbarController>()
        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkForAppUpdates()

        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode != RESULT_OK) {
                lifecycleScope.launch {
                    snackbarController.sendEvent(
                        SnackbarEvent(
                            Res.string.app_updated_error
                        )
                    )
                }
            }
        }

        setContent {
            App()
        }
    }

    private fun checkForAppUpdates() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            val isUpdateAvailable =
                appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            val isUpdateAllowed = appUpdateInfo.isImmediateUpdateAllowed
            if (isUpdateAvailable && isUpdateAllowed) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    updateType,
                    this,
                    requestCode
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (
                appUpdateInfo.updateAvailability() ==
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    updateType,
                    this,
                    requestCode
                )
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}