import {
  ConfigPlugin,
  withDangerousMod,
  AndroidConfig,
} from '@expo/config-plugins'
import * as fs from 'fs'
import * as path from 'path'

interface ForegroundServicePluginProps {
  /**
   * Path to the notification icon (relative to project root).
   * Should be a PNG file. Will be copied to Android drawable folders.
   */
  notificationIcon?: string

  /**
   * Name for the notification icon resource.
   * Defaults to 'ic_notification'.
   */
  notificationIconName?: string
}

const withForegroundServiceIcon: ConfigPlugin<ForegroundServicePluginProps> = (
  config,
  props = {}
) => {
  const { notificationIcon, notificationIconName = 'ic_notification' } = props

  if (!notificationIcon) {
    return config
  }

  return withDangerousMod(config, [
    'android',
    async (config) => {
      const projectRoot = config.modRequest.projectRoot
      const platformRoot = config.modRequest.platformProjectRoot

      const sourcePath = path.resolve(projectRoot, notificationIcon)

      if (!fs.existsSync(sourcePath)) {
        throw new Error(
          `expo-foreground-service: notificationIcon file not found: ${sourcePath}`
        )
      }

      // Copy to drawable folders (using mdpi as default)
      // For production, you might want to handle multiple densities
      const drawableFolders = [
        'drawable-mdpi',
        'drawable-hdpi',
        'drawable-xhdpi',
        'drawable-xxhdpi',
        'drawable-xxxhdpi',
      ]

      const resPath = path.join(platformRoot, 'app', 'src', 'main', 'res')

      for (const folder of drawableFolders) {
        const destFolder = path.join(resPath, folder)
        const destPath = path.join(destFolder, `${notificationIconName}.png`)

        // Create folder if it doesn't exist
        if (!fs.existsSync(destFolder)) {
          fs.mkdirSync(destFolder, { recursive: true })
        }

        // Copy the icon
        fs.copyFileSync(sourcePath, destPath)
      }

      console.log(
        `expo-foreground-service: Copied notification icon to Android drawable folders as ${notificationIconName}.png`
      )

      return config
    },
  ])
}

const withForegroundService: ConfigPlugin<ForegroundServicePluginProps> = (
  config,
  props = {}
) => {
  config = withForegroundServiceIcon(config, props)
  return config
}

export default withForegroundService
