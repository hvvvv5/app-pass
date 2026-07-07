# PassGo

A professional Android password manager with encrypted local storage.

> **Status:** Milestone 4D.3 — Developer & Infrastructure Vault Templates (Complete)
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

## Architecture Decisions

### Single Unified VaultItem Model
All vault entries use one unified `VaultItem` model with a `VaultItemCategory` discriminator. There is no subclass per category — the category determines which fields are shown via metadata, not via a separate type.

### Dynamic Vault Type Engine
The metadata-driven engine generates every Add/Edit/Detail screen from `FieldDefinition` metadata at runtime. Category registration, field validation, formatting, and display are all driven by the `VaultItemCategory` enum entries.

### FieldDefinition Metadata-Driven Architecture
Each field identifier (`FieldId`) has a corresponding `FieldDefinition` sealed class implementation defining its label, input type, validation rules, formatting, parsing, and autofill hints. All category-level metadata (fields list, required fields, recommended fields, field groups) is declared declaratively in `VaultItemCategory`.

### Custom Fields Stored as Structured Database Rows
Fields that map to standard `VaultItem` columns (name, username, password, url, notes) are stored in those columns. All other fields are stored as rows in a dedicated `custom_fields` table with `(item_id, field_id, value, sort_order)`, joined via a LEFT JOIN for unified search.

### No Separate Database Table Per Category
Every category, from Google Account to SSH Key to Database, stores its data in the same `vault_items` and `custom_fields` tables. Adding a new category requires zero schema changes — only new `FieldId` enum entries, `FieldDefinition` objects, and a `VaultItemCategory` entry.

### No Category-Specific Screens
There are no per-category composables, ViewModels, or navigation routes. The `DynamicFormScreen` and `DynamicItemDetailScreen` render every category from metadata alone. Adding a new template does not require creating or modifying any UI code.

## Features

| Feature | Status |
|---|---|
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
| 34 item categories (Google, Email, Banking, Identity, Developer, Infrastructure, etc.) | ✅ |
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
| Dynamic Vault Type Engine | ✅ |
| Metadata-driven dynamic forms | ✅ |
| Metadata-driven item details | ✅ |
| Financial Vault Templates | ✅ |
| Identity & Personal Vault Templates | ✅ |
| Developer & Infrastructure Vault Templates | ✅ |
| Reusable FieldDefinition validation engine | ✅ |
| Dynamic category rendering | ✅ |
| Generic custom field architecture | ✅ |

## Screens

| Screen | Route | Description |
|---|---|---|
| Setup | `/setup` | Master password creation (first launch) |
| Unlock | `/unlock` | Password verification (subsequent launches) |
| Home | `/home` | Dashboard with stats, security status, tip |
| Vault | `/vault` | Item list with search, sort, filter, categories |
| Add Item | `/vault/add` | Dynamic metadata-driven form |
| Item Detail | `/vault/detail/{id}` | Dynamic metadata-driven detail screen |
| Edit Item | `/vault/edit/{id}` | Dynamic metadata-driven editor |
| Premium | `/premium` | Upgrade features |
| Settings | `/settings` | Theme, auto-lock, app info |

## Build

### Windows PowerShell

```powershell
.\gradlew :app:assembleDebug
.\gradlew testDebugUnitTest
.\gradlew connectedDebugAndroidTest
```

### Linux / macOS

```bash
./gradlew :app:assembleDebug
./gradlew testDebugUnitTest
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
| M4D.1 | Financial Vault Templates (Credit Card, Debit Card, Bank Account, PayPal, Wise, Stripe) | ✅ Complete |
| M4D.2 | Identity & Personal Vault Templates (Passport, National ID, Driver License, Residence Permit, Health Insurance, Social Security, Tax ID, Student ID) | ✅ Complete |
| M4D.3 | Developer & Infrastructure Vault Templates (GitHub, GitLab, Docker Hub, AWS, Azure, GCP, SSH Key, Database) | ✅ Complete |
| M4D | Advanced Search & Attachments (full-text, files, preview) | ⏳ Pending |
| M5 | Security + Polish (clipboard, audit, accessibility) | ⏳ Pending |

## Roadmap

| Feature | Status |
|---|---|
| Advanced Search & Attachments | ⏳ Pending |
| Full-text search improvements | ⏳ Pending |
| Encrypted file attachments | ⏳ Pending |
| Clipboard security (auto-clear) | ⏳ Pending |
| Security audit & hardening | ⏳ Pending |
| Backup / restore | ⏳ Pending |
| Cloud sync | ⏳ Pending |
