import { MasterDetail } from "devextreme-react/data-grid";
import Grid from "src/components/Grid";
import WarningsDetail from "./WarningsDetail";
const columns = [
  {
    sortIndex: 0,
    sortOrder: "desc",
    dataField: "changedAt",
    caption: "Дата",
    dataType: "datetime",
  },
  {
    dataField: "message",
    caption: "Текст предупреждения",
    dataType: "string",
  },
];
export default function Component() {
  return (
    <Grid
      collectionName="sys_Warning"
      title="Предупреждения"
      countByColumn="changedAt"
      columns={columns}
    >
       <MasterDetail enabled={true} component={WarningsDetail} />
    </Grid>
  );
}
