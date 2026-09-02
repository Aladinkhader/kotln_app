package com.example.sheikhaudio

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val bg = Color.rgb(15, 61, 59)
    private val dark = Color.rgb(9, 39, 38)
    private val card = Color.rgb(18, 72, 69)
    private val teal = Color.rgb(59, 177, 170)
    private val muted = Color.rgb(137, 214, 209)
    private val sampleUrl = "https://archive.org/download/23-23-mp-3-160-k/%D8%A7%D9%84%D8%AA%D8%B4%D9%88%D9%8A%D9%82%D9%8A%D8%A9.mp3"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = dark
        window.navigationBarColor = dark
        requestNotificationPermission()
        showSplash()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 20)
        }
    }

    private fun showSplash() {
        val splash = FrameLayout(this).apply { setBackgroundColor(dark) }
        val image = ImageView(this).apply {
            setImageResource(R.drawable.sheikh)
            scaleType = ImageView.ScaleType.CENTER_CROP
            alpha = 0f
            scaleX = 0.6f
            scaleY = 0.6f
        }
        val params = FrameLayout.LayoutParams(300, 300)
        params.gravity = Gravity.CENTER
        splash.addView(image, params)
        setContentView(splash)
        image.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(900)
            .withEndAction { setContentView(buildUi()) }.start()
    }

    private fun text(value: String, size: Float, color: Int = Color.WHITE) =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            setPadding(8, 8, 8, 8)
            textDirection = View.TEXT_DIRECTION_RTL
        }

    private fun buildUi(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        val header = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16, 20, 16, 12)
        }
        val avatar = ImageView(this).apply {
            setImageResource(R.drawable.sheikh)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        header.addView(avatar, LinearLayout.LayoutParams(58, 58))
        val heading = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        heading.addView(text("الشيخ د. محمد الأمين إسماعيل", 17f))
        heading.addView(text("الرئيسية", 12f, muted))
        header.addView(heading, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(text("🔔", 22f, muted))
        root.addView(header)

        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 8, 16, 120)
        }

        val intro = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 18, 20, 18)
            setBackgroundColor(Color.rgb(35, 117, 113))
        }
        intro.addView(text("استمع إلى أحدث المواعظ والبرامج والخطب العلمية", 14f))
        val browse = Button(this).apply {
            this.text = "تصفح كل الأقسام"
            setTextColor(dark)
            setBackgroundColor(teal)
        }
        intro.addView(browse)
        content.addView(intro)

        content.addView(text("مختارات من المحاضرات", 15f, muted))
        content.addView(lecture("التشويقية لبرنامج ليتفقهوا", "برنامج ليتفقهوا", sampleUrl))
        content.addView(lecture("من أسباب التحصيل العلمي", "برنامج ليتفقهوا", sampleUrl))
        content.addView(text("الأقسام العلمية", 15f, muted))

        listOf("برنامج ليتفقهوا", "مواعظ", "خطب الجمعة").forEach { section ->
            val button = Button(this).apply {
                this.text = section
                setTextColor(Color.WHITE)
                setBackgroundColor(card)
                setOnClickListener {
                    Toast.makeText(context, "سيتم تحميل محاضرات $section", Toast.LENGTH_SHORT).show()
                }
            }
            content.addView(button, LinearLayout.LayoutParams(-1, 64).apply {
                setMargins(0, 6, 0, 6)
            })
        }

        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val navigation = text("⌂       ♫       ▣       ⇩       ⚙", 25f, muted).apply {
            gravity = Gravity.CENTER
            setBackgroundColor(dark)
        }
        root.addView(navigation, LinearLayout.LayoutParams(-1, 72))
        return root
    }

    private fun lecture(title: String, series: String, url: String): View {
        val row = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(12, 8, 12, 8)
            setBackgroundColor(card)
        }
        val info = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        info.addView(text(title, 14f))
        info.addView(text(series, 11f, muted))
        row.addView(info, LinearLayout.LayoutParams(0, -2, 1f))
        val play = Button(this).apply {
            this.text = "▶"
            setTextColor(teal)
            setOnClickListener { startPlayback(title, series, url) }
        }
        row.addView(play, LinearLayout.LayoutParams(70, 60))
        row.layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, 5, 0, 5)
        }
        return row
    }

    private fun startPlayback(title: String, series: String, url: String) {
        val intent = Intent(this, PlaybackService::class.java).apply {
            action = PlaybackService.PLAY
            putExtra("title", title)
            putExtra("series", series)
            putExtra("url", url)
        }
        ContextCompat.startForegroundService(this, intent)
        Toast.makeText(this, "جارٍ التشغيل في الخلفية", Toast.LENGTH_SHORT).show()
    }
}
