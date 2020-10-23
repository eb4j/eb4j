# Change Log
All notable changes to this project will be documented in this file.

## [Unreleased]

### Changed
- Refactoring gradle build structures
- Fix tests(checkstyle).
- Bump to gradle@6.1.1
- Target java version to 8.
- Add github actions workflows


## 1.99.1.dev - 2016-10-15
### Add
- Add unit tests.

### Changed
- Introduce EBFormat enum class.
  Now EBFile.FORMAT_* integer constants are deprecated. Please use EBFormat enum instead of constants.
- [#3] Build with Gradle build system.
- [#4] Change package location to io.github.eb4j from original fuku.eb4j.
- [#30] Refactoring to reduce method size smaller.
-- Introduce BookReader.BookReaderHandler class.
- [#30] Become final to all method arguments and remove adverse effect.
- Translate javadoc comments in English.

### Fixed
- [#14] Fix findbugs DM_DEFAULT_ENCODING warnings.
- [#2] Fix failure to read Gakken Gendai Shin-Kokugo Jiten + Kanjigen(@amake).

## 1.99.0.dev - 2016-06-01
### Added
- Import from eb4j-1.0.5

[Unreleased]: https://github.com/miurahr/dictzip-java/compare/v1.99.0.dev...HEAD
