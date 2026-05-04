# Technical Overview

Αυτό το αρχείο περιγράφει σε high level πώς λειτουργεί σήμερα η JavaScript πλευρά της εφαρμογής.

## 1. Γενική αρχιτεκτονική

Η εφαρμογή είναι client-side only. Δεν υπάρχει custom backend και δεν υπάρχει build step.

Η λογική χωρίζεται σε τρία επίπεδα:

- model layer με τις κλάσεις `Connection` και `Network`
- page scripts που διαβάζουν/γράφουν δεδομένα και συνδέουν events με το UI
- HTML pages που φορτώνουν τα scripts με συγκεκριμένη σειρά

Βασικές σελίδες:

- [public/index.html](../public/index.html): dashboard με λίστες επαφών
- [public/app/add-connection.html](../public/app/add-connection.html): φόρμα προσθήκης
- [public/app/edit-connection.html](../public/app/edit-connection.html): φόρμα επεξεργασίας/διαγραφής

Βασικά scripts:

- [public/js/connection.js](../public/js/connection.js): class για μία επαφή
- [public/js/network.js](../public/js/network.js): class για συλλογή επαφών και persistence
- [public/js/script.js](../public/js/script.js): dashboard rendering και actions
- [public/js/add-connection.js](../public/js/add-connection.js): logic για create flow
- [public/js/edit-connection.js](../public/js/edit-connection.js): logic για edit/delete flow
- [public/js/Q.js](../public/js/Q.js): helper library για DOM επιλογή, event binding και μικρά utilities

## 2. Script loading order

Η σωστή σειρά φόρτωσης στα pages είναι σημαντική:

1. `Q.js`
2. `connection.js`
3. `network.js`
4. page-specific script

Ο λόγος είναι ότι:

- το `Network` χρειάζεται το `Connection`
- τα page scripts χρειάζονται και το `Q` και το `Network`

## 3. Η class `Connection`

Η `Connection` αναπαριστά μία επαφή. Είναι το domain object της εφαρμογής.

Βασικά fields:

- `id`: μοναδικό id string
- `name`: όνομα επαφής
- `communicationFrequencyDays`: κάθε πόσες μέρες πρέπει να γίνεται επικοινωνία
- `lastCommunicationDate`: τελευταία πραγματοποιημένη επικοινωνία σε μορφή `YYYY-MM-DD` ή `null`
- `scheduledNextCommunicationDate`: προγραμματισμένη επόμενη επικοινωνία σε μορφή `YYYY-MM-DD` ή `null`
- `createdAt`: ημερομηνία δημιουργίας της επαφής σε μορφή `YYYY-MM-DD`

Βασικά computed properties:

- `nextCommunication`
  - αν υπάρχει `scheduledNextCommunicationDate`, χρησιμοποιείται αυτό
  - αλλιώς υπολογίζεται από `lastCommunicationDate`
  - αν δεν υπάρχει τελευταία επικοινωνία, υπολογίζεται από `createdAt`
- `isOverdue`: true όταν η επόμενη επικοινωνία είναι πριν από σήμερα
- `isToday`: true όταν η επόμενη επικοινωνία είναι σήμερα
- `daysUntilNext`: διαφορά ημερών από σήμερα μέχρι την επόμενη επικοινωνία

Βοηθητικές μέθοδοι:

- `toPlain()`: μετατρέπει το instance σε plain object για αποθήκευση
- `fromPlain(obj)`: κάνει hydrate ένα plain object ξανά σε `Connection`
- `_today()`: επιστρέφει τη σημερινή ημερομηνία ως `YYYY-MM-DD`
- `_addDays(dateStr, days)`: προσθέτει ημέρες σε string ημερομηνίας
- `_generateId()`: φτιάχνει ένα απλό id string

## 4. Η class `Network`

Η `Network` είναι το application-level container για όλες τις επαφές του χρήστη.

Ρόλοι της:

- φόρτωση δεδομένων από `localStorage`
- μετατροπή raw data σε `Connection` instances
- CRUD operations πάνω στις επαφές
- αποθήκευση πίσω στο `localStorage`
- helper actions για business flows του dashboard

Βασικές public μέθοδοι:

- `getConnections()`
- `getConnection(id)`
- `addConnection(data)`
- `updateConnection(id, data)`
- `removeConnection(id)`
- `markCommunicated(id)`
- `postponeToTomorrow(id)`

Business rules που υλοποιεί:

- `markCommunicated(id)`
  - βάζει `lastCommunicationDate` = σήμερα
  - καθαρίζει το `scheduledNextCommunicationDate` μόνο αν είναι σήμερα ή έχει ήδη περάσει
  - αν η προγραμματισμένη ημερομηνία είναι στο μέλλον, την κρατάει
- `postponeToTomorrow(id)`
  - βάζει `scheduledNextCommunicationDate` = αύριο
  - αυτό κάνει override την ημερομηνία που θα προέκυπτε από τη συχνότητα

## 5. `localStorage`

Η εφαρμογή χρησιμοποιεί ένα μόνο storage key:

- `hearth_connections`

Η αποθήκευση γίνεται ως `JSON string`.

Στη μνήμη η `Network` κρατάει array από `Connection` instances.
Στο `localStorage` αποθηκεύεται array από plain objects.

Παράδειγμα μορφής:

```json
[
  {
    "id": "maf9j2abx4kq1",
    "name": "Νίκος",
    "communicationFrequencyDays": 7,
    "lastCommunicationDate": "2026-05-01",
    "scheduledNextCommunicationDate": null,
    "createdAt": "2026-04-20"
  },
  {
    "id": "maf9j3cde8pzm",
    "name": "Μαρία",
    "communicationFrequencyDays": 30,
    "lastCommunicationDate": null,
    "scheduledNextCommunicationDate": "2026-05-10",
    "createdAt": "2026-05-04"
  }
]
```

Flow persistence:

1. Η `Network` κάνει `localStorage.getItem('hearth_connections')`
2. Κάνει `JSON.parse(...)`
3. Κάνει hydrate κάθε object με `Connection.fromPlain`
4. Όταν υπάρχει αλλαγή, κάνει `toPlain()` σε κάθε instance
5. Κάνει `JSON.stringify(...)`
6. Αποθηκεύει ξανά με `localStorage.setItem(...)`

## 6. Dashboard flow

Το [public/js/script.js](../public/js/script.js) είναι το page script του dashboard.

Τι κάνει:

- δημιουργεί ένα `new Network()` instance
- διαβάζει όλες τις επαφές με `getConnections()`
- τις ταξινομεί με βάση το `nextCommunication`
- τις χωρίζει σε 3 buckets:
  - overdue
  - today
  - upcoming
- κάνει render cards στο DOM

Dashboard actions:

- `✓` κουμπί: καλεί `network.markCommunicated(id)` και κάνει rerender
- `+1` κουμπί: καλεί `network.postponeToTomorrow(id)` και κάνει rerender
- `✎` link: πηγαίνει στη σελίδα επεξεργασίας με query parameter `?id=...`

Το overdue section κρύβεται τελείως όταν δεν έχει items.

## 7. Add flow

Το [public/js/add-connection.js](../public/js/add-connection.js) κάνει τα εξής:

1. δημιουργεί `new Network()`
2. ακούει submit στο form
3. διαβάζει `name`, `communicationFrequencyDays`, `lastCommunicationDate`
4. καλεί `network.addConnection(...)`
5. κάνει redirect στο dashboard

Αν `lastCommunicationDate` είναι κενό, αποθηκεύεται ως `null`.

## 8. Edit/Delete flow

Το [public/js/edit-connection.js](../public/js/edit-connection.js) κάνει τα εξής:

1. διαβάζει το `id` από το URL μέσω `Q.url.get('id')`
2. αν λείπει ή δεν βρεθεί επαφή, επιστρέφει στο dashboard
3. γεμίζει το form με τα υπάρχοντα δεδομένα
4. στο submit καλεί `network.updateConnection(...)`
5. στο delete καλεί `network.removeConnection(...)`

Επιπλέον:

- αν η αποθηκευμένη συχνότητα δεν υπάρχει στα predefined `<option>`, προστίθεται δυναμικά νέο option ώστε να μη χαθεί η τιμή

## 9. Date handling

Η εφαρμογή δουλεύει μόνο με ημερομηνίες και όχι ώρες.

Format που χρησιμοποιείται παντού:

- `YYYY-MM-DD`

Αυτό βοηθάει ώστε:

- να ταιριάζει με τα native `<input type="date">`
- να είναι απλή η αποθήκευση σε string μορφή
- να μπορεί να γίνει λεξικογραφική σύγκριση σε αρκετά σημεία όπως το `nextCommunication.localeCompare(...)`

## 10. Q.js usage

Η εφαρμογή βασίζεται στο `Q.js` αντί για άμεσο `document.querySelector` usage στα page scripts.

Συνήθη patterns:

- `Q('#id').element` για πρόσβαση στο raw DOM element
- `Q('#id').on('event', handler)` για listeners
- `Q('.selector').on(...)` για delegated events
- `Q('#id').show(condition)` για hide/show με `d-none`

Αυτό φαίνεται ιδιαίτερα στο dashboard, όπου τα action buttons γίνονται render δυναμικά και τα click handlers δουλεύουν μέσω delegated events.

## 11. PWA notes

Σε high level, το PWA layer είναι ξεχωριστό από την business logic.

Αρχεία:

- [public/pwa/manifest.json](../public/pwa/manifest.json)
- [public/pwa/pwa.js](../public/pwa/pwa.js)
- [public/service-worker.js](../public/service-worker.js)

Ρόλος τους:

- manifest: metadata και icons
- pwa.js: registration του service worker
- service-worker.js: caching strategy για offline shell

## 12. Τρέχοντα όρια της υλοποίησης

Αυτή τη στιγμή η υλοποίηση:

- δεν έχει Firestore persistence
- δεν έχει authentication flow
- δεν κρατάει ιστορικό επικοινωνιών
- δεν έχει categories ή tags
- δεν έχει form validation πέρα από βασικούς ελέγχους presence
- δεν έχει automated tests

## 13. Αν θέλεις να κάνεις extend το app

Τα πιο φυσικά σημεία επέκτασης είναι:

- προσθήκη extra πεδίων στο `Connection`
- versioning ή migration του storage format
- abstraction layer πάνω από `Network` για μελλοντικό Firestore sync
- shared utility για validation/date formatting
- περισσότερα dashboard filters ή sorting options