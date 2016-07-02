# Change Log
All notable changes to this project will be documented in this file.

## [Unreleased]
### Add
- Add unit tests.

### Changed
- Introduce EBFormat enum class.
  Now EBFile.FORMAT_* integer constants are deprecated. Please use EBFormat enum instead of constants.
- [#3] Build with Gradle build system.
- [#4] Change package location to io.github.eb4j from original's fuku.eb4j.
- [#30] Refactoring to reduce method size smaller.
-- Introduce BookReader.BookReaderHandler class.
- [#30] Become final to all method's arguments and remove adverse effect.

### Fixed
- [#14] Fix findbugs DM_DEFAULT_ENCODING warnings.
- [#2] Fix failure to read Gakken Gendai Shin-Kokugo Jiten + Kanjigen(@amake).

## 1.99.0.dev - 2016-06-01
### Added
- Import from eb4j-1.0.5

[Unreleased]: https://github.com/miurahr/dictzip-java/compare/v1.99.0.dev...HEAD
