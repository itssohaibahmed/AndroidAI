package com.sohaib.androidai.analytics

/**
 * GA4 names shared by every app.
 * Kotlin constants are SCREAMING_SNAKE. String values are lowercase snake_case.
 * A shipped name is never renamed.
 */
object EventsProvider {

    const val SCREEN_VIEW = "screen_view"
    const val UI_CLICK = "ui_click"
    const val SPLASH_COMPLETE = "splash_complete"
    const val LANGUAGE_CONFIRM = "language_confirm"
    const val ONBOARDING_COMPLETE = "onboarding_complete"
    const val PERMISSION_GRANT = "permission_grant"
    const val PERMISSION_DENY = "permission_deny"
    const val ADS_CLICK = "ads_click"
    const val ADS_RETURN = "ads_return"
    const val ADS_SHOW_FAIL = "ads_show_fail"
    const val PREMIUM_VIEW = "premium_view"
    const val PREMIUM_CLOSE = "premium_close"
    const val IAP_START = "iap_start"
    const val IAP_SUCCESS = "iap_success"
    const val IAP_FAIL = "iap_fail"
    const val FEATURE_START = "feature_start"
    const val FEATURE_COMPLETE = "feature_complete"
    const val FEATURE_FAIL = "feature_fail"
    const val ERROR_SHOWN = "error_shown"
    const val EXIT_CONFIRM = "exit_confirm"

    const val HOME_SCREEN = "home_screen"

    const val PREVIOUS_SCREEN = "previous_screen"
    const val ENTRY_SOURCE = "entry_source"
    const val ELEMENT_NAME = "element_name"
    const val ELEMENT_TYPE = "element_type"

    const val ENTRY_ORGANIC = "organic"
    const val ENTRY_NOTIFICATION = "notification"
    const val ENTRY_DEEPLINK = "deeplink"
    const val ENTRY_AD = "ad"
    const val ENTRY_WIDGET = "widget"

    const val TYPE_BUTTON = "button"
    const val TYPE_ICON = "icon"
    const val TYPE_TAB = "tab"
    const val TYPE_CARD = "card"
    const val TYPE_ITEM = "item"
    const val TYPE_TOGGLE = "toggle"
    const val TYPE_LINK = "link"
    const val TYPE_FAB = "fab"
    const val TYPE_CHIP = "chip"
    const val TYPE_SLIDER = "slider"
}