package com.bhajostore.app

import android.annotation.SuppressLint
import android.content.ComponentName
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
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAndSwitchFestivalIcon()

        supportActionBar?.hide()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && checkSelfPermission(
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
        
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true

        webView.webChromeClient = WebChromeClient()

        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                super.onReceivedError(view, request, error)
                if (request.isForMainFrame) {
                    loadOfflinePage(view)
                }
            }

            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String,
                failingUrl: String
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                loadOfflinePage(view)
            }

            private fun loadOfflinePage(view: WebView) {
                val offlineHtml = """
                    <html>
                    <head>
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <style>
                            body { 
                                font-family: -apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif;
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
                                animation: slideUpFade 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
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
                """.trimIndent()

                view.loadDataWithBaseURL(null, offlineHtml, "text/html", "UTF-8", null)
            }
        }

        webView.addJavascriptInterface(PdfStorageBridge(this), "AndroidPdf")
        webView.loadUrl("https://jyotiprakashmohapatra63-creator.github.io/Bhajo-Store")
    }

    private fun checkAndSwitchFestivalIcon() {
        val today = Calendar.getInstance()
        val currentMonth = today.get(Calendar.MONTH) + 1
        val currentDay = today.get(Calendar.DAY_OF_MONTH)

        var targetAlias = "$packageName.MainActivityDefault"

        if (isWithinRange(currentMonth, currentDay, 12, 30, 1, 14)) {
            targetAlias = "$packageName.MainActivityMakarSankranti"
        } else if (isWithinRange(currentMonth, currentDay, 2, 11, 2, 26)) {
            targetAlias = "$packageName.MainActivityMahaShivaratri"
        } else if (isWithinRange(currentMonth, currentDay, 2, 27, 3, 14)) {
            targetAlias = "$packageName.MainActivityHoli"
        } else if (isWithinRange(currentMonth, currentDay, 3, 22, 4, 6)) {
            targetAlias = "$packageName.MainActivityRamNavami"
        } else if (isWithinRange(currentMonth, currentDay, 6, 12, 6, 27)) {
            targetAlias = "$packageName.MainActivityRathYatra"
        } else if (isWithinRange(currentMonth, currentDay, 7, 25, 8, 9)) {
            targetAlias = "$packageName.MainActivityRakshaBandhan"
        } else if (isWithinRange(currentMonth, currentDay, 8, 1, 8, 16)) {
            targetAlias = "$packageName.MainActivityKrishnaJanmashtami"
        } else if (isWithinRange(currentMonth, currentDay, 8, 12, 8, 27)) {
            targetAlias = "$packageName.MainActivityGaneshChaturthi"
        } else if (isWithinRange(currentMonth, currentDay, 9, 17, 10, 2)) {
            targetAlias = "$packageName.MainActivityDurgaPuja"
        } else if (isWithinRange(currentMonth, currentDay, 9, 30, 10, 15)) {
            targetAlias = "$packageName.MainActivityLaxmiPuja"
        } else if (isWithinRange(currentMonth, currentDay, 10, 5, 10, 20)) {
            targetAlias = "$packageName.MainActivityDiwali"
        }

        updateAppIcon(targetAlias)
    }

    private fun isWithinRange(cMonth: Int, cDay: Int, startMonth: Int, startDay: Int, endMonth: Int, endDay: Int): Boolean {
        val currentDateVal = cMonth * 100 + cDay
        val startDateVal = startMonth * 100 + startDay
        val endDateVal = endMonth * 100 + endDay

        return if (startDateVal <= endDateVal) {
            currentDateVal in startDateVal..endDateVal
        } else {
            currentDateVal >= startDateVal || currentDateVal <= endDateVal
        }
    }

    private fun updateAppIcon(targetAliasName: String) {
        val pm = packageManager
        val allAliases = arrayOf(
            "$packageName.MainActivityDefault",
            "$packageName.MainActivityMakarSankranti",
            "$packageName.MainActivityMahaShivaratri",
            "$packageName.MainActivityHoli",
            "$packageName.MainActivityRamNavami",
            "$packageName.MainActivityRathYatra",
            "$packageName.MainActivityRakshaBandhan",
            "$packageName.MainActivityKrishnaJanmashtami",
            "$packageName.MainActivityGaneshChaturthi",
            "$packageName.MainActivityDurgaPuja",
            "$packageName.MainActivityLaxmiPuja",
            "$packageName.MainActivityDiwali"
        )

        for (alias in allAliases) {
            pm.setComponentEnabledSetting(
                ComponentName(this, alias),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }

        pm.setComponentEnabledSetting(
            ComponentName(this, targetAliasName),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
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
                val isFolderReady = folder.exists() || folder.mkdirs()
                if (!isFolderReady) {
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
