#!/usr/bin/env bash
sudo curl -L https://github.com/docker/compose/releases/download/1.23.0-rc2/docker-compose-`uname -s`-`uname -m` -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
cd /home
sudo mkdir poc
sudo git clone https://github.com/nhsd-a2si/a2si-uec-appointment-poc

cd a2si-uec-appointment-poc
docker-compose pull
docker-compose up -d
sleep 20m

docker cp ccri-dataload/src/main/resources/Examples/dataload.sql ccrisql:home/dataload.sql

docker exec -it ccrisql psql -U fhirjpa -d careconnect -f home/dataload.sql