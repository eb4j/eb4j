# Change Log
All notable changes to this project will be documented in this file.

## [Unreleased]

### Added
- Support Unicode escape sequence for alternate gaiji map(#64)

### Fixed
- Remove slf4j-nop from distribution

### Changed
- Use Apache commons-lang3 (3.11) and commons-text(#64)
 

## [2.1.10] - 2021-8-18

### Fixed
- Fix index out of bound error for unicode map loading(#62, #63)

### Changed
- Bump versions
  - testng@7.4.0(#61)
  - asciidoctor@3.3.2(#60)
  - actions/github-script@4.0.2(#57)
  - actions/setup-java@v2(#55)

## [2.1.9] - 2021-08-18

### Changed
- Accept unicode map with compound and alternate characters(#53) 

### Deprecated
- Drop azure artifactory repository to publish(#52)

## [2.1.8] - 2021-03-15

### Changed
- Publish to Sonatype OSSRH nexus.
- Gradle: utilize gradle nexus.publish plugin

## [2.1.6] - 2021-03-13

### Changed

- Publish to Azure artifactory
- Able to publish to gitlab packages and repositories

## [2.1.4] - 2021-03-11

### Changed

- publish to github package

## [2.1.1] - 2021-03-11

### Changed
- Re-organize source tree.
  * Split eb4j-tools out to another project and remove.
  * Move to single tree project
- Gradle: Drop version automation.


## [2.1.0] - 2021-03-10

### Added
- Extension: Support unicode maps definition file that bundled by EBView application.
- Extension: Support Appendix feature that EB library extend.
- Publish to jitpack.io.
- Publish to github packages repository.

### Fixed
- Able to handle a monochrome image at HONMON instead of HONMONG

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

[Unreleased]: https://github.com/eb4j/eb4j/compare/v2.1.10...HEAD
[2.1.10]: https://github.com/eb4j/eb4j/compare/v2.1.9...v2.1.10
[2.1.9]: https://github.com/eb4j/eb4j/compare/v2.1.8...v2.1.9
[2.1.8]: https://github.com/eb4j/eb4j/compare/v2.1.6...v2.1.8
[2.1.6]: https://github.com/eb4j/eb4j/compare/v2.1.4...v2.1.6
[2.1.4]: https://github.com/eb4j/eb4j/compare/v2.1.1...v2.1.4
[2.1.1]: https://github.com/eb4j/eb4j/compare/v2.1.0...v2.1.1
[2.1.0]: https://github.com/eb4j/eb4j/compare/v2.0.0...v2.1.0
[2.0.0]: https://github.com/eb4j/eb4j/compare/v1.99.1...v2.0.0
[1.99.1.dev]: https://github.com/eb4j/eb4j/compare/v1.99.0...v1.99.1
