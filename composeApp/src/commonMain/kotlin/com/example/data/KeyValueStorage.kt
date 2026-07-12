package com.example.data

expect object KeyValueStorage {
    fun putBoolean(key: String, value: Boolean)
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun putString(key: String, value: String)
    fun getString(key: String, defaultValue: String?): String?
    fun remove(key: String)
    fun contains(key: String): Boolean
}
