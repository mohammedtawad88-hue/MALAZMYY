package com.example.data

/**
 * Platform-specific helper to fetch deleted prepopulated file IDs from Firebase
 */
expect suspend fun fetchDeletedPrepopulatedIds(): Set<String>
