package com.example.financialliteracyapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.financialliteracyapp.data.local.entity.ShopEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {
    @Query("SELECT * FROM shop WHERE id = 1")
    fun observe(): Flow<ShopEntity?>

    @Query("SELECT * FROM shop WHERE id = 1")
    suspend fun getOnce(): ShopEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(shop: ShopEntity)
}
