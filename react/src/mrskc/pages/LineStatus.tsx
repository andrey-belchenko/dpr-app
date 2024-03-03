import Grid from "src/common/components/grid/Grid";
const columns = [
  {
    dataField: "region",
    caption: "РЭС",
    dataType: "string",
  },
  {
    caption: "Линия",
    columns: [
      {
        dataField: "lineName",
        caption: "Наименование",
        dataType: "string",
      },
      {
        dataField: "lineCode",
        caption: "Код",
        dataType: "string",
      },
      {
        dataField: "lineStatus",
        caption: "Состояние",
        dataType: "string",
      },
    ],
  },
  {
    caption: "Сегмент",
    columns: [
      {
        dataField: "segmentName",
        caption: "Наименование",
        dataType: "string",
      },
      {
        dataField: "segmentStatus",
        caption: "Состояние",
        dataType: "string",
      },
      {
        dataField: "segmentId",
        caption: "Идентификатор",
        dataType: "string",
      },
    ],
  },
  {
    caption: "Начало",
    columns: [
      {
        dataField: "grounded1",
        caption: "Заземление",
        dataType: "string",
      },
      {
        dataField: "energized1",
        caption: "Пост. под напр.",
        dataType: "string",
      },
    ],
  },
  {
    caption: "Конец",
    columns: [
      {
        dataField: "grounded2",
        caption: "Заземление",
        dataType: "string",
      },
      {
        dataField: "energized2",
        caption: "Пост. под напр.",
        dataType: "string",
      },
    ],
  },
  {
    sortIndex: 0,
    sortOrder: "desc",
    dataField: "changedAt",
    caption: "Изменено",
    dataType: "datetime",
  },
];

export default function Component() {
  return (
    <Grid
      collectionName="view_NodesStatus"
      title="Состояние линий"
      countByColumn="region"
      columns={columns}
    />
  );
}

// changedAt: "$changedAt",
// id: "$s.id",
// deletedAt: "$deletedAt",
// region: "$r.model.IdentifiedObject_name",
// lineCode: "$lineCode",
// lineName: "$l.model.IdentifiedObject_name",
// lineStatus: statusMap("$lineStatus"),
// segmentId: "$s.id",
// segmentName: "$s.model.IdentifiedObject_name",
// segmentStatus: statusMap("$segmentStatus"),
// energized1: energizedMap("$energized1"),
// grounded1: groundedMap("$grounded1"),
// energized2: energizedMap("$energized2"),
// grounded2: groundedMap("$grounded2"),
