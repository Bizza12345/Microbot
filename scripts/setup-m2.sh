#!/usr/bin/env bash
# Download and extract the prebuilt Maven repository using gdown
set -euo pipefail

ZIP_ID="1KrGFnI5IIJImsfH7jDnrQ7pHeNTNZk8n"
ZIP_FILE="m2.zip"
TARGET_DIR="$HOME/.m2"

mkdir -p "$HOME"

echo "Downloading Maven cache..."
gdown "https://drive.google.com/uc?id=${ZIP_ID}" -O "$ZIP_FILE"

echo "Extracting archive to $TARGET_DIR..."
unzip -o -q "$ZIP_FILE" -d "$HOME"

if [ ! -d "$TARGET_DIR" ]; then
  echo "Failed to extract Maven cache"
  exit 1
fi

rm "$ZIP_FILE"
echo "Maven repository ready in $TARGET_DIR"
