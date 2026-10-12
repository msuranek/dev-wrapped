package com.devwrapped.plugin

import com.intellij.openapi.ide.CopyPasteManager
import java.awt.Image
import java.awt.datatransfer.DataFlavor
import java.awt.image.BufferedImage

/** Copies the original, full-resolution card through the IntelliJ clipboard API. */
object ClipboardImageCopier {
    fun copy(image: BufferedImage) {
        require(image.width > 0 && image.height > 0) { "The rendered card is empty." }

        val clipboard = CopyPasteManager.getInstance()
        clipboard.setContents(ImageTransferable(image))

        // On macOS the native clipboard conversion is deferred. Reading the
        // advertised image flavor back here verifies that ownership was
        // accepted before the UI reports success.
        val copied = clipboard.getContents(DataFlavor.imageFlavor) as? Image
            ?: throw IllegalStateException("macOS did not accept the card as image data.")
        if (copied.getWidth(null) != image.width || copied.getHeight(null) != image.height) {
            throw IllegalStateException("The clipboard image dimensions do not match the rendered card.")
        }
    }
}
