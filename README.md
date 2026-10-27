# archeryutils-kotlin

> **This is a Kotlin Multiplatform port of [jatkinson1000/archeryutils](https://github.com/jatkinson1000/archeryutils), ported and maintained by [@retbrown](https://github.com/retbrown). The original library is written in Python — all credit for the underlying algorithms, data, and design goes to the upstream authors. This port is not affiliated with or endorsed by the upstream project.**

A collection of archery utilities for Kotlin Multiplatform (Android, iOS, JVM), ported from
the Python library [archeryutils](https://github.com/jatkinson1000/archeryutils) by Jack
Atkinson et al. Designed to make the development of archery apps easier.

Contains:
- Generic representations of targets and rounds
- World Archery, Archery GB, IFAA, and Archery Australia rounds (embedded)
- Calculations for Archery GB handicaps and Archery Australia archer skill level
- Calculation of Archery GB classifications (outdoor, indoor, field, legacy indoor/field)

Synced against upstream Python v3.0.0 (2026.1 AGB classifications).

## Packages

All under `io.github.retbrown.archeryutils`:

| Package | Description |
|---|---|
| `length` | Unit conversion helpers (metres, yards, cm, inches) |
| `targets` | `Target`, `ScoringSystem`, `FaceSpec`, `Quantity` |
| `rounds` | `Pass`, `Round`, and the built-in round databases via `Rounds` |
| `handicaps` | AGB, AGBold, AA, AA2 handicap schemes; `HandicapTable` |
| `classifications` | AGB outdoor, indoor, field, old indoor and old field classifications |

## Installation

Releases are published to [GitHub Packages](https://github.com/retbrown/archeryutils-kotlin/packages).
GitHub Packages requires authentication to download Maven artifacts, even from public
repositories, using a GitHub username and a personal access token with the `read:packages` scope.

Add the repository (e.g. in `settings.gradle.kts`):

```kotlin
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/retbrown/archeryutils-kotlin")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.token").orNull ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

and depend on it from your Kotlin Multiplatform module:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.retbrown:archeryutils-kotlin:3.0.0")
        }
    }
}
```

Gradle module metadata resolves the right artifact for each target (Android, JVM, iOS). To try
unreleased changes locally run `./gradlew publishToMavenLocal` and add `mavenLocal()`.

Publishing a release: bump `VERSION_NAME` in `gradle.properties`, then push a matching `vX.Y.Z` tag;
the Release workflow publishes every target from macOS.

## Usage

```kotlin
import io.github.retbrown.archeryutils.classifications.*
import io.github.retbrown.archeryutils.handicaps.Handicaps
import io.github.retbrown.archeryutils.rounds.Rounds

val round = Rounds.waOutdoor.getValue("wa1440_90")

val classification = calculateAgbOutdoorClassification(
    1200, round, AgbBowstyle.RECURVE, AgbGender.OPEN, AgbAge.ADULT,
)

val handicap = Handicaps.handicapFromScore(1200.0, round, "AGB")
```

Custom targets and rounds:

```kotlin
val target = Target(ScoringSystem.TEN_ZONE, Quantity(40.0, "cm"), Quantity(18.0, "m"), indoor = true)
val custom = Round("My round", listOf(Pass(30, target)))
```

## Differences from the Python library

- Gender, age group and bowstyle are enums rather than strings.
- Round databases are compiled in (common code has no resource API); see
  `scripts/generate_data.py`, which regenerates them from `data/`.
- Scalar `Double` APIs, with `DoubleArray` overloads in place of numpy arrays.
- No file output for handicap tables: `HandicapTable.toCsv()` returns a string.
- No `UserWarning`s; decimal ring scores are not supported.

## Development

```sh
./gradlew jvmTest                 # JVM tests (includes large regression snapshots)
./gradlew testAndroidHostTest     # Android host tests
./gradlew iosSimulatorArm64Test   # iOS simulator tests (macOS only)
```

To resync round/classification data after an upstream change, edit the JSON in `data/` and run
`python3 scripts/generate_data.py`.

## License

MIT, as per the upstream project. See [LICENSE](LICENSE).
