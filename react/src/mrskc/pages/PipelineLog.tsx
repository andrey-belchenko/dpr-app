import { MasterDetail } from "devextreme-react/data-grid";
import Grid from "src/mrskc/components/Grid";
import PipelineLogDetail from "./PipelineLogDetail";


const columns = [
  {
    sortIndex: 0,
    sortOrder: "desc",
    dataField: "changedAt",
    caption: "Дата",
    dataType: "datetime",
    format: "dd.MM.yyyy HH:mm:ss"
  },
  {
    dataField: "input",
    dataType: "string",
    caption: "Вход"
  },
  {
    dataField: "output",
    dataType: "string",
    caption: "Выход"
  },
  {
    dataField: "timeMs",
    caption: "Длительность (мс)",
    dataType: "number"
  },
  {
    dataField: "count",
    caption: "К-во записей",
    dataType: "number"
  },
  {
    dataField: "error.message",
    dataType: "string",
    caption: "Ошибка",
    width: 100
  },
  {
    dataField: "processorName",
    dataType: "string",
    caption: "Сервис"
  },
  {
    dataField: "sysAction",
    dataType: "string",
    caption: "Системная операция"
  },
  {
    dataField: "src",
    dataType: "string",
    caption: "Определение"
    
  },
  {
    dataField: "_id",
    caption: "Ид записи",
    dataType: "string",
  }
];


export default function Component() {
  return (
    <Grid
      collectionName="sys_PipelineLog"
      title="Журнал операций"
      countByColumn="changedAt"
      columns={columns}
    >
      <MasterDetail enabled={true} component={PipelineLogDetail} />
    </Grid>
  );
}
