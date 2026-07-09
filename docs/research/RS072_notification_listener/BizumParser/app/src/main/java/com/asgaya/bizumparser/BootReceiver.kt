package com.asgaya.bizumparser

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Receives BOOT_COMPLETED broadcast to ensure BizumParser auto-starts after reboot.
 *
 * Note: NotificationListenerService auto-starts automatically when notification access
 * is granted, so we don't need to manually start the service here. This receiver
 * primarily serves as a hook for any future boot-time initialization logic.
 *
 * Asgaya Phase -1: Production hardening
 * Research: RS073 (NotifyFlow comparison)
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed - BizumParser ready")
            Log.d(TAG, "NotificationListenerService will auto-start when notification access is enabled")

            // Future: Add any boot-time initialization here
            // - Check if notification access is still granted
            // - Verify database integrity
            // - Send status notification
        }
    }

    companion object {
        private const val TAG = "BizumParser-Boot"
    }
}
