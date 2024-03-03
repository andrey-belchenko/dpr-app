import { MasterDetail } from "devextreme-react/data-grid";
import Grid from "src/components/Grid";
import IncomingMessageDetail from "./IncomingMessageDetail";

const columns = [
  { dataField: "payload.verb", caption: "Код события"},
  { dataField: "payload.source", caption: "Источник"  },
  { dataField: "objectId", caption: "Код объекта" },
  {
    sortIndex: 0,
    sortOrder: "desc",
    dataField: "changedAt",
    caption: "Дата",
    dataType: "datetime",
  },
  { dataField: "status", caption: "Статус" },
  { dataField: "messageId", caption: "Ид сообщения" },
];
export default function Component() {
  return (
    <Grid
      collectionName="view_incomingMessages"
      title="Входящие сообщения"
      countByColumn="payload.verb"
      columns={columns}
    >
      <MasterDetail enabled={true} component={IncomingMessageDetail} />
    </Grid>
  );
}
