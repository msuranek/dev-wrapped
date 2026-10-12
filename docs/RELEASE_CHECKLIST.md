# Dev Wrapped release checklist

Use this checklist for the exact release candidate that will be uploaded. Every item is mandatory unless marked **Optional**. Keep an item unchecked until it has been verified against the final ZIP.

The previous Plugin Verifier attempt did not complete because the required IDE distributions were still downloading. Plugin Verifier is therefore intentionally unchecked.

## 1. Build and automated verification

- [ ] Use JDK 21 and confirm `java -version`.
- [ ] Run `./gradlew clean test buildPlugin verifyPluginProjectConfiguration`.
- [ ] Confirm every automated test passes without skipped critical coverage.
- [ ] Run `./gradlew verifyPlugin` and review every Plugin Verifier finding.
- [ ] Confirm the release ZIP exists under `build/distributions/` and opens successfully.
- [ ] Confirm the ZIP filename, packaged `plugin.xml` version, `gradle.properties` version, and changelog version agree.
- [ ] Record the final ZIP SHA-256 checksum.

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

- [ ] Review `git status --short`, `git status --short --ignored`, and the complete staged diff.
- [ ] Confirm build outputs, IDE metadata, local configuration, logs, temporary files, and secrets are not staged.
- [ ] Scan the staged files for tokens, passwords, private keys, personal paths, and real private identities.
- [ ] Confirm `README.md`, `CHANGELOG.md`, `MARKETPLACE.md`, `LICENSE`, the preview image, and this checklist are present and internally consistent.
- [ ] Confirm the README image and all relative links resolve on GitHub.
- [ ] Confirm the MIT license text and copyright attribution are intentional.
- [ ] Confirm the GitHub Actions workflow succeeds with read-only repository permissions.
- [ ] Confirm the public source URL is `https://github.com/msuranek/dev-wrapped`.

## 5. Marketplace preparation

- [ ] Review the Marketplace description for accuracy, formatting, privacy wording, and unsupported claims.
- [ ] Confirm plugin name, ID, vendor, description, icons, version, change notes, license, and source URL.
- [ ] Confirm the declared minimum build is intentional and supported by test evidence.
- [ ] Do not claim IntelliJ IDEA, PhpStorm, or other IDE compatibility until the exact product build has passed manual testing and Plugin Verifier review.
- [ ] Store `PUBLISH_TOKEN`, `CERTIFICATE_CHAIN`, `PRIVATE_KEY`, and `PRIVATE_KEY_PASSWORD` only in an approved secret store.
- [ ] Confirm signing credentials and certificate material are absent from Git history and build artifacts.
- [ ] Prepare release notes from the matching changelog section.

## 6. Release and post-release verification

- [ ] Obtain explicit approval for publication.
- [ ] Sign the final plugin artifact if Marketplace policy requires it.
- [ ] Upload only the reviewed final ZIP; do not publish from an unreviewed local build.
- [ ] Verify the Marketplace listing, version, compatibility range, description, icon, license, and source link after processing.
- [ ] Install the published build from Marketplace into a clean supported IDE profile.
- [ ] Repeat the basic generate, PNG export, clipboard, and privacy smoke tests using the published build.
- [ ] Confirm the GitHub release, tag, ZIP checksum, and changelog all reference the same version.
- [ ] Monitor initial installation reports and document any release-blocking regressions.
