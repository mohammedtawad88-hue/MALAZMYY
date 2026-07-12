package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.darwin.NSObject
import platform.Foundation.NSURL

class PickerDelegate(private val onFilePicked: (String, String, String) -> Unit) : NSObject(), UIDocumentPickerDelegateProtocol {
    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL ?: return
        val name = url.lastPathComponent ?: "file.pdf"
        onFilePicked(url.absoluteString ?: "", name, "Unknown Size")
    }
}

@Composable
actual fun rememberPlatformFilePicker(onFilePicked: (fileUri: String, name: String, sizeStr: String) -> Unit): () -> Unit {
    val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
    val delegate = remember { PickerDelegate(onFilePicked) }
    
    val picker = remember {
        val documentTypes = listOf("com.adobe.pdf")
        UIDocumentPickerViewController(
            documentTypes = documentTypes,
            inMode = UIDocumentPickerMode.UIDocumentPickerModeImport
        ).apply {
            this.delegate = delegate
        }
    }
    return {
        rootViewController?.presentViewController(picker, animated = true, completion = null)
    }
}
