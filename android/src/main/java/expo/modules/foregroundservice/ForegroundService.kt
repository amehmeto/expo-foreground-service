package expo.modules.foregroundservice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class ForegroundService : Service() {

    companion object {
        private const val TAG = "ForegroundService"
        private const val NOTIFICATION_ID = 1001

        @Volatile
        var isRunning: Boolean = false
            private set

        var onStateChange: ((Boolean) -> Unit)? = null

        // Store current config for updates
        private var currentChannelId: String = "foreground_service"
        private var currentIconName: String = "ic_notification"

        // Callback support via reflection
        @Volatile
        private var callbackClassName: String? = null

        @Volatile
        private var callbackInstance: ForegroundServiceCallback? = null

        fun setCallbackClass(className: String) {
            callbackClassName = className
            Log.d(TAG, "Callback class set: $className")
        }

        fun clearCallbackClass() {
            callbackClassName = null
            callbackInstance = null
            Log.d(TAG, "Callback class cleared")
        }

        fun start(
            context: Context,
            channelId: String,
            channelName: String,
            title: String,
            body: String,
            iconName: String
        ) {
            currentChannelId = channelId
            currentIconName = iconName

            val intent = Intent(context, ForegroundService::class.java).apply {
                putExtra("channelId", channelId)
                putExtra("channelName", channelName)
                putExtra("title", title)
                putExtra("body", body)
                putExtra("icon", iconName)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, ForegroundService::class.java)
            context.stopService(intent)
        }

        fun updateNotification(context: Context, title: String, body: String) {
            if (!isRunning) {
                Log.w(TAG, "Cannot update notification: service not running")
                return
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notification = buildNotification(context, currentChannelId, title, body, currentIconName)
            manager.notify(NOTIFICATION_ID, notification)
        }

        private fun buildNotification(
            context: Context,
            channelId: String,
            title: String,
            body: String,
            iconName: String
        ): Notification {
            val iconResId = context.resources.getIdentifier(
                iconName, "drawable", context.packageName
            ).takeIf { it != 0 } ?: android.R.drawable.ic_menu_info_details

            val pendingIntent = context.packageManager
                .getLaunchIntentForPackage(context.packageName)
                ?.let { intent ->
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    PendingIntent.getActivity(
                        context, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                }

            return NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(iconResId)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .build()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun instantiateAndInvokeCallback() {
        val className = callbackClassName ?: return

        try {
            Log.d(TAG, "Instantiating callback: $className")
            val clazz = Class.forName(className)

            // Verify it implements ForegroundServiceCallback
            if (!ForegroundServiceCallback::class.java.isAssignableFrom(clazz)) {
                Log.e(TAG, "Class $className does not implement ForegroundServiceCallback")
                return
            }

            val instance = clazz.getDeclaredConstructor().newInstance() as ForegroundServiceCallback
            callbackInstance = instance

            // Invoke with application context
            instance.onServiceStarted(applicationContext)
            Log.d(TAG, "Callback onServiceStarted invoked successfully")

        } catch (e: ClassNotFoundException) {
            Log.e(TAG, "Callback class not found: $className", e)
        } catch (e: NoSuchMethodException) {
            Log.e(TAG, "Callback class must have no-arg constructor: $className", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to instantiate callback: ${e.message}", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channelId = intent?.getStringExtra("channelId") ?: "foreground_service"
        val channelName = intent?.getStringExtra("channelName") ?: "Foreground Service"
        val title = intent?.getStringExtra("title") ?: "Running"
        val body = intent?.getStringExtra("body") ?: ""
        val iconName = intent?.getStringExtra("icon") ?: "ic_notification"

        Log.d(TAG, "Service starting: channelId=$channelId, title=$title")

        createNotificationChannel(channelId, channelName)

        val notification = buildNotification(this, channelId, title, body, iconName)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        isRunning = true

        // Instantiate and invoke callback via reflection
        instantiateAndInvokeCallback()

        onStateChange?.invoke(true)

        Log.d(TAG, "Service started successfully")
        return START_STICKY
    }

    override fun onDestroy() {
        // Invoke callback before cleanup
        try {
            callbackInstance?.onServiceStopped()
            Log.d(TAG, "Callback onServiceStopped invoked")
        } catch (e: Exception) {
            Log.e(TAG, "Error invoking callback onServiceStopped: ${e.message}", e)
        }
        callbackInstance = null

        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        isRunning = false
        onStateChange?.invoke(false)
    }

    private fun createNotificationChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Foreground service notification channel"
                setShowBadge(false)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created: $channelId")
        }
    }
}
