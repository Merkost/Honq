package com.merkost.honq.presentation.util

import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow

actual fun openUrl(url: String) {
    val nsUrl = NSURL.URLWithString(url) ?: return
    UIApplication.sharedApplication.openURL(nsUrl)
}

actual fun shareText(text: String, title: String?) {
    val activityItems = listOf(text)
    val activityController = UIActivityViewController(
        activityItems = activityItems,
        applicationActivities = null
    )

    val window = UIApplication.sharedApplication.windows.firstOrNull {
        (it as? UIWindow)?.isKeyWindow() == true
    } as? UIWindow
    window?.rootViewController?.presentViewController(
        activityController,
        animated = true,
        completion = null
    )
}

actual fun sendEmail(email: String, subject: String, body: String) {
    val encodedSubject = subject.replace(" ", "%20")
    val encodedBody = body.replace(" ", "%20").replace("\n", "%0A")
    val urlString = "mailto:$email?subject=$encodedSubject&body=$encodedBody"
    val nsUrl = NSURL.URLWithString(urlString) ?: return
    UIApplication.sharedApplication.openURL(nsUrl)
}

actual fun openAppStore() {
    val appStoreUrl = "https://apps.apple.com/app/honq"
    val nsUrl = NSURL.URLWithString(appStoreUrl) ?: return
    UIApplication.sharedApplication.openURL(nsUrl)
}
