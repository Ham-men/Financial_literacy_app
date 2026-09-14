package com.example.financialliteracyapp.game

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.financialliteracyapp.data.AppContainer
import com.example.financialliteracyapp.domain.economy.Balance

/**
 * Падение шкал питомца со временем. План День 4.
 * MVP: -5 голода/настроения/энергии каждый запуск (периодика раз в час).
 */
class PetDecayWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val db = AppContainer.db(applicationContext)
        val pet = db.petDao().getOnce() ?: return Result.success()
        val d = Balance.PET_DECAY_PER_HOUR
        val updated = pet.copy(
            hunger = (pet.hunger - d).coerceAtLeast(0),
            mood = (pet.mood - d).coerceAtLeast(0),
            energy = (pet.energy - d).coerceAtLeast(0)
        )
        db.petDao().upsert(updated)
        return Result.success()
    }
}
