package com.mercury.messengerportal.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.text.SimpleDateFormat
import java.util.*

/** Returns true if the device currently has active network connectivity */
fun Context.isNetworkAvailable(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

/** Formats an epoch millisecond timestamp to a human-readable time: "14:32" */
fun Long.toTimeString(): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(this))

/** Formats an epoch millisecond timestamp to date + time: "11 Apr 2026 14:32" */
fun Long.toDateTimeString(): String =
    SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date(this))

/** Formats an epoch ms timestamp to date string: "11 Apr 2026" */
fun Long.toDateString(): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(this))

/** Today's date in yyyy-MM-dd format for Room queries */
fun todayDateString(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
