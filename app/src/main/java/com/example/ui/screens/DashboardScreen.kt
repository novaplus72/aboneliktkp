package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Subscription
import com.example.ui.SubscriptionViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.example.util.Localization.getString

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    viewModel: SubscriptionViewModel,
    modifier: Modifier = Modifier
) {
    val subscriptions by viewModel.allSubscriptions.collectAsState()
    val stats by viewModel.statsState.collectAsState()
    val upcoming by viewModel.upcomingPayments.collectAsState()
    val preferredCurrency by viewModel.preferredCurrency.collectAsState()

    val context = LocalContext.current
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale("tr"))

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Greeting & Avatar Row
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val greeting = when {
                hour in 5..11 -> getString("Günaydın")
                hour in 12..17 -> getString("İyi Günler")
                hour in 18..21 -> getString("İyi Akşamlar")
                else -> getString("İyi Geceler")
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "$greeting! 👋",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = getString("Abonelikleriniz kontrol altında."),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
                
            }

            // Premium Hero Stats Panel (Fintech Gradient)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1E3C72),
                                Color(0xFF2A5298),
                                Color(0xFF6B11FF)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .testTag("hero_stats_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getString("TOPLAM AYLIK GİDER").uppercase(com.example.util.Localization.getLocale()),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Premium",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = String.format(Locale.US, "%.2f %s", stats.monthlyTotal, preferredCurrency),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.testTag("monthly_total_text")
                    )

                    val monthlyBudget by viewModel.monthlyBudget.collectAsState()
                    if (monthlyBudget > 0) {
                        val percentage = (stats.monthlyTotal / monthlyBudget).toFloat().coerceIn(0f, 1f)
                        val isOverBudget = stats.monthlyTotal > monthlyBudget
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = getString("Bütçe Kullanımı: ") + "%${(percentage * 100).toInt()}",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = String.format(Locale.US, "Limit: %.0f %s", monthlyBudget, preferredCurrency),
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = percentage,
                            color = if (isOverBudget) Color(0xFFFF5252) else Color(0xFF00E676),
                            trackColor = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                        if (isOverBudget) {
                            Text(
                                text = getString("Aylık bütçenizi aştınız!"),
                                color = Color(0xFFFF8A80),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.15f))
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = getString("Yıllık Toplam Projeksiyon").uppercase(com.example.util.Localization.getLocale()),
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = String.format(Locale.US, "%.2f %s", stats.annualTotal, preferredCurrency),
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("annual_total_text")
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = getString("Aktif Abonelikler").uppercase(com.example.util.Localization.getLocale()),
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${subscriptions.size} " + getString("Abonelik"),
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("active_count_text")
                            )
                        }
                    }
                }
            }

            // Real System Trigger Scan for Payments approaching
            LaunchedEffect(upcoming) {
                if (upcoming.isNotEmpty()) {
                    viewModel.checkUpcomingPaymentsAndNotify(context)
                }
            }

            // Notification / Upcoming Banner Alert List
            AnimatedVisibility(
                visible = upcoming.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("upcoming_alert_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.error, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = getString("Yaklaşan Ödemeler Var!"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "${upcoming.size} " + getString("aboneliğinizin ödeme tarihi çok yaklaştı."),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Quick Insights Section
            Text(
                text = getString("Hızlı Analizler"),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 12.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Insight 1: Average Cost
                Card(
                    modifier = Modifier.width(135.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(getString("Ortalama Gider"), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        val avgVal = if (subscriptions.isNotEmpty()) stats.monthlyTotal / subscriptions.size else 0.0
                        Text(
                            text = String.format(Locale.US, "%.1f %s", avgVal, preferredCurrency),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Insight 2: Most Expensive
                val mostExpensive = subscriptions.maxByOrNull { it.amount }
                Card(
                    modifier = Modifier.width(135.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFFE50914),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(getString("En Yüksek Gider"), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = mostExpensive?.let { String.format(Locale.US, "%.0f %s", it.amount, it.currency) } ?: "-",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Insight 3: Next payment
                val nextUpcoming = subscriptions.filter { it.nextPaymentDate >= System.currentTimeMillis() }.minByOrNull { it.nextPaymentDate }
                Card(
                    modifier = Modifier.width(155.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = Color(0xFFFFA000),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(getString("Sıradaki Ödeme"), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = nextUpcoming?.name ?: "-",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Payment Calendar
            PaymentCalendarStrip(subscriptions = subscriptions, preferredCurrency = preferredCurrency)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PaymentCalendarStrip(
    subscriptions: List<Subscription>,
    preferredCurrency: String
) {
    val calendar = Calendar.getInstance()
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
    val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)

    val paymentsByDay = remember(subscriptions, currentMonth, currentYear) {
        val map = mutableMapOf<Int, MutableList<Subscription>>()
        subscriptions.forEach { sub ->
            val subCal = Calendar.getInstance().apply { timeInMillis = sub.nextPaymentDate }
            // Consider monthly subscriptions that might recur or just next payment date
            if (subCal.get(Calendar.MONTH) == currentMonth && subCal.get(Calendar.YEAR) == currentYear) {
                val day = subCal.get(Calendar.DAY_OF_MONTH)
                val list = map.getOrPut(day) { mutableListOf() }
                list.add(sub)
            }
        }
        map
    }

    var selectedDay by remember { mutableIntStateOf(currentDay) }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = maxOf(0, currentDay - 3))

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
        Text(
            text = getString("Ödeme Takvimi"),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(count = maxDays) { index ->
                val day = index + 1
                val hasPayment = paymentsByDay.containsKey(day)
                val isSelected = selectedDay == day

                val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else if (hasPayment) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

                Column(
                    modifier = Modifier
                        .width(48.dp)
                        .height(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .clickable { selectedDay = day }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = day.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = contentColor)
                    if (hasPayment) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val selectedPayments = paymentsByDay[selectedDay] ?: emptyList()
        AnimatedVisibility(visible = true) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = String.format(getString("%d. Günü Ödemeleri"), selectedDay),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (selectedPayments.isEmpty()) {
                        Text(
                            text = getString("Bugün herhangi bir ödemeniz bulunmuyor. Harika!"),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        selectedPayments.forEach { sub ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = sub.name,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = String.format(Locale.US, "%.2f %s", sub.amount, sub.currency),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubscriptionItemCard(
    subscription: Subscription,
    turkishCategoryName: String,
    onDelete: () -> Unit,
    sdf: SimpleDateFormat
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val icon = when (subscription.category) {
        "Streaming" -> Icons.Default.Tv
        "Gaming" -> Icons.Default.Gamepad
        "Cloud" -> Icons.Default.Cloud
        "Music" -> Icons.Default.MusicNote
        "Work" -> Icons.Default.Work
        else -> Icons.Default.Category
    }

    val iconBgColor = when (subscription.category) {
        "Streaming" -> Color(0xFFE50914).copy(alpha = 0.12f)
        "Gaming" -> Color(0xFF3F51B5).copy(alpha = 0.12f)
        "Cloud" -> Color(0xFF03A9F4).copy(alpha = 0.12f)
        "Music" -> Color(0xFF1DB954).copy(alpha = 0.12f)
        "Work" -> Color(0xFFFFA000).copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    }

    val iconColor = when (subscription.category) {
        "Streaming" -> Color(0xFFE50914)
        "Gaming" -> Color(0xFF3F51B5)
        "Cloud" -> Color(0xFF03A9F4)
        "Music" -> Color(0xFF1DB954)
        "Work" -> Color(0xFFFFA000)
        else -> MaterialTheme.colorScheme.primary
    }

    val daysLeft = ((subscription.nextPaymentDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)

    val (badgeBgColor, badgeTextColor, badgeText) = when {
        daysLeft == 0 -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            getString("Bugün Ödeme")
        )
        daysLeft == 1 -> Triple(
            Color(0xFFFF8A80),
            Color(0xFF8C0000),
            getString("Yarın")
        )
        daysLeft <= 3 -> Triple(
            Color(0xFFFFD54F).copy(alpha = 0.25f),
            Color(0xFF6F4E00),
            "$daysLeft " + getString("Gün Kaldı")
        )
        else -> Triple(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            MaterialTheme.colorScheme.onPrimaryContainer,
            "$daysLeft " + getString("Gün Kaldı")
        )
    }

    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("subscription_item_${subscription.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(iconBgColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subscription.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = turkishCategoryName,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "•",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = if (subscription.billingCycle == "Monthly") getString("Aylık") else getString("Yıllık"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format(Locale.US, "%.2f %s", subscription.amount, subscription.currency),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeBgColor)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeTextColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            // Modern progress bar indicating payment proximity
            val totalDays = if (subscription.billingCycle == "Monthly") 30f else 365f
            val elapsedProgress = ((totalDays - daysLeft).coerceIn(0f, totalDays) / totalDays)
            LinearProgressIndicator(
                progress = elapsedProgress,
                color = if (daysLeft <= 3) Color(0xFFFF5252) else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getString("Gelecek Ödeme: ") + sdf.format(Date(subscription.nextPaymentDate)),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("delete_sub_${subscription.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Sil",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(text = getString("Aboneliği Sil")) },
            text = { Text(text = "${subscription.name} " + getString("aboneliğinizi silmek istediğinizden emin misiniz?")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text(text = getString("Evet, Sil"), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false },
                    modifier = Modifier.testTag("dismiss_delete_button")
                ) {
                    Text(text = getString("İptal"))
                }
            }
        )
    }
}

@Composable
fun AddSubscriptionDialog(
    categoriesList: List<String>,
    getCategoryName: (String) -> String,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        amount: Double,
        currency: String,
        billingCycle: String,
        category: String,
        nextPaymentDate: Long,
        reminderEnabled: Boolean,
        description: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("TRY") }
    var billingCycle by remember { mutableStateOf("Monthly") }
    var category by remember { mutableStateOf(if (categoriesList.isNotEmpty()) categoriesList.first() else "Other") }
    var description by remember { mutableStateOf("") }
    var reminderEnabled by remember { mutableStateOf(true) }

    var nextPaymentDate by remember { mutableStateOf(System.currentTimeMillis()) }

    val context = LocalContext.current
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale("tr"))

    var currencyExpanded by remember { mutableStateOf(false) }
    var cycleExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getString("Yeni Abonelik Ekle"),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(getString("Abonelik Adı (örn: Netflix)")) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_sub_name")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Amount
                        OutlinedTextField(
                            value = amountStr,
                            onValueChange = { amountStr = it },
                            label = { Text(getString("Tutar")) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("input_sub_amount")
                        )

                        // Currency Selection
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { currencyExpanded = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp) // match text field height
                                    .padding(top = 8.dp)
                                    .testTag("btn_select_currency")
                            ) {
                                Text(currency)
                                Spacer(Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = currencyExpanded,
                                onDismissRequest = { currencyExpanded = false }
                            ) {
                                val currencies = listOf("TRY", "USD", "EUR", "GBP")
                                currencies.forEach { curr ->
                                    DropdownMenuItem(
                                        text = { Text(curr) },
                                        onClick = {
                                            currency = curr
                                            currencyExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Billing Cycle
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { cycleExpanded = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("btn_select_cycle")
                            ) {
                                Text(if (billingCycle == "Monthly") getString("Aylık") else getString("Yıllık"))
                                Spacer(Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = cycleExpanded,
                                onDismissRequest = { cycleExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(getString("Aylık")) },
                                    onClick = {
                                        billingCycle = "Monthly"
                                        cycleExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(getString("Yıllık")) },
                                    onClick = {
                                        billingCycle = "Yearly"
                                        cycleExpanded = false
                                    }
                                )
                            }
                        }

                        // Category Selection
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { categoryExpanded = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("btn_select_category")
                            ) {
                                Text(getCategoryName(category), overflow = TextOverflow.Ellipsis, maxLines = 1)
                                Spacer(Modifier.weight(1f))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                categoriesList.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(getCategoryName(cat)) },
                                        onClick = {
                                            category = cat
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Payment Date
                    OutlinedButton(
                        onClick = {
                            val calendar = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val selectedCal = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, year)
                                        set(Calendar.MONTH, month)
                                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                    }
                                    nextPaymentDate = selectedCal.timeInMillis
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("btn_payment_date")
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(getString("Gelecek Ödeme: ") + sdf.format(Date(nextPaymentDate)))
                        Spacer(Modifier.weight(1f))
                    }

                    // Description Notes
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(getString("Açıklama / Notlar (İsteğe bağlı)")) },
                        singleLine = false,
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_sub_desc")
                    )

                    // Reminder toggler
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(getString("Ödeme Hatırlatıcısı"), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                            Switch(
                                checked = reminderEnabled,
                                onCheckedChange = { reminderEnabled = it },
                                modifier = Modifier.testTag("switch_reminder")
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Text(getString("Vazgeç"), fontWeight = FontWeight.Bold)
                        }
                        
                        Button(
                            onClick = {
                                val amount = amountStr.toDoubleOrNull() ?: 0.0
                                if (name.isNotEmpty() && amount > 0) {
                                    onConfirm(
                                        name,
                                        amount,
                                        currency,
                                        billingCycle,
                                        category,
                                        nextPaymentDate,
                                        reminderEnabled,
                                        description
                                    )
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("btn_save_subscription")
                        ) {
                            Text(getString("Kaydet"), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
