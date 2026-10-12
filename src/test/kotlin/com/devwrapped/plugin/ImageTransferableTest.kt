package com.devwrapped.plugin

import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Image
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.image.BufferedImage
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ImageTransferableTest {
    @Test fun `payload exposes only the image flavor and preserves original pixels`() {
        val card = BufferedImage(1200, 1500, BufferedImage.TYPE_INT_ARGB).apply {
            setRGB(725, 814, Color(46, 226, 214, 255).rgb)
        }
        val transferable = ImageTransferable(card)

        assertEquals(listOf(DataFlavor.imageFlavor), transferable.transferDataFlavors.toList())
        assertTrue(transferable.isDataFlavorSupported(DataFlavor.imageFlavor))
        assertFalse(transferable.isDataFlavorSupported(DataFlavor.stringFlavor))
        assertFalse(transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))

        val transferred = transferable.getTransferData(DataFlavor.imageFlavor)
        assertSame(card, transferred)
        assertEquals(1200, transferred.width)
        assertEquals(1500, transferred.height)
        assertEquals(card.getRGB(725, 814), transferred.getRGB(725, 814))
        assertFailsWith<UnsupportedFlavorException> { transferable.getTransferData(DataFlavor.stringFlavor) }
    }

    @Test fun `an in-memory clipboard receives image data rather than text or a file list`() {
        val card = BufferedImage(1200, 1500, BufferedImage.TYPE_INT_ARGB)
        val clipboard = Clipboard("dev-wrapped-test")

        clipboard.setContents(ImageTransferable(card), null)

        assertTrue(clipboard.isDataFlavorAvailable(DataFlavor.imageFlavor))
        assertFalse(clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor))
        assertFalse(clipboard.isDataFlavorAvailable(DataFlavor.javaFileListFlavor))
        val pasted = assertIs<Image>(clipboard.getData(DataFlavor.imageFlavor))
        assertEquals(1200, pasted.getWidth(null))
        assertEquals(1500, pasted.getHeight(null))
    }
}
