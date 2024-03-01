- вынести пароли в отдельные свойства в конфиге (совместимость с k8s secrets ) 
- сделать логирование правильно, разобраться как работать с логгерами, попроборать реализовать через AOP
- отказоустойчивость, подавлять ли ошибки
- записывать сообщения в монго для отладки и мониторинга
- (?)создать класс конфигурации, добавить валидацию конфигурации на старте
- логика для dictionary
- как будем поступать с сообщениями для которых обработка прошла с ошибкой, пока возвращаются в очередь
- см. todo по коду
- readme
docker compose --project-directory .  --file ./docker-compose/use-source.yml  up   --build 
docker compose --project-directory .  --file ./docker-compose/use-jar.yml  up   --build -d 
docker compose --project-directory .  --file ./docker-compose/use-jar-dev.yml  up   --build -d  
docker compose --project-directory .  --file ./docker-compose/use-jar-sk11-outgoing.yml  up   --build -d
docker compose --project-directory .  --file ./docker-compose/use-source-exchange.yml  up   --build -d  
- adapter-lib разбить на несколько проектов? вынести логику обработки входящего отдельно
- исходящие
- удаление
- гарантированная доставка

