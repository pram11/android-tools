# System Architecture

This document describes the architectural patterns, module layouts, and data schemas designed to ensure the application remains highly scalable and decoupled as new utility features are introduced.

## 1. Architectural Patterns & Technology Stack

The application strictly adheres to an offline-first architecture, utilizing local hardware APIs and deterministic algorithms without external cloud dependencies.

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose with Material 3 Design Tokens
*   **Navigation:** Navigation Compose (handling adaptive rail/bottom bar transitions)
*   **State Management:** Unidirectional Data Flow (UDF) via MVVM/MVI architecture patterns using Kotlin Coroutines and StateFlow.
*   **Adaptive Layout:** Material 3 Adaptive UI components (`NavigationSuiteScaffold`) to handle dynamic configuration switches between standard mobile, foldable, and tablet form factors.

---

## 2. Module & Package Structure

To maximize isolation and prevent circular dependencies when adding features, the codebase uses a modularized package strategy:

```
com.armyknife.tools
 ├── core
 │    ├── common       --> Base interfaces, extension functions, styles, and utility classes.
 │    ├── designsystem --> Custom Material 3 components, theme definitions, and icons.
 │    └── navigation   --> Central navigation controller, route structures, and global arguments.
 ├── data
 │    ├── registry     --> Central Tool Registry holding the dynamic list of all ToolItems.
 │    └── storage      --> Local Preferences (DataStore) for settings and user favorites.
 └── features
      ├── dashboard    --> The Home Screen rendering the category grids dynamically.
      ├── search       --> Global search interface across the registry.
      ├── favorites    --> Filtered view of pinned tools.
      ├── sensor       --> Sub-package/module containing sensor-based tools screens.
      ├── hardware     --> Sub-package/module containing hardware control screens.
      ├── media        --> Sub-package/module containing media utility screens.
      └── utility      --> Sub-package/module containing data calculation screens.
```

---

## 3. Data-Driven Scalability Schema

The core mechanism for adding new features without altering the layout logic of the main navigation or dashboard screens relies on the following immutable data declarations.

### 3.1 Category Definition
```kotlin
enum class ToolCategory(val id: String, val title: String, val iconRes: Int) {
    SENSOR("sensor", "Sensor Tools", R.drawable.ic_sensor),
    HARDWARE("hardware", "Hardware Control", R.drawable.ic_hardware),
    MEDIA("media", "Media & Files", R.drawable.ic_media),
    UTILITY("utility", "Data & Utilities", R.drawable.ic_utility)
}
```

### 3.2 Tool Specification Model
```kotlin
data class ToolItem(
    val id: String,
    val title: String,
    val description: String,
    val category: ToolCategory,
    val iconRes: Int,
    val route: String,
    val requiredPermissions: List<String> = emptyList()
)
```

### 3.3 Dynamic Flow of Extensions
1.  **Creation:** When building a new tool (e.g., "Lux Meter"), create a separate Compose function inside `features/sensor/LuxMeterScreen.kt`.
2.  **Registration:** Add a new `ToolItem` instance to the immutable list inside the central `ToolRegistry.kt`.
3.  **Route Mapping:** Append the screen composable to the NavGraph using the `ToolItem.route` identifier.
4.  **Automatic Integration:** The Dashboard, Search, and Favorites screens automatically discover, display, and filter the new feature based on the appended metadata, eliminating the need to rewrite UI containers.
