# Run this JavaFX app on Mobile (Android)

This project is a desktop JavaFX app. To run it on a phone, you need to build a native mobile app. The simplest path is Android using the GluonFX (GraalVM) toolchain.

> iOS builds require macOS + Xcode; you can only build Android on Windows.

## Overview
- Toolchain: GraalVM Native Image + Android SDK via GluonFX Maven plugin
- Output: a signed debug APK you can install with `adb install`

## 1) Install prerequisites (Windows)
1. Install Android Studio (SDK + Platform Tools): https://developer.android.com/studio
   - Make sure these are installed via SDK Manager:
     - Android SDK Platform (Android 14 or 13)
     - Android SDK Platform-Tools
     - NDK + CMake (from SDK Tools tab)
2. Set environment variables (adjust paths to your machine):
   - `ANDROID_HOME` → `C:\Users\<you>\AppData\Local\Android\Sdk`
   - Add to `PATH` → `%ANDROID_HOME%\platform-tools` (for `adb`)
3. Install a GraalVM JDK 21+ distribution and the `native-image` tool.
   - Easiest: use Gluon’s GraalVM builds (or standard GraalVM) for Windows x64.
   - Verify: `java -version` shows GraalVM; `native-image --version` works.

## 2) Add GluonFX plugin to Maven (pom.xml)
Add the plugin and an Android profile. This won’t affect your desktop build.

```xml
<!-- In <build><plugins> ... -->
<plugin>
  <groupId>org.gluonhq</groupId>
  <artifactId>gluonfx-maven-plugin</artifactId>
  <version>1.0.\*<!-- use the latest 1.x --></version>
  <configuration>
    <mainClass>com.smartmedicare.Main</mainClass>
    <target>${gluon.target}</target>
  </configuration>
</plugin>
```

```xml
<!-- At bottom of pom.xml -->
<profiles>
  <profile>
    <id>android</id>
    <properties>
      <gluon.target>android</gluon.target>
    </properties>
  </profile>
</profiles>
```
```
Note: If you already have a <profiles> section, just add the <profile> inside it.
```

## 3) Build the Android APK
From the project root in PowerShell:

```powershell
# Clean and build native for Android (first run may download SDK parts)
mvn -Pandroid -DskipTests gluonfx:build

# Package to produce an .apk artifact
mvn -Pandroid -DskipTests gluonfx:package
```

When it finishes, look for the APK under `target\gluonfx\**\*.apk`. The exact folder varies by plugin version and CPU arch (e.g., `aarch64-android`). The build log will print the final APK path.

## 4) Install the APK on your phone
1. Enable Developer Options on your Android phone and turn on USB Debugging.
2. Connect via USB and verify the device is detected:

```powershell
adb devices
```

3. Install the APK (replace the path with the one printed by Maven):

```powershell
adb install -r ".\target\gluonfx\aarch64-android\**\smart-medicare.apk"
```

If you see `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, uninstall the old debug build first:

```powershell
adb uninstall com.smartmedicare.smartmedicare
```

### If `adb` is not recognized
This means Android Platform-Tools are not on your PATH. Fix options:

- If you have Android Studio:
  1) Install Platform-Tools via SDK Manager.
  2) In a new PowerShell window, run:

  ```powershell
  $env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
  $env:Path += ";$env:ANDROID_HOME\platform-tools"
  adb version
  ```

- If you downloaded the standalone Platform-Tools ZIP to `C:\Android\platform-tools`:

  ```powershell
  $env:Path += ";C:\Android\platform-tools"
  adb version
  ```

Tip: To make PATH permanent, use Windows System Properties > Environment Variables. Avoid `setx` if your PATH is already long (it may truncate).

### Robust way to find and install the APK
PowerShell may not expand `**` inside quotes. Use this to locate the built APK and install it:

```powershell
$apk = Get-ChildItem -Path .\target\gluonfx -Filter *.apk -Recurse | Select-Object -First 1 -Expand FullName
if (-not $apk) { Write-Error "APK not found. Run: mvn -Pandroid gluonfx:build; mvn -Pandroid gluonfx:package"; break }
adb install -r "$apk"
```

## 5) Release build (Play Store)
- Create a release-keystore and configure signing in the GluonFX plugin section.
- Build a release artifact/profile and generate an AAB if required by Play Console.
- This involves extra steps (keystore, versioning, shrinker). Do it after you validate debug builds.

## 6) Common issues
- `adb` not found: Ensure `%ANDROID_HOME%\platform-tools` is on `PATH` and a new PowerShell is opened.
- `native-image` not found: Ensure you installed GraalVM and `native-image` component, and it is the active JDK (`java -version`).
- NDK/CMake missing: Use Android Studio > SDK Manager > SDK Tools to install them.
- Build too slow: First-time native builds download toolchains and can take a while.

## 7) iOS note
- Requires a Mac with Xcode. On macOS, use the same plugin with a `-Pios` profile and Xcode toolchain installed.

---
If you want, I can wire the GluonFX plugin into your `pom.xml` for you and run a first Android build. Let me know and I’ll set it up. 