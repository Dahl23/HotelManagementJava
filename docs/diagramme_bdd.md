# Diagramme simple des tables SQLite

## Vue relationnelle

```mermaid
erDiagram
    CLIENTS ||--o{ RESERVATIONS : "possede"
    CHAMBRES ||--o{ RESERVATIONS : "attribuee a"
    RESERVATIONS ||--o{ CONSOMMATION_SERVICES : "genere"
    SERVICES ||--o{ CONSOMMATION_SERVICES : "reference"

    CLIENTS {
        INTEGER id PK
        TEXT nom
        TEXT prenom
        TEXT email
        TEXT telephone
        TEXT adresse
        TEXT numero_passeport
        TEXT date_naissance
    }

    CHAMBRES {
        INTEGER id PK
        TEXT numero
        TEXT type
        REAL prix_nuit
        INTEGER capacite
        TEXT equipements
        INTEGER disponible
    }

    RESERVATIONS {
        INTEGER id PK
        INTEGER client_id FK
        INTEGER chambre_id FK
        TEXT date_debut
        TEXT date_fin
        INTEGER nombre_personnes
        TEXT statut
        REAL montant_total
        TEXT date_reservation
    }

    SERVICES {
        INTEGER id PK
        TEXT nom
        TEXT description
        REAL prix
    }

    CONSOMMATION_SERVICES {
        INTEGER id PK
        INTEGER reservation_id FK
        INTEGER service_id FK
        INTEGER quantite
        TEXT date_consommation
        REAL prix_unitaire
    }
```

## Lecture rapide

- Un client peut avoir plusieurs reservations.
- Une chambre peut etre utilisee dans plusieurs reservations, a des dates differentes.
- Une reservation peut contenir plusieurs consommations de services.
- Une consommation relie une reservation a un service avec quantite et prix unitaire.
