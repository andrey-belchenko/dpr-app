
import Grid from "src/mrskc/components/Grid";
const columns = [
  
  {
    dataField: "processor",
    caption: "Сервис",
    dataType: "string",
  },
  {
    dataField: "status",
    caption: "Состояние",
    dataType: "string",
  },
  {
    dataField: "changedAt",
    caption: "Изменен",
    dataType: "datetime",
  },
];

export default function Component() {
  return (
    <Grid
      collectionName="view_processorStatus"
      title="Состояние сервисов"
      countByColumn="processor"
      columns={columns}
    >
   
    </Grid>
  );
}
