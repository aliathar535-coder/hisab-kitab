
package com.hisab.kitab

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>

    private var cameraUri: Uri? = null
    private var selectedPhotoPath: String? = null
    private var photoPreviewTarget: ImageView? = null
    private var photoStatusTarget: TextView? = null
    private var drawerOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDatabase(this)

        galleryLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) saveSelectedPhoto(uri)
        }

        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            val uri = cameraUri
            if (success && uri != null) {
                saveSelectedPhoto(uri)
            } else {
                showMessage("फोटो नहीं लिया जा सका।")
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
                            .setPositiveButton("हाँ") { _, _ -> finish() }
                            .show()
                    }
                }
            }
        )
    }

    private fun baseLayout(): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 18)
            setBackgroundColor(0xFF101827.toInt())
        }

    private fun showHome() {
        drawerOpen = false

        val root = baseLayout()
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, -1))

        content.addView(text("HISAB-KITAB", 28f, WHITE, true))
        content.addView(text("आपके काम का पूरा हिसाब", 14f, MUTED))

        val summary = HistoryManager(db).getSummary()

        content.addView(space())
        content.addView(summaryCard("कुल पीस", summary.totalPieces.toString()))
        content.addView(summaryCard("कुल कमाई", money(summary.totalIncome)))
        content.addView(summaryCard("मिली हुई रकम", money(summary.received)))
        content.addView(summaryCard("कुल खर्च", money(summary.expenses)))
        content.addView(summaryCard("बाकी रकम", money(summary.remaining)))

        val profitTitle = if (summary.profit >= 0) "कुल मुनाफा" else "कुल नुकसान"
        content.addView(
            summaryCard(
                "$profitTitle (${String.format(Locale.getDefault(), "%.2f", summary.profitPercentage)}%)",
                money(summary.profit)
            )
        )

        content.addView(space())
        addButton(content, "🧮 SMART CALCULATOR") { showCalculator() }
        addButton(content, "＋ नया पीस जोड़ें") { showAddPiece() }
        addButton(content, "₹ पेमेंट जोड़ें") { showPayment() }
        addButton(content, "पेमेंट हिस्ट्री देखें") { showPaymentHistory() }
        addButton(content, "खर्च जोड़ें") { showExpense() }
        addButton(content, "खर्च हिस्ट्री देखें") { showExpenseHistory() }
        addButton(content, "पीस हिस्ट्री / Edit / Delete") { showHistory() }
        addButton(content, "डिजाइन और STYLE") {
            showMessage("Design Studio का अगला हिस्सा बाद में जोड़ा जाएगा।")
        }

        setContentView(root)
    }

    private fun showAddPiece() {
        selectedPhotoPath = null

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 8, 20, 8)
        }

        val name = EditText(this).apply { hint = "पीस का नाम" }
        val quantity = EditText(this).apply {
            hint = "कितने पीस बनाए?"
            inputType = 2
        }
        val rate = EditText(this).apply {
            hint = "एक पीस का रेट"
            inputType = 8194
        }
        val preview = ImageView(this).apply {
            visibility = View.GONE
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            maxHeight = 220
            setOnClickListener {
                selectedPhotoPath?.let { showFullPhoto(it) }
            }
        }
        val status = text("अभी कोई फोटो नहीं चुना गया", 13f, MUTED)

        val photoButton = Button(this).apply {
            text = "📷 कैमरा / गैलरी से फोटो जोड़ें"
            isAllCaps = false
            setOnClickListener { showPhotoOptions(preview, status) }
        }

        box.addView(name)
        box.addView(quantity)
        box.addView(rate)
        box.addView(space())
        box.addView(photoButton)
        box.addView(status)
        box.addView(preview)

        val dialog = AlertDialog.Builder(this)
            .setTitle("नया पीस जोड़ें")
            .setView(box)
            .setNegativeButton("रद्द", null)
            .setPositiveButton("सेव करें", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val q = quantity.text.toString().toIntOrNull() ?: 0
                val r = rate.text.toString().toDoubleOrNull() ?: -1.0

                if (name.text.isBlank() || q <= 0 || r < 0) {
                    showMessage("सही नाम, मात्रा और रेट भरें।")
                    return@setOnClickListener
                }

                try {
                    ProductionManager(db).addEntry(
                        name.text.toString(),
                        q,
                        r,
                        selectedPhotoPath
                    )
                    selectedPhotoPath = null
                    dialog.dismiss()
                    showHome()
                } catch (e: Exception) {
                    showMessage("एंट्री सेव नहीं हुई: ${e.message}")
                }
            }
        }

        dialog.setOnDismissListener {
            selectedPhotoPath = null
            photoPreviewTarget = null
            photoStatusTarget = null
        }
        dialog.show()
    }

    private fun showPhotoOptions(preview: ImageView, status: TextView) {
        photoPreviewTarget = preview
        photoStatusTarget = status

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
    }

    private fun openCamera() {
        try {
            val folder = File(cacheDir, "camera").apply { mkdirs() }
            val file = File(folder, "camera_${System.currentTimeMillis()}.jpg")

            cameraUri = FileProvider.getUriForFile(
                this,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                file
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
            val bitmap = PhotoManager(this).loadPhoto(path)

            if (bitmap != null) {
                photoPreviewTarget?.apply {
                    setImageBitmap(bitmap)
                    visibility = View.VISIBLE
                }
            }

            photoStatusTarget?.text = "✓ फोटो तैयार है। देखने के लिए फोटो पर दबाएँ।"
        } catch (e: Exception) {
            showMessage("फोटो सेव करने में समस्या हुई।")
        }
    }

    private fun showFullPhoto(path: String) {
        val bitmap = PhotoManager(this).loadPhoto(path, 2400, 2400)
        if (bitmap == null) {
            showMessage("यह फोटो खोली नहीं जा सकी।")
            return
        }

        val image = ZoomImageView()
        image.setImageBitmap(bitmap)

        val dialog = AlertDialog.Builder(this)
            .setView(image)
            .setPositiveButton("बंद", null)
            .create()

        dialog.show()
        dialog.window?.setLayout(
            -1,
            (resources.displayMetrics.heightPixels * 0.88f).toInt()
        )
    }

    private inner class ZoomImageView : ImageView(this@MainActivity) {
        private val matrixValue = Matrix()
        private var scale = 1f
        private var lastX = 0f
        private var lastY = 0f

        private val detector = ScaleGestureDetector(
            this@MainActivity,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val next = (scale * detector.scaleFactor).coerceIn(1f, 5f)
                    val factor = next / scale
                    matrixValue.postScale(
                        factor, factor,
                        detector.focusX, detector.focusY
                    )
                    scale = next
                    imageMatrix = matrixValue
                    return true
                }
            }
        )

        init {
            scaleType = ScaleType.MATRIX
            setBackgroundColor(0xFF080D16.toInt())
            setOnTouchListener { _, event ->
                detector.onTouchEvent(event)

                if (event.pointerCount == 1 && !detector.isInProgress) {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            lastX = event.x
                            lastY = event.y
                        }
                        MotionEvent.ACTION_MOVE -> {
                            if (scale > 1f) {
                                matrixValue.postTranslate(
                                    event.x - lastX,
                                    event.y - lastY
                                )
                                imageMatrix = matrixValue
                            }
                            lastX = event.x
                            lastY = event.y
                        }
                    }
                }
                true
            }
        }

        override fun setImageBitmap(bm: Bitmap?) {
            super.setImageBitmap(bm)
            matrixValue.reset()
            scale = 1f
            imageMatrix = matrixValue
        }
    }

    private fun showCalculator() {
        val calculatorView = CalculatorScreen(this).show()
        val dialog = AlertDialog.Builder(this)
            .setView(calculatorView)
            .create()

        dialog.show()
        val window = dialog.window ?: return
        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.setLayout(
            (resources.displayMetrics.widthPixels * 0.94f).toInt(),
            -2
        )

        val params = window.attributes
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 12
        params.y = 100
        window.attributes = params

        val handle = calculatorView.findViewWithTag<View>("calc_drag_handle")
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
                    updated.x = (initialX + event.rawX - startX).toInt().coerceAtLeast(0)
                    updated.y = (initialY + event.rawY - startY).toInt().coerceAtLeast(0)
                    window.attributes = updated
                    true
                }
                else -> false
            }
        }
    }
    
    private fun showPayment() {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 8, 20, 8)
        }
        val amount = EditText(this).apply {
            hint = "मिली हुई रकम"
            inputType = 8194
        }
        val note = EditText(this).apply {
            hint = "विवरण (जैसे एडवांस / बाकी पेमेंट)"
        }
        box.addView(amount)
        box.addView(note)

        AlertDialog.Builder(this)
            .setTitle("पेमेंट जोड़ें")
            .setView(box)
            .setNegativeButton("रद्द", null)
            .setPositiveButton("सेव") { _, _ ->
                val value = amount.text.toString().toDoubleOrNull() ?: 0.0
                if (value > 0) {
                    PaymentManager(db).addPayment(value, note.text.toString())
                    showHome()
                } else showMessage("सही रकम लिखें।")
            }
            .show()
    }

    private fun showPaymentHistory() {
        val manager = PaymentManager(db)
        val items = manager.getAllPayments()

        if (items.isEmpty()) {
            showMessage("अभी कोई पेमेंट हिस्ट्री नहीं है।")
            return
        }

        val labels = items.map {
            "${money(it.amount)}  •  ${it.note.orEmpty().ifBlank { "पेमेंट" }}\n${date(it.createdAt)}"
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("पेमेंट हिस्ट्री")
            .setItems(labels) { _, index ->
                val item = items[index]
                AlertDialog.Builder(this)
                    .setTitle("पेमेंट विवरण")
                    .setMessage(
                        "रकम: ${money(item.amount)}\n" +
                        "विवरण: ${item.note.orEmpty()}\n" +
                        "तारीख: ${date(item.createdAt)}"
                    )
                    .setNeutralButton("Edit") { _, _ ->
                        editPayment(item.id, item.amount, item.note)
                    }
                    .setNegativeButton("Delete") { _, _ ->
                        confirmDelete(
                            "यह पेमेंट हटाएँ?",
                            "हटाने के बाद कुल मिली रकम बदल जाएगी।"
                        ) {
                            manager.deletePayment(item.id)
                            showPaymentHistory()
                            showHome()
                        }
                    }
                    .setPositiveButton("बंद", null)
                    .show()
            }
            .setPositiveButton("बंद", null)
            .show()
    }

    private fun editPayment(id: Long, oldAmount: Double, oldNote: String?) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 8, 20, 8)
        }
        val amount = EditText(this).apply {
            hint = "रकम"
            inputType = 8194
            setText(oldAmount.toString())
        }
        val note = EditText(this).apply {
            hint = "विवरण"
            setText(oldNote.orEmpty())
        }
        box.addView(amount)
        box.addView(note)

        AlertDialog.Builder(this)
            .setTitle("पेमेंट बदलें")
            .setView(box)
            .setNegativeButton("रद्द", null)
            .setPositiveButton("सेव") { _, _ ->
                val value = amount.text.toString().toDoubleOrNull() ?: 0.0
                if (value > 0) {
                    PaymentManager(db).updatePayment(
                        id, value, note.text.toString()
                    )
                    showHome()
                } else showMessage("सही रकम लिखें।")
            }
            .show()
    }

    private fun showExpense() {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 8, 20, 8)
        }
        val title = EditText(this).apply { hint = "खर्च का नाम" }
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
                    ExpenseManager(db).addExpense(title.text.toString(), value)
                    showHome()
                } else showMessage("खर्च का नाम और सही रकम लिखें।")
            }
            .show()
    }

    private fun showExpenseHistory() {
        val manager = ExpenseManager(db)
        val items = manager.getAllExpenses()

        if (items.isEmpty()) {
            showMessage("अभी कोई खर्च हिस्ट्री नहीं है।")
            return
        }

        val labels = items.map {
            "${it.title}  •  ${money(it.amount)}\n${date(it.createdAt)}"
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("खर्च हिस्ट्री")
            .setItems(labels) { _, index ->
                val item = items[index]
                AlertDialog.Builder(this)
                    .setTitle("खर्च विवरण")
                    .setMessage(
                        "नाम: ${item.title}\n" +
                        "रकम: ${money(item.amount)}\n" +
                        "तारीख: ${date(item.createdAt)}"
                    )
                    .setNeutralButton("Edit") { _, _ ->
                        editExpense(item.id, item.title, item.amount)
                    }
                    .setNegativeButton("Delete") { _, _ ->
                        confirmDelete(
                            "यह खर्च हटाएँ?",
                            "हटाने के बाद कुल खर्च और मुनाफा बदल जाएगा।"
                        ) {
                            manager.deleteExpense(item.id)
                            showExpenseHistory()
                            showHome()
                        }
                    }
                    .setPositiveButton("बंद", null)
                    .show()
            }
            .setPositiveButton("बंद", null)
            .show()
    }

    private fun editExpense(id: Long, oldTitle: String, oldAmount: Double) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 8, 20, 8)
        }
        val title = EditText(this).apply {
            hint = "खर्च का नाम"
            setText(oldTitle)
        }
        val amount = EditText(this).apply {
            hint = "रकम"
            inputType = 8194
            setText(oldAmount.toString())
        }
        box.addView(title)
        box.addView(amount)

        AlertDialog.Builder(this)
            .setTitle("खर्च बदलें")
            .setView(box)
            .setNegativeButton("रद्द", null)
            .setPositiveButton("सेव") { _, _ ->
                val value = amount.text.toString().toDoubleOrNull() ?: 0.0
                if (title.text.isNotBlank() && value > 0) {
                    ExpenseManager(db).updateExpense(
                        id, title.text.toString(), value
                    )
                    showHome()
                } else showMessage("सही नाम और रकम लिखें।")
            }
            .show()
    }

    private fun showHistory() {
        val manager = ProductionManager(db)
        val entries = manager.getAllEntries()

        if (entries.isEmpty()) {
            showMessage("अभी कोई पीस दर्ज नहीं है।")
            return
        }

        val labels = entries.map {
            "${it.name}  •  ${it.quantity} पीस × ${money(it.rate)} = ${money(it.total)}" +
                if (it.photo != null) "\n📷 फोटो जुड़ी है" else ""
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("पीस हिस्ट्री")
            .setItems(labels) { _, index ->
                showProductionDetails(entries[index])
            }
            .setPositiveButton("बंद", null)
            .show()
    }

    private fun showProductionDetails(item: ProductionEntry) {
        val manager = ProductionManager(db)

        val message =
            "नाम: ${item.name}\n" +
            "मात्रा: ${item.quantity} पीस\n" +
            "रेट: ${money(item.rate)}\n" +
            "कुल: ${money(item.total)}\n" +
            "तारीख: ${date(item.createdAt)}\n" +
            "फोटो: ${if (item.photo.isNullOrBlank()) "नहीं" else "हाँ"}"

        val actions = if (item.photo.isNullOrBlank()) {
            arrayOf("Edit", "Delete", "बंद")
        } else {
            arrayOf("Edit", "फोटो देखें", "Delete", "बंद")
        }

        AlertDialog.Builder(this)
            .setTitle("पीस का पूरा विवरण")
            .setMessage(message)
            .setItems(actions) { _, which ->
                when (actions[which]) {
                    "Edit" -> editProduction(item)
                    "फोटो देखें" -> item.photo?.let { showFullPhoto(it) }
                    "Delete" -> confirmDelete(
                        "यह पीस हटाएँ?",
                        "यह एंट्री और इससे जुड़ी फोटो हट जाएगी।"
                    ) {
                        manager.deleteEntry(item.id)
                        item.photo?.let { PhotoManager(this).deletePhoto(it) }
                        showHome()
                    }
                }
            }
            .setPositiveButton("बंद", null)
            .show()
    }

    private fun editProduction(item: ProductionEntry) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 8, 20, 8)
        }
        val name = EditText(this).apply {
            hint = "पीस का नाम"
            setText(item.name)
        }
        val quantity = EditText(this).apply {
            hint = "मात्रा"
            inputType = 2
            setText(item.quantity.toString())
        }
        val rate = EditText(this).apply {
            hint = "रेट"
            inputType = 8194
            setText(item.rate.toString())
        }

        box.addView(name)
        box.addView(quantity)
        box.addView(rate)

        AlertDialog.Builder(this)
            .setTitle("पीस की एंट्री बदलें")
            .setView(box)
            .setNegativeButton("रद्द", null)
            .setPositiveButton("सेव") { _, _ ->
                val q = quantity.text.toString().toIntOrNull() ?: 0
                val r = rate.text.toString().toDoubleOrNull() ?: -1.0

                if (name.text.isNotBlank() && q > 0 && r >= 0) {
                    ProductionManager(db).updateDetails(
                        item.id, name.text.toString(), q, r
                    )
                    showHome()
                } else showMessage("सही नाम, मात्रा और रेट भरें।")
            }
            .show()
    }

    private fun confirmDelete(
        title: String,
        message: String,
        action: () -> Unit
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton("नहीं", null)
            .setPositiveButton("हाँ, हटाएँ") { _, _ -> action() }
            .show()
    }

    private fun summaryCard(title: String, value: String): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 16, 18, 16)
            setBackgroundColor(0xFF202D42.toInt())
        }
        card.addView(text(title, 14f, MUTED))
        card.addView(text(value, 23f, WHITE, true))
        card.layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
            bottomMargin = 12
        }
        return card
    }

    private fun text(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ): TextView = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        gravity = Gravity.CENTER_VERTICAL
        if (bold) setTypeface(null, android.graphics.Typeface.BOLD)
        setPadding(4, 8, 4, 8)
    }

    private fun addButton(
        parent: LinearLayout,
        label: String,
        action: () -> Unit
    ) {
        parent.addView(Button(this).apply {
            text = label
            isAllCaps = false
            setOnClickListener { action() }
        })
    }

    private fun space(): View =
        View(this).apply {
            layoutParams = LinearLayout.LayoutParams(1, 16)
        }

    private fun money(value: Double): String =
        "₹" + String.format(Locale.getDefault(), "%.2f", value)

    private fun date(timestamp: Long): String =
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            .format(Date(timestamp))

    private fun showMessage(message: String) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("ठीक है", null)
            .show()
    }

    companion object {
        private val WHITE = 0xFFFFFFFF.toInt()
        private val MUTED = 0xFFB8C5D9.toInt()
    }
}
