package com.example.data

import kotlinx.coroutines.flow.Flow

class SubscriptionRepository(private val subscriptionDao: SubscriptionDao) {
    val allSubscriptions: Flow<List<Subscription>> = subscriptionDao.getAllSubscriptions()

    suspend fun insert(subscription: Subscription) {
        subscriptionDao.insertSubscription(subscription)
    }

    suspend fun deleteById(id: Int) {
        subscriptionDao.deleteSubscriptionById(id)
    }

    suspend fun deleteAll() {
        subscriptionDao.deleteAllSubscriptions()
    }
}
