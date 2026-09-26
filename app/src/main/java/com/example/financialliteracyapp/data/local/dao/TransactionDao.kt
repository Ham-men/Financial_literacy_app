package com.example.financialliteracyapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.financialliteracyapp.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM `transactions` ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM `transactions` WHERE day = :day ORDER BY timestamp DESC")
    fun observeByDay(day: Int): Flow<List<TransactionEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM `transactions` WHERE kind = 'INCOME' AND day = :day")
    suspend fun incomeOfDay(day: Int): Int

    @Query("SELECT COALESCE(SUM(amount), 0) FROM `transactions` WHERE kind = 'EXPENSE' AND day = :day")
    suspend fun expenseOfDay(day: Int): Int

    @Query("SELECT DISTINCT category FROM `transactions` WHERE day = :day")
    suspend fun categoriesOfDay(day: Int): List<String>

    @Insert
    suspend fun insert(tx: TransactionEntity): Long
}
