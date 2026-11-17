
package com.hisab.kitab

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>

    private var cameraUri: Uri? = null
    private var selectedPhotoPath: String? = null
    private var drawerOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = AppDatabase(this)

        galleryLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                saveSelectedPhoto(uri)
            }
        }

        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            val uri = cameraUri
            if (success && uri != null) {
                saveSelectedPhoto(uri)
            } else {
                showMessage("फोटो नहीं लिया जा सका। दोबारा कोशिश करें।")
            }
        }

        showHome()

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (drawerOpen) {
                        drawerOpen = false
                        showHome()
                    } else {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("ऐप बंद करें?")
                            .setMessage("क्या आप HISAB-KITAB बंद करना चाहते हैं?")
                            .setNegativeButton("नहीं", null)
                            .setPositiveButton("हाँ") { _, _ ->
                                finish()
                            }
                            .show()
                    }
                }
            }
        )
    }

    private fun baseLayout(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 18)
            setBackgroundColor(0xFF101827.toInt())
        }
    }

    private fun showHome() {
        drawerOpen = false
        val root = baseLayout()

        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        scroll.addView(content)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(-1, -1)
        )

        content.addView(
            text("HISAB-KITAB", 28f, 0xFFFFFFFF.toInt(), true)
        )

        content.addView(
            text("आपके काम का पूरा हिसाब", 14f, 0xFFB8C5D9.toInt())
        )

        val production = ProductionManager(db)
        val totalPieces = production.totalQuantity()
        val totalAmount = production.totalAmount()
        val received = PaymentManager(db).totalReceived()
        val expenses = ExpenseManager(db).totalExpenses()
        val remaining = totalAmount - received

        content.addView(space())
        content.addView(summaryCard("कुल पीस", totalPieces.toString()))
        content.addView(summaryCard("कुल कमाई", money(totalAmount)))
        content.addView(summaryCard("मिली हुई रकम", money(received)))
        content.addView(summaryCard("कुल खर्च", money(expenses)))
        content.addView(summaryCard("बाकी रकम", money(remaining)))

        content.addView(space())

        addButton(content, "🧮 SMART CALCULATOR") {
            showCalculator()
        }

        addButton(content, "＋ नया पीस जोड़ें") {
            showAddPiece()
        }

        addButton(content, "₹ पेमेंट जोड़ें") {
            showPayment()
        }

        addButton(content, "खर्च जोड़ें") {
            showExpense()
        }

        addButton(content, "पीस हिस्ट्री") {
            showHistory()
        }

        addButton(content, "डिजाइन और STYLE") {
            showMessage("Design Studio अगले अपडेट में जोड़ा जाएगा।")
        }

        setContentView(root)
    }

    private fun showAddPiece() {
        selectedPhotoPath = null

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 8, 20, 8)
        }

        val name = EditText(this).apply {
            hint = "पीस का नाम"
        }

        val quantity = EditText(this).apply {
            hint = "कितने पीस बनाए?"
            inputType = 2
        }

        val rate = EditText(this).apply {
            hint = "एक पीस का रेट"
            inputType = 8194
        }

        val photoPreview = ImageView(this).apply {
            visibility = View.GONE
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            maxHeight = 220
        }

        val photoStatus = text(
            "अभी कोई फोटो नहीं चुना गया",
            13f,
            0xFFB8C5D9.toInt()
        )

        val photoButton = Button(this).apply {
            text = "📷 कैमरा / गैलरी से फोटो जोड़ें"
            isAllCaps = false
        }

        photoButton.setOnClickListener {
            showPhotoOptions(photoPreview, photoStatus)
        }

        box.addView(name)
        box.addView(quantity)
        box.addView(rate)
        box.addView(space())
        box.addView(photoButton)
        box.addView(photoStatus)
        box.addView(photoPreview)

        val dialog = AlertDialog.Builder(this)
            .setTitle("नया पीस जोड़ें")
            .setView(box)
            .setNegativeButton("रद्द", null)
            .setPositiveButton("सेव करें", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    val q = quantity.text.toString().toIntOrNull() ?: 0
                    val r = rate.text.toString().toDoubleOrNull() ?: 0.0

                    if (name.text.isBlank() || q <= 0 || r < 0) {
                        showMessage("सही नाम, मात्रा और रेट भरें।")
                        return@setOnClickListener
                    }

                    ProductionManager(db).addEntry(
                        name.text.toString(),
                        q,
                        r,
                        selectedPhotoPath
                    )

                    selectedPhotoPath = null
                    dialog.dismiss()
                    showHome()
                }
        }

        dialog.setOnDismissListener {
            selectedPhotoPath = null
        }

        dialog.show()
    }

    private fun showPhotoOptions(
        preview: ImageView,
        status: TextView
    ) {
        AlertDialog.Builder(this)
            .setTitle("फोटो कहाँ से जोड़ना है?")
            .setItems(
                arrayOf("📷 कैमरा से फोटो खींचें", "🖼️ गैलरी से फोटो चुनें")
            ) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> galleryLauncher.launch("image/*")
                }
            }
            .setNegativeButton("रद्द", null)
            .show()

        photoPreviewTarget = preview
        photoStatusTarget = status
    }

    private var photoPreviewTarget: ImageView? = null
    private var photoStatusTarget: TextView? = null

    private fun openCamera() {
        try {
            val cameraFolder = File(cacheDir, "camera").apply {
                if (!exists()) mkdirs()
            }

            val photoFile = File(
                cameraFolder,
                "camera_${System.currentTimeMillis()}.jpg"
            )

            cameraUri = FileProvider.getUriForFile(
                this,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                photoFile
            )

            cameraLauncher.launch(cameraUri!!)
        } catch (e: Exception) {
            showMessage("कैमरा खोलने में समस्या हुई: ${e.message}")
        }
    }

    private fun saveSelectedPhoto(uri: Uri) {
        try {
            val path = PhotoManager(this).savePhoto(uri)

            if (path.isNullOrBlank()) {
                showMessage("फोटो सेव नहीं हो सकी।")
                return
            }

            selectedPhotoPath = path

            val bitmap = loadSafePreview(path)
            val preview = photoPreviewTarget
            val status = photoStatusTarget

            if (bitmap != null && preview != null) {
                preview.setImageBitmap(bitmap)
                preview.visibility = View.VISIBLE
            }

            status?.text = "✓ फोटो एंट्री के लिए तैयार है"
        } catch (e: Exception) {
            showMessage("फोटो सेव करने में समस्या हुई।")
        }
    }

    private fun loadSafePreview(path: String): Bitmap? {
        return try {
            val bounds = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            BitmapFactory.decodeFile(path, bounds)

            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                return null
            }

            var sample = 1
            while (
                bounds.outWidth / sample > 1000 ||
                bounds.outHeight / sample > 1000
            ) {
                sample *= 2
            }

            val options = BitmapFactory.Options().apply {
                inSampleSize = sample
                inPreferredConfig = Bitmap.Config.RGB_565
            }

            BitmapFactory.decodeFile(path, options)
        } catch (e: Exception) {
            null
        }
    }

    private fun showCalculator() {
        val calculatorView = CalculatorScreen(this).show()

        val dialog = AlertDialog.Builder(this)
            .setView(calculatorView)
            .create()

        dialog.show()

        val window = dialog.window ?: return

        window.setBackgroundDrawableResource(
            android.R.color.transparent
        )

        window.setLayout(
            (resources.displayMetrics.widthPixels * 0.94f).toInt(),
            -2
        )

        val params = window.attributes
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 12
        params.y = 100
        window.attributes = params

        val handle = calculatorView.findViewWithTag<View>(
            "calc_drag_handle"
        )

        var startX = 0f
        var startY = 0f
        var initialX = 0
        var initialY = 0

        handle?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.rawX
                    startY = event.rawY
                    initialX = window.attributes.x
                    initialY = window.attributes.y
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val updated = window.attributes
                    updated.x = (
                        initialX + event.rawX - startX
                    ).toInt().coerceAtLeast(0)
                    updated.y = (
                        initialY + event.rawY - startY
                    ).toInt().coerceAtLeast(0)
                    window.attributes = updated
                    true
                }

                else -> false
            }
        }
    }

    private fun showPayment() {
        val box = EditText(this).apply {
            hint = "मिली हुई रकम"
            inputType = 8194
        }

        AlertDialog.Builder(this)
            .setTitle("पेमेंट जोड़ें")
            .setView(box)
            .setNegativeButton("रद्द", null)
            .setPositiveButton("सेव") { _, _ ->
                val amount = box.text.toString().toDoubleOrNull() ?: 0.0

                if (amount > 0) {
                    PaymentManager(db).addPayment(amount)
                    showHome()
                } else {
                    showMessage("सही रकम लिखें।")
                }
            }
            .show()
    }

    private fun showExpense() {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 8, 20, 8)
        }

        val title = EditText(this).apply {
            hint = "खर्च का नाम"
        }

        val amount = EditText(this).apply {
            hint = "खर्च की रकम"
            inputType = 8194
        }

        box.addView(title)
        box.addView(amount)

        AlertDialog.Builder(this)
            .setTitle("नया खर्च")
            .setView(box)
            .setNegativeButton("रद्द", null)
            .setPositiveButton("सेव") { _, _ ->
                val value = amount.text.toString().toDoubleOrNull() ?: 0.0

                if (title.text.isNotBlank() && value > 0) {
                    ExpenseManager(db).addExpense(
                        title.text.toString(),
                        value
                    )
                    showHome()
                } else {
                    showMessage("खर्च का नाम और सही रकम लिखें।")
                }
            }
            .show()
    }

    private fun showHistory() {
        val entries = ProductionManager(db).getAllEntries()

        val message = if (entries.isEmpty()) {
            "अभी कोई पीस दर्ज नहीं है।"
        } else {
            entries.joinToString("\n\n") {
                "${it.name}\n${it.quantity} पीस × " +
                    "${money(it.rate)} = ${money(it.total)}" +
                    if (it.photo != null) "\n📷 फोटो जुड़ी हुई है" else ""
            }
        }

        AlertDialog.Builder(this)
            .setTitle("पीस हिस्ट्री")
            .setMessage(message)
            .setPositiveButton("बंद", null)
            .show()
    }

    private fun summaryCard(title: String, value: String): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 16, 18, 16)
            setBackgroundColor(0xFF202D42.toInt())
        }

        card.addView(text(title, 14f, 0xFFB8C5D9.toInt()))
        card.addView(text(value, 23f, 0xFFFFFFFF.toInt(), true))

        val params = LinearLayout.LayoutParams(-1, -2)
        params.bottomMargin = 12
        card.layoutParams = params

        return card
    }

    private fun text(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            gravity = Gravity.CENTER_VERTICAL

            if (bold) {
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            setPadding(4, 8, 4, 8)
        }
    }

    private fun addButton(
        parent: LinearLayout,
        label: String,
        action: () -> Unit
    ) {
        val button = Button(this).apply {
            text = label
            isAllCaps = false
            setOnClickListener { action() }
        }

        parent.addView(button)
    }

    private fun space(): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(1, 16)
        }
    }

    private fun money(value: Double): String {
        return "₹" + String.format("%.2f", value)
    }

    private fun showMessage(message: String) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("ठीक है", null)
            .show()
    }
}
