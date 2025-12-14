import ExpoForegroundServiceModule from './ExpoForegroundServiceModule'
import type {
  ForegroundServiceConfig,
  ForegroundServiceSubscription,
} from './ExpoForegroundService.types'

/**
 * Start the foreground service with the given configuration.
 * This will show a persistent notification while the service is running.
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
export function updateNotification(
  title: string,
  body: string
): Promise<void> {
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

export type { ForegroundServiceConfig, ForegroundServiceSubscription }
