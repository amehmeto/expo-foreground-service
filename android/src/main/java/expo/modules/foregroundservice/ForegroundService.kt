package expo.modules.foregroundservice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
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

        // Multi-callback support with persistence
        private const val PREFS_NAME = "expo_foreground_service_callbacks"
        private const val KEY_CALLBACK_CLASSES = "callback_class_names"

        // Thread-safe access via synchronized blocks
        private val callbackInstances: MutableMap<String, ForegroundServiceCallback> = mutableMapOf()

        private val mainHandler = Handler(Looper.getMainLooper())

        // SharedPreferences helpers
        private fun getPrefs(context: Context): SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        private fun loadCallbackClassNames(context: Context): Set<String> =
            getPrefs(context).getStringSet(KEY_CALLBACK_CLASSES, emptySet()) ?: emptySet()

        private fun saveCallbackClassNames(context: Context, classNames: Set<String>) {
            getPrefs(context).edit().putStringSet(KEY_CALLBACK_CLASSES, classNames).apply()
        }

        // Callback management methods
        fun addCallbackClass(context: Context, className: String) {
            val current = loadCallbackClassNames(context).toMutableSet()
            if (current.add(className)) {
                saveCallbackClassNames(context, current)
                Log.d(TAG, "Callback added: $className (total: ${current.size})")
            } else {
                Log.d(TAG, "Callback already registered: $className")
            }
        }

        fun removeCallbackClass(context: Context, className: String) {
            val current = loadCallbackClassNames(context).toMutableSet()
            if (current.remove(className)) {
                saveCallbackClassNames(context, current)
                synchronized(callbackInstances) { callbackInstances.remove(className) }
                Log.d(TAG, "Callback removed: $className (remaining: ${current.size})")
            } else {
                Log.d(TAG, "Callback not found for removal: $className")
            }
        }

        fun getCallbackClasses(context: Context): Set<String> = loadCallbackClassNames(context)

        fun clearAllCallbackClasses(context: Context) {
            saveCallbackClassNames(context, emptySet())
            synchronized(callbackInstances) { callbackInstances.clear() }
            Log.d(TAG, "All callback classes cleared")
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

    /**
     * Get or create callback instance, caching for reuse.
     */
    private fun getOrCreateCallbackInstance(className: String): ForegroundServiceCallback? {
        synchronized(callbackInstances) {
            callbackInstances[className]?.let { return it }

            return try {
                Log.d(TAG, "Instantiating callback: $className")
                val clazz = Class.forName(className)

                if (!ForegroundServiceCallback::class.java.isAssignableFrom(clazz)) {
                    Log.e(TAG, "$className doesn't implement ForegroundServiceCallback")
                    return null
                }

                val instance = clazz.getDeclaredConstructor().newInstance() as ForegroundServiceCallback
                callbackInstances[className] = instance
                Log.d(TAG, "Callback instance created and cached: $className")
                instance
            } catch (e: ClassNotFoundException) {
                Log.e(TAG, "Callback class not found: $className", e)
                null
            } catch (e: NoSuchMethodException) {
                Log.e(TAG, "Callback class must have no-arg constructor: $className", e)
                null
            } catch (e: Exception) {
                Log.e(TAG, "Failed to instantiate $className: ${e.message}", e)
                null
            }
        }
    }

    /**
     * Invoke onServiceStarted on all registered callbacks on the main thread.
     */
    private fun invokeCallbacksOnStart() {
        val classNames = loadCallbackClassNames(applicationContext)
        if (classNames.isEmpty()) {
            Log.d(TAG, "No callback classes registered")
            return
        }

        Log.d(TAG, "Invoking onServiceStarted for ${classNames.size} callback(s)")

        classNames.forEach { className ->
            getOrCreateCallbackInstance(className)?.let { instance ->
                mainHandler.post {
                    try {
                        instance.onServiceStarted(applicationContext)
                        Log.d(TAG, "onServiceStarted invoked for: $className")
                    } catch (e: Exception) {
                        Log.e(TAG, "onServiceStarted failed for $className: ${e.message}", e)
                    }
                }
            }
        }
    }

    /**
     * Invoke onServiceStopped on all cached callback instances on the main thread.
     */
    private fun invokeCallbacksOnStop() {
        synchronized(callbackInstances) {
            if (callbackInstances.isEmpty()) {
                Log.d(TAG, "No callback instances to notify of stop")
                return
            }

            Log.d(TAG, "Invoking onServiceStopped for ${callbackInstances.size} callback(s)")

            callbackInstances.forEach { (className, instance) ->
                mainHandler.post {
                    try {
                        instance.onServiceStopped()
                        Log.d(TAG, "onServiceStopped invoked for: $className")
                    } catch (e: Exception) {
                        Log.e(TAG, "onServiceStopped failed for $className: ${e.message}", e)
                    }
                }
            }
            // NOTE: Do NOT clear instances here - they are reused across service cycles
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

        // Invoke all registered callbacks on main thread
        invokeCallbacksOnStart()

        onStateChange?.invoke(true)

        Log.d(TAG, "Service started successfully")
        return START_STICKY
    }

    override fun onDestroy() {
        // Invoke all callbacks before cleanup
        invokeCallbacksOnStop()

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
