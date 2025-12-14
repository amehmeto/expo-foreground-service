# Expo Foreground Service

An Expo module for running Android foreground services. This module allows your app to continue performing tasks in the background while displaying a persistent notification to the user.

## Features

* Start/stop foreground services programmatically
* Update notification content while service is running
* Event-based architecture for service state changes
* TypeScript support
* Expo SDK 53+ compatible

## Installation

```bash
npm install @amehmeto/expo-foreground-service
```

### Configuration

Add the module to your `app.json` or `app.config.js`:

```json
{
  "expo": {
    "plugins": ["@amehmeto/expo-foreground-service"]
  }
}
```

## Usage

### Basic Service Control

```typescript
import * as ForegroundService from '@amehmeto/expo-foreground-service';
import { useEffect, useState } from 'react';
import { Button, Text, View, Platform, Alert } from 'react-native';

export default function App() {
  const [isRunning, setIsRunning] = useState(false);

  useEffect(() => {
    // Check initial state
    ForegroundService.isRunning().then(setIsRunning);

    // Subscribe to state changes
    const subscription = ForegroundService.addServiceEventListener((event) => {
      setIsRunning(event.isRunning);
    });

    return () => subscription.remove();
  }, []);

  const handleStart = async () => {
    if (Platform.OS !== 'android') {
      Alert.alert('Not Supported', 'Foreground services are only available on Android');
      return;
    }

    try {
      await ForegroundService.startService({
        channelId: 'my-service-channel',
        channelName: 'My Service',
        notificationTitle: 'Service Running',
        notificationBody: 'Tap to return to the app',
      });
    } catch (error) {
      Alert.alert('Error', `Failed to start service: ${error}`);
    }
  };

  const handleStop = async () => {
    try {
      await ForegroundService.stopService();
    } catch (error) {
      Alert.alert('Error', `Failed to stop service: ${error}`);
    }
  };

  return (
    <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
      <Text>Service Status: {isRunning ? 'Running' : 'Stopped'}</Text>
      <Button title="Start Service" onPress={handleStart} disabled={isRunning} />
      <Button title="Stop Service" onPress={handleStop} disabled={!isRunning} />
    </View>
  );
}
```

### Updating Notification Content

```typescript
import * as ForegroundService from '@amehmeto/expo-foreground-service';

// Update notification while service is running
await ForegroundService.updateNotification(
  'New Title',
  'Updated notification body text'
);
```

## API Reference

### `startService(config: ForegroundServiceConfig): Promise<void>`

Starts the foreground service with the specified configuration.

**Parameters:**

* `config`: Configuration object for the service

```typescript
type ForegroundServiceConfig = {
  channelId: string;       // Notification channel ID
  channelName: string;     // Human-readable channel name
  notificationTitle: string;  // Initial notification title
  notificationBody: string;   // Initial notification body text
};
```

**Example:**

```typescript
await ForegroundService.startService({
  channelId: 'tracking-service',
  channelName: 'Location Tracking',
  notificationTitle: 'Tracking Active',
  notificationBody: 'Your location is being tracked',
});
```

### `stopService(): Promise<void>`

Stops the foreground service.

**Example:**

```typescript
await ForegroundService.stopService();
```

### `updateNotification(title: string, body: string): Promise<void>`

Updates the notification content while the service is running.

**Parameters:**

* `title`: New notification title
* `body`: New notification body text

**Example:**

```typescript
await ForegroundService.updateNotification(
  'Still Running',
  'Elapsed time: 5 minutes'
);
```

### `isRunning(): Promise<boolean>`

Checks if the foreground service is currently running.

**Returns:** A promise that resolves to `true` if the service is running, `false` otherwise.

**Example:**

```typescript
const running = await ForegroundService.isRunning();
console.log('Service running:', running);
```

### `addServiceEventListener(listener: (event: ServiceStateEvent) => void): ServiceEventSubscription`

Registers a listener for service state change events.

**Parameters:**

* `listener`: A callback function that receives `ServiceStateEvent` objects

**Returns:** A `ServiceEventSubscription` object with a `remove()` method to unsubscribe

**Example:**

```typescript
const subscription = ForegroundService.addServiceEventListener((event) => {
  console.log('Service running:', event.isRunning);
});

// Later, to stop listening:
subscription.remove();
```

## Types

### `ForegroundServiceConfig`

Configuration object for starting the foreground service.

```typescript
type ForegroundServiceConfig = {
  channelId: string;
  channelName: string;
  notificationTitle: string;
  notificationBody: string;
};
```

### `ServiceStateEvent`

Event object emitted when the service state changes.

```typescript
type ServiceStateEvent = {
  isRunning: boolean;
};
```

### `ServiceEventSubscription`

Subscription object returned by `addServiceEventListener()`.

```typescript
type ServiceEventSubscription = {
  remove: () => void;
};
```

## Platform Support

### Android

* Full support
* Requires Android 8.0 (API 26) or higher for notification channels
* Service continues running when app is backgrounded

### iOS

* Not supported
* iOS doesn't have the same foreground service concept as Android
* Methods will return gracefully without effect

## Permissions

The module automatically declares the required permissions in the Android manifest:

* `FOREGROUND_SERVICE` - Required for foreground services
* `FOREGROUND_SERVICE_SPECIAL_USE` - Required for generic foreground service types

## Development

### Running the Example

```bash
cd example
npm install
npx expo prebuild --platform android
npx expo run:android
```

### Building the Module

```bash
npm run build
```

### Testing

```bash
npm test
```

## Related

* [Expo Modules API](https://docs.expo.dev/modules/overview/)
* [Android Foreground Services](https://developer.android.com/develop/background-work/services/foreground-services)
