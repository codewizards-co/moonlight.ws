#!/bin/bash

DIR=`dirname $0`
cd "$DIR"
DIR=`pwd`
#echo "$DIR"

rsync -avP mlws.rs/target/moonlight.ws.rs.war bran:/srv/dragonkingchocolate.com/data/moonlight-ws-rs/wildfly/deployments/
