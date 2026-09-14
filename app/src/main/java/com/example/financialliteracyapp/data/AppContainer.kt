package com.example.financialliteracyapp.data

import android.content.Context
import com.example.financialliteracyapp.data.local.AppDatabase
import com.example.financialliteracyapp.data.prefs.UserPrefs

/** Простой Service Locator без Hilt (MVP). */
object AppContainer {
    @Volatile private var db: AppDatabase? = null
    @Volatile private var repo: GameRepository? = null
    @Volatile private var prefs: UserPrefs? = null

    fun db(context: Context): AppDatabase =
        db ?: synchronized(this) {
            db ?: AppDatabase.get(context).also { db = it }
        }

    fun repo(context: Context): GameRepository =
        repo ?: synchronized(this) {
            repo ?: GameRepository(db(context)).also { repo = it }
        }

    fun prefs(context: Context): UserPrefs =
        prefs ?: synchronized(this) {
            prefs ?: UserPrefs(context.applicationContext).also { prefs = it }
        }
}
