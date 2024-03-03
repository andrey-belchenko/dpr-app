import { MasterDetail } from "devextreme-react/data-grid";
import Grid from "src/components/Grid";
import KeyProcessingLogDetail from "./KeyProcessingLogDetail";

const columns = [
  {
    dataField: "name",
    caption: "Операция",
    dataType: "string",
  },
  {
    sortIndex: 0,
    sortOrder: "desc",
    dataField: "startedAt",
    caption: "Начало",
    dataType: "datetime",
  },
  {
    dataField: "finishedAt",
    caption: "Завершение",
    dataType: "datetime",
  },
  {
    dataField: "entitiesCount",
    caption: "К-во объектов",
    dataType: "number",
  },
  {
    dataField: "messagesCount",
    caption: "К-во сообщений",
    dataType: "number",
  },
  {
    dataField: "status",
    caption: "Статус",
    dataType: "string",
  },
];
export default function Component() {
  return (
    <Grid
      collectionName="sys_applyKeysOperation"
      title="Журнал обработки"
      countByColumn="name"
      columns={columns}
    >
      <MasterDetail enabled={true} component={KeyProcessingLogDetail} />
    </Grid>
  );
}
