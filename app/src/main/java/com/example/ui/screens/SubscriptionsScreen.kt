package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Subscription
import com.example.ui.SubscriptionViewModel
import com.example.util.Localization.getString
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SubscriptionsScreen(
    viewModel: SubscriptionViewModel,
    modifier: Modifier = Modifier
) {
    val subscriptions by viewModel.allSubscriptions.collectAsState()
    val stats by viewModel.statsState.collectAsState()
    val preferredCurrency by viewModel.preferredCurrency.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var sortBy by remember { mutableStateOf("NextPayment") } // NextPayment, Amount, Name
    var sortMenuExpanded by remember { mutableStateOf(false) }

    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale("tr"))

    // Filtered and sorted subscriptions list
    val processedSubscriptions = remember(subscriptions, searchQuery, selectedCategoryFilter, sortBy) {
        var list = subscriptions

        // 1. Category Filter
        if (selectedCategoryFilter != "All") {
            list = list.filter { it.category == selectedCategoryFilter }
        }

        // 2. Search Query Filter
        if (searchQuery.isNotBlank()) {
            list = list.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }

        // 3. Sorting
        when (sortBy) {
            "Amount" -> list.sortedByDescending { it.amount }
            "Name" -> list.sortedBy { it.name.lowercase(Locale.ROOT) }
            else -> list.sortedBy { it.nextPaymentDate }
        }
    }

    // Dynamic cost of filtered subscriptions
    val filteredTotalCost = remember(processedSubscriptions) {
        processedSubscriptions.sumOf { it.amount }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .testTag("add_subscription_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = getString("Abonelik Ekle"))
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Elegant Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = getString("Aboneliklerim"),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = getString("Kayıtlı tüm ödemeleriniz ve detayları"),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }

            // High-fidelity Category Summary Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (selectedCategoryFilter == "All") getString("TÜM ABONELİKLER") else viewModel.getTurkishCategoryName(selectedCategoryFilter).uppercase(com.example.util.Localization.getLocale()),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format(Locale.US, "%.2f %s", filteredTotalCost, preferredCurrency),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${processedSubscriptions.size} " + getString("Aktif"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Search Bar & Sorting Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sleek Search TextField
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { 
                        Text(
                            text = getString("Abonelik ara..."), 
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        ) 
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("subscription_search_input")
                )

                // Sort Action Menu Button
                Box {
                    IconButton(
                        onClick = { sortMenuExpanded = true },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = getString("Sırala"),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(getString("Sıradaki Ödeme")) },
                            leadingIcon = { Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                sortBy = "NextPayment"
                                sortMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(getString("Tutar (En Yüksek)")) },
                            leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                sortBy = "Amount"
                                sortMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(getString("İsim")) },
                            leadingIcon = { Icon(Icons.Default.SortByAlpha, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                sortBy = "Name"
                                sortMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Category Tab Filter Row
            val dynamicCategories = viewModel.categories.collectAsState().value.toList()
            val filters = listOf("All") + dynamicCategories
            ScrollableTabRow(
                selectedTabIndex = filters.indexOf(selectedCategoryFilter).coerceAtLeast(0),
                edgePadding = 0.dp,
                divider = {},
                indicator = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("subscriptions_category_filter_row")
            ) {
                filters.forEach { filter ->
                    val isSelected = selectedCategoryFilter == filter
                    val filterIcon = when (filter) {
                        "Streaming" -> Icons.Default.Tv
                        "Gaming" -> Icons.Default.Gamepad
                        "Cloud" -> Icons.Default.Cloud
                        "Music" -> Icons.Default.MusicNote
                        "Work" -> Icons.Default.Work
                        "Other" -> Icons.Default.Category
                        "All" -> Icons.Default.Apps
                        else -> Icons.Default.Label
                    }

                    Tab(
                        selected = isSelected,
                        onClick = { selectedCategoryFilter = filter },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = filterIcon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (filter == "All") getString("Hepsi") else viewModel.getTurkishCategoryName(filter),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subscriptions List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("subscriptions_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (processedSubscriptions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 64.dp)
                                .testTag("empty_state_box"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = getString("Abonelik Bulunamadı"),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty()) getString("Arama kriterlerinize uyan abonelik bulunmamaktadır.") else getString("Sağ alttaki + butonuna tıklayarak yeni abonelik ekleyin."),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                            }
                        }
                    }
                } else {
                    items(processedSubscriptions, key = { it.id }) { sub ->
                        SubscriptionItemCard(
                            subscription = sub,
                            turkishCategoryName = viewModel.getTurkishCategoryName(sub.category),
                            onDelete = { viewModel.deleteSubscription(sub.id) },
                            sdf = sdf
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddSubscriptionDialog(
            categoriesList = viewModel.categories.collectAsState().value.toList(),
            getCategoryName = { viewModel.getTurkishCategoryName(it) },
            onDismiss = { showAddDialog = false },
            onConfirm = { name, amount, curr, cycle, cat, date, remind, desc ->
                viewModel.addSubscription(name, amount, curr, cycle, cat, date, remind, desc)
                showAddDialog = false
            }
        )
    }
}
