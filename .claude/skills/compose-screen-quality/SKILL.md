---
name: compose-screen-quality
description: Use when writing or modifying Compose screen files in the Handpass KMP app. Enforces FlowMVI Container pattern, file organization, composable extraction, naming, and design token usage. Triggers on any screen creation, UI refactor, or code cleanup involving Compose UI files.
---

# Compose Screen Quality Standards (Handpass)

Standards for clean, professional Compose screen files in the Handpass KMP project.

## FlowMVI Container Requirement

Every screen with business logic MUST use the FlowMVI Container pattern:

```kotlin
class XxxViewModel(...) : ViewModel(), Container<XxxUiState, XxxIntent, XxxAction> {
    override val store = store<XxxUiState, XxxIntent, XxxAction>(
        initial = XxxUiState.Content(),
        scope = viewModelScope
    ) {
        configure {
            name = "Xxx"
            actionShareBehavior = ActionShareBehavior.Distribute()
        }
        recover { e -> Cedar.tag(TAG).e("Store error", e); null }
        init { loadData() }
        reduce { intent -> when (intent) { ... } }
    }
}
```

Screen composable MUST use `with(viewModel.store) { val state by subscribe { ... } }` pattern.

## File Organization

Order composables in this sequence:

1. **Public screen composable** — the nav-graph entry point (e.g., `SettingsScreen`)
2. **Private content composable** (optional) — for complex screens, extract to enable previewing different states
3. **Section composables** — logical groups, in display order
4. **Utility composables** — small helpers
5. **Previews** — at the bottom of the file

## Composable Extraction Rules

- **Extract content composable** for complex screens with multiple states. Takes `state: XxxUiState.Content` and `onIntent: (XxxIntent) -> Unit`
- **Keep inline** for simpler screens where the content lives directly in the Scaffold body
- **Bottom sheets:** always extract to named composables

## Naming

| Type | Convention | Examples |
|------|-----------|----------|
| Screen entry | `XxxScreen` | `SettingsScreen` |
| Content (if extracted) | `XxxContent` | `HomeContent`, `SearchContent` |
| Section | `XxxSection` or `XxxCard` | `AppearanceSection` |
| Bottom sheet | `XxxBottomSheet` | `ImageSourceBottomSheet` |
| Preview | `PreviewXxxScreen` | `PreviewSettingsScreen` |

Previews must be `private fun` and use the `Preview` prefix consistently.

## Parameters

- `Modifier` as first optional parameter, defaulted to `Modifier`
- Data parameters first, then callbacks
- Use `invoke()` for nullable callbacks: `onClick?.invoke()`

## State & Intent Pattern

- Screen-level: `with(viewModel.store) { val uiState by subscribe { action -> ... } }`
- Dispatch intents: `intent(XxxIntent.Something)` inside the `with(store)` block
- For content composables, pass `onIntent: (XxxIntent) -> Unit` callback
- Local UI state (dialog visibility): `var showXxx by remember { mutableStateOf(false) }` in the screen
- `derivedStateOf` for computed values

## Error Handling Pattern

- Errors in actions use typed `AppError`, never raw strings
- Map errors to user-facing strings via `action.error.resolveString()` from `ui/error/ErrorResolver.kt`
- Show error messages using snackbar pattern:

```kotlin
val snackbarHostState = remember { SnackbarHostState() }
val scope = rememberCoroutineScope()

with(viewModel.store) {
    val uiState by subscribe { action ->
        when (action) {
            is XxxAction.ShowError -> {
                scope.launch { snackbarHostState.showSnackbar(action.error.resolveString()) }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        ...
    ) { paddingValues ->
        val state = uiState as? XxxUiState.Content ?: return@Scaffold
    }
}
```

## Navigation Arguments

For screens receiving parameters from navigation, use `LaunchedEffect` to pass them to the ViewModel:

```kotlin
LaunchedEffect(Unit) {
    intent(XxxIntent.Initialize(itemId))
}
```

## Design Token Usage

All values from the design system — see CLAUDE.md for full token reference:

| Type | Use | Never |
|------|-----|-------|
| Colors | `MaterialTheme.colorScheme.*`, `AppColors.*` | `Color.White`, `Color(0xFF...)` |
| Spacing | `AppTheme.spacing.*` | `16.dp`, `8.dp` |
| Corner radius | `AppTheme.cornerRadius.*` | `RoundedCornerShape(12.dp)` |
| Typography | `MaterialTheme.typography.*`, `AppTextStyles.*` | `TextStyle(fontSize = 16.sp)` |
| Dimensions | `AppTheme.dimensions.*` | Raw dp for standard component sizes |

## Verification Checklist

Before considering a screen file done:

- [ ] Uses FlowMVI Container pattern (`Container`, `store()`, `subscribe`)
- [ ] Contract file exists with `MVIState`, `MVIIntent`, `MVIAction`
- [ ] ViewModel registered in `ViewModelModule.kt` (get() count matches constructor)
- [ ] `@Preview` with `AppTheme { }` wrapper, prefixed `PreviewXxx`, marked `private`
- [ ] All user-facing strings from `stringResource(Res.string.xxx)`
- [ ] Error actions use `resolveString()` for user-facing messages
- [ ] No raw colors, spacing, corner radii, or text styles (see CLAUDE.md)
- [ ] No code comments (per CLAUDE.md)
- [ ] No unused imports
- [ ] Compiles: `./gradlew :sharedApp:compileKotlinMetadata`
