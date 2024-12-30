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
    TIMEOUT=10;
fi



output=$(timeout ${TIMEOUT}s scip -c "read $1 optimize display solution" 2>&1 < /dev/null)


echo "$output" | sed -E 's/^@.*$//g' | tr -s '\n' | awk '/SCIP Status/{buffer=""} {buffer=buffer ORS $0} END{print buffer}' | head -n -1 > "$1SOLUTION";

exit 0;