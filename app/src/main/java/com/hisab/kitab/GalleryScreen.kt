package com.hisab.kitab

import android.content.Context
import android.view.View
import android.widget.*
import android.graphics.BitmapFactory
import java.io.File

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
            setPadding(8, 12, 8, 18)
        })

        val photos = PhotoManager(context).allPhotos()

        if (photos.isEmpty()) {
            root.addView(TextView(context).apply {
                text = "अभी कोई फोटो उपलब्ध नहीं है।"
                textSize = 16f
            })
            return root
        }

        val grid = GridLayout(context).apply {
            columnCount = 2
            useDefaultMargins = true
        }

        photos.forEach { file ->
            val card = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(6, 6, 6, 10)
            }

            val image = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    145,
                    145
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageBitmap(
                    PhotoManager(context).loadPhoto(
                        file.absolutePath,
                        500,
                        500
                    )
                )
                setOnClickListener {
                    onPhotoClick(file.absolutePath)
                }
            }

            val name = TextView(context).apply {
                text = file.name
                textSize = 11f
                maxLines = 1
            }

            card.addView(image)
            card.addView(name)
            grid.addView(card)
        }

        root.addView(ScrollView(context).apply {
            addView(grid)
        })

        return root
    }
}
