docker compose --project-directory .  --file ./docker-compose/use-jar-exchange-for-package.yml  up   --build -d
docker tag exchange-app-processor dockerhub.oastu.lan:5000/adapter-processor:v230417_1
docker tag exchange-app-con-rabbit-incoming dockerhub.oastu.lan:5000/rabbit-incoming:v230414_3
docker tag exchange-app-con-rabbit-outgoing dockerhub.oastu.lan:5000/rabbit-outgoing:v230414_3
docker tag exchange-app-con-sk11-outgoing dockerhub.oastu.lan:5000/sk11-outgoing:v230416_1
docker tag exchange-app-con-sk11-incoming dockerhub.oastu.lan:5000/sk11-incoming:v230414_3
docker tag exchange-app-con-sk11-rest-incoming dockerhub.oastu.lan:5000/sk11-rest-incoming:v230414_3
docker tag exchange-app-con-platform-outgoing dockerhub.oastu.lan:5000/platform-outgoing:v230414_3
docker tag adapter-sandbox-app-sk-ping dockerhub.oastu.lan:5000/exchange-sk11-ping:v230414_3
docker push dockerhub.oastu.lan:5000/adapter-processor:v230417_1
docker push dockerhub.oastu.lan:5000/rabbit-incoming:v230414_3
docker push dockerhub.oastu.lan:5000/rabbit-outgoing:v230414_3
docker push dockerhub.oastu.lan:5000/sk11-outgoing:v230416_1
docker push dockerhub.oastu.lan:5000/sk11-incoming:v230414_3
docker push dockerhub.oastu.lan:5000/sk11-rest-incoming:v230414_3
docker push dockerhub.oastu.lan:5000/platform-outgoing:v230414_3
docker push dockerhub.oastu.lan:5000/exchange-sk11-ping:v230414_3
docker compose --project-directory .  --file ./docker-compose/use-jar-exchange-for-package.yml  down

