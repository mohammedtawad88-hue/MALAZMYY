package com.example.ui

import androidx.compose.runtime.Composable

@Composable
expect fun rememberPlatformFilePicker(onFilePicked: (fileUri: String, name: String, sizeStr: String) -> Unit): () -> Unit
