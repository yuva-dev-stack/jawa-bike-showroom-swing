#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
#  build.sh — Compile & Run Jawa Bike Showroom SWING GUI
#  Usage:
#    chmod +x build.sh && ./build.sh
#    ./build.sh compile   # Compile only
#    ./build.sh run       # Run only (after compiling)
#    ./build.sh clean     # Remove compiled classes
# ─────────────────────────────────────────────────────────────────────────────

SRC_DIR="src"
OUT_DIR="out"
MAIN_CLASS="com.jawa.showroom.MainSwing"

compile() {
    echo "Compiling Jawa Bike Showroom Swing GUI..."
    mkdir -p "$OUT_DIR"
    find "$SRC_DIR" -name "*.java" > sources.txt
    javac -d "$OUT_DIR" -sourcepath "$SRC_DIR" @sources.txt
    rm -f sources.txt
    if [ $? -eq 0 ]; then
        echo "✓  Compilation successful! $(find "$OUT_DIR" -name '*.class' | wc -l) class files generated."
    else
        echo "✗  Compilation failed. Check errors above."
        exit 1
    fi
}

run() {
    echo "Launching Jawa Bike Showroom GUI..."
    java -cp "$OUT_DIR" "$MAIN_CLASS"
}

clean() {
    rm -rf "$OUT_DIR" && echo "✓  Cleaned."
}

case "$1" in
    compile) compile ;;
    run)     run ;;
    clean)   clean ;;
    *)       compile && run ;;
esac
