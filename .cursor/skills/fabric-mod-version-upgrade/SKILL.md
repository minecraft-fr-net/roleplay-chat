---
name: fabric-mod-version-upgrade
description: Met à jour un mod Fabric depuis la version Minecraft indiquée dans gradle.properties vers une version cible fournie par l’utilisateur. Consulte le blog Fabric pour les articles couvrant l’intervalle de versions, applique les changements (code, mixins, API), synchronise gradle.properties via fabricmc.net/develop (bloc Latest Versions), puis valide avec j21 et ./gradlew build. À utiliser quand l’utilisateur demande de passer à une autre version Minecraft, de bump Fabric/Yarn/Loom/API, ou d’appliquer cette skill explicitement.
---

# Mise à jour de version d’un mod Fabric

## Prérequis communiqués à l’utilisateur

- La **version cible** Minecraft doit être fournie explicitement (ex. `1.21.11`) si ce n’est pas déjà clair dans le message.
- **Java 21** est requise pour les versions récentes du jeu et de Loom (souvent alignée avec le template officiel).

## 1. Point de départ : version actuelle

1. Lire `gradle.properties` et noter au minimum :
   - `minecraft_version`
   - `yarn_mappings`
   - `loader_version`
   - `loom_version`
   - `fabric_api_version`
   - `mod_version` (souvent aligné sur la version MC)
2. Lire `src/main/resources/fabric.mod.json` : mettre à jour `depends.minecraft` (et si besoin `depends.fabricloader`) pour refléter la cible.
3. Parcourir le code (mixins, appels Yarn/Mojang mappings, Fabric API) pour repérer ce qui est sensible aux changements de version.

## 2. Blog Fabric : articles à couvrir

**Source :** [Blog Fabric](https://fabricmc.net/blog/)

**Objectif :** pour chaque saut de version entre la version **actuelle** et la **cible**, identifier et lire **tous** les articles de release qui couvrent des versions dans cet intervalle (titres du type « Fabric for Minecraft 1.21.x & 1.21.y » ou équivalent).

**Méthode :**

1. Ouvrir la liste des articles du blog (navigation ou page d’accueil du blog).
2. Pour chaque article pertinent entre l’ancienne et la nouvelle version :
   - noter les breaking changes, dépréciations, changements Loom/Yarn/Loader ;
   - appliquer les adaptations dans le projet (refactors d’API, renommages, mixins, `fabric.mod.json`, Gradle si mentionné).
3. Ne pas se limiter au premier article : un passage de `1.21.6` à `1.21.11` peut nécessiter **plusieurs** articles (ex. un post groupant 1.21.6–1.21.8, un autre 1.21.9–1.21.10, puis celui pour 1.21.11), comme dans l’exemple utilisateur.

Si un article mentionne le dépôt [fabric-example-mod](https://github.com/FabricMC/fabric-example-mod) ou la doc, s’y référer pour les patterns à jour.

## 3. gradle.properties : « Latest Versions » sur Develop

**Page :** [Develop | Fabric](https://fabricmc.net/develop/)

1. Aller à la section **Latest Versions** (souvent chargée dynamiquement sur la page).
2. **Si le HTML brut (fetch, `curl`, etc.) ne contient pas le snippet** : ne pas s’arrêter là — utiliser l’**outil navigateur** de Cursor (`navigate` vers la page Develop, attendre le chargement, `snapshot` pour lire le DOM rendu). Le bloc équivalent au **`<pre><code>`** n’est visible qu’après exécution du JavaScript de la page.
3. Utiliser le sélecteur **Minecraft Version** et choisir la **version cible**.
4. Sous le sélecteur, copier le contenu exact du bloc dans la balise **`<pre><code>`** (ou le bloc de code rendu à cet emplacement dans le snapshot navigateur).
5. Fusionner ce snippet avec le `gradle.properties` du projet :
   - mettre à jour les clés officielles (`minecraft_version`, `yarn_mappings`, `loader_version`, `loom_version`, `fabric_api_version`, etc.) ;
   - **conserver** les clés propres au mod (`mod_version`, `maven_group`, `archives_base_name`, options JVM Gradle, etc.) ;
   - aligner `mod_version` sur la convention du projet (souvent suffixe `+1.xx.yy`).

Vérifier aussi `settings.gradle` / `gradle/wrapper/gradle-wrapper.properties` si le blog ou le snippet indique une exigence Gradle plus récente.

## 4. Autres fichiers à contrôler

- `build.gradle` / `settings.gradle` : versions de plugins, dépôts, options Java (`release` 21, compatibilité).
- Mixins et ressources : chemins ou noms touchés par les mappings.
- Toute dépendance mod publiée par version de Minecraft.

## 5. Build et Java 21

1. **Avant** `./gradlew build`, activer Java 21 dans le shell :
   ```bash
   j21
   ```
2. Vérifier que Java 21 est bien disponible (ex. `java -version` doit indiquer 21). Si `j21` n’existe pas ou si Java 21 n’est pas installée : **l’indiquer clairement à l’utilisateur** et ne pas prétendre que le build est validé.
3. Lancer depuis la racine du projet :
   ```bash
   ./gradlew build
   ```
4. En cas d’échec : corriger à partir des messages Gradle/Java, du blog et de la doc Fabric, puis relancer jusqu’à succès ou blocage documenté.

## 6. Synthèse pour l’utilisateur

À la fin, résumer en français :

- version d’origine → version cible ;
- articles du blog consultés (titres ou plages de versions) ;
- changements principaux dans `gradle.properties`, `fabric.mod.json` et code ;
- résultat de `./gradlew build` ou ce qui manque côté Java.

## Ressources

- [Blog Fabric](https://fabricmc.net/blog/)
- [Develop | Fabric](https://fabricmc.net/develop/) (Latest Versions)
- [Documentation Fabric](https://docs.fabricmc.net/)
