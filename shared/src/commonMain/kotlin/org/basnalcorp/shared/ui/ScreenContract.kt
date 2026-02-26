package org.basnalcorp.shared.ui

/**
 * # Screen contract (Phase 4.4)
 *
 * Every screen composable in shared follows this contract so layout selection and navigation
 * are consistent across Android and Desktop.
 *
 * ## Signature
 *
 * ```kotlin
 * @Composable
 * fun XxxScreen(
 *     context: LayoutContext,
 *     state: XxxState,           // or ViewModel / StateHolder
 *     onNavigate: (Route) -> Unit
 * )
 * ```
 *
 * - **LayoutContext**: [LayoutContext] (platform + [WindowSizeClass]) is provided by the host.
 *   Use it to choose Compact vs Expanded layout and any platform-specific UI (e.g. back button).
 *
 * - **State**: Screen-specific state or state holder (e.g. from [org.basnalcorp.shared.state.NotebookListStateHolder]).
 *   No Android ViewModel/LiveData in shared; use Flow/StateFlow from state holders.
 *
 * - **onNavigate**: Callback to request navigation. The host owns the back stack and handles [Route].
 *
 * ## Layout selection inside each screen
 *
 * Each screen selects its layout based on [LayoutContext.windowSizeClass]:
 *
 * ```kotlin
 * when (context.windowSizeClass) {
 *     WindowSizeClass.Compact -> XxxCompactLayout(...)
 *     WindowSizeClass.Medium  -> XxxMediumLayout(...)  // optional
 *     WindowSizeClass.Expanded -> XxxExpandedLayout(...)
 * }
 * ```
 *
 * - **Compact**: Single column, typical phone.
 * - **Medium**: Optional; tablet portrait or large phone.
 * - **Expanded**: Multi-pane or wide layout; tablet landscape and desktop.
 *
 * Use [windowSizeClassFromWidth] only when the host passes width; normally the host
 * computes [LayoutContext] and passes it in.
 */
