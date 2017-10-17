#!/bin/bash

# The size is multiply to 3,592 bytes so 10 will produce
# a file of 35,920 byte. So to get a file of 1MB enter a
# size value of 292.


programname=$0

function usage {
    echo " The size is multiply to 3,592 bytes so 10 will produce"
    echo " a file of 35,920 byte. So to get a file of 1MB enter a"
    echo " size value of 292."
    echo " "
    echo "usage: $programname <size> <filename>"
    echo "  size    number to multiply by 3592 bytes"
    echo "  file    name of the output filename"
    exit 1
}

if [ $# -eq 0 ]
  then
    usage
fi

if [ -z "$1" ]
  then # default size 10 or 35,929 bytes
    size=10
else
    size=$1
fi

if [ -z "$2" ]
  then # deafult output file name
    output=testFile.txt
else
    output=$2
fi

dd if=/dev/urandom of=TemporaryFile count=$size bs=1024
hexdump TemporaryFile > $output
rm TemporaryFile

