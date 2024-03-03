import { MasterDetail } from "devextreme-react/data-grid";
import Grid from "src/common/components/grid/Grid";
import ErrorsDetail from "./ErrorsDetail";

// const serviceName=(rowData:any)=> {
//   const value = rowData.appName;
//   let text = value
//   switch(value) {
//     case "sk11-outgoing": {
//        text = "СК-11 исходящие"
//        break;
//     }
//     default: {
//        break;
//     }
//  }
//  return text;
// }

const columns = [
  {
    sortIndex: 0,
    sortOrder: "desc",
    dataField: "timestamp",
    caption: "Дата",
    dataType: "datetime",
    format: "dd.MM.yyyy HH:mm:ss",
  },
  {
    dataField: "appName",
    caption: "Сервис",
  },
  {
    dataField: "message",
    caption: "Ошибка",
    width: 600,
  },
  {
    dataField: "_id",
    caption: "Ид записи",
    dataType: "string",
  },
];

export default function Component() {
  return (
    <Grid
      collectionName="sys_Errors"
      title="Журнал ошибок"
      countByColumn="timestamp"
      columns={columns}
      allowColumnResizing={true}
    >
      <MasterDetail enabled={true} component={ErrorsDetail} />
    </Grid>
  );
}
