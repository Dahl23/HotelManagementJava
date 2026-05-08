# HotelManager

Application Android de gestion hoteliere en Java avec SQLite natif (`SQLiteOpenHelper`) et build Gradle en ligne de commande, concue pour etre developpee integralement dans VSCode sous Windows 11.

## Extensions VSCode requises

- Extension Pack for Java (Microsoft)
- Android iOS Emulator (Diemas Michiels)
- Gradle for Java (Microsoft)
- XML (Red Hat)

## Stack technique

- Java 17
- Android SDK 36
- Gradle Wrapper 9.3.1
- SQLite local via `SQLiteOpenHelper`
- AndroidX AppCompat, Material Components, RecyclerView
- Aucune bibliotheque metier externe

## Fonctionnalites incluses

- Tableau de bord avec statistiques en temps reel
- Gestion des clients avec recherche et formulaire d ajout
- Gestion des chambres avec filtres et taux d occupation
- Reservation avec controle de disponibilite en transaction SQLite
- Consommation de services liee a une reservation
- Facture detaillee, partage de facture et cloture de sejour en transaction SQLite

## Structure

```text
app/
  src/main/
    java/com/hotel/gestion/
    res/
build.gradle
settings.gradle
gradle.properties
gradlew.bat
```

## Prerequis Windows 11

1. Installer un JDK 17 et definir `JAVA_HOME`.
2. Installer le SDK Android en ligne de commande.
3. Definir `ANDROID_SDK_ROOT` vers votre SDK reel.
4. Installer au minimum :
   - Android SDK Platform 36
   - Android SDK Build-Tools 36.1.0
   - Android SDK Platform-Tools
5. Creer `local.properties` a la racine si necessaire :

```properties
sdk.dir=C:\\Android\\Sdk
```

## Commandes utiles

Depuis PowerShell dans la racine du projet :

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

L APK de debug est genere dans :

```text
app\build\outputs\apk\debug\app-debug.apk
```

## Logo de l application

Un logo par defaut a deja ete ajoute dans :

```text
app/src/main/res/drawable/ic_app_logo.xml
```

Il est relie au manifeste ici :

```text
app/src/main/AndroidManifest.xml
```

Pour le remplacer :

1. Creez un nouveau drawable XML ou un PNG dans `app/src/main/res/drawable/`.
2. Gardez le nom `ic_app_logo` pour remplacer directement l actuel, ou changez la valeur de `android:icon` et `android:roundIcon` dans le manifeste.
3. Recompilez avec `.\gradlew.bat assembleDebug`.

## Notes d architecture

- Les dates sont stockees au format ISO `yyyy-MM-dd`.
- Les reservations sont securisees par `beginTransaction()`, `setTransactionSuccessful()` et `endTransaction()`.
- Le statut des clients est calcule depuis l historique des reservations.
- Le statut des chambres est derive du booleen `disponible` et des reservations en cours.
- La facture peut etre partagee par Android `ACTION_SEND`.

## References utiles

- Build Android en ligne de commande : https://developer.android.com/build/building-cmdline
- Compatibilite Java/AGP : https://developer.android.com/build/jdks
- Notes de version Android Gradle Plugin : https://developer.android.com/build/releases/gradle-plugin
