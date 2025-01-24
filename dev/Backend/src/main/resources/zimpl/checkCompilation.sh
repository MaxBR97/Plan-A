#! /bin/bash

if [ ! -f $1 ]; then
    echo "Cannot Find File!";
    exit 1;
elif [ -z "$1" ]; then
    echo "Enter File Name!";
    exit 1;
fi

TIMEOUT=$2
if [ -z "$2" ]; then
    TIMEOUT=4;
fi

output=$(timeout ${TIMEOUT}s scip -c "read $1" 2>&1 < /dev/null)

if echo "$output" | grep -q "original problem has"; then
    echo "Compilation Successful"
    exit 0
elif echo "$output" | grep -q "error reading file"; then
    echo "Compilation Failed"
    exit 1
else
    echo "Compilation timed out or no matching output."
    exit 1
fi