package com.example.data

import platform.Foundation.NSUserDefaults

actual object KeyValueStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun putBoolean(key: String, value: Boolean) {
        defaults.setBool(value, key)
    }

    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        if (!contains(key)) return defaultValue
        return defaults.boolForKey(key)
    }

    actual fun putString(key: String, value: String) {
        defaults.setObject(value, key)
    }

    actual fun getString(key: String, defaultValue: String?): String? {
        return defaults.stringForKey(key) ?: defaultValue
    }

    actual fun remove(key: String) {
        defaults.removeObjectForKey(key)
    }

    actual fun contains(key: String): Boolean {
        return defaults.objectForKey(key) != null
    }
}
