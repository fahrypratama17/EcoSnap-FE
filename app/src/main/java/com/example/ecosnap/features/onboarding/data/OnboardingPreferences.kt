package com.example.ecosnap.features.onboarding.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.ecosnapDataStore by preferencesDataStore(
  name = "ecosnap_preferences"
)

class OnboardingPreferences (
  private val context: Context
) {
  companion object {
    private val ONBOARDING_COMPLETED =
      booleanPreferencesKey("onboarding_completed")
  }

  val isCompleted: Flow<Boolean> =
    context.ecosnapDataStore.data.map { preferences ->
      preferences[ONBOARDING_COMPLETED] ?: false
    }

  suspend fun markCompleted() {
    context.ecosnapDataStore.edit { preferences ->
      preferences[ONBOARDING_COMPLETED] = true
    }
  }
}