// Mock react-native
jest.mock('react-native', () => ({
  Platform: {
    OS: 'android',
  },
}))

// Mock the native module - must be before imports due to jest.mock hoisting
const mockStartService = jest.fn()
const mockStopService = jest.fn()
const mockUpdateNotification = jest.fn()
const mockIsRunning = jest.fn()
const mockAddListener = jest.fn()
const mockRequestPermissions = jest.fn()
const mockCheckPermissions = jest.fn()

jest.mock('../ExpoForegroundServiceModule', () => ({
  startService: mockStartService,
  stopService: mockStopService,
  updateNotification: mockUpdateNotification,
  isRunning: mockIsRunning,
  addListener: mockAddListener,
  requestPermissions: mockRequestPermissions,
  checkPermissions: mockCheckPermissions,
}))

// eslint-disable-next-line import/first -- Import must come after jest.mock due to hoisting
import * as ForegroundService from '../index'

describe('ExpoForegroundService', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  describe('startService', () => {
    it('calls native startService with correct config', async () => {
      const config = {
        channelId: 'test-channel',
        channelName: 'Test Channel',
        notificationTitle: 'Test Title',
        notificationBody: 'Test Body',
      }

      await ForegroundService.startService(config)

      expect(mockStartService).toHaveBeenCalledWith(
        'test-channel',
        'Test Channel',
        'Test Title',
        'Test Body',
        'ic_notification'
      )
    })

    it('uses custom notification icon when provided', async () => {
      const config = {
        channelId: 'test-channel',
        channelName: 'Test Channel',
        notificationTitle: 'Test Title',
        notificationBody: 'Test Body',
        notificationIcon: 'custom_icon',
      }

      await ForegroundService.startService(config)

      expect(mockStartService).toHaveBeenCalledWith(
        'test-channel',
        'Test Channel',
        'Test Title',
        'Test Body',
        'custom_icon'
      )
    })

    it('passes through native module errors', async () => {
      const error = new Error('Failed to start service')
      mockStartService.mockRejectedValueOnce(error)

      await expect(
        ForegroundService.startService({
          channelId: 'test',
          channelName: 'Test',
          notificationTitle: 'Title',
          notificationBody: 'Body',
        })
      ).rejects.toThrow('Failed to start service')
    })
  })

  describe('stopService', () => {
    it('calls native stopService', async () => {
      await ForegroundService.stopService()

      expect(mockStopService).toHaveBeenCalled()
    })
  })

  describe('updateNotification', () => {
    it('calls native updateNotification with title and body', async () => {
      await ForegroundService.updateNotification('New Title', 'New Body')

      expect(mockUpdateNotification).toHaveBeenCalledWith(
        'New Title',
        'New Body'
      )
    })
  })

  describe('isRunning', () => {
    it('returns true when service is running', async () => {
      mockIsRunning.mockResolvedValueOnce(true)

      const result = await ForegroundService.isRunning()

      expect(result).toBe(true)
    })

    it('returns false when service is not running', async () => {
      mockIsRunning.mockResolvedValueOnce(false)

      const result = await ForegroundService.isRunning()

      expect(result).toBe(false)
    })
  })

  describe('addServiceEventListener', () => {
    it('registers listener and returns subscription', () => {
      const mockRemove = jest.fn()
      mockAddListener.mockReturnValueOnce({ remove: mockRemove })

      const listener = jest.fn()
      const subscription = ForegroundService.addServiceEventListener(listener)

      expect(mockAddListener).toHaveBeenCalledWith(
        'onServiceStateChange',
        listener
      )
      expect(subscription).toHaveProperty('remove')
    })

    it('subscription.remove unsubscribes listener', () => {
      const mockRemove = jest.fn()
      mockAddListener.mockReturnValueOnce({ remove: mockRemove })

      const subscription = ForegroundService.addServiceEventListener(jest.fn())
      subscription.remove()

      expect(mockRemove).toHaveBeenCalled()
    })
  })

  describe('requestPermissions', () => {
    it('returns granted when permission is granted', async () => {
      mockRequestPermissions.mockResolvedValueOnce({
        granted: true,
        status: 'granted',
      })

      const result = await ForegroundService.requestPermissions()

      expect(result.granted).toBe(true)
      expect(result.status).toBe('granted')
      expect(mockRequestPermissions).toHaveBeenCalled()
    })

    it('returns denied when permission is denied', async () => {
      mockRequestPermissions.mockResolvedValueOnce({
        granted: false,
        status: 'denied',
      })

      const result = await ForegroundService.requestPermissions()

      expect(result.granted).toBe(false)
      expect(result.status).toBe('denied')
    })
  })

  describe('checkPermissions', () => {
    it('returns current permission status', async () => {
      mockCheckPermissions.mockResolvedValueOnce({
        granted: true,
        status: 'granted',
      })

      const result = await ForegroundService.checkPermissions()

      expect(result.granted).toBe(true)
      expect(result.status).toBe('granted')
      expect(mockCheckPermissions).toHaveBeenCalled()
    })
  })
})
