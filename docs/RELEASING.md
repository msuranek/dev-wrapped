# Releasing Dev Wrapped

This document describes the repeatable release process for maintainers. Release-specific notes, temporary approval details, checksums, and test coordination belong in the relevant GitHub issue or release draft rather than in this file.

## Prerequisites

- JDK 21 and Git are available locally.
- The intended version is set in `gradle.properties` and documented in `CHANGELOG.md`.
- Marketplace publishing and signing credentials are supplied through an approved local or CI secret store and are never committed or printed.
- The working tree contains only reviewed release changes.

## Build and automated verification

Run the release checks with JDK 21:

```bash
./gradlew clean test buildPlugin verifyPluginProjectConfiguration
```

Run Plugin Verifier against the current IntelliJ IDEA target:

```bash
./gradlew verifyPlugin
```

To include an existing PhpStorm installation without downloading another IDE distribution:

```bash
PHPSTORM_HOME=/path/to/PhpStorm ./gradlew verifyPlugin
```

Review every verifier finding. Do not suppress compatibility, deprecated API, or internal API problems merely to obtain a passing result.

## Inspect the distribution

The installable archive is generated under `build/distributions/`. Before upload, confirm that:

- the ZIP filename and packaged plugin version match `gradle.properties` and `CHANGELOG.md`;
- `META-INF/plugin.xml` contains the intended plugin ID, compatibility range, description, vendor, source URL, and change notes;
- both light and dark SVG plugin icons are packaged;
- the archive contains no tests, build caches, logs, local configuration, credentials, signing material, or machine-specific paths;
- `git diff --check` succeeds.

Record the checksum of the exact uploaded archive in the release draft or release issue.

## Manual IDE testing

Install the exact release ZIP in supported IntelliJ IDEA and PhpStorm builds. Verify at least:

- **Tools → Dev Wrapped → Generate My Dev Wrapped** is available;
- repository, identity, and reporting-period controls behave correctly;
- manual identity selection survives period changes when the identity remains available;
- periods with no matching commits do not generate a misleading report;
- the card preview remains proportional when the dialog is resized;
- **Copy Image** pastes an image into an image-aware application;
- **Save PNG…** exports a readable 1200 × 1500 image;
- the generated card contains no identity, repository, path, remote URL, commit message, or source code;
- light and dark IDE themes remain usable.

Use [BETA_TESTING.md](BETA_TESTING.md) when coordinating external beta feedback.

## Marketplace preparation

Before submitting a release, review:

- plugin name, version, description, change notes, icon, vendor, and source URL;
- MIT license and privacy information;
- supported IDE range backed by Plugin Verifier evidence;
- accurate Marketplace tags;
- the intended release channel and visibility;
- any Marketplace compatibility or API warnings.

Use a custom Beta channel for beta builds and the default channel for Stable releases. Treat plugin visibility separately from the release channel, and make a plugin publicly discoverable only after the release is ready.

## Publish and verify

Publishing requires an explicit maintainer decision. After Marketplace processing:

- verify the displayed version, compatibility, description, license, links, icon, and release channel;
- install the Marketplace-delivered build in a clean IDE profile;
- repeat the generation, PNG export, clipboard, and privacy smoke tests;
- ensure Beta-channel users have a documented path to Stable updates;
- monitor initial issue reports for release-blocking regressions.

Git commits, tags, pushes, GitHub releases, and Marketplace publication are performed manually by the maintainer.
