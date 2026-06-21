package gr.dimvai.hearth.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Hearth Brand Colors
 * Derived from HSL values in style.css
 */

// --color-primary: hsl(221, 100%, 45%)
val Primary = Color.hsl(221f, 1f, 0.45f)
// --color-primary-dark: hsl(221, 100%, 30%)
val PrimaryDark = Color.hsl(221f, 1f, 0.30f)
// --color-primary-light: hsl(221, 100%, 60%)
val PrimaryLight = Color.hsl(221f, 1f, 0.60f)
// A very light blue for highlights
val PrimaryExLight = Color.hsl(221f, 1f, 0.85f)

// --color-accent: hsl(32, 100%, 36%)
val Accent = Color.hsl(32f, 1f, 0.36f)
// --color-accent-hover: hsl(36, 100%, 43%)
val AccentVariant = Color.hsl(36f, 1f, 0.43f)

// --bs-body-bg: hsl(221, 30%, 8%)
val Background = Color.hsl(221f, 0.30f, 0.08f)
// --color-card-bg: hsl(221, 25%, 13%)
val Surface = Color.hsl(221f, 0.25f, 0.13f)
// --color-header-bg: hsl(221, 100%, 18%)
val HeaderBackground = Color.hsl(221f, 1f, 0.18f)

// --bs-body-color: hsl(0, 0%, 88%)
val OnBackground = Color.hsl(221f, 0.15f, 0.90f) // Ήπιο άσπρο με λίγο μπλε
// --bs-heading-color: hsl(0, 0%, 97%)
val OnSurface = Color.hsl(221f, 0.10f, 0.96f) // Πολύ ανοιχτό γαλάζιο/άσπρο

// Ήπιο άσπρο για γενική χρήση
val SoftWhite = Color.hsl(221f, 0.08f, 0.94f)

// --color-body-muted: hsl(221, 20%, 55%)
val OnSurfaceVariant = Color.hsl(221f, 0.20f, 0.55f)

// --border-color: hsl(221, 25%, 22%)
val Outline = Color.hsl(221f, 0.25f, 0.22f)

// Danger color from btn-danger
val Danger = Color.hsl(348f, 0.90f, 0.38f)
val OnDanger = SoftWhite
