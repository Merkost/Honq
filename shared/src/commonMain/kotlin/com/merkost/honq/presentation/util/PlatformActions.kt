package com.merkost.honq.presentation.util

expect fun openUrl(url: String)

expect fun shareText(text: String, title: String? = null)

expect fun sendEmail(email: String, subject: String = "", body: String = "")

expect fun openAppStore()

expect val storeUrl: String

expect val appVersion: String
