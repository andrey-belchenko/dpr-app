import { MasterDetail } from "devextreme-react/data-grid";
import Grid from "src/common/components/grid/Grid";
import WarningsDetail from "./WarningsDetail";
import TopologyIntegrityCheckDetail from "./TopologyIntegrityCheckDetail";
const columns = [
  {
    dataField: "dataSet",
    caption: "Набор данных",
    dataType: "string",
  },
  {
    dataField: "line",
    caption: "Код линии",
    dataType: "string",
  },
  {
    dataField: "code",
    caption: "Код объекта",
    dataType: "string",
  },
  {
    dataField: "issue",
    caption: "Проблема",
    dataType: "string",
  },
  {
    dataField: "value",
    caption: "Значение",
    dataType: "string",
  },
];

export default function Component() {
  return (
    <Grid
      collectionName="view_topology_integrityCheck"
      title="Проверка целостности данных"
      countByColumn="dataSet"
      columns={columns}
    >
       <MasterDetail enabled={true} component={TopologyIntegrityCheckDetail} />
    </Grid>
  );
}
