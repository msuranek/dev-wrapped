# Dev Wrapped beta testing

Thank you for testing Dev Wrapped. A focused smoke test should take about 10 minutes.

## Install the beta

1. Open **Settings → Plugins**, select the gear menu, and choose **Manage Plugin Repositories…**.
2. Add the [Dev Wrapped Beta repository](https://plugins.jetbrains.com/plugins/beta/35069):

   ```text
   https://plugins.jetbrains.com/plugins/beta/35069
   ```

3. Return to the **Marketplace** tab, search for **Dev Wrapped**, and install version 0.1.2.
4. Restart the IDE if prompted.

The beta requires a JetBrains IDE based on IntelliJ Platform 2025.2 or later and a system Git executable available on `PATH`.

## Smoke-test checklist

- [ ] Record the IDE product, exact version, operating system, and Dev Wrapped version.
- [ ] Start from a clean Marketplace installation rather than a previously installed local ZIP.
- [ ] Open a project backed by a local Git repository.
- [ ] Confirm **Tools → Dev Wrapped → Generate My Dev Wrapped** opens the configuration dialog.
- [ ] Confirm the repository label is concise and Git identities show plausible commit counts.
- [ ] Switch between Last 30 days, This year, and All time; confirm counts update and a manual identity choice is preserved when possible.
- [ ] Generate a card for a period with commits and confirm the preview is proportional and readable.
- [ ] Confirm the result footer shows **Copy Image**, **Save PNG…**, and **Close**.
- [ ] Save the PNG and confirm it is exactly 1200 × 1500 pixels.
- [ ] Copy the image and paste it into an image-aware application such as Apple Notes; confirm an image is pasted, not text or a file path.
- [ ] Confirm the card contains no author name, email, repository name, local path, remote URL, commit message, or source code.
- [ ] Try a period with no matching commits and confirm Dev Wrapped offers a useful path back to identity or period selection instead of generating a misleading zero-commit card.

## Report feedback

Use the [GitHub issue tracker](https://github.com/msuranek/dev-wrapped/issues). Include:

- IDE product and exact build number;
- operating system;
- Dev Wrapped version;
- steps to reproduce;
- expected and actual behavior;
- a screenshot only when it contains no private information.

Do not attach private repositories, Git identities, commit messages, remote URLs, local filesystem paths, source code, or access credentials. If a problem depends on repository history, describe the smallest synthetic reproduction instead.

## Current verification status

- Plugin Verifier: compatible with IntelliJ IDEA Community 2025.2.4 and PhpStorm 2025.3.2.
- Manual Marketplace installation and core flow: verified in PhpStorm 2025.3.2 on macOS.
- Manual IntelliJ IDEA testing: still needed.
