package com.example.financialliteracyapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.financialliteracyapp.data.local.dao.BotDao
import com.example.financialliteracyapp.data.local.dao.BuildingDao
import com.example.financialliteracyapp.data.local.dao.CatalogItemDao
import com.example.financialliteracyapp.data.local.dao.GoalDao
import com.example.financialliteracyapp.data.local.dao.PetDao
import com.example.financialliteracyapp.data.local.dao.QuestDao
import com.example.financialliteracyapp.data.local.dao.TransactionDao
import com.example.financialliteracyapp.data.local.dao.WalletDao
import com.example.financialliteracyapp.data.local.entity.BotEntity
import com.example.financialliteracyapp.data.local.entity.BuildingEntity
import com.example.financialliteracyapp.data.local.entity.CatalogItemEntity
import com.example.financialliteracyapp.data.local.entity.GoalEntity
import com.example.financialliteracyapp.data.local.entity.PetEntity
import com.example.financialliteracyapp.data.local.entity.QuestEntity
import com.example.financialliteracyapp.data.local.entity.TransactionEntity
import com.example.financialliteracyapp.data.local.entity.WalletEntity

@Database(
    entities = [PetEntity::class, WalletEntity::class, BuildingEntity::class, TransactionEntity::class, BotEntity::class, GoalEntity::class, QuestEntity::class, CatalogItemEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun walletDao(): WalletDao
    abstract fun buildingDao(): BuildingDao
    abstract fun transactionDao(): TransactionDao
    abstract fun botDao(): BotDao
    abstract fun goalDao(): GoalDao
    abstract fun questDao(): QuestDao
    abstract fun catalogItemDao(): CatalogItemDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mini-economy.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}