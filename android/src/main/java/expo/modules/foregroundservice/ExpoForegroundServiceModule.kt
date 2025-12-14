package expo.modules.foregroundservice

import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import android.util.Log

class ExpoForegroundServiceModule : Module() {

    companion object {
        private const val TAG = "ExpoForegroundService"
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
