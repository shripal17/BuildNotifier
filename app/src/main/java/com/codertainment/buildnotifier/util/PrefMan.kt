package com.codertainment.buildnotifier.util

import android.content.Context

class PrefMan private constructor(ctx: Context, fileName: String = "prefs") {

  companion object {

    private var mInstance: PrefMan? = null

    fun getInstance(ctx: Context, fileName: String = "prefs"): PrefMan {
      if (mInstance == null) {
        mInstance = PrefMan(ctx, fileName)
      }
      return mInstance!!
    }
  }

  private val prefs = ctx.getSharedPreferences(fileName, Context.MODE_PRIVATE)
  private val editor = prefs.edit()

  fun saveString(key: String, value: String) {
    editor.putString(key, value)
    editor.apply()
  }

  fun saveInt(key: String, value: Int) {
    editor.putInt(key, value)
    editor.apply()
  }

  fun saveFloat(key: String, value: Float) {
    editor.putFloat(key, value)
    editor.apply()
  }

  fun saveBool(key: String, value: Boolean) {
    editor.putBoolean(key, value)
    editor.apply()
  }

  fun getString(key: String): String? = prefs.getString(key, null)

  fun getInt(key: String): Int = prefs.getInt(key, 0)

  fun getFloat(key: String): Float = prefs.getFloat(key, 0f)

  fun getBool(key: String): Boolean = prefs.getBoolean(key, false)

  fun saveUsername(username: String) = saveString("username", username)

  fun getUsername(): String? = getString("username")

  fun saveSession() = saveBool("logged_in", true)

  fun deleteSession() = saveBool("logged_in", false)

  fun getSession(): Boolean = getBool("logged_in")

  fun getDevice(): String? = getString("device")

  fun saveDevice(device: String) = saveString("device", device)

  fun clear() {
    editor.clear()
    editor.apply()
  }
}