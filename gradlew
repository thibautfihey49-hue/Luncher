#!/usr/bin/env sh
set -e
if command -v gradle >/dev/null 2>&1; then exec gradle "$@"; else echo "Use Android Studio"; exit 1; fi
