package expo.modules.foregroundservice

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import expo.modules.kotlin.Promise
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import expo.modules.interfaces.permissions.Permissions

class ExpoForegroundServiceModule : Module() {

    companion object {
        private const val TAG = "ExpoForegroundService"
        private const val POST_NOTIFICATIONS_PERMISSION = Manifest.permission.POST_NOTIFICATIONS
    }

    override fun definition() = ModuleDefinition {
        Name("ExpoForegroundService")

        Events("onServiceStateChange")

        OnCreate {
            Log.d(TAG, "Module created, registering state change listener")
            ForegroundService.onStateChange = { isRunning ->
                sendEvent("onServiceStateChange", mapOf("isRunning" to isRunning))
            }
        }

        OnDestroy {
            Log.d(TAG, "Module destroyed, unregistering state change listener")
            ForegroundService.onStateChange = null
        }

        AsyncFunction("requestPermissions") { promise: Promise ->
            val context = appContext.reactContext
                ?: throw Exception("Context not available")

            // POST_NOTIFICATIONS only required on Android 13+ (API 33)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                Log.d(TAG, "Android < 13, no notification permission needed")
                promise.resolve(mapOf("granted" to true, "status" to "granted"))
                return@AsyncFunction
            }

            val currentPermission = ContextCompat.checkSelfPermission(context, POST_NOTIFICATIONS_PERMISSION)
            if (currentPermission == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission already granted")
                promise.resolve(mapOf("granted" to true, "status" to "granted"))
                return@AsyncFunction
            }

            Log.d(TAG, "Requesting notification permission")
            Permissions.askForPermissionsWithPermissionsManager(
                appContext.permissions,
                promise,
                POST_NOTIFICATIONS_PERMISSION
            )
        }

        AsyncFunction("checkPermissions") { ->
            val context = appContext.reactContext
                ?: throw Exception("Context not available")

            // POST_NOTIFICATIONS only required on Android 13+ (API 33)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                return@AsyncFunction mapOf("granted" to true, "status" to "granted")
            }

            val currentPermission = ContextCompat.checkSelfPermission(context, POST_NOTIFICATIONS_PERMISSION)
            val granted = currentPermission == PackageManager.PERMISSION_GRANTED
            val status = if (granted) "granted" else "denied"

            mapOf("granted" to granted, "status" to status)
        }

        AsyncFunction("startService") {
            channelId: String,
            channelName: String,
            title: String,
            body: String,
            icon: String ->

            val context = appContext.reactContext
                ?: throw Exception("Context not available")

            Log.d(TAG, "Starting foreground service: channelId=$channelId, title=$title")
            ForegroundService.start(context, channelId, channelName, title, body, icon)
        }

        AsyncFunction("stopService") {
            val context = appContext.reactContext
                ?: throw Exception("Context not available")

            Log.d(TAG, "Stopping foreground service")
            ForegroundService.stop(context)
        }

        AsyncFunction("updateNotification") { title: String, body: String ->
            val context = appContext.reactContext
                ?: throw Exception("Context not available")

            Log.d(TAG, "Updating notification: title=$title")
            ForegroundService.updateNotification(context, title, body)
        }

        AsyncFunction("isRunning") {
            ForegroundService.isRunning
        }
    }
}
