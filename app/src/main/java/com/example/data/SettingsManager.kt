package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("is_dark_theme", true))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean("notifications_enabled", true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled

    private val _warningDays = MutableStateFlow(prefs.getInt("warning_days", 1))
    val warningDays: StateFlow<Int> = _warningDays

    private val _preferredCurrency = MutableStateFlow(prefs.getString("preferred_currency", "TRY") ?: "TRY")
    val preferredCurrency: StateFlow<String> = _preferredCurrency

    private val _monthlyBudget = MutableStateFlow(prefs.getFloat("monthly_budget", 0f).toDouble())
    val monthlyBudget: StateFlow<Double> = _monthlyBudget

    private val _webDavUrl = MutableStateFlow(prefs.getString("webdav_url", "") ?: "")
    val webDavUrl: StateFlow<String> = _webDavUrl

    private val _webDavUsername = MutableStateFlow(prefs.getString("webdav_username", "") ?: "")
    val webDavUsername: StateFlow<String> = _webDavUsername

    private val _webDavPassword = MutableStateFlow(prefs.getString("webdav_password", "") ?: "")
    val webDavPassword: StateFlow<String> = _webDavPassword

    private val _webDavPath = MutableStateFlow(prefs.getString("webdav_path", "subscriptions_backup.json") ?: "subscriptions_backup.json")
    val webDavPath: StateFlow<String> = _webDavPath

    private val _webDavLastSync = MutableStateFlow(prefs.getString("webdav_last_sync", "Hiç yapılmadı") ?: "Hiç yapılmadı")
    val webDavLastSync: StateFlow<String> = _webDavLastSync

    private val _webDavLastStatus = MutableStateFlow(prefs.getString("webdav_last_status", "Bilinmiyor") ?: "Bilinmiyor")
    val webDavLastStatus: StateFlow<String> = _webDavLastStatus

    private val _webDavAutoSyncOnLaunch = MutableStateFlow(prefs.getBoolean("webdav_auto_sync_on_launch", false))
    val webDavAutoSyncOnLaunch: StateFlow<Boolean> = _webDavAutoSyncOnLaunch

    private val _webDavAutoSyncOnChange = MutableStateFlow(prefs.getBoolean("webdav_auto_sync_on_change", false))
    val webDavAutoSyncOnChange: StateFlow<Boolean> = _webDavAutoSyncOnChange

    private val _webDavConflictStrategy = MutableStateFlow(prefs.getString("webdav_conflict_strategy", "merge") ?: "merge")
    val webDavConflictStrategy: StateFlow<String> = _webDavConflictStrategy

    private val _language = MutableStateFlow(prefs.getString("app_language", "device") ?: "device")
    val language: StateFlow<String> = _language

    private val _isFirstLaunch = MutableStateFlow(prefs.getBoolean("is_first_launch", true))
    val isFirstLaunch: StateFlow<Boolean> = _isFirstLaunch

    private val defaultCategories = setOf("Streaming", "Gaming", "Cloud", "Music", "Work", "Other")
    private val _categories = MutableStateFlow(prefs.getStringSet("custom_categories", defaultCategories) ?: defaultCategories)
    val categories: StateFlow<Set<String>> = _categories

    fun setCategories(newCategories: Set<String>) {
        prefs.edit().putStringSet("custom_categories", newCategories).apply()
        _categories.value = newCategories
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString("app_language", lang).apply()
        _language.value = lang
    }

    fun setFirstLaunchCompleted() {
        prefs.edit().putBoolean("is_first_launch", false).apply()
        _isFirstLaunch.value = false
    }

    fun setDarkTheme(enabled: Boolean) {
        prefs.edit().putBoolean("is_dark_theme", enabled).apply()
        _isDarkTheme.value = enabled
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
        _notificationsEnabled.value = enabled
    }

    fun setWarningDays(days: Int) {
        prefs.edit().putInt("warning_days", days).apply()
        _warningDays.value = days
    }

    fun setPreferredCurrency(currency: String) {
        prefs.edit().putString("preferred_currency", currency).apply()
        _preferredCurrency.value = currency
    }

    fun setMonthlyBudget(budget: Double) {
        prefs.edit().putFloat("monthly_budget", budget.toFloat()).apply()
        _monthlyBudget.value = budget
    }

    fun setWebDavCredentials(url: String, user: String, pass: String, path: String) {
        prefs.edit()
            .putString("webdav_url", url)
            .putString("webdav_username", user)
            .putString("webdav_password", pass)
            .putString("webdav_path", path)
            .apply()
        _webDavUrl.value = url
        _webDavUsername.value = user
        _webDavPassword.value = pass
        _webDavPath.value = path
    }

    fun setWebDavLastSync(timeStr: String) {
        prefs.edit().putString("webdav_last_sync", timeStr).apply()
        _webDavLastSync.value = timeStr
    }

    fun setWebDavLastStatus(statusStr: String) {
        prefs.edit().putString("webdav_last_status", statusStr).apply()
        _webDavLastStatus.value = statusStr
    }

    fun setWebDavAutoSyncOnLaunch(enabled: Boolean) {
        prefs.edit().putBoolean("webdav_auto_sync_on_launch", enabled).apply()
        _webDavAutoSyncOnLaunch.value = enabled
    }

    fun setWebDavAutoSyncOnChange(enabled: Boolean) {
        prefs.edit().putBoolean("webdav_auto_sync_on_change", enabled).apply()
        _webDavAutoSyncOnChange.value = enabled
    }

    fun setWebDavConflictStrategy(strategy: String) {
        prefs.edit().putString("webdav_conflict_strategy", strategy).apply()
        _webDavConflictStrategy.value = strategy
    }
}
