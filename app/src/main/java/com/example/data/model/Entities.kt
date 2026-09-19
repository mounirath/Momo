package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val category: String, // "cleaners" (منظفات منزلية) or "cars" (العناية بالسيارات)
    val title: String,
    val percentages: String,
    val steps: String,
    val warnings: String = "",
    val videoUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "access_codes")
data class AccessCode(
    @PrimaryKey
    val code: String,
    val createdAt: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "subscriptions")
data class Subscription(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contact: String,
    val createdAt: Long = System.currentTimeMillis()
)
