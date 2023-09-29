#!/usr/bin/env bash

DIRECTORY="./connectors"

URL="http://localhost:8083/connectors"

# Loop through each file in the directory
for FILE in "$DIRECTORY"/*; do
    if [ -f "$FILE" ]; then
        echo "Sending $FILE..."
        curl -X POST -H "Content-Type: application/json" -F "file=@$FILE" "$URL"
        echo -e "\nDone with $FILE\n"
    fi
done