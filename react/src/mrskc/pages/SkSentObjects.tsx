import Grid from "src/mrskc/components/Grid";
const columns = [
  {
    sortIndex: 0,
    sortOrder: "desc",
    dataField: "changedAt",
    caption: "Дата",
    dataType: "datetime",
  },
  {
    dataField: "region",
    caption: "РЭС",
  },
  {
    caption: "Корневой контейнер",
    columns: [
      {
        dataField: "rcName",
        caption: "Наименование",
      },
      {
        dataField: "rcType",
        caption: "Тип",
      },
      {
        dataField: "rcCode",
        caption: "Код СКК",
      },
    ],
  },
  {
    caption: "Объект",
    columns: [
      {
        dataField: "name",
        caption: "Наименование",
      },
      {
        dataField: "type",
        caption: "Тип",
      },
      {
        dataField: "code",
        caption: "Код СКК",
      },
    ],
  },
  {
    dataField: "id",
    caption: "Идентификатор",
  },
];
export default function Component() {
  return (
    <Grid
      collectionName="flow_skSentObjects"
      title="Объекты отправленные в СК-11"
      countByColumn="changedAt"
      columns={columns}
    >
    </Grid>
  );
}
