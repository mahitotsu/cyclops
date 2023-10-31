#!/bin/bash
set -ex

basedir=$(pwd $(dirname $0))
subcmd=$1

case "$subcmd" in
"diff")
    npm --prefix $basedir/webapp run build && cdk diff
    ;;
"deploy")
    npm --prefix $basedir/webapp run build && cdk deploy
    ;;
*)
    exit 1
    ;;
esac
