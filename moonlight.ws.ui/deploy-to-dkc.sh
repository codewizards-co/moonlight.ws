#!/bin/bash

DIR=`dirname $0`
cd "$DIR"
DIR=`pwd`
#echo "$DIR"

rsync -avP --delete dist/moonlight.ws.ui/browser/ bran:/srv/dragonkingchocolate.com/data/nginx/html/moonlight.ws.ui/
