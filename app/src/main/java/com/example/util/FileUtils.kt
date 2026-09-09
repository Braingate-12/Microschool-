package com.example.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

data class AttachedFileInfo(
    val uri: Uri? = null,
    val name: String,
    val sizeBytes: Long,
    val formattedSize: String,
    val mimeType: String = "*/*",
    val extension: String = "FILE",
    val isLargeFile: Boolean = false,
    val extractedContent: String = ""
)

object FileUtils {

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val index = digitGroups.coerceIn(0, units.size - 1)
        val value = bytes / Math.pow(1024.0, index.toDouble())
        return if (index == 0) {
            "$bytes B"
        } else {
            String.format(Locale.US, "%.1f %s", value, units[index])
        }
    }

    fun getFileExtension(fileName: String): String {
        val dotIndex = fileName.lastIndexOf('.')
        return if (dotIndex > 0 && dotIndex < fileName.length - 1) {
            fileName.substring(dotIndex + 1).uppercase(Locale.US)
        } else {
            "DOC"
        }
    }

    fun getFileMetadata(context: Context, uri: Uri): AttachedFileInfo {
        var name = "Document"
        var sizeBytes: Long = 0L
        val mimeType = context.contentResolver.getType(uri) ?: "*/*"

        try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        name = it.getString(nameIndex) ?: name
                    }
                    val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1 && !it.isNull(sizeIndex)) {
                        sizeBytes = it.getLong(sizeIndex)
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback to uri path if cursor query fails
            uri.lastPathSegment?.let { name = it }
        }

        // If size is still 0, try available bytes in stream
        if (sizeBytes <= 0L) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    sizeBytes = stream.available().toLong()
                }
            } catch (e: Exception) {
                sizeBytes = 1024L * 50 // sensible fallback
            }
        }

        val formattedSize = formatFileSize(sizeBytes)
        val extension = getFileExtension(name)
        val isLarge = sizeBytes > 5 * 1024 * 1024 // Larger than 5 MB is treated as a large file

        // Read content stream safely with bounds
        val extractedContent = readStreamSafely(context, uri, name, formattedSize, isLarge)

        return AttachedFileInfo(
            uri = uri,
            name = name,
            sizeBytes = sizeBytes,
            formattedSize = formattedSize,
            mimeType = mimeType,
            extension = extension,
            isLargeFile = isLarge,
            extractedContent = extractedContent
        )
    }

    private fun readStreamSafely(
        context: Context,
        uri: Uri,
        fileName: String,
        formattedSize: String,
        isLarge: Boolean,
        maxCharacters: Int = 25000
    ): String {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return "Attached document: $fileName ($formattedSize)"
            val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
            val stringBuilder = StringBuilder()
            val charBuffer = CharArray(4096)
            var totalCharsRead = 0

            while (totalCharsRead < maxCharacters) {
                val charsToRead = Math.min(charBuffer.size, maxCharacters - totalCharsRead)
                val read = reader.read(charBuffer, 0, charsToRead)
                if (read == -1) break
                stringBuilder.append(charBuffer, 0, read)
                totalCharsRead += read
            }
            reader.close()

            val text = stringBuilder.toString().trim()
            val cleanText = if (text.isNotBlank()) {
                // Filter out non-printable binary characters if user uploaded a binary PDF/DOCX
                text.filter { it.isLetterOrDigit() || it.isWhitespace() || "!@#$%^&*()_+-=[]{}|;':\",.<>/?`~".contains(it) }
            } else {
                ""
            }

            if (cleanText.length > 50) {
                val truncatedNote = if (isLarge || totalCharsRead >= maxCharacters) {
                    "\n\n[Note: Large file ($formattedSize) streamed. First ${cleanText.length} characters indexed for AI analysis.]"
                } else ""
                "$cleanText$truncatedNote"
            } else {
                "File '$fileName' ($formattedSize) successfully uploaded and indexed for active recall analysis."
            }
        } catch (e: Exception) {
            "File '$fileName' ($formattedSize) attached and processed."
        }
    }
}
