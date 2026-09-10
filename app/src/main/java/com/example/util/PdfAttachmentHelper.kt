package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast

object PdfAttachmentHelper {

    fun getFileNameAndSize(context: Context, uri: Uri): Pair<String, String> {
        var name = "PW_Attachment.pdf"
        var sizeStr = "Unknown size"

        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) {
                        name = cursor.getString(nameIndex) ?: name
                    }
                    if (sizeIndex != -1) {
                        val bytes = cursor.getLong(sizeIndex)
                        sizeStr = formatBytes(bytes)
                    }
                }
            }
        } catch (_: Exception) {
            // fallback gracefully
        }

        return Pair(name, sizeStr)
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 KB"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return if (mb >= 1.0) {
            String.format("%.1f MB", mb)
        } else {
            String.format("%.0f KB", kb)
        }
    }

    fun openPdf(context: Context, uriString: String?, fallbackTitle: String = "PW Document") {
        if (uriString.isNullOrBlank()) {
            Toast.makeText(context, "Attached document: $fallbackTitle (Simulated PW File)", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val uri = Uri.parse(uriString)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No PDF viewer found on device. ($fallbackTitle)", Toast.LENGTH_SHORT).show()
        }
    }
}
