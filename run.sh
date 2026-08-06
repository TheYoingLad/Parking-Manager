#!/usr/bin/env bash
set -e

OUT="out/production/Parking-Manager"
CP="lib/gson-2.11.0.jar"

echo "[1/2] Compiling..."
mkdir -p "$OUT"
find src -name "*.java" | xargs javac -cp "$CP" -d "$OUT"

echo "[2/2] Starting Parking Manager..."
echo
java -cp "${OUT}:${CP}" Main
