package com.devwrapped.plugin

import com.devwrapped.core.GitIdentity
import com.devwrapped.core.ReportingPeriod
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import java.nio.file.Path
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent

class DevWrappedDialog(
    project: Project,
    repository: Path,
    identitiesByPeriod: Map<ReportingPeriod, List<GitIdentity>>,
    configuredIdentity: GitIdentity?,
) : DialogWrapper(project) {
    private val controller = IdentitySelectionController(identitiesByPeriod, configuredIdentity)
    private val identityBox = ComboBox<GitIdentity>()
    private val periodBox = ComboBox(ReportingPeriod.entries.toTypedArray())
    private val statusLabel = JBLabel()
    private val repositoryLabel = JBLabel(repository.fileName?.toString() ?: repository.toString(), AllIcons.Nodes.Folder, JBLabel.LEADING).apply {
        toolTipText = repository.toAbsolutePath().normalize().toString()
    }
    private var updating = false

    val selectedIdentity: GitIdentity get() = requireNotNull(identityBox.selectedItem as? GitIdentity)
    val selectedPeriod: ReportingPeriod get() = controller.currentPeriod

    init {
        title = "Generate Dev Wrapped"
        setOKButtonText("Generate My Dev Wrapped")
        identityBox.renderer = SimpleListCellRenderer.create("No active identities") { it.displayName }
        periodBox.renderer = SimpleListCellRenderer.create("") { it.displayName }
        periodBox.selectedItem = controller.currentPeriod
        refreshIdentities(controller.initialSelection())

        identityBox.addActionListener {
            if (!updating) {
                controller.markManualSelection(identityBox.selectedItem as? GitIdentity)
                updateStatus()
            }
        }
        periodBox.addActionListener {
            if (!updating) {
                val period = periodBox.selectedItem as? ReportingPeriod ?: return@addActionListener
                refreshIdentities(controller.switchPeriod(period))
            }
        }
        init()
    }

    private fun refreshIdentities(selection: GitIdentity?) {
        updating = true
        try {
            identityBox.model = DefaultComboBoxModel(controller.identities().toTypedArray())
            identityBox.selectedItem = selection
        } finally {
            updating = false
        }
        updateStatus()
    }

    private fun updateStatus() {
        val identity = identityBox.selectedItem as? GitIdentity
        if (identity == null) {
            statusLabel.icon = AllIcons.General.Warning
            statusLabel.text = "No commits found for ${controller.currentPeriod.displayName.lowercase()}. Select another identity or reporting period."
            setOKActionEnabled(false)
        } else {
            statusLabel.icon = AllIcons.General.Information
            statusLabel.text = "${identity.commitCount} ${if (identity.commitCount == 1) "commit" else "commits"} will be analyzed for ${controller.currentPeriod.displayName.lowercase()}."
            setOKActionEnabled(true)
        }
    }

    override fun createCenterPanel(): JComponent = panel {
        row("Repository:") { cell(repositoryLabel) }
        row("Reporting period:") { cell(periodBox).align(AlignX.FILL) }
        row("Git identity:") { cell(identityBox).align(AlignX.FILL) }
        row { cell(statusLabel).align(AlignX.FILL) }
        row { comment("Commit counts reflect the selected period. Share cards never include your name, email, repository, or paths.") }
    }.apply {
        border = JBUI.Borders.emptyTop(8)
        preferredSize = java.awt.Dimension(JBUI.scale(620), preferredSize.height)
    }
}
