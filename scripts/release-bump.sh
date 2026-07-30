#!/usr/bin/env bash
# Bump Semver (major | minor | patch), met à jour mod_version dans gradle.properties
# (format <semver>+<minecraft_version>), commit, tag V..., push.
#
# Usage:
#   ./scripts/release-bump.sh           # interactif
#   ./scripts/release-bump.sh --dry-run # affiche sans modifier ni git
#
# Prérequis : dépôt git propre (rien en staging ni modifications non commitées),
# branche courante (pas en detached HEAD), remote origin configuré.

set -euo pipefail

ROOT="$(git rev-parse --show-toplevel 2>/dev/null)" || {
  echo "Erreur : exécuter depuis un dépôt git." >&2
  exit 1
}
cd "$ROOT"
GRADLE_PROPERTIES="$ROOT/gradle.properties"

DRY_RUN=0
for arg in "$@"; do
  case "$arg" in
    --dry-run) DRY_RUN=1 ;;
    -h | --help)
      sed -n '1,12p' "$0"
      exit 0
      ;;
  esac
done

[[ -f "$GRADLE_PROPERTIES" ]] || {
  echo "Erreur : $GRADLE_PROPERTIES introuvable." >&2
  exit 1
}

if [[ -n "$(git status --porcelain)" ]]; then
  echo "Erreur : dépôt non propre (fichiers modifiés ou indexés). Committez ou stash avant." >&2
  git status -s
  exit 1
fi

BRANCH="$(git branch --show-current)"
if [[ -z "$BRANCH" ]]; then
  echo "Erreur : HEAD détaché. Positionnez-vous sur une branche." >&2
  exit 1
fi

get_prop() {
  local key="$1"
  grep "^${key}=" "$GRADLE_PROPERTIES" | head -1 | cut -d= -f2-
}

MINECRAFT_VERSION="$(get_prop minecraft_version)"
MOD_VERSION_LINE="$(get_prop mod_version)"

if [[ -z "$MINECRAFT_VERSION" ]]; then
  echo "Erreur : minecraft_version absent de gradle.properties." >&2
  exit 1
fi
if [[ -z "$MOD_VERSION_LINE" ]]; then
  echo "Erreur : mod_version absent de gradle.properties." >&2
  exit 1
fi

# Partie semver seule (avant le premier +)
if [[ "$MOD_VERSION_LINE" == *+* ]]; then
  SEMVER_BASE="${MOD_VERSION_LINE%%+*}"
else
  SEMVER_BASE="$MOD_VERSION_LINE"
fi

IFS='.' read -r MA MI PA <<<"$SEMVER_BASE"
MA=${MA:-0}
MI=${MI:-0}
PA=${PA:-0}

bump_semver() {
  local kind="$1"
  case "$kind" in
    major) echo "$((MA + 1)).0.0" ;;
    minor) echo "${MA}.$((MI + 1)).0" ;;
    patch) echo "${MA}.${MI}.$((PA + 1))" ;;
    *)
      echo "internal error" >&2
      exit 1
      ;;
  esac
}

echo "Version actuelle (gradle) : mod_version=$MOD_VERSION_LINE (semver: $SEMVER_BASE)"
echo "minecraft_version=$MINECRAFT_VERSION"
echo ""
echo "Type d'incrément ?"
echo "  1) patch  (correctifs)"
echo "  2) minor  (fonctionnalités)"
echo "  3) major  (cassant)"
read -r -p "Choix [1-3] : " CHOICE

case "$CHOICE" in
  1) KIND="patch" ;;
  2) KIND="minor" ;;
  3) KIND="major" ;;
  *)
    echo "Choix invalide." >&2
    exit 1
    ;;
esac

NEW_SEMVER="$(bump_semver "$KIND")"
NEW_MOD_VERSION="${NEW_SEMVER}+${MINECRAFT_VERSION}"
TAG_NAME="V${NEW_MOD_VERSION}"

echo ""
echo "Nouvelle mod_version : $NEW_MOD_VERSION"
echo "Tag à créer         : $TAG_NAME"
echo ""

if [[ "$DRY_RUN" -eq 1 ]]; then
  echo "[dry-run] Aucune modification ni opération git."
  exit 0
fi

read -r -p "Confirmer commit + tag + push ? [y/N] " CONF
case "$CONF" in
  y | Y | yes | o | O | oui) ;;
  *)
    echo "Annulé."
    exit 1
    ;;
esac

replace_mod_version() {
  local new="$1"
  if sed --version 2>/dev/null | grep -q GNU; then
    sed -i "s/^mod_version=.*/mod_version=${new}/" "$GRADLE_PROPERTIES"
  else
    sed -i '' "s/^mod_version=.*/mod_version=${new}/" "$GRADLE_PROPERTIES"
  fi
}

replace_mod_version "$NEW_MOD_VERSION"

git add gradle.properties
git commit -m "🔖 set version ${NEW_MOD_VERSION}"

git tag -a "$TAG_NAME" -m "Release ${NEW_MOD_VERSION}"

git push origin "$BRANCH"
git push origin "$TAG_NAME"

echo ""
echo "Terminé : commit, tag $TAG_NAME et push effectués."
