
package com.hisab.kitab

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.*

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GalleryScreen(
    private val context: Context,
    private val onPhotoClick: (String) -> Unit
) {

    fun show(): LinearLayout {
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 12, 12, 12)
        }

        root.addView(TextView(context).apply {
            text = "🖼️ मेरी फोटो गैलरी"
            textSize = 23f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setPadding(8, 12, 8, 18)
        })

        val photos = PhotoManager(context).allPhotos()

        root.addView(TextView(context).apply {
            text = "कुल ${photos.size} फोटो"
            textSize = 14f
            setTextColor(0xFF38BDF8.toInt())
            setPadding(8, 4, 8, 12)
        })

        if (photos.isEmpty()) {
            root.addView(TextView(context).apply {
                text = "अभी कोई फोटो उपलब्ध नहीं है।\nपीस जोड़ते समय कैमरा या गैलरी से फोटो जोड़ें।"
                textSize = 16f
                setTextColor(Color.WHITE)
                setPadding(12, 24, 12, 24)
            })
            return root
        }

        val grid = GridLayout(context).apply {
            columnCount = 2
            useDefaultMargins = true
        }

        val dateFormat = SimpleDateFormat(
            "dd MMM yyyy, hh:mm a",
            Locale.getDefault()
        )

        photos.forEach { file ->
            val card = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(7, 7, 7, 10)
                background = GradientDrawable().apply {
                    setColor(0xFF1E293B.toInt())
                    cornerRadius = 18f
                    setStroke(1, 0x44FFFFFF)
                }
            }

            val image = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(-1, 145)
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageBitmap(
                    PhotoManager(context).loadPhoto(
                        file.absolutePath, 500, 500
                    )
                )
                contentDescription = "पीस की फोटो"
                setOnClickListener {
                    onPhotoClick(file.absolutePath)
                }
            }

            val name = TextView(context).apply {
                text = file.name
                textSize = 11f
                maxLines = 1
                setTextColor(Color.WHITE)
                setPadding(4, 8, 4, 2)
            }

            val date = TextView(context).apply {
                text = dateFormat.format(Date(file.lastModified()))
                textSize = 11f
                setTextColor(0xFFCBD5E1.toInt())
                setPadding(4, 2, 4, 4)
            }

            val open = Button(context).apply {
                text = "फोटो खोलें"
                isAllCaps = false
                setOnClickListener {
                    onPhotoClick(file.absolutePath)
                }
            }

            card.addView(image)
            card.addView(name)
            card.addView(date)
            card.addView(open)

            val params = GridLayout.LayoutParams().apply {
                width = 0
                columnSpec = GridLayout.spec(
                    GridLayout.UNDEFINED, 1f
                )
                setMargins(5, 5, 5, 8)
            }

            grid.addView(card, params)
        }

        root.addView(ScrollView(context).apply {
            addView(grid)
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
        })

        return root
    }
}
