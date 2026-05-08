# Rapport de projet - Application Android de gestion hoteliere

## 1. Titre du projet

Conception et realisation d une application Android de gestion hoteliere en Java avec SQLite local.

## 2. Membres du groupe

- A completer

## 3. Contexte et objectif

Ce projet a pour objectif de developper une application mobile Android permettant de gerer les operations essentielles d un hotel sans recourir a un backend externe. L application doit fonctionner localement sur smartphone ou emulateur Android et utiliser SQLite comme base de donnees embarquee.

Les objectifs principaux etaient les suivants :

- gerer les clients de l hotel ;
- gerer les chambres et leur disponibilite ;
- enregistrer les reservations avec controle de disponibilite ;
- enregistrer la consommation des services durant le sejour ;
- calculer automatiquement la facture finale du client.

## 4. Outils et technologies utilises

- Langage : Java
- Plateforme : Android
- Base de donnees : SQLite via `SQLiteOpenHelper`
- Environnement de developpement : VSCode sous Windows 11
- Build : Gradle en ligne de commande avec `gradlew.bat`
- Composants UI : AndroidX, Material Components, RecyclerView

## 5. Description generale de l application

L application "HotelManager" propose une gestion complete d un hotel a travers plusieurs ecrans specialises :

- un tableau de bord principal affichant les statistiques globales ;
- un module de gestion des clients ;
- un module de gestion des chambres ;
- un module de reservation ;
- un module de services associes a une reservation ;
- un module de facturation et de cloture de sejour.

L objectif de conception etait d offrir une interface claire, sobre et professionnelle, facile a utiliser par un receptionniste ou un gestionnaire d hotel.

## 6. Fonctionnalites implementees

### 6.1 Tableau de bord

Le tableau de bord affiche en temps reel les informations suivantes :

- nombre de chambres disponibles ;
- nombre de reservations actives ;
- nombre total de clients ;
- nombre de check-outs du jour.

Il propose aussi des actions rapides permettant d acceder directement aux modules de gestion des clients, chambres et reservations.

### 6.2 Gestion des clients

Le module client permet :

- d afficher la liste des clients ;
- de rechercher un client par nom, prenom, telephone ou numero de passeport ;
- d ajouter un nouveau client via un formulaire structure ;
- d afficher le statut du client : actif, en sejour ou archive.

### 6.3 Gestion des chambres

Le module chambre permet :

- d afficher toutes les chambres ;
- de visualiser le taux d occupation ;
- de filtrer les chambres par statut : disponible, occupee, maintenance ;
- d ajouter une nouvelle chambre avec numero, type, prix, capacite et equipements.

### 6.4 Gestion des reservations

Le module reservation permet :

- de selectionner un client ;
- de selectionner une chambre ;
- de definir la date d arrivee et la date de depart ;
- de saisir le nombre de personnes ;
- de calculer automatiquement un recapitulatif du cout du sejour ;
- de verifier la disponibilite de la chambre via une transaction SQLite.

La reservation est protegee par un controle de validite des dates, de la capacite de la chambre et des chevauchements de sejour.

### 6.5 Gestion des services

Le module service permet :

- d afficher la liste des services disponibles ;
- d ajouter un service a une reservation ;
- de cumuler les prestations consommees ;
- de recalculer automatiquement le montant total des services.

### 6.6 Facturation et cloture de sejour

Le module facture permet :

- d afficher les informations du client ;
- d afficher les informations de la chambre et du sejour ;
- d afficher la liste des services consommes ;
- de calculer le sous-total chambre, le sous-total services et le total general ;
- de partager la facture via les applications Android disponibles ;
- de fermer le sejour et liberer la chambre via transaction SQLite.

## 7. Structure de la base de donnees

Le projet repose sur cinq tables principales :

- `clients`
- `chambres`
- `reservations`
- `services`
- `consommation_services`

Le diagramme des tables est fourni dans le fichier :

```text
docs/diagramme_bdd.md
```

## 8. Captures d ecran a inserer dans la version finale

Inserer ici les captures suivantes :

- capture 1 : ecran d accueil / tableau de bord ;
- capture 2 : liste des clients ;
- capture 3 : formulaire d ajout client ;
- capture 4 : liste des chambres ;
- capture 5 : formulaire de reservation ;
- capture 6 : ecran des services ;
- capture 7 : facture finale ;
- capture 8 : partage de facture ou cloture du sejour.

Format conseille pour chaque capture :

- titre de l ecran ;
- courte explication de ce que l on voit ;
- capture centree dans le document.

## 9. Diagramme logique et relations

L application suit une logique simple :

1. un client est enregistre ;
2. une chambre est definie avec ses caracteristiques ;
3. une reservation relie un client a une chambre sur une plage de dates ;
4. plusieurs services peuvent etre ajoutes a cette reservation ;
5. la facture finale est calculee a partir du prix des nuits et des services consommes.

## 10. Gestion des erreurs et validations

Plusieurs mecanismes de validation ont ete mis en place :

- verification des champs obligatoires dans les formulaires ;
- verification du format de l email ;
- verification du prix et de la capacite des chambres ;
- verification des dates de reservation ;
- verification de la capacite maximale de la chambre ;
- verification de la disponibilite de la chambre avant insertion ;
- messages `Toast` ou erreurs de formulaire en cas de probleme.

## 11. Difficultes rencontrees

Les principales difficultes rencontrees au cours du projet ont ete :

- la mise en place du projet Android dans VSCode sans Android Studio ;
- la gestion complete des ressources Android en Java et XML ;
- la verification de disponibilite des chambres sans backend externe ;
- la synchronisation des montants de reservation et des consommations de services ;
- l harmonisation du design pour obtenir un rendu plus professionnel.

## 12. Solutions apportees

Pour repondre a ces difficultes :

- le projet a ete structure avec Gradle wrapper pour permettre la compilation en ligne de commande ;
- la base SQLite a ete geree via `SQLiteOpenHelper` ;
- les reservations ont ete protegees par des transactions SQLite ;
- les interfaces ont ete recomposees avec des cartes Material, des couleurs coherentes et des formulaires plus lisibles ;
- une fonctionnalite supplementaire de partage de facture a ete ajoutee pour enrichir l usage reel de l application.

## 13. Ameliorations possibles

Plusieurs pistes d amelioration peuvent etre envisagees :

- ajout d une authentification utilisateur ;
- export PDF de la facture ;
- historique complet des reservations avec filtres avances ;
- ajout d une gestion de maintenance planifiee ;
- statistiques mensuelles des revenus et du taux d occupation ;
- synchronisation cloud ou sauvegarde distante dans une version future.

## 14. Innovation apportee

Une fonctionnalite supplementaire a ete ajoutee pour renforcer la valeur pratique de l application :

- le partage de la facture via les applications Android (messagerie, email, notes, etc.).

Cette innovation permet au receptionniste d envoyer rapidement les details du sejour au client ou a l administration sans developper de backend externe.

## 15. Conclusion

Ce projet a permis de concevoir une application Android complete de gestion hoteliere respectant les contraintes imposees : Java, SQLite natif, absence de backend externe et developpement sous VSCode avec Gradle en ligne de commande.

L application couvre les besoins essentiels d un hotel : gestion des clients, chambres, reservations, services et facturation. Les validations, les transactions SQLite et l interface Material renforcent la fiabilite et l ergonomie de la solution.

Le projet reste evolutif et peut servir de base solide pour une version plus avancee dans un contexte professionnel reel.
