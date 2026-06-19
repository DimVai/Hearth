# Implementation Plan - Βήμα 2: Branding & Identity

Ο στόχος αυτού του βήματος είναι η δημιουργία της βασικής δομής της Android εφαρμογής με το σωστό όνομα, εικονίδιο, χρώματα και splash screen, ώστε να μπορεί να εγκατασταθεί στο κινητό.

## Proposed Changes

### 1. Resources & Styling
- **Colors**: Ορισμός των χρωμάτων στο `ui/theme/Color.kt` χρησιμοποιώντας τις HSL τιμές από το `style.css`.
- **Theme**: Ρύθμιση του `HearthTheme` στο `ui/theme/Theme.kt` για αποκλειστική υποστήριξη Dark Mode.
- **Icons**: Μετατροπή των assets από το `/Hearth-PWA/public/resources/` σε Android Vector Drawables.

### 2. Branding
- **App Name**: Ορισμός του "Hearth" στο `strings.xml`.
- **Launcher Icon**: Δημιουργία Adaptive Icon χρησιμοποιώντας το λογότυπο της εφαρμογής.
- **Splash Screen**: Υλοποίηση της Splash Screen με το λογότυπο σε μπλε φόντο.

### 3. Project Configuration
- **Package Name**: `com.dimitris.hearth`.
- **Activity**: Μία `MainActivity` που θα εμφανίζει προς το παρόν μια απλή κενή οθόνη με το logo.

## Verification Plan

### Manual Verification
- **Build & Install**: Θα τρέξω το build και θα επιβεβαιώσω ότι η εφαρμογή εγκαθίσταται σωστά.
- **Splash Screen Check**: Επιβεβαίωση ότι η splash screen εμφανίζεται κατά την εκκίνηση.
- **Icon Check**: Επιβεβαίωση ότι το εικονίδιο στην αρχική οθόνη του κινητού είναι το σωστό.
- **Theme Check**: Επιβεβαίωση ότι τα χρώματα (background, status bar) ταιριάζουν με το branding.

---
> [!NOTE]
> Μετά από αυτό το βήμα, θα έχεις μια εφαρμογή που μπορείς να κάνεις sideload, η οποία όμως θα είναι "άδεια" λειτουργικά.
