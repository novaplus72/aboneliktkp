package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class Subscription(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: Double,
    val currency: String = "TRY", // TRY, USD, EUR, GBP
    val billingCycle: String = "Monthly", // Monthly, Yearly
    val category: String = "Other", // Streaming, Gaming, Cloud, Music, Work, Other
    val nextPaymentDate: Long = System.currentTimeMillis(),
    val reminderEnabled: Boolean = true,
    val description: String = ""
)
