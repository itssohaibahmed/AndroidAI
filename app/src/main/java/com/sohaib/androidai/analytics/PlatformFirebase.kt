package com.sohaib.androidai.analytics

import android.os.Bundle
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.sohaib.androidai.Constants.TAG_FIREBASE

object PlatformFirebase {

    private var currentScreen: String? = null
    private var previousScreen: String? = null
    private var entrySource: String = EventsProvider.ENTRY_ORGANIC

    fun setEntrySource(source: String) {
        entrySource = source
    }

    fun postScreenView(screenName: String, screenClass: String) {
        val screenBefore = when {
            currentScreen != null && currentScreen != screenName -> currentScreen
            else -> previousScreen
        }
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
            screenBefore?.let { putString(EventsProvider.PREVIOUS_SCREEN, it) }
            putString(EventsProvider.ENTRY_SOURCE, entrySource)
        }
        logEvent("postScreenView", EventsProvider.SCREEN_VIEW, bundle)
        if (currentScreen != null && currentScreen != screenName) {
            previousScreen = currentScreen
        }
        currentScreen = screenName
    }

    fun postUiClick(screenName: String, elementName: String, elementType: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(EventsProvider.ELEMENT_NAME, elementName)
            putString(EventsProvider.ELEMENT_TYPE, elementType)
        }
        logEvent("postUiClick", EventsProvider.UI_CLICK, bundle)
    }

    private fun logEvent(functionName: String, eventName: String, bundle: Bundle) {
        runCatching {
            Firebase.analytics.logEvent(eventName, bundle)
        }.onSuccess {
            Log.d(TAG_FIREBASE, "PlatformFirebase: $functionName: Success: event=$eventName")
        }.onFailure { error ->
            Log.e(
                TAG_FIREBASE,
                "PlatformFirebase: $functionName: Failed: event=$eventName ${error.message}",
            )
        }
    }
}
