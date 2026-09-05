package com.bhajostore.app

import android.annotation.SuppressLint
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
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
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
        webView.webChromeClient = WebChromeClient()

        // WebViewClient with Error Handling & Offline Popup support
        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    loadOfflinePage(view)
                }
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                loadOfflinePage(view)
            }

            private fun loadOfflinePage(view: WebView?) {
                val offlineHtml = """
                    <html>
                    <head>
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <style>
                            body { 
                                font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                                text-align: center; 
                                background-color: #0b0f19; 
                                margin: 0;
                                padding: 0;
                                display: flex;
                                justify-content: center;
                                align-items: center;
                                height: 100vh;
                                color: #f8fafc;
                            }
                            .popup { 
                                background: #111827; 
                                border: 1px solid #1f2937;
                                padding: 35px 25px; 
                                margin: 20px; 
                                border-radius: 16px; 
                                box-shadow: 0 20px 40px rgba(0,0,0,0.7); 
                                width: 100%;
                                max-width: 340px;
                                animation: slideUpFade 0.6s cubic-getByzier(0.16, 1, 0.3, 1) forwards;
                            }
                            @keyframes slideUpFade {
                                0% { opacity: 0; transform: translateY(25px); }
                                100% { opacity: 1; transform: translateY(0); }
                            }
                            .icon-box { font-size: 40px; margin-bottom: 15px; }
                            h2 { color: #f97316; font-size: 20px; font-weight: 600; margin: 0 0 10px 0; }
                            p { color: #9ca3af; font-size: 14px; line-height: 1.5; margin: 0 0 25px 0; }
                            button { 
                                background: linear-gradient(135deg, #f97316, #ea580c); 
                                color: white; 
                                border: none; 
                                padding: 12px 0; 
                                width: 100%;
                                border-radius: 8px; 
                                font-size: 15px; 
                                font-weight: 600;
                                cursor: pointer; 
                                box-shadow: 0 4px 12px rgba(249, 115, 22, 0.4);
                            }
                            button:active { transform: scale(0.98); }
                            .dev-name {
                                margin-top: 25px;
                                font-size: 13px;
                                font-weight: 700;
                                letter-spacing: 1.5px;
                                text-transform: uppercase;
                                background: linear-gradient(90deg, #38bdf8, #818cf8, #c084fc, #38bdf8);
                                background-size: 300% auto;
                                color: transparent;
                                -webkit-background-clip: text;
                                -webkit-text-fill-color: transparent;
                                animation: shimmerEffect 4s linear infinite, softPulse 2s ease-in-out infinite alternate;
                            }
                            @keyframes shimmerEffect {
                                0% { background-position: 0% center; }
                                100% { background-position: 300% center; }
                            }
                            @keyframes softPulse {
                                0% { transform: scale(0.97); opacity: 0.85; }
                                100% { transform: scale(1.03); opacity: 1; filter: drop-shadow(0 0 8px rgba(56, 189, 248, 0.4)); }
                            }
                        </style>
                    </head>
                    <body>
                        <div class="popup">
                            <div class="icon-box">⚡</div>
                            <h2>No Internet Connection</h2>
                            <p>ଲାଇଭ୍ ପେଜ୍ ଲୋଡ୍ ହୋଇପାରିଲା ନାହିଁ। ଦୟାକରି ଆପଣଙ୍କର ଇଣ୍ଟରନେଟ୍ କନେକ୍ସନ ଯାଞ୍ଚ କରନ୍ତୁ।</p>
                            <button onclick="window.location.reload()">Retry</button>
                            <div class="dev-name">Phoenix Edit Point</div>
                        </div>
                    </body>
                    </html>
                """
                view?.loadDataWithBaseURL(null, offlineHtml, "text/html", "UTF-8", null)
            }
        }

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
