package com.rnapkinstall

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise

class RnApkInstallModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String {
        return NAME
    }

    @ReactMethod
    fun installApk(filePath: String, fileProviderAuthority: String) {
        val file = File(filePath)
        if (!file.exists()) {
            Log.e("rn-install-apk", "installApk: file does not exist '$filePath'")
            // FIXME this should take a promise and fail it
            return@Function
        }

        if (Build.VERSION.SDK_INT >= 24) {
            // API24 and up has a package installer that can handle FileProvider content:// URIs
            val contentUri: Uri
            try {
                contentUri = FileProvider.getUriForFile(
                    appContext.reactContext?.applicationContext!!,
                    fileProviderAuthority,
                    file
                )
            } catch (e: Exception) {
                // FIXME should be a Promise.reject really
                Log.e(
                    "rn-install-apk",
                    "installApk exception with authority name '$fileProviderAuthority'",
                    e
                )
                throw e
            }
            val installApp = Intent(Intent.ACTION_INSTALL_PACKAGE)
            installApp.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            installApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            installApp.data = contentUri
            installApp.putExtra(
                Intent.EXTRA_INSTALLER_PACKAGE_NAME,
                appContext.reactContext?.applicationInfo?.packageName
            )
            appContext.reactContext?.startActivity(installApp)
        } else {
            // Old APIs do not handle content:// URIs, so use an old file:// style
            val cmd = "chmod 777 $file"
            try {
                Runtime.getRuntime().exec(cmd)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.setDataAndType(
                Uri.parse("file://$file"),
                "application/vnd.android.package-archive"
            )
            appContext.reactContext?.startActivity(intent)
        }
    }
}

companion object {
    const val NAME = "RnApkInstall"
}
}
