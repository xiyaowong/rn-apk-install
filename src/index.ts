import { NativeModules, Platform } from 'react-native'

const LINKING_ERROR
  = `The package 'rn-apk-install' doesn't seem to be linked. Make sure: \n\n`
  + '- You rebuilt the app after installing the package\n'
  + `- You are not using Expo Go\n`

const RnApkInstall = NativeModules.RnApkInstall
  ? NativeModules.RnApkInstall
  : new Proxy(
    {},
    {
      get() {
        throw new Error(LINKING_ERROR)
      },
    },
  )

/**
 * Installs an APK file.
 *
 * @param apk - The path to the APK file.
 */
export function installApk(apk: `file://${string}` | string) {
  if (Platform.OS === 'android') {
    RnApkInstall.installApk(apk)
  }
}
