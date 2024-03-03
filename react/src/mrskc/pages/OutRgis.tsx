import { MasterDetail } from "devextreme-react/data-grid";
import Grid from "src/mrskc/components/Grid";
import IncomingMessageDetail from "./IncomingMessageDetail";

const columns = [
  { dataField: "payload.КодСобытия", caption: "Код события" },
  { dataField: "objectId", caption: "Идентификатор объекта" },
  { dataField: "objectName", caption: "Наименование объекта" },
  {
    sortIndex: 0,
    sortOrder: "desc",
    dataField: "changedAt",
    caption: "Дата",
    dataType: "datetime",
  },
  { dataField: "queueName", caption: "Очередь" },
  { dataField: "_id", caption: "Ид записи" },
];
export default function Component() {
  return (
    <Grid
      collectionName="out_Rgis"
      title="Исходящие сообщения РГИС"
      countByColumn="payload.КодСобытия"
      columns={columns}
    >
      <MasterDetail enabled={true} component={IncomingMessageDetail} />
    </Grid>
  );
}
