package com.devwrapped.plugin

import com.devwrapped.core.AnalysisRequest
import com.devwrapped.core.GitAnalyzer
import com.devwrapped.core.PersonalityClassifier
import com.devwrapped.render.CardRenderer
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.Messages
import java.nio.file.Path
import java.time.Instant
import java.time.ZoneId

class GenerateDevWrappedAction : AnAction(), DumbAware {
    private val analyzer = GitAnalyzer()

    override fun update(e: AnActionEvent) { e.presentation.isEnabledAndVisible = e.project != null }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val basePath = project.basePath
        if (basePath == null) {
            Messages.showWarningDialog(project, "Open a local Git project before generating Dev Wrapped.", "Dev Wrapped")
            return
        }
        val repository = Path.of(basePath)
        ProgressManager.getInstance().run(object : Task.Modal(project, "Finding Git Identities", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                val identityData = runCatching {
                    val now = Instant.now()
                    val zone = ZoneId.systemDefault()
                    val byPeriod = com.devwrapped.core.ReportingPeriod.entries.associateWith { period ->
                        indicator.text = "Finding identities for ${period.displayName.lowercase()}…"
                        analyzer.discoverIdentities(repository, period, now, zone) { indicator.checkCanceled() }
                    }
                    byPeriod to analyzer.configuredIdentity(repository)
                }
                ApplicationManager.getApplication().invokeLater {
                    identityData.onFailure { Messages.showErrorDialog(project, it.message ?: "Could not read Git history.", "Dev Wrapped") }
                    identityData.onSuccess { (byPeriod, configuredIdentity) ->
                        if (byPeriod.values.all { it.isEmpty() }) Messages.showInfoMessage(project, "This repository has no commits to analyze.", "Dev Wrapped")
                        else configureAndGenerate(project, repository, byPeriod, configuredIdentity)
                    }
                }
            }
        })
    }

    private fun configureAndGenerate(
        project: com.intellij.openapi.project.Project,
        repository: Path,
        identitiesByPeriod: Map<com.devwrapped.core.ReportingPeriod, List<com.devwrapped.core.GitIdentity>>,
        configuredIdentity: com.devwrapped.core.GitIdentity?,
    ) {
        val dialog = DevWrappedDialog(project, repository, identitiesByPeriod, configuredIdentity)
        if (!dialog.showAndGet()) return
        ProgressManager.getInstance().run(object : Task.Modal(project, "Generating Dev Wrapped", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Analyzing local Git history…"
                val result = runCatching {
                    val profile = analyzer.analyze(AnalysisRequest(repository, dialog.selectedIdentity, dialog.selectedPeriod)) { indicator.checkCanceled() }
                    indicator.text = "Rendering your Developer DNA…"
                    val personality = PersonalityClassifier.classify(profile)
                    Triple(profile, personality, CardRenderer.render(profile, personality))
                }
                ApplicationManager.getApplication().invokeLater {
                    result.onFailure { Messages.showErrorDialog(project, it.message ?: "Dev Wrapped could not be generated.", "Dev Wrapped") }
                    result.onSuccess { (profile, _, image) ->
                        if (profile.totalCommits == 0) {
                            val changeSelection = Messages.showYesNoDialog(
                                project,
                                "No commits were found for that identity and reporting period. Choose another identity or period?",
                                "No Commits Found",
                                "Change Selection",
                                "Close",
                                Messages.getWarningIcon(),
                            ) == Messages.YES
                            if (changeSelection) configureAndGenerate(project, repository, identitiesByPeriod, configuredIdentity)
                        } else {
                            ResultDialog(project, profile, image).show()
                        }
                    }
                }
            }
        })
    }
}
