import Button from "devextreme-react/button";
import DataGrid from "devextreme-react/data-grid";
import React, { useEffect, useRef, useState } from "react";
import ButtonCell from "src/mrskc/components/ButtonCell";
import Grid, { GridRef } from "src/common/components/grid/Grid";
import { createArrayStore, downloadFile, getArray } from "src/mrskc/data/apiClient";
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
              downloadFile(item.data.id, item.data.model.IdentifiedObject_name);
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
  {
    dataField: "item.id",
    caption: "Идентификатор",
    dataType: "string",
    filterValue: "1da54cea-368a-41f1-a93b-2da6942eac1c",
    visible: false,
  },
  // filterValue
];
export default function Component({ equipmentId, equipmentName }: any) {
  const gridRef = useRef<GridRef>(null);
  // useEffect(() => {
  //   // setTimeout(() => {
  //   //   gridRef.current?.getInstance()?.repaint();
  //   // }, 500);
  //   setTimeout(() => {
  //     gridRef.current?.getInstance()?.repaint();
  //   }, 1000);
  //   // setTimeout(() => {
  //   //   gridRef.current?.getInstance()?.repaint();
  //   // }, 1500);
  // }, [repaintTrigger]);

  let filter: any = undefined;

  if (equipmentId) {
    filter = ["item.id", "=", equipmentId];
  }

  const [data, setData] = useState<any>([]);
  useEffect(() => {
    const fetchData = async () => {
      const res = await createArrayStore("view_files","_id", {
        "item.id": equipmentId,
      });
      setData(res);
    };
    if (equipmentId) {
      fetchData();
    }
  }, [equipmentId]);

  return React.useMemo(() => {
    setTimeout(() => {
      gridRef.current?.getInstance()?.repaint();
    }, 300);

    return (
      <Grid
        key={equipmentId}
        ref={gridRef}
        title={"Файлы " + equipmentName}
        countByColumn="model.IdentifiedObject_name"
        columns={columns}
        stateSavingEnabled={false}
        filterPanel={{ visible: false }}
        groupPanel={{ visible: false }}
        dataSource={data}
        remoteOperations={false}
        refreshButtonVisible={false}
      ></Grid>
    
      
    );
  }, [equipmentId, data]);
}
