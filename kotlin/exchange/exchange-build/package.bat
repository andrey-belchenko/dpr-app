docker compose --project-directory .  --file ./docker-compose/use-jar-exchange-for-package.yml  up   --build -d
docker tag exchange-app-processor andreybelchenko/info-exchange-processor:v230417_1
docker tag exchange-app-con-rabbit-incoming andreybelchenko/info-exchange-con-rabbit-incoming:23-04-13-3
docker tag exchange-app-con-rabbit-outgoing andreybelchenko/info-exchange-con-rabbit-outgoing:23-04-13-3
docker tag exchange-app-con-sk11-outgoing andreybelchenko/info-exchange-con-sk-outgoing:23-04-13-3
docker tag exchange-app-con-sk11-incoming andreybelchenko/info-exchange-con-sk-incoming:23-04-13-4
docker tag exchange-app-con-sk11-rest-incoming andreybelchenko/info-exchange-con-sk-rest-incoming:23-04-13-3
docker tag exchange-app-con-platform-outgoing andreybelchenko/info-exchange-con-platform-outgoing:23-04-13-3
docker tag adapter-sandbox-app-sk-ping andreybelchenko/info-exchange-sk-ping:23-04-13-3
docker push andreybelchenko/info-exchange-processor:v230417_1
docker push andreybelchenko/info-exchange-con-rabbit-incoming:23-04-13-3
docker push andreybelchenko/info-exchange-con-rabbit-outgoing:23-04-13-3
docker push andreybelchenko/info-exchange-con-sk-outgoing:23-04-13-3
docker push andreybelchenko/info-exchange-con-sk-incoming:23-04-13-4
docker push andreybelchenko/info-exchange-con-sk-rest-incoming:23-04-13-3
docker push andreybelchenko/info-exchange-con-platform-outgoing:23-04-13-3
docker push andreybelchenko/info-exchange-sk-ping:23-04-13-3
docker compose --project-directory .  --file ./docker-compose/use-jar-exchange-for-package.yml  down

