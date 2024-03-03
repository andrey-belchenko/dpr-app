import Grid from "src/common/components/grid/Grid";
const columns = [
  {
    dataField: "region",
    caption: "РЭС",
    dataType: "string",
  },
  {
    caption: "Линия",
    columns: [
      {
        dataField: "lineName",
        caption: "Наименование",
        dataType: "string",
      },
      {
        dataField: "lineCode",
        caption: "Код",
        dataType: "string",
      },
    ],
  },
  {
    caption: "Объект",
    columns: [
      {
        dataField: "type",
        caption: "Тип",
        dataType: "string",
      },
      {
        dataField: "name",
        caption: "Наименование",
        dataType: "string",
      },
      {
        dataField: "code",
        caption: "Код",
        dataType: "string",
      },
      {
        dataField: "status",
        caption: "Статус",
        dataType: "string",
      },
      // {
      //   dataField: "entityChangedAt",
      //   caption: "Создан",
      //   dataType: "datetime",
      // },
      {
        sortIndex: 0,
        sortOrder: "desc",
        dataField: "entityChangedAt",
        caption: "Изменен",
        dataType: "datetime",
      },
      {
        dataField: "id",
        caption: "Идентификатор",
        dataType: "string",
      },
    ],
  },
];

export default function Component() {
  return (
    <Grid
      collectionName="view_aplInfo"
      title="Статус обраб. элем. сети"
      countByColumn="region"
      columns={columns}
    />
  );
}
