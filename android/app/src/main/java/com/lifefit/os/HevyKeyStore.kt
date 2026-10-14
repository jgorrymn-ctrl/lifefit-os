package com.lifefit.os

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class HevyKeyStore(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val preferences = EncryptedSharedPreferences.create(
        context,
        "lifefit_hevy_secure",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveApiKey(apiKey: String) {
        preferences.edit()
            .putString("hevy_api_key", apiKey.trim())
            .apply()
    }

    fun getApiKey(): String? {
        return preferences.getString("hevy_api_key", null)
    }

    fun clearApiKey() {
        preferences.edit()
            .remove("hevy_api_key")
            .apply()
    }
}