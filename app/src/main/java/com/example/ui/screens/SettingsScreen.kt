package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import com.example.util.Localization.getString
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.animation.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.material.icons.filled.Sync
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.SubscriptionViewModel

@Composable
fun SettingsScreen(
    viewModel: SubscriptionViewModel,
    modifier: Modifier = Modifier
) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val warningDays by viewModel.warningDays.collectAsState()
    val preferredCurrency by viewModel.preferredCurrency.collectAsState()

    val webDavState by viewModel.webDavState.collectAsState()
    val webDavUrl by viewModel.webDavUrl.collectAsState()
    val webDavUsername by viewModel.webDavUsername.collectAsState()
    val webDavPassword by viewModel.webDavPassword.collectAsState()
    val webDavPath by viewModel.webDavPath.collectAsState()
    val webDavLastSync by viewModel.webDavLastSync.collectAsState()
    val webDavLastStatus by viewModel.webDavLastStatus.collectAsState()
    val webDavAutoSyncOnLaunch by viewModel.webDavAutoSyncOnLaunch.collectAsState()
    val webDavAutoSyncOnChange by viewModel.webDavAutoSyncOnChange.collectAsState()
    val webDavConflictStrategy by viewModel.webDavConflictStrategy.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showWarningDaysDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showWebDavEditDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCategoriesDialog by remember { mutableStateOf(false) }
    val currentLang = viewModel.language.collectAsState().value

    var isGeneralExpanded by remember { mutableStateOf(true) }
    var isNotificationsExpanded by remember { mutableStateOf(false) }
    var isSyncExpanded by remember { mutableStateOf(false) }
    var isReportingExpanded by remember { mutableStateOf(false) }
    var isDangerExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleNotifications(true)
            Toast.makeText(context, getString("Bildirim izni verildi! Hatırlatıcılar aktif."), Toast.LENGTH_SHORT).show()
        } else {
            viewModel.toggleNotifications(false)
            Toast.makeText(context, getString("Bildirim izni reddedildi. Ödeme uyarıları gönderilemeyecek."), Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = getString("Ayarlar"),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Group 1: General & Appearance
        SettingsGroupCard(
            title = getString("Arayüz ve Genel Ayarlar"),
            icon = Icons.Default.Palette,
            isExpanded = isGeneralExpanded,
            onHeaderClick = { isGeneralExpanded = !isGeneralExpanded },
            testTag = "group_general"
        ) {
            // Dark Theme Toggler
            SettingsToggleRow(
                icon = Icons.Default.DarkMode,
                title = getString("Koyu Tema"),
                subtitle = getString("Koyu renk şemasını aktif et"),
                checked = isDarkTheme,
                onCheckedChange = { viewModel.toggleDarkTheme(it) },
                testTag = "switch_dark_theme"
            )

            // Currency Selector Row
            SettingsSelectableRow(
                icon = Icons.Default.Payments,
                title = getString("Varsayılan Para Birimi"),
                value = preferredCurrency,
                onClick = { showCurrencyDialog = true },
                testTag = "row_currency_selector"
            )

            // Monthly Budget Selector Row
            SettingsSelectableRow(
                icon = Icons.Default.AccountBalanceWallet,
                title = getString("Aylık Harcama Limiti"),
                value = if (monthlyBudget > 0) String.format(java.util.Locale.US, "%.0f %s", monthlyBudget, preferredCurrency) else getString("Limit Yok"),
                onClick = { showBudgetDialog = true },
                testTag = "row_budget_selector"
            )

            val langDisplay = when (currentLang) {
                "tr" -> "Türkçe"
                "en" -> "English"
                "de" -> "Deutsch"
                else -> getString("Cihaz Dili")
            }

            // Language Selector Row
            SettingsSelectableRow(
                icon = Icons.Default.Language,
                title = getString("Dil"),
                value = langDisplay,
                onClick = { showLanguageDialog = true },
                testTag = "row_language_selector"
            )

            // Categories Row
            SettingsSelectableRow(
                icon = Icons.Default.Category,
                title = getString("Kategoriler"),
                value = "${categories.size}",
                onClick = { showCategoriesDialog = true },
                testTag = "row_categories_selector"
            )
        }

        // Group 2: Notifications & Reminders
        SettingsGroupCard(
            title = getString("Bildirim ve Hatırlatıcılar"),
            icon = Icons.Default.Notifications,
            isExpanded = isNotificationsExpanded,
            onHeaderClick = { isNotificationsExpanded = !isNotificationsExpanded },
            testTag = "group_notifications"
        ) {
            // Notifications Master Toggler with permissions request flow
            SettingsToggleRow(
                icon = Icons.Default.Notifications,
                title = getString("Hatırlatıcı Bildirimler"),
                subtitle = getString("Ödeme günleri yaklaştığında uyar"),
                checked = notificationsEnabled,
                onCheckedChange = { checked ->
                    if (checked) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                            
                            if (!hasPermission) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.toggleNotifications(true)
                            }
                        } else {
                            viewModel.toggleNotifications(true)
                        }
                    } else {
                        viewModel.toggleNotifications(false)
                    }
                },
                testTag = "switch_notifications_master"
            )

            // Days Threshold Selector
            SettingsSelectableRow(
                icon = Icons.Default.Alarm,
                title = getString("Kaç Gün Önce Uyarılsın?"),
                value = "$warningDays Gün",
                onClick = { showWarningDaysDialog = true },
                testTag = "row_warning_days"
            )

            // Immediate Notification Simulator Trigger button
            SettingsActionRow(
                icon = Icons.Default.NotificationsActive,
                title = getString("Bildirim Sistemini Test Et"),
                subtitle = getString("Örnek bir hatırlatıcı gönder"),
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                        
                        if (!hasPermission) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.triggerTestNotification(context)
                        }
                    } else {
                        viewModel.triggerTestNotification(context)
                    }
                },
                testTag = "btn_simulate_notification"
            )
        }

        // Group 3: WebDAV Synchronization & Cloud
        SettingsGroupCard(
            title = getString("Bulut ve Yerel Senkronizasyon"),
            icon = Icons.Default.Cloud,
            isExpanded = isSyncExpanded,
            onHeaderClick = { isSyncExpanded = !isSyncExpanded },
            testTag = "group_synchronization"
        ) {
            val createDocumentLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.CreateDocument("application/json")
            ) { uri ->
                uri?.let { viewModel.exportBackupToFile(context, it) }
            }

            val openDocumentLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument()
            ) { uri ->
                uri?.let { viewModel.importBackupFromFile(context, it) }
            }

            SettingsActionRow(
                icon = Icons.Default.Save,
                title = getString("Cihaza Yedekle (Yerel)"),
                subtitle = getString("Abonelikleri telefonunuza JSON olarak kaydedin"),
                onClick = { createDocumentLauncher.launch("abonelik_yedek.json") },
                testTag = "btn_local_backup"
            )

            SettingsActionRow(
                icon = Icons.Default.FolderOpen,
                title = getString("Cihazdan Geri Yükle (Yerel)"),
                subtitle = getString("Kayıtlı JSON yedeğini telefondan yükleyin"),
                onClick = { openDocumentLauncher.launch(arrayOf("application/json", "*/*")) },
                testTag = "btn_local_restore"
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getString("WebDAV (Bulut) Yapılandırması"),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(
                    onClick = { showWebDavEditDialog = true },
                    modifier = Modifier.size(24.dp).testTag("btn_edit_webdav_config")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Düzenle",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(getString("Sunucu:"), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (webDavUrl.isBlank()) getString("Ayarlanmadı") else webDavUrl,
                                fontSize = 11.sp,
                                color = if (webDavUrl.isBlank()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(getString("Dosya Yolu:"), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(webDavPath, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(getString("Son Senkronizasyon:"), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(webDavLastSync, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(getString("Senkronizasyon Durumu:"), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val statusColor = when {
                                webDavLastStatus.contains("Başarılı") || webDavLastStatus.contains("Eşitleme") -> MaterialTheme.colorScheme.primary
                                webDavLastStatus.contains("Başarısız") || webDavLastStatus.contains("Hata") -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                            Text(webDavLastStatus, fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.Bold)
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

                    Text(
                        text = getString("Otomatik Eşitleme Seçenekleri"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(getString("Açılışta Eşitle"), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(getString("Uygulama açılırken buluttan veriyi günceller"), fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = webDavAutoSyncOnLaunch,
                            onCheckedChange = { viewModel.saveWebDavCredentials(webDavUrl, webDavUsername, webDavPassword, webDavPath, it, webDavAutoSyncOnChange, webDavConflictStrategy) },
                            modifier = Modifier.scale(0.75f).testTag("switch_webdav_auto_sync_on_launch")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(getString("Değişiklikte Yedekle"), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(getString("Abonelik ekleme/silme anında buluta yedekler"), fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = webDavAutoSyncOnChange,
                            onCheckedChange = { viewModel.saveWebDavCredentials(webDavUrl, webDavUsername, webDavPassword, webDavPath, webDavAutoSyncOnLaunch, it, webDavConflictStrategy) },
                            modifier = Modifier.scale(0.75f).testTag("switch_webdav_auto_sync_on_change")
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

                    Text(
                        text = getString("Çatışma Çözüm Yöntemi"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        listOf(
                            "merge" to getString("Verileri Birleştir (Tavsiye Edilen)"),
                            "local" to getString("Telefonun Verilerini Kullan (Bulutu ez)"),
                            "cloud" to getString("Bulutun Verilerini Kullan (Telefonu ez)")
                        ).forEach { (stratKey, stratLabel) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.saveWebDavCredentials(
                                            webDavUrl, webDavUsername, webDavPassword, webDavPath,
                                            webDavAutoSyncOnLaunch, webDavAutoSyncOnChange, stratKey
                                        )
                                    }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (webDavConflictStrategy == stratKey),
                                    onClick = {
                                        viewModel.saveWebDavCredentials(
                                            webDavUrl, webDavUsername, webDavPassword, webDavPath,
                                            webDavAutoSyncOnLaunch, webDavAutoSyncOnChange, stratKey
                                        )
                                    },
                                    modifier = Modifier.size(28.dp).testTag("radio_conflict_$stratKey")
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stratLabel, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Button(
                        onClick = { viewModel.syncWithWebDav() },
                        enabled = webDavUrl.isNotBlank() && webDavState !is com.example.ui.WebDavState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("btn_webdav_sync_now"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(getString("Şimdi Eşitle"), fontSize = 12.sp)
                    }
                }
            }

            when (val state = webDavState) {
                is com.example.ui.WebDavState.Loading -> {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 1.5.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(getString("Eşitleme yapılıyor, lütfen bekleyin..."), fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
                is com.example.ui.WebDavState.Success -> {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = state.message,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(horizontal = 12.dp)
                    )
                }
                is com.example.ui.WebDavState.Error -> {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = state.errorMessage,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(horizontal = 12.dp)
                    )
                }
                com.example.ui.WebDavState.Idle -> {}
            }
        }

        // Group 4: Export & Share
        SettingsGroupCard(
            title = getString("Raporlama ve Paylaşım"),
            icon = Icons.Default.Share,
            isExpanded = isReportingExpanded,
            onHeaderClick = { isReportingExpanded = !isReportingExpanded },
            testTag = "group_reporting"
        ) {
            SettingsActionRow(
                icon = Icons.Default.Share,
                title = getString("Raporu Dışa Aktar"),
                subtitle = getString("Abonelik verilerini paylaş"),
                onClick = { viewModel.exportReport(context) },
                testTag = "btn_export_report"
            )
        }

        // Group 5: Danger Zone
        SettingsGroupCard(
            title = getString("Tehlikeli Bölge"),
            icon = Icons.Default.DeleteForever,
            isExpanded = isDangerExpanded,
            onHeaderClick = { isDangerExpanded = !isDangerExpanded },
            testTag = "group_danger"
        ) {
            SettingsActionRow(
                icon = Icons.Default.DeleteForever,
                title = getString("Tüm Verileri Temizle"),
                subtitle = getString("Abonelikleri kalıcı olarak sil"),
                onClick = { showResetConfirmDialog = true },
                testTag = "btn_clear_database",
                isDestructive = true
            )
        }

        var showAboutDialog by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
        ) {
            SettingsActionRow(
                icon = Icons.Default.Info,
                title = getString("Hakkında"),
                subtitle = getString("Versiyon, Lisans ve Bilgiler"),
                onClick = { showAboutDialog = true },
                testTag = "btn_show_about"
            )
        }

        if (showAboutDialog) {
            AboutDialog(onDismiss = { showAboutDialog = false })
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Budget Settings Dialog
    if (showBudgetDialog) {
        var budgetInput by remember { mutableStateOf(if (monthlyBudget > 0) monthlyBudget.toString() else "") }
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = { Text(getString("Aylık Harcama Limiti")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(getString("Aylık olarak aşmak istemediğiniz toplam abonelik bütçenizi girin. İptal etmek için boş bırakın veya 0 yazın."), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { budgetInput = it },
                        label = { Text("Tutar ($preferredCurrency)") },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val newBudget = budgetInput.toDoubleOrNull() ?: 0.0
                    viewModel.setMonthlyBudget(newBudget)
                    showBudgetDialog = false
                }) {
                    Text(getString("Kaydet"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) {
                    Text(getString("İptal"))
                }
            }
        )
    }

    // Language Picker Dialog
    if (showLanguageDialog) {
        val languages = listOf("device" to com.example.util.Localization.getString("Cihaz Dili"), "tr" to "Türkçe", "en" to "English", "de" to "Deutsch")
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(com.example.util.Localization.getString("Dil")) },
            text = {
                Column {
                    languages.forEach { (code, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setLanguage(code)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentLang == code,
                                onClick = {
                                    viewModel.setLanguage(code)
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = name, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(com.example.util.Localization.getString("İptal"))
                }
            }
        )
    }

    if (showCategoriesDialog) {
        var newCategoryName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCategoriesDialog = false },
            title = { Text(getString("Kategori Yönetimi")) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
                ) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text(getString("Kategori Adı")) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                if (newCategoryName.isNotBlank()) {
                                    viewModel.addCategory(newCategoryName.trim())
                                    newCategoryName = ""
                                }
                            }) {
                                Icon(Icons.Default.Add, contentDescription = getString("Kategori Ekle"))
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories.toList()) { cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = viewModel.getTurkishCategoryName(cat), fontSize = 14.sp)
                                IconButton(onClick = {
                                    viewModel.removeCategory(cat)
                                }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = getString("Sil"), tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoriesDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // Currency Picker Selection Dialog
    if (showCurrencyDialog) {
        val currencies = listOf("TRY", "USD", "EUR", "GBP")
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text(getString("Varsayılan Para Birimi Seçin")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    currencies.forEach { curr ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setPreferredCurrency(curr)
                                    showCurrencyDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(curr, fontWeight = FontWeight.Medium)
                            if (curr == preferredCurrency) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) {
                    Text(getString("Kapat"))
                }
            }
        )
    }

    // Warning Threshold Dialog
    if (showWarningDaysDialog) {
        val daysOptions = listOf(1, 2, 3, 5, 7)
        AlertDialog(
            onDismissRequest = { showWarningDaysDialog = false },
            title = { Text(getString("Bildirim Eşiğini Belirleyin")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    daysOptions.forEach { days ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setWarningDays(days)
                                    showWarningDaysDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("$days Gün Önce", fontWeight = FontWeight.Medium)
                            if (days == warningDays) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWarningDaysDialog = false }) {
                    Text(getString("Kapat"))
                }
            }
        )
    }

    // Clear DB Reset Alert
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text(getString("Sıfırlama İşlemini Onayla")) },
            text = { Text("Tüm abonelikleriniz silinecektir. Devam etmek istediğinizden emin misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllData()
                        showResetConfirmDialog = false
                    },
                    modifier = Modifier.testTag("confirm_reset_db_button")
                ) {
                    Text(getString("Sıfırla"), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text(getString("İptal"))
                }
            }
        )
    }

    // WebDAV Credentials Config Dialog
    if (showWebDavEditDialog) {
        var urlInput by remember { mutableStateOf(webDavUrl) }
        var usernameInput by remember { mutableStateOf(webDavUsername) }
        var passwordInput by remember { mutableStateOf(webDavPassword) }
        var pathInput by remember { mutableStateOf(webDavPath) }

        AlertDialog(
            onDismissRequest = {
                viewModel.resetWebDavState()
                showWebDavEditDialog = false
            },
            title = { Text(getString("WebDAV Sunucu Ayarları")) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { Text("Sunucu URL (örn: https://dav.box.com/dav)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_webdav_url"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { usernameInput = it },
                        label = { Text(getString("Kullanıcı Adı")) },
                        modifier = Modifier.fillMaxWidth().testTag("input_webdav_username"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text(getString("Şifre / Uygulama Şifresi")) },
                        modifier = Modifier.fillMaxWidth().testTag("input_webdav_password"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = pathInput,
                        onValueChange = { pathInput = it },
                        label = { Text(getString("Dosya Yolu")) },
                        placeholder = { Text("subscriptions_backup.json") },
                        modifier = Modifier.fillMaxWidth().testTag("input_webdav_path"),
                        singleLine = true
                    )

                    // Inline connection test button and state display inside the Dialog
                    Button(
                        onClick = { viewModel.testWebDavConnection(urlInput, usernameInput, passwordInput, pathInput) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("btn_test_webdav_connection")
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(getString("Bağlantıyı Test Et"))
                    }

                    when (val state = webDavState) {
                        is com.example.ui.WebDavState.Loading -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(getString("Bağlanıyor..."), fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        is com.example.ui.WebDavState.Success -> {
                            Text(
                                text = state.message,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                        is com.example.ui.WebDavState.Error -> {
                            Text(
                                text = state.errorMessage,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                        com.example.ui.WebDavState.Idle -> {}
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.saveWebDavCredentials(
                            urlInput, usernameInput, passwordInput, pathInput,
                            webDavAutoSyncOnLaunch, webDavAutoSyncOnChange, webDavConflictStrategy
                        )
                        viewModel.resetWebDavState()
                        showWebDavEditDialog = false
                    },
                    modifier = Modifier.testTag("btn_save_webdav_credentials")
                ) {
                    Text(getString("Kaydet"))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.resetWebDavState()
                        showWebDavEditDialog = false
                    }
                ) {
                    Text(getString("İptal"))
                }
            }
        )
    }
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        val bgColor = if (isDestructive) MaterialTheme.colorScheme.error.copy(alpha = 0.1f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)
        
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(bgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isDestructive) color else MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, fontSize = 11.sp, color = color.copy(alpha = 0.7f))
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = color.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun SettingsSelectableRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = getString("Değiştirmek için tıklayın"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(text = value, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SettingsGroupCard(
    title: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onHeaderClick: () -> Unit,
    testTag: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHeaderClick() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Daralt" else "Genişlet",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 4.dp))
                    content()
                }
            }
        }
    }
}
