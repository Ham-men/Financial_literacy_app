package com.example.financialliteracyapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.financialliteracyapp.data.local.entity.QuestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestDao {
    @Query("SELECT * FROM quests ORDER BY `order` ASC")
    fun observeAll(): Flow<List<QuestEntity>>

    @Query("SELECT * FROM quests WHERE id = :id")
    suspend fun getById(id: Int): QuestEntity?

    @Query("SELECT * FROM quests WHERE topic = :topic ORDER BY `order` ASC")
    fun observeByTopic(topic: String): Flow<List<QuestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(quest: QuestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(quests: List<QuestEntity>)

    @Query("DELETE FROM quests")
    suspend fun clear()
}