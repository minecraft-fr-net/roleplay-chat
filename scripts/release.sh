#!/usr/bin/env bash
# Usage: ./scripts/release.sh <base_version>
# Example: ./scripts/release.sh 1.4.3
#
# Bumps mod_version in gradle.properties, commits, tags, and pushes.
# The tag push triggers the publish workflow.

set -euo pipefail

BASE_VERSION="${1:-}"
if [[ -z "$BASE_VERSION" ]]; then
  echo "Usage: $0 <base_version>  (e.g., 1.4.3)" >&2
  exit 1
fi

# Read current Minecraft version
MC_VERSION=$(grep '^minecraft_version=' gradle.properties | cut -d= -f2-)
if [[ -z "$MC_VERSION" ]]; then
  echo "Error: minecraft_version not found in gradle.properties" >&2
  exit 1
fi

FULL_VERSION="${BASE_VERSION}+${MC_VERSION}"
TAG="V${FULL_VERSION}"

# Check the tag doesn't already exist
if git rev-parse "$TAG" >/dev/null 2>&1; then
  echo "Error: tag $TAG already exists. Delete it first:" >&2
  echo "  git tag -d $TAG && git push origin :refs/tags/$TAG" >&2
  exit 1
fi

# Update mod_version in gradle.properties
sed -i.bak "s|^mod_version=.*|mod_version=${FULL_VERSION}|" gradle.properties
rm -f gradle.properties.bak

echo "mod_version → ${FULL_VERSION}"

# Commit and tag
git add gradle.properties
git commit -m "🔖  Set version ${FULL_VERSION}"
git tag "${TAG}"

# Push
git push
git push origin "${TAG}"

echo ""
echo "✅ Released ${TAG} — publish workflow triggered."
