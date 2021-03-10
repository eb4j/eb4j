# Change Log
All notable changes to this project will be documented in this file.

## [Unreleased]

### Changed
- Gradle: Drop version automation.
- Change project group to com.github.eb4j
- Add repository to docs subproject

## [2.1.0] - 2021-03-10

### Added
- Extension: Support unicode maps definition file that bundled by EBView application.
- Extension: Support Appendix feature that EB library extend.
- Publish to jitpack.io.
- Publish to github packages repository.

### Fixed
- Able to handle monochrome image at HONMON instead of HONMONG

### Changed
- Automation of release versioning based on git tag.
- Change version scheme to <major>.<minor>.<patchlevel>.<build>
- Use Asciidoc for javadoc comment for Japanese and English.

### Deprecated
- Drop bintray repository to publish.

## [2.0.0] - 2020-10-25
### Changed
- Released the library at bintray
- Release automation
- Refactoring gradle build structures
- Fix tests(checkstyle).
- Bump to gradle@6.1.1
- Target java version to 8.
- Add github actions workflows

## [1.99.1.dev] - 2016-10-15
### Added
- Add unit tests.

### Changed
- Introduce EBFormat enum class.
  Now deprecated EBFile.FORMAT_* integer constants. Please use EBFormat enum instead of constants.
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

[Unreleased]: https://github.com/eb4j/eb4j/compare/v2.1.0...HEAD
[2.1.0]: https://github.com/eb4j/eb4j/compare/v2.0.0...v2.1.0
[2.0.0]: https://github.com/eb4j/eb4j/compare/v1.99.1...v2.0.0
[1.99.1.dev]: https://github.com/eb4j/eb4j/compare/v1.99.0...v1.99.1
