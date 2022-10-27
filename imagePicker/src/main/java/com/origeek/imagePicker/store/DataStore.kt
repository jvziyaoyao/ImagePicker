package com.origeek.imagePicker.util

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * @program: ImagePicker
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2022-10-26 10:55
 **/

const val PREFERENCE_DATA_STORE_KEY = "IMAGE_PICKER_SAMPLE_DATA_STORE"

val Context.dataStore by preferencesDataStore(name = PREFERENCE_DATA_STORE_KEY)

fun <T> Context.getStoreData(key: Preferences.Key<T>): Flow<T?> {
    return dataStore.data.map { preferences -> preferences[key] }
}

suspend fun <T> Context.setStoreData(key: Preferences.Key<T>, data: T) {
    dataStore.edit { edit ->
        edit[key] = data
    }
}
