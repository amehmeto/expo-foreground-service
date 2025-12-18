package expo.modules.foregroundservice

import android.content.Context

/**
 * Callback interface for foreground service lifecycle events.
 * Implement this in your module and register via setCallbackClass().
 * The implementing class must have a no-arg constructor for reflection.
 */
interface ForegroundServiceCallback {
    /**
     * Called when the foreground service starts.
     * @param context Application context (not activity context)
     */
    fun onServiceStarted(context: Context)

    /**
     * Called when the foreground service is about to stop.
     */
    fun onServiceStopped()
}
