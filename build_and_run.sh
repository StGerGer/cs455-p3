#!/bin/bash

make build
docker build -t purvesta/cs455-p3:latest .
docker-compose up -d --scale server=3
