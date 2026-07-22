# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

An Xposed/LSPosed module ("不要竖屏" / Portrait2Landscape) that forces Bilibili to play vertical (portrait) videos in the traditional landscape player instead of the swipe-based "看一看" (story) player.

The entire mechanism is one trick: Bilibili routes portrait videos through `bilibili://story/...` deep links and landscape videos through `bilibili://video/...`. The module rewrites `story` URIs to `video` URIs at the data-model layer, so the app builds its normal landscape player for every video. There is no UI/Activity; the module is configured and toggled entirely from within the LSPosed manager.

Targets two packages (kept in sync across `app/src/main/java/.../hook/Main.kt`, `SCOPE`, and `res/values/strings.xml`):
- `tv.danmaku.bili` — standard (CN) Bilibili
- `com.bilibili.app.in` — international Bilibili

## Architecture

Built on **YukiHookAPI** (a Kotlin DSL over Xposed), not raw Xposed APIs.

- `hook/Main.kt` — entry point implementing `IYukiHookXposedInit`. The `@InjectYukiHookWithXposed(entryClassName = "Entry")` annotation makes KSP generate the real Xposed entry class `Entry` at build time (referenced from `assets/xposed_init`). `onHook()` calls `loadApp(...)` once per target package, attaching the same `Hook` instance.
- `hook/Hook.kt` — the actual hook logic. Targets `com.bilibili.pegasus.api.model.BasicIndexItem` and patches its `uri` field in two places:
  - `afterHook` on **all constructors** — rewrites the `uri` field after an item is built.
  - `beforeHook` on **`setUri(String)`** — rewrites the argument before it is stored.
  - Both do the same transform: if the URI starts with `bilibili://story/`, replace that prefix with `bilibili://video/`.

When Bilibili obfuscates or renames `BasicIndexItem`, the `uri` field, or `setUri` across app versions, this hook is what breaks and needs updating. Debug logging (`loggerD`, tag `Portrait2Landscape`) is gated on `BuildConfig.DEBUG`.

The KSP-generated `Entry` class does not exist in source — it only appears after a build. Don't look for it in the tree.

## Build

```bash
./gradlew assembleRelease   # release APK (signed, minified) — primary build
./gradlew assembleDebug     # debug APK with logging enabled
```

Output APK lands in `app/build/outputs/apk/<type>/` named `com.zombie12138.nobiliportrait-<versionName>_<versionCode>-<type>.apk`. `versionCode` is the git commit count and `versionName` is the git tag at HEAD (or `<base>-dev+<short hash>` when untagged), both computed in `app/build.gradle` via `git rev-list`/`git tag`, so builds must run inside the git work tree (CI must use `fetch-depth: 0`).

There are no tests, no lint config, and no CI; this is a single-purpose hook module.

### Release signing

`release` builds require four Gradle properties that are **not** committed (not in `gradle.properties`). Supply them via `~/.gradle/gradle.properties`, `-P` flags, or env vars, or the `assembleRelease` build will fail:

- `ANDROID_STORE_FILE`, `ANDROID_STORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`

`assembleDebug` uses the default debug keystore and needs none of these.

## Versioning / packaging notes

- Package was renamed to `com.zombie12138.nobiliportrait` (this fork). The upstream original used `cn.wankkoree.xp.portrait2landscape`, itself renamed from an older name to avoid Bilibili's module detection (commit `2cc2690`). The applicationId/namespace is now `com.zombie12138.nobiliportrait`; keep this consistent across `build.gradle`, the code package, and `assets/xposed_init`/`assets/yukihookapi_init`.
- `app/build.gradle` derives `versionCode` from the git commit count and `versionName` from the git tag at HEAD (or `<base>-dev+<short hash>` when untagged). Release by pushing a `v*` tag (e.g. `v3.0.0`); no manual version bump. CI must use `fetch-depth: 0` so the commit count is correct.
- Dependency repos in `settings.gradle` include the Xposed Maven (`api.xposed.info`), Sonatype (YukiHookAPI), and an Aliyun mirror.
