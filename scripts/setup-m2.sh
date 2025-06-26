#!/usr/bin/env bash
# Download and extract the prebuilt Maven repository
set -euo pipefail

ZIP_URL="https://drive.google.com/uc?export=download&id=1KrGFnI5IIJImsfH7jDnrQ7pHeNTNZk8n"
ZIP_FILE="m2.zip"
TARGET_DIR="$HOME/.m2"

mkdir -p "$TARGET_DIR"

echo "Downloading Maven cache..."
curl -L "$ZIP_URL" -o "$ZIP_FILE"

echo "Extracting archive to $TARGET_DIR..."
unzip -o -q "$ZIP_FILE" -d "$TARGET_DIR"

rm "$ZIP_FILE"
echo "Maven repository ready in $TARGET_DIR"
