# PROJECT_MAP — PassGo

> **Last Updated:** 2026-07-07  
> **Status:** Milestone 4B — Architecture Refinements (Complete)  
> **Target Platform:** Android 16 (API 36)

---

## TECH_STACK

| Layer | Technology | Version | Rationale |
|---|---|---|---|
| Language | Kotlin | **2.4.0** | Latest stable |
| Build System | Gradle | **9.6.1** | Latest stable; config cache improvements |
| Android Gradle Plugin | AGP | **9.2.0** | Latest stable; requires Gradle 9.4.1+ |
| UI | Jetpack Compose + Material 3 | **BOM 2026.06.00** (Compose 1.11.3, Material3 1.4.0) | Modern declarative UI |
| Navigation | Navigation Compose | **2.9.8** | Official Jetpack navigation for Compose |
| DI | Hilt | **2.60** | Compile-time safety |
| Async | Kotlin Coroutines + Flow | Built into Kotlin 2.4.0 | Structured concurrency |
| Local DB | Room + SQLCipher | **Room 2.8.4** + **SQLCipher 4.16.0** | Encrypted on-device storage |
| Key Storage | AndroidKeyStore | **API 26+** | Hardware-backed AES-256-GCM |
| Key Derivation | PBKDF2-HMAC-SHA256 | **600K iterations** | NIST-recommended strength |
| Logging | Custom lightweight async logger | N/A | INFO/WARN/ERROR; non-blocking |
| Serialization | Kotlinx Serialization | **1.8.1** | Reserved for export/import |

### Reserved for Future Milestones

| Technology | Milestone | Purpose |
|---|---|---|

---

## ARCHITECTURE

### Feature-First Package Structure

```
com.passgo.app/
 ├── PassGoApplication.kt   # @HiltAndroidApp entry point
 ├── MainActivity.kt         # Single Activity, Compose entry
 ├── core/
│   ├── database/
│   │   ├── dao/            # VaultDao, VaultItemDao, FolderDao, TagDao, AttachmentDao, CustomFieldDao
│   │   ├── entity/         # VaultEntity, VaultItemEntity, FolderEntity, TagEntity, TagItemCrossRef, AttachmentEntity, CustomFieldEntity
 │   │   ├── DatabaseMigrations.kt
 │   │   └── PassGoDatabase.kt
 │   ├── error/              # AppResult, AppException sealed types
 │   ├── logging/            # PassGoLogger (async channel)
 │   ├── model/              # Domain models: Vault, VaultItem, Folder, Tag, Attachment, SyncStatus, CustomField, FieldId, FieldDefinition
 │   ├── navigation/         # PassGoNavHost + Screen sealed class + auth routing
 │   ├── security/           # KeyDerivation, KeyStoreManager, MasterKeyManager, PasswordHasher, PasswordValidator, MasterPasswordStore
 │   └── ui/theme/           # Material 3 theme (Color, Type, Theme) with ThemeMode support
 ├── data/
 │   ├── mapper/             # Entity-to-domain mapping
 │   ├── repository/         # Repository interfaces + implementations
 │   ├── session/            # SessionManager (LOCKED/UNLOCKED/SETUP_REQUIRED, auto-lock)
 │   └── settings/           # ThemeMode enum, UserPreferences (DataStore)
 ├── di/                     # AppModule, DatabaseModule
 └── feature/
     ├── home/               # HomeScreen (dashboard with stats, security status, tip card)
     ├── vault/              # VaultScreen (items list with CRUD), Add/Edit, ItemDetail
      ├── autofill/            # PassGoAutofillService + session/parser/matcher/dataset/domain/repository
      │   ├── service/        # PassGoAutofillService (@AndroidEntryPoint)
      │   ├── session/        # AutofillSession lifecycle manager
      │   ├── parser/         # RequestParser (AssistStructure -> AutofillRequest)
      │   ├── matcher/        # FieldMatcher + CredentialMatcher (field + domain matching)
      │   ├── dataset/        # DatasetBuilder (Autofill Dataset + SaveInfo)
      │   ├── response/       # ResponseBuilder (FillResponse construction)
      │   ├── domain/         # DomainHandler (website/package name normalization)
      │   ├── repository/     # AutofillRepository (vault integration)
      │   └── model/          # AutofillField, AutofillRequest, AutofillCredential, SessionState
     ├── premium/            # PremiumScreen (upgrade placeholder)
     ├── settings/           # SettingsScreen (theme, auto-lock, version)
     ├── setup/              # SetupScreen (master password creation)
     └── unlock/             # UnlockScreen (password verification)
```

### Key Principles

- **Feature-first**: Packages organized by feature, not by layer. Each feature is self-contained.
- **No micro-files**: Files are kept to a reasonable size; no unnecessary splitting.
- **Minimal abstraction**: Interfaces and base classes are introduced only when real reuse is proven.
- **Single module (`:app`)**: No multi-module until genuine decoupling is required.
- **Single Activity**: Navigation composed via `NavHost` + `NavigationBar` (bottom tabs).

---

## ENCRYPTION ARCHITECTURE

```
┌─────────────────────────────────────────────────────────────┐
│                   Android KeyStore                          │
│           (Hardware-backed AES-256-GCM key)                 │
└──────────────────────┬──────────────────────────────────────┘
                       │ wraps
┌──────────────────────▼──────────────────────────────────────┐
│               Database Master Key (256-bit)                 │
│         Generated via SecureRandom, encrypted at rest       │
└──────────────────────┬──────────────────────────────────────┘
                       │ used as passphrase
┌──────────────────────▼──────────────────────────────────────┐
│              SQLCipher Encrypted Database                   │
│               (AES-256-CBC / PBKDF2 key)                    │
└─────────────────────────────────────────────────────────────┘
```

Master password verification path:
```
Password → PasswordValidator (strength + rules) → PasswordHasher.hashPassword()
         → PBKDF2-HMAC-SHA256 (600K iterations, 32-byte salt) → 256-bit hash
         → MasterPasswordStore (EncryptedSharedPreferences, AES256_GCM)
```

Password-based unlock path:
```
Password → PasswordHasher.verifyPassword() → PBKDF2-HMAC-SHA256 (600K iterations)
         → compare hash → SessionManager.unlock()
```

Database unlock path:
```
App → MasterKeyManager.getOrCreateMasterKey() → KeyStoreManager (KeyStore AES key)
    → Master Key (256-bit random) → PassGoDatabase (SQLCipher passphrase) → DB ready
```

---

## MODULES

| Package | Responsibility | Key Files |
|---|---|---|
| `core.navigation` | Bottom nav, route definitions, auth routing | `PassGoNavHost.kt` (Screen sealed class + NavHost + session-aware start) |
| `core.ui.theme` | Material 3 theme with ThemeMode support | `Color.kt`, `Type.kt`, `Theme.kt` |
| `core.logging` | Async channel-based logger | `PassGoLogger.kt`, `LogLevel.kt` |
| `core.error` | Error handling types | `AppResult.kt` (Success / Error sealed class) |
| `core.security` | Key derivation, storage, master key lifecycle, password hashing/validation | `KeyDerivation.kt`, `KeyStoreManager.kt`, `MasterKeyManager.kt`, `PasswordHasher.kt`, `PasswordValidator.kt`, `MasterPasswordStore.kt` |
| `core.model` | Domain models | `Vault.kt`, `VaultItem.kt`, `Folder.kt`, `Tag.kt`, `Attachment.kt`, `SyncStatus.kt`, `CustomField.kt`, `FieldId.kt`, `FieldDefinition.kt` |
| `core.database` | Room entities, DAOs, database class | `PassGoDatabase.kt`, `DatabaseMigrations.kt`, `dao/*` (6 DAOs), `entity/*` (7 entities) |
| `data.mapper` | Entity-to-domain mapping | `Mappers.kt` |
| `data.repository` | Repository interfaces + implementations | `VaultRepository.kt`, `VaultRepositoryImpl.kt`, etc. |
| `data.session` | Session state, auto-lock timeout | `SessionManager.kt` |
| `data.settings` | Theme mode, auto-lock, language, security tips | `ThemeMode.kt`, `UserPreferences.kt` (DataStore) |
| `feature.home` | Dashboard with stats, vault status, security tip, FAB | `HomeScreen.kt`, `HomeViewModel.kt` |
| `feature.vault` | Vault item list, add/edit form, item detail | `VaultScreen.kt`, `VaultViewModel.kt`, `AddEditItemScreen.kt`, `AddEditItemViewModel.kt`, `ItemDetailScreen.kt`, `ItemDetailViewModel.kt` |
| `feature.autofill` | Android Autofill Framework service, session, parser, field matcher, credential matcher, dataset builder, domain handler, repository, response builder | `PassGoAutofillService.kt`, `AutofillSession.kt`, `RequestParser.kt`, `FieldMatcher.kt`, `CredentialMatcher.kt`, `DatasetBuilder.kt`, `ResponseBuilder.kt`, `DomainHandler.kt`, `AutofillRepository.kt`, `AutofillField.kt`, `AutofillRequest.kt`, `AutofillCredential.kt`, `SessionState.kt` |
| `feature.premium` | Premium upgrade (placeholder) | `PremiumScreen.kt` |
| `feature.settings` | Theme selection, auto-lock timer, app version | `SettingsScreen.kt`, `SettingsViewModel.kt` |
| `feature.setup` | Master password creation | `SetupScreen.kt`, `SetupViewModel.kt` |
| `feature.unlock` | Password verification and vault unlock | `UnlockScreen.kt`, `UnlockViewModel.kt` |
| `di` | Hilt modules | `AppModule.kt` (logger, session), `DatabaseModule.kt` (DB + DAOs) |

---

## DATABASE SCHEMA

### Tables

| Table | Columns | Foreign Keys |
|---|---|---|
| `vaults` | id, name, description, created_at, updated_at, deleted_at, sync_version, sync_status | — |
| `vault_items` | id, vault_id, folder_id, type, name, username, password, url, notes, favorite, archived_at, created_at, updated_at, deleted_at, sync_version, sync_status | vault_id→vaults, folder_id→folders |
| `folders` | id, vault_id, name, icon, parent_id, sort_order, created_at, updated_at, deleted_at, sync_version, sync_status | vault_id→vaults, parent_id→folders |
| `tags` | id, vault_id, name, color, created_at, updated_at, deleted_at, sync_version, sync_status | vault_id→vaults |
| `tag_item` | tag_id, item_id | tag_id→tags, item_id→vault_items |
| `attachments` | id, item_id, name, mime_type, encrypted_file_uri, size_bytes, created_at, updated_at, deleted_at, sync_version, sync_status | item_id→vault_items |
| `custom_fields` | id, item_id, field_id, field_value, sort_order | item_id→vault_items (CASCADE) |

All tables support: UUID primary keys, soft delete (deleted_at), sync metadata (sync_version, sync_status).

---

## SYSTEM_FLOW

```
App Launch
    │
    ▼
┌────────────────────────────────────────────────┐
│  SessionManager.sessionState (Flow)            │
│  ├── SETUP_REQUIRED → startDestination="setup" │
│  ├── LOCKED         → startDestination="unlock"│
│  └── UNLOCKED       → startDestination="home"  │
└────────────────────┬───────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────┐
│  PassGoNavHost                           │
│  ├── /setup      → SetupScreen           │
│  │   (onComplete → /home, pop setup)     │
│  ├── /unlock     → UnlockScreen          │
│  │   (onUnlocked → /home, pop unlock)    │
│  ├── BottomNavigation (4 tabs, hidden    │
│  │   on auth screens)                    │
│  │   ├── /home     → HomeScreen          │
│  │   ├── /vault    → VaultScreen         │
│  │   ├── /premium  → PremiumScreen       │
│  │   └── /settings → SettingsScreen      │
│  └── NavHost with nested navigation      │
└──────────────────────────────────────────┘
```

Database initialization:
```
AppModule (provides logger, session) → DatabaseModule
DatabaseModule → MasterKeyManager → KeyStoreManager (KeyStore AES key)
                                  → KeyDerivation (PBKDF2)
                                  → MasterKey (256-bit random)
                                  → PassGoDatabase (SQLCipher)
                                  → DAOs
                                  → Repositories
```

---

## ERROR_HANDLING

```kotlin
sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val exception: AppException) : AppResult<Nothing>()
}

sealed class AppException : Exception {
    class NetworkException(message: String, cause: Throwable? = null)
    class DatabaseException(message: String, cause: Throwable? = null)
    class AuthenticationException(message: String, cause: Throwable? = null)
    class UnknownException(message: String, cause: Throwable? = null)
    companion object {
        fun fromThrowable(throwable: Throwable): AppException
    }
}
```

Used as a return type for all repository operations. Callers pattern-match to handle success vs. error.

---

## TESTING_STRATEGY

| Layer | Tool | What to Test |
|---|---|---|
| **Unit** — Repository | JUnit 5 + MockK + Turbine | Data mapping; error wrapping |
| **Unit** — Security | JUnit 5 | Key derivation, salt generation, password clearing |
| **Instrumentation** — DAO | AndroidJUnit4 + Room in-memory | CRUD operations, queries, soft delete, search |
| **Instrumentation** — Database | AndroidJUnit4 | Schema creation, migration |
| **Instrumentation** — Compose UI | Compose UI Test + Hilt | Screen rendering; navigation |

### Commands

```bash
./gradlew testDebugUnitTest           # Unit tests
./gradlew connectedDebugAndroidTest   # Instrumentation tests
./gradlew assembleDebug               # Build APK
```

---

## MILESTONE PLAN

| # | Milestone | Verifiable Outcome |
|---|---|---|
| M0 | **Foundation** (✅ Done) | `gradlew assembleDebug` succeeds; 4 placeholder screens render |
| M1 | **Database + Cryptography** (✅ Done) | Room + SQLCipher DB; KeyStore key protection; repository layer; tests pass |
| M2 | **Vault Core** (✅ Done) | CRUD, 12 categories, search/sort/filter, password generator, strength indicator, item detail with copy/show/open |
| M3A | **Autofill Foundation** (✅ Done) | AutofillService registered, session lifecycle, field detection, domain handling, response builder, all compile |
| M3B | **Autofill Engine** (✅ Done) | Vault integration, credential matching, dataset filling, save request handling, auth-aware responses |
| M3C | **Autofill Polish, Compatibility & Security** (✅ Done) | Biometric auth, inline suggestions, UX polish, accessibility, compatibility, security hardening, error recovery |
| M4A | **Vault Organization** (✅ Done) | Smart folders, tags, favorites, archive, trash, smart collections |
| M4B | **Architecture Refinements** (✅ Done) | FieldId enum, FieldDefinition sealed class, CustomField entity/DAO/repository, custom_fields table, unified search via LEFT JOIN, migration 3→4, unit tests |
| M4C | **Advanced Search & Attachments** (⏳ Pending) | Full-text search, file attachments, preview |
| M5 | **Security + Polish** (⏳ Pending) | Auto-clear clipboard, security audit, accessibility, crash reporting |

---

## IMPLEMENTATION_STATUS

### Milestone 0 — Foundation (✅ Complete)

- [x] Gradle wrapper 9.6.1 + AGP 9.2.0 + Kotlin 2.4.0
- [x] Version catalog with Compose, Hilt, Navigation
- [x] Hilt DI: `@HiltAndroidApp`, `@AndroidEntryPoint`, `AppModule`
- [x] Material 3 theme with dynamic color support
- [x] Bottom Navigation with 4 tabs (Home, Vault, Premium, Settings)
- [x] Placeholder screens for all 4 tabs
- [x] Async logging infrastructure (`PassGoLogger`)
- [x] Error handling infrastructure (`AppResult` sealed class)
- [x] Launcher icon (adaptive icon)
- [x] `.gitignore`, `README.md`, `PROJECT_MAP.md`
- [x] `assembleDebug` builds with zero errors, zero warnings

### Milestone 1 — Core Foundation (✅ Complete)

#### 1. Local Database Foundation
- [x] `build.gradle.kts` — Room + SQLCipher + KSP schema export config
- [x] **Domain models**: `Vault.kt`, `VaultItem.kt` (8 item types), `Folder.kt`, `Tag.kt`, `Attachment.kt`, `SyncStatus.kt`
- [x] **Room entities**: `VaultEntity`, `VaultItemEntity`, `FolderEntity`, `TagEntity`, `TagItemCrossRef`, `AttachmentEntity` — foreign keys, indices, cascade deletes
- [x] **DAOs**: `VaultDao`, `VaultItemDao`, `FolderDao`, `TagDao`, `AttachmentDao` — all Flow-based; `VaultItemDao.getActiveItemsCount()` added for dashboard
- [x] **PassGoDatabase**: SQLCipher `SupportOpenHelperFactory`, schema exported
- [x] **Hilt module**: `DatabaseModule.kt` — provides database (via MasterKeyManager), all 5 DAOs
- [x] **Mappers**: `Mappers.kt` — entity↔domain bidirectional mapping
- [x] **Repositories**: `VaultRepository`, `VaultItemRepository`, `FolderRepository`, `TagRepository`, `AttachmentRepository` — interfaces + impls using `AppResult`

#### 2. Encryption Infrastructure
- [x] `KeyDerivation.kt`: PBKDF2-HMAC-SHA256, 600K iterations, 32-byte salt, 256-bit key
- [x] `KeyStoreManager.kt`: AndroidKeyStore AES-256-GCM wrapper, hardware-backed
- [x] `MasterKeyManager.kt`: Key lifecycle, cache, rotation preparation
- [x] `PasswordHasher.kt`: PBKDF2-HMAC-SHA256 password hashing + verification + zero-fill
- [x] `PasswordValidator.kt`: Password strength rules (min 8, max 128, uppercase, lowercase, digit, special), 5 strength levels
- [x] `MasterPasswordStore.kt`: EncryptedSharedPreferences (AES256_SIV key, AES256_GCM value) via Android Jetpack Security `MasterKey.Builder`

#### 3. Master Password Flow
- [x] `SetupScreen` + `SetupViewModel`: Create master password with validation, strength indicator, confirm match, hash storage, session unlock
- [x] `UnlockScreen` + `UnlockViewModel`: Verify password against stored hash/salt, error display, session unlock
- [x] `SessionManager`: Session state (SETUP_REQUIRED/LOCKED/UNLOCKED), auto-lock timer, initial state from `MasterPasswordStore`

#### 4. Application Settings
- [x] `ThemeMode.kt`: SYSTEM / LIGHT / DARK enum
- [x] `UserPreferences.kt`: DataStore Preferences — theme mode, auto-lock seconds, language code, security tips toggle
- [x] Reactive theme via `collectAsState` in `MainActivity`

#### 5. Home Dashboard
- [x] `HomeScreen` + `HomeViewModel`: Total items count, vault status (locked/unlocked), security status card, daily security tip card (toggleable), FloatingActionButton
- [x] Uses `VaultItemDao.getActiveItemsCount()` and `SessionManager.isUnlocked()`

#### 6. Settings Screen
- [x] `SettingsScreen` + `SettingsViewModel`: Theme selection (Light/Dark/System), auto-lock timer (1/5/10/30 min), app version display

#### 7. Navigation
- [x] `PassGoNavHost`: Setup + Unlock routes; `startDestination` based on `sessionState`; bottom bar hidden on auth screens; unlock/setup → home pop with inclusive clear
- [x] `Screen` sealed class updated with `Setup` + `Unlock`

#### 8. Logging Integration
- [x] Every new feature integrates with `PassGoLogger` (tag, message convention)
- [x] SessionManager, SetupViewModel, UnlockViewModel — all log key events

#### 9. Error Handling
- [x] Encryption errors caught with try/catch and logged (no AppResult in auth path — UI-driven error state)
- [x] All existing repository layer continues using `AppResult` architecture

#### 10. Tests
- [x] `KeyDerivationTest` (unit, 6 tests) passes
- [x] Tests compile and pass with `testDebugUnitTest`

#### Build Verification
- [x] `assembleDebug` — zero errors, zero warnings
- [x] `testDebugUnitTest` — passes (6/6)
- [x] Gradle sync succeeds
- [x] No deprecated API usage in new code
- [x] `error_prone_annotations` dependency added for Hilt 2.60 + KSP 2.3.9 compat

### Milestone 2 — Vault Core (✅ Complete)

#### Data Layer
- [x] `VaultItemCategory` enum (12 categories with display names)
- [x] `VaultItem` model: replaced `ItemType` with `VaultItemCategory`, added `email` field
- [x] `VaultItemEntity`: added `email` column
- [x] `DatabaseMigrations`: 1→2 migration adds `email` column
- [x] `PassGoDatabase`: version bumped to 2 with migration registered
- [x] `VaultItemDao`: 16 query methods — search by text/type/folder/favorites, sorted by name/newest/favorite
- [x] `Mappers.kt`: updated for VaultItemCategory + email mapping
- [x] `VaultItemRepository` + impl: getById, searchByType/Favorites/Folder, sorted queries
- [x] `Hilt DI`: `DatabaseModule` with `@Binds` for all 5 repository interfaces

#### Feature — Vault Screen
- [x] `VaultScreen` + `VaultViewModel`: search text, sort dropdown (RECENT/NAME/FAVORITE), category filter chips (first 6), folder filter chips, favorites toggle, empty state with contextual messages, delete confirmation dialog, FAB
- [x] Reactive multi-filter: `combine(5 flows).flatMapLatest` → 9 query combinations
- [x] `ItemCard` composable: category icon, name, username, category label, favorite star

#### Feature — Add/Edit Form
- [x] `AddEditItemScreen` + `AddEditItemViewModel`: full form with name, username, email, password, website, notes, category dropdown, folder dropdown, favorite checkbox
- [x] Input validation: name and password required, errors displayed inline
- [x] Password generator integration: 20-char default with uppercase, lowercase, digits, symbols, no ambiguous
- [x] Password strength indicator: reactive bar with label
- [x] Password visibility toggle, save/update with loading spinner

#### Feature — Item Detail
- [x] `ItemDetailScreen` + `ItemDetailViewModel`: displays all fields with copy buttons for username, email, password, website
- [x] Password show/hide toggle
- [x] Open website via `ACTION_VIEW` intent with safe URL handling
- [x] Edit/delete actions with delete confirmation dialog
- [x] Snackbar feedback on copy actions

#### Core Components
- [x] `PasswordGenerator`: `SecureRandom` with configurable options, `CharArray` zero-fill
- [x] `PasswordStrengthIndicator`: `LinearProgressIndicator` colored by strength level
- [x] `PasswordStrengthSuggestions`: actionable improvement text per `ValidationError`

#### Navigation
- [x] Routes: `vault/add`, `vault/detail/{itemId}`, `vault/edit/{itemId}`
- [x] Home FAB navigates to add item
- [x] Vault item click → detail, edit button → edit form

#### Build Verification
- [x] `assembleDebug` — zero errors, zero warnings
- [x] `testDebugUnitTest` — passes
- [x] Gradle sync succeeds
- [x] No deprecated API usage
- [x] No TODO/FIXME/HACK in new code
- [x] AppResult consistently checked in all ViewModels
- [x] Sensitive info never logged
- [x] URL open handles invalid protocols safely

### Milestone 3A — Autofill Foundation (✅ Complete)

#### Service Registration
- [x] `PassGoAutofillService` with `@AndroidEntryPoint` (Hilt DI)
- [x] Manifest: `BIND_AUTOFILL_SERVICE` permission, service with `<intent-filter>`, `<meta-data>` config
- [x] XML config: `res/xml/autofill_service_config.xml`
- [x] Service visible in Android Autofill settings when installed

#### Session Architecture
- [x] `AutofillSession`: lifecycle manager with `SessionState` machine (CREATED → PARSING → PARSED → RESPONDING → RESPONDED → FINISHED / CANCELLED)
- [x] New session per request via `Provider<AutofillSession>`
- [x] Per-request cancellation via `CancellationSignal`
- [x] Save request handler (infrastructure only, no save logic yet)

#### Request Parsing
- [x] `RequestParser`: parses `AssistStructure` into `AutofillRequest`
- [x] Window-by-window traversal of view hierarchy
- [x] AutofillId extraction, focused field detection
- [x] Package name extraction from `ComponentName`

#### Field Detection
- [x] `FieldMatcher`: classifies fields as USERNAME / EMAIL / PASSWORD / UNKNOWN
- [x] Detection via `inputType` variations (PASSWORD, EMAIL_ADDRESS, PERSON_NAME)
- [x] Detection via `autofillHints` (username, email, password, etc.)
- [x] Partial-match fallback for compound hints
- [x] `hasLoginFields()`: requires at least 1 password + 1 username/email

#### Domain Recognition
- [x] `DomainHandler`: URL domain extraction via `URI`
- [x] Known app package → domain mapping (Gmail, Facebook, Twitter, LinkedIn, Slack, Outlook, Netflix, Amazon, etc.)
- [x] Domain normalization (lowercase, strip www.)
- [x] Subdomain-aware matching

#### Dataset & Response Foundation
- [x] `DatasetBuilder`: builds `Dataset` with username/email/password field values + presentation
- [x] `DatasetBuilder.buildSaveDataset()`: builds `SaveInfo` with required/optional IDs
- [x] `ResponseBuilder`: assembles `FillResponse` with datasets + save info
- [x] Support for API 26+ (minSdk) with API 30+ improvements

#### Repository Integration
- [x] `AutofillSession` receives `RequestParser`, `ResponseBuilder`, `DomainHandler`, `FieldMatcher` via Hilt
- [x] Ready to integrate `VaultItemRepository` (M3B) for credential lookup

#### Security & Logging
- [x] Never logs credentials: log messages are "Session started", "Session finished", "No autofillable fields detected", "Unsupported screen", "Matching attempt"
- [x] Passwords, usernames, emails never appear in log statements
- [x] No sensitive values exposed in Dataset or SaveInfo building

#### Build Verification
- [x] `assembleDebug` — zero errors, zero warnings
- [x] `testDebugUnitTest` — passes
- [x] No deprecated API usage
- [x] No TODO/FIXME/HACK

### Milestone 3B — Autofill Engine (✅ Complete)

#### Credential Matching
- [x] `CredentialMatcher`: matches vault items by domain, normalized domain, subdomain, and known app package domain mapping
- [x] Sorting: favorites first, recently updated second, alphabetically last
- [x] Never chooses automatically — presents all matching accounts

#### Vault Integration
- [x] `AutofillRepository`: integrates `VaultRepository` and `VaultItemRepository` for credential retrieval
- [x] Passwords decrypted only during fill request processing (SQLCipher transparent decryption)
- [x] No plaintext password caching; credentials scoped to request lifecycle

#### Dataset Generation
- [x] Real `AutofillDataset` population with username, email, and password values
- [x] Presentation shows credential name with username subtitle
- [x] Multiple datasets supported for multiple matching accounts

#### FillResponse
- [x] One credential: single dataset response with SaveInfo
- [x] Multiple credentials: multiple datasets with SaveInfo
- [x] No credentials: empty response with SaveInfo
- [x] Graceful handling of all cases

#### SaveRequest Handling
- [x] Parse `SaveRequest` structure to extract username, email, password values
- [x] Detect existing vault item by domain/package match for update vs. new creation
- [x] Update existing: preserves existing username/email if not changed
- [x] Create new: creates `VaultItem` with `OTHER` category, domain-derived URL

#### Multiple Account Support
- [x] All matching accounts displayed in autofill dropdown
- [x] Favorites sorted first
- [x] Recently updated sorted second
- [x] Alphabetical by name as final tiebreaker

#### Authentication Flow
- [x] `SessionManager.isUnlocked()` checked before any vault read
- [x] Locked vault returns empty response — no credential exposure
- [x] Locked vault blocks save requests silently

#### Security
- [x] Passwords exist in memory only during fill request processing
- [x] No credential logging: log messages are "Matching started", "Matching completed", "N datasets", "SaveRequest received", "Authentication required"
- [x] No clipboard usage
- [x] No credential leaks in presentation or log output

#### Performance
- [x] Single vault query per fill request
- [x] In-memory filtering for domain matching (avoids multiple DB queries)
- [x] `runBlocking(Dispatchers.IO)` for synchronous autofill context

#### Error Handling
- [x] `AppResult` used consistently in repository layer
- [x] Empty vault: returns empty credentials list, SaveInfo still attached
- [x] Locked vault: returns empty response with no datasets
- [x] Corrupted entry or DB failure: caught and logged, returns empty list

#### Logging
- [x] "Matching started for request: {id}"
- [x] "No autofillable fields detected"
- [x] "Unsupported screen — no login fields detected"
- [x] "Authentication required — vault locked"
- [x] "Matching attempt for package: {package}"
- [x] "Matching completed — {count} datasets"
- [x] "SaveRequest received"
- [x] "SaveRequest — vault locked, skipping save"
- [x] Usernames, passwords, emails never appear in log messages

#### Build Verification
- [x] `assembleDebug` — zero errors, zero warnings
- [x] `testDebugUnitTest` — passes
- [x] No deprecated API usage
- [x] No TODO/FIXME/HACK

### Milestone 3C — Autofill Polish, Compatibility & Security (✅ Complete)

#### Inline Autofill Suggestions
- [x] `Dataset.Builder(Presentations)` for API 31+ inline support
- [x] `Presentations.Builder` with menu presentation for dropdown + inline
- [x] Graceful fallback to `Dataset.Builder(RemoteViews)` on pre-31
- [x] Multiple account support preserved
- [x] Fast rendering: single RemoteViews per dataset

#### Biometric Authentication
- [x] `AutofillAuthActivity` with `BiometricPrompt` (fingerprint + face unlock + device credential)
- [x] `BiometricAuthManager` singleton for availability check via `BiometricManager`
- [x] `SessionManager.tempUnlockForAutofill()` — scoped unlock for autofill session only
- [x] `SessionManager.lockIfAutofillOnly()` — auto-relock after session ends
- [x] `SessionManager.markAutofillAuthAttempted()` — prevents infinite auth loop
- [x] Auth response via `FillResponse.Builder.setAuthentication()` + PendingIntent
- [x] Clean cancel: auth failure → RESULT_CANCELED → empty response on retry

#### Autofill UX
- [x] Better dataset labels: simple_list_item_2 with primary + secondary text
- [x] Primary: `name (username)` or `name` format
- [x] Secondary: website URL or package name
- [x] Inline suggestion hint via `Presentations` pass-through
- [x] String resources for biometric prompt, empty state

#### Compatibility
- [x] API 26+ (minSdk) — all existing paths preserved
- [x] API 30+ (Android 11) — biometric availability check via AndroidX
- [x] API 31+ (Android 12) — `InlinePresentationSpec`, `Presentations.Builder`, non-deprecated auth API
- [x] API 29+ — `PendingIntent.FLAG_IMMUTABLE` for auth PendingIntent
- [x] All API differences handled with `Build.VERSION.SDK_INT` checks
- [x] `onBackPressed` uses `OnBackPressedDispatcher` (not deprecated override)

#### Accessibility
- [x] Content descriptions on dataset text1 and text2 via `RemoteViews.setContentDescription()`
- [x] Inline suggestion content descriptions
- [x] simple_list_item_2 supports large fonts natively
- [x] `android.R.layout.simple_list_item_2` provides clear high-contrast text
- [x] String resources for screen reader labels in `strings.xml`

#### Performance
- [x] Single vault query per fill request preserved
- [x] In-memory filtering for domain matching preserved
- [x] Session start/end timing logged for performance metrics
- [x] No unnecessary allocations: `emptyArray<AutofillId>()` in auth response
- [x] `BiometricAuthManager` uses cached context, no per-call allocations

#### Security Hardening
- [x] Session cleanup: `lockIfAutofillOnly()` called in `completeWithEmpty()`, `finishSession()`, and `cancel()`
- [x] Temp unlock scoped: `autofillSessionUnlock` flag prevents normal unlock from being auto-locked
- [x] Auth timeout: autofill session re-locks after response delivery
- [x] No credential logging: all log messages are lifecycle/auth/performance only
- [x] Safe exception handling: every file extraction wrapped in try/catch
- [x] Locked vault: returns empty response before any vault read
- [x] `SessionManager.hasAutofillAuthBeenAttempted()` prevents repeated auth prompts

#### Error Recovery
- [x] Cancelled authentication: returns empty response on retry (vault still locked)
- [x] Invalid AssistStructure: caught in `RequestParser.parse()`, returns empty
- [x] No package name: early return with empty response
- [x] Credential retrieval failure: caught, logged, returns empty
- [x] Domain extraction failure: caught, falls back to package name
- [x] Field value extraction failure: caught individually, defaults to ""
- [x] Save failure: caught, logged, callback still calls onSuccess()
- [x] Empty vault: returns empty credentials list, SaveInfo still attached
- [x] Never crashes on any input

#### Logging
- [x] Session lifecycle: "Matching started", "Session finished", "Session cancelled", "SaveRequest received/completed"
- [x] Authentication events: "Biometric authentication succeeded/error/failed"
- [x] Performance metrics: "Session fill completed in {N}ms"
- [x] Recovery events: "Failed to parse request", "Failed to retrieve credentials", "Failed to extract {field}"
- [x] Auth flow: "Authentication required — vault locked, skipping autofill", "Authentication response sent"
- [x] Never logs: username, email, password, secret, encryption keys

#### Code Quality
- [x] Zero compile errors
- [x] Zero warnings
- [x] No TODO/FIXME/HACK
- [x] No placeholder or fake implementation
- [x] Deprecated APIs suppressed only with @Suppress("DEPRECATION") where unavoidable
- [x] All public API surface is production-ready

#### Build Verification
- [x] `compileDebugKotlin` — zero errors, zero warnings
- [x] `assembleDebug` — BUILD SUCCESSFUL
- [x] `testDebugUnitTest` — passes

### Milestone 4A — Vault Organization (✅ Complete)

#### 1. Smart Folders
- [x] Default folders (created on demand via "Create Folder" dialog)
- [x] Custom folders with rename and delete
- [x] Move items between folders (VaultScreen + ItemDetailScreen)
- [x] Folder selector dialog for moving items
- [x] Rename dialog with current name pre-filled
- [x] Delete with confirmation, items preserved (folder_id set to null)

#### 2. Tags
- [x] Multiple tags per item (many-to-many via tag_item cross-ref)
- [x] Create tag via "+ Tag" chip in filter row
- [x] Delete tag with confirmation (removed from all items)
- [x] Filter by one or more tags (AND logic via HAVING COUNT)
- [x] Tag selection chips in AddEditItemScreen
- [x] Tag display chips in ItemDetailScreen
- [x] Tag filter chips in VaultScreen
- [x] Tag search via TagDao.searchTags()

#### 3. Favorites
- [x] Pin/unpin favorite items (toggle star in VaultScreen)
- [x] Favorite filter collection (Favorites smart collection)
- [x] Favorite sorting (SortOption.FAVORITE)

#### 4. Archive
- [x] `archived_at` column on vault_items table
- [x] Archive items (sets archived_at timestamp)
- [x] Unarchive/restore archived items (clears archived_at)
- [x] Archived items hidden from All Items, Recent, Favorites, Categories, Folders
- [x] Archived smart collection to view archived items
- [x] Archive action in overflow menu (VaultScreen long-press, ItemDetailScreen)

#### 5. Trash
- [x] Soft delete (sets deleted_at timestamp) — already existed
- [x] Restore from trash (clears deleted_at)
- [x] Permanent delete
- [x] Trash smart collection view
- [x] Automatic cleanup infrastructure (permanentDeleteOldTrash query, old trash count)
- [x] Empty state messages per collection

#### 6. Smart Collections
- [x] All Items — default view, all active non-archived, non-deleted items
- [x] Recent — last 20 updated items
- [x] Favorites — all favorited active items
- [x] Archived — all archived non-deleted items
- [x] Trash — all soft-deleted items
- [x] Banking, Google, Email, Social Media — category-filtered views
- [x] User-created folders appear in collection selector
- [x] `VaultCollection` sealed class for type-safe collection routing

#### 7. UI Improvements
- [x] Collection selector dropdown (smart collections + categories + folders)
- [x] Tag filter chips with multi-select
- [x] Context menu on long-press (archive, move, delete)
- [x] Collection-aware empty states with contextual hints
- [x] Folder management (create/rename/delete dialogs)
- [x] Tag management (create/delete dialogs)
- [x] Move-to-folder dialog
- [x] Material 3 components throughout

#### Data Layer
- [x] `VaultItemDao`: archive/unarchive, getArchivedItems, getRecentItems, getItemsByTags, moveItem, permanentDeleteOldTrash
- [x] `FolderDao`: rename, getItemCount queries
- [x] `TagDao`: searchTags, removeAllTagsFromItem
- [x] `DatabaseMigrations`: MIGRATION_2_3 (archived_at column + index)
- [x] `PassGoDatabase`: version bumped to 3, migration registered
- [x] `VaultItemRepositoryImpl`: archive, unarchive, moveItem, permanentDeleteOldTrash, getItemsByTags
- [x] `FolderRepositoryImpl`: rename
- [x] `TagRepositoryImpl`: setItemTags (replace all tags on item)
- [x] `VaultItem` model: added `archivedAt` field
- [x] `Mappers.kt`: archivedAt mapping for entity↔domain

#### Security
- [x] Archived and trashed items remain encrypted (SQLCipher transparent encryption)
- [x] No sensitive information in logs
- [x] No passwords, usernames, or emails logged

#### Performance
- [x] Reactive queries (Flow-based, auto-update)
- [x] Efficient Room queries (indexed archived_at, tagged join with HAVING COUNT)
- [x] Tag filter applied in-memory from two reactive flows (avoids complex dynamic SQL)
- [x] Collection-based routing uses specific DAO queries (not post-filtering)

#### Build Verification
- [x] `compileDebugKotlin` — zero errors, zero warnings
- [x] `assembleDebug` — BUILD SUCCESSFUL
- [x] `testDebugUnitTest` — passes
- [x] No TODO/FIXME/HACK
- [x] No deprecated APIs

---

## FILES_CREATED

### Milestone 0
| File | Lines | Purpose |
|---|---|---|
| `core/navigation/PassGoNavHost.kt` | 80 | Bottom nav + NavHost + Screen sealed class |
| `core/error/AppResult.kt` | 17 | Error handling sealed types |
| `di/AppModule.kt` | 20 | Hilt module (PassGoLogger provider) |
| `feature/home/HomeScreen.kt` | 36 | Placeholder home screen |
| `feature/vault/VaultScreen.kt` | 36 | Placeholder vault screen |
| `feature/premium/PremiumScreen.kt` | 36 | Placeholder premium screen |
| `feature/settings/SettingsScreen.kt` | 36 | Placeholder settings screen |
| `PROJECT_MAP.md` | ~180 | Architecture documentation |

### Milestone 1 — New Files

**Encryption & Security**
| File | Lines | Purpose |
|---|---|---|
| `core/security/KeyDerivation.kt` | 36 | PBKDF2-HMAC-SHA256 key derivation |
| `core/security/KeyStoreManager.kt` | 110 | AndroidKeyStore AES-256-GCM wrapper |
| `core/security/MasterKeyManager.kt` | 93 | Master key lifecycle, cache, rotation |
| `core/security/PasswordHasher.kt` | 49 | PBKDF2 password hashing + verify + zero-fill |
| `core/security/PasswordValidator.kt` | 82 | Password strength rules, 5 levels |
| `core/security/MasterPasswordStore.kt` | 68 | EncryptedSharedPreferences hash storage |

**Domain Models**
| File | Lines | Purpose |
|---|---|---|
| `core/model/Vault.kt` | 12 | Vault domain model |
| `core/model/VaultItem.kt` | 30 | Vault item with 8 types |
| `core/model/Folder.kt` | 15 | Folder domain model |
| `core/model/Tag.kt` | 13 | Tag domain model |
| `core/model/Attachment.kt` | 15 | Attachment metadata domain model |
| `core/model/SyncStatus.kt` | 8 | Sync status enum |

**Database**
| File | Lines | Purpose |
|---|---|---|
| `core/database/PassGoDatabase.kt` | 77 | SQLCipher-encrypted Room database |
| `core/database/DatabaseMigrations.kt` | 14 | Migration strategy |
| `core/database/entity/VaultEntity.kt` | 23 | Vault Room entity |
| `core/database/entity/VaultItemEntity.kt` | 56 | Vault item Room entity |
| `core/database/entity/FolderEntity.kt` | 52 | Folder Room entity |
| `core/database/entity/TagEntity.kt` | 41 | Tag Room entity |
| `core/database/entity/TagItemCrossRef.kt` | 35 | Tag-item many-to-many |
| `core/database/entity/AttachmentEntity.kt` | 46 | Attachment metadata Room entity |
| `core/database/dao/VaultDao.kt` | 25 | Vault CRUD queries |
| `core/database/dao/VaultItemDao.kt` | 46 | Vault item queries (search, filter, batch) |
| `core/database/dao/FolderDao.kt` | 31 | Folder CRUD queries |
| `core/database/dao/TagDao.kt` | 35 | Tag CRUD + cross-ref queries |
| `core/database/dao/AttachmentDao.kt` | 22 | Attachment CRUD queries |

**Data Layer**
| File | Lines | Purpose |
|---|---|---|
| `data/mapper/Mappers.kt` | 113 | Entity↔domain bidirectional mapping |
| `data/repository/VaultRepository.kt` | 13 | Vault repository interface |
| `data/repository/VaultRepositoryImpl.kt` | 47 | Vault repository implementation |
| `data/repository/VaultItemRepository.kt` | 22 | VaultItem repository interface |
| `data/repository/VaultItemRepositoryImpl.kt` | 84 | VaultItem repository implementation |
| `data/repository/FolderRepository.kt` | 14 | Folder repository interface |
| `data/repository/FolderRepositoryImpl.kt` | 59 | Folder repository implementation |
| `data/repository/TagRepository.kt` | 16 | Tag repository interface |
| `data/repository/TagRepositoryImpl.kt` | 72 | Tag repository implementation |
| `data/repository/AttachmentRepository.kt` | 12 | Attachment repository interface |
| `data/repository/AttachmentRepositoryImpl.kt` | 51 | Attachment repository implementation |
| `data/session/SessionManager.kt` | 62 | Session state, auto-lock, initial flow detection |
| `data/settings/ThemeMode.kt` | 7 | SYSTEM/LIGHT/DARK enum |
| `data/settings/UserPreferences.kt` | 76 | DataStore preferences (theme, auto-lock, language, tips) |

**Feature Screens**
| File | Lines | Purpose |
|---|---|---|
| `feature/home/HomeScreen.kt` | 189 | Dashboard with stats, vault status, security tip card, FAB |
| `feature/home/HomeViewModel.kt` | 29 | Total items, vault status (reactive), security tips toggle |
| `feature/settings/SettingsScreen.kt` | 185 | Theme selection, auto-lock, app version |
| `feature/settings/SettingsViewModel.kt` | 50 | Theme + auto-lock reactive state, version parsing |
| `feature/setup/SetupScreen.kt` | 177 | Master password creation with strength indicator |
| `feature/setup/SetupViewModel.kt` | 96 | Password validation, hashing, storage, session unlock |
| `feature/unlock/UnlockScreen.kt` | 109 | Password input + error display + unlock |
| `feature/unlock/UnlockViewModel.kt` | 82 | Hash verification, error handling, session unlock |

**DI Modules**
| File | Lines | Purpose |
|---|---|---|
| `di/DatabaseModule.kt` | 46 | Hilt module for DB + DAOs |
| `di/AppModule.kt` | 30 | Hilt module for logger + session manager |

### Milestone 1 — Modified Files
| File | Changes |
|---|---|---|
| `MainActivity.kt` | Injects SessionManager + UserPreferences; passes themeMode to PassGoTheme and sessionManager to PassGoNavHost |
| `core/ui/theme/Theme.kt` | Accepts `themeMode: ThemeMode` parameter; dynamic color on Android 12+ |
| `core/navigation/PassGoNavHost.kt` | Added Setup + Unlock routes; startDestination based on sessionState; bottom bar hidden on auth screens |
| `core/database/dao/VaultItemDao.kt` | Added `getActiveItemsCount(): Flow<Int>` query |
| `app/build.gradle.kts` | Added `security-crypto`, `datastore-preferences`, `error_prone_annotations` dependencies |
| `feature/home/HomeScreen.kt` | Rewritten from placeholder: added boxes, cards, FAB; `isUnlocked` uses `collectAsState()` |
| `feature/home/HomeViewModel.kt` | `isUnlocked` changed from Boolean getter to reactive `StateFlow<Boolean>` |

### Milestone 1 — Test Files
| File | Purpose |
|---|---|
| `test/.../KeyDerivationTest.kt` | Unit tests: salt generation, key derivation, password clearing (6 tests, pass) |
| `androidTest/.../DaoInstrumentedTest.kt` | Instrumented tests: vault/item CRUD, search, soft delete, restore, favorites (8 tests) |
| `androidTest/.../MigrationTest.kt` | Instrumented tests: schema creation, database version (2 tests) |

### Milestone 2 — New Files

**Vault Feature**
| File | Lines | Purpose |
|---|---|---|
| `feature/vault/VaultViewModel.kt` | 123 | Reactive multi-filter list state |
| `feature/vault/VaultScreen.kt` | 332 | Search, sort, filter chips, LazyColumn, FAB, empty state |
| `feature/vault/AddEditItemViewModel.kt` | 177 | Full form VM with validation + password generator |
| `feature/vault/AddEditItemScreen.kt` | 301 | Add/edit form with category/folder dropdowns, strength indicator |
| `feature/vault/ItemDetailViewModel.kt` | 86 | Detail VM with copy, show/hide, open URL, delete |
| `feature/vault/ItemDetailScreen.kt` | 259 | Detail screen with copy/Snackbar, show/hide, open, delete dialog |

**Core Components**
| File | Lines | Purpose |
|---|---|---|
| `core/model/VaultItemCategory.kt` | 16 | 12 categories with display names |
| `core/security/PasswordGenerator.kt` | 54 | Crypto-strong `SecureRandom` password generator |
| `core/ui/components/PasswordStrengthIndicator.kt` | 94 | Reusable strength bar + suggestions |

### Milestone 2 — Modified Files
| File | Changes |
|---|---|
| `core/model/VaultItem.kt` | Replaced `ItemType` with `VaultItemCategory`, added `email` field |
| `core/database/entity/VaultItemEntity.kt` | Added `email` column |
| `core/database/dao/VaultItemDao.kt` | Extended to 16 query methods (search, filter, sort by name/newest/favorite) |
| `core/database/DatabaseMigrations.kt` | Replaced MIGRATION_1_2 with email ADD COLUMN migration |
| `core/database/PassGoDatabase.kt` | Version 2, added migration registration |
| `data/mapper/Mappers.kt` | Updated for VaultItemCategory mapping + email |
| `data/repository/VaultItemRepository.kt` | Added getById, search, sorted queries |
| `data/repository/VaultItemRepositoryImpl.kt` | Implemented all new query methods |
| `di/DatabaseModule.kt` | Restructured to abstract class with `@Binds` for repositories |
| `core/navigation/PassGoNavHost.kt` | Added vault/add, vault/detail/{id}, vault/edit/{id} routes |
| `feature/home/HomeScreen.kt` | Added `onAddItem` callback, wired FAB navigation |
| `feature/vault/VaultScreen.kt` | Full rewrite from placeholder |
| `README.md` | Updated features, screens, milestone status |
| `PROJECT_MAP.md` | This file — updated throughout |

### Milestone 3A — New Files

**Autofill Feature**
| File | Lines | Purpose |
|---|---|---|
| `feature/autofill/service/PassGoAutofillService.kt` | 44 | Main AutofillService with `@AndroidEntryPoint` Hilt DI |
| `feature/autofill/session/AutofillSession.kt` | 90 | Session lifecycle state machine |
| `feature/autofill/parser/RequestParser.kt` | 115 | `AssistStructure` → `AutofillRequest` parser |
| `feature/autofill/matcher/FieldMatcher.kt` | 104 | Username/email/password field classification |
| `feature/autofill/dataset/DatasetBuilder.kt` | 80 | Dataset + SaveInfo builder for Autofill |
| `feature/autofill/response/ResponseBuilder.kt` | 46 | FillResponse builder |
| `feature/autofill/domain/DomainHandler.kt` | 82 | Domain normalization, package→domain mapping |
| `feature/autofill/model/AutofillField.kt` | 19 | Parsed field representation |
| `feature/autofill/model/AutofillRequest.kt` | 9 | Parsed request model |
| `feature/autofill/model/AutofillCredential.kt` | 8 | Credential data model |
| `feature/autofill/model/SessionState.kt` | 11 | Session state enum |

**XML Configuration**
| File | Purpose |
|---|---|
| `res/xml/autofill_service_config.xml` | Autofill service configuration |

### Milestone 3A — Modified Files
| File | Changes |
|---|---|
| `AndroidManifest.xml` | Added `BIND_AUTOFILL_SERVICE` permission, `PassGoAutofillService` with intent-filter and meta-data |
| `PROJECT_MAP.md` | Added M3A sections, autofill package structure, milestone plan |
| `README.md` | _(no changes needed — autofill is infrastructure, not user-facing feature)_ |

**Files kept** from M0/M1/M2: All existing files — `DatabaseModule` provides all repositories needed for M3B credential lookup.

### Milestone 3B — New Files

**Autofill Engine**
| File | Lines | Purpose |
|---|---|---|
| `feature/autofill/matcher/CredentialMatcher.kt` | 37 | Domain-based credential matching with sorted results |
| `feature/autofill/repository/AutofillRepository.kt` | 107 | Vault integration, credential retrieval, save request handling |

### Milestone 3B — Modified Files
| File | Lines | Changes |
|---|---|---|
| `feature/autofill/model/AutofillCredential.kt` | 8 → 13 | Added `name`, `url`, `favorite`, `createdAt`, `updatedAt` fields |
| `feature/autofill/session/AutofillSession.kt` | 90 → 197 | Full rewrite: vault integration, auth flow, credential matching, save request parsing, field extraction |
| `feature/autofill/dataset/DatasetBuilder.kt` | 80 → 83 | Improved presentation with credential name + username subtitle |
| `feature/autofill/domain/DomainHandler.kt` | 82 → 86 | URL extraction fallback for bare domains (no protocol) |
| `PROJECT_MAP.md` | — | Added M3B sections, file entries, milestone status |

### Milestone 3C — New Files

**Biometric Authentication**
| File | Lines | Purpose |
|---|---|---|
| `feature/autofill/auth/AutofillAuthActivity.kt` | 98 | Transparent Activity with BiometricPrompt for autofill unlock |
| `feature/autofill/auth/BiometricAuthManager.kt` | 45 | Biometric availability check via AndroidX BiometricManager |

### Milestone 3C — Modified Files
| File | Lines | Changes |
|---|---|---|
| `feature/autofill/service/PassGoAutofillService.kt` | 44 → 73 | Auth PendingIntent flow, SessionManager integration, needsAuthentication gate |
| `feature/autofill/session/AutofillSession.kt` | 197 → 272 | Auth gate, error recovery wrapping, timing metrics, re-lock after session, biometric injection |
| `feature/autofill/dataset/DatasetBuilder.kt` | 83 → 99 | Presentations.Builder for API 31+ inline support, content descriptions, simple_list_item_2, @Suppress("DEPRECATION") |
| `feature/autofill/response/ResponseBuilder.kt` | 46 → 71 | buildAuthResponse() with setAuthentication(Presentations), non-deprecated overloads |
| `data/session/SessionManager.kt` | 62 → 81 | tempUnlockForAutofill(), lockIfAutofillOnly(), markAutofillAuthAttempted(), hasAutofillAuthBeenAttempted() |
| `app/build.gradle.kts` | — | Added `libs.biometric` dependency |
| `AndroidManifest.xml` | — | Registered `AutofillAuthActivity` with transparent theme |
| `res/values/themes.xml` | — | Added `Theme.PassGo.Transparent` style |
| `res/values/strings.xml` | — | Added autofill string resources (biometric prompt, empty state, accessibility) |
| `PROJECT_MAP.md` | — | Added M3C sections, file entries, milestone status |
| `README.md` | — | Updated features list and milestone plan |

### Milestone 4A — Modified Files

| File | Lines | Changes |
|------|-------|---------|
| `core/database/entity/VaultItemEntity.kt` | 57 → 58 | Added `archived_at` column |
| `core/database/dao/VaultItemDao.kt` | 93 → 126 | Added archive, unarchive, getArchivedItems, getRecentItems, getItemsByTags, moveItem, permanentDeleteOldTrash queries; updated all existing queries to exclude archived items |
| `core/database/dao/FolderDao.kt` | 34 → 39 | Added rename, getItemCount queries |
| `core/database/dao/TagDao.kt` | 43 → 50 | Added searchTags, removeAllTagsFromItem |
| `core/database/DatabaseMigrations.kt` | 13 → 20 | Added MIGRATION_2_3 (archived_at + index) |
| `core/database/PassGoDatabase.kt` | — | Version bumped 2→3 |
| `core/model/VaultItem.kt` | 20 → 21 | Added `archivedAt` field |
| `data/mapper/Mappers.kt` | 154 → 158 | Added archivedAt ↔ archivedAt mapping |
| `data/repository/VaultItemRepository.kt` | 28 → 37 | Added archive, unarchive, moveItem, permanentDeleteOldTrash, getArchivedItems, getRecentItems, getItemsByTags |
| `data/repository/VaultItemRepositoryImpl.kt` | 100 → 150 | Implemented all new repository methods |
| `data/repository/FolderRepository.kt` | 14 → 15 | Added rename |
| `data/repository/FolderRepositoryImpl.kt` | 52 → 55 | Implemented rename |
| `data/repository/TagRepository.kt` | 16 → 18 | Added searchTags, setItemTags |
| `data/repository/TagRepositoryImpl.kt` | 63 → 80 | Implemented searchTags, setItemTags |
| `feature/vault/VaultViewModel.kt` | 123 → 240 | Complete rewrite: VaultCollection sealed class, collection routing, tag filtering, folder CRUD, tag CRUD, archive/trash actions |
| `feature/vault/VaultScreen.kt` | 332 → 550+ | Complete rewrite: collection dropdown, tag chips, folder management dialogs, context menu, archive/trash, collection-aware empty states |
| `feature/vault/AddEditItemViewModel.kt` | 179 → 195 | Added tag selection state, toggleTag, setItemTags on save |
| `feature/vault/AddEditItemScreen.kt` | 301 → 315 | Added tag chip selection UI |
| `feature/vault/ItemDetailViewModel.kt` | 87 → 110 | Added archiveItem, moveItem, tags loading, folders |
| `feature/vault/ItemDetailScreen.kt` | 259 → 360+ | Added tag display chips, overflow menu (archive, move, delete) |

### Milestone 4B — New Files

| File | Lines | Purpose |
|------|-------|---------|
| `core/model/FieldId.kt` | 50 | Enum with 47 stable custom field identifiers |
| `core/model/FieldDefinition.kt` | 473 | Sealed class: 25 definitions with validate/format/parse/inputType/autofillHint, companion fromId() lookup |
| `core/model/CustomField.kt` | 13 | Domain model (id, itemId, fieldId, value, sortOrder) with definition delegate + formattedValue |
| `core/database/entity/CustomFieldEntity.kt` | 40 | Room entity for custom_fields table: FK→vault_items (CASCADE), unique(item_id, field_id) |
| `core/database/dao/CustomFieldDao.kt` | 30 | DAO: getFieldsForItem (Flow), getFieldsForItems, insert/insertBatch, delete/deleteAllForItem |
| `data/repository/CustomFieldRepository.kt` | 14 | Repository interface |
| `data/repository/CustomFieldRepositoryImpl.kt` | 56 | Repository implementation with AppResult error handling |

**Test files (5 new, 69 tests)**

| File | Purpose |
|------|---------|
| `test/.../FieldDefinitionTest.kt` | Validation, format, parse for all field types (parameterized) |
| `test/.../FieldIdTest.kt` | Enum name stability, FieldId→FieldDefinition mapping completeness |
| `test/.../CustomFieldTest.kt` | Definition delegation, formattedValue delegation |
| `test/.../CustomFieldMapperTest.kt` | Entity↔domain bidirectional mapping, unknown fieldId fallback |
| `test/.../DatabaseMigrationTest.kt` | Migration version sequencing, MIGRATION_3_4 verification |

### Milestone 4B — Modified Files

| File | Changes |
|------|---------|
| `core/database/DatabaseMigrations.kt` | Added MIGRATION_3_4 (CREATE TABLE custom_fields + indices) |
| `core/database/PassGoDatabase.kt` | Version 3→4, added CustomFieldEntity + CustomFieldDao |
| `di/DatabaseModule.kt` | Added CustomFieldRepository @Binds + CustomFieldDao @Provides |
| `data/mapper/Mappers.kt` | Added CustomFieldEntity↔CustomField mapping functions |
| `core/database/dao/VaultItemDao.kt` | 4 search queries: LEFT JOIN custom_fields for unified search |
