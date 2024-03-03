import { MasterDetail } from "devextreme-react/data-grid";
import Grid from "src/components/Grid";
import ExchangeLogDetail from "./ExchangeLogDetail";


const columns = [
  {
    sortIndex: 0,
    sortOrder: "desc",
    dataField: "changedAt",
    caption: "Дата",
    dataType: "datetime",
    format: "dd.MM.yyyy HH:mm:ss.SSS",
  },
  {
    dataField: "service",
    caption: "Сервис",
    dataType: "string",
  },
  {
    dataField: "messageType",
    caption: "Стадия",
    dataType: "string",
  },
  
  {
    caption: "Интервал",
    columns:[
      {
        dataField: "startTimestamp",
        caption: "C",
        dataType: "datetime",
        format: "dd.MM.yyyy HH:mm:ss.SSS",
      },
      {
        dataField: "endTimestamp",
        caption: "По",
        dataType: "datetime",
        format: "dd.MM.yyyy HH:mm:ss.SSS",
      }
    ]
  },
  {
    dataField: "_sid",
    caption: "Ид записи",
    dataType: "string",
  },
];

export default function Component() {
  return (
    <Grid
      collectionName="view_messageLog"
      title="Журнал информобмена"
      countByColumn="changedAt"
      columns={columns}
    >
      <MasterDetail enabled={true} component={ExchangeLogDetail} />
    </Grid>
  );
}
