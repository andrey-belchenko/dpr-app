
import  {
  MasterDetail,
} from "devextreme-react/data-grid";
import BlockedMessageDetail from "./BlockedMessageDetail";
import Grid from "src/mrskc/components/Grid";

const columns = [
  { dataField: "idSource", caption: "Источник" },
  { dataField: "eventId", caption: "Код события" },
  { dataField: "objectId", caption: "Код объекта" },
  {
    sortIndex: 0,
    sortOrder: "desc",
    dataField: "messageChangedAt",
    caption: "Дата сообщения",
    dataType: "datetime",
  },
  { dataField: "status", caption: "Статус" },
  {
    caption: "Объекты",
    columns: [
      {
        dataField: "blockedEntitiesCount",
        caption: "Отложено",
        dataType: "number",
      },
      {
        dataField: "matchedEntitiesCount",
        caption: "Сопоставлено",
        dataType: "number",
      },
    ],
  },
  {
    dataField: "processedAt",
    caption: "Дата обработки",
    dataType: "datetime",
  },
  { dataField: "blockedMessageId", caption: "Ид сообщения" },
];
export default function Component() {
  return (
    <Grid
      collectionName="view_blockedDto"
      title="Отложенные сообщения"
      countByColumn="idSource"
      columns={columns}
    >
      <MasterDetail enabled={true} component={BlockedMessageDetail} />
    </Grid>
  );
}
