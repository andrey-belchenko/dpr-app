import React, {
  useState,
  useEffect,
  useRef,
  useCallback,
  useMemo,
} from "react";

import { createDataSource } from "src/common/data/apiClient";
// import { TreeList } from "devextreme-react";
import TreeList, { Column, Item } from "devextreme-react/tree-list";
import Tree from "src/mrskc/components/Tree";
import Button from "devextreme-react/button";
import ButtonCell from "src/mrskc/components/ButtonCell";
import PopupWindow from "src/mrskc/components/PopupWindow";
import { FilesTable } from "./_index";
import EquipmentFiles from "./EquipmentFiles";
import { RowPreparedEvent } from "devextreme/ui/tree_list";

// const columns = [
//     {
//       sortIndex: 0,
//       sortOrder: "asc",
//       dataField: "name",
//       dataType: "string",
//       caption: "Наименование",
//       fixed: true,
//       fixedPosition: "left",
//     },
//     {
//       dataField: "type",
//       caption: "Тип",
//       dataType: "string",
//     },
//     {
//       dataField: "ccsCode",
//       caption: "Код СКК",
//       dataType: "string",
//     },
//     {
//       dataField: "filesCount",
//       caption: "Файлы Файлы Файлы",
//       dataType: "number",
//       alignment: "left",
//       cellRender(item: any) {
//         let text = "";
//         if (item.data.filesCount) {
//           text = `К-во: ${item.data.filesCount}`;
//           return (
//             <ButtonCell text={text}>
//               <Button
//                 icon="more"
//                 // onClick={() => setFileListVisible(true)}
//               ></Button>
//             </ButtonCell>
//           );
//         } else {
//           return <div />;
//         }
//       },
//     },
//     {
//       dataField: "entityCreatedAt",
//       caption: "Создан",
//       dataType: "datetime",
//     },
//     {
//       dataField: "entityChangedAt",
//       caption: "Изменен",
//       dataType: "datetime",
//     },
//     {
//       dataField: "skLoadedAt",
//       caption: "Получен из СК-11",
//       dataType: "datetime",
//     },
//     {
//       dataField: "skIsActual",
//       caption: "Актуален в СК-11",
//       dataType: "boolean",
//       showEditorAlways: false,
//       trueText: "Да",
//       falseTest: "Нет",
//     },
//     {
//       dataField: "id",
//       caption: "Идентификатор",
//       dataType: "string",
//     },
//     {
//       dataField: "baseCode",
//       caption: "Базовый Код",
//       dataType: "string",
//     },
//   ];

export default function Component() {
  const [currentItem, setCurrentItem] = useState<any>({});
  const [fileListVisible, setFileListVisible] = useState(false);
  const showFileList = useCallback((item) => {
    setCurrentItem(item);
    setTimeout(() => {
      setFileListVisible(true);
    }, 300);
  }, []);

  const columns = useMemo(() => {
    return [
      {
        sortIndex: 0,
        sortOrder: "asc",
        dataField: "name",
        dataType: "string",
        caption: "Наименование",
        fixed: true,
        fixedPosition: "left",
      },
      {
        dataField: "type",
        caption: "Тип",
        dataType: "string",
      },
      {
        dataField: "ccsCode",
        caption: "Код СКК",
        dataType: "string",
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
      {
        dataField: "entityCreatedAt",
        caption: "Создан",
        dataType: "datetime",
      },
      {
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
        dataType: "string",
      },
      {
        dataField: "baseCode",
        caption: "Базовый Код",
        dataType: "string",
      },
    ];
  }, []);

  const onRowPrepared = useCallback((e: RowPreparedEvent<any, any>) => {
    if (e.data?.entityDeletedAt) {
      e.rowElement.className += " df-row-pale";
    }
  }, []);
  const tree = React.useMemo(() => {
    return (
      <Tree
        collectionName="view_objectTreeWithStat"
        title="Дерево объектов"
        dataSourceOptions={{ idField: "id" }}
        columns={columns}
        onRowPrepared={onRowPrepared}
      ></Tree>
    );
  }, []);

  return (
    <React.Fragment>
      {tree}
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
