package com.example.data

/**
 * iOS implementation to fetch deleted prepopulated file IDs from Firebase
 * iOS doesn't have Firebase configured, so return empty set
 */
actual suspend fun fetchDeletedPrepopulatedIds(): Set<String> {
    return emptySet()
}
