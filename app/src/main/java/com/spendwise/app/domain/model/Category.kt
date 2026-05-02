package com.spendwise.app.domain.model

data class Category(
    val id: Long = 0,
    val name: String,
    val icon: String,
    val colorHex: String,
    val sortOrder: Int = 0
)
