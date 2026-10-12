# Changelog

All notable changes to Dev Wrapped will be documented here.

The project follows the structure of [Keep a Changelog](https://keepachangelog.com/en/1.1.0/). The versions below are development milestones; none is claimed here as a published JetBrains Marketplace release.

## [Unreleased]

### Changed

- Polished the README, Marketplace copy, changelog, and release checklist for the initial public GitHub launch.

## [0.1.2] — Development build

### Added

- Period-aware commit counts for discovered Git identities.
- Automatic selection of the active configured Git author, with a most-recently-active fallback.
- Preservation of manual identity choices while switching reporting periods.

### Changed

- Redesigned the configuration dialog with native IntelliJ controls, concise repository labeling, and human-readable reporting periods.

### Fixed

- Prevented generation of misleading zero-commit cards and added a direct path back to identity and period selection.

## [0.1.1] — Development build

### Changed

- Made the result preview scale proportionally to the available display area without changing export resolution.
- Routed image copying through the IntelliJ clipboard API with image-flavor verification.

### Fixed

- Corrected result-dialog action initialization so **Copy Image**, **Save PNG…**, and **Close** appear in the footer.

## [0.1.0] — Initial development build

### Added

- Local Git analysis with exact author identity and reporting-period filtering.
- Commit, active-day, streak, time-of-day, fix-keyword, and file-based language statistics.
- Six deterministic Developer DNA archetypes.
- Fixed 1200 × 1500 share-card rendering and proportional IDE preview.
- PNG export and image clipboard copying.
- Cancellable background analysis.
- Privacy-first defaults with no backend, accounts, telemetry, or uploads.
