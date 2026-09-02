# 💸 Expense Tracker

A clean, offline-first Android expense tracking app built with **Jetpack Compose**, **Room**, and a strict **MVVM** architecture. Designed to help users log, categorise, search, and analyse their spending — all stored locally on-device with no account or internet connection required.

---

## 📱 Screenshots

> _Add screenshots of the Home, Add Expense, Reports, and Details screens here._

---

## ✨ Features

### 🏠 Home Screen
- **Summary card** showing your all-time total and today's spending at a glance.
- **Live search** — filter your expense list in real-time by category or description.
- **Chronological expense feed** (newest first) rendered with a smooth `LazyColumn`.
- **Empty state** with a prompt to add your first expense.
- **Floating Action Button (FAB)** to quickly navigate to the Add Expense screen.

### ➕ Add Expense Screen
- **Amount field** with a KSh prefix and decimal keyboard, with inline validation.
- **Category picker** — choose from 8 preset categories via an exposed dropdown menu.
- **Optional description** field (multi-line, up to 4 lines).
- **Date picker** — defaults to today, with a Material 3 `DatePickerDialog` to select any date.
- Form validation prevents saving with a zero/invalid amount or no category selected.

### 🔍 Expense Details Screen
- View full details of any expense.
- **Edit** existing records in-place.
- **Delete** an expense with confirmation.

### 📊 Reports Screen
- **Period filter chips**: Today · This Week · This Month · This Year · Custom Range.
- **Custom Range** mode reveals two date fields backed by `DatePickerDialog`s.
- Per-period **summary card** showing:
  - Total spending
  - Transaction count
  - Average transaction amount
- **Category breakdown** with proportional spending bars for every category in the period.
- Loading and error states handled gracefully with a `CircularProgressIndicator` and inline error text.

### ⚙️ Settings Screen
- App version / info placeholder — ready to be extended.

---

## 🏗️ Architecture

The app follows clean **MVVM** with a unidirectional data flow:

```
UI (Compose Screens)
       ↕
ExpenseViewModel  ←  StateFlow / combine / flatMapLatest
       ↕
ExpenseRepository
       ↕
ExpenseDao  (Room)
       ↕
Room Database  (SQLite on-device)
```

### Layer Breakdown

| Layer | Files | Responsibility |
|---|---|---|
| **UI – Screens** | `HomeScreen`, `AddExpenseScreen`, `ExpenseDetailsScreen`, `ReportsScreen`, `SettingsScreen` | Stateless composables; observe `StateFlow`s via `collectAsStateWithLifecycle` |
| **UI – Components** | `BottomNavigationBar`, `CategoryDropdown`, `CategorySpendingBar`, `EmptyExpenseState`, `ExpenseCard` | Reusable composables |
| **UI – Theme** | `Color.kt`, `Theme.kt`, `Type.kt` | Material 3 colour scheme and typography |
| **ViewModel** | `ExpenseViewModel`, `ExpenseViewModelFactory` | Business logic, `StateFlow` derivation, coroutine launches |
| **State models** | `ReportUiState`, `ReportPeriod` | Immutable UI state and period enum |
| **Repository** | `ExpenseRepository` | Single source of truth; wraps DAO |
| **Local data** | `ExpenseDao`, `ExpenseDatabase`, `ExpenseEntity`, `CategoryTotal` | Room DAO, database singleton, and entity definitions |
| **Utilities** | `DateUtils` | Calendar helpers for day/week/month/year boundaries |

---

## 🗄️ Data Model

### `ExpenseEntity` — Room table `expenses`

| Column | Type | Notes |
|---|---|---|
| `id` | `Int` | Auto-generated primary key |
| `amount` | `Long` | Stored in **cents** (e.g. KSh 10.50 → `1050`) |
| `category` | `String` | One of the 8 preset categories |
| `description` | `String` | Optional; defaults to `""` |
| `date` | `Long` | Unix epoch milliseconds (normalised to start-of-day) |

> **Why cents?** Storing amounts as `Long` integers avoids floating-point rounding errors entirely. The UI always divides by `100.0` for display.

### `CategoryTotal` — projection

```kotlin
data class CategoryTotal(
    val category: String,
    val total: Long
)
```
Returned by the `getCategoryTotals` query; not stored as a separate table.

---

## 📋 Expense Categories

The app ships with 8 fixed categories:

| | | | |
|---|---|---|---|
| 🍔 Food | 🚌 Transport | 🧾 Bills | 🛍️ Shopping |
| 🎬 Entertainment | 💊 Health | 📚 Education | 📦 Other |

---

## 🔄 Reactive Data Flow

All database reads are exposed as Kotlin `Flow` and converted to `StateFlow` in the ViewModel using `stateIn(SharingStarted.WhileSubscribed(5_000))`. This means:

- The UI always reflects the latest database state without manual refresh.
- Flows are paused 5 seconds after the last subscriber unsubscribes, saving resources.

### Report computation pipeline

```
_selectedPeriod + _customStartDate + _customEndDate
           ↓  combine
     resolveRange()  →  (startMs, endMs)
           ↓  flatMapLatest
  combine(total, count, average, categoryTotals)
           ↓
     ReportUiState  →  ReportsScreen
```

Switching a period chip cancels the previous database query chain and starts a new one atomically — no stale data leaks.

---

## 🛠️ Tech Stack

| Technology | Version / Details |
|---|---|
| **Language** | Kotlin |
| **UI** | Jetpack Compose + Material 3 |
| **Architecture** | MVVM + Repository pattern |
| **Navigation** | Jetpack Navigation Compose |
| **Database** | Room (SQLite) |
| **Async** | Kotlin Coroutines + `Flow` |
| **Build system** | Gradle (Kotlin DSL) with KSP |
| **Min SDK** | API 24 (Android 7.0 Nougat) |
| **Target SDK** | API 36 |
| **Compile SDK** | API 37 |

---

## 📁 Project Structure

```
Expense-Tracker/
├── app/
│   ├── schemas/                          # Room schema JSON exports
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/example/expensetracker/
│           ├── MainActivity.kt           # Entry point + NavHost
│           ├── data/
│           │   ├── local/
│           │   │   ├── CategoryTotal.kt  # DAO projection model
│           │   │   ├── ExpenseDao.kt     # Room DAO interface
│           │   │   ├── ExpenseDatabase.kt# Room database singleton
│           │   │   └── ExpenseEntity.kt  # Room @Entity
│           │   └── repository/
│           │       └── ExpenseRepository.kt
│           ├── ui/
│           │   ├── components/
│           │   │   ├── BottomNavigationBar.kt
│           │   │   ├── CategoryDropdown.kt
│           │   │   ├── CategorySpendingBar.kt
│           │   │   ├── EmptyExpenseState.kt
│           │   │   └── ExpenseCard.kt
│           │   ├── screens/
│           │   │   ├── AddExpenseScreen.kt
│           │   │   ├── ExpenseDetailsScreen.kt
│           │   │   ├── HomeScreen.kt
│           │   │   ├── ReportsScreen.kt
│           │   │   └── SettingsScreen.kt
│           │   └── theme/
│           │       ├── Color.kt
│           │       ├── Theme.kt
│           │       └── Type.kt
│           ├── util/
│           │   └── DateUtils.kt          # Calendar boundary helpers
│           └── viewmodel/
│               ├── ExpenseViewModel.kt
│               ├── ExpenseViewModelFactory.kt
│               ├── ReportPeriod.kt       # Enum: TODAY/WEEK/MONTH/YEAR/CUSTOM
│               └── ReportUiState.kt      # Immutable report state holder
├── build.gradle.kts
├── gradle.properties
├── gradlew / gradlew.bat
└── settings.gradle.kts
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** Ladybug (2024.2) or newer
- **JDK 11** or newer
- Android SDK with API 24+ installed

### Clone & Open

```bash
git clone https://github.com/<your-username>/Expense-Tracker.git
```

Open the project in Android Studio — it will auto-sync Gradle dependencies.

### Run

1. Connect a physical device (USB debugging on) **or** start an Android emulator (API 24+).
2. Click **Run ▶** in Android Studio, or from the terminal:

```bash
./gradlew installDebug
```

### Build a Release APK

```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release-unsigned.apk`

> Sign the APK with your keystore before distributing.

---

## 🧪 Testing

The project includes both unit and instrumented test stubs:

| Test type | Location | Runner |
|---|---|---|
| Unit tests | `app/src/test/` | JUnit 4 |
| Instrumented (UI) tests | `app/src/androidTest/` | AndroidJUnit4 + Espresso + Compose UI Test |

Run all tests:

```bash
# Unit tests
./gradlew test

# Instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest
```

---

## 🔢 Amount Formatting

All monetary amounts are stored as `Long` values in **cents** and formatted for display using:

```kotlin
String.format(Locale.US, "KSh %,.2f", amount / 100.0)
// e.g. 1050L → "KSh 10.50"
// e.g. 125000L → "KSh 1,250.00"
```

The currency prefix `KSh` (Kenyan Shillings) is hardcoded. To localise for a different currency, update the format strings in `HomeScreen.kt` and `ReportsScreen.kt`.

---

## 📅 Date Handling

`DateUtils` provides precise boundary helpers using `java.util.Calendar`:

| Method | Returns |
|---|---|
| `startOfDay(ts?)` | Midnight of the given day |
| `endOfDay(ts?)` | Midnight of the next day |
| `startOfWeek(ts?)` | Monday 00:00:00 of the current week |
| `endOfWeek(ts?)` | Monday 00:00:00 of the following week |
| `startOfMonth(ts?)` | 1st of the month at 00:00:00 |
| `endOfMonth(ts?)` | 1st of the next month at 00:00:00 |
| `startOfYear(ts?)` | January 1st 00:00:00 |
| `endOfYear(ts?)` | January 1st 00:00:00 of the next year |

All dates stored in the database are normalised to `startOfDay` at save time.

---

## 🗺️ Navigation Routes

| Route | Screen | Bottom bar |
|---|---|---|
| `home` | `HomeScreen` | ✅ |
| `reports` | `ReportsScreen` | ✅ |
| `settings` | `SettingsScreen` | ✅ |
| `add_expense` | `AddExpenseScreen` | ❌ |
| `expense/{expenseId}` | `ExpenseDetailsScreen` | ❌ |

The bottom navigation bar is conditionally shown only for the three top-level destinations.

---

## 🔮 Roadmap / Potential Improvements

- [ ] Currency localisation / user-configurable currency symbol
- [ ] Budget limits per category with overspend alerts
- [ ] Export expenses to CSV or PDF
- [ ] Dark / Light theme toggle in Settings
- [ ] Recurring expense support
- [ ] Charts (bar/pie) for the Reports screen
- [ ] Biometric / PIN lock for privacy
- [ ] Cloud backup / sync

---

## 📄 License

This project is open source. Add your preferred license here (e.g. MIT, Apache 2.0).

---

## 🤝 Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.
