package com.merkost.honq.presentation.util

import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSCharacterSet
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController

actual fun openUrl(url: String) {
    val nsUrl = NSURL.URLWithString(url) ?: return
    UIApplication.sharedApplication.openURL(nsUrl, emptyMap<Any?, Any>(), null)
}

actual fun shareText(text: String, title: String?) {
    val activityController = UIActivityViewController(
        activityItems = listOf(text),
        applicationActivities = null
    )

    val topViewController = getTopViewController() ?: return
    activityController.popoverPresentationController?.sourceView = topViewController.view
    topViewController.presentViewController(
        activityController,
        animated = true,
        completion = null
    )
}

actual fun sendEmail(email: String, subject: String, body: String) {
    val encodedSubject = (subject as NSString)
        .stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet) ?: subject
    val encodedBody = (body as NSString)
        .stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLQueryAllowedCharacterSet) ?: body
    val urlString = "mailto:$email?subject=$encodedSubject&body=$encodedBody"
    val nsUrl = NSURL.URLWithString(urlString) ?: return
    UIApplication.sharedApplication.openURL(nsUrl, emptyMap<Any?, Any>(), null)
}

actual val storeUrl: String = "https://apps.apple.com/app/id6759510354"

actual fun openAppStore() {
    val appStoreUrl = "itms-apps://itunes.apple.com/app/id6759510354"
    val nsUrl = NSURL.URLWithString(appStoreUrl) ?: return
    UIApplication.sharedApplication.openURL(nsUrl, emptyMap<Any?, Any>(), null)
}

private fun getTopViewController(): UIViewController? {
    val keyWindow = UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .flatMap { it.windows.filterIsInstance<UIWindow>() }
        .firstOrNull { it.isKeyWindow() }
        ?: return null

    var topController = keyWindow.rootViewController ?: return null
    while (topController.presentedViewController != null) {
        topController = topController.presentedViewController!!
    }
    return topController
}
