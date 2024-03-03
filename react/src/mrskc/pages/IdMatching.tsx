import React, { useEffect, useRef, useState } from "react";
import Button from "devextreme-react/button";
import { Editing, Item } from "devextreme-react/data-grid";
import {
  sendModifyRequest,
  sendModifyRequestGetJson,
} from "src/mrskc/data/apiClient";
import { Workbook } from "exceljs";
import notify from "devextreme/ui/notify";
import DialogOkCancel from "../components/DialogOkCancel";
import { v4 as uuidv4 } from "uuid";
import ButtonWithHint from "src/mrskc/components/ButtonWithHint";
import DialogOk from "src/mrskc/components/DialogOk";
import Grid, { GridRef } from "src/mrskc/components/Grid";
import * as utils from "./matchingUtils";
import { getHostUrl } from "src/common/utils/host-url";
const columns = [
  {
    dataField: "name",
    caption: "Наименование КИСУР",
    dataType: "string",
    allowEditing: false,
  },
  {
    dataField: "type",
    caption: "Тип объекта",
    dataType: "string",
    allowEditing: false,
  },
  {
    dataField: "id",
    caption: "Код СКК",
    dataType: "string",
    allowEditing: false,
    sortIndex: 1,
    sortOrder: "asc",
  },
  {
    dataField: "allowCreate",
    caption: "Разрешить создание",
    dataType: "boolean",
    allowEditing: true,
  },
  {
    dataField: "platformId",
    caption: "Идентификатор СК-11",
    dataType: "string",
    allowEditing: true,
  },
  {
    dataField: "platformName",
    caption: "Наименование СК-11",
    dataType: "string",
    allowEditing: false,
  },
  {
    dataField: "status",
    caption: "Статус",
    dataType: "string",
    allowEditing: false,
  },
  {
    dataField: "lastBlocked",
    caption: "Дата сообщения",
    dataType: "datetime",
    allowEditing: false,
    sortIndex: 0,
    sortOrder: "desc",
  },
  {
    dataField: "changedAt",
    caption: "Изменен",
    dataType: "datetime",
    allowEditing: false,
  },
];

export default function Component() {
  const gridRef = useRef<GridRef>(null);

  const fileInputRef = useRef<HTMLInputElement>(null);
  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target && e.target.files && e.target.files[0]) {
      gridRef.current?.getInstance()?.beginCustomLoading("");
      const file = e.target.files[0];
      const reader = new FileReader();
      let count = 0;
      reader.onload = (event: ProgressEvent<FileReader>) => {
        const data = new Uint8Array(event.target!.result as ArrayBuffer);
        const workbook = new Workbook();
        const columns = [
          "Код СКК",
          "Идентификатор СК-11",
          "Разрешить создание",
        ];
        const columnIndexes: any = {};

        workbook.xlsx.load(data).then(() => {
          const worksheet = workbook.getWorksheet(1);
          const dataFromFile: any[] = [];

          let colINdex = 1;
          let hasCol = true;
          while (hasCol) {
            let colName = worksheet!.getCell(1, colINdex).value?.toString();
            if (colName) {
              hasCol = true;
              if (columns.indexOf(colName) > -1) {
                columnIndexes[colName] = colINdex;
              }
            } else {
              hasCol = false;
            }
            colINdex++;
          }
          // не работает 0 вызовов callback
          // worksheet.eachColumnKey((col, index) => {
          //   let colName = worksheet.getCell(1, index).value?.toString();
          //   if (colName && columns.indexOf(colName) > -1) {
          //      columnIndexes[colName] = index
          //   }
          // });
          worksheet!.eachRow((row, rowNumber) => {
            if (rowNumber > 1) {
              const rowData: any = {};
              for (let colName in columnIndexes) {
                rowData[colName] = row.getCell(columnIndexes[colName]).value;
              }
              dataFromFile.push(rowData);
            }
          });

          // console.log(dataFromFile);

          var updateData: any[] = [];
          var ids: string[] = [];
          for (let row of dataFromFile) {
            if (row["Код СКК"]) {
              let platformId: string | undefined =
                row["Идентификатор СК-11"]?.toString();
              let allowCreate = row["Разрешить создание"];
              if (platformId) {
                platformId = platformId.replaceAll(" ", "");
                if (platformId == "") {
                  platformId = undefined;
                }
              }
              if (
                (platformId && !allowCreate) ||
                (!platformId && allowCreate)
              ) {
                let id = "КИСУР-" + row["Код СКК"];
                updateData.push({
                  fullId: id,
                  platformId: platformId,
                  allowCreate: allowCreate,
                });
                ids.push(id);
              }
            }
          }
          count = updateData.length;

          // let options = {
          //   commands: [
          //     {
          //       command: "merge",
          //       into: "sys_model_ExtraIdMatching",
          //       whenMatched: "merge",
          //       on: ["fullId"],
          //       data: updateData,
          //     },
          //     {
          //       command: "trigger",
          //       trigger: "trigger_IdMatching",
          //       filter: { fullId: { $in: ids } },
          //     },
          //   ],
          // };

          let options = utils.getUpdateOptions(updateData, ids);

          sendModifyRequest(options).then(() => {
            gridRef.current?.getInstance()?.endCustomLoading();
            gridRef.current?.getInstance()?.refresh();
            notify(
              "Загрузка выполнена. Количество записей:" + count,
              "success"
            );
          });
        });
        e.target.value = "";
      };
      reader.readAsArrayBuffer(file);
    }
  };

  //https://supportcenter.devexpress.com/ticket/details/t1118438/datagrid-for-devextreme-how-to-obtain-all-filtered-and-sorted-rows
  const [itemsForApply, setItemsForApply] = useState<any>({ data: [] });
  const collectItemsForApply = React.useCallback(async (): Promise<any> => {
    const grid = gridRef!!.current!!.getInstance()!!;
    grid.beginCustomLoading("");
    let filterExpr = grid.getCombinedFilter(true);
    filterExpr = [filterExpr, "and", ["status", "=", "Готов"]];
    const dataSource = grid.getDataSource();
    const result = await dataSource.store().load({
      filter: filterExpr /*, sort: loadOptions.sort, group: loadOptions.group*/,
    });
    grid.endCustomLoading();
    return result;
  }, [gridRef]);

  const [applyDialogVisible, setApplyDialogVisible] = useState(false);
  const [cantApplyDialogVisible, setCantApplyDialogVisible] = useState(false);

  const prepareLoadToModel = async () => {
    const items = await collectItemsForApply();
    if (items.data.length > 0) {
      setItemsForApply(items);
      setApplyDialogVisible(true);
    } else {
      setCantApplyDialogVisible(true);
    }
  };

  const loadToModel = async () => {
    setApplyDialogVisible(false);
    const items = await collectItemsForApply();

    let id = uuidv4();
    gridRef.current?.getInstance()!!.beginCustomLoading("");
    var updateData = [];
    for (let item of items.data) {
      updateData.push({
        fullId: item.fullId,
        status: "Обработка",
        applyOperationId: id,
      });
    }
    let options = {
      commands: [
        {
          command: "merge",
          into: "sys_applyKeysOperation",
          whenNotMatched: "insert",
          whenMatched: "merge",
          on: ["id"],
          data: [{ id: id, name: "Применение сопоставленных ключей" }],
        },
        {
          command: "merge",
          into: "sys_model_ExtraIdMatching",
          whenMatched: "merge",
          on: ["fullId"],
          data: updateData,
        },
        {
          command: "trigger",
          trigger: "trigger_ExtraMatchingApply",
          filter: { id: id },
        },
        {
          collection: "sys_applyKeysOperation",
          command: "find",
          filter: { id: id },
        },
      ],
    };

    var res = await sendModifyRequestGetJson(options);

    const item = res.data[0];
    gridRef.current?.getInstance()!!.endCustomLoading();
    gridRef.current?.getInstance()!!.refresh();

    notify(
      `Загрузка выполнена. Количество объектов: ${item.entitiesCount}. Количество сообщений: ${item.messagesCount} `,
      "success"
    );
    // sendModifyRequest(options).then((response) => {

    // });
  };

  const [host, setHost] = useState("");

  useEffect(() => {
    setHost(getHostUrl());
  }, []);

  const openHierarchy = () => {
    let url = `${host}/#/MatchingTree?`;
    let item = gridRef.current?.getCurrentDataItem();
    if (item) {
      let idParts = item.id.split("-");
      let treeState = {
        filterValue: ["id", "=", idParts[0] + "-" + idParts[1]],
        focusedRowKey: item.id,
      };
      url +=
        "matchingTreeState=" +
        encodeURIComponent(JSON.stringify(treeState)) +
        "&";
    }
    url += "&pickingEnabled=1";
    window.open(url, "_blank");
  };

  const toolbarItemsRightBefore = [
    <Item location="after">
      <Button onClick={openHierarchy} icon="hierarchy" text="Иерархия"></Button>
    </Item>,
    <Item location="after">
      <ButtonWithHint
        onClick={prepareLoadToModel}
        icon="fieldchooser"
        text="Применить"
      >
        Загрузка сопоставленных ключей со статусом "Готов" в модель.
        <br />
        Загруженные ключи будут использоваться при обработке сообщений.
        <br />
        Выбор ключей для загрузки осуществляется с учетом текущего фильтра.
      </ButtonWithHint>
    </Item>,
  ];

  const toolbarItemsRightAfter = [
    <Item location="after">
      <Button
        icon="import"
        text="Импорт"
        onClick={() => fileInputRef.current!.click()}
      />
    </Item>,
  ];

  return (
    <React.Fragment>
      <input
        type="file"
        accept=".xlsx"
        ref={fileInputRef}
        style={{ display: "none" }}
        onChange={handleFileChange}
      />
      <DialogOkCancel
        width={500}
        height={250}
        title="Применение сопоставленных ключей"
        visible={applyDialogVisible}
        onOk={loadToModel}
        onCancel={() => setApplyDialogVisible(false)}
      >
        Количество выбранных сопоставленных ключей:{" "}
        <b>{itemsForApply.data.length}</b>. <br />
        После выполнения операции изменить сопоставление для этих ключей будет
        невозможно. <br />
        Результат выполнения операции можно проанализировать в журнале. <br />
      </DialogOkCancel>
      <DialogOk
        // width={500}
        height={200}
        title="Применение сопоставленных ключей"
        visible={cantApplyDialogVisible}
        onOk={() => setCantApplyDialogVisible(false)}
      >
        Не найдены ключи в со статусом "Готов". <br />
        Проверьте настройки фильтра в таблице.
      </DialogOk>
      <Grid
        ref={gridRef}
        collectionName="sys_model_ExtraIdMatching"
        dataSourceOptions={{
          ...utils.dataSourceOptions,
        }}
        title="Сопоставление ключей"
        countByColumn="name"
        columns={columns}
        onEditorPreparing={utils.onEditorPreparing}
        toolbarItemsRightBefore={toolbarItemsRightBefore}
        toolbarItemsRightAfter={toolbarItemsRightAfter}
      >
        <Editing mode="cell" allowUpdating={true} />
      </Grid>
    </React.Fragment>
  );
}
