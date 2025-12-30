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
 * Adds a callback class to the list of callbacks that will be invoked
 * when the foreground service starts/stops.
 *
 * Multiple callbacks can be registered. Callbacks are persisted
 * in SharedPreferences and survive app restarts.
 *
 * The callback class must:
 * - Implement the ForegroundServiceCallback interface
 * - Have a public no-argument constructor
 *
 * @param className Fully qualified class name (e.g., "expo.modules.blockingoverlay.BlockingCallback")
 * @returns Promise that resolves when the callback class is added
 *
 * @example
 * ```typescript
 * // Add multiple callbacks before starting service
 * await addCallbackClass('expo.modules.module1.Callback1');
 * await addCallbackClass('expo.modules.module2.Callback2');
 * await startService({ ... });
 * ```
 */
export function addCallbackClass(className: string): Promise<void> {
  return ExpoForegroundServiceModule.addCallbackClass(className)
}

/**
 * Removes a callback class from the list of registered callbacks.
 *
 * @param className Fully qualified class name to remove
 * @returns Promise that resolves when the callback class is removed
 */
export function removeCallbackClass(className: string): Promise<void> {
  return ExpoForegroundServiceModule.removeCallbackClass(className)
}

/**
 * Gets the list of all registered callback class names.
 *
 * @returns Promise that resolves with an array of registered class names
 */
export function getCallbackClasses(): Promise<string[]> {
  return ExpoForegroundServiceModule.getCallbackClasses()
}

/**
 * Clears all registered callback classes.
 *
 * @returns Promise that resolves when all callback classes are cleared
 */
export function clearAllCallbackClasses(): Promise<void> {
  return ExpoForegroundServiceModule.clearAllCallbackClasses()
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
