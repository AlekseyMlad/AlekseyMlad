package ru.netology.nework.util

import android.content.Context
import android.net.Uri
import java.io.File

fun Uri.toFile(context: Context): File {
    val inputStream = context.contentResolver.openInputStream(this)
    val tempFile = File.createTempFile("tmp_", ".tmp", context.cacheDir)
    tempFile.outputStream().use {
        inputStream?.copyTo(it)
    }
    inputStream?.close()
    return tempFile
}
