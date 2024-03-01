# Назначение
Набор приложений (адаптеров) которые организуют взаимодействие между ядром платформы и внешними системами.
- Транспорт сообщений
- Преобразование сообщений

# Перечень адаптеров
- [adapter-app-rabbit-incoming](./adapter-app-rabbit-incoming/README.md)
- [adapter-app-rabbit-outgoing](adapter-app-sk11-outgoing/README.md)
- [adapter-app-sk11-outgoing](adapter-app-sk11-outgoing/README.md)
- [adapter-app-sk11-incoming](adapter-app-sk11-incoming/README.md)

# Расширения CustomProcessor DRAFT
Для публикации системных библиотек в GitLab Package Registry через
```
./gradlew publish
```
Нужно добавить указать deploy token в USER_HOME/.gradle/gradle.properties (на Windows C:\Users\<user>\.gradle\gradle.properties)
```
adpDeployToken=<value>
```


[//]: # (TODO)