# Internal Implementation Plan - Hearth Android Migration

## Phase 2: Branding & Identity (Next Step)
- [ ] **App Name & Package**: Define the package name (e.g., `com.dimitris.hearth`).
- [ ] **Resources**:
    - [ ] Import fonts (if any specific ones are used, otherwise system fonts).
    - [ ] Import/Convert SVG icons to Vector Drawables (Settings, Edit, Check, etc.).
    - [ ] Define HSL colors in `Theme.kt`.
    - [ ] Create adaptive launcher icon using the Hearth logo.
- [ ] **Splash Screen**: Implement the modern Android Splash Screen API.
- [ ] **Activity/Theme Setup**: Basic `MainActivity` with the custom theme.

## Phase 3: Core Functionality (Data & UI)
- [ ] **Data Layer**:
    - [ ] `Connection` Entity (Room).
    - [ ] `ConnectionDao`.
    - [ ] `HearthDatabase`.
    - [ ] `ConnectionRepository`.
- [ ] **Business Logic**:
    - [ ] Port date calculation logic from `Connection.js`.
- [ ] **Navigation**:
    - [ ] Set up Compose Navigation (Dashboard -> Add/Edit -> Settings).
- [ ] **Dashboard**:
    - [ ] `DashboardViewModel` with `StateFlow`.
    - [ ] Compose components: `ConnectionCard`, `SectionHeader`, `DashboardList`.
- [ ] **Add/Edit Screens**:
    - [ ] `EditViewModel` with auto-save logic for existing connections.
    - [ ] Shared form components.

## Phase 4: Settings & Notifications
- [ ] **Settings**:
    - [ ] `DataStore` for persisting reminder settings (Enabled, Time).
- [ ] **Notifications**:
    - [ ] `WorkManager` for daily checks.
    - [ ] Notification Channel setup.
    - [ ] `NotificationBroadcastReceiver`.
