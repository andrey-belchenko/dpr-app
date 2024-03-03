import { MasterDetail } from "devextreme-react/data-grid";
import Grid from "src/common/components/grid/Grid";
import NotificationsDetails from "./NotificationsDetails";
const columns = [
  {
    sortIndex: 0,
    sortOrder: "desc",
    dataField: "changedAt",
    caption: "Дата",
    dataType: "datetime",
  },
  {
    dataField: "subject",
    caption: "Заголовок",
    dataType: "string",
  },
  {
    dataField: "recipients",
    caption: "Получатели",
    dataType: "string",
  },
  {
    dataField: "isSent",
    caption: "Отправлено",
    dataType: "boolean",
  }
];
export default function Component() {
  return (
    <Grid
      collectionName="out_notifications"
      title="Уведомления"
      countByColumn="changedAt"
      columns={columns}
    >
       {/* <MasterDetail enabled={true} component={NotificationsDetails} /> */}
    </Grid>
  );
}
