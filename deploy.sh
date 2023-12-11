#!/bin/bash

# build
npm --prefix ./01_frontend run build
npm --prefix ./03_proxy run build

# deploy
cdk deploy