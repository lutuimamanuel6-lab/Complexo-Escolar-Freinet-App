package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val studentName: String,
    val gradeClass: String = "11º Ano - Artes e Ciências"
)

@Entity(tableName = "school_orders")
data class SchoolOrder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentName: String,
    val orderType: String, // "Cantina" or "Papelaria" (Store)
    val itemDetails: String, // e.g., "Bolinho de Chouriço x1, Bola de Berlim x2"
    val totalAmount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Pendente" // "Pendente", "Em Preparação", "Pronto para Levantamento", "Concluído"
)

@Entity(tableName = "subject_grades")
data class SubjectGrade(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectName: String,
    val score: Double,
    val maxScore: Double = 20.0,
    val coeff: Int = 1,
    val term: String // "1º Trimestre", "2º Trimestre", "3º Trimestre"
)

@Entity(tableName = "canteen_items")
data class CanteenFoodItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
    val category: String, // "Refeições" / "Sobremesas" / "Bebidas" / "Guloseimas / Outros"
    val emoji: String
)

@Entity(tableName = "store_items")
data class StoreProductItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
    val category: String, // "Artigos" / "Vestuário" / "Material"
    val emoji: String
)
