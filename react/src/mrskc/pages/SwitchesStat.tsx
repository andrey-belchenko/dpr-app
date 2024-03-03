import Grid from "src/common/components/grid/Grid";
const columns = [
  {
    dataField: "РЭС",
    caption: "РЭС",
  },
  {
    caption: "Объект",
    columns: [
      {
        dataField: "НаименованиеТехническогоОбъекта",
        caption: "Наименование",
      },
      {
        dataField: "КодТехническогоОбъекта",
        caption: "Код СКК",
      },
    ],
  },
  {
    caption: "Состояние",
    columns: [
      {
        dataField: "ЗначениеИзмеренияСостояния",
        caption: "Значение",
      },
      {
        dataField: "ОбобщённыйКодКачестваТелеизмерения",
        caption: "Код качества",
      },
      {
        dataField: "МеткаВремени",
        caption: "Метка времени",
        dataType: "datetime",
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
      collectionName="view_skSwitchesStat"
      title="Состояния КА"
      countByColumn="РЭС"
      columns={columns}
    >
    </Grid>
  );
}
