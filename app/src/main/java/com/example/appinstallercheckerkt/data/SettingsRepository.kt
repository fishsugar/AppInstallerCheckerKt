package com.example.appinstallercheckerkt.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.appinstallercheckerkt.model.SortAndFilterState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
private val FILTER_OBJ = stringPreferencesKey("filterObj")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun saveFilterObj(filterObj: SortAndFilterState) {
        context.dataStore.edit { settings ->
            settings[FILTER_OBJ] = Json.Default.encodeToString(filterObj)
        }
    }

    val filterObjFlow: Flow<SortAndFilterState> = context.dataStore.data
        .mapNotNull { preferences -> preferences[FILTER_OBJ] }
        .map { Json.Default.decodeFromString<SortAndFilterState>(it) }
}