/**
 * Configuration for starting the foreground service.
 */
export interface ForegroundServiceConfig {
  /**
   * Unique identifier for the notification channel.
   * Used to group notifications and allow users to customize notification settings.
   */
  channelId: string

  /**
   * Human-readable name for the notification channel.
   * Displayed in the system notification settings.
   */
  channelName: string

  /**
   * Title displayed in the notification.
   */
  notificationTitle: string

  /**
   * Body text displayed in the notification.
   */
  notificationBody: string

  /**
   * Name of the drawable resource to use as the notification icon.
   * The icon should be placed in android/app/src/main/res/drawable.
   * Defaults to 'ic_notification' if not specified.
   */
  notificationIcon?: string
}

/**
 * Subscription object returned by addServiceEventListener.
 */
export interface ForegroundServiceSubscription {
  /**
   * Removes the event listener subscription.
   */
  remove: () => void
}

/**
 * Permission status for the foreground service.
 */
export type PermissionStatus = 'granted' | 'denied' | 'undetermined'

/**
 * Result of permission check or request.
 */
export interface PermissionResponse {
  /**
   * Whether the permission is granted.
   */
  granted: boolean

  /**
   * The current status of the permission.
   */
  status: PermissionStatus
}
