# PassGo

A professional Android password manager with encrypted local storage.

> **Status:** Milestone 4C — Dynamic Vault Type Engine (Complete)
> **Target:** Android 16 (API 36), APK distribution

## Architecture

Feature-first, single-module Android app built with:

- **Kotlin 2.4.0** + **Jetpack Compose** + **Material 3**
- **Hilt** for dependency injection
- **Navigation Compose** with bottom navigation + auth flow (setup/unlock)
- **Room + SQLCipher** for encrypted local storage
- **AndroidKeyStore** for hardware-backed key protection
- **EncryptedSharedPreferences** for master password hash/salt storage
- **PBKDF2-HMAC-SHA256** (600K iterations) for key derivation and password hashing
- **DataStore Preferences** for app settings persistence
- **Coroutines + Flow** for async operations

## Features

| Feature | Status |
|---|---|---|
| Encrypted local database (SQLCipher) | ✅ |
| Master password creation with strength validation | ✅ |
| Vault unlock with password verification | ✅ |
| Session management with auto-lock | ✅ |
| Theme switching (Light / Dark / System) | ✅ |
| Dashboard with vault stats and security tips | ✅ |
| App settings (theme, auto-lock, version) | ✅ |
| Logging infrastructure | ✅ |
| Error handling (AppResult) | ✅ |
| Vault item list with search, sort, filter | ✅ |
| Add/edit vault items with category & folder | ✅ |
| Password generation (crypto-strong) | ✅ |
| Password strength indicator | ✅ |
| Item detail with copy, show/hide, open URL | ✅ |
| 12 item categories (Google, Email, Banking, etc.) | ✅ |
| Android Autofill Framework service | ✅ |
| Credential matching by domain / package | ✅ |
| Save request handling (new + update existing) | ✅ |
| Multiple account autofill | ✅ |
| Locked vault protection | ✅ |
| Inline suggestions (Android 12+) | ✅ |
| Biometric authentication for autofill | ✅ |
| Accessibility (content descriptions, screen reader support) | ✅ |
| Smart collections (Recent, Favorites, Archived, Trash, Categories) | ✅ |
| Smart folders (create, rename, delete) | ✅ |
| Tags (multi-tag, create, delete, filter by multiple tags) | ✅ |
| Archive / restore items | ✅ |
| Trash (soft delete, restore, permanent delete) | ✅ |
| Folder management | ✅ |
| Custom field architecture (FieldId, FieldDefinition, custom_fields table) | ✅ |
| Unified search (VaultItem fields + custom field values) | ✅ |

## Screens

| Screen | Route | Description |
|---|---|---|
| Setup | `/setup` | Master password creation (first launch) |
| Unlock | `/unlock` | Password verification (subsequent launches) |
| Home | `/home` | Dashboard with stats, security status, tip |
| Vault | `/vault` | Item list with search, sort, filter, categories |
| Add Item | `/vault/add` | Add new password item form |
| Item Detail | `/vault/detail/{id}` | View/copy/show/open item details |
| Edit Item | `/vault/edit/{id}` | Edit existing item |
| Premium | `/premium` | Upgrade features |
| Settings | `/settings` | Theme, auto-lock, app info |

## Build

```bash
# Debug APK
./gradlew :app:assembleDebug

# Unit tests
./gradlew testDebugUnitTest

# Instrumentation tests
./gradlew connectedDebugAndroidTest
```

## Milestones

| # | Milestone | Status |
|---|---|---|
| M0 | Project Foundation | ✅ Complete |
| M1 | Core Foundation | ✅ Complete |
| M2 | Vault Core (CRUD, categories, search/sort/filter, generator, strength, detail) | ✅ Complete |
| M3A | Autofill Foundation | ✅ Complete |
| M3B | Autofill Engine | ✅ Complete |
| M3C | Autofill Polish, Compatibility & Security | ✅ Complete |
| M4A | Vault Organization (folders, tags, favorites, archive, trash, collections) | ✅ Complete |
| M4B | Architecture Refinements (FieldId, FieldDefinition, custom_fields, unified search) | ✅ Complete |
| M4C | Dynamic Vault Type Engine (auto-generated screens, metadata-driven) | ✅ Complete |
| M4D | Advanced Search & Attachments (full-text, files, preview) | ⏳ Pending |
| M5 | Security + Polish (clipboard, audit, accessibility) | ⏳ Pending |
