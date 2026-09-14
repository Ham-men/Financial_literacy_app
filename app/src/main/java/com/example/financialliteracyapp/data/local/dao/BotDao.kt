package com.example.financialliteracyapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.financialliteracyapp.data.local.entity.BotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BotDao {
    @Query("SELECT * FROM bots")
    fun observeAll(): Flow<List<BotEntity>>

    @Query("SELECT * FROM bots")
    suspend fun getAllOnce(): List<BotEntity>

    @Query("SELECT COUNT(*) FROM bots")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(bots: List<BotEntity>)

    @Query("DELETE FROM bots")
    suspend fun clear()
}
