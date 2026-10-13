package com.example.ui

import android.content.Context
import android.content.Intent
import com.example.util.Localization.getString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Subscription
import com.example.data.SubscriptionRepository
import com.example.data.SettingsManager
import com.example.util.NotificationHelper
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

sealed class WebDavState {
    object Idle : WebDavState()
    object Loading : WebDavState()
    data class Success(val message: String) : WebDavState()
    data class Error(val errorMessage: String) : WebDavState()
}

class SubscriptionViewModel(
    private val repository: SubscriptionRepository,
    private val settingsManager: SettingsManager
) : ViewModel() {

    val allSubscriptions: StateFlow<List<Subscription>> = repository.allSubscriptions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isDarkTheme = settingsManager.isDarkTheme
    val notificationsEnabled = settingsManager.notificationsEnabled
    val warningDays = settingsManager.warningDays
    val preferredCurrency = settingsManager.preferredCurrency
    val monthlyBudget = settingsManager.monthlyBudget
    val language = settingsManager.language
    val isFirstLaunch = settingsManager.isFirstLaunch
    val categories = settingsManager.categories

    fun addCategory(category: String) {
        val current = settingsManager.categories.value.toMutableSet()
        current.add(category)
        settingsManager.setCategories(current)
    }

    fun removeCategory(category: String) {
        val current = settingsManager.categories.value.toMutableSet()
        current.remove(category)
        settingsManager.setCategories(current)
    }

    // WebDAV State flow and preferences
    val webDavUrl = settingsManager.webDavUrl
    val webDavUsername = settingsManager.webDavUsername
    val webDavPassword = settingsManager.webDavPassword
    val webDavPath = settingsManager.webDavPath
    val webDavLastSync = settingsManager.webDavLastSync
    val webDavLastStatus = settingsManager.webDavLastStatus
    val webDavAutoSyncOnLaunch = settingsManager.webDavAutoSyncOnLaunch
    val webDavAutoSyncOnChange = settingsManager.webDavAutoSyncOnChange
    val webDavConflictStrategy = settingsManager.webDavConflictStrategy

    private val _webDavState = MutableStateFlow<WebDavState>(WebDavState.Idle)
    val webDavState: StateFlow<WebDavState> = _webDavState.asStateFlow()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, Subscription::class.java)
    private val jsonAdapter = moshi.adapter<List<Subscription>>(listType)

    fun saveWebDavCredentials(
        url: String, 
        user: String, 
        pass: String, 
        path: String,
        autoSyncOnLaunch: Boolean,
        autoSyncOnChange: Boolean,
        conflictStrategy: String
    ) {
        settingsManager.setWebDavCredentials(url, user, pass, path)
        settingsManager.setWebDavAutoSyncOnLaunch(autoSyncOnLaunch)
        settingsManager.setWebDavAutoSyncOnChange(autoSyncOnChange)
        settingsManager.setWebDavConflictStrategy(conflictStrategy)
    }

    fun resetWebDavState() {
        _webDavState.value = WebDavState.Idle
    }

    fun testWebDavConnection(url: String, user: String, pass: String, path: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _webDavState.value = WebDavState.Loading
            val result = com.example.util.WebDavSyncManager.testConnectionDiagnostics(url, user, pass, path)
            result.fold(
                onSuccess = { diagnostics ->
                    if (diagnostics.isAuthorized) {
                        _webDavState.value = WebDavState.Success(diagnostics.message)
                    } else {
                        _webDavState.value = WebDavState.Error(diagnostics.message)
                    }
                },
                onFailure = { error ->
                    _webDavState.value = WebDavState.Error("Bağlantı hatası: ${error.message ?: "Bilinmeyen hata"}")
                }
            )
        }
    }

    fun syncWithWebDav() {
        val url = webDavUrl.value
        val user = webDavUsername.value
        val pass = webDavPassword.value
        val path = webDavPath.value
        val strategy = webDavConflictStrategy.value

        if (url.isBlank()) {
            _webDavState.value = WebDavState.Error("WebDAV sunucu adresi girilmemiş.")
            return
        }

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _webDavState.value = WebDavState.Loading
            val localSubs = repository.allSubscriptions.first()
            val restoreResult = com.example.util.WebDavSyncManager.restore(url, user, pass, path)

            restoreResult.fold(
                onSuccess = { cloudJson ->
                    try {
                        val cloudSubs = jsonAdapter.fromJson(cloudJson) ?: emptyList()
                        val timestamp = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("tr")).format(Date())

                        when (strategy) {
                            "merge" -> {
                                val mergedList = mutableListOf<Subscription>()
                                mergedList.addAll(localSubs)

                                for (cloudSub in cloudSubs) {
                                    val exists = localSubs.any { localSub ->
                                        localSub.name.equals(cloudSub.name, ignoreCase = true) &&
                                        localSub.category == cloudSub.category
                                    }
                                    if (!exists) {
                                        mergedList.add(cloudSub.copy(id = 0))
                                    }
                                }

                                repository.deleteAll()
                                for (sub in mergedList) {
                                    repository.insert(sub)
                                }

                                val mergedJson = jsonAdapter.toJson(mergedList)
                                val backupResult = com.example.util.WebDavSyncManager.backup(url, user, pass, path, mergedJson)

                                backupResult.fold(
                                    onSuccess = {
                                        settingsManager.setWebDavLastSync(timestamp)
                                        settingsManager.setWebDavLastStatus("Eşitleme Başarılı (Grup)")
                                        _webDavState.value = WebDavState.Success("Eşitleme başarıyla tamamlandı (Grup birleştirildi)! ($timestamp)")
                                    },
                                    onFailure = { error ->
                                        settingsManager.setWebDavLastStatus("Eşitleme Başarısız")
                                        _webDavState.value = WebDavState.Error("Eşitleme sırasında yükleme hatası: ${error.message}")
                                    }
                                )
                            }
                            "local" -> {
                                val localJson = jsonAdapter.toJson(localSubs)
                                val backupResult = com.example.util.WebDavSyncManager.backup(url, user, pass, path, localJson)
                                backupResult.fold(
                                    onSuccess = {
                                        settingsManager.setWebDavLastSync(timestamp)
                                        settingsManager.setWebDavLastStatus("Yedekleme Başarılı (Yerel)")
                                        _webDavState.value = WebDavState.Success("Yedekleme başarıyla tamamlandı! ($timestamp)")
                                    },
                                    onFailure = { error ->
                                        settingsManager.setWebDavLastStatus("Yedekleme Başarısız")
                                        _webDavState.value = WebDavState.Error("Yedekleme hatası: ${error.message}")
                                    }
                                )
                            }
                            "cloud" -> {
                                repository.deleteAll()
                                for (sub in cloudSubs) {
                                    repository.insert(sub)
                                }
                                settingsManager.setWebDavLastSync(timestamp)
                                settingsManager.setWebDavLastStatus("Geri Yükleme Başarılı (Bulut)")
                                _webDavState.value = WebDavState.Success("Geri yükleme başarıyla tamamlandı! ${cloudSubs.size} adet abonelik aktarıldı.")
                            }
                            else -> {
                                _webDavState.value = WebDavState.Error("Bilinmeyen eşitleme stratejisi: $strategy")
                            }
                        }
                    } catch (e: Exception) {
                        settingsManager.setWebDavLastStatus("Hata")
                        _webDavState.value = WebDavState.Error("Eşitleme verisi dönüştürme hatası: ${e.message}")
                    }
                },
                onFailure = { error ->
                    if (error.message?.contains("bulunamadı") == true || error.message?.contains("404") == true) {
                        val timestamp = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("tr")).format(Date())
                        val localJson = jsonAdapter.toJson(localSubs)
                        val backupResult = com.example.util.WebDavSyncManager.backup(url, user, pass, path, localJson)
                        backupResult.fold(
                            onSuccess = {
                                settingsManager.setWebDavLastSync(timestamp)
                                settingsManager.setWebDavLastStatus("Yedekleme Başarılı")
                                _webDavState.value = WebDavState.Success("WebDAV yedek dosyası yeni başlatıldı ve yerel verileriniz yüklendi! ($timestamp)")
                            },
                            onFailure = { backupError ->
                                settingsManager.setWebDavLastStatus("Başlatma Başarısız")
                                _webDavState.value = WebDavState.Error("İlk yedek oluşturma hatası: ${backupError.message}")
                            }
                        )
                    } else {
                        settingsManager.setWebDavLastStatus("Bağlantı Başarısız")
                        _webDavState.value = WebDavState.Error("Buluttan yedek alınamadı: ${error.message ?: "Bilinmeyen hata"}")
                    }
                }
            )
        }
    }

    fun syncWithWebDavOnLaunch() {
        if (webDavUrl.value.isNotBlank() && webDavAutoSyncOnLaunch.value) {
            syncWithWebDav()
        }
    }

    private fun backupToWebDavSilent() {
        val url = webDavUrl.value
        val user = webDavUsername.value
        val pass = webDavPassword.value
        val path = webDavPath.value
        if (url.isBlank()) return

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val localSubs = repository.allSubscriptions.first()
            try {
                val json = jsonAdapter.toJson(localSubs)
                val result = com.example.util.WebDavSyncManager.backup(url, user, pass, path, json)
                result.fold(
                    onSuccess = {
                        val timestamp = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("tr")).format(Date())
                        settingsManager.setWebDavLastSync(timestamp)
                        settingsManager.setWebDavLastStatus("Eşitleme Başarılı (Otomatik)")
                    },
                    onFailure = {
                        settingsManager.setWebDavLastStatus("Otomatik Eşitleme Başarısız")
                    }
                )
            } catch (e: Exception) {
                // Ignore silent failure
            }
        }
    }

    // Standard conversion rates relative to TRY (Lira)
    val conversionRates = mapOf(
        "TRY" to 1.0,
        "USD" to 33.0,
        "EUR" to 36.0,
        "GBP" to 42.0
    )

    fun convertCurrency(amount: Double, from: String, to: String): Double {
        val amountInTry = amount * (conversionRates[from] ?: 1.0)
        return amountInTry / (conversionRates[to] ?: 1.0)
    }

    // Calculations based on current subscriptions and selected preferred currency
    val statsState = combine(allSubscriptions, preferredCurrency) { subs, currency ->
        var monthlyTotal = 0.0
        var annualTotal = 0.0
        val categoryMonthly = mutableMapOf<String, Double>()

        for (sub in subs) {
            val amountInPreferred = convertCurrency(sub.amount, sub.currency, currency)
            
            // Monthly calculation
            val subMonthly = if (sub.billingCycle == "Monthly") {
                amountInPreferred
            } else {
                amountInPreferred / 12.0
            }
            monthlyTotal += subMonthly

            // Annual calculation
            val subAnnual = if (sub.billingCycle == "Yearly") {
                amountInPreferred
            } else {
                amountInPreferred * 12.0
            }
            annualTotal += subAnnual

            // Category group (monthly breakdown)
            val currentVal = categoryMonthly[sub.category] ?: 0.0
            categoryMonthly[sub.category] = currentVal + subMonthly
        }

        SubscriptionStats(
            monthlyTotal = monthlyTotal,
            annualTotal = annualTotal,
            categoryMonthlyBreakdown = categoryMonthly
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SubscriptionStats())

    // Filter payments approaching based on warning days setting
    val upcomingPayments = combine(allSubscriptions, warningDays) { subs, days ->
        val now = System.currentTimeMillis()
        val limit = now + (days.toLong() * 24 * 60 * 60 * 1000)
        subs.filter { sub ->
            sub.nextPaymentDate in now..limit
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleDarkTheme(enabled: Boolean) {
        settingsManager.setDarkTheme(enabled)
    }

    fun toggleNotifications(enabled: Boolean) {
        settingsManager.setNotificationsEnabled(enabled)
    }

    fun setWarningDays(days: Int) {
        settingsManager.setWarningDays(days)
    }

    fun setPreferredCurrency(currency: String) {
        settingsManager.setPreferredCurrency(currency)
    }

    fun setMonthlyBudget(budget: Double) {
        settingsManager.setMonthlyBudget(budget)
    }

    fun setLanguage(lang: String) {
        settingsManager.setLanguage(lang)
    }

    fun setFirstLaunchCompleted() {
        settingsManager.setFirstLaunchCompleted()
    }

    fun addSubscription(
        name: String,
        amount: Double,
        currency: String,
        billingCycle: String,
        category: String,
        nextPaymentDate: Long,
        reminderEnabled: Boolean,
        description: String
    ) {
        viewModelScope.launch {
            val newSub = Subscription(
                name = name,
                amount = amount,
                currency = currency,
                billingCycle = billingCycle,
                category = category,
                nextPaymentDate = nextPaymentDate,
                reminderEnabled = reminderEnabled,
                description = description
            )
            repository.insert(newSub)
            if (webDavAutoSyncOnChange.value) {
                backupToWebDavSilent()
            }
        }
    }

    fun deleteSubscription(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
            if (webDavAutoSyncOnChange.value) {
                backupToWebDavSilent()
            }
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            repository.deleteAll()
            if (webDavAutoSyncOnChange.value) {
                backupToWebDavSilent()
            }
        }
    }

    // Trigger test payment reminder immediately
    fun triggerTestNotification(context: Context) {
        if (!notificationsEnabled.value) return
        viewModelScope.launch {
            val subs = allSubscriptions.value
            if (subs.isEmpty()) {
                NotificationHelper.postNotification(
                    context,
                    999,
                    "Abonelik Hatırlatıcı Testi",
                    "Henüz kayıtlı bir aboneliğiniz yok, ancak bildirimleriniz başarıyla aktif!"
                )
            } else {
                val sub = subs.first()
                val formattedAmount = String.format(Locale.US, "%.2f %s", sub.amount, sub.currency)
                NotificationHelper.postNotification(
                    context,
                    sub.id,
                    "Ödeme Yaklaşıyor: ${sub.name}",
                    "${sub.name} aboneliğinizin ödeme tarihi yaklaşıyor. Tutar: $formattedAmount"
                )
            }
        }
    }

    private var hasCheckedUpcomingPaymentsThisSession = false

    // Scan and post notifications for all upcoming bills
    fun checkUpcomingPaymentsAndNotify(context: Context) {
        if (!notificationsEnabled.value) return
        if (hasCheckedUpcomingPaymentsThisSession) return
        
        val upcoming = upcomingPayments.value
        if (upcoming.isNotEmpty()) {
            hasCheckedUpcomingPaymentsThisSession = true
        }

        for (sub in upcoming) {
            if (sub.reminderEnabled) {
                val formattedAmount = String.format(Locale.US, "%.2f %s", sub.amount, sub.currency)
                NotificationHelper.postNotification(
                    context,
                    sub.id,
                    "Yaklaşan Ödeme: ${sub.name}",
                    "${sub.name} ödemeniz çok yakında! Ödeme Tutarı: $formattedAmount"
                )
            }
        }
    }

    // --- Local File Backup & Restore ---
    fun exportBackupToFile(context: Context, uri: android.net.Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                _webDavState.value = WebDavState.Loading
                val subs = allSubscriptions.value
                val json = jsonAdapter.toJson(subs)
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(json.toByteArray(Charsets.UTF_8))
                }
                val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr"))
                val timestamp = sdf.format(Date())
                _webDavState.value = WebDavState.Success("Yerel yedekleme başarıyla tamamlandı! ($timestamp)")
            } catch (e: Exception) {
                _webDavState.value = WebDavState.Error("Yerel yedekleme hatası: ${e.message}")
            }
        }
    }

    fun importBackupFromFile(context: Context, uri: android.net.Uri) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                _webDavState.value = WebDavState.Loading
                val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader(Charsets.UTF_8).readText()
                }
                if (json != null) {
                    val subs = jsonAdapter.fromJson(json)
                    if (subs != null) {
                        for (sub in subs) {
                            repository.insert(sub)
                        }
                        _webDavState.value = WebDavState.Success("Yerel geri yükleme başarılı! ${subs.size} abonelik aktarıldı.")
                    } else {
                        _webDavState.value = WebDavState.Error("Geçersiz yedekleme dosyası formatı.")
                    }
                } else {
                    _webDavState.value = WebDavState.Error("Dosya okunamadı.")
                }
            } catch (e: Exception) {
                _webDavState.value = WebDavState.Error("Yerel geri yükleme hatası: ${e.message}")
            }
        }
    }

    // Generate reporting summary for export / sharing
    fun exportReport(context: Context) {
        val subs = allSubscriptions.value
        val stats = statsState.value
        val currency = preferredCurrency.value
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale("tr"))

        val sb = StringBuilder()
        sb.append("📊 ABONELİK RAPORU - ${sdf.format(Date())}\n")
        sb.append("===============================\n\n")
        sb.append("💵 Aylık Toplam Harcama: ${String.format(Locale.US, "%.2f", stats.monthlyTotal)} $currency\n")
        sb.append("📅 Yıllık Toplam Harcama: ${String.format(Locale.US, "%.2f", stats.annualTotal)} $currency\n")
        sb.append("🔢 Aktif Abonelik Sayısı: ${subs.size}\n\n")

        sb.append("📂 Kategori Dağılımı (Aylık):\n")
        stats.categoryMonthlyBreakdown.forEach { (category, amount) ->
            val trCategory = getTurkishCategoryName(category)
            sb.append("- $trCategory: ${String.format(Locale.US, "%.2f", amount)} $currency\n")
        }
        sb.append("\n")

        sb.append("💳 Abonelik Detayları:\n")
        subs.forEachIndexed { index, sub ->
            sb.append("${index + 1}. ${sub.name}\n")
            sb.append("   Tutar: ${sub.amount} ${sub.currency} (${sub.billingCycle})\n")
            sb.append("   Kategori: ${getTurkishCategoryName(sub.category)}\n")
            sb.append("   Gelecek Ödeme: ${sdf.format(Date(sub.nextPaymentDate))}\n")
            if (sub.description.isNotEmpty()) {
                sb.append("   Açıklama: ${sub.description}\n")
            }
            sb.append("\n")
        }
        sb.append("===============================\n")
        sb.append("Abonelik Takibi Uygulaması ile oluşturulmuştur.")

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Abonelik Takip Raporu")
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        context.startActivity(Intent.createChooser(shareIntent, "Raporu Paylaş"))
    }

    fun getTurkishCategoryName(category: String): String {
        val trName = when (category) {
            "Streaming" -> "Dizi / Film"
            "Gaming" -> "Oyun"
            "Cloud" -> "Bulut Depolama"
            "Music" -> "Müzik"
            "Work" -> "İş / Üretkenlik"
            "Other" -> "Diğer"
            else -> category
        }
        return com.example.util.Localization.getString(trName)
    }
}

data class SubscriptionStats(
    val monthlyTotal: Double = 0.0,
    val annualTotal: Double = 0.0,
    val categoryMonthlyBreakdown: Map<String, Double> = emptyMap()
)

class SubscriptionViewModelFactory(
    private val repository: SubscriptionRepository,
    private val settingsManager: SettingsManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubscriptionViewModel::class.java)) {
            return SubscriptionViewModel(repository, settingsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
