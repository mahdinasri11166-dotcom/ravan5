package com.therapist.assistant.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("therapist_settings")

class SettingsRepository(private val context: Context) {
    private val remindersKey = booleanPreferencesKey("reminders_enabled")
    private val dayBeforeKey = booleanPreferencesKey("remind_day_before")
    private val hourBeforeKey = booleanPreferencesKey("remind_hour_before")
    private val morningKey = booleanPreferencesKey("remind_morning")
    private val onboardingKey = booleanPreferencesKey("onboarding_done")
    private val pinHashKey = stringPreferencesKey("pin_hash")
    private val clinicNameKey = stringPreferencesKey("clinic_name")
    private val clinicPhoneKey = stringPreferencesKey("clinic_phone")
    private val clinicAddressKey = stringPreferencesKey("clinic_address")
    private val msgTemplateKey = stringPreferencesKey("msg_template")
    private val themeHueKey = floatPreferencesKey("theme_hue")
    private val themeIdKey = stringPreferencesKey("theme_id")
    private val isProKey = booleanPreferencesKey("is_pro")
    private val privacyKey = booleanPreferencesKey("privacy_accepted")
    private val biometricKey = booleanPreferencesKey("biometric_enabled")
    private val trialStartKey = longPreferencesKey("trial_start_millis")

    val remindersEnabled: Flow<Boolean> = context.dataStore.data.map { it[remindersKey] ?: true }
    val remindDayBefore: Flow<Boolean> = context.dataStore.data.map { it[dayBeforeKey] ?: true }
    val remindHourBefore: Flow<Boolean> = context.dataStore.data.map { it[hourBeforeKey] ?: true }
    val remindMorning: Flow<Boolean> = context.dataStore.data.map { it[morningKey] ?: false }
    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { it[onboardingKey] ?: false }
    val pinHash: Flow<String?> = context.dataStore.data.map { it[pinHashKey] }
    val clinicName: Flow<String> = context.dataStore.data.map { it[clinicNameKey] ?: "مرکز گل سرخ" }
    val clinicPhone: Flow<String> = context.dataStore.data.map { it[clinicPhoneKey] ?: "" }
    val clinicAddress: Flow<String> = context.dataStore.data.map { it[clinicAddressKey] ?: "" }
    val msgTemplate: Flow<String> = context.dataStore.data.map {
        it[msgTemplateKey] ?: "سلام {name}، یادآوری جلسه: {date} ساعت {time}. مرکز گل سرخ"
    }
    val isPro: Flow<Boolean> = context.dataStore.data.map { it[isProKey] ?: false }
    val privacyAccepted: Flow<Boolean> = context.dataStore.data.map { it[privacyKey] ?: false }
    val biometricEnabled: Flow<Boolean> = context.dataStore.data.map { it[biometricKey] ?: false }
    val trialStartMillis: Flow<Long> = context.dataStore.data.map { it[trialStartKey] ?: 0L }
    val themeHue: Flow<Float> = context.dataStore.data.map { it[themeHueKey] ?: 350f }
    val themeId: Flow<String> = context.dataStore.data.map { it[themeIdKey] ?: "sky" }

    suspend fun setRemindersEnabled(v: Boolean) = context.dataStore.edit { it[remindersKey] = v }
    suspend fun setRemindDayBefore(v: Boolean) = context.dataStore.edit { it[dayBeforeKey] = v }
    suspend fun setRemindHourBefore(v: Boolean) = context.dataStore.edit { it[hourBeforeKey] = v }
    suspend fun setRemindMorning(v: Boolean) = context.dataStore.edit { it[morningKey] = v }
    suspend fun setOnboardingDone(v: Boolean) = context.dataStore.edit { it[onboardingKey] = v }
    suspend fun setPinHash(hash: String?) = context.dataStore.edit {
        if (hash == null) it.remove(pinHashKey) else it[pinHashKey] = hash
    }
    suspend fun setClinicName(v: String) = context.dataStore.edit { it[clinicNameKey] = v }
    suspend fun setClinicPhone(v: String) = context.dataStore.edit { it[clinicPhoneKey] = v }
    suspend fun setClinicAddress(v: String) = context.dataStore.edit { it[clinicAddressKey] = v }
    suspend fun setMsgTemplate(v: String) = context.dataStore.edit { it[msgTemplateKey] = v }
    suspend fun setThemeHue(v: Float) = context.dataStore.edit { it[themeHueKey] = v }
    suspend fun setThemeId(v: String) = context.dataStore.edit { it[themeIdKey] = v }
    suspend fun setIsPro(v: Boolean) = context.dataStore.edit { it[isProKey] = v }
    suspend fun setPrivacyAccepted(v: Boolean) = context.dataStore.edit { it[privacyKey] = v }
    suspend fun setBiometricEnabled(v: Boolean) = context.dataStore.edit { it[biometricKey] = v }
    suspend fun setTrialStartMillis(v: Long) = context.dataStore.edit { it[trialStartKey] = v }

    /** همه تنظیمات به‌صورت Map برای پشتیبان */
    suspend fun getAllAsMap(): Map<String, String> {
        val prefs = context.dataStore.data.first()
        val map = mutableMapOf<String, String>()
        prefs[remindersKey]?.let { map["reminders_enabled"] = it.toString() }
        prefs[dayBeforeKey]?.let { map["remind_day_before"] = it.toString() }
        prefs[hourBeforeKey]?.let { map["remind_hour_before"] = it.toString() }
        prefs[morningKey]?.let { map["remind_morning"] = it.toString() }
        prefs[onboardingKey]?.let { map["onboarding_done"] = it.toString() }
        prefs[pinHashKey]?.let { map["pin_hash"] = it }
        prefs[clinicNameKey]?.let { map["clinic_name"] = it }
        prefs[clinicPhoneKey]?.let { map["clinic_phone"] = it }
        prefs[clinicAddressKey]?.let { map["clinic_address"] = it }
        prefs[msgTemplateKey]?.let { map["msg_template"] = it }
        prefs[themeHueKey]?.let { map["theme_hue"] = it.toString() }
        prefs[themeIdKey]?.let { map["theme_id"] = it }
        prefs[isProKey]?.let { map["is_pro"] = it.toString() }
        prefs[privacyKey]?.let { map["privacy_accepted"] = it.toString() }
        prefs[biometricKey]?.let { map["biometric_enabled"] = it.toString() }
        prefs[trialStartKey]?.let { map["trial_start_millis"] = it.toString() }
        return map
    }

    suspend fun applyFromMap(map: Map<String, String>) {
        context.dataStore.edit { prefs ->
            map["reminders_enabled"]?.toBooleanStrictOrNull()?.let { prefs[remindersKey] = it }
            map["remind_day_before"]?.toBooleanStrictOrNull()?.let { prefs[dayBeforeKey] = it }
            map["remind_hour_before"]?.toBooleanStrictOrNull()?.let { prefs[hourBeforeKey] = it }
            map["remind_morning"]?.toBooleanStrictOrNull()?.let { prefs[morningKey] = it }
            map["onboarding_done"]?.toBooleanStrictOrNull()?.let { prefs[onboardingKey] = it }
            map["pin_hash"]?.let { prefs[pinHashKey] = it }
            map["clinic_name"]?.let { prefs[clinicNameKey] = it }
            map["clinic_phone"]?.let { prefs[clinicPhoneKey] = it }
            map["clinic_address"]?.let { prefs[clinicAddressKey] = it }
            map["msg_template"]?.let { prefs[msgTemplateKey] = it }
            map["theme_hue"]?.toFloatOrNull()?.let { prefs[themeHueKey] = it }
            map["theme_id"]?.let { prefs[themeIdKey] = it }
            map["is_pro"]?.toBooleanStrictOrNull()?.let { prefs[isProKey] = it }
            map["privacy_accepted"]?.toBooleanStrictOrNull()?.let { prefs[privacyKey] = it }
            map["biometric_enabled"]?.toBooleanStrictOrNull()?.let { prefs[biometricKey] = it }
            map["trial_start_millis"]?.toLongOrNull()?.let { prefs[trialStartKey] = it }
        }
    }
}
