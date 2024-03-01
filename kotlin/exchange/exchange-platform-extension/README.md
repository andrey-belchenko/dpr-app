# Расширение платформы для обработки сообщений в MongoDB
## Содержание
[Алгоритм работы](#алгоритм-работы) \
[Пример использования](#пример-использования) \
[Настройка расширения для реализации сценария 3](#настройка-расширения-для-реализации-сценария-3)
## Алгоритм работы
### Вход
- Конфигурация расширения: параметры доступа к БД, рабочая директория, версия профиля _(передается из платформы при старте расширения)_.
- Ссылка на сервисы платформы для: чтения профиля, выполнение сем. запросов, чтения хранилища ключей _(передается из платформы при старте расширения)_.
- Сообщение в формате JSON c указанием типа сообщения(кода события) _(передается из платформы при вызове обработки через расширение)_.
- Семантические запросы для получения контекста _(настраиваются в моделлере)_.
- Правила обработки сообщения в расширении _(настраивается конфигурационном проекте и публикуется в БД Mongo)_:
  - пайпланы MAF и др. настройки обеспечивающие обработку данных
  - настройки для получения контекста по типу сообщения
    - идентификаторы сем. запросов
    - пути к полям сообщения которые содержат идентификаторы корневых объектов для передачи в качестве параметров сем. запроса

### Обработка сообщения
#### Загрузка профиля
Выполняется загрузка профиля из платформы. Результат кешируется.
#### Загрузка конфигурации
Выполняется загрузка конфигурации расширения (правила) из MongoDB. Результат кешируется.
#### Получение контекста из платформы
- Получение идентификаторов корневых объектов из сообщения согласно настройке (внешние ключи).
- Получение iri корневых объектов из хранилища ключей по внешним ключам.
- Выполнение сем. запросов - получение контекста из модели платформы.
- Получение внешних ключей из хранилища ключей по всем объектам контекста.
- Очистка интеграционной БД (Mongo) и запись контекста (данные модели и сопоставление ключей) в нее 
#### Обработка сообщения
Выполняется обработка сообщения в соответствии с правилами настроенными в расширении. При обработке используется загруженный контекст. В результате обработки заполняются выходные коллекции.
#### Формирование результатов
Чтение данных из выходных коллекций и преобразование к виду, который предназначен для передачи в платформу.
### Выход
- Изменения в модели в формате XML дифф (создание, изменение, удаление сущностей).
- Сопоставление ключей для новых сущностей в формате JSON.
- Сводная информация (количество затронутых сущностей, время выполнения).
- Дифф. для отправки в СК-11 в формате XML, если правилами обработки предусмотрено формирование диффа в СК-11
- Сообщения в шину в формате JSON, если правилами обработки предусмотрено формирование сообщений в шину
## Пример использования
### Тест кейс
Для демонстрации работы выбран искусственный пример, чтобы продемонстрировать всю основную функциональность в упрощенном виде. \
На входе сообщение о создании ячейки.
```
{
      "ИдСообщения": "7054ae1c-a87d-4a19-b63f-c9e14b624876",
      "КодСобытия": "СозданиеЯчейки_Тест",
      "ДатаФормированияСообщения": "2023-01-12T08:06:16.264552",
      "СистемаИсточник": "КИСУР",
      "Тело": {
            "КодТехническогоОбъекта": "TP010-0142864-14-01-01",
            "НаименованиеТехнОбъекта": "Присоединение ЛЭП №1",
            "ВышестоящийОбъект": "TP010-0142864-14"
      }
}
```
Новая ячейка (Bay) привязывается к РУ (VoltageLevel), которое привязано к подстанции (Substation), у которой указан участок (SubGeographicalRegion) \
Данные о РУ и подстанции уже есть в модели. \
Система должна: 
- Создать новую ячейку в модели добавив к наименованию участок и подстанцию ("Мамоновский участок. ТП 006-33 аб. Присоединение ЛЭП №1").
- Отправить ячейку в СК-11
- Отправить сообщение о ячейке в шину

### Настройки
#### Семантические запросы
В моделере (http://modeller-test3.test.oastu.lan/) настроены семантические запросы для получения контекста
- extension_test2 (http://ontology.adms.ru/UIP/modeler/config#SemanticQuery_798a5aa1-b8f9-484d-a78f-92a784918465) \
Получает информацию о вышестоящих объектах от РУ VoltageLevel->Substation->SubGeographicalRegion
- extension_test3  (http://ontology.adms.ru/UIP/modeler/config#SemanticQuery_d7ddbc37-0f95-4a32-9997-4a34bf8e5f81) \
Получает информацию о ячейке Bay, на случай если она уже существует.

#### Конфигурация расширения
Настройки выполнены в конфигурационном проекте и загружены в тестовую БД
- Настройки для получения контекста \
http://gl.astu.lan/mrsk/mrsk-configuration/-/blob/develop/exchange-configuration/builder/src/experiments/exampleContextSettings.ts
```
 {
    messageType: "СозданиеЯчейки_Тест",
    idSource: "КИСУР",
    contextQueries: [
      {
        queryId: "http://ontology.adms.ru/UIP/modeler/config#SemanticQuery_d7ddbc37-0f95-4a32-9997-4a34bf8e5f81",
        rootIds: ["$Тело.КодТехническогоОбъекта"],
      },
      {
        queryId: "http://ontology.adms.ru/UIP/modeler/config#SemanticQuery_798a5aa1-b8f9-484d-a78f-92a784918465",
        rootIds: ["$Тело.ВышестоящийОбъект"],
      },
    ],
  }
```
- Правила обработки 
  - Создание ячейки в модели на основе входящего сообщения и контекста \
  (http://gl.astu.lan/mrsk/mrsk-configuration/-/blob/develop/exchange-configuration/builder/src/experiments/exampleRule.ts#L7)
  - Отправка сообщения в шину при изменении Bay в модели \
  (http://gl.astu.lan/mrsk/mrsk-configuration/-/blob/develop/exchange-configuration/builder/src/experiments/exampleRule.ts#L82)
  - Формирование дифф. в СК-11 (общее правило)
  (http://gl.astu.lan/mrsk/mrsk-configuration/-/blob/develop/exchange-configuration/builder/src/sk11.ts)

### Подготовка начального состояния платформы
- В модели уже есть VoltageLevel->Substation->SubGeographicalRegion
- Добавляем записи по внешним ключам для Substation и VoltageLevel в postgresql://crdb.oastu.lan:26257/test3
```sql
delete from vocabularies where external_system = 'КИСУР';

insert into vocabularies (iri,document,external_system)
VALUES
('Substation_37663a61-2aa0-4c51-aec6-7dbb29b4f4b7','{"externalKey": "TP010-0142864"}','КИСУР'),
('VoltageLevel_00b4b3a3-3b69-4ab3-9cdc-1085f4663581','{"externalKey": "TP010-0142864-14"}','КИСУР');
```

### Запуск
Запуск выполняется с помощью тестового проекта [test-extensions-consumer](http://gl.astu.lan/mrsk/adapter-lite/-/tree/master/test-extensions-consumer) который эмулирует действия платформы по активации расширения и передаче в него сообщения.
- Клонировать http://gl.astu.lan/mrsk/adapter-lite/-/tree/master
- Открыть папку в IntelliJ IDEA
- Проверить (изменить при необходимости) конфигурацию в файле [test-extensions-consumer/src/main/resources/application.yml](../test-extensions-consumer/src/main/resources/application.yml)
  - Обратите внимание на блок exchange
  - В текущий момент настройки сделаны на стенд test3
- Запустить gradle команду test-extensions-consumer -> quarkus -> quarkusDev
- Отправить запрос 
```
POST /debug HTTP/1.1
Host: localhost:9911
{
      "ИдСообщения": "7054ae1c-a87d-4a19-b63f-c9e14b624876",
      "КодСобытия": "СозданиеЯчейки_Тест",
      "ДатаФормированияСообщения": "2023-01-12T08:06:16.264552",
      "СистемаИсточник": "КИСУР",
      "Тело": {
            "КодТехническогоОбъекта": "TP010-0142864-14-01-01",
            "НаименованиеТехнОбъекта": "Присоединение ЛЭП №1",
            "ВышестоящийОбъект": "TP010-0142864-14"
      }
}
```

При этом вызывается метод [ExtensionConsumer.handleMessage](../test-extensions-consumer/src/main/kotlin/cc/datafabric/adapter/consumer/ExtensionConsumer.kt)

```kotlin
fun handleMessage(message: String): Response {
  val result = pipelineExtension.handle(message, mapOf())
  // Summary и OperationLog возвращаем в качестве ответа
  val responseText =  """{
                "summary":${result["Summary"]}, 
                "operationLog":${result["OperationLog"]}, 
            }""".trimIndent()
  // Остальную информацию пишем в лог
  logger.info("\nDiff for platform:\n${result["Platform"]}")
  logger.info("\nNew key mappings for platform:\n${result["KeyMapping"]}")
  logger.info("\nDiff for SK-11:\n${result["Sk11"]}")
  logger.info("\nMessages to bus:\n${result["Bus"]}")
  return Response.ok(responseText).build()
}
```
### Результат
Ответ HTTP
```json
{
    "summary": {
        "contextEntitiesCount": 393,
        "affectedEntitiesCount": 2,
        "totalTimeMs": 22097,
        "platformSideTimeMs": 20433,
        "resultDataSetsNames": [
            "Summary",
            "Bus",
            "Platform",
            "KeyMapping",
            "Sk11",
            "OperationLog"
        ]
    },
    "operationLog": {
        "items": [
            {
                "scope": "platform",
                "operation": "loadProfile",
                "timeMs": 6544
            },
            {
                "scope": "extension",
                "operation": "initExchangeSettings",
                "timeMs": 457
            },
            {
                "scope": "platform",
                "operation": "loadContext-QueryRootIrisByMessageKeys",
                "timeMs": 47,
                "count": 2
            },
            {
                "scope": "platform",
                "operation": "loadContext-ExecuteSemQueries",
                "timeMs": 8013,
                "count": 1
            },
            {
                "scope": "extension",
                "operation": "loadContext-ParseQueryResultsAndExtractIris",
                "timeMs": 3,
                "count": 393
            },
            {
                "scope": "platform",
                "operation": "loadContext-QueryExtKeysByContextIris",
                "timeMs": 5829,
                "count": 393
            },
            {
                "scope": "extension",
                "operation": "loadContext-ConvertContext",
                "timeMs": 1
            },
            {
                "scope": "extension",
                "operation": "loadContext-ImportToExchangeDb",
                "timeMs": 565
            },
            {
                "scope": "extension",
                "operation": "ExecuteRules",
                "timeMs": 212
            },
            {
                "scope": "extension",
                "operation": "PrepareResult",
                "timeMs": 14
            }
        ]
    },
}
```

Другие данные в логе:

```
Diff for platform:
<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:cim="http://iec.ch/TC57/2014/CIM-schema-cim16#" xmlns:dm="http://iec.ch/2002/schema/CIM_difference_model#" xmlns:md="http://iec.ch/TC57/61970-552/ModelDescription/1#">
                   <dm:DifferenceModel rdf:about="#33bdf954-d217-4d86-900a-c5fa3f55d7e9">
                       <md:Model.created>2023-05-28T03:27:23.558369400</md:Model.created>
                       <md:Model.version/>
                       <dm:forwardDifferences>
                       <cim:Bay rdf:about="#4ce4fd5d-3b8a-4799-a105-111ed6bbd168"><cim:IdentifiedObject.name>??????????? ???????. ?? 006-33 ??. ????????????? ??? ?1</cim:IdentifiedObject.name><cim:PowerSystemResource.ccsCode>TP010-0142864-14-01-01</cim:PowerSystemResource.ccsCode><cim:EquipmentContainer.PlaceEquipmentContainer rdf:resource="#f032c048-c159-403a-9428-090cbc5cf936"/><cim:IdentifiedObject.ParentObject rdf:resource="#VoltageLevel_00b4b3a3-3b69-4ab3-9cdc-1085f4663581"/></cim:Bay><cim:TechPlace rdf:about="#f032c048-c159-403a-9428-090cbc5cf936"><cim:TechPlace.CodeTP>TP010-0142864-14-01-01</cim:TechPlace.CodeTP><cim:TechPlace.PlaceEquipmentContainer rdf:resource="#4ce4fd5d-3b8a-4799-a105-111ed6bbd168"/></cim:TechPlace></dm:forwardDifferences>
                       <dm:reverseDifferences>
                       </dm:reverseDifferences>
                   </dm:DifferenceModel>
               </rdf:RDF>

New key mappings for platform:
{
  "items": [
    {
      "iri": "4ce4fd5d-3b8a-4799-a105-111ed6bbd168",
      "externalSystemId": "?????",
      "document": {
        "externalKey": "TP010-0142864-14-01-01"
      }
    },
    {
      "iri": "f032c048-c159-403a-9428-090cbc5cf936",
      "externalSystemId": "?????",
      "document": {
        "externalKey": "TechPlaceTP010-0142864-14-01-01"
      }
    }
  ]
}

Diff for SK-11:
<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:cim="http://iec.ch/TC57/2014/CIM-schema-cim16#" xmlns:dm="http://iec.ch/2002/schema/CIM_difference_model#" xmlns:md="http://iec.ch/TC57/61970-552/ModelDescription/1#" xmlns:me="http://monitel.com/2014/schema-cim16#">
                   <dm:DifferenceModel rdf:about="#_ce6bb0b0-2ace-4588-b119-96ab9428249c">
                       <md:Model.created>2023-05-28T03:27:23.442885400</md:Model.created>
                       <md:Model.version/>
                       <dm:forwardDifferences>
                       <cim:Bay rdf:about="#_4ce4fd5d-3b8a-4799-a105-111ed6bbd168"><cim:IdentifiedObject.name>??????????? ???????. ?? 006-33 ??. ????????????? ??? ?1</cim:IdentifiedObject.name><cim:PowerSystemResource.ccsCode>TP010-0142864-14-01-01</cim:PowerSystemResource.ccsCode><me:IdentifiedObject.ParentObject rdf:resource="#_VoltageLevel_00b4b3a3-3b69-4ab3-9cdc-1085f4663581"/></cim:Bay></dm:forwardDifferences>
                       <dm:reverseDifferences>
                       </dm:reverseDifferences>
                   </dm:DifferenceModel>
               </rdf:RDF>

Messages to bus:
{
  "messages": [
    {
      "_id": {
        "$oid": "64729febe1ec80738abe66c0"
      },
      "batchId": "03 ddb1274d-2ce7-464d-ba65-5ca76de22748",
      "changedAt": {
        "$date": "2023-05-28T00:27:23.169Z"
      },
      "executionId": "6b6e62bf-4687-4cfd-bb65-1817c31af725",
      "operationId": "cfbab900-1ce9-41ba-abf2-23a200422e1d",
      "payload": {
        "??????????": "??????????????",
        "?????????????????????????": {
          "$date": "2023-05-28T00:27:23.169Z"
        },
        "???????????????": "???",
        "????": {
          "??????????????????????": "TP010-0142864-14-01-01",
          "???????????????????????": "??????????? ???????. ?? 006-33 ??. ????????????? ??? ?1"
        }
      },
      "queueName": "EQUIPMENT.ASTU.IN"
    }
  ]
}

```


## Настройка расширения для реализации сценария 3
Настройка расширения для реализации сценария 3: Актуализация связей между ACLineSegment. \
Проект.
### Описание сценария
Описание сценария приведено в документе [Описание решения МРСКЦ.docx](https://datafabricrus.atlassian.net/wiki/spaces/ASTU/pages/1084162060) \
В рамках сценария поступают следующие типы входящих сообщений:
- СозданиеУчасткаМагистрали
- СозданиеОтпайки
- РазделениеУчасткаМагистралиКА
- УдалениеКА
- УдалениеСегмента \
[Примеры](http://gl.astu.lan/mrsk/mrsk-configuration/-/tree/develop/exchange-configuration/messages/examples/scenario3)

В результате обработки сообщений должны быть:
- Внесены изменения в модель платформы
- Отравлен diff в СК-11 по заданному набору классов и атрибутов
- Отправлены сообщения в шину (очередь Rabbit MQ, РГИС) \
  Примеры исходящих сообщений в РГИС
  - [Создание сегмента](http://gl.astu.lan/mrsk/mrsk-configuration/-/blob/develop/exchange-configuration/messages/matyushkin/!%D0%A0%D0%93%D0%98%D0%A1%20%D0%A1%D0%BE%D0%B7%D0%B4%D0%B0%D0%BD%D0%B8%D0%B5%20%D0%A1%D0%B5%D0%B3%D0%BC%D0%B5%D0%BD%D1%82%D0%B0%20%D0%B4%D0%BB%D1%8F%20%D1%82%D0%B5%D1%81%D1%82%D0%B0/%D0%A1%D0%B5%D0%B3%D0%BC%D0%B5%D0%BD%D1%82%D0%92%D0%A0%D0%93%D0%98%D0%A1_5.json)
  - [РазделениеУчасткаМагистралиКА](http://gl.astu.lan/mrsk/mrsk-configuration/-/blob/develop/exchange-configuration/messages/matyushkin/%D0%A0%D0%93%D0%98%D0%A1%20%D0%A0%D0%B0%D0%B7%D0%B1%D0%B8%D0%B5%D0%BD%D0%B8%D0%B5%20%D1%83%D1%87%D0%9C%D0%B0%D0%B3%20%D0%9A%D0%90/%D0%9A%D0%90_%D0%92%D0%A0%D0%93%D0%98%D0%A1.json)


### Настройки
#### Семантические запросы
Должен быть настроен семантический запрос который возвращает следующие данные по коду линии.
- Line
  - Line.AccountPartLines
    - AccountPartLine.LineSpans
    - AccountPartLine.Towers
  - EquipmentContainer.Equipments
    - ConductingEquipment.Terminals
    - Terminal.ConnectivityNode

#### Конфигурация расширения

- Настройки для получения контекста (драфт) \
  (http://gl.astu.lan/mrsk/mrsk-configuration/-/blob/develop/exchange-configuration/builder/src/experiments/exampleContextSettingsScenario3.ts) \
  Нужна доработка входящих сообщений со стороны КИСУР. Добавить код линии во все сообщения. Это позволит использовать один и тот же запрос для получения контекста во всех сообщениях.
- Правила обработки
  - Реализован сложный алгоритм виде множества пайплайнов которые в конечном итоге вносят нужные изменения в модель в интеграционной БД \
    (http://gl.astu.lan/mrsk/mrsk-configuration/-/tree/develop/exchange-configuration/builder/src/scenario/003-new)

#### Дополнения профиля в платформе
Для реализации алгоритма обработки в модель интеграционной БД сохраняются дополнительные данные, которых нет в профиле платформы. 
Для этого в конфигурации заведено "расширение профиля". \
Требуется добавление этих полей и классов в профиль платформы. \
Многие из них не имеют бизнес смысла, являются чисто служебными. \
(http://gl.astu.lan/mrsk/mrsk-configuration/-/blob/develop/exchange-configuration/settings/profile/profileExtension.csv) \
В файле содержатся расширение профиля по всем сценариям, не только по 3-му.

#### Настройка платформы для отправки диффа в СК-11
Сейчас формирование диффа для СК-11 происходит с помощью правил в расширении, собираются все изменения по заданному набору классов и предикатов.\
Планируется передать этот функционал на сторону платформы. \
Нужно выполнить конфигурацию платформы для реализации этого функционала. 

#### Доработка для формирования исходящего сообщения в общем профиле
Сейчас расширение выдает готовое сообщение в частном профиле для отправки в РГИС. \
Планируется передать этот функционал на сторону платформы. \
Расширение при этом должно формировать сообщение в общем профиле \
Со стороны расширения:
 - доработать системную часть для формирования сообщения в общем профиле 
 - доработать правила для формирования сообщения в общем профиле 

Со стороны платформы:
- обеспечить формирование в частном профиле на основе сообщений в общем профиле получаемого от расширения

#### Доработка/настройка коннекторов
- Получение сообщений из очереди Rabbit MQ
- Отправка сообщений в очередь Rabbit MQ
- Отправка диффа в СК-11
- Получение диффа из СК-11 (особенность при записи данных полученных из СК-11 фиксируется признак, что объекты или связи получены из СК-11, этот признак используется при построении дерева объектов в правилах)

### Подготовка начального состояния хранилища

Сейчас при отладке этого сценария мы производим инициализацию БД с помощью скриптов. \
Выполняются следующие операции. 
- Очистка
- Отключение коннекторов, чтобы избежать отправки данных при инициализации.
- Загрузка модели СК-11 из xml файла
- Загрузка других начальных данных и сопоставления ключей из препаратора и csv файлов (в csv файлах справочники, их можно перенести в препаратор при необходимости)
- Выполнение обработки загруженных данных. Обработка выполняется с помощью пайплайнов, которые частично переиспользуют логику заложенную в правилах обработки сообщений
- Включение коннекторов
- В некоторых случаях требуется массовая отправка исходящих сообщений по всем сегментам для инициализации БД в РГИС. 
  Исходящие сообщения формируются существующими правилам данным из модели.
  Скрипт инициализирует этот процесс.

Требуется обеспечить возможность выполнения аналогичных операций для инициализации хранилища платформы для тестов.

### Результат обработки сообщения расширением

- Дифф. для загрузки в хранилище
- Изменения в хранилище ключей
- Исходящие сообщения в общем профиле

В отличие от реализованного тестового примера исключается дифф в СК-11 и исходящее сообщение формируется в общем профиле, а не в частном.

