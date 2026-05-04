# Hearth

Το Hearth είναι μια απλή web εφαρμογή για να οργανώνεις τις επαφές σου και να θυμάσαι πότε πρέπει να επικοινωνήσεις ξανά με κάθε άτομο.

Η εφαρμογή αυτή τη στιγμή προσφέρει:

- dashboard με ομαδοποίηση επαφών σε `εκπρόθεσμες`, `σήμερα` και `επόμενες`
- προσθήκη νέας επαφής
- επεξεργασία και διαγραφή επαφής
- ορισμό συχνότητας επικοινωνίας
- καταχώριση τελευταίας επικοινωνίας
- προγραμματισμό επόμενης επικοινωνίας που κάνει override τη συχνότητα
- αποθήκευση δεδομένων τοπικά στον browser μέσω `localStorage`
- PWA setup για installability/offline shell

## Τεχνολογίες

- HTML
- CSS
- Vanilla JavaScript
- `Q.js` για απλό DOM manipulation
- `localStorage` για persistence

## Δομή εφαρμογής

- [public/index.html](public/index.html): dashboard
- [public/app/add-connection.html](public/app/add-connection.html): νέα επαφή
- [public/app/edit-connection.html](public/app/edit-connection.html): επεξεργασία επαφής

## Τεκμηρίωση για developer

Για τεχνική περιγραφή της JavaScript αρχιτεκτονικής, του `localStorage` και του data flow, δες το [docs/technical-overview.md](docs/technical-overview.md).