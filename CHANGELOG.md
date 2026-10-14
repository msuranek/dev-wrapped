# Changelog

All notable changes to Dev Wrapped will be documented here.

The project follows the structure of [Keep a Changelog](https://keepachangelog.com/en/1.1.0/). Version 0.1.2 is the first approved JetBrains Marketplace beta; earlier versions were development builds.

## [Unreleased]

No unreleased changes.

## [0.1.2] — 2026-10-13 Marketplace Beta

Approved and distributed through the JetBrains Marketplace Beta channel while the plugin remains hidden from regular Marketplace search.

### Added

- Period-aware commit counts for discovered Git identities.
- Automatic selection of the active configured Git author, with a most-recently-active fallback.
- Preservation of manual identity choices while switching reporting periods.
- Archetype-specific vector artwork and decorative motifs for all six Developer DNA personalities.
- A **Your Coding Superpower** achievement section with a distinct achievement for each archetype.
- Deterministic achievement descriptions that incorporate available Git activity statistics, with fallbacks for missing or low-activity data.
- Automated rendering checks for all six archetypes, long text, and the low-signal state.

### Changed

- Redesigned the configuration dialog with native IntelliJ controls, concise repository labeling, and human-readable reporting periods.
- Refined share-card typography, layout, spacing, gradients, and metric labels while keeping the 1200 × 1500 export format.
- Improved the lower portion of the card and replaced the generic DNA decoration with archetype-specific artwork.
- Updated achievement copy to avoid repeating archetype subtitles and to describe commit activity without implying measured working hours or productivity.
- Regenerated the README preview using fictional Midnight Architect sample data and the same renderer as the plugin.
- Polished the README, Marketplace copy, changelog, and release documentation for the initial public GitHub launch.

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
