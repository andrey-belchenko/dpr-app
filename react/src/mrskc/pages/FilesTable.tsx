import Button from "devextreme-react/button";
import ButtonCell from "src/mrskc/components/ButtonCell";
import Grid from "src/common/components/grid/Grid";
import { downloadFile } from "src/common/data/apiClient";
const columns = [
  {
    // width:72,
    fixed: true,
    fixedPosition: "left",
    width: 72,
    cellRender(item: any) {
      return (
        <ButtonCell>
          <Button
            icon="download"
            hint="Скачать"
            onClick={async () => {
               downloadFile(
                item.data.id,
                item.data.model.IdentifiedObject_name
              );
            }}
          ></Button>
          <Button
            icon="file"
            hint="Открыть в браузере"
            onClick={async () => {
               downloadFile(
                item.data.id,
                item.data.model.IdentifiedObject_name,
                true
              );
            }}
          ></Button>
        </ButtonCell>
      );
    },
  },

  {
    caption: "Файл",
    columns: [
      {
        dataField: "model.IdentifiedObject_name",
        caption: "Имя",
        dataType: "string",
      },
      {
        dataField: "model.PsrFile_description",
        caption: "Описание",
        dataType: "string",
      },
      {
        dataField: "model.PsrFile_docNumber",
        caption: "Код документа",
        dataType: "string",
      },
    ],
  },
  {
    dataField: "item.region",
    caption: "РЭС",
    dataType: "string",
  },
  {
    caption: "Корневой контейнер",
    columns: [
      {
        dataField: "item.rcName",
        caption: "Наименование",
        dataType: "string",
      },
      {
        dataField: "item.rcType",
        caption: "Тип",
        dataType: "string",
      },
      {
        dataField: "item.rcCode",
        caption: "Код СКК",
        dataType: "string",
      },
    ],
  },
  {
    caption: "Объект",
    columns: [
      {
        dataField: "item.name",
        caption: "Наименование",
        dataType: "string",
      },
      {
        dataField: "item.type",
        caption: "Тип",
        dataType: "string",
      },
      {
        dataField: "item.ccsCode",
        caption: "Код СКК",
        dataType: "string",
      },
      {
        sortIndex: 0,
        sortOrder: "desc",
        dataField: "item.entityChangedAt",
        caption: "Изменен",
        dataType: "datetime",
      },
      {
        dataField: "item.id",
        caption: "Идентификатор",
        dataType: "string",
      },
    ],
  },
];
export default function Component() {
  return (
    <Grid
      collectionName="view_files"
      title="Файлы из КИСУР"
      countByColumn="model.IdentifiedObject_name"
      columns={columns}
    ></Grid>
  );
}
