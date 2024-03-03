import React, { useCallback, useEffect, useRef, useState } from "react";

// import { TreeList } from "devextreme-react";
import TreeList, {
  Column,
  FilterRow,
  HeaderFilter,
  SearchPanel,
  RemoteOperations,
  FilterPanel,
  Toolbar,
  Item,
  Paging,
  Scrolling,
  StateStoring,
  Sorting,
  ColumnFixing,
} from "devextreme-react/tree-list";
import Button from "devextreme-react/button";

import { createDataSource } from "src/data/apiClient";
// import Button from "devextreme-react/button";

import dxTreeList from "devextreme/ui/tree_list";
import { customProcessColumns, processColumns } from "src/utils/grid-columns";

// Child component
interface TreeCoreProps {
  collectionName: string;
  dataSourceOptions?: any;
  title: string;
  columns?: any[];
  toolbarItemsRightBefore?: any[];
  toolbarItemsRightAfter?: any[];
  onEditorPreparing?: any;
  onFocusedRowChanged?: any;
  onContentReady?: any;
  onRowPrepared?: any;
  saveState: (state: any) => void;
  toolbarVisible?: boolean;
  filterPanelVisible?: boolean;
  children: any;
  // initialized: boolean;
}

export interface TreeCoreRef {
  getInstance: () => dxTreeList<any, any> | undefined;
  update: (key: any, values: any) => Promise<any>;
  // setFocusedRow: (key: any) => void;
  // getCurrentDataItem: () => any | undefined;
}

// Можно передавать настройки колонок напрямую в свойство TreeList, но так почему то не работает опция renderCell на колонке
// TODO Возможно требуется какая-то оптимизация , чтобы исключить лишние вызовы
export const createColumns = (columns?: any[]) => {
  const colElements: any[] = [];
  if (columns) {
    processColumns(columns);
    processTreeColumns(columns);
    for (let col of columns) {
      colElements.push(<Column {...col}></Column>);
    }
  }
  return colElements;
};

const processTreeColumns = (columns?: any[]): boolean => {
  let changes = false;
  customProcessColumns(columns, (col) => {
    if (col.filterValue == undefined) {
      if (col.selectedFilterOperation != "=") {
        col.selectedFilterOperation = "=";
        changes = true;
      }
    }
  });
  return changes;
};
// заморочки с разделением на 2 компонента и React.memo связаны с тем,   что иначе от TreeList идет запрос на сервер при любом изменении state (своего или вышестоящих компонентов) визуально проявлялось например в появлении индикатора загрузки при смене фокуса строки
const component = React.forwardRef<TreeCoreRef, TreeCoreProps>((props, ref) => {
  const gridRef = useRef<TreeList>(null);

  let dummyData: any = {};
  dummyData[props?.dataSourceOptions?.idField ?? "_id"] = "1";
  // const [data, setData] = useState<any>([dummyData]);

  // useEffect(() => {
  //   if (props.initialized) {
  //     gridRef.current!.instance.endUpdate();
  //     setData(createDataSource(props.collectionName, props.dataSourceOptions));
  //   }
  // }, [props.initialized]);


  const [data, setData] = useState<any>(createDataSource(props.collectionName, props.dataSourceOptions));


  const getInstance = (): dxTreeList<any, any> | undefined => {
    return gridRef.current?.instance;
  };

  const update = (key: any, values: any) => {
    const index = gridRef.current!!.instance.getRowIndexByKey(key);
    for (let name in values) {
      gridRef.current!!.instance.cellValue(index, name, values[name]);
    }
    return gridRef.current!!.instance.saveEditData();

    // return data.update(key, values);
  };

  React.useImperativeHandle(ref, () => ({
    getInstance,
    update,
    // setFocusedRow,
    // getCurrentDataItem,
  }));

  const refreshGrid = useCallback(() => {
    if (gridRef.current) {
      gridRef.current.instance.refresh();
    }
  }, []);

  const handleOptionChanged = (e: any) => {
    // чтобы при сбросе фильтра устанавливалась дефолтная операция
    if (e.name === "filterValue" && e.value === null && gridRef.current) {
      let columns = gridRef.current.instance.option("columns");
      if (processTreeColumns(gridRef.current.instance.option("columns"))) {
        gridRef.current.instance.option("columns", columns);
      }
    }
  };

  const treeColumns = React.useMemo(() => {
    return createColumns(props.columns);
  }, []);

  // const [isFistLoad, setIsFirstLoad] = useState(true);

  return (
    <TreeList
      ref={gridRef}
      className={"treeList"}
      dataSource={data}
      showBorders={false}
      defaultFocusedRowIndex={0}
      columnAutoWidth={true}
      columnHidingEnabled={false}
      showColumnLines={true}
      hoverStateEnabled={true}
      onOptionChanged={handleOptionChanged}
      autoNavigateToFocusedRow={false}
      // visible={props.initialized}
      // onInitialized={(e) => {
      //   // e.component?.beginUpdate();
      // }}

      
      // repaintChangesOnly={true}
      // columns={props.columns}
      onEditorPreparing={props.onEditorPreparing}
      onRowPrepared={props.onRowPrepared}
      // onRowPrepared={(e) => {
      //   if (!e.rowElement.classList.contains("df-row")) {
      //     e.rowElement.className += " df-row";
      //   }
      // }}
      keyExpr="id"
      parentIdExpr="parent"
      hasItemsExpr="hasChildren"
      autoExpandAll={false}
      rootValue={null}
      focusedRowEnabled={true}
      filterMode="fullBranch"
      onFocusedRowChanged={(e) => {
        // setCurrentDataItem(e.row?.data);
        if (props.onFocusedRowChanged) {
          props.onFocusedRowChanged(e);
        }
      }}
      // remoteOperations="auto"
      remoteOperations={{ filtering: true, sorting: true, grouping: true }}
      onContentReady={props.onContentReady}
      // onContentReady={(props) => {
      //   let state = props.component.state();
      //   if (state.focusedRowKey /* && isFistLoad*/) {
      //     // props.component.beginUpdate();
      //     // setIsFirstLoad(false);
      //     setTimeout(() => {
      //       props.component.navigateToRow(state.focusedRowKey);
      //     }, 300);
      //     // .then(()=>{
      //     //   props.component.endUpdate();
      //     // });
      //   }
      // }}
      // focusedRowKey={focusedRowKey}
      // filterMode = "matchOnly"
      // remoteOperations={{ filtering: true }}
    >
      <ColumnFixing enabled={true} />
      <Paging enabled={false} />
      <FilterRow visible={true} />
      <Scrolling
        useNative={true}
        // scrollByContent={true}
        // scrollByThumb={true}
        // showScrollbar="always"
        mode="virtual"
      />
      <HeaderFilter visible={true} />
      <FilterPanel visible={props.filterPanelVisible !== false} />
      {/* <Summary>
          <TotalItem column={props.countByColumn} summaryType="count" />
          <GroupItem column={props.countByColumn} summaryType="count" />
        </Summary> */}
      <Toolbar visible={props.toolbarVisible !== false}>
        <Item location="before">
          <Button icon="refresh" text="Обновить" onClick={refreshGrid} />
        </Item>
        {props.toolbarItemsRightBefore}
        {/* <Item location="after">
          <Button icon="export" text="Экспорт" />
        </Item> */}
        {props.toolbarItemsRightAfter}
      </Toolbar>
      <StateStoring
        enabled={true}
        type="custom"
        customSave={props.saveState}
        savingTimeout={300}
      />
      <Sorting mode="multiple" />
      {props.children}
      {/* {createColumns(props.columns)} */}
      {treeColumns}
    </TreeList>
  );
});

// export default component;

export default React.memo(component);
