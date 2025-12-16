import { useEffect, useState } from 'react'
import { StyleSheet, Text, View, Button, Alert, Platform } from 'react-native'
import * as ForegroundService from 'expo-foreground-service'

export default function App() {
  const [isRunning, setIsRunning] = useState(false)
  const [elapsedSeconds, setElapsedSeconds] = useState(0)

  useEffect(() => {
    // Check initial state
    ForegroundService.isRunning().then(setIsRunning)

    // Subscribe to state changes
    const subscription = ForegroundService.addServiceEventListener((event) => {
      setIsRunning(event.isRunning)
      if (!event.isRunning) {
        setElapsedSeconds(0)
      }
    })

    return () => subscription.remove()
  }, [])

  useEffect(() => {
    let interval: NodeJS.Timeout | null = null

    if (isRunning) {
      interval = setInterval(() => {
        setElapsedSeconds((prev) => {
          const newValue = prev + 1
          // Update notification every 5 seconds
          if (newValue % 5 === 0) {
            ForegroundService.updateNotification(
              'Service Running',
              `Elapsed time: ${formatTime(newValue)}`
            )
          }
          return newValue
        })
      }, 1000)
    }

    return () => {
      if (interval) clearInterval(interval)
    }
  }, [isRunning])

  const formatTime = (seconds: number): string => {
    const mins = Math.floor(seconds / 60)
    const secs = seconds % 60
    return `${mins}:${secs.toString().padStart(2, '0')}`
  }

  const handleStart = async () => {
    if (Platform.OS !== 'android') {
      Alert.alert(
        'Not Supported',
        'Foreground services are only available on Android'
      )
      return
    }

    // Request notification permission using the module API
    let granted: boolean
    try {
      const result = await ForegroundService.requestPermissions()
      granted = result.granted
    } catch (error) {
      Alert.alert('Error', `Failed to request permissions: ${error}`)
      return
    }

    if (!granted) {
      Alert.alert(
        'Permission Required',
        'Notification permission is required to show the foreground service notification.'
      )
      return
    }

    try {
      await ForegroundService.startService({
        channelId: 'foreground-service-example',
        channelName: 'Example Service',
        notificationTitle: 'Service Running',
        notificationBody: 'Tap to return to the app',
      })
    } catch (error) {
      Alert.alert('Error', `Failed to start service: ${error}`)
    }
  }

  const handleStop = async () => {
    try {
      await ForegroundService.stopService()
    } catch (error) {
      Alert.alert('Error', `Failed to stop service: ${error}`)
    }
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Foreground Service Example</Text>

      <View style={styles.statusContainer}>
        <Text style={styles.statusLabel}>Status:</Text>
        <Text
          style={[styles.statusValue, isRunning ? styles.running : styles.stopped]}
        >
          {isRunning ? 'Running' : 'Stopped'}
        </Text>
      </View>

      {isRunning && (
        <View style={styles.timerContainer}>
          <Text style={styles.timerLabel}>Elapsed Time:</Text>
          <Text style={styles.timerValue}>{formatTime(elapsedSeconds)}</Text>
        </View>
      )}

      <View style={styles.buttonContainer}>
        <Button title="Start Service" onPress={handleStart} disabled={isRunning} />
        <View style={styles.buttonSpacer} />
        <Button
          title="Stop Service"
          onPress={handleStop}
          disabled={!isRunning}
          color="#ff4444"
        />
      </View>

      <Text style={styles.note}>
        The service will continue running even when the app is in the background.
        A notification will be shown while the service is active.
      </Text>
    </View>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 20,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 40,
  },
  statusContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 20,
  },
  statusLabel: {
    fontSize: 18,
    marginRight: 10,
  },
  statusValue: {
    fontSize: 18,
    fontWeight: 'bold',
  },
  running: {
    color: '#4CAF50',
  },
  stopped: {
    color: '#9E9E9E',
  },
  timerContainer: {
    alignItems: 'center',
    marginBottom: 30,
  },
  timerLabel: {
    fontSize: 16,
    color: '#666',
  },
  timerValue: {
    fontSize: 48,
    fontWeight: 'bold',
    color: '#2196F3',
  },
  buttonContainer: {
    width: '100%',
    maxWidth: 300,
    marginBottom: 40,
  },
  buttonSpacer: {
    height: 10,
  },
  note: {
    fontSize: 14,
    color: '#666',
    textAlign: 'center',
    paddingHorizontal: 20,
  },
})
