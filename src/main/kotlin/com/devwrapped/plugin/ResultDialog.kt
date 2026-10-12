package com.devwrapped.plugin

import com.devwrapped.core.DeveloperProfile
import com.devwrapped.render.CardRenderer
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GraphicsEnvironment
import java.awt.image.BufferedImage
import java.nio.file.Path
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JPanel

class ResultDialog(private val project: Project, private val profile: DeveloperProfile, private val image: BufferedImage) : DialogWrapper(project) {
    /*
     * These actions must be initialized before DialogWrapper.init(). That call
     * builds the footer and invokes createActions(); declaring them below the
     * init block leaves their backing fields null during footer construction.
     */
    private val saveAction = object : DialogWrapperAction("Save PNG…") {
        override fun doAction(e: java.awt.event.ActionEvent?) {
            val descriptor = FileSaverDescriptor("Save Dev Wrapped", "Save the full-resolution card as a PNG image", "png")
            val base = project.basePath?.let { LocalFileSystem.getInstance().findFileByPath(it) }
            val target = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project).save(base, "dev-wrapped.png") ?: return
            runCatching { CardRenderer.writePng(image, Path.of(target.file.path)) }
                .onSuccess { Messages.showInfoMessage(project, "Your full-resolution card was saved to ${target.file.path}", "Dev Wrapped") }
                .onFailure { Messages.showErrorDialog(project, it.message ?: "Could not save the PNG.", "Dev Wrapped") }
        }
    }

    private val copyAction = object : DialogWrapperAction("Copy Image") {
        override fun doAction(e: java.awt.event.ActionEvent?) {
            runCatching { ClipboardImageCopier.copy(image) }
                .onSuccess { Messages.showInfoMessage(project, "The card image is on your clipboard.", "Dev Wrapped") }
                .onFailure {
                    Messages.showErrorDialog(
                        project,
                        "Could not copy the card image. ${it.message ?: "The system clipboard may be unavailable."}",
                        "Dev Wrapped",
                    )
                }
        }
    }

    init {
        title = "Your Developer DNA"
        setOKButtonText("Close")
        init()
    }

    override fun createCenterPanel(): JComponent {
        val screen = GraphicsEnvironment.getLocalGraphicsEnvironment().maximumWindowBounds
        val available = Dimension(
            minOf(JBUI.scale(720), (screen.width * 0.72).toInt().coerceAtLeast(JBUI.scale(320))),
            minOf(JBUI.scale(900), (screen.height - JBUI.scale(190)).coerceAtLeast(JBUI.scale(400))),
        )
        return JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(12)
            add(CardPreviewPanel(image, available), BorderLayout.CENTER)
        }
    }

    override fun createActions(): Array<Action> = arrayOf(copyAction, saveAction, okAction)
}
