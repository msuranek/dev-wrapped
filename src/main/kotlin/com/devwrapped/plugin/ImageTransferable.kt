package com.devwrapped.plugin

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.image.BufferedImage

/**
 * An image-only clipboard payload. In particular, this intentionally exposes
 * neither stringFlavor nor javaFileListFlavor, so a card can never degrade to
 * a path or text representation when pasted.
 */
class ImageTransferable(private val image: BufferedImage) : Transferable {
    override fun getTransferDataFlavors(): Array<DataFlavor> = arrayOf(DataFlavor.imageFlavor)
    override fun isDataFlavorSupported(flavor: DataFlavor) = flavor == DataFlavor.imageFlavor
    override fun getTransferData(flavor: DataFlavor): BufferedImage {
        if (!isDataFlavorSupported(flavor)) throw UnsupportedFlavorException(flavor)
        return image
    }
}
