# Changelog

## [2.0.0+1.21.6] - 2026-06-18

### Nouveautés
- ✨ Implement configurable message types with customizable settings for roleplay chat
- ✨ feat: système de pseudo roleplay (/rpname)
- ✨ feat: afficher le username MC sous le nom RP dans le nametag
- ✨ feat: afficher le username MC au survol du nom RP dans le chat
- ✨ feat: afficher le MC username en dessous du pseudo RP dans le nametag

### Documentation
- 📝  Update readme

### Divers
- 🔧 Update dependencies and improve compatibility with Minecraft 1.21.11
- 🔖 Set version 1.1.0+1.21.11
- 🔧 Rétrograder vers Minecraft 1.21.6 et corriger les incompatibilités d'API
- ✅ Ajouter les Gametests serveur pour les préfixes et rayons des messages
- ✅ Ajouter le test client Gametest avec screenshots du chat
- 🔧 Migrer la validation du wrapper Gradle vers gradle/actions@v4
- ✅ Vérifier l'affichage du chat par comparaison de screenshots
- 🔧 Retirer org.gradle.java.home du projet
- ✅ Ajouter le test visuel de filtrage par rayon (speak radius)
- 💬 Remplacer les messages système par des messages joueur signés
- 🖼️ Régénérer les templates de screenshots après refonte des messages
- ✅ Utiliser le vrai flow de chat dans ChatRadiusDisplayTest
- 🎲 Ajouter la commande /roll avec critiques et i18n
- ✅ test: gametest visuel du pseudo RP + username fixe
- ✅ test: gametest visuel du nameplate RP avec joueur mock
- 👷 Mettre en place la CI et le script de release
- 🚚 Déplacer le script de release dans bin/
- 🔧 Exclure bin/release.sh du gitignore
