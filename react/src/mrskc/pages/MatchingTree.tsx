import React, { useState, useEffect, useRef, useCallback } from "react";

import { createDataSource } from "src/common/data/mongo-api";
// import { TreeList } from "devextreme-react";
import TreeList, { Column, Editing } from "devextreme-react/tree-list";
import Button from "devextreme-react/button";
import Tree, { TreeRef } from "src/mrskc/components/Tree";
import Splitter from "src/mrskc/components/Splitter";
import Toolbar, { Item } from "devextreme-react/toolbar";
import Box, { Item as BoxItem } from "devextreme-react/box";
// import { Switch } from "devextreme-react/switch";
import { CheckBox } from "devextreme-react/check-box";
import "./Form.scss";

import * as utils from "./matchingUtils";
import { BooleanParam, useQueryParam } from "use-query-params";
import DialogOkCancel from "src/common/components/dialog/DialogOkCancel";
import DialogOk from "src/common/components/dialog/DialogOk";
import { sendModifyRequest } from "src/common/data/mongo-api";
import { RowPreparedEvent } from "devextreme/ui/tree_list";
const matchingColumns = [
  {
    dataField: "name",
    caption: "КИСУР: Наименование",
    dataType: "string",
    allowEditing: false,
    fixed: true,
    fixedPosition: "left",
    selectedFilterOperation: "=",
    sortIndex: 0,
    sortOrder: "asc",
  },
  {
    dataField: "status",
    caption: "Статус",
    dataType: "string",
    allowEditing: false,
    selectedFilterOperation: "=",
  },
  {
    dataField: "type",
    caption: "Тип объекта",
    dataType: "string",
    allowEditing: false,
    selectedFilterOperation: "=",
  },
  {
    dataField: "id",
    caption: "Код СКК",
    dataType: "string",
    allowEditing: false,
    selectedFilterOperation: "=",
  },
  {
    dataField: "allowCreate",
    caption: "Разрешить создание",
    dataType: "boolean",
    allowEditing: true,
    width: 100,
    selectedFilterOperation: "=",
  },
  {
    dataField: "platformId",
    caption: "Идентификатор СК-11",
    dataType: "string",
    allowEditing: true,
    selectedFilterOperation: "=",
  },
  {
    dataField: "lastBlocked",
    caption: "Дата сообщения",
    dataType: "datetime",
    sortOrder: "desc",
    allowEditing: false,
  },
  {
    dataField: "changedAt",
    caption: "Изменен",
    dataType: "datetime",
    allowEditing: false,
  },
];

const modelColumns = [
  {
    sortIndex: 0,
    sortOrder: "asc",
    dataField: "name",
    dataType: "string",
    caption: "Модель: Наименование",
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
    dataField: "id",
    caption: "Идентификатор",
    dataType: "string",
  },
];

export default function Component() {
  // const [horizontal, setHorizontal] = useState(false);
  // const [pickingEnabled, setPickingEnabled] = useState(false);

  const [horizontal, setHorizontal] = useQueryParam("horizontal", BooleanParam);
  const [pickingEnabled, setPickingEnabled] = useQueryParam(
    "pickingEnabled",
    BooleanParam
  );
  const matchingTreeRef = useRef<TreeRef>(null);
  const modelTreeRef = useRef<TreeRef>(null);
  const refreshData = useCallback(() => {
    matchingTreeRef.current?.getInstance()?.refresh();
    modelTreeRef.current?.getInstance()?.refresh();
  }, []);

  const [focused] = useState<any>({});
  const setMatchItem = (value: any) => {
    focused.matched = value;
  };
  const getMatchItem = () => {
    return focused.matched;
  };

  const setModelItem = (value: any) => {
    focused.model = value;
  };
  const getModelItem = () => {
    return focused.model;
  };

  const setMatchRowIndex = (value: any) => {
    focused.matchRowIndex = value;
  };
  const getMatchRowIndex = () => {
    return focused.matchRowIndex;
  };

  const findInModel = useCallback(() => {
    setPickingEnabled(true);
    if (getMatchItem()?.platformId) {
      modelTreeRef.current?.setFocusedRow(getMatchItem().platformId);
    }
  }, []);

  const repaint = () => {
    setTimeout(() => {
      matchingTreeRef.current?.getInstance()?.repaint();
      modelTreeRef.current?.getInstance()?.repaint();
    }, 0);
  };

  const repaintModel = () => {
    const matchItem = getMatchItem();
    setTimeout(() => {
      // const matchItem1 = getMatchItem();
      modelTreeRef.current?.getInstance()?.repaint();
    }, 0);
  };

  const changeHorizontal = (value: boolean) => {
    setHorizontal(value);
    repaint();
  };

  const changePickingEnabled = (value: boolean) => {
    setPickingEnabled(value);
    repaint();
  };

  const onMatchFocusedRowChanged = useCallback((e) => {
    if (e.row) {
      setMatchItem(e.row.data);
      setMatchRowIndex(e.rowIndex);
      repaintModel();
    }
  }, []);

  const onModelFocusedRowChanged = useCallback((e) => {
    if (e.row) {
      setModelItem(e.row.data);
    }
  }, []);

  const onMatchingRowPrepared = useCallback((e: RowPreparedEvent<any, any>) => {
    if (!utils.isNeedMatch(e.data)) {
      e.rowElement.className += " df-row-pale";
    }
  }, []);

  const onModelRowPreparedAction = useCallback(
    (e: RowPreparedEvent<any, any>) => {
      let isAllowed = false;
      const matchItem = getMatchItem();
      if (e.data) {
        if (utils.isNeedMatch(matchItem)) {
          if (!matchItem.type || e.data.type == matchItem.type) {
            if (!e.data.ccsCode || e.data.ccsCode == matchItem.id) {
              isAllowed = true;
            }
          }
        }
        if (e.data.id == matchItem?.platformId) {
          e.rowElement.className += " df-row-matched";
        } else {
          e.rowElement.className += " df-row-not-matched";
        }
      }

      if (e.data && !isAllowed) {
        e.rowElement.className += " df-row-pale";
      }
    },
    []
  );

  const onModelRowPrepared = (e: RowPreparedEvent<any, any>) => {
    onModelRowPreparedAction(e);
  };
  const matchingTree = React.useMemo(() => {
    return (
      <Tree
        ref={matchingTreeRef}
        collectionName="sys_model_ExtraIdMatching"
        title="Сопоставление ключей"
        dataSourceOptions={{ idField: "id", ...utils.dataSourceOptions }}
        columns={matchingColumns}
        // stateSaveEnabled={false}
        onEditorPreparing={utils.onEditorPreparing}
        onRowPrepared={onMatchingRowPrepared}
        toolbarVisible={false}
        gridId="matchingTree"
        onFocusedRowChanged={onMatchFocusedRowChanged}
      >
        <Editing mode="cell" allowUpdating={true} />
      </Tree>
    );
  }, []);

  const modelTree = React.useMemo(() => {
    return (
      <Tree
        ref={modelTreeRef}
        collectionName="view_objectTreeWithStat"
        title="Дерево объектов"
        dataSourceOptions={{ idField: "id" }}
        columns={modelColumns}
        toolbarVisible={false}
        gridId="modelTree"
        onFocusedRowChanged={onModelFocusedRowChanged}
        onRowPrepared={onModelRowPrepared}
      ></Tree>
    );
  }, []);
  const content = () => {
    let components = [];
    components = [
      <Splitter horizontal={horizontal} onlyFirst={!pickingEnabled}>
        {matchingTree}
        {modelTree}
      </Splitter>,
    ];
    return components;
  };

  const [dialogOkCancelVisible, setDialogOkCancelVisible] = useState(false);
  const [dialogOkCancelText, setDialogOkCancelText] = useState("");
  const [dialogOkVisible, setDialogOkVisible] = useState(false);
  const [dialogOkText, setDialogOkText] = useState("");

  const saveMatching = useCallback(() => {
    

    matchingTreeRef.current
      ?.update(getMatchItem().id, { platformId: getModelItem().id })
      .then((res) => {
        let tree = matchingTreeRef.current!!.getInstance()!!;
        let item = tree.getNodeByKey(getMatchItem().id).data;
        setMatchItem(item);
        repaint();
      });

    // tree?.cellValue(getMatchRowIndex(), "platformId", getModelItem().id);
    // tree?.saveEditData();
    // .then(() => {
    //   repaint();
    // });
    setDialogOkCancelVisible(false);
  }, []);

  const matchClick = useCallback(() => {
    if (getMatchItem().platformId == getModelItem().id) {
      setDialogOkText("Выбранные записи уже сопоставлены");
      setDialogOkVisible(true);
      return;
    }

    if (getMatchItem().type && getMatchItem().type != getModelItem().type) {
      setDialogOkText("Типы объектов не совпадают");
      setDialogOkVisible(true);
      return;
    }

    if (getModelItem().ccsCode && getModelItem().ccsCode != getMatchItem().id) {
      setDialogOkCancelText("Не совпадают коды СКК. Выполнить сопоставление?");
      setDialogOkCancelVisible(true);
      return;
    }

    if (getMatchItem().platformId) {
      setDialogOkCancelText("Объект уже сопоставлен. Изменить привязку?");
      setDialogOkCancelVisible(true);
      return;
    }
    saveMatching();
  }, []);

  return (
    <React.Fragment>
      <DialogOkCancel
        width={300}
        height={200}
        title="Сопоставление ключей"
        visible={dialogOkCancelVisible}
        onOk={saveMatching}
        onCancel={() => setDialogOkCancelVisible(false)}
      >
        {dialogOkCancelText}
      </DialogOkCancel>
      <DialogOk
        width={300}
        height={200}
        title="Сопоставление ключей"
        visible={dialogOkVisible}
        onOk={() => setDialogOkVisible(false)}
      >
        {dialogOkText}
      </DialogOk>
      <Box direction="col" className="df-form-box">
        {/* TODO: хорошо бы избавиться от явного определения размера 39 */}
        <BoxItem ratio={0} baseSize={39}>
          <Toolbar className="df-toolbar">
            <Item location="before">
              <Button icon="refresh" text="Обновить" onClick={refreshData} />
            </Item>
            <Item location="before">
              <Button
                icon="find"
                text="Найти в модели"
                disabled={!getMatchItem()?.platformId}
                onClick={findInModel}
              />
            </Item>
            <Item location="after" visible={!!pickingEnabled}>
              <Button
                icon="chevrondown"
                disabled={
                  !utils.isNewMatchAllowed(getMatchItem()) || !getModelItem()
                }
                text="Сопоставить"
                onClick={matchClick}
              />
            </Item>
            <Item location="after"></Item>
            <Item location="after">
              <CheckBox
                text="Режим выбора"
                value={!!pickingEnabled}
                onValueChanged={(e) => changePickingEnabled(e.value)}
              />
            </Item>
            <Item location="after"></Item>
            <Item location="after" visible={!!pickingEnabled && !!horizontal}>
              <Button
                type="default"
                icon="columnfield"
                hint="Вертикально"
                onClick={() => changeHorizontal(false)}
              />
            </Item>
            <Item location="after" visible={!!pickingEnabled && !horizontal}>
              <Button
                type="default"
                icon="rowfield"
                hint="Горизонтально"
                stylingMode="contained"
                onClick={() => changeHorizontal(true)}
              />
            </Item>
          </Toolbar>
        </BoxItem>
        <BoxItem ratio={1}>{content()}</BoxItem>
      </Box>
    </React.Fragment>
  );
}
