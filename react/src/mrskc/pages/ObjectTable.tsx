import Button from "devextreme-react/button";
import React from "react";
import { useCallback, useMemo, useState } from "react";
import ButtonCell from "src/mrskc/components/ButtonCell";
import Grid from "src/common/components/grid/Grid";
import PopupWindow from "src/mrskc/components/PopupWindow";
import EquipmentFiles from "./EquipmentFiles";
import { RowPreparedEvent } from "devextreme/ui/data_grid";

//   {
//     dataField: "region",
//     caption: "РЭС",
//   },
//   {
//     caption: "Корневой контейнер",
//     columns: [
//       {
//         dataField: "rcName",
//         caption: "Наименование",
//       },
//       {
//         dataField: "rcType",
//         caption: "Тип",
//       },
//       {
//         dataField: "rcCode",
//         caption: "Код СКК",
//       },
//     ],
//   },
//   {
//     caption: "Объект",
//     columns: [
//       {
//         dataField: "name",
//         caption: "Наименование",
//       },
//       {
//         dataField: "type",
//         caption: "Тип",
//       },
//       {
//         dataField: "ccsCode",
//         caption: "Код СКК",
//       },
//       {
//         dataField: "filesCount",
//         caption: "Файлы",
//         dataType: "number",
//         alignment: "left",
//         cellRender(item: any) {
//           let text = "";
//           if (item.data.filesCount) {
//             let data = item.data;
//             text = `К-во: ${data.filesCount}`;

//             return (
//               <ButtonCell text={text}>
//                 <Button
//                   icon="more"
//                   onClick={() => {
//                     showFileList(data);
//                   }}
//                 ></Button>
//               </ButtonCell>
//             );
//           } else {
//             return <div />;
//           }
//         },
//       },
//     ],
//   },
//   {
//     dataField: "entityCreatedAt",
//     caption: "Создан",
//     dataType: "datetime",
//   },
//   {
//     sortIndex: 0,
//     sortOrder: "desc",
//     dataField: "entityChangedAt",
//     caption: "Изменен",
//     dataType: "datetime",
//   },
//   {
//     dataField: "skLoadedAt",
//     caption: "Получен из СК-11",
//     dataType: "datetime",
//   },
//   {
//     dataField: "skIsActual",
//     caption: "Актуален в СК-11",
//     dataType: "boolean",
//     showEditorAlways: false,
//     trueText: "Да",
//     falseTest: "Нет",
//   },
//   {
//     dataField: "id",
//     caption: "Идентификатор",
//   },
// ];
export default function Component() {
  const [currentItem, setCurrentItem] = useState<any>({});
  const [fileListVisible, setFileListVisible] = useState(false);
  const showFileList = useCallback((item) => {
    setCurrentItem(item);
    setTimeout(() => {
      setFileListVisible(true);
    }, 300);
  }, []);



  const onRowPrepared = useCallback((e: RowPreparedEvent<any, any>) => {
    if (e.data?.entityDeletedAt) {
      e.rowElement.className += " df-row-pale";
    }
  }, []);

  const grid = React.useMemo(() => {
    let columns = [
      {
        dataField: "region",
        caption: "РЭС",
      },
      {
        caption: "Корневой контейнер",
        columns: [
          {
            dataField: "rcName",
            caption: "Наименование",
          },
          {
            dataField: "rcType",
            caption: "Тип",
          },
          {
            dataField: "rcCode",
            caption: "Код СКК",
          },
        ],
      },
      {
        caption: "Объект",
        columns: [
          {
            dataField: "name",
            caption: "Наименование",
          },
          {
            dataField: "type",
            caption: "Тип",
          },
          {
            dataField: "ccsCode",
            caption: "Код СКК",
          },
          {
            dataField: "filesCount",
            caption: "Файлы",
            dataType: "number",
            alignment: "left",
            cellRender(item: any) {
              let text = "";
              if (item.data.filesCount) {
                let data = item.data;
                text = `К-во: ${data.filesCount}`;
                return (
                  <ButtonCell text={text}>
                    <Button
                      icon="more"
                      onClick={() => {
                        showFileList(data);
                      }}
                    ></Button>
                  </ButtonCell>
                );
              } else {
                return <div />;
              }
            },
          },
        ],
      },
      {
        dataField: "entityCreatedAt",
        caption: "Создан",
        dataType: "datetime",
      },
      {
        sortIndex: 0,
        sortOrder: "desc",
        dataField: "entityChangedAt",
        caption: "Изменен",
        dataType: "datetime",
      },
      {
        dataField: "skLoadedAt",
        caption: "Получен из СК-11",
        dataType: "datetime",
      },
      {
        dataField: "skIsActual",
        caption: "Актуален в СК-11",
        dataType: "boolean",
        showEditorAlways: false,
        trueText: "Да",
        falseTest: "Нет",
      },
      {
        dataField: "entityDeletedAt",
        caption: "Удален",
        dataType: "datetime",
      },
      {
        dataField: "id",
        caption: "Идентификатор",
      },
    ];

    return (
      <Grid
        collectionName="view_objectTable"
        title="Оборудование и контейнеры"
        columns={columns}
        onRowPrepared={onRowPrepared}
      ></Grid>
    );
  }, []);

  return (
    <React.Fragment>
      {grid}
      <PopupWindow
        width={1000}
        height={600}
        title={"Файлы " + currentItem?.name}
        visible={fileListVisible}
        onHiding={() => setFileListVisible(false)}
      >
        <EquipmentFiles
          equipmentId={currentItem?.id}
          equipmentName={currentItem?.name}
        ></EquipmentFiles>
      </PopupWindow>
    </React.Fragment>
  );
}
