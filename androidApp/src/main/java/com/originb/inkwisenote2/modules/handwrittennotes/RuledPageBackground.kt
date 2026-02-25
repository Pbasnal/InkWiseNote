package com.originb.inkwisenote2.modules.handwrittennotes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.originb.inkwisenote2.common.ScreenUtils.pxToDp
import com.originb.inkwisenote2.config.ConfigReader
import com.originb.inkwisenote2.modules.handwrittennotes.data.PageTemplate

class RuledPageBackground(
    private val context: Context,
    private val configReader: ConfigReader,
    width: Int,
    height: Int
) {
    var pageTemplate: PageTemplate
    var pageTemplateBitmap: Bitmap?
    var templateCanvas: Canvas

    init {
        pageTemplateBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        templateCanvas = Canvas(pageTemplateBitmap!!)

        pageTemplate = loadPageTemplate()
    }

    fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (w != oldw || h != oldh) {
            pageTemplateBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            templateCanvas = Canvas(pageTemplateBitmap!!)
        }
    }

    fun drawTemplate(): Bitmap? {
        val density = context.resources.displayMetrics.density

        val canvasWidth = templateCanvas.width
        val canvasHeight = templateCanvas.height
        val lineSpacing = pxToDp(pageTemplate.lineSpacing, context)

        val color = Color.parseColor(pageTemplate.lineColor ?: "#000000")
        val linePaint = Paint().apply {
            this.color = color
            strokeWidth = pageTemplate.lineWidth * density
        }

        // Draw horizontal lines
        var y = lineSpacing
        while (y < canvasHeight) {
            templateCanvas.drawLine(0f, y.toFloat(), canvasWidth.toFloat(), y.toFloat(), linePaint)
            y += lineSpacing
        }

        return pageTemplateBitmap
    }

    private fun loadPageTemplate(): PageTemplate {
        return configReader.getAppConfig()?.getPageTemplates()?.get(PageBackgroundType.BASIC_RULED_PAGE_TEMPLATE.name)
            ?: PageTemplate()
    }
}
