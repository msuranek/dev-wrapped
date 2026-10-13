# Dev Wrapped release checklist

Use this checklist for the exact release candidate that will be uploaded. Every item is mandatory unless marked **Optional**. Keep an item unchecked until it has been verified against the final ZIP.

Audit snapshot from 2026-10-13:

- Candidate version: `0.1.2`.
- Clean build and all 31 automated tests passed with JDK 21.0.8.
- Plugin Verifier 1.410 reported **Compatible** for IntelliJ IDEA Community `IC-252.27397.103` and PhpStorm `PS-253.30387.85`.
- The verifier uses the current IntelliJ target plus a local PhpStorm installation supplied through `PHPSTORM_HOME`; it no longer resolves the broad recommended IDE set.
- The plugin had previously been manually tested in PhpStorm on macOS. The rebuilt release-candidate ZIP still needs an exact final-package smoke test, and IntelliJ IDEA manual testing remains outstanding.
- Marketplace-only fields still need to be selected during upload: MIT license, public source URL, at least one accurate tag, and the Beta custom release channel.

## 1. Build and automated verification

- [x] Use JDK 21 and confirm `java -version`.
- [x] Run `./gradlew clean test buildPlugin verifyPluginProjectConfiguration`.
- [x] Confirm every automated test passes without skipped critical coverage.
- [x] Run `PHPSTORM_HOME=/path/to/PhpStorm ./gradlew verifyPlugin` and review every Plugin Verifier finding.
- [x] Confirm the release ZIP exists under `build/distributions/` and opens successfully.
- [x] Confirm the ZIP filename, packaged `plugin.xml` version, `gradle.properties` version, and changelog version agree.
- [x] Record the final ZIP SHA-256 checksum after the last metadata rebuild: `e8e97010291effd158e32988947ce159b022230063fa71bdff82e5f61cc22628`.

## 2. Manual IDE testing

- [ ] Install the final ZIP in an exact supported IntelliJ IDEA 2025.2 build and restart the IDE.
- [ ] Install the final ZIP in an exact supported PhpStorm 2025.2 build and restart the IDE.
- [ ] Confirm **Tools → Dev Wrapped → Generate My Dev Wrapped** is visible and opens the configuration dialog.
- [ ] Confirm the repository label is concise and the full path is available only as supporting UI context.
- [ ] Test configured-author preselection and most-recently-active fallback.
- [ ] Switch between Last 30 days, This year, and All time; verify identity counts and manual selections update correctly.
- [ ] Test a repository with multiple authors and multiple author emails.
- [ ] Test an empty repository and a reporting period with no commits; confirm no zero-commit card is produced.
- [ ] Test detached HEAD and commits reachable from multiple branches.
- [ ] Cancel identity discovery and report generation; confirm the IDE remains responsive.
- [ ] Confirm the result footer shows **Copy Image**, **Save PNG…**, and **Close**, and that each action works.

## 3. Visual and platform QA

- [ ] Resize the result dialog and confirm the preview remains proportional without unnecessary scrolling or clipping.
- [ ] Save a PNG and verify that it decodes at exactly 1200 × 1500 pixels.
- [ ] Copy the card and paste it into an image-aware application such as Apple Notes; confirm it is an image, not text or a file path.
- [ ] Inspect the exported card and confirm it contains no author name, email, repository name, local path, remote URL, commit message, or source code.
- [ ] Inspect representative low-activity, missing-language, and long-archetype-title cards.
- [ ] Verify plugin icons and dialog rendering in both light and dark IDE themes.
- [ ] **Optional:** Repeat visual, PNG, and clipboard checks on Windows.
- [ ] **Optional:** Repeat visual, PNG, and clipboard checks on Linux.

## 4. GitHub and open source

- [x] Review `git status --short`, `git status --short --ignored`, and the complete staged diff (no files are staged).
- [x] Confirm build outputs, IDE metadata, local configuration, logs, temporary files, and secrets are not staged.
- [x] Scan the proposed files, Git history, and release archive for tokens, passwords, private keys, personal paths, and real private identities.
- [x] Confirm `README.md`, `CHANGELOG.md`, `MARKETPLACE.md`, `LICENSE`, the preview image, and this checklist are present and internally consistent.
- [x] Confirm the README image and all relative links resolve to tracked repository files.
- [ ] Confirm the MIT license text and copyright attribution are intentional.
- [x] Confirm the GitHub Actions workflow requests read-only repository permissions and runs tests, packaging, project configuration checks, and baseline Plugin Verifier checks.
- [x] Confirm the configured source URL is `https://github.com/msuranek/dev-wrapped`.

## 5. Marketplace preparation

- [x] Review the Marketplace description for accuracy, formatting, privacy wording, and unsupported claims.
- [x] Confirm the packaged plugin name, ID, vendor, description, icons, version, change notes, and source URL.
- [ ] In the Marketplace upload form, select the MIT license, add the public source URL, choose accurate tags, and use the Beta custom release channel.
- [x] Confirm the declared minimum build `252` is intentional and supported by Plugin Verifier evidence.
- [ ] Do not claim IntelliJ IDEA, PhpStorm, or other IDE compatibility until the exact product build has passed manual testing and Plugin Verifier review.
- [ ] Store `PUBLISH_TOKEN`, `CERTIFICATE_CHAIN`, `PRIVATE_KEY`, and `PRIVATE_KEY_PASSWORD` only in an approved secret store.
- [x] Confirm signing credentials and certificate material are absent from Git history and build artifacts.
- [x] Prepare concise Marketplace release notes matching the `0.1.2` changelog section.

## 6. Release and post-release verification

- [ ] Obtain explicit approval for publication.
- [ ] Sign the final plugin artifact if Marketplace policy requires it.
- [ ] Upload only the reviewed final ZIP; do not publish from an unreviewed local build.
- [ ] Verify the Marketplace listing, version, compatibility range, description, icon, license, and source link after processing.
- [ ] Install the published build from Marketplace into a clean supported IDE profile.
- [ ] Repeat the basic generate, PNG export, clipboard, and privacy smoke tests using the published build.
- [ ] Confirm the GitHub release, tag, ZIP checksum, and changelog all reference the same version.
- [ ] Monitor initial installation reports and document any release-blocking regressions.
