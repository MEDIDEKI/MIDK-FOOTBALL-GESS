# ⚽ Football Guess (Devine le Joueur)

Application Android moderne, native et 100% hors-ligne (offline) d'un jeu de devinettes footballistiques en face à face, par équipes et contre une IA tactique.

---

## 🎯 Concept du Jeu

Deux joueurs ou deux équipes s'affrontent face à face. L'application attribue secrètement un joueur de football à chaque camp avec **exactement le même niveau de difficulté**.

* Le **Joueur / Équipe A** doit deviner le joueur secret de **B**.
* Le **Joueur / Équipe B** doit deviner le joueur secret de **A**.
* Les participants échangent oralement en face à face avec des questions fermées (réponse par **OUI** ou **NON**).
* L'application sert à sélectionner les joueurs, gérer le tableau d'affichage, proposer des questions stratégiques, détecter d'éventuelles incohérences factuelles et arbitrer le duel.

---

## 🚀 Fonctionnalités Clés

1. **Modes de Jeu Complets** :
   - **1 contre 1** : Duel classique face à face sur le même appareil avec écran de confidentialité.
   - **2 contre 2** : Match par équipes (Équipe A vs Équipe B).
   - **Joueur vs Robot IA** : Affronte une IA tactique à 4 niveaux (Facile, Normal, Difficile, Expert) qui analyse l'espace des candidats, calcule la division binaire d'entropie d'information et répond honnêtement selon son joueur secret.

2. **5 Niveaux de Difficulté Symétriques** :
   - 🟢 **Très facile** (Score 1) : Superstars mondiales incontournables (Messi, Ronaldo, Zidane, Pelé, etc.).
   - 🔵 **Facile** (Score 2) : Joueurs très connus du grand public (Pirlo, Drogba, Kane, Buffon, etc.).
   - 🟡 **Moyen** (Score 3) : Joueurs renommés et grands internationaux (Kroos, Forlán, Sneijder, etc.).
   - 🟠 **Difficile** (Score 4) : Piliers d'équipes prestigieuses ou époques ciblées (Puyol, Vieira, Ziyech, etc.).
   - 🔴 **Expert** (Score 5) : Légendes historiques et spécialistes du football (Di Stéfano, Puskás, Madjer, etc.).
   - **Règle absolue** : La sélection est indépendante des clubs, de la génération ou de la nationalité, et le niveau de difficulté est rigoureusement identique pour les deux côtés.

3. **Système Anti-Répétition & Cycles** :
   - Sauvegarde locale Room des joueurs déjà utilisés dans `player_usages`.
   - Évite de reproposer les mêmes joueurs tant que la difficulté n'a pas été épuisée.
   - Passage automatique aux cycles suivants (Cycle 1, Cycle 2...) lorsque le pool a été complètement exploré.

4. **Analyse & Détection d'Incohérences Factuelles (IA Locale)** :
   - Vérifie la cohérence des réponses (OUI/NON) par rapport aux attributs certifiés du joueur secret (Poste, Statut actif/retraité, Nationalité, Continent, Palmarès LDC/CdM/Ballon d'Or, Clubs).
   - Trois cas stricts :
     - **Cohérent** (`CORRECT`)
     - **Probablement incorrect** (`LIKELY_INCORRECT` avec avertissement neutre "⚠️ Réponse probablement incorrecte")
     - **Incertain / Libre** (`UNKNOWN` pour les questions custom ou non certifiées)
   - L'adversaire reste toujours responsable et le système n'accuse jamais ("Tu triches").

5. **Sécurité et Confidentialité de l'Écran** :
   - Écran de distribution séquentiel "Révéler mon joueur secrètement" puis "Masquer", évitant toute fuite d'information lorsque l'appareil circule de main en main.
   - Bouton de coup d'œil discret pendant la partie.

6. **Base de Données Locale Intégrée (277 Joueurs)** :
   - Aucun joueur fictif : 277 joueurs certifiés.
   - Explorateur complet avec recherche textuelle et filtres par difficulté et poste.
   - Script de validation automatique au démarrage (`validateDatabase()`).

7. **Persistance 100% Offline (Room Database)** :
   - Historique détaillé des parties (`game_records`).
   - Statistiques globales (`game_stats` : victoires par mode, taux de réussite, questions posées, etc.).
   - Export et import de données au format JSON sans aucun compte ni cloud.

8. **Architecture Réseau Local (LAN / Bluetooth)** :
   - Écran dédié avec statut réseau local, mode hôte/scan et documentation des options sans fil.

---

## 🏗️ Architecture Logicielle

Le projet respecte les principes de l'architecture MVVM et Clean Architecture :

```text
app/src/main/java/com/example/
├── MainActivity.kt                # Point d'entrée avec navigation Compose et BackHandler
├── model/                         # Modèles de données
│   ├── Player.kt                  # Entité joueur, drapeaux et helpers
│   ├── DifficultyLevel.kt         # Niveaux 1 à 5
│   ├── GameMode.kt                # 1v1, 2v2, Robot
│   ├── RobotLevel.kt              # Niveaux d'IA
│   ├── ValidationStatus.kt        # CORRECT, LIKELY_INCORRECT, UNKNOWN
│   ├── QuestionItem.kt            # Historique d'une question
│   └── GameState.kt               # État réactif du match en cours
├── engine/                        # Moteurs de logique métier indépendants de l'UI
│   ├── RandomPlayerEngine.kt      # Tirage équiprobable, indépendant et anti-répétition
│   ├── QuestionEngine.kt          # Suggestions catégorisées et détection de doublons
│   ├── AnswerValidator.kt         # Analyseur des réponses Oui/Non vs faits certifiés
│   └── RobotEngine.kt             # IA robot avec réduction d'entropie et devinette
├── data/
│   ├── database/                  # Room Database, DAO et Entités
│   │   ├── FootballGuessDatabase.kt
│   │   ├── GameDao.kt
│   │   ├── GameRecordEntity.kt
│   │   ├── GameStatsEntity.kt
│   │   └── PlayerUsageEntity.kt
│   └── repository/
│       ├── PlayerRepository.kt    # Chargement assets/players.json et validation
│       └── GameHistoryRepository.kt # Gestion historique, stats et import/export JSON
└── ui/
    ├── components/
    │   └── PlayerCard.kt          # Composant de carte de joueur avec badges et trophées
    ├── screens/
    │   ├── HomeScreen.kt          # Accueil et sélection de mode
    │   ├── SetupScreen.kt         # Configuration de manche et difficulté commune
    │   ├── RevealSecretScreen.kt  # Écran de protection séquentiel
    │   ├── GamePlayScreen.kt      # Écran de jeu, scoreboard, historique et dialogue
    │   ├── StatsScreen.kt         # Vue des statistiques locales
    │   ├── HistoryScreen.kt       # Consultation des parties passées
    │   ├── DatabaseExplorerScreen.kt # Explorateur des 277 joueurs
    │   ├── SettingsScreen.kt      # Paramètres, export/import JSON et son
    │   └── LocalNetworkScreen.kt  # Mode multijoueur local sans fil
    ├── theme/
    │   ├── Color.kt               # Thème vert stade, or trophée et bleu tech
    │   ├── Theme.kt               # Thème Material 3
    │   └── Type.kt
    └── viewmodel/
        └── GameViewModel.kt       # ViewModel centralisé
```

---

## 🧪 Tests Unitaires et Robolectric (14/14 validés)

La suite de tests dans `app/src/test/java/com/example/FootballGuessEngineTest.kt` vérifie l'intégralité des 14 exigences du cahier des charges :

- **Test 1** : Deux joueurs différents sont toujours sélectionnés.
- **Test 2** : Les deux joueurs ont toujours exactement la même difficulté.
- **Test 3** : La nationalité n'influence pas artificiellement le tirage.
- **Test 4** : Le club n'influence pas artificiellement le tirage.
- **Test 5** : La génération (actif / retraité) n'influence pas artificiellement le tirage.
- **Test 6** : Un joueur récemment utilisé est moins susceptible d'être immédiatement sélectionné (anti-répétition).
- **Test 7** : Une réponse incorrecte est signalée comme `LIKELY_INCORRECT` sans modifier la réponse.
- **Test 8** : Une question incompréhensible ou libre produit `UNKNOWN`.
- **Test 9** : La partie fonctionne à 100% sans connexion internet.
- **Test 10** : Les statistiques restent enregistrées après redémarrage (Room).
- **Test 11** : Les données restent persistées et peuvent être exportées/importées en JSON.
- **Test 12** : Le mode 1 contre 1 fonctionne.
- **Test 13** : Le mode 2 contre 2 fonctionne.
- **Test 14** : Le mode Robot IA fonctionne.

Pour exécuter les tests :
```bash
gradle :app:testDebugUnitTest
```

---

## 📦 Instructions de Compilation & Build

### Prérequis
- JDK 17 / 21
- Android SDK 36 (minSdk 24, targetSdk 36)

### Compiler l'application
```bash
gradle assembleDebug
```

L'APK généré sera disponible dans `app/build/outputs/apk/debug/app-debug.apk`.
