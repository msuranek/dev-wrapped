package com.devwrapped.plugin

import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.swing.JPanel
import kotlin.math.min
import kotlin.math.roundToInt

object PreviewSizing {
    fun fit(sourceWidth: Int, sourceHeight: Int, maximumWidth: Int, maximumHeight: Int): Dimension {
        require(sourceWidth > 0 && sourceHeight > 0)
        require(maximumWidth > 0 && maximumHeight > 0)
        val scale = min(1.0, min(maximumWidth.toDouble() / sourceWidth, maximumHeight.toDouble() / sourceHeight))
        return Dimension(
            (sourceWidth * scale).roundToInt().coerceAtLeast(1),
            (sourceHeight * scale).roundToInt().coerceAtLeast(1),
        )
    }
}

/** Paints a proportional preview while retaining the original image unchanged. */
class CardPreviewPanel(
    private val image: BufferedImage,
    availableSize: Dimension,
) : JPanel() {
    init {
        preferredSize = PreviewSizing.fit(image.width, image.height, availableSize.width, availableSize.height)
        minimumSize = PreviewSizing.fit(image.width, image.height, 240, 300)
        isOpaque = false
    }

    override fun paintComponent(graphics: Graphics) {
        super.paintComponent(graphics)
        val insets = insets
        val availableWidth = (width - insets.left - insets.right).coerceAtLeast(1)
        val availableHeight = (height - insets.top - insets.bottom).coerceAtLeast(1)
        val target = PreviewSizing.fit(image.width, image.height, availableWidth, availableHeight)
        val x = insets.left + (availableWidth - target.width) / 2
        val y = insets.top + (availableHeight - target.height) / 2
        val g = graphics.create() as Graphics2D
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            g.drawImage(image, x, y, target.width, target.height, null)
        } finally {
            g.dispose()
        }
    }
}
