package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CreditCard
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.SettingsManager
import com.example.data.SubscriptionRepository
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.SubscriptionViewModel
import com.example.ui.SubscriptionViewModelFactory
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SubscriptionsScreen
import com.example.util.NotificationHelper

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // Optionally handle permission outcome
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Notification Channels
        NotificationHelper.createNotificationChannel(this)

        // Request Notification Permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Initialize Database, Settings & Repository
        val database = AppDatabase.getDatabase(this)
        val repository = SubscriptionRepository(database.subscriptionDao())
        val settingsManager = SettingsManager(this)

        // Set up custom Factory and ViewModel
        val factory = SubscriptionViewModelFactory(repository, settingsManager)
        val viewModel = ViewModelProvider(this, factory)[SubscriptionViewModel::class.java]

        enableEdgeToEdge()

        setContent {
            val isDarkPref by viewModel.isDarkTheme.collectAsState()
            val languagePref by viewModel.language.collectAsState()
            val isFirstLaunch by viewModel.isFirstLaunch.collectAsState()
            
            var selectedTab by remember { mutableIntStateOf(0) }

            // Sync language
            com.example.util.Localization.setLanguage(languagePref)

            MyApplicationTheme(darkTheme = isDarkPref) {
                if (isFirstLaunch) {
                    com.example.ui.screens.OnboardingScreen(
                        viewModel = viewModel,
                        onComplete = { viewModel.setFirstLaunchCompleted() }
                    )
                } else {

                        LaunchedEffect(Unit) {
                            viewModel.syncWithWebDavOnLaunch()
                        }

                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            contentWindowInsets = WindowInsets(0, 0, 0, 0)
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                // Page Content Area with bottom padding to avoid overlapping the floating dock
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = 88.dp)
                                ) {
                                    when (selectedTab) {
                                        0 -> DashboardScreen(
                                            viewModel = viewModel,
                                            modifier = Modifier.statusBarsPadding()
                                        )
                                        1 -> SubscriptionsScreen(
                                            viewModel = viewModel,
                                            modifier = Modifier.statusBarsPadding()
                                        )
                                        2 -> AnalyticsScreen(
                                            viewModel = viewModel,
                                            modifier = Modifier.statusBarsPadding()
                                        )
                                        3 -> SettingsScreen(
                                            viewModel = viewModel,
                                            modifier = Modifier.statusBarsPadding()
                                        )
                                    }
                                }

                                // Sleek Glassmorphic Floating Navigation Dock
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .windowInsetsPadding(WindowInsets.navigationBars)
                                        .padding(bottom = 16.dp, start = 24.dp, end = 24.dp)
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                                        shape = RoundedCornerShape(24.dp),
                                        shadowElevation = 12.dp,
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(66.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val items = listOf(
                                                Triple(0, Icons.Default.Payments, com.example.util.Localization.getString("Panel")),
                                                Triple(1, Icons.Default.CreditCard, com.example.util.Localization.getString("Abonelikler")),
                                                Triple(2, Icons.Default.Analytics, com.example.util.Localization.getString("Analiz")),
                                                Triple(3, Icons.Default.Settings, com.example.util.Localization.getString("Ayarlar"))
                                            )

                                            items.forEach { (index, icon, label) ->
                                                val isSelected = selectedTab == index
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .clickable { selectedTab = index }
                                                        .padding(vertical = 8.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = icon,
                                                            contentDescription = label,
                                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                            modifier = Modifier.size(22.dp)
                                                        )
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = label,
                                                            fontSize = 10.sp,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
