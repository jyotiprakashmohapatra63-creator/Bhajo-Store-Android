package com.bhajostore.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import java.io.File
import java.io.FileOutputStream

class MainActivity : Activity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && checkSelfPermission(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
        }

        val webView = WebView(this)
        setContentView(webView)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()
        webView.addJavascriptInterface(PdfStorageBridge(this), "AndroidPdf")
        webView.loadUrl("https://jyotiprakashmohapatra63-creator.github.io/Bhajo-Store/")
    }
}

class PdfStorageBridge(private val context: Context) {
    @JavascriptInterface
    fun savePdf(base64Pdf: String, requestedName: String): String {
        return try {
            val safeName = requestedName
                .replace(Regex("[^A-Za-z0-9._ -]"), "_")
                .let { if (it.endsWith(".pdf", true)) it else "$it.pdf" }
            val bytes = Base64.decode(base64Pdf, Base64.DEFAULT)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, safeName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/Bhajo Store")
                }
                val uri = context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    values
                ) ?: throw IllegalStateException("Could not create the PDF file")

                context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                    ?: throw IllegalStateException("Could not write the PDF file")
            } else {
                val hasPermission = context.checkSelfPermission(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
                if (!hasPermission) throw IllegalStateException("Storage permission is required")

                val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val folder = File(downloads, "Bhajo Store")
                if (!folder.exists() && !folder.mkdirs()) {
                    throw IllegalStateException("Could not create Downloads folder")
                }
                FileOutputStream(File(folder, safeName)).use { it.write(bytes) }
            }
            "OK"
        } catch (error: Exception) {
            "ERROR: ${error.message ?: "Unable to save PDF"}"
        }
    }
}
