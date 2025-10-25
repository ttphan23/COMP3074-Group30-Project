Upgrade project to Java 21 (manual steps)

Context
- Automated Copilot app-modernization tools are unavailable in this environment (plan restriction). I updated `app/build.gradle.kts` to set Java language compatibility to 21, but you must install and configure JDK 21 locally and verify AGP/Gradle compatibility before building.

Steps (Windows / PowerShell)

1) Install JDK 21
- Option A: AdoptOpenJDK / Eclipse Temurin (recommended)
  - Download & install JDK 21 from Eclipse Adoptium / Temurin.
- Option B: Use a package manager (chocolatey/scoop) or your preferred installer.

2) Set JAVA_HOME and update PATH (PowerShell example)

# Open an elevated PowerShell and run:
$envPath = [System.Environment]::GetEnvironmentVariable('Path', 'Machine')
[System.Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\path\to\jdk-21', 'Machine')
[System.Environment]::SetEnvironmentVariable('Path', $envPath + ';%JAVA_HOME%\\bin', 'Machine')

Replace 'C:\path\\to\\jdk-21' with the actual installation path returned by the JDK installer.

3) (Optional / Preferred) Configure Gradle Toolchain or org.gradle.java.home
- Option A (recommended): Configure Gradle to use the JDK 21 toolchain in your Gradle settings. Example (root `build.gradle.kts` or `settings.gradle.kts`):

// Example (not applied automatically):
// java {
//     toolchain {
//         languageVersion.set(org.gradle.api.JavaVersion.toVersion("21"))
//     }
// }

- Option B: Set `org.gradle.java.home` in `gradle.properties` to the JDK 21 install path:
org.gradle.java.home=C:/path/to/jdk-21

4) Verify Gradle / Android Gradle Plugin compatibility
- Check Android Gradle Plugin (AGP) version in `gradle/libs.versions.toml` (currently: `agp = "8.13.0"`).
- Verify AGP + Gradle support JDK 21. If AGP doesn't support Java 21 yet, you may need to remain on a supported Java level (e.g., 17) until AGP updates.

5) Clean and build
- From project root run (PowerShell):
./gradlew clean assembleDebug

6) Fix compile or runtime issues
- If you hit compilation errors related to language features, adjust source/target or update build plugins/dependencies.
- If Gradle/AGP rejects Java 21, revert source/target in `app/build.gradle.kts` to the supported Java version (e.g., 17) and instead configure the Gradle JVM separately if you need JDK 21 for other tools.

Notes & recommendations
- I updated `app/build.gradle.kts` to use `JavaVersion.VERSION_21`. If your installed Gradle version's `JavaVersion` enum doesn't include VERSION_21, prefer using a toolchain (JavaLanguageVersion.of(21)).
- Because the automated upgrade tools are blocked in this environment, I didn't change other build files automatically; after you install JDK 21, run a build and share errors/warnings and I can iterate further (e.g., add toolchain block, adjust Gradle wrapper, or apply source-level fixes).

If you want, I can:
- Add a Gradle toolchain block to the project (I can apply a conservative change that uses JavaLanguageVersion.of(21)).
- Help with commands to install Temurin JDK 21 on Windows.
- Attempt to run a local build here if you install JDK 21 and tell me the path (or set `org.gradle.java.home`).
