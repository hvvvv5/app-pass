PassGo

A professional Android password manager with encrypted local storage.

«Status: Milestone 4D — Advanced Search & Attachments (Complete)
Target: Android 16 (API 36), APK distribution»

Architecture

Feature-first, single-module Android app built with:

- Kotlin 2.4.0 + Jetpack Compose + Material 3
- Hilt for dependency injection
- Navigation Compose with bottom navigation + auth flow (setup/unlock)
- Room + SQLCipher for encrypted local storage
- AndroidKeyStore for hardware-backed key protection
 EncryptedSharedPreferences for master password hash/salt storage
- PBKDF2-HMAC-SHA256 (600K iterations) for key derivation and password hashing
- DataStore Preferences for app settings persistence
- Coroutines + Flow for async operations

Architecture Decisions

Single Unified VaultItem Model

All vault entries use one unified "VaultItem" model with a "VaultItemCategory" discriminator. There is no subclass per category—the category determines which fields are shown via metadata rather than separate modls.

Dynamic Vault Type Engine

The metadata-driven engine generates every Add/Edit/Detail screen from "FieldDefinition" metadata at runtime. Category registration, validation, formatting, and rendering are driven entirely by "VaultItemCategory".

FieldDefinition Metadata-Driven Architecture

Each "FieldId" has a corresponding "FieldDefinition" implementation defining labels, input types, validation rules, formatting, parsing, and autofill hints. Category metadata is declared declaratively inside "VaultIemCategory".

Custom Fields Stored as Structured Database Rows

Standard fields ("name", "username", "password", "url", "notes") are stored directly in "vault_items". Additional fields are stored in the "custom_fields" table and joined for unified searching.

No Separate Database Table Per Category

All categories—from Google Account to SSH Key and Database credentials—share the same "vault_items" and "custom_fields" tables. Adding a category requires no database migration.

No Category-Specific Screens
"DynamicFormScreen" and "DynamicItemDetailScreen" render every category from metadata without category-specific UI implementations.

Features

Feature| Status
Encrypted local database (SQLCipher)| ✅
Master password creation with strength validation| ✅
Vault unlock with password verification| ✅
Session management with auto-lock| ✅
Theme switching (Light / Dark / System)| ✅
Dashboard with vault stats and security tips| ✅
App settings (theme, auto-lock, version)| ✅
Logging infrastructure| ✅
Error handling AppResult)| ✅
Vault item list with search, sort, filter| ✅
Add/edit vault items with category & folder| ✅
Password generation (crypto-strong)| ✅
Password strength indicator| ✅
Item detail with copy, show/hide, open URL| ✅
34 vault item categories| ✅
Android Autofill Framework| ✅
Credential matching by domain / package| ✅
Save request handling| ✅
Multiple account autofill| ✅
Locked vault protection| ✅
Inline suggestions (Android 12+)| ✅
Biometric authentication for autofill| ✅
Accessibility support| ✅
Smrt collections| ✅
Smart folders| ✅
Tags with multi-tag filtering| ✅
Archive / Restore| ✅
Trash (soft delete & restore)| ✅
Folder management| ✅
Custom field architecture| ✅
Dynamic Vault Type Engine| ✅
Metadata-driven forms| ✅
Metadata-driven detail screens| ✅
Financial vault templates| ✅
Identity & personal templates| ✅
Developer & infrastructure templates| ✅
Reusable validation engine| ✅
Generic custom field architecture| ✅
Room FTS4 full-text search| ✅
Search history & suggestions| ✅
Dedicated advancedsearch screen| ✅
Keyword highlighting| ✅
Encrypted file attachments (AES-256-GCM)| ✅
Secure attachment storage| ✅
Attachment preview (images, text, PDF, external apps)| ✅

Screens

Screen| Route| Description
Setup| "/setup"| Master password creation
Unlock| "/unlock"| Password verification
Home| "/home"| Dashboard
Vault| "/vault"| Vault items
Search| "/vault/search"| Advanced full-text search
Add Item| "/vault/add"| Dynamic metadata-driven form
Item Detail| "/vault/detail/{id}"| Dynamic detail screen
Edt Item| "/vault/edit/{id}"| Dynamic editor
Attachment Preview| "/vault/attachment-preview/{attachmentId}"| Secure attachment preview
Premium| "/premium"| Premium features
Settings| "/settings"| Theme, auto-lock and app settings
Build

Windows PowerShell

.\gradlew :app:assembleDebug
.\gradlew testDebugUnitTest
.\gradlew connectedDebugAndroidTest

Linux / macOS

./gradlew :app:assembleDebug
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest

Milestones

#| Milestone| Status
M0| Project Foundation| ✅ Complete
M1| Core Foundation| ✅ Complete
M2| Vault Core (CRUD, categories, search/sort/filter, password generator, strength indicator, detail screen)| ✅ Complete
M3A| Autofill Foundation| ✅ Complete
M3B| Autofill Engine|✅ Complete
M3C| Autofill Polish, Compatibility & Security| ✅ Complete
M4A| Vault Organization (folders, tags, favorites, archive, trash, smart collections)| ✅ Complete
M4B| Architecture Refinements (FieldId, FieldDefinition, custom_fields, unified search)| ✅ Complete
M4C| Dynamic Vault Type Engine (metadata-driven UI)| ✅ Complete
M4D.1| Financial Vault Templates| ✅ Complete
M4D.2| Identity & Personal Vault Templates| ✅ Complete
M4D.3| Developer & Infrastructure Vault Templates| ✅ Complete
M4D.4| Full-Tet Search (Room FTS4)| ✅ Complete
M4D.5| Search History & Suggestions| ✅ Complete
M4D.6| Secure Attachment Storage| ✅ Complete
M4D.7| Advanced Search Screen| ✅ Complete
M4D.8| Attachment Management UI| ✅ Complete
M4D.9| Attachment Preview| ✅ Complete
M4D.10| Polish & Tests| ✅ Complete
M4D| Advanced Search & Attachments| ✅ Complete
M5| Security & Polish| ⏳ Planned

Roadmap

Feature| Status
Clipboard auto-clear| ⏳ Planned
Password security audit| ⏳ Planned
Backup & Restore| ⏳ Planned
Cloud Sync| ⏳ Planned
Scure sharing| ⏳ Planned
Passkey support| ⏳ Planned
Wear OS support| ⏳ Planned

Current Project Status

Completed

- Local encrypted password vault
- SQLCipher encrypted database
- Android Autofill integration
- Metadata-driven dynamic vault engine
- 34 vault categories
- Smart folders, tags, archive and trash
- Room FTS4 full-text search
- Search history and suggestions
- AES-256-GCM encrypted attachments
- Secure attachment preview
- 221+ automated tests
- Android 16 (API 36) compatibility

Next Milestne

M5 — Security & Polish

Focus areas:

- Clipboard auto-clear
- Security audit
- Accessibility improvements
- Backup & Restore
- Performance optimizations
- Additional hardening

License

This project is currently under active development.
Contributing

Contributions, suggestions, and issue reports are welcome.

If you would like to contribute:

1. Fork the repository.
2. Create a feature branch.
3. Implement your changes.
4. Run all tests.
5. Submit a Pull Request.

Project Structure

app/
 ├── core/
 │   ├── database/
 │   ├── navigation/
 │   ├── security/
 │   ├── model/
 │   └── ui/
 ├── data/
 │   ├── mapper/
 │   └── repository/
 ├── di/
 ├── feature/
 │   ├── auth/
 │   ├── home/
 │   ├── search/
 │   ├── settings/
 │   └── vault/
└── res/

Technology Stack

- Kotlin
- Jetpack Compose
- Material Design 3
- Room
- SQLCipher
- AndroidKeyStore
- Hilt
- Coroutines
- Flow
- DataStore
- Android Autofill Framework
- JUnit
- Mockito
- Turbine

Project Highlights

- Metadata-driven architecture
- Dynamic form generation
- Dynamic detail rendering
- Unified vault model
- Full-text search with Room FTS4
- Secure encrypted attachment storage
- Reactive architecture with Flow
- Repository pattern
- Dependency Injection with Hilt
- Clean separtion of layers
- Extensive unit and migration testing

Testing

Run all tests before every commit:

./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest

Current status:

- ✅ 221+ Unit Tests
- ✅ Migration Tests
- ✅ DAO Tests
- ✅ ViewModel Tests
- ✅ Repository Tests
- ✅ Build Verification

Development Status

PassGo is under active development.

Completed through Milestone M4D:

- Advanced Search
- Full-text Search (FTS4)
- Search History
- Search Suggestions
- Encrypted Attachments
- Attachmen Preview
- Dynamic Vault Engine
- Smart Vault Organization
- Android Autofill Support

The next development phase is Milestone M5 — Security & Polish, focusing on security hardening, clipboard protection, backup/restore, accessibility improvements, and overall application polish.
Security

PassGo follows a security-first architecture.

Current security features include:

- SQLCipher encrypted database
- AndroidKeyStore hardware-backed encryption
- AES-256-GCM encrypted attachments
- PBKDF2-HMAC-SHA256 (600K iterations)
- EncryptedSharedPreferences
- Secure session management
- Automatic vault locking
- Android Autofill security integration

Additional security improvements will continue during Milestone M5.

---

Performance

Designed for excellent performance with large vaults.
Highlights:

- Room FTS4 full-text search
- Metadata-driven rendering
- Reactive Flow architecture
- Lazy Compose lists
- Efficient Room queries
- Repository caching where appropriate

---

Compatibility

Platform| Status
Android 12| ✅
Android 13| ✅
Android 14| ✅
Android 15| ✅
Android 16 (API 36)| ✅ Target

---

Author

Developed by hvvvv5

GitHub:
https://github.com/hvvvv5

---

Acknowledgements

Built with the Android Jetpack ecosystem and modern Android development best practices.

Special thanks to he open-source community for the libraries and tools that make this project possible.
