package com.example.slaughterhousescreen.util

import android.content.Context
import android.content.SharedPreferences

object PreferenceManager {
    private const val PREF_NAME = "user_prefs"
    private const val KEY_BASE_URL = "base_url"
    private const val KEY_IS_ADDED_URL = "is_AddedUrl"
    private const val KEY_URL = "url"
    private const val KEY_IS_ADDED_URL_2 = "is_AddedUrl"

    private const val KEY_BRANCH_CODE ="branch_code"
    private const val KEY_IS_ADDED_CODE = "is_added_code"

    private const val KEY_DISPLAY_NUMBER ="display_number"
    private const val KEY_IS_ADDED_DISPLAY_NUMBER = "is_added_display_number"


    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isSavedAnyURL(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_IS_ADDED_URL_2, false)
    }

    // Get the flag value from SharedPreferences
    fun getDeviceConfigurationApiFlag(context: Context): Boolean {
        // Use applicationContext to ensure that context is valid
        val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("isDeviceConfigurationAPICalled", false)
    }

    fun setDeviceConfigurationApiFlag(isCalled: Boolean, context: Context) {
        // Use applicationContext to ensure that context is valid
        val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isDeviceConfigurationAPICalled", isCalled)
        editor.apply()  // Save the flag asynchronously
    }



    fun saveUrl(context: Context, baseUrl: String , isAddedUrl : Boolean) {
        val editor = getPreferences(context).edit()
        editor.putString(KEY_URL, baseUrl)
        editor.putBoolean(KEY_IS_ADDED_URL_2, isAddedUrl)
        editor.apply()
    }
    fun getUrl(context: Context): String? {
        return getPreferences(context).getString(KEY_URL, "")
    }


    fun saveBaseUrl(context: Context, baseUrl: String , isAddedUrl : Boolean) {
        val editor = getPreferences(context).edit()
        editor.putString(KEY_BASE_URL, baseUrl)
        editor.putBoolean(KEY_IS_ADDED_URL, isAddedUrl)
        editor.apply()
    }


    fun isAddedURL(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_IS_ADDED_URL, false)
    }
    fun getBaseUrl(context: Context): String? {
        return getPreferences(context).getString(KEY_BASE_URL, "")
    }
    fun clearUrl(context: Context) {
        val editor = getPreferences(context).edit()
        editor.remove(KEY_BASE_URL)
        editor.remove(KEY_IS_ADDED_URL) // Optionally clear this as well
        editor.apply()
        editor.commit()
    }

    fun setURl(context: Context, isAddedUrl: Boolean) {
        val editor = getPreferences(context).edit()
        editor.putBoolean(KEY_IS_ADDED_URL, isAddedUrl)
        editor.apply()
    }

}
