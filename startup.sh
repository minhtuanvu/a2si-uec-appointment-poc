#!/usr/bin/env bash
sudo curl -L https://github.com/docker/compose/releases/download/1.23.0-rc2/docker-compose-`uname -s`-`uname -m` -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
cd /home
sudo mkdir poc
cd poc
sudo git clone https://github.com/nhsd-a2si/a2si-uec-appointment-poc

cd a2si-uec-appointment-poc
docker-compose pull
docker-compose up -d
sleep 5m

docker cp ccri-dataload/src/main/resources/Examples/dataload.sql ccrisql:home/dataload.sql

docker exec -it ccrisql psql -U fhirjpa -d careconnect -f home/dataload.sql

 docker run -e spring.profiles.active=doswrapper-local-dos-stub-na-cpsc-stub-na -e capacity.service.api.username=dummyValue -e capacity.service.api.password=dummyValue -p 7030:7030/tcp pathways-dos-test-stub &