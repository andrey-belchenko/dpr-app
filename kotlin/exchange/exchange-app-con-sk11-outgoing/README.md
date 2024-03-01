## Запуск с помощью Docker Compose
Выполнение описанных выше операций с помощью Docker Compose
- Примеры [yaml файлов](../docker-compose/)
- Пример запуска из [корневой папки](..)
```
docker compose --project-directory .  --file ./docker-compose/use-jar-sk11-outgoing.yml  up   --build -d 
```