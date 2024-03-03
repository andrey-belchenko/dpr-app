import { Grid } from "src/common/components";
const columns = [
  {
    dataField: "РЭС",
    caption: "РЭС",
  },
  {
    caption: "Объект",
    columns: [
      {
        dataField: "Объект",
        caption: "Объект",
      },
      {
        dataField: "Оборудование",
        caption: "Оборудование",
      },
    ],
  },
  {
    caption: "Пометка",
    columns: [
      {
        dataField: "Тип",
        caption: "Тип",
      },
      {
        dataField: "Комментарий",
        caption: "Комментарий",
      },
      {
        dataField: "Автор",
        caption: "Автор",
      },
    ],
  },
  {
    dataField: "ВремяСоздания",
    caption: "Создан",
    dataType: "datetime",
  },
  {
    dataField: "id",
    caption: "Идентификатор",
  },
];
export default function Component() {
  return (
    <Grid
      collectionName="view_skMarkers"
      title="Диспетчерские пометки"
      countByColumn="РЭС"
      columns={columns}
    ></Grid>
  );
}
