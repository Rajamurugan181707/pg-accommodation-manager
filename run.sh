#!/bin/sh
# Compile and run the PG Accommodation Management System (macOS / Linux).
cd "$(dirname "$0")" || exit 1
rm -rf out && mkdir -p out
javac -nowarn -Xlint:-options --release 8 -encoding UTF-8 -d out $(find src -name "*.java") || exit 1
java -cp "out:lib/mysql-connector-j-9.1.0.jar" com.pgms.ui.Main
