import { KeycloakRole } from "../common/contexts/keycloak";

export const menuData = [
  {
    text: "Главная",
    path: "/home",
    icon: "home",
    hintText: "Функционал в разработке",
    hintIcon: "warning",
  },
  {
    text: "Витрины данных",
    roles: [KeycloakRole.Аналитик],
    icon: "smalliconslayout",
    items: [
      {
        text: "Дерево объектов",
        path: "/ObjectTree",
      },
      {
        text: "Оборудование и контейнеры",
        path: "/ObjectTable",
      },
      {
        text: "Файлы из КИСУР",
        path: "/FilesTable",
      },
      {
        text: "Объекты отпр. в СК-11",
        path: "/SkSentObjects",
      },
      {
        text: "Статус обраб. элем. сети",
        path: "/AplInfo",
      },
      {
        text: "Состояние линий",
        path: "/LineStatus",
      },
      {
        text: "Диспетчерские пометки",
        path: "/Markers",
      },
      {
        text: "Состояния КА",
        path: "/SwitchesStat",
      },
      {
        text: "Предупреждения",
        path: "/Warnings",
      },
      {
        text: "Уведомления",
        path: "/Notifications",
      },
    ],
  },
  {
    text: "Обогащение данных",
    roles: [KeycloakRole.Аналитик],
    icon: "rename",
    items: [
      // {
      //   text: "Сопоставление (список)",
      //   path: "/IdMatching",
      // },
      // {
      //   text: "Сопоставление (иерархия)",
      //   path: "/MatchingTree",
      // },
      {
        text: "Сопоставление ключей",
        items: [
          {
            text: "Список",
            path: "/IdMatching",
          },
          {
            text: "Иерархия",
            path: "/MatchingTree",
          },
        ],
      },
      // {
      //   text: "Сопоставление элем. сети",
      //   path: "/TopologyMatching",
      //   hintText: "Функционал в разработке",
      //   hintIcon: "warning",
      // },
      {
        text: "Сопоставление элем. сети",
        items: [
          {
            text: "Подготовка данных",
            items: [
              {
                text: "Запуск",
                path: "/RunLineMatching",
              },
              {
                text: "Результаты",
                path: "/LineMatchingResults",
              },
            ]
          },
          {
            text: "Статус по линиям",
            path: "/TopologyMatching",
          },
          {
            text: "Проверка целостности",
            path: "/TopologyIntegrityCheck",
          },
        ],
      },
      // {
      //   text: "Проверка целостности",
      //   path: "/TopologyIntegrityCheck",
      // },
    ],
  },
  {
    text: "Отложенные сообщения",
    icon: "clock",
    roles: [KeycloakRole.Аналитик],
    items: [
      {
        text: "Список сообщений",
        path: "/BlockedMessages",
      },
      {
        text: "Журнал обработки",
        path: "/KeyProcessingLog",
      },
    ],
  },

  {
    text: "Мониторинг",
    icon: "eyeopen",
    roles: [KeycloakRole.Инженер, KeycloakRole.Администратор, KeycloakRole.Эксперт],
    items: [
      {
        text: "Входящие сооб. КИСУР",
        path: "/IncomingMessages",
      },
      {
        text: "Исходящие сооб. РГИС",
        path: "/OutRgis",
      },
      {
        text: "Журнал операций",
        path: "/PipelineLog",
      },
      {
        text: "Журнал ошибок",
        path: "/Errors",
      },
      {
        text: "Журнал информобмена",
        path: "/ExchangeLog",
      },
    ],
  },
 
];
