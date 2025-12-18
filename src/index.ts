import type {
  ForegroundServiceConfig,
  ForegroundServiceSubscription,
  PermissionResponse,
} from './ExpoForegroundService.types'
import ExpoForegroundServiceModule from './ExpoForegroundServiceModule'

/**
 * Request notification permissions required for the foreground service.
 * On Android 13+ (API 33), this will show the system permission dialog.
 * On older Android versions, this returns granted immediately.
 *
 * @returns Promise with the permission response
 *
 * @example
 * ```typescript
 * const { granted } = await requestPermissions();
 * if (granted) {
 *   await startService({ ... });
 * }
 * ```
 */
export function requestPermissions(): Promise<PermissionResponse> {
  return ExpoForegroundServiceModule.requestPermissions()
}

/**
 * Check current notification permission status without requesting.
 *
 * @returns Promise with the permission response
 */
export function checkPermissions(): Promise<PermissionResponse> {
  return ExpoForegroundServiceModule.checkPermissions()
}

/**
 * Start the foreground service with the given configuration.
 * This will show a persistent notification while the service is running.
 *
 * Note: On Android 13+, you should call requestPermissions() first.
 * The service may fail to show a notification if permission is not granted.
 *
 * @param config - Configuration for the foreground service
 * @returns Promise that resolves when the service is started
 */
export function startService(config: ForegroundServiceConfig): Promise<void> {
  return ExpoForegroundServiceModule.startService(
    config.channelId,
    config.channelName,
    config.notificationTitle,
    config.notificationBody,
    config.notificationIcon ?? 'ic_notification'
  )
}

/**
 * Stop the foreground service.
 *
 * @returns Promise that resolves when the service is stopped
 */
export function stopService(): Promise<void> {
  return ExpoForegroundServiceModule.stopService()
}

/**
 * Update the notification displayed by the foreground service.
 *
 * @param title - New notification title
 * @param body - New notification body
 * @returns Promise that resolves when the notification is updated
 */
export function updateNotification(title: string, body: string): Promise<void> {
  return ExpoForegroundServiceModule.updateNotification(title, body)
}

/**
 * Check if the foreground service is currently running.
 *
 * @returns Promise that resolves to true if the service is running
 */
export function isRunning(): Promise<boolean> {
  return ExpoForegroundServiceModule.isRunning()
}

/**
 * Sets the callback class that will be instantiated via reflection
 * when the foreground service starts/stops.
 *
 * The callback class must:
 * - Implement the ForegroundServiceCallback interface
 * - Have a public no-argument constructor
 *
 * @param className Fully qualified class name (e.g., "expo.modules.blockingoverlay.BlockingCallback")
 * @returns Promise that resolves when the callback class is set
 *
 * @example
 * ```typescript
 * // Set callback before starting service
 * await setCallbackClass('expo.modules.mymodule.MyCallback');
 * await startService({ ... });
 * ```
 */
export function setCallbackClass(className: string): Promise<void> {
  return ExpoForegroundServiceModule.setCallbackClass(className)
}

/**
 * Clears the callback class. Service will continue without callbacks.
 *
 * @returns Promise that resolves when the callback class is cleared
 */
export function clearCallbackClass(): Promise<void> {
  return ExpoForegroundServiceModule.clearCallbackClass()
}

/**
 * Add a listener for foreground service state changes.
 *
 * @param listener - Callback function that receives state change events
 * @returns A subscription object with a remove() method to unsubscribe
 *
 * @example
 * ```typescript
 * const subscription = addServiceEventListener((event) => {
 *   console.log('Service running:', event.isRunning);
 * });
 *
 * // Later, to stop listening:
 * subscription.remove();
 * ```
 */
export function addServiceEventListener(
  listener: (event: { isRunning: boolean }) => void
): ForegroundServiceSubscription {
  const subscription = ExpoForegroundServiceModule.addListener(
    'onServiceStateChange',
    listener
  )

  return {
    remove: () => subscription.remove(),
  }
}

export type {
  ForegroundServiceConfig,
  ForegroundServiceSubscription,
  PermissionResponse,
}
