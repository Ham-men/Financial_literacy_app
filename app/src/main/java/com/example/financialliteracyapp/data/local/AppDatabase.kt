package com.example.financialliteracyapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 6,
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
                ).addMigrations(MIGRATION_4_5, MIGRATION_5_6).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }

        /** v5: купленные магазины на карте — новая колонка plotId у зданий. */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE buildings ADD COLUMN plotId TEXT NOT NULL DEFAULT ''")
            }
        }

        /** v6: опыт развлечений — pet.xp + описание/опыт у игрушек. */
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE pet ADD COLUMN xp INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE catalog_items ADD COLUMN xpReward INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE catalog_items ADD COLUMN description TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE catalog_items SET xpReward = 3, description = 'Гоняй по полу и лови лапкой' WHERE title = 'Мячик'")
                db.execSQL("UPDATE catalog_items SET xpReward = 2, description = 'Яркий бантик для красивых фото' WHERE title = 'Бантик'")
                db.execSQL("UPDATE catalog_items SET xpReward = 4, description = 'Картина, чтобы любоваться на рыб' WHERE title = 'Картина на стену'")
                db.execSQL("UPDATE catalog_items SET xpReward = 5, description = 'Торт на праздничный день' WHERE title = 'Праздничный торт'")
            }
        }
    }
}