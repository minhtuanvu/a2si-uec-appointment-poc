#!/usr/bin/env bash
echo installing docker-compose
sudo curl -L https://github.com/docker/compose/releases/download/1.23.0-rc2/docker-compose-`uname -s`-`uname -m` -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

echo Retrieving PoC
cd /home
sudo mkdir poc
cd poc
sudo git clone https://github.com/nhsd-a2si/a2si-uec-appointment-poc
cd a2si-uec-appointment-poc
docker-compose pull

echo Starting up PoC
docker-compose up -d
sleep 5m

echo setting up test data
docker cp ccri-dataload/src/main/resources/Examples/dataload.sql ccrisql:home/dataload.sql
docker exec -it ccrisql psql -U fhirjpa -d careconnect -f home/dataload.sql

echo setting up DoS test stub
cd ..
sudo git clone https://github.com/mstephens-xsl/a2si-appt-poc-dos-test-stub
cd a2si-appt-poc-dos-test-stub
docker build -t pathways-dos-test-stub .
 docker run -e spring.profiles.active=doswrapper-local-dos-stub-na-cpsc-stub-na -e capacity.service.api.username=dummyValue -e capacity.service.api.password=dummyValue -p 7030:7030/tcp pathways-dos-test-stub &